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

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;

import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NetconfTransportOrder {
    String m_transportType;
    SslProvider m_sslProvider = SslProvider.JDK;
    // TLS stuff
    private boolean m_allowSelfSigned = true;
    private File m_trustChain;
    private File m_certiChain;
    private File m_privateKey;
    private String m_ipForTlsTransport;
    private int m_portIdForTlsTrasnport = -1;

    // SSH stuff
    private String m_serverSshHostKeyPath;
    InetSocketAddress m_serverSocketAddress;

    private String m_callHomeIp;
    private Integer m_callHomePort;

    private CallHomeListener m_callHomeListener;
    private SocketAddress m_localAddress;
    private boolean m_clientAuthenticationNeeded = false;

    private String m_privateKeyPassword;

    private int m_heartbeatIntervalSecs;

    private boolean m_tlsKeepalive;

    private TrustManager m_trustManager;

    private KeyManager m_keyManager;
    private long m_tlsHandshaketimeoutMillis = 20000;

    public NetconfTransportOrder() {

    }

    public String getTransportType() {
        return m_transportType;
    }

    public void setTransportType(String transportType) {
        if (transportType != null) {
            transportType = transportType.trim();
        }
        this.m_transportType = transportType;
    }

    public InetSocketAddress getServerSocketAddress() {
        return m_serverSocketAddress;
    }

    public void setServerSocketAddress(InetSocketAddress socketAddress) {
        this.m_serverSocketAddress = socketAddress;
    }

    public int getPortIdForTlsTransport() {
        return m_portIdForTlsTrasnport;
    }

    public void setPortIdForTlsTransport(int portId) {
        this.m_portIdForTlsTrasnport = portId;
    }

    public String getServerSshHostKeyPath() {
        return this.m_serverSshHostKeyPath;
    }

    public void setServerSshHostKeyPath(String sshHostKeyPath) {
        if (sshHostKeyPath != null) {
            sshHostKeyPath = sshHostKeyPath.trim();
        }
        this.m_serverSshHostKeyPath = sshHostKeyPath;
    }

    public boolean validate() throws NetconfConfigurationBuilderException {
        StringBuilder errorBuilder = new StringBuilder();
        boolean error = false;

        if (NetconfTransportProtocol.REVERSE_TLS.name().equals(m_transportType)) {
            if (isAllowSelfSigned() && (getTrustManager() != null || getKeyManager() != null || getCertificateChain()
                    != null
                    || getPrivateKey() != null || getPrivateKeyPassword() != null)) {
                error = true;
                errorBuilder.append("Cannot use Self signed mode with trust/key manager OR certificate/private key " +
                        "files");
            }
        }

        if (NetconfTransportProtocol.SSH.name().equals(m_transportType)) {
            if (getServerSocketAddress() == null) {
                error = true;
                errorBuilder.append("server socket address is null ");
            }
        } else if (NetconfTransportProtocol.REVERSE_TLS.name().equals(m_transportType)) {
            if (getCallHomeIp() == null) {
                error = true;
                errorBuilder.append("Call Home IP address is null ");
            }
        } else {
            throw new NetconfConfigurationBuilderException("Invalid transport type : " + this.toString() +
                    errorBuilder.toString());
        }
        if (error) {
            throw new NetconfConfigurationBuilderException("Invalid options " + errorBuilder.toString());
        }
        return true;
    }

    public File getPrivateKey() {
        return m_privateKey;
    }

    /**
     * @param privateKey an X.509 certificate chain file in PEM format. pass null if you are using a
     * {@link KeyManager} via
     *                   {@link NetconfTransportOrder#setKeyManager(KeyManager)}
     * @return
     * @see NetconfTransportOrder#setKeyManager(KeyManager)
     */
    public void setPrivateKey(File privateKey) {
        this.m_privateKey = privateKey;
    }

    /**
     * @param privateKeyPassword - the password to open the privateKey file. null if the file is not password protected.
     * @return
     * @see NetconfTransportOrder#setPrivateKey(File)
     */
    public void setPrivateKeyPassword(String privateKeyPassword) {
        if (privateKeyPassword != null) {
            privateKeyPassword = privateKeyPassword.trim();
        }
        m_privateKeyPassword = privateKeyPassword;
    }

    public String getPrivateKeyPassword() {
        return m_privateKeyPassword;
    }

    public KeyManager getKeyManager() {
        return m_keyManager;
    }

    /**
     * @param keyManager - the {@link KeyManager} that is used to encrypt data being sent to clients. {@code null} to
     *                  use the results of
     *                   parsing {@code certiChain} and {@code keyFile}.
     * @return
     * @see NetconfTransportOrder#setCertificateChain(File)
     * @see NetconfTransportOrder#setPrivateKey(File)
     * @see NetconfTransportOrder#setPrivateKeyPassword(String)
     */
    public void setKeyManager(KeyManager keyManager) {
        m_keyManager = keyManager;
    }

    public File getCertificateChain() {
        return m_certiChain;
    }

    /**
     * @param certChain - A X.509 certificate chain file in PEM format.
     * @return
     * @see NetconfTransportOrder#setPrivateKey(File)
     * @see NetconfTransportOrder#setPrivateKeyPassword(String)
     * @see NetconfTransportOrder#setKeyManager(KeyManager)
     */
    public void setCertificateChain(File certChain) {
        m_certiChain = certChain;
    }

    public File getTrustChain() {
        return m_trustChain;
    }

    /**
     * @param trustChain A X.509 certificate chain file in PEM format.This provides the certificate chains used for
     *                   mutual authentication.
     *                   pass {@code null} if you are setting a {@code TrustManagerFactory} via
     *                   {@link NetconfTransportOrder#setTrustManager(TrustManager)} .
     * @return
     * @see NetconfTransportOrder#setTrustManager(TrustManager)
     */
    public void setTrustChain(File trustChain) {
        m_trustChain = trustChain;
    }

    public TrustManager getTrustManager() {
        return m_trustManager;
    }

    /**
     * @param trustManager - the {@link TrustManager} that verifies the certificates sent from netconf peers. {@code
     * null} to use the
     *                     results of parsing {@code trustChain} file.
     * @return
     * @see NetconfTransportOrder#setTrustChain(File)
     */
    public void setTrustManager(TrustManager trustManager) {
        m_trustManager = trustManager;
    }

    public boolean isAllowSelfSigned() {
        return m_allowSelfSigned;
    }

    public void setAllowSelfSigned(boolean allowSelfSigned) {
        this.m_allowSelfSigned = allowSelfSigned;
    }

    public String getIpForTlsTransport() {
        return m_ipForTlsTransport;
    }

    public void setIpForTlsTransport(String ipForTlsTransport) {
        if (ipForTlsTransport != null) {
            ipForTlsTransport = ipForTlsTransport.trim();
        }
        m_ipForTlsTransport = ipForTlsTransport;
    }

    public String getCallHomeIp() {
        return m_callHomeIp;
    }

    public void setCallHomeIp(String callHomeIp) {
        if (callHomeIp != null) {
            callHomeIp = callHomeIp.trim();
        }
        this.m_callHomeIp = callHomeIp;
    }

    public Integer getCallHomePort() {
        return m_callHomePort;
    }

    public void setCallHomePort(Integer callHomePort) {
        this.m_callHomePort = callHomePort;
    }

    public CallHomeListener getCallHomeListener() {
        return m_callHomeListener;
    }

    public void setCallHomeListener(CallHomeListener listener) {
        this.m_callHomeListener = listener;
    }

    public SocketAddress getLocalAddress() {
        return m_localAddress;
    }

    public void setLocalAddress(SocketAddress localAddress) {
        m_localAddress = localAddress;
    }

    public boolean isClientAuthenticationNeeded() {
        return m_clientAuthenticationNeeded;
    }

    /**
     * This method is to be called by the server if it needs the clients connecting via TLS to be authenticated with
     * a certificate.
     *
     * @return
     */
    public void setClientAuthenticationNeeded() {
        m_clientAuthenticationNeeded = true;
    }

    /**
     * ssh heart beat interval in seconds.
     *
     * @return
     */
    public void setHeartbeatInterval(int heartbeatIntervalSecs) {
        m_heartbeatIntervalSecs = heartbeatIntervalSecs;
    }

    /**
     * ssh heart beat interval in seconds.
     *
     * @return
     */
    public int getHeartbeatInterval() {
        return m_heartbeatIntervalSecs;
    }

    public boolean isTlsKeepalive() {
        return m_tlsKeepalive;
    }

    public void setTlsKeepalive(boolean tlsKeepalive) {
        this.m_tlsKeepalive = tlsKeepalive;
    }

    public SslProvider getSslProvider() {
        return m_sslProvider;
    }

    public void setSslProvider(SslProvider sslProvider) {
        m_sslProvider = sslProvider;
    }

    @Override
    public String toString() {
        return "NetconfTransportOrder{" +
                "m_transportType='" + m_transportType + '\'' +
                ", m_sslProvider=" + m_sslProvider +
                ", m_allowSelfSigned=" + m_allowSelfSigned +
                ", m_trustChain=" + m_trustChain +
                ", m_certiChain=" + m_certiChain +
                ", m_privateKey=" + m_privateKey +
                ", m_ipForTlsTransport='" + m_ipForTlsTransport + '\'' +
                ", m_portIdForTlsTrasnport=" + m_portIdForTlsTrasnport +
                ", m_serverSshHostKeyPath='" + m_serverSshHostKeyPath + '\'' +
                ", m_serverSocketAddress=" + m_serverSocketAddress +
                ", m_callHomeIp='" + m_callHomeIp + '\'' +
                ", m_callHomePort=" + m_callHomePort +
                ", m_callHomeListener=" + m_callHomeListener +
                ", m_localAddress=" + m_localAddress +
                ", m_clientAuthenticationNeeded=" + m_clientAuthenticationNeeded +
                ", m_privateKeyPassword='" + m_privateKeyPassword + '\'' +
                ", m_heartbeatIntervalSecs=" + m_heartbeatIntervalSecs +
                ", m_tlsKeepalive=" + m_tlsKeepalive +
                ", m_trustManager=" + m_trustManager +
                ", m_keyManager=" + m_keyManager +
                ", m_tlsHandshaketimeoutMillis=" + m_tlsHandshaketimeoutMillis +
                '}';
    }


    public long getTlsHandshaketimeoutMillis() {
        return m_tlsHandshaketimeoutMillis;
    }

    public void setTlsHandshaketimeoutMillis(long tlsHandshaketimeoutMillis) {
        m_tlsHandshaketimeoutMillis = tlsHandshaketimeoutMillis;
    }
}
