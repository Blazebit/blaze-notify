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

import com.blazebit.notify.notification.NotificationRecipientResolver;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class NotificationJobProcessorTest extends AbstractNotificationJobTest<SimpleNotificationRecipient, SimpleNotificationMessage> {

    @Test
    public void testTriggerToChannel() throws Exception {
        // We expect the trigger, job instance and notification to run
        this.jobContext = builder(3).createContext();
        jobContext.getJobManager().addJobInstance(new SimpleNotificationJobTrigger(channel, NotificationRecipientResolver.of(new SimpleNotificationRecipient(Locale.GERMAN)), new OnceSchedule(), new OnceSchedule(), Collections.emptyMap()));
        await();
        jobContext.stop(1, TimeUnit.MINUTES);
        assertEquals(1, sink.size());
    }
    // TODO: updateEarliestSchedule tests + channel partition tests
}
