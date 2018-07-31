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

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LogUtil;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;

import org.apache.log4j.Logger;

public class ServerMessageHandlerImpl implements ServerMessageHandler {
    private static final Logger LOGGER = Logger.getLogger(ServerMessageHandlerImpl.class);
    protected NetconfServerMessageListener m_serverMessageListener;
    protected RequestScheduler m_requestScheduler;
    private NotificationService m_notificationService;
    private final NetconfLogger m_netconfLogger;

    public ServerMessageHandlerImpl(NetconfServerMessageListener serverMessageListener, RequestScheduler
            requestScheduler,
                                    NotificationService notificationService, NetconfLogger netconfLogger) {
        m_serverMessageListener = serverMessageListener;
        m_requestScheduler = requestScheduler;
        m_notificationService = notificationService;
        m_netconfLogger = netconfLogger;
    }

    @Override
    public void processRequest(NetconfClientInfo clientInfo, AbstractNetconfRequest request, final ResponseChannel
            channel) {
        RequestTask requestTask = new RequestTask(clientInfo, request, channel, m_serverMessageListener,
                m_netconfLogger);
        requestTask.setNotificationService(m_notificationService);
        // submit request task to requestScheduler for queuing and processing
        LogUtil.logTrace(LOGGER, "Scheduling a incoming request from: %s with message-id: %s ", clientInfo, request
                .getMessageId());
        m_requestScheduler.scheduleTask(requestTask);
        LogUtil.logTrace(LOGGER, "Scheduled a incoming request from: %s with message-id: %s successfully.", clientInfo,
                request.getMessageId());
    }

}
