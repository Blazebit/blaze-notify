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
package com.blazebit.notify.email.message;

import com.blazebit.notify.NotificationMessagePart;

/**
 * The E-Mail body message part.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailBody implements NotificationMessagePart {
    private final String body;

    /**
     * Creates a new E-Mail body.
     *
     * @param body The body
     */
    public EmailBody(String body) {
        this.body = body;
    }

    /**
     * Returns the E-Mail body as string.
     *
     * @return the E-Mail body as string
     */
    public String getBody() {
        return body;
    }
}
