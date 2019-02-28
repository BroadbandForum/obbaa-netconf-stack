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

package org.broadband_forum.obbaa.netconf.api.client;

import static org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionGetServerCapabilityTestUtil.getNetConfResponses;
import static org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionGetServerCapabilityTestUtil.getYangModulesResponseElementWithDeviations;
import static org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionGetServerCapabilityTestUtil.getYangModulesResponseElementWithSimYang;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.broadband_forum.obbaa.netconf.api.client.util.AbstractNetconfClientSessionTestSetUp;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StateChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;

public class AbstractNetconfClientSessionTest extends AbstractNetconfClientSessionTestSetUp {
    private static final String TEST_STREAM = "testStream";
    private static final String TEST_SOURCE = "testSource";
    private static final String TEST_OPTION = "testOption";
    private static final String TEST_OPERATION = "testOperation";
    private static final String TEST_TARGET = "testTarget";
    private static final String RPC_REPLY_NAMESPACE = "urn:ietf:params:xml:ns:netconf:base:1.0";
    private static final String RPC_REPLY = "rpc-reply";

    private static boolean isRpcReplyDocument(NetConfResponse netConfResponse) throws NetconfMessageBuilderException {
        String localName = netConfResponse.getResponseDocument().getDocumentElement().getLocalName();
        String namespaceURI = netConfResponse.getResponseDocument().getDocumentElement().getNamespaceURI();

        if (localName.equals(RPC_REPLY) && namespaceURI.equals(RPC_REPLY_NAMESPACE))
            return true;

        return false;
    }

