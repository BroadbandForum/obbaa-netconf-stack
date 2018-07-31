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

package org.broadband_forum.obbaa.netconf.server.ssh;

import static org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener.CLOSE_RESPONSE_TIME_OUT_SECS;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.broadband_forum.obbaa.netconf.server.AbstractResponseChannel;
import org.broadband_forum.obbaa.netconf.server.AnvTracingUtil;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLoggingContext;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CompletableMessage;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

/**
 * An abstract class provides that takes care of sending processed netconf request. The actual processing is done by
 * the subclasses based on
 * the type netconf SSH Framing Protocol.
 *
 * @author keshava
 * @see <a href="https://tools.ietf.org/html/rfc6242#section-4.1">RFC 6242 Netconf over SSH Framing Protocol</a>
 */
public abstract class AbstractSshNetconfServerMessageHandler implements SshServerNetconfMessageHandler {
    private static final Logger LOGGER = Logger.getLogger(AbstractSshNetconfServerMessageHandler.class);
    protected NetconfServerMessageListener m_serverMessageListener;
    private NetconfClientInfo m_clientInfo;
    protected IoOutputStream m_out;
    protected ExitCallback m_exitCallBack;
    private boolean m_closeSession = false;
    private final ChannelSession m_channelSession;

    protected ServerMessageHandler m_serverMessageHandler;
    protected ResponseChannel m_responseChannel;

    public AbstractSshNetconfServerMessageHandler(NetconfServerMessageListener axsNetconfServerMessageListener,
                                                  IoOutputStream out,
                                                  ExitCallback exitCallBack, ServerMessageHandler
                                                          serverMessageHandler, ChannelSession channel) {
        this.m_serverMessageListener = axsNetconfServerMessageListener;
        this.m_out = out;
        this.m_exitCallBack = exitCallBack;
        this.m_serverMessageHandler = serverMessageHandler;
        this.m_responseChannel = new SSHChannel();
        this.m_channelSession = channel;
    }

    @Override
    public void onHello(NetconfClientInfo clientInfo, Set<String> clientCaps) {
        this.m_clientInfo = clientInfo;
        this.m_serverMessageListener.onHello(clientInfo, clientCaps);
    }

