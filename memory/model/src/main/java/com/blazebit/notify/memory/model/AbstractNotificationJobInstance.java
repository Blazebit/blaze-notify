/*
 * Copyright 2018 - 2023 Blazebit.
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

package com.blazebit.notify.memory.model;

import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.job.memory.model.AbstractJobInstance;
import com.blazebit.notify.NotificationJobInstance;

/**
 * An abstract base class implementing the {@link NotificationJobInstance} interface.
 *
 * @param <R> The recipient cursor type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractNotificationJobInstance<R> extends AbstractJobInstance<Long> implements NotificationJobInstance<Long, R> {

    private static final long serialVersionUID = 1L;

    private R recipientCursor;

    /**
     * Creates an empty notification job instance.
     */
    public AbstractNotificationJobInstance() {
    }

    /**
     * Creates a notification job instance with the given id.
     *
     * @param id The notification job instance id
     */
    public AbstractNotificationJobInstance(Long id) {
        super(id);
    }

    @Override
    public R getLastProcessed() {
        return getRecipientCursor();
    }

    @Override
    public void onChunkSuccess(JobInstanceProcessingContext<?> context) {
        setRecipientCursor((R) context.getLastProcessed());
    }

    @Override
    public R getRecipientCursor() {
        return recipientCursor;
    }

    /**
     * Sets the given recipient cursor.
     *
     * @param recipientCursor The recipient cursor
     */
    public void setRecipientCursor(R recipientCursor) {
        this.recipientCursor = recipientCursor;
    }
}
