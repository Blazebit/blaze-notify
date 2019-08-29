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
import com.blazebit.notify.job.jpa.model.JpaPartitionKey;
import com.blazebit.notify.job.spi.PartitionKeyProvider;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class JpaPartitionKeyProvider implements PartitionKeyProvider {

    public static final String JOB_TRIGGER_ID_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_id_attribute_name";
    public static final String JOB_TRIGGER_SCHEDULE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_schedule_attribute_name";
    public static final String JOB_TRIGGER_STATE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_state_attribute_name";
    public static final String JOB_TRIGGER_STATE_READY_VALUE_PROPERTY = "job.jpa.storage.job_trigger_state_ready_value";
    public static final String JOB_INSTANCE_ID_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_id_attribute_name";
    public static final String JOB_INSTANCE_PARTITION_KEY_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_partition_key_attribute_name";
    public static final String JOB_INSTANCE_SCHEDULE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_schedule_attribute_name";
    public static final String JOB_INSTANCE_STATE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_state_attribute_name";
    public static final String JOB_INSTANCE_STATE_READY_VALUE_PROPERTY = "job.jpa.storage.job_instance_state_ready_value";

    private final Collection<PartitionKey> jobTriggerPartitionKeys;
    private final Collection<PartitionKey> jobInstancePartitionKeys;

    public JpaPartitionKeyProvider(ServiceProvider serviceProvider, ConfigurationSource configurationSource) {
        this(
                serviceProvider.getService(EntityManager.class),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_SCHEDULE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "scheduleTime"),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "state"),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_STATE_READY_VALUE_PROPERTY, Object.class, null, o -> JobInstanceState.NEW),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_PARTITION_KEY_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_SCHEDULE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "scheduleTime"),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "state"),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_STATE_READY_VALUE_PROPERTY, Object.class, null, o -> JobInstanceState.NEW)
        );
    }

    public JpaPartitionKeyProvider(EntityManager entityManager, String jobTriggerIdAttributeName, String jobTriggerScheduleAttributeName, String jobTriggerStateAttributeName, Object jobTriggerStateReadyValue,
                                   String jobInstanceIdAttributeName, String jobInstancePartitionKeyAttributeName, String jobInstanceScheduleAttributeName, String jobInstanceStateAttributeName, Object jobInstanceStateReadyValue) {
        if (entityManager == null) {
            throw new JobException("No entity manager given!");
        }

        Collection<PartitionKey> jobTriggerPartitionKeys = new ArrayList<>();
        Collection<PartitionKey> jobInstancePartitionKeys = new ArrayList<>();
        for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
            Class<?> javaType = entity.getJavaType();
            if (javaType != null) {
                if (JobTrigger.class.isAssignableFrom(javaType)) {
                    jobTriggerPartitionKeys.add(
                            JpaPartitionKey.builder()
                                    .withName(entity.getName())
                                    .withJobInstanceType((Class<? extends JobInstance<?>>) javaType)
                                    .withIdAttributeName(jobTriggerIdAttributeName)
                                    .withScheduleAttributeName(jobTriggerScheduleAttributeName)
                                    .withPartitionKeyAttributeName(jobTriggerIdAttributeName)
                                    .withStateAttributeName(jobTriggerStateAttributeName)
                                    .withReadyStateValue(jobTriggerStateReadyValue)
                                    .build()
                    );
                } else if (JobInstance.class.isAssignableFrom(javaType)) {
                    jobInstancePartitionKeys.add(
                            JpaPartitionKey.builder()
                                    .withName(entity.getName())
                                    .withJobInstanceType((Class<? extends JobInstance<?>>) javaType)
                                    .withIdAttributeName(jobInstanceIdAttributeName)
                                    .withScheduleAttributeName(jobInstanceScheduleAttributeName)
                                    .withPartitionKeyAttributeName(jobInstancePartitionKeyAttributeName)
                                    .withStateAttributeName(jobInstanceStateAttributeName)
                                    .withReadyStateValue(jobInstanceStateReadyValue)
                                    .build()
                    );
                }
            }
        }
        this.jobTriggerPartitionKeys = jobTriggerPartitionKeys;
        this.jobInstancePartitionKeys = jobInstancePartitionKeys;
    }

    @Override
    public Collection<PartitionKey> getDefaultTriggerPartitionKeys() {
        return jobTriggerPartitionKeys;
    }

    @Override
    public Collection<PartitionKey> getDefaultJobInstancePartitionKeys() {
        return jobInstancePartitionKeys;
    }
}
