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

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.notify.job.ConfigurationSource;
import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.PartitionKey;
import com.blazebit.notify.notification.ChannelKey;
import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.spi.NotificationPartitionKeyProvider;
import com.blazebit.notify.notification.spi.NotificationPartitionKeyProviderFactory;

@ServiceProvider(NotificationPartitionKeyProviderFactory.class)
public class MemoryNotificationPartitionKeyProvider implements NotificationPartitionKeyProvider, NotificationPartitionKeyProviderFactory {

    @Override
    public NotificationPartitionKeyProvider createNotificationPartitionKeyProvider(com.blazebit.notify.job.ServiceProvider serviceProvider, ConfigurationSource configurationSource) {
        return this;
    }

    @Override
    public PartitionKey getDefaultTriggerPartitionKey(PartitionKey defaultJobTriggerPartitionKey) {
        return defaultJobTriggerPartitionKey;
    }

    @Override
    public PartitionKey getDefaultJobInstancePartitionKey(PartitionKey defaultJobInstancePartitionKey) {
        return new PartitionKey() {
            @Override
            public boolean matches(JobInstance<?> jobInstance) {
                return !(jobInstance instanceof Notification<?>) && defaultJobInstancePartitionKey.matches(jobInstance);
            }

            @Override
            public String toString() {
                return "jobInstance";
            }
        };
    }

    @Override
    public PartitionKey getPartitionKey(PartitionKey defaultJobInstancePartitionKey, String channelType) {
        return new PartitionKey() {
            @Override
            public boolean matches(JobInstance<?> jobInstance) {
                return jobInstance instanceof Notification<?> && channelType.equals(((Notification<?>) jobInstance).getChannelType());
            }

            @Override
            public String toString() {
                if (channelType == null) {
                    return "notification";
                } else {
                    return "notification/" + channelType;
                }
            }
        };
    }
}
