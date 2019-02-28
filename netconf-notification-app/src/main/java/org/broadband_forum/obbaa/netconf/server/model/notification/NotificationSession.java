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

import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;

/**
 * NotificationSession represents session managing notifications for create-subscription request
 * 
 * <pre>
 * The NotificationSession is responsible for 
 *	1. Queuing of notifications to be send to NotificationDispatcher
 * 	2. Send replayComplete after replay of notification
 * </pre>
 * Created by pregunat on 1/20/16.
 */
public interface NotificationSession {
	
    public static final String SEND_NOTIFICATION_METHOD = "sendNotification";
 
    public FilterNode getSubscriptionFilter();

	public CreateSubscriptionRequest getSubscriptionRequest();

	public void sendNotification(Notification notification);
    
	public void replayNotification(List<Notification> replayNotificationList);
	
	public void sendNotificationComplete();
	
	public void close();
    
}
