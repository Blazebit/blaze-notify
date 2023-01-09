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
package com.blazebit.notify.processor.hibernate.insertselect;

import com.blazebit.expression.ExpressionSerializer;
import com.blazebit.expression.ExpressionServiceFactory;
import com.blazebit.expression.Predicate;
import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.job.processor.hibernate.insertselect.AbstractInsertSelectJobInstanceProcessor;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationJobInstance;
import com.blazebit.notify.NotificationJobInstanceProcessor;
import com.blazebit.notify.NotificationRecipient;
import com.blazebit.notify.NotificationRecipientResolver;
import com.blazebit.notify.recipient.resolver.expression.AbstractPredicatingExpressionNotificationRecipientResolver;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.WhereBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An abstract notification job instance processor implementation that produces target entities via a INSERT-SELECT statement.
 *
 * @param <ID> The job instance cursor type
 * @param <T>  The result type of the processing
 * @param <I>  The job instance type
 * @param <R>  The recipient type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractInsertSelectNotificationJobInstanceProcessor<ID, T, I extends NotificationJobInstance<Long, ID>, R extends NotificationRecipient<?>> extends AbstractInsertSelectJobInstanceProcessor<ID, T, I> implements NotificationJobInstanceProcessor<ID, I> {

    @Override
    protected void bindTargetAttributes(InsertCriteriaBuilder<T> insertCriteriaBuilder, I jobInstance, JobInstanceProcessingContext<ID> context, String jobInstanceAlias) {
        String recipientAlias = "recipient";
        insertCriteriaBuilder.from(getNotificationRecipientEntityClass(jobInstance), recipientAlias);

        String recipientIdPath = recipientAlias + "." + getNotificationRecipientIdPath(jobInstance);
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
                List<? extends NotificationRecipient<?>> notificationRecipients = recipientResolver.resolveNotificationRecipients(jobInstance, context);
                List<Object> ids = new ArrayList<>(notificationRecipients.size());
                for (int i = 0; i < notificationRecipients.size(); i++) {
                    ids.add(notificationRecipients.get(i).getId());
                }
                insertCriteriaBuilder.where(recipientIdPath).in(ids);
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

    /**
     * Returns the target channel type.
     *
     * @return the target channel type
     */
    protected String getTargetChannelType() {
        return null;
    }

    /**
     * Returns the serializer context for applying the notification recipient predicate.
     *
     * @param jobInstance      The notification job instance
     * @param context          The processing context
     * @param recipientAlias   The recipient alias
     * @param jobInstanceAlias The job instance alias
     * @return the serializer context
     */
    protected abstract Map<String, Object> getSerializerContext(I jobInstance, JobInstanceProcessingContext<ID> context, String recipientAlias, String jobInstanceAlias);

    @Override
    protected ID execute(InsertCriteriaBuilder<T> insertCriteriaBuilder, I jobInstance, JobInstanceProcessingContext<ID> context) {
        ReturningResult<ID> returningResult = insertCriteriaBuilder.executeWithReturning(getNotificationIdRecipientIdPath(jobInstance), getNotificationRecipientIdClass(jobInstance));
        if (returningResult.getUpdateCount() == 0 || returningResult.getUpdateCount() != context.getProcessCount()) {
            markDone(jobInstance, context);
        }
        if (returningResult.getResultList().isEmpty()) {
            return null;
        }
        return returningResult.getLastResult();
    }

    @Override
    protected Class<T> getTargetEntityClass(I jobInstance) {
        return getNotificationEntityClass(jobInstance);
    }

    @Override
    protected Class<I> getJobInstanceEntityClass(I jobInstance) {
        return getNotificationJobInstanceEntityClass(jobInstance);
    }

    @Override
    protected String getJobInstanceIdPath(I jobInstance) {
        return getNotificationJobInstanceIdPath(jobInstance);
    }

    /**
     * Binds the notification attributes to the criteria builder.
     *
     * @param insertCriteriaBuilder The insert criteria builder
     * @param jobInstance           The job instance
     * @param context               The processing context
     * @param recipientAlias        The recipient alias
     * @param jobInstanceAlias      The job instance alias
     * @return the next schedule
     */
    protected abstract Instant bindNotificationAttributes(InsertCriteriaBuilder<T> insertCriteriaBuilder, I jobInstance, JobInstanceProcessingContext<ID> context, String recipientAlias, String jobInstanceAlias);

    /**
     * Marks the given notification job instance as done.
     *
     * @param jobInstance The notification job instance
     * @param context     The processing context
     */
    protected abstract void markDone(I jobInstance, JobInstanceProcessingContext<ID> context);

    /**
     * Returns the entity class of the target entities that should be produced by this processor.
     *
     * @param jobInstance The notification job instance
     * @return the entity class
     */
    protected abstract Class<T> getNotificationEntityClass(I jobInstance);

    /**
     * Returns the entity class of the notification job instance.
     *
     * @param jobInstance The notification job instance
     * @return the entity class
     */
    protected abstract Class<I> getNotificationJobInstanceEntityClass(I jobInstance);

    /**
     * Returns the id attribute path for the notification job instance entity type.
     *
     * @param jobInstance The notification job instance
     * @return the id attribute path
     */
    protected abstract String getNotificationJobInstanceIdPath(I jobInstance);

    /**
     * Returns the entity class of the notification recipient.
     *
     * @param jobInstance The notification job instance
     * @return the entity class
     */
    protected abstract Class<R> getNotificationRecipientEntityClass(I jobInstance);

    /**
     * Returns the notification recipient id attribute path for the notification entity type.
     *
     * @param jobInstance The notification job instance
     * @return the id attribute path
     */
    protected abstract String getNotificationIdRecipientIdPath(I jobInstance);

    /**
     * Returns the id attribute path for the notification recipient entity type.
     *
     * @param jobInstance The notification job instance
     * @return the id attribute path
     */
    protected abstract String getNotificationRecipientIdPath(I jobInstance);

    /**
     * Returns the id class of the notification recipient id.
     *
     * @param jobInstance The notification job instance
     * @return the id class
     */
    protected abstract Class<ID> getNotificationRecipientIdClass(I jobInstance);

}
