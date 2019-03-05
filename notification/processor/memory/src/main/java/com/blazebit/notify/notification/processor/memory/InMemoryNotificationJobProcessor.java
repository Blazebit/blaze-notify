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
package com.blazebit.notify.notification.processor.memory;

import com.blazebit.notify.notification.*;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public abstract class InMemoryNotificationJobProcessor<R extends NotificationRecipient, N extends Notification<R, N, T>, T extends NotificationMessage> implements NotificationJobProcessor<R, N, T> {

    private static final Logger LOG = Logger.getLogger(InMemoryNotificationJobProcessor.class.getName());

    private final BlockingQueue<N> sink;

    public InMemoryNotificationJobProcessor(BlockingQueue<N> sink) {
        this.sink = sink;
    }

    @Override
    public N process(NotificationJob<R, N, T> notificationJob, NotificationJobProcessingContext context) {
        List<R> recipientBatch = notificationJob.getRecipientResolver().resolveNotificationRecipients(notificationJob, context);

        N lastNotificationProcessed = null;
        for (int i = 0; i < recipientBatch.size(); i++) {
            N notification = produceNotification(notificationJob, recipientBatch.get(i));
            try {
                sink.put(notification);
                lastNotificationProcessed = notification;
            } catch (InterruptedException e) {
                LOG.warning("Thread was interrupted while adding notification " + notification + " to sink");
                Thread.currentThread().interrupt();
                break;
            }
        }

        return lastNotificationProcessed;
    }

    protected abstract N produceNotification(NotificationJob<R, N, T> notificationJob, R recipient);
}
