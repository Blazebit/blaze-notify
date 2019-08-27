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

import com.blazebit.notify.expression.ExpressionSerializer;
import com.blazebit.notify.expression.ExpressionServiceFactory;
import com.blazebit.notify.expression.Predicate;
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.notification.*;
import com.blazebit.notify.notification.recipient.resolver.expression.AbstractPredicatingExpressionNotificationRecipientResolver;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.WhereBuilder;

import java.time.Instant;
import java.util.Map;

/**
 *
 * @param <ID> JobInstance id type
 * @param <T> The target type that is created by this processor
 * @param <I> The job instance type
 * @param <R> The recipient type
 */
public abstract class AbstractInsertSelectNotificationJobInstanceProcessor<ID, T, I extends NotificationJobInstance<Long, ?>, R extends NotificationRecipient<?>> extends AbstractInsertSelectJobInstanceProcessor<ID, T, I> implements NotificationJobInstanceProcessor<ID, I> {

    @Override
    protected void bindTargetAttributes(InsertCriteriaBuilder<T> insertCriteriaBuilder, I jobInstance, JobInstanceProcessingContext<ID> context, String jobInstanceAlias) {
        String recipientAlias = "recipient";
        insertCriteriaBuilder.from(getNotificationRecipientEntityClass(), recipientAlias);

        String recipientIdPath = recipientAlias + "." + getNotificationRecipientIdPath();
        if (context.getLastProcessed() != null) {
            insertCriteriaBuilder.where(recipientIdPath).gt(context.getLastProcessed());
        }
        insertCriteriaBuilder.orderByAsc(recipientIdPath);
        NotificationRecipientResolver recipientResolver = context.getJobContext().getService(NotificationRecipientResolver.class);

        if (recipientResolver != null) {
            if (recipientResolver instanceof AbstractPredicatingExpressionNotificationRecipientResolver) {
                Predicate predicate = ((AbstractPredicatingExpressionNotificationRecipientResolver) recipientResolver).resolveNotificationRecipientPredicate(jobInstance, context);
                if (predicate != null) {
                    ExpressionServiceFactory expressionServiceFactory = context.getJobContext().getService(ExpressionServiceFactory.class);
                    ExpressionSerializer<WhereBuilder> serializer = expressionServiceFactory.createSerializer(WhereBuilder.class);
                    ExpressionSerializer.Context serializerContext = serializer.createContext(getSerializerContext(jobInstance, context, recipientAlias, jobInstanceAlias));
                    serializer.serializeTo(serializerContext, predicate, insertCriteriaBuilder);
                }
            } else {
                throw new IllegalArgumentException("Expected recipient resolver of type " + AbstractPredicatingExpressionNotificationRecipientResolver.class.getName() + " but got " + recipientResolver.getClass().getName());
            }
        }

        Instant earliestNewNotificationSchedule = bindNotificationAttributes(insertCriteriaBuilder, jobInstance, context, recipientAlias, jobInstanceAlias);
        String channelType = getTargetChannelType();
        NotificationJobContext notificationJobContext = (NotificationJobContext) context.getJobContext();
        notificationJobContext.getTransactionSupport().registerPostCommitListener(() -> {
            if (channelType == null) {
                notificationJobContext.triggerNotificationScan(earliestNewNotificationSchedule.toEpochMilli());
            } else {
                notificationJobContext.triggerNotificationScan(channelType, earliestNewNotificationSchedule.toEpochMilli());
            }
        });
    }

    protected String getTargetChannelType() {
        return null;
    }

    protected abstract Map<String, Object> getSerializerContext(I jobInstance, JobInstanceProcessingContext<ID> context, String recipientAlias, String jobInstanceAlias);

    @Override
    protected ID execute(InsertCriteriaBuilder<T> insertCriteriaBuilder, I jobInstance, JobInstanceProcessingContext<ID> context) {
        ReturningResult<ID> returningResult = insertCriteriaBuilder.executeWithReturning(getNotificationIdRecipientIdPath(), getNotificationRecipientIdClass());
        if (returningResult.getUpdateCount() == 0 || returningResult.getUpdateCount() != context.getProcessCount()) {
            markDone(jobInstance, context);
        }
        return returningResult.getLastResult();
    }

    @Override
    protected Class<T> getTargetEntityClass() {
        return getNotificationEntityClass();
    }

    @Override
    protected Class<I> getJobInstanceEntityClass() {
        return getNotificationJobInstanceEntityClass();
    }

    @Override
    protected String getJobInstanceIdPath() {
        return getNotificationJobInstanceIdPath();
    }

    protected abstract Instant bindNotificationAttributes(InsertCriteriaBuilder<T> insertCriteriaBuilder, I jobInstance, JobInstanceProcessingContext<ID> context, String recipientAlias, String jobInstanceAlias);

    protected abstract void markDone(I jobInstance, JobInstanceProcessingContext<ID> context);

    protected abstract Class<T> getNotificationEntityClass();

    protected abstract Class<I> getNotificationJobInstanceEntityClass();

    protected abstract String getNotificationJobInstanceIdPath();

    protected abstract Class<R> getNotificationRecipientEntityClass();

    protected abstract String getNotificationIdRecipientIdPath();

    protected abstract String getNotificationRecipientIdPath();

    protected abstract Class<ID> getNotificationRecipientIdClass();

}
