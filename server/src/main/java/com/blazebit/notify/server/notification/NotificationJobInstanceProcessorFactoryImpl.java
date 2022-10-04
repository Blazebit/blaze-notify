/*
 * Copyright 2018 - 2022 Blazebit.
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

import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationJobInstance;
import com.blazebit.notify.NotificationJobInstanceProcessor;
import com.blazebit.notify.NotificationJobInstanceProcessorFactory;
import com.blazebit.notify.server.model.EmailNotificationJobInstance;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class NotificationJobInstanceProcessorFactoryImpl implements NotificationJobInstanceProcessorFactory {

    @Override
    public <T extends NotificationJobInstance<?, ?>> NotificationJobInstanceProcessor<?, T> createJobInstanceProcessor(NotificationJobContext jobContext, T jobInstance) {
        if (jobInstance instanceof EmailNotificationJobInstance) {
            return (NotificationJobInstanceProcessor<?, T>) EmailNotificationJobInstanceProcessor.INSTANCE;
        }
        return null;
    }
}
