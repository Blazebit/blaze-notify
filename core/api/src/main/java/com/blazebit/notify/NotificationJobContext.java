/*
 * Copyright 2018 - 2019 Blazebit.
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

package com.blazebit.notify;

import com.blazebit.actor.ActorContext;
import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.JobContext;
import com.blazebit.job.JobException;
import com.blazebit.job.JobInstance;
import com.blazebit.job.JobInstanceProcessor;
import com.blazebit.job.PartitionKey;
import com.blazebit.job.ServiceProvider;
import com.blazebit.job.JobInstanceListener;
import com.blazebit.job.JobTriggerListener;
import com.blazebit.job.spi.JobInstanceProcessorFactory;
import com.blazebit.job.spi.JobManagerFactory;
import com.blazebit.job.spi.JobProcessorFactory;
import com.blazebit.job.spi.JobSchedulerFactory;
import com.blazebit.job.spi.PartitionKeyProvider;
import com.blazebit.job.spi.ScheduleFactory;
import com.blazebit.job.spi.TransactionSupport;
import com.blazebit.notify.spi.NotificationPartitionKeyProvider;
import com.blazebit.notify.spi.NotificationPartitionKeyProviderFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A closable context in which notification jobs and normal jobs can run.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationJobContext extends JobContext {

    /**
     * Returns the resolver for recipients of a notification job instance.
     *
     * @return the resolver for recipients of a notification job instance
     */
    NotificationRecipientResolver getRecipientResolver();

    /**
     * Triggers a scan for new notifications if the given earliest new notification schedule is lower than the known earliest schedule.
     *
     * @param earliestNewNotificationSchedule The new earliest notification schedule
     */
    void triggerNotificationScan(long earliestNewNotificationSchedule);

    /**
     * Triggers a scan for new notifications for a channel if the given earliest new notification schedule is lower than the known earliest schedule for that channel.
     *
     * @param channelType The channel to trigger notification scanning for
     * @param earliestNewNotificationSchedule The new earliest notification schedule for the channel
     */
    void triggerNotificationScan(String channelType, long earliestNewNotificationSchedule);

    /**
     * Returns a {@link NotificationMessageResolver} for the given notification message class using this {@link NotificationJobContext} as configuration source.
     *
     * @param notificationMessageClass The notification message class
     * @param <M> The notification message type
     * @return a {@link NotificationMessageResolver}
     */
    <M extends NotificationMessage> NotificationMessageResolver<M> getNotificationMessageResolver(Class<M> notificationMessageClass);

    /**
     * Returns a {@link NotificationMessageResolver} for the given notification message class and the given configuration source.
     *
     * @param notificationMessageClass The notification message class
     * @param configurationSource The configuration source to use for the resolver
     * @param <M> The notification message type
     * @return a {@link NotificationMessageResolver}
     */
    <M extends NotificationMessage> NotificationMessageResolver<M> getNotificationMessageResolver(Class<M> notificationMessageClass, ConfigurationSource configurationSource);

    /**
     * Returns a {@link Channel} for the given channel type using this {@link NotificationJobContext} as the configuration source.
     *
     * @param channelType The channel type identifier
     * @param <T> The channel type
     * @return a {@link Channel}
     */
    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(String channelType);

    /**
     * Returns a {@link Channel} for the given channel type and the given configuration source.
     *
     * @param channelType The channel type identifier
     * @param configurationSource The configuration source
     * @param <T> The channel type
     * @return a {@link Channel}
     */
    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(String channelType, ConfigurationSource configurationSource);

    /**
     * Returns a {@link Channel} for the given channel key using this {@link NotificationJobContext} as the configuration source.
     *
     * @param channelKey The channel key
     * @param <T> The channel type
     * @return a {@link Channel}
     */
    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelKey);

    /**
     * Returns a {@link Channel} for the given channel key and the given configuration source.
     *
     * @param channelKey The channel key
     * @param configurationSource The configuration source
     * @param <T> The channel type
     * @return a {@link Channel}
     */
    <T extends Channel<? extends NotificationRecipient<?>, ? extends NotificationMessage>> T getChannel(ChannelKey<T> channelKey, ConfigurationSource configurationSource);

    /**
     * Returns a builder for a notification job context.
     *
     * @return a builder for a notification job context
     */
    static Builder builder() {
        Builder builder = new Builder();
        builder.loadDefaults();
        return builder;
    }

    /**
     * A builder for a notification job context.
     *
     * @author Christian Beikov
     * @since 1.0.0
     */
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
                Collection<PartitionKey> defaultJobInstancePartitionKeys = partitionKeyProvider.getDefaultJobInstancePartitionKeys();
                partitionKeyMap = new HashMap<>(defaultJobInstancePartitionKeys.size() * (channelFactories.size() + 1));
                if (defaultJobInstancePartitionKeys.size() == 1) {
                    PartitionKey defaultJobInstancePartitionKey = defaultJobInstancePartitionKeys.iterator().next();
                    partitionKeyMap.put(notificationPartitionKeyProvider.getDefaultJobInstancePartitionKey(defaultJobInstancePartitionKey), DEFAULT_JOB_INSTANCE_PROCESS_COUNT);
                } else {
                    for (PartitionKey defaultJobInstancePartitionKey : defaultJobInstancePartitionKeys) {
                        if (Notification.class.isAssignableFrom(defaultJobInstancePartitionKey.getJobInstanceType())) {
                            for (String channelType : channelFactories.keySet()) {
                                PartitionKey partitionKey = notificationPartitionKeyProvider.getPartitionKey(defaultJobInstancePartitionKey, channelType);
                                channelPartitionKeys.put(channelType, partitionKey);
                                partitionKeyMap.put(partitionKey, DEFAULT_NOTIFICATION_PROCESS_COUNT);
                            }
                        } else {
                            partitionKeyMap.put(notificationPartitionKeyProvider.getPartitionKey(defaultJobInstancePartitionKey, null), DEFAULT_JOB_INSTANCE_PROCESS_COUNT);
                        }
                    }
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
            return new PartitionKeyProvider() {
                @Override
                public Collection<PartitionKey> getDefaultTriggerPartitionKeys() {
                    PartitionKeyProvider partitionKeyProvider = Builder.super.getPartitionKeyProvider();
                    NotificationPartitionKeyProvider notificationPartitionKeyProvider = getNotificationPartitionKeyProvider();
                    Collection<PartitionKey> defaultTriggerPartitionKeys = partitionKeyProvider.getDefaultTriggerPartitionKeys();
                    List<PartitionKey> newPartitionKeys = new ArrayList<>(defaultTriggerPartitionKeys.size());
                    for (PartitionKey defaultTriggerPartitionKey : defaultTriggerPartitionKeys) {
                        newPartitionKeys.add(notificationPartitionKeyProvider.getDefaultTriggerPartitionKey(defaultTriggerPartitionKey));
                    }

                    return newPartitionKeys;
                }

                @Override
                public Collection<PartitionKey> getDefaultJobInstancePartitionKeys() {
                    PartitionKeyProvider partitionKeyProvider = Builder.super.getPartitionKeyProvider();
                    NotificationPartitionKeyProvider notificationPartitionKeyProvider = getNotificationPartitionKeyProvider();
                    Collection<PartitionKey> defaultJobInstancePartitionKeys = partitionKeyProvider.getDefaultJobInstancePartitionKeys();
                    List<PartitionKey> newPartitionKeys = new ArrayList<>(defaultJobInstancePartitionKeys.size());
                    for (PartitionKey defaultJobInstancePartitionKey : defaultJobInstancePartitionKeys) {
                        newPartitionKeys.add(notificationPartitionKeyProvider.getDefaultJobInstancePartitionKey(defaultJobInstancePartitionKey));
                    }

                    return newPartitionKeys;
                }
            };
        }

        /**
         * Returns the configured notification partition key provider.
         *
         * @return the configured notification partition key provider
         */
        protected NotificationPartitionKeyProvider getNotificationPartitionKeyProvider() {
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

        /**
         * Returns the configured notification partition key provider factory.
         *
         * @return the configured notification partition key provider factory
         */
        public NotificationPartitionKeyProviderFactory getNotificationPartitionKeyProviderFactory() {
            return notificationPartitionKeyProviderFactory;
        }

        /**
         * Sets the given notification partition key provider factory.
         *
         * @param notificationPartitionKeyProviderFactory The notification partition key provider factory
         * @return this for chaining
         */
        public Builder withNotificationPartitionKeyProviderFactory(NotificationPartitionKeyProviderFactory notificationPartitionKeyProviderFactory) {
            this.notificationPartitionKeyProviderFactory = notificationPartitionKeyProviderFactory;
            this.notificationPartitionKeyProvider = null;
            return this;
        }

        /**
         * Returns the configured recipient resolver.
         *
         * @return the configured recipient resolver
         */
        public NotificationRecipientResolver getRecipientResolver() {
            return recipientResolver;
        }

        /**
         * Sets the given recipient resolver.
         *
         * @param recipientResolver The recipient resolver
         * @return this for chaining
         */
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

        /**
         * Sets the given notification job processor factory.
         *
         * @param notificationJobProcessorFactory The notification job processor factory
         * @return this for chaining
         */
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

        /**
         * Sets the given notification job instance processor factory.
         *
         * @param notificationJobInstanceProcessorFactory The notification job instance processor factory
         * @return this for chaining
         */
        public Builder withJobInstanceProcessorFactory(NotificationJobInstanceProcessorFactory notificationJobInstanceProcessorFactory) {
            this.notificationJobInstanceProcessorFactory = notificationJobInstanceProcessorFactory;
            return this;
        }

        /**
         * Returns the configured notification processor factory.
         *
         * @return the configured notification processor factory
         */
        public NotificationProcessorFactory getNotificationProcessorFactory() {
            return notificationProcessorFactory;
        }

        /**
         * Sets the given notification processor factory.
         *
         * @param notificationProcessorFactory The notification processor factory
         * @return this for chaining
         */
        public Builder withNotificationProcessorFactory(NotificationProcessorFactory notificationProcessorFactory) {
            this.notificationProcessorFactory = notificationProcessorFactory;
            return this;
        }

        /**
         * Returns the configured channel factories.
         *
         * @return the configured channel factories
         */
        public Map<String, ChannelFactory<?>> getChannelFactories() {
            return channelFactories;
        }

        /**
         * Sets the given channel factory.
         *
         * @param channelFactory The channel factory
         * @return this for chaining
         */
        public Builder withChannelFactory(ChannelFactory channelFactory) {
            channelFactories.put(channelFactory.getChannelType().getChannelType(), channelFactory);
            return this;
        }

        /**
         * Returns the configured message resolver factories.
         *
         * @return the configured message resolver factories
         */
        protected Map<Class<? extends NotificationMessage>, NotificationMessageResolverFactory<?>> getMessageResolverFactories() {
            return messageResolverFactories;
        }

        /**
         * Registers the given message resolver factory.
         *
         * @param messageResolverFactory The message resolver factory
         * @return this for chaining
         */
        public Builder withMessageResolverFactory(NotificationMessageResolverFactory<?> messageResolverFactory) {
            this.messageResolverFactories.put(messageResolverFactory.getNotificationMessageType(), messageResolverFactory);
            return this;
        }

        /**
         * A basic implementation of the {@link NotificationJobContext} interface.
         *
         * @author Christian Beikov
         * @since 1.0.0
         */
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

            @Override
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

            /**
             * A map key for a channel.
             *
             * @author Christian Beikov
             * @since 1.0.0
             */
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

            /**
             * A map key for a message resolver.
             *
             * @author Christian Beikov
             * @since 1.0.0
             */
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