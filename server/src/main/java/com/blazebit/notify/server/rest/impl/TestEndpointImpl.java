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

package com.blazebit.notify.server.rest.impl;

import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.server.model.EmailNotificationJob;
import com.blazebit.notify.server.model.EmailNotificationJobTrigger;
import com.blazebit.notify.server.model.EmailNotificationRecipient;
import com.blazebit.notify.server.rest.api.TestEndpoint;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Locale;

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

        EmailNotificationJob emailJob = new EmailNotificationJob();
        emailJob.setName("test");
        emailJob.setRecipientExpression("user.id = " + recipient.getId());
        jobContext.getJobManager().addJob(emailJob);

        EmailNotificationJobTrigger emailNotificationJobTrigger = new EmailNotificationJobTrigger();
        emailNotificationJobTrigger.setName("test");
        emailNotificationJobTrigger.setJob(emailJob);
        emailNotificationJobTrigger.setScheduleCronExpression(jobContext.getScheduleFactory().asCronExpression(Instant.now()));
        jobContext.getJobManager().addJobTrigger(emailNotificationJobTrigger);

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
