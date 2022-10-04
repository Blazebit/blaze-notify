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
package com.blazebit.notify.email.model.memory;

import com.blazebit.notify.email.sns.EmailBounceEvent;
import com.blazebit.notify.email.sns.EmailComplaintEvent;
import com.blazebit.notify.email.sns.EmailEvent;
import com.blazebit.notify.email.sns.EmailEventBounceRecipient;

import javax.json.Json;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * An event handler for E-Mail feedback events.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailEventHandler {

    /**
     * Handles the given {@link EmailEvent} of the given notification and returns the suppressed E-Mail addresses.
     *
     * @param event             The E-Mail feedback event
     * @param emailNotification The notification
     * @return the suppressed E-Mail addresses
     */
    public List<String> onEmailEvent(EmailEvent event, AbstractEmailNotification<?> emailNotification) {
        emailNotification.setDeliveredTime(Instant.now());

        List<String> suppressedEmails = new ArrayList<String>();

        switch (event.getNotificationType()) {
            case BOUNCE:
                EmailBounceEvent bounceEvent = (EmailBounceEvent) event;

                switch (bounceEvent.getBounceType()) {
                    case TRANSIENT:
                        // Manually review bounce
                        emailNotification.setReviewState(EmailNotificationReviewState.NECESSARY);
                        break;
                    default:
                        // Just set status and suppress emails
                        for (EmailEventBounceRecipient recipient : bounceEvent.getBouncedRecipients()) {
                            suppressedEmails.add(recipient.getEmailAddress());
                        }
                        break;
                }

                emailNotification.setDeliveryState(EmailNotificationDeliveryState.BOUNCED);

                emailNotification.setDeliveryNotification(Json.createObjectBuilder()
                                                              .add("bounceType", bounceEvent.getBounceType().toString())
                                                              .add("bounceSubType", bounceEvent.getBounceSubType().toString())
                                                              .build()
                                                              .toString());
                break;
            case COMPLAINT:
                EmailComplaintEvent complaintEvent = (EmailComplaintEvent) event;
                suppressedEmails.addAll(complaintEvent.getComplainedRecipients());
                emailNotification.setDeliveryState(EmailNotificationDeliveryState.COMPLAINED);

                emailNotification.setDeliveryNotification(Json.createObjectBuilder()
                                                              .add("complaintFeedbackType", complaintEvent.getComplaintFeedbackType().toString())
                                                              .add("userAgent", complaintEvent.getUserAgent())
                                                              .build()
                                                              .toString());
                break;
            case DELIVERY:
                emailNotification.setDeliveryState(EmailNotificationDeliveryState.DELIVERED);
                break;
            default:
                break;
        }

        return suppressedEmails;
    }

}
