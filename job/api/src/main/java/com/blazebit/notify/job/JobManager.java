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

package com.blazebit.notify.job;

import com.blazebit.notify.job.event.JobInstanceListener;
import com.blazebit.notify.job.event.JobTriggerListener;

import java.util.List;

public interface JobManager extends JobTriggerListener, JobInstanceListener {

    long addJob(Job job);

    long addJobTrigger(JobTrigger jobTrigger);

    long addJobInstance(JobInstance jobInstance);

    List<JobTrigger> getUndoneJobTriggers(int partition, int partitionCount);

    List<JobInstance> getUndoneJobInstances(int partition, int partitionCount);

    JobTrigger getJobTrigger(long id);

    JobInstance getJobInstance(long id);
}
