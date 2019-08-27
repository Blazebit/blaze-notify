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

import com.blazebit.notify.job.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@MappedSuperclass
@Table(name = "job_trigger")
public abstract class AbstractJobTrigger<T extends AbstractJob> extends AbstractJobInstance<Long> implements JpaJobTrigger {

	private static final long serialVersionUID = 1L;

	private T job;
	private String name;
	private JobConfiguration jobConfiguration = new JobConfiguration();

	/**
	 * True if overlapping executions of the job are allowed
	 */
	private boolean allowOverlap;
	private String scheduleCronExpression;

	public AbstractJobTrigger() {
	}

	public AbstractJobTrigger(Long id) {
		super(id);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
	public Long getId() {
		return id();
	}

	@Override
	public void onChunkSuccess(JobInstanceProcessingContext<?> processingContext) {
	}

	@NotNull
	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// NOTE: We moved the relation from the embedded id to the outer entity because of HHH-10292
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "job_id", nullable = false, foreignKey = @ForeignKey(name = "job_trigger_fk_job"))
	public T getJob() {
		return job;
	}

	public void setJob(T job) {
		this.job = job;
	}

	@Override
	public void setJob(Job job) {
		setJob((T) job);
	}

	@Override
	@Embedded
	@AssociationOverrides({
			@AssociationOverride(name = "executionTimeFrames", joinTable = @JoinTable(name = "job_trigger_execution_time_frames", foreignKey = @ForeignKey(name = "job_trigger_execution_time_frames_fk_job_trigger"))),
			@AssociationOverride(name = "jobParameters", joinTable = @JoinTable(name = "job_trigger_parameter", foreignKey = @ForeignKey(name = "job_trigger_parameter_fk_job_trigger")))
	})
	public JobConfiguration getJobConfiguration() {
		return jobConfiguration;
	}

	@Transient
	@Override
	public JobConfiguration getOrCreateJobConfiguration() {
		if (getJobConfiguration() == null) {
			setJobConfiguration(new JobConfiguration());
		}
		return getJobConfiguration();
	}

	public void setJobConfiguration(JobConfiguration jobConfiguration) {
		this.jobConfiguration = jobConfiguration;
	}

	@Override
	@Column(nullable = false)
	public boolean isAllowOverlap() {
		return allowOverlap;
	}

	public void setAllowOverlap(boolean allowOverlap) {
		this.allowOverlap = allowOverlap;
	}

	@Column(nullable = false)
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

}
