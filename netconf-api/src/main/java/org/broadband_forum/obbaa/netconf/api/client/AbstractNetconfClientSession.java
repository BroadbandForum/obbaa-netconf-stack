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
import org.broadband_forum.obbaa.netconf.api.messages.LogUtil;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.BlockingMap;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An abstract class that takes care of converting Pojo netconf requests into {@link Document}. The class delegates
 * the actual transport of
 * the {@link Document} to the subclasses via {@link #sendRpcMessage(String, Document, long)}
 *
 * @author keshava
 */
public abstract class AbstractNetconfClientSession implements NetconfClientSession {
    private static final Logger LOGGER = Logger.getLogger(AbstractNetconfClientSession.class);
    public static final long DEFAULT_MESSAGE_TIMEOUT = 100000;

    protected BlockingMap<String, NetConfResponse> m_rpcResponses = new BlockingMap<String, NetConfResponse>();
    AtomicLong m_messageId = new AtomicLong(0);
    private Set<String> m_serverCapabilities = new HashSet<String>();
    private int m_sessionId;
    private Set<String> m_clientCaps = new HashSet<String>();
    private List<NetconfClientSessionListener> m_sessionListeners = new ArrayList<NetconfClientSessionListener>();
    private Set<NotificationListener> m_notificationListeners = new CopyOnWriteArraySet<>();
    private Notification m_notificationReceived;
    private AtomicInteger m_keepAliveFailure = new AtomicInteger(0);
    private long m_idleTimeStart;

    public AbstractNetconfClientSession() {
        super();
        m_idleTimeStart = System.currentTimeMillis();
    }

    @Override
    public Future<NetConfResponse> getConfig(GetConfigRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    protected abstract Future<NetConfResponse> sendRpcMessage(String currentMessageId, Document requestDocument, long
            timoutMillis);

    protected void responseRecieved(String msgId, NetConfResponse axsNetconfResponse) {
        m_rpcResponses.put(msgId, axsNetconfResponse);
    }

    @Override
    public Future<NetConfResponse> editConfig(EditConfigRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> copyConfig(CopyConfigRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> deleteConfig(DeleteConfigRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> lock(LockRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> unlock(UnLockRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> get(GetRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> rpc(NetconfRpcRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> action(ActionRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> sendRpc(AbstractNetconfRequest request) throws NetconfMessageBuilderException {
        setMessageId(request);
        return sendRpcAndGetFuture(request, request.getMessageId());
    }

    protected Future<NetConfResponse> sendRpcAndGetFuture(AbstractNetconfRequest request, String currentMessageId)
            throws NetconfMessageBuilderException {
        Document requestDocument = request.getRequestDocument();
        Future<NetConfResponse> responseFuture = sendRpcMessage(currentMessageId, requestDocument, request
                .getReplyTimeout());
        resetIdleTimeStart();
        return responseFuture;
    }

    @Override
    public void setMessageId(AbstractNetconfRequest request) {
        final String currentMessageId = String.valueOf(m_messageId.addAndGet(1));
        request.setMessageId(currentMessageId);
    }

    @Override
    public Future<NetConfResponse> createSubscription(CreateSubscriptionRequest request, NotificationListener
            notificationListener)
            throws NetconfMessageBuilderException {
        addNotificationListener(notificationListener);
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> closeSession(CloseSessionRequest request) throws NetconfMessageBuilderException {
        return sendRpc(request);
    }

    @Override
    public Future<NetConfResponse> killSession(KillSessionRequest request) throws NetconfMessageBuilderException {
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

    public void responseRecieved(Document responseDoc) throws NetconfMessageBuilderException {
        LOGGER.debug("Received response: " + DocumentUtils.prettyPrint(responseDoc));
        resetIdleTimeStart();
        Element rootElement = responseDoc.getDocumentElement();
        if (NetconfResources.HELLO.equals(rootElement.getNodeName())) {
            NetconfHelloMessage hello = DocumentToPojoTransformer.getHelloMessage(responseDoc);
            onHello(hello);
        } else if (NetconfResources.RPC_REPLY.equals(rootElement.getNodeName())) {
            String msgId = DocumentUtils.getInstance().getMessageIdFromRpcReplyDocument(responseDoc);
            if (msgId == null || msgId.isEmpty()) {// There is a possibility that netconf server can send rpc-error
                // without a message-id
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
            Notification notification = DocumentToPojoTransformer.getNotification(responseDoc);
            m_notificationReceived = notification;
            notificationReceived(notification);
            LOGGER.debug("called notificationReceived for " + notification.notificationToPrettyString());
        }
    }

    //Added for Unit Test
    protected Notification getNotificationReceived() {
        return m_notificationReceived;
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
        for (NetconfClientSessionListener listener : m_sessionListeners) {
            listener.sessionClosed(this.m_sessionId);
        }
    }

    private void notificationReceived(Notification notification) {
        LOGGER.debug("notification-received : " + notification.notificationToPrettyString());
        for (NotificationListener listener : m_notificationListeners) {
            listener.notificationReceived(notification);
        }
    }

    protected void logRequest(Document requestDocument, String currentMessageId) {
        try {
            LogUtil.logTrace(LOGGER, "Sending RPC Request to: %s with message-id: %s rpc-request:\n %s",
                    getRemoteAddress(), currentMessageId, DocumentUtils.documentToPrettyString(requestDocument));
        } catch (NetconfMessageBuilderException e) {
            LogUtil.logTrace(LOGGER, "Sending RPC Request to: %s with message-id: %s ", getRemoteAddress(),
                    currentMessageId);
        }
    }

    protected void logResponse(NetConfResponse response, String currentMessageId) {
        if (response != null) {
            LogUtil.logTrace(LOGGER, "Received response from: %s for a message with with message-id: %s rpc-reply:\n " +
                            "%s", getRemoteAddress(),
                    currentMessageId, response);
        } else {
            LogUtil.logTrace(LOGGER, "Received NO response from: %s for a message with with message-id: %s",
                    getRemoteAddress(),
                    currentMessageId);
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

    @Override
    public int incrementAndGetFailedKACount() {
        int count = m_keepAliveFailure.incrementAndGet();
        m_keepAliveFailure.set(count);
        return count;
    }

    @Override
    public void resetKAFailureCount() {
        m_keepAliveFailure.set(0);
        LOGGER.trace("keep-alive failure count has been reset to zero");
    }

    public AtomicInteger getKeepAliveFailure() {
        return m_keepAliveFailure;
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
}
