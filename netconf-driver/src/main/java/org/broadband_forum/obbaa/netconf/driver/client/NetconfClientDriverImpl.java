/**
 * 
 */
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

package org.broadband_forum.obbaa.netconf.driver.client;

import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getCloseSession;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getCopyConfig;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getCreateSubscriptionRequest;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getEditConfig;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getGet;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getGetConfig;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getKillSession;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getLockRequest;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getRpcRequest;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getUnLockRequest;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.w3c.dom.Document;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

/**
 * NetconfClientDriverImpl implements NetconfClientDriver such that allows sending seamless netconf request as xml messages.
 * 
 *
 * 
 */
public class NetconfClientDriverImpl implements NetconfClientDriver {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NetconfClientDriverImpl.class, LogAppNames.NETCONF_LIB);
    private static final String ERROR_MESSAGE = "A message could not be handled because it failed to \nbe parsed correctly. For example, "
            + "the message is not \nwell-formed XML or it uses an invalid character set";

    private NetconfClientSession m_netconfClientSession;

    public NetconfClientDriverImpl(NetconfClientSession netconfClientSession) {
        this.m_netconfClientSession = netconfClientSession;
    }
    
    @Override
    public void closeSession() throws InterruptedException, IOException {
        m_netconfClientSession.close();
    }

    @Override
    public String sendGetRequest(String getXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendGetRequest(getXmlRequest, null);
    }

    @Override
    public String sendGetRequest(String getXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        try {
            return sendRequest(getRequest(getXmlRequest, NetconfResources.GET), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendGetConfigRequest(String getConfigXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendGetConfigRequest(getConfigXmlRequest, null);
    }

    @Override
    public String sendGetConfigRequest(String getConfigXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException {
        try {
            return sendRequest(getRequest(getConfigXmlRequest, NetconfResources.GET_CONFIG), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendEditConfigRequest(String editConfigXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendEditConfigRequest(editConfigXmlRequest, null);
    }

    @Override
    public String sendEditConfigRequest(String editConfigXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException {
        try {
            return sendRequest(getRequest(editConfigXmlRequest, NetconfResources.EDIT_CONFIG), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendCopyConfigRequest(String copyConfigXmlRequest) throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        return sendCopyConfigRequest(copyConfigXmlRequest, null);
    }

    @Override
    public String sendCopyConfigRequest(String copyConfigXmlRequest, Long timeoutInMillis) throws NetconfMessageBuilderException, InterruptedException,
            ExecutionException {
        try {
            return sendRequest(getRequest(copyConfigXmlRequest, NetconfResources.COPY_CONFIG), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendRpcRequest(String rpcXmlRequest) throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        return sendRpcRequest(rpcXmlRequest, null);
    }

    @Override
    public String sendRpcRequest(String rpcXmlRequest, Long timeoutInMillis) throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        try {
            return sendRequest(getRequest(rpcXmlRequest, NetconfResources.RPC), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendLockRequest(String lockXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendLockRequest(lockXmlRequest, null);
    }

    @Override
    public String sendLockRequest(String lockXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        try {
            return sendRequest(getRequest(lockXmlRequest, NetconfResources.LOCK), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendUnLockRequest(String unLockXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendUnLockRequest(unLockXmlRequest, null);
    }

    @Override
    public String sendUnLockRequest(String unLockXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException {
        try {
            return sendRequest(getRequest(unLockXmlRequest, NetconfResources.UNLOCK), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendCloseSessionRequest(String closeSessionXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendCloseSessionRequest(closeSessionXmlRequest, null);
    }

    @Override
    public String sendCloseSessionRequest(String closeSessionXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException {
        try {
            return sendRequest(getRequest(closeSessionXmlRequest, NetconfResources.CLOSE_SESSION), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendKillSessionRequest(String killSessionXmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendKillSessionRequest(killSessionXmlRequest, null);
    }

    @Override
    public String sendKillSessionRequest(String killSessionXmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException {
        try {
            return sendRequest(getRequest(killSessionXmlRequest, NetconfResources.KILL_SESSION), timeoutInMillis);
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendCreateSubscriptionRequest(String createSubscriptionXmlRequest, NotificationListener notificationListener)
            throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendCreateSubscriptionRequest(createSubscriptionXmlRequest, notificationListener, null);
    }

    @Override
    public String sendCreateSubscriptionRequest(String createSubscriptionXmlRequest, NotificationListener notificationListener, Long timeoutInMillis)
            throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        try {
            CreateSubscriptionRequest createSubscriptionRequest = (CreateSubscriptionRequest) getRequest(createSubscriptionXmlRequest,
                    NetconfResources.CREATE_SUBSCRIPTION);
            if (timeoutInMillis != null) {
                createSubscriptionRequest.setReplyTimeout(timeoutInMillis);
            }
            NetConfResponse response = m_netconfClientSession.createSubscription(createSubscriptionRequest, notificationListener).get();
            return response.responseToString();
        } catch (NetconfMessageBuilderException e) {
            return createErrorResponse();
        }
    }

    @Override
    public String sendRequest(String xmlRequest) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        return sendRequest(xmlRequest, null);
    }

    @Override
    public String sendRequest(String xmlRequest, Long timeoutInMillis) throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        Document document = DocumentUtils.stringToDocument(xmlRequest);
        AbstractNetconfRequest request = DocumentToPojoTransformer.getRequest(document);
        return sendRequest(request, timeoutInMillis);
    }

    private String sendRequest(AbstractNetconfRequest request, Long timeoutInMillis) throws InterruptedException, ExecutionException,
            NetconfMessageBuilderException {
        NetConfResponse response = null;
        LOGGER.info(String.format("Before sending request: %s, client info: %s \n Is session %s opening: %s", request.getMessageId(),
                request.getClientInfo(), m_netconfClientSession.getSessionId(), m_netconfClientSession.isOpen()));
        if (timeoutInMillis != null) {
            request.setReplyTimeout(timeoutInMillis);
        }
        if (request instanceof GetRequest) {
            response = m_netconfClientSession.get((GetRequest) request).get();
        } else if (request instanceof GetConfigRequest) {
            response = m_netconfClientSession.getConfig((GetConfigRequest) request).get();
        } else if (request instanceof EditConfigRequest) {
            response = m_netconfClientSession.editConfig((EditConfigRequest) request).get();
        } else if (request instanceof CopyConfigRequest) {
            response = m_netconfClientSession.copyConfig((CopyConfigRequest) request).get();
        } else if (request instanceof LockRequest) {
            response = m_netconfClientSession.lock((LockRequest) request).get();
        } else if (request instanceof UnLockRequest) {
            response = m_netconfClientSession.unlock((UnLockRequest) request).get();
        } else if (request instanceof CloseSessionRequest) {
            response = m_netconfClientSession.closeSession((CloseSessionRequest) request).get();
        } else if (request instanceof KillSessionRequest) {
            response = m_netconfClientSession.killSession((KillSessionRequest) request).get();
        } else if (request instanceof NetconfRpcRequest) {
            response = m_netconfClientSession.rpc((NetconfRpcRequest) request).get();
        } else {
            throw new NetconfMessageBuilderException("invalid netconf xml request.");
        }
        if (response != null) {
            return response.responseToString();
        }
        else {
            if(request instanceof KillSessionRequest || request instanceof CloseSessionRequest){
                LOGGER.info("Response is null, but we did a close/kill session request, then ok");
                return null;
            } else {
                LOGGER.info(String.format("Response is null, is session %s opening: %s", m_netconfClientSession.getSessionId(),
                        m_netconfClientSession.isOpen()));
                throw new RuntimeException("Timeout on request or session not open");
            }
        }
    }

    private AbstractNetconfRequest getRequest(String request, String requestType) throws NetconfMessageBuilderException {
        Document requestDoc = null;
        try {
            requestDoc = stringToDocument(request);
            switch (requestType) {
            case NetconfResources.GET:
                return getGet(requestDoc);
            case NetconfResources.GET_CONFIG:
                return getGetConfig(requestDoc);
            case NetconfResources.EDIT_CONFIG:
                return getEditConfig(requestDoc);
            case NetconfResources.COPY_CONFIG:
                return getCopyConfig(requestDoc);
            case NetconfResources.RPC:
                return getRpcRequest(requestDoc);
            case NetconfResources.LOCK:
                return getLockRequest(requestDoc);
            case NetconfResources.UNLOCK:
                return getUnLockRequest(requestDoc);
            case NetconfResources.CLOSE_SESSION:
                return getCloseSession(requestDoc);
            case NetconfResources.KILL_SESSION:
                return getKillSession(requestDoc);
            case NetconfResources.CREATE_SUBSCRIPTION:
                return getCreateSubscriptionRequest(requestDoc);
            default:
                return null;
            }
        } catch (NetconfMessageBuilderException e) {
            throw new NetconfMessageBuilderException();
        }

    }

    private String createErrorResponse() throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        NetconfRpcError rpcError = new NetconfRpcError(NetconfRpcErrorTag.MALFORMED_MESSAGE, NetconfRpcErrorType.RPC,
                NetconfRpcErrorSeverity.Error, ERROR_MESSAGE);
        NetConfResponse netConfResponse = new NetConfResponse();
        netConfResponse.addError(rpcError);
        String response = netConfResponse.responseToString();
        m_netconfClientSession.sessionClosed();
        return response;
    }

}
