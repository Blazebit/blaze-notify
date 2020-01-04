/*
 * Copyright 2018 - 2020 Blazebit.
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
package com.blazebit.notify.email.sns;

import java.util.HashMap;
import java.util.Map;

/**
 * The E-Mail bounce subtype.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public enum EmailEventBounceSubType {

    /**
     * The bounce type is undetermined.
     */
    UNDETERMINED("Undetermined"),
    // Permanent
    /**
     * A general bounce.
     */
    GENERAL("General"),
    /**
     * No E-Mail.
     */
    NO_EMAIL("NoEmail"),
    /**
     * The E-Mail address is suppressed.
     */
    SUPPRESSED("Suppressed"),

    // Transient
    /**
     * The mailbox of the recipient is full.
     */
    MAILBOX_FULL("MailboxFull"),
    /**
     * The message is too large.
     */
    MESSAGE_TOO_LARGE("MessageTooLarge"),
    /**
     * The content was rejected.
     */
    CONTENT_REJECTED("ContentRejected"),
    /**
     * The attachment was rejected.
     */
    ATTACHMENT_REJECTED("AttachmentRejected");

    private static final Map<String, EmailEventBounceSubType> CACHE;

    private final String snsValue;

    /**
     * Create the type.
     *
     * @param snsValue The SNS value
     */
    EmailEventBounceSubType(String snsValue) {
        this.snsValue = snsValue;
    }

    static {
        EmailEventBounceSubType[] values = values();
        Map<String, EmailEventBounceSubType> cache = new HashMap<>(values.length);
        for (EmailEventBounceSubType value : values) {
            cache.put(value.snsValue, value);
        }
        CACHE = cache;
    }

    /**
     * Returns the type by string.
     *
     * @param s The string
     * @return the type
     */
    public static EmailEventBounceSubType fromString(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty bounce sub type");
        }

        return CACHE.get(s);
    }

}
