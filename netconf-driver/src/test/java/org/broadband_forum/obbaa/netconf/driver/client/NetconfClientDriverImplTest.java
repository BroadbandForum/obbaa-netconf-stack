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

/**
 * 
 */
package org.broadband_forum.obbaa.netconf.driver.client;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.client.notification.NetconfNotificationListener;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getNetconfResponse;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.documentToPrettyString;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * 
 */
public class NetconfClientDriverImplTest {

    private NetconfClientSession m_clientSession;

    private NetconfClientDriverImpl m_netconfDriver;

    private NetconfNotificationListener m_notificationListener;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        m_clientSession = Mockito.mock(NetconfClientSession.class);
        m_netconfDriver = new NetconfClientDriverImpl(m_clientSession);
        m_notificationListener = Mockito.mock(NetconfNotificationListener.class);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        m_clientSession = null;
        m_netconfDriver = null;
    }

    @Test
    public void testSendGetRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, SAXException,
            IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                GetRequest actualGetRequest = (GetRequest) invocation.getArguments()[0];
                assertEquals(20000L, actualGetRequest.getReplyTimeout());

                // verify GetRequest properly constructed
                assertEquals("1", actualGetRequest.getMessageId());
                NetconfFilter getFilter = actualGetRequest.getFilter();
                assertEquals(NetconfResources.SUBTREE_FILTER, getFilter.getType());

                // return test response object
                Document responseDocument = stringToDocument(loadAsString("getResponse.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).get(Mockito.any(GetRequest.class));

        // test get Request on netconf driver
        String response = m_netconfDriver.sendGetRequest(loadAsString("getRequest.xml"), 20000L);

        // verify test response serialized properly
        Element expectedResponse = stringToDocument(loadAsString("getResponse.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);

    }

    @Test
    public void testSendGetConfigRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, SAXException,
            IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                GetConfigRequest actualGetConfigRequest = (GetConfigRequest) invocation.getArguments()[0];
                assertEquals(30000L, actualGetConfigRequest.getReplyTimeout());

                assertEquals("2", actualGetConfigRequest.getMessageId());

                Document responseDocument = stringToDocument(loadAsString("getConfigResponse.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).getConfig(Mockito.any(GetConfigRequest.class));

        String response = m_netconfDriver.sendGetConfigRequest(loadAsString("getConfigRequest.xml"), 30000L);

        Element expectedResponse = stringToDocument(loadAsString("getConfigResponse.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);

    }

    @Test
    public void testSendEditConfigRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, SAXException,
            IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                EditConfigRequest actualEditConfigRequest = (EditConfigRequest) invocation.getArguments()[0];
                assertEquals(40000L, actualEditConfigRequest.getReplyTimeout());

                assertEquals("3", actualEditConfigRequest.getMessageId());
                assertEquals("running", actualEditConfigRequest.getTarget());
                assertEquals("set", actualEditConfigRequest.getTestOption());

                Document responseDocument = stringToDocument(loadAsString("ok-response.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).editConfig(Mockito.any(EditConfigRequest.class));

        String response = m_netconfDriver.sendEditConfigRequest(loadAsString("editConfigRequest.xml"), 40000L);

        Element expectedResponse = stringToDocument(loadAsString("ok-response.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);

    }

    @Test
    public void testSendCopyConfigRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, SAXException,
            IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                CopyConfigRequest actualCopyConfigRequest = (CopyConfigRequest) invocation.getArguments()[0];
                assertEquals(50000L, actualCopyConfigRequest.getReplyTimeout());

                assertEquals("4", actualCopyConfigRequest.getMessageId());
                assertEquals("running", actualCopyConfigRequest.getSource());
                assertEquals("candidate", actualCopyConfigRequest.getTarget());

                Document responseDocument = stringToDocument(loadAsString("ok-response.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).copyConfig(Mockito.any(CopyConfigRequest.class));

        String response = m_netconfDriver.sendCopyConfigRequest(loadAsString("copyConfigRequest.xml"), 50000L);

        Element expectedResponse = stringToDocument(loadAsString("ok-response.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);

    }

    @Test
    public void testSendRpcRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, SAXException,
            IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public CompletableFuture<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                NetconfRpcRequest actualRpcRequest = (NetconfRpcRequest) invocation.getArguments()[0];
                assertEquals(60000L, actualRpcRequest.getReplyTimeout());

                assertEquals("5", actualRpcRequest.getMessageId());

                Document responseDocument = stringToDocument(loadAsString("ok-response.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).rpc(Mockito.any(NetconfRpcRequest.class));

        String response = m_netconfDriver.sendRpcRequest(loadAsString("rpcRequest.xml"), 60000L);

        Element expectedResponse = stringToDocument(loadAsString("ok-response.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);

    }

    @Test
    public void testSendLockRequestRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, SAXException,
            IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                LockRequest actualLockRequest = (LockRequest) invocation.getArguments()[0];
                assertEquals(70000L, actualLockRequest.getReplyTimeout());

                assertEquals("6", actualLockRequest.getMessageId());
                assertEquals("running", actualLockRequest.getTarget());

                Document responseDocument = stringToDocument(loadAsString("ok-response.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).lock(Mockito.any(LockRequest.class));

        String response = m_netconfDriver.sendLockRequest(loadAsString("lockRequest.xml"), 70000L);

        Element expectedResponse = stringToDocument(loadAsString("ok-response.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testSendUnLockRequestRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException,
            SAXException, IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                UnLockRequest actualUnlockRequest = (UnLockRequest) invocation.getArguments()[0];
                assertEquals(80000L, actualUnlockRequest.getReplyTimeout());

                assertEquals("7", actualUnlockRequest.getMessageId());
                assertEquals("running", actualUnlockRequest.getTarget());

                Document responseDocument = stringToDocument(loadAsString("ok-response.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).unlock(Mockito.any(UnLockRequest.class));

        String response = m_netconfDriver.sendUnLockRequest(loadAsString("unlockRequest.xml"), 80000L);

        Element expectedResponse = stringToDocument(loadAsString("ok-response.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testSendCloseSessionRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException,
            SAXException, IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                CloseSessionRequest actualCloseSessionRequest = (CloseSessionRequest) invocation.getArguments()[0];
                assertEquals(90000L, actualCloseSessionRequest.getReplyTimeout());

                assertEquals("8", actualCloseSessionRequest.getMessageId());

                Document responseDocument = stringToDocument(loadAsString("ok-response.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).closeSession(Mockito.any(CloseSessionRequest.class));

        String response = m_netconfDriver.sendCloseSessionRequest(loadAsString("closeSessionRequest.xml"), 90000L);

        Element expectedResponse = stringToDocument(loadAsString("ok-response.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testSendKillSessionRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, SAXException,
            IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                KillSessionRequest actualKillSessionRequest = (KillSessionRequest) invocation.getArguments()[0];
                assertEquals(60000L, actualKillSessionRequest.getReplyTimeout());

                assertEquals("9", actualKillSessionRequest.getMessageId());
                assertEquals("4", actualKillSessionRequest.getSessionId().toString());

                Document responseDocument = stringToDocument(loadAsString("ok-response.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).killSession(Mockito.any(KillSessionRequest.class));

        String response = m_netconfDriver.sendKillSessionRequest(loadAsString("killSessionRequest.xml"));

        Element expectedResponse = stringToDocument(loadAsString("ok-response.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testSendCreateSubscriptionRequest() throws NetconfMessageBuilderException, InterruptedException, ExecutionException,
            SAXException, IOException {

        Mockito.doAnswer(new Answer<Future<NetConfResponse>>() {
            @Override
            public Future<NetConfResponse> answer(InvocationOnMock invocation) throws Throwable {
                CreateSubscriptionRequest actualCreateSubscriptionRequest = (CreateSubscriptionRequest) invocation.getArguments()[0];
                assertEquals(100000L, actualCreateSubscriptionRequest.getReplyTimeout());

                assertEquals("10", actualCreateSubscriptionRequest.getMessageId());
                assertEquals("NETCONF", actualCreateSubscriptionRequest.getStream());

                Document responseDocument = stringToDocument(loadAsString("ok-response.xml"));
                final NetConfResponse testResponse = getNetconfResponse(responseDocument);
                return new FutureNetconfReponse(testResponse);
            }
        }).when(m_clientSession).createSubscription(Mockito.any(CreateSubscriptionRequest.class), Mockito.any(NotificationListener.class));
        NotificationListener notificationListener = Mockito.mock(NotificationListener.class);
        String response = m_netconfDriver
                .sendCreateSubscriptionRequest(loadAsString("createSubscriptionRequest.xml"), notificationListener, 100000L);

        Element expectedResponse = stringToDocument(loadAsString("ok-response.xml")).getDocumentElement();
        Element actualResponse = stringToDocument(response).getDocumentElement();
        assertXMLEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testSendANotWellformedXMLRequest() throws InterruptedException, ExecutionException, NetconfMessageBuilderException,
            SAXException, IOException {

        String errorResponse = m_netconfDriver.sendEditConfigRequest(loadAsString("notWellformedXMLRequest.xml"), null);
        Element expectedErrorResponse = stringToDocument(loadAsString("errorWellformed-response.xml")).getDocumentElement();
        Element actualErrorResponse = stringToDocument(errorResponse).getDocumentElement();
        assertXMLEquals(expectedErrorResponse, actualErrorResponse);
    }

    public static boolean assertXMLEquals(Element expectedOutput, Element actualOutput) throws SAXException, IOException,
            NetconfMessageBuilderException {
        String expected = documentToPrettyString(expectedOutput);
        String actual = documentToPrettyString(actualOutput);
        Diff diff = new Diff(expected, actual);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        diff.overrideDifferenceListener(new DifferenceListener() {
            @Override
            public void skippedComparison(Node arg0, Node arg1) {
            }

            @Override
            public int differenceFound(Difference arg0) {
                if (DifferenceConstants.CHILD_NODELIST_SEQUENCE.equals(arg0)) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                return RETURN_ACCEPT_DIFFERENCE;
            }
        });
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        boolean result = diff.identical();
        assertTrue(result);
        return result;
    }

    public static String loadAsString(String name) {
        StringBuilder sb = new StringBuilder();

        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
