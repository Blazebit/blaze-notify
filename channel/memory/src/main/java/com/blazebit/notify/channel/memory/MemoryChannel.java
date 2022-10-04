/*
 * Copyright 2018 - 2022 Blazebit.
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
package com.blazebit.notify.channel.memory;

import com.blazebit.notify.Channel;
import com.blazebit.notify.ChannelKey;
import com.blazebit.notify.NotificationMessage;
import com.blazebit.notify.NotificationRecipient;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A in-memory channel that sends messages to a queue.
 *
 * @param <R> The notification recipient type
 * @param <T> The notification message type
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MemoryChannel<R extends NotificationRecipient<?>, T extends NotificationMessage> implements Channel<R, T> {

    /**
     * The key for which the channel is registered.
     */
    public static final ChannelKey<MemoryChannel<NotificationRecipient<?>, NotificationMessage>> KEY = (ChannelKey<MemoryChannel<NotificationRecipient<?>, NotificationMessage>>) (ChannelKey) ChannelKey.of("memory", MemoryChannel.class);
    private static final int DEFAULT_CAPACITY = 1024;
    private final Queue<T> queue;

    /**
     * Creates a channel with a queue that has a default capacity.
     */
    public MemoryChannel() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a channel with a queue with the given capacity.
     *
     * @param capacity The capacity
     */
    public MemoryChannel(int capacity) {
        this(new ArrayBlockingQueue<T>(capacity));
    }

    /**
     * Creates a channel with the given queue.
     *
     * @param queue The queue
     */
    public MemoryChannel(Queue<T> queue) {
        this.queue = queue;
    }

    /**
     * Returns the queue.
     *
     * @return the queue
     */
    public Queue<T> getQueue() {
        return queue;
    }

    @Override
    public Class<T> getNotificationMessageType() {
        return (Class<T>) NotificationMessage.class;
    }

    @Override
    public Object sendNotificationMessage(R recipient, T message) {
        queue.add(message);
        return null;
    }

    @Override
    public void close() {
    }
}
