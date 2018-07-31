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

import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;

/**
 * NotificationStream represents set of event notifications matching some forwarding criteria. Name of Stream is
 * available to Netconf client
 * for subscription.
 * <p>
 * <pre>
 * NotificationStream is responsible for
 * 1. Creates NotificationSession for each subscription request.
 * 2. when subscription stopTime expires, send notificationComplete and dispose NotificationSession
 * 3. Retrieving logged notification and initiate replay on NotificationSession
 * 4. Broadcasting the notification to all NotificationSession that are matching the subscription filter.
 * </pre>
 * <p>
 * Created by pregunat on 1/22/16.
 */
public interface NotificationStream {

    boolean isActiveSubscription(NetconfClientInfo clientInfo);

    void createSubscription(NetconfClientInfo clientInfo, CreateSubscriptionRequest request, ResponseChannel
            responseChannel);

    void broadcastNotification(Notification notification);

    void stopNotification(NetconfClientInfo clientInfo);

    void closeSubscription(NetconfClientInfo clientInfo);
}
