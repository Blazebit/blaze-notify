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
package com.blazebit.notify.notification;

import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.JobInstanceProcessor;
import com.blazebit.notify.job.spi.JobInstanceProcessorFactory;

public interface NotificationJobInstanceProcessorFactory extends JobInstanceProcessorFactory {

    @Override
    default <T extends JobInstance> JobInstanceProcessor<?, T> createJobInstanceProcessor(JobContext jobContext, T jobInstance) {
        return (JobInstanceProcessor<?, T>) createJobInstanceProcessor((NotificationJobContext) jobContext, (NotificationJobInstance<?>) jobInstance);
    }

    <T extends NotificationJobInstance<?>> NotificationJobInstanceProcessor<?, T> createJobInstanceProcessor(NotificationJobContext jobContext, T jobInstance);
}
