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

/**
 *
 * @param <ID> The cursor value that allows the processor to work incrementally
 */
public interface JobInstanceProcessor<ID, J extends JobInstance<?>> {

    default boolean isTransactional() {
        return false;
    }

    /**
     * Processes the job instance with the given job instance context. If the job instance works incrementally,
     * returns the last processed element. When done, it returns <code>null</code>.
     *
     * @param jobInstance The job instance to process
     * @param context The job instance context
     * @return The last processed element, or <code>null</code> if done
     */
    ID process(J jobInstance, JobInstanceProcessingContext<ID> context);

}
