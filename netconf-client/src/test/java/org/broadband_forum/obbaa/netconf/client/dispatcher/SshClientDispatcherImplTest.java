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

package org.broadband_forum.obbaa.netconf.client.dispatcher;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.io.IoWriteFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.client.ssh.SshHelloMessageListener;
import org.broadband_forum.obbaa.netconf.client.ssh.SshNetconfClientSession;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.PasswordLoginProvider;

public class SshClientDispatcherImplTest {

    @Mock
    private ConnectFuture m_connectFuture;
    @Mock
    private ClientSession m_clientSession;
    @Mock
    private AuthFuture m_authFuture;
    private SshClientDispatcherImpl m_dispatcher;
    private NetconfClientConfiguration m_clientConfig;
    @Mock
    private ChannelSubsystem m_channelSs;
    @Mock
    private OpenFuture m_openFuture;
    @Mock
    private IoOutputStream m_asyncIn;
    @Mock
    private IoInputStream m_asyncOut;
    @Mock
    private IoReadFuture m_ioReadFuture;
    @Mock
    private IoWriteFuture m_ioWriteFuture;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_dispatcher = new TestSshClientDispatcherImpl();
        NetconfLoginProvider authorizationProvider = new PasswordLoginProvider("user", "pass");
        NetconfTransportOrder clientTransportOrder = new NetconfTransportOrder();
        clientTransportOrder.setServerSocketAddress(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 9292));
        clientTransportOrder.setTransportType(NetconfTransportProtocol.SSH.name());

        Set<String> clientCaps = new HashSet<>();
        m_clientConfig = new NetconfClientConfigurationBuilder()
                .setNetconfLoginProvider(authorizationProvider) //provide username and password login
                .setTransport(NetconfTransportFactory.makeNetconfTransport(clientTransportOrder))
                .setCapabilities(clientCaps).build();
        when(m_connectFuture.getSession()).thenReturn(m_clientSession);
        when(m_clientSession.auth()).thenReturn(m_authFuture);
        when(m_authFuture.addListener(anyObject())).thenReturn(m_authFuture);
        when(m_clientSession.createSubsystemChannel(anyString())).thenReturn(m_channelSs);
        when(m_channelSs.open()).thenReturn(m_openFuture);
        when(m_channelSs.getAsyncIn()).thenReturn(m_asyncIn);
        when(m_channelSs.getAsyncOut()).thenReturn(m_asyncOut);
        when(m_asyncOut.read(anyObject())).thenReturn(m_ioReadFuture);
        ArgumentCaptor<SshFutureListener> helloFutureListener = ArgumentCaptor.forClass(SshFutureListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                SshHelloMessageListener futureListener = (SshHelloMessageListener) invocationOnMock.getArguments()[0];
                futureListener.setHelloMessageRecieved(true);
                return m_ioReadFuture;
            }
        }).when(m_ioReadFuture).addListener(anyObject());
        when(m_asyncIn.write(anyObject())).thenReturn(m_ioWriteFuture);
    }
    @Test
    public void testIdleTimeoutProperties() throws Exception {
        Future<NetconfClientSession> futureSession = m_dispatcher.createClient(m_clientConfig);
        NetconfClientSession session = futureSession.get();
        assertEquals(0, (((SshNetconfClientSession)session).getSshClient().getProperties().get(FactoryManager.IDLE_TIMEOUT)));
        assertEquals(Long.MAX_VALUE, (((SshNetconfClientSession)session).getSshClient().getProperties().get(FactoryManager.NIO2_READ_TIMEOUT)));
    }

    private class TestSshClientDispatcherImpl extends SshClientDispatcherImpl {
        @Override
        protected ConnectFuture getConnectFuture(SshClient sshClient, String username, InetSocketAddress socketAddress) throws IOException {
            return m_connectFuture;
        }
    }
}
