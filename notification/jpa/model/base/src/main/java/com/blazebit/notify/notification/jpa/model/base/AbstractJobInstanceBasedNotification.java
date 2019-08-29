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

import com.blazebit.notify.notification.NotificationJobInstance;
import com.blazebit.notify.notification.NotificationRecipient;

import javax.persistence.*;

@MappedSuperclass
@Table(name = "notification")
public abstract class AbstractJobInstanceBasedNotification<ID extends AbstractNotificationId<?, ?>, R extends NotificationRecipient<?>, I extends NotificationJobInstance<Long, Long>> extends AbstractNotification<ID> {

	private static final long serialVersionUID = 1L;

	private R recipient;
	private I notificationJobInstance;

	public AbstractJobInstanceBasedNotification(ID id) {
		super(id);
	}

	@Override
	@Transient
	public Long getPartitionKey() {
		return (Long) getRecipient().getId();
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
}
