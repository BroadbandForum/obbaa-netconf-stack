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

package org.broadband_forum.obbaa.netconf.client.callhome.tls;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.TcpServerSession;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.client.dispatcher.NetconfClientDispatcherImpl;
import org.broadband_forum.obbaa.netconf.server.QueuingMessageHandler;
import org.broadband_forum.obbaa.netconf.server.dispatcher.NetconfServerDispatcherImpl;

import io.netty.channel.nio.NioEventLoopGroup;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.client.tests.server.DummyLoggingServerMessageListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReverseTlsTest {

    private static final Integer NUMBER_OF_REMOTE_DEVICES = 10;
    private static final Logger LOGGER = Logger.getLogger(ReverseTlsTest.class);
    private static Integer c_testCallHomePort = 12345;
    static HashSet<String> m_caps = new HashSet<String>();
    static Set<String> m_severCaps = new HashSet<String>();
    private static String m_ipAddress = null;
    private NetconfServerDispatcher m_serverDispatcher = new NetconfServerDispatcherImpl(ExecutorServiceProvider
            .getInstance()
            .getExecutorService());

    static {
        m_caps.add(NetconfResources.NETCONF_BASE_CAP_1_0);

        m_severCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        m_severCaps.add(NetconfResources.NETCONF_WRITABLE_RUNNNG);
    }

    private static final Logger m_logger = Logger.getLogger(ReverseTlsTest.class);
    private TcpServerSession m_session;
    private ExecutorService m_callhomeExecutorSpy;
    private ExecutorService m_executorService;

    @BeforeClass
    public static void setUpSuite() throws IOException {
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            int port = s.getLocalPort();
            c_testCallHomePort = port;

        } catch (IOException e) {
            // ignore, let the port be default
        } finally {
            // close the socket
            s.close();
        }
        m_logger.info("Using call-home port :" + c_testCallHomePort);
    }

    @Before
    public void setUp() throws IOException {
        m_callhomeExecutorSpy = spy(Executors.newCachedThreadPool());
        m_executorService = Executors.newCachedThreadPool();
        BufferedReader input = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("hostname -i")
                .getInputStream()));
        m_ipAddress = input.readLine();
        m_logger.info(m_ipAddress + "m_ipAddress");
    }

    @Test
    public void testNetconfClientGetsNotifiedWhenServerCallsHome() throws NetconfClientDispatcherException,
            NetconfConfigurationBuilderException, InterruptedException, ExecutionException,
            NetconfServerDispatcherException,
            UnknownHostException, NetconfMessageBuilderException {

        NetconfClientDispatcher dispatcher = new NetconfClientDispatcherImpl(m_executorService, m_callhomeExecutorSpy);

        NetconfClientConfigurationBuilder clientConfigBuilder = new NetconfClientConfigurationBuilder();
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        transportOrder.setCallHomeIp(m_ipAddress);
        transportOrder.setCallHomePort(c_testCallHomePort);
        transportOrder.setTlsKeepalive(true);
        CountDownLatch latch = new CountDownLatch(1);
        transportOrder.setCallHomeListener(new TestCallHomeLister(latch));

        NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOrder);
        clientConfigBuilder.setCapabilities(m_caps).setConnectionTimeout(10000L).setEventLoopGroup(new
                NioEventLoopGroup(10))
                .setTransport(transport);
        NetconfClientConfiguration config = clientConfigBuilder.build();

        m_session = dispatcher.createReverseClient(config).get();

        // now make a server call home
        startCallHomeServer();
        latch.await(1, TimeUnit.MINUTES);
        verify(m_callhomeExecutorSpy).execute((Runnable) anyObject());
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testNetconfClientGetsNotifiedWhenManyServersCallHome() throws NetconfClientDispatcherException,
            NetconfConfigurationBuilderException, InterruptedException, ExecutionException,
            NetconfServerDispatcherException,
            UnknownHostException, NetconfMessageBuilderException {

        NetconfClientDispatcher dispatcher = new NetconfClientDispatcherImpl(m_executorService, m_callhomeExecutorSpy);

        NetconfClientConfigurationBuilder clientConfigBuilder = new NetconfClientConfigurationBuilder();
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        transportOrder.setCallHomeIp(m_ipAddress);
        transportOrder.setCallHomePort(c_testCallHomePort);
        CountDownLatch latch = new CountDownLatch(10);
        transportOrder.setCallHomeListener(new TestCallHomeLister(latch));

        NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOrder);
        clientConfigBuilder.setCapabilities(m_caps).setConnectionTimeout(10000L).setEventLoopGroup(new
                NioEventLoopGroup(10))
                .setTransport(transport);
        NetconfClientConfiguration config = clientConfigBuilder.build();

        m_session = dispatcher.createReverseClient(config).get();

        // now make 10 servers call home
        startManyCallHomeServers();
        latch.await(1, TimeUnit.MINUTES);
        verify(m_callhomeExecutorSpy, times(10)).execute((Runnable) anyObject());
        assertEquals(0, latch.getCount());
    }

    @After
    public void tearDown() {
        if (m_session != null) {
            m_session.stopListening(true);
        }
        m_callhomeExecutorSpy.shutdownNow();
        m_executorService.shutdownNow();
    }

    private void startManyCallHomeServers() throws UnknownHostException, NetconfConfigurationBuilderException,
            InterruptedException,
            ExecutionException, NetconfServerDispatcherException {
        for (int i = 0; i < 10; i++) {
            startCallHomeServer();
        }

    }

    private void startCallHomeServer() throws NetconfConfigurationBuilderException, InterruptedException,
            ExecutionException,
            NetconfServerDispatcherException, UnknownHostException {
        NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder();
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        transportOrder.setCallHomeIp(m_ipAddress);
        transportOrder.setTlsKeepalive(true);
        transportOrder.setCallHomePort(c_testCallHomePort);
        NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOrder);
        NetconfServerMessageListener netconfServerMessageListener = new DummyLoggingServerMessageListener();
        builder.setCapabilities(m_severCaps).setTransport(transport).setNetconfServerMessageListener
                (netconfServerMessageListener)
                .setServerMessageHandler(new QueuingMessageHandler(netconfServerMessageListener));
        NetconfLogger logger = mock(NetconfLogger.class);
        builder.setLogger(logger);
        NetconfServerConfiguration config = builder.build();

        m_serverDispatcher.createServer(config).get();

    }

    private class TestCallHomeLister implements CallHomeListener {
        private final CountDownLatch m_latch;

        public TestCallHomeLister(CountDownLatch latch) {
            m_latch = latch;
        }

        @Override
        public void connectionEstablished(NetconfClientSession clientSession, NetconfLoginProvider
                netconfLoginProvider, X509Certificate
                peerX509Certificate, boolean isSelfSigned) {
            LOGGER.info("Got a call back for a session with id : " + clientSession.getSessionId());
            m_latch.countDown();
        }
    }
}
