/*
 * Copyright 2018 - 2025 Blazebit.
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
package com.blazebit.notify.jpa.storage;

import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.JobException;
import com.blazebit.job.JobInstance;
import com.blazebit.job.JobInstanceState;
import com.blazebit.job.PartitionKey;
import com.blazebit.job.ServiceProvider;
import com.blazebit.job.jpa.model.JpaPartitionKey;
import com.blazebit.notify.spi.NotificationPartitionKeyProvider;
import java.util.function.Function;
import javax.persistence.EntityManager;

/**
 * A {@link NotificationPartitionKeyProvider} implementation that makes use of the JPA metamodel and configuration attributes for creating relevant {@link JpaPartitionKey}s.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class JpaNotificationPartitionKeyProvider implements NotificationPartitionKeyProvider {

    /**
     * Configuration property for the notification id attribute name.
     * The default value is "id".
     */
    public static final String NOTIFICATION_ID_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_id_attribute_name";
    /**
     * Configuration property for the notification partition key attribute name.
     * The default value is "recipient.id".
     */
    public static final String NOTIFICATION_PARTITION_KEY_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_partition_key_attribute_name";
    /**
     * Configuration property for the notification schedule attribute name.
     * The default value is "scheduleTime".
     */
    public static final String NOTIFICATION_SCHEDULE_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_schedule_attribute_name";
    /**
     * Configuration property for the notification last execution time attribute name.
     * The default value is "lastExecutionTime".
     */
    public static final String NOTIFICATION_LAST_EXECUTION_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_last_execution_attribute_name";
    /**
     * Configuration property for the notification state attribute name.
     * The default value is "state".
     */
    public static final String NOTIFICATION_STATE_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_state_attribute_name";
    /**
     * Configuration property for a mapping function <code>Function&lt;JobInstanceState, Object&gt;</code> from notification state to the actual model state.
     * The default value is a function that just returns the passed in {@link JobInstanceState}.
     */
    public static final String NOTIFICATION_STATE_VALUE_MAPPING_FUNCTION_PROPERTY = "notification.jpa.storage.notification_state_value_mapping_function";
    /**
     * Configuration property for the notification channel attribute name.
     * The default value is "channelType".
     */
    public static final String NOTIFICATION_CHANNEL_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_channel_attribute_name";

    private final String notificationIdAttributeName;
    private final String partitionKeyAttributeName;
    private final String notificationScheduleAttributeName;
    private final String notificationLastExecutionAttributeName;
    private final String notificationStateAttributeName;
    private final Function<JobInstanceState, Object> notificationStateValueMappingFunction;
    private final String channelAttributeName;

    /**
     * Creates a new notification partition key provider that makes use of the service provider and configuration source to determine the {@link EntityManager} and attribute names.
     *
     * @param serviceProvider     The service provider
     * @param configurationSource The configuration source
     */
    public JpaNotificationPartitionKeyProvider(ServiceProvider serviceProvider, ConfigurationSource configurationSource) {
        this(
            serviceProvider.getService(EntityManager.class),
            configurationSource.getPropertyOrDefault(NOTIFICATION_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
            configurationSource.getPropertyOrDefault(NOTIFICATION_PARTITION_KEY_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "recipient.id"),
            configurationSource.getPropertyOrDefault(NOTIFICATION_SCHEDULE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "scheduleTime"),
            configurationSource.getPropertyOrDefault(NOTIFICATION_LAST_EXECUTION_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "lastExecution"),
            configurationSource.getPropertyOrDefault(NOTIFICATION_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "state"),
            configurationSource.getPropertyOrDefault(NOTIFICATION_STATE_VALUE_MAPPING_FUNCTION_PROPERTY, Function.class, null, o -> Function.identity()),
            configurationSource.getPropertyOrDefault(NOTIFICATION_CHANNEL_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "channelType")
        );
    }

    /**
     * Creates a new notification partition key provider.
     *
     * @param entityManager                          The entity manager
     * @param notificationIdAttributeName            The notification id attribute name
     * @param notificationPartitionKeyAttributeName  The notification partition key attribute name
     * @param notificationScheduleAttributeName      The notification schedule attribute name
     * @param notificationLastExecutionAttributeName The notification last execution attribute name
     * @param notificationStateAttributeName         The notification state attribute name
     * @param notificationStateValueMappingFunction  The notification state value mapping function
     * @param channelAttributeName                   The notification channel attribute name
     */
    public JpaNotificationPartitionKeyProvider(EntityManager entityManager, String notificationIdAttributeName, String notificationPartitionKeyAttributeName, String notificationScheduleAttributeName,
                                               String notificationLastExecutionAttributeName, String notificationStateAttributeName, Function<JobInstanceState, Object> notificationStateValueMappingFunction, String channelAttributeName) {
        if (entityManager == null) {
            throw new JobException("No entity manager given!");
        }

        this.notificationIdAttributeName = notificationIdAttributeName;
        this.notificationScheduleAttributeName = notificationScheduleAttributeName;
        this.notificationLastExecutionAttributeName = notificationLastExecutionAttributeName;
        this.notificationStateAttributeName = notificationStateAttributeName;
        this.notificationStateValueMappingFunction = notificationStateValueMappingFunction;
        this.partitionKeyAttributeName = notificationPartitionKeyAttributeName;
        this.channelAttributeName = channelAttributeName;
    }

    @Override
    public PartitionKey getDefaultTriggerPartitionKey(PartitionKey defaultJobTriggerPartitionKey) {
        return defaultJobTriggerPartitionKey;
    }

    @Override
    public PartitionKey getDefaultJobInstancePartitionKey(PartitionKey defaultJobInstancePartitionKey) {
        return defaultJobInstancePartitionKey;
    }

    @Override
    public PartitionKey getPartitionKey(PartitionKey defaultJobInstancePartitionKey, String channelType) {
        if (channelType == null) {
            return defaultJobInstancePartitionKey;
        }
        JpaPartitionKey k = (JpaPartitionKey) defaultJobInstancePartitionKey;
        String name = k.toString() + "/" + channelType;
        return new JpaPartitionKey() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Class<? extends JobInstance<?>> getEntityClass() {
                return k.getEntityClass();
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
            public String getStateExpression(String jobAlias) {
                return jobAlias + "." + notificationStateAttributeName;
            }

            @Override
            public String getLastExecutionAttributeName() {
                return notificationLastExecutionAttributeName;
            }

            @Override
            public Function<JobInstanceState, Object> getStateValueMappingFunction() {
                return notificationStateValueMappingFunction;
            }

            @Override
            public String getJoinFetches(String jobAlias) {
                return "";
            }

            @Override
            public String toString() {
                return getName();
            }
        };
    }
}
