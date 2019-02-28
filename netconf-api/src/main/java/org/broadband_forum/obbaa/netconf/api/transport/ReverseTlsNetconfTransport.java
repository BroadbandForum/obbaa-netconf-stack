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

import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.net.SocketAddress;

public class ReverseTlsNetconfTransport extends AbstractTLSNetconfTransport{
    private String m_callHomeIp;
    private Integer m_callHomePort;
    private CallHomeListener m_callHomeListener;
    private SocketAddress m_localAddress;

    public ReverseTlsNetconfTransport(String callHomeIp, Integer callHomePort, CallHomeListener callHomeListener, boolean selfSigned,
                                      File trustChain, TrustManager trustManager, File certificateChain, File privateKey, String privateKeyPassword,
                                      KeyManager keyManager, SocketAddress localAddress, boolean clientAuthenticationNeeded,
                                      boolean tlsKeepAlive, SslProvider sslProvider, long tlsHandshaketimeoutMillis) {
        super(clientAuthenticationNeeded, tlsKeepAlive, certificateChain, privateKey, trustManager, privateKeyPassword, selfSigned,
                keyManager, trustChain, sslProvider, tlsHandshaketimeoutMillis);
        this.m_callHomeIp = callHomeIp;
        this.m_callHomePort = callHomePort;
        this.m_callHomeListener = callHomeListener;
        this.m_localAddress = localAddress;
    }

    @Override
    public String getTranportProtocol() {
        return NetconfTransportProtocol.REVERSE_TLS.name();
    }

    public String getCallHomeIp() {
        return m_callHomeIp;
    }

    public Integer getCallHomePort() {
        return m_callHomePort;
    }

    public CallHomeListener getCallHomeListener() {
        return m_callHomeListener;
    }

    public SocketAddress getLocalAddress() {
        return m_localAddress;
    }
}
