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

import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getDocFromFile;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CHUNK_SIZE;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.MAXIMUM_SIZE_OF_CHUNKED_MESSAGES;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.codec.v2.ChunkedNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtilsUserTest;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.netty.buffer.ByteBuf;

public class SshNetconfClientSessionListenerTest extends XMLTestCase {
    private static final Boolean FALSE = new Boolean(false);
    private SshNetconfClientSession m_clientSession = spy(new TestSshNetconfClientSession());
    private ByteBuf m_byteBuf = unpooledHeapByteBuf();
    private ChannelSubsystem m_mockChannel = spy(new ChannelSubsystem("test"));
    private IoReadFuture m_mockFuture = mock(IoReadFuture.class);
    protected Buffer m_buffer = new ByteArrayBuffer();
    private IoInputStream m_mockStream = mock(IoInputStream.class);
    private IoReadFuture m_mockReadFuture = mock(IoReadFuture.class);
    private SystemPropertyUtilsUserTest m_systemPropertyUtils;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshNetconfClientSessionListenerTest.class, LogAppNames.NETCONF_LIB);

    @Override
    protected void setUp() throws Exception {
        m_systemPropertyUtils = new SystemPropertyUtilsUserTest();
        m_systemPropertyUtils.setUpMockUtils();;
        m_systemPropertyUtils.mockPropertyUtils(MAXIMUM_SIZE_OF_CHUNKED_MESSAGES, "2000");
        m_systemPropertyUtils.mockPropertyUtils(CHUNK_SIZE, "200");
        XMLUnit.setIgnoreWhitespace(true);
        when(m_mockChannel.getAsyncOut()).thenReturn(m_mockStream);
        when(m_mockStream.read((Buffer) Matchers.anyObject())).thenReturn(m_mockReadFuture);
        when(m_mockFuture.getBuffer()).then(new Answer<Buffer>() {
            @Override
            public Buffer answer(InvocationOnMock invocation) throws Throwable {
                return m_buffer;
            }
        });
        doNothing().when(m_clientSession).close();
    }

    public void testListenerSendsMessageToSessionEOM() throws NetconfMessageBuilderException {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = getMessageFromFile("EOMDelimitedEditConfigReq.txt").replaceAll("]]>]]>\\n", "]]>]]>");
        m_buffer.clear();
        m_buffer.putRawBytes(message.getBytes());

        listener.operationComplete(m_mockFuture);

        URL url = Thread.currentThread().getContextClassLoader().getResource("editConfig.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        Document lastRecievedMessage = TestSshNetconfClientSession.c_lastRecievedMessage;
        assertXMLEqual(expectedDoc, lastRecievedMessage);
        verify(m_clientSession).responseRecieved(any(DocumentInfo.class));
        verifyListenerAdded(listener, 1);
    }

    public void testListenerSendsMessageToSessionWhenWrittenInMultipleStepsEOM() {
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = getMessageFromFile("EOMDelimitedEditConfigReq.txt").replaceAll("]]>]]>\\n", "]]>]]>");
        performOperationCompleteInMultiplePackets(listener, message);

        URL url = Thread.currentThread().getContextClassLoader().getResource("editConfig.xml");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);
        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        verifyListenerAdded(listener, 76);
    }



    public void testListenerSendsMessageToSessionChunked() {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = getMessageFromFile("sampleChunkedMessage1.txt");
        performOperationCompleteInMultiplePackets(listener, message);
        URL url = Thread.currentThread().getContextClassLoader().getResource("expectedMessage1.txt");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);
        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        verifyListenerAdded(listener, 60);
    }

    public void testListenerSendsMessageToSession2Chunked() {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = getMessageFromFile("sampleChunkedMessage2.txt");
        performOperationCompleteInMultiplePackets(listener, message);

        URL url = Thread.currentThread().getContextClassLoader().getResource("expectedMessage2.txt");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        verifyListenerAdded(listener, 60);
    }

    public void testListenerSendsMessageToSession3Chunked() {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = getMessageFromFile("sampleChunkedMessage3.txt");
        performOperationCompleteInMultiplePackets(listener, message);

        URL url = Thread.currentThread().getContextClassLoader().getResource("expectedMessage3.txt");
        File file = new File(url.getPath());
        Document expectedDoc = getDocFromFile(file);

        assertXMLEqual(expectedDoc, TestSshNetconfClientSession.c_lastRecievedMessage);
        verifyListenerAdded(listener, 12);
    }

    public void testErrorScenarioAlsoAddsListener() throws IOException {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = getMessageFromFile("sampleIncorrectRpcChunked.txt");
        m_buffer.clear();
        m_buffer.putRawBytes(message.getBytes());
        listener.operationComplete(m_mockFuture);
        verify(m_clientSession).close();
        assertEquals(0, listener.getByteBuf().refCnt());
    }

    public void testListenerSendsMessageToSessionForMultipleChunkedRpcsInMultiplePackets() throws NetconfMessageBuilderException {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = getMessageFromFile("sampleMultipleChunksFor2RPCs.txt");
        performOperationCompleteInMultiplePackets(listener, message);
        assertXMLEqual(DocumentUtils.stringToDocument(getExpectedNotification("24166")), TestSshNetconfClientSession.c_lastRecievedMessage);
        verifyListenerAdded(listener, 218);
    }

    public void testListenerSendsMessageToSessionForMultipleChunkedRpcsInSinglePacket() throws NetconfMessageBuilderException {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = getMessageFromFile("sampleMultipleChunksFor2RPCs.txt");
        m_buffer.clear();
        m_buffer.putRawBytes(message.getBytes());
        listener.operationComplete(m_mockFuture);
        assertXMLEqual(DocumentUtils.stringToDocument(getExpectedNotification("24166")), TestSshNetconfClientSession.c_lastRecievedMessage);
    }

    public void testExceptionThrownDueToInvalidXml() throws NetconfMessageBuilderException, IOException, SAXException {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = "\n" +
                "#919\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>1970-01-01T03:05:39.090Z</eventTime>\n" +
                "    <sros-log-generic-event xmlns=\"urn:test1.com:sros:ns:yang:sr:notifications\">\n" +
                "        <sequence-number>859</sequence-number>\n" +
                "        <severity>critical</severity>\n" +
                "        <application>logger</application>\n" +
                "        <event-id>2002</event-id>\n" +
                "        <event-name>tmnxLogTraceError</event-name>\n" +
                "        <router-name>Base</router-name>\n" +
                "        <subject>1:IOM:UNUSUAL_ERROR</subject>\n" +
                "        <message>Slot 1: PORT_QGROUP_INSTANCE_LOCATION: map error for 1/3/1 host <invalid></message>\n" +
                "        <event-params>\n" +
                "            <tmnxLogTraceErrorTitle>Slot 1: PORT_QGROUP_INSTANCE_LOCATION</tmnxLogTraceErrorTitle>\n" +
                "            <tmnxLogTraceErrorMessage>1:IOM:UNUSUAL_ERROR</tmnxLogTraceErrorMessage>\n" +
                "        </event-params>\n" +
                "    </sros-log-generic-event>\n" +
                "</notification>\n" +
                "##\n";
        m_buffer.clear();
        m_buffer.putRawBytes(message.getBytes());
        listener.operationComplete(m_mockFuture);
        verify(m_clientSession, times(0)).responseRecieved(any());
        verify(m_clientSession, times(0)).close();

        //other rpcs which come later are processed
        m_buffer.putRawBytes("\n#6\n<rpc/>\n##\n".getBytes());
        listener.operationComplete(m_mockFuture);
        assertXMLEqual("<rpc/>", DocumentUtils.documentToPrettyString(TestSshNetconfClientSession.c_lastRecievedMessage));
    }

    public void testExceptionThrownDueToInvalidXmlAndSomeOtherXmlInSamePkt() throws NetconfMessageBuilderException, IOException, SAXException {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = "\n" +
                "#919\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>1970-01-01T03:05:39.090Z</eventTime>\n" +
                "    <sros-log-generic-event xmlns=\"urn:test1.com:sros:ns:yang:sr:notifications\">\n" +
                "        <sequence-number>859</sequence-number>\n" +
                "        <severity>critical</severity>\n" +
                "        <application>logger</application>\n" +
                "        <event-id>2002</event-id>\n" +
                "        <event-name>tmnxLogTraceError</event-name>\n" +
                "        <router-name>Base</router-name>\n" +
                "        <subject>1:IOM:UNUSUAL_ERROR</subject>\n" +
                "        <message>Slot 1: PORT_QGROUP_INSTANCE_LOCATION: map error for 1/3/1 host <invalid></message>\n" +
                "        <event-params>\n" +
                "            <tmnxLogTraceErrorTitle>Slot 1: PORT_QGROUP_INSTANCE_LOCATION</tmnxLogTraceErrorTitle>\n" +
                "            <tmnxLogTraceErrorMessage>1:IOM:UNUSUAL_ERROR</tmnxLogTraceErrorMessage>\n" +
                "        </event-params>\n" +
                "    </sros-log-generic-event>\n" +
                "</notification>\n" +
                "##\n\n#12\n<rpc-reply/>\n##\n";
        m_buffer.clear();
        m_buffer.putRawBytes(message.getBytes());
        listener.operationComplete(m_mockFuture);

        verify(m_clientSession, times(0)).responseRecieved(any());

        //the next time operation complete is called, or some other data on the channel, this RPC is processed
        listener.operationComplete(m_mockFuture);
        assertXMLEqual("<rpc-reply/>", DocumentUtils.documentToPrettyString(TestSshNetconfClientSession.c_lastRecievedMessage));
    }

    public void testExceptionThrownDueToInvalidXmlAndSomeOtherXmlIn2Packets() throws NetconfMessageBuilderException, IOException, SAXException {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = "\n" +
                "#919\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>1970-01-01T03:05:39.090Z</eventTime>\n" +
                "    <sros-log-generic-event xmlns=\"urn:test1.com:sros:ns:yang:sr:notifications\">\n" +
                "        <sequence-number>859</sequence-number>\n" +
                "        <severity>critical</severity>\n" +
                "        <application>logger</application>\n" +
                "        <event-id>2002</event-id>\n" +
                "        <event-name>tmnxLogTraceError</event-name>\n" +
                "        <router-name>Base</router-name>\n" +
                "        <subject>1:IOM:UNUSUAL_ERROR</subject>\n" +
                "        <message>Slot 1: PORT_QGROUP_INSTANCE_LOCATION: map error for 1/3/1 host <invalid></message>\n" +
                "        <event-params>\n" +
                "            <tmnxLogTraceErrorTitle>Slot 1: PORT_QGROUP_INSTANCE_LOCATION</tmnxLogTraceErrorTitle>\n" +
                "            <tmnxLogTraceErrorMessage>1:IOM:UNUSUAL_ERROR</tmnxLogTraceErrorMessage>\n" +
                "        </event-params>\n" +
                "    </sros-log-generic-event>\n" +
                "</notification>\n" +
                "##\n\n#12\n<";
        m_buffer.clear();
        m_buffer.putRawBytes(message.getBytes());
        listener.operationComplete(m_mockFuture);
        verify(m_clientSession, times(0)).responseRecieved(any());

        //other half on next 2nd rpc comes in the next packet
        m_buffer.putRawBytes("rpc-reply/>\n##\n".getBytes());
        listener.operationComplete(m_mockFuture);
        assertXMLEqual("<rpc-reply/>", DocumentUtils.documentToPrettyString(TestSshNetconfClientSession.c_lastRecievedMessage));
    }

    public void testExceptionThrownDueSomeOtherChunkingIssue() throws NetconfMessageBuilderException, IOException, SAXException {
        m_clientSession.useChunkedFraming();
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        String message = "\n#13\n<rpc-reply/>\n##\n\n#12\n<rpc-reply/>\n##\n";
        m_buffer.clear();
        m_buffer.putRawBytes(message.getBytes());

        verify(m_clientSession, times(0)).close();
    }

    public void testExceptionThrownDueToMessageTooLargeExp() throws IOException {
        m_clientSession.useChunkedFraming();
        m_systemPropertyUtils.mockPropertyUtils(MAXIMUM_SIZE_OF_CHUNKED_MESSAGES, "500");
        m_systemPropertyUtils.mockPropertyUtils(CHUNK_SIZE, "200");
        ChunkedNetconfMessageCodecV2 codec = new ChunkedNetconfMessageCodecV2();
        when(m_clientSession.getCodec()).thenReturn(codec);
        TestSshNetconfClientSession.c_lastRecievedMessage = null;
        SshNetconfClientSessionListener listener = new SshNetconfClientSessionListener(m_mockChannel, m_clientSession, m_byteBuf);
        m_buffer.clear();
        m_buffer.putRawBytes(getMessageFromFile("sampleChunkedMessage1.txt").getBytes());
        listener.operationComplete(m_mockFuture);

        verify(m_clientSession).close();
    }

    private void performOperationCompleteInMultiplePackets(SshNetconfClientSessionListener listener, String message) {
        m_buffer.clear();
        byte[] messagBytes = message.getBytes();
        for (int i = 0; i <= messagBytes.length; i += 10) {
            int lastindex = ((i + 10) > messagBytes.length) ? messagBytes.length : (i + 10);
            byte[] messagePart = Arrays.copyOfRange(messagBytes, i, lastindex);
            m_buffer.putRawBytes(messagePart);
            listener.operationComplete(m_mockFuture);
        }
    }

    private String getExpectedNotification(String notId) {
        return "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>1970-01-02T13:47:31.550Z</eventTime>\n" +
                "    <sros-log-generic-event xmlns=\"urn:test1.com:sros:ns:yang:sr:notifications\">\n" +
                "        <sequence-number>" + notId +"</sequence-number>\n" +
                "        <severity>critical</severity>\n" +
                "        <application>logger</application>\n" +
                "        <event-id>2002</event-id>\n" +
                "        <event-name>tmnxLogTraceError</event-name>\n" +
                "        <router-name>Base</router-name>\n" +
                "        <subject>1:IOM:UNUSUAL_ERROR</subject>\n" +
                "          <message>Slot 1: ~NPAPI_MAC_ENTRY:\n" +
                " Sap Associated to the MAC is NULL !!!</message>\n" +
                "        <event-params>\n" +
                "            <tmnxLogTraceErrorTitle>Slot 1: ~NPAPI_MAC_ENTRY</tmnxLogTraceErrorTitle>\n" +
                "            <tmnxLogTraceErrorMessage>1:IOM:UNUSUAL_ERROR</tmnxLogTraceErrorMessage>\n" +
                "        </event-params>\n" +
                "    </sros-log-generic-event>\n" +
                "</notification>\n";
    }

    @Override
    public void tearDown() {
        m_systemPropertyUtils.tearDownMockUtils();
    }

    private void verifyListenerAdded(SshNetconfClientSessionListener listener, int timesListenerAdded) {
        verify(m_mockChannel, times(timesListenerAdded)).getAsyncOut();
        verify(m_mockStream, times(timesListenerAdded)).read(m_buffer);
        verify(m_mockReadFuture, times(timesListenerAdded)).addListener(listener);
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