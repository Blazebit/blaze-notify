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
package com.blazebit.notify.actor;

public class ActorRunResult {

    private static final int SUSPEND = -1;
    private static final int DONE = -2;

    private final long delayMillis;

    private ActorRunResult(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public static ActorRunResult done() {
        return new ActorRunResult(DONE);
    }

    public static ActorRunResult suspend() {
        return new ActorRunResult(SUSPEND);
    }

    public static ActorRunResult rescheduleIn(long millis) {
        return new ActorRunResult(millis < 0 ? 0 : millis);
    }

    public boolean isDone() {
        return delayMillis == DONE;
    }

    public boolean isSuspend() {
        return delayMillis == SUSPEND;
    }

    public boolean isReschedule() {
        return delayMillis > -1;
    }

    public long getDelayMillis() {
        return delayMillis > -1 ? delayMillis : -1;
    }

}
