/*
 * Copyright 2018 - 2019 Blazebit.
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

package com.blazebit.notify.jpa.model.base;

import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.job.jpa.model.AbstractTriggerBasedJobInstance;
import com.blazebit.notify.NotificationJobInstance;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * An abstract mapped superclass implementing the {@link NotificationJobInstance} interface.
 *
 * @param <ID> The job instance id type
 * @param <R> The recipient cursor type
 * @param <J> The notification job type
 * @param <T> The notification job trigger type
 * @author Christian Beikov
 * @since 1.0.0
 */
@MappedSuperclass
public abstract class AbstractTriggerBasedNotificationJobInstance<ID, R, J extends AbstractNotificationJob, T extends AbstractNotificationJobTrigger<J>> extends AbstractTriggerBasedJobInstance<ID, T> implements NotificationJobInstance<ID, R> {

    private static final long serialVersionUID = 1L;

    private R recipientCursor;

    /**
     * Creates an empty trigger based notification job instance.
     */
    public AbstractTriggerBasedNotificationJobInstance() {
    }

    /**
     * Creates a trigger based notification job instance with the given id.
     *
     * @param id The trigger based notification job instance id
     */
    public AbstractTriggerBasedNotificationJobInstance(ID id) {
        super(id);
    }

    @Override
    @Transient
    public Long getPartitionKey() {
        return (Long) getId();
    }

    @Override
    @Transient
    public R getLastProcessed() {
        return getRecipientCursor();
    }

    @Override
    public void onChunkSuccess(JobInstanceProcessingContext<?> processingContext) {
        setRecipientCursor((R) processingContext.getLastProcessed());
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
