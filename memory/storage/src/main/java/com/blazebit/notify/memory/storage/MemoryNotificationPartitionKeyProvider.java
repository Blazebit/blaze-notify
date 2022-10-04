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
package com.blazebit.notify.memory.storage;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.JobInstance;
import com.blazebit.job.PartitionKey;
import com.blazebit.notify.Notification;
import com.blazebit.notify.spi.NotificationPartitionKeyProvider;
import com.blazebit.notify.spi.NotificationPartitionKeyProviderFactory;

/**
 * A factory as well as {@link NotificationPartitionKeyProvider} implementation providing decorated partition keys.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@ServiceProvider(NotificationPartitionKeyProviderFactory.class)
public class MemoryNotificationPartitionKeyProvider implements NotificationPartitionKeyProvider, NotificationPartitionKeyProviderFactory {

    @Override
    public NotificationPartitionKeyProvider createNotificationPartitionKeyProvider(com.blazebit.job.ServiceProvider serviceProvider, ConfigurationSource configurationSource) {
        return this;
    }

    @Override
    public PartitionKey getDefaultTriggerPartitionKey(PartitionKey defaultJobTriggerPartitionKey) {
        return defaultJobTriggerPartitionKey;
    }

    @Override
    public PartitionKey getDefaultJobInstancePartitionKey(PartitionKey defaultJobInstancePartitionKey) {
        return defaultJobInstancePartitionKey;
    }

    @Override
    public PartitionKey getPartitionKey(PartitionKey defaultJobInstancePartitionKey, String channelType) {
        if (channelType == null) {
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
        return new PartitionKey() {
            @Override
            public boolean matches(JobInstance<?> jobInstance) {
                return jobInstance instanceof Notification<?> && channelType.equals(((Notification<?>) jobInstance).getChannelType()) && defaultJobInstancePartitionKey.matches(jobInstance);
            }

            @Override
            public String toString() {
                return "notification/" + channelType;
            }
        };
    }
}
