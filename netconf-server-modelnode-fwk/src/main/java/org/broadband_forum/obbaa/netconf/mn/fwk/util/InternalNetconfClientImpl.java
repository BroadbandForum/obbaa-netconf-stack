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

package org.broadband_forum.obbaa.netconf.mn.fwk.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.broadband_forum.obbaa.netconf.api.client.InternalNetconfClient;
import org.broadband_forum.obbaa.netconf.api.client.InternalNetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.RequestContext;
import org.broadband_forum.obbaa.netconf.server.UserContext;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.w3c.dom.Document;

import com.google.common.base.Stopwatch;

public class InternalNetconfClientImpl implements InternalNetconfClient {
    public static final String NC_INTERNAL_CLIENT_TIMEOUT_MILLIS = "NC_INTERNAL_CLIENT_TIMEOUT_MILLIS";
    private static final String DEFAULT_REPLY_TIMEOUT = "60000";
    protected NetconfClientInfo m_clientInfo = null;
    private ServerMessageHandler m_serverMessageHandler = null;
    private AtomicInteger m_requestId = new AtomicInteger();
    protected NetconfLogger m_netconfLogger;
    protected boolean m_runningUT = false;

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(InternalNetconfClientImpl.class, LogAppNames.NETCONF_STACK);
    private Long m_requestTimeout;

    public InternalNetconfClientImpl(String internalUserName, ServerMessageHandler serverMessageHandler, NetconfLogger netconfLogger) {
        m_clientInfo = new InternalNetconfClientInfo(internalUserName, 0);
        m_serverMessageHandler = serverMessageHandler;
        m_netconfLogger = netconfLogger;
    }

    public InternalNetconfClientImpl(NetconfClientInfo clientInfo, ServerMessageHandler serverMessageHandler, NetconfLogger netconfLogger) {
        m_clientInfo = clientInfo;
        m_serverMessageHandler = serverMessageHandler;
        m_netconfLogger = netconfLogger;
    }


    @Override
    public NetConfResponse get(GetRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }

    @Override
    public NetConfResponse editConfig(EditConfigRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }

    @Override
    public NetConfResponse rpc(NetconfRpcRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }
    
    @Override
    public NetConfResponse action(ActionRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }
    
    // Added for UT purpose
    public void setRunningUT(boolean runningUT) {
        m_runningUT = runningUT;
    }
    
    private NetConfResponse assignIdAndSend(AbstractNetconfRequest request) throws InterruptedException, ExecutionException {
        String requestId;
        if(request.getMessageId() != null && request.getMessageId().trim().matches("\\d+")) {
            requestId = request.getMessageId().trim();
        } else {
            requestId = String.valueOf(m_requestId.incrementAndGet());
        }
        request.setMessageId(requestId);
        return sendRequest(request);
    }

