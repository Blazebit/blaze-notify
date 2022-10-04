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
import com.blazebit.job.JobProcessor;
import com.blazebit.job.JobTrigger;
import com.blazebit.job.spi.JobProcessorFactory;

/**
 * A factory for creating notification job processors for a notification job trigger.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationJobProcessorFactory extends JobProcessorFactory {

    @Override
    default <T extends JobTrigger> JobProcessor<T> createJobProcessor(JobContext jobContext, T jobTrigger) {
        return (JobProcessor<T>) createJobProcessor((NotificationJobContext) jobContext, (NotificationJobTrigger) jobTrigger);
    }

    /**
     * Creates a notification job processor for the given notification job trigger.
     *
     * @param jobContext The notification job context
     * @param jobTrigger The notification job trigger
     * @param <T> The notification job instance type
     * @return The notification job processor
     * @throws JobException if the notification job processor can't be created
     */
    <T extends NotificationJobTrigger> NotificationJobProcessor<T> createJobProcessor(NotificationJobContext jobContext, T jobTrigger);

    /**
     * Creates a notification job processor factory that always returns the given notification job processor.
     *
     * @param jobProcessor The notification job processor to return
     * @return the notification job processor factory
     */
    static NotificationJobProcessorFactory of(NotificationJobProcessor<?> jobProcessor) {
        return new NotificationJobProcessorFactory() {

            @Override
            @SuppressWarnings("unchecked")
            public <T extends NotificationJobTrigger> NotificationJobProcessor<T> createJobProcessor(NotificationJobContext jobContext, T jobTrigger) {
                return (NotificationJobProcessor<T>) jobProcessor;
            }

        };
    }
}
