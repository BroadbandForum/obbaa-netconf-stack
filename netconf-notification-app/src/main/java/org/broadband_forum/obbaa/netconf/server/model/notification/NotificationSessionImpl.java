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

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.NotificationComplete;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;

/**
 * This class provides the implementation of <tt>NotificationSession<tt> interface
 * 
 * Created by pregunat on 1/20/16.
 * @see NotificationSession
 */
public class NotificationSessionImpl implements NotificationSession {

    protected static final int REPLAY_DELAY = 2;

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
			.NETCONF_NOTIFICATION);

    private NetconfClientInfo m_clientInfo;

    private CreateSubscriptionRequest m_createSubscriptionRequest;

    private ResponseChannel m_responseChannel;

    private Queue<Notification> m_notificationQueue = new ConcurrentLinkedQueue<Notification>();

    private Executor m_dispatchTaskExecutor;

    private NotificationReplayTask m_notificationReplayTask;

    private FilterNode m_subscriptionFilter;

    public NotificationSessionImpl(NetconfClientInfo clientInfo, CreateSubscriptionRequest request, ResponseChannel responseChannel, Executor notifExecutor) {
		m_dispatchTaskExecutor = notifExecutor;
        m_clientInfo = clientInfo;
        m_createSubscriptionRequest = request;
        m_responseChannel = responseChannel;
        init();
    }

	private void init() {
		NetconfFilter netconfFilter = m_createSubscriptionRequest.getFilter();
		m_subscriptionFilter = new FilterNode();
		if (netconfFilter != null && netconfFilter.getXmlFilterElements() != null) {
			List<Element> filterXml = netconfFilter.getXmlFilterElements();
			FilterUtil.processFilter(m_subscriptionFilter, filterXml);
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace(null,"notification filter: \n {}" , m_subscriptionFilter);
			}
		} else {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace(null,"no notification filter");
			}
			m_subscriptionFilter = null;
		}
	}

	public NetconfClientInfo getClientInfo() {
        return m_clientInfo;
    }

    public CreateSubscriptionRequest getSubscriptionRequest() {
        return m_createSubscriptionRequest;
    }

	@Override
	public FilterNode getSubscriptionFilter() {
		return m_subscriptionFilter;
	}

    @Override
    public void sendNotification(Notification notification) {
    	if(m_notificationReplayTask == null || m_notificationReplayTask.isDone()) {
    		m_responseChannel.sendNotification(notification);
    	} else {
    		m_notificationQueue.add(notification);
    	}
    }

	@Override
	public void replayNotification(final List<Notification> notificationList) {
		m_notificationReplayTask = new NotificationReplayTask(notificationList, m_responseChannel, new PostReplayCallBackImpl());
		m_dispatchTaskExecutor.execute(m_notificationReplayTask);
	}

	@Override
	public void sendNotificationComplete() {
		try {
			NotificationComplete notificationComplete = new NotificationComplete();

			if(m_notificationReplayTask == null || m_notificationReplayTask.isDone()) {
			    LOGGER.debug(null, "Sending notification complete");
	    		m_responseChannel.sendNotification(notificationComplete);
	    	} else {
	    		m_notificationQueue.add(notificationComplete);
	    	}
		} catch (NetconfMessageBuilderException e) {
		    LOGGER.error(null, "Exception while sendNotificationComplete ", e);
		}

	}

	@Override
	public void close() {
	}

	private class PostReplayCallBackImpl implements PostReplayCallBack {

		@Override
		public void postReplayAction() {
			Notification notification = null;
			while( (notification = m_notificationQueue.poll()) != null) {
				m_responseChannel.sendNotification(notification);
			}
		}

	}

	// for UT only
	protected Queue<Notification> getNotificationQueue() {
		return m_notificationQueue;
	}
}
