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
import com.blazebit.notify.job.JobInstanceState;
import com.blazebit.notify.job.JobRateLimitException;
import com.blazebit.notify.job.JobTemporaryException;
import com.blazebit.notify.job.memory.model.JobConfiguration;
import com.blazebit.notify.job.memory.model.TimeFrame;
import com.blazebit.notify.job.spi.JobInstanceProcessorFactory;
import com.blazebit.notify.job.spi.PartitionKeyProvider;
import com.blazebit.notify.job.spi.TransactionSupport;
import org.junit.After;
import org.junit.Test;

import java.time.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class JobInstanceTest extends AbstractJobTest {

    private BlockingQueue<Object> sink;

    public JobInstanceTest() {
        this.sink = new ArrayBlockingQueue<>(1024);
    }

    @Test
    public void testJobInstanceSchedule() throws Exception {
        // GIVEN
        this.jobContext = builder()
                .withJobInstanceProcessorFactory(JobInstanceProcessorFactory.of(((jobInstance, context) -> {
                    sink.add(jobInstance);
                    return null;
                })))
                .createContext();

        // WHEN
        jobContext.getJobManager().addJobInstance(new SimpleJobInstance());

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(1, sink.size());
    }

    @Test
    public void testFailSchedulerJobInstance() throws Exception {
        // GIVEN
        this.jobContext = builder().createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance() {
            @Override
            public JobConfiguration getJobConfiguration() {
                throw new RuntimeException();
            }
        };

        // WHEN
        jobContext.getJobManager().addJobInstance(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.FAILED, jobInstance.getState());
    }

    @Test
    public void testFailJobInstance() throws Exception {
        // GIVEN
        this.jobContext = builder()
                .withJobInstanceProcessorFactory(JobInstanceProcessorFactory.of(((jobInstance, context) -> {
                    throw new RuntimeException();
                })))
                .createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance();

        // WHEN
        jobContext.getJobManager().addJobInstance(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.FAILED, jobInstance.getState());
    }

    @Test
    public void testFailRateLimitJobInstance() throws Exception {
        // GIVEN
        Clock clock = Clock.fixed(Instant.parse("2018-01-01T00:00:00.00Z"), ZoneOffset.UTC);
        // We wait for 3x setScheduleTime invocations. Constructor, setter and via rate limiting
        this.jobContext = builder(3).withService(Clock.class, clock)
                .withJobInstanceProcessorFactory(JobInstanceProcessorFactory.of(((jobInstance, context) -> {
                    throw new JobRateLimitException(1000L);
                })))
                .createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance() {
            @Override
            public void setScheduleTime(Instant scheduleTime) {
                super.setScheduleTime(scheduleTime);
                latch.countDown();
            }
        };
        jobInstance.setCreationTime(clock.instant());
        jobInstance.setScheduleTime(jobInstance.getCreationTime());

        // WHEN
        jobContext.getJobManager().addJobInstance(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.NEW, jobInstance.getState());
        assertEquals(Instant.parse("2018-01-01T00:00:00.00Z").plusSeconds(1L), jobInstance.getScheduleTime());
    }

    @Test
    public void testFailTemporaryJobInstance() throws Exception {
        // GIVEN
        Clock clock = Clock.fixed(Instant.parse("2018-01-01T00:00:00.00Z"), ZoneOffset.UTC);
        // We wait for 3x setScheduleTime invocations. Constructor, setter and via temporary exception handling
        this.jobContext = builder(3).withService(Clock.class, clock)
                .withJobInstanceProcessorFactory(JobInstanceProcessorFactory.of(((jobInstance, context) -> {
                    throw new JobTemporaryException(1000L);
                })))
                .createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance() {
            @Override
            public void setScheduleTime(Instant scheduleTime) {
                super.setScheduleTime(scheduleTime);
                latch.countDown();
            }
        };
        jobInstance.setCreationTime(clock.instant());
        jobInstance.setScheduleTime(jobInstance.getCreationTime());

        // WHEN
        jobContext.getJobManager().addJobInstance(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.NEW, jobInstance.getState());
        assertEquals(Instant.parse("2018-01-01T00:00:00.00Z").plusSeconds(1L), jobInstance.getScheduleTime());
    }

    @Test
    public void testFailJobInstanceTransaction() throws Exception {
        // GIVEN
        CountDownLatch txLatch = new CountDownLatch(1);
        this.jobContext = builder()
                .withTransactionSupport(new TransactionSupport() {
                    @Override
                    public <T> T transactional(JobContext context, long transactionTimeoutMillis, boolean joinIfPossible, Callable<T> callable, Consumer<Throwable> exceptionHandler) {
                        if (txLatch.getCount() == 0) {
                            try {
                                return callable.call();
                            } catch (Exception e) {
                                exceptionHandler.accept(e);
                                return null;
                            }
                        }
                        txLatch.countDown();
                        return null;
                    }

                    @Override
                    public void registerPostCommitListener(Runnable o) {
                        o.run();
                    }
                })
                .withJobInstanceProcessorFactory(JobInstanceProcessorFactory.of(((jobInstance, context) -> {
                    sink.add(jobInstance);
                    return null;
                })))
                .createContext();

        // WHEN
        jobContext.getJobManager().addJobInstance(new SimpleJobInstance());

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(1, sink.size());
        assertEquals(0, txLatch.getCount());
    }

    @Test
    public void testDeadline() throws Exception {
        // GIVEN
        this.jobContext = builder().createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance();
        jobInstance.getJobConfiguration().setDeadline(jobInstance.getCreationTime());

        // WHEN
        jobContext.getJobManager().addJobInstance(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.DEADLINE_REACHED, jobInstance.getState());
    }

    @Test
    public void testExecutionTimeFrames() throws Exception {
        // GIVEN
        this.jobContext = builder().createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance();
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setEndYear(Year.of(2018));
        jobInstance.getJobConfiguration().getExecutionTimeFrames().add(timeFrame);

        // WHEN
        jobContext.getJobManager().addJobInstance(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.DROPPED, jobInstance.getState());
    }

    @Test
    public void testDefer() throws Exception {
        // GIVEN
        Clock clock = Clock.fixed(Instant.parse("2018-01-01T00:00:00.00Z"), ZoneOffset.UTC);
        this.jobContext = builder().withService(Clock.class, clock).createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance() {
            @Override
            public void markDeferred(Instant newScheduleTime) {
                super.markDeferred(newScheduleTime);
                latch.countDown();
            }
        };
        jobInstance.setCreationTime(clock.instant());
        jobInstance.setScheduleTime(jobInstance.getCreationTime());
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setStartYear(Year.of(2019));
        jobInstance.getJobConfiguration().setMaximumDeferCount(1);
        jobInstance.getJobConfiguration().getExecutionTimeFrames().add(timeFrame);

        // WHEN
        jobContext.getJobManager().addJobInstance(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.NEW, jobInstance.getState());
        assertEquals(1, jobInstance.getDeferCount());
        assertEquals(Instant.parse("2019-01-01T00:00:00.00Z"), jobInstance.getScheduleTime());
    }

    @Test
    public void testDeferDrop() throws Exception {
        // GIVEN
        Clock clock = Clock.fixed(Instant.parse("2018-01-01T00:00:00.00Z"), ZoneOffset.UTC);
        this.jobContext = builder().withService(Clock.class, clock).createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance();
        jobInstance.setCreationTime(clock.instant());
        jobInstance.setScheduleTime(jobInstance.getCreationTime());
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setStartYear(Year.of(2019));
        jobInstance.getJobConfiguration().setMaximumDeferCount(0);
        jobInstance.getJobConfiguration().getExecutionTimeFrames().add(timeFrame);

        // WHEN
        jobContext.getJobManager().addJobInstance(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.DROPPED, jobInstance.getState());
        assertEquals(1, jobInstance.getDeferCount());
        assertEquals(Instant.parse("2019-01-01T00:00:00.00Z"), jobInstance.getScheduleTime());
    }

    @Test
    public void testRefreshJobInstanceSchedulesSpecific() throws Exception {
        // GIVEN
        Clock clock = Clock.fixed(Instant.parse("2018-01-01T00:00:00.00Z"), ZoneOffset.UTC);
        this.jobContext = builder().withService(Clock.class, clock).createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance();
        jobInstance.setState(JobInstanceState.DONE);
        jobInstance.setCreationTime(clock.instant());
        jobInstance.setScheduleTime(jobInstance.getCreationTime());
        jobContext.getJobManager().addJobInstance(jobInstance);

        // WHEN
        jobInstance.setState(JobInstanceState.NEW);
        jobContext.refreshJobInstanceSchedules(jobInstance);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.DONE, jobInstance.getState());
    }

    @Test
    public void testRefreshJobInstanceSchedulesRescan() throws Exception {
        // GIVEN
        Clock clock = Clock.fixed(Instant.parse("2018-01-01T00:00:00.00Z"), ZoneOffset.UTC);
        this.jobContext = builder().withService(Clock.class, clock).createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance();
        jobInstance.setState(JobInstanceState.DONE);
        jobInstance.setCreationTime(clock.instant());
        jobInstance.setScheduleTime(jobInstance.getCreationTime());
        jobContext.getJobManager().addJobInstance(jobInstance);

        // WHEN
        jobInstance.setState(JobInstanceState.NEW);
        jobContext.refreshJobInstanceSchedules(0L);

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.DONE, jobInstance.getState());
    }

    @Test
    public void testRefreshJobInstanceSchedulesGeneral() throws Exception {
        // GIVEN
        Clock clock = Clock.fixed(Instant.parse("2018-01-01T00:00:00.00Z"), ZoneOffset.UTC);
        this.jobContext = builder().withService(Clock.class, clock).createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance();
        jobInstance.setState(JobInstanceState.DONE);
        jobInstance.setCreationTime(clock.instant());
        jobInstance.setScheduleTime(jobInstance.getCreationTime());
        jobContext.getJobManager().addJobInstance(jobInstance);

        // WHEN
        jobInstance.setState(JobInstanceState.NEW);
        jobContext.refreshJobInstanceSchedules(clock.instant().toEpochMilli());

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.DONE, jobInstance.getState());
    }

    @Test
    public void testRefreshJobInstanceSchedulesPartition() throws Exception {
        // GIVEN
        Clock clock = Clock.fixed(Instant.parse("2018-01-01T00:00:00.00Z"), ZoneOffset.UTC);
        JobContext.Builder builder = builder().withService(Clock.class, clock);
        PartitionKeyProvider partitionKeyProvider = builder.getPartitionKeyProviderFactory().createPartitionKeyProvider(null, null);
        this.jobContext = builder.createContext();
        SimpleJobInstance jobInstance = new SimpleJobInstance();
        jobInstance.setState(JobInstanceState.DONE);
        jobInstance.setCreationTime(clock.instant());
        jobInstance.setScheduleTime(jobInstance.getCreationTime());
        jobContext.getJobManager().addJobInstance(jobInstance);

        // WHEN
        jobInstance.setState(JobInstanceState.NEW);
        jobContext.refreshJobInstanceSchedules(partitionKeyProvider.getDefaultJobInstancePartitionKey(), clock.instant().toEpochMilli());

        // THEN
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(JobInstanceState.DONE, jobInstance.getState());
    }

    // TODO: chunking tests + cluster tests
}
