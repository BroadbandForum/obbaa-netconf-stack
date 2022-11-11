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
import org.broadband_forum.obbaa.netconf.api.logger.DefaultNetconfLogger;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.logger.ual.DefaultNCUserActivityLogHandlerImpl;
import org.broadband_forum.obbaa.netconf.stack.logging.ual.DefaultUALLoggerImpl;
import org.broadband_forum.obbaa.netconf.api.logger.ual.NCUserActivityLogHandler;
import org.broadband_forum.obbaa.netconf.stack.logging.ual.UALLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

/**
 * Simply queues incoming requests.
 */
public class QueuingMessageHandler implements ServerMessageHandler {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(QueuingMessageHandler.class, LogAppNames.NETCONF_LIB);
    private NetconfServerMessageListener m_netconfServerMessageListener;
    private NetconfLogger m_netconfLogger;
    private NCUserActivityLogHandler m_ncUserActivityLogHandler;
    private UALLogger m_ualLogger;

    public QueuingMessageHandler(NetconfServerMessageListener netconfServerMessageListener) {
        m_netconfServerMessageListener = netconfServerMessageListener;
        m_netconfLogger = new DefaultNetconfLogger();
        m_ncUserActivityLogHandler = new DefaultNCUserActivityLogHandlerImpl();
        m_ualLogger = new DefaultUALLoggerImpl();
    }

    @Override
    public synchronized void processRequest(NetconfClientInfo clientInfo, AbstractNetconfRequest request, ResponseChannel channel) {
        LOGGER.debug(String.format("processing incoming request %s from client %s on channel %s", request, clientInfo, channel));
        RequestTask task = new RequestTask(clientInfo, request, channel, m_netconfServerMessageListener, m_netconfLogger,
                m_ncUserActivityLogHandler, m_ualLogger);
        task.run();
        LOGGER.debug(String.format("done processing incoming request %s from client %s on channel %s", request, clientInfo, channel));
    }

    @Override
    public NetconfServerMessageListener getServerMessageListener() {
        return m_netconfServerMessageListener;
    }

    @Override
    public void setServerMessageListener(NetconfServerMessageListener listener) {
        m_netconfServerMessageListener = listener;
    }
}
