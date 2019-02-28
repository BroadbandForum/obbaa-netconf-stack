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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.joda.time.DateTime;

import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;

import org.broadband_forum.obbaa.netconf.server.model.notification.utils.NotificationFilterUtil;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * @author gnanavek
 *
 */
public class StreamLoggerInMemoryImpl implements StreamLogger {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
			.NETCONF_NOTIFICATION);
	private String m_streamName;
	
	private int defaultQueueSize = 100000;
	
	private int m_size;
	
	private DateTime m_replayLogCreationTime;

	private RangeMap<DateTime, List<Notification>> m_streamLogger = TreeRangeMap.<DateTime, List<Notification>>create();

	private Object lock_Object = new Object();
	
	private boolean m_replaySupport;
	
	private DateTime m_testForReplayLogAgedTime;


	public StreamLoggerInMemoryImpl(String streamName, boolean isReplaySupport) {
		m_streamName = streamName;
		m_size = 0;
		m_replayLogCreationTime = DateTime.now();
		m_replaySupport = isReplaySupport;
		m_testForReplayLogAgedTime = null;
	}

	public StreamLoggerInMemoryImpl(String streamName, boolean isReplaySupport, int queueSize) {
		m_streamName = streamName;
		m_size = 0;
		m_replayLogCreationTime = DateTime.now();
		m_replaySupport = isReplaySupport;
		m_testForReplayLogAgedTime = null;
        defaultQueueSize = queueSize;
	}
	public String getStreamName() {
		return m_streamName;
	}
	
	
	public int getSize() {
		return m_size;
	}
	

    @Override
	public void logNotification(DateTime eventTime, Notification notification) {
    	synchronized (lock_Object ) {
			if (m_size >= defaultQueueSize) {
				DateTime oldestLoggedTime = m_streamLogger.span().lowerEndpoint();
				m_streamLogger.remove(Range.singleton(oldestLoggedTime));
				m_size--;
			}
			if (m_streamLogger.asMapOfRanges().containsKey(Range.singleton(eventTime))) {
				m_streamLogger.get(eventTime).add(notification);
			} else {
				List<Notification> notificationList = new ArrayList<Notification>();
				notificationList.add(notification);
				m_streamLogger.put(Range.singleton(eventTime), notificationList);
			}
			m_size++;
			m_testForReplayLogAgedTime = null;
		}
    }

    @Override
    public List<Notification> retrieveNotifications(DateTime startTime, DateTime stopTime, FilterNode filterNode) {
        LOGGER.debug(null, "Retrieve notifications between startTime: {} and stopTime: {}", startTime, stopTime);
    	Map<Range<DateTime>, List<Notification>> subRangeLogger;
    	if (stopTime == null){
    		 subRangeLogger = m_streamLogger.subRangeMap(Range.atLeast(startTime)).asMapOfRanges();
    	} else {
    		subRangeLogger = m_streamLogger.subRangeMap(Range.closed(startTime, stopTime)).asMapOfRanges();
    	}
    	List<Notification> notificationList = new ArrayList<Notification>();
    	for(Entry<Range<DateTime>, List<Notification>> notificationEntry : subRangeLogger.entrySet()) {
			for (Notification notification : notificationEntry.getValue()) {
				if (NotificationFilterUtil.matches(notification, filterNode)) {
					notificationList.add(notification);
				}
			}
    	}
    	return notificationList;
    }

    @Override
    public DateTime getReplayLogCreationTime() {
    	return m_replayLogCreationTime;
    }

    @Override
    public DateTime getReplayLogAgedTime() {
        DateTime result=null;
        if (m_testForReplayLogAgedTime != null) {
            result = m_testForReplayLogAgedTime;
        } else {
            if (!m_streamLogger.asMapOfRanges().isEmpty()) {
                result = m_streamLogger.span().lowerEndpoint();
            }
        }
        return result;
    }

	public RangeMap<DateTime, List<Notification>> getNotificationsRangeMap() {
		return m_streamLogger;
	}

    @Override
    public void setReplaySupport(boolean isReplaySpport) {
        this.m_replaySupport = isReplaySpport;
    }

    @Override
    public boolean isReplaySupport() {
        return m_replaySupport;
    }

    @Override
    public void setReplayLogCreationTime(DateTime logCreationTime) {
        m_replayLogCreationTime = logCreationTime;
    }

    @Override
    public void setTestForReplayLogAgedTime(DateTime logAgedTime) {
        m_testForReplayLogAgedTime = logAgedTime;
    }
}
