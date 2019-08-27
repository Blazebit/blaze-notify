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
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.JobInstanceState;
import com.blazebit.notify.job.Schedule;
import com.blazebit.notify.job.spi.ScheduleFactory;
import com.blazebit.notify.notification.*;
import com.blazebit.notify.notification.channel.memory.MemoryChannel;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@RunWith(Parameterized.class)
public abstract class AbstractConfigurationTest<R extends NotificationRecipient<?>, T extends NotificationMessage> {

    protected static ChannelKey channelKey = ChannelKey.of("default", null);
    protected NotificationJobContext jobContext;
    protected Channel<R, T> channel;
    protected T defaultMessage;
    protected BlockingQueue<NotificationMessage> sink;

    public AbstractConfigurationTest(Channel<R, T> channel, T defaultMessage, BlockingQueue<NotificationMessage> sink, NotificationJobProcessorFactory jobProcessorFactory, NotificationJobInstanceProcessorFactory jobInstanceProcessorFactory) {
        this.jobContext = NotificationJobContext.Builder.create()
                .withJobProcessorFactory(jobProcessorFactory)
                .withJobInstanceProcessorFactory(jobInstanceProcessorFactory)
                .withNotificationProcessorFactory(new SimpleNotificationProcessorFactory())
                .withMessageResolverFactory(new SimpleMessageResolverFactory())
                .withChannelFactory(new SimpleChannelFactory())
                .withRecipientResolver((jobInstance, jobProcessingContext) -> ((SimpleNotificationJobInstance) jobInstance).getTrigger().getJob().getRecipientResolver().resolveNotificationRecipients(jobInstance, jobProcessingContext))
                .withScheduleFactory(new SimpleScheduleFactory())
                .withProperty(ExecutorServiceScheduler.EXECUTOR_SERVICE_PROPERTY, Executors.newScheduledThreadPool(2))
                .withProperty(ExecutorServiceScheduler.EXECUTOR_SERVICE_PROPERTY + ".jobInstanceScheduler/jobInstance/processor", Executors.newSingleThreadScheduledExecutor())
                .createContext();
        this.channel = channel;
        this.defaultMessage = defaultMessage;
        this.sink = sink;
    }

    @After
    public void stop() {
        jobContext.stop();
    }

    @Parameterized.Parameters
    public static Object[][] createCombinations() {
        return createCombinations(new SimpleNotificationJobProcessorFactory(), new SimpleNotificationJobInstanceProcessorFactory());
    }

    public static Object[][] createCombinations(NotificationJobProcessorFactory jobProcessorFactory) {
        return createCombinations(jobProcessorFactory, new SimpleNotificationJobInstanceProcessorFactory());
    }

    public static Object[][] createCombinations(NotificationJobInstanceProcessorFactory jobInstanceProcessorFactory) {
        return createCombinations(new SimpleNotificationJobProcessorFactory(), jobInstanceProcessorFactory);
    }

    public static Object[][] createCombinations(NotificationJobProcessorFactory jobProcessorFactory, NotificationJobInstanceProcessorFactory jobInstanceProcessorFactory) {
        BlockingQueue<NotificationMessage> sink;
        return new Object[][]{
                {new MemoryChannel(sink = new ArrayBlockingQueue<>(1024)), new SimpleNotificationMessage(), sink, jobProcessorFactory, jobInstanceProcessorFactory}
        };
    }

    private class SimpleChannelFactory implements ChannelFactory {
        @Override
        public ChannelKey getChannelType() {
            return channelKey;
        }

        @Override
        public Channel createChannel(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
            return channel;
        }
    }

    private static class SimpleNotificationJobProcessorFactory implements NotificationJobProcessorFactory {
        @Override
        public <T extends NotificationJobTrigger> NotificationJobProcessor<T> createJobProcessor(NotificationJobContext jobContext, T jobTrigger) {
            return (NotificationJobProcessor<T>) new SimpleNotificationJobProcessor();
        }
    }

    private static class SimpleNotificationJobProcessor implements NotificationJobProcessor<NotificationJobTrigger> {
        @Override
        public void process(NotificationJobTrigger jobTrigger, NotificationJobContext context) {
            SimpleNotificationJobInstance jobInstance = new SimpleNotificationJobInstance();
            jobInstance.setTrigger((SimpleNotificationJobTrigger) jobTrigger);
            jobInstance.setState(JobInstanceState.NEW);
            jobInstance.setCreationTime(Instant.now());
            jobInstance.setScheduleTime(Instant.ofEpochMilli(jobTrigger.getNotificationSchedule(context).nextEpochSchedule()));
            context.getJobManager().addJobInstance(jobInstance);
        }
    }

    private static class SimpleNotificationJobInstanceProcessorFactory implements NotificationJobInstanceProcessorFactory {
        @Override
        public <T extends NotificationJobInstance<?, ?>> NotificationJobInstanceProcessor<?, T> createJobInstanceProcessor(NotificationJobContext jobContext, T jobInstance) {
            return (NotificationJobInstanceProcessor<?, T>) new SimpleNotificationJobInstanceProcessor();
        }
    }

    private static class SimpleNotificationJobInstanceProcessor implements NotificationJobInstanceProcessor<Object, NotificationJobInstance<Long, ?>> {
        @Override
        public Object process(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext<Object> context) {
            return null;
        }
    }

    private static class SimpleScheduleFactory implements ScheduleFactory {

        @Override
        public String asCronExpression(Instant instant) {
            return null;
        }

        @Override
        public Schedule createSchedule(String cronExpression) {
            return new SimpleSchedule();
        }
    }

    private static class SimpleNotificationProcessorFactory implements NotificationProcessorFactory {

        @Override
        public <N extends Notification<?>> NotificationProcessor<N> createNotificationProcessor(NotificationJobContext jobContext, N notification) {
            return null;
        }
    }

    private static class SimpleMessageResolverFactory implements NotificationMessageResolverFactory {
        @Override
        public Class getNotificationMessageType() {
            return null;
        }

        @Override
        public NotificationMessageResolver createNotificationMessageResolver(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
            return null;
        }
    }
}
