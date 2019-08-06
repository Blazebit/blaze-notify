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

package com.blazebit.notify.notification.channel.smtp;

import com.blazebit.notify.notification.*;

public class SmtpNotificationProcessor<N extends Notification<?>> implements NotificationProcessor<N> {

    public static final SmtpNotificationProcessor<Notification<?>> INSTANCE = new SmtpNotificationProcessor<>();

    protected SmtpNotificationProcessor() {
    }

    @Override
    public Object process(N notification, NotificationJobContext context) {
        SmtpChannel channel = context.getChannel(SmtpChannel.KEY);
        SmtpNotificationRecipient<?> recipient = getRecipient(notification, context);

        if (recipient == null) {
            throw new NotificationException("No recipient can be resolved from: " + notification);
        }
        NotificationMessageResolver<SmtpNotificationMessage> notificationMessageResolver =
                context.getNotificationMessageResolver(notification, SmtpNotificationMessage.class);
        if (notificationMessageResolver == null) {
            throw new NotificationException("No notification message resolver can be resolved from: " + notification);
        }
        SmtpNotificationMessage smtpNotificationMessage = notificationMessageResolver.resolveNotificationMessage(notification);
        if (smtpNotificationMessage == null) {
            throw new NotificationException("No notification message can be resolved from: " + notification);
        }
        return channel.sendNotificationMessage(recipient, smtpNotificationMessage);
    }

    protected SmtpNotificationRecipient<?> getRecipient(N notification, NotificationJobContext context) {
        return (SmtpNotificationRecipient<?>) notification.getRecipient();
    }
}
