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

import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;

public interface NetconfLogger {
    void logRequest(String remoteHost, String remotePort, String userName, String sessionId, Document request);
    void logResponse(String remoteHost, String remotePort, String userName, String sessionId, Document response, AbstractNetconfRequest request);
    void logNotificationIn(String remoteHost, String remotePort, String userName, String sessionId, Document notification);
    void logNotificationOut(String stream, Notification notification);
    void setThreadLocalDeviceLogId(Document doc);
}
