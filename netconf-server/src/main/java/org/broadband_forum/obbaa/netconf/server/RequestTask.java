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
import java.util.concurrent.TimeUnit;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.logger.ual.NCUserActivityLogHandler;
import org.broadband_forum.obbaa.netconf.api.logger.ual.NetconfUALLog;
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
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.ual.UALLogger;
import org.w3c.dom.Document;

import com.google.common.base.Stopwatch;

public class RequestTask implements Runnable {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(RequestTask.class, LogAppNames.NETCONF_LIB);
    public static final String CURRENT_REQ = "CURRENT_REQ";
    public static final String CURRENT_REQ_FORCE_INSTANCE_CREATION = "CURRENT_REQ_FORCE_INSTANCE_CREATION";
    public static final String CURRENT_REQ_TYPE = "CURRENT_REQ_TYPE";
    public static final String CURRENT_REQ_MESSAGE_ID = "CURRENT_REQ_MESSAGE_ID";
    public static final String CURRENT_REQ_START_TIME = "CURRENT_REQ_START_TIME";
    public static final String CURRENT_REQ_SHOULD_TRIGGER_SYNC_UPON_SUCCESS = "CURRENT_REQ_SHOULD_TRIGGER_SYNC_UPON_SUCCESS";
    public static final String REQ_TYPE_GET = "GET";
    public static final String REQ_TYPE_GET_CONFIG = "GET-CONFIG";
    public static final String REQ_TYPE_EDIT_CONFIG = "EDIT-CONFIG";
    public static final String CURRENT_REQ_CONTEXT = "CURRENT_REQUEST_CONTEXT";
    public static final String CURRENT_REQ_DOCUMENT = "CURRENT_REQ_DOCUMENT";
    private final Stopwatch m_sw;
    private NetconfClientInfo m_clientInfo;
    private AbstractNetconfRequest m_netconfRequest;
    private ResponseChannel m_channel;
    private NetconfServerMessageListener m_serverMessageListener;
    private List<RequestTaskListener> m_listeners = new ArrayList<>();
    private List<Notification> m_netconfConfigChangeNotifications;
    private NotificationService m_notificationService;
    private Document m_netconfRequestDocument;
    private long m_queuedTime;
    private long m_offeredTime;
    private long m_executionStartTime;
    private long m_executionEndTime;
    private boolean m_canUalLog;
    private final NetconfLogger m_netconfLogger;
    private final UALLogger m_ualLogger;
    private NCUserActivityLogHandler m_ncUserActivityLogHandler;
    private RequestContext m_requestContext;
    private RequestTaskPostRequestExecuteListener m_requestTaskPostRequestExecuteListener;
    private Stopwatch m_enQueueTimer;
    private Long m_queueWaitingTime;

