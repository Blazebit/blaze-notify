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
package com.blazebit.notify.notification.scheduler.spring;

import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.NotificationJob;
import com.blazebit.notify.notification.NotificationJobProcessor;
import com.blazebit.notify.notification.NotificationJobScheduler;
import com.blazebit.notify.notification.event.NotificationEventListener;
import org.springframework.scheduling.TaskScheduler;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TaskSchedulerBasedNotificationJobScheduler implements NotificationJobScheduler {

    private final TaskScheduler taskScheduler;
    private final NotificationEventListener eventListener;
    private volatile boolean closed;

    public TaskSchedulerBasedNotificationJobScheduler(TaskScheduler taskScheduler) {
        this(taskScheduler, null);
    }

    public TaskSchedulerBasedNotificationJobScheduler(TaskScheduler taskScheduler, NotificationEventListener eventListener) {
        this.taskScheduler = taskScheduler;
        this.eventListener = eventListener;
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
            taskScheduler.schedule(notificationJobRunner, new Date(nextSchedule));
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
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void run() {
            long supposedSchedule = job.getSchedule().nextEpochSchedule(scheduleContext);
            scheduleContext.setLastScheduledExecutionTime(supposedSchedule);
            scheduleContext.setLastActualExecutionTime(System.currentTimeMillis());
            NotificationJobProcessor jobProcessor = job.getJobProcessor();
            MutableNotificationJobProcessingContext jobContext = new MutableNotificationJobProcessingContext(0);
            Notification<?, ?, ?> lastProcessedNotification;
            do {
                lastProcessedNotification = jobProcessor.process(job, jobContext);
                jobContext.setLastProcessed(lastProcessedNotification);
                if (eventListener != null) {
                    eventListener.onNotificationsCreated();
                }
            } while (lastProcessedNotification != null);
            scheduleContext.setLastCompletionTime(System.currentTimeMillis());
            requeue(this, supposedSchedule);
        }
    }
}
