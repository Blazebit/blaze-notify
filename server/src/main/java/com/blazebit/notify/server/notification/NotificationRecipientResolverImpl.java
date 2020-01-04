/*
 * Copyright 2018 - 2020 Blazebit.
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

import com.blazebit.domain.runtime.model.DomainModel;
import com.blazebit.domain.runtime.model.DomainType;
import com.blazebit.expression.ExpressionServiceFactory;
import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.notify.NotificationJobInstance;
import com.blazebit.notify.recipient.resolver.expression.AbstractPredicatingExpressionNotificationRecipientResolver;
import com.blazebit.notify.server.model.EmailNotificationJobInstance;
import com.blazebit.notify.server.model.EmailNotificationRecipient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class NotificationRecipientResolverImpl extends AbstractPredicatingExpressionNotificationRecipientResolver {

    @Override
    protected Map<String, DomainType> getRootDomainTypes(JobInstanceProcessingContext<?> jobContext) {
        Map<String, DomainType> rootDomainTypes = new HashMap<>();
        ExpressionServiceFactory expressionServiceFactory = jobContext.getJobContext().getService(ExpressionServiceFactory.class);
        DomainModel domainModel = expressionServiceFactory.getDomainModel();
        rootDomainTypes.put("user", domainModel.getType(EmailNotificationRecipient.class));
        return rootDomainTypes;
    }

    @Override
    protected String getRecipientPredicateExpression(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext<?> jobContext) {
        if (jobInstance instanceof EmailNotificationJobInstance) {
            return ((EmailNotificationJobInstance) jobInstance).getTrigger().getJob().getRecipientExpression();
        }
        return null;
    }
}
