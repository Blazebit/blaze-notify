/*
 * Copyright 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.notify.notification.impl;

import com.blazebit.notify.actor.ActorContext;
import com.blazebit.notify.actor.ActorRunResult;
import com.blazebit.notify.actor.ScheduledActor;
import com.blazebit.notify.actor.spi.*;
import com.blazebit.notify.job.TimeFrame;
import com.blazebit.notify.job.spi.TransactionSupport;
import com.blazebit.notify.notification.*;
import com.blazebit.notify.notification.spi.NotificationScheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationSchedulerImpl implements NotificationScheduler, ClusterStateListener {

    private static final Logger LOG = Logger.getLogger(NotificationSchedulerImpl.class.getName());
    private static final String DEFAULT_ACTOR_NAME = "notificationPublisher";
    private static final int DEFAULT_PROCESS_COUNT = 10;

    private final NotificationJobContext jobContext;
    private final ActorContext actorContext;
    private final Scheduler scheduler;
    private final NotificationManager notificationManager;
    private final NotificationPublisherRunner runner;
    private final String actorName;
    private final ChannelKey<?> channelKey;
    private final int processCount;
    private final AtomicLong earliestKnownNotificationSchedule = new AtomicLong(Long.MAX_VALUE);
    private volatile ClusterNodeInfo clusterNodeInfo;
    private volatile boolean closed;

    public NotificationSchedulerImpl(NotificationJobContext jobContext, ActorContext actorContext) {
        this(jobContext, actorContext, actorContext.getService(SchedulerFactory.class), DEFAULT_ACTOR_NAME, DEFAULT_PROCESS_COUNT, null);
    }

    // TODO: Channel wise scheduling?
    public NotificationSchedulerImpl(NotificationJobContext jobContext, ActorContext actorContext, SchedulerFactory schedulerFactory, String actorName, int processCount, ChannelKey<?> channelKey) {
        this.jobContext = jobContext;
        this.actorContext = actorContext;
        this.scheduler = schedulerFactory.createScheduler(actorContext, actorName + "/scheduler");
        this.notificationManager = jobContext.getNotificationManager();
        this.runner = new NotificationPublisherRunner();
        this.actorName = actorName;
        this.processCount = processCount;
        this.channelKey = channelKey;
        actorContext.getActorManager().registerSuspendedActor(actorName, runner);
        actorContext.getService(ClusterStateManager.class).registerListener(this);
    }

    @Override
    public void onClusterStateChanged(ClusterNodeInfo clusterNodeInfo) {
        this.clusterNodeInfo = clusterNodeInfo;
        if (!closed) {
            Instant nextSchedule = notificationManager.getNextSchedule(clusterNodeInfo.getClusterPosition(), clusterNodeInfo.getClusterSize(), channelKey);
            if (nextSchedule == null) {
                resetEarliestKnownNotificationSchedule();
            } else {
                triggerNotificationScan(null, nextSchedule.toEpochMilli());
            }
        }
    }

    private void resetEarliestKnownNotificationSchedule() {
        long earliestKnownNotificationSchedule = this.earliestKnownNotificationSchedule.get();
        // Only reset the value if the currently known earliest schedule is in the past
        if (earliestKnownNotificationSchedule < System.currentTimeMillis()) {
            updateEarliestKnownNotificationSchedule(earliestKnownNotificationSchedule, Long.MAX_VALUE);
        }
    }

    @Override
    public void triggerNotificationScan(String name, long earliestNewNotificationSchedule) {
        long delayMillis = rescan(name, earliestNewNotificationSchedule);
        if (delayMillis != -1L) {
            actorContext.getActorManager().rescheduleActor(actorName, delayMillis);
        }
    }

    private long rescan(String name, long earliestNewNotificationSchedule) {
        if (!closed) {
            if (earliestNewNotificationSchedule == 0) {
                // This is special. We want to recheck schedules, no matter what
                ClusterNodeInfo clusterNodeInfo = this.clusterNodeInfo;
                Instant nextSchedule = notificationManager.getNextSchedule(clusterNodeInfo.getClusterPosition(), clusterNodeInfo.getClusterSize(), channelKey);
                // No new schedules available
                if (nextSchedule == null) {
                    resetEarliestKnownNotificationSchedule();
                    return -1L;
                }
                earliestNewNotificationSchedule = nextSchedule.toEpochMilli();
            }
            long earliestKnownNotificationSchedule = this.earliestKnownNotificationSchedule.get();
            if (earliestNewNotificationSchedule < earliestKnownNotificationSchedule) {
                // There are new notifications that should be scheduled before our known earliest notification

                // We just reschedule based on the new earliest schedule if it stays that
                if (!updateEarliestKnownNotificationSchedule(earliestKnownNotificationSchedule, earliestNewNotificationSchedule)) {
                    // A different thread won the race and apparently has a notification that should be scheduled earlier
                    return -1L;
                }
                long delayMillis = earliestNewNotificationSchedule - System.currentTimeMillis();

                delayMillis = delayMillis < 0 ? 0 : delayMillis;
                return delayMillis;
            }
        }

        return -1L;
    }

    private boolean updateEarliestKnownNotificationSchedule(long oldValue, long newValue) {
        do {
            if (earliestKnownNotificationSchedule.compareAndSet(oldValue, newValue)) {
                return true;
            }

            oldValue = earliestKnownNotificationSchedule.get();
        } while (oldValue < newValue);

        return false;
    }

    @Override
    public void stop() {
        closed = true;
        actorContext.stop();
    }

    @Override
    public void stop(long timeout, TimeUnit unit) throws InterruptedException {
        closed = true;
        actorContext.stop(timeout, unit);
    }

    private class NotificationPublisherRunner implements ScheduledActor {

        private final long maxBackOff = 10_000L; // 10 seconds by default
        private final long baseBackOff = 1_000L; // 1 second by default
        private final long rateLimitDeferSeconds = 10;
        private int retryAttempt;

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ActorRunResult work() {
            NotificationManager notificationManager = jobContext.getNotificationManager();
            TransactionSupport transactionSupport = jobContext.getTransactionSupport();
            // TODO: make configurable
            long transactionTimeout = 60_000L;
            long earliestKnownNotificationSchedule = NotificationSchedulerImpl.this.earliestKnownNotificationSchedule.get();
            ActorRunResult result = transactionSupport.transactional(jobContext, transactionTimeout, false, () -> {
                    ClusterNodeInfo clusterNodeInfo = NotificationSchedulerImpl.this.clusterNodeInfo;
                    List<Notification<?>> notificationsToSend = notificationManager.getNotificationsToSend(clusterNodeInfo.getClusterPosition(), clusterNodeInfo.getClusterSize(), processCount, channelKey);
                    int size = notificationsToSend.size();
                    if (size == 0) {
                        return ActorRunResult.suspend();
                    }
                    List<Map.Entry<Notification<?>, Future<Object>>> futures = new ArrayList<>(size);
                    Instant now = Instant.now();
                    for (int i = 0; i < size; i++) {
                        Notification<?> notification = notificationsToSend.get(i);
                        boolean future = false;
                        try {
                            Instant deadline = notification.getDeadline();
                            if (deadline != null && deadline.isBefore(Instant.now())) {
                                notification.markDeadlineReached();
                            } else {
                                Set<? extends TimeFrame> publishTimeFrames = notification.getPublishTimeFrames();
                                if (TimeFrame.isContained(publishTimeFrames, now)) {
                                    NotificationProcessor notificationProcessor = jobContext.getNotificationProcessor(notification);
                                    futures.add(new AbstractMap.SimpleImmutableEntry<>(notification, scheduler.submit(() -> notificationProcessor.process(notification, jobContext))));
                                    future = true;
                                } else {
                                    Instant nextSchedule = TimeFrame.getNearestTimeFrameSchedule(publishTimeFrames, now);
                                    if (nextSchedule == Instant.MAX) {
                                        if (LOG.isLoggable(Level.FINEST)) {
                                            LOG.log(Level.FINEST, "Dropping notification: " + notification);
                                        }
                                        notification.markDropped();
                                    } else {
                                        if (LOG.isLoggable(Level.FINEST)) {
                                            LOG.log(Level.FINEST, "Deferring notification to " + nextSchedule);
                                        }
                                        notification.markDeferred(nextSchedule);
                                    }
                                }
                            }
                        } catch (Throwable t) {
                            LOG.log(Level.SEVERE, "An error occurred in the notification processor", t);
                            notification.markFailed(t);
                        } finally {
                            if (!future) {
                                notificationManager.updateNotification(notification);
                            }
                        }
                    }

                    Instant rescheduleRateLimitTime = null;
                    for (int i = 0; i < futures.size(); i++) {
                        Map.Entry<Notification<?>, Future<Object>> entry = futures.get(i);
                        Notification<?> notification = entry.getKey();
                        Future<Object> future = entry.getValue();
                        try {
                            Object channelResult = future.get();
                            notification.markDone(channelResult);
                        } catch (NotificationRateLimitException t) {
                            LOG.log(Level.FINEST, "Deferring notification due to rate limit", t);
                            if (rescheduleRateLimitTime == null) {
                                rescheduleRateLimitTime = Instant.now().plus(rateLimitDeferSeconds, ChronoUnit.SECONDS);
                            }
                            notification.setScheduleTime(rescheduleRateLimitTime);
                        } catch (Throwable t) {
                            LOG.log(Level.SEVERE, "An error occurred in the notification processor", t);
                            notification.markFailed(t);
                        } finally {
                            notificationManager.updateNotification(notification);
                        }
                    }

                    return ActorRunResult.rescheduleIn(0);
                },
                t -> {
                    LOG.log(Level.SEVERE, "An error occurred in the notification publisher", t);
                }
            );

            if (closed) {
                return ActorRunResult.done();
            } else if (result == null) {
                // An error occurred like e.g. a TX timeout or a temporary DB issue. We do exponential back-off
                return ActorRunResult.rescheduleIn(getWaitTime(maxBackOff, baseBackOff, retryAttempt++));
            }

            retryAttempt = 0;
            if (result.isSuspend()) {
                // This will reschedule based on the next schedule
                updateEarliestKnownNotificationSchedule(earliestKnownNotificationSchedule, Long.MAX_VALUE);
                long delayMillis = rescan(null, 0L);
                if (delayMillis != -1L) {
                    return ActorRunResult.rescheduleIn(delayMillis);
                }
            }
            // NOTE: we don't need to update earliestKnownNotificationSchedule when rescheduling immediately
            return result;
        }
    }

    private static long getWaitTime(final long maximum, final long base, final long attempt) {
        final long expWait = ((long) Math.pow(2, attempt)) * base;
        return expWait <= 0 ? maximum : Math.min(maximum, expWait);
    }
}
