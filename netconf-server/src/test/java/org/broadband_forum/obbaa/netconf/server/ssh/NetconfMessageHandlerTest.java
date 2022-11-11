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

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.codec.v2.FrameAwareNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.codec.v2.FrameAwareNetconfMessageCodecV2Impl;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.custommonkey.xmlunit.Diff;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;

import io.netty.buffer.ByteBuf;

/**
 * Created by keshava on 8/12/15.
 */
public class NetconfMessageHandlerTest {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NetconfMessageHandlerTest.class, LogAppNames.NETCONF_LIB);

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
    ByteBuf m_byteBuf = unpooledHeapByteBuf();

    private NetconfMessageHandler m_msgHandler;
    private FrameAwareNetconfMessageCodecV2 m_codec;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_codec = new FrameAwareNetconfMessageCodecV2Impl();
        m_msgHandler = new NetconfMessageHandler(m_msgListener, m_outputStream, m_exitCallBack, m_serverMessageHandler, m_channel, m_codec);
        m_msgHandler.onHello(m_netconfClientInfo, new HashSet<String>());
    }

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException, MessageToolargeException {
        m_msgHandler.useChunkedFraming();
        Document actual = m_msgHandler.decode(m_byteBuf.writeBytes(FileUtil
                .loadAsString("/netconfmessagehandlertest/sampleChunkedMessage1.txt").getBytes())).getDocument();
        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/netconfmessagehandlertest/expectedMessage1.txt"));
        Diff diff = new Diff(expected, actual);
        boolean similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual =
                m_msgHandler.decode(m_byteBuf.writeBytes(FileUtil.loadAsString("/netconfmessagehandlertest/sampleChunkedMessage2.txt").getBytes())).getDocument();
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/netconfmessagehandlertest/expectedMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual =
                m_msgHandler.decode(m_byteBuf.writeBytes(FileUtil.loadAsString("/netconfmessagehandlertest/sampleChunkedMessage4.txt").getBytes())).getDocument();
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/netconfmessagehandlertest/expectedMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

    }

    @Test
    public void testGetResponseBytes() throws NetconfMessageBuilderException {
        m_msgHandler.useChunkedFraming();
        Document sampleResponse = DocumentUtils.stringToDocument(FileUtil
                .loadAsString("/netconfmessagehandlertest/sampleResponse.xml"));

        // the document itself has 2931 bytes, \n##\n will take 4 bytes , \n#2931\n will take another
        // 7 bytes
        Long expectedLength = new Long(DocumentUtils.documentToString(sampleResponse).getBytes().length + "\n#2931\n".getBytes().length
                + "\n##\n".getBytes().length);

        assertEquals(expectedLength, new Long(m_msgHandler.encode(sampleResponse).length));

    }

    @Test
    public void testCloseSessionSendResponseWaits() throws Exception{
        IoOutputStream ioStream = mock(IoOutputStream.class);
        IoWriteFuture writeFuture = mock(IoWriteFuture.class);
        when(ioStream.writePacket(anyObject())).thenReturn(writeFuture);
        NetconfServerMessageListener messageListener = mock(NetconfServerMessageListener.class);
        doNothing().when(messageListener).sessionClosed(anyString(),anyInt());
        NetconfClientInfo clientInfo = mock(NetconfClientInfo.class);
        when(clientInfo.getSessionId()).thenReturn(1);
        ChannelSession channel =mock(ChannelSession.class);
        ServerSession session = mock(ServerSession.class);
        CloseFuture closeFuture = mock(CloseFuture.class);
        when(session.close(true)).thenReturn(closeFuture);
        when(channel.getSession()).thenReturn(session);
        NetconfMessageHandler messageHandler = new NetconfMessageHandler(null, ioStream, mock(ExitCallback.class), null,channel, m_codec);
        messageHandler.setNetConfClientInfo(clientInfo);
        messageHandler.m_serverMessageListener=messageListener;
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        messageHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getCloseSession(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><close-session/></rpc>")));

        verify(ioStream).writePacket(anyObject());
        verify(writeFuture).await(10, TimeUnit.SECONDS);
    }
    
    @Test
    public void testCloseSessionWithException() throws Exception{
        IoOutputStream ioStream = mock(IoOutputStream.class);
        IoWriteFuture writeFuture = mock(IoWriteFuture.class);
        when(ioStream.writePacket(anyObject())).thenReturn(writeFuture);
        NetconfServerMessageListener messageListener = mock(NetconfServerMessageListener.class);
        doNothing().when(messageListener).sessionClosed(anyString(),anyInt());
        NetconfClientInfo clientInfo = mock(NetconfClientInfo.class);
        when(clientInfo.getSessionId()).thenReturn(1);
        ChannelSession channel =mock(ChannelSession.class);
        ServerSession session = mock(ServerSession.class);
        CloseFuture closeFuture = mock(CloseFuture.class);
        when(session.close(true)).thenReturn(closeFuture);
        when(channel.getSession()).thenReturn(session);
        when (ioStream.writePacket(any(Buffer.class))).thenThrow(new RuntimeException("closed"));
        NetconfMessageHandler messageHandler = new NetconfMessageHandler(null, ioStream, mock(ExitCallback.class), null,channel, m_codec);
        messageHandler.setNetConfClientInfo(clientInfo);
        messageHandler.m_serverMessageListener=messageListener;
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        messageHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getCloseSession(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><close-session/></rpc>")));
        
        assertTrue(response.getMessageSentFuture().isCompletedExceptionally());
        verify(writeFuture, never()).await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testKillSession_NotCloseExistingSession() throws NetconfMessageBuilderException, IOException {
        IoOutputStream ioStream = mock(IoOutputStream.class);
        IoWriteFuture writeFuture = mock(IoWriteFuture.class);
        when(ioStream.writePacket(anyObject())).thenReturn(writeFuture);
        ChannelSession channel = mock(ChannelSession.class);
        NetconfClientInfo clientInfo = mock(NetconfClientInfo.class);
        when(clientInfo.getSessionId()).thenReturn(1);
        String killSessionRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\n" +
                "    <kill-session>\n" +
                "        <session-id>4</session-id>\n" +
                "    </kill-session>\n" +
                "</rpc>";
        NetconfMessageHandler messageHandler = new NetconfMessageHandler(null, ioStream, mock(ExitCallback.class), null, channel, m_codec);
        messageHandler.setNetConfClientInfo(clientInfo);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("101");
        response.setOk(true);
        messageHandler.getResponseChannel().sendResponse(response,
                DocumentToPojoTransformer.getKillSession(DocumentUtils.stringToDocument(killSessionRequest)));
        assertFalse(messageHandler.getResponseChannel().isSessionClosed());
    }

    @Test
    public void testGetSendResponseDoesNotWait() throws Exception{
        IoOutputStream ioStream = mock(IoOutputStream.class);
        IoWriteFuture writeFuture = mock(IoWriteFuture.class);
        when(ioStream.writePacket(anyObject())).thenReturn(writeFuture);
        NetconfMessageHandler messageHandler = new NetconfMessageHandler(null, ioStream, mock(ExitCallback.class), null,null, m_codec);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        messageHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getGet(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><get/></rpc>")));

        verify(ioStream).writePacket(anyObject());
        verify(writeFuture, never()).await(anyInt(), anyObject());
    }

    @Test
    public void testValidXmlRequest() throws NetconfMessageBuilderException {
        ArgumentCaptor<NetconfClientInfo> clientCaptor = ArgumentCaptor.forClass(NetconfClientInfo.class);
        when(m_netconfClientInfo.getUsername()).thenReturn("testUser");
        when(m_netconfClientInfo.getSessionId()).thenReturn(1);
        when(m_netconfClientInfo.getRemoteHost()).thenReturn("1.1.1.1");
        when(m_netconfClientInfo.getRemotePort()).thenReturn("1");
        String docString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1001\"> <get> </get></rpc>";
        DocumentInfo documentinfo = new DocumentInfo(DocumentUtils.stringToDocument(docString), docString);
        m_msgHandler.processRequest(documentinfo);
        verify(m_serverMessageHandler).processRequest(clientCaptor.capture(), any(GetRequest.class), any(ResponseChannel.class));
        assertEquals(m_netconfClientInfo, clientCaptor.getValue());
    }

    @Test
    public void testValidActionRequest() throws NetconfMessageBuilderException {
        ArgumentCaptor<NetconfClientInfo> clientCaptor = ArgumentCaptor.forClass(NetconfClientInfo.class);
        when(m_netconfClientInfo.getUsername()).thenReturn("testUser");
        when(m_netconfClientInfo.getSessionId()).thenReturn(1);
        when(m_netconfClientInfo.getRemoteHost()).thenReturn("1.1.1.1");
        when(m_netconfClientInfo.getRemotePort()).thenReturn("1");

        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
                + "<test:server xmlns:test=\"urn:example:server-farm\">"
                +"<test:name>apache-1</test:name>"
                +"<test:reset>"
                + "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"
                +"</test:reset>"
                +"</test:server>"
                +"</action>"
                +"</rpc>";

        DocumentInfo documentInfo = new DocumentInfo(DocumentUtils.stringToDocument(req), req);
        m_msgHandler.processRequest(documentInfo);
        verify(m_serverMessageHandler).processRequest(clientCaptor.capture(), any(GetRequest.class), any(ResponseChannel.class));
        assertEquals(m_netconfClientInfo, clientCaptor.getValue());
    }
}
