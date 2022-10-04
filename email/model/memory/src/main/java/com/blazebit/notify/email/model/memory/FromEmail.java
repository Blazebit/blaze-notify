/*
 * Copyright 2018 - 2022 Blazebit.
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
package com.blazebit.notify.email.model.memory;

/**
 * A model for the from address.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class FromEmail {

    private String email;
    private String name;
    private String replyToEmail;
    private String replyToName;

    /**
     * Creates an empty {@link FromEmail} entity.
     */
    public FromEmail() {
    }

    /**
     * Creates {@link FromEmail} entity for the given E-Mail address.
     *
     * @param email The E-Mail address
     */
    public FromEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the E-Mail address.
     *
     * @return the E-Mail address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the E-Mail address.
     *
     * @param email The E-Mail address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the display name.
     *
     * @return the display name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name.
     *
     * @param name The display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the reply to E-Mail address.
     *
     * @return the reply to E-Mail address
     */
    public String getReplyToEmail() {
        return replyToEmail;
    }

    /**
     * Sets the reply to E-Mail address.
     *
     * @param replyToEmail The reply to E-Mail address
     */
    public void setReplyToEmail(String replyToEmail) {
        this.replyToEmail = replyToEmail;
    }

    /**
     * Returns the reply to display name.
     *
     * @return the reply to display name
     */
    public String getReplyToName() {
        return replyToName;
    }

    /**
     * Sets the reply to display name.
     *
     * @param replyToName The reply to display name
     */
    public void setReplyToName(String replyToName) {
        this.replyToName = replyToName;
    }
}
