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

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ExecutorServiceNotificationJobScheduler implements NotificationJobScheduler, Runnable {

    private static final int DEFAULT_PROCESS_COUNT = 100;
    private final int processCount;
    private final PriorityBlockingQueue<JobScheduleEntry> jobQueue;
    private final ScheduledExecutorService executorService;
    private final AtomicReference<ScheduleEntry> nextScheduleEntry = new AtomicReference<>(new ScheduleEntry(Long.MAX_VALUE, null));
    private volatile boolean closed;

    public ExecutorServiceNotificationJobScheduler() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    public ExecutorServiceNotificationJobScheduler(ScheduledExecutorService executorService) {
        this(executorService, DEFAULT_PROCESS_COUNT);
    }

    public ExecutorServiceNotificationJobScheduler(ScheduledExecutorService executorService, int processCount) {
        this.processCount = processCount;
        this.jobQueue = new PriorityBlockingQueue<>(11);
        this.executorService = executorService;
    }

    @Override
    public boolean add(NotificationJob<?, ?> job) {
        return queue(new JobScheduleEntry(job));
    }

    private void scheduleOrRequeue(JobScheduleEntry scheduleEntry) {
        long jobSchedule = scheduleEntry.schedule;
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
        long jobSchedule = scheduleEntry.schedule;
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
                long sleepingMillis = peek.schedule - sleepStart;
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

    public void schedule(JobScheduleEntry scheduleEntry) {
        NotificationJobProcessor jobProcessor = scheduleEntry.job.getChannel().getJobProcessor();
        MutableNotificationJobContext jobContext = new MutableNotificationJobContext(processCount);
        Notification<?> lastProcessedNotification;
        do {
            lastProcessedNotification = jobProcessor.process(scheduleEntry.job, jobContext);
            jobContext.setLastProcessed(lastProcessedNotification);
        } while (lastProcessedNotification != null);
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

        private final NotificationJob<?, ?> job;
        private final long schedule;

        public JobScheduleEntry(NotificationJob<?, ?> job) {
            this.job = job;
            this.schedule = job.getSchedule().nextEpochSchedule();
        }

        @Override
        public int compareTo(JobScheduleEntry o) {
            return Long.compare(schedule, o.schedule);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            JobScheduleEntry that = (JobScheduleEntry) o;

            if (schedule != that.schedule) {
                return false;
            }
            return job.equals(that.job);
        }

        @Override
        public int hashCode() {
            int result = job.hashCode();
            result = 31 * result + (int) (schedule ^ (schedule >>> 32));
            return result;
        }
    }
}
