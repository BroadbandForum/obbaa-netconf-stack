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

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.apache.log4j.Logger;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getDocFromFile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SshHelloMessageListenerTest extends XMLTestCase {

    private static final Boolean FALSE = new Boolean(false);
    private SshNetconfClientSession m_clientSession = new TestSshNetconfClientSession();
    private ChannelSubsystem m_mockChannel = mock(ChannelSubsystem.class);
    private IoReadFuture m_mockFuture = mock(IoReadFuture.class);
    protected Buffer m_buffer = new ByteArrayBuffer();
    private IoInputStream m_mockStream = mock(IoInputStream.class);
    private SshFutureListener<IoReadFuture> m_futureListener;
    private TestIoReadFuture m_mockReadFuture = new TestIoReadFuture();
    private static final Logger LOGGER = Logger.getLogger(SshNetconfEOMClientSessionListenerTest.class);

    @Override
    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        when(m_mockChannel.isClosed()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return FALSE;
            }
        });
        when(m_mockChannel.close(true)).thenReturn(mock(CloseFuture.class));
        when(m_mockChannel.getAsyncOut()).thenReturn(m_mockStream);
        when(m_mockStream.read((Buffer) Matchers.anyObject())).thenReturn(m_mockReadFuture);
        when(m_mockFuture.getBuffer()).then(new Answer<Buffer>() {
            @Override
            public Buffer answer(InvocationOnMock invocation) throws Throwable {
                return m_buffer;
            }
        });
    }

    public void testListenerSendsMessageToSessionAndSetsEOMListener() {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        String message = getMessageFromFile("sampleHelloMessage.txt");
        m_buffer.clear();
        byte[] messagBytes = message.getBytes();
        for (int i = 0; i < messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            listener.operationComplete(m_mockFuture);
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleHelloMessage.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        assertTrue(m_futureListener instanceof SshNetconfEOMClientSessionListener);
    }

    public void testListenerSendsMessageToSessionAndSetsChunkedListener() {
        Set<String> caps_with_1_1 = new HashSet<>();
        caps_with_1_1.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        caps_with_1_1.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        m_clientSession.setClientCapabilities(caps_with_1_1);

        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        String message = getMessageFromFile("sampleHelloMessageWith_1_1.txt");
        m_buffer.clear();
        byte[] messagBytes = message.getBytes();
        for (int i = 0; i < messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            listener.operationComplete(m_mockFuture);
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleHelloMessageWith_1_1.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        assertTrue(m_futureListener instanceof SshNetconfChunkedClientSessionListener);
    }

    public void testIfListenerClosesChannelIfNonHelloMessageIsRecieved() {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshHelloMessageListener listener = new SshHelloMessageListener(m_mockChannel, m_clientSession);
        String message = getMessageFromFile("EOMDelimitedEditConfigReq.txt");
        m_buffer.clear();
        byte[] messagBytes = message.getBytes();
        for (int i = 0; i < messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            listener.operationComplete(m_mockFuture);
        }

        verify(m_mockChannel, times(1)).close(true);
    }

    @SuppressWarnings("resource")
    private String getMessageFromFile(String file) {
        StringBuilder fileContents = new StringBuilder();
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(Thread.currentThread().getContextClassLoader().getResource(file)
                    .getFile()));
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
    }
}
