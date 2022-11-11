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

/**
 * 
 */
package org.broadband_forum.obbaa.netconf.server.model.notification;

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.NETCONF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.management.NotCompliantMBeanException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author gnanavek
 *
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class NotificationLoggerImplTest {

	private static final Logger LOGGER = Logger.getLogger(NotificationLoggerImplTest.class);

	@Mock
	private Stream m_stream;

	private NotificationLoggerImpl m_notificationLogger;

    private Properties m_properties;
	
	@Before
	public void init() throws NotCompliantMBeanException {
		when(m_stream.getName()).thenReturn(NETCONF);
		when(m_stream.getReplaySupport()).thenReturn(true);
		m_properties = new Properties();
		m_notificationLogger = new NotificationLoggerImpl(Collections.singletonList(m_stream));
	}

	@After
	public void destroy() {
		m_stream = null;
		m_notificationLogger = null;
	}

	@Test
    public void testGetStreamList() {
        assertEquals(Collections.singletonList(m_stream), m_notificationLogger.getStreamList());
    }

	@Test
	public void testIsLogSupportedByStream() {
		assertTrue(m_notificationLogger.isLogSupportedByStream(NETCONF));
	}
	
	@Test
	public void testGetReplayLogCreationTime() throws NotCompliantMBeanException {
		DateTime beforeTime = DateTime.now();
		init();
		DateTime afterTime = DateTime.now();
		DateTime actualCreationTime = m_notificationLogger.getReplayLogCreationTime(NETCONF);
		
		//Logger is created just after beforeTime, so beforeTime should be either before or equal to actualCreationTime.
		assertTrue(beforeTime.isBefore(actualCreationTime) || beforeTime.isEqual(actualCreationTime));
		
		//Logger is created just after afterTime, so afterTime should be either after or equals to actualCreationTime
		assertTrue(afterTime.isAfter(actualCreationTime) || afterTime.isEqual(actualCreationTime));
	}
	
	@Test
	public void testGetReplayLogAgedTime() throws NotCompliantMBeanException {
		
		//prepare test  notification sample 1
		Notification notification1 = new NetconfNotification();
		DateTime notification1DateTime = DateTime.now();

		//do log notification and test ReplayLogAgedTime
		m_notificationLogger.logNotification(notification1DateTime, notification1, NETCONF);
		DateTime actualReplayLogAgedTime1 = m_notificationLogger.getReplayLogAgedTime(NETCONF);		
		assertEquals(notification1DateTime, actualReplayLogAgedTime1);
		
		//prepare test  notification sample 2
		Notification notification2 = new NetconfNotification();
		DateTime notification2DateTime = DateTime.now();
		
		//do log notification and test ReplayLogAgedTime
		m_notificationLogger.logNotification(notification2DateTime, notification2, NETCONF);		
		DateTime actualReplayLogAgedTime2 = m_notificationLogger.getReplayLogAgedTime(NETCONF);		
		assertEquals(notification1DateTime, actualReplayLogAgedTime2);
	}
	
	@Test
	public void testRetrieveNotifications() throws NotCompliantMBeanException {
		//prepare test notification sample 1
		Notification notification1 = new NetconfNotification();
		DateTime notification1DateTime = DateTime.now();

		//do log notification and test ReplayLogAgedTime
		m_notificationLogger.logNotification(notification1DateTime, notification1, NETCONF);
		List<Notification> retrievedNotificationList1 = m_notificationLogger.retrieveNotifications(notification1DateTime, DateTime.now(), NETCONF, null);
		assertEquals(1, retrievedNotificationList1.size());
		assertEquals(notification1, retrievedNotificationList1.get(0));
		
		//prepare test notification sample 2
		Notification notification2 = new NetconfNotification();
		DateTime notification2DateTime = DateTime.now();
		
		//do log notification and test ReplayLogAgedTime
		m_notificationLogger.logNotification(notification2DateTime, notification2, NETCONF);
		List<Notification> retrievedNotificationList2 = m_notificationLogger.retrieveNotifications(notification1DateTime, DateTime.now(), NETCONF, null);
		assertEquals(2, retrievedNotificationList2.size());
		assertEquals(notification1, retrievedNotificationList2.get(0));
		assertEquals(notification2, retrievedNotificationList2.get(1));
		
		//prepare test notification sample 3
		Notification notification3 = new NetconfNotification();
		DateTime notification3DateTime = DateTime.now();
		
		//do log notification and test with stopTime = null
		m_notificationLogger.logNotification(notification3DateTime, notification3, NETCONF);
		List<Notification> retrievedNotificationList3 = m_notificationLogger.retrieveNotifications(notification1DateTime, null, NETCONF, null);
		assertEquals(3, retrievedNotificationList3.size());
		assertEquals(notification1, retrievedNotificationList3.get(0));
		assertEquals(notification2, retrievedNotificationList3.get(1));
		assertEquals(notification3, retrievedNotificationList3.get(2));

	}

	@Test
	public void testLogBufferCapacityWithDifferentTime() throws NotCompliantMBeanException {

		DateTime startTime = DateTime.now();
        LOGGER.info("start time: " + startTime);

		DateTime stopTime = generateNotifications(m_notificationLogger, startTime, true);

        LOGGER.info("stop time: " + stopTime);
        List<Notification> retrievedNotificationList = m_notificationLogger.retrieveNotifications(startTime, stopTime, NETCONF, null);

		stopTime = generateNotifications(m_notificationLogger,  stopTime, true);
        LOGGER.info("stop time2: " + stopTime);
        retrievedNotificationList = m_notificationLogger.retrieveNotifications(startTime, stopTime, NETCONF, null);
	}

	@Test
	public void testLogBufferCapacityWithSameTime() throws NotCompliantMBeanException {

		DateTime startTime = DateTime.now();
		LOGGER.info("start time: " + startTime);

		DateTime generationTime = new DateTime(startTime.getMillis() + 1);

		DateTime stopTime = generateNotifications(m_notificationLogger,  generationTime, false);

		LOGGER.info("stop time: " + stopTime);

		List<Notification> retrievedNotificationList = m_notificationLogger.retrieveNotifications(startTime, stopTime, NETCONF, null);

		StreamLoggerInMemoryImpl streamLogger = (StreamLoggerInMemoryImpl) m_notificationLogger.getStreamLogger(NETCONF);

		assertEquals(1, streamLogger.getNotificationsRangeMap().asMapOfRanges().size());
	}

	private DateTime generateNotifications(NotificationLogger logger, DateTime dateTime, boolean differentTime){
		DateTime generationTime = dateTime;
			Notification notification = new NetconfNotification();
			if (differentTime) {
				generationTime = new DateTime(generationTime.getMillis() + 1);
			}
            LOGGER.info("notification"+ 1 +" generated time :" + generationTime);
            logger.logNotification(generationTime, notification, NETCONF);
		return new DateTime(generationTime.getMillis() + 1);

	}
}
