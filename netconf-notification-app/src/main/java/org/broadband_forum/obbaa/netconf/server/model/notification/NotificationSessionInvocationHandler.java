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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;

import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import org.broadband_forum.obbaa.netconf.stack.NcNotificationCounterService;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class NotificationSessionInvocationHandler implements InvocationHandler {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
            .NETCONF_NOTIFICATION);
    private NotificationSession m_notificationSession;
    private NcNotificationCounterService m_nbiNotificationsCounterInterceptor;
    private Integer m_sessionId;

    public NotificationSessionInvocationHandler(NcNotificationCounterService ncNotificationCounterService,
            NetconfClientInfo clientInfo, CreateSubscriptionRequest request, ResponseChannel responseChannel, Executor notifExecutor) {
        m_nbiNotificationsCounterInterceptor = ncNotificationCounterService;
        m_sessionId = clientInfo.getSessionId();
        m_notificationSession = new NotificationSessionImpl(clientInfo, request, responseChannel, notifExecutor);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        LOGGER.info(null, "Invoking method: {} with args: {}", methodName, args);
        if (methodName.equals(NotificationSession.SEND_NOTIFICATION_METHOD)) {
            LOGGER.info(null, "Notification counter should be increased by 1");
            m_nbiNotificationsCounterInterceptor.increaseNumberOfNotifications();
            m_nbiNotificationsCounterInterceptor.increaseOutNotifications(m_sessionId);
        }
        return method.invoke(m_notificationSession, args);
    }
}
