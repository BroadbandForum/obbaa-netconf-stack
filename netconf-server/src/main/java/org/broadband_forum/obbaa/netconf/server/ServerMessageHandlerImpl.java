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

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class ServerMessageHandlerImpl implements ServerMessageHandler {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ServerMessageHandlerImpl.class, LogAppNames.NETCONF_LIB);
    protected NetconfServerMessageListener m_serverMessageListener;
    protected RequestScheduler m_requestScheduler;
    private NotificationService m_notificationService;
    private final NetconfLogger m_netconfLogger;

    public ServerMessageHandlerImpl(NetconfServerMessageListener serverMessageListener, RequestScheduler requestScheduler,
                                    NotificationService notificationService, NetconfLogger netconfLogger) {
        m_serverMessageListener = serverMessageListener;
        m_requestScheduler = requestScheduler;
        m_notificationService = notificationService;
        m_netconfLogger = netconfLogger;
    }
    
    RequestTask getRequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest request, final ResponseChannel channel) {
        return new RequestTask(clientInfo, request, channel, m_serverMessageListener, m_netconfLogger);
    }
    
    @Override
    public void processRequest(NetconfClientInfo clientInfo, AbstractNetconfRequest request, final ResponseChannel channel) {
        RequestTask requestTask = getRequestTask(clientInfo, request, channel);

        //Adding request context to Request Task
        Object requestCatgoryObj = RequestScope.getCurrentScope().getFromCache(RequestContext.REQUEST_CATEGORY_NAME);
        RequestCategory requestCategoryFromCache = RequestCategory.class.cast(requestCatgoryObj);
        
        //Make the default request category as NBI, if no category is setting in RequestScope
        RequestCategory requestCategory = requestCategoryFromCache == null ? RequestCategory.NBI : requestCategoryFromCache;
        RequestContext requestContext = new RequestContext(requestCategory);
        requestTask.setNotificationService(m_notificationService);
        requestTask.setRequestContext(requestContext);
        
        // submit request task to requestScheduler for queuing and processing
        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("Scheduling a incoming request from: {} with message-id: {} ", clientInfo, request.getMessageId());
        }
        m_requestScheduler.scheduleTask(requestTask);
        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("Scheduled a incoming request from: {} with message-id: {} successfully.", clientInfo,
                    request.getMessageId());
        }
    }

    @Override
    public NetconfServerMessageListener getServerMessageListener() {
        return m_serverMessageListener;
    }

    @Override
    public void setServerMessageListener(NetconfServerMessageListener listener) {
        m_serverMessageListener = listener;
    }

}
