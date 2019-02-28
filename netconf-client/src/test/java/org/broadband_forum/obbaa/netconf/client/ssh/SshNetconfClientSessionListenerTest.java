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

import org.apache.log4j.Logger;
import org.apache.sshd.client.channel.ChannelSubsystem;
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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getDocFromFile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SshNetconfClientSessionListenerTest extends XMLTestCase {
    private static final Boolean FALSE = new Boolean(false);
    private SshNetconfClientSession m_clientSession = new TestSshNetconfClientSession();
    private ChannelSubsystem m_mockChannel = mock(ChannelSubsystem.class);
    private IoReadFuture m_mockFuture = mock(IoReadFuture.class);
    protected Buffer m_buffer = new ByteArrayBuffer();
    private IoInputStream m_mockStream = mock(IoInputStream.class);
    private IoReadFuture m_mockReadFuture = mock(IoReadFuture.class);
    private static final Logger LOGGER = Logger.getLogger(SshNetconfClientSessionListenerTest.class);

    @Override
    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        when(m_mockChannel.isClosed()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return FALSE;
            }
        });
        when(m_mockChannel.getAsyncOut()).thenReturn(m_mockStream);
        when(m_mockStream.read((Buffer) Matchers.anyObject())).thenReturn(m_mockReadFuture);
        when(m_mockFuture.getBuffer()).then(new Answer<Buffer>() {
            @Override
            public Buffer answer(InvocationOnMock invocation) throws Throwable {
                return m_buffer;
            }
        });
    }

    public void testListenerSendsMessageToSessionEOM() {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession);
        String message = getMessageFromFile("EOMDelimitedEditConfigReq.txt").replaceAll("]]>]]>\\n", "]]>]]>");
        m_buffer.clear();
        m_buffer.putRawBytes(message.getBytes());

        listener.operationComplete(m_mockFuture);

        URL url = Thread.currentThread().getContextClassLoader().getResource("editConfig.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
    }

    public void testListenerSendsMessageToSessionWhenWrittenInMultipleStepsEOM() {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession);
        String message = getMessageFromFile("EOMDelimitedEditConfigReq.txt").replaceAll("]]>]]>\\n", "]]>]]>");
        m_buffer.clear();
        byte[] messagBytes = message.getBytes();
        for (int i = 0; i <= messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            listener.operationComplete(m_mockFuture);
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource("editConfig.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);
        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
    }

    public void testListenerSendsMessageToSessionChunked() {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession);
        String message = getMessageFromFile("sampleChunkedMessage1.txt");
        m_buffer.clear();
        byte[] messagBytes = message.getBytes();
        for (int i = 0; i <= messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            listener.operationComplete(m_mockFuture);
        }
        URL url = Thread.currentThread().getContextClassLoader().getResource("expectedMessage1.txt");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);
        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
    }

    public void testListenerSendsMessageToSession2Chunked() {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession);
        String message = getMessageFromFile("sampleChunkedMessage2.txt");
        m_buffer.clear();

        byte[] messagBytes = message.getBytes();
        for (int i = 0; i <= messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            listener.operationComplete(m_mockFuture);
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource("expectedMessage2.txt");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
    }

    public void testListenerSendsMessageToSession3Chunked() {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession);
        String message = getMessageFromFile("sampleChunkedMessage3.txt");
        m_buffer.clear();
        byte[] messagBytes = message.getBytes();
        for (int i = 0; i <= messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            listener.operationComplete(m_mockFuture);
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource("expectedMessage3.txt");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
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
        String fileContentStr = fileContents.toString();
        return fileContentStr;
    }


}
