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
import com.blazebit.notify.actor.ScheduledActor;
import com.blazebit.notify.job.event.JobInstanceListener;
import com.blazebit.notify.job.event.JobInstanceScheduleListener;
import com.blazebit.notify.job.event.JobTriggerListener;
import com.blazebit.notify.job.event.JobTriggerScheduleListener;
import com.blazebit.notify.job.spi.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface JobContext extends ConfigurationSource {

    <T> T getService(Class<T> serviceClass);

    TransactionSupport getTransactionSupport();

    JobManager getJobManager();

    ScheduleFactory getScheduleFactory();

    <T extends JobTrigger> JobProcessor<T> getJobProcessor(T job);

    <T extends JobInstance> JobInstanceProcessor<?, T> getJobInstanceProcessor(T job);

    void notifyJobTriggerScheduleListeners(JobTrigger jobTrigger);

    void notifyJobInstanceScheduleListeners(JobInstance jobInstance);

    void forEachJobTriggerListeners(Consumer<JobTriggerListener> listenerConsumer);

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
        private final List<JobTriggerScheduleListener> jobTriggerScheduleListeners = new ArrayList<>();
        private final List<JobInstanceScheduleListener> jobInstanceScheduleListeners = new ArrayList<>();
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

            jobTriggerScheduleListeners.addAll(loadServices(JobTriggerScheduleListener.class));
            jobInstanceScheduleListeners.addAll(loadServices(JobInstanceScheduleListener.class));
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
                    getJobTriggerScheduleListeners(),
                    getJobInstanceScheduleListeners(),
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

        public List<JobTriggerScheduleListener> getJobTriggerScheduleListeners() {
            return jobTriggerScheduleListeners;
        }

        public T withJobTriggerScheduleListener(JobTriggerScheduleListener jobTriggerScheduleListener) {
            this.jobTriggerScheduleListeners.add(jobTriggerScheduleListener);
            return (T) this;
        }

        public T withJobTriggerScheduleListeners(List<JobTriggerScheduleListener> jobTriggerScheduleListeners) {
            this.jobTriggerScheduleListeners.addAll(jobTriggerScheduleListeners);
            return (T) this;
        }

        public List<JobInstanceScheduleListener> getJobInstanceScheduleListeners() {
            return jobInstanceScheduleListeners;
        }

        public T withJobInstanceScheduleListener(JobInstanceScheduleListener jobInstanceScheduleListener) {
            this.jobInstanceScheduleListeners.add(jobInstanceScheduleListener);
            return (T) this;
        }

        public T withJobInstanceScheduleListeners(List<JobInstanceScheduleListener> jobInstanceScheduleListeners) {
            this.jobInstanceScheduleListeners.addAll(jobInstanceScheduleListeners);
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

            private final TransactionSupport transactionSupport;
            private final JobManager jobManager;
            private final ScheduleFactory scheduleFactory;
            private final JobProcessorFactory jobProcessorFactory;
            private final JobInstanceProcessorFactory jobInstanceProcessorFactory;
            private final JobScheduler jobScheduler;
            private final JobTriggerScheduleListener[] jobTriggerScheduleListeners;
            private final JobInstanceScheduleListener[] jobInstanceScheduleListeners;
            private final JobTriggerListener[] jobTriggerListeners;
            private final JobInstanceListener[] jobInstanceListeners;
            private final Map<String, Object> properties;
            private final Map<Class<?>, Object> serviceMap;

            protected DefaultJobContext(TransactionSupport transactionSupport, JobManagerFactory jobManagerFactory, ActorContext actorContext, ScheduleFactory scheduleFactory,
                                        JobSchedulerFactory jobSchedulerFactory, JobProcessorFactory jobProcessorFactory, JobInstanceProcessorFactory jobInstanceProcessorFactory,
                                        List<JobTriggerScheduleListener> jobTriggerScheduleListeners, List<JobInstanceScheduleListener> jobInstanceScheduleListeners,
                                        List<JobTriggerListener> jobTriggerListeners, List<JobInstanceListener> jobInstanceListeners,
                                        Map<String, Object> properties, Map<Class<?>, Object> serviceMap) {
                this.transactionSupport = transactionSupport;
                this.scheduleFactory = scheduleFactory;
                this.jobProcessorFactory = jobProcessorFactory;
                this.jobInstanceProcessorFactory = jobInstanceProcessorFactory;
                this.properties = new HashMap<>(properties);
                this.serviceMap = new HashMap<>(serviceMap);
                this.jobManager = jobManagerFactory.createJobManager(this);
                this.jobScheduler = jobSchedulerFactory.createJobScheduler(this, actorContext);
                jobTriggerScheduleListeners.add(jobScheduler::add);
                jobInstanceScheduleListeners.add(jobScheduler::add);
                this.jobTriggerScheduleListeners = jobTriggerScheduleListeners.toArray(new JobTriggerScheduleListener[jobTriggerScheduleListeners.size()]);
                this.jobInstanceScheduleListeners = jobInstanceScheduleListeners.toArray(new JobInstanceScheduleListener[jobInstanceScheduleListeners.size()]);
                this.jobTriggerListeners = jobTriggerListeners.toArray(new JobTriggerListener[jobTriggerListeners.size()]);
                this.jobInstanceListeners = jobInstanceListeners.toArray(new JobInstanceListener[jobInstanceListeners.size()]);
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
            public <T extends JobInstance> JobInstanceProcessor<?, T> getJobInstanceProcessor(T jobInstance) {
                return jobInstanceProcessorFactory.createJobInstanceProcessor(this, jobInstance);
            }

            @Override
            public void notifyJobTriggerScheduleListeners(JobTrigger jobTrigger) {
                if (jobTrigger.getJobConfiguration().isDone()) {
                    throw new JobException("JobTrigger is already done and can't be scheduled: " + jobTrigger);
                }
                for (int i = 0; i < jobTriggerScheduleListeners.length; i++) {
                    jobTriggerScheduleListeners[i].onJobTriggerSchedule(jobTrigger);
                }
            }

            @Override
            public void notifyJobInstanceScheduleListeners(JobInstance jobInstance) {
                if (jobInstance.getState() != JobInstanceState.NEW) {
                    throw new JobException("JobInstance is already done and can't be scheduled: " + jobInstance);
                }
                for (int i = 0; i < jobInstanceScheduleListeners.length; i++) {
                    jobInstanceScheduleListeners[i].onJobInstanceSchedule(jobInstance);
                }
            }

            @Override
            public void forEachJobTriggerListeners(Consumer<JobTriggerListener> listenerConsumer) {
                for (int i = 0; i < jobTriggerListeners.length; i++) {
                    listenerConsumer.accept(jobTriggerListeners[i]);
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
                jobScheduler.stop();
            }

            @Override
            public void stop(long timeout, TimeUnit unit) throws InterruptedException {
                jobScheduler.stop(timeout, unit);
            }
        }
    }
}
