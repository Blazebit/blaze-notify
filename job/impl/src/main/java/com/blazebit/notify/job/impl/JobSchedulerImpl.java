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
package com.blazebit.notify.job.impl;

import com.blazebit.notify.actor.ActorContext;
import com.blazebit.notify.actor.ActorRunResult;
import com.blazebit.notify.actor.ScheduledActor;
import com.blazebit.notify.actor.spi.ClusterNodeInfo;
import com.blazebit.notify.actor.spi.ClusterStateListener;
import com.blazebit.notify.actor.spi.ClusterStateManager;
import com.blazebit.notify.job.*;
import com.blazebit.notify.job.spi.JobScheduler;
import com.blazebit.notify.job.spi.TransactionSupport;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobSchedulerImpl implements JobScheduler, ClusterStateListener {

    private static final Logger LOG = Logger.getLogger(JobSchedulerImpl.class.getName());
    private static final int DEFAULT_PROCESS_COUNT = 100;

    private final JobContext jobContext;
    private final JobManager jobManager;
    private final ActorContext actorContext;
    private final int processCount;
    private final ClusterStateManager clusterStateManager;
    private final ConcurrentMap<String, Object> registeredJobs = new ConcurrentHashMap<>();
    private volatile AtomicReference<ClusterNodeInfo> clusterNodeInfo = new AtomicReference<>();
    private volatile boolean closed;

    public JobSchedulerImpl(JobContext jobContext, ActorContext actorContext) {
        this(jobContext, actorContext, DEFAULT_PROCESS_COUNT);
    }

    public JobSchedulerImpl(JobContext jobContext, ActorContext actorContext, int processCount) {
        this.jobContext = jobContext;
        this.jobManager = jobContext.getJobManager();
        this.actorContext = actorContext;
        this.processCount = processCount;
        this.clusterStateManager = actorContext.getService(ClusterStateManager.class);
        this.clusterStateManager.registerListener(JobTriggerAddedEvent.class, e -> {
            ClusterNodeInfo clusterNodeInfo = this.clusterNodeInfo.get();
            if (clusterNodeInfo.getClusterSize() == 1 || (e.getId() % clusterNodeInfo.getClusterSize()) == clusterNodeInfo.getClusterPosition()) {
                addLocally(jobManager.getJobTrigger(e.getId()), clusterNodeInfo);
            }
        });
        this.clusterStateManager.registerListener(JobInstanceAddedEvent.class, e -> {
            ClusterNodeInfo clusterNodeInfo = this.clusterNodeInfo.get();
            if (clusterNodeInfo.getClusterSize() == 1 || (e.getId() % clusterNodeInfo.getClusterSize()) == clusterNodeInfo.getClusterPosition()) {
                addLocally(jobManager.getJobInstance(e.getId()), clusterNodeInfo);
            }
        });
        actorContext.getService(ClusterStateManager.class).registerListener(this);
    }

    @Override
    public void onClusterStateChanged(ClusterNodeInfo clusterNodeInfo) {
        if (!closed) {
            ClusterNodeInfo oldClusterNodeInfo = this.clusterNodeInfo.getAndSet(clusterNodeInfo);
            if (oldClusterNodeInfo != null) {
                String prefix = oldClusterNodeInfo.getClusterVersion() + "/";

                // Remove old job versions
                for (Iterator<String> iterator = registeredJobs.keySet().iterator(); iterator.hasNext(); ) {
                    String name = iterator.next();
                    if (name.startsWith(prefix)) {
                        actorContext.getActorManager().removeActor(name);
                        iterator.remove();
                    }
                }

                // Register new job versions
                List<JobTrigger> undoneJobTriggers = jobManager.getUndoneJobTriggers(clusterNodeInfo.getClusterPosition(), clusterNodeInfo.getClusterSize());
                for (int i = 0; i < undoneJobTriggers.size(); i++) {
                    addLocally(undoneJobTriggers.get(i), clusterNodeInfo);
                }
                List<JobInstance> undoneJobInstances = jobManager.getUndoneJobInstances(clusterNodeInfo.getClusterPosition(), clusterNodeInfo.getClusterSize());
                for (int i = 0; i < undoneJobInstances.size(); i++) {
                    addLocally(undoneJobInstances.get(i), clusterNodeInfo);
                }
            }
        }
    }

    @Override
    public void add(JobTrigger jobTrigger) {
        if (jobTrigger.getJobConfiguration().isDone()) {
            throw new JobException("JobTrigger is already done and can't be scheduled: " + jobTrigger);
        }
        // Only queue if part of current partition, otherwise inform cluster
        ClusterNodeInfo clusterNodeInfo = this.clusterNodeInfo.get();
        if (clusterNodeInfo.getClusterSize() == 1 || (jobTrigger.getId() % clusterNodeInfo.getClusterSize()) == clusterNodeInfo.getClusterPosition()) {
            addLocally(jobTrigger, clusterNodeInfo);
        } else {
            clusterStateManager.fireEventExcludeSelf(new JobTriggerAddedEvent(jobTrigger.getId()));
        }
    }

    @Override
    public void add(JobInstance jobInstance) {
        if (jobInstance.getState() != JobInstanceState.NEW) {
            throw new JobException("JobInstance is already done and can't be scheduled: " + jobInstance);
        }
        // Only queue if part of current partition, otherwise inform cluster
        ClusterNodeInfo clusterNodeInfo = this.clusterNodeInfo.get();
        if (clusterNodeInfo.getClusterSize() == 1 || (jobInstance.getId() % clusterNodeInfo.getClusterSize()) == clusterNodeInfo.getClusterPosition()) {
            addLocally(jobInstance, clusterNodeInfo);
        } else {
            clusterStateManager.fireEventExcludeSelf(new JobInstanceAddedEvent(jobInstance.getId()));
        }
    }

    private void addLocally(JobTrigger jobTrigger, ClusterNodeInfo clusterNodeInfo) {
        queue(clusterNodeInfo.getClusterVersion() + "/jobTrigger/" + jobTrigger.getId(), new JobRunner(jobTrigger));
    }

    private void addLocally(JobInstance jobInstance, ClusterNodeInfo clusterNodeInfo) {
        queue(clusterNodeInfo.getClusterVersion() + "/jobInstance/" + jobInstance.getId(), new JobInstanceRunner(jobInstance));
    }

    private boolean queue(String name, AbstractRunner jobRunner) {
        if (!closed) {
            final long nextSchedule = jobRunner.nextSchedule();
            jobRunner.onSchedule(nextSchedule);
            long delayMillis = nextSchedule - System.currentTimeMillis();
            delayMillis = delayMillis < 0 ? 0 : delayMillis;
            if (registeredJobs.putIfAbsent(name, name) == null) {
                actorContext.getActorManager().registerActor(name, jobRunner, delayMillis);
                return true;
            }
        }

        return false;
    }

    @Override
    public void stop() {
        closed = true;
        actorContext.stop();
    }

    @Override
    public void stop(long timeout, TimeUnit unit) throws InterruptedException {
        closed = true;
        actorContext.stop(timeout, unit);
    }

    private abstract class AbstractRunner implements ScheduledActor {

        protected final MutableScheduleContext scheduleContext = new MutableScheduleContext();

        protected void onSchedule(long nextSchedule) {
            scheduleContext.setLastScheduledExecutionTime(nextSchedule);
        }

        protected abstract long nextSchedule();

    }

    private class JobRunner extends AbstractRunner {
        private final JobTrigger jobTrigger;
        private final Schedule schedule;
        private final JobProcessor jobProcessor;

        private JobRunner(JobTrigger jobTrigger) {
            this.jobTrigger = jobTrigger;
            this.schedule = jobTrigger.getSchedule(jobContext);
            if (schedule == null) {
                throw new JobException("Invalid null schedule for job trigger: " + jobTrigger);
            }
            this.jobProcessor = jobContext.getJobProcessor(jobTrigger);
        }

        @Override
        protected long nextSchedule() {
            return schedule.nextEpochSchedule(scheduleContext);
        }

        @Override
        @SuppressWarnings({ "rawtypes" })
        public ActorRunResult work() {
            TransactionSupport transactionSupport = jobContext.getTransactionSupport();
            // TODO: make configurable
            long transactionTimeout = 60_000L;
            Instant lastExecutionTime = jobTrigger.getLastExecutionTime();
            if (lastExecutionTime == null) {
                lastExecutionTime = Instant.now();
                jobTrigger.setLastExecutionTime(lastExecutionTime);
            }
            scheduleContext.setLastActualExecutionTime(lastExecutionTime.toEpochMilli());

            ActorRunResult result = transactionSupport.transactional(jobContext, transactionTimeout, false, () -> {
                        jobProcessor.process(jobTrigger, jobContext);
                        jobContext.forEachJobTriggerListeners(listener -> {
                            listener.onJobTriggerSuccess(jobTrigger, jobContext);
                        });
                        jobContext.getJobManager().onJobTriggerSuccess(jobTrigger, jobContext);

                        scheduleContext.setLastCompletionTime(System.currentTimeMillis());

                        if (jobTrigger.getJobConfiguration().isDone()) {
                            onSuccess();
                            return ActorRunResult.done();
                        } else {
                            final long nextSchedule = nextSchedule();
                            // This is essential for limited time or fixed time schedules. When these are done, they always return a nextSchedule equal to getLastScheduledExecutionTime()
                            if (nextSchedule == scheduleContext.getLastScheduledExecutionTime()) {
                                // We are done with this job
                                onSuccess();
                                return ActorRunResult.done();
                            } else {
                                // This is a recurring job that needs rescheduling
                                return ActorRunResult.rescheduleIn(System.currentTimeMillis() - nextSchedule);
                            }
                        }
                    },
                    t -> {
                        LOG.log(Level.SEVERE, "An error occurred in the job processor", t);
                        transactionSupport.transactional(jobContext, transactionTimeout, true, () -> {
                            onError(t);
                            return null;
                        }, t2 -> {
                            LOG.log(Level.SEVERE, "An error occurred in the job error handler", t2);
                        });
                    }
            );

            if (result == null || closed) {
                return ActorRunResult.done();
            }
            return result;
        }

        private void onError(Throwable e) {
            jobContext.forEachJobTriggerListeners(listener -> {
                listener.onJobTriggerError(jobTrigger, jobContext);
            });
            jobContext.getJobManager().onJobTriggerError(jobTrigger, jobContext);
        }

        private void onSuccess() {
            jobContext.forEachJobTriggerListeners(listener -> {
                listener.onJobTriggerEnded(jobTrigger, jobContext);
            });
            jobContext.getJobManager().onJobTriggerEnded(jobTrigger, jobContext);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof JobRunner)) {
                return false;
            }

            JobRunner jobRunner = (JobRunner) o;

            return jobTrigger.equals(jobRunner.jobTrigger);
        }

        @Override
        public int hashCode() {
            return jobTrigger.hashCode();
        }
    }

    private class JobInstanceRunner extends AbstractRunner {
        private final MutableJobInstanceProcessingContext jobProcessingContext;
        private final JobInstance jobInstance;
        private final JobInstanceProcessor jobInstanceProcessor;

        public JobInstanceRunner(JobInstance jobInstance) {
            this.jobProcessingContext = new MutableJobInstanceProcessingContext(jobContext, processCount);
            this.jobInstance = jobInstance;
            if (jobInstance.getScheduleTime() == null) {
                throw new JobException("Invalid null schedule time for job instance: " + jobInstance);
            }
            this.jobInstanceProcessor = jobContext.getJobInstanceProcessor(jobInstance);
        }

        @Override
        protected long nextSchedule() {
            return jobInstance.getScheduleTime().toEpochMilli();
        }

        @Override
        @SuppressWarnings({ "rawtypes" })
        public ActorRunResult work() {
            TransactionSupport transactionSupport = jobContext.getTransactionSupport();
            // TODO: make configurable
            long transactionTimeout = 60_000L;
            Instant lastExecutionTime = jobInstance.getLastExecutionTime();
            if (lastExecutionTime == null) {
                lastExecutionTime = Instant.now();
                jobInstance.setLastExecutionTime(lastExecutionTime);
            }
            scheduleContext.setLastActualExecutionTime(lastExecutionTime.toEpochMilli());

            ActorRunResult result = transactionSupport.transactional(jobContext, transactionTimeout, false, () -> {
                        int deferCount = jobInstance.getDeferCount();
                        Object lastProcessed = process();
                        if (jobInstance.getState() == JobInstanceState.NEW && jobInstance.getDeferCount() == deferCount) {
                            onChunkSuccess(lastProcessed);
                        }
                        scheduleContext.setLastCompletionTime(System.currentTimeMillis());

                        if (jobInstance.getState() != JobInstanceState.NEW) {
                            onSuccess();
                            return ActorRunResult.done();
                        } else if (lastProcessed == null) {
                            final long nextSchedule = nextSchedule();
                            // This is essential for limited time or fixed time schedules. When these are done, they always return a nextSchedule equal to getLastScheduledExecutionTime()
                            if (nextSchedule == scheduleContext.getLastScheduledExecutionTime()) {
                                // We are done with this job
                                onSuccess();
                                return ActorRunResult.done();
                            } else {
                                // This is a recurring job that needs rescheduling
                                return ActorRunResult.rescheduleIn(System.currentTimeMillis() - nextSchedule);
                            }
                        } else {
                            // Reschedule the next chunk as soon as possible
                            return ActorRunResult.rescheduleIn(0);
                        }
                    },
                    t -> {
                        LOG.log(Level.SEVERE, "An error occurred in the job processor", t);
                        transactionSupport.transactional(jobContext, transactionTimeout, true, () -> {
                            onError(t);
                            return null;
                        }, t2 -> {
                            LOG.log(Level.SEVERE, "An error occurred in the job error handler", t2);
                        });
                    }
            );

            if (result == null || closed) {
                return ActorRunResult.done();
            }
            return result;
        }

        private Object process() {
            Instant deadline = jobInstance.getJobConfiguration().getDeadline();
            if (deadline != null && deadline.isBefore(Instant.now())) {
                jobInstance.markDeadlineReached();
            } else {
                Set<? extends TimeFrame> executionTimeFrames = jobInstance.getJobConfiguration().getExecutionTimeFrames();
                Instant now = Instant.now();
                if (TimeFrame.isContained(executionTimeFrames, now)) {
                    ClusterNodeInfo clusterNodeInfo = JobSchedulerImpl.this.clusterNodeInfo.get();
                    jobProcessingContext.setPartitionCount(clusterNodeInfo.getClusterSize());
                    jobProcessingContext.setPartitionId(clusterNodeInfo.getClusterPosition());
                    return jobInstanceProcessor.process(jobInstance, jobProcessingContext);
                } else {
                    Instant nextSchedule = TimeFrame.getNearestTimeFrameSchedule(executionTimeFrames, now);
                    if (nextSchedule == Instant.MAX) {
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, "Dropping job instance: " + jobInstance);
                        }
                        jobInstance.markDropped();
                    } else {
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, "Deferring job instance to " + nextSchedule);
                        }
                        jobInstance.markDeferred(nextSchedule);
                    }
                }
            }

            return null;
        }

        private void onChunkSuccess(Object lastProcessed) {
            jobProcessingContext.setLastProcessed(lastProcessed);
            jobContext.forEachJobInstanceListeners(listener -> {
                listener.onJobInstanceChunkSuccess(jobInstance, jobProcessingContext);
            });
            jobContext.getJobManager().onJobInstanceChunkSuccess(jobInstance, jobProcessingContext);
        }

        private void onError(Throwable e) {
            jobContext.forEachJobInstanceListeners(listener -> {
                listener.onJobInstanceError(jobInstance, jobProcessingContext);
            });
            jobContext.getJobManager().onJobInstanceError(jobInstance, jobProcessingContext);
        }

        private void onSuccess() {
            jobContext.forEachJobInstanceListeners(listener -> {
                listener.onJobInstanceSuccess(jobInstance, jobProcessingContext);
            });
            jobContext.getJobManager().onJobInstanceSuccess(jobInstance, jobProcessingContext);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof JobInstanceRunner)) {
                return false;
            }

            JobInstanceRunner that = (JobInstanceRunner) o;

            return jobInstance.equals(that.jobInstance);
        }

        @Override
        public int hashCode() {
            return jobInstance.hashCode();
        }
    }

    private static class MutableScheduleContext implements ScheduleContext {

        private long lastScheduledExecutionTime;
        private long lastActualExecutionTime;
        private long lastCompletionTime;

        @Override
        public long getLastScheduledExecutionTime() {
            return lastScheduledExecutionTime;
        }

        public void setLastScheduledExecutionTime(long lastScheduledExecutionTime) {
            this.lastScheduledExecutionTime = lastScheduledExecutionTime;
        }

        @Override
        public long getLastActualExecutionTime() {
            return lastActualExecutionTime;
        }

        public void setLastActualExecutionTime(long lastActualExecutionTime) {
            this.lastActualExecutionTime = lastActualExecutionTime;
        }

        @Override
        public long getLastCompletionTime() {
            return lastCompletionTime;
        }

        public void setLastCompletionTime(long lastCompletionTime) {
            this.lastCompletionTime = lastCompletionTime;
        }
    }

    private static class MutableJobInstanceProcessingContext implements JobInstanceProcessingContext<Object> {

        private final JobContext jobContext;
        private int processCount;
        private int partitionId;
        private int partitionCount;
        private Object lastProcessed;

        public MutableJobInstanceProcessingContext(JobContext jobContext, int processCount) {
            this.jobContext = jobContext;
            this.processCount = processCount;
        }

        @Override
        public JobContext getJobContext() {
            return jobContext;
        }

        @Override
        public int getProcessCount() {
            return processCount;
        }

        public void setProcessCount(int processCount) {
            this.processCount = processCount;
        }

        @Override
        public int getPartitionId() {
            return partitionId;
        }

        public void setPartitionId(int partitionId) {
            this.partitionId = partitionId;
        }

        @Override
        public int getPartitionCount() {
            return partitionCount;
        }

        public void setPartitionCount(int partitionCount) {
            this.partitionCount = partitionCount;
        }

        @Override
        public Object getLastProcessed() {
            return lastProcessed;
        }

        public void setLastProcessed(Object lastProcessed) {
            this.lastProcessed = lastProcessed;
        }
    }
}
