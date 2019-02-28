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

package org.broadband_forum.obbaa.netconf.server;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

public class RequestTaskTest {

    private NetconfServerMessageListener m_serverMessageListener;
    private EditConfigRequest m_editConfigRequest;
    NetconfConfigChangeNotification m_netconfConfigChangeNotification;
    private List<Notification> m_notifications;
    @Mock
    private NotificationService m_notificationService;
    @Mock
    protected ResponseChannel m_responseChannel;
    @Mock
    private NetconfLogger m_netconfLogger;
    private NetconfClientInfo m_clientInfo;

    @Before
    public void setUp() throws NetconfMessageBuilderException {
        m_clientInfo = new NetconfClientInfo("ut",1);
        m_clientInfo.setRemoteHost("keshava-nilaya-gudra");
        m_clientInfo.setRemotePort("574241");
        MockitoAnnotations.initMocks(this);
        m_serverMessageListener = Mockito.mock(NetconfServerMessageListener.class);
        m_netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        m_notifications = new ArrayList<>();
        m_notifications.add(m_netconfConfigChangeNotification);
        m_editConfigRequest = new EditConfigRequest();
        m_editConfigRequest.setConfigElement(new EditConfigElement().addConfigElementContent(DocumentUtils.stringToDocument("<adh:device xmlns:adh=\"http://www.test-company.com/solutions/anv-device-holders\" > \n" +
                "    <adh:device-id>R1.S1.LT1.PON1.ONT1</adh:device-id> \n" +
                "    <adh:hardware-type>SX16F</adh:hardware-type> \n" +
                "    <adh:interface-version>5.6</adh:interface-version> \n" +
                "    <adh:duid>TESTANV.R1.S1.LT1.PON1.ONT1</adh:duid> \n" +
                "</adh:device> ").getDocumentElement()));
        m_editConfigRequest.setMessageId("1");
        when(m_responseChannel.isSessionClosed()).thenReturn(false);
        ArgumentMatcher<EditConfigRequest> ediConfigRequest = new ArgumentMatcher<EditConfigRequest>() {

            @Override
            public boolean matches(Object argument) {
                EditConfigRequest editConfig = (EditConfigRequest) argument;
                return editConfig.getMessageId().equals("1");
            }
        };
        when(m_serverMessageListener.onEditConfig(any(NetconfClientInfo.class), argThat(ediConfigRequest), any(NetConfResponse.class)))
                .thenReturn(m_notifications);

    }

    @Test
    public void testSentNetConfConfigChangeNotification() throws NetconfMessageBuilderException {
        RequestTask requestTask = new RequestTask(m_clientInfo, m_editConfigRequest, m_responseChannel, m_serverMessageListener, m_netconfLogger);
        requestTask.setNotificationService(m_notificationService);
        requestTask.doExecuteRequest();
        ArgumentCaptor<Document> documentArgumentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(m_notificationService, times(1)).sendNotification(NetconfResources.CONFIG_CHANGE_STREAM, m_netconfConfigChangeNotification);
        verify(m_netconfLogger).logRequest(eq("keshava-nilaya-gudra"), eq("574241"), eq("ut"), eq("1"), documentArgumentCaptor.capture());
        assertEquals(DocumentUtils.documentToPrettyString(m_editConfigRequest.getRequestDocument()), DocumentUtils.documentToPrettyString(documentArgumentCaptor.getValue()));
        verify(m_netconfLogger).logResponse(eq("keshava-nilaya-gudra"), eq("574241"), eq("ut"), eq("1"), any(Document.class), eq(m_editConfigRequest));
    }

    @Test
    public void testResponseSentWhenThereIsExceptionWhileHandlingRpcMessage() throws NetconfMessageBuilderException {
        RequestTask requestTask = new RequestTask(m_clientInfo, m_editConfigRequest, m_responseChannel, m_serverMessageListener, m_netconfLogger);
        requestTask.setNotificationService(m_notificationService);
        doThrow(new RuntimeException("I feel like failing today")).when(m_serverMessageListener).onEditConfig(anyObject(), anyObject(), anyObject());
        requestTask.run();
        ArgumentCaptor<NetConfResponse> responseCaptor = ArgumentCaptor.forClass(NetConfResponse.class);
        verify(m_responseChannel).sendResponse(responseCaptor.capture(), eq(m_editConfigRequest));

        assertEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rpc-error>\n" +
                "    <error-type>application</error-type>\n" +
                "    <error-tag>operation-failed</error-tag>\n" +
                "    <error-severity>error</error-severity>\n" +
                "    <error-message>I feel like failing today</error-message>\n" +
                "  </rpc-error>\n" +
                "</rpc-reply>\n", responseCaptor.getValue().responseToString());
    }

    @Test
    public void testRunSetsMessageId() throws NetconfMessageBuilderException {

        NetconfRpcResponse rpcResponse = new NetconfRpcResponse();
        rpcResponse.setMessageId("rpc-id");

        m_editConfigRequest.setMessageId("new-id");
        NetconfServerMessageListener listener = new ServerMessageListenerAdapter()  {
            @Override
            public List<Notification> onEditConfig(NetconfClientInfo info, EditConfigRequest req, NetConfResponse resp) {
                assertNotNull(req.getMessageId());
                assertNotNull(resp.getMessageId());
                assertEquals(req.getMessageId(), resp.getMessageId());
                return null;
            }

            @Override
            public List<Notification> onRpc(NetconfClientInfo info, NetconfRpcRequest rpcRequest, NetconfRpcResponse response) {
                assertNotNull(rpcRequest.getMessageId());
                assertNotNull(response.getMessageId());
                assertEquals(rpcRequest.getMessageId(), response.getMessageId());
                return null;
            }
        };
        RequestTask requestTask = new RequestTask(m_clientInfo, m_editConfigRequest, m_responseChannel, listener, m_netconfLogger);
        requestTask.run();

        NetconfRpcRequest rpcReq = new CreateSubscriptionRequest();
        rpcReq.setMessageId("rpc-id");
        requestTask = new RequestTask(m_clientInfo, rpcReq, m_responseChannel, listener, m_netconfLogger);
        requestTask.run();

    }

}
