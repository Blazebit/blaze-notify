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
import com.blazebit.notify.notification.NotificationJobInstance;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractNotificationJobInstance<R, J extends AbstractNotificationJob, T extends AbstractNotificationJobTrigger<J>> extends AbstractJobInstance<T> implements NotificationJobInstance<R> {

	private static final long serialVersionUID = 1L;

	private R recipientCursor;

	public AbstractNotificationJobInstance() {
	}

	public AbstractNotificationJobInstance(Long id) {
		super(id);
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
