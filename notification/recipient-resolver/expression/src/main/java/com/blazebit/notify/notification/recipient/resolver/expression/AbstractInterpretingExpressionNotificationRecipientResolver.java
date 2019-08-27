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
package com.blazebit.notify.notification.recipient.resolver.expression;

import com.blazebit.notify.domain.runtime.model.DomainType;
import com.blazebit.notify.expression.ExpressionCompiler;
import com.blazebit.notify.expression.ExpressionInterpreter;
import com.blazebit.notify.expression.ExpressionServiceFactory;
import com.blazebit.notify.expression.Predicate;
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.notification.NotificationJobInstance;
import com.blazebit.notify.notification.NotificationRecipient;
import com.blazebit.notify.notification.NotificationRecipientResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractInterpretingExpressionNotificationRecipientResolver implements NotificationRecipientResolver {

    @Override
    public List<? extends NotificationRecipient<?>> resolveNotificationRecipients(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext<?> jobContext) {
        ExpressionServiceFactory expressionServiceFactory = jobContext.getJobContext().getService(ExpressionServiceFactory.class);
        ExpressionInterpreter interpreter = expressionServiceFactory.createInterpreter();
        ExpressionCompiler compiler = expressionServiceFactory.createCompiler();
        Map<String, DomainType> rootDomainTypes = getRootDomainTypes(jobContext);
        Map<String, Object> rootDomainObjects = getRootDomainObjects(jobContext);
        ExpressionCompiler.Context compilerContext = compiler.createContext(rootDomainTypes);
        ExpressionInterpreter.Context context = interpreter.createContext(rootDomainTypes, rootDomainObjects);
        String recipientPredicateExpression = getRecipientPredicateExpression(jobInstance, jobContext);
        List<? extends NotificationRecipient<?>> allRecipients = getAllRecipients(jobContext);
        if (recipientPredicateExpression != null && !recipientPredicateExpression.isEmpty()) {
            Predicate predicate = compiler.createPredicate(recipientPredicateExpression, compilerContext);
            List<NotificationRecipient<?>> recipientList = new ArrayList<>(allRecipients.size());
            for (NotificationRecipient<?> recipient : allRecipients) {
                if (Boolean.TRUE.equals(interpreter.evaluate(predicate, context))) {
                    recipientList.add(recipient);
                }
            }

            return recipientList;
        }
        return allRecipients;
    }

    protected abstract List<? extends NotificationRecipient<?>> getAllRecipients(JobInstanceProcessingContext jobContext);

    protected abstract Map<String, DomainType> getRootDomainTypes(JobInstanceProcessingContext jobContext);

    protected abstract Map<String, Object> getRootDomainObjects(JobInstanceProcessingContext jobContext);

    protected abstract String getRecipientPredicateExpression(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext jobContext);
}
