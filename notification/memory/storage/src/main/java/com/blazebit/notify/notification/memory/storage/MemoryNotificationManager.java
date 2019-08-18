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

package com.blazebit.notify.notification.memory.storage;

import com.blazebit.notify.notification.*;
import com.blazebit.notify.notification.memory.model.AbstractNotification;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MemoryNotificationManager implements NotificationManager {

    private static final String NOTIFICATIONS_PROPERTY = "notification.memory.storage.notifications";
    private static final Comparator<? super Notification<?>> COMPARATOR;

    private final NotificationJobContext jobContext;
    private final AtomicLong notificationCounter = new AtomicLong();
    private final Set<Notification<?>> notifications;

    static {
        Comparator<Notification<Comparable>> comparator = Comparator.comparing(Notification::getScheduleTime);
        comparator = comparator.reversed().thenComparing(Notification::getId);
        COMPARATOR = (Comparator) comparator;
    }

    public MemoryNotificationManager(NotificationJobContext jobContext) {
        this(
                jobContext,
                jobContext.getPropertyOrDefault(NOTIFICATIONS_PROPERTY, Set.class, null, o -> new HashSet<>())
        );
    }

    public MemoryNotificationManager(NotificationJobContext jobContext, Set<Notification<?>> notifications) {
        this.jobContext = jobContext;
        this.notifications = notifications;
    }

    @Override
    public <ID> ID addNotification(Notification<ID> notification) {
        ((AbstractNotification) notification).setId(notificationCounter.incrementAndGet());
        notifications.add(notification);
        return notification.getId();
    }

    @Override
    public List<Notification<?>> getNotificationsToSend(int partition, int partitionCount, int limit, ChannelKey<?> channelKey) {
//        if (notificationChannelTypeAttributeName == null && channelKey != null) {
//            throw new IllegalArgumentException("Can't filter based on the channel key since there is no channel type attribute name configured!");
//        }
        return notifications.stream()
                .filter(n -> n.getState() == NotificationState.NEW && ((Number) n.getId()).longValue() % partitionCount == partition)
                .sorted(COMPARATOR)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Instant getNextSchedule(int partition, int partitionCount, ChannelKey<?> channelKey) {
//        if (notificationChannelTypeAttributeName == null && channelKey != null) {
//            throw new IllegalArgumentException("Can't filter based on the channel key since there is no channel type attribute name configured!");
//        }
        return notifications.stream()
                .filter(n -> n.getState() == NotificationState.NEW && ((Number) n.getId()).longValue() % partitionCount == partition)
                .min(COMPARATOR)
                .map(Notification::getScheduleTime)
                .orElse(null);
    }

    @Override
    public void updateNotification(Notification<?> notification) {
        if (notification.getMaximumDeferCount() > notification.getDeferCount()) {
            notification.markDropped();
        }
    }
}
