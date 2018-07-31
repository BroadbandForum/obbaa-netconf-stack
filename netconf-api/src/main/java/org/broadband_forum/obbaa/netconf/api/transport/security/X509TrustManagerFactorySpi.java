/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.api.transport.security;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * A X509 TrustManagerFactory Service Provider which lets the users supply an implementation of {@link TrustManager}
 *
 * @author keshava
 */
public class X509TrustManagerFactorySpi extends TrustManagerFactorySpi {

    private TrustManager m_trustManager;

    public X509TrustManagerFactorySpi(TrustManager trustManager) {
        this.m_trustManager = trustManager;
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return new TrustManager[]{m_trustManager};
    }

    @Override
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        // We let the trust manager manager his keys on his own, nothing to be done here

    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        // We let the trust manager manager his keys on his own, nothing to be done here
    }

}
