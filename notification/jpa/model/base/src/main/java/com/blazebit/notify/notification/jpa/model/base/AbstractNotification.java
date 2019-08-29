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
public abstract class AbstractNotification<ID> extends AbstractJobInstance<ID> implements Notification<ID>, com.blazebit.notify.job.JobConfiguration {

	private static final long serialVersionUID = 1L;

	private String channelType;
	private NotificationJobConfiguration jobConfiguration = new NotificationJobConfiguration();

	public AbstractNotification() {
	}

	public AbstractNotification(ID id) {
		super(id);
	}

	@Override
	@Column(nullable = false)
	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
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

	@Override
	@Transient
	public Set<com.blazebit.notify.job.jpa.model.TimeFrame> getExecutionTimeFrames() {
		return jobConfiguration.getExecutionTimeFrames();
	}

	public void setExecutionTimeFrames(Set<com.blazebit.notify.job.jpa.model.TimeFrame> executionTimeFrames) {
		jobConfiguration.setExecutionTimeFrames(executionTimeFrames);
	}

	@Override
	@Transient
	public Map<String, Serializable> getParameters() {
		return jobConfiguration.getParameters();
	}

	public void setParameters(Map<String, Serializable> jobParameters) {
		jobConfiguration.setParameters(jobParameters);
	}

	@Lob
	@Column(name = "parameters")
	protected Serializable getParameterSerializable() {
		return jobConfiguration.getParameterSerializable();
	}

	protected void setParameterSerializable(Serializable parameterSerializable) {
		jobConfiguration.setParameterSerializable(parameterSerializable);
	}

	@Override
	@Transient
	public com.blazebit.notify.job.JobConfiguration getJobConfiguration() {
		return this;
	}

	@Override
	public void onChunkSuccess(JobInstanceProcessingContext<?> processingContext) {
	}

	private static class NotificationJobConfiguration extends JobConfiguration {
		@Override
		public Serializable getParameterSerializable() {
			return super.getParameterSerializable();
		}

		@Override
		public void setParameterSerializable(Serializable parameterSerializable) {
			super.setParameterSerializable(parameterSerializable);
		}
	}
}
