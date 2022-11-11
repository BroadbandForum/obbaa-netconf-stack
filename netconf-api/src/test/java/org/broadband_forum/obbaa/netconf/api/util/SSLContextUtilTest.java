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

package org.broadband_forum.obbaa.netconf.api.util;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.x509certificates.DynamicX509TrustManagerImpl;
import org.broadband_forum.obbaa.netconf.api.x509certificates.TrustManagerInitException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.net.URL;
import java.util.HashSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by kbhatk on 7/27/16.
 */
@Ignore
public class SSLContextUtilTest {
    static HashSet<String> m_caps = new HashSet<String>();
    static {
        m_caps.add(NetconfResources.NETCONF_BASE_CAP_1_0);

    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    private TrustManager m_mockTrustManager;
    @Mock
    private KeyManager m_mockKeyManager;

    @Test
    public void testGetServerSSLContext_WithCustomManagers() throws Exception{
        NetconfServerConfiguration config = getCallHomeTLSServerConfiguration();
        doServerAsserts(config);
    }

    @Test
    public void testGetServerSSLContext_WithSelfSignedCert() throws NetconfConfigurationBuilderException, NetconfServerDispatcherException {
        NetconfServerConfiguration config = getCallHomeTLSServerConfigurationSelfSignedCert();
        doServerAsserts(config);
    }

    @Test
    public void testGetServerSSLContext_WithFiles() throws NetconfConfigurationBuilderException, NetconfServerDispatcherException {
        NetconfServerConfiguration config = getCallHomeTLSServerConfigurationFiles();
        doServerAsserts(config);
    }

    @Test
    public void testGetClientSSLContext_WithCustomManagers() throws Exception {
        NetconfClientConfiguration config = getCallHomeTLSClientConfiguration();
        doClientAsserts(config);
    }

    @Test
    @Ignore
    public void testGetClientSSLContext_WithCustomManagersAndOpenSsl() throws Exception {
        NetconfClientConfiguration config = getCallHomeTLSClientConfiguration(SslProvider.OPENSSL);
        doClientAsserts(config);
    }

    @Test
    public void testGetClientSSLContext_WithSelfSignedCert() throws NetconfConfigurationBuilderException, NetconfClientDispatcherException {
        NetconfClientConfiguration config = getCallHomeTLSClientConfigurationSelfSignedCert();
        doClientAsserts(config);
    }

    @Test
    public void testGetClientSSLContext_WithFiles() throws NetconfConfigurationBuilderException, NetconfClientDispatcherException {
        NetconfClientConfiguration config = getCallHomeTLSClientConfigurationFiles();
        doClientAsserts(config);
    }

    private NetconfClientConfiguration getCallHomeTLSClientConfigurationFiles() throws NetconfConfigurationBuilderException {
        NetconfClientConfigurationBuilder builder = new NetconfClientConfigurationBuilder();
        NetconfTransportOrder transportOder = new NetconfTransportOrder();
        transportOder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        transportOder.setCallHomeIp("127.0.0.1");
        transportOder.setTlsKeepalive(true);
        transportOder.setCallHomePort(4335);
        transportOder.setAllowSelfSigned(false);
        URL clientTrustChain = Thread.currentThread().getContextClassLoader().getResource("sslcontextutiltest/trust.crt");
        File trustChain = new File(clientTrustChain.getPath());

        transportOder.setTrustChain(trustChain);

        File keyCertificate = new File(Thread.currentThread().getContextClassLoader()
                .getResource("sslcontextutiltest/keyCert.crt").getPath());
        transportOder.setCertificateChain(keyCertificate);
        File privateKey = new File(Thread.currentThread().getContextClassLoader().getResource("sslcontextutiltest/PK.pem")
                .getPath());
        transportOder.setPrivateKey(privateKey);

        NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOder);
        builder.setCapabilities(m_caps).setTransport(transport);
        return builder.build();
    }

