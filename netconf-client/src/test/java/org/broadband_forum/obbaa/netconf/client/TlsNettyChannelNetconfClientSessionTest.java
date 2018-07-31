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

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.client.tls.TlsNettyChannelNetconfClientSession;

import io.netty.channel.Channel;

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TlsNettyChannelNetconfClientSessionTest {

    private static final String EXPECTED_CLIENT_HELLO_MESSAGE = "<hello " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
            + "<capabilities><capability>urn:ietf:params:netconf:base:1.0</capability>"
            + "<capability>urn:ietf:params:netconf:base:1.1</capability></capabilities></hello>]]>]]>";

    private Channel m_channel;

    private TlsNettyChannelNetconfClientSession m_tlsClientSession;

    @Before
    public void setUp() throws Exception {
        m_channel = mock(Channel.class);
        when(m_channel.localAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 4335));
        when(m_channel.remoteAddress()).thenReturn(new InetSocketAddress("135.0.0.1", 9496));
        m_tlsClientSession = new TlsNettyChannelNetconfClientSession(m_channel, mock(ExecutorService.class));
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
    public void testIsOpen() {
        when(m_channel.isOpen()).thenReturn(true);
        assertTrue(m_tlsClientSession.isOpen());

        when(m_channel.isOpen()).thenReturn(false);
        assertFalse(m_tlsClientSession.isOpen());

        m_tlsClientSession.setServerChannel(null);
        when(m_channel.isOpen()).thenReturn(false);
    }

    @Test
    public void testToString() {
        assertEquals("TlsNettyChannelNetconfClientSession{localsocket=/127.0.0.1:4335, remotesocket=/135.0.0.1:9496, " +
                        "creationtime=" +
                        NetconfResources.DATE_TIME_FORMATTER.print(m_tlsClientSession.getCreationTime()) + "}",
                m_tlsClientSession.toString());
    }

}
