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
package com.blazebit.notify.notification.channel.memory;

import com.blazebit.notify.notification.*;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class MemoryChannel implements Channel<Notification<NotificationMessage>, NotificationMessage> {

    private static final int DEFAULT_CAPACITY = 1024;
    private final Queue<NotificationMessage> queue;
    private final NotificationJobProcessor<? extends Notification<NotificationMessage>, ? extends NotificationMessage> jobProcessor;

    public MemoryChannel(NotificationJobProcessor<? extends Notification<NotificationMessage>, ? extends NotificationMessage> jobProcessor) {
        this(DEFAULT_CAPACITY, jobProcessor);
    }

    public MemoryChannel(int capacity, NotificationJobProcessor<? extends Notification<NotificationMessage>, ? extends NotificationMessage> jobProcessor) {
        this(new ArrayBlockingQueue<NotificationMessage>(capacity), jobProcessor);
    }

    public MemoryChannel(Queue<NotificationMessage> queue, NotificationJobProcessor<? extends Notification<NotificationMessage>, ? extends NotificationMessage> jobProcessor) {
        this.queue = queue;
        this.jobProcessor = jobProcessor;
    }

    public Queue<NotificationMessage> getQueue() {
        return queue;
    }

    @Override
    public NotificationJobProcessor<Notification<NotificationMessage>, NotificationMessage> getJobProcessor() {
        return (NotificationJobProcessor<Notification<NotificationMessage>, NotificationMessage>) jobProcessor;
    }

    @Override
    public void sendNotification(Notification notification) {
        sendNotification(notification.getReceiver(), notification.getMessage());
    }

    @Override
    public void sendNotification(NotificationReceiver receiver, NotificationMessage message) {
        queue.add(message);
    }
}
