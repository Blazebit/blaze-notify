/*
 * Copyright 2018 - 2019 Blazebit.
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

import com.blazebit.notify.NotificationRecipient;

import java.util.Locale;

/**
 * A base type for a E-Mail notification recipient.
 *
 * @param <ID> The id type of the notification recipient
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface EmailNotificationRecipient<ID> extends NotificationRecipient<ID> {

    /**
     * Returns the E-Mail address of the notification recipient.
     *
     * @return the E-Mail address of the notification recipient
     */
    String getEmail();

    /**
     * Returns a E-Mail notification recipient with the given identifier, locale and E-Mail address.
     *
     * @param id     The notification recipient identifier
     * @param locale The notification recipient locale
     * @param email  The notification recipient E-Mail address
     * @param <X>    The notification recipient identifier type
     * @return a simple notification recipient
     */
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
