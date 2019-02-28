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

package org.broadband_forum.obbaa.netconf.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.client.tls.TlsNettyChannelNetconfClientSession;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;


public class TlsNettyChannelNetconfClientSessionTest {
    
    private static final String EXPECTED_CLIENT_HELLO_MESSAGE = "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "<capabilities>\n" +
            "<capability>urn:ietf:params:netconf:base:1.0</capability>\n" +
            "<capability>urn:ietf:params:netconf:base:1.1</capability>\n" +
            "</capabilities>\n" +
            "</hello>\n" +
            "]]>]]>";
    
    private SocketChannel m_channel;

    private TlsNettyChannelNetconfClientSession m_tlsClientSession;
    private SocketChannelConfig m_channelConfig;

    @Before
    public void setUp() throws Exception {
        m_channel = mock(SocketChannel.class);
        m_channelConfig = mock(SocketChannelConfig.class);
        when(m_channel.localAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 4335));
        when(m_channel.remoteAddress()).thenReturn(new InetSocketAddress("135.0.0.1", 9496));
        m_tlsClientSession = new TlsNettyChannelNetconfClientSession(m_channel);
    }

    @Test
    public void testSendHelloMessage() {
        //prepare capability
        Set<String> clientCapability = new LinkedHashSet<>();
        clientCapability.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        clientCapability.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        
        //test send helo message
        m_tlsClientSession.sendHelloMessage(clientCapability);
        
        verify(m_channel, times(1)).writeAndFlush(EXPECTED_CLIENT_HELLO_MESSAGE);
    }

    @Test
    public void testIsOpen(){
        when(m_channel.isOpen()).thenReturn(true);
        assertTrue(m_tlsClientSession.isOpen());

        when(m_channel.isOpen()).thenReturn(false);
        assertFalse(m_tlsClientSession.isOpen());

        m_tlsClientSession.setServerChannel(null);
        when(m_channel.isOpen()).thenReturn(false);
    }

    @Test
    public void testToString() {
        assertEquals("TlsNettyChannelNetconfClientSession{localsocket=/127.0.0.1:4335, remotesocket=/135.0.0.1:9496, creationtime=" +
                NetconfResources.DATE_TIME_FORMATTER.print(m_tlsClientSession.getCreationTime()) + "}",
                m_tlsClientSession.toString());
    }

    @Test
    public void testSetTcpKA() {
        when(m_channel.config()).thenReturn(m_channelConfig);
        when(m_channelConfig.setKeepAlive(anyBoolean())).thenReturn(m_channelConfig);
        m_tlsClientSession.setTcpKeepAlive(false);
        verify(m_channel.config()).setKeepAlive(false);

        m_tlsClientSession.setTcpKeepAlive(true);
        verify(m_channel.config()).setKeepAlive(true);

        doThrow(new RuntimeException("Exception setting TCP KA")).when(m_channelConfig).setKeepAlive(false);
        try {
            m_tlsClientSession.setTcpKeepAlive(false);
        } catch(Exception e) {
            assertEquals("Exception setting TCP KA", e.getMessage());
        }
    }
}
