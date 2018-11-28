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

import java.util.Map;

public class SimpleNotificationJob<R extends NotificationReceiver> implements NotificationJob<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> {

    private final Channel<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> channel;
    private final NotificationJobProcessor<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> jobProcessor;
    private final NotificationMessageResolver<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> messageResolver;
    private final Schedule schedule;
    private final Schedule notificationSchedule;
    private final NotificationReceiverResolver<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> receiverResolver;
    private final Map<String, Object> jobParameters;

    public SimpleNotificationJob(
            Channel<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> channel,
            NotificationJobProcessor<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> jobProcessor, NotificationMessageResolver<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> messageResolver,
            Schedule schedule,
            Schedule notificationSchedule,
            NotificationReceiverResolver<R, SimpleNotification<R, SimpleNotificationMessage>,
            SimpleNotificationMessage> receiverResolver, Map<String, Object> jobParameters) {
        this.channel = channel;
        this.jobProcessor = jobProcessor;
        this.messageResolver = messageResolver;
        this.schedule = schedule;
        this.notificationSchedule = notificationSchedule;
        this.receiverResolver = receiverResolver;
        this.jobParameters = jobParameters;
    }

    @Override
    public Channel<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> getChannel() {
        return channel;
    }

    @Override
    public NotificationJobProcessor<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> getJobProcessor() {
        return jobProcessor;
    }

    @Override
    public NotificationMessageResolver<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> getMessageResolver() {
        return messageResolver;
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public Schedule getNotificationSchedule() {
        return notificationSchedule;
    }

    @Override
    public NotificationReceiverResolver<R, SimpleNotification<R, SimpleNotificationMessage>, SimpleNotificationMessage> getReceiverResolver() {
        return receiverResolver;
    }

    @Override
    public Map<String, Object> getJobParameters() {
        return jobParameters;
    }
}
