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

import com.blazebit.notify.job.JobTrigger;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@MappedSuperclass
@Table(name = "job_instance")
public abstract class AbstractTriggerBasedJobInstance<ID, T extends AbstractJobTrigger<? extends AbstractJob>> extends AbstractJobInstance<ID> implements JpaTriggerBasedJobInstance<ID> {

	private static final long serialVersionUID = 1L;

	private T trigger;

	protected AbstractTriggerBasedJobInstance() {
	}

	protected AbstractTriggerBasedJobInstance(ID id) {
		super(id);
	}

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
	public void setTrigger(JobTrigger jobTrigger) {
		setTrigger((T) jobTrigger);
	}
}
