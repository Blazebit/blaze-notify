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

package com.blazebit.notify.notification.jpa.model.base;

import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.job.Schedule;
import com.blazebit.notify.job.jpa.model.AbstractJobTrigger;
import com.blazebit.notify.notification.NotificationJobTrigger;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractNotificationJobTrigger<J extends AbstractNotificationJob> extends AbstractJobTrigger<J> implements NotificationJobTrigger {

	private static final long serialVersionUID = 1L;

	private String notificationCronExpression;

	public AbstractNotificationJobTrigger() {
	}

	public AbstractNotificationJobTrigger(Long id) {
		super(id);
	}

	@Override
	public Schedule getNotificationSchedule(JobContext jobContext) {
		return getNotificationCronExpression() == null ? null : jobContext.getScheduleFactory().createSchedule(getNotificationCronExpression());
	}

	public String getNotificationCronExpression() {
		return notificationCronExpression;
	}

	public void setNotificationCronExpression(String notificationCronExpression) {
		this.notificationCronExpression = notificationCronExpression;
	}
}
