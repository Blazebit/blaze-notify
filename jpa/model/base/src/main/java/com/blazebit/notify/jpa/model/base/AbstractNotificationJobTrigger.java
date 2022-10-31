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

package com.blazebit.notify.jpa.model.base;

import com.blazebit.job.JobContext;
import com.blazebit.job.Schedule;
import com.blazebit.job.jpa.model.AbstractJobTrigger;
import com.blazebit.notify.NotificationJobTrigger;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.MappedSuperclass;

/**
 * An abstract mapped superclass implementing the {@link NotificationJobTrigger} interface.
 *
 * @param <J> The notification job type
 * @author Christian Beikov
 * @since 1.0.0
 */
@Access(AccessType.PROPERTY)
@MappedSuperclass
public abstract class AbstractNotificationJobTrigger<J extends AbstractNotificationJob> extends AbstractJobTrigger<J> implements NotificationJobTrigger {

    private static final long serialVersionUID = 1L;

    private String notificationCronExpression;

    /**
     * Creates an empty notification job trigger.
     */
    public AbstractNotificationJobTrigger() {
    }

    /**
     * Creates a notification job trigger based with the given id.
     *
     * @param id The notification job trigger id
     */
    public AbstractNotificationJobTrigger(Long id) {
        super(id);
    }

    @Override
    public Schedule getNotificationSchedule(JobContext jobContext) {
        return getNotificationCronExpression() == null ? null : jobContext.getScheduleFactory().createSchedule(getNotificationCronExpression());
    }

    /**
     * Returns the cron expression when the notification should be triggered or <code>null</code> if it should be triggered immediately.
     *
     * @return the cron expression when the notification should be triggered
     */
    public String getNotificationCronExpression() {
        return notificationCronExpression;
    }

    /**
     * Sets the given notification cron expression.
     *
     * @param notificationCronExpression The notification cron expression
     */
    public void setNotificationCronExpression(String notificationCronExpression) {
        this.notificationCronExpression = notificationCronExpression;
    }
}
