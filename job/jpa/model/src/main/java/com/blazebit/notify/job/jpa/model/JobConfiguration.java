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

import javax.persistence.*;
import java.io.Serializable;
import java.time.*;
import java.util.*;

@Embeddable
public class JobConfiguration implements com.blazebit.notify.job.JobConfiguration, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * True if the job is considered as done
	 */
	private boolean done;
	/**
	 * True if overlapping executions of the job are allowed
	 */
	private boolean allowOverlap;
	/**
	 * Whether a job may be dropped when deferred more then maximumDeferCount
	 */
	private boolean dropable;
	/**
	 * The maximum number(inclusive) of times a job may be deferred
	 */
	private int maximumDeferCount;
	/**
	 * The deadline until which it makes sense to start processing the job
	 */
	private Instant deadline;
	/**
	 * The time frames within which this job may be executed
	 */
	private Set<TimeFrame> executionTimeFrames = new HashSet<>(0);
	/**
	 * The parameter for the job
	 */
	private Map<String, Serializable> jobParameters = new HashMap<>(0);

	public JobConfiguration() {
	}

	@Override
	@Column(nullable = false)
	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	@Column(nullable = false)
	public boolean isAllowOverlap() {
		return allowOverlap;
	}

	public void setAllowOverlap(boolean allowOverlap) {
		this.allowOverlap = allowOverlap;
	}

	@Override
	@Column(nullable = false)
	public boolean isDropable() {
		return dropable;
	}

	public void setDropable(boolean dropable) {
		this.dropable = dropable;
	}

	@Override
	@Column(nullable = false)
	public int getMaximumDeferCount() {
		return maximumDeferCount;
	}

	public void setMaximumDeferCount(int maximumDeferCount) {
		this.maximumDeferCount = maximumDeferCount;
	}

	@Override
	public Instant getDeadline() {
		return deadline;
	}

	public void setDeadline(Instant deadline) {
		this.deadline = deadline;
	}

	@ElementCollection
	public Set<TimeFrame> getExecutionTimeFrames() {
		return executionTimeFrames;
	}

	public void setExecutionTimeFrames(Set<TimeFrame> executionTimeFrames) {
		this.executionTimeFrames = executionTimeFrames;
	}

	@Override
	@ElementCollection
	@Column(name = "value")
	@MapKeyColumn(name = "name")
	public Map<String, Serializable> getJobParameters() {
		return jobParameters;
	}

	public void setJobParameters(Map<String, Serializable> jobParameters) {
		this.jobParameters = jobParameters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JobConfiguration)) {
			return false;
		}

		JobConfiguration that = (JobConfiguration) o;

		if (isDone() != that.isDone()) {
			return false;
		}
		if (isAllowOverlap() != that.isAllowOverlap()) {
			return false;
		}
		if (isDropable() != that.isDropable()) {
			return false;
		}
		if (getMaximumDeferCount() != that.getMaximumDeferCount()) {
			return false;
		}
		if (getDeadline() != null ? !getDeadline().equals(that.getDeadline()) : that.getDeadline() != null) {
			return false;
		}
		if (getExecutionTimeFrames() != null ? !getExecutionTimeFrames().equals(that.getExecutionTimeFrames()) : that.getExecutionTimeFrames() != null) {
			return false;
		}
		return getJobParameters() != null ? getJobParameters().equals(that.getJobParameters()) : that.getJobParameters() == null;

	}

	@Override
	public int hashCode() {
		int result = (isDone() ? 1 : 0);
		result = 31 * result + (isAllowOverlap() ? 1 : 0);
		result = 31 * result + (isDropable() ? 1 : 0);
		result = 31 * result + getMaximumDeferCount();
		result = 31 * result + (getDeadline() != null ? getDeadline().hashCode() : 0);
		result = 31 * result + (getExecutionTimeFrames() != null ? getExecutionTimeFrames().hashCode() : 0);
		result = 31 * result + (getJobParameters() != null ? getJobParameters().hashCode() : 0);
		return result;
	}
}
