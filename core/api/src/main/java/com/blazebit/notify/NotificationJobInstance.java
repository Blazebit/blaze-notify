/*
 * Copyright 2018 - 2025 Blazebit.
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
package com.blazebit.notify;

import com.blazebit.job.JobInstance;

/**
 * An abstract description of a notification job instance that produces notifications incrementally by using a cursor.
 *
 * @param <ID> The job instance id type
 * @param <R> The recipient cursor type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationJobInstance<ID, R> extends JobInstance<ID> {

    /**
     * Returns the recipient cursor representing the last processed recipient or <code>null</code> if none.
     *
     * @return the recipient cursor
     */
    R getRecipientCursor();

}
