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
package com.blazebit.notify;

import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.job.JobInstanceProcessor;

/**
 * A processor for notification job instances.
 *
 * @param <ID> The notification job instance cursor type
 * @param <J> The notification job instance type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationJobInstanceProcessor<ID, J extends NotificationJobInstance<?, ID>> extends JobInstanceProcessor<ID, J> {

    @Override
    ID process(J jobInstance, JobInstanceProcessingContext<ID> context);

}
