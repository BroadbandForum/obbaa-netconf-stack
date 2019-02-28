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

package org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.server.rpc.CreateSubscriptionRpcHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;

public class CreateSubscriptionRpcHandlerImplTest {
    private RpcName m_rpcQName;
    private NotificationService m_notificationService;
    private CreateSubscriptionRpcHandler m_createSubscriptionRpcHandler;
    private NetconfClientInfo m_clientInfo;
    private NetconfRpcRequest m_request;

    @Before
    public void setUp() {
        m_notificationService = mock(NotificationService.class);
        m_clientInfo = new NetconfClientInfo("admin", 1);
        m_request = new NetconfRpcRequest();
        Element element = mock(Element.class);
        m_request.setRpcInput(element);
        m_rpcQName = new RpcName("urn:ietf:params:xml:ns:netconf:notification:1.0", "create-subscription");
        m_createSubscriptionRpcHandler = new CreateSubscriptionRpcHandlerImpl(m_notificationService, m_rpcQName);
    }

    @Test
    public void testProcessRequestReturnNotification() throws RpcProcessException {
        NetconfRpcResponse response = mock(NetconfRpcResponse.class);
        try {
            m_createSubscriptionRpcHandler.processRequest(m_clientInfo, m_request, response);
            fail();
        } catch (RpcProcessException expectedEx) {
            assertTrue(expectedEx.toString().contains("create-subscription request requires callback dispatcher"));
        }
    }

    @Test
    public void testProcessRequest() throws RpcProcessException, ParseException {
        ResponseChannel responseChannel = mock(ResponseChannel.class);
        m_createSubscriptionRpcHandler.processRequest(m_clientInfo, m_request, responseChannel);
        CreateSubscriptionRequest subscriptionRequest = DocumentUtils.getInstance().getSubscriptionRequest(m_request);
        Matcher<CreateSubscriptionRequest> matcher = new ArgumentMatcher<CreateSubscriptionRequest>() {
            @Override
            public boolean matches(Object o) {
                CreateSubscriptionRequest expected = (CreateSubscriptionRequest) o;
                return expected != null && expected.getStream().equals(subscriptionRequest.getStream());
            }
        };
        verify(m_notificationService).createSubscription(eq(m_clientInfo), argThat(matcher), eq(responseChannel));
    }

    @Test
    public void testProcessRequestWithException() throws Exception {
        try {
            ResponseChannel responseChannel = mock(ResponseChannel.class);
            NetconfRpcRequest request = m_request;
            when(DocumentUtils.getInstance().getSubscriptionRequest(request)).thenThrow(ParseException.class);
            m_createSubscriptionRpcHandler.processRequest(m_clientInfo, request, responseChannel);
            fail("Expect NotificationService send create subscription request but RpcProcessException is threw");
        } catch (RpcProcessException e) {
            assertTrue(e.toString().contains("Parsing date failed"));
        }
    }

    @Test
    public void testValidate() throws RpcValidationException {
        RpcPayloadConstraintParser rpcConstraintParser = mock(RpcPayloadConstraintParser.class);
        NetconfMessage request = new NetconfRpcRequest();
        m_createSubscriptionRpcHandler.validate(rpcConstraintParser, request);
        verify(rpcConstraintParser).validate((NetconfRpcRequest)request, RequestType.RPC);
    }
}
