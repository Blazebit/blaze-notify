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

import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.TimeFrame;
import com.blazebit.notify.job.jpa.model.AbstractJobInstance;
import com.blazebit.notify.job.jpa.model.JobConfiguration;
import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.NotificationJobInstance;
import com.blazebit.notify.notification.NotificationRecipient;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@MappedSuperclass
@Table(name = "notification")
public abstract class AbstractNotification<ID extends AbstractNotificationId<?, ?>, R extends NotificationRecipient<?>, I extends NotificationJobInstance<Long, Long>> extends AbstractJobInstance<ID> implements Notification<ID>, com.blazebit.notify.job.JobConfiguration {

	private static final long serialVersionUID = 1L;

	private String channelType;
	private R recipient;
	private I notificationJobInstance;
	private JobConfiguration jobConfiguration = new JobConfiguration();

	public AbstractNotification(ID id) {
		super(id);
	}

	@Override
	@Transient
	public Long getPartitionKey() {
		return (Long) getRecipient().getId();
	}

	@Override
	@Column(nullable = false)
	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	@Override
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipient_id", insertable = false, updatable = false, nullable = false)
	public R getRecipient() {
		return recipient;
	}

	public void setRecipient(R recipient) {
		this.recipient = recipient;
		if (recipient == null) {
			id().setRecipientId(null);
		} else {
			((AbstractNotificationId) id()).setRecipientId(recipient.getId());
		}
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "notification_job_instance_id", insertable = false, updatable = false, nullable = false)
	public I getNotificationJobInstance() {
		return notificationJobInstance;
	}

	public void setNotificationJobInstance(I notificationJobInstance) {
		this.notificationJobInstance = notificationJobInstance;
		if (notificationJobInstance == null) {
			id().setNotificationJobInstanceId(null);
		} else {
			((AbstractNotificationId) id()).setNotificationJobInstanceId(notificationJobInstance.getId());
		}
	}

	/*
	 * We need to implement the JobConfiguration interface here to be able to make use of the insert-select strategy because Hibernate can't bind properties of embeddables.
	 */

	@Override
	@Column(nullable = false)
	public boolean isDropable() {
		return jobConfiguration.isDropable();
	}

	public void setDropable(boolean dropable) {
		jobConfiguration.setDropable(dropable);
	}

	@Override
	@Column(nullable = false)
	public int getMaximumDeferCount() {
		return jobConfiguration.getMaximumDeferCount();
	}

	public void setMaximumDeferCount(int maximumDeferCount) {
		jobConfiguration.setMaximumDeferCount(maximumDeferCount);
	}

	@Override
	public Instant getDeadline() {
		return jobConfiguration.getDeadline();
	}

	public void setDeadline(Instant deadline) {
		jobConfiguration.setDeadline(deadline);
	}

	@ElementCollection
	@CollectionTable(name = "notification_publish_time_frames", foreignKey = @ForeignKey(name = "notification_publish_time_frames_fk_notification"))
	// TODO: Use string encoding to avoid joins
	public Set<com.blazebit.notify.job.jpa.model.TimeFrame> getExecutionTimeFrames() {
		return jobConfiguration.getExecutionTimeFrames();
	}

	public void setExecutionTimeFrames(Set<com.blazebit.notify.job.jpa.model.TimeFrame> executionTimeFrames) {
		jobConfiguration.setExecutionTimeFrames(executionTimeFrames);
	}

	@Override
	@ElementCollection
	@Column(name = "value")
	@MapKeyColumn(name = "name")
	@CollectionTable(name = "notification_parameter", foreignKey = @ForeignKey(name = "notification_parameter_fk_notification"))
	// TODO: Use string encoding to avoid joins
	public Map<String, Serializable> getParameters() {
		return jobConfiguration.getParameters();
	}

	public void setParameters(Map<String, Serializable> jobParameters) {
		jobConfiguration.setParameters(jobParameters);
	}

	@Override
	@Transient
	public com.blazebit.notify.job.JobConfiguration getJobConfiguration() {
		return this;
	}

	@Override
	@Transient
	public Set<? extends TimeFrame> getPublishTimeFrames() {
		return getJobConfiguration().getExecutionTimeFrames();
	}

	@Override
	public void onChunkSuccess(JobInstanceProcessingContext<?> processingContext) {
	}
}