    @Override
    public void processRequest(String rpcMessage) throws NetconfMessageBuilderException {
        boolean invalidRequest = false;
        Document request = null;
        NetConfResponse response = new NetConfResponse();
        AbstractNetconfRequest netconfRequest = null;
        try {
            request = getRequestDocument(rpcMessage);
            String requestType = DocumentToPojoTransformer.getTypeOfNetconfRequest(request);
            if (AnvTracingUtil.isEmptyRequest(request, requestType)) {
                NetconfLoggingContext.suppress();
            }
            String messageId = DocumentUtils.getInstance().getMessageIdFromRpcDocument(request);
            if (messageId == null || messageId.isEmpty()) {
                throw new NetconfMessageBuilderException(new NetconfRpcError(NetconfRpcErrorTag.MISSING_ATTRIBUTE,
                        NetconfRpcErrorType.RPC,
                        NetconfRpcErrorSeverity.Error, "<message-id> cannot be null/empty")
                        .addErrorInfoElement(NetconfRpcErrorInfo.BadAttribute, NetconfResources.MESSAGE_ID)
                        .addErrorInfoElement(NetconfRpcErrorInfo.BadElement, NetconfResources.RPC).toString());
            }
            response.setMessageId(messageId);
            if (requestType != null) {
                switch (requestType) {
                    case NetconfResources.CLOSE_SESSION:
                        netconfRequest = DocumentToPojoTransformer.getCloseSession(request);
                        break;
                    case NetconfResources.COPY_CONFIG:
                        netconfRequest = DocumentToPojoTransformer.getCopyConfig(request);
                        break;
                    case NetconfResources.DELETE_CONFIG:
                        netconfRequest = DocumentToPojoTransformer.getDeleteConfig(request);
                        break;
                    case NetconfResources.EDIT_CONFIG:
                        netconfRequest = DocumentToPojoTransformer.getEditConfig(request);
                        break;
                    case NetconfResources.GET:
                        netconfRequest = DocumentToPojoTransformer.getGet(request);
                        break;
                    case NetconfResources.GET_CONFIG:
                        netconfRequest = DocumentToPojoTransformer.getGetConfig(request);
                        break;
                    case NetconfResources.KILL_SESSION:
                        netconfRequest = DocumentToPojoTransformer.getKillSession(request);
                        if (!isKillSessionValid((KillSessionRequest) netconfRequest)) {
                            invalidRequest = true;
                            response.setOk(false);
                            NetconfRpcError rpcError = new NetconfRpcError(NetconfRpcErrorTag.INVALID_VALUE,
                                    NetconfRpcErrorType.Application,
                                    NetconfRpcErrorSeverity.Error, "Invalid session ID");
                            response.addError(rpcError);
                        }
                        break;
                    case NetconfResources.LOCK:
                        netconfRequest = DocumentToPojoTransformer.getLockRequest(request);
                        break;
                    case NetconfResources.UNLOCK:
                        netconfRequest = DocumentToPojoTransformer.getUnLockRequest(request);
                        break;
                    case NetconfResources.CREATE_SUBSCRIPTION:
                        netconfRequest = DocumentToPojoTransformer.getCreateSubscriptionRequest(request);
                        break;
                    case NetconfResources.ACTION:
                        netconfRequest = DocumentToPojoTransformer.getAction(request);
                        break;
                    default:
                        // Could be a special rpc request
                        netconfRequest = DocumentToPojoTransformer.getRpcRequest(request);
                }
            } else {
                // Send RPC error and close the session
                invalidRequest = true;
                response.setOk(false);
                NetconfRpcError rpcError = new NetconfRpcError(NetconfRpcErrorTag.MALFORMED_MESSAGE,
                        NetconfRpcErrorType.RPC,
                        NetconfRpcErrorSeverity.Error, "Invalid request type");
                response.addError(rpcError);
                m_closeSession = true;
            }
        } catch (NetconfMessageBuilderException e) {
            if (request != null) {
                try {
                    LOGGER.error("Got an invalid netconf request from :" + m_clientInfo + " " + DocumentUtils
                            .documentToString(request));
                } catch (NetconfMessageBuilderException ex) {
                    LOGGER.error("Got an invalid netconf request from :" + m_clientInfo, ex);
                }
            }
            invalidRequest = true;
            response.setOk(false);
            NetconfRpcError rpcError = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.RPC,
                    NetconfRpcErrorSeverity.Error, "Got an invalid netconf request from " + m_clientInfo);
            response.addError(rpcError);
        } finally {
            NetconfLoggingContext.enable();
        }

