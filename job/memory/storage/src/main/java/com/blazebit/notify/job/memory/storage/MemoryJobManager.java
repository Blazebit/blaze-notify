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

package com.blazebit.notify.job.memory.storage;

import com.blazebit.notify.job.*;
import com.blazebit.notify.job.memory.model.AbstractJob;
import com.blazebit.notify.job.memory.model.AbstractJobInstance;
import com.blazebit.notify.job.memory.model.AbstractJobTrigger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MemoryJobManager implements JobManager {

    private static final String JOBS_PROPERTY = "job.memory.storage.jobs";
    private static final String JOB_TRIGGERS_PROPERTY = "job.memory.storage.jobTriggers";
    private static final String JOB_INSTANCES_PROPERTY = "job.memory.storage.jobInstances";

    private final JobContext jobContext;
    private final AtomicLong jobCounter = new AtomicLong();
    private final AtomicLong jobTriggerCounter = new AtomicLong();
    private final AtomicLong jobInstanceCounter = new AtomicLong();
    private final Set<Job> jobs;
    private final Map<Job, Set<JobTrigger>> jobTriggers;
    private final Set<JobInstance> jobInstances;

    public MemoryJobManager(JobContext jobContext) {
        this.jobContext = jobContext;
        Object jobsProperty = jobContext.getProperty(JOBS_PROPERTY);
        if (jobsProperty == null) {
            this.jobs = new HashSet<>();
        } else if (jobsProperty instanceof Set<?>) {
            this.jobs = (Set<Job>) jobsProperty;
        } else {
            throw new JobException("The property value for " + JOBS_PROPERTY + " must be an instance of java.util.Set if given!");
        }
        Object jobTriggersProperty = jobContext.getProperty(JOB_TRIGGERS_PROPERTY);
        if (jobTriggersProperty == null) {
            this.jobTriggers = new HashMap<>();
        } else if (jobTriggersProperty instanceof Map<?, ?>) {
            this.jobTriggers = (Map<Job, Set<JobTrigger>>) jobTriggersProperty;
        } else {
            throw new JobException("The property value for " + JOB_TRIGGERS_PROPERTY + " must be an instance of java.util.Map if given!");
        }
        Object jobInstancesProperty = jobContext.getProperty(JOB_INSTANCES_PROPERTY);
        if (jobInstancesProperty == null) {
            this.jobInstances = new HashSet<>();
        } else if (jobInstancesProperty instanceof Set<?>) {
            this.jobInstances = (Set<JobInstance>) jobInstancesProperty;
        } else {
            throw new JobException("The property value for " + JOB_INSTANCES_PROPERTY + " must be an instance of java.util.Set if given!");
        }
    }

    public MemoryJobManager(JobContext jobContext, Set<Job> jobs, Map<Job, Set<JobTrigger>> jobTriggers, Set<JobInstance> jobInstances) {
        this.jobContext = jobContext;
        this.jobs = jobs;
        this.jobTriggers = jobTriggers;
        this.jobInstances = jobInstances;
    }

    @Override
    public long addJob(Job job) {
        ((AbstractJob) job).setId(jobCounter.incrementAndGet());
        jobs.add(job);
        return job.getId();
    }

    @Override
    public long addJobTrigger(JobTrigger jobTrigger) {
        Set<JobTrigger> jobTriggers = this.jobTriggers.computeIfAbsent(jobTrigger.getJob(), (key) -> {
            addJob(key);
            return new HashSet<>();
        });
        ((AbstractJobTrigger<?>) jobTrigger).setId(jobTriggerCounter.incrementAndGet());
        jobTriggers.add(jobTrigger);
        if (!jobTrigger.getJobConfiguration().isDone()) {
            jobContext.notifyJobTriggerScheduleListeners(jobTrigger);
        }
        return jobTrigger.getId();
    }

    @Override
    public long addJobInstance(JobInstance jobInstance) {
        if (jobInstance.getTrigger().getJob().getId() == null) {
            addJobTrigger(jobInstance.getTrigger());
        }
        ((AbstractJobInstance<?>) jobInstance).setId(jobInstanceCounter.incrementAndGet());
        jobInstances.add(jobInstance);
        if (jobInstance.getState() == JobInstanceState.NEW) {
            jobContext.notifyJobInstanceScheduleListeners(jobInstance);
        }
        return jobInstance.getId();
    }

    @Override
    public List<JobTrigger> getUndoneJobTriggers(int partition, int partitionCount) {
        return jobTriggers.values().stream()
                .flatMap(s -> s.stream())
                .filter(t -> !t.getJobConfiguration().isDone())
                .collect(Collectors.toList());
    }

    @Override
    public List<JobInstance> getUndoneJobInstances(int partition, int partitionCount) {
        return jobInstances.stream()
                .filter(i -> i.getState() == JobInstanceState.NEW)
                .sorted(Comparator.comparing(JobInstance::getScheduleTime))
                .collect(Collectors.toList());
    }

    @Override
    public JobTrigger getJobTrigger(long id) {
        return jobTriggers.values().stream()
                .flatMap(s -> s.stream())
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public JobInstance getJobInstance(long id) {
        return jobInstances.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void onJobInstanceChunkSuccess(JobInstance jobInstance, JobInstanceProcessingContext<?> context) {
        if (!(jobInstance instanceof AbstractJobInstance<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-memory-model are supported!");
        }
        ((AbstractJobInstance) jobInstance).onChunkSuccess(context);
    }

    @Override
    public void onJobInstanceError(JobInstance jobInstance, JobInstanceProcessingContext<?> context) {
        if (!(jobInstance instanceof AbstractJobInstance<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-memory-model are supported!");
        }
        ((AbstractJobInstance) jobInstance).setState(JobInstanceState.FAILED);
    }

    @Override
    public void onJobInstanceSuccess(JobInstance jobInstance, JobInstanceProcessingContext<?> context) {
        if (!(jobInstance instanceof AbstractJobInstance<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-memory-model are supported!");
        }
        ((AbstractJobInstance) jobInstance).setState(JobInstanceState.DONE);
    }

    @Override
    public void onJobTriggerError(JobTrigger jobTrigger, JobContext context) {
        if (!(jobTrigger instanceof AbstractJobTrigger<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-memory-model are supported!");
        }
        // This is a hook for users to e.g. do logging
    }

    @Override
    public void onJobTriggerSuccess(JobTrigger jobTrigger, JobContext context) {
        if (!(jobTrigger instanceof AbstractJobTrigger<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-memory-model are supported!");
        }
        // This is a hook for users to e.g. do logging
    }

    @Override
    public void onJobTriggerEnded(JobTrigger jobTrigger, JobContext context) {
        if (!(jobTrigger instanceof AbstractJobTrigger<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-memory-model are supported!");
        }
        ((AbstractJobTrigger<?>) jobTrigger).getJobConfiguration().setDone(true);
    }
}
