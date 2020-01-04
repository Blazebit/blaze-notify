/*
 * Copyright 2018 - 2020 Blazebit.
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

import com.blazebit.notify.Notification;
import com.blazebit.notify.NotificationJobInstance;
import com.blazebit.notify.NotificationRecipient;

/**
 * An abstract base class implementing the {@link com.blazebit.notify.Notification} interface that is based on a {@link NotificationJobInstance}.
 *
 * @param <ID> The notification id type
 * @param <R>  The notification recipient type
 * @param <I>  The notification job instance type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractJobInstanceBasedNotification<ID extends AbstractNotificationId<?, ?>, R extends NotificationRecipient<?>, I extends NotificationJobInstance<?, ?>> extends AbstractNotification<ID> implements Notification<ID> {

    private static final long serialVersionUID = 1L;

    private R recipient;
    private I notificationJobInstance;

    /**
     * Creates a notification with the given id.
     *
     * @param id The notification id
     */
    public AbstractJobInstanceBasedNotification(ID id) {
        super(id);
    }

    @Override
    public R getRecipient() {
        return recipient;
    }

    /**
     * Sets the given recipient.
     *
     * @param recipient The recipient
     */
    public void setRecipient(R recipient) {
        this.recipient = recipient;
        if (recipient == null) {
            getId().setRecipientId(null);
        } else {
            ((AbstractNotificationId) getId()).setRecipientId(recipient.getId());
        }
    }

    /**
     * Returns the notification job instance.
     *
     * @return the notification job instance
     */
    public I getNotificationJobInstance() {
        return notificationJobInstance;
    }

    /**
     * Sets the given notification job instance.
     *
     * @param notificationJobInstance The notification job instance
     */
    public void setNotificationJobInstance(I notificationJobInstance) {
        this.notificationJobInstance = notificationJobInstance;
        if (notificationJobInstance == null) {
            getId().setNotificationJobInstanceId(null);
        } else {
            ((AbstractNotificationId) getId()).setNotificationJobInstanceId(notificationJobInstance.getId());
        }
    }

}
