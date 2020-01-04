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
package com.blazebit.notify.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * A {@link TruststoreProvider} based configurator for creating a {@link javax.net.ssl.SSLSocketFactory}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class JSSETruststoreConfigurator {
    private TruststoreProvider provider;
    private volatile javax.net.ssl.SSLSocketFactory sslFactory;
    private volatile TrustManager[] tm;

    /**
     * Creates a trust store provider based configurator.
     *
     * @param provider The trust store provider
     */
    public JSSETruststoreConfigurator(TruststoreProvider provider) {
        this.provider = provider;
    }

    /**
     * Returns the {@link javax.net.ssl.SSLSocketFactory} configured with trust managers.
     *
     * @return the {@link javax.net.ssl.SSLSocketFactory}
     */
    public javax.net.ssl.SSLSocketFactory getSSLSocketFactory() {
        if (provider == null) {
            return null;
        }

        if (sslFactory == null) {
            synchronized (this) {
                if (sslFactory == null) {
                    try {
                        SSLContext sslctx = SSLContext.getInstance("TLS");
                        sslctx.init(null, getTrustManagers(), null);
                        sslFactory = sslctx.getSocketFactory();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize SSLContext: ", e);
                    }
                }
            }
        }
        return sslFactory;
    }

    /**
     * Returns the trust managers for the configured trust store provider.
     *
     * @return the trust managers
     */
    public TrustManager[] getTrustManagers() {
        if (provider == null) {
            return null;
        }

        if (tm == null) {
            synchronized (this) {
                if (tm == null) {
                    TrustManagerFactory tmf;
                    try {
                        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        tmf.init(provider.getTruststore());
                        tm = tmf.getTrustManagers();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize TrustManager: ", e);
                    }
                }
            }
        }
        return tm;
    }

    /**
     * Returns the trust store provider.
     *
     * @return the trust store provider
     */
    public TruststoreProvider getProvider() {
        return provider;
    }
}
