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

package com.blazebit.notify.jpa.model.base;

import com.blazebit.job.jpa.model.AbstractJob;
import com.blazebit.notify.NotificationJob;

import javax.persistence.MappedSuperclass;

/**
 * An abstract mapped superclass implementing the {@link NotificationJob} interface.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@MappedSuperclass
public abstract class AbstractNotificationJob extends AbstractJob implements NotificationJob {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an empty notification job.
     */
    public AbstractNotificationJob() {
    }

    /**
     * Creates a notification job with the given id.
     *
     * @param id The notification job id
     */
    public AbstractNotificationJob(Long id) {
        super(id);
    }
}
