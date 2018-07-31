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

package org.broadband_forum.obbaa.netconf.server.tls;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import junit.framework.TestCase;

import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.server.ExitCallback;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecureNetconfServerHandlerTest extends TestCase {

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
    ChannelHandlerContext m_context;
    @Mock
    Channel m_channel;
    @Mock
    ChannelFuture m_future;
    @Mock
    NetconfLogger m_netconfLogger;
    @Mock
    private ChannelFuture m_writeFuture;

    SecureNetconfServerHandler m_msgHandler;


    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_msgHandler = new SecureNetconfServerHandler(m_msgListener, m_serverMessageHandler, 1, m_netconfLogger);
        when(m_context.channel()).thenReturn(m_channel);
        when(m_channel.remoteAddress()).thenReturn(mock(SocketAddress.class));
        m_msgHandler.channelActive(m_context);
        when(m_channel.close()).thenReturn(m_future);
        m_msgHandler.channelRead0(m_context, "<hello " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><capabilities></capabilities></hello>");
    }

    public void testInvalidXmlRequest() throws Exception {
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        m_msgHandler.channelRead0(m_context, "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"1001\"> <get> </rpc>");
        verify(m_channel).writeAndFlush(anyString());
    }

    public void testCloseSessionRequest() throws Exception {
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        m_msgHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getCloseSession(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><close-session/></rpc>")));
        verify(m_channel).writeAndFlush(anyString());
        verify(m_writeFuture).await(10, TimeUnit.SECONDS);
    }

    public void testSendResponse_exception() throws Exception {
        when(m_channel.writeAndFlush(anyString())).thenThrow(new RuntimeException("closed"));
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        m_msgHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getCloseSession(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><close-session/></rpc>")));
        assertTrue(response.getMessageSentFuture().isCompletedExceptionally());
        verify(m_writeFuture, never()).await(10, TimeUnit.SECONDS);
    }

    public void testSendResponseToGetRequest() throws Exception {
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_msgHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getGet(
                DocumentUtils.stringToDocument("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                        "message-id=\"1001\"> <get/> </rpc>")));
        verify(m_channel).writeAndFlush(anyString());
        verify(m_writeFuture, never()).await(anyInt(), anyObject());
    }

    public void testValidXmlRequest() throws Exception {
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        ArgumentCaptor<NetconfClientInfo> clientCaptor = ArgumentCaptor.forClass(NetconfClientInfo.class);
        m_msgHandler.channelRead0(m_context,
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1001\"> <get> </get></rpc>");
        verify(m_serverMessageHandler).processRequest(clientCaptor.capture(), any(GetRequest.class), any
                (ResponseChannel.class));
    }

}
