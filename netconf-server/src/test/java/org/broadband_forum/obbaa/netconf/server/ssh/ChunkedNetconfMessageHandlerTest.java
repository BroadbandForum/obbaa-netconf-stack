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
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.custommonkey.xmlunit.Diff;
import org.junit.Test;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;

/**
 * Created by keshava on 8/12/15.
 */
public class ChunkedNetconfMessageHandlerTest {
    private static final Logger LOGGER = Logger.getLogger(ChunkedNetconfMessageHandlerTest.class);

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException {
        ChunkedNetconfMessageHandler messageHandler = new ChunkedNetconfMessageHandler(null, null, null, null, null);
        Document actual = messageHandler.getRequestDocument(FileUtil
                .loadAsString("/chunkednetconfmessagehandlertest/sampleChunkedMessage1.txt"));
        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString
                ("/chunkednetconfmessagehandlertest/expectedMessage1.txt"));
        Diff diff = new Diff(expected, actual);
        boolean similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = messageHandler.getRequestDocument(FileUtil.loadAsString
                ("/chunkednetconfmessagehandlertest/sampleChunkedMessage2.txt"));
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString
                ("/chunkednetconfmessagehandlertest/expectedMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = messageHandler.getRequestDocument(FileUtil.loadAsString
                ("/chunkednetconfmessagehandlertest/sampleChunkedMessage4.txt"));
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString
                ("/chunkednetconfmessagehandlertest/expectedMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

    }

    @Test
    public void testGetResponseBytes() throws NetconfMessageBuilderException {
        ChunkedNetconfMessageHandler messageHandler = new ChunkedNetconfMessageHandler(null, null, null, null, null);
        Document sampleResponse = DocumentUtils.stringToDocument(FileUtil
                .loadAsString("/chunkednetconfmessagehandlertest/sampleResponse.xml"));

        // the document itself has 2931 bytes, \n##\n will take 4 bytes , \n#2931\n will take another
        // 7 bytes
        Long expectedLength = new Long(DocumentUtils.documentToString(sampleResponse).getBytes().length + "\n#2931\n"
                .getBytes().length
                + "\n##\n".getBytes().length);

        assertEquals(expectedLength, new Long(messageHandler.getResponseBytes(sampleResponse).length));

    }

    @Test
    public void testCloseSessionSendResponseWaits() throws Exception {
        IoOutputStream ioStream = mock(IoOutputStream.class);
        IoWriteFuture writeFuture = mock(IoWriteFuture.class);
        when(ioStream.write(anyObject())).thenReturn(writeFuture);
        NetconfServerMessageListener messageListener = mock(NetconfServerMessageListener.class);
        doNothing().when(messageListener).sessionClosed(anyString(), anyInt());
        NetconfClientInfo clientInfo = mock(NetconfClientInfo.class);
        when(clientInfo.getSessionId()).thenReturn(1);
        ChannelSession channel = mock(ChannelSession.class);
        ServerSession session = mock(ServerSession.class);
        CloseFuture closeFuture = mock(CloseFuture.class);
        when(session.close(true)).thenReturn(closeFuture);
        when(channel.getSession()).thenReturn(session);
        ChunkedNetconfMessageHandler messageHandler = new ChunkedNetconfMessageHandler(null, ioStream, mock
                (ExitCallback.class), null, channel);
        messageHandler.setNetConfClientInfo(clientInfo);
        messageHandler.m_serverMessageListener = messageListener;
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        messageHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getCloseSession(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><close-session/></rpc>")));

        verify(ioStream).write(anyObject());
        verify(writeFuture).await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testCloseSessionWithException() throws Exception {
        IoOutputStream ioStream = mock(IoOutputStream.class);
        IoWriteFuture writeFuture = mock(IoWriteFuture.class);
        when(ioStream.write(anyObject())).thenReturn(writeFuture);
        NetconfServerMessageListener messageListener = mock(NetconfServerMessageListener.class);
        doNothing().when(messageListener).sessionClosed(anyString(), anyInt());
        NetconfClientInfo clientInfo = mock(NetconfClientInfo.class);
        when(clientInfo.getSessionId()).thenReturn(1);
        ChannelSession channel = mock(ChannelSession.class);
        ServerSession session = mock(ServerSession.class);
        CloseFuture closeFuture = mock(CloseFuture.class);
        when(session.close(true)).thenReturn(closeFuture);
        when(channel.getSession()).thenReturn(session);
        when(ioStream.write(any(Buffer.class))).thenThrow(new RuntimeException("closed"));
        ChunkedNetconfMessageHandler messageHandler = new ChunkedNetconfMessageHandler(null, ioStream, mock
                (ExitCallback.class), null, channel);
        messageHandler.setNetConfClientInfo(clientInfo);
        messageHandler.m_serverMessageListener = messageListener;
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
    public void testGetSendResponseDoesNotWait() throws Exception {
        IoOutputStream ioStream = mock(IoOutputStream.class);
        IoWriteFuture writeFuture = mock(IoWriteFuture.class);
        when(ioStream.write(anyObject())).thenReturn(writeFuture);
        ChunkedNetconfMessageHandler messageHandler = new ChunkedNetconfMessageHandler(null, ioStream, mock
                (ExitCallback.class), null, null);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        response.setOk(true);
        messageHandler.getResponseChannel().sendResponse(response, DocumentToPojoTransformer.getGet(
                DocumentUtils.stringToDocument("<rpc message-id=\"2\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><get/></rpc>")));

        verify(ioStream).write(anyObject());
        verify(writeFuture, never()).await(anyInt(), anyObject());
    }
}
