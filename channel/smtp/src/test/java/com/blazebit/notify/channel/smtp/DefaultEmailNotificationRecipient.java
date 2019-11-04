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
package com.blazebit.notify.channel.smtp;

import com.blazebit.notify.email.message.EmailNotificationRecipient;

import java.util.Locale;
import java.util.Objects;

public class DefaultEmailNotificationRecipient<ID> implements EmailNotificationRecipient<ID> {

    private final ID id;
    private final String email;

    public DefaultEmailNotificationRecipient(ID id, String email) {
        this.id = id;
        this.email = email;
    }

    public DefaultEmailNotificationRecipient(String email) {
        this.id = null;
        this.email = email;
    }

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultEmailNotificationRecipient that = (DefaultEmailNotificationRecipient) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
