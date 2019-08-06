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

import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.JobInstanceState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@MappedSuperclass
@Table(name = "job_instance")
public abstract class AbstractJobInstance<T extends AbstractJobTrigger<? extends AbstractJob>> extends BaseEntity implements JobInstance {

	private static final long serialVersionUID = 1L;

	private T trigger;
	private JobInstanceState state;

	private int deferCount;
	private Instant scheduleTime;
	private Instant creationTime;
	private Instant lastExecutionTime;
	
	protected AbstractJobInstance() {
	}

	protected AbstractJobInstance(Long id) {
		super(id);
	}

	public abstract void onChunkSuccess(JobInstanceProcessingContext<?> processingContext);

	@NotNull
	@Override
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trigger_id", nullable = false, foreignKey = @ForeignKey(name = "job_instance_fk_job_trigger"))
	public T getTrigger() {
		return trigger;
	}

	public void setTrigger(T trigger) {
		this.trigger = trigger;
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

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	public JobInstanceState getState() {
		return state;
	}

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
