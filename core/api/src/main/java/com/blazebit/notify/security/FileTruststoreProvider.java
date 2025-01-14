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
package com.blazebit.notify.security;

import java.security.KeyStore;

/**
 * A simple trust store provider POJO.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class FileTruststoreProvider implements TruststoreProvider {

    private final HostnameVerificationPolicy policy;
    private final KeyStore truststore;

    /**
     * Creates a trust store provider from the given trust store and policy.
     *
     * @param truststore The keystore
     * @param policy The host name verification policy
     */
    FileTruststoreProvider(KeyStore truststore, HostnameVerificationPolicy policy) {
        this.policy = policy;
        this.truststore = truststore;
    }

    @Override
    public HostnameVerificationPolicy getPolicy() {
        return policy;
    }

    @Override
    public KeyStore getTruststore() {
        return truststore;
    }
}
