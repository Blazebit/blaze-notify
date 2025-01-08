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

package com.blazebit.notify.testsuite;

import com.blazebit.job.JobContext;
import com.blazebit.job.Schedule;
import com.blazebit.notify.Channel;
import com.blazebit.notify.NotificationRecipientResolver;
import com.blazebit.notify.memory.model.AbstractNotificationJobTrigger;

import java.io.Serializable;
import java.util.Map;

public class SimpleNotificationJobTrigger extends AbstractNotificationJobTrigger<SimpleNotificationJob> {

    private final Schedule schedule;
    private final Schedule notificationSchedule;

    public SimpleNotificationJobTrigger(Channel<SimpleNotificationRecipient, SimpleNotificationMessage> channel, NotificationRecipientResolver recipientResolver, Schedule schedule, Schedule notificationSchedule, Map<String, Serializable> jobParameters) {
        setJob(new SimpleNotificationJob(channel, recipientResolver));
        this.schedule = schedule;
        this.notificationSchedule = notificationSchedule;
        getJobConfiguration().setParameters(jobParameters);
    }

    @Override
    public Schedule getSchedule(JobContext jobContext) {
        return schedule;
    }

    @Override
    public Schedule getNotificationSchedule(JobContext jobContext) {
        return notificationSchedule;
    }

}
