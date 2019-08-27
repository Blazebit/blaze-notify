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

package com.blazebit.notify.server.notification;

import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.JobInstanceState;
import com.blazebit.notify.job.Schedule;
import com.blazebit.notify.notification.processor.hibernate.insertselect.AbstractInsertSelectNotificationJobInstanceProcessor;
import com.blazebit.notify.server.model.EmailNotification;
import com.blazebit.notify.server.model.EmailNotificationJobInstance;
import com.blazebit.notify.server.model.EmailNotificationRecipient;
import com.blazebit.persistence.InsertCriteriaBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class EmailNotificationJobInstanceProcessor extends AbstractInsertSelectNotificationJobInstanceProcessor<Long, EmailNotification, EmailNotificationJobInstance, EmailNotificationRecipient> {

    public static final EmailNotificationJobInstanceProcessor INSTANCE = new EmailNotificationJobInstanceProcessor();

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    protected Instant bindNotificationAttributes(InsertCriteriaBuilder<EmailNotification> insertCriteriaBuilder, EmailNotificationJobInstance jobInstance, JobInstanceProcessingContext<Long> context, String recipientAlias, String jobInstanceAlias) {
        insertCriteriaBuilder.bind("state", JobInstanceState.NEW)
                .bind("channelType").select("'smtp'")
                .bind("dropable").select("false")
                .bind("maximumDeferCount").select("0")
                .bind("deferCount").select("0")
                .bind("creationTime").select("FUNCTION('TREAT_INSTANT', CURRENT_TIMESTAMP)")
                .bind("recipientId").select(recipientAlias + "." + getNotificationRecipientIdPath())
                .bind("notificationJobInstanceId").select(jobInstanceAlias + "." + getJobInstanceIdPath());
        Schedule notificationSchedule = jobInstance.getTrigger().getNotificationSchedule(context.getJobContext());
        Instant nextSchedule;
        if (notificationSchedule == null) {
            insertCriteriaBuilder.bind("scheduleTime").select("FUNCTION('TREAT_INSTANT', CURRENT_TIMESTAMP)");
            nextSchedule = Instant.now();
        } else {
            nextSchedule = notificationSchedule.nextSchedule();
            insertCriteriaBuilder.bind("scheduleTime", nextSchedule);
        }

        return nextSchedule;
    }

    @Override
    protected String getTargetChannelType() {
        return "smtp";
    }

    @Override
    protected void markDone(EmailNotificationJobInstance jobInstance, JobInstanceProcessingContext<Long> context) {
        jobInstance.setState(JobInstanceState.DONE);
    }

    @Override
    protected Map<String, Object> getSerializerContext(EmailNotificationJobInstance jobInstance, JobInstanceProcessingContext<Long> context, String recipientAlias, String jobInstanceAlias) {
        Map<String, Object> serializerContext = new HashMap<>();
        serializerContext.put("user", recipientAlias);
        return serializerContext;
    }

    @Override
    protected Class<EmailNotification> getNotificationEntityClass() {
        return EmailNotification.class;
    }

    @Override
    protected Class<EmailNotificationJobInstance> getNotificationJobInstanceEntityClass() {
        return EmailNotificationJobInstance.class;
    }

    @Override
    protected String getNotificationJobInstanceIdPath() {
        return "id";
    }

    @Override
    protected Class<EmailNotificationRecipient> getNotificationRecipientEntityClass() {
        return EmailNotificationRecipient.class;
    }

    @Override
    protected String getNotificationRecipientIdPath() {
        return "id";
    }

    @Override
    protected String getNotificationIdRecipientIdPath() {
        return "recipientId";
    }

    @Override
    protected Class<Long> getNotificationRecipientIdClass() {
        return Long.class;
    }
}
