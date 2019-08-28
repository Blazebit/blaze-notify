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

import com.blazebit.notify.actor.scheduler.executor.ExecutorServiceScheduler;
import com.blazebit.notify.job.JobContext;
import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.Schedule;
import com.blazebit.notify.job.event.JobInstanceListener;
import com.blazebit.notify.job.spi.JobInstanceProcessorFactory;
import com.blazebit.notify.job.spi.JobProcessorFactory;
import com.blazebit.notify.job.spi.ScheduleFactory;
import org.junit.After;
import org.junit.Assert;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AbstractJobTest {

    protected CountDownLatch latch;
    protected JobContext jobContext;

    protected JobContext.Builder builder() {
        return builder(1);
    }

    protected JobContext.Builder builder(int count) {
        latch = new CountDownLatch(count);
        return JobContext.Builder.create()
                .withJobProcessorFactory(JobProcessorFactory.of(((jobTrigger, context) -> {})))
                .withJobInstanceProcessorFactory(JobInstanceProcessorFactory.of(((jobInstance, context) -> {
                    return null;
                })))
                .withJobInstanceListener(new JobInstanceListener() {
                    @Override
                    public void onJobInstanceChunkSuccess(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
                        latch.countDown();
                    }

                    @Override
                    public void onJobInstanceError(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
                        latch.countDown();
                    }

                    @Override
                    public void onJobInstanceSuccess(JobInstance<?> jobInstance, JobInstanceProcessingContext<?> context) {
                        latch.countDown();
                    }
                })
                .withScheduleFactory(new SimpleScheduleFactory())
                .withProperty(ExecutorServiceScheduler.EXECUTOR_SERVICE_PROPERTY, Executors.newScheduledThreadPool(2))
                .withProperty(ExecutorServiceScheduler.EXECUTOR_SERVICE_PROPERTY + ".jobInstanceScheduler/jobInstance/processor", Executors.newSingleThreadScheduledExecutor());
    }

    protected void await() {
        await(3L, TimeUnit.SECONDS);
    }

    protected void await(long timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
            Assert.assertEquals("Expected to run " + latch.getCount() + " more jobs", 0L, latch.getCount());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void stop() {
        if (jobContext != null) {
            jobContext.stop();
        }
    }

    protected static class SimpleScheduleFactory implements ScheduleFactory {

        private final Schedule schedule;

        public SimpleScheduleFactory() {
            this(new SimpleSchedule());
        }

        public SimpleScheduleFactory(Schedule schedule) {
            this.schedule = schedule;
        }

        @Override
        public String asCronExpression(Instant instant) {
            return null;
        }

        @Override
        public Schedule createSchedule(String cronExpression) {
            return schedule;
        }
    }

    protected static class MutableClock extends Clock {

        private Instant instant;

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return null;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        public void setInstant(Instant instant) {
            this.instant = instant;
        }
    }
}
