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

import com.blazebit.job.JobContext;
import com.blazebit.notify.email.model.jpa.AbstractEmailNotification;
import com.blazebit.notify.server.model.EmailNotification;
import com.blazebit.notify.server.model.EmailNotificationJob;
import com.blazebit.notify.server.model.EmailNotificationJobTrigger;
import com.blazebit.notify.server.model.EmailNotificationRecipient;
import com.blazebit.notify.server.model.FromEmail;
import com.blazebit.notify.server.rest.api.TestEndpoint;
import java.time.Instant;
import java.util.Locale;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.Response;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@Stateless
public class TestEndpointImpl implements TestEndpoint {

    @Inject
    JobContext jobContext;

    @Inject
    EntityManager entityManager;

    @Override
    public Response test() {
        EmailNotificationRecipient recipient = new EmailNotificationRecipient();
        recipient.setLocale(Locale.GERMANY);
        recipient.setEmail("test@localhost");
        entityManager.persist(recipient);
        boolean direct = true;

        if (direct) {
            EmailNotification emailNotification = new EmailNotification();
            emailNotification.setTo(recipient.getEmail());
            emailNotification.setChannelType("smtp");
            emailNotification.setFrom(entityManager.createQuery("SELECT e FROM FromEmail e", FromEmail.class).setMaxResults(1).getSingleResult());
            emailNotification.setSubject("Hello");
            emailNotification.setBodyText("Hey my friend!");
            emailNotification.setScheduleTime(Instant.now());
            jobContext.getJobManager().addJobInstance(emailNotification);
        } else {
            EmailNotificationJob emailJob = new EmailNotificationJob();
            emailJob.setName("test");
            emailJob.setRecipientExpression("user.id = " + recipient.getId());

            EmailNotificationJobTrigger emailNotificationJobTrigger = new EmailNotificationJobTrigger();
            emailNotificationJobTrigger.setName("test");
            emailNotificationJobTrigger.setJob(emailJob);
            emailNotificationJobTrigger.setNotificationCronExpression("0 0 22 * * ?");
            emailNotificationJobTrigger.getJobConfiguration().getParameters().put(AbstractEmailNotification.TEMPLATE_PROCESSOR_TYPE_PARAMETER, "freemarker");
            emailNotificationJobTrigger.getJobConfiguration().getParameters().put(AbstractEmailNotification.SUBJECT_TEMPLATE_PARAMETER, "subject.ftl");
            emailNotificationJobTrigger.getJobConfiguration().getParameters().put(AbstractEmailNotification.BODY_TEXT_TEMPLATE_PARAMETER, "text.ftl");
//        emailNotificationJobTrigger.setScheduleCronExpression("0 * * * * ?");
            emailNotificationJobTrigger.setScheduleCronExpression(jobContext.getScheduleFactory().asCronExpression(Instant.now()));
            jobContext.getJobManager().addJobInstance(emailNotificationJobTrigger);
        }

        /*

        1. Trigger is persisted and is scheduled in JobScheduler
        2. JobScheduler runs JobProcessor for the Job that generates JobInstance. For shortcut, set JobInstance#done to true
        3. JobInstanceScheduler runs JobInstanceProcessor for the JobInstance incrementally until done

        JobProcessor and JobInstanceProcessor are free to do whatever they want, but usually NotificationJobInstanceProcessor does

        1. For every recipient schedule a Notification
        2. Notify the NotificationScheduler about new work

        NotificationScheduler

        1. Resolve the Channel for the Notification
        2. Resolve NotificationMessage of the Notification for the Channel
        3. Send NotificationMessage through Channel

        Job is something that can be triggered multiple times via different JobTriggers.
        A JobTrigger defines a possibly recurring trigger schedule for a job.
        A JobInstance is the result of a Job being triggered by a JobTrigger trigger schedule(There may be multiple JobInstance objects for a single JobTrigger when it is recurring)
        A NotificationJobInstance is incrementally processed for its NotificationRecipient cursor
        A Notification is the result of a NotificationJobInstance being processed for a NotificationRecipient
        The NotificationScheduler processes Notifications partition-wise, but one-by-one and send Notifications through a Channel

         */

        return Response.ok().build();
    }
}
