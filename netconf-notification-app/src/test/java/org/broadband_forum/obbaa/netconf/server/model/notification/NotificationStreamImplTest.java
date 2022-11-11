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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.server.model.notification.utils.NotificationFilterUtilTest;
import org.broadband_forum.obbaa.netconf.stack.DefaultNcNotificationCounterService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.w3c.dom.Document;

public class NotificationStreamImplTest {

	private NotificationStreamImpl m_notificationStream;

	private NetconfClientInfo m_clientInfo;
    private CreateSubscriptionRequest m_request;
    private DefaultNcNotificationCounterService m_notifCounterInterceptor;
    private ResponseChannel m_responseChannel;

	@Before
    public void setup() {
        m_request = new CreateSubscriptionRequest();
    	Stream stream = mock(Stream.class);
    	m_notifCounterInterceptor = mock(DefaultNcNotificationCounterService.class);
    	when(stream.getName()).thenReturn(NetconfResources.NETCONF);
    	when(stream.getReplaySupport()).thenReturn(true);
        NotificationLogger notificationLogger = mock(NotificationLogger.class);
        TimerManager scheduler = mock(TimerManager.class);
		m_clientInfo = mock(NetconfClientInfo.class);
        Executor notifExecutor = mock(Executor.class);
        m_responseChannel = mock(ResponseChannel.class);
        when(m_responseChannel.getCloseFuture()).thenReturn(new CompletableFuture<>());
        m_notificationStream = new NotificationStreamImpl(stream, notificationLogger, scheduler, notifExecutor, m_notifCounterInterceptor);
    }

    @Test
    public void testCreateSubscription() throws NetconfMessageBuilderException {
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        m_request.setMessageId("1");
        m_request.setStream(NetconfResources.NETCONF);
        m_notificationStream.createSubscription(clientInfo, m_request, m_responseChannel);
        verify(m_responseChannel).sendResponse(any(NetConfResponse.class), any(CreateSubscriptionRequest.class));
        assertTrue(m_notificationStream.isActiveSubscription(clientInfo));
    }

    @Test
    public void testCreateSubscriptionWithStartTimeMissing() throws ParseException, NetconfMessageBuilderException {
        DateTime stopTime = DateTime.now();
        m_request.setMessageId("1");
        m_request.setStream(NetconfResources.NETCONF);
        m_request.setStopTime(stopTime.toString());
        verifyTestCreateSubscription(m_request);
    }

    @Test
    public void testCreateSubscriptionWithReplayNotSupported() throws ParseException, NetconfMessageBuilderException {
        DateTime stopTime = DateTime.now();
        m_request.setMessageId("1");
        m_request.setStream(NetconfResources.NETCONF);
        m_request.setStopTime(stopTime.toString());
        m_request.setStartTime(stopTime.toString());
        when(m_notificationStream.isReplaySupport()).thenReturn(false);
        verifyTestCreateSubscription(m_request);
    }

    @Test
    public void testCreateSubscriptionWithRelayWithStopTimeAfterNow() throws ParseException, NetconfMessageBuilderException {
        DateTime startTime = DateTime.now();
        m_request.setMessageId("1");
        m_request.setStream(NetconfResources.NETCONF);
        m_request.setStartTime(startTime.toString());
        m_request.setStopTime(startTime.plusMinutes(1).toString());
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        m_notificationStream.createSubscription(clientInfo, m_request, m_responseChannel);
        verify(m_responseChannel).sendResponse(any(NetConfResponse.class), any(CreateSubscriptionRequest.class));
        assertTrue(m_notificationStream.isActiveSubscription(clientInfo));
    }

    @Test
    public void testCreateSubscriptionWithReplay() throws ParseException, NetconfMessageBuilderException {
        DateTime startTime = DateTime.now();
        m_request.setMessageId("1");
        m_request.setStream(NetconfResources.NETCONF);
        m_request.setStartTime(startTime.toString());
        m_request.setStopTime(startTime.toString());
        verifyTestCreateSubscription(m_request);
    }

    @Test
    public void testCreateSubscriptionWithStopTimeBeforeStartTime() throws ParseException, NetconfMessageBuilderException {
        DateTime stopTime = DateTime.now();
        m_request.setMessageId("1");
        m_request.setStream(NetconfResources.NETCONF);
        m_request.setStartTime(stopTime.plusMinutes(1).toString());
        m_request.setStopTime(stopTime.toString());
        verifyTestCreateSubscription(m_request);
    }

    @Test
    public void testCreateSubscriptionWithStartTimeAfterNow() throws ParseException, NetconfMessageBuilderException {
        DateTime startTime = DateTime.now();
        m_request.setMessageId("1");
        m_request.setStream(NetconfResources.NETCONF);
        m_request.setStartTime(startTime.plusMillis(200).toString());
        verifyTestCreateSubscription(m_request);
    }

    @Test
    public void testBroadCastNotification() throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.loadXmlDocument(NotificationFilterUtilTest.class.getResourceAsStream("/sample-state-change-notification.xml"));
        DocumentInfo documentInfo = new DocumentInfo(doc, FileUtil.loadAsString("/sample-state-change-notification.xml"));
        Notification notification = DocumentToPojoTransformer.getNotification(documentInfo);
        
        CreateSubscriptionRequest request = DocumentToPojoTransformer.getCreateSubscriptionRequest(DocumentUtils.loadXmlDocument(NotificationStreamImplTest.class.getResourceAsStream("/create-subscription-with-filter.xml")));

        m_notificationStream.createSubscription(m_clientInfo, request, m_responseChannel);
        verify(m_responseChannel).sendResponse(any(NetConfResponse.class), any(CreateSubscriptionRequest.class));
           
        m_notificationStream.broadcastNotification(notification);
        verify(m_responseChannel).sendNotification(any(Notification.class));
    }

	@Test
	public void testCloseSubscription(){
		CreateSubscriptionRequest request = new CreateSubscriptionRequest();
		request.setMessageId("1");
		request.setStream(NetconfResources.NETCONF);
		m_notificationStream.createSubscription(m_clientInfo, request, m_responseChannel);
		Map<NetconfClientInfo, NotificationSession> subscriptionSessionMap = m_notificationStream.getSubscriptionSessionMap();
		assertFalse(subscriptionSessionMap.isEmpty());
		assertTrue(m_notificationStream.isActiveSubscription(m_clientInfo));
		m_notificationStream.closeSubscription(m_clientInfo);
		subscriptionSessionMap = m_notificationStream.getSubscriptionSessionMap();
		assertTrue(subscriptionSessionMap.isEmpty());
		assertFalse(m_notificationStream.isActiveSubscription(m_clientInfo));
	}

	private void verifyTestCreateSubscription(CreateSubscriptionRequest request) throws ParseException, NetconfMessageBuilderException{
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        m_notificationStream.createSubscription(clientInfo, request, m_responseChannel);
        verify(m_responseChannel).sendResponse(any(NetConfResponse.class), any(CreateSubscriptionRequest.class));
        assertFalse(m_notificationStream.isActiveSubscription(clientInfo));
    }
}
