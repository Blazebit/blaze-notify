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
package com.blazebit.notify;

import com.blazebit.job.JobContext;
import com.blazebit.job.JobTrigger;
import com.blazebit.job.Schedule;

/**
 * A trigger for a {@link NotificationJob} that allows recurring schedules.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationJobTrigger extends JobTrigger {

    @Override
    NotificationJob getJob();

    /**
     * Returns the schedule that should be used for a notification.
     *
     * @param jobContext The job context
     * @return the schedule or <code>null</code> if it should be scheduled immediately
     */
    Schedule getNotificationSchedule(JobContext jobContext);

}
