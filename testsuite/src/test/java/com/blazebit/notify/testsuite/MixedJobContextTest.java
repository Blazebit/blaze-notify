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
package com.blazebit.notify.testsuite;

import com.blazebit.job.JobConfiguration;
import com.blazebit.job.JobInstance;
import com.blazebit.job.JobInstanceProcessingContext;
import com.blazebit.job.JobInstanceProcessor;
import com.blazebit.job.Schedule;
import com.blazebit.job.memory.model.AbstractJobInstance;
import com.blazebit.job.processor.memory.AbstractMemoryJobInstanceProcessor;
import com.blazebit.job.spi.JobInstanceProcessorFactory;
import com.blazebit.notify.NotificationJobInstance;
import com.blazebit.notify.NotificationJobInstanceProcessor;
import com.blazebit.notify.NotificationRecipientResolver;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class MixedJobContextTest extends AbstractNotificationJobTest<SimpleNotificationRecipient, SimpleNotificationMessage> {

    @Test
    public void testMixedJobContext() throws Exception {
        this.jobContext = builder(4)
                .withJobInstanceProcessorFactory(JobInstanceProcessorFactory.of(new DelegatingJobInstanceProcessor(
                        new SimpleNotificationJobInstanceProcessor(),
                        new SomeJobInstanceProcessor())
                )).createContext();
        jobContext.getJobManager().addJobInstance(new SimpleNotificationJobTrigger(channel, NotificationRecipientResolver.of(new SimpleNotificationRecipient(Locale.GERMAN)), new OnceSchedule(), new OnceSchedule(), Collections.emptyMap()));
        jobContext.getJobManager().addJobInstance(new SomeJobInstance(new OnceSchedule()));
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(1, sink.size());
    }

    private static class DelegatingJobInstanceProcessor implements JobInstanceProcessor<Object, JobInstance<Object>> {

        private final NotificationJobInstanceProcessor<Object, NotificationJobInstance<?, Object>> notificationJobInstanceProcessor;
        private final JobInstanceProcessor<Object, JobInstance<?>> generalJobInstanceProcessor;

        public DelegatingJobInstanceProcessor(NotificationJobInstanceProcessor notificationJobInstanceProcessor, JobInstanceProcessor generalJobInstanceProcessor) {
            this.notificationJobInstanceProcessor = notificationJobInstanceProcessor;
            this.generalJobInstanceProcessor = generalJobInstanceProcessor;
        }

        @Override
        public Object process(JobInstance<Object> jobInstance, JobInstanceProcessingContext<Object> jobInstanceProcessingContext) {
            if (jobInstance instanceof NotificationJobInstance) {
                return notificationJobInstanceProcessor.process(
                        (NotificationJobInstance<?, Object>) jobInstance,
                        jobInstanceProcessingContext
                );
            } else {
                return generalJobInstanceProcessor.process(
                        jobInstance,
                        jobInstanceProcessingContext
                );
            }
        }
    }

    private static class SomeJobInstanceProcessor extends AbstractMemoryJobInstanceProcessor<Long, Object, JobInstance<Long>> {

        protected SomeJobInstanceProcessor() {
            super((a, b) -> {});
        }

        @Override
        protected Object processSingle(JobInstance<Long> jobInstance, JobInstanceProcessingContext<Long> context, Long lastJobResultProcessed) {
            return new Object();
        }
    }

    private static class SomeJobInstance extends AbstractJobInstance<Long> {

        private final JobConfiguration jobConfiguration = new com.blazebit.job.memory.model.JobConfiguration();

        public SomeJobInstance(Schedule schedule) {
            setScheduleTime(schedule.nextSchedule());
        }

        @Override
        public Long getPartitionKey() {
            return getId();
        }

        @Override
        public void onChunkSuccess(JobInstanceProcessingContext<?> jobInstanceProcessingContext) {

        }

        @Override
        public JobConfiguration getJobConfiguration() {
            return jobConfiguration;
        }
    }
}
