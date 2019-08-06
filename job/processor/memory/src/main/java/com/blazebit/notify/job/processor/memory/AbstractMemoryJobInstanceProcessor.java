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
package com.blazebit.notify.job.processor.memory;

import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.JobInstanceProcessor;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public abstract class AbstractMemoryJobInstanceProcessor<T, I, J extends JobInstance> implements JobInstanceProcessor<T, J> {

    private static final Logger LOG = Logger.getLogger(AbstractMemoryJobInstanceProcessor.class.getName());

    protected final BlockingQueue<I> sink;

    public AbstractMemoryJobInstanceProcessor(BlockingQueue<I> sink) {
        this.sink = sink;
    }

    @Override
    public T process(J jobInstance, JobInstanceProcessingContext<T> context) {
        T lastJobResultProcessed = context.getLastProcessed();
        for (int i = 0; i < context.getProcessCount(); i++) {
            I processingResult = processSingle(jobInstance, context, lastJobResultProcessed);
            if (processingResult == null) {
                break;
            }
            try {
                sink.put(processingResult);
                lastJobResultProcessed = getProcessingResultId(processingResult);
            } catch (InterruptedException e) {
                LOG.warning("Thread was interrupted while adding job result " + processingResult + " to sink");
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (lastJobResultProcessed == context.getLastProcessed()) {
            lastJobResultProcessed = null;
        }

        return lastJobResultProcessed;
    }

    protected abstract I processSingle(J job, JobInstanceProcessingContext<T> context, T lastJobResultProcessed);

    protected abstract T getProcessingResultId(I processingResult);

}
