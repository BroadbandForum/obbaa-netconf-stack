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

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.NotCompliantMBeanException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LogUtil;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.joda.time.DateTime;

/**
 * @author gnanavek
 */
public class NotificationLoggerImpl implements NotificationLogger {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
            .NETCONF_NOTIFICATION);

    private Map<String, StreamLogger> m_streamLoggerMap = new ConcurrentHashMap<String, StreamLogger>();

    private List<Stream> m_streamList;
    private Map<String, Logger> m_debugLogger;

    public NotificationLoggerImpl(List<Stream> streamList) throws NotCompliantMBeanException {
        m_debugLogger = new HashMap<String, Logger>();
        m_streamList = streamList;
        for (Stream stream : streamList) {
            if (stream.getReplaySupport()) {
                try {
                    LOGGER.debug(null, "Set replayLogCreationTime for stream: {} ", stream.getName());
                    stream.setReplayLogCreationTime(System.currentTimeMillis());
                } catch (ParseException e) {
                    LOGGER.error(null, "Error in setting replayLogCreationTime for stream: " + stream.getName());
                }
            }
            if (SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("DISABLE_STREAM_LOGGER", null) == null) {
                m_streamLoggerMap.put(stream.getName(), new StreamLoggerInMemoryImpl(stream.getName(), stream.getReplaySupport()));
            }
            m_debugLogger.put(stream.getName(), Logger.getLogger(stream.getName() + NetconfResources.SUFFIX));
        }
    }

    public StreamLogger getStreamLogger(String streamName) {
        return m_streamLoggerMap.get(streamName);
    }

    public Logger getLogger(String streamName) {
        Logger logger = m_debugLogger.get(streamName);
        return logger;
    }

    @Override
    public List<Stream> getStreamList() {
        return m_streamList;
    }

    @Override
    public boolean isLogSupportedByStream(String streamName) {
        return m_streamLoggerMap.containsKey(streamName);
    }

    @Override
    public void logNotification(DateTime eventTime, Notification notification, String streamName) {
        getStreamLogger(streamName).logNotification(eventTime, notification);
        Logger logger = getLogger(streamName);

        if (logger.isDebugEnabled()) {
            LogUtil.logDebug((org.apache.logging.log4j.Logger) logger, NetconfResources.NOTIFICATION_LOG_STMT, streamName, notification.notificationToString());
        }
    }

    @Override
    public List<Notification> retrieveNotifications(DateTime startTime, DateTime stopTime, String streamName, FilterNode filterNode) {
        if (getLogger(streamName).isDebugEnabled()) {
            getLogger(streamName).debug("Retrieving notifications for stream " + streamName + " between " + startTime + " and " + stopTime);
        }
        return getStreamLogger(streamName).retrieveNotifications(startTime, stopTime, filterNode);
    }

    @Override
    public DateTime getReplayLogCreationTime(String streamName) {
        return getStreamLogger(streamName).getReplayLogCreationTime();
    }

    @Override
    public DateTime getReplayLogAgedTime(String streamName) {
        return getStreamLogger(streamName).getReplayLogAgedTime();
    }

    @Override
    public void logSubScription(CreateSubscriptionRequest request) {
        Logger logger = getLogger(request.getStream());
        if (logger.isDebugEnabled()) {
            LogUtil.logDebug((org.apache.logging.log4j.Logger) logger, NetconfResources.CREATESUBSCRIPTION_LOG_STMT, request.requestToString());
        }
    }

}
