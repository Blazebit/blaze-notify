/*
 * Copyright 2018 - 2023 Blazebit.
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
package com.blazebit.notify;

import com.blazebit.job.JobInstance;

/**
 * A notification for a specific channel type and a specific recipient.
 *
 * @param <ID> The id type of the notification
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface Notification<ID> extends JobInstance<ID> {

    /**
     * Returns the channel type identifier that should match a registered channel {@link ChannelKey#getChannelType()}.
     *
     * @return the channel type identifier
     */
    String getChannelType();

    /**
     * Returns the recipient to use for sending the notification.
     *
     * @return The recipient
     */
    NotificationRecipient<?> getRecipient();

}
