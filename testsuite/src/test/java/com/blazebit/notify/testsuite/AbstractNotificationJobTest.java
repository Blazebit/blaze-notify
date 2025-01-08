/*
 * Copyright 2018 - 2025 Blazebit.
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
package com.blazebit.notify.testsuite;

import com.blazebit.actor.scheduler.executor.ExecutorServiceScheduler;
import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.JobInstance;
import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.job.Schedule;
import com.blazebit.job.JobInstanceListener;
import com.blazebit.job.spi.ScheduleFactory;
import com.blazebit.notify.Channel;
import com.blazebit.notify.ChannelFactory;
import com.blazebit.notify.ChannelKey;
import com.blazebit.notify.Notification;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationJobInstanceProcessorFactory;
import com.blazebit.notify.NotificationJobProcessorFactory;
import com.blazebit.notify.NotificationMessage;
import com.blazebit.notify.NotificationMessageResolver;
import com.blazebit.notify.NotificationMessageResolverFactory;
import com.blazebit.notify.NotificationRecipient;
import com.blazebit.notify.channel.memory.MemoryChannel;
import com.blazebit.notify.processor.memory.AbstractMemoryNotificationJobInstanceProcessor;
import org.junit.After;
import org.junit.Assert;

import java.time.Instant;
import java.util.concurrent.*;

public abstract class AbstractNotificationJobTest<R extends NotificationRecipient<?>, T extends NotificationMessage> {

    protected static ChannelKey<?> channelKey = ChannelKey.of("default", null);

    private CountDownLatch latch;
    protected NotificationJobContext jobContext;
    protected Channel<R, T> channel;
    protected BlockingQueue<T> sink;

    public AbstractNotificationJobTest() {
        this.sink = new ArrayBlockingQueue<>(1024);
        this.channel = new MemoryChannel<>(sink);
    }

    protected NotificationJobContext.Builder builder() {
        return builder(1);
    }

    protected NotificationJobContext.Builder builder(int count) {
        latch = new CountDownLatch(count);
        return NotificationJobContext.builder()
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
                .withChannelFactory(new ChannelFactory<Channel<R, T>>() {
                    @Override
                    public ChannelKey<Channel<R, T>> getChannelType() {
                        return (ChannelKey<Channel<R, T>>) channelKey;
                    }

                    @Override
                    public Channel<R, T> createChannel(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
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
            this(new OnceSchedule());
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

    protected static class SimpleNotificationJobInstanceProcessor extends AbstractMemoryNotificationJobInstanceProcessor<Long, SimpleNotification, SimpleNotificationJobInstance, SimpleNotificationRecipient> {

        @Override
        protected SimpleNotification produceNotification(JobInstanceProcessingContext<Long> context, SimpleNotificationJobInstance notificationJobInstance, SimpleNotificationRecipient recipient) {
            SimpleNotification notification = new SimpleNotification(notificationJobInstance);
            notification.setChannelType(channelKey.getChannelType());
            notification.setRecipient(recipient);
            context.getJobContext().getJobManager().addJobInstance(notificationJobInstance);
            return notification;
        }

        @Override
        protected Long getProcessingResultId(SimpleNotification processingResult) {
            return null;
        }
    }

    private static class SimpleMessageResolverFactory implements NotificationMessageResolverFactory<NotificationMessage> {
        @Override
        public Class getNotificationMessageType() {
            return NotificationMessage.class;
        }

        @Override
        public NotificationMessageResolver<NotificationMessage> createNotificationMessageResolver(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
            return new NotificationMessageResolver<NotificationMessage>() {
                @Override
                public NotificationMessage resolveNotificationMessage(Notification<?> notification) {
                    return new SimpleNotificationMessage();
                }
            };
        }
    }
}
