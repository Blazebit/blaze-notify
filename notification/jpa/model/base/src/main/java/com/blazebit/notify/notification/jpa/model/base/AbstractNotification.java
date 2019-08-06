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

import com.blazebit.notify.job.jpa.model.EmbeddedIdEntity;
import com.blazebit.notify.job.jpa.model.TimeFrame;
import com.blazebit.notify.notification.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
@Table(name = "notification")
public abstract class AbstractNotification<ID extends AbstractNotificationId<?, ?>, R extends NotificationRecipient<?>, I extends NotificationJobInstance<Long>> extends EmbeddedIdEntity<ID> implements Notification<ID> {

	private static final long serialVersionUID = 1L;

	private R recipient;
	private I notificationJobInstance;
	private NotificationState state;
	private int deferCount;
	private Instant scheduleTime;
	private Instant creationTime;
	private Set<TimeFrame> publishTimeFrames = new HashSet<>(0);

	public AbstractNotification() {
	}

	public AbstractNotification(ID id) {
		super(id);
	}

	@EmbeddedId
	@Override
	public ID getId() {
		return id();
	}

	@Override
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public R getRecipient() {
		return recipient;
	}

	public void setRecipient(R recipient) {
		this.recipient = recipient;
	}

	@Override
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public I getNotificationJobInstance() {
		return notificationJobInstance;
	}

	public void setNotificationJobInstance(I notificationJobInstance) {
		this.notificationJobInstance = notificationJobInstance;
	}

	@Override
	public void markDone(Object result) {
		setState(NotificationState.DONE);
	}

	@Override
	public void markFailed(Throwable t) {
		setState(NotificationState.FAILED);
	}

	@Override
	public void incrementDeferCount() {
		setDeferCount(getDeferCount() + 1);
	}

	@Override
	public void markDeadlineReached() {
		setState(NotificationState.DEADLINE_REACHED);
	}

	@Override
	public void markDropped() {
		setState(NotificationState.DROPPED);
	}

	@Override
	@Column(nullable = false)
	public NotificationState getState() {
		return state;
	}

	public void setState(NotificationState state) {
		this.state = state;
	}

	@Override
	@Column(nullable = false)
	public int getDeferCount() {
		return deferCount;
	}

	public void setDeferCount(int deferCount) {
		this.deferCount = deferCount;
	}

	@Override
	@Column(nullable = false)
	public Instant getScheduleTime() {
		return scheduleTime;
	}

	@Override
	public void setScheduleTime(Instant scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	@Override
	@Column(nullable = false)
	public Instant getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Instant creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	@ElementCollection
	public Set<TimeFrame> getPublishTimeFrames() {
		return publishTimeFrames;
	}

	public void setPublishTimeFrames(Set<TimeFrame> publishTimeFrames) {
		this.publishTimeFrames = publishTimeFrames;
	}
}
