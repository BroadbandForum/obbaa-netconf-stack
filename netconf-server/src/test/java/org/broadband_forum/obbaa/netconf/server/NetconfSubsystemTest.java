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

package org.broadband_forum.obbaa.netconf.server;

import org.broadband_forum.obbaa.netconf.api.logger.DefaultNetconfLogger;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.server.DefaultSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.broadband_forum.obbaa.netconf.server.ssh.NetconfSubsystem;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.RPC_EOM_DELIMITER;
import static org.broadband_forum.obbaa.netconf.server.ssh.NetconfSubsystem.HELLO_MESSAGE_NOT_RECIEVED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NetconfSubsystemTest {

    private static final String HELLO_MSG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <capabilities>\n" +
            "        <capability>urn:ietf:params:netconf:base:1.0</capability>\n" +
            "        <capability>urn:ietf:params:netconf:base:1.1</capability>\n" +
            "        <capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>\n" +
            "    </capabilities>\n" +
            "</hello>"+RPC_EOM_DELIMITER;

    private static final String HELLO_MSG2 = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xc:hello xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <xc:capabilities>\n" +
            "        <xc:capability>urn:ietf:params:netconf:base:1.0</xc:capability>\n" +
            "        <xc:capability>urn:ietf:params:netconf:base:1.1</xc:capability>\n" +
            "        <xc:capability>urn:ietf:params:netconf:capability:candidate:1.0</xc:capability>\n" +
            "    </xc:capabilities>\n" +
            "</xc:hello>"+RPC_EOM_DELIMITER;

    private static final String HELLO_MSG3 = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- some comment -->"+
            "<xc:hello xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <xc:capabilities>\n" +
            "        <xc:capability>urn:ietf:params:netconf:base:1.0</xc:capability>\n" +
            "        <xc:capability>urn:ietf:params:netconf:base:1.1</xc:capability>\n" +
            "        <xc:capability>urn:ietf:params:netconf:capability:candidate:1.0</xc:capability>\n" +
            "    </xc:capabilities>\n" +
            "</xc:hello>"+RPC_EOM_DELIMITER;
    private static final String GET = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
            "    <edit-config>\n" +
            "        <target>\n" +
            "            <running />\n" +
            "        </target>\n" +
            "        <test-option>set</test-option>\n" +
            "        <config>\n" +
            "            <device-manager xmlns=\"http://www.test-company.com/solutions/anv\" " +
            "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "                <device-holder xmlns=\"http://www.test-company.com/solutions/anv-device-holders\">\n" +
            "                    <name>OLT1</name>\n" +
            "                    <device xc:operation=\"replace\">\n" +
            "                        <device-id>ONT1</device-id>\n" +
            "                        <hardware-type>G.FAST</hardware-type>\n" +
            "                        <interface-version>1.0</interface-version>\n" +
            "                        <duid>OLT1.ONT1</duid>\n" +
            "                    </device>\n" +
            "                </device-holder>\n" +
            "            </device-manager>\n" +
            "        </config>\n" +
            "    </edit-config>\n" +
            "</rpc>"+RPC_EOM_DELIMITER;

    private NetconfSubsystem m_subsystem;
    private NetconfLogger m_netconfLogger;
    private NetconfServerAuthenticationHandler m_serverAuthHandler;
    private NetconfServerMessageListener m_serverMsgListener;
    private ServerMessageHandler m_serverMsgHandler;
    private NetconfSessionIdProvider m_sessionIdProvider;
    private ExitCallback m_exitCallBack;

    @Before
    public void setUp() throws IOException {
        m_netconfLogger = new DefaultNetconfLogger();
        m_serverAuthHandler = mock(NetconfServerAuthenticationHandler.class);
        m_serverMsgListener = mock(NetconfServerMessageListener.class);
        m_serverMsgHandler = mock(ServerMessageHandler.class);
        m_sessionIdProvider = new DefaultSessionIdProvider();
        m_subsystem = new NetconfSubsystem(m_serverMsgListener, m_serverMsgHandler, Collections.emptySet(), m_sessionIdProvider


        );
        m_exitCallBack = mock(ExitCallback.class);
        m_subsystem.setExitCallback(m_exitCallBack);
        m_subsystem.setSessionId(1);
        IoOutputStream mock = mock(IoOutputStream.class);
        when(mock.write(anyObject())).thenReturn(mock(IoWriteFuture.class));
        m_subsystem.setIoOutputStream(mock);
        ChannelSession channelSession = mock(ChannelSession.class);
        Session session = mock(Session.class);
        IoSession ioSession = mock(IoSession.class);
        when(ioSession.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1234));
        when(session.getIoSession()).thenReturn(ioSession);
        when(channelSession.getSession()).thenReturn(session);
        ServerSession serverSession = mock(ServerSession.class);
        when(serverSession.getIoSession()).thenReturn(ioSession);
        when(channelSession.getServerSession()).thenReturn(serverSession);
        m_subsystem.setChannelSession(channelSession);
        m_subsystem.start(mock(Environment.class));
    }

    @Test
    public void testData_HelloMsg() throws IOException {
        ChannelSession channel = mock(ChannelSession.class);
        assertEquals(HELLO_MSG.getBytes().length, m_subsystem.data(channel, HELLO_MSG.getBytes(), 0, HELLO_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verify(m_exitCallBack, never()).onExit(anyInt());
        verify(m_exitCallBack, never()).onExit(anyInt(), anyString());
    }

    @Test
    public void testData_HelloMsg2() throws IOException {
        ChannelSession channel = mock(ChannelSession.class);
        assertEquals(HELLO_MSG2.getBytes().length, m_subsystem.data(channel, HELLO_MSG2.getBytes(), 0, HELLO_MSG2.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verify(m_exitCallBack, never()).onExit(anyInt());
        verify(m_exitCallBack, never()).onExit(anyInt(), anyString());
    }

    @Test
    public void testData_HelloMsg3() throws IOException {
        ChannelSession channel = mock(ChannelSession.class);
        assertEquals(HELLO_MSG3.getBytes().length, m_subsystem.data(channel, HELLO_MSG3.getBytes(), 0, HELLO_MSG3.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verify(m_exitCallBack, never()).onExit(anyInt());
        verify(m_exitCallBack, never()).onExit(anyInt(), anyString());
    }

    @Test
    public void testData_HelloMsgNotRecieved() throws IOException {
        ChannelSession channel = mock(ChannelSession.class);
        assertEquals(GET.getBytes().length, m_subsystem.data(channel, GET.getBytes(), 0, GET.getBytes().length));
        assertEquals(false, m_subsystem.isHelloReceived());
        verify(m_exitCallBack).onExit(1, HELLO_MESSAGE_NOT_RECIEVED);
    }

    @Test
    public void testData_messageHandlerThrowsException() throws IOException {
        ChannelSession channel = mock(ChannelSession.class);
        assertEquals(HELLO_MSG3.getBytes().length, m_subsystem.data(channel, HELLO_MSG3.getBytes(), 0, HELLO_MSG3.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        m_subsystem.sendHelloToClient();
        RuntimeException exception = new RuntimeException("test");
        try{
            doThrow(exception).when(m_serverMsgHandler).processRequest(anyObject(), anyObject(), anyObject());
            m_subsystem.data(channel, GET.getBytes(), 0, GET.getBytes().length);
            fail("Expected IOException here");
        }catch (IOException e ){
            assertEquals(exception, e.getCause());
        }
    }
}
