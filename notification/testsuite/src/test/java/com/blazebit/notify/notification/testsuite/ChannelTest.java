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

import java.util.Queue;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ChannelTest<R extends NotificationRecipient<?>, T extends NotificationMessage> extends AbstractConfigurationTest<R, T> {

    public ChannelTest(Channel<R, T> channel, T defaultMessage, Queue<NotificationMessage> sink, NotificationJobProcessorFactory jobProcessorFactory, NotificationJobInstanceProcessorFactory jobInstanceProcessorFactory) {
        super(channel, defaultMessage, sink, jobProcessorFactory, jobInstanceProcessorFactory);
    }

    @Test
    public void simpleTest() {
        channel.sendNotificationMessage(null, defaultMessage);
        assertEquals(1, sink.size());
    }

}
