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

public class DefaultNotification<R extends NotificationRecipient, N extends Notification<R, N, T>, T extends NotificationMessage> implements Notification<R, N, T> {

    private final NotificationJob<R, N, T> notificationJob;
    private final Channel<R, N, T> channel;
    private final R recipient;
    private final long epochDeadline;

    public DefaultNotification(NotificationJob<R, N, T> notificationJob, Channel<R, N, T> channel, R recipient, long epochDeadline) {
        this.notificationJob = notificationJob;
        this.channel = channel;
        this.recipient = recipient;
        this.epochDeadline = epochDeadline;
    }

    @Override
    public NotificationJob<R, N, T> getNotificationJob() {
        return notificationJob;
    }

    @Override
    public Channel<R, N, T> getChannel() {
        return channel;
    }

    @Override
    public R getRecipient() {
        return recipient;
    }

    @Override
    public long getEpochDeadline() {
        return epochDeadline;
    }

    @Override
    public int compareTo(N o) {
        return 0;
    }
}
