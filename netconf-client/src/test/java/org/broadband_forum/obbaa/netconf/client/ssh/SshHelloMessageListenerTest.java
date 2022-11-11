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

package org.broadband_forum.obbaa.netconf.client.ssh;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getDocFromFile;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;

public class SshHelloMessageListenerTest extends XMLTestCase {
    private static final Boolean FALSE = new Boolean(false);
    private SshNetconfClientSession m_clientSession = spy(new TestSshNetconfClientSession());
    private ChannelSubsystem m_mockChannel = spy(new ChannelSubsystem("test"));
    private IoReadFuture m_mockFuture = mock(IoReadFuture.class);
    protected Buffer m_buffer = new ByteArrayBuffer();
    private IoInputStream m_mockStream = mock(IoInputStream.class);
    private SshFutureListener<IoReadFuture> m_futureListener;
    private SshHelloMessageListenerTest.TestIoReadFuture m_mockReadFuture = new SshHelloMessageListenerTest.TestIoReadFuture();
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshNetconfClientSessionListenerTest.class, LogAppNames.NETCONF_LIB);

    @Override
    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        doNothing().when(m_mockChannel).addChannelListener(any());
        when(m_mockChannel.getAsyncOut()).thenReturn(m_mockStream);
        when(m_mockStream.read((Buffer) Matchers.anyObject())).thenReturn(m_mockReadFuture);
        when(m_mockFuture.getBuffer()).then(new Answer<Buffer>() {
            @Override
            public Buffer answer(InvocationOnMock invocation) throws Throwable {
                return m_buffer;
            }
        });
        doReturn(true).when(m_mockChannel).isOpen();
    }

    public void testListenerSendsMessageToSessionAndSetsEOMListener() throws InterruptedException {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        createBuffer(listener, "sampleHelloMessage.txt", true);
        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleHelloMessage.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        assertTrue(m_futureListener instanceof SshNetconfClientSessionListener);
        assertTrue(listener.await(50));
    }

    public void testListenerSendsMessageToSessionAndSetsEOMListenerSendFullHello() throws InterruptedException {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        m_buffer.clear();
        m_buffer.putRawBytes(getMessageFromFile("sampleHelloMessage.txt").getBytes());
        listener.operationComplete(m_mockFuture);
        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleHelloMessage.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        assertTrue(m_futureListener instanceof SshNetconfClientSessionListener);
        assertTrue(listener.await(50));
    }

    public void testListenerSendsMessageToSessionAndSetsEOMListenerSendHelloInParts() throws InterruptedException {
        String hello1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                " <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "   <capabilities>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.0\n" +
                "     </capability>\n";
        String hello2 = "<capability>\n" +
                "       urn:ietf:params:ns:netconf:capability:startup:1.0\n" +
                "     </capability>\n" +
                "   </capabilities>\n" +
                "   <session-id>4</session-id>\n" +
                " </hello>\n" +
                "]]>]]>";
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        m_buffer.clear();
        m_buffer.putRawBytes(hello1.getBytes());
        listener.operationComplete(m_mockFuture);
        m_buffer.putRawBytes(hello2.getBytes());
        listener.operationComplete(m_mockFuture);
        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleHelloMessage.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        assertTrue(m_futureListener instanceof SshNetconfClientSessionListener);
        assertTrue(listener.await(50));
    }

    public void testListenerSendsMessageToSessionAndSetsChunkedListener() throws InterruptedException {
        Set<String> caps_with_1_1 = new HashSet<>();
        caps_with_1_1.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        caps_with_1_1.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        m_clientSession.setClientCapabilities(caps_with_1_1);

        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        createBuffer(listener, "sampleHelloMessageWith_1_1.txt", true);
        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleHelloMessageWith_1_1.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        assertTrue(m_futureListener instanceof SshNetconfClientSessionListener);
        assertTrue(listener.await(50));
    }
    public void testIfListenerClosesChannelIfNonHelloMessageIsRecieved() throws InterruptedException {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        assertFalse(m_mockChannel.isClosed());
        createBuffer(listener, "EOMDelimitedEditConfigReq.txt", true);
        assertTrue(m_mockChannel.isClosed());
        assertFalse(listener.await(50));
        assertEquals(0, listener.getByteBuf().refCnt());
    }

    public void testNonHelloMessageIsRecievedAndChannelClosedInBetween() throws InterruptedException, IOException {
        //when the channel closed in between and on not receiving hello first, do not close the channel again
        doReturn(true).doReturn(false).when(m_mockChannel).isOpen();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        assertFalse(m_mockChannel.isClosed());
        m_buffer.clear();
        m_buffer.putRawBytes(getMessageFromFile("EOMDelimitedEditConfigReq.txt").getBytes());
        listener.operationComplete(m_mockFuture);
        verify(m_mockChannel, times(0)).close(true);
    }

    public void testWhenExceptionOccursBufferIsReleasedAndSessionIsClosed() throws InterruptedException, NetconfMessageBuilderException {
        doThrow(new RuntimeException("Simply throwing exception")).when(m_clientSession).responseRecieved(any());
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        m_buffer.clear();
        m_buffer.putRawBytes(getMessageFromFile("sampleHelloMessage.txt").getBytes());
        assertFalse(m_mockChannel.isClosed());
        listener.operationComplete(m_mockFuture);
        assertTrue(m_mockChannel.isClosed());
        assertFalse(listener.await(50));
        assertEquals(0, listener.getByteBuf().refCnt());
    }

    public void testWhenExceptionOccursAndChannelClosedInBetween() throws NetconfMessageBuilderException, IOException {
        //when the channel closed and an exception occurs, do not close the channel again
        doReturn(true).doReturn(false).when(m_mockChannel).isOpen();
        doThrow(new RuntimeException("Simply throwing exception")).when(m_clientSession).responseRecieved(any());
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        m_buffer.clear();
        m_buffer.putRawBytes(getMessageFromFile("sampleHelloMessage.txt").getBytes());
        assertFalse(m_mockChannel.isClosed());
        listener.operationComplete(m_mockFuture);
        verify(m_mockChannel, times(0)).close(true);
    }

    public void testAwait() throws InterruptedException {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        createBuffer(listener, "sampleHelloMessage.txt", false);
        assertFalse(listener.await(50));
    }

    public void testNoOperationsWhenChannelClosed() throws InterruptedException {
        doReturn(false).when(m_mockChannel).isOpen();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        listener.operationComplete(m_mockFuture);
        verifyZeroInteractions(m_clientSession);
    }

    public void testSessionClosedWhenInvalidHelloXml() {
        String helloInvalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                " <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "   <capabilities>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.0\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:ns:netconf:capability:startup:1.0\n" +
                "     </capability>\n" +
                "   </capabilities>\n" +
                "   <session-id>" + //no closing tag for session-id
                " </hello>\n" +
                "]]>]]>";
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        m_buffer.clear();
        m_buffer.putRawBytes(helloInvalidXml.getBytes());
        listener.operationComplete(m_mockFuture);
        assertTrue(m_mockChannel.isClosed());
    }

    @SuppressWarnings("resource")
    private String getMessageFromFile(String file) {
        StringBuilder fileContents = new StringBuilder();
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(Thread.currentThread().getContextClassLoader().getResource(file).getFile()));
            String line = "";
            while ((line = in.readLine()) != null) {
                fileContents.append(line).append("\n");
            }
        } catch (IOException e) {
            LOGGER.error("Error while reading file " + file, e);
        }
        String fileContentStr = fileContents.toString().trim();
        return fileContentStr;
    }

    private final class TestIoReadFuture implements IoReadFuture {

        @Override
        public IoReadFuture addListener(SshFutureListener<IoReadFuture> listener) {
            m_futureListener = listener;
            return null;
        }

        @Override
        public IoReadFuture removeListener(SshFutureListener<IoReadFuture> listener) {
            return null;
        }

        @Override
        public IoReadFuture verify() throws SshException {

            return null;
        }

        @Override
        public IoReadFuture verify(long timeoutMillis) throws IOException {
            return null;
        }

        @Override
        public int getRead() {
            return 0;
        }

        @Override
        public Throwable getException() {
            return null;
        }

        @Override
        public Buffer getBuffer() {
            return null;
        }

        @Override
        public boolean await(long timeoutMillis) throws IOException {
            return false;
        }

        @Override
        public boolean awaitUninterruptibly(long timeoutMillis) {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object getId() {
            return "id";
        }
    }

    private void createBuffer(SshHelloMessageListener listener, String fileName, boolean includeOperationCompleteListener) {
        String message = getMessageFromFile(fileName);
        m_buffer.clear();
        byte[] messagBytes = message.getBytes();
        for (int i = 0; i < messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            if (includeOperationCompleteListener) {
                listener.operationComplete(m_mockFuture);
            }
        }
    }
}
