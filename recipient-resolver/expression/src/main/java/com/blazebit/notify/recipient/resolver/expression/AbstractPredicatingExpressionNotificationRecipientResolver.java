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
package com.blazebit.notify.recipient.resolver.expression;

import com.blazebit.domain.runtime.model.DomainType;
import com.blazebit.expression.ExpressionCompiler;
import com.blazebit.expression.ExpressionServiceFactory;
import com.blazebit.expression.Predicate;
import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.notify.NotificationJobInstance;
import com.blazebit.notify.NotificationRecipient;
import com.blazebit.notify.NotificationRecipientResolver;

import java.util.List;
import java.util.Map;

/**
 * A recipient resolver that provides a Blaze-Expression predicate that can be serialized to a query.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractPredicatingExpressionNotificationRecipientResolver implements NotificationRecipientResolver {

    @Override
    public List<? extends NotificationRecipient<?>> resolveNotificationRecipients(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext<?> jobContext) {
        throw new UnsupportedOperationException("Please resolve a predicate with the method " + AbstractPredicatingExpressionNotificationRecipientResolver.class.getName() + "#resolveNotificationRecipientPredicate");
    }

    /**
     * Returns the recipient predicate for the job instance.
     *
     * @param jobInstance The notification job instance
     * @param jobContext  The processing context
     * @return the recipient predicate
     */
    public Predicate resolveNotificationRecipientPredicate(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext<?> jobContext) {
        ExpressionServiceFactory expressionServiceFactory = jobContext.getJobContext().getService(ExpressionServiceFactory.class);
        ExpressionCompiler compiler = expressionServiceFactory.createCompiler();
        Map<String, DomainType> rootDomainTypes = getRootDomainTypes(jobContext);
        ExpressionCompiler.Context context = compiler.createContext(rootDomainTypes);
        String recipientPredicateExpression = getRecipientPredicateExpression(jobInstance, jobContext);
        if (recipientPredicateExpression != null && !recipientPredicateExpression.isEmpty()) {
            return compiler.createPredicate(recipientPredicateExpression, context);
        }
        return null;
    }

    /**
     * Returns the root domain types.
     *
     * @param jobContext The processing context
     * @return the root domain types
     */
    protected abstract Map<String, DomainType> getRootDomainTypes(JobInstanceProcessingContext<?> jobContext);

    /**
     * Returns the recipient predicate expression.
     *
     * @param jobInstance The notification job instance
     * @param jobContext  The processing context
     * @return the recipient predicate expression
     */
    protected abstract String getRecipientPredicateExpression(NotificationJobInstance<Long, ?> jobInstance, JobInstanceProcessingContext<?> jobContext);
}
