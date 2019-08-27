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

package com.blazebit.notify.job.memory.storage;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.notify.job.ConfigurationSource;
import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.JobTrigger;
import com.blazebit.notify.job.PartitionKey;
import com.blazebit.notify.job.spi.PartitionKeyProvider;
import com.blazebit.notify.job.spi.PartitionKeyProviderFactory;

@ServiceProvider(PartitionKeyProviderFactory.class)
public class MemoryPartitionKeyProvider implements PartitionKeyProvider, PartitionKeyProviderFactory {

    @Override
    public PartitionKeyProvider createPartitionKeyProvider(com.blazebit.notify.job.ServiceProvider serviceProvider, ConfigurationSource configurationSource) {
        return this;
    }

    @Override
    public PartitionKey getDefaultTriggerPartitionKey() {
        return TRIGGER_ONLY;
    }

    @Override
    public PartitionKey getDefaultJobInstancePartitionKey() {
        return NON_TRIGGER;
    }

    private static final PartitionKey NON_TRIGGER = new PartitionKey() {
        @Override
        public boolean matches(JobInstance<?> jobInstance) {
            return !(jobInstance instanceof JobTrigger);
        }
        @Override
        public String toString() {
            return "jobInstance";
        }
    };
    private static final PartitionKey TRIGGER_ONLY = new PartitionKey() {
        @Override
        public Class<? extends JobInstance<?>> getJobInstanceType() {
            return JobTrigger.class;
        }
        @Override
        public boolean matches(JobInstance<?> jobInstance) {
            return jobInstance instanceof JobTrigger;
        }
        @Override
        public String toString() {
            return "jobTrigger";
        }
    };
}
