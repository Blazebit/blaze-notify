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
package com.blazebit.notify.email.sns;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * An E-Mail event.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EmailEventNotificationType notificationType;
    private final ZonedDateTime created;
    private final String messageId;

    /**
     * Creates a new E-Mail event.
     *
     * @param notificationType The notification type
     * @param created          The creation time
     * @param messageId        The message id
     */
    public EmailEvent(EmailEventNotificationType notificationType, ZonedDateTime created, String messageId) {
        this.notificationType = notificationType;
        this.created = created;
        this.messageId = messageId;
    }

    /**
     * Returns the notification type.
     *
     * @return the notification type
     */
    public EmailEventNotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * Returns the creation time.
     *
     * @return the creation time
     */
    public ZonedDateTime getCreated() {
        return created;
    }

    /**
     * Returns the message id.
     *
     * @return the message id
     */
    public String getMessageId() {
        return messageId;
    }

}
