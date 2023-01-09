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
package com.blazebit.notify.email.message;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.job.ConfigurationSource;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationMessageResolver;
import com.blazebit.notify.NotificationMessageResolverFactory;
import com.blazebit.notify.NotificationMessageResolverModelCustomizer;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * A factory for {@link EmailNotificationMessageResolver}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@ServiceProvider(NotificationMessageResolverFactory.class)
public class EmailNotificationMessageResolverFactory implements NotificationMessageResolverFactory<EmailNotificationMessage> {

    protected final List<NotificationMessageResolverModelCustomizer> modelCustomizers;

    /**
     * This is the no-parameter constructor that loads the {@link NotificationMessageResolverModelCustomizer}s.
     */
    public EmailNotificationMessageResolverFactory() {
        this.modelCustomizers = loadServices(NotificationMessageResolverModelCustomizer.class);
    }

    @Override
    public Class<EmailNotificationMessage> getNotificationMessageType() {
        return EmailNotificationMessage.class;
    }

    @Override
    public NotificationMessageResolver<EmailNotificationMessage> createNotificationMessageResolver(NotificationJobContext jobContext, ConfigurationSource configurationSource) {
        return new EmailNotificationMessageResolver(jobContext, configurationSource, modelCustomizers);
    }

    private static <T> List<T> loadServices(Class<T> serviceType) {
        List<T> services = new ArrayList<>();
        for (T service : ServiceLoader.load(serviceType)) {
            services.add(service);
        }
        return services;
    }
}
