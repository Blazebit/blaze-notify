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
import com.blazebit.job.memory.model.JobConfiguration;
import com.blazebit.notify.Notification;
import com.blazebit.notify.NotificationJobInstance;

/**
 * An abstract base class implementing the {@link com.blazebit.notify.Notification} interface that is based on a {@link NotificationJobInstance}.
 *
 * @param <ID> The notification id type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractNotification<ID> extends AbstractJobInstance<ID> implements Notification<ID> {

    private static final long serialVersionUID = 1L;

    private String channelType;
    private JobConfiguration jobConfiguration = new JobConfiguration();

    /**
     * Creates a notification with the given id.
     *
     * @param id The notification id
     */
    public AbstractNotification(ID id) {
        super(id);
    }

    @Override
    public String getChannelType() {
        return channelType;
    }

    /**
     * Sets the given channel type.
     *
     * @param channelType The channel type
     */
    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    @Override
    public JobConfiguration getJobConfiguration() {
        return jobConfiguration;
    }

    /**
     * Sets the given job configuration.
     *
     * @param jobConfiguration The job configuration
     */
    public void setJobConfiguration(JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
    }

    @Override
    public void onChunkSuccess(JobInstanceProcessingContext<?> processingContext) {
    }
}
