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

package org.broadband_forum.obbaa.netconf.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.AccessDeniedException;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.SessionNotFoundException;

public class RequestTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(RequestTask.class);
    public static final String CURRENT_REQ = "CURRENT_REQ";
    public static final String CURRENT_REQ_TYPE = "CURRENT_REQ_TYPE";
    public static final String CURRENT_REQ_MESSAGE_ID = "CURRENT_REQ_MESSAGE_ID";
    public static final String CURRENT_REQ_START_TIME = "CURRENT_REQ_START_TIME";
    public static final String REQ_TYPE_GET = "GET";
    public static final String REQ_TYPE_GET_CONFIG = "GET-CONFIG";
    public static final String CURRENT_REQ_CONTEXT = "CURRENT_REQUEST_CONTEXT";
    private NetconfClientInfo m_clientInfo;
    private AbstractNetconfRequest m_netconfRequest;
    private ResponseChannel m_channel;
    private NetconfServerMessageListener m_serverMessageListener;
    private List<RequestTaskListener> m_listeners = new ArrayList<>();
    private List<Notification> m_netconfConfigChangeNotifications = new ArrayList<>();
    private NotificationService m_notificationService;
    private long m_queuedTime;
    private long m_offeredTime;
    private long m_executionStartTime;
    private long m_executionEndTime;
    private final NetconfLogger m_netconfLogger;
    private RequestContext m_requestContext;

    public RequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest netconfRequest, ResponseChannel channel,
                       NetconfServerMessageListener serverMessageListener, NetconfLogger netconfLogger) {
        m_clientInfo = clientInfo;
        m_netconfRequest = netconfRequest;
        m_channel = channel;
        m_serverMessageListener = serverMessageListener;
        m_netconfLogger = netconfLogger;
    }

    public RequestContext getRequestContext() {
        return m_requestContext;
    }

    public void setRequestContext(RequestContext requestContext) {
        this.m_requestContext = requestContext;
    }

    public NotificationService getNotificationService() {
        return m_notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.m_notificationService = notificationService;
    }

    public List<Notification> getNetconfConfigChangeNotifications() {
        return m_netconfConfigChangeNotifications;
    }

    public boolean isWrite() {
        if (m_netconfRequest instanceof GetRequest || m_netconfRequest instanceof GetConfigRequest) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isGetOrGetConfig() {
        return m_netconfRequest instanceof GetRequest || m_netconfRequest instanceof GetConfigRequest;
    }

    public boolean isEditConfig() {
        return m_netconfRequest instanceof EditConfigRequest;
    }

    public boolean isKillSessionRequest() {
        return m_netconfRequest instanceof KillSessionRequest;
    }

    public boolean isCloseSessionRequest() {
        return m_netconfRequest instanceof CloseSessionRequest;
    }

    public boolean isCreateSubScriptionRequest() {
        return m_netconfRequest instanceof CreateSubscriptionRequest;
    }

    public boolean isLockOrUnlockRequest() {
        return m_netconfRequest instanceof LockRequest || m_netconfRequest instanceof UnLockRequest;
    }

    public boolean isRpcRequest() {
        return m_netconfRequest instanceof NetconfRpcRequest;
    }

    public boolean isActionRequest() {
        return m_netconfRequest instanceof ActionRequest;
    }

    private void delayRequest() throws InterruptedException {
        int withDelay = 0;
        if (m_netconfRequest instanceof AbstractNetconfRequest) {
            withDelay = ((AbstractNetconfRequest) m_netconfRequest).getWithDelay();
        }
        if (withDelay > 0) {
            try {
                Thread.sleep(withDelay * 1000);
            } catch (InterruptedException e) {
                LOGGER.error("Error while sleeping request with with-delay" + withDelay, e);
            }
        }
    }

    @Override
    public void run() {
        try {
            setExecutionStartTime(System.currentTimeMillis());
            delayRequest();
            RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
                @Override
                public Void execute() {
                    RequestScope.getCurrentScope().putInCache(CURRENT_REQ, m_netconfRequest);
                    RequestScope.getCurrentScope().putInCache(CURRENT_REQ_START_TIME, System.currentTimeMillis());
                    RequestScope.getCurrentScope().putInCache(CURRENT_REQ_MESSAGE_ID, m_netconfRequest.getMessageId());
                    RequestScope.getCurrentScope().putInCache(CURRENT_REQ_CONTEXT, getRequestContext());
                    doExecuteRequest();
                    return null;
                }
            });

        } catch (AccessDeniedException e) {
            LOGGER.error("Error when execute for request: " + m_netconfRequest.requestToString(), e);
            NetConfResponse response = new NetConfResponse();
            response.setMessageId(m_netconfRequest.getMessageId());
            response.addError(e.getRpcError());
            sendResponseToChannel(response);
        } catch (SessionNotFoundException e) {
            LOGGER.error("Error when execute for request: " + m_netconfRequest.requestToString(), e);
            NetConfResponse response = new NetConfResponse();
            response.setMessageId(m_netconfRequest.getMessageId());
            response.addError(e.getRpcError());
            response.setCloseSession(true);
            sendResponseToChannel(response);
        } catch (Exception e) {
            LOGGER.error("Error when execute for request: " + m_netconfRequest.requestToString(), e);
            NetConfResponse response = new NetConfResponse();
            response.setMessageId(m_netconfRequest.getMessageId());
            NetconfRpcError error = NetconfRpcError.getApplicationError(e.getMessage());
            response.addError(error);
            sendResponseToChannel(response);
        } finally {
            setExecutionEndTime(System.currentTimeMillis());
            fireRequestDoneEvent();
            RequestScope.resetScope();
        }
    }

    protected void doExecuteRequest() {
        try {
            m_netconfLogger.logRequest(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(),
                    m_clientInfo.getUsername(), String.valueOf(m_clientInfo.getSessionId()),
                    m_netconfRequest.getRequestDocument());
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException("Cannot extract request document", e);
        }
        if (m_netconfRequest instanceof CreateSubscriptionRequest) {
            // delegate to NetconfServerMessageListener to create subscription request,
            // It also takes care of sending response for request
            m_serverMessageListener.onCreateSubscription(m_clientInfo, (CreateSubscriptionRequest) m_netconfRequest, m_channel);
        } else {
            // call to NetconfServerMessageListener to process request and retrieve response
            NetConfResponse response = new NetConfResponse();
            response.setMessageId(m_netconfRequest.getMessageId());
            if (m_netconfRequest instanceof CloseSessionRequest) {
                m_serverMessageListener.onCloseSession(m_clientInfo, (CloseSessionRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof CopyConfigRequest) {
                m_serverMessageListener.onCopyConfig(m_clientInfo, (CopyConfigRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof DeleteConfigRequest) {
                m_serverMessageListener.onDeleteConfig(m_clientInfo, (DeleteConfigRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof EditConfigRequest) {
                m_netconfConfigChangeNotifications = (List<Notification>) m_serverMessageListener.onEditConfig(m_clientInfo,
                        (EditConfigRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof GetRequest) {
                RequestScope.getCurrentScope().putInCache(CURRENT_REQ_TYPE, REQ_TYPE_GET);
                m_serverMessageListener.onGet(m_clientInfo, (GetRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof GetConfigRequest) {
                RequestScope.getCurrentScope().putInCache(CURRENT_REQ_TYPE, REQ_TYPE_GET_CONFIG);
                m_serverMessageListener.onGetConfig(m_clientInfo, (GetConfigRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof KillSessionRequest) {
                m_serverMessageListener.onKillSession(m_clientInfo, (KillSessionRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof LockRequest) {
                m_serverMessageListener.onLock(m_clientInfo, (LockRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof UnLockRequest) {
                m_serverMessageListener.onUnlock(m_clientInfo, (UnLockRequest) m_netconfRequest, response);
            } else if (m_netconfRequest instanceof ActionRequest) {
                response = new ActionResponse();
                response.setMessageId(m_netconfRequest.getMessageId());
                m_serverMessageListener.onAction(m_clientInfo, (ActionRequest) m_netconfRequest, (ActionResponse) response);
            } else if (m_netconfRequest instanceof NetconfRpcRequest) {
                response = new NetconfRpcResponse();
                response.setMessageId(m_netconfRequest.getMessageId());
                m_netconfConfigChangeNotifications = m_serverMessageListener.onRpc(m_clientInfo, (NetconfRpcRequest) m_netconfRequest,
                        (NetconfRpcResponse) response);
            }
            sendResponseToChannel(response);
            if ((m_netconfConfigChangeNotifications != null) && (!m_netconfConfigChangeNotifications.isEmpty()) && (m_notificationService != null)) {
                LOGGER.debug("sending edit-config change notification on NC stream " + NetconfResources.CONFIG_CHANGE_STREAM);
                for (Notification notification : m_netconfConfigChangeNotifications) {
                    m_notificationService.sendNotification(NetconfResources.CONFIG_CHANGE_STREAM, notification);
                }
            }
        }
    }

    public void sendResponse(NetConfResponse response) {
        try {
            response.setMessageId(m_netconfRequest.getMessageId());
            sendResponseToChannel(response);
        } finally {
            fireRequestDoneEvent();
        }
    }

    protected void sendResponseToChannel(NetConfResponse response) {
        if (!m_channel.isSessionClosed()) {
            try {
                m_netconfLogger.logResponse(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(), m_clientInfo.getUsername(),
                        new Integer(m_clientInfo.getSessionId()).toString(), response.getResponseDocument(), m_netconfRequest);
                m_channel.sendResponse(response, m_netconfRequest);
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error("Can not send response for request: " + m_netconfRequest.requestToString(), e);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Could not log response \n %s \n as the response channel was already closed", response.responseToString()));
            }

        }
    }

    public NetconfClientInfo getClientInfo() {
        return m_clientInfo;
    }

    public AbstractNetconfRequest getRequest() {
        return m_netconfRequest;
    }

    public boolean isSessionClosed() {
        return m_channel.isSessionClosed();
    }

    public void addListener(RequestTaskListener listener) {
        m_listeners.add(listener);
    }

    private void fireRequestDoneEvent() {
        for (RequestTaskListener listener : m_listeners) {
            listener.requestDone();
        }
    }

    public static interface RequestTaskListener {
        void requestDone();
    }

    public void setQueuedTime(long queuedTime) {
        m_queuedTime = queuedTime;
    }

    public long getQueuedTime() {
        return m_queuedTime;
    }

    public void setExecutionStartTime(long time) {
        this.m_executionStartTime = time;
    }

    public long getExecutionStartTime() {
        return m_executionStartTime;
    }

    public void setExecutionEndTime(long time) {
        this.m_executionEndTime = time;
    }

    public long getExecutionEndTime() {
        return m_executionEndTime;
    }

    public long getOfferedTime() {
        return m_offeredTime;
    }

    public void setOfferedTime(long offeredTime) {
        this.m_offeredTime = offeredTime;
    }
}
