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
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;

import javax.persistence.EntityManager;

public abstract class AbstractInsertSelectNotificationJobProcessor<N extends Notification<T>, T extends NotificationMessage> implements NotificationJobProcessor<N, T> {

    private final CriteriaBuilderFactory cbf;
    private final EntityManager em;

    public AbstractInsertSelectNotificationJobProcessor(CriteriaBuilderFactory cbf, EntityManager em) {
        this.cbf = cbf;
        this.em = em;
    }

    @Override
    public N process(NotificationJob<N, T> notificationJob, NotificationJobContext context) {
        InsertCriteriaBuilder<?> insertCriteriaBuilder = cbf.insert(em, getNotificationEntityClass())
                .from(getNotificationReceiverEntityClass(), "receiver")
                .from(getNotificationJobEntityClass(), "job");

        String receiverIdPath = "receiver." + getNotificationReceiverIdPath();
        insertCriteriaBuilder.where("job." + getNotificationJobIdPath()).eq(getJobId(notificationJob));
        insertCriteriaBuilder.where(receiverIdPath).gt(getReceiverId(context.getLastProcessed().getReceiver()));
        insertCriteriaBuilder.orderByAsc(receiverIdPath);
        insertCriteriaBuilder.setMaxResults(context.getProcessCount());

        if (notificationJob.getSelectorPredicate() != null) {
            // TODO: Apply predicate
//            notificationJob.getSelectorPredicate()
        }

        bindNotificationAttributes(insertCriteriaBuilder, "receiver", "job");

        ReturningResult<Long> returningResult = insertCriteriaBuilder.executeWithReturning(receiverIdPath, Long.class);
        return createNotificationReference(returningResult.getLastResult());
    }

    protected abstract void bindNotificationAttributes(InsertCriteriaBuilder<?> insertCriteriaBuilder, String receiverAlias, String jobAlias);

    protected abstract Class<?> getNotificationEntityClass();

    protected abstract Class<?> getNotificationJobEntityClass();

    protected abstract String getNotificationJobIdPath();

    protected abstract Class<?> getNotificationReceiverEntityClass();

    protected abstract String getNotificationReceiverIdPath();

    protected abstract Long getJobId(NotificationJob<N, T> job);

    protected abstract Long getReceiverId(NotificationReceiver receiver);

    protected abstract N createNotificationReference(Long receiverId);

}
