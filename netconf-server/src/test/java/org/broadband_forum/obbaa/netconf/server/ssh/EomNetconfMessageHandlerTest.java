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

package org.broadband_forum.obbaa.netconf.server.ssh;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import junit.framework.TestCase;

import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EomNetconfMessageHandlerTest extends TestCase {

    @Mock
    NetconfServerMessageListener m_msgListener;
    @Mock
    IoOutputStream m_outputStream;
    @Mock
    ExitCallback m_exitCallBack;
    @Mock
    ServerMessageHandler m_serverMessageHandler;
    @Mock
    NetconfClientInfo m_netconfClientInfo;
    @Mock
    NetconfLogger m_netconfLogger;
    @Mock
    ChannelSession m_channel;

    EomNetconfMessageHandler m_msgHandler;

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_msgHandler = new EomNetconfMessageHandler(m_msgListener, m_outputStream, m_exitCallBack,
                m_serverMessageHandler, m_channel);
        m_msgHandler.onHello(m_netconfClientInfo, new HashSet<String>());
    }

    public void testInvalidXmlRequest() throws NetconfMessageBuilderException {
        m_msgHandler.processRequest("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1001\"> " +
                "<get> </rpc>]]>]]>");
        verify(m_outputStream).write(any(Buffer.class));
    }

    public void testValidXmlRequest() throws NetconfMessageBuilderException {
        ArgumentCaptor<NetconfClientInfo> clientCaptor = ArgumentCaptor.forClass(NetconfClientInfo.class);
        when(m_netconfClientInfo.getUsername()).thenReturn("testUser");
        when(m_netconfClientInfo.getSessionId()).thenReturn(1);
        when(m_netconfClientInfo.getRemoteHost()).thenReturn("1.1.1.1");
        when(m_netconfClientInfo.getRemotePort()).thenReturn("1");
        m_msgHandler.processRequest("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1001\"> " +
                "<get> </get></rpc>]]>]]>");
        verify(m_serverMessageHandler).processRequest(clientCaptor.capture(), any(GetRequest.class), any
                (ResponseChannel.class));
        assertEquals(m_netconfClientInfo, clientCaptor.getValue());
    }

    public void testValidActionRequest() throws NetconfMessageBuilderException {
        ArgumentCaptor<NetconfClientInfo> clientCaptor = ArgumentCaptor.forClass(NetconfClientInfo.class);
        when(m_netconfClientInfo.getUsername()).thenReturn("testUser");
        when(m_netconfClientInfo.getSessionId()).thenReturn(1);
        when(m_netconfClientInfo.getRemoteHost()).thenReturn("1.1.1.1");
        when(m_netconfClientInfo.getRemotePort()).thenReturn("1");

        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
                + "<test:server xmlns:test=\"urn:example:server-farm\">"
                + "<test:name>apache-1</test:name>"
                + "<test:reset>"
                + "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"
                + "</test:reset>"
                + "</test:server>"
                + "</action>"
                + "</rpc>"
                + "]]>]]>";

        m_msgHandler.processRequest(req);
        verify(m_serverMessageHandler).processRequest(clientCaptor.capture(), any(GetRequest.class), any
                (ResponseChannel.class));
        assertEquals(m_netconfClientInfo, clientCaptor.getValue());
    }

}
