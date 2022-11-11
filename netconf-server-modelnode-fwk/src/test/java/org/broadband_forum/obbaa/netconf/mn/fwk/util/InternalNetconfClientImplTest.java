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

package org.broadband_forum.obbaa.netconf.mn.fwk.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentBuilderFactoryWithoutDTD;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * Created by vgotagi on 10/13/16.
 */
public class InternalNetconfClientImplTest {

    private InternalNetconfClientImpl m_internalNetconfClient;
    private ServerMessageHandler m_serverMessageHandler = null;
    private NetconfLogger m_netconfLogger;
    private NetConfResponse m_stubresponse=new NetConfResponse();
    NetConfResponse m_response=null;
    EditConfigRequest m_editConfigRequest = new EditConfigRequest();
    GetConfigRequest m_getConfigRequest = new GetConfigRequest();
    CopyConfigRequest m_copyConfigRequest = new CopyConfigRequest();
    GetRequest m_getRequest = new GetRequest();
    NetconfRpcRequest m_netConfRPCRequest = new NetconfRpcRequest();
    private CompletableFuture<NetConfResponse> m_futureResponse = mock(CompletableFuture.class);

    private static final String m_EDIT_REQ_CONTENT=
            "      <platform:platform xmlns:platform=\"http://www.test-company.com/solutions/anv-platform\">\n" +
            "        <anvlog:logging xmlns:anvlog=\"http://www.test-company.com/solutions/anv-logging\">\n" +
            "          <anvlog:logger-config>\n" +
            "            <anvlog:application>protocol.nbi.netconf</anvlog:application>\n" +
            "            <anvlog:log-type>customer</anvlog:log-type>\n" +
            "            <anvlog:log-scope>global</anvlog:log-scope>\n" +
            "            <anvlog:log-level>debug</anvlog:log-level>\n" +
            "          </anvlog:logger-config>\n" +
            "        </anvlog:logging>\n" +
            "      </platform:platform>\n";



    @Before
    public void setUp() throws Exception {
        m_serverMessageHandler = mock(ServerMessageHandler.class);
        m_netconfLogger = mock(NetconfLogger.class);
        m_internalNetconfClient = new InternalNetconfClientImpl("admin", m_serverMessageHandler, m_netconfLogger) {
            @Override
            protected CompletableFuture<NetConfResponse> initCompletableFuture() {
                return m_futureResponse;
            }
        };
        m_stubresponse.setOk(true);
        m_internalNetconfClient.setRunningUT(true);
        when(m_futureResponse.get(0, TimeUnit.MILLISECONDS)).thenReturn(m_stubresponse);
    }

    @Test
    public void testMessageIdMissing() throws Exception {
        DocumentBuilderFactory dbf =  DocumentBuilderFactoryWithoutDTD.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db  = dbf.newDocumentBuilder();
        Document document = db.newDocument();
        NetConfResponse response = m_internalNetconfClient.sendRpcMessage(document);

        verify(m_netconfLogger).setThreadLocalDeviceLogId(document);
        verify(m_netconfLogger).setThreadLocalDeviceLogId(null);
        assertNotNull(response);
        assertFalse(response.isOk());
        assertEquals("RPC Tag with message ID is missing",response.getErrors().get(0).getErrorMessage());

    }

    @Test
    public void testEditConfig() throws Exception{
        m_stubresponse.setMessageId("1");
        m_editConfigRequest.setTargetRunning();
        m_editConfigRequest.setMessageId("0");
        m_editConfigRequest.setConfigElement(new EditConfigElement().addConfigElementContent(DocumentUtils.stringToDocument(m_EDIT_REQ_CONTENT).getDocumentElement()));
        Document requestDocument = m_editConfigRequest.getRequestDocument();
        m_response = m_internalNetconfClient.sendRpcMessage(requestDocument);

        verify(m_netconfLogger).setThreadLocalDeviceLogId(requestDocument);
        verify(m_netconfLogger).setThreadLocalDeviceLogId(null);
        assertNotNull(m_response);
        assertTrue(m_response.isOk());

        m_response = m_internalNetconfClient.editConfig(m_editConfigRequest);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }
    
