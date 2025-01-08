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
 * An E-Mail complaint event.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailComplaintEvent extends EmailEvent {

    private static final long serialVersionUID = 1L;

    private final EmailEventComplaintFeedbackType complaintFeedbackType;
    private final List<String> complainedRecipients;
    private final String userAgent;

    /**
     * Creates a new E-Mail complaint event.
     *
     * @param notificationType      The notification type
     * @param created               The creation time
     * @param messageId             The message id
     * @param complaintFeedbackType The complaint feedback type
     * @param complainedRecipients  The complained recipients
     * @param userAgent             The user agent
     */
    public EmailComplaintEvent(EmailEventNotificationType notificationType, ZonedDateTime created, String messageId, EmailEventComplaintFeedbackType complaintFeedbackType, List<String> complainedRecipients, String userAgent) {
        super(notificationType, created, messageId);
        this.complaintFeedbackType = complaintFeedbackType;
        this.complainedRecipients = complainedRecipients;
        this.userAgent = userAgent;
    }

    /**
     * Returns the complaint feedback type.
     *
     * @return the complaint feedback type
     */
    public EmailEventComplaintFeedbackType getComplaintFeedbackType() {
        return complaintFeedbackType;
    }

    /**
     * Returns the complained recipients.
     *
     * @return the complained recipients
     */
    public List<String> getComplainedRecipients() {
        return complainedRecipients;
    }

    /**
     * Returns the user agent.
     *
     * @return the user agent
     */
    public String getUserAgent() {
        return userAgent;
    }
}
