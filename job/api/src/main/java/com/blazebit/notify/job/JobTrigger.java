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

import java.time.Instant;

public interface JobTrigger extends JobInstance<Long> {

    @Override
    default Long getPartitionKey() {
        return getId();
    }

    String getName();

    Job getJob();

    /**
     * When <code>true</code>, multiple executions of the same job for this trigger are allowed.
     * Otherwise, the new executions will be deferred and maybe dropped due to the deferring.
     *
     * @return Whether overlapping executions are allowed
     */
    boolean isAllowOverlap();

    /**
     * The schedule according to which to execute this job.
     *
     * @return The schedule
     */
    Schedule getSchedule(JobContext jobContext);

    default Instant nextSchedule(JobContext jobContext, ScheduleContext scheduleContext) {
        return getSchedule(jobContext).nextSchedule(scheduleContext);
    }

}
