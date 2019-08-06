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
package com.blazebit.notify.notification.testsuite;

import com.blazebit.notify.notification.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NotificationJobProcessorTest extends AbstractConfigurationTest<SimpleNotificationRecipient, SimpleNotificationMessage> {

    public NotificationJobProcessorTest(Channel<SimpleNotificationRecipient, SimpleNotificationMessage> channel, SimpleNotificationMessage defaultMessage, Queue<NotificationMessage> sink, NotificationJobProcessorFactory jobProcessorFactory, NotificationJobInstanceProcessorFactory jobInstanceProcessorFactory) {
        super(channel, defaultMessage, sink, jobProcessorFactory, jobInstanceProcessorFactory);
    }

    @Parameterized.Parameters
    public static Object[][] createCombinations() {
        return createCombinations(new NotificationJobProcessorFactory() {
            @Override
            public <T extends NotificationJobTrigger> NotificationJobProcessor<T> createJobProcessor(NotificationJobContext jobContext, T jobTrigger) {
                return new NotificationJobProcessor<T>() {
                    @Override
                    public void process(T jobTrigger, NotificationJobContext context) {
                        SimpleNotification notification = new SimpleNotification((SimpleNotificationJobTrigger) jobTrigger);
                        Channel channel = jobContext.getChannel(jobContext.resolveChannelKey(notification));
                        channel.sendNotificationMessage(null, new SimpleNotificationMessage());
                        channel.sendNotificationMessage(null, new SimpleNotificationMessage());
                    }
                };
            }
        });
    }

    @Test
    public void simpleTest() throws InterruptedException {
        jobContext.getJobManager().addJobTrigger(new SimpleNotificationJobTrigger(channel, null, null/* TODO recipientResolver */, new SimpleSchedule(), new SimpleSchedule(), Collections.emptyMap()));
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(2, sink.size());
    }

}
