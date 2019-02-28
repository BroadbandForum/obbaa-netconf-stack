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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.NotificationComplete;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

/**
 * Created by pregunat on 1/29/16.
 */
public class NotificationSessionImplTest {

    private NotificationSession m_notificationSession;
    private ResponseChannel m_responseChannel;
    private final Notification m_notification = mock(Notification.class);
    private ScheduledExecutorService m_dispatchTaskExecutor;

    @Before
    public void setup() {
        m_dispatchTaskExecutor = mock(ScheduledExecutorService.class);
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setMessageId("1");
        request.setStream(NetconfResources.NETCONF);
        m_responseChannel = mock(ResponseChannel.class);
        m_notificationSession = new NotificationSessionImpl(clientInfo, request, m_responseChannel, m_dispatchTaskExecutor);
        CreateSubscriptionRequest request2 = new CreateSubscriptionRequest();
        request2.setFilter(mock(NetconfFilter.class));
    }

    @Test
    public void testDispatchNotification() {
        m_notificationSession.sendNotification(m_notification);
        verify(m_responseChannel, times(1)).sendNotification(m_notification);
    }

    @Test
    public void testAddNotificationToQueue() {
        //set m_notificationReplayTask != null
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(m_notification);
        m_notificationSession.replayNotification(notificationList);
        m_notificationSession.sendNotification(m_notification);
        Queue<Notification> notificationQueue = ((NotificationSessionImpl) m_notificationSession).getNotificationQueue();
        assertTrue(!notificationQueue.isEmpty());
        assertEquals(m_notification, notificationQueue.peek());
    }

    @Test
    public void testReplayNotification() {
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(m_notification);
        m_notificationSession.replayNotification(notificationList);
        NotificationReplayTask notificationReplayTask = new NotificationReplayTask(notificationList, m_responseChannel,
                mock(PostReplayCallBack.class));
        Matcher<NotificationReplayTask> matcher = new ArgumentMatcher<NotificationReplayTask>() {
            @Override
            public boolean matches(Object o) {
                NotificationReplayTask expected = (NotificationReplayTask) o;
                return expected.getPostReplayCallBack() != null && expected.getNotificationList()
                        .equals(notificationReplayTask.getNotificationList()) && expected.getResponseChannel()
                        .equals(notificationReplayTask.getResponseChannel());
            }
        };
        verify(m_dispatchTaskExecutor).execute(argThat(matcher));

    }

    @Test
    public void testSendNotificationComplete() {
        m_notificationSession.sendNotificationComplete();
        verify(m_responseChannel, times(1)).sendNotification(any(NotificationComplete.class));
    }

    @Test
    public void testAddNotificationCompleteToQueue() {
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(m_notification);
        m_notificationSession.replayNotification(notificationList);
        m_notificationSession.sendNotificationComplete();
        Queue<Notification> notificationQueue = ((NotificationSessionImpl) m_notificationSession).getNotificationQueue();
        assertTrue(!notificationQueue.isEmpty());
    }

    @Test
    public void testClosed() {
        ((NotificationSessionImpl) m_notificationSession).close();
    }
}
