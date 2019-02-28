package org.broadband_forum.obbaa.netconf.mn.fwk.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

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
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;


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
    public void setUp(){
        m_serverMessageHandler = mock(ServerMessageHandler.class);
        m_netconfLogger = mock(NetconfLogger.class);
        m_internalNetconfClient = new InternalNetconfClientImpl("admin", m_serverMessageHandler, m_netconfLogger);
        m_stubresponse.setOk(true);
        m_internalNetconfClient.getResponsesQueue().put(String.valueOf(m_internalNetconfClient.getRequestId().get()+1),m_stubresponse);
        m_internalNetconfClient.getResponsesQueue().put(String.valueOf(m_internalNetconfClient.getRequestId().get()),m_stubresponse);
        m_internalNetconfClient.getResponsesQueue().put(String.valueOf(m_internalNetconfClient.getRequestId().get()+2),m_stubresponse);
        m_internalNetconfClient.getResponsesQueue().put(String.valueOf(m_internalNetconfClient.getRequestId().get()+13938),m_stubresponse);

    }

    @Test
    public void testMessageIdMissing() throws Exception {
        DocumentBuilderFactory dbf =  DocumentBuilderFactory.newInstance();
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
        m_editConfigRequest.setTargetRunning();
        m_editConfigRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()));
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
        
    }
    
    @Test
    public void testGetConfig() throws Exception{

        m_getConfigRequest.setSourceRunning();
        m_getConfigRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        Document requestDocument = m_getConfigRequest.getRequestDocument();
        m_response = m_internalNetconfClient.sendRpcMessage(requestDocument);

        verify(m_netconfLogger).setThreadLocalDeviceLogId(requestDocument);
        verify(m_netconfLogger).setThreadLocalDeviceLogId(null);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
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

        m_copyConfigRequest.setSourceRunning();
        m_copyConfigRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        m_copyConfigRequest.setTargetRunning();
        m_copyConfigRequest.setSourceConfigElement((DocumentUtils.stringToDocument(m_EDIT_REQ_CONTENT).getDocumentElement()));
        Document requestDocument = m_copyConfigRequest.getRequestDocument();
        m_response = m_internalNetconfClient.sendRpcMessage(requestDocument);

        verify(m_netconfLogger).setThreadLocalDeviceLogId(requestDocument);
        verify(m_netconfLogger).setThreadLocalDeviceLogId(null);
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testDeleConfig() throws Exception{
        DeleteConfigRequest request = new DeleteConfigRequest();
        request.setTargetRunning();
        request.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        Document requestDocument = request.getRequestDocument();
        m_response = m_internalNetconfClient.sendRpcMessage(requestDocument);

        verify(m_netconfLogger).setThreadLocalDeviceLogId(requestDocument);
        verify(m_netconfLogger).setThreadLocalDeviceLogId(null);
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetRequest() throws Exception{

        m_getRequest.setIncludeConfig(false);
        m_getRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));

        m_response = m_internalNetconfClient.sendRpcMessage(m_getRequest.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testCloseSession() throws Exception{
        CloseSessionRequest request = new CloseSessionRequest();
        request.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));

        m_response = m_internalNetconfClient.sendRpcMessage(request.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testKillSession() throws Exception{
        KillSessionRequest request = new KillSessionRequest();
        request.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        request.setSessionId(123);

        m_response = m_internalNetconfClient.sendRpcMessage(request.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }



    @Test
    public void testLock() throws Exception{
        LockRequest request = new LockRequest();
        request.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        request.setTargetRunning();

        m_response = m_internalNetconfClient.sendRpcMessage(request.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());

    }

    @Test
    public void testUnLock() throws Exception{
        UnLockRequest unRequest = new UnLockRequest();
        unRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        unRequest.setTargetRunning();

        m_response = m_internalNetconfClient.sendRpcMessage(unRequest.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());


    }

    @Test
    public void testCreateSubscription() throws Exception{
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));

        m_response = m_internalNetconfClient.sendRpcMessage(request.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());

    }

    @Test
    public void testEditConfigMethod() throws Exception{
        m_editConfigRequest.setTargetRunning();
        m_editConfigRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        m_editConfigRequest.setConfigElement(new EditConfigElement().addConfigElementContent(DocumentUtils.stringToDocument(m_EDIT_REQ_CONTENT).getDocumentElement()));
        m_response = m_internalNetconfClient.editConfig(m_editConfigRequest);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetConfigMethod() throws Exception{
        m_getConfigRequest.setSourceRunning();
        m_getConfigRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        m_response = m_internalNetconfClient.getConfig(m_getConfigRequest);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetConfigMethodWithMessageIdAsString() throws Exception{
        m_getConfigRequest.setSourceRunning();
        m_getConfigRequest.setMessageId(String.valueOf("test"));
        m_internalNetconfClient.setRunningUT(true);
        m_response = m_internalNetconfClient.getConfig(m_getConfigRequest);
        assertEquals(1, m_internalNetconfClient.getRequestId().get());
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetConfigMethodWithMessageIdAsInteger() throws Exception{
        m_getConfigRequest.setSourceRunning();
        m_getConfigRequest.setMessageId(String.valueOf("13938"));
        m_internalNetconfClient.setRunningUT(true);
        m_response = m_internalNetconfClient.getConfig(m_getConfigRequest);
        assertEquals(13938, m_internalNetconfClient.getRequestId().get());
        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void tetstRpcMethod() throws Exception{

        m_netConfRPCRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        m_response = m_internalNetconfClient.rpc(m_netConfRPCRequest);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }

    @Test
    public void testGetMethod() throws Exception{
        m_getRequest.setIncludeConfig(false);
        m_getRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));
        m_response = m_internalNetconfClient.get(m_getRequest);

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
        assertEquals(m_netconfLogger, m_internalNetconfClient.getNetconfLogger());
    }

    @Test
    public void testSpecialRpcRequestType() throws Exception{
        m_netConfRPCRequest.setRpcInput(DocumentUtils.stringToDocument(m_EDIT_REQ_CONTENT).getDocumentElement());
        m_netConfRPCRequest.setMessageId(String.valueOf(m_internalNetconfClient.getRequestId().get()+1));

        m_response = m_internalNetconfClient.sendRpcMessage(m_netConfRPCRequest.getRequestDocument());

        assertNotNull(m_response);
        assertTrue(m_response.isOk());
    }


}