    public RequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest netconfRequest, ResponseChannel channel,
                       NetconfServerMessageListener serverMessageListener, NetconfLogger netconfLogger,
                       NCUserActivityLogHandler ncUserActivityLogHandler, UALLogger ualLogger) {
        m_clientInfo = clientInfo;
        m_netconfRequest = netconfRequest;
        m_sw = Stopwatch.createUnstarted();
        m_enQueueTimer = Stopwatch.createUnstarted();
        m_channel = channel;
        m_serverMessageListener = serverMessageListener;
        m_netconfLogger = netconfLogger;
        m_ncUserActivityLogHandler = ncUserActivityLogHandler;
        m_ualLogger = ualLogger;
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

    public boolean isEditConfigOrRpcOrAction(){
        return isEditConfig() || isActionRequest() || isRpcRequest() ;
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

                    //keep old copies
                    UserContext loggedInUserCtxtTL = RequestContext.getLoggedInUserCtxtTL();
                    UserContext additionalUserCtxtTL = RequestContext.getAdditionalUserCtxtTL();
                    AggregateContext aggregateContextTL = RequestContext.getAggregateContextTL();
                    RequestContext requestContext = getRequestContext();
                    RequestScope.getCurrentScope().putInCache(CURRENT_REQ_CONTEXT, requestContext);
                    try {
                        if(requestContext != null) {
                            //set new TLS
                            RequestContext.setLoggedInUserCtxtTL(requestContext.getLoggedInUserCtxt());
                            RequestContext.setAdditionalUserCtxtTL(requestContext.getAdditionalUserCtxt());
                            
                            if(requestContext.getAggregateContext() != null) {
                            	RequestContext.setAggregateContextTL(requestContext.getAggregateContext());
                            }
                        }
                        if (m_netconfRequest instanceof EditConfigRequest) {
                            RequestScope.getCurrentScope().putInCache(CURRENT_REQ_SHOULD_TRIGGER_SYNC_UPON_SUCCESS,
                                    ((EditConfigRequest) m_netconfRequest).isTriggerSyncUponSuccess());
                            RequestScope.getCurrentScope().putInCache(CURRENT_REQ_FORCE_INSTANCE_CREATION,
                                    m_netconfRequest.isForceInstanceCreation());
                        }
                        doExecuteRequest();
                    } finally {
                        //restore old copies
                        RequestContext.setLoggedInUserCtxtTL(loggedInUserCtxtTL);
                        RequestContext.setAdditionalUserCtxtTL(additionalUserCtxtTL);
                        RequestContext.setAggregateContextTL(aggregateContextTL);
                    }

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
        }
    }

    protected void doExecuteRequest() {
        try {
			try {
                m_netconfRequestDocument = m_netconfRequest.getRequestDocument();
                m_netconfLogger.setThreadLocalDeviceLogId(m_netconfRequestDocument);
                m_ualLogger.setCanLog(m_canUalLog);
				m_netconfLogger.logRequest(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(),
						m_clientInfo.getUsername(), String.valueOf(m_clientInfo.getSessionId()),
                        m_netconfRequestDocument);
			} catch (NetconfMessageBuilderException e) {
				throw new RuntimeException("Cannot extract request document", e);
			}
            m_queueWaitingTime = m_enQueueTimer.isRunning()? m_enQueueTimer.elapsed(TimeUnit.MILLISECONDS): null;
            m_sw.start();
			if (m_netconfRequest instanceof CreateSubscriptionRequest) {
				// delegate to NetconfServerMessageListener to create subscription request,
				// It also takes care of sending response for request
				m_serverMessageListener.onCreateSubscription(m_clientInfo, (CreateSubscriptionRequest) m_netconfRequest,
						m_channel);
			} else {
				// call to NetconfServerMessageListener to process request and retrieve response
				NetConfResponse response = new NetConfResponse();
				response.setMessageId(m_netconfRequest.getMessageId());
				if (m_netconfRequest instanceof CloseSessionRequest) {
					m_serverMessageListener.onCloseSession(m_clientInfo, (CloseSessionRequest) m_netconfRequest,
							response);
				} else if (m_netconfRequest instanceof CopyConfigRequest) {
					m_serverMessageListener.onCopyConfig(m_clientInfo, (CopyConfigRequest) m_netconfRequest, response);
				} else if (m_netconfRequest instanceof DeleteConfigRequest) {
					m_serverMessageListener.onDeleteConfig(m_clientInfo, (DeleteConfigRequest) m_netconfRequest,
							response);
				} else if (m_netconfRequest instanceof EditConfigRequest) {
					m_netconfConfigChangeNotifications = (List<Notification>) m_serverMessageListener
							.onEditConfig(m_clientInfo, (EditConfigRequest) m_netconfRequest, response);
				} else if (m_netconfRequest instanceof GetRequest) {
					RequestScope.getCurrentScope().putInCache(CURRENT_REQ_TYPE, REQ_TYPE_GET);
					m_serverMessageListener.onGet(m_clientInfo, (GetRequest) m_netconfRequest, response);
				} else if (m_netconfRequest instanceof GetConfigRequest) {
					RequestScope.getCurrentScope().putInCache(CURRENT_REQ_TYPE, REQ_TYPE_GET_CONFIG);
					m_serverMessageListener.onGetConfig(m_clientInfo, (GetConfigRequest) m_netconfRequest, response);
				} else if (m_netconfRequest instanceof KillSessionRequest) {
					m_serverMessageListener.onKillSession(m_clientInfo, (KillSessionRequest) m_netconfRequest,
							response);
				} else if (m_netconfRequest instanceof LockRequest) {
					m_serverMessageListener.onLock(m_clientInfo, (LockRequest) m_netconfRequest, response);
				} else if (m_netconfRequest instanceof UnLockRequest) {
					m_serverMessageListener.onUnlock(m_clientInfo, (UnLockRequest) m_netconfRequest, response);
				} else if (m_netconfRequest instanceof ActionRequest) {
					response = new ActionResponse();
					response.setMessageId(m_netconfRequest.getMessageId());
					m_serverMessageListener.onAction(m_clientInfo, (ActionRequest) m_netconfRequest,
							(ActionResponse) response);
				} else if (m_netconfRequest instanceof NetconfRpcRequest) {
					response = new NetconfRpcResponse();
					response.setMessageId(m_netconfRequest.getMessageId());
					m_netconfConfigChangeNotifications = m_serverMessageListener.onRpc(m_clientInfo,
							(NetconfRpcRequest) m_netconfRequest, (NetconfRpcResponse) response);
				}
				sendResponseToChannel(response);
				if ((m_netconfConfigChangeNotifications != null) && (!m_netconfConfigChangeNotifications.isEmpty())
						&& (m_notificationService != null)) {
					LOGGER.debug("sending edit-config change notification on NC stream "
							+ NetconfResources.CONFIG_CHANGE_STREAM);
					for (Notification notification : m_netconfConfigChangeNotifications) {
						try {
							m_notificationService.sendNotification(NetconfResources.CONFIG_CHANGE_STREAM, notification);
						} catch (Exception e) {
							LOGGER.error("Unable to send notification: {}",
									LOGGER.sensitiveData(notification.notificationToPrettyString()), e);
						}

					}
				}
			} 
		} finally {
			m_netconfLogger.setThreadLocalDeviceLogId(null);
			m_ualLogger.resetCanLog();
		}
    }

    public void sendResponse(NetConfResponse response) {
        try {
            response.setMessageId(m_netconfRequest.getMessageId());
            sendResponseToChannel(response);
        }finally {
            fireRequestDoneEvent();
        }
    }

    protected void sendResponseToChannel(NetConfResponse response) {
        if (!m_channel.isSessionClosed()) {
            try {
                Long elapsed = m_sw.isRunning()? m_sw.elapsed(TimeUnit.MILLISECONDS): null;
                m_netconfLogger.logResponse(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(), m_clientInfo.getUsername(),
                        Integer.toString(m_clientInfo.getSessionId()), response.getResponseDocument(), m_netconfRequest,
                        elapsed);
                if(m_requestTaskPostRequestExecuteListener !=null) {
                    m_requestTaskPostRequestExecuteListener.postExecuteRequest(m_netconfRequest, response, m_queueWaitingTime, elapsed);
                }
                logUserActivity(response);
                m_channel.sendResponse(response, m_netconfRequest);
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error("Can not send response for request: " + m_netconfRequest.requestToString(), e);
            }
        }else {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug(String.format("Could not log response \n %s \n as the response channel was already closed", response.responseToString()));
            }

        }
    }

    private void logUserActivity(NetConfResponse response) {
        try {
            m_ualLogger.setCanLog(m_canUalLog);
            // UAL should record only for edit-config, action and rpc requests and
            // should not log if action/rpc request has ual:disabled annotation(read-only requests)
            if (m_canUalLog && m_ualLogger.isUalLogEnabled() && isEditConfigOrRpcOrAction() && !m_ncUserActivityLogHandler.isReadOnlyRequest(m_netconfRequest)) {
                NetconfUALLog ualLog = buildNetconfUALLog();
                ualLog.setResult(response.getErrors().size() > 0 ? "failure" : "success");
                m_ncUserActivityLogHandler.logUserActivity(ualLog);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Error while logging user activity log for request : " + m_netconfRequest.requestToString(), e);
        }
    }

    private NetconfUALLog buildNetconfUALLog() {
        long requestStartTime = 0;
        Object cachedValue = RequestScope.getCurrentScope().getFromCache(CURRENT_REQ_START_TIME);
        if(cachedValue != null){
            requestStartTime = (long) cachedValue;
        }
        NetconfUALLog netconfUALLog = new NetconfUALLog()
                .setNetconfRequest(m_netconfRequest)
                .setInvocationTime(requestStartTime)
                .setRequestDocument(m_netconfRequestDocument)
                .setApplications(m_requestContext.getApplication());
        //it is  a strict requirement that loggedIn user context is not null, if it is its a bug
        UserContext loggedInUserCtxt = m_requestContext.getLoggedInUserCtxt();
        UserContext additionalUserCtxt = m_requestContext.getAdditionalUserCtxt();

        if(additionalUserCtxt != null){
            //the twist, the logged inuser now is the delegate
            netconfUALLog.setUsername(additionalUserCtxt.getUsername())
                    .setSessionId(additionalUserCtxt.getSessionId())
                    .setDelegateUser(loggedInUserCtxt.getUsername())
                    .setDelegateSession(loggedInUserCtxt.getSessionId());
        } else {
            netconfUALLog.setUsername(loggedInUserCtxt.getUsername())
                    .setSessionId(loggedInUserCtxt.getSessionId());
        }
        return netconfUALLog;
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

    public void setCanLog(boolean canLog) {
        this.m_canUalLog = canLog;
    }

    public void enqueued() {
        m_enQueueTimer.start();
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

    public Stopwatch getEnQueueTimer() {
        return m_enQueueTimer;
    }

    public Long getWaitingTimeInQueue() {
        return m_queueWaitingTime;
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

    public void setRequestTaskPostRequestListener(RequestTaskPostRequestExecuteListener requestTaskPostRequestExecuteListener) {
        m_requestTaskPostRequestExecuteListener = requestTaskPostRequestExecuteListener;
    }

    public RequestTaskPostRequestExecuteListener getRequestTaskPostRequestExecuteListener() {
        return m_requestTaskPostRequestExecuteListener;
    }
}