    private NetconfServerConfiguration getCallHomeTLSServerConfigurationFiles() throws NetconfConfigurationBuilderException {
        NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder();
        NetconfTransportOrder transportOder = new NetconfTransportOrder();
        transportOder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        transportOder.setCallHomeIp("127.0.0.1");
        transportOder.setTlsKeepalive(true);
        transportOder.setCallHomePort(4335);
        transportOder.setAllowSelfSigned(false);
        URL clientTrustChain = Thread.currentThread().getContextClassLoader().getResource("sslcontextutiltest/trust.crt");
        File trustChain = new File(clientTrustChain.getPath());

        transportOder.setTrustChain(trustChain);

        File keyCertificate = new File(Thread.currentThread().getContextClassLoader()
                .getResource("sslcontextutiltest/keyCert.crt").getPath());
        transportOder.setCertificateChain(keyCertificate);
        File privateKey = new File(Thread.currentThread().getContextClassLoader().getResource("sslcontextutiltest/PK.pem")
                .getPath());
        transportOder.setPrivateKey(privateKey);

        NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOder);
        builder.setCapabilities(m_caps).setTransport(transport);
        return builder.build();
    }

    private void doClientAsserts(NetconfClientConfiguration config) throws NetconfClientDispatcherException {
        SslContext sslContext = SSLContextUtil.getClientSSLContext(config);
        assertNotNull(sslContext);
        assertTrue(sslContext.isClient());
    }

    private NetconfClientConfiguration getCallHomeTLSClientConfiguration() throws Exception {
        return getCallHomeTLSClientConfiguration(SslProvider.JDK);
    }

    private NetconfClientConfiguration getCallHomeTLSClientConfiguration(SslProvider sslProvider) throws Exception {
        NetconfClientConfigurationBuilder builder = new NetconfClientConfigurationBuilder();
        NetconfTransport transport = getTransportWithMgrs(sslProvider);
        builder.setCapabilities(m_caps).setTransport(transport);
        return builder.build();
    }

    private NetconfTransport getTransportWithMgrs(SslProvider sslProvider) throws NetconfConfigurationBuilderException, TrustManagerInitException {
        NetconfTransportOrder transportOder = new NetconfTransportOrder();
        transportOder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        transportOder.setCallHomeIp("127.0.0.1");
        transportOder.setTlsKeepalive(true);
        transportOder.setCallHomePort(4335);
        transportOder.setTrustManager(new DynamicX509TrustManagerImpl(Thread.currentThread().getContextClassLoader()
                .getResource("sslcontextutiltest/keyCert.crt").getPath()));
        if(sslProvider.equals(SslProvider.JDK)) {
            transportOder.setKeyManager(m_mockKeyManager);
        }else{
            transportOder.setCertificateChain(new File(Thread.currentThread().getContextClassLoader()
                    .getResource("sslcontextutiltest/keyCert.crt").getPath()));
            transportOder.setPrivateKey(new File(Thread.currentThread().getContextClassLoader()
                    .getResource("sslcontextutiltest/PK.pem").getPath()));
        }
        transportOder.setAllowSelfSigned(false);
        transportOder.setSslProvider(sslProvider);
        return NetconfTransportFactory.makeNetconfTransport(transportOder);
    }

    private NetconfServerConfiguration getCallHomeTLSServerConfigurationSelfSignedCert() throws NetconfConfigurationBuilderException {
        NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder();
        NetconfTransport transport = getSelfSignedNetconfTransport();
        builder.setCapabilities(m_caps).setTransport(transport);
        return builder.build();
    }

    private NetconfClientConfiguration getCallHomeTLSClientConfigurationSelfSignedCert() throws NetconfConfigurationBuilderException {
        NetconfClientConfigurationBuilder builder = new NetconfClientConfigurationBuilder();
        NetconfTransport transport = getSelfSignedNetconfTransport();
        builder.setCapabilities(m_caps).setTransport(transport);
        return builder.build();
    }

    private NetconfTransport getSelfSignedNetconfTransport() throws NetconfConfigurationBuilderException {
        NetconfTransportOrder transportOder = new NetconfTransportOrder();
        transportOder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        transportOder.setCallHomeIp("127.0.0.1");
        transportOder.setTlsKeepalive(true);
        transportOder.setCallHomePort(4335);
        transportOder.setAllowSelfSigned(true);
        return NetconfTransportFactory.makeNetconfTransport(transportOder);
    }

    private void doServerAsserts(NetconfServerConfiguration config) throws NetconfServerDispatcherException {
        SslContext sslContext = SSLContextUtil.getServerSSLContext(config);
        assertNotNull(sslContext);
        assertTrue(sslContext.isServer());
    }


    private NetconfServerConfiguration getCallHomeTLSServerConfiguration() throws Exception {
        return getCallHomeTLSServerConfiguration(SslProvider.JDK);
    }

    private NetconfServerConfiguration getCallHomeTLSServerConfiguration(SslProvider sslProvider) throws Exception {
        NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder();
        NetconfTransport transport = getTransportWithMgrs(sslProvider);
        builder.setCapabilities(m_caps).setTransport(transport);
        return builder.build();
    }


}