        if (invalidRequest) {
            m_responseChannel.sendResponse(response, netconfRequest);
        } else {
            m_serverMessageHandler.processRequest(m_clientInfo, netconfRequest, m_responseChannel);
        }
    }

    private boolean isKillSessionValid(KillSessionRequest killReq) {
        boolean valid = true;
        if (killReq.getSessionId().equals(m_clientInfo.getSessionId())) {
            valid = false;
        }
        return valid;

    }

    protected void closeSession() {
        m_exitCallBack.onExit(0, "close-session sent by client");
        m_responseChannel.markSessionClosed();
        LOGGER.info("Channel is marked as closed, sessionId: " + m_clientInfo.getSessionId());
        m_serverMessageListener.sessionClosed("session closed", m_clientInfo.getSessionId());
        NetconfSubsystem.removeSession(m_clientInfo.getSessionId());
        try {
            m_channelSession.getSession().close();
        } catch (IOException e) {
            LOGGER.error(String.format("Error while closing connection: {}", m_channelSession), e);
        }
    }

    protected void killSession(KillSessionRequest request) {
        NetconfSubsystem.killSession(request.getSessionId());
        m_responseChannel.markSessionClosed();
    }

    protected abstract byte[] getResponseBytes(Document responseDoc) throws NetconfMessageBuilderException;

    protected abstract Document getRequestDocument(String rpcMessage) throws NetconfMessageBuilderException;

    protected ResponseChannel getResponseChannel() {
        return m_responseChannel;
    }

    private class SSHChannel extends AbstractResponseChannel {
        public synchronized void sendResponse(NetConfResponse response, AbstractNetconfRequest request) throws
                NetconfMessageBuilderException {
            // write to the wire
            if (request != null) {
                try {
                    response.setOtherRpcAttributes(DocumentUtils.getInstance().getRpcOtherAttributes(request
                            .getRequestDocument()));
                    if (AnvTracingUtil.isEmptyRequest(request)) {
                        NetconfLoggingContext.suppress();
                    }
                    if (request instanceof CloseSessionRequest) {
                        LOGGER.info("received close session request, sessionId: " + m_clientInfo.getSessionId());
                        if (response.isOk()) {
                            m_closeSession = true;
                        }
                    } else if (request instanceof KillSessionRequest) {
                        LOGGER.info("received kill session request, sessionId: " + m_clientInfo.getSessionId());

                        if (response.isOk()) {
                            killSession((KillSessionRequest) request);
                        }
                    }
                } finally {
                    NetconfLoggingContext.enable();
                }
            }

            try {
                Document responseDoc = response.getResponseDocument();
                byte[] responseBytes = getResponseBytes(responseDoc);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Sending response: %s , bytes to be sent: %s", DocumentUtils
                            .prettyPrint(responseDoc), responseBytes));
                }
                writeBytes(response, responseBytes);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Sent response: %s", DocumentUtils.prettyPrint(responseDoc)));
                }
            } finally {
                if (m_closeSession) {
                    LOGGER.info("closing session after sending response");
                    closeSession();
                }
            }
        }

        @Override
        public synchronized void sendNotification(Notification notification) {
            try {
                Document document = notification.getNotificationDocument();
                byte[] notificationBytes = DocumentToPojoTransformer.addRpcDelimiter(DocumentToPojoTransformer
                        .getBytesFromDocument(document));
                if (m_out.isClosed()) {
                    LOGGER.debug("session " + m_clientInfo.getSessionId() + " is already closed, m_out: " + m_out
                            .hashCode());
                }
                if (m_out.isClosing()) {
                    LOGGER.debug("session " + m_clientInfo.getSessionId() + " is being closed");
                }
                if (!m_out.isClosed() && !m_out.isClosing()) {
                    writeBytes(notification, notificationBytes);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("A notification has been pushed to %s", m_clientInfo.toString()));
                    }
                } else {
                    // session is closed
                    m_closeSession = true;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Notification not pushed to " + m_clientInfo.toString() + "-session closed-" +
                                notification.notificationToPrettyString());
                    }
                }
            } catch (Exception e) {
                // When the server fails to deliver a notification (EG TCP error), the server closes the session that
                // created the
                // subscription
                LOGGER.error("Error occurred when sending notification, closing session..", e);
                m_closeSession = true;
            } finally {
                if (m_closeSession) {
                    closeSession();
                }
            }
        }

        private void writeBytes(CompletableMessage message, byte[] responseBytes) {
            try {
                IoWriteFuture writeFuture = m_out.write(new ByteArrayBuffer(responseBytes));
                writeFuture.addListener(new SshFutureListener<IoWriteFuture>() {
                    public void operationComplete(IoWriteFuture future) {
                        if (future.isWritten()) {
                            message.getMessageSentFuture().complete("Response Sent");
                        } else {
                            LOGGER.error("Message not sent to " + m_clientInfo.toString(), future.getException());
                            message.getMessageSentFuture().completeExceptionally(future.getException());
                        }
                    }
                });

                if (m_closeSession) {
                    try {
                        writeFuture.await(CLOSE_RESPONSE_TIME_OUT_SECS, TimeUnit.SECONDS);
                    } catch (IOException e) {
                        LOGGER.warn("Interrupted while sending close/kill session response", e);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error while sending the reponse to " + m_clientInfo.toString(), e);
                message.getMessageSentFuture().completeExceptionally(e);
                if (message instanceof Notification) {
                    throw e;
                }
            }
        }

    }

    /**
     * Only for UT
     **/
    protected void setNetConfClientInfo(NetconfClientInfo clientInfo) {
        m_clientInfo = clientInfo;
    }

}
