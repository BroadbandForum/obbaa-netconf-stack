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

package org.broadband_forum.obbaa.netconf.api.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.TcpConnectionListener;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

import io.netty.channel.EventLoopGroup;

public class NetconfServerConfigurationBuilderTest {

    private ExecutorService m_executorService;
    private NetconfServerConfigurationBuilder m_builder;
    private HashSet<String> m_serverCaps;
    private NetconfServerMessageListener m_serverMessageListener;
    private NetconfServerAuthenticationHandler m_authenticationHander;

    private ServerCapabilityProvider m_capabilityProvider;
    private TcpConnectionListener m_tcpConnectionListener;
    private AuthenticationListener m_authListener;
    private ServerMessageHandler m_serverMessageHandler;
    private EventLoopGroup m_loopGroup;
    private NetconfLogger m_logger;
    private long m_idleTimeOut = 1000L;
    private SocketAddress m_localAddress_null = null;


    @Before
    public void setUp() throws Exception {
        m_executorService = mock(ThreadPoolExecutor.class);
        m_serverMessageListener = mock(NetconfServerMessageListener.class);
        m_serverMessageHandler = mock(ServerMessageHandler.class);
        m_capabilityProvider = mock(ServerCapabilityProvider.class);
        m_authListener = mock(AuthenticationListener.class);
        m_tcpConnectionListener = mock(TcpConnectionListener.class);
        m_loopGroup = mock(EventLoopGroup.class);
        m_logger = mock(NetconfLogger.class);

        m_serverCaps = new HashSet<String>();
        m_serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        m_serverCaps.add(NetconfResources.NETCONF_WRITABLE_RUNNNG);
        m_authenticationHander = new NetconfServerAuthenticationHandler() {

            @Override
            public boolean authenticate(ClientAuthenticationInfo clientAuthInfo) {
                return true;
            }

            @Override
            public boolean authenticate(PublicKey pubKey) {
                return true;
            }

            @Override
            public void logout(Serializable sshSessionId) {

            }

        };
        m_builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder(5000).setAuthenticationHandler
                (m_authenticationHander)
                .setNetconfServerMessageListener(m_serverMessageListener)
                .setCapabilities(m_serverCaps)
                .setExecutorService(m_executorService)
                .setAuthenticationListener(m_authListener)
                .setAuthenticationHandler(m_authenticationHander)
                .setServerMessageHandler(m_serverMessageHandler)
                .setCapabilityProvider(m_capabilityProvider)
                .setTcpConnectionListener(m_tcpConnectionListener)
                .setEventLoopGroup(m_loopGroup);
    }

    @Test
    public void testBuildServerConfigurationWithExecutorService() throws NetconfConfigurationBuilderException {
        //build server configuration
        NetconfServerConfiguration serverConfig = m_builder.build();
        assertNotNull(serverConfig);

        //assert authentication hander
        assertEquals(m_authenticationHander, serverConfig.getNetconfServerAuthenticationHandler());
        //assert server Message listener
        assertEquals(m_serverMessageListener, serverConfig.getNetconfServerMessageListener());
        //assert server capability
        assertEquals(m_serverCaps, serverConfig.getCaps());
        //assert executor service
        assertEquals(m_executorService, serverConfig.getExecutorService());
        assertEquals(m_executorService, m_builder.getExecutorService());

        assertEquals(m_authListener, serverConfig.getAuthenticationListener());
        assertEquals(m_authListener, m_builder.getAuthenticationListener());

        assertEquals(m_serverMessageHandler, serverConfig.getServerMessageHandler());
        assertEquals(m_serverMessageHandler, m_builder.getServerMessageHandler());

        assertEquals(m_capabilityProvider, serverConfig.getCapabilityProvider());
        assertEquals(m_tcpConnectionListener, serverConfig.getTcpConnectionListener());
        assertEquals(m_loopGroup, serverConfig.getEventLoopGroup());
    }

    @Test
    public void testDefaultNetconfServerConfiguration() throws NetconfConfigurationBuilderException,
            UnknownHostException {

        m_builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder()
                .setAuthenticationHandler(m_authenticationHander)
                .setCapabilities(m_serverCaps);
        m_builder.setLogger(m_logger);

        NetconfServerConfiguration netconfServerConfig = m_builder.build();
        netconfServerConfig.setCaps(m_serverCaps);
        netconfServerConfig.setExecutorService(m_executorService);
        netconfServerConfig.setServerMessageHandler(m_serverMessageHandler);
        netconfServerConfig.setNetconfServerMessageListener(m_serverMessageListener);
        netconfServerConfig.setNetconfLogger(m_logger);
        netconfServerConfig.setCapabilityProvider(m_capabilityProvider);
        netconfServerConfig.setConnectionIdleTimeOutMillis(m_idleTimeOut);
        netconfServerConfig.setTcpConnectionListener(m_tcpConnectionListener);
        netconfServerConfig.setNetconfServerAuthenticationHandler(m_authenticationHander);
        netconfServerConfig.setLocalAddress(m_localAddress_null);

        assertEquals(m_executorService, netconfServerConfig.getExecutorService());
        assertEquals(m_serverCaps, netconfServerConfig.getCaps());
        assertEquals(m_logger, netconfServerConfig.getNetconfLogger());
        assertEquals(m_serverMessageListener, netconfServerConfig.getNetconfServerMessageListener());
        assertEquals(m_serverMessageHandler, netconfServerConfig.getServerMessageHandler());
        assertEquals(m_capabilityProvider, netconfServerConfig.getCapabilityProvider());
        assertEquals(m_idleTimeOut, netconfServerConfig.getConnectionIdleTimeOutMillis());
        assertEquals(m_tcpConnectionListener, netconfServerConfig.getTcpConnectionListener());
        assertNull(netconfServerConfig.getLocalAddress());
        assertNotNull(netconfServerConfig.toString());
        assertNotNull(netconfServerConfig.getNetconfTransport());
        assertNotNull(netconfServerConfig.getSessionIdProvider());

    }

    @Test
    public void testValidateNetconfServerConfiguration() throws UnknownHostException {

        try {
            m_builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder();
            m_builder.build();
            fail("Should have thrown an Exception");
        } catch (NetconfConfigurationBuilderException e) {
            assertEquals("caps and capability provider is null", e.getMessage());
        }

        try {
            m_builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder()
                    .setTransport(null);
            m_builder.build();
            fail("Should have thrown an Exception");
        } catch (NetconfConfigurationBuilderException e) {
            assertEquals("netconfTransport is null", e.getMessage());
        }

        try {
            m_builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder()
                    .setCapabilities(m_serverCaps);
            m_builder.build();
            fail("Should have thrown an Exception");
        } catch (NetconfConfigurationBuilderException e) {
            assertEquals("NetconfAuthenticationHandler is null", e.getMessage());
        }
    }

    @Test
    public void testAddCapability() throws NetconfConfigurationBuilderException, UnknownHostException {
        m_builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder()
                .setAuthenticationHandler(m_authenticationHander)
                .addCapability("cap1")
                .addCapability("cap2")
                .addCapability("cap1");

        Set<String> expectedCaps = new HashSet<>();
        expectedCaps.add("cap2");
        expectedCaps.add("cap1");
        Set<String> actualCaps = m_builder.build().getCaps();
        assertEquals(expectedCaps, actualCaps);
    }

}

