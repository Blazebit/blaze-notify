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
package com.blazebit.notify.notification.schedule.spring;

import com.blazebit.notify.notification.Schedule;
import com.blazebit.notify.notification.ScheduleContext;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.util.Date;

public class TriggerSchedule implements Schedule {

    private final Trigger trigger;

    public TriggerSchedule(Trigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public long nextEpochSchedule(ScheduleContext ctx) {
        return trigger.nextExecutionTime(new DelegatingTriggerContext(ctx)).getTime();
    }

    private class DelegatingTriggerContext implements TriggerContext {
        private final ScheduleContext delegate;

        private DelegatingTriggerContext(ScheduleContext delegate) {
            this.delegate = delegate;
        }

        @Override
        public Date lastScheduledExecutionTime() {
            return new Date(delegate.getLastScheduledExecutionTime());
        }

        @Override
        public Date lastActualExecutionTime() {
            return new Date(delegate.getLastActualExecutionTime());
        }

        @Override
        public Date lastCompletionTime() {
            return new Date(delegate.getLastCompletionTime());
        }
    }

}
