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

package com.blazebit.notify.server.rest.impl;

import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.server.model.FromEmail;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@Singleton
@Startup
public class StartupBean {

    @Inject
    EntityManager entityManager;

    @Inject
    NotificationJobContext notificationJobContext;

    @PostConstruct
    public void init() {
        FromEmail fromEmail = new FromEmail();
        fromEmail.setName("Christian Beikov");
        fromEmail.setEmail("christian@blazebit.com");
        fromEmail.setReplyToName(fromEmail.getName());
        fromEmail.setReplyToEmail(fromEmail.getEmail());
        entityManager.persist(fromEmail);
        notificationJobContext.triggerNotificationScan(0L);
    }
}
