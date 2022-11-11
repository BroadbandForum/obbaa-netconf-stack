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

package org.broadband_forum.obbaa.netconf.api.logger;

import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class DefaultNetconfLogger implements NetconfLogger {

    public static final String NETCONF_LOGGER_NAME = "NETCONF_LOGGER";
    public static final String NBI_REQUEST_MSG = "Got request from";
    public static final String NBI_RESPONSE_MSG = "Sending response to";
    public static final String SBI_REQUEST_MSG = "Sent request to";
    public static final String SBI_RESPONSE_MSG = "Got response from";
    public static final String SBI_NOTIF_MSG = "Got notification from";
    public static final String NETCONF_REQUEST_TIMEDOUT_MSG = "Netconf request timed out on";

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NETCONF_LOGGER_NAME, LogAppNames.NETCONF_LIB);
    private final AdvancedLogger m_logger;

    public DefaultNetconfLogger() {
        this(LOGGER);
    }

    public DefaultNetconfLogger(AdvancedLogger logger) {
        m_logger = logger;
    }

    @Override
    public void logRequest(String remoteHost, String remotePort, String userName, String sessionId, Document request) {
        log(NBI_REQUEST_MSG, remoteHost, remotePort, userName, sessionId, request);
    }

    @Override
    public void logRequest(String remoteHost, String remotePort, String userName, String sessionId, AbstractNetconfRequest request) {
        try {
            logRequest(remoteHost, remotePort, userName, sessionId, request.getRequestDocument());
        } catch (NetconfMessageBuilderException e) {
            m_logger.error("Could not get request document", e);
            //ignore
        }
    }

    @Override
    public void logResponse(String remoteHost, String remotePort, String userName, String sessionId, Document response, AbstractNetconfRequest request) {
        logResponse(remoteHost, remotePort, userName, sessionId, response, request, null);
    }

    @Override
    public void logResponse(String remoteHost, String remotePort, String userName, String sessionId, Document response,
                            AbstractNetconfRequest request, Long responseTimeMillis) {
        log(NBI_RESPONSE_MSG, remoteHost, remotePort, userName, sessionId, response);
    }

    @Override
    public void logNotificationIn(String remoteHost, String remotePort, String userName, String sessionId, Document notification) {
        log(SBI_NOTIF_MSG, remoteHost, remotePort, userName, sessionId, notification);
    }

    @Override
    public void logNotificationOut(String stream, Notification notification) {
        m_logger.debug("broadcasting notification to stream {}", stream, notification.notificationToPrettyString());
    }

    @Override
    public void setThreadLocalDeviceLogId(Document doc) {
        // Do nothing.
    }

    private void log(String message, String remoteHost, String remotePort, String userName, String sessionId, Document doc) {
        try {
            m_logger.debug(message + " {}/{} ( {} ) session-id {} \n {} \n", m_logger.sensitiveData(remoteHost), m_logger.sensitiveData(remotePort),
                    m_logger.sensitiveData(userName), m_logger.sensitiveData(sessionId), DocumentUtils.documentToPrettyString(doc));
        } catch (NetconfMessageBuilderException e) {
            m_logger.error("Error while logging ", e);
        }

    }

}
