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

package com.blazebit.notify.server.notification;

import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationJobProcessor;
import com.blazebit.notify.NotificationJobProcessorFactory;
import com.blazebit.notify.NotificationJobTrigger;
import com.blazebit.notify.server.model.EmailNotificationJobTrigger;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class NotificationJobProcessorFactoryImpl implements NotificationJobProcessorFactory {

    @Override
    public <T extends NotificationJobTrigger> NotificationJobProcessor<T> createJobProcessor(NotificationJobContext jobContext, T jobTrigger) {
        if (jobTrigger instanceof EmailNotificationJobTrigger) {
            return (NotificationJobProcessor<T>) EmailNotificationJobProcessor.INSTANCE;
        }
        return null;
    }
}
