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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class JpaPartitionKeyProvider implements PartitionKeyProvider {

    public static final String JOB_TRIGGER_ENTITY_CLASS_PROPERTY = "job.jpa.storage.job_trigger_entity_class";
    public static final String JOB_TRIGGER_ID_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_id_attribute_name";
    public static final String JOB_TRIGGER_SCHEDULE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_schedule_attribute_name";
    public static final String JOB_TRIGGER_STATE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_trigger_state_attribute_name";
    public static final String JOB_TRIGGER_STATE_READY_VALUE_PROPERTY = "job.jpa.storage.job_trigger_state_ready_value";
    public static final String JOB_INSTANCE_ENTITY_CLASS_PROPERTY = "job.jpa.storage.job_instance_entity_class";
    public static final String JOB_INSTANCE_ID_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_id_attribute_name";
    public static final String JOB_INSTANCE_PARTITION_KEY_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_partition_key_attribute_name";
    public static final String JOB_INSTANCE_SCHEDULE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_schedule_attribute_name";
    public static final String JOB_INSTANCE_STATE_ATTRIBUTE_NAME_PROPERTY = "job.jpa.storage.job_instance_state_attribute_name";
    public static final String JOB_INSTANCE_STATE_READY_VALUE_PROPERTY = "job.jpa.storage.job_instance_state_ready_value";

    private final JpaPartitionKey jobTriggerPartitionKey;
    private final JpaPartitionKey jobInstancePartitionKey;

    public JpaPartitionKeyProvider(ServiceProvider serviceProvider, ConfigurationSource configurationSource) {
        this(
                serviceProvider.getService(EntityManager.class),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_ENTITY_CLASS_PROPERTY, Class.class, JpaPartitionKeyProvider::forName, o -> JobTrigger.class),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_SCHEDULE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "scheduleTime"),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "state"),
                configurationSource.getPropertyOrDefault(JOB_TRIGGER_STATE_READY_VALUE_PROPERTY, Object.class, null, o -> JobInstanceState.NEW),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_ENTITY_CLASS_PROPERTY, Class.class, JpaPartitionKeyProvider::forName, o -> JobInstance.class),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_PARTITION_KEY_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_SCHEDULE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "scheduleTime"),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "state"),
                configurationSource.getPropertyOrDefault(JOB_INSTANCE_STATE_READY_VALUE_PROPERTY, Object.class, null, o -> JobInstanceState.NEW)
        );
    }

    public JpaPartitionKeyProvider(EntityManager entityManager, Class<?> jobTriggerEntityClass, String jobTriggerIdAttributeName, String jobTriggerScheduleAttributeName, String jobTriggerStateAttributeName, Object jobTriggerStateReadyValue,
                                   Class<?> jobInstanceEntityClass, String jobInstanceIdAttributeName, String jobInstancePartitionKeyAttributeName, String jobInstanceScheduleAttributeName, String jobInstanceStateAttributeName, Object jobInstanceStateReadyValue) {
        if (entityManager == null) {
            throw new JobException("No entity manager given!");
        }

        Set<Class<?>> nonTriggerJobInstanceTypes = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        sb.append("NOT IN (");
        for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
            if (entity.getJavaType() != null) {
                if (JobTrigger.class.isAssignableFrom(entity.getJavaType())) {
                    sb.append(entity.getName()).append(", ");
                } else {
                    nonTriggerJobInstanceTypes.add(entity.getJavaType());
                }
            }
        }
        sb.setLength(sb.length() - 2);
        sb.append(')');

        String nonTriggerPredicate = sb.toString();

        this.jobTriggerPartitionKey = JpaPartitionKey.builder()
                .withName("jobTrigger")
                .withJobInstanceType((Class<? extends JobInstance<?>>) (Class<?>) jobTriggerEntityClass)
                .withIdAttributeName(jobTriggerIdAttributeName)
                .withScheduleAttributeName(jobTriggerScheduleAttributeName)
                .withPartitionKeyAttributeName(jobTriggerIdAttributeName)
                .withStateAttributeName(jobTriggerStateAttributeName)
                .withReadyStateValue(jobTriggerStateReadyValue)
                .build();

        JpaPartitionKey.JpaPartitionKeyBuilder jpaPartitionKeyBuilder = JpaPartitionKey.builder()
                .withName("jobInstance");

        // If there is no concrete job instance type given, we need a non trigger predicate
        if (jobInstanceEntityClass == JobInstance.class) {
            if (nonTriggerJobInstanceTypes.size() == 1) {
                // Unless there is only one job instance entity type
                jobInstanceEntityClass = nonTriggerJobInstanceTypes.iterator().next();
            } else {
                jpaPartitionKeyBuilder.withPartitionPredicateProvider(alias -> "TYPE(" + alias + ") " + nonTriggerPredicate);
            }
        }
        this.jobInstancePartitionKey = jpaPartitionKeyBuilder.withJobInstanceType((Class<? extends JobInstance<?>>) (Class<?>) jobInstanceEntityClass)
                .withIdAttributeName(jobInstanceIdAttributeName)
                .withScheduleAttributeName(jobInstanceScheduleAttributeName)
                .withPartitionKeyAttributeName(jobInstancePartitionKeyAttributeName)
                .withStateAttributeName(jobInstanceStateAttributeName)
                .withReadyStateValue(jobInstanceStateReadyValue)
                .build();
    }

    private static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new JobException("Could not resolve class for name!", e);
        }
    }

    @Override
    public PartitionKey getDefaultTriggerPartitionKey() {
        return jobTriggerPartitionKey;
    }

    @Override
    public PartitionKey getDefaultJobInstancePartitionKey() {
        return jobInstancePartitionKey;
    }
}
