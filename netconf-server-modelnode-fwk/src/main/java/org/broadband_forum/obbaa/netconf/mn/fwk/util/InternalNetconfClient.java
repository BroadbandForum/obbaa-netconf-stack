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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.client.InternalNetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
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
import org.broadband_forum.obbaa.netconf.api.util.BlockingMap;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.server.AbstractResponseChannel;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class InternalNetconfClient {
    public static final String NC_INTERNAL_CLIENT_TIMEOUT_MILLIS = "NC_INTERNAL_CLIENT_TIMEOUT_MILLIS";
    private static final String DEFAULT_REPLY_TIMEOUT = "60000";
    NetconfClientInfo m_clientInfo = null;
    private ServerMessageHandler m_serverMessageHandler = null;
    private BlockingMap<String, NetConfResponse> m_responsesQueue = new BlockingMap<>();
    private AtomicInteger m_requestId = new AtomicInteger();
    private ResponseChannel m_responseChannel;
    private NetconfLogger m_netconfLogger;
    private boolean m_runningUT = false;

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(InternalNetconfClient.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");
    private Long m_requestTimeout;

    public InternalNetconfClient(String internalUserName, ServerMessageHandler serverMessageHandler, NetconfLogger
            netconfLogger) {
        m_clientInfo = new InternalNetconfClientInfo(internalUserName, 0);
        m_serverMessageHandler = serverMessageHandler;
        m_responseChannel = new PmaWebAppResponseChannel();
        m_netconfLogger = netconfLogger;
    }

    public InternalNetconfClient(NetconfClientInfo clientInfo, ServerMessageHandler serverMessageHandler,
                                 NetconfLogger netconfLogger) {
        m_clientInfo = clientInfo;
        m_serverMessageHandler = serverMessageHandler;
        m_responseChannel = new PmaWebAppResponseChannel();
        m_netconfLogger = netconfLogger;
    }


    public NetConfResponse get(GetRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }

    public NetConfResponse editConfig(AbstractNetconfRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }

    public NetConfResponse rpc(NetconfRpcRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }

    // Added for UT purpose
    public void setRunningUT(boolean runningUT) {
        m_runningUT = runningUT;
    }

    /*
    * For UT only
    * **/
    protected AtomicInteger getRequestId() {
        return m_requestId;
    }


    /*
    * For UT only
    * **/
    protected BlockingMap<String, NetConfResponse> getResponsesQueue() {
        return m_responsesQueue;
    }

    private NetConfResponse assignIdAndSend(AbstractNetconfRequest request) throws InterruptedException,
            ExecutionException {
        String requestId = String.valueOf(m_requestId.incrementAndGet());
        request.setMessageId(requestId);
        return sendRequest(request);
    }

    public NetConfResponse getConfig(GetConfigRequest request) throws InterruptedException, ExecutionException {
        return assignIdAndSend(request);
    }


    public NetConfResponse sendRpcMessage(Document document) throws InterruptedException, ExecutionException {
        NetConfResponse response = new NetConfResponse();
        AbstractNetconfRequest netconfRequest = null;

        try {
            m_netconfLogger.logRequest(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(), m_clientInfo
                    .getUsername(), new Integer(m_clientInfo.getSessionId()).toString(), document);

            if (document.getElementsByTagName(NetconfResources.RPC).getLength() == 0) {
                response.setOk(false);
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag
                                .MISSING_ATTRIBUTE, NetconfRpcErrorType.RPC,
                        NetconfRpcErrorSeverity.Error, "RPC Tag with message ID is missing").addErrorInfoElement(
                        NetconfRpcErrorInfo.BadAttribute, NetconfResources.MESSAGE_ID).addErrorInfoElement
                        (NetconfRpcErrorInfo.BadElement,
                        NetconfResources.RPC);
                response.addError(rpcError);

                m_netconfLogger.logResponse(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(), m_clientInfo
                                .getUsername(),
                        new Integer(m_clientInfo.getSessionId()).toString(), response.getResponseDocument());
                return response;
            }

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

                String newRequestId = String.valueOf(m_requestId.incrementAndGet());
                netconfRequest.setMessageId(newRequestId);

                response = sendRequest(netconfRequest);
            } else {
                response.setOk(false);
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag
                                .MALFORMED_MESSAGE, NetconfRpcErrorType.RPC,
                        NetconfRpcErrorSeverity.Error, "Invalid request type");
                response.addError(rpcError);
            }

            if (response != null) {
                m_netconfLogger.logResponse(m_clientInfo.getRemoteHost(), m_clientInfo.getRemotePort(), m_clientInfo
                                .getUsername(),
                        new Integer(m_clientInfo.getSessionId()).toString(), response.getResponseDocument());
                return response;
            }
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("invalid response received ", e);
        }
        return null;
    }

    public long getRequestTimeOut() {
        if (m_runningUT) {
            return 0L;
        } else {
            if (m_requestTimeout == null) {
                synchronized (this) {
                    if (m_requestTimeout == null) {
                        String replyTimeout = DEFAULT_REPLY_TIMEOUT;
                        if (System.getProperties().containsKey(NC_INTERNAL_CLIENT_TIMEOUT_MILLIS)) {
                            replyTimeout = System.getProperty(NC_INTERNAL_CLIENT_TIMEOUT_MILLIS);
                        } else if (System.getenv().containsKey(NC_INTERNAL_CLIENT_TIMEOUT_MILLIS)) {
                            replyTimeout = System.getenv(NC_INTERNAL_CLIENT_TIMEOUT_MILLIS);
                        }
                        m_requestTimeout = Long.valueOf(replyTimeout);
                    }
                }
            }
            return m_requestTimeout;
        }
    }

    private NetConfResponse sendRequest(final AbstractNetconfRequest request) throws InterruptedException,
            ExecutionException {
        request.setReplyTimeout(getRequestTimeOut());
        m_serverMessageHandler.processRequest(m_clientInfo, request, m_responseChannel);
        if (m_runningUT) {
            return m_responsesQueue.get(request.getMessageId(), 0, TimeUnit.MILLISECONDS);
        }
        return m_responsesQueue.get(request.getMessageId(), Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public NetconfLogger getNetconfLogger() {
        return m_netconfLogger;
    }

    /**
     * For UT only.
     *
     * @param requestTimeout
     */
    void setRequestTimeout(long requestTimeout) {
        m_requestTimeout = requestTimeout;
    }

    private class PmaWebAppResponseChannel extends AbstractResponseChannel {
        @Override
        public void sendResponse(NetConfResponse response, AbstractNetconfRequest request) throws
                NetconfMessageBuilderException {
            m_responsesQueue.put(response.getMessageId(), response);
        }

        @Override
        public void sendNotification(Notification notification) {
        }
    }

    public ServerMessageHandler getServerMessageHandler() {
        return m_serverMessageHandler;
    }
}
