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
package com.blazebit.notify.email.sns;

import java.util.HashMap;
import java.util.Map;

/**
 * The E-Mail complaint feedback type.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public enum EmailEventComplaintFeedbackType {

    /**
     * Abuse complaint.
     */
    ABUSE("abuse"),
    /**
     * Auth-failure complaint.
     */
    AUTH_FAILURE("auth-failure"),
    /**
     * Fraud complaint.
     */
    FRAUD("fraud"),
    /**
     * Not-Spam complaint.
     */
    NOT_SPAM("not-spam"),
    /**
     * Virus complaint.
     */
    VIRUS("virus"),
    /**
     * Other complaint.
     */
    OTHER("other");

    private static final Map<String, EmailEventComplaintFeedbackType> CACHE;

    private final String snsValue;

    /**
     * Create the type.
     *
     * @param snsValue The SNS value
     */
    EmailEventComplaintFeedbackType(String snsValue) {
        this.snsValue = snsValue;
    }

    static {
        EmailEventComplaintFeedbackType[] values = values();
        Map<String, EmailEventComplaintFeedbackType> cache = new HashMap<>(values.length);
        for (EmailEventComplaintFeedbackType value : values) {
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
    public static EmailEventComplaintFeedbackType fromString(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty complaint type");
        }

        return CACHE.get(s);
    }

}
