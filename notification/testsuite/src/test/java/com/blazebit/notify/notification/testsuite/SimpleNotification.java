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

import com.blazebit.notify.notification.Channel;
import com.blazebit.notify.notification.Notification;
import com.blazebit.notify.notification.NotificationMessage;
import com.blazebit.notify.notification.NotificationReceiver;

public class SimpleNotification<T extends NotificationMessage> implements Notification<T> {

    private final T message;
    private final Channel<Notification<T>, T> channel;
    private final NotificationReceiver receiver;
    private final long epochDeadline;

    public SimpleNotification(T message, Channel<Notification<T>, T> channel, NotificationReceiver receiver, long epochDeadline) {
        this.message = message;
        this.channel = channel;
        this.receiver = receiver;
        this.epochDeadline = epochDeadline;
    }

    @Override
    public T getMessage() {
        return message;
    }

    @Override
    public Channel<Notification<T>, T> getChannel() {
        return channel;
    }

    @Override
    public NotificationReceiver getReceiver() {
        return receiver;
    }

    @Override
    public long getEpochDeadline() {
        return epochDeadline;
    }
}