    @Test
    public void testGetConfig() throws NetconfMessageBuilderException {
        GetConfigRequest getConfigRequest = new GetConfigRequest();
        getConfigRequest.setSource(TEST_SOURCE);
        assertNotNull(m_abstractNetconfClientSession.getConfig(getConfigRequest));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<get-config>"
                        + "<source>"
                        + "<testSource/>"
                        + "</source>"
                        + "</get-config>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testEditConfig() throws NetconfMessageBuilderException {
        EditConfigRequest editConfigRequest = new EditConfigRequest();
        editConfigRequest.setTarget(TEST_TARGET);
        editConfigRequest.setDefaultOperation(TEST_OPERATION);
        editConfigRequest.setTestOption(TEST_OPTION);
        EditConfigElement editConfigElement = new EditConfigElement();
        editConfigRequest.setConfigElement(editConfigElement);
        assertNotNull(m_abstractNetconfClientSession.editConfig(editConfigRequest));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<edit-config>"
                        + "<target><testTarget/></target>"
                        + "<default-operation>testOperation</default-operation>"
                        + "<test-option>testOption</test-option>"
                        + "<error-option>stop-on-error</error-option>"
                        + "<config/>"
                        + "</edit-config>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testCopyConfig() throws NetconfMessageBuilderException {
        CopyConfigRequest configRequest = new CopyConfigRequest();
        configRequest.setSourceRunning();
        configRequest.setTargetRunning();
        assertNotNull(m_abstractNetconfClientSession.copyConfig(configRequest));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<copy-config>"
                        + "<target><running/></target>"
                        + "<source><running/></source>"
                        + "</copy-config>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testDeleteConfig() throws NetconfMessageBuilderException {
        DeleteConfigRequest deleteConfigRequest = new DeleteConfigRequest();
        deleteConfigRequest.setTarget(TEST_TARGET);
        assertNotNull(m_abstractNetconfClientSession.deleteConfig(deleteConfigRequest));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<delete-config>"
                        + "<target><testTarget/></target>"
                        + "</delete-config>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testLock() throws NetconfMessageBuilderException {
        LockRequest lockRequest = new LockRequest();
        lockRequest.setTargetRunning();
        assertNotNull(m_abstractNetconfClientSession.lock(lockRequest));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<lock>"
                        + "<target><running/></target>"
                        + "</lock>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testUnlock() throws NetconfMessageBuilderException {
        UnLockRequest unLockRequest = new UnLockRequest();
        unLockRequest.setTargetRunning();
        assertNotNull(m_abstractNetconfClientSession.unlock(unLockRequest));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<unlock>"
                        + "<target><running/></target>"
                        + "</unlock>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testGetRequest() throws NetconfMessageBuilderException {
        GetRequest request = new GetRequest();
        assertNotNull(m_abstractNetconfClientSession.get(request));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<get/>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testNetconfRpcRequest() throws NetconfMessageBuilderException {
        String rpcRequest =
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<get/>"
                        + "</rpc>";
        NetconfRpcRequest netconfRpcRequest = DocumentToPojoTransformer
                .getRpcRequest(DocumentUtils.stringToDocument(rpcRequest));
        assertNotNull(m_abstractNetconfClientSession.rpc(netconfRpcRequest));
        assertEquals(rpcRequest + "]]>]]>", m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testActionRequest() throws NetconfMessageBuilderException {
        String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:action-list>" +
                "<test:name>apache</test:name>" +
                "<test:reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:reset>" +
                "</test:action-list>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";

        ActionRequest request = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
        assertNotNull(m_abstractNetconfClientSession.action(request));
        assertEquals(actionRequest + "]]>]]>", m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testKillSession() throws NetconfMessageBuilderException {

        KillSessionRequest killSessionReq = new KillSessionRequest();
        killSessionReq.setSessionId(10);
        assertNotNull(m_abstractNetconfClientSession.killSession(killSessionReq));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<kill-session>"
                        + "<session-id>10</session-id>"
                        + "</kill-session>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testCloseSession() throws NetconfMessageBuilderException {

        CloseSessionRequest closeRequest = new CloseSessionRequest();
        assertNotNull(m_abstractNetconfClientSession.closeSession(closeRequest));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<close-session/>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testResponseReceivedForRpcReplyDocument() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, TimeoutException {
        CloseSessionRequest closeRequest = new CloseSessionRequest();
        CompletableFuture<NetConfResponse> future = m_abstractNetconfClientSession.closeSession(closeRequest);
        assertNotNull(future);
        final boolean[] called = {false};
        future.whenComplete((netConfResponse, throwable) -> called[0] = true);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
        assertFalse(called[0]);
        m_abstractNetconfClientSession.responseRecieved(getDocumentFromFilePath("sampleRpcReply.xml"));
        assertTrue(called[0]);
        NetConfResponse netConfResponse = future.get();
        assertTrue(isRpcReplyDocument(netConfResponse));
        assertEquals("1", netConfResponse.getMessageId());
        assertTrue(netConfResponse.isOk());
        verify(m_abstractNetconfClientSession, times(2)).resetIdleTimeStart();
    }

    @Test
    public void testResponseReceivedForRpcReplyDocumentForException() {
        try {
            m_abstractNetconfClientSession.responseRecieved(getDocumentFromFilePath("missing-message-id-error.xml"));
            fail("Should have thrown an exception");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(
                    "NetconfRpcError [errorType=rpc, errorTag=missing-attribute, errorSeverity=error, errorAppTag=null, errorPath=null, "
                            + "errorMessage=<message-id> cannot be null/empty, "
                            + "errorInfoContent=<bad-attribute xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">message-id</bad-attribute>"
                            + "\n<bad-element xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">rpc</bad-element>]",
                    e.getMessage());
        }
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testResponseReceivedForNotificationDocument() throws NetconfMessageBuilderException {
        Document helloDocument = getDocumentFromFilePath("stateChangeNotification.xml");
        final Notification notification = DocumentToPojoTransformer.getNotification(helloDocument);
        NotificationListener listener = new NotificationListener() {
            @Override
            public void notificationReceived(Notification notif) {
                assertEquals(notification.notificationToString(), notif.notificationToString());
            }
        };
        NotificationListener mockListener = Mockito.spy(listener);
        m_abstractNetconfClientSession.addNotificationListener(mockListener);
        m_abstractNetconfClientSession.responseRecieved(helloDocument);

        DateTime expectedDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime("2016-02-02T13:33:35+07:00");
        DateTime actualDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime(notification.getEventTime());

        assertEquals(expectedDateTime, actualDateTime);

        verify(mockListener).notificationReceived(any(Notification.class));

        assertTrue(notification instanceof StateChangeNotification);
        assertEquals("song-count", ((StateChangeNotification) notification).getTarget());
        assertEquals("8", ((StateChangeNotification) notification).getValue());
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testGetAndSetServerCapabilities() throws NetconfMessageBuilderException {
        Set<String> capabilities = new HashSet<>();
        capabilities.add("urn:ietf:params:netconf:capability:candidate:1.0");
        capabilities.add("urn:ietf:params:netconf:base:1.1");
        capabilities.add("urn:ietf:params:netconf:base:1.0");

        NetconfClientSessionListener netconfClientSessionListener = mock(NetconfClientSessionListener.class);

        m_abstractNetconfClientSession.addSessionListener(netconfClientSessionListener);

        // This will set Server Capabilities using the document
        m_abstractNetconfClientSession.responseRecieved(getDocumentFromFilePath("hello.xml"));

        assertTrue(m_abstractNetconfClientSession.getServerCapability("urn:ietf:params:netconf:capability:candidate:1.0"));
        assertFalse(m_abstractNetconfClientSession.getServerCapability("urn:ietf:params:netconf:base:1.2"));
        assertEquals(47, m_abstractNetconfClientSession.getSessionId());
        assertEquals(capabilities, m_abstractNetconfClientSession.getServerCapabilities());

        m_abstractNetconfClientSession.sessionClosed();
    }

    @Test
    public void testGetAndSetClientCapabilities() {
        Set<String> capabilities = new HashSet<>();
        capabilities.add("urn:ietf:params:netconf:base:1.1");
        capabilities.add("urn:ietf:params:netconf:base:1.0");
        m_abstractNetconfClientSession.setClientCapabilities(capabilities);

        assertTrue(m_abstractNetconfClientSession.getClientCapability("urn:ietf:params:netconf:base:1.1"));
        assertFalse(m_abstractNetconfClientSession.getClientCapability("urn:ietf:params:netconf:base:1.2"));
        assertEquals(capabilities, m_abstractNetconfClientSession.getClientCapabilities());
    }

    @Test
    public void testCreateSubscription() throws NetconfMessageBuilderException {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setStream(TEST_STREAM);
        assertNotNull(m_abstractNetconfClientSession.createSubscription(request, m_notificationListener));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<create-subscription xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">"
                        + "<stream>testStream</stream>"
                        + "</create-subscription>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testGetIdleTimeStart() throws NetconfMessageBuilderException {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setStream(TEST_STREAM);
        long currentTimeMillis = System.currentTimeMillis();
        try {
            //by sleeping for 1 milli , we make sure the idletimestart gets reset to a higher value
            Thread.sleep(1L);
        } catch (InterruptedException e) {
            fail("interrupted while sleeping");
        }
        assertNotNull(m_abstractNetconfClientSession.createSubscription(request, m_notificationListener));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<create-subscription xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">"
                        + "<stream>testStream</stream>"
                        + "</create-subscription>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
        assertTrue(m_abstractNetconfClientSession.getIdleTimeStart() > currentTimeMillis);
    }

    @Test
    public void testResponseFutureCompletedWhenSessionClosed() throws Exception {
        String rpcRequest =
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<get/>"
                        + "</rpc>";
        NetconfRpcRequest netconfRpcRequest = DocumentToPojoTransformer
                .getRpcRequest(DocumentUtils.stringToDocument(rpcRequest));
        CompletableFuture<NetConfResponse> respFuture = m_abstractNetconfClientSession.rpc(netconfRpcRequest);
        assertNotNull(respFuture);
        m_abstractNetconfClientSession.sessionClosed();
        NetConfResponse response = respFuture.get();
        assertNull(response);
    }

    @Test
    public void testWhetherCapabilityUpdatedWithLatestYangLibraryFlagUpdatedDuringCapabilityChangeNotificationMessage() throws NetconfMessageBuilderException {
        Mockito.doReturn(null).when(m_abstractNetconfClientSession).get(any());
        m_abstractNetconfClientSession.responseRecieved(getDocumentFromFilePath("hello_yang1.1.xml"));
        Mockito.doReturn(getNetConfResponses(getYangModulesResponseElementWithDeviations())).when(m_abstractNetconfClientSession).get(any());
        m_abstractNetconfClientSession.getServerCapabilities();
        assertEquals(12, m_abstractNetconfClientSession.getServerCapabilities().size());
        m_abstractNetconfClientSession.responseRecieved(getDocumentFromFilePath("CapabilityChangeNotification.xml"));
        Mockito.doReturn(getNetConfResponses(getYangModulesResponseElementWithSimYang())).when(m_abstractNetconfClientSession).get(any());
        m_abstractNetconfClientSession.getServerCapabilities();
        assertEquals(18, m_abstractNetconfClientSession.getServerCapabilities().size());
    }

}