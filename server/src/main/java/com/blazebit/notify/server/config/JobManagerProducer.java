/*
 * Copyright 2018 - 2025 Blazebit.
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

package com.blazebit.notify.server.config;

import com.blazebit.expression.ExpressionService;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.channel.smtp.SmtpChannel;
import com.blazebit.notify.server.notification.NotificationJobInstanceProcessorFactoryImpl;
import com.blazebit.notify.server.notification.NotificationJobProcessorFactoryImpl;
import com.blazebit.notify.server.notification.NotificationRecipientResolverImpl;
import com.blazebit.notify.template.api.TemplateContext;
import com.blazebit.persistence.CriteriaBuilderFactory;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@ApplicationScoped
public class JobManagerProducer {

    @Inject
    EntityManager entityManager;
    @Inject
    CriteriaBuilderFactory criteriaBuilderFactory;
    @Inject
    ExpressionService expressionService;

    @Resource
    ManagedScheduledExecutorService scheduledExecutorService;

    @Produces
    @ApplicationScoped
    public NotificationJobContext createNotificationJobContext() {
        return NotificationJobContext.builder()
                .withService(EntityManager.class, entityManager)
                .withService(ExpressionService.class, expressionService)
                .withService(CriteriaBuilderFactory.class, criteriaBuilderFactory)
                .withService(ScheduledExecutorService.class, scheduledExecutorService)
                .withService(TemplateContext.class, TemplateContext.builder().createContext())
                .withJobProcessorFactory(new NotificationJobProcessorFactoryImpl())
                .withJobInstanceProcessorFactory(new NotificationJobInstanceProcessorFactoryImpl())
                .withRecipientResolver(new NotificationRecipientResolverImpl())
                .withProperty(SmtpChannel.SMTP_HOST_PROPERTY, "192.168.99.100")
                .withProperty(SmtpChannel.SMTP_PORT_PROPERTY, 25)
//                .withProperty(SmtpChannel.SMTP_USER_PROPERTY, "test")
//                .withProperty(SmtpChannel.SMTP_PASSWORD_PROPERTY, "test")
                .createContext();
    }

    public void closeNotificationJobContext(@Disposes NotificationJobContext notificationJobContext) {
        notificationJobContext.stop();
    }
}