    @Override
    public NetConfResponse getConfig(GetConfigRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }

    
    @Override
    public NetConfResponse sendRpcMessage(Document document) throws InterruptedException, ExecutionException {
        NetConfResponse response = null;
        AbstractNetconfRequest netconfRequest = null;
        Stopwatch sw = Stopwatch.createStarted();

        try {
            m_netconfLogger.setThreadLocalDeviceLogId(document);
            m_netconfLogger.logRequest(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(), m_clientInfo.getUsername(),
                    Integer.toString(m_clientInfo.getSessionId()), document);

            if (document.getElementsByTagName(NetconfResources.RPC).getLength() == 0) {
                response = new NetConfResponse();
                response.setOk(false);
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.MISSING_ATTRIBUTE, NetconfRpcErrorType.RPC,
                        NetconfRpcErrorSeverity.Error, "RPC Tag with message ID is missing").addErrorInfoElement(
                        NetconfRpcErrorInfo.BadAttribute, NetconfResources.MESSAGE_ID).addErrorInfoElement(NetconfRpcErrorInfo.BadElement,
                        NetconfResources.RPC);
                response.addError(rpcError);

                m_netconfLogger.logResponse(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(), m_clientInfo.getUsername(),
                    Integer.toString(m_clientInfo.getSessionId()), response.getResponseDocument(), null,
                    sw.elapsed(TimeUnit.MILLISECONDS));
            }

            // if response != null, then there is error - message id missing.
            if(response == null) {
                String typeOfNetconfRequest = DocumentToPojoTransformer.getTypeOfNetconfRequest(document);
                if (typeOfNetconfRequest != null) {
                    switch (typeOfNetconfRequest) {
                        case NetconfResources.CLOSE_SESSION:
                            netconfRequest = DocumentToPojoTransformer.getCloseSession(document);
                            break;
                        case NetconfResources.COPY_CONFIG:
                            netconfRequest = DocumentToPojoTransformer.getCopyConfig(document);
                            break;
                        case NetconfResources.DELETE_CONFIG:
                            netconfRequest = DocumentToPojoTransformer.getDeleteConfig(document);
                            break;
                        case NetconfResources.EDIT_CONFIG:
                            netconfRequest = DocumentToPojoTransformer.getEditConfig(document);
                            break;
                        case NetconfResources.GET:
                            netconfRequest = DocumentToPojoTransformer.getGet(document);
                            break;
                        case NetconfResources.GET_CONFIG:
                            netconfRequest = DocumentToPojoTransformer.getGetConfig(document);
                            break;
                        case NetconfResources.KILL_SESSION:
                            netconfRequest = DocumentToPojoTransformer.getKillSession(document);
                            break;
                        case NetconfResources.LOCK:
                            netconfRequest = DocumentToPojoTransformer.getLockRequest(document);
                            break;
                        case NetconfResources.UNLOCK:
                            netconfRequest = DocumentToPojoTransformer.getUnLockRequest(document);
                            break;
                        case NetconfResources.CREATE_SUBSCRIPTION:
                            netconfRequest = DocumentToPojoTransformer.getCreateSubscriptionRequest(document);
                            break;
                        case NetconfResources.ACTION:
                            netconfRequest = DocumentToPojoTransformer.getAction(document);
                            break;
                        default:
                            // Could be a special rpc request
                            netconfRequest = DocumentToPojoTransformer.getRpcRequest(document);
                    }

                    String messageId = getRequestMessageId(netconfRequest);
                    netconfRequest.setMessageId(messageId);

                    response = sendRequest(netconfRequest);
                } else {
                    response = new NetConfResponse();
                    response.setOk(false);
                    NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.MALFORMED_MESSAGE, NetconfRpcErrorType.RPC,
                            NetconfRpcErrorSeverity.Error, "Invalid request type");
                    response.addError(rpcError);
                }
                if (response != null) {
                    m_netconfLogger.logResponse(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(), m_clientInfo.getUsername(),
                            Integer.toString(m_clientInfo.getSessionId()), response.getResponseDocument(), netconfRequest,
                            sw.elapsed(TimeUnit.MILLISECONDS));
                }
            }
        } catch (NetconfMessageBuilderException e) {
            response = new NetConfResponse();
            if (document != null) {
                try {
                    LOGGER.error("Got an invalid netconf request from: " + m_clientInfo + "\nRequest: "
                            + DocumentUtils.documentToString(document), e);
                } catch (NetconfMessageBuilderException ex) {
                    LOGGER.error("Got an invalid netconf request from : " + m_clientInfo, ex);
                }
            }
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.RPC,
                    NetconfRpcErrorSeverity.Error, "RPC parsing error - " + e.getMessage());
            response.addError(rpcError);
        }
        finally {
            m_netconfLogger.setThreadLocalDeviceLogId(null);
        }
        return response;
    }

    private String getRequestMessageId(AbstractNetconfRequest netconfRequest) {
        String messageId = netconfRequest.getMessageId();
        if (messageId == null || messageId.isEmpty()) {
            messageId = String.valueOf(m_requestId.incrementAndGet());
        }
        return messageId;
    }

    @Override
    public NetconfClientInfo getClientInfo() {
        return m_clientInfo;
    }

    @Override
    public long getRequestTimeOut() {
        if (m_runningUT) {
            return 0L;
        } else {
            if(m_requestTimeout == null){
                synchronized (this){
                    if(m_requestTimeout == null){
                        String replyTimeout = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(NC_INTERNAL_CLIENT_TIMEOUT_MILLIS,DEFAULT_REPLY_TIMEOUT);
                        m_requestTimeout = Long.valueOf(replyTimeout);
                    }
                }
            }
            return m_requestTimeout;
        }
    }

    public NetConfResponse sendRequest(final AbstractNetconfRequest request) throws InterruptedException, ExecutionException {
        request.setReplyTimeout(getRequestTimeOut());
        UserContext additionalUserCtxtTL = RequestContext.getAdditionalUserCtxtTL();
        if(additionalUserCtxtTL != null) {
            request.setUserContext(additionalUserCtxtTL.getUsername());
            request.setContextSessionId(additionalUserCtxtTL.getSessionId());
        }
        CompletableFuture<NetConfResponse> futureResponse = initCompletableFuture();
        m_serverMessageHandler.processRequest(m_clientInfo, request, getResponseChannel(futureResponse));
        try {
            if (m_runningUT) {
                return futureResponse.get(0, TimeUnit.MILLISECONDS);
            }
            return futureResponse.get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            //maintaining blocking map behavior
            return null;
        }
    }

    //for UT
    protected CompletableFuture<NetConfResponse> initCompletableFuture() {
        return new CompletableFuture<>();
    }

    protected static ResponseChannel getResponseChannel(CompletableFuture<NetConfResponse> futureResponse) {
        return new ResponseChannel() {
            @Override
            public void sendResponse(NetConfResponse response, AbstractNetconfRequest request) {
                futureResponse.complete(response);
            }

            @Override
            public void sendNotification(Notification notification) {

            }

            @Override
            public boolean isSessionClosed() {
                return false;
            }

            @Override
            public void markSessionClosed() {

            }

            @Override
            public CompletableFuture<Boolean> getCloseFuture() {
                return new CompletableFuture<>();
            }
        };
    }

    @Override
    public NetconfLogger getNetconfLogger(){
    	return m_netconfLogger;
    }

    /**
     * For UT only.
     * @param requestTimeout
     */
    @Override
    public void setRequestTimeout(long requestTimeout) {
        m_requestTimeout = requestTimeout;
    }

    @Override
    public ServerMessageHandler getServerMessageHandler() {
        return m_serverMessageHandler;
    }

    @Override
    public void setServerMessageHandler(ServerMessageHandler serverMessageHandler) {
        m_serverMessageHandler = serverMessageHandler;
    }
}
