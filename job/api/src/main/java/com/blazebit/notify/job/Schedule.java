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
package com.blazebit.notify.job;

import java.time.Instant;

public interface Schedule {

    default Instant nextSchedule() {
        return nextSchedule(now());
    }

    default Instant nextSchedule(ScheduleContext ctx) {
        return Instant.ofEpochMilli(nextEpochSchedule(ctx));
    }

    default long nextEpochSchedule() {
        return nextEpochSchedule(now());
    }

    /**
     * The next epoch in milliseconds, when the schedule should fire.
     * Returning the value {@link ScheduleContext#getLastScheduleTime()}
     * means that there is no next schedule.
     *
     * @param ctx The schedule context
     * @return The next schedule in epoch milliseconds
     */
    long nextEpochSchedule(ScheduleContext ctx);

    static ScheduleContext now() {
        final long now = System.currentTimeMillis();
        return scheduleContext(now);
    }

    static ScheduleContext scheduleContext(long lastScheduleTime) {
        return new ScheduleContext() {
            @Override
            public long getLastScheduleTime() {
                return lastScheduleTime;
            }

            @Override
            public long getLastExecutionTime() {
                return 0;
            }

            @Override
            public long getLastCompletionTime() {
                return 0;
            }
        };
    }
}
