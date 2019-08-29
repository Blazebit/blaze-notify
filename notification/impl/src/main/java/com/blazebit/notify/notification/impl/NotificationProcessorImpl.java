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

package com.blazebit.notify.notification.impl;

import com.blazebit.notify.notification.*;

public class NotificationProcessorImpl<N extends Notification<?>> implements NotificationProcessor<N> {

    public static final NotificationProcessorImpl<Notification<?>> INSTANCE = new NotificationProcessorImpl<>();

    protected NotificationProcessorImpl() {
    }

    @Override
    public Object process(N notification, NotificationJobContext context) {
        Channel<NotificationRecipient<?>, NotificationMessage> channel = context.getChannel(notification.getChannelType());
        NotificationRecipient<?> recipient = notification.getRecipient();

        if (recipient == null) {
            throw new NotificationException("No recipient can be resolved from: " + notification);
        }
        NotificationMessageResolver<NotificationMessage> notificationMessageResolver;
        if (notification instanceof ConfigurationSourceProvider) {
            notificationMessageResolver = context.getNotificationMessageResolver((Class<NotificationMessage>) channel.getNotificationMessageType(), ((ConfigurationSourceProvider) notification).getConfigurationSource(context));
        } else {
            notificationMessageResolver = context.getNotificationMessageResolver((Class<NotificationMessage>) channel.getNotificationMessageType());
        }
        NotificationMessage notificationMessage;
        if (notificationMessageResolver == null) {
            if (notification instanceof NotificationMessage) {
                notificationMessage = (NotificationMessage) notification;
            } else {
                throw new NotificationException("No notification message resolver can be resolved from: " + notification);
            }
        } else {
            notificationMessage = notificationMessageResolver.resolveNotificationMessage(notification);
        }
        if (notificationMessage == null) {
            throw new NotificationException("No notification message can be resolved from: " + notification);
        }
        Object result = channel.sendNotificationMessage(recipient, notificationMessage);
        notification.markDone(result);
        return result;
    }
}
