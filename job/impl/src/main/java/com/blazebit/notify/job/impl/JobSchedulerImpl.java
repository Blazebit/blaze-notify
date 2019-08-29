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
import com.blazebit.notify.actor.spi.*;
import com.blazebit.notify.job.*;
import com.blazebit.notify.job.event.JobInstanceListener;
import com.blazebit.notify.job.spi.JobScheduler;
import com.blazebit.notify.job.spi.TransactionSupport;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobSchedulerImpl implements JobScheduler, ClusterStateListener {

    private static final Logger LOG = Logger.getLogger(JobSchedulerImpl.class.getName());

    private final JobContext jobContext;
    private final ActorContext actorContext;
    private final Clock clock;
    private final Scheduler scheduler;
    private final JobManager jobManager;
    private final JobInstanceRunner runner;
    private final String actorName;
    private final PartitionKey partitionKey;
    private final int processCount;
    private final AtomicLong earliestKnownSchedule = new AtomicLong(Long.MAX_VALUE);
    private volatile ClusterNodeInfo clusterNodeInfo;
    private volatile boolean closed;

    public JobSchedulerImpl(JobContext jobContext, ActorContext actorContext, SchedulerFactory schedulerFactory, String actorName, int processCount, PartitionKey partitionKey) {
        this.jobContext = jobContext;
        this.actorContext = actorContext;
        this.clock = jobContext.getService(Clock.class) == null ? Clock.systemUTC() : jobContext.getService(Clock.class);
        this.scheduler = schedulerFactory.createScheduler(actorContext, actorName + "/processor");
        this.jobManager = jobContext.getJobManager();
        this.runner = new JobInstanceRunner();
        this.actorName = actorName;
        this.processCount = processCount;
        this.partitionKey = partitionKey;
    }

    @Override
    public void start() {
        actorContext.getActorManager().registerSuspendedActor(actorName, runner);
        actorContext.getService(ClusterStateManager.class).registerListener(this);
    }

    @Override
    public void onClusterStateChanged(ClusterNodeInfo clusterNodeInfo) {
        this.clusterNodeInfo = clusterNodeInfo;
        if (!closed) {
            Instant nextSchedule = jobManager.getNextSchedule(clusterNodeInfo.getClusterPosition(), clusterNodeInfo.getClusterSize(), partitionKey);
            if (nextSchedule == null) {
                resetEarliestKnownSchedule();
            } else {
                refreshSchedules(nextSchedule.toEpochMilli());
            }
        }
    }

    @Override
    public void refreshSchedules(long earliestNewSchedule) {
        long delayMillis = rescan(earliestNewSchedule);
        if (delayMillis != -1L) {
            actorContext.getActorManager().rescheduleActor(actorName, delayMillis);
        }
    }

    private long rescan(long earliestNewSchedule) {
        if (!closed) {
            if (earliestNewSchedule == 0) {
                // This is special. We want to recheck schedules, no matter what
                ClusterNodeInfo clusterNodeInfo = this.clusterNodeInfo;
                Instant nextSchedule = jobManager.getNextSchedule(clusterNodeInfo.getClusterPosition(), clusterNodeInfo.getClusterSize(), partitionKey);
                // No new schedules available
                if (nextSchedule == null) {
                    resetEarliestKnownSchedule();
                    return -1L;
                }
                earliestNewSchedule = nextSchedule.toEpochMilli();
            }
            long earliestKnownSchedule = this.earliestKnownSchedule.get();
            // We use lower or equal because a re-schedule event could be cause from within a processor
            if (earliestNewSchedule <= earliestKnownSchedule) {
                // There are new job instances that should be scheduled before our known earliest job instance

                // We just reschedule based on the new earliest schedule if it stays that
                if (!updateEarliestKnownSchedule(earliestKnownSchedule, earliestNewSchedule)) {
                    // A different thread won the race and apparently has a job instance that should be scheduled earlier
                    return -1L;
                }
                long delayMillis = earliestNewSchedule - System.currentTimeMillis();

                delayMillis = delayMillis < 0 ? 0 : delayMillis;
                return delayMillis;
            }
        }

        return -1L;
    }

    private boolean updateEarliestKnownSchedule(long oldValue, long newValue) {
        do {
            if (earliestKnownSchedule.compareAndSet(oldValue, newValue)) {
                return true;
            }

            oldValue = earliestKnownSchedule.get();
            // We use lower or equal because a re-schedule event could be cause from within a processor
        } while (oldValue <= newValue);

        return false;
    }

    private void resetEarliestKnownSchedule() {
        long earliestKnownSchedule = this.earliestKnownSchedule.get();
        // Only reset the value if the currently known earliest schedule is in the past
        if (earliestKnownSchedule < clock.millis()) {
            updateEarliestKnownSchedule(earliestKnownSchedule, Long.MAX_VALUE);
        }
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

    private class JobInstanceRunner implements ScheduledActor {

        private final long maxBackOff = 10_000L; // 10 seconds by default
        private final long baseBackOff = 1_000L; // 1 second by default
        private final long temporaryErrorDeferSeconds = 10;
        private final long rateLimitDeferSeconds = 10;
        private int retryAttempt;

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ActorRunResult work() {
            JobManager jobManager = jobContext.getJobManager();
            TransactionSupport transactionSupport = jobContext.getTransactionSupport();
            // TODO: make configurable
            long transactionTimeout = 60_000L;
            long earliestKnownNotificationSchedule = JobSchedulerImpl.this.earliestKnownSchedule.get();
            ActorRunResult result = transactionSupport.transactional(jobContext, transactionTimeout, false, () -> {
                    ClusterNodeInfo clusterNodeInfo = JobSchedulerImpl.this.clusterNodeInfo;
                    List<JobInstance<?>> jobInstancesToProcess = jobManager.getJobInstancesToProcess(clusterNodeInfo.getClusterPosition(), clusterNodeInfo.getClusterSize(), processCount, partitionKey);
                    int size = jobInstancesToProcess.size();
                    if (size == 0) {
                        return ActorRunResult.suspend();
                    }
                    Instant earliestNewSchedule = Instant.MAX;
                    List<JobInstanceExecution> jobInstanceExecutions = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        Instant now = clock.instant();
                        JobInstance<?> jobInstance = jobInstancesToProcess.get(i);
                        MutableJobInstanceProcessingContext jobProcessingContext = new MutableJobInstanceProcessingContext(jobContext, partitionKey, processCount);
                        jobProcessingContext.setPartitionCount(clusterNodeInfo.getClusterSize());
                        jobProcessingContext.setPartitionId(clusterNodeInfo.getClusterPosition());
                        jobProcessingContext.setLastProcessed(jobInstance.getLastProcessed());
                        MutableScheduleContext scheduleContext = new MutableScheduleContext();
                        boolean future = false;
                        Instant lastExecutionTime = jobInstance.getLastExecutionTime();
                        if (lastExecutionTime == null) {
                            lastExecutionTime = now;
                        }
                        scheduleContext.setLastScheduleTime(jobInstance.getScheduleTime().toEpochMilli());
                        scheduleContext.setLastExecutionTime(lastExecutionTime.toEpochMilli());
                        try {
                            Instant deadline = jobInstance.getJobConfiguration().getDeadline();
                            if (deadline != null && deadline.compareTo(now) <= 0) {
                                jobInstance.markDeadlineReached();
                                jobContext.forEachJobInstanceListeners(new JobInstanceErrorListenerConsumer(jobInstance, jobProcessingContext));
                            } else {
                                Set<? extends TimeFrame> executionTimeFrames = jobInstance.getJobConfiguration().getExecutionTimeFrames();
                                if (TimeFrame.isContained(executionTimeFrames, now)) {
                                    int deferCount = jobInstance.getDeferCount();
                                    JobInstanceProcessor jobInstanceProcessor = jobContext.getJobInstanceProcessor(jobInstance);
                                    Future<Object> f;
                                    // By default we execute transactional job instance processors synchronously within our transaction
                                    if (jobInstanceProcessor.isTransactional()) {
                                        f = new SyncJobInstanceProcessorFuture(jobInstanceProcessor, jobInstance, jobProcessingContext);
                                    } else {
                                        f = scheduler.submit(() -> jobInstanceProcessor.process(jobInstance, jobProcessingContext));
                                    }
                                    jobInstanceExecutions.add(new JobInstanceExecution(jobInstance, deferCount, scheduleContext, jobProcessingContext, f));
                                    future = true;
                                } else {
                                    Instant nextSchedule = TimeFrame.getNearestTimeFrameSchedule(executionTimeFrames, now);
                                    if (nextSchedule == Instant.MAX) {
                                        if (LOG.isLoggable(Level.FINEST)) {
                                            LOG.log(Level.FINEST, "Dropping job instance: " + jobInstance);
                                        }
                                        jobInstance.markDropped();
                                        jobContext.forEachJobInstanceListeners(new JobInstanceErrorListenerConsumer(jobInstance, jobProcessingContext));
                                    } else {
                                        if (LOG.isLoggable(Level.FINEST)) {
                                            LOG.log(Level.FINEST, "Deferring job instance to " + nextSchedule);
                                        }
                                        jobInstance.markDeferred(nextSchedule);
                                        if (jobInstance.getState() == JobInstanceState.DROPPED) {
                                            jobContext.forEachJobInstanceListeners(new JobInstanceErrorListenerConsumer(jobInstance, jobProcessingContext));
                                        }
                                        if (jobInstance.getScheduleTime().isBefore(earliestNewSchedule)) {
                                            earliestNewSchedule = jobInstance.getScheduleTime();
                                        }
                                    }
                                }
                            }
                        } catch (Throwable t) {
                            LOG.log(Level.SEVERE, "An error occurred in the job scheduler", t);
                            jobInstance.markFailed(t);
                            jobContext.forEachJobInstanceListeners(new JobInstanceErrorListenerConsumer(jobInstance, jobProcessingContext));
                        } finally {
                            if (!future) {
                                jobInstance.setLastExecutionTime(lastExecutionTime);
                                jobManager.updateJobInstance(jobInstance);
                            }
                        }
                    }

                    Instant rescheduleRateLimitTime = null;
                    for (int i = 0; i < jobInstanceExecutions.size(); i++) {
                        JobInstanceExecution execution = jobInstanceExecutions.get(i);
                        MutableJobInstanceProcessingContext jobProcessingContext = execution.jobProcessingContext;
                        MutableScheduleContext scheduleContext = execution.scheduleContext;
                        JobInstance<?> jobInstance = execution.jobInstance;
                        int deferCount = execution.deferCount;
                        Future<Object> future = execution.future;
                        boolean success = true;
                        try {
                            Object lastProcessed = future.get();
                            jobInstance.setLastExecutionTime(Instant.ofEpochMilli(scheduleContext.getLastExecutionTime()));
                            jobProcessingContext.setLastProcessed(lastProcessed);
                            scheduleContext.setLastCompletionTime(System.currentTimeMillis());

                            if (jobInstance.getState() == JobInstanceState.NEW) {
                                Instant nextSchedule = jobInstance.nextSchedule(jobContext, scheduleContext);
                                // This is essential for limited time or fixed time schedules. When these are done, they always return a nextSchedule equal to getLastScheduledExecutionTime()
                                if (nextSchedule.toEpochMilli() != scheduleContext.getLastScheduleTime()) {
                                    // This is a recurring job that needs rescheduling
                                    if (jobInstance.getDeferCount() == deferCount) {
                                        jobInstance.onChunkSuccess(jobProcessingContext);
                                        jobContext.forEachJobInstanceListeners(new JobInstanceChunkSuccessListenerConsumer(jobInstance, jobProcessingContext));
                                    }
                                    jobInstance.setScheduleTime(nextSchedule);
                                    if (jobInstance.getScheduleTime().isBefore(earliestNewSchedule)) {
                                        earliestNewSchedule = jobInstance.getScheduleTime();
                                    }
                                    continue;
                                } else if (lastProcessed != null) {
                                    // Chunk processing
                                    if (jobInstance.getDeferCount() == deferCount) {
                                        jobInstance.onChunkSuccess(jobProcessingContext);
                                        jobContext.forEachJobInstanceListeners(new JobInstanceChunkSuccessListenerConsumer(jobInstance, jobProcessingContext));
                                    }
                                    if (jobInstance.getScheduleTime().isBefore(earliestNewSchedule)) {
                                        earliestNewSchedule = jobInstance.getScheduleTime();
                                    }
                                    continue;
                                }
                            }

                            if (jobInstance.getState() != JobInstanceState.DONE) {
                                jobInstance.markDone(lastProcessed);
                            }
                            jobContext.forEachJobInstanceListeners(new JobInstanceSuccessListenerConsumer(jobInstance, jobProcessingContext));
                        } catch (ExecutionException ex) {
                            Throwable t = ex.getCause();
                            jobInstance.setLastExecutionTime(Instant.ofEpochMilli(scheduleContext.getLastExecutionTime()));
                            if (t instanceof JobRateLimitException) {
                                JobRateLimitException e = (JobRateLimitException) t;
                                LOG.log(Level.FINEST, "Deferring job instance due to rate limit", e);
                                if (rescheduleRateLimitTime == null) {
                                    if (e.getDeferMillis() != -1) {
                                        rescheduleRateLimitTime = clock.instant().plus(e.getDeferMillis(), ChronoUnit.MILLIS);
                                    } else {
                                        rescheduleRateLimitTime = clock.instant().plus(rateLimitDeferSeconds, ChronoUnit.SECONDS);
                                    }
                                }
                                jobInstance.setScheduleTime(rescheduleRateLimitTime);
                                if (jobInstance.getScheduleTime().isBefore(earliestNewSchedule)) {
                                    earliestNewSchedule = jobInstance.getScheduleTime();
                                }
                            } else if (t instanceof JobTemporaryException) {
                                JobTemporaryException e = (JobTemporaryException) t;
                                LOG.log(Level.FINEST, "Deferring job instance due to temporary error", e);
                                if (e.getDeferMillis() != -1) {
                                    jobInstance.setScheduleTime(clock.instant().plus(e.getDeferMillis(), ChronoUnit.MILLIS));
                                } else {
                                    jobInstance.setScheduleTime(clock.instant().plus(temporaryErrorDeferSeconds, ChronoUnit.SECONDS));
                                }
                                if (jobInstance.getScheduleTime().isBefore(earliestNewSchedule)) {
                                    earliestNewSchedule = jobInstance.getScheduleTime();
                                }
                            } else {
                                LOG.log(Level.SEVERE, "An error occurred in the job instance processor", t);
                                success = false;
                                transactionSupport.transactional(jobContext, transactionTimeout, true, () -> {
                                    jobContext.forEachJobInstanceListeners(new JobInstanceErrorListenerConsumer(jobInstance, jobProcessingContext));
                                    jobInstance.markFailed(t);
                                    jobManager.updateJobInstance(jobInstance);
                                    return null;
                                }, t2 -> {
                                    LOG.log(Level.SEVERE, "An error occurred in the job instance error handler", t2);
                                });
                            }
                        } finally {
                            if (success) {
                                jobManager.updateJobInstance(jobInstance);
                            }
                        }
                    }

                    if (earliestNewSchedule == Instant.MAX) {
                        return ActorRunResult.suspend();
                    }
                    return ActorRunResult.rescheduleIn(earliestNewSchedule.toEpochMilli() - clock.millis());
                },
                t -> {
                    LOG.log(Level.SEVERE, "An error occurred in the job scheduler", t);
                }
            );

            if (closed) {
                return ActorRunResult.done();
            } else if (result == null) {
                // An error occurred like e.g. a TX timeout or a temporary DB issue. We do exponential back-off
                return ActorRunResult.rescheduleIn(getWaitTime(maxBackOff, baseBackOff, retryAttempt++));
            }

            retryAttempt = 0;
            if (result.isSuspend()) {
                // This will reschedule based on the next schedule
                updateEarliestKnownSchedule(earliestKnownNotificationSchedule, Long.MAX_VALUE);
                long delayMillis = rescan(0L);
                if (delayMillis != -1L) {
                    return ActorRunResult.rescheduleIn(delayMillis);
                }
            }
            // NOTE: we don't need to update earliestKnownNotificationSchedule when rescheduling immediately
            return result;
        }
    }

    private static long getWaitTime(final long maximum, final long base, final long attempt) {
        final long expWait = ((long) Math.pow(2, attempt)) * base;
        return expWait <= 0 ? maximum : Math.min(maximum, expWait);
    }

    private static class JobInstanceChunkSuccessListenerConsumer implements Consumer<JobInstanceListener> {
        private final JobInstance<?> jobInstance;
        private final MutableJobInstanceProcessingContext jobProcessingContext;

        public JobInstanceChunkSuccessListenerConsumer(JobInstance<?> jobInstance, MutableJobInstanceProcessingContext jobProcessingContext) {
            this.jobInstance = jobInstance;
            this.jobProcessingContext = jobProcessingContext;
        }

        @Override
        public void accept(JobInstanceListener listener) {
            listener.onJobInstanceChunkSuccess(jobInstance, jobProcessingContext);
        }
    }

    private static class JobInstanceSuccessListenerConsumer implements Consumer<JobInstanceListener> {
        private final JobInstance<?> jobInstance;
        private final MutableJobInstanceProcessingContext jobProcessingContext;

        public JobInstanceSuccessListenerConsumer(JobInstance<?> jobInstance, MutableJobInstanceProcessingContext jobProcessingContext) {
            this.jobInstance = jobInstance;
            this.jobProcessingContext = jobProcessingContext;
        }

        @Override
        public void accept(JobInstanceListener listener) {
            listener.onJobInstanceSuccess(jobInstance, jobProcessingContext);
        }
    }

    private static class JobInstanceErrorListenerConsumer implements Consumer<JobInstanceListener> {
        private final JobInstance<?> jobInstance;
        private final MutableJobInstanceProcessingContext jobProcessingContext;

        public JobInstanceErrorListenerConsumer(JobInstance<?> jobInstance, MutableJobInstanceProcessingContext jobProcessingContext) {
            this.jobInstance = jobInstance;
            this.jobProcessingContext = jobProcessingContext;
        }

        @Override
        public void accept(JobInstanceListener listener) {
            listener.onJobInstanceError(jobInstance, jobProcessingContext);
        }
    }

    private static class SyncJobInstanceProcessorFuture implements Future<Object> {

        private final JobInstanceProcessor jobInstanceProcessor;
        private final JobInstance<?> jobInstance;
        private final JobInstanceProcessingContext<?> processingContext;
        private boolean done;
        private Object result;
        private Exception exception;

        public SyncJobInstanceProcessorFuture(JobInstanceProcessor jobInstanceProcessor, JobInstance<?> jobInstance, JobInstanceProcessingContext<?> processingContext) {
            this.jobInstanceProcessor = jobInstanceProcessor;
            this.jobInstance = jobInstance;
            this.processingContext = processingContext;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object get() throws InterruptedException, ExecutionException {
            if (done) {
                if (exception == null) {
                    return result;
                } else {
                    throw new ExecutionException(exception);
                }
            }

            done = true;
            try {
                return result = jobInstanceProcessor.process(jobInstance, processingContext);
            } catch (Exception e) {
                throw new ExecutionException(exception = e);
            }
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }

    private static class JobInstanceExecution {
        private final JobInstance<?> jobInstance;
        private final int deferCount;
        private final MutableScheduleContext scheduleContext;
        private final MutableJobInstanceProcessingContext jobProcessingContext;
        private final Future<Object> future;

        public JobInstanceExecution(JobInstance<?> jobInstance, int deferCount, MutableScheduleContext scheduleContext, MutableJobInstanceProcessingContext jobProcessingContext, Future<Object> future) {
            this.jobInstance = jobInstance;
            this.deferCount = deferCount;
            this.scheduleContext = scheduleContext;
            this.jobProcessingContext = jobProcessingContext;
            this.future = future;
        }
    }

    private static class MutableScheduleContext implements ScheduleContext {

        private long lastScheduleTime;
        private long lastExecutionTime;
        private long lastCompletionTime;

        @Override
        public long getLastScheduleTime() {
            return lastScheduleTime;
        }

        public void setLastScheduleTime(long lastScheduleTime) {
            this.lastScheduleTime = lastScheduleTime;
        }

        @Override
        public long getLastExecutionTime() {
            return lastExecutionTime;
        }

        public void setLastExecutionTime(long lastExecutionTime) {
            this.lastExecutionTime = lastExecutionTime;
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
        private final PartitionKey partitionKey;
        private final int processCount;
        private int partitionId;
        private int partitionCount;
        private Object lastProcessed;

        public MutableJobInstanceProcessingContext(JobContext jobContext, PartitionKey partitionKey, int processCount) {
            this.jobContext = jobContext;
            this.partitionKey = partitionKey;
            this.processCount = processCount;
        }

        @Override
        public JobContext getJobContext() {
            return jobContext;
        }

        @Override
        public PartitionKey getPartitionKey() {
            return partitionKey;
        }

        @Override
        public int getProcessCount() {
            return processCount;
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
