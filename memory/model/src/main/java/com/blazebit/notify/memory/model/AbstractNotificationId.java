/*
 * Copyright 2018 - 2025 Blazebit.
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

package com.blazebit.notify.memory.model;

import java.io.Serializable;

/**
 * An abstract composite representing the composite id base class for {@link AbstractNotification}.
 *
 * @param <R> The notification recipient id type
 * @param <I> The notification job instance id type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractNotificationId<I, R> implements Serializable {

    private static final long serialVersionUID = 1L;

    private I notificationJobInstanceId;
    private R recipientId;

    /**
     * Creates an empty notification id.
     */
    public AbstractNotificationId() {
    }

    /**
     * Creates a notification id with the given notification job instance id and recipient id.
     *
     * @param notificationJobInstanceId The notification job instance id
     * @param recipientId               The recipient id
     */
    public AbstractNotificationId(I notificationJobInstanceId, R recipientId) {
        this.notificationJobInstanceId = notificationJobInstanceId;
        this.recipientId = recipientId;
    }

    /**
     * Returns the notification job instance id.
     *
     * @return the notification job instance id
     */
    public I getNotificationJobInstanceId() {
        return notificationJobInstanceId;
    }

    /**
     * Sets the given notification job instance id.
     *
     * @param notificationJobInstanceId The notification job instance id
     */
    public void setNotificationJobInstanceId(I notificationJobInstanceId) {
        this.notificationJobInstanceId = notificationJobInstanceId;
    }

    /**
     * Returns the recipient id.
     *
     * @return the recipient id
     */
    public R getRecipientId() {
        return recipientId;
    }

    /**
     * Sets the given recipient id.
     *
     * @param recipientId The recipient id
     */
    public void setRecipientId(R recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractNotificationId)) {
            return false;
        }

        AbstractNotificationId<?, ?> that = (AbstractNotificationId<?, ?>) o;

        if (getNotificationJobInstanceId() != null ? !getNotificationJobInstanceId().equals(that.getNotificationJobInstanceId()) : that.getNotificationJobInstanceId() != null) {
            return false;
        }
        return getRecipientId() != null ? getRecipientId().equals(that.getRecipientId()) : that.getRecipientId() == null;

    }

    @Override
    public int hashCode() {
        int result = getNotificationJobInstanceId() != null ? getNotificationJobInstanceId().hashCode() : 0;
        result = 31 * result + (getRecipientId() != null ? getRecipientId().hashCode() : 0);
        return result;
    }
}
