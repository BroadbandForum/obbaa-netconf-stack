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
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NotificationStopTaskTest {
    private NotificationStopTask m_notificationStopTask;
    @Before
    public void setUp() {
        m_notificationStopTask = new NotificationStopTask();
    }

    @Test
    public void testExecute() throws Exception {
        NetconfClientInfo netconfClientInfo = new NetconfClientInfo("test", 1);
        NotificationStream notificationStream = mock(NotificationStream.class);
        Map<String, Object> context = new HashMap<>();
        context.put(NotificationStopTask.NETCONF_CLIENT_INFO, netconfClientInfo);
        context.put(NotificationStopTask.NOTIFICATION_STREAM, notificationStream);
        m_notificationStopTask.execute(context);
        verify(notificationStream).stopNotification(netconfClientInfo);
    }
}
