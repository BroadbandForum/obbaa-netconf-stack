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
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.stack.DefaultNcNotificationCounterService;
import org.broadband_forum.obbaa.netconf.stack.NcNotificationCounterService;

public class NotificationSessionInvocationHandlerTest {

	private NotificationSessionInvocationHandler m_notificationSessionInvocationHandler;
	private NcNotificationCounterService m_counterInterceptor;
	private CreateSubscriptionRequest m_createSubscriptionRequest;
	private NetconfClientInfo m_clientInfo = new NetconfClientInfo("test", 1);
	private ResponseChannel m_responseChannel;
	private Executor m_notifExecutor;
	private Notification[] m_args;
	private Notification m_notification;

	private Method m_method;

	@Before
	public void setUp() throws Exception {
		m_counterInterceptor = new DefaultNcNotificationCounterService();
		m_createSubscriptionRequest = mock(CreateSubscriptionRequest.class);
		m_responseChannel = mock(ResponseChannel.class);
		/*m_threadMgr = mock(ThreadManager.class);
		m_taskCategory = new TaskCategory("notificationServiceTaskCategory", "Handling Netconf notification sessions",
				1, 1, 100, 0, 2000);*/
		m_notifExecutor = Executors.newCachedThreadPool();
		m_method = NotificationSession.class.getMethod(NotificationSession.SEND_NOTIFICATION_METHOD, Notification.class);
		m_notification = new NetconfNotification();
		m_args = new Notification[] { m_notification };
		m_notificationSessionInvocationHandler = new NotificationSessionInvocationHandler(m_counterInterceptor,
				m_clientInfo, m_createSubscriptionRequest, m_responseChannel, m_notifExecutor);	
	}

	@Test
	public void testNbiNotificationsInterceptorInvocation() throws Throwable {
		m_notificationSessionInvocationHandler.invoke(null, m_method, m_args);
		assertEquals(1, m_counterInterceptor.getNumberOfNotifications());
		assertEquals(1, m_counterInterceptor.getOutNotifications(m_clientInfo.getSessionId()));		
	}
}
