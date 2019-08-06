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

import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.job.JobTrigger;
import com.blazebit.notify.job.Schedule;

import java.time.Instant;

public abstract class AbstractJobTrigger<T extends AbstractJob> extends BaseEntity<Long> implements JobTrigger {

	private static final long serialVersionUID = 1L;

	private T job;
	private String name;
	private JobConfiguration jobConfiguration = new JobConfiguration();

	private String scheduleCronExpression;
	private Instant creationTime;
	private Instant lastExecutionTime;

	public AbstractJobTrigger() {
	}

	public AbstractJobTrigger(Long id) {
		super(id);
	}

	@Override
	public Long getId() {
		return id();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public T getJob() {
		return job;
	}

	public void setJob(T job) {
		this.job = job;
	}

	@Override
	public JobConfiguration getJobConfiguration() {
		return jobConfiguration;
	}

	public void setJobConfiguration(JobConfiguration jobConfiguration) {
		this.jobConfiguration = jobConfiguration;
	}

	public String getScheduleCronExpression() {
		return scheduleCronExpression;
	}

	public void setScheduleCronExpression(String scheduleCronExpression) {
		this.scheduleCronExpression = scheduleCronExpression;
	}

	@Override
	public Schedule getSchedule(JobContext jobContext) {
		return scheduleCronExpression == null ? null : jobContext.getScheduleFactory().createSchedule(scheduleCronExpression);
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
