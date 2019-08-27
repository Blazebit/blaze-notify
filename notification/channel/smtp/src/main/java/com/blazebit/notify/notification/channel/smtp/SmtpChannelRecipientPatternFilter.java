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

import com.blazebit.notify.notification.email.message.EmailNotificationMessage;
import com.blazebit.notify.notification.email.message.EmailNotificationRecipient;
import com.sun.mail.smtp.SMTPMessage;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class SmtpChannelRecipientPatternFilter implements SmtpChannelFilter {

    private final List<Pattern> includes;
    private final List<Pattern> excludes;

    public SmtpChannelRecipientPatternFilter(List<Pattern> includes, List<Pattern> excludes) {
        this.includes = includes == null ? Collections.<Pattern>emptyList() : includes;
        this.excludes = excludes == null ? Collections.<Pattern>emptyList() : excludes;
    }

    @Override
    public boolean filterSmtpMessage(EmailNotificationRecipient<?> recipient, EmailNotificationMessage blazeNotifySmtpMessage, SMTPMessage constructedSmtpMessage) {
        String recipientEmail = recipient.getEmail();
        if (!includes.isEmpty()) {
            boolean included = false;
            for (int i = 0; i < includes.size(); i++) {
                if (includes.get(i).matcher(recipientEmail).matches()) {
                    included = true;
                }
            }
            if (!included) {
                return false;
            }
        }

        for (int i = 0; i < excludes.size(); i++) {
            if (excludes.get(i).matcher(recipientEmail).matches()) {
                return false;
            }
        }

        return true;
    }
}
