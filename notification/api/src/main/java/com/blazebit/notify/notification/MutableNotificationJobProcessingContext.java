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

import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.NotificationJobProcessingContext;

public class MutableNotificationJobProcessingContext implements NotificationJobProcessingContext {

    private final int processCount;
    private final int partitionId;
    private final int partitionCount;
    private Notification<?, ?, ?> lastProcessed;

    MutableNotificationJobProcessingContext(int processCount) {
        this(processCount, -1, -1);
    }

    MutableNotificationJobProcessingContext(int processCount, int partitionId, int partitionCount) {
        this.processCount = processCount;
        this.partitionId = partitionId;
        this.partitionCount = partitionCount;
    }

    @Override
    public Notification<?, ?, ?> getLastProcessed() {
        return lastProcessed;
    }

    void setLastProcessed(Notification<?, ?, ?> lastProcessed) {
        this.lastProcessed = lastProcessed;
    }

    @Override
    public int getProcessCount() {
        return processCount;
    }

    @Override
    public int getPartitionId() {
        return partitionId;
    }

    @Override
    public int getPartitionCount() {
        return partitionCount;
    }
}
