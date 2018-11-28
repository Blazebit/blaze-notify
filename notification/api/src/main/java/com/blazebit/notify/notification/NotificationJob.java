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
package com.blazebit.notify.notification;

import java.util.Map;

public interface NotificationJob<R extends NotificationReceiver, N extends Notification<R, N, T>, T extends NotificationMessage> {

    Channel<R, N, T> getChannel();

    NotificationJobProcessor<R, N, T> getJobProcessor();

    NotificationMessageResolver<R, N, T> getMessageResolver();

    Schedule getSchedule();

    Schedule getNotificationSchedule();

    NotificationReceiverResolver<R, N, T> getReceiverResolver();

    Map<String, Object> getJobParameters();
}
