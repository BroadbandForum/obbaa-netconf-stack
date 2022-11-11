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

package org.broadband_forum.obbaa.netconf.api.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.broadband_forum.obbaa.netconf.api.ClosureReason;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfHelloMessage;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An abstract class that takes care of converting Pojo netconf requests into {@link Document}. The class delegates the actual transport of
 * the {@link Document} to the subclasses via {@link #sendRpcMessage(String, Document, long)}
 *
 * 
 */
public abstract class AbstractNetconfClientSession implements NetconfClientSession {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AbstractNetconfClientSession.class, LogAppNames.NETCONF_LIB);
    public static final long DEFAULT_MESSAGE_TIMEOUT = 100000;

    protected Map<String, NetconfResponseFuture> m_responseFutures = new ConcurrentHashMap<>();
    AtomicLong m_messageId = new AtomicLong(0);
    private Set<String> m_serverCapabilities = new HashSet<>();
    private int m_sessionId;
    private Set<String> m_clientCaps = new HashSet<>();
    private List<NetconfClientSessionListener> m_sessionListeners = new ArrayList<>();
    private Set<NotificationListener> m_notificationListeners = new CopyOnWriteArraySet<>();
    private long m_idleTimeStart;
    private ClosureReason m_closureReason;

    public AbstractNetconfClientSession() {
        super();
        m_idleTimeStart = System.currentTimeMillis();
    }

    @Override
    public NetconfResponseFuture getConfig(GetConfigRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    protected abstract NetconfResponseFuture sendRpcMessage(String currentMessageId, Document requestDocument, long timoutMillis);

    protected void responseRecieved(String msgId, NetConfResponse response) {
        NetconfResponseFuture responseFuture = m_responseFutures.get(msgId);
        if (responseFuture != null) {
            responseFuture.complete(response);
        } else if(response != null) {
            LOGGER.error("Unable to handle the response  " + response.responseToString());
        }
        m_responseFutures.remove(msgId);
    }

    @Override
    public NetconfResponseFuture editConfig(EditConfigRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture copyConfig(CopyConfigRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture deleteConfig(DeleteConfigRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture lock(LockRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture unlock(UnLockRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture get(GetRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture rpc(NetconfRpcRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture action(ActionRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture sendRpc(AbstractNetconfRequest request) throws NetconfMessageBuilderException {
        setMessageId(request);
        return sendRpcAndGetFuture(request, request.getMessageId());
    }

    protected NetconfResponseFuture sendRpcAndGetFuture(AbstractNetconfRequest request, String currentMessageId) throws NetconfMessageBuilderException {
        Document requestDocument = getRequestDocument(request);
        NetconfResponseFuture responseFuture;
        if (isOpen()) {
            responseFuture = sendRpcMessage(currentMessageId, requestDocument, request.getReplyTimeout());
        } else {
            LOGGER.warn("Session with id=" + getSessionId() + " was closed. Failed to send the " +
                    request.getClass().getName() + " with message id = " + request.getMessageId());
            responseFuture = new NetconfResponseFuture(1, TimeUnit.MILLISECONDS);
            responseFuture.complete(null);
        }
        resetIdleTimeStart();
        unsetConfigElement(request);
        return responseFuture;
    }

    private void unsetConfigElement(AbstractNetconfRequest request) {
        if (hasReqStringCopy(request)) {
            ((EditConfigRequest)request).unsetConfigElement();
        }
    }

    private Document getRequestDocument(AbstractNetconfRequest request) throws NetconfMessageBuilderException {
        if (hasReqStringCopy(request)) {
            ((EditConfigRequest)request).setConfigElement();
        }
        return request.getRequestDocument();
    }

    private boolean hasReqStringCopy(AbstractNetconfRequest request) {
        return request instanceof EditConfigRequest && ((EditConfigRequest) request).getReqXmlStrCopy() != null;
    }

    @Override
    public void setMessageId(AbstractNetconfRequest request) {
        final String currentMessageId = String.valueOf(m_messageId.addAndGet(1));
        request.setMessageId(currentMessageId);
    }

    @Override
    public NetconfResponseFuture createSubscription(CreateSubscriptionRequest request, NotificationListener notificationListener)
            throws NetconfMessageBuilderException {
        addNotificationListener(notificationListener);
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture closeSession(CloseSessionRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public NetconfResponseFuture killSession(KillSessionRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public boolean getServerCapability(String capability) {
        return m_serverCapabilities.contains(capability);
    }

    @Override
    public Set<String> getServerCapabilities() {
        return m_serverCapabilities;
    }

    @Override
    public int getSessionId() {
        return m_sessionId;
    }

    public void responseRecieved(DocumentInfo documentInfo) throws NetconfMessageBuilderException {
        Document responseDoc = documentInfo.getDocument();
        LOGGER.debug("Received response: " + DocumentUtils.prettyPrint(responseDoc));
        resetIdleTimeStart();
        Element rootElement = responseDoc.getDocumentElement();
        if (NetconfResources.HELLO.equals(rootElement.getNodeName())) {
            NetconfHelloMessage hello = DocumentToPojoTransformer.getHelloMessage(responseDoc);
            onHello(hello);
        } else if (NetconfResources.RPC_REPLY.equals(rootElement.getNodeName())) {
            String msgId = DocumentUtils.getInstance().getMessageIdFromRpcReplyDocument(responseDoc);
            if (msgId == null || msgId.isEmpty()) {// There is a possibility that netconf server can send rpc-error without a message-id
                List<NetconfRpcError> errors = DocumentToPojoTransformer.getRpcErrorsFromRpcReply(responseDoc);
                if (!badMessageId(errors)) {
                    throw new NetconfMessageBuilderException(new NetconfRpcError(NetconfRpcErrorTag.MISSING_ATTRIBUTE,
                            NetconfRpcErrorType.RPC, NetconfRpcErrorSeverity.Error, "<message-id> cannot be null/empty")
                            .addErrorInfoElement(NetconfRpcErrorInfo.BadAttribute, NetconfResources.MESSAGE_ID)
                            .addErrorInfoElement(NetconfRpcErrorInfo.BadElement, NetconfResources.RPC).toString());
                }
            }
            NetConfResponse axsNetconfResponse = DocumentToPojoTransformer.getNetconfResponse(responseDoc);
            responseRecieved(msgId, axsNetconfResponse);
        } else if (NetconfResources.NOTIFICATION.equals(rootElement.getNodeName())) {
            Notification notification = DocumentToPojoTransformer.getNotification(documentInfo);
            notificationReceived(notification);
            LOGGER.debug("called notificationReceived for " + notification.notificationToPrettyString());
        }
    }

    private boolean badMessageId(List<NetconfRpcError> errors) {
        for (NetconfRpcError error : errors) {
            Element errorElement = error.getErrorInfo();
            if (errorElement != null) {
                NodeList nodes = errorElement.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node item = nodes.item(i);
                        if (item.getNodeName().equals(NetconfRpcErrorTag.BAD_ATTRIBUTE)) {
                            if (item.hasChildNodes()) {
                                return item.getFirstChild().getNodeValue().equals(NetconfResources.MESSAGE_ID);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    protected void setSessionId(int sessionId) {
        this.m_sessionId = sessionId;
    }

    protected void onHello(NetconfHelloMessage hello) {
        LOGGER.debug("hello-received : " + hello);
        setSessionId(hello.getSessionId());
        setServerCapabilities(hello.getCapabilities());
    }

    private void setServerCapabilities(Set<String> capabilities) {
        this.m_serverCapabilities = capabilities;
    }

    public void setClientCapabilities(Set<String> caps) {
        this.m_clientCaps = caps;

    }

    public Set<String> getClientCapabilities() {
        return this.m_clientCaps;
    }

    public boolean getClientCapability(String cap) {
        return this.m_clientCaps.contains(cap);
    }

    public void addSessionListener(NetconfClientSessionListener listener) {
        if (listener != null) {
            this.m_sessionListeners.add(listener);
        }
        if (!isOpen()) {
            sessionClosed();
        }
    }

    public void addNotificationListener(NotificationListener listener) {
        if (listener != null) {
            this.m_notificationListeners.add(listener);
            LOGGER.debug("Added a NotificationListener");
        }
    }

    @Override
    public void sessionClosed() {
        LOGGER.debug("Received session closed on sessionId : " + m_sessionId);

        for (CompletableFuture responseFuture : m_responseFutures.values()) {
            responseFuture.complete(null);
        }
        for (NetconfClientSessionListener listener : m_sessionListeners) {
            listener.sessionClosed(this.m_sessionId, m_closureReason);
        }
    }

    public void closeAsync() {
        closeAsync(null);
    }

    public void closeAsync(ClosureReason closureReason) {
        m_closureReason = closureReason;
    }

    @Override
    public void setClosureReason(ClosureReason closureReason) {
        m_closureReason = closureReason;
    }

    private void notificationReceived(Notification notification) {
        LOGGER.debug("notification-received : " + notification.notificationToPrettyString());
        for (NotificationListener listener : m_notificationListeners) {
            listener.notificationReceived(notification);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractNetconfClientSession that = (AbstractNetconfClientSession) o;

        return m_sessionId == that.m_sessionId;

    }

    @Override
    public int hashCode() {
        return m_sessionId;
    }

    @Override
    public String toString() {
        return "NetconfClientSession [m_messageId=" + m_messageId + ", m_sessionId=" + m_sessionId + "]";
    }


    protected void resetIdleTimeStart() {
        m_idleTimeStart = System.currentTimeMillis();
    }

    @Override
    public long getIdleTimeStart() {
        return m_idleTimeStart;
    }

    @Override
    public void closeGracefully() throws IOException {
    }

    @Override
    public NetconfClientSession getType() {
        return this;
    }

}
