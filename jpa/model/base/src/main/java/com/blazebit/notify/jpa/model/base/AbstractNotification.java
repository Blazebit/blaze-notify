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
import com.blazebit.job.jpa.model.JobConfiguration;
import com.blazebit.job.jpa.model.TimeFrame;
import com.blazebit.notify.Notification;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * An abstract mapped superclass implementing the {@link Notification} interface.
 *
 * @param <ID> The notification id type
 * @author Christian Beikov
 * @since 1.0.0
 */
@MappedSuperclass
@Table(name = "notification")
public abstract class AbstractNotification<ID> extends AbstractJobInstance<ID> implements Notification<ID>, com.blazebit.job.JobConfiguration {

    private static final long serialVersionUID = 1L;

    private String channelType;
    private NotificationJobConfiguration jobConfiguration = new NotificationJobConfiguration();

    /**
     * Creates an empty notification.
     */
    public AbstractNotification() {
    }

    /**
     * Creates a notification with the given id.
     *
     * @param id The notification id
     */
    public AbstractNotification(ID id) {
        super(id);
    }

    @Override
    @Column(nullable = false)
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

    /*
     * We need to implement the JobConfiguration interface here to be able to make use of the insert-select strategy because Hibernate can't bind properties of embeddables.
     */

    @Override
    @Column(nullable = false)
    public boolean isDropable() {
        return jobConfiguration.isDropable();
    }

    /**
     * Sets whether the notification is dropable due to reaching the maximum defer count.
     *
     * @param dropable whether the notification is dropable
     */
    public void setDropable(boolean dropable) {
        jobConfiguration.setDropable(dropable);
    }

    @Override
    @Column(nullable = false)
    public int getMaximumDeferCount() {
        return jobConfiguration.getMaximumDeferCount();
    }

    /**
     * Sets the maximum defer count.
     *
     * @param maximumDeferCount The maximum defer count
     */
    public void setMaximumDeferCount(int maximumDeferCount) {
        jobConfiguration.setMaximumDeferCount(maximumDeferCount);
    }

    @Override
    public Instant getDeadline() {
        return jobConfiguration.getDeadline();
    }

    /**
     * Sets the deadline.
     *
     * @param deadline The deadline
     */
    public void setDeadline(Instant deadline) {
        jobConfiguration.setDeadline(deadline);
    }

    @Override
    @Transient
    public Set<TimeFrame> getExecutionTimeFrames() {
        return jobConfiguration.getExecutionTimeFrames();
    }

    /**
     * Sets the execution time frames.
     *
     * @param executionTimeFrames The execution time frames
     */
    public void setExecutionTimeFrames(Set<TimeFrame> executionTimeFrames) {
        jobConfiguration.setExecutionTimeFrames(executionTimeFrames);
    }

    @Override
    @Transient
    public Map<String, Serializable> getParameters() {
        return jobConfiguration.getParameters();
    }

    /**
     * Sets the notification parameters.
     *
     * @param parameters The job parameters
     */
    public void setParameters(Map<String, Serializable> parameters) {
        jobConfiguration.setParameters(parameters);
    }

    /**
     * The serializable representing the execution time frames and parameters.
     *
     * @return the serializable
     */
    @Lob
    @Column(name = "parameters")
    protected Serializable getParameterSerializable() {
        return jobConfiguration.getParameterSerializable();
    }

    /**
     * Sets the parameter serializable.
     *
     * @param parameterSerializable The parameter serializable
     */
    protected void setParameterSerializable(Serializable parameterSerializable) {
        jobConfiguration.setParameterSerializable(parameterSerializable);
    }

    @Override
    @Transient
    public com.blazebit.job.JobConfiguration getJobConfiguration() {
        return this;
    }

    @Override
    public void onChunkSuccess(JobInstanceProcessingContext<?> processingContext) {
    }

    /**
     * An embeddable implementing the {@link com.blazebit.job.JobConfiguration} interface.
     * The job parameters and execution time frames are serialized to a LOB to avoid join tables.
     *
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class NotificationJobConfiguration extends JobConfiguration {
        @Override
        public Serializable getParameterSerializable() {
            return super.getParameterSerializable();
        }

        @Override
        public void setParameterSerializable(Serializable parameterSerializable) {
            super.setParameterSerializable(parameterSerializable);
        }
    }
}
