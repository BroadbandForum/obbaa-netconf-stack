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

package org.broadband_forum.obbaa.netconf.driver.client;

import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nhtoan on 3/9/16.
 */
public class NotificationQueueTest {

    private NotificationQueue m_notificationQueue = new NotificationQueue();

    @Test
    public void testNotificationQueued() {
        Notification notification = mock(Notification.class);
        String testContent = "test content notification";
        when(notification.notificationToString()).thenReturn(testContent);
        m_notificationQueue.notificationReceived(notification);
        List<String> notifications = m_notificationQueue.retrieveAllNotification();
        assertEquals(1, notifications.size());
        assertEquals(testContent, notifications.get(0));

        String notificationContent = m_notificationQueue.retrieveNotification();
        assertEquals(testContent, notificationContent);

        m_notificationQueue.clearAllNotifications();
        assertTrue(m_notificationQueue.retrieveAllNotification().isEmpty());
    }

}
