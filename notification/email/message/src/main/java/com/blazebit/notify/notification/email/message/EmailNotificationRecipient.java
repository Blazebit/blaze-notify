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
package com.blazebit.notify.notification.email.message;

import com.blazebit.notify.notification.NotificationRecipient;

import java.util.Locale;

public interface EmailNotificationRecipient<ID> extends NotificationRecipient<ID> {

    String getEmail();

    static <X> EmailNotificationRecipient<X> of(X id, Locale locale, String email) {
        return new EmailNotificationRecipient<X>() {
            @Override
            public X getId() {
                return id;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getEmail() {
                return email;
            }
        };
    }
}
