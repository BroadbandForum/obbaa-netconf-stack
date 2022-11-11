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

package org.broadband_forum.obbaa.netconf.api.server.notification;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

/**
 * NotificationService represents Central Notification Processor
 * 
 * <pre>
 * NotificationService is responsible for
 *  1. Validating the create-subscription request  such as start, stop time and active Subscription already exists
 *  2. Delegate valid subscription request to a NotificationStream
 *  3. Forwarding the notification to the specified NotificationStream and also to the default NETCONF Stream
 *  4. Logging Notifications to Notification Log Service
 * </pre>
 * 
 * Created by pregunat on 12/14/15.
 */
public interface NotificationService {
    
    public static final String SEND_NOTIFICATION_METHOD = "sendNotification";

    public boolean isActiveSubscription(NetconfClientInfo clientInfo);

    public void createSubscription(NetconfClientInfo clientInfo, CreateSubscriptionRequest createSubscriptionRequest,
            ResponseChannel responseChannel);

    public void sendNotification(String streamName, Notification notification);
    
    public NotificationStream getDefaultNotificationStream();

    public NotificationStream getNotificationStream(String streamName);

    public List<Stream> getSupportedStreams();

    /**
     * It is called when session is closed or notification is failed to send to a specific client
     * 
     * @param clientInfo
     */
    public void closeSubscription(NetconfClientInfo clientInfo);

    public void registerCallBack(List<NotificationCallBackInfo> callBackInfos);

    public void unregisterCallBack(Set<QName> notificationTypes);

    public void executeCallBack(Notification notification, NotificationContext context);
    
    public boolean isNotificationCallbackRegistered(QName qname, String moduleCapString);

    public NetConfResponse createSubscriptionWithCallback(NetconfClientSession clientSession, String timeOfLastSentEvent,
            NotificationListener subscriber, Object synchronizedId);

    public NetConfResponse createSubscriptionWithCallback(NetconfClientSession clientSession, String timeOfLastSentEvent,
            NotificationListener subscriber, Object synchronizedId, boolean isReplaySupported);

    public boolean isNotificationSupported(NetconfClientSession clientSession);

    public boolean isReplaySupported(NetconfClientSession clientSession) throws NetconfMessageBuilderException, InterruptedException,
            ExecutionException;

    public int getThresholdForPersistTimeLastSentEvent();

    public boolean isSynchronizingId(Object synchronizedId);

    public boolean isReplayPossible(NetconfClientSession clientSession, String startTime);

    public List<NotificationCallBackInfo> getNotificationCallBacks();

    public void unregisterCallBackInfo(NotificationCallBackInfo notificationCallBackInfo);

}
