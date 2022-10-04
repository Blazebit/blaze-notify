/*
 * Copyright 2018 - 2022 Blazebit.
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
 * An E-Mail delivery event.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailDeliveryEvent extends EmailEvent {

    private static final long serialVersionUID = 1L;

    private final List<String> recipients;

    /**
     * Creates a new E-Mail delivery event.
     *
     * @param notificationType The notification type
     * @param created          The creation time
     * @param messageId        The message id
     * @param recipients       The recipients
     */
    public EmailDeliveryEvent(EmailEventNotificationType notificationType, ZonedDateTime created, String messageId, List<String> recipients) {
        super(notificationType, created, messageId);
        this.recipients = recipients;
    }

    /**
     * Returns the recipients.
     *
     * @return the recipients
     */
    public List<String> getRecipients() {
        return recipients;
    }
}
