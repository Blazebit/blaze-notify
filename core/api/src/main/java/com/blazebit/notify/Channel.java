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

/**
 * A channel is a protocol specific implementation for sending notification messages.
 * The recipient and the message interfaces are dictated by the protocol.
 *
 * @param <R> The recipient type
 * @param <M> The message type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface Channel<R extends NotificationRecipient<?>, M extends NotificationMessage> extends AutoCloseable {

    /**
     * Returns the notification message type class.
     *
     * @return the notification message type class
     */
    Class<M> getNotificationMessageType();

    /**
     * Sends the given message to the given recipient.
     *
     * @param recipient The recipient
     * @param message The message
     * @return A send identifier to track the progress of the send or <code>null</code>
     * @throws com.blazebit.job.JobTemporaryException If there is a temporary issue with the channel
     * @throws com.blazebit.job.JobRateLimitException If the rate limit for the channel is reached
     * @throws NotificationException If there is any other issue with sending
     */
    Object sendNotificationMessage(R recipient, M message);

}
