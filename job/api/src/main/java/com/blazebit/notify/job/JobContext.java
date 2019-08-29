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

package com.blazebit.notify.job;

import com.blazebit.notify.actor.ActorContext;
import com.blazebit.notify.job.event.JobInstanceListener;
import com.blazebit.notify.job.event.JobTriggerListener;
import com.blazebit.notify.job.spi.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface JobContext extends ServiceProvider, ConfigurationSource {

    TransactionSupport getTransactionSupport();

    JobManager getJobManager();

    ScheduleFactory getScheduleFactory();

    <T extends JobTrigger> JobProcessor<T> getJobProcessor(T job);

    <T extends JobInstance<?>> JobInstanceProcessor<?, T> getJobInstanceProcessor(T job);

    void refreshJobInstanceSchedules(JobInstance<?> jobInstance);

    void refreshJobInstanceSchedules(long earliestNewSchedule);

    void refreshJobInstanceSchedules(PartitionKey partitionKey, long earliestNewSchedule);

    void forEachJobInstanceListeners(Consumer<JobInstanceListener> jobInstanceListenerConsumer);

    void stop();

    void stop(long timeout, TimeUnit unit) throws InterruptedException;

    class Builder extends BuilderBase<Builder> {

        public static Builder create() {
            Builder builder = new Builder();
            builder.loadDefaults();
            return builder;
        }
    }

    class BuilderBase<T extends BuilderBase<T>> {

        private TransactionSupport transactionSupport;
        private ActorContext actorContext;
        private ActorContext.Builder actorContextBuilder;
        private JobManagerFactory jobManagerFactory;
        private ScheduleFactory scheduleFactory;
        private JobSchedulerFactory jobSchedulerFactory;
        private JobProcessorFactory jobProcessorFactory;
        private JobInstanceProcessorFactory jobInstanceProcessorFactory;
        private PartitionKeyProviderFactory partitionKeyProviderFactory;
        private PartitionKeyProvider partitionKeyProvider;
        private final Map<PartitionKey, Integer> partitionKeys = new HashMap<>();
        private final List<JobTriggerListener> jobTriggerListeners = new ArrayList<>();
        private final List<JobInstanceListener> jobInstanceListeners = new ArrayList<>();
        private final Map<String, Object> properties = new HashMap<>();
        private final Map<Class<?>, Object> serviceMap = new HashMap<>();

        protected void loadDefaults() {
            transactionSupport = loadFirstServiceOrNone(TransactionSupport.class);
            if (transactionSupport == null) {
                transactionSupport = TransactionSupport.NOOP;
            }

            jobManagerFactory = loadFirstServiceOrNone(JobManagerFactory.class);
            scheduleFactory = loadFirstServiceOrNone(ScheduleFactory.class);
            jobSchedulerFactory = loadFirstServiceOrNone(JobSchedulerFactory.class);
            jobProcessorFactory = loadFirstServiceOrNone(JobProcessorFactory.class);
            jobInstanceProcessorFactory = loadFirstServiceOrNone(JobInstanceProcessorFactory.class);
            partitionKeyProviderFactory = loadFirstServiceOrNone(PartitionKeyProviderFactory.class);

            jobTriggerListeners.addAll(loadServices(JobTriggerListener.class));
            jobInstanceListeners.addAll(loadServices(JobInstanceListener.class));
        }

        protected static <X> X loadFirstServiceOrNone(Class<X> serviceClass) {
            Iterator<X> scheduleFactoryIterator = ServiceLoader.load(serviceClass).iterator();
            if (scheduleFactoryIterator.hasNext()) {
                X o = scheduleFactoryIterator.next();
                if (scheduleFactoryIterator.hasNext()) {
                    return null;
                }
                return o;
            }
            return null;
        }

        protected static <X> List<X> loadServices(Class<X> serviceClass) {
            List<X> list = new ArrayList<>();
            for (X service : ServiceLoader.load(serviceClass)) {
                list.add(service);
            }
            return list;
        }

        protected void checkCreateContext() {
            if (getTransactionSupport() == null) {
                throw new JobException("No transaction support given!");
            }
            if (getJobManagerFactory() == null) {
                throw new JobException("No job manager factory given!");
            }
            if (getScheduleFactory() == null) {
                throw new JobException("No schedule factory given!");
            }
            if (getJobSchedulerFactory() == null) {
                throw new JobException("No job scheduler factory given!");
            }
            if (getJobProcessorFactory() == null) {
                throw new JobException("No job processor factory given!");
            }
            if (getJobInstanceProcessorFactory() == null) {
                throw new JobException("No job instance processor factory given!");
            }
            if (getPartitionKeyProviderFactory() == null) {
                throw new JobException("No job instance partition key provider factory given!");
            }
        }

        public JobContext createContext() {
            checkCreateContext();
            return new DefaultJobContext(
                    transactionSupport,
                    getJobManagerFactory(),
                    getOrCreateActorContext(),
                    getScheduleFactory(),
                    getJobSchedulerFactory(),
                    getJobProcessorFactory(),
                    getJobInstanceProcessorFactory(),
                    getPartitionKeyMap(),
                    getPartitionKeyProvider(),
                    getJobTriggerListeners(),
                    getJobInstanceListeners(),
                    properties,
                    serviceMap
            );
        }

        protected ActorContext getOrCreateActorContext() {
            ActorContext actorContext = getActorContext();
            if (actorContext == null) {
                ActorContext.Builder builder = getActorContextBuilder();
                if (builder == null) {
                    builder = ActorContext.Builder.create();
                }
                builder.withProperties(properties);
                for (Map.Entry<Class<?>, Object> entry : serviceMap.entrySet()) {
                    builder.withService((Class<Object>) entry.getKey(), entry.getValue());
                }

                return builder.createContext();
            }
            return actorContext;
        }

        public TransactionSupport getTransactionSupport() {
            return transactionSupport;
        }

        public T withTransactionSupport(TransactionSupport transactionSupport) {
            this.transactionSupport = transactionSupport;
            return (T) this;
        }

        public JobManagerFactory getJobManagerFactory() {
            return jobManagerFactory;
        }

        public T withJobManagerFactory(JobManagerFactory jobManagerFactory) {
            this.jobManagerFactory = jobManagerFactory;
            return (T) this;
        }

        public ActorContext getActorContext() {
            return actorContext;
        }

        public T withActorContext(ActorContext actorContext) {
            this.actorContext = actorContext;
            return (T) this;
        }

        public ActorContext.Builder getActorContextBuilder() {
            return actorContextBuilder;
        }

        public T withActorContextBuilder(ActorContext.Builder actorContextBuilder) {
            this.actorContextBuilder = actorContextBuilder;
            return (T) this;
        }

        public ScheduleFactory getScheduleFactory() {
            return scheduleFactory;
        }

        public T withScheduleFactory(ScheduleFactory scheduleFactory) {
            this.scheduleFactory = scheduleFactory;
            return (T) this;
        }

        public JobProcessorFactory getJobProcessorFactory() {
            return jobProcessorFactory;
        }

        public T withJobProcessorFactory(JobProcessorFactory jobProcessorFactory) {
            this.jobProcessorFactory = jobProcessorFactory;
            return (T) this;
        }

        public JobInstanceProcessorFactory getJobInstanceProcessorFactory() {
            return jobInstanceProcessorFactory;
        }

        public T withJobInstanceProcessorFactory(JobInstanceProcessorFactory jobInstanceProcessorFactory) {
            this.jobInstanceProcessorFactory = jobInstanceProcessorFactory;
            return (T) this;
        }

        public JobSchedulerFactory getJobSchedulerFactory() {
            return jobSchedulerFactory;
        }

        public T withJobSchedulerFactory(JobSchedulerFactory jobSchedulerFactory) {
            this.jobSchedulerFactory = jobSchedulerFactory;
            return (T) this;
        }

        public Set<PartitionKey> getPartitionKeys() {
            return partitionKeys.keySet();
        }

        protected Map<PartitionKey, Integer> getPartitionKeyMap() {
            return partitionKeys;
        }

        public T withPartitionKey(PartitionKey partitionKey, int processingCount) {
            this.partitionKeys.put(partitionKey, processingCount);
            return (T) this;
        }

        protected PartitionKeyProvider getPartitionKeyProvider() {
            if (partitionKeyProvider == null) {
                partitionKeyProvider = partitionKeyProviderFactory.createPartitionKeyProvider(
                        new ServiceProvider() {
                            @Override
                            public <T> T getService(Class<T> serviceClass) {
                                return serviceClass.cast(getServiceMap().get(serviceClass));
                            }
                        },
                        this::getProperty
                );
            }
            return partitionKeyProvider;
        }

        public PartitionKeyProviderFactory getPartitionKeyProviderFactory() {
            return partitionKeyProviderFactory;
        }

        public T withPartitionKeyProviderFactory(PartitionKeyProviderFactory partitionKeyProviderFactory) {
            this.partitionKeyProviderFactory = partitionKeyProviderFactory;
            this.partitionKeyProvider = null;
            return (T) this;
        }

        public List<JobTriggerListener> getJobTriggerListeners() {
            return jobTriggerListeners;
        }

        public T withJobTriggerListener(JobTriggerListener jobTriggerListener) {
            this.jobTriggerListeners.add(jobTriggerListener);
            return (T) this;
        }

        public T withJobTriggerListeners(List<JobTriggerListener> jobTriggerListeners) {
            this.jobTriggerListeners.addAll(jobTriggerListeners);
            return (T) this;
        }

        public List<JobInstanceListener> getJobInstanceListeners() {
            return jobInstanceListeners;
        }

        public T withJobInstanceListener(JobInstanceListener jobInstanceListener) {
            this.jobInstanceListeners.add(jobInstanceListener);
            return (T) this;
        }

        public T withJobInstanceListeners(List<JobInstanceListener> jobInstanceListeners) {
            this.jobInstanceListeners.addAll(jobInstanceListeners);
            return (T) this;
        }

        protected Map<String, Object> getProperties() {
            return properties;
        }

        public Object getProperty(String property) {
            return properties.get(property);
        }

        public T withProperty(String property, Object value) {
            this.properties.put(property, value);
            return (T) this;
        }

        public T withProperties(Map<String, Object> properties) {
            this.properties.putAll(properties);
            return (T) this;
        }

        protected Map<Class<?>, Object> getServiceMap() {
            return serviceMap;
        }

        public Collection<Object> getServices() {
            return serviceMap.values();
        }

        public <X> T withService(Class<X> serviceClass, X service) {
            this.serviceMap.put(serviceClass, service);
            return (T) this;
        }

        protected static class DefaultJobContext implements JobContext {
            private static final String DEFAULT_JOB_INSTANCE_ACTOR_NAME = "jobInstanceScheduler";
            private static final int DEFAULT_JOB_INSTANCE_PROCESS_COUNT = 1;
            private static final String DEFAULT_JOB_TRIGGER_ACTOR_NAME = "jobTriggerScheduler";
            private static final int DEFAULT_JOB_TRIGGER_PROCESS_COUNT = 1;

            private final TransactionSupport transactionSupport;
            private final JobManager jobManager;
            private final ScheduleFactory scheduleFactory;
            private final JobProcessorFactory jobProcessorFactory;
            private final JobInstanceProcessorFactory jobInstanceProcessorFactory;
            private final PartitionKeyProvider partitionKeyProvider;
            private final Map<PartitionKey, JobScheduler> jobSchedulers;
            private final Map<Class<?>, List<PartitionKey>> jobInstanceClassToPartitionKeysMapping = new ConcurrentHashMap<>();
            private final JobInstanceListener[] jobInstanceListeners;
            private final Map<String, Object> properties;
            private final Map<Class<?>, Object> serviceMap;

            protected DefaultJobContext(TransactionSupport transactionSupport, JobManagerFactory jobManagerFactory, ActorContext actorContext, ScheduleFactory scheduleFactory,
                                        JobSchedulerFactory jobSchedulerFactory, JobProcessorFactory jobProcessorFactory, JobInstanceProcessorFactory jobInstanceProcessorFactory,
                                        Map<PartitionKey, Integer> partitionKeyEntries, PartitionKeyProvider partitionKeyProvider, List<JobTriggerListener> jobTriggerListeners, List<JobInstanceListener> jobInstanceListeners,
                                        Map<String, Object> properties, Map<Class<?>, Object> serviceMap) {
                this.transactionSupport = transactionSupport;
                this.scheduleFactory = scheduleFactory;
                this.jobProcessorFactory = jobProcessorFactory;
                this.jobInstanceProcessorFactory = jobInstanceProcessorFactory;
                this.properties = new HashMap<>(properties);
                this.serviceMap = new HashMap<>(serviceMap);
                this.jobManager = jobManagerFactory.createJobManager(this);
                if (partitionKeyProvider == null) {
                    throw new JobException("No PartitionKeyProvider given!");
                } else {
                    this.partitionKeyProvider = partitionKeyProvider;
                }
                Collection<PartitionKey> defaultTriggerPartitionKeys = this.partitionKeyProvider.getDefaultTriggerPartitionKeys();
                if (partitionKeyEntries.isEmpty()) {
                    Collection<PartitionKey> instancePartitionKeys = this.partitionKeyProvider.getDefaultJobInstancePartitionKeys();
                    this.jobSchedulers = new HashMap<>(defaultTriggerPartitionKeys.size() + instancePartitionKeys.size());
                    for (PartitionKey instancePartitionKey : instancePartitionKeys) {
                        JobScheduler jobInstanceScheduler = jobSchedulerFactory.createJobScheduler(this, actorContext, DEFAULT_JOB_INSTANCE_ACTOR_NAME, DEFAULT_JOB_INSTANCE_PROCESS_COUNT, instancePartitionKey);
                        jobSchedulers.put(instancePartitionKey, jobInstanceScheduler);
                    }
                } else {
                    this.jobSchedulers = new HashMap<>(defaultTriggerPartitionKeys.size() + partitionKeyEntries.size());
                    for (Map.Entry<PartitionKey, Integer> entry : partitionKeyEntries.entrySet()) {
                        jobSchedulers.put(entry.getKey(), jobSchedulerFactory.createJobScheduler(this, actorContext, DEFAULT_JOB_INSTANCE_ACTOR_NAME + "/" + entry.getKey(), entry.getValue(), entry.getKey()));
                    }
                }
                for (PartitionKey jobTriggerPartitionKey : defaultTriggerPartitionKeys) {
                    jobSchedulers.put(jobTriggerPartitionKey, jobSchedulerFactory.createJobScheduler(this, actorContext, DEFAULT_JOB_TRIGGER_ACTOR_NAME, DEFAULT_JOB_TRIGGER_PROCESS_COUNT, jobTriggerPartitionKey));
                }

                jobInstanceListeners.addAll(jobTriggerListeners);
                this.jobInstanceListeners = jobInstanceListeners.toArray(new JobInstanceListener[jobInstanceListeners.size()]);
                afterConstruct();
            }

            protected void afterConstruct() {
                start();
            }

            protected void start() {
                for (JobScheduler jobScheduler : jobSchedulers.values()) {
                    jobScheduler.start();
                }
            }

            @Override
            public Object getProperty(String property) {
                return properties.get(property);
            }

            @Override
            public <T> T getService(Class<T> serviceClass) {
                return (T) serviceMap.get(serviceClass);
            }

            @Override
            public TransactionSupport getTransactionSupport() {
                return transactionSupport;
            }

            @Override
            public JobManager getJobManager() {
                return jobManager;
            }

            @Override
            public ScheduleFactory getScheduleFactory() {
                return scheduleFactory;
            }

            @Override
            public <T extends JobTrigger> JobProcessor<T> getJobProcessor(T jobTrigger) {
                return jobProcessorFactory.createJobProcessor(this, jobTrigger);
            }

            @Override
            public <T extends JobInstance<?>> JobInstanceProcessor<?, T> getJobInstanceProcessor(T jobInstance) {
                if (jobInstance instanceof JobTrigger) {
                    return (JobInstanceProcessor<?, T>) jobProcessorFactory.createJobProcessor(this, (JobTrigger) jobInstance);
                } else {
                    return jobInstanceProcessorFactory.createJobInstanceProcessor(this, jobInstance);
                }
            }

            @Override
            public void refreshJobInstanceSchedules(JobInstance<?> jobInstance) {
                if (jobInstance.getState() != JobInstanceState.NEW) {
                    throw new JobException("JobInstance is already done and can't be scheduled: " + jobInstance);
                }
                long earliestNewSchedule = jobInstance.getScheduleTime().toEpochMilli();
                List<PartitionKey> partitionKeys = getPartitionKeys(jobInstance);
                for (int i = 0; i < partitionKeys.size(); i++) {
                    jobSchedulers.get(partitionKeys.get(i)).refreshSchedules(earliestNewSchedule);
                }
            }

            private List<PartitionKey> getPartitionKeys(JobInstance<?> jobInstance) {
                return jobInstanceClassToPartitionKeysMapping.computeIfAbsent(jobInstance.getClass(), (k) -> {
                    List<PartitionKey> v = new ArrayList<>(jobSchedulers.keySet().size());
                    for (PartitionKey partitionKey : jobSchedulers.keySet()) {
                        if (partitionKey.getJobInstanceType().isAssignableFrom(k)) {
                            v.add(partitionKey);
                        }
                    }
                    return v;
                });
            }

            @Override
            public void refreshJobInstanceSchedules(long earliestNewSchedule) {
                for (JobScheduler jobScheduler : jobSchedulers.values()) {
                    jobScheduler.refreshSchedules(earliestNewSchedule);
                }
            }

            @Override
            public void refreshJobInstanceSchedules(PartitionKey partitionKey, long earliestNewSchedule) {
                JobScheduler jobScheduler = jobSchedulers.get(partitionKey);
                if (jobScheduler != null) {
                    jobScheduler.refreshSchedules(earliestNewSchedule);
                }
            }

            @Override
            public void forEachJobInstanceListeners(Consumer<JobInstanceListener> jobInstanceListenerConsumer) {
                for (int i = 0; i < jobInstanceListeners.length; i++) {
                    jobInstanceListenerConsumer.accept(jobInstanceListeners[i]);
                }
            }

            @Override
            public void stop() {
                for (JobScheduler jobScheduler : jobSchedulers.values()) {
                    jobScheduler.stop();
                }
            }

            @Override
            public void stop(long timeout, TimeUnit unit) throws InterruptedException {
                for (JobScheduler jobScheduler : jobSchedulers.values()) {
                    jobScheduler.stop(timeout, unit);
                }
            }
        }
    }
}
