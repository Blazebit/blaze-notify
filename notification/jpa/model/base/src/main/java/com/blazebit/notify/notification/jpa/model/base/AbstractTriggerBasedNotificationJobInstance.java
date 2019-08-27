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
import com.blazebit.notify.job.jpa.model.AbstractTriggerBasedJobInstance;
import com.blazebit.notify.notification.NotificationJobInstance;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class AbstractTriggerBasedNotificationJobInstance<ID, R, J extends AbstractNotificationJob, T extends AbstractNotificationJobTrigger<J>> extends AbstractTriggerBasedJobInstance<ID, T> implements NotificationJobInstance<ID, R> {

	private static final long serialVersionUID = 1L;

	private R recipientCursor;

	public AbstractTriggerBasedNotificationJobInstance() {
	}

	public AbstractTriggerBasedNotificationJobInstance(ID id) {
		super(id);
	}

	@Override
	@Transient
	public Long getPartitionKey() {
		return (Long) getId();
	}

	@Override
	public void onChunkSuccess(JobInstanceProcessingContext<?> processingContext) {
		setRecipientCursor((R) processingContext.getLastProcessed());
	}

	@Override
	public R getRecipientCursor() {
		return recipientCursor;
	}

	public void setRecipientCursor(R recipientCursor) {
		this.recipientCursor = recipientCursor;
	}
}
