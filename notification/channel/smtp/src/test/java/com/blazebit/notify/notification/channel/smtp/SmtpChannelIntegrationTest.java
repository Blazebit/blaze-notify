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
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SmtpChannelIntegrationTest extends AbstractSmtpChannelIntegrationTest {

    @Test
    public void simpleSendSmtpNotification() throws MessagingException, InterruptedException, IOException {
        String from = "no-reply@localhost";
        String subject = "test subject";
        String textBody = "test body";
        String expectedContentType = "text/plain; charset=UTF-8";
        smtpChannel.sendNotificationMessage(new SmtpNotificationReceiver(TEST_MAIL_USER), new SmtpMessage(from, new EmailSubject(subject), new EmailBody(textBody), null));

        Message[] receivedEmails = mailClient.awaitIncomingEmails(5000, 1);
        assertEquals(1, receivedEmails.length);
        assertEquals(from, receivedEmails[0].getFrom()[0].toString());
        assertEquals(expectedContentType.toLowerCase(), receivedEmails[0].getContentType().toLowerCase());
        assertEquals(subject, receivedEmails[0].getSubject());
        assertEquals(textBody, ((String) receivedEmails[0].getContent()).trim());
    }
}
