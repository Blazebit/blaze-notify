/*
 * Copyright 2018 - 2025 Blazebit.
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

package com.blazebit.notify.memory.model;

import com.blazebit.job.memory.model.BaseEntity;
import com.blazebit.notify.NotificationRecipient;

import java.util.Locale;
import java.util.TimeZone;

/**
 * An abstract base class implementing the {@link NotificationRecipient} interface.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractNotificationRecipient extends BaseEntity<Long> implements NotificationRecipient<Long> {

    private Locale locale;
    private TimeZone timeZone;

    /**
     * Creates an empty notification recipient.
     */
    public AbstractNotificationRecipient() {
    }

    /**
     * Creates a notification recipient with the given id.
     *
     * @param id The notification recipient id
     */
    public AbstractNotificationRecipient(Long id) {
        super(id);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the given locale.
     *
     * @param locale The locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
