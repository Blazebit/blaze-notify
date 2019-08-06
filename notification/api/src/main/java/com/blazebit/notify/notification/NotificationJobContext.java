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

import com.blazebit.notify.actor.ActorContext;
import com.blazebit.notify.job.*;
import com.blazebit.notify.job.event.JobInstanceListener;
import com.blazebit.notify.job.event.JobInstanceScheduleListener;
import com.blazebit.notify.job.event.JobTriggerListener;
import com.blazebit.notify.job.event.JobTriggerScheduleListener;
import com.blazebit.notify.job.spi.*;
import com.blazebit.notify.notification.spi.NotificationManagerFactory;
import com.blazebit.notify.notification.spi.NotificationScheduler;
import com.blazebit.notify.notification.spi.NotificationSchedulerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public interface NotificationJobContext extends JobContext {

    NotificationRecipientResolver getRecipientResolver();

    NotificationManager getNotificationManager();

    void triggerNotificationScan(long earliestNewNotificationSchedule);

    void triggerNotificationScan(String name, long earliestNewNotificationSchedule);

    <J extends NotificationJobTrigger> NotificationJobProcessor<J> getJobProcessor(J jobTrigger);

    <I extends NotificationJobInstance<?>> NotificationJobInstanceProcessor<?, I> getJobInstanceProcessor(I jobInstance);

    <N extends Notification<?>> NotificationProcessor<N> getNotificationProcessor(N notification);

    <M extends NotificationMessage> NotificationMessageResolver<M> getNotificationMessageResolver(Notification<?> notification, Class<M> notificationMessageClass);

    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelClass);

    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelClass, ConfigurationSource configurationSource);

    ChannelKey<Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> resolveChannelKey(Notification<?> notification);

    class Builder extends JobContext.BuilderBase<Builder> {

        private NotificationManagerFactory notificationManagerFactory;
        private NotificationSchedulerFactory notificationSchedulerFactory;
        private NotificationRecipientResolver recipientResolver;
        private NotificationJobProcessorFactory notificationJobProcessorFactory;
        private NotificationJobInstanceProcessorFactory notificationJobInstanceProcessorFactory;
        private NotificationProcessorFactory notificationProcessorFactory;
        private final Map<ChannelKey<?>, ChannelFactory<?>> channelFactories = new HashMap<>();
        private final Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> messageResolverFactories = new HashMap<>();
        private ChannelResolver channelResolver;

        public static Builder create() {
            Builder builder = new Builder();
            builder.loadDefaults();
            return builder;
        }

        @Override
        protected void loadDefaults() {
            super.loadDefaults();
            notificationManagerFactory = loadFirstServiceOrNone(NotificationManagerFactory.class);
            notificationSchedulerFactory = loadFirstServiceOrNone(NotificationSchedulerFactory.class);
            recipientResolver = loadFirstServiceOrNone(NotificationRecipientResolver.class);
            notificationJobProcessorFactory = loadFirstServiceOrNone(NotificationJobProcessorFactory.class);
            notificationJobInstanceProcessorFactory = loadFirstServiceOrNone(NotificationJobInstanceProcessorFactory.class);
            notificationProcessorFactory = loadFirstServiceOrNone(NotificationProcessorFactory.class);
            channelResolver = loadFirstServiceOrNone(ChannelResolver.class);
            for (ChannelFactory channelFactory : loadServices(ChannelFactory.class)) {
                channelFactories.put(channelFactory.getChannelType(), channelFactory);
            }
            for (NotificationMessageResolverFactory notificationMessageResolverFactory : loadServices(NotificationMessageResolverFactory.class)) {
                messageResolverFactories.put(notificationMessageResolverFactory.getNotificationMessageType(), notificationMessageResolverFactory);
            }
        }

        @Override
        protected void checkCreateContext() {
            super.checkCreateContext();
            if (getNotificationManagerFactory() == null) {
                throw new JobException("No notification manager factory given!");
            }
            if (getNotificationSchedulerFactory() == null) {
                throw new JobException("No notification scheduler factory given!");
            }
            if (getNotificationProcessorFactory() == null) {
                throw new JobException("No notification processor factory given!");
            }
            if (getRecipientResolver() == null) {
                throw new JobException("No recipient resolver given!");
            }
            if (getChannelFactories().isEmpty()) {
                throw new JobException("No channel factories given!");
            }
            if (getMessageResolverFactories().isEmpty()) {
                throw new JobException("No message resolver factories given!");
            }
            if (getChannelResolver() == null) {
                throw new JobException("No channel resolver given!");
            }
        }

        @Override
        public NotificationJobContext createContext() {
            checkCreateContext();
            return new DefaultNotificationJobContext(
                    getTransactionSupport(),
                    getJobManagerFactory(),
                    getOrCreateActorContext(),
                    getScheduleFactory(),
                    getJobSchedulerFactory(),
                    getJobProcessorFactory(),
                    getJobInstanceProcessorFactory(),
                    getJobTriggerScheduleListeners(),
                    getJobInstanceScheduleListeners(),
                    getJobTriggerListeners(),
                    getJobInstanceListeners(),
                    getProperties(),
                    getServiceMap(),
                    getNotificationManagerFactory(),
                    getNotificationSchedulerFactory(),
                    getNotificationProcessorFactory(),
                    getRecipientResolver(),
                    getChannelFactories(),
                    getMessageResolverFactories(),
                    getChannelResolver()
            );
        }

        public NotificationManagerFactory getNotificationManagerFactory() {
            return notificationManagerFactory;
        }

        public Builder withNotificationManagerFactory(NotificationManagerFactory notificationManagerFactory) {
            this.notificationManagerFactory = notificationManagerFactory;
            return this;
        }

        public NotificationSchedulerFactory getNotificationSchedulerFactory() {
            return notificationSchedulerFactory;
        }

        public Builder withNotificationSchedulerFactory(NotificationSchedulerFactory notificationSchedulerFactory) {
            this.notificationSchedulerFactory = notificationSchedulerFactory;
            return this;
        }

        public NotificationRecipientResolver getRecipientResolver() {
            return recipientResolver;
        }

        public Builder withRecipientResolver(NotificationRecipientResolver recipientResolver) {
            this.recipientResolver = recipientResolver;
            return this;
        }

        @Override
        public NotificationJobProcessorFactory getJobProcessorFactory() {
            return notificationJobProcessorFactory;
        }

        @Override
        public Builder withJobProcessorFactory(JobProcessorFactory jobProcessorFactory) {
            return withJobProcessorFactory((NotificationJobProcessorFactory) jobProcessorFactory);
        }

        public Builder withJobProcessorFactory(NotificationJobProcessorFactory notificationJobProcessorFactory) {
            this.notificationJobProcessorFactory = notificationJobProcessorFactory;
            return this;
        }

        @Override
        public NotificationJobInstanceProcessorFactory getJobInstanceProcessorFactory() {
            return notificationJobInstanceProcessorFactory;
        }

        @Override
        public Builder withJobInstanceProcessorFactory(JobInstanceProcessorFactory jobInstanceProcessorFactory) {
            return withJobInstanceProcessorFactory((NotificationJobInstanceProcessorFactory) jobInstanceProcessorFactory);
        }

        public Builder withJobInstanceProcessorFactory(NotificationJobInstanceProcessorFactory notificationJobInstanceProcessorFactory) {
            this.notificationJobInstanceProcessorFactory = notificationJobInstanceProcessorFactory;
            return this;
        }

        public NotificationProcessorFactory getNotificationProcessorFactory() {
            return notificationProcessorFactory;
        }

        public Builder withNotificationProcessorFactory(NotificationProcessorFactory notificationProcessorFactory) {
            this.notificationProcessorFactory = notificationProcessorFactory;
            return this;
        }

        public Map<ChannelKey<?>, ChannelFactory<?>> getChannelFactories() {
            return channelFactories;
        }

        public Builder withChannelFactory(ChannelFactory channelFactory) {
            channelFactories.put(channelFactory.getChannelType(), channelFactory);
            return this;
        }

        public Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> getMessageResolverFactories() {
            return messageResolverFactories;
        }

        public Builder withMessageResolverFactory(NotificationMessageResolverFactory<?> messageResolverFactory) {
            this.messageResolverFactories.put(messageResolverFactory.getNotificationMessageType(), messageResolverFactory);
            return this;
        }

        public ChannelResolver getChannelResolver() {
            return channelResolver;
        }

        public Builder withChannelResolver(ChannelResolver channelResolver) {
            this.channelResolver = channelResolver;
            return this;
        }

        private static final class DefaultNotificationJobContext extends JobContext.Builder.DefaultJobContext implements NotificationJobContext {

            private final NotificationManager notificationManager;
            private final NotificationScheduler notificationScheduler;
            private final NotificationProcessorFactory notificationProcessorFactory;
            private final NotificationRecipientResolver recipientResolver;
            private final Map<ChannelKey<?>, ChannelFactory<?>> channelFactories;
            private final Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> messageResolverFactories;
            private final ChannelResolver channelResolver;
            private final Map<ChannelMapKey, Channel<?, ?>> channels = new ConcurrentHashMap<>();
            private final Map<MessageResolverMapKey, NotificationMessageResolver<?>> messageResolvers = new ConcurrentHashMap<>();

            protected DefaultNotificationJobContext(TransactionSupport transactionSupport, JobManagerFactory jobManagerFactory, ActorContext actorContext, ScheduleFactory scheduleFactory, JobSchedulerFactory jobSchedulerFactory, JobProcessorFactory jobProcessorFactory,
                                                    JobInstanceProcessorFactory jobInstanceProcessorFactory, List<JobTriggerScheduleListener> jobTriggerScheduleListeners, List<JobInstanceScheduleListener> jobInstanceScheduleListeners, List<JobTriggerListener> jobTriggerListeners, List<JobInstanceListener> jobInstanceListeners,
                                                    Map<String, Object> properties, Map<Class<?>, Object> serviceMap, NotificationManagerFactory notificationManagerFactory, NotificationSchedulerFactory notificationSchedulerFactory, NotificationProcessorFactory notificationProcessorFactory, NotificationRecipientResolver recipientResolver,
                                                    Map<ChannelKey<?>, ChannelFactory<?>> channelFactories, Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> messageResolverFactories, ChannelResolver channelResolver) {
                super(transactionSupport, jobManagerFactory, actorContext, scheduleFactory, jobSchedulerFactory, jobProcessorFactory, jobInstanceProcessorFactory, jobTriggerScheduleListeners, jobInstanceScheduleListeners, jobTriggerListeners, jobInstanceListeners, properties, serviceMap);
                this.notificationProcessorFactory = notificationProcessorFactory;
                this.recipientResolver = recipientResolver;
                this.channelFactories = channelFactories;
                this.messageResolverFactories = messageResolverFactories;
                this.channelResolver = channelResolver;
                this.notificationManager = notificationManagerFactory.createNotificationManager(this);
                this.notificationScheduler = notificationSchedulerFactory.createNotificationScheduler(this, actorContext);
            }

            @Override
            public <T> T getService(Class<T> serviceClass) {
                if (NotificationRecipientResolver.class == serviceClass) {
                    return (T) getRecipientResolver();
                }
                return super.getService(serviceClass);
            }

            @Override
            public void triggerNotificationScan(long earliestNewNotificationSchedule) {
                notificationScheduler.triggerNotificationScan(null, earliestNewNotificationSchedule);
            }

            @Override
            public void triggerNotificationScan(String name, long earliestNewNotificationSchedule) {
                notificationScheduler.triggerNotificationScan(name, earliestNewNotificationSchedule);
            }

            @Override
            public NotificationManager getNotificationManager() {
                return notificationManager;
            }

            @Override
            public NotificationRecipientResolver getRecipientResolver() {
                return recipientResolver;
            }

            @Override
            public <J extends NotificationJobTrigger> NotificationJobProcessor<J> getJobProcessor(J jobTrigger) {
                return (NotificationJobProcessor<J>) super.getJobProcessor(jobTrigger);
            }

            @Override
            public <I extends NotificationJobInstance<?>> NotificationJobInstanceProcessor<?, I> getJobInstanceProcessor(I jobInstance) {
                return (NotificationJobInstanceProcessor<?, I>) super.getJobInstanceProcessor(jobInstance);
            }

            @Override
            public <N extends Notification<?>> NotificationProcessor<N> getNotificationProcessor(N notification) {
                return notificationProcessorFactory.createNotificationProcessor(this, notification);
            }

            @Override
            public <T extends NotificationMessage> NotificationMessageResolver<T> getNotificationMessageResolver(Notification<?> notification, Class<T> notificationMessageClass) {
                return getNotificationMessageResolver(notification, notificationMessageClass, this);
            }

            public <T extends NotificationMessage> NotificationMessageResolver<T> getNotificationMessageResolver(Notification<?> notification, Class<T> notificationMessageClass, ConfigurationSource configurationSource) {
                return (NotificationMessageResolver<T>) messageResolvers.computeIfAbsent(
                        new MessageResolverMapKey(notificationMessageClass, configurationSource),
                        k -> {
                            NotificationMessageResolverFactory<?> notificationMessageResolverFactory = messageResolverFactories.get(notificationMessageClass);
                            if (notificationMessageResolverFactory == null) {
                                throw new NotificationException("No notification message resolver factory for notification message class available: " + notificationMessageClass.getName());
                            }
                            return notificationMessageResolverFactory.createNotificationMessageResolver(this, configurationSource);
                        }
                );
            }

            @Override
            public <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelKey) {
                return getChannel(channelKey, this);
            }

            @Override
            public <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelKey, ConfigurationSource configurationSource) {
                return (T) channels.computeIfAbsent(
                        new ChannelMapKey(channelKey, configurationSource),
                        k -> {
                            ChannelFactory<?> channelFactory = channelFactories.get(channelKey);
                            if (channelFactory == null) {
                                throw new NotificationException("No channel factory for channel key available: " + channelKey);
                            }
                            return channelFactory.createChannel(this, configurationSource);
                        }
                );
            }

            @Override
            public ChannelKey<Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> resolveChannelKey(Notification<?> notification) {
                return (ChannelKey) channelResolver.resolveChannel(notification, this);
            }

            private static class ChannelMapKey {

                private final ChannelKey<?> channelKey;
                private final ConfigurationSource configurationSource;

                public ChannelMapKey(ChannelKey<?> channelKey, ConfigurationSource configurationSource) {
                    this.channelKey = channelKey;
                    this.configurationSource = configurationSource;
                }

                @Override
                public boolean equals(Object o) {
                    ChannelMapKey that = (ChannelMapKey) o;
                    return channelKey.equals(that.channelKey) && configurationSource.equals(that.configurationSource);

                }

                @Override
                public int hashCode() {
                    int result = channelKey.hashCode();
                    result = 31 * result + configurationSource.hashCode();
                    return result;
                }
            }

            private static class MessageResolverMapKey {

                private final Class<?> messageType;
                private final ConfigurationSource configurationSource;

                public MessageResolverMapKey(Class<?> messageType, ConfigurationSource configurationSource) {
                    this.messageType = messageType;
                    this.configurationSource = configurationSource;
                }

                @Override
                public boolean equals(Object o) {
                    MessageResolverMapKey that = (MessageResolverMapKey) o;
                    return messageType == that.messageType && configurationSource.equals(that.configurationSource);

                }

                @Override
                public int hashCode() {
                    int result = messageType.hashCode();
                    result = 31 * result + configurationSource.hashCode();
                    return result;
                }
            }
        }
    }
}
