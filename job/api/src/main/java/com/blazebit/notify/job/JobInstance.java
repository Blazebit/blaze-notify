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

public interface JobInstance<ID> {

    ID getId();

    Long getPartitionKey();

    JobInstanceState getState();

    int getDeferCount();

    void incrementDeferCount();

    Instant getScheduleTime();

    void setScheduleTime(Instant scheduleTime);

    default Instant nextSchedule(JobContext jobContext, ScheduleContext scheduleContext) {
        return getScheduleTime();
    }

    Instant getCreationTime();

    Instant getLastExecutionTime();

    void setLastExecutionTime(Instant lastExecutionTime);

    void onChunkSuccess(JobInstanceProcessingContext<?> processingContext);

    JobConfiguration getJobConfiguration();

    void markDone(Object result);

    void markFailed(Throwable t);

    default void markDeferred(Instant newScheduleTime) {
        incrementDeferCount();
        if (getDeferCount() > getJobConfiguration().getMaximumDeferCount()) {
            markDropped();
        }
        setScheduleTime(newScheduleTime);
    }

    void markDeadlineReached();

    void markDropped();
}
