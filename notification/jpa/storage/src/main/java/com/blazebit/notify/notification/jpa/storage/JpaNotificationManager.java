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

import com.blazebit.notify.job.JobException;
import com.blazebit.notify.job.spi.TransactionSupport;
import com.blazebit.notify.notification.*;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public class JpaNotificationManager implements NotificationManager {

    private static final String NOTIFICATION_ID_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_id_attribute_name";
    private static final String NOTIFICATION_CHANNEL_TYPE_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_channel_type_attribute_name";
    private static final String NOTIFICATION_SCHEDULE_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_schedule_attribute_name";
    private static final String NOTIFICATION_STATE_ATTRIBUTE_NAME_PROPERTY = "notification.jpa.storage.notification_state_attribute_name";
    private static final String NOTIFICATION_STATE_READY_VALUE_PROPERTY = "notification.jpa.storage.notification_state_ready_value";

    private final NotificationJobContext jobContext;
    private final EntityManager entityManager;
    private final String notificationIdAttributeName;
    private final String notificationChannelTypeAttributeName;
    private final String notificationScheduleAttributeName;
    private final String notificationStateAttributeName;
    private final Object notificationStateReadyValue;

    public JpaNotificationManager(NotificationJobContext jobContext) {
        this(
                jobContext,
                jobContext.getService(EntityManager.class),
                jobContext.getPropertyOrDefault(NOTIFICATION_ID_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "id"),
                jobContext.getPropertyOrDefault(NOTIFICATION_CHANNEL_TYPE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> null),
                jobContext.getPropertyOrDefault(NOTIFICATION_SCHEDULE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "scheduleTime"),
                jobContext.getPropertyOrDefault(NOTIFICATION_STATE_ATTRIBUTE_NAME_PROPERTY, String.class, Function.identity(), o -> "state"),
                jobContext.getPropertyOrDefault(NOTIFICATION_STATE_READY_VALUE_PROPERTY, Object.class, null, o -> NotificationState.NEW)
        );
    }

    public JpaNotificationManager(NotificationJobContext jobContext, EntityManager entityManager, String notificationIdAttributeName, String notificationChannelTypeAttributeName, String notificationScheduleAttributeName, String notificationStateAttributeName, Object notificationStateReadyValue) {
        if (entityManager == null) {
            throw new JobException("No entity manager given!");
        }
        if (jobContext.getTransactionSupport() == TransactionSupport.NOOP) {
            throw new JobException("JPA storage requires transaction support!");
        }
        this.jobContext = jobContext;
        this.entityManager = entityManager;
        this.notificationIdAttributeName = notificationIdAttributeName;
        this.notificationChannelTypeAttributeName = notificationChannelTypeAttributeName;
        this.notificationScheduleAttributeName = notificationScheduleAttributeName;
        this.notificationStateAttributeName = notificationStateAttributeName;
        this.notificationStateReadyValue = notificationStateReadyValue;
    }

    @Override
    public <ID> ID addNotification(Notification<ID> notification) {
        entityManager.persist(notification);
        return notification.getId();
    }

    @Override
    public List<Notification<?>> getNotificationsToSend(int partition, int partitionCount, int limit, ChannelKey<?> channelKey) {
        if (notificationChannelTypeAttributeName == null && channelKey != null) {
            throw new IllegalArgumentException("Can't filter based on the channel key since there is no channel type attribute name configured!");
        }
        String query = "SELECT e FROM " + Notification.class.getName() + " e " +
                "WHERE e." + notificationStateAttributeName + " = :readyState " +
                (channelKey != null ? "AND e." + notificationChannelTypeAttributeName + " = '" + channelKey.getChannelType() + "' " : "") +
                (partitionCount > 1 ? "AND MOD(e." + notificationIdAttributeName + ", " + partitionCount + ") = " + partition + " " : "") +
                "AND e." + notificationScheduleAttributeName + " <= CURRENT_TIMESTAMP " +
                "ORDER BY e." + notificationScheduleAttributeName + " ASC, e." + notificationIdAttributeName + " ASC";
        return (List<Notification<?>>) (List) entityManager.createQuery(query, Notification.class)
                .setParameter("readyState", notificationStateReadyValue)
                // TODO: lockMode for update? advisory locks?
                // TODO: PostgreSQL 9.5 supports the skip locked clause, but since then, we have to use advisory locks
//                .where("FUNCTION('pg_try_advisory_xact_lock', id.userId)").eqExpression("true")
                .setHint("org.hibernate.lockMode.e", "UPGRADE_SKIPLOCKED")
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public Instant getNextSchedule(int partition, int partitionCount, ChannelKey<?> channelKey) {
        if (notificationChannelTypeAttributeName == null && channelKey != null) {
            throw new IllegalArgumentException("Can't filter based on the channel key since there is no channel type attribute name configured!");
        }
        String query = "SELECT e." + notificationScheduleAttributeName + " FROM " + Notification.class.getName() + " e " +
                (channelKey != null ? "AND e." + notificationChannelTypeAttributeName + " = '" + channelKey.getChannelType() + "' " : "") +
                "WHERE e." + notificationStateAttributeName + " = :readyState " +
                (partitionCount > 1 ? "AND MOD(e." + notificationIdAttributeName + ", " + partitionCount + ") = " + partition + " " : "") +
                "ORDER BY e." + notificationScheduleAttributeName + " ASC, e." + notificationIdAttributeName + " ASC";
        List<Instant> nextSchedule = entityManager.createQuery(query, Instant.class)
                .setParameter("readyState", notificationStateReadyValue)
                .setMaxResults(1)
                .getResultList();
        return nextSchedule.size() == 0 ? null : nextSchedule.get(0);
    }

    @Override
    public void updateNotification(Notification<?> notification) {
        if (notification.getMaximumDeferCount() > notification.getDeferCount()) {
            notification.markDropped();
        }
        if (!entityManager.contains(notification)) {
            entityManager.merge(notification);
        }
    }
}
