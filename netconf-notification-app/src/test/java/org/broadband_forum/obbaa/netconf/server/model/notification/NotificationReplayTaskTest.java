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

import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.ReplayComplete;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.opendaylight.yangtools.yang.common.QName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NotificationReplayTaskTest {
    private ResponseChannel m_responseChannel;
    private PostReplayCallBack m_postReplayCallBack;
    private NotificationReplayTask m_notificationReplayTask;
    private List m_notificationList;

    @Before
    public void setUp() {
        m_responseChannel = mock(ResponseChannel.class);
        m_postReplayCallBack = mock(PostReplayCallBack.class);
        m_notificationList = new ArrayList<>();
        m_notificationReplayTask = new NotificationReplayTask(m_notificationList, m_responseChannel, m_postReplayCallBack);
    }

    @Test
    public void testRun() throws NetconfMessageBuilderException {
        Notification expectedNotif = mock(Notification.class);
        QName expectedQname = QName.create("urn:ietf:params:xml:ns:netconf:notification:1.0","expected-notification");
        when(expectedNotif.getType()).thenReturn(expectedQname);
        m_notificationList.add(expectedNotif);
        Matcher<Notification> matcher = new ArgumentMatcher<Notification>() {
            @Override
            public boolean matches(Object o) {
                Notification notification = (Notification) o;
                return notification.getType().equals(expectedQname);
            }
        };
        m_notificationReplayTask.run();
        verify(m_responseChannel).sendNotification(argThat(matcher));

        Matcher<Notification> replayNotifMatcher = new ArgumentMatcher<Notification>() {
            @Override
            public boolean matches(Object o) {
                return  ((Notification)o).getType().equals(ReplayComplete.TYPE);
            }
        };
        verify(m_responseChannel).sendNotification(argThat(replayNotifMatcher));
        verify(m_postReplayCallBack).postReplayAction();
        assertTrue(m_notificationReplayTask.isDone());
    }
}
