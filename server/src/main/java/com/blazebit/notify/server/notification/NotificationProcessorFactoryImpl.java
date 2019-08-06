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

package com.blazebit.notify.server.notification;

import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.NotificationJobContext;
import com.blazebit.notify.notification.NotificationProcessor;
import com.blazebit.notify.notification.NotificationProcessorFactory;
import com.blazebit.notify.notification.channel.smtp.SmtpNotificationProcessor;
import com.blazebit.notify.server.model.EmailNotification;

public class NotificationProcessorFactoryImpl implements NotificationProcessorFactory {

    @Override
    public <N extends Notification<?>> NotificationProcessor<N> createNotificationProcessor(NotificationJobContext jobContext, N notification) {
        if (notification instanceof EmailNotification) {
            return (NotificationProcessor<N>) SmtpNotificationProcessor.INSTANCE;
        }
        return null;
    }
}
