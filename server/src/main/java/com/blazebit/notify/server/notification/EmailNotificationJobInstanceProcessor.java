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

import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.job.JobInstanceState;
import com.blazebit.job.Schedule;
import com.blazebit.notify.email.model.jpa.EmailNotificationReviewState;
import com.blazebit.notify.email.model.jpa.FromEmail;
import com.blazebit.notify.processor.hibernate.insertselect.AbstractInsertSelectNotificationJobInstanceProcessor;
import com.blazebit.notify.server.model.EmailNotificationJobInstance;
import com.blazebit.notify.server.model.EmailNotificationRecipient;
import com.blazebit.notify.server.model.JobBasedEmailNotification;
import com.blazebit.persistence.InsertCriteriaBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailNotificationJobInstanceProcessor extends AbstractInsertSelectNotificationJobInstanceProcessor<Long, JobBasedEmailNotification, EmailNotificationJobInstance, EmailNotificationRecipient> {

    public static final EmailNotificationJobInstanceProcessor INSTANCE = new EmailNotificationJobInstanceProcessor();

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    protected Instant bindNotificationAttributes(InsertCriteriaBuilder<JobBasedEmailNotification> insertCriteriaBuilder, EmailNotificationJobInstance jobInstance, JobInstanceProcessingContext<Long> context, String recipientAlias, String jobInstanceAlias) {
        insertCriteriaBuilder.bind("state", JobInstanceState.NEW)
                .bind("reviewState", EmailNotificationReviewState.UNNECESSARY)
                .bind("channelType").select("'smtp'")
                .bind("dropable").select("false")
                .bind("maximumDeferCount").select("0")
                .bind("deferCount").select("0")
                .bind("creationTime").select("FUNCTION('TREAT_INSTANT', CURRENT_TIMESTAMP)")
                .bind("fromId").selectSubquery()
                    .from(FromEmail.class, "fromEmail")
                    .select("fromEmail.id")
                    .setMaxResults(1)
                .end()
                .bind("to").select(recipientAlias + ".email")
                .bind("recipientId").select(recipientAlias + "." + getNotificationRecipientIdPath(jobInstance))
                .bind("notificationJobInstanceId").select(jobInstanceAlias + "." + getJobInstanceIdPath(jobInstance))
                .bind("parameterSerializable").select(jobInstanceAlias + ".trigger.jobConfiguration.parameterSerializable");
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
    protected Class<JobBasedEmailNotification> getNotificationEntityClass(EmailNotificationJobInstance jobInstance) {
        return JobBasedEmailNotification.class;
    }

    @Override
    protected Class<EmailNotificationJobInstance> getNotificationJobInstanceEntityClass(EmailNotificationJobInstance jobInstance) {
        return EmailNotificationJobInstance.class;
    }

    @Override
    protected String getNotificationJobInstanceIdPath(EmailNotificationJobInstance jobInstance) {
        return "id";
    }

    @Override
    protected Class<EmailNotificationRecipient> getNotificationRecipientEntityClass(EmailNotificationJobInstance jobInstance) {
        return EmailNotificationRecipient.class;
    }

    @Override
    protected String getNotificationRecipientIdPath(EmailNotificationJobInstance jobInstance) {
        return "id";
    }

    @Override
    protected String getNotificationIdRecipientIdPath(EmailNotificationJobInstance jobInstance) {
        return "recipientId";
    }

    @Override
    protected Class<Long> getNotificationRecipientIdClass(EmailNotificationJobInstance jobInstance) {
        return Long.class;
    }
}
