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

import com.blazebit.notify.job.TimeFrame;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public interface Notification<ID> {

    ID getId();

    NotificationJobInstance<?> getNotificationJobInstance();

    NotificationRecipient<?> getRecipient();

    NotificationState getState();

    int getDeferCount();

    void incrementDeferCount();

    Instant getScheduleTime();

    void setScheduleTime(Instant scheduleTime);

    Instant getCreationTime();

    Set<? extends TimeFrame> getPublishTimeFrames();

    default Map<String, Serializable> getParameters() {
        return getNotificationJobInstance().getJobConfiguration().getJobParameters();
    }

    default int getMaximumDeferCount() {
        return getNotificationJobInstance().getJobConfiguration().getMaximumDeferCount();
    }

    default Instant getDeadline() {
        return getNotificationJobInstance().getJobConfiguration().getDeadline();
    }

    void markDone(Object result);

    void markFailed(Throwable t);

    default void markDeferred(Instant newScheduleTime) {
        incrementDeferCount();
        setScheduleTime(newScheduleTime);
    }

    void markDeadlineReached();

    void markDropped();
}
