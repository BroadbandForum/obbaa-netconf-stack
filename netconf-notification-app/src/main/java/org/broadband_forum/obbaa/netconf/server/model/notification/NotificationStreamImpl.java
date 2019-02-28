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


import static org.broadband_forum.obbaa.netconf.server.ResponseChannelUtil.sendResponse;

import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import org.broadband_forum.obbaa.netconf.server.model.notification.utils.NotificationFilterUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.joda.time.DateTime;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationStream;
import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.stack.NcNotificationCounterService;

/**
 * This class provides the implementation of <tt>NotificationStream<tt> interface
 *
 * Created by pregunat on 1/20/16.
 * @see NotificationStream
 */
public class NotificationStreamImpl implements NotificationStream {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
			.NETCONF_NOTIFICATION);
	
	private static final String NOTIFICATION_STOP_TASK_PREFIX = "NotificationStopTask";

	private Stream m_stream;
	
	private NotificationLogger m_notificationLogger;

	private final TimerManager m_timerMgr;
	
	private String m_notificationStopTaskName;

    private Map<NetconfClientInfo, NotificationSession> m_subscriptions = new ConcurrentHashMap<>();

	private static int m_streamIdCounter = 0;
	private int m_streamId;
	private final Executor m_notifExecutor;
	private NcNotificationCounterService m_nbiNotificationCounterInterceptor;
	
	public NotificationStreamImpl(Stream stream, NotificationLogger notificationLogger, TimerManager timerMgr, Executor notifExecutor, NcNotificationCounterService nbiNotificationCounterInterceptor) {
    	m_stream = stream;
		m_timerMgr = timerMgr;
		m_notifExecutor = notifExecutor;
		m_streamId = generateStreamId();
    	m_notificationLogger = notificationLogger;
    	m_nbiNotificationCounterInterceptor = nbiNotificationCounterInterceptor;
		//adding stream id to form unique task name per netconf stream
    	m_notificationStopTaskName = NOTIFICATION_STOP_TASK_PREFIX + m_stream.getName() + ":" + m_streamId;
    }

	private synchronized static int generateStreamId() {
		return ++m_streamIdCounter;
	}

    @Override
    public void createSubscription(NetconfClientInfo clientInfo, CreateSubscriptionRequest request, ResponseChannel responseChannel) {
    	NetConfResponse response = new NetConfResponse();
		response.setMessageId(request.getMessageId());
    	if(!validateSubscriptionRequest(request, response)) {
    		//invalid subscription request, send rpc-reply with rpc-error
    	    LOGGER.debug(null, "Invalid create subscription request: {}",request.requestToString());
    		sendResponse(responseChannel, request, response);
    		return;
    	}
    	//valid subscription request
        DateTime startTime = request.getStartTime();
        DateTime stopTime = request.getStopTime();
        LOGGER.debug(null, "Client {} creating subscription with startTime: {} stopTime: {}", clientInfo, startTime, stopTime);
        if(startTime == null && stopTime == null) {
        	//only subscription scenario
        	createSubscriptionWithoutReplay(clientInfo, request, responseChannel);
        } else if(startTime != null && isReplaySupport()) {
            //subscription and replay scenario
        	createSubscriptionWithReplay(clientInfo, request, startTime, stopTime, responseChannel);
    	}
		responseChannel.getCloseFuture().thenAccept(isSessionClosed -> m_subscriptions.remove(clientInfo));

        m_notificationLogger.logSubScription(request);
    }

    @Override
    public void broadcastNotification(Notification notification){
        for (NotificationSession notificationSession : m_subscriptions.values()) {
            NetconfFilter filter = notificationSession.getSubscriptionRequest().getFilter();
            Notification filteredNotification = NotificationFilterUtil.filterNotification(notification, filter);
            if (filteredNotification != null) {
                notificationSession.sendNotification(filteredNotification);
            }
        }
    }

	@Override
	public void stopNotification(NetconfClientInfo clientInfo) {
		stopNotification(clientInfo,true);
	}

	private void stopNotification(NetconfClientInfo clientInfo, boolean sendNotificationComplete){
		NotificationSession notificationSession = m_subscriptions.remove(clientInfo);
		if(notificationSession != null) {
			if(sendNotificationComplete){
				notificationSession.sendNotificationComplete();
			}
			notificationSession.close();
		}
	}

	@Override
	public void closeSubscription(NetconfClientInfo clientInfo) {
		stopNotification(clientInfo, false);
	}

	@Override
    public boolean isActiveSubscription(NetconfClientInfo clientInfo) {
        return m_subscriptions.containsKey(clientInfo);
    }

    public Map<NetconfClientInfo, NotificationSession> getSubscriptionSessionMap() {
        return m_subscriptions;
    }
    
    public boolean isReplaySupport() {
    	return m_stream.getReplaySupport();
    }
    
    private boolean validateSubscriptionRequest(CreateSubscriptionRequest request, NetConfResponse response) {
        DateTime startTime = request.getStartTime();
        DateTime stopTime = request.getStopTime();

		LOGGER.debug(null, "Current time is {}", DateTime.now());
		LOGGER.debug(null, "startTime is {} stopTime is {}", startTime, stopTime);

        if(startTime == null && stopTime != null) {
        	//start time is missing
        	response.setOk(false);
    		response.addError(NetconfRpcError.getMissingElementError(Collections.singletonList("startTime"), NetconfRpcErrorType.Protocol));
    		return false;
        } else if(stopTime != null && !isReplaySupport()){
    		//replay not supported
    		response.setOk(false);
    		response.addError(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, 
    				NetconfRpcErrorType.Protocol, 
    				NetconfRpcErrorSeverity.Error, 
    				"Replay feature is not supported by notification stream:" + m_stream.getName()));
    		return false;
    	} else if(startTime != null && stopTime != null && stopTime.isBefore(startTime)) {
    		response.setOk(false);
    		NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT, 
    				NetconfRpcErrorType.Protocol, 
    				NetconfRpcErrorSeverity.Error, 
    				"stopTime is requested that is earlier than the specified startTime");
    		rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, "stopTime");
    		response.addError(rpcError);
    		return false;
    	} else if(startTime != null && startTime.isAfterNow()) {
    		response.setOk(false);
    		NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT, 
    				NetconfRpcErrorType.Protocol, 
    				NetconfRpcErrorSeverity.Error, 
    				"startTime is requested that is later than the current time");
    		rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, "startTime");
    		response.addError(rpcError);
    		return false;
    	}
        return true;
    }
    


	/**
	 * @param clientInfo
	 * @param request
	 */
    

	protected NotificationSession createNotificationSession(NetconfClientInfo clientInfo,
			CreateSubscriptionRequest request, ResponseChannel responseChannel) {
		return (NotificationSession) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] { NotificationSession.class }, new NotificationSessionInvocationHandler(
						m_nbiNotificationCounterInterceptor, clientInfo, request, responseChannel, m_notifExecutor));
	}

	private NotificationSession createSubscriptionWithoutReplay(NetconfClientInfo clientInfo, CreateSubscriptionRequest request, 
			ResponseChannel responseChannel) {
		LOGGER.debug(null, "Client {} creating subscription without replay", clientInfo);
		NotificationSession notificationSession = createNotificationSession(clientInfo, request, responseChannel);
		LOGGER.debug(null, "Using the created notification session and putting it inside subscriptions_map");
		m_subscriptions.put(clientInfo, notificationSession);
		NetConfResponse response = new NetConfResponse();
		response.setMessageId(request.getMessageId());
		response.setOk(true);
		//send ok-reply for subscription request
		sendResponse(responseChannel, request, response);
		return notificationSession;
	}

	/**
	 * @param clientInfo
	 * @param request
	 * @param startTime
	 * @param stopTime
	 */
	private NotificationSession createSubscriptionWithReplay(NetconfClientInfo clientInfo, CreateSubscriptionRequest request, DateTime startTime, DateTime stopTime,
			ResponseChannel responseChannel) {
	    LOGGER.debug(null, "Client {} creating subscription between startTime: {} stopTime: {}", clientInfo, startTime, stopTime);
	    NotificationSession  notificationSession = createNotificationSession(clientInfo, request, responseChannel);
	    LOGGER.debug(null, "Using the created notification session and putting it inside subscriptions_map");
		m_subscriptions.put(clientInfo, notificationSession);
		NetConfResponse response = new NetConfResponse();
		response.setMessageId(request.getMessageId());
		response.setOk(true);
		//send ok-reply for subscription request
		sendResponse(responseChannel, request, response);
	    FilterNode filter = notificationSession.getSubscriptionFilter();
		List<Notification> notificationList = m_notificationLogger.retrieveNotifications(startTime, stopTime, m_stream.getName(), filter);
		notificationSession.replayNotification(notificationList);
		
		if(stopTime != null) {
			if(stopTime.isAfterNow()) {
				schuduleNotificationStopTask(clientInfo, stopTime);
			} else {
				stopNotification(clientInfo);
			}
		}
		return notificationSession;
	}

	/**
	 * @param clientInfo
	 * @param stopTime
	 */
	private void schuduleNotificationStopTask(NetconfClientInfo clientInfo, DateTime stopTime) {
		Map<String, Object> context = new HashMap<String, Object>();
		context.put(NotificationStopTask.NOTIFICATION_STREAM, this);
		context.put(NotificationStopTask.NETCONF_CLIENT_INFO, clientInfo);
		Calendar calendar = Calendar.getInstance();
		long currentTimeInMillis = calendar.getTimeInMillis();
		long stopTimeInMillis = stopTime.getMillis();
		long delay = stopTimeInMillis - currentTimeInMillis;
		m_timerMgr.schedule(m_notificationStopTaskName, new NotificationStopTask(context), delay, 0);
	}
}
