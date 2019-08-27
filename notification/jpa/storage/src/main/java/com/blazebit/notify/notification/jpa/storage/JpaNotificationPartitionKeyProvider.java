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
package com.blazebit.notify.notification.jpa.storage;

import com.blazebit.notify.job.*;
import com.blazebit.notify.job.jpa.model.JpaPartitionKey;
import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.spi.NotificationPartitionKeyProvider;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.util.function.Function;

public class JpaNotificationPartitionKeyProvider implements NotificationPartitionKeyProvider {

    public static final String NOTIFICATION_ENTITY_CLASS_PROPERTY = "notification.jpa.storage.notification_instance_entity_class";
    public static final String NOTIFICATION_ID_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_instance_id_attribute_name";
    public static final String NOTIFICATION_PARTITION_KEY_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_partition_key_attribute_name";
    public static final String NOTIFICATION_SCHEDULE_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_instance_schedule_attribute_name";
    public static final String NOTIFICATION_STATE_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_instance_state_attribute_name";
    public static final String NOTIFICATION_STATE_READY_VALUE_PROPERTY = "notification.jpa.storage.notification_instance_state_ready_value";
    public static final String NOTIFICATION_CHANNEL_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_channel_attribute_name";

    private final String nonNotificationPredicate;
    private final Class<?> notificationEntityClass;
    private final String notificationIdAttributeName;
    private final String partitionKeyAttributeName;
    private final String notificationScheduleAttributeName;
    private final String notificationStateAttributeName;
    private final Object notificationStateReadyValue;
    private final String channelAttributeName;

    public JpaNotificationPartitionKeyProvider(ServiceProvider serviceProvider, ConfigurationSource configurationSource) {
        this(
                serviceProvider.getService(EntityManager.class),
                configurationSource.getPropertyOrDefault(NOTIFICATION_ENTITY_CLASS_PROPERTY, Class.class, JpaNotificationPartitionKeyProvider::forName, o -> Notification.class),
                configurationSource.getPropertyOrDefault(NOTIFICATION_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                configurationSource.getPropertyOrDefault(NOTIFICATION_PARTITION_KEY_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "recipient.id"),
                configurationSource.getPropertyOrDefault(NOTIFICATION_SCHEDULE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "scheduleTime"),
                configurationSource.getPropertyOrDefault(NOTIFICATION_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "state"),
                configurationSource.getPropertyOrDefault(NOTIFICATION_STATE_READY_VALUE_PROPERTY, Object.class, null, o -> JobInstanceState.NEW),
                configurationSource.getPropertyOrDefault(NOTIFICATION_CHANNEL_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "channelType")
        );
    }

    public JpaNotificationPartitionKeyProvider(EntityManager entityManager, Class<?> notificationEntityClass, String notificationIdAttributeName, String notificationPartitionKeyAttributeName, String notificationScheduleAttributeName, String notificationStateAttributeName, Object notificationStateReadyValue, String channelAttributeName) {
        if (entityManager == null) {
            throw new JobException("No entity manager given!");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("NOT IN (");
        int emptyLength = sb.length();
        for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
            if (entity.getJavaType() != null) {
                if (JobTrigger.class.isAssignableFrom(entity.getJavaType()) || Notification.class.isAssignableFrom(entity.getJavaType())) {
                    sb.append(entity.getName()).append(", ");
                }
            }
        }

        if (emptyLength == sb.length()) {
            this.nonNotificationPredicate = null;
        } else {
            sb.setLength(sb.length() - 2);
            sb.append(')');
            this.nonNotificationPredicate = sb.toString();
        }
        this.notificationEntityClass = notificationEntityClass;
        this.notificationIdAttributeName = notificationIdAttributeName;
        this.notificationScheduleAttributeName = notificationScheduleAttributeName;
        this.notificationStateAttributeName = notificationStateAttributeName;
        this.notificationStateReadyValue = notificationStateReadyValue;
        this.partitionKeyAttributeName = notificationPartitionKeyAttributeName;
        this.channelAttributeName = channelAttributeName;
    }

    private static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new JobException("Could not resolve class for name!", e);
        }
    }

    @Override
    public PartitionKey getDefaultTriggerPartitionKey(PartitionKey defaultJobTriggerPartitionKey) {
        return defaultJobTriggerPartitionKey;
    }

    @Override
    public PartitionKey getDefaultJobInstancePartitionKey(PartitionKey defaultJobInstancePartitionKey) {
        JpaPartitionKey k = (JpaPartitionKey) defaultJobInstancePartitionKey;
        return new JpaPartitionKey() {
            @Override
            public String getPartitionPredicate(String jobAlias) {
                String partitionPredicate = k.getPartitionPredicate(jobAlias);
                // We don't need a type filter if we already have a concrete job instance type
                if (nonNotificationPredicate == null || ((Class<?>) k.getJobInstanceType()) != JobInstance.class) {
                    return partitionPredicate;
                }
                if (partitionPredicate == null) {
                    partitionPredicate = "";
                } else {
                    partitionPredicate += " AND ";
                }
                return partitionPredicate + "TYPE(" + jobAlias + ") " + nonNotificationPredicate;
            }

            @Override
            public Class<? extends JobInstance<?>> getJobInstanceType() {
                return k.getJobInstanceType();
            }

            @Override
            public String getIdAttributeName() {
                return k.getIdAttributeName();
            }

            @Override
            public String getScheduleAttributeName() {
                return k.getScheduleAttributeName();
            }

            @Override
            public String getPartitionKeyAttributeName() {
                return k.getPartitionKeyAttributeName();
            }

            @Override
            public String getStatePredicate(String jobAlias) {
                return k.getStatePredicate(jobAlias);
            }

            @Override
            public Object getReadyStateValue() {
                return k.getReadyStateValue();
            }

            @Override
            public String getJoinFetches(String jobAlias) {
                return k.getJoinFetches(jobAlias);
            }

            @Override
            public String toString() {
                return "jobInstance";
            }
        };
    }

    @Override
    public PartitionKey getPartitionKey(PartitionKey defaultJobInstancePartitionKey, String channelType) {
        JpaPartitionKey k = (JpaPartitionKey) defaultJobInstancePartitionKey;
        return new JpaPartitionKey() {
            @Override
            public Class<? extends JobInstance<?>> getJobInstanceType() {
                return (Class<? extends JobInstance<?>>) (Class<?>) notificationEntityClass;
            }

            @Override
            public String getPartitionPredicate(String jobAlias) {
                return jobAlias + "." + channelAttributeName + " = '" + channelType + "'";
            }

            @Override
            public String getIdAttributeName() {
                return notificationIdAttributeName;
            }

            @Override
            public String getScheduleAttributeName() {
                return notificationScheduleAttributeName;
            }

            @Override
            public String getPartitionKeyAttributeName() {
                return partitionKeyAttributeName;
            }

            @Override
            public String getStatePredicate(String jobAlias) {
                return jobAlias + "." + notificationStateAttributeName + " = :readyState";
            }

            @Override
            public Object getReadyStateValue() {
                return notificationStateReadyValue;
            }

            @Override
            public String getJoinFetches(String jobAlias) {
                return "";
            }

            @Override
            public String toString() {
                if (channelType == null) {
                    return "notification";
                } else {
                    return "notification/" + channelType;
                }
            }
        };
    }
}
