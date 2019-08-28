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

import com.blazebit.notify.job.*;

import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public abstract class AbstractMemoryJobInstanceProcessor<T, I, J extends JobInstance<?>> implements JobInstanceProcessor<T, J> {

    protected final BiConsumer<JobContext, I> sink;

    public AbstractMemoryJobInstanceProcessor(BlockingQueue<I> sink) {
        this(new BiConsumer<JobContext, I>() {
            @Override
            public void accept(JobContext context, I object) {
                try {
                    sink.put(object);
                } catch (InterruptedException e) {
                    throw new JobException(e);
                }
            }
        });
    }

    public AbstractMemoryJobInstanceProcessor(BiConsumer<JobContext, I> sink) {
        this.sink = sink;
    }

    @Override
    public T process(J jobInstance, JobInstanceProcessingContext<T> context) {
        JobContext jobContext = context.getJobContext();
        T lastJobResultProcessed = context.getLastProcessed();
        for (int i = 0; i < context.getProcessCount(); i++) {
            I processingResult = processSingle(jobInstance, context, lastJobResultProcessed);
            if (processingResult == null) {
                break;
            }
            sink.accept(jobContext, processingResult);
            lastJobResultProcessed = getProcessingResultId(processingResult);
        }

        if (lastJobResultProcessed == context.getLastProcessed()) {
            lastJobResultProcessed = null;
        }

        return lastJobResultProcessed;
    }

    protected abstract I processSingle(J job, JobInstanceProcessingContext<T> context, T lastJobResultProcessed);

    protected abstract T getProcessingResultId(I processingResult);

}
