/*
 * Copyright 2018 - 2020 Blazebit.
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
import com.blazebit.job.jpa.model.AbstractJobInstance;
import com.blazebit.notify.NotificationJobInstance;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * An abstract mapped superclass implementing the {@link NotificationJobInstance} interface.
 *
 * @param <R> The recipient cursor type
 * @author Christian Beikov
 * @since 1.0.0
 */
@MappedSuperclass
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

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
    public Long getId() {
        return id();
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