    @Test
    public void testGetConfig() throws Exception{
        m_stubresponse.setMessageId("1");
        m_getConfigRequest.setSourceRunning();
        m_getConfigRequest.setMessageId("1");
        Document requestDocument = m_getConfigRequest.getRequestDocument();
        m_response = m_internalNetconfClient.sendRpcMessage(requestDocument);

        verify(m_netconfLogger).setThreadLocalDeviceLogId(requestDocument);
        verify(m_netconfLogger).setThreadLocalDeviceLogId(null);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }


    @Test
    public void testEmptyMsgId() throws Exception{

        m_getConfigRequest.setSourceRunning();

        m_response = m_internalNetconfClient.sendRpcMessage(m_getConfigRequest.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testCopyConfig() throws Exception{
        m_stubresponse.setMessageId("1");
        m_copyConfigRequest.setSourceRunning();
        m_copyConfigRequest.setMessageId("1");
        m_copyConfigRequest.setTargetRunning();
        m_copyConfigRequest.setSourceConfigElement((DocumentUtils.stringToDocument(m_EDIT_REQ_CONTENT).getDocumentElement()));
        Document requestDocument = m_copyConfigRequest.getRequestDocument();
        m_response = m_internalNetconfClient.sendRpcMessage(requestDocument);

        verify(m_netconfLogger).setThreadLocalDeviceLogId(requestDocument);
        verify(m_netconfLogger).setThreadLocalDeviceLogId(null);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }

    @Test
    public void testDeleConfig() throws Exception{
        m_stubresponse.setMessageId("1");
        DeleteConfigRequest request = new DeleteConfigRequest();
        request.setTargetRunning();
        request.setMessageId("1");
        Document requestDocument = request.getRequestDocument();
        m_response = m_internalNetconfClient.sendRpcMessage(requestDocument);

        verify(m_netconfLogger).setThreadLocalDeviceLogId(requestDocument);
        verify(m_netconfLogger).setThreadLocalDeviceLogId(null);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }

    @Test
    public void testGetRequest() throws Exception{
        m_stubresponse.setMessageId("1");
        m_getRequest.setIncludeConfig(false);
        m_getRequest.setMessageId("1");

        m_response = m_internalNetconfClient.sendRpcMessage(m_getRequest.getRequestDocument());

        assertEquals("1", m_response.getMessageId());
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testCloseSession() throws Exception{
        m_stubresponse.setMessageId("1");
        CloseSessionRequest request = new CloseSessionRequest();
        request.setMessageId("1");

        m_response = m_internalNetconfClient.sendRpcMessage(request.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }

    @Test
    public void testKillSession() throws Exception {
        m_stubresponse.setMessageId("1");
        KillSessionRequest request = new KillSessionRequest();
        request.setMessageId("1");
        request.setSessionId(123);

        m_response = m_internalNetconfClient.sendRpcMessage(request.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }

    @Test
    public void testLock() throws Exception {
        m_stubresponse.setMessageId("1");
        LockRequest request = new LockRequest();
        request.setMessageId("1");
        request.setTargetRunning();

        m_response = m_internalNetconfClient.sendRpcMessage(request.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }

    @Test
    public void testUnLock() throws Exception{
        m_stubresponse.setMessageId("1");
        UnLockRequest unRequest = new UnLockRequest();
        unRequest.setMessageId("1");
        unRequest.setTargetRunning();

        m_response = m_internalNetconfClient.sendRpcMessage(unRequest.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }

    @Test
    public void testCreateSubscription() throws Exception {
        m_stubresponse.setMessageId("1");
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setMessageId("1");

        m_response = m_internalNetconfClient.sendRpcMessage(request.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals("1", m_response.getMessageId());
    }

    @Test
    public void testEditConfigMethod() throws Exception {
        m_stubresponse.setMessageId("1");
        m_editConfigRequest.setTargetRunning();
        m_editConfigRequest.setMessageId("1");
        m_editConfigRequest.setConfigElement(new EditConfigElement().addConfigElementContent(DocumentUtils.stringToDocument(m_EDIT_REQ_CONTENT).getDocumentElement()));
        m_response = m_internalNetconfClient.editConfig(m_editConfigRequest);

        assertEquals("1", m_response.getMessageId());
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetConfigMethod() throws Exception {
        m_stubresponse.setMessageId("1");
        m_getConfigRequest.setSourceRunning();
        m_getConfigRequest.setMessageId("1");
        m_response = m_internalNetconfClient.getConfig(m_getConfigRequest);

        assertEquals("1", m_response.getMessageId());
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetConfigMethodWithMessageIdAsString() throws Exception {
        m_stubresponse.setMessageId("test");
        m_getConfigRequest.setSourceRunning();
        m_getConfigRequest.setMessageId(String.valueOf("test"));
        m_internalNetconfClient.setRunningUT(true);
        m_response = m_internalNetconfClient.getConfig(m_getConfigRequest);
        assertEquals("test", m_response.getMessageId());
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetConfigMethodWithMessageIdAsInteger() throws Exception {
        m_stubresponse.setMessageId("13938");
        m_getConfigRequest.setSourceRunning();
        m_getConfigRequest.setMessageId("13938");
        m_internalNetconfClient.setRunningUT(true);
        m_response = m_internalNetconfClient.getConfig(m_getConfigRequest);
        assertEquals((String)"13938", m_response.getMessageId());
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void tetstRpcMethod() throws Exception{
        m_stubresponse.setMessageId("1");
        m_netConfRPCRequest.setMessageId("1");
        m_response = m_internalNetconfClient.rpc(m_netConfRPCRequest);

        assertNotNull(m_response);
        assertEquals("1", m_response.getMessageId());
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetMethod() throws Exception {
        m_stubresponse.setMessageId("1");
        m_getRequest.setIncludeConfig(false);
        m_getRequest.setMessageId("1");
        m_response = m_internalNetconfClient.get(m_getRequest);

        assertNotNull(m_response);
        assertEquals("1", m_response.getMessageId());
        assertTrue(m_response.isOk());
        assertEquals(m_netconfLogger, m_internalNetconfClient.getNetconfLogger());
    }

    @Test
    public void testSpecialRpcRequestType() throws Exception {
        m_stubresponse.setMessageId("1");
        m_netConfRPCRequest.setRpcInput(DocumentUtils.stringToDocument(m_EDIT_REQ_CONTENT).getDocumentElement());
        m_netConfRPCRequest.setMessageId("1");

        m_response = m_internalNetconfClient.sendRpcMessage(m_netConfRPCRequest.getRequestDocument());

        assertNotNull(m_response);
        assertEquals("1", m_response.getMessageId());
        assertTrue(m_response.isOk());
    }

    @Test
    public void testSendRpcRequestWithException() throws Exception {
        String req = "<rpc message-id=\"1235\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<get>\n" +
                "<filter type=\"subtree\">\n" +
                "<platform:platform xmlns:platform=\"http://www.test-company.com/solutions/anv-platform\">\n" +
                "</platform:platform>\n" +
                "</filter>\n" +
                "<depth xmlns=\"http://www.test-company.com/solutions/netconf-extensions\"></depth> \n" +
                "</get>\n" +
                "</rpc>";

        Document document = DocumentUtils.stringToDocument(req);
        m_response = m_internalNetconfClient.sendRpcMessage(document);
        assertNotNull(m_response);
        assertFalse(m_response.isOk());
        assertEquals("RPC parsing error - The value of depth \"\" is not valid. Expected length is [1...65535].", m_response.getErrors().get(0).getErrorMessage());
    }
}
