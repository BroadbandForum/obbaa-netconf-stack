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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerMessageHandlerTest {

    private ServerMessageHandlerImpl m_serverMessageHandler;
    private NetconfServerMessageListener m_serverMessageListener;
    private RequestScheduler m_requestScheduler;
    private NotificationService m_notificationService;
    private NetconfLogger m_netconfLogger;
    
    @BeforeClass
    public static void start(){
        RequestScope.setEnableThreadLocalInUT(true);
    }
    
    @AfterClass
    public static void stop(){
        RequestScope.setEnableThreadLocalInUT(false);
    }
    
    @Before
    public void init() {
        m_serverMessageListener = mock(NetconfServerMessageListener.class);
        m_requestScheduler = mock(RequestScheduler.class);
        m_notificationService = mock(NotificationService.class);
        m_netconfLogger = mock(NetconfLogger.class);
        m_serverMessageHandler = new ServerMessageHandlerImpl(m_serverMessageListener, m_requestScheduler, m_notificationService, m_netconfLogger);
    }
    
    @Test
    public void testProcessGUIRequest() {
        NetconfClientInfo clientInfo = mock(NetconfClientInfo.class);
        AbstractNetconfRequest request= mock(AbstractNetconfRequest.class);
        ResponseChannel channel= mock(ResponseChannel.class);
        RequestTask requestTask = new RequestTask(clientInfo, request, channel, m_serverMessageListener, m_netconfLogger);
        m_serverMessageHandler = new ServerMessageHandlerImpl(m_serverMessageListener, m_requestScheduler, m_notificationService, m_netconfLogger) {
            
            @Override
            RequestTask getRequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest request, ResponseChannel channel) {
                return requestTask;
            }
        };
        
        RequestScope.getCurrentScope().putInCache(RequestContext.REQUEST_CATEGORY_NAME, RequestCategory.GUI);
        m_serverMessageHandler.processRequest(clientInfo, request, channel);
        assertEquals(requestTask.getRequestContext().getRequestCategory(), RequestCategory.GUI);
        verify(m_requestScheduler).scheduleTask(any(RequestTask.class));
    }
    
    @Test
    public void testProcessNBIRequest() {
        NetconfClientInfo clientInfo = mock(NetconfClientInfo.class);
        AbstractNetconfRequest request= mock(AbstractNetconfRequest.class);
        ResponseChannel channel= mock(ResponseChannel.class);
        RequestTask requestTask = new RequestTask(clientInfo, request, channel, m_serverMessageListener, m_netconfLogger);
        m_serverMessageHandler = new ServerMessageHandlerImpl(m_serverMessageListener, m_requestScheduler, m_notificationService, m_netconfLogger) {
            
            @Override
            RequestTask getRequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest request, ResponseChannel channel) {
                return requestTask;
            }
        };
        
        RequestScope.getCurrentScope().putInCache(RequestContext.REQUEST_CATEGORY_NAME, RequestCategory.NBI);
        m_serverMessageHandler.processRequest(clientInfo, request, channel);
        assertEquals(requestTask.getRequestContext().getRequestCategory(), RequestCategory.NBI);
        verify(m_requestScheduler).scheduleTask(any(RequestTask.class));
    }
    
    @After
    public void destroy() {
        m_serverMessageHandler = null;
        m_serverMessageListener = null;
        m_requestScheduler = null;
        m_notificationService = null;
        m_netconfLogger = null;
        RequestScope.resetScope();
    }
}
