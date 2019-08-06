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

package com.blazebit.notify.job.memory.model;

import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.JobInstanceState;

import java.time.Instant;

public abstract class AbstractJobInstance<T extends AbstractJobTrigger<? extends AbstractJob>> extends BaseEntity<Long> implements JobInstance {

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

	public abstract void onChunkSuccess(JobInstanceProcessingContext<?> context);

	public Long getId() {
		return id();
	}

	@Override
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

	public JobInstanceState getState() {
		return state;
	}

	public void setState(JobInstanceState state) {
		this.state = state;
	}

	@Override
	public int getDeferCount() {
		return deferCount;
	}

	public void setDeferCount(int deferCount) {
		this.deferCount = deferCount;
	}

	@Override
	public Instant getScheduleTime() {
		return scheduleTime;
	}

	@Override
	public void setScheduleTime(Instant scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	@Override
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
}
