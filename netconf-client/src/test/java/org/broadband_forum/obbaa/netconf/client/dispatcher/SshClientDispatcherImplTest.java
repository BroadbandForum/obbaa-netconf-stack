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
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DEFAULT_NC_SSH_CIPHERS;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DEFAULT_NC_SSH_MACS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.mac.BuiltinMacs;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtilsUserTest;
import org.broadband_forum.obbaa.netconf.client.ssh.SshHelloMessageListener;
import org.broadband_forum.obbaa.netconf.client.ssh.SshNetconfClientSession;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.PasswordLoginProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SshClientDispatcherImplTest extends SystemPropertyUtilsUserTest {

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
                .setConnectionTimeout(50)
                .setCapabilities(clientCaps).build();
        when(m_connectFuture.getSession()).thenReturn(m_clientSession);
        when(m_clientSession.auth()).thenReturn(m_authFuture);
        when(m_authFuture.addListener(anyObject())).thenReturn(m_authFuture);
        when(m_clientSession.createSubsystemChannel(anyString())).thenReturn(m_channelSs);
        when(m_channelSs.open()).thenReturn(m_openFuture);
        when(m_channelSs.getAsyncIn()).thenReturn(m_asyncIn);
        when(m_channelSs.getAsyncOut()).thenReturn(m_asyncOut);
        when(m_asyncOut.read(anyObject())).thenReturn(m_ioReadFuture);
        when(m_asyncIn.writePacket(anyObject())).thenReturn(m_ioWriteFuture);
        mockPropertyUtils("NC_SSH_CLIENT_MACS", DEFAULT_NC_SSH_MACS);
        mockPropertyUtils("NC_SSH_CLIENT_CIPHERS", DEFAULT_NC_SSH_CIPHERS);
        mockPropertyUtils("NC_SSH_CLIENT_DH_KEXS", "");
        mockPropertyUtils("NETCONF_SOCKET_READ_TIMEOUT_MS", NetconfClientSession.DEFAULT_SOCKET_READ_TIMEOUT_MS);
    }

    @Test
    public void testIdleTimeoutProperties() throws Exception {
        setSshHelloMessageListener(true);
        Future<NetconfClientSession> futureSession = m_dispatcher.createClient(m_clientConfig);
        NetconfClientSession session = futureSession.get();
        assertEquals(0, (((SshNetconfClientSession) session).getSshClient().getProperties().get(FactoryManager.IDLE_TIMEOUT)));
        assertEquals(Long.parseLong(NetconfClientSession.DEFAULT_SOCKET_READ_TIMEOUT_MS), (((SshNetconfClientSession) session).getSshClient().getProperties().get(FactoryManager.NIO2_READ_TIMEOUT)));
        Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        assertEquals(BouncyCastleProvider.PROVIDER_NAME, provider.getName());
    }

    @Test
    public void testExceptionErrorMessage() throws IOException {
        doThrow(new RuntimeException("exception with text")).when(m_clientSession).auth();
        try {
            m_dispatcher.createFutureSession(m_clientConfig);
        } catch (NetconfClientDispatcherException e) {
            assertEquals("Could not get a client session due to : exception with text", e.getMessage());
        }

        doThrow(new RuntimeException()).when(m_clientSession).auth();
        try {
            m_dispatcher.createFutureSession(m_clientConfig);
        } catch (NetconfClientDispatcherException e) {
            assertEquals("Could not get a client session due to : java.lang.RuntimeException", e.getMessage());
        }
    }

    @Test
    public void testSshClientDefaultAlgorithms() throws Exception {
        setSshHelloMessageListener(true);
        Future<NetconfClientSession> futureSession = m_dispatcher.createClient(m_clientConfig);
        NetconfClientSession session = futureSession.get();
        List<BuiltinMacs> expectedMacs = new ArrayList<>();
        expectedMacs.add(BuiltinMacs.hmacsha1);
        expectedMacs.add(BuiltinMacs.hmacsha256);
        expectedMacs.add(BuiltinMacs.hmacsha512);
        assertEquals(expectedMacs, ((SshNetconfClientSession) session).getSshClient().getMacFactories());

        List<BuiltinCiphers> expectedCiphers = new ArrayList<>();
        expectedCiphers.add(BuiltinCiphers.aes128ctr);
        expectedCiphers.add(BuiltinCiphers.aes192ctr);
        expectedCiphers.add(BuiltinCiphers.aes256ctr);
        assertEquals(expectedCiphers, ((SshNetconfClientSession) session).getSshClient().getCipherFactories());

        assertEquals(12, ((SshNetconfClientSession) session).getSshClient().getKeyExchangeFactories().size());
    }

    @Test
    public void testSettingSshClientAlgorithms() throws Exception {
        mockPropertyUtils("NC_SSH_CLIENT_MACS", "hmacsha256   ,    hmac-sha2-512-etm@openssh.com");
        mockPropertyUtils("NC_SSH_CLIENT_CIPHERS", "aes128ctr , aes256-ctr");
        mockPropertyUtils("NC_SSH_CLIENT_DH_KEXS", "dhg14_256, diffie-hellman-group15-sha512");
        setSshHelloMessageListener(true);
        Future<NetconfClientSession> futureSession = m_dispatcher.createClient(m_clientConfig);
        NetconfClientSession session = futureSession.get();
        List<BuiltinMacs> expectedMacs = new ArrayList<>();
        expectedMacs.add(BuiltinMacs.hmacsha256);
        expectedMacs.add(BuiltinMacs.hmacsha512etm);
        assertEquals(expectedMacs, ((SshNetconfClientSession) session).getSshClient().getMacFactories());

        List<BuiltinCiphers> expectedCiphers = new ArrayList<>();
        expectedCiphers.add(BuiltinCiphers.aes128ctr);
        expectedCiphers.add(BuiltinCiphers.aes256ctr);
        assertEquals(expectedCiphers, ((SshNetconfClientSession) session).getSshClient().getCipherFactories());

        assertEquals(2, ((SshNetconfClientSession) session).getSshClient().getKeyExchangeFactories().size());
    }

    @Test
    public void testSettingSshClientAlgorithmsWhenNoAlgosMentioned() throws Exception {
        mockPropertyUtils("NC_SSH_CLIENT_MACS", "");
        mockPropertyUtils("NC_SSH_CLIENT_CIPHERS", "");
        mockPropertyUtils("NC_SSH_CLIENT_DH_KEXS", "");
        setSshHelloMessageListener(true);
        Future<NetconfClientSession> futureSession = m_dispatcher.createClient(m_clientConfig);
        NetconfClientSession session = futureSession.get();
        assertEquals(9, ((SshNetconfClientSession) session).getSshClient().getMacFactories().size());

        assertEquals(10, ((SshNetconfClientSession) session).getSshClient().getCipherFactories().size());

        assertEquals(12, ((SshNetconfClientSession) session).getSshClient().getKeyExchangeFactories().size());
    }

    @Test
    public void testSettingSshClientAlgorithmsWhenAllAlgosMentioned() throws Exception {
        mockPropertyUtils("NC_SSH_CLIENT_MACS", "hmac-sha2-256-etm@openssh.com, hmac-sha2-512-etm@openssh.com, hmac-sha1-etm@openssh.com,\n" +
                "        hmac-sha2-256, hmac-sha2-512, hmac-sha1, hmac-md5, hmac-sha1-96, hmac-md5-96");
        mockPropertyUtils("NC_SSH_CLIENT_CIPHERS", "aes128-ctr, aes192-ctr, aes256-ctr, arcfour256, arcfour128, aes128-cbc, 3des-cbc, blowfish-cbc,\n" +
                "        aes192-cbc, aes256-cbc");
        mockPropertyUtils("NC_SSH_CLIENT_DH_KEXS", "diffie-hellman-group1-sha1, diffie-hellman-group14-sha1, diffie-hellman-group14-sha256,\n" +
                "        diffie-hellman-group15-sha512, diffie-hellman-group16-sha512, diffie-hellman-group17-sha512,\n" +
                "        diffie-hellman-group18-sha512, diffie-hellman-group-exchange-sha1, diffie-hellman-group-exchange-sha256,\n" +
                "        ecdh-sha2-nistp256, ecdh-sha2-nistp384, ecdh-sha2-nistp521");
        setSshHelloMessageListener(true);
        Future<NetconfClientSession> futureSession = m_dispatcher.createClient(m_clientConfig);
        NetconfClientSession session = futureSession.get();
        assertEquals(9, ((SshNetconfClientSession) session).getSshClient().getMacFactories().size());

        assertEquals(10, ((SshNetconfClientSession) session).getSshClient().getCipherFactories().size());

        assertEquals(12, ((SshNetconfClientSession) session).getSshClient().getKeyExchangeFactories().size());
    }

    @Test
    public void testInvalidAlgorithms() throws NetconfClientDispatcherException {
        setSshHelloMessageListener(true);
        mockPropertyUtils("NC_SSH_CLIENT_MACS", "invalidMac");
        Future<NetconfClientSession> futureSession = m_dispatcher.createClient(m_clientConfig);
        try {
            futureSession.get();
            fail("should have thrown exception");
        } catch (Exception e) {
            assertEquals("MAC not supported / Invalid MAC : invalidMac", e.getCause().getCause().getMessage());
        }

        mockPropertyUtils("NC_SSH_CLIENT_MACS", "");
        mockPropertyUtils("NC_SSH_CLIENT_CIPHERS", "invalidCipher");
        futureSession = m_dispatcher.createClient(m_clientConfig);
        try {
            futureSession.get();
            fail("should have thrown exception");
        } catch (Exception e) {
            assertEquals("Cipher not supported / Invalid Cipher : invalidCipher", e.getCause().getCause().getMessage());
        }

        mockPropertyUtils("NC_SSH_CLIENT_MACS", "");
        mockPropertyUtils("NC_SSH_CLIENT_CIPHERS", "");
        mockPropertyUtils("NC_SSH_CLIENT_DH_KEXS", "invalidDhKex");
        futureSession = m_dispatcher.createClient(m_clientConfig);
        try {
            futureSession.get();
            fail("should have thrown exception");
        } catch (Exception e) {
            assertEquals("DH Kex not supported / Invalid DH Kex: invalidDhKex", e.getCause().getCause().getMessage());
        }
    }

    @Test
    public void verifyHellowMessageReadTimeoutThrowsException() throws Exception {
        setSshHelloMessageListener(false);
        Future<NetconfClientSession> futureSession = m_dispatcher.createClient(m_clientConfig);
        try {
            futureSession.get();
            fail("Should have thrown ExecutionException but did not!");
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause().getMessage().equals("could not receive netconf hello message from the netconf server within 50 millis"));
            assertTrue(e.getCause() instanceof NetconfClientDispatcherException);
        }
    }

    private class TestSshClientDispatcherImpl extends SshClientDispatcherImpl {
        @Override
        protected ConnectFuture getConnectFuture(SshClient sshClient, String username, InetSocketAddress socketAddress) throws IOException {
            return m_connectFuture;
        }
    }

    private void setSshHelloMessageListener(final boolean isHelloMessageReceived) {
        doAnswer(invocationOnMock -> {
            SshHelloMessageListener futureListener = (SshHelloMessageListener) invocationOnMock.getArguments()[0];
            futureListener.setHelloMessageRecieved(isHelloMessageReceived);
            return m_ioReadFuture;
        }).when(m_ioReadFuture).addListener(anyObject());
    }
}
