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
package com.blazebit.notify.spi;

import com.blazebit.job.PartitionKey;

/**
 * Interface implemented by the notification implementation provider.
 *
 * Provides default notification partition keys for trigger and notification job instance processing.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationPartitionKeyProvider {

    /**
     * Returns a possibly decorated default trigger partition key based on the given default job trigger partition key.
     *
     * @param defaultJobTriggerPartitionKey The default job trigger partition key
     * @return the possibly decorated default trigger partition key
     */
    PartitionKey getDefaultTriggerPartitionKey(PartitionKey defaultJobTriggerPartitionKey);

    /**
     * Returns a possibly decorated default instance partition key based on the given default job instance partition key.
     *
     * @param defaultJobInstancePartitionKey The default job instance partition key
     * @return the possibly decorated default instance partition key
     */
    PartitionKey getDefaultJobInstancePartitionKey(PartitionKey defaultJobInstancePartitionKey);

    /**
     * Returns a partition key for the given channel type, based on the given default job instance partition key.
     *
     * @param defaultJobInstancePartitionKey The default job instance partition key
     * @param channelType The channel type
     * @return the partition key for the channel type
     */
    PartitionKey getPartitionKey(PartitionKey defaultJobInstancePartitionKey, String channelType);
}
