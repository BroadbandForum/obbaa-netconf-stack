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
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;

import io.netty.handler.ssl.SslProvider;
import junit.framework.TestCase;

import org.junit.Test;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.mockito.Mockito.mock;

public class NetconfTransportFactoryTest extends TestCase {
    NetconfTransportFactory factory = new NetconfTransportFactory();
    private String m_sshHostKeyPath = "hostKeyPath";
    private int m_sshHeartBeatIntervalSecs = 60;

    public void testFactoryMakesRightObjects() throws UnknownHostException, NetconfConfigurationBuilderException {
        InetSocketAddress expectedSockAddr = new InetSocketAddress(InetAddress.getLocalHost(), 1234);

        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setServerSocketAddress(expectedSockAddr);
        transportOrder.setTransportType(NetconfTransportProtocol.SSH.name());
        NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOrder);
        assertTrue(transport.getTranportProtocol().equals(NetconfTransportProtocol.SSH.name()));
        assertTrue(transport instanceof SshNetconfTransport);
        assertEquals(expectedSockAddr, ((SshNetconfTransport) transport).getSocketAddress());

        transportOrder.setTransportType("MY_VERY_OWN_KIND_OF_TRANSPORT");
        try {
            transport = NetconfTransportFactory.makeNetconfTransport(transportOrder);
            fail("Expected an exception here...");
        } catch (NetconfConfigurationBuilderException e) {
            // ok
        }
    }

    @Test
    public void testSSHTransportOptions() throws Exception {
        InetSocketAddress expectedSockAddr = new InetSocketAddress(InetAddress.getLocalHost(), 1234);

        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setServerSocketAddress(expectedSockAddr);
        transportOrder.setTransportType(NetconfTransportProtocol.SSH.name());
        transportOrder.setServerSshHostKeyPath(m_sshHostKeyPath);
        transportOrder.setHeartbeatInterval(m_sshHeartBeatIntervalSecs);
        NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOrder);
        assertTrue(transport.getTranportProtocol().equals(NetconfTransportProtocol.SSH.name()));
        assertTrue(transport instanceof SshNetconfTransport);
        assertEquals(expectedSockAddr, ((SshNetconfTransport) transport).getSocketAddress());
        assertEquals(m_sshHostKeyPath, ((SshNetconfTransport) transport).getHostKeyPath());
        assertEquals(m_sshHeartBeatIntervalSecs, ((SshNetconfTransport) transport).getHeartBeatInterval());
        assertNotNull(((SshNetconfTransport) transport).toString());
    }

    @Test
    public void testCallHomeTLSTransportOptions() throws NetconfConfigurationBuilderException {
        NetconfTransportOrder transportOrder = getCallHomeTLSTransport();
        transportOrder.setAllowSelfSigned(true);
        AbstractTLSNetconfTransport transport = assertValidationPasses(transportOrder);
        assertTrue(SslProvider.JDK.equals(transport.getSslProvider()));

        transportOrder = getCallHomeTLSTransport();
        transportOrder.setTrustManager(mock(TrustManager.class));
        transportOrder.setAllowSelfSigned(false);
        transportOrder.setCertificateChain(mock(File.class));
        transportOrder.setSslProvider(SslProvider.OPENSSL);
        transport = assertValidationPasses(transportOrder);
        assertTrue(SslProvider.OPENSSL.equals(transport.getSslProvider()));

        transportOrder = getCallHomeTLSTransport();
        transportOrder.setTrustChain(mock(File.class));
        transportOrder.setCertificateChain(mock(File.class));
        transportOrder.setAllowSelfSigned(false);
        assertValidationPasses(transportOrder);

        transportOrder = getCallHomeTLSTransport();
        transportOrder.setKeyManager(mock(KeyManager.class));
        transportOrder.setAllowSelfSigned(true);
        assertValidationFails(transportOrder);

        transportOrder = getCallHomeTLSTransport();
        transportOrder.setKeyManager(mock(KeyManager.class));
        transportOrder.setAllowSelfSigned(false);
        transportOrder.setCertificateChain(mock(File.class));
        assertValidationPasses(transportOrder);

    }

    @Test
    public void testWithOpenSSlProvider() throws NetconfConfigurationBuilderException {
        NetconfTransportOrder transportOrder = getCallHomeTLSTransport();
        transportOrder.setSslProvider(SslProvider.OPENSSL);
        transportOrder.setAllowSelfSigned(false);
        assertValidationPasses(transportOrder);

        transportOrder = getCallHomeTLSTransport();
        transportOrder.setSslProvider(SslProvider.OPENSSL);
        transportOrder.setAllowSelfSigned(false);
        transportOrder.setCertificateChain(mock(File.class));
        assertValidationPasses(transportOrder);
    }

    private void assertValidationFails(NetconfTransportOrder transportOrder) {
        try {
            NetconfTransportFactory.makeNetconfTransport(transportOrder);
            fail("expected an exception due to invalid NetconfTransportOrder");
        } catch (NetconfConfigurationBuilderException e) {
            //expected
        }
    }

    private NetconfTransportOrder getCallHomeTLSTransport() {
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        transportOrder.setCallHomeIp("127.0.0.1");
        transportOrder.setCallHomePort(4335);
        transportOrder.setTlsHandshaketimeoutMillis(20000);
        return transportOrder;
    }

    private AbstractTLSNetconfTransport assertValidationPasses(NetconfTransportOrder transportOder) throws
            NetconfConfigurationBuilderException {
        NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOder);
        assertNotNull(transport);
        assertTrue(transport instanceof AbstractTLSNetconfTransport);
        return (AbstractTLSNetconfTransport) transport;
    }


}

