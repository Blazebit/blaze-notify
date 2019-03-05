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
package com.blazebit.notify.notification.processor.hibernate.insertselect;

import com.blazebit.notify.notification.*;
import com.blazebit.notify.notification.recipient.resolver.expression.ExpressionNotificationRecipientResolver;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;

import javax.persistence.EntityManager;

public abstract class AbstractInsertSelectNotificationJobProcessor<R extends NotificationRecipient, N extends Notification<R, N, T>, T extends NotificationMessage> implements NotificationJobProcessor<R, N, T> {

    private final CriteriaBuilderFactory cbf;
    private final EntityManager em;

    public AbstractInsertSelectNotificationJobProcessor(CriteriaBuilderFactory cbf, EntityManager em) {
        this.cbf = cbf;
        this.em = em;
    }

    @Override
    public N process(NotificationJob<R, N, T> notificationJob, NotificationJobProcessingContext context) {
        InsertCriteriaBuilder<?> insertCriteriaBuilder = cbf.insert(em, getNotificationEntityClass())
                .from(getNotificationRecipientEntityClass(), "recipient")
                .from(getNotificationJobEntityClass(), "job");

        String recipientIdPath = "recipient." + getNotificationRecipientIdPath();
        insertCriteriaBuilder.where("job." + getNotificationJobIdPath()).eq(getJobId(notificationJob));
        insertCriteriaBuilder.where(recipientIdPath).gt(getRecipientId(context.getLastProcessed().getRecipient()));
        insertCriteriaBuilder.orderByAsc(recipientIdPath);
        insertCriteriaBuilder.setMaxResults(context.getProcessCount());

        if (notificationJob.getRecipientResolver() != null) {
            if (notificationJob.getRecipientResolver() instanceof ExpressionNotificationRecipientResolver) {
                // TODO: Apply predicate
//            ((ExpressionNotificationRecipientResolver<?, ?>) notificationJob.getRecipientResolver()).getPredicate()
            } else {
                throw new IllegalArgumentException("Expected recipient resolver of type " + ExpressionNotificationRecipientResolver.class.getName() + " but got " + notificationJob.getRecipientResolver().getClass().getName());
            }
        }

        bindNotificationAttributes(insertCriteriaBuilder, "recipient", "job");

        ReturningResult<Long> returningResult = insertCriteriaBuilder.executeWithReturning(recipientIdPath, Long.class);
        return createNotificationReference(returningResult.getLastResult());
    }

    protected abstract void bindNotificationAttributes(InsertCriteriaBuilder<?> insertCriteriaBuilder, String recipientAlias, String jobAlias);

    protected abstract Class<?> getNotificationEntityClass();

    protected abstract Class<?> getNotificationJobEntityClass();

    protected abstract String getNotificationJobIdPath();

    protected abstract Class<?> getNotificationRecipientEntityClass();

    protected abstract String getNotificationRecipientIdPath();

    protected abstract Long getJobId(NotificationJob<R, N, T> job);

    protected abstract Long getRecipientId(NotificationRecipient recipient);

    protected abstract N createNotificationReference(Long recipientId);

}
