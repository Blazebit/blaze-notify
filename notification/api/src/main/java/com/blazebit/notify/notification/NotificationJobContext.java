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
import com.blazebit.notify.job.event.JobTriggerListener;
import com.blazebit.notify.job.spi.*;
import com.blazebit.notify.notification.spi.NotificationPartitionKeyProvider;
import com.blazebit.notify.notification.spi.NotificationPartitionKeyProviderFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface NotificationJobContext extends JobContext {

    NotificationRecipientResolver getRecipientResolver();

    void triggerNotificationScan(long earliestNewNotificationSchedule);

    void triggerNotificationScan(String channelType, long earliestNewNotificationSchedule);

    <M extends NotificationMessage> NotificationMessageResolver<M> getNotificationMessageResolver(Class<M> notificationMessageClass);

    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(String channelType);

    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(String channelType, ConfigurationSource configurationSource);

    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelClass);

    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelClass, ConfigurationSource configurationSource);

    class Builder extends JobContext.BuilderBase<Builder> {

        private static final int DEFAULT_JOB_INSTANCE_PROCESS_COUNT = 1;
        private static final int DEFAULT_NOTIFICATION_PROCESS_COUNT = 10;

        private NotificationPartitionKeyProviderFactory notificationPartitionKeyProviderFactory;
        private NotificationPartitionKeyProvider notificationPartitionKeyProvider;
        private NotificationRecipientResolver recipientResolver;
        private NotificationJobProcessorFactory notificationJobProcessorFactory;
        private NotificationJobInstanceProcessorFactory notificationJobInstanceProcessorFactory;
        private NotificationProcessorFactory notificationProcessorFactory;
        private final Map<String, ChannelFactory<?>> channelFactories = new HashMap<>();
        private final Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> messageResolverFactories = new HashMap<>();

        public static Builder create() {
            Builder builder = new Builder();
            builder.loadDefaults();
            return builder;
        }

        @Override
        protected void loadDefaults() {
            super.loadDefaults();
            notificationPartitionKeyProviderFactory = loadFirstServiceOrNone(NotificationPartitionKeyProviderFactory.class);
            recipientResolver = loadFirstServiceOrNone(NotificationRecipientResolver.class);
            notificationJobProcessorFactory = loadFirstServiceOrNone(NotificationJobProcessorFactory.class);
            notificationJobInstanceProcessorFactory = loadFirstServiceOrNone(NotificationJobInstanceProcessorFactory.class);
            notificationProcessorFactory = loadFirstServiceOrNone(NotificationProcessorFactory.class);
            for (ChannelFactory channelFactory : loadServices(ChannelFactory.class)) {
                channelFactories.put(channelFactory.getChannelType().getChannelType(), channelFactory);
            }
            for (NotificationMessageResolverFactory notificationMessageResolverFactory : loadServices(NotificationMessageResolverFactory.class)) {
                messageResolverFactories.put(notificationMessageResolverFactory.getNotificationMessageType(), notificationMessageResolverFactory);
            }
        }

        @Override
        protected void checkCreateContext() {
            super.checkCreateContext();
            if (getNotificationProcessorFactory() == null) {
                throw new JobException("No notification processor factory given!");
            }
            if (getNotificationPartitionKeyProviderFactory() == null) {
                throw new JobException("No notification partition key provider given!");
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
        }

        @Override
        public NotificationJobContext createContext() {
            checkCreateContext();
            Map<PartitionKey, Integer> partitionKeyMap = getPartitionKeyMap();
            Map<String, PartitionKey> channelPartitionKeys = new HashMap<>(channelFactories.size());
            if (partitionKeyMap.isEmpty()) {
                PartitionKeyProvider partitionKeyProvider = super.getPartitionKeyProvider();
                NotificationPartitionKeyProvider notificationPartitionKeyProvider = getNotificationPartitionKeyProvider();
                partitionKeyMap = new HashMap<>(channelFactories.size() + 1);
                partitionKeyMap.put(notificationPartitionKeyProvider.getDefaultJobInstancePartitionKey(partitionKeyProvider.getDefaultJobInstancePartitionKey()), DEFAULT_JOB_INSTANCE_PROCESS_COUNT);
                for (String channelType : channelFactories.keySet()) {
                    PartitionKey partitionKey = notificationPartitionKeyProvider.getPartitionKey(partitionKeyProvider.getDefaultJobInstancePartitionKey(), channelType);
                    channelPartitionKeys.put(channelType, partitionKey);
                    partitionKeyMap.put(partitionKey, DEFAULT_NOTIFICATION_PROCESS_COUNT);
                }
            }
            return new DefaultNotificationJobContext(
                    getTransactionSupport(),
                    getJobManagerFactory(),
                    getOrCreateActorContext(),
                    getScheduleFactory(),
                    getJobSchedulerFactory(),
                    getJobProcessorFactory(),
                    getJobInstanceProcessorFactory(),
                    partitionKeyMap,
                    getPartitionKeyProvider(),
                    getJobTriggerListeners(),
                    getJobInstanceListeners(),
                    getProperties(),
                    getServiceMap(),
                    getNotificationProcessorFactory(),
                    getNotificationPartitionKeyProvider(),
                    getRecipientResolver(),
                    getChannelFactories(),
                    getMessageResolverFactories(),
                    channelPartitionKeys
            );
        }

        @Override
        protected PartitionKeyProvider getPartitionKeyProvider() {
            PartitionKeyProvider partitionKeyProvider = super.getPartitionKeyProvider();
            NotificationPartitionKeyProvider notificationPartitionKeyProvider = getNotificationPartitionKeyProvider();

            return new PartitionKeyProvider() {
                @Override
                public PartitionKey getDefaultTriggerPartitionKey() {
                    return notificationPartitionKeyProvider.getDefaultTriggerPartitionKey(partitionKeyProvider.getDefaultTriggerPartitionKey());
                }

                @Override
                public PartitionKey getDefaultJobInstancePartitionKey() {
                    return notificationPartitionKeyProvider.getDefaultJobInstancePartitionKey(partitionKeyProvider.getDefaultJobInstancePartitionKey());
                }
            };
        }

        public NotificationPartitionKeyProvider getNotificationPartitionKeyProvider() {
            if (notificationPartitionKeyProvider == null) {
                notificationPartitionKeyProvider = notificationPartitionKeyProviderFactory.createNotificationPartitionKeyProvider(
                        new ServiceProvider() {
                            @Override
                            public <T> T getService(Class<T> serviceClass) {
                                return serviceClass.cast(getServiceMap().get(serviceClass));
                            }
                        },
                        this::getProperty
                );
            }
            return notificationPartitionKeyProvider;
        }

        public NotificationPartitionKeyProviderFactory getNotificationPartitionKeyProviderFactory() {
            return notificationPartitionKeyProviderFactory;
        }

        public Builder withNotificationPartitionKeyProviderFactory(NotificationPartitionKeyProviderFactory notificationPartitionKeyProviderFactory) {
            this.notificationPartitionKeyProviderFactory = notificationPartitionKeyProviderFactory;
            this.notificationPartitionKeyProvider = null;
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

        public Map<String, ChannelFactory<?>> getChannelFactories() {
            return channelFactories;
        }

        public Builder withChannelFactory(ChannelFactory channelFactory) {
            channelFactories.put(channelFactory.getChannelType().getChannelType(), channelFactory);
            return this;
        }

        public Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> getMessageResolverFactories() {
            return messageResolverFactories;
        }

        public Builder withMessageResolverFactory(NotificationMessageResolverFactory<?> messageResolverFactory) {
            this.messageResolverFactories.put(messageResolverFactory.getNotificationMessageType(), messageResolverFactory);
            return this;
        }

        private static final class DefaultNotificationJobContext extends JobContext.Builder.DefaultJobContext implements NotificationJobContext {

            private final NotificationProcessorFactory notificationProcessorFactory;
            private final NotificationRecipientResolver recipientResolver;
            private final Map<String, ChannelFactory<?>> channelFactories;
            private final Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> messageResolverFactories;
            private final Map<String, PartitionKey> channelPartitionKeys;
            private final Map<ChannelMapKey, Channel<?, ?>> channels = new ConcurrentHashMap<>();
            private final Map<MessageResolverMapKey, NotificationMessageResolver<?>> messageResolvers = new ConcurrentHashMap<>();

            protected DefaultNotificationJobContext(TransactionSupport transactionSupport, JobManagerFactory jobManagerFactory, ActorContext actorContext, ScheduleFactory scheduleFactory, JobSchedulerFactory jobSchedulerFactory, JobProcessorFactory jobProcessorFactory,
                                                    JobInstanceProcessorFactory jobInstanceProcessorFactory, Map<PartitionKey, Integer> partitionKeyEntries, PartitionKeyProvider partitionKeyProvider, List<JobTriggerListener> jobTriggerListeners, List<JobInstanceListener> jobInstanceListeners,
                                                    Map<String, Object> properties, Map<Class<?>, Object> serviceMap, NotificationProcessorFactory notificationProcessorFactory, NotificationPartitionKeyProvider notificationPartitionKeyProvider, NotificationRecipientResolver recipientResolver,
                                                    Map<String, ChannelFactory<?>> channelFactories, Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> messageResolverFactories, Map<String, PartitionKey> channelPartitionKeys) {
                super(transactionSupport, jobManagerFactory, actorContext, scheduleFactory, jobSchedulerFactory, jobProcessorFactory, jobInstanceProcessorFactory, partitionKeyEntries, partitionKeyProvider, jobTriggerListeners, jobInstanceListeners, properties, serviceMap);
                this.notificationProcessorFactory = notificationProcessorFactory;
                this.recipientResolver = recipientResolver;
                this.channelFactories = channelFactories;
                this.messageResolverFactories = messageResolverFactories;
                this.channelPartitionKeys = channelPartitionKeys;
                start();
            }

            @Override
            protected void afterConstruct() {
                // Wait until we are done with our construction
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
                refreshJobInstanceSchedules(earliestNewNotificationSchedule);
            }

            @Override
            public void triggerNotificationScan(String channelType, long earliestNewNotificationSchedule) {
                PartitionKey partitionKey = channelPartitionKeys.get(channelType);
                if (partitionKey == null) {
                    refreshJobInstanceSchedules(earliestNewNotificationSchedule);
                } else {
                    refreshJobInstanceSchedules(partitionKey, earliestNewNotificationSchedule);
                }
            }

            @Override
            public NotificationRecipientResolver getRecipientResolver() {
                return recipientResolver;
            }

            @Override
            public <T extends JobInstance<?>> JobInstanceProcessor<?, T> getJobInstanceProcessor(T jobInstance) {
                if (jobInstance instanceof Notification<?>) {
                    return (JobInstanceProcessor<?, T>) notificationProcessorFactory.createNotificationProcessor(this, (Notification<?>) jobInstance);
                } else {
                    return super.getJobInstanceProcessor(jobInstance);
                }
            }

            @Override
            public <T extends NotificationMessage> NotificationMessageResolver<T> getNotificationMessageResolver(Class<T> notificationMessageClass) {
                return getNotificationMessageResolver(notificationMessageClass, this);
            }

            public <T extends NotificationMessage> NotificationMessageResolver<T> getNotificationMessageResolver(Class<T> notificationMessageClass, ConfigurationSource configurationSource) {
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
                return getChannel(channelKey.getChannelType(), this);
            }

            @Override
            public <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelKey, ConfigurationSource configurationSource) {
                return getChannel(channelKey.getChannelType(), configurationSource);
            }

            @Override
            public <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(String channelKey) {
                return getChannel(channelKey, this);
            }

            @Override
            public <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(String channelKey, ConfigurationSource configurationSource) {
                if (channelKey == null) {
                    throw new IllegalArgumentException("Illegal null channel key!");
                }
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

            private static class ChannelMapKey {

                private final String channelKey;
                private final ConfigurationSource configurationSource;

                public ChannelMapKey(String channelKey, ConfigurationSource configurationSource) {
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
