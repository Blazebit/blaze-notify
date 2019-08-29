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

import com.blazebit.notify.job.Schedule;
import com.blazebit.notify.job.ScheduleContext;
import com.blazebit.notify.job.spi.JobProcessorFactory;
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

    @Test
    public void testTriggerBasedJobInstanceSchedule() throws Exception {
        this.jobContext = builder()
                .withJobProcessorFactory(JobProcessorFactory.of(((jobInstance, context) -> {
                    sink.add(jobInstance);
                })))
                .createContext();
        jobContext.getJobManager().addJobInstance(new SimpleJobTrigger(new OnceSchedule(), Collections.emptyMap()));
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(1, sink.size());
    }

    @Test
    public void testRecurringTriggerBasedJobInstanceSchedule() throws Exception {
        this.jobContext = builder(2)
                .withJobProcessorFactory(JobProcessorFactory.of(((jobInstance, context) -> {
                    sink.add(jobInstance);
                })))
                .createContext();
        jobContext.getJobManager().addJobInstance(new SimpleJobTrigger(new Schedule() {
            @Override
            public long nextEpochSchedule(ScheduleContext ctx) {
                if (ctx.getLastExecutionTime() == 0) {
                    return ctx.getLastScheduleTime();
                }
                return 0;
            }
        }, Collections.emptyMap()));
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(2, sink.size());
    }
}
