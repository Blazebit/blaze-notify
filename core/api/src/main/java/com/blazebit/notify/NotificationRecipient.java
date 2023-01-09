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
package com.blazebit.notify;

import java.util.Locale;

/**
 * A base type for a notification recipient.
 *
 * @param <ID> The id type of the notification recipient
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface NotificationRecipient<ID> {

    /**
     * Returns the identifier for this notification recipient.
     *
     * @return the identifier for this notification recipient
     */
    ID getId();

    /**
     * Returns the locale for this notification recipient.
     *
     * @return the locale for this notification recipient
     */
    Locale getLocale();

    /**
     * Returns a simple notification recipient with the given identifier and locale.
     *
     * @param id The notification recipient identifier
     * @param locale The notification recipient locale
     * @param <X> The notification recipient identifier type
     * @return a simple notification recipient
     */
    static <X> NotificationRecipient<X> of(X id, Locale locale) {
        return new NotificationRecipient<X>() {
            @Override
            public X getId() {
                return id;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }
        };
    }
}
