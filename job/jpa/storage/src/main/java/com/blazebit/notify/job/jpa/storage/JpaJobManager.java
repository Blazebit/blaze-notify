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

package com.blazebit.notify.job.jpa.storage;

import com.blazebit.notify.job.*;
import com.blazebit.notify.job.jpa.model.AbstractJob;
import com.blazebit.notify.job.jpa.model.AbstractJobInstance;
import com.blazebit.notify.job.jpa.model.AbstractJobTrigger;
import com.blazebit.notify.job.spi.TransactionSupport;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class JpaJobManager implements JobManager {

    private static final String JOB_TRIGGER_ID_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_id_attribute_name";
    private static final String JOB_TRIGGER_JOB_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_job_attribute_name";
    private static final String JOB_TRIGGER_STATE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_state_attribute_name";
    private static final String JOB_TRIGGER_STATE_READY_VALUE_PROPERTY = "job.jpa.storage.job_trigger_state_ready_value";
    private static final String JOB_INSTANCE_ID_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_id_attribute_name";
    private static final String JOB_INSTANCE_TRIGGER_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_trigger_attribute_name";
    private static final String JOB_INSTANCE_SCHEDULE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_schedule_attribute_name";
    private static final String JOB_INSTANCE_STATE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_state_attribute_name";
    private static final String JOB_INSTANCE_STATE_READY_VALUE_PROPERTY = "job.jpa.storage.job_instance_state_ready_value";

    private final JobContext jobContext;
    private final EntityManager entityManager;
    private final String jobTriggerIdAttributeName;
    private final String jobTriggerJobAttributeName;
    private final String jobTriggerStateAttributeName;
    private final Object jobTriggerStateReadyValue;
    private final String jobInstanceIdAttributeName;
    private final String jobInstanceTriggerAttributeName;
    private final String jobInstanceScheduleAttributeName;
    private final String jobInstanceStateAttributeName;
    private final Object jobInstanceStateReadyValue;

    public JpaJobManager(JobContext jobContext) {
        this(
                jobContext,
                jobContext.getService(EntityManager.class),
                jobContext.getPropertyOrDefault(JOB_TRIGGER_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                jobContext.getPropertyOrDefault(JOB_TRIGGER_JOB_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "job"),
                jobContext.getPropertyOrDefault(JOB_TRIGGER_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "jobConfiguration.done"),
                jobContext.getPropertyOrDefault(JOB_TRIGGER_STATE_READY_VALUE_PROPERTY, Object.class, null, o -> false),
                jobContext.getPropertyOrDefault(JOB_INSTANCE_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                jobContext.getPropertyOrDefault(JOB_INSTANCE_TRIGGER_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "trigger"),
                jobContext.getPropertyOrDefault(JOB_INSTANCE_SCHEDULE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "scheduleTime"),
                jobContext.getPropertyOrDefault(JOB_INSTANCE_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "state"),
                jobContext.getPropertyOrDefault(JOB_INSTANCE_STATE_READY_VALUE_PROPERTY, Object.class, null, o -> JobInstanceState.NEW)
        );
    }

    public JpaJobManager(JobContext jobContext, EntityManager entityManager, String jobTriggerIdAttributeName, String jobTriggerJobAttributeName, String jobTriggerStateAttributeName, Object jobTriggerStateReadyValue, String jobInstanceIdAttributeName, String jobInstanceTriggerAttributeName, String jobInstanceScheduleAttributeName, String jobInstanceStateAttributeName, Object jobInstanceStateReadyValue) {
        this.jobInstanceTriggerAttributeName = jobInstanceTriggerAttributeName;
        if (entityManager == null) {
            throw new JobException("No entity manager given!");
        }
        if (jobContext.getTransactionSupport() == TransactionSupport.NOOP) {
            throw new JobException("JPA storage requires transaction support!");
        }
        this.jobContext = jobContext;
        this.entityManager = entityManager;
        this.jobTriggerIdAttributeName = jobTriggerIdAttributeName;
        this.jobTriggerJobAttributeName = jobTriggerJobAttributeName;
        this.jobTriggerStateAttributeName = jobTriggerStateAttributeName;
        this.jobTriggerStateReadyValue = jobTriggerStateReadyValue;
        this.jobInstanceIdAttributeName = jobInstanceIdAttributeName;
        this.jobInstanceScheduleAttributeName = jobInstanceScheduleAttributeName;
        this.jobInstanceStateAttributeName = jobInstanceStateAttributeName;
        this.jobInstanceStateReadyValue = jobInstanceStateReadyValue;
    }

    @Override
    public long addJob(Job job) {
        entityManager.persist(job);
        return job.getId();
    }

    protected void setJob(JobTrigger jobTrigger, Job job) {
        if (!(jobTrigger instanceof AbstractJobTrigger<?>) || !(job instanceof AbstractJob)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-jpa-model are supported!");
        }
        ((AbstractJobTrigger<AbstractJob>) jobTrigger).setJob((AbstractJob) job);
    }

    protected void setTrigger(JobInstance jobInstance, JobTrigger jobTrigger) {
        if (!(jobTrigger instanceof AbstractJobTrigger<?>) || !(jobInstance instanceof AbstractJobInstance<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-jpa-model are supported!");
        }
        ((AbstractJobInstance<AbstractJobTrigger<AbstractJob>>) jobInstance).setTrigger((AbstractJobTrigger<AbstractJob>) jobTrigger);
    }

    protected Job findJob(Class<?> entityClass, long jobId) {
        return (Job) entityManager.find(entityClass, jobId);
    }

    protected JobTrigger findJobTrigger(Class<?> entityClass, long triggerId) {
        EntityGraph<?> entityGraph = entityManager.createEntityGraph(entityClass);
        entityGraph.addSubgraph(jobTriggerJobAttributeName);
        Map<String, Object> properties = Collections.singletonMap("javax.persistence.loadgraph", entityGraph);
        JobTrigger jobTrigger = (JobTrigger) entityManager.find(entityClass, triggerId, properties);
        // Make sure the job is initialized
        jobTrigger.getJob().getName();
        return jobTrigger;
    }

    protected Class<?> getEntityClass(Job job) {
        return getEntityClass(job.getClass());
    }

    protected Class<?> getEntityClass(JobTrigger jobTrigger) {
        return getEntityClass(jobTrigger.getClass());
    }

    protected Class<?> getEntityClass(Class<?> clazz) {
        while (clazz.getName().contains("javassist")) {
            clazz = clazz.getSuperclass();
        }

        return clazz;
    }

    @Override
    public long addJobTrigger(JobTrigger jobTrigger) {
        if (jobTrigger.getJob().getId() == null) {
            addJob(jobTrigger.getJob());
            setJob(jobTrigger, jobTrigger.getJob());
        } else if (!entityManager.contains(jobTrigger.getJob())) {
            setJob(jobTrigger, findJob(getEntityClass(jobTrigger.getJob()), jobTrigger.getJob().getId()));
        }
        JobConfiguration jobConfiguration = jobTrigger.getJob().getJobConfiguration();
        JobConfiguration triggerJobConfiguration = jobTrigger.getJobConfiguration();
        if (jobConfiguration != null && triggerJobConfiguration != null && jobConfiguration.getJobParameters() != null) {
            Map<String, Serializable> jobParameters = triggerJobConfiguration.getJobParameters();
            for (Map.Entry<String, Serializable> entry : jobConfiguration.getJobParameters().entrySet()) {
                jobParameters.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        entityManager.persist(jobTrigger);
        if (!jobTrigger.getJobConfiguration().isDone()) {
            jobContext.getTransactionSupport().registerPostCommitListener(() -> {
                jobContext.notifyJobTriggerScheduleListeners(jobTrigger);
            });
        }
        return jobTrigger.getId();
    }

    @Override
    public long addJobInstance(JobInstance jobInstance) {
        if (jobInstance.getTrigger().getJob().getId() == null) {
            addJobTrigger(jobInstance.getTrigger());
        } else if (!entityManager.contains(jobInstance.getTrigger())) {
            setTrigger(jobInstance, findJobTrigger(getEntityClass(jobInstance.getTrigger()), jobInstance.getTrigger().getId()));
        }
        entityManager.persist(jobInstance);
        if (jobInstance.getState() == JobInstanceState.NEW) {
            jobContext.getTransactionSupport().registerPostCommitListener(() -> {
                jobContext.notifyJobInstanceScheduleListeners(jobInstance);
            });
        }
        return jobInstance.getId();
    }

    @Override
    public List<JobTrigger> getUndoneJobTriggers(int partition, int partitionCount) {
        return (List<JobTrigger>) (List) entityManager.createQuery(
                "SELECT e FROM " + JobTrigger.class.getName() + " e " +
                        "JOIN FETCH e." + jobTriggerJobAttributeName + " " +
                        "WHERE e." + jobTriggerStateAttributeName + " = :readyState " +
                        (partitionCount > 1 ? "AND MOD(e." + jobTriggerIdAttributeName + ", " + partitionCount + ") = " + partition + " " : "") +
                        "ORDER BY e." + jobTriggerIdAttributeName + " ASC",
                JobTrigger.class
        ).setParameter("readyState", jobTriggerStateReadyValue).getResultList();
    }

    @Override
    public List<JobInstance> getUndoneJobInstances(int partition, int partitionCount) {
        return (List<JobInstance>) (List) entityManager.createQuery(
                "SELECT e FROM " + JobInstance.class.getName() + " e " +
                        "JOIN FETCH e." + jobInstanceTriggerAttributeName + " t " +
                        "JOIN FETCH t." + jobTriggerJobAttributeName + " " +
                        "WHERE e." + jobInstanceStateAttributeName + " = :readyState " +
                        (partitionCount > 1 ? "AND MOD(e." + jobInstanceIdAttributeName + ", " + partitionCount + ") = " + partition + " " : "") +
                        "ORDER BY e." + jobInstanceScheduleAttributeName + " ASC, e." + jobInstanceIdAttributeName + " ASC",
                JobInstance.class
        ).setParameter("readyState", jobInstanceStateReadyValue).getResultList();
    }

    @Override
    public JobTrigger getJobTrigger(long id) {
        return entityManager.createQuery(
                "SELECT e FROM " + JobTrigger.class.getName() + " e " +
                "JOIN FETCH e." + jobTriggerJobAttributeName + " " +
                "WHERE e." + jobTriggerIdAttributeName + " = :id ",
                JobTrigger.class
        ).setParameter("id", id).getSingleResult();
    }

    @Override
    public JobInstance getJobInstance(long id) {
        return entityManager.createQuery(
                "SELECT e FROM " + JobInstance.class.getName() + " e " +
                "JOIN FETCH e." + jobInstanceTriggerAttributeName + " t " +
                "JOIN FETCH t." + jobTriggerJobAttributeName + " " +
                "WHERE e." + jobInstanceIdAttributeName + " = :id ",
                JobInstance.class
        ).setParameter("id", id).getSingleResult();
    }

    @Override
    public void onJobInstanceChunkSuccess(JobInstance jobInstance, JobInstanceProcessingContext<?> context) {
        jobInstance.onChunkSuccess(context);
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobInstance);
    }

    @Override
    public void onJobInstanceError(JobInstance jobInstance, JobInstanceProcessingContext<?> context) {
        setState(jobInstance, JobInstanceState.FAILED);
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobInstance);
    }

    @Override
    public void onJobInstanceSuccess(JobInstance jobInstance, JobInstanceProcessingContext<?> context) {
        setState(jobInstance, JobInstanceState.DONE);
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobInstance);
    }

    @Override
    public void onJobTriggerError(JobTrigger jobTrigger, JobContext context) {
        // This is a hook for users to e.g. do logging
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobTrigger);
    }

    @Override
    public void onJobTriggerSuccess(JobTrigger jobTrigger, JobContext context) {
        // This is a hook for users to e.g. do logging
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobTrigger);
    }

    @Override
    public void onJobTriggerEnded(JobTrigger jobTrigger, JobContext context) {
        setDone(jobTrigger, true);
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobTrigger);
    }

    protected void setState(JobInstance jobInstance, JobInstanceState state) {
        if (!(jobInstance instanceof AbstractJobInstance<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-jpa-model are supported!");
        }
        ((AbstractJobInstance<?>) jobInstance).setState(state);
    }

    protected void setDone(JobTrigger jobTrigger, boolean done) {
        if (!(jobTrigger instanceof AbstractJobTrigger<?>)) {
            throw new IllegalArgumentException("Only models from blaze-notify-job-jpa-model are supported!");
        }
        ((AbstractJobTrigger<?>) jobTrigger).getJobConfiguration().setDone(done);
    }
}
