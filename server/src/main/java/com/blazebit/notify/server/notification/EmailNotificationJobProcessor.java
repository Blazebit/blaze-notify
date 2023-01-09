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

package com.blazebit.notify.server.notification;

import com.blazebit.job.JobInstanceState;
import com.blazebit.notify.NotificationJobContext;
import com.blazebit.notify.NotificationJobProcessor;
import com.blazebit.notify.server.model.EmailNotificationJobInstance;
import com.blazebit.notify.server.model.EmailNotificationJobTrigger;

import java.time.Instant;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailNotificationJobProcessor implements NotificationJobProcessor<EmailNotificationJobTrigger> {

    public static final EmailNotificationJobProcessor INSTANCE = new EmailNotificationJobProcessor();

    private EmailNotificationJobProcessor() {
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public void process(EmailNotificationJobTrigger jobTrigger, NotificationJobContext context) {
        EmailNotificationJobInstance jobInstance = new EmailNotificationJobInstance();
        jobInstance.setState(JobInstanceState.NEW);
        jobInstance.setTrigger(jobTrigger);
        jobInstance.setScheduleTime(Instant.now());
        context.getJobManager().addJobInstance(jobInstance);
    }
}
