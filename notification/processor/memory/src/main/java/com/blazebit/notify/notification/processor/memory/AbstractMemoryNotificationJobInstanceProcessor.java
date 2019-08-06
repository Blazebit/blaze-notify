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

import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.processor.memory.AbstractMemoryJobInstanceProcessor;
import com.blazebit.notify.notification.*;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public abstract class AbstractMemoryNotificationJobInstanceProcessor<T, I, J extends NotificationJobInstance<?>, R extends NotificationRecipient<?>> extends AbstractMemoryJobInstanceProcessor<T, I, J> implements NotificationJobInstanceProcessor<T, J> {

    private static final Logger LOG = Logger.getLogger(AbstractMemoryNotificationJobInstanceProcessor.class.getName());

    public AbstractMemoryNotificationJobInstanceProcessor(BlockingQueue<I> sink) {
        super(sink);
    }

    @Override
    public T process(J jobInstance, JobInstanceProcessingContext<T> context) {
        List<? extends NotificationRecipient<?>> recipientBatch = context.getJobContext().getService(NotificationRecipientResolver.class).resolveNotificationRecipients(jobInstance, context);

        T lastNotificationProcessed = null;
        for (int i = 0; i < recipientBatch.size(); i++) {
            I jobResult = produceNotification(jobInstance, (R) recipientBatch.get(i));
            if (jobResult == null) {
                break;
            }
            try {
                sink.put(jobResult);
                lastNotificationProcessed = getProcessingResultId(jobResult);
            } catch (InterruptedException e) {
                LOG.warning("Thread was interrupted while adding notification " + jobResult + " to sink");
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (lastNotificationProcessed == context.getLastProcessed()) {
            lastNotificationProcessed = null;
        }

        return lastNotificationProcessed;
    }

    @Override
    protected I processSingle(J jobInstance, JobInstanceProcessingContext<T> context, T lastJobResultProcessed) {
        throw new UnsupportedOperationException();
    }

    protected abstract I produceNotification(J notificationJob, R recipient);
}
