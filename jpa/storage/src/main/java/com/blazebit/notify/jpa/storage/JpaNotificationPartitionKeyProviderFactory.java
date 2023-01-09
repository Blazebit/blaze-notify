/*
 * Copyright 2018 - 2023 Blazebit.
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
package com.blazebit.notify.jpa.storage;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.job.ConfigurationSource;
import com.blazebit.notify.spi.NotificationPartitionKeyProvider;
import com.blazebit.notify.spi.NotificationPartitionKeyProviderFactory;

/**
 * A factory for {@link JpaNotificationPartitionKeyProvider}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@ServiceProvider(NotificationPartitionKeyProviderFactory.class)
public class JpaNotificationPartitionKeyProviderFactory implements NotificationPartitionKeyProviderFactory {

    @Override
    public NotificationPartitionKeyProvider createNotificationPartitionKeyProvider(com.blazebit.job.ServiceProvider serviceProvider, ConfigurationSource configurationSource) {
        return new JpaNotificationPartitionKeyProvider(serviceProvider, configurationSource);
    }

}
