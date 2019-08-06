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
package com.blazebit.notify.notification;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface NotificationManager {

    public <ID> ID addNotification(Notification<ID> notification);

    public List<Notification<?>> getNotificationsToSend(int partition, int partitionCount, int limit, ChannelKey<?> channelKey);

    public Instant getNextSchedule(int partition, int partitionCount, ChannelKey<?> channelKey);

    void updateNotification(Notification<?> notification);
}
