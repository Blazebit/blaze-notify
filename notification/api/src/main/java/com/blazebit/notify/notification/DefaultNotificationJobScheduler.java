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
package com.blazebit.notify.notification;

import com.blazebit.notify.notification.event.NotificationEventListener;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultNotificationJobScheduler implements NotificationJobScheduler {
    private static final Logger LOG = Logger.getLogger(DefaultNotificationJobScheduler.class.getName());
    private static final int DEFAULT_PROCESS_COUNT = 100;

    private final Scheduler scheduler;
    private final NotificationEventListener eventListener;
    private final int processCount;
    private volatile boolean closed;

    public DefaultNotificationJobScheduler(Scheduler scheduler) {
        this(scheduler, null, DEFAULT_PROCESS_COUNT);
    }

    public DefaultNotificationJobScheduler(Scheduler scheduler, NotificationEventListener eventListener) {
        this(scheduler, eventListener, DEFAULT_PROCESS_COUNT);
    }

    public DefaultNotificationJobScheduler(Scheduler scheduler, NotificationEventListener eventListener, int processCount) {
        this.scheduler = scheduler;
        this.eventListener = eventListener;
        this.processCount = processCount;
    }

    @Override
    public boolean add(NotificationJob<?, ?, ?> job) {
        return queue(new NotificationJobRunner(job));
    }

    private boolean queue(NotificationJobRunner notificationJobRunner) {
        if (closed) {
            return false;
        } else {
            long nextSchedule = notificationJobRunner.job.getSchedule().nextEpochSchedule(notificationJobRunner.scheduleContext);
            scheduler.schedule(notificationJobRunner, nextSchedule);
            return true;
        }
    }

    private void requeue(NotificationJobRunner notificationJobRunner, long lastSupposedSchedule) {
        long nextSchedule = notificationJobRunner.job.getSchedule().nextEpochSchedule(notificationJobRunner.scheduleContext);
        if (nextSchedule != lastSupposedSchedule) {
            queue(notificationJobRunner);
        }
    }

    @Override
    public void stop() {
        closed = true;
    }

    @Override
    public void stop(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    private class NotificationJobRunner implements Runnable {
        private final NotificationJob<?, ?, ?> job;
        private final MutableScheduleContext scheduleContext = new MutableScheduleContext();

        private NotificationJobRunner(NotificationJob<?, ?, ?> job) {
            this.job = job;
            this.scheduleContext.setLastScheduledExecutionTime(new Date().getTime());
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void run() {
            long supposedSchedule = job.getSchedule().nextEpochSchedule(scheduleContext);
            scheduleContext.setLastScheduledExecutionTime(supposedSchedule);
            scheduleContext.setLastActualExecutionTime(System.currentTimeMillis());
            NotificationJobProcessor jobProcessor = job.getJobProcessor();
            MutableNotificationJobProcessingContext jobContext = new MutableNotificationJobProcessingContext(processCount);
            Notification<?, ?, ?> lastProcessedNotification;
            do {
                lastProcessedNotification = jobProcessor.process(job, jobContext);
                jobContext.setLastProcessed(lastProcessedNotification);
                if (eventListener != null) {
                    try {
                        eventListener.onNotificationsCreated();
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "An error occurred in the notification event listener", e);
                    }
                }
            } while (lastProcessedNotification != null);
            scheduleContext.setLastCompletionTime(System.currentTimeMillis());
            requeue(this, supposedSchedule);
        }
    }
}
