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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.server.ExitCallback;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Splitter;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import junit.framework.TestCase;

public class SecureNetconfServerHandlerTest extends TestCase {

    public static final String GET_REQ = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1001\"> <get> </get></rpc>]]>]]>";
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
    @Mock
    private ChannelPipeline m_pipeline;

    SecureNetconfServerHandler m_msgHandler;

    HashSet<String> caps = new HashSet<>();
    private static final String HELLO_10 = "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><capabilities></capabilities></hello>]]>]]>";


    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        caps.add("urn:ietf:params:netconf:base:1.0");
        caps.add("urn:ietf:params:netconf:base:1.1");
        m_msgHandler = new SecureNetconfServerHandler(m_msgListener, m_serverMessageHandler, caps, 1, m_netconfLogger);
        when(m_context.channel()).thenReturn(m_channel);
        when(m_channel.remoteAddress()).thenReturn(mock(SocketAddress.class));
        m_msgHandler.channelActive(m_context);
        when(m_channel.close()).thenReturn(m_future);
        when(m_context.pipeline()).thenReturn(m_pipeline);
    }

    public void testInvalidXmlRequest() throws Exception {
        m_msgHandler.channelRead0(m_context, HELLO_10);
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        try {
            m_msgHandler.channelRead0(m_context, "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1001\"> <get></rpc>]]>]]>");
        } catch (NetconfMessageBuilderException e){
            assertEquals("Error while converting string to xml document" ,e.getCause().getMessage());
        }
        verify(m_channel ,never()).writeAndFlush(anyString());
        assertTrue(m_msgHandler.getByteBuf().refCnt() > 0);
    }

    public void testCloseSessionRequest() throws Exception {
        m_msgHandler.channelRead0(m_context, HELLO_10);
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        m_msgHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getCloseSession(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><close-session/></rpc>")));
        verify(m_channel).writeAndFlush(anyString());
        verify(m_writeFuture).await(10, TimeUnit.SECONDS);
        assertEquals(0, m_msgHandler.getByteBuf().refCnt());
    }

    public void testSendResponse_exception() throws Exception {
        m_msgHandler.channelRead0(m_context, HELLO_10);
        when(m_channel.writeAndFlush(anyString())).thenThrow(new RuntimeException("closed"));
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        m_msgHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getCloseSession(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><close-session/></rpc>")));
        assertTrue(response.getMessageSentFuture().isCompletedExceptionally());
        verify(m_writeFuture, never()).await(10, TimeUnit.SECONDS);
        assertEquals(0, m_msgHandler.getByteBuf().refCnt());
    }

    public void testSendResponseToGetRequest() throws Exception {
        m_msgHandler.channelRead0(m_context, HELLO_10);
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
        m_msgHandler.channelRead0(m_context,HELLO_10);
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        ArgumentCaptor<NetconfClientInfo> clientCaptor = ArgumentCaptor.forClass(NetconfClientInfo.class);
        m_msgHandler.channelRead0(m_context,
               "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1001\"> <get> </get></rpc>]]>]]>");
        verify(m_serverMessageHandler).processRequest(clientCaptor.capture(), any(GetRequest.class), any(ResponseChannel.class));
    }

    public void testValid2XmlRequests() throws Exception {
        m_msgHandler.channelRead0(m_context,HELLO_10);
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        ArgumentCaptor<NetconfClientInfo> clientCaptor = ArgumentCaptor.forClass(NetconfClientInfo.class);
        m_msgHandler.channelRead0(m_context,
               (GET_REQ + "\n\n       \n" +GET_REQ));
        verify(m_serverMessageHandler, times(2)).processRequest(clientCaptor.capture(), any(GetRequest.class), any(ResponseChannel.class));
    }

    public void testValidXmlsBeingSentInPackets1() throws Exception {
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        ArgumentCaptor<NetconfClientInfo> clientCaptor = ArgumentCaptor.forClass(NetconfClientInfo.class);
        sendMessageInPackets(HELLO_10 + GET_REQ + GET_REQ);
        verify(m_serverMessageHandler, times(2)).processRequest(clientCaptor.capture(), any(GetRequest.class), any(ResponseChannel.class));
    }

    public void testForChannelInactiveByteBufIsReleased() throws Exception {
        m_msgHandler.channelInactive(m_context);
        assertEquals(0, m_msgHandler.getByteBuf().refCnt());
    }

    public void testBufferIsReleasedWhenCloseSessionCalled() {
        m_msgHandler.close();
        assertEquals(0, m_msgHandler.getByteBuf().refCnt());
    }

    public void testMalformedHello1_1() throws Exception {
        when(m_channel.isOpen()).thenReturn(true);
        String hello11 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                " <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "   <capabilities>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.0\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.1\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:ns:netconf:capability:startup:1.0\n" +
                "     </capability>\n" +
                "   </capabilities>\n" +
                "   <session-id>\n" + //no closing tag for session-id
                " </hello>\n" +
                "]]>]]>";
        m_msgHandler.channelRead0(m_context, hello11);
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);
        verify(m_channel).close();
    }

    public void testHello1_1FollowedByIncorrectChunkSize() throws Exception {
        when(m_channel.isOpen()).thenReturn(true);
        String hello11 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                " <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "   <capabilities>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.0\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.1\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:ns:netconf:capability:startup:1.0\n" +
                "     </capability>\n" +
                "   </capabilities>\n" +
                "   <session-id>4</session-id>\n" +
                " </hello>\n" +
                "]]>]]>";
        m_msgHandler.channelRead0(m_context, hello11);
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);


        //chunk size is wrong -> Throws some other exception
        String chunkedGet = "\n#20\n<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"30\">\n" +
                "    <get/>\n" +
                "</rpc>\n##\n";
        m_msgHandler.channelRead0(m_context, chunkedGet);
        verify(m_channel).close();
    }

    public void testHello1_1FollowedByMalformedRPC() throws Exception {
        when(m_channel.isOpen()).thenReturn(true);
        String hello11 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                " <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "   <capabilities>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.0\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.1\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:ns:netconf:capability:startup:1.0\n" +
                "     </capability>\n" +
                "   </capabilities>\n" +
                "   <session-id>4</session-id>\n" +
                " </hello>\n" +
                "]]>]]>";
        m_msgHandler.channelRead0(m_context, hello11);
        when(m_channel.writeAndFlush(anyString())).thenReturn(m_writeFuture);

        String chunkedGet = "\n#86\n<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"30\">\n" +
                "    <get>\n" + // no closing tag for get
                "</rpc>\n##\n";
        m_msgHandler.channelRead0(m_context, chunkedGet);
        verify(m_channel, times(0)).close();
    }

    private void sendMessageInPackets(String message) throws Exception {
        for(final String token :
                Splitter
                        .fixedLength(10)
                        .split(message)){
            m_msgHandler.channelRead0(m_context, token);
        }
    }
}
