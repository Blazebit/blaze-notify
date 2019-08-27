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

public abstract class AbstractTriggerBasedJobInstance<T extends AbstractJobTrigger<? extends AbstractJob>> extends AbstractJobInstance<Long> implements JobInstance<Long> {

	private static final long serialVersionUID = 1L;

	private T trigger;

	protected AbstractTriggerBasedJobInstance() {
	}

	protected AbstractTriggerBasedJobInstance(Long id) {
		super(id);
	}

	public T getTrigger() {
		return trigger;
	}

	public void setTrigger(T trigger) {
		this.trigger = trigger;
	}

	@Override
	public Long getPartitionKey() {
		return getId();
	}
}
