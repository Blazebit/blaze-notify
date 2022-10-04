/*
 * Copyright 2018 - 2022 Blazebit.
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

import com.blazebit.job.JobContext;
import com.blazebit.job.JobException;
import com.blazebit.job.JobInstance;
import com.blazebit.job.JobInstanceProcessor;
import com.blazebit.job.spi.JobInstanceProcessorFactory;

/**
 * A factory for creating notification job instance processors for a notification job instance.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationJobInstanceProcessorFactory extends JobInstanceProcessorFactory {

    @Override
    default <T extends JobInstance<?>> JobInstanceProcessor<?, T> createJobInstanceProcessor(JobContext jobContext, T jobInstance) {
        return (JobInstanceProcessor<?, T>) createJobInstanceProcessor((NotificationJobContext) jobContext, (NotificationJobInstance<?, ?>) jobInstance);
    }

    /**
     * Creates a notification job instance processor for the given notification job instance.
     *
     * @param jobContext The notification job context
     * @param jobInstance The notification job instance
     * @param <T> The notification job instance type
     * @return The notification job processor
     * @throws JobException if the notification job instance processor can't be created
     */
    <T extends NotificationJobInstance<?, ?>> NotificationJobInstanceProcessor<?, T> createJobInstanceProcessor(NotificationJobContext jobContext, T jobInstance);

    /**
     * Creates a notification job instance processor factory that always returns the given notification job instance processor.
     *
     * @param jobInstanceProcessor The notification job instance processor to return
     * @return the notification job instance processor factory
     */
    static NotificationJobInstanceProcessorFactory of(NotificationJobInstanceProcessor<?, ?> jobInstanceProcessor) {
        return new NotificationJobInstanceProcessorFactory() {

            @Override
            @SuppressWarnings("unchecked")
            public <T extends NotificationJobInstance<?, ?>> NotificationJobInstanceProcessor<?, T> createJobInstanceProcessor(NotificationJobContext jobContext, T jobInstance) {
                return (NotificationJobInstanceProcessor<?, T>) jobInstanceProcessor;
            }

        };
    }
}
