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
package com.blazebit.notify.spi;

import com.blazebit.job.ConfigurationSource;
import com.blazebit.job.ServiceProvider;

/**
 * Interface implemented by the notification implementation provider.
 *
 * Implementations are instantiated via {@link java.util.ServiceLoader}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationPartitionKeyProviderFactory {

    /**
     * Creates a notification partition key provider based on the given services and configuration.
     *
     * @param serviceProvider The service provider
     * @param configurationSource The configuration source
     * @return a new notification partition key provider
     */
    NotificationPartitionKeyProvider createNotificationPartitionKeyProvider(ServiceProvider serviceProvider, ConfigurationSource configurationSource);
}
