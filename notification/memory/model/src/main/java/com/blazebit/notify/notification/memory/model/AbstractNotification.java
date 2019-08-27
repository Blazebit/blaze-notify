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

package com.blazebit.notify.notification.memory.model;

import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.TimeFrame;
import com.blazebit.notify.job.memory.model.AbstractJobInstance;
import com.blazebit.notify.job.memory.model.JobConfiguration;
import com.blazebit.notify.notification.*;

import java.util.Set;

public abstract class AbstractNotification<ID extends AbstractNotificationId<?, ?>, R extends NotificationRecipient<?>, I extends NotificationJobInstance<Long, Long>> extends AbstractJobInstance<ID> implements Notification<ID> {

	private static final long serialVersionUID = 1L;

	private String channelType;
	private R recipient;
	private I notificationJobInstance;
	private JobConfiguration jobConfiguration = new JobConfiguration();

	public AbstractNotification(ID id) {
		super(id);
	}

	@Override
	public Long getPartitionKey() {
		return (Long) getRecipient().getId();
	}

	@Override
	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	@Override
	public R getRecipient() {
		return recipient;
	}

	public void setRecipient(R recipient) {
		this.recipient = recipient;
		if (recipient == null) {
			getId().setRecipientId(null);
		} else {
			((AbstractNotificationId) getId()).setRecipientId(recipient.getId());
		}
	}

	public I getNotificationJobInstance() {
		return notificationJobInstance;
	}

	public void setNotificationJobInstance(I notificationJobInstance) {
		this.notificationJobInstance = notificationJobInstance;
		if (notificationJobInstance == null) {
			getId().setNotificationJobInstanceId(null);
		} else {
			((AbstractNotificationId) getId()).setNotificationJobInstanceId(notificationJobInstance.getId());
		}
	}
	public JobConfiguration getJobConfiguration() {
		return jobConfiguration;
	}

	public void setJobConfiguration(JobConfiguration jobConfiguration) {
		this.jobConfiguration = jobConfiguration;
	}

	@Override
	public Set<? extends TimeFrame> getPublishTimeFrames() {
		return getJobConfiguration().getExecutionTimeFrames();
	}

	@Override
	public void onChunkSuccess(JobInstanceProcessingContext<?> processingContext) {
	}
}
