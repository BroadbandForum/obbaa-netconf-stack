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

import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class DefaultNetconfLogger implements NetconfLogger {

    public static final String NETCONF_LOGGER_NAME = "NETCONF_LOGGER";
    public static final String REQUEST_MSG = "Got request from";
    public static final String RESPONSE_MSG = "Sending response to";
    public static final String NOTIF_MSG = "Got notification from";

    private static final Logger LOGGER = LoggerFactory.getLogger(NETCONF_LOGGER_NAME);

    @Override
    public void logRequest(String remoteHost, String remotePort, String userName, String sessionId, Document request) {
        log(REQUEST_MSG, remoteHost, remotePort, userName, sessionId, request);
    }

    @Override
    public void logResponse(String remoteHost, String remotePort, String userName, String sessionId, Document
            response) {
        log(RESPONSE_MSG, remoteHost, remotePort, userName, sessionId, response);
    }

    @Override
    public void logNotificationIn(String remoteHost, String remotePort, String userName, String sessionId, Document
            notification) {
        log(NOTIF_MSG, remoteHost, remotePort, userName, sessionId, notification);
    }

    @Override
    public void logNotificationOut(String stream, Notification notification) {
        LOGGER.debug("broadcasting notification to stream {}", stream, notification.notificationToPrettyString());
    }

    private void log(String message, String remoteHost, String remotePort, String userName, String sessionId,
                     Document doc) {
        try {
            LOGGER.debug(message + " {}/{} ( {} ) session-id {} \n {} \n", remoteHost, remotePort, userName, sessionId,
                    DocumentUtils.documentToPrettyString(doc));
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while logging ", e);
        }

    }

}
