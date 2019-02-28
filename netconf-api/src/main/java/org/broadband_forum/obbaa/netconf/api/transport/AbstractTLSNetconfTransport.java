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

package org.broadband_forum.obbaa.netconf.api.transport;

import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.File;

/**
 * Created by kbhatk on 7/27/16.
 */
public abstract class AbstractTLSNetconfTransport extends NetconfTransport {
    private SslProvider m_sslProvider;
    private boolean m_selfSigned = true;
    private File m_trustChain;
    private File m_privateKey;
    private boolean m_clientAuthenticationNeeded;
    private File m_certificateChain;
    private String m_privateKeyPassword;
    private boolean m_tlsKeepAlive;
    private TrustManager m_trustManager;
    private KeyManager m_keyManager;
    private long m_tlsHandshakeTimeOutMillis;

    public AbstractTLSNetconfTransport(boolean clientAuthenticationNeeded, boolean tlsKeepAlive, File certificateChain, File privateKey,
                                       TrustManager trustManager, String privateKeyPassword, boolean selfSigned,
                                       KeyManager keyManager, File trustChain, SslProvider sslProvider, long tlsHandshaketimeoutMillis) {
        this.m_clientAuthenticationNeeded = clientAuthenticationNeeded;
        this.m_tlsKeepAlive = tlsKeepAlive;
        this.m_certificateChain = certificateChain;
        this.m_privateKey = privateKey;
        this.m_trustManager = trustManager;
        this.m_privateKeyPassword = privateKeyPassword;
        this.m_selfSigned = selfSigned;
        this.m_keyManager = keyManager;
        this.m_trustChain = trustChain;
        this.m_sslProvider = sslProvider;
        this.m_tlsHandshakeTimeOutMillis = tlsHandshaketimeoutMillis;
    }

    public boolean isClientAuthenticationNeeded() {
        return m_clientAuthenticationNeeded;
    }

    public File getPrivateKey() {
        return m_privateKey;
    }

    public void setPrivateKey(File privateKey) {
        this.m_privateKey = privateKey;
    }

    public File getTrustChain() {
        return m_trustChain;
    }

    public void setTrustChain(File clientTrustStore) {
        this.m_trustChain = clientTrustStore;
    }

    public boolean isSelfSigned() {
        return m_selfSigned;
    }

    public void setSelfSigned(boolean selfSigned) {
        this.m_selfSigned = selfSigned;
    }

    public File getCertificateChain() {
        return this.m_certificateChain;
    }

    public String getPrivateKeyPassword() {
        return m_privateKeyPassword;
    }

    public boolean isTlsKeepalive() {
        return m_tlsKeepAlive;
    }

    public TrustManager getTrustManager() {
        return m_trustManager;
    }

    public KeyManager getKeyManager() {
        return m_keyManager;
    }

    public SslProvider getSslProvider() {
        return m_sslProvider;
    }

    public void setSslProvider(SslProvider sslProvider) {
        m_sslProvider = sslProvider;
    }

    public long getTlsHandshakeTimeOutMillis() {
        return m_tlsHandshakeTimeOutMillis;
    }

    public void setTlsHandshakeTimeOutMillis(long tlsHandshakeTimeOutMillis) {
        m_tlsHandshakeTimeOutMillis = tlsHandshakeTimeOutMillis;
    }
}
