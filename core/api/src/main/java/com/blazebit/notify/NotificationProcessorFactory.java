/*
 * Copyright 2018 - 2019 Blazebit.
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
package com.blazebit.notify;

/**
 * A factory for creating notification processors for a notification.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationProcessorFactory {

    /**
     * Creates a notification processor for the given notification.
     *
     * @param jobContext The notification job context
     * @param notification The notification
     * @param <N> The notification type
     * @return The notification processor
     * @throws NotificationException if the notification processor can't be created
     */
    <N extends Notification<?>> NotificationProcessor<N> createNotificationProcessor(NotificationJobContext jobContext, N notification);
}
