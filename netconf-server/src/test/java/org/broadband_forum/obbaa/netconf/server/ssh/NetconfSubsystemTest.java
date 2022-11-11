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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.RPC_CHUNKED_DELIMITER;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.RPC_EOM_DELIMITER;
import static org.broadband_forum.obbaa.netconf.server.ssh.NetconfSubsystem.HELLO_MALFORMED;
import static org.broadband_forum.obbaa.netconf.server.ssh.NetconfSubsystem.HELLO_MESSAGE_NOT_RECIEVED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.broadband_forum.obbaa.netconf.api.codec.v2.ChunkedNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.codec.v2.EOMNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.server.DefaultSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.junit.Before;
import org.junit.Test;

public class NetconfSubsystemTest {

    private NetconfSubsystem m_subsystem;
    private NetconfServerMessageListener m_serverMsgListener;
    private ServerMessageHandler m_serverMsgHandler;
    private DefaultSessionIdProvider m_sessionIdProvider;
    private ExitCallback m_exitCallBack;
    private ChannelSession m_channel;

    public static final String HELLO_BASE_1_0_MSG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "    <capabilities>\n" +
                    "        <capability>urn:ietf:params:netconf:base:1.0</capability>\n" +
                    "        <capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>\n" +
                    "    </capabilities>\n" +
                    "</hello>\n\r" + RPC_EOM_DELIMITER;

    private static final String HELLO_BASE_1_0_WITH_LINE_FEED = HELLO_BASE_1_0_MSG + "\n";

    public static final String HELLO_BASE_1_1_MSG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "    <capabilities>\n" +
                    "        <capability>urn:ietf:params:netconf:base:1.0</capability>\n" +
                    "        <capability>urn:ietf:params:netconf:base:1.1</capability>\n" +
                    "        <capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>\n" +
                    "    </capabilities>\n" +
                    "</hello>\n" + RPC_EOM_DELIMITER;

    private static final String HELLO_BASE_1_1_WITH_LINE_FEED = HELLO_BASE_1_1_MSG + "\n";

    private static final String HELLO_BASE_1_1_WITH_MORE_LINE_FEED = HELLO_BASE_1_1_MSG + "\n\n\n\n";

    private static final String HELLO_PACKET_1 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "    <capabilities>\n" +
                    "        <capability>urn:ietf:params:netconf:base:1.0</capability>\n" +
                    "        <capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>\n" +
                    "    </capabilities>\n" +
                    "</hello>\n";

    private static final String HELLO_PACKET_2 = RPC_EOM_DELIMITER + "\n";

    private static final String HELLO_MSG2 = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xc:hello xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <xc:capabilities>\n" +
            "        <xc:capability>urn:ietf:params:netconf:base:1.0</xc:capability>\n" +
            "        <xc:capability>urn:ietf:params:netconf:base:1.1</xc:capability>\n" +
            "        <xc:capability>urn:ietf:params:netconf:capability:candidate:1.0</xc:capability>\n" +
            "    </xc:capabilities>\n" +
            "</xc:hello>" + RPC_EOM_DELIMITER;

    private static final String HELLO_MSG3 = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- some comment -->" +
            "<xc:hello xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <xc:capabilities>\n" +
            "        <xc:capability>urn:ietf:params:netconf:base:1.0</xc:capability>\n" +
            "        <xc:capability>urn:ietf:params:netconf:base:1.1</xc:capability>\n" +
            "        <xc:capability>urn:ietf:params:netconf:capability:candidate:1.0</xc:capability>\n" +
            "    </xc:capabilities>\n" +
            "</xc:hello>" + RPC_EOM_DELIMITER;

    public static final String EDIT_CONFIG_RPC = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
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
            "</rpc>";

    private static final String EDIT_WITH_EOM_DELIM = EDIT_CONFIG_RPC + RPC_EOM_DELIMITER;

    private static final String EDIT_WITH_LINEFEED = EDIT_WITH_EOM_DELIM + "\n";

    private static final String EDIT_WITH_MORE_LINEFEED = EDIT_WITH_EOM_DELIM + "\n\n\n\n";
    private IoOutputStream m_ioOutputStream;

