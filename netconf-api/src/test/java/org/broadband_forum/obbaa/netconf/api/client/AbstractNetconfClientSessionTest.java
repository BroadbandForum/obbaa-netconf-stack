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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfDelimiters;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StateChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;

import io.netty.channel.Channel;

public class AbstractNetconfClientSessionTest {

    private static final String TEST_STREAM = "testStream";
    private static final String TEST_SOURCE = "testSource";
    private static final String TEST_OPTION = "testOption";
    private static final String TEST_OPERATION = "testOperation";
    private static final String TEST_TARGET = "testTarget";
    private static final String RPC_REPLY_NAMESPACE = "urn:ietf:params:xml:ns:netconf:base:1.0";
    private static final String RPC_REPLY = "rpc-reply";

    private AbstractNetconfClientSession m_abstractNetconfClientSession;

    private ExecutorService m_executorService;
    private Future<NetConfResponse> m_futureResponse;
    private String m_obtainedXmlString;
    private NotificationListener m_notificationListener;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {

        m_executorService = mock(ExecutorService.class);
        m_futureResponse = mock(Future.class);
        when(m_executorService.submit(any(Callable.class))).thenReturn(m_futureResponse);
        m_abstractNetconfClientSession = spy(new AbstractNetconfClientSession() {
            Channel serverChannel = mock(Channel.class);
            ;

            @Override
            public void sendHeartBeat(long timeout) throws InterruptedException, IOException {

            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public long getCreationTime() {
                return 0;
            }

            @Override
            public SocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public void close() throws InterruptedException, IOException {

            }

            @Override
            public void closeAsync() {

            }

            @Override
            protected Future<NetConfResponse> sendRpcMessage(final String currentMessageId, Document requestDocument,
                                                             final long timoutMillis) {
                String xmlString = "";
                try {
                    xmlString = DocumentUtils.documentToString(requestDocument) + NetconfDelimiters
                            .rpcEndOfMessageDelimiterString();
                    logRequest(requestDocument, currentMessageId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                m_obtainedXmlString = xmlString;
                serverChannel.writeAndFlush(xmlString);
                Future<NetConfResponse> futureResponse = m_executorService.submit(new Callable<NetConfResponse>() {
                    @Override
                    public NetConfResponse call() throws Exception {
                        // wait for a certain time to get the message back
                        NetConfResponse response = m_rpcResponses.get(currentMessageId, timoutMillis, TimeUnit
                                .MILLISECONDS);
                        logResponse(response, currentMessageId);
                        return response;
                    }
                });

                return futureResponse;
            }
        });
        m_notificationListener = mock(NotificationListener.class);
    }

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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.getConfig(getConfigRequest));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.editConfig(editConfigRequest));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.copyConfig(configRequest));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.deleteConfig(deleteConfigRequest));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.lock(lockRequest));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.unlock(unLockRequest));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.get(request));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.rpc(netconfRpcRequest));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.action(request));
        assertEquals(actionRequest + "]]>]]>", m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testKillSession() throws NetconfMessageBuilderException {

        KillSessionRequest killSessionReq = new KillSessionRequest();
        killSessionReq.setSessionId(10);
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.killSession(killSessionReq));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.closeSession(closeRequest));
        assertEquals(
                "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "<close-session/>"
                        + "</rpc>]]>]]>",
                m_obtainedXmlString);
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testResponseReceivedForRpcReplyDocument() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleRpcReply.xml");
        File file = new File(url.getPath());
        Document rpcReplyDocument = DocumentUtils.getDocFromFile(file);
        m_abstractNetconfClientSession.responseRecieved(rpcReplyDocument);
        NetConfResponse netConfResponse = m_abstractNetconfClientSession.m_rpcResponses.get("1", 10000L, TimeUnit
                .MILLISECONDS);
        m_abstractNetconfClientSession.logResponse(netConfResponse, "1");
        m_abstractNetconfClientSession.logResponse(null, "1");
        assertTrue(isRpcReplyDocument(netConfResponse));
        assertEquals("1", netConfResponse.getMessageId());
        assertTrue(netConfResponse.isOk());
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testResponseReceivedForRpcReplyDocumentForException() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("missing-message-id-error.xml");
        File file = new File(url.getPath());
        Document rpcReplyDocument = DocumentUtils.getDocFromFile(file);
        try {
            m_abstractNetconfClientSession.responseRecieved(rpcReplyDocument);
            fail("Should have thrown an exception");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(
                    "NetconfRpcError [errorType=rpc, errorTag=missing-attribute, errorSeverity=error, " +
                            "errorAppTag=null, errorPath=null, "
                            + "errorMessage=<message-id> cannot be null/empty, "
                            + "errorInfoContent=<bad-attribute " +
                            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">message-id</bad-attribute>"
                            + "\n<bad-element xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">rpc</bad-element>]",
                    e.getMessage());
        }
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testResponseReceivedForNotificationDocument() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("stateChangeNotification.xml");
        File file = new File(url.getPath());
        Document helloDocument = DocumentUtils.getDocFromFile(file);
        m_abstractNetconfClientSession.addNotificationListener(m_notificationListener);
        m_abstractNetconfClientSession.responseRecieved(helloDocument);
        Notification notification = m_abstractNetconfClientSession.getNotificationReceived();

        DateTime expectedDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime
                ("2016-02-02T13:33:35+07:00");
        DateTime actualDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime(notification
                .getEventTime());

        assertEquals(expectedDateTime, actualDateTime);

        verify(m_notificationListener).notificationReceived(notification);

        assertTrue(notification instanceof StateChangeNotification);
        assertEquals("song-count", ((StateChangeNotification) notification).getTarget());
        assertEquals("8", ((StateChangeNotification) notification).getValue());
        verify(m_abstractNetconfClientSession).resetIdleTimeStart();
    }

    @Test
    public void testGetAndSetServerCapabilities() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("hello.xml");
        File file = new File(url.getPath());
        Document helloDocument = DocumentUtils.getDocFromFile(file);
        Set<String> capabilities = new HashSet<>();
        capabilities.add("urn:ietf:params:netconf:capability:candidate:1.0");
        capabilities.add("urn:ietf:params:netconf:base:1.1");
        capabilities.add("urn:ietf:params:netconf:base:1.0");

        NetconfClientSessionListener netconfClientSessionListener = mock(NetconfClientSessionListener.class);

        m_abstractNetconfClientSession.addSessionListener(netconfClientSessionListener);

        // This will set Server Capabilities using the document
        m_abstractNetconfClientSession.responseRecieved(helloDocument);

        assertTrue(m_abstractNetconfClientSession.getServerCapability
                ("urn:ietf:params:netconf:capability:candidate:1.0"));
        assertFalse(m_abstractNetconfClientSession.getServerCapability("urn:ietf:params:netconf:base:1.2"));
        assertEquals(47, m_abstractNetconfClientSession.getSessionId());
        assertEquals(capabilities, m_abstractNetconfClientSession.getServerCapabilities());

        m_abstractNetconfClientSession.sessionClosed();
    }

    @Test
    public void testGetAndSetClientCapabilities() throws NetconfMessageBuilderException {
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.createSubscription(request,
                m_notificationListener));
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
        assertEquals(m_futureResponse, m_abstractNetconfClientSession.createSubscription(request,
                m_notificationListener));
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
    public void testLogRequestWithIncompleteRpcDoesNotThrowException() throws NetconfMessageBuilderException {
        m_abstractNetconfClientSession.logRequest(DocumentUtils.stringToDocument("<invalid-rpc " +
                "xmlns=\"blah:blah\"/>"), "1");
    }

    @Test
    public void testIncrementAndGetFailedKACount() {
        m_abstractNetconfClientSession.incrementAndGetFailedKACount();
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(ai.get(), m_abstractNetconfClientSession.getKeepAliveFailure().get());

    }

    @Test
    public void testResetKACount() {
        m_abstractNetconfClientSession.incrementAndGetFailedKACount();
        AtomicInteger ai = new AtomicInteger(1);
        assertEquals(ai.get(), m_abstractNetconfClientSession.getKeepAliveFailure().get());
        m_abstractNetconfClientSession.resetKAFailureCount();
        ai.set(0);
        assertEquals(ai.get(), m_abstractNetconfClientSession.getKeepAliveFailure().get());

    }

}