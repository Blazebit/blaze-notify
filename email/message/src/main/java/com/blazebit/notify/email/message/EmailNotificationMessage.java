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
package com.blazebit.notify.email.message;

import com.blazebit.notify.NotificationMessage;

import java.util.Collection;
import java.util.Collections;

/**
 * The E-Mail notification message.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailNotificationMessage implements NotificationMessage {
    private final String from;
    private final String fromDisplayName;
    private final String replyTo;
    private final String replyToDisplayName;
    private final String envelopeFrom;
    private final EmailSubject subject;
    private final EmailBody textBody;
    private final EmailBody htmlBody;
    private final Collection<Attachment> attachments;

    /**
     * Creates a new E-Mail notification message.
     *
     * @param from               The from address
     * @param fromDisplayName    The from display name
     * @param replyTo            The reply to address
     * @param replyToDisplayName The reply to display name
     * @param envelopeFrom       The envelop from address
     * @param subject            The E-Mail subject
     * @param textBody           The E-Mail text body
     * @param htmlBody           The E-Mail html body
     * @param attachments        The E-Mail attachments
     */
    public EmailNotificationMessage(String from, String fromDisplayName, String replyTo, String replyToDisplayName, String envelopeFrom, EmailSubject subject, EmailBody textBody, EmailBody htmlBody, Collection<Attachment> attachments) {
        this.from = from;
        this.fromDisplayName = fromDisplayName;
        this.replyTo = replyTo;
        this.replyToDisplayName = replyToDisplayName;
        this.envelopeFrom = envelopeFrom;
        this.subject = subject;
        this.textBody = textBody;
        this.htmlBody = htmlBody;
        this.attachments = attachments;
    }

    /**
     * Creates a new E-Mail notification message.
     *
     * @param from     The from address
     * @param subject  The E-Mail subject
     * @param textBody The E-Mail text body
     */
    public EmailNotificationMessage(String from, String subject, String textBody) {
        this(from, subject, textBody, null);
    }

    /**
     * Creates a new E-Mail notification message.
     *
     * @param from     The from address
     * @param subject  The E-Mail subject
     * @param textBody The E-Mail text body
     * @param htmlBody The E-Mail html body
     */
    public EmailNotificationMessage(String from, String subject, String textBody, String htmlBody) {
        this(from, new EmailSubject(subject), new EmailBody(textBody), htmlBody == null ? null : new EmailBody(htmlBody));
    }

    /**
     * Creates a new E-Mail notification message.
     *
     * @param from     The from address
     * @param subject  The E-Mail subject
     * @param textBody The E-Mail text body
     * @param htmlBody The E-Mail html body
     */
    public EmailNotificationMessage(String from, EmailSubject subject, EmailBody textBody, EmailBody htmlBody) {
        this(from, null, null, null, null, subject, textBody, htmlBody, Collections.<Attachment>emptyList());
    }

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the from display name.
     *
     * @return the from display name
     */
    public String getFromDisplayName() {
        return fromDisplayName;
    }

    /**
     * Returns the reply to address.
     *
     * @return the reply to address
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Returns the reply to display name.
     *
     * @return the reply to display name
     */
    public String getReplyToDisplayName() {
        return replyToDisplayName;
    }

    /**
     * Returns the envelop from address.
     *
     * @return the envelop from address
     */
    public String getEnvelopeFrom() {
        return envelopeFrom;
    }

    /**
     * Returns the subject.
     *
     * @return the subject
     */
    public EmailSubject getSubject() {
        return subject;
    }

    /**
     * Returns the text body.
     *
     * @return the text body
     */
    public EmailBody getTextBody() {
        return textBody;
    }

    /**
     * Returns the html body.
     *
     * @return the html body
     */
    public EmailBody getHtmlBody() {
        return htmlBody;
    }

    /**
     * Returns the attachments.
     *
     * @return the attachments
     */
    public Collection<Attachment> getAttachments() {
        return attachments;
    }
}
