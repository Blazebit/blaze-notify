/*
 * Copyright 2018 Blazebit.
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
package com.blazebit.notify.notification.channel.smtp;

import com.blazebit.notify.notification.NotificationMessage;

public class SmtpMessage implements NotificationMessage {
    private final String from;
    private final String fromDisplayName;
    private final String replyTo;
    private final String replyToDisplayName;
    private final String envelopeFrom;
    private final EmailSubject subject;
    private final EmailBody textBody;
    private final EmailBody htmlBody;

    public SmtpMessage(String from, String fromDisplayName, String replyTo, String replyToDisplayName, String envelopeFrom, EmailSubject subject, EmailBody textBody, EmailBody htmlBody) {
        this.from = from;
        this.fromDisplayName = fromDisplayName;
        this.replyTo = replyTo;
        this.replyToDisplayName = replyToDisplayName;
        this.envelopeFrom = envelopeFrom;
        this.subject = subject;
        this.textBody = textBody;
        this.htmlBody = htmlBody;
    }

    public SmtpMessage(String from, EmailSubject subject, EmailBody textBody, EmailBody htmlBody) {
        this(from, null, null, null, null, subject, textBody, htmlBody);
    }

    public String getFrom() {
        return from;
    }

    public String getFromDisplayName() {
        return fromDisplayName;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public String getReplyToDisplayName() {
        return replyToDisplayName;
    }

    public String getEnvelopeFrom() {
        return envelopeFrom;
    }

    public EmailSubject getSubject() {
        return subject;
    }

    public EmailBody getTextBody() {
        return textBody;
    }

    public EmailBody getHtmlBody() {
        return htmlBody;
    }
}
