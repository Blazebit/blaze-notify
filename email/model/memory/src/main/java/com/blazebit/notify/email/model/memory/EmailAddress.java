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
 * An model for the state of E-Mail addresses.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EmailAddress {

    private static final long serialVersionUID = 1L;

    /**
     * Whether the current email address is verified.
     */
    private Boolean verified = false;
    /**
     * Email addresses can be suppressed when emails to the address bounce or
     * complaints are received.
     */
    private Boolean suppressed = false;

    /**
     * Creates an empty E-Mail address.
     */
    public EmailAddress() {
    }

    /**
     * Creates an E-Mail address.
     *
     * @param email      The E-Mail address
     * @param verified   Whether the E-Mail address is verified
     * @param suppressed Whether the E-Mail address is suppressed
     */
    public EmailAddress(String email, Boolean verified, Boolean suppressed) {
        this.verified = verified;
        this.suppressed = suppressed;
    }

    /**
     * Create a verified E-Mail address.
     *
     * @param address The E-Mail address
     * @return a verified E-Mail address
     */
    public static EmailAddress verified(String address) {
        return new EmailAddress(address, true, false);
    }

    /**
     * Create a unverified E-Mail address.
     *
     * @param address The E-Mail address
     * @return a unverified E-Mail address
     */
    public static EmailAddress unverified(String address) {
        return new EmailAddress(address, false, false);
    }

    /**
     * Returns whether the E-Mail address is verified.
     *
     * @return whether the E-Mail address is verified
     */
    public Boolean getVerified() {
        return verified;
    }

    /**
     * Sets whether the E-Mail address is verified.
     *
     * @param verified Whether the E-Mail address is verified
     */
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    /**
     * Returns whether the E-Mail address is suppressed.
     *
     * @return whether the E-Mail address is suppressed
     */
    public Boolean getSuppressed() {
        return suppressed;
    }

    /**
     * Sets whether the E-Mail address is suppressed.
     *
     * @param suppressed Whether the E-Mail address is suppressed
     */
    public void setSuppressed(Boolean suppressed) {
        this.suppressed = suppressed;
    }


}
