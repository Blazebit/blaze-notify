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
import com.blazebit.notify.job.event.JobInstanceListener;
import com.blazebit.notify.job.event.JobTriggerListener;
import com.blazebit.notify.job.jpa.model.JpaJobInstance;
import com.blazebit.notify.job.jpa.model.JpaJobTrigger;
import com.blazebit.notify.job.jpa.model.JpaPartitionKey;
import com.blazebit.notify.job.jpa.model.JpaTriggerBasedJobInstance;
import com.blazebit.notify.job.spi.TransactionSupport;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JpaJobManager implements JobManager, JobTriggerListener, JobInstanceListener {

    private final JobContext jobContext;
    private final EntityManager entityManager;
    private final Set<Class<?>> entityClasses;

    public JpaJobManager(JobContext jobContext) {
        this(
                jobContext,
                jobContext.getService(EntityManager.class)
        );
    }

    public JpaJobManager(JobContext jobContext, EntityManager entityManager) {
        if (entityManager == null) {
            throw new JobException("No entity manager given!");
        }
        if (jobContext.getTransactionSupport() == TransactionSupport.NOOP) {
            throw new JobException("JPA storage requires transaction support!");
        }
        this.jobContext = jobContext;
        this.entityManager = entityManager;
        Set<Class<?>> entityClasses = new HashSet<>();
        for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
            if (entity.getJavaType() != null) {
                entityClasses.add(entity.getJavaType());
            }
        }
        this.entityClasses = entityClasses;
    }

    protected JpaJobTrigger getJobTrigger(JobTrigger jobTrigger) {
        if (!(jobTrigger instanceof JpaJobTrigger)) {
            throw new IllegalArgumentException("The job trigger does not implement the JpaJobTrigger interface from blaze-notify-job-jpa-model!");
        }
        return (JpaJobTrigger) jobTrigger;
    }

    protected JpaJobInstance<?> getJobInstance(JobInstance<?> jobInstance) {
        if (!(jobInstance instanceof JpaJobInstance<?>)) {
            throw new IllegalArgumentException("The job instance does not implement the JpaJobInstance interface from blaze-notify-job-jpa-model!");
        }
        return (JpaJobInstance<?>) jobInstance;
    }

    protected void setJob(JobTrigger jobTrigger, Job job) {
        getJobTrigger(jobTrigger).setJob(job);
    }

    protected void setTrigger(JpaTriggerBasedJobInstance<?> jpaTriggerBasedJobInstance, JobTrigger jobTrigger) {
        jpaTriggerBasedJobInstance.setTrigger(jobTrigger);
    }

    protected Job findJob(Class<?> entityClass, long jobId) {
        return (Job) entityManager.find(entityClass, jobId);
    }

    protected JobTrigger findJobTrigger(Class<?> entityClass, long triggerId) {
        return (JobTrigger) entityManager.find(entityClass, triggerId);
    }

    protected Class<?> getEntityClass(Job job) {
        return getEntityClass(job.getClass());
    }

    protected Class<?> getEntityClass(JobTrigger jobTrigger) {
        return getEntityClass(jobTrigger.getClass());
    }

    protected Class<?> getEntityClass(Class<?> clazz) {
        while (clazz != Object.class && !entityClasses.contains(clazz)) {
            clazz = clazz.getSuperclass();
        }

        return clazz;
    }

    private void addJobTrigger(JobTrigger jobTrigger) {
        if (jobTrigger.getJob().getId() == null) {
            entityManager.persist(jobTrigger.getJob());
            setJob(jobTrigger, jobTrigger.getJob());
        } else if (!entityManager.contains(jobTrigger.getJob())) {
            setJob(jobTrigger, findJob(getEntityClass(jobTrigger.getJob()), jobTrigger.getJob().getId()));
        }
        JobConfiguration jobConfiguration = jobTrigger.getJob().getJobConfiguration();
        JobConfiguration triggerJobConfiguration = jobTrigger.getJobConfiguration();
        if (jobConfiguration != null && triggerJobConfiguration != null && jobConfiguration.getParameters() != null) {
            Map<String, Serializable> jobParameters = triggerJobConfiguration.getParameters();
            for (Map.Entry<String, Serializable> entry : jobConfiguration.getParameters().entrySet()) {
                jobParameters.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        if (jobTrigger.getScheduleTime() == null) {
            jobTrigger.setScheduleTime(jobTrigger.getSchedule(jobContext).nextSchedule());
        }
        entityManager.persist(jobTrigger);
        if (jobTrigger.getState() == JobInstanceState.NEW) {
            jobContext.getTransactionSupport().registerPostCommitListener(() -> {
                jobContext.refreshJobInstanceSchedules(jobTrigger);
            });
        }
    }

    @Override
    public void addJobInstance(JobInstance<?> jobInstance) {
        if (jobInstance instanceof JpaJobTrigger) {
            addJobTrigger((JobTrigger) jobInstance);
            return;
        }
        JpaJobInstance<?> jpaJobInstance = getJobInstance(jobInstance);
        if (jpaJobInstance instanceof JpaTriggerBasedJobInstance<?>) {
            JpaTriggerBasedJobInstance<?> jpaTriggerBasedJobInstance = (JpaTriggerBasedJobInstance<?>) jpaJobInstance;
            JobTrigger trigger = (jpaTriggerBasedJobInstance).getTrigger();
            if (trigger.getJob().getId() == null) {
                addJobTrigger(trigger);
            } else if (!entityManager.contains(trigger)) {
                setTrigger(jpaTriggerBasedJobInstance, findJobTrigger(getEntityClass(trigger), trigger.getId()));
            }
        }
        entityManager.persist(jobInstance);
        if (jobInstance.getState() == JobInstanceState.NEW) {
            jobContext.getTransactionSupport().registerPostCommitListener(() -> {
                jobContext.refreshJobInstanceSchedules(jobInstance);
            });
        }
    }

    @Override
    public List<JobInstance<?>> getJobInstancesToProcess(int partition, int partitionCount, int limit, PartitionKey partitionKey) {
        if (!(partitionKey instanceof JpaPartitionKey)) {
            throw new IllegalArgumentException("The given partition key does not implement JpaPartitionKey: " + partitionKey);
        }
        Class<? extends JobInstance<?>> jobInstanceType = partitionKey.getJobInstanceType();
        JpaPartitionKey jpaPartitionKey = (JpaPartitionKey) partitionKey;
        String partitionPredicate = jpaPartitionKey.getPartitionPredicate("e");
        String idAttributeName = jpaPartitionKey.getIdAttributeName();
        String partitionKeyAttributeName = jpaPartitionKey.getPartitionKeyAttributeName();
        String scheduleAttributeName = jpaPartitionKey.getScheduleAttributeName();
        String statePredicate = jpaPartitionKey.getStatePredicate("e");
        Object readyStateValue = jpaPartitionKey.getReadyStateValue();
        String joinFetches = jpaPartitionKey.getJoinFetches("e");
        TypedQuery<? extends JobInstance<?>> typedQuery = entityManager.createQuery(
                "SELECT e FROM " + jobInstanceType.getName() + " e " +
                        joinFetches + " " +
                        "WHERE " + statePredicate + " " +
                        (partitionPredicate.isEmpty() ? "" : "AND " + partitionPredicate + " ") +
                        (partitionCount > 1 ? "AND MOD(e." + partitionKeyAttributeName + ", " + partitionCount + ") = " + partition + " " : "") +
                        "AND e." + scheduleAttributeName + " <= CURRENT_TIMESTAMP " +
                        "ORDER BY e." + scheduleAttributeName + " ASC, e." + idAttributeName + " ASC",
                jobInstanceType
        );
        if (readyStateValue != null) {
            typedQuery.setParameter("readyState", readyStateValue);
        }
        return (List<JobInstance<?>>) (List) typedQuery
                // TODO: lockMode for update? advisory locks?
                // TODO: PostgreSQL 9.5 supports the skip locked clause, but since then, we have to use advisory locks
//                .where("FUNCTION('pg_try_advisory_xact_lock', id.userId)").eqExpression("true")
                .setHint("org.hibernate.lockMode.e", "UPGRADE_SKIPLOCKED")
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public Instant getNextSchedule(int partition, int partitionCount, PartitionKey partitionKey) {
        Class<? extends JobInstance<?>> jobInstanceType = partitionKey.getJobInstanceType();
        JpaPartitionKey jpaPartitionKey = (JpaPartitionKey) partitionKey;
        String partitionPredicate = jpaPartitionKey.getPartitionPredicate("e");
        String idAttributeName = jpaPartitionKey.getIdAttributeName();
        String partitionKeyAttributeName = jpaPartitionKey.getPartitionKeyAttributeName();
        String scheduleAttributeName = jpaPartitionKey.getScheduleAttributeName();
        String statePredicate = jpaPartitionKey.getStatePredicate("e");
        Object readyStateValue = jpaPartitionKey.getReadyStateValue();
        TypedQuery<Instant> typedQuery = entityManager.createQuery(
                "SELECT e." + scheduleAttributeName + " FROM " + jobInstanceType.getName() + " e " +
                        "WHERE " + statePredicate + " " +
                        (partitionPredicate.isEmpty() ? "" : "AND " + partitionPredicate + " ") +
                        (partitionCount > 1 ? "AND MOD(e." + partitionKeyAttributeName + ", " + partitionCount + ") = " + partition + " " : "") +
                        "AND e." + scheduleAttributeName + " <= CURRENT_TIMESTAMP " +
                        "ORDER BY e." + scheduleAttributeName + " ASC, e." + idAttributeName + " ASC",
                Instant.class
        );
        if (readyStateValue != null) {
            typedQuery.setParameter("readyState", readyStateValue);
        }

        List<Instant> nextSchedule = typedQuery.setMaxResults(1).getResultList();
        return nextSchedule.size() == 0 ? null : nextSchedule.get(0);
    }

    @Override
    public void updateJobInstance(JobInstance<?> jobInstance) {
        if (jobInstance.getJobConfiguration().getMaximumDeferCount() > jobInstance.getDeferCount()) {
            jobInstance.markDropped();
        }
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        if (!entityManager.contains(jobInstance)) {
            entityManager.merge(jobInstance);
        }
    }

    @Override
    public void onJobInstanceChunkSuccess(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobInstance);
    }

    @Override
    public void onJobInstanceError(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobInstance);
    }

    @Override
    public void onJobInstanceSuccess(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
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
        if (!entityManager.isJoinedToTransaction()) {
            entityManager.joinTransaction();
        }
        entityManager.merge(jobTrigger);
    }
}
