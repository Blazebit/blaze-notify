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
package com.blazebit.notify.job.testsuite;

import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.job.JobProcessor;
import com.blazebit.notify.job.JobTrigger;
import com.blazebit.notify.job.spi.JobInstanceProcessorFactory;
import com.blazebit.notify.job.spi.JobProcessorFactory;
import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TriggerBasedJobInstanceTest extends AbstractJobTest {

    protected BlockingQueue<Object> sink;

    public TriggerBasedJobInstanceTest() {
        this.sink = new ArrayBlockingQueue<>(1024);
    }

    @Override
    protected JobContext.Builder builder() {
        // We expect the trigger, and the job instance to run
        return builder(2);
    }

    @Override
    protected JobContext.Builder builder(int count) {
        return super.builder(count)
                .withJobProcessorFactory(JobProcessorFactory.of(new SimpleJobProcessor()));
    }

    @Test
    public void testTriggerBasedJobInstanceSchedule() throws Exception {
        this.jobContext = builder()
                .withJobInstanceProcessorFactory(JobInstanceProcessorFactory.of(((jobInstance, context) -> {
                    sink.add(jobInstance);
                    return null;
                })))
                .createContext();
        jobContext.getJobManager().addJobInstance(new SimpleJobTrigger(new SimpleSchedule(), Collections.emptyMap()));
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(1, sink.size());
    }
    // TODO: recurring trigger tests

    private static class SimpleJobProcessor implements JobProcessor<JobTrigger> {
        @Override
        public void process(JobTrigger jobTrigger, JobContext context) {
            context.getJobManager().addJobInstance(new SimpleTriggerBasedJobInstance((SimpleJobTrigger) jobTrigger));
        }
    }
}
