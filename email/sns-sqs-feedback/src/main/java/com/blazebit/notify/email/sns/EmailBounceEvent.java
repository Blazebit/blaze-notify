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
package com.blazebit.notify.email.sns;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * An E-Mail bounce event.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailBounceEvent extends EmailEvent {

    private static final long serialVersionUID = 1L;

    private final EmailEventBounceType bounceType;
    private final EmailEventBounceSubType bounceSubType;
    private final List<EmailEventBounceRecipient> bouncedRecipients;

    /**
     * Creates a new E-Mail bounce event.
     *
     * @param notificationType  The notification type
     * @param created           The creation time
     * @param messageId         The message id
     * @param bounceType        The bounce type
     * @param bounceSubType     The bounce subtype
     * @param bouncedRecipients The bounced recipients
     */
    public EmailBounceEvent(EmailEventNotificationType notificationType, ZonedDateTime created, String messageId, EmailEventBounceType bounceType, EmailEventBounceSubType bounceSubType, List<EmailEventBounceRecipient> bouncedRecipients) {
        super(notificationType, created, messageId);
        this.bounceType = bounceType;
        this.bounceSubType = bounceSubType;
        this.bouncedRecipients = bouncedRecipients;
    }

    /**
     * Returns the bounce type.
     *
     * @return the bounce type
     */
    public EmailEventBounceType getBounceType() {
        return bounceType;
    }

    /**
     * Returns the bounce sub type.
     *
     * @return the bounce sub type
     */
    public EmailEventBounceSubType getBounceSubType() {
        return bounceSubType;
    }

    /**
     * Returns the bounced recipients.
     *
     * @return the bounced recipients
     */
    public List<EmailEventBounceRecipient> getBouncedRecipients() {
        return bouncedRecipients;
    }
}
