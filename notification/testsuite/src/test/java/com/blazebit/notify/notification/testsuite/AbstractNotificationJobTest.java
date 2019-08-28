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
package com.blazebit.notify.notification.testsuite;

import com.blazebit.notify.actor.scheduler.executor.ExecutorServiceScheduler;
import com.blazebit.notify.job.ConfigurationSource;
import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.Schedule;
import com.blazebit.notify.job.event.JobInstanceListener;
import com.blazebit.notify.job.spi.ScheduleFactory;
import com.blazebit.notify.notification.*;
import com.blazebit.notify.notification.channel.memory.MemoryChannel;
import com.blazebit.notify.notification.processor.memory.AbstractMemoryNotificationJobInstanceProcessor;
import org.junit.After;
import org.junit.Assert;

import java.time.Instant;
import java.util.concurrent.*;

public abstract class AbstractNotificationJobTest<R extends NotificationRecipient<?>, T extends NotificationMessage> {

    protected static ChannelKey channelKey = ChannelKey.of("default", null);

    private CountDownLatch latch;
    protected NotificationJobContext jobContext;
    protected Channel<R, T> channel;
    protected BlockingQueue<NotificationMessage> sink;

    public AbstractNotificationJobTest() {
        this.sink = new ArrayBlockingQueue<>(1024);
        this.channel = new MemoryChannel(sink);
    }

    protected NotificationJobContext.Builder builder() {
        return builder(1);
    }

    protected NotificationJobContext.Builder builder(int count) {
        latch = new CountDownLatch(count);
        return NotificationJobContext.Builder.create()
                .withJobProcessorFactory(NotificationJobProcessorFactory.of((jobTrigger, context) -> {
                    context.getJobManager().addJobInstance(new SimpleNotificationJobInstance((SimpleNotificationJobTrigger) jobTrigger));
                }))
                .withJobInstanceProcessorFactory(NotificationJobInstanceProcessorFactory.of(new SimpleNotificationJobInstanceProcessor()))
                .withJobInstanceListener(new JobInstanceListener() {
                    @Override
                    public void onJobInstanceChunkSuccess(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
                        latch.countDown();
                    }

                    @Override
                    public void onJobInstanceError(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
                        latch.countDown();
                    }

                    @Override
                    public void onJobInstanceSuccess(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
                        latch.countDown();
                    }
                })
                .withMessageResolverFactory(new SimpleMessageResolverFactory())
                .withChannelFactory(new ChannelFactory() {
                    @Override
                    public ChannelKey getChannelType() {
                        return channelKey;
                    }

                    @Override
                    public Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage> createChannel(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
                        return channel;
                    }
                })
                .withRecipientResolver((jobInstance, jobProcessingContext) -> ((SimpleNotificationJobInstance) jobInstance).getTrigger().getJob().getRecipientResolver().resolveNotificationRecipients(jobInstance, jobProcessingContext))
                .withScheduleFactory(new SimpleScheduleFactory())
                .withProperty(ExecutorServiceScheduler.EXECUTOR_SERVICE_PROPERTY, Executors.newScheduledThreadPool(2))
                .withProperty(ExecutorServiceScheduler.EXECUTOR_SERVICE_PROPERTY + ".jobInstanceScheduler/jobInstance/processor", Executors.newSingleThreadScheduledExecutor());
    }

    @After
    public void stop() {
        if (jobContext != null) {
            jobContext.stop();
        }
    }

    protected void await() {
        await(3L, TimeUnit.SECONDS);
    }

    protected void await(long timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
            Assert.assertEquals("Expected to run " + latch.getCount() + " more jobs", 0L, latch.getCount());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected static class SimpleScheduleFactory implements ScheduleFactory {

        private final Schedule schedule;

        public SimpleScheduleFactory() {
            this(new SimpleSchedule());
        }

        public SimpleScheduleFactory(Schedule schedule) {
            this.schedule = schedule;
        }

        @Override
        public String asCronExpression(Instant instant) {
            return null;
        }

        @Override
        public Schedule createSchedule(String cronExpression) {
            return schedule;
        }
    }

    protected static class SimpleNotificationJobInstanceProcessor extends AbstractMemoryNotificationJobInstanceProcessor<Object, SimpleNotification, SimpleNotificationJobInstance, SimpleNotificationRecipient> {

        @Override
        protected SimpleNotification produceNotification(SimpleNotificationJobInstance notificationJobInstance, SimpleNotificationRecipient recipient) {
            SimpleNotification notification = new SimpleNotification(notificationJobInstance);
            notification.setChannelType(channelKey.getChannelType());
            notification.setRecipient(recipient);
            return notification;
        }

        @Override
        protected Object getProcessingResultId(SimpleNotification processingResult) {
            return null;
        }
    }

    private static class SimpleMessageResolverFactory implements NotificationMessageResolverFactory {
        @Override
        public Class getNotificationMessageType() {
            return NotificationMessage.class;
        }

        @Override
        public NotificationMessageResolver createNotificationMessageResolver(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
            return new NotificationMessageResolver() {
                @Override
                public NotificationMessage resolveNotificationMessage(Notification notification) {
                    return new SimpleNotificationMessage();
                }
            };
        }
    }
}
