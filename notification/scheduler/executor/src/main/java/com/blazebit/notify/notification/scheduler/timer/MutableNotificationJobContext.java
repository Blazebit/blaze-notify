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
package com.blazebit.notify.notification.scheduler.timer;

import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.NotificationJobContext;

public class MutableNotificationJobContext implements NotificationJobContext {

    private final int processCount;
    private Notification<?> lastProcessed;

    public MutableNotificationJobContext(int processCount) {
        this.processCount = processCount;
    }

    @Override
    public Notification<?> getLastProcessed() {
        return lastProcessed;
    }

    public void setLastProcessed(Notification<?> lastProcessed) {
        this.lastProcessed = lastProcessed;
    }

    @Override
    public int getProcessCount() {
        return processCount;
    }

}
