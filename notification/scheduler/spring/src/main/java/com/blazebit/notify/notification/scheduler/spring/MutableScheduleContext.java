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
package com.blazebit.notify.notification.scheduler.spring;

import com.blazebit.notify.notification.ScheduleContext;

public class MutableScheduleContext implements ScheduleContext {
    private long lastScheduledExecutionTime;
    private long lastActualExecutionTime;
    private long lastCompletionTime;

    @Override
    public long getLastScheduledExecutionTime() {
        return lastScheduledExecutionTime;
    }

    void setLastScheduledExecutionTime(long lastScheduledExecutionTime) {
        this.lastScheduledExecutionTime = lastScheduledExecutionTime;
    }

    @Override
    public long getLastActualExecutionTime() {
        return lastActualExecutionTime;
    }

    void setLastActualExecutionTime(long lastActualExecutionTime) {
        this.lastActualExecutionTime = lastActualExecutionTime;
    }

    @Override
    public long getLastCompletionTime() {
        return lastCompletionTime;
    }

    void setLastCompletionTime(long lastCompletionTime) {
        this.lastCompletionTime = lastCompletionTime;
    }
}
