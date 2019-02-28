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

package org.broadband_forum.obbaa.netconf.server.model.notification;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationStream;

import java.util.Map;

public class NotificationStopTask implements Runnable {

    public static final String NOTIFICATION_STREAM = "NotificationStream";

    public static final String NETCONF_CLIENT_INFO = "NetconfClientInfo";
    private final Map<String, Object> m_context;

    public NotificationStopTask() {
        m_context = null;
    }

    public NotificationStopTask(Map<String, Object> context) {
        m_context = context;
    }

    public void execute(Map<String, Object> context) {
        NotificationStream notificationStream = (NotificationStream) context.get(NOTIFICATION_STREAM);
        NetconfClientInfo clientInfo = (NetconfClientInfo) context.get(NETCONF_CLIENT_INFO);
        notificationStream.stopNotification(clientInfo);
    }

    @Override
    public void run() {
        if (m_context != null) {
            execute(m_context);
        }
    }
}
