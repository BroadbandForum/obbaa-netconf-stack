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

import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.ReplayComplete;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;


public final class NotificationReplayTask implements Runnable {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
			.NETCONF_NOTIFICATION);
	
	private List<Notification> m_notificationList;
	
	private ResponseChannel m_responseChannel;
	
	private PostReplayCallBack m_postReplayCallBack;
	
	private boolean m_isDone = false;

	public NotificationReplayTask(List<Notification> notificationList, ResponseChannel responseChannel, PostReplayCallBack  postReplayCallBack) {
		m_notificationList = notificationList;
		m_responseChannel = responseChannel;
		m_postReplayCallBack = postReplayCallBack;
	}
	
	public boolean isDone() {
		return m_isDone;
	}

	public void run() {
		for (Notification notification : m_notificationList) {
			m_responseChannel.sendNotification(notification);
		}
		try {
			ReplayComplete replayComplete = new ReplayComplete();
			LOGGER.debug(null, "Sending replay complete notification");
			m_responseChannel.sendNotification(replayComplete);
		} catch (NetconfMessageBuilderException e) {
			LOGGER.error(null,"could not build replayComplete notification", e);
		}
		m_postReplayCallBack.postReplayAction();
		m_isDone = true;
	}

	//for UT only
	public List<Notification> getNotificationList(){
		return m_notificationList;
	}

	//for UT only
	public ResponseChannel getResponseChannel() {
		return m_responseChannel;
	}

	//for UT only
	public PostReplayCallBack getPostReplayCallBack() {
		return m_postReplayCallBack;
	}
}