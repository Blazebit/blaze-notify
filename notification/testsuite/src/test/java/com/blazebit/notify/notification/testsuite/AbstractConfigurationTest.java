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
import com.blazebit.notify.notification.channel.memory.MemoryChannel;
import com.blazebit.notify.notification.scheduler.timer.ExecutorServiceNotificationJobScheduler;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@RunWith(Parameterized.class)
public abstract class AbstractConfigurationTest<N extends Notification<T>, T extends NotificationMessage> {

    protected Channel<N, T> channel;
    protected NotificationJobScheduler jobScheduler;
    protected T defaultMessage;
    protected Queue<NotificationMessage> sink;

    public AbstractConfigurationTest(Channel<N, T> channel, NotificationJobScheduler jobScheduler, T defaultMessage, Queue<NotificationMessage> sink) {
        this.channel = channel;
        this.jobScheduler = jobScheduler;
        this.defaultMessage = defaultMessage;
        this.sink = sink;
    }

    @Parameterized.Parameters
    public static Object[][] createCombinations() {
        return createCombinations(null);
    }

    public static Object[][] createCombinations(NotificationJobProcessor jobProcessor) {
        Queue<NotificationMessage> sink;
        return new Object[][]{
                {new MemoryChannel(sink = new ArrayBlockingQueue<>(1024), jobProcessor), new ExecutorServiceNotificationJobScheduler(), new SimpleNotificationMessage(), sink}
        };
    }
}
