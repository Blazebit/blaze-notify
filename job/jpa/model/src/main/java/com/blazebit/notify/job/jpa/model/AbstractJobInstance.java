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

package com.blazebit.notify.job.jpa.model;

import com.blazebit.notify.job.JobInstanceState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@MappedSuperclass
@Table(name = "job_instance")
public abstract class AbstractJobInstance<ID> extends BaseEntity<ID> implements JpaJobInstance<ID> {

	private static final long serialVersionUID = 1L;

	private JobInstanceState state = JobInstanceState.NEW;

	private int deferCount;
	private Instant scheduleTime;
	private Instant creationTime;
	private Instant lastExecutionTime;
	
	protected AbstractJobInstance() {
	}

	protected AbstractJobInstance(ID id) {
		super(id);
	}

	@Override
	public void incrementDeferCount() {
		setDeferCount(getDeferCount() + 1);
	}

	@Override
	public void markDeadlineReached() {
		setState(JobInstanceState.DEADLINE_REACHED);
	}

	@Override
	public void markDropped() {
		setState(JobInstanceState.DROPPED);
	}

	@Override
	public void markDone(Object result) {
		setState(JobInstanceState.DONE);
	}

	@Override
	public void markFailed(Throwable t) {
		setState(JobInstanceState.FAILED);
	}

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	public JobInstanceState getState() {
		return state;
	}

	@Override
	public void setState(JobInstanceState state) {
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
	public Instant getLastExecutionTime() {
		return lastExecutionTime;
	}

	@Override
	public void setLastExecutionTime(Instant lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

	@PrePersist
	protected void onPersist() {
		if (this.creationTime == null) {
			this.creationTime = Instant.now();
		}
	}
}
