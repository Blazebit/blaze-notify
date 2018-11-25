/*
 * Copyright 2018 Blazebit.
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
package com.blazebit.notify.notification.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.logging.Logger;

public abstract class AbstractFileTruststoreProviderFactory implements TruststoreProviderFactory<FileTruststoreProvider> {

    private static final Logger LOG = Logger.getLogger(AbstractFileTruststoreProviderFactory.class.getName());

    private FileTruststoreProvider fileTruststoreProvider;

    protected abstract String getStorePath();
    protected abstract String getStorePassword();
    protected abstract HostnameVerificationPolicy getHostnameVerificationPolicy();

    @Override
    public FileTruststoreProvider create() {
        if (fileTruststoreProvider == null) {
            fileTruststoreProvider = init();
        }
        return fileTruststoreProvider;
    }

    private FileTruststoreProvider init() {
        String storepath = getStorePath();
        String pass = getStorePassword();
        HostnameVerificationPolicy verificationPolicy = getHostnameVerificationPolicy();

        if (storepath == null) {
            throw new RuntimeException("Attribute 'file' missing in 'truststore':'file' configuration");
        }
        if (pass == null) {
            throw new RuntimeException("Attribute 'password' missing in 'truststore':'file' configuration");
        }

        KeyStore truststore;
        try {
            truststore = loadStore(storepath, pass.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TruststoreProviderFactory: " + new File(storepath).getAbsolutePath(), e);
        }

        FileTruststoreProvider provider = new FileTruststoreProvider(truststore, verificationPolicy);
        LOG.finest("File trustore provider initialized: " + new File(storepath).getAbsolutePath());

        return provider;
    }

    private KeyStore loadStore(String path, char[] password) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream is = new FileInputStream(path)) {
            ks.load(is, password);
            return ks;
        }
    }
}
