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
import org.broadband_forum.obbaa.netconf.api.logger.ual.NCUserActivityLogHandler;
import org.broadband_forum.obbaa.netconf.server.util.MessageHandlerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.ual.UALLogger;
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
    private final NCUserActivityLogHandler m_ncUserActivityLogHandler;
    private UALLogger m_ualLogger;
    private RequestTaskPostRequestExecuteListener requestTaskPostRequestExecuteListener;

    public ServerMessageHandlerImpl(NetconfServerMessageListener serverMessageListener, RequestScheduler requestScheduler,
                                    NotificationService notificationService, NetconfLogger netconfLogger,
                                    NCUserActivityLogHandler ncUserActivityLogHandler, UALLogger ualLogger) {
        m_serverMessageListener = serverMessageListener;
        m_requestScheduler = requestScheduler;
        m_notificationService = notificationService;
        m_netconfLogger = netconfLogger;
        m_ncUserActivityLogHandler = ncUserActivityLogHandler;
        m_ualLogger = ualLogger;
    }
    
    RequestTask getRequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest request, final ResponseChannel channel) {
        return new RequestTask(clientInfo, request, channel, m_serverMessageListener, m_netconfLogger,m_ncUserActivityLogHandler, m_ualLogger);
    }
    
    @Override
    public void processRequest(NetconfClientInfo clientInfo, AbstractNetconfRequest request, final ResponseChannel channel) {
		try {
			RequestTask requestTask = getRequestTask(clientInfo, request, channel);
			requestTask.setRequestTaskPostRequestListener(requestTaskPostRequestExecuteListener);
			MessageHandlerUtil.updateRequestTask(clientInfo, request, requestTask, m_notificationService, m_ualLogger);

			// submit request task to requestScheduler for queuing and processing
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Scheduling a incoming request from: {} with message-id: {} ", clientInfo,
						request.getMessageId());
			}
			m_requestScheduler.scheduleTask(requestTask);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Scheduled a incoming request from: {} with message-id: {} successfully.", clientInfo,
						request.getMessageId());
			}
		} finally {
			m_ualLogger.resetCanLog();
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

    public void setRequestTaskPostRequestExecuteListener(RequestTaskPostRequestExecuteListener requestTaskPostRequestExecuteListener) {
        this.requestTaskPostRequestExecuteListener = requestTaskPostRequestExecuteListener;
    }
}
