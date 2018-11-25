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

package com.blazebit.notify.notification.testsuite;

import com.blazebit.notify.notification.*;

public class SimpleNotification<R extends NotificationReceiver, T extends NotificationMessage> implements Notification<R, SimpleNotification<R, T>, T> {

    private final NotificationJob<R, SimpleNotification<R, T>, T> notificationJob;
    private final Channel<R, SimpleNotification<R, T>, T> channel;
    private final R receiver;
    private final long epochDeadline;

    public SimpleNotification(NotificationJob<R, SimpleNotification<R, T>, T> notificationJob, Channel<R, SimpleNotification<R, T>, T> channel, R receiver, long epochDeadline) {
        this.notificationJob = notificationJob;
        this.channel = channel;
        this.receiver = receiver;
        this.epochDeadline = epochDeadline;
    }

    @Override
    public NotificationJob<R, SimpleNotification<R, T>, T> getNotificationJob() {
        return notificationJob;
    }

    @Override
    public Channel<R, SimpleNotification<R, T>, T> getChannel() {
        return channel;
    }

    @Override
    public R getReceiver() {
        return receiver;
    }

    @Override
    public long getEpochDeadline() {
        return epochDeadline;
    }
}
