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
package com.blazebit.notify.notification.scheduler.timer;

import com.blazebit.notify.notification.*;
import com.blazebit.notify.notification.event.NotificationEventListener;

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ExecutorServiceNotificationJobScheduler implements NotificationJobScheduler, Runnable {

    private static final int DEFAULT_PROCESS_COUNT = 100;
    private final int processCount;
    private final PriorityBlockingQueue<JobScheduleEntry> jobQueue;
    private final ScheduledExecutorService executorService;
    private final AtomicReference<ScheduleEntry> nextScheduleEntry = new AtomicReference<>(new ScheduleEntry(Long.MAX_VALUE, null));
    private final NotificationEventListener eventListener;
    private volatile boolean closed;

    public ExecutorServiceNotificationJobScheduler() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    public ExecutorServiceNotificationJobScheduler(ScheduledExecutorService executorService) {
        this(executorService, null, DEFAULT_PROCESS_COUNT);
    }

    public ExecutorServiceNotificationJobScheduler(ScheduledExecutorService executorService, NotificationEventListener eventListener, int processCount) {
        this.processCount = processCount;
        this.jobQueue = new PriorityBlockingQueue<>(11);
        this.executorService = executorService;
        this.eventListener = eventListener;
    }

    @Override
    public boolean add(NotificationJob<?, ?, ?> job) {
        return queue(new JobScheduleEntry(job, new MutableScheduleContext()));
    }

    private void scheduleOrRequeue(JobScheduleEntry scheduleEntry) {
        long jobSchedule = scheduleEntry.job.getSchedule().nextEpochSchedule(scheduleEntry.scheduleContext);
        if (System.currentTimeMillis() > jobSchedule) {
            try {
                schedule(scheduleEntry);
            } finally {
                try {
                    scheduleOrRequeue(jobQueue.remove());
                } catch (NoSuchElementException ex) {
                    // Ignore
                }
            }
        } else {
            queue(scheduleEntry);
        }
    }

    private boolean queue(JobScheduleEntry scheduleEntry) {
        if (closed) {
            return false;
        }
        jobQueue.add(scheduleEntry);
        long jobSchedule = scheduleEntry.nextSchedule;
        do {
            ScheduleEntry currentScheduleEntry = nextScheduleEntry.get();
            long lastSchedule = currentScheduleEntry.schedule;
            if (jobSchedule < lastSchedule) {
                ScheduledFuture<?> schedule = executorService.schedule(this, jobSchedule - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                ScheduleEntry newScheduleEntry = new ScheduleEntry(jobSchedule, schedule);
                if (nextScheduleEntry.compareAndSet(currentScheduleEntry, newScheduleEntry)) {
                    if (currentScheduleEntry.future != null) {
                        currentScheduleEntry.future.cancel(false);
                    }
                    break;
                }
            } else {
                break;
            }
        } while (true);

        return true;
    }

    @Override
    public void stop() {
        stop(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop(long timeout, TimeUnit unit) {
        closed = true;
        long millisRemaining = unit.toMillis(timeout);
        while (!jobQueue.isEmpty()) {
            JobScheduleEntry peek = jobQueue.peek();
            if (peek != null) {
                long sleepStart = System.currentTimeMillis();
                long sleepingMillis = peek.nextSchedule - sleepStart;
                if (sleepingMillis > 0) {
                    try {
                        Thread.sleep(sleepingMillis);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    millisRemaining -= System.currentTimeMillis() - sleepStart;
                }
                if (millisRemaining < 1) {
                    executorService.shutdown();
                    Thread.currentThread().interrupt();
                }
            }
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(millisRemaining, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        try {
            scheduleOrRequeue(jobQueue.remove());
        } catch (NoSuchElementException ex) {
            // Ignore
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void schedule(JobScheduleEntry scheduleEntry) {
        scheduleEntry.scheduleContext.setLastScheduledExecutionTime(scheduleEntry.nextSchedule);
        scheduleEntry.scheduleContext.setLastActualExecutionTime(System.currentTimeMillis());
        NotificationJobProcessor jobProcessor = scheduleEntry.job.getJobProcessor();
        MutableNotificationJobProcessingContext jobContext = new MutableNotificationJobProcessingContext(processCount);
        Notification<?, ?, ?> lastProcessedNotification;
        do {
            lastProcessedNotification = jobProcessor.process(scheduleEntry.job, jobContext);
            jobContext.setLastProcessed(lastProcessedNotification);
            if (eventListener != null) {
                eventListener.onNotificationsCreated();
            }
        } while (lastProcessedNotification != null);
        scheduleEntry.scheduleContext.setLastCompletionTime(System.currentTimeMillis());
    }

    private static class ScheduleEntry {
        private final long schedule;
        private final ScheduledFuture<?> future;

        private ScheduleEntry(long schedule, ScheduledFuture<?> future) {
            this.schedule = schedule;
            this.future = future;
        }
    }

    private static class JobScheduleEntry implements Comparable<JobScheduleEntry> {

        private final NotificationJob<?, ?, ?> job;
        private final MutableScheduleContext scheduleContext;
        private final long nextSchedule;

        JobScheduleEntry(NotificationJob<?, ?, ?> job, MutableScheduleContext scheduleContext) {
            this.job = job;
            this.scheduleContext = scheduleContext;
            this.nextSchedule = job.getSchedule().nextEpochSchedule(scheduleContext);
        }

        @Override
        public int compareTo(JobScheduleEntry o) {
            return Long.compare(nextSchedule, o.nextSchedule);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            JobScheduleEntry that = (JobScheduleEntry) o;

            if (nextSchedule != that.nextSchedule) {
                return false;
            }
            return job.equals(that.job);
        }

        @Override
        public int hashCode() {
            int result = job.hashCode();
            result = 31 * result + (int) (nextSchedule ^ (nextSchedule >>> 32));
            return result;
        }
    }
}
