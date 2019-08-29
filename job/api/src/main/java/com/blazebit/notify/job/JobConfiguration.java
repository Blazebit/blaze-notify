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

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public interface JobConfiguration {

    /**
     * When <code>true</code> the job execution will not be attempted if the maximum defer count is reached
     * and the constraints(time frame) do not allow the execution.
     *
     * @return <code>true</code> if dropable, <code>false</code> otherwise
     */
    boolean isDropable();

    /**
     * The maximum number of times a job execution may be deferred due to constraints(time frame).
     * A value of -1 defers a job execution indefinitely. A value of 0 leads to trying to execute the job
     * within the time frame, if that is not possible, the execution will happen as soon as possible.
     * A value greater than 0 e.g. X leads to deferring the execution up to X times due to constraints(time frame).
     * After deferring X times, it behaves as if it had the value 0.
     *
     * @return The maximum number of defers
     */
    int getMaximumDeferCount();

    /**
     * The deadline after which the job execution should not be attempted anymore.
     *
     * @return The deadline or <code>null</code> if there is none
     */
    Instant getDeadline();

    /**
     * The time frames within which the job should be executed.
     *
     * @return The time frames
     */
    Set<? extends TimeFrame> getExecutionTimeFrames();

    /**
     * The job parameters.
     *
     * @return The job parameters
     */
    Map<String, Serializable> getParameters();

}
