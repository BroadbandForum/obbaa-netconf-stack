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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.netconf.api.client.ContextSetter;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.logger.ual.NCUserActivityLogHandler;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.stack.logging.ual.UALLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class ServerMessageHandlerTest {

    private ServerMessageHandlerImpl m_serverMessageHandler;
    private NetconfServerMessageListener m_serverMessageListener;
    private RequestScheduler m_requestScheduler;
    private NotificationService m_notificationService;
    private NetconfLogger m_netconfLogger;
    private NCUserActivityLogHandler m_ncUserActivityLogHandler;
    private UALLogger m_ualLogger;
    private NetconfClientInfo m_clientInfo;
    RequestTaskPostRequestExecuteListener m_requestPostExecuteListener = mock(RequestTaskPostRequestExecuteListener.class);

    @Before
    public void init() {
        RequestContext.reset();
        m_serverMessageListener = mock(NetconfServerMessageListener.class);
        m_requestScheduler = mock(RequestScheduler.class);
        m_notificationService = mock(NotificationService.class);
        m_netconfLogger = mock(NetconfLogger.class);
        m_ncUserActivityLogHandler = mock(NCUserActivityLogHandler.class);
        m_ualLogger = mock(UALLogger.class);
        m_serverMessageHandler = new ServerMessageHandlerImpl(m_serverMessageListener, m_requestScheduler, m_notificationService,
                m_netconfLogger, m_ncUserActivityLogHandler, m_ualLogger);
        m_clientInfo = mock(NetconfClientInfo.class);
        when(m_clientInfo.getUsername()).thenReturn("user1");
        when(m_clientInfo.getClientSessionId()).thenReturn("sessionId");
    }
    
    @Test
    public void testProcessGUIRequest() {
        AbstractNetconfRequest request= mock(AbstractNetconfRequest.class);
        ResponseChannel channel= mock(ResponseChannel.class);
        RequestTask requestTask = new RequestTask(m_clientInfo, request, channel, m_serverMessageListener, m_netconfLogger,
                m_ncUserActivityLogHandler, m_ualLogger);
        m_serverMessageHandler = new ServerMessageHandlerImpl(m_serverMessageListener, m_requestScheduler, m_notificationService,
                m_netconfLogger, m_ncUserActivityLogHandler, m_ualLogger) {
            
            @Override
            RequestTask getRequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest request, ResponseChannel channel) {
                return requestTask;
            }
        };

        RequestContext.setRequestCategoryTL(RequestCategory.GUI);
        RequestContext.setLoggedInUserCtxtTL(new UserContext("userName",""));
        RequestContext.setApplicationTL("application");
        m_serverMessageHandler.processRequest(m_clientInfo, request, channel);
        assertEquals(requestTask.getRequestContext().getRequestCategory(), RequestCategory.GUI);
        assertEquals(new UserContext("userName", ""), requestTask.getRequestContext().getLoggedInUserCtxt());
        assertEquals("application", requestTask.getRequestContext().getApplication());
        verify(m_requestScheduler).scheduleTask(any(RequestTask.class));
    }
    
    @Test
    public void testProcessNBIRequest() {
        AbstractNetconfRequest request= mock(AbstractNetconfRequest.class);
        ResponseChannel channel= mock(ResponseChannel.class);
        RequestTask requestTask = new RequestTask(m_clientInfo, request, channel, m_serverMessageListener, m_netconfLogger,
                m_ncUserActivityLogHandler, m_ualLogger);
        m_serverMessageHandler = new ServerMessageHandlerImpl(m_serverMessageListener, m_requestScheduler, m_notificationService,
                m_netconfLogger, m_ncUserActivityLogHandler, m_ualLogger) {
            
            @Override
            RequestTask getRequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest request, ResponseChannel channel) {
                return requestTask;
            }
        };
        m_serverMessageHandler.setRequestTaskPostRequestExecuteListener(m_requestPostExecuteListener);
        RequestContext.setRequestCategoryTL(RequestCategory.NBI);
        m_serverMessageHandler.processRequest(m_clientInfo, request, channel);
        assertEquals(m_requestPostExecuteListener, requestTask.getRequestTaskPostRequestExecuteListener());
        assertEquals(requestTask.getRequestContext().getRequestCategory(), RequestCategory.NBI);
        assertEquals(new UserContext("user1","sessionId"),requestTask.getRequestContext().getLoggedInUserCtxt());
        assertNull(requestTask.getRequestContext().getAdditionalUserCtxt());
        assertNull(requestTask.getRequestContext().getApplication());
        verify(m_requestScheduler).scheduleTask(any(RequestTask.class));
    }
    
    @Test
    public void testProcessExternalNBIRequest() {
        NetconfClientInfo clientInfo = new NetconfClientInfo("testUser", 0, "testSession");
        ContextSetter contextSetter = mock(ContextSetter.class);
        when(contextSetter.getSessionId()).thenReturn("puttur-vittla-sessiond-id");
        clientInfo.setClientContextSetter(contextSetter);
        AbstractNetconfRequest request = mock(AbstractNetconfRequest.class);
        ResponseChannel channel = mock(ResponseChannel.class);
        RequestContext.reset();
        RequestTask requestTask = new RequestTask(clientInfo, request, channel, m_serverMessageListener, m_netconfLogger,
                m_ncUserActivityLogHandler, m_ualLogger);
        m_serverMessageHandler = new ServerMessageHandlerImpl(m_serverMessageListener, m_requestScheduler, m_notificationService,
                m_netconfLogger, m_ncUserActivityLogHandler, m_ualLogger) {

            @Override
            RequestTask getRequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest request, ResponseChannel channel) {
                return requestTask;
            }
        };
        m_serverMessageHandler.processRequest(clientInfo, request, channel);
        assertEquals(new UserContext("testUser", "puttur-vittla-sessiond-id"), requestTask.getRequestContext().getLoggedInUserCtxt());
    }
    
    @After
    public void destroy() {
        m_serverMessageHandler = null;
        m_serverMessageListener = null;
        m_requestScheduler = null;
        m_notificationService = null;
        m_netconfLogger = null;
        RequestContext.reset();
    }
}
