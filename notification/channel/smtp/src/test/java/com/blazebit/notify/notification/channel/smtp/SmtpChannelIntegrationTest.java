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

import org.junit.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SmtpChannelIntegrationTest extends AbstractSmtpChannelIntegrationTest {

    @Test
    public void simpleSendSmtpNotification() throws MessagingException, InterruptedException, IOException {
        String from = "no-reply@localhost";
        String subject = "test subject";
        String textBody = "test body";
        String attachmentName = "attachment";
        String attachmentContent = "attachment content";
        String expectedContentTypePrefix = "multipart/alternative";
        String attachmentMimeType = "text/plain; charset=UTF-8";
        List<Attachment> attachments = Collections.singletonList(new Attachment(attachmentName, new ByteArrayDataSource(attachmentContent, attachmentMimeType)));
        smtpChannel.sendNotificationMessage(
                new DefaultSmtpNotificationRecipient(TEST_MAIL_USER),
                new SmtpNotificationMessage(from, null, null, null, null, new EmailSubject(subject), new EmailBody(textBody), null, attachments)
        );

        Message[] receivedEmails = mailClient.awaitIncomingEmails(5000, 1);
        assertEquals(1, receivedEmails.length);
        Message msg = receivedEmails[0];
        assertEquals(from, msg.getFrom()[0].toString());
        assertTrue(msg.getContentType().toLowerCase().startsWith(expectedContentTypePrefix));
        assertEquals(subject, msg.getSubject());
        assertTrue(msg.getContent() instanceof MimeMultipart);
        MimeMultipart msgContent = (MimeMultipart) msg.getContent();
        assertEquals(textBody, ((String) msgContent.getBodyPart(0).getContent()).trim());
        assertTrue(msgContent.getBodyPart(1).getContentType().contains("name=" + attachmentName));
        assertEquals(attachmentContent, msgContent.getBodyPart(1).getContent());
    }
}
