/*
 * Copyright 2018 - 2023 Blazebit.
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
package com.blazebit.notify.processor.memory;

import com.blazebit.job.JobContext;
import com.blazebit.job.JobInstance;
import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.job.processor.memory.AbstractMemoryJobInstanceProcessor;
import com.blazebit.notify.NotificationJobInstance;
import com.blazebit.notify.NotificationJobInstanceProcessor;
import com.blazebit.notify.NotificationRecipient;
import com.blazebit.notify.NotificationRecipientResolver;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;

/**
 * An abstract notification job instance processor implementation that writes into a sink.
 *
 * @param <ID> The job instance cursor type
 * @param <T>  The result type of the processing
 * @param <I>  The job instance type
 * @param <R>  The recipient type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractMemoryNotificationJobInstanceProcessor<ID, T, I extends NotificationJobInstance<Long, ID>, R extends NotificationRecipient<?>> extends AbstractMemoryJobInstanceProcessor<ID, T, I> implements NotificationJobInstanceProcessor<ID, I> {

    /**
     * Creates a new job instance processor that publishes results as job instances to the {@link com.blazebit.job.JobManager}.
     */
    public AbstractMemoryNotificationJobInstanceProcessor() {
        super((context, notification) -> context.getJobManager().addJobInstance((JobInstance<?>) notification));
    }

    /**
     * Creates a new job instance processor that publishes results to the given sink.
     *
     * @param sink The sink to publish results to
     */
    public AbstractMemoryNotificationJobInstanceProcessor(BlockingQueue<T> sink) {
        super(sink);
    }

    /**
     * Creates a new job instance processor that publishes results to the given sink.
     *
     * @param sink The sink to publish results to
     */
    public AbstractMemoryNotificationJobInstanceProcessor(BiConsumer<JobContext, T> sink) {
        super(sink);
    }

    @Override
    public ID process(I jobInstance, JobInstanceProcessingContext<ID> context) {
        JobContext jobContext = context.getJobContext();
        List<? extends NotificationRecipient<?>> recipientBatch = jobContext.getService(NotificationRecipientResolver.class).resolveNotificationRecipients(jobInstance, context);

        ID lastNotificationProcessed = null;
        for (int i = 0; i < recipientBatch.size(); i++) {
            T jobResult = produceNotification(context, jobInstance, (R) recipientBatch.get(i));
            if (jobResult == null) {
                break;
            }
            sink.accept(jobContext, jobResult);
            lastNotificationProcessed = getProcessingResultId(jobResult);
        }

        if (lastNotificationProcessed == context.getLastProcessed()) {
            lastNotificationProcessed = null;
        }

        return lastNotificationProcessed;
    }

    @Override
    protected T processSingle(I jobInstance, JobInstanceProcessingContext<ID> context, ID lastJobResultProcessed) {
        throw new UnsupportedOperationException();
    }

    /**
     * Produces a notification for the given notification job instance and the given recipient.
     *
     * @param context         The processing context
     * @param notificationJob The notification job instance
     * @param recipient       The recipient
     * @return the notification
     */
    protected abstract T produceNotification(JobInstanceProcessingContext<ID> context, I notificationJob, R recipient);
}