    @Before
    public void setUp() throws Exception {
        m_serverMsgListener = mock(NetconfServerMessageListener.class);
        m_serverMsgHandler = mock(ServerMessageHandler.class);
        m_sessionIdProvider = spy(new DefaultSessionIdProvider());
        when(m_sessionIdProvider.getNewSessionId()).thenReturn(1);
        HashSet<String> serverCaps = new HashSet<>();
        serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        m_subsystem = new NetconfSubsystem(m_serverMsgListener, m_serverMsgHandler, serverCaps, m_sessionIdProvider);
        m_exitCallBack = mock(ExitCallback.class);
        m_subsystem.setExitCallback(m_exitCallBack);
        m_subsystem.setSessionId(1);
        m_ioOutputStream = mock(IoOutputStream.class);
        when(m_ioOutputStream.writePacket(anyObject())).thenReturn(mock(IoWriteFuture.class));
        m_subsystem.setIoOutputStream(m_ioOutputStream);
        ChannelSession channelSession = mock(ChannelSession.class);
        ServerSession session = mock(ServerSession.class);
        IoSession ioSession = mock(IoSession.class);
        when(ioSession.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 1234));
        when(session.getIoSession()).thenReturn(ioSession);
        when(channelSession.getSession()).thenReturn(session);
        ServerSession serverSession = mock(ServerSession.class);
        when(serverSession.getIoSession()).thenReturn(ioSession);
        when(channelSession.getServerSession()).thenReturn(serverSession);
        m_subsystem.setChannelSession(channelSession);
        m_subsystem.start(null,mock(Environment.class));
        m_channel = mock(ChannelSession.class);
    }

    @Test
    public void testCodecSwitches() throws IOException {
        assertTrue(m_subsystem.getCodec().currentCodec() instanceof EOMNetconfMessageCodecV2);

        byte[] helloBytes = HELLO_BASE_1_1_MSG.getBytes();
        assertEquals(helloBytes.length, m_subsystem.data(null, helloBytes, 0, helloBytes.length));
        assertTrue(m_subsystem.getCodec().currentCodec() instanceof ChunkedNetconfMessageCodecV2);
    }

    @Test
    public void testData_HelloBase1_0Msg() throws IOException {
        assertEquals(HELLO_BASE_1_0_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_0_MSG.getBytes(), 0, HELLO_BASE_1_0_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_HelloBase1_0MsgWithLineFeed() throws IOException {
        assertEquals(HELLO_BASE_1_0_WITH_LINE_FEED.getBytes().length, m_subsystem.data(m_channel,
                HELLO_BASE_1_0_WITH_LINE_FEED.getBytes(), 0, HELLO_BASE_1_0_WITH_LINE_FEED.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_HelloBase1_1Msg() throws IOException {
        assertEquals(HELLO_BASE_1_1_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_1_MSG.getBytes(), 0, HELLO_BASE_1_1_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_HelloBase1_1MsgWithLineFeed() throws IOException {
        assertEquals(HELLO_BASE_1_1_WITH_LINE_FEED.getBytes().length, m_subsystem.data(m_channel,
                HELLO_BASE_1_1_WITH_LINE_FEED.getBytes(), 0, HELLO_BASE_1_1_WITH_LINE_FEED.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_HelloBase1_1MsgWithMoreLineFeed() throws IOException {
        assertEquals(HELLO_BASE_1_1_WITH_MORE_LINE_FEED.getBytes().length, m_subsystem.data(m_channel,
                HELLO_BASE_1_1_WITH_MORE_LINE_FEED.getBytes(), 0, HELLO_BASE_1_1_WITH_MORE_LINE_FEED.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testDataTwoPackets_HelloBase1_1Msg() throws IOException {
        assertEquals(HELLO_PACKET_1.getBytes().length, m_subsystem.data(m_channel, HELLO_PACKET_1.getBytes(), 0, HELLO_PACKET_1.getBytes().length));
        assertEquals(false, m_subsystem.isHelloReceived());
        assertEquals(HELLO_PACKET_2.getBytes().length, m_subsystem.data(m_channel, HELLO_PACKET_2.getBytes(), 0, HELLO_PACKET_2.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testDataMultiplePackets_HelloBase1_1Msg() throws IOException {
        assertEquals(HELLO_PACKET_1.getBytes().length, m_subsystem.data(m_channel, HELLO_PACKET_1.getBytes(), 0, HELLO_PACKET_1.getBytes().length));
        assertEquals(false, m_subsystem.isHelloReceived());
        assertEquals("]]>".getBytes().length, m_subsystem.data(m_channel, "]]>".getBytes(), 0, "]]>".getBytes().length));
        assertEquals(false, m_subsystem.isHelloReceived());
        assertEquals("]]>".getBytes().length, m_subsystem.data(m_channel, "]]>".getBytes(), 0, "]]>".getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testDataMultiplePackets_HelloBase1_1Msg2() throws IOException {
        assertEquals("\n".getBytes().length, m_subsystem.data(m_channel, "\n".getBytes(), 0, "\n".getBytes().length));
        assertEquals("\n".getBytes().length, m_subsystem.data(m_channel, "\n".getBytes(), 0, "\n".getBytes().length));
        assertEquals("\n".getBytes().length, m_subsystem.data(m_channel, "\n".getBytes(), 0, "\n".getBytes().length));
        assertEquals(HELLO_PACKET_1.getBytes().length, m_subsystem.data(m_channel, HELLO_PACKET_1.getBytes(), 0, HELLO_PACKET_1.getBytes().length));
        assertEquals(false, m_subsystem.isHelloReceived());
        assertEquals("]]>".getBytes().length, m_subsystem.data(m_channel, "]]>".getBytes(), 0, "]]>".getBytes().length));
        assertEquals(false, m_subsystem.isHelloReceived());
        assertEquals("]]".getBytes().length, m_subsystem.data(m_channel, "]]".getBytes(), 0, "]]".getBytes().length));
        assertEquals(false, m_subsystem.isHelloReceived());
        assertEquals(">\n".getBytes().length, m_subsystem.data(m_channel, ">\n".getBytes(), 0, ">\n".getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_Hello_ThenEditEOMMsg() throws Exception {
        assertEquals(HELLO_BASE_1_0_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_0_MSG.getBytes(), 0, HELLO_BASE_1_0_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(EDIT_CONFIG_RPC));
        assertEquals(EDIT_WITH_LINEFEED.getBytes().length, m_subsystem.data(m_channel, EDIT_WITH_LINEFEED.getBytes(), 0, EDIT_WITH_LINEFEED.getBytes().length));
        verify(m_serverMsgHandler).processRequest(anyObject(), eq(editConfigRequest), anyObject());
        verifyNoUnexpectedExits();
    }


    @Test
    public void testData_Hello_ThenEditTwiceEachWithChunkEOM() throws Exception {
        assertEquals(HELLO_BASE_1_0_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_0_MSG.getBytes(), 0, HELLO_BASE_1_0_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(EDIT_CONFIG_RPC));
        assertEquals(EDIT_WITH_LINEFEED.getBytes().length*2, m_subsystem.data(m_channel, ArrayUtils.addAll(EDIT_WITH_LINEFEED.getBytes(),EDIT_WITH_LINEFEED.getBytes()), 0, EDIT_WITH_LINEFEED.getBytes().length*2));
        verify(m_serverMsgHandler, times(2)).processRequest(anyObject(), eq(editConfigRequest), anyObject());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_Hello_ThenEditEOMMoreLineFeedMsg() throws Exception {
        assertEquals(HELLO_BASE_1_0_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_0_MSG.getBytes(), 0, HELLO_BASE_1_0_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(EDIT_CONFIG_RPC));
        assertEquals(EDIT_WITH_MORE_LINEFEED.getBytes().length, m_subsystem.data(m_channel, EDIT_WITH_MORE_LINEFEED.getBytes(), 0, EDIT_WITH_MORE_LINEFEED.getBytes().length));
        verify(m_serverMsgHandler).processRequest(anyObject(), eq(editConfigRequest), anyObject());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_Hello_ThenEditChunkedMsg() throws Exception {
        assertEquals(HELLO_BASE_1_1_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_1_MSG.getBytes(), 0, HELLO_BASE_1_1_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(EDIT_CONFIG_RPC));
        String editWithChunkedDelim = getSingleChunkedMessage(EDIT_CONFIG_RPC);
        assertEquals(editWithChunkedDelim.getBytes().length, m_subsystem.data(m_channel, editWithChunkedDelim.getBytes(), 0, editWithChunkedDelim.getBytes().length));
        verify(m_serverMsgHandler).processRequest(anyObject(), eq(editConfigRequest), anyObject());
    }

    @Test
    public void testData_Hello_Then2EditChunkedMsgs() throws Exception {
        assertEquals(HELLO_BASE_1_1_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_1_MSG.getBytes(), 0, HELLO_BASE_1_1_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(EDIT_CONFIG_RPC));
        String editWithChunkedDelim = getSingleChunkedMessage(EDIT_CONFIG_RPC);
        assertEquals(editWithChunkedDelim.getBytes().length*2, m_subsystem.data(m_channel, ArrayUtils.addAll(editWithChunkedDelim.getBytes(), editWithChunkedDelim.getBytes()), 0, editWithChunkedDelim.getBytes().length*2));
        verify(m_serverMsgHandler, times(2)).processRequest(anyObject(), eq(editConfigRequest), anyObject());
    }

    @Test
    public void testData_Hello_ThenEditChunkedMoreLineFeedMsg() throws Exception {
        assertEquals(HELLO_BASE_1_1_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_1_MSG.getBytes(), 0, HELLO_BASE_1_1_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(EDIT_CONFIG_RPC));
        String editWithChunkedDelim = getSingleChunkedMessage(EDIT_CONFIG_RPC) + "\n\n\n\n";
        assertEquals(editWithChunkedDelim.getBytes().length, m_subsystem.data(m_channel, editWithChunkedDelim.getBytes(), 0, editWithChunkedDelim.getBytes().length));
        verify(m_serverMsgHandler).processRequest(anyObject(), eq(editConfigRequest), anyObject());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_Hello_ThenEditChunkedMsgTwoPackets() throws Exception {
        assertEquals(HELLO_BASE_1_1_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_1_MSG.getBytes(), 0, HELLO_BASE_1_1_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        String editChunkedPacket1 = getEditChunkedWithoutDelim(EDIT_CONFIG_RPC) + "\n";
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(EDIT_CONFIG_RPC));
        String editChunkedPacket2 = "##\n";
        assertEquals(editChunkedPacket1.getBytes().length, m_subsystem.data(m_channel, editChunkedPacket1.getBytes(), 0, editChunkedPacket1.getBytes().length));
        verify(m_serverMsgHandler, never()).processRequest(anyObject(), anyObject(), anyObject());
        assertEquals(editChunkedPacket2.getBytes().length, m_subsystem.data(m_channel, editChunkedPacket2.getBytes(), 0, editChunkedPacket2.getBytes().length));
        verify(m_serverMsgHandler).processRequest(anyObject(), eq(editConfigRequest), anyObject());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_Hello_ThenEditChunkedMsgTwoPackets2() throws Exception {
        assertEquals(HELLO_BASE_1_1_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_1_MSG.getBytes(), 0, HELLO_BASE_1_1_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        String editChunkedPacket1 = getEditChunkedWithoutDelim(EDIT_CONFIG_RPC).substring(0,795);
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(EDIT_CONFIG_RPC));
        String editChunkedPacket2 = "c>\n##\n";
        assertEquals(editChunkedPacket1.getBytes().length, m_subsystem.data(m_channel, editChunkedPacket1.getBytes(), 0, editChunkedPacket1.getBytes().length));
        verify(m_serverMsgHandler, never()).processRequest(anyObject(), anyObject(), anyObject());
        assertEquals(editChunkedPacket2.getBytes().length, m_subsystem.data(m_channel, editChunkedPacket2.getBytes(), 0, editChunkedPacket2.getBytes().length));
        //verify(m_serverMsgHandler).processRequest(anyObject(), anyObject(), anyObject());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_HelloMsg2() throws IOException {
        assertEquals(HELLO_MSG2.getBytes().length, m_subsystem.data(m_channel, HELLO_MSG2.getBytes(), 0, HELLO_MSG2.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_HelloMsg3() throws IOException {
        assertEquals(HELLO_MSG3.getBytes().length, m_subsystem.data(m_channel, HELLO_MSG3.getBytes(), 0, HELLO_MSG3.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        verifyNoUnexpectedExits();
    }

    @Test
    public void testData_HelloMsg3FollowedByIncorrectChunkedMsg() throws IOException {
        assertEquals(HELLO_MSG3.getBytes().length, m_subsystem.data(m_channel, HELLO_MSG3.getBytes(), 0, HELLO_MSG3.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        String editConfig = "#371\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"2\">\n" +
                "  <get>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <platform:platform xmlns:platform=\"http://www.test-company.com/solutions/anv-platform\">\n" +
                "        <license:license-details xmlns:license=\"http://www.test-company.com/solutions/license-management\"/>\n" +
                "      </platform:platform>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>\n" +
                "# \n" +
                "\n" +
                "\n" +
                "#\n" +
                "\n" +
                "\n" +
                "##\n";
        try {
            m_subsystem.data(m_channel, editConfig.getBytes(), 0, editConfig.getBytes().length);
            fail("This UT should have failed");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Got invalid character"));
        }
    }

    @Test
    public void testData_HelloMsgNotRecieved() throws IOException {
        assertEquals(EDIT_WITH_EOM_DELIM.getBytes().length, m_subsystem.data(m_channel, EDIT_WITH_EOM_DELIM.getBytes(), 0, EDIT_WITH_EOM_DELIM.getBytes().length));
        assertEquals(false, m_subsystem.isHelloReceived());
        verify(m_exitCallBack).onExit(1, HELLO_MESSAGE_NOT_RECIEVED);
    }

    @Test
    public void testData_messageHandlerThrowsException() throws IOException {
        assertEquals(HELLO_BASE_1_0_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_0_MSG.getBytes(), 0, HELLO_BASE_1_0_MSG.getBytes().length));
        assertEquals(true, m_subsystem.isHelloReceived());
        m_subsystem.sendHelloToClient();
        RuntimeException exception = new RuntimeException("test");
        try {
            doThrow(exception).when(m_serverMsgHandler).processRequest(anyObject(), anyObject(), anyObject());
            m_subsystem.data(m_channel, EDIT_WITH_EOM_DELIM.getBytes(), 0, EDIT_WITH_EOM_DELIM.getBytes().length);
            fail("Expected IOException here");
        } catch (IOException e) {
            assertEquals(exception, e.getCause());
        }
    }

    @Test
    public void testData_ErrorDuringHello() throws IOException {
        String incorrectHello1_0 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <capabilities>\n" +
                "        <capability>urn:ietf:params:netconf:base:1.0</capability>\n" +
                "        <capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>\n" +
                "    </capabilities>\n" +
                "</helloo>\n\r" + RPC_EOM_DELIMITER;
        assertEquals(incorrectHello1_0.getBytes().length, m_subsystem.data(m_channel, incorrectHello1_0.getBytes(), 0, incorrectHello1_0.getBytes().length));
        assertFalse(m_subsystem.isHelloReceived());
        verify(m_exitCallBack).onExit(1, HELLO_MALFORMED);
        verify(m_serverMsgListener).sessionClosed(HELLO_MALFORMED, 1);
    }

    @Test
    public void testWhenSessionClosesBufferIsReleased() throws IOException {
        m_subsystem.close();
        assertEquals(0, m_subsystem.getBtyeBuf().refCnt());
    }

    @Test
    public void testData_Hello_ThenEditChunkedWithIsMalformed() throws Exception {
        assertEquals(HELLO_BASE_1_1_MSG.getBytes().length, m_subsystem.data(m_channel, HELLO_BASE_1_1_MSG.getBytes(), 0, HELLO_BASE_1_1_MSG.getBytes().length));
        assertTrue(m_subsystem.isHelloReceived());
        String malformedEdit = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <edit-config>\n" +
                "        <target>\n" +
                "            <running />\n" +
                "        </target>\n" +
                "        <test-option>set</test-option>\n" +
                "        <config>\n" +
                "            <device-manager xmlns=\"http://www.test-company.com/solutions/anv\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "                <device-holder xmlns=\"http://www.test-company.com/solutions/anv-device-holders\">\n" +
                "                    <name>OLT1</name>\n" +
                "                    <device xc:operation=\"replace\">\n" +
                "                        <device-id>ONT1</device-id>\n" +
                "                        <hardware-type>G.FAST</hardware-type>\n" +
                "                        <interface-version>1.0</interface-version>\n" +
                "                        <duid>OLT1.ONT1</duid>\n" +
                "                    </device><invalid>\n" +
                "                </device-holder>\n" +
                "            </device-manager>\n" +
                "        </config>\n" +
                "    </edit-config>\n" +
                "</rpc>";
        String editWithChunkedDelim = getSingleChunkedMessage(malformedEdit);
        assertEquals(editWithChunkedDelim.getBytes().length, m_subsystem.data(m_channel, editWithChunkedDelim.getBytes(), 0, editWithChunkedDelim.getBytes().length));
        verifyNoUnexpectedExits();
    }

    @Test
    public void testKillSession_ForExistingSession() {
        assertTrue(NetconfSubsystem.killSession(1));
        verify(m_serverMsgListener).sessionClosed("Session Destroyed", 1);
        verify(m_exitCallBack).onExit(1,NetconfSubsystem.prepareExitMessage(1));
    }

    @Test
    public void testKillSession_ForNonExistingSession() {
        assertFalse(NetconfSubsystem.killSession(2));
        verify(m_serverMsgListener, never()).sessionClosed(anyString(), anyInt());
        verify(m_exitCallBack, never()).onExit(anyInt(),anyString());
    }

    private void verifyNoUnexpectedExits() {
        verify(m_exitCallBack, never()).onExit(anyInt());
        verify(m_exitCallBack, never()).onExit(anyInt(), anyString());
    }

    private String getEditChunkedWithoutDelim(String editConfigRpc) {
        int len = editConfigRpc.length();
        String chunkedMessage = "\n#" + len + "\n" + editConfigRpc;
        return chunkedMessage;
    }

    private String getSingleChunkedMessage(String editConfigRpc) {
        int len = editConfigRpc.length();
        String chunkedMessage = "\n#" + len + "\n" + editConfigRpc + RPC_CHUNKED_DELIMITER;
        return chunkedMessage;
    }
}
