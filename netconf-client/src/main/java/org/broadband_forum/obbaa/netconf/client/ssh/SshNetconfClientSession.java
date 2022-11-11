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

package org.broadband_forum.obbaa.netconf.client.ssh;

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DATE_TIME_FORMATTER;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.io.nio2.Nio2Session;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.broadband_forum.obbaa.netconf.api.ClosureReason;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfSessionClosedException;
import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfResponseFuture;
import org.broadband_forum.obbaa.netconf.api.codec.v2.FrameAwareNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.codec.v2.FrameAwareNetconfMessageCodecV2Impl;
import org.broadband_forum.obbaa.netconf.api.codec.v2.NetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.Document;

public class SshNetconfClientSession extends AbstractNetconfClientSession {
    private ChannelSubsystem m_clientChannel;
    private ClientSession m_clientSession;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshNetconfClientSession.class, LogAppNames.NETCONF_LIB);
    private final ExecutorService m_executorService;// NOSONAR
    private SshClient m_sshClient;
    private final long m_creationTime;
    private FrameAwareNetconfMessageCodecV2 m_codec = new FrameAwareNetconfMessageCodecV2Impl();

    public SshNetconfClientSession(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
        m_creationTime = System.currentTimeMillis();
    }

    @Override
    public synchronized NetconfResponseFuture sendRpcMessage(final String currentMessageId, Document requestDocument, final long messageTimeOut) {
        LOGGER.debug("Sending RPC request, message-id: {}", currentMessageId);
        if (m_clientSession.isClosed() || m_clientSession.isClosing()) {
            throw new NetconfSessionClosedException("Session is closed/closing, cannot send messages now");
        }
        try {
            LOGGER.debug("Session is opening, sending message-id {} ...", currentMessageId);
            DocumentToPojoTransformer.addNetconfNamespace(requestDocument, NetconfResources.NETCONF_RPC_NS_1_0);
            final byte[] temp = m_codec.encode(requestDocument);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Channel is writing, encoded document: {}", LOGGER.sensitiveData(new String(temp)));
            }
            NetconfResponseFuture futureResponse = new NetconfResponseFuture(messageTimeOut, TimeUnit.MILLISECONDS);
            m_responseFutures.put(currentMessageId, futureResponse);
            IoWriteFuture writeFuture = m_clientChannel.getAsyncIn().writePacket(new ByteArrayBuffer(temp));
            writeFuture.addListener(future -> {
                if (!future.isWritten()) {
                    try {
                        LOGGER.error("The RPC message {} could not be completed successfully, closing the session {}",
                                DocumentUtils.documentToPrettyString(requestDocument), toString());
                    } catch (NetconfMessageBuilderException e) {
                        LOGGER.error("Error while converting request document to String");
                    }
                    completeFutureAndCloseSession(futureResponse);
                }
            });
            boolean isWritten = writeFuture.await(messageTimeOut, TimeUnit.MILLISECONDS);
            if (isWritten) {
                LOGGER.debug("Channel wrote successfully");
            } else {
				LOGGER.error(
						"The RPC message {} with message-id {} could not be written to the channel for {} milliseconds, hence closing the session {}",
						DocumentUtils.documentToPrettyString(requestDocument), currentMessageId,
						String.valueOf(messageTimeOut), toString());
                completeFutureAndCloseSession(futureResponse);
            }
            return futureResponse;
        } catch (Exception e) {
            LOGGER.error("Error while sending RPC with message-id " + currentMessageId, e);
        }
        return null;
    }

    private void completeFutureAndCloseSession(NetconfResponseFuture futureResponse) {
        futureResponse.complete(null);
        try {
            closeAsync();
        } catch (Exception e) {
            LOGGER.error("Error while closing the session " + toString(), e);
        }
    }

    public NetconfMessageCodecV2 getCodec() {
        return m_codec;
    }

    public void setClientChannel(ChannelSubsystem clientChannel) {
        this.m_clientChannel = clientChannel;
    }

    public void setClientSession(ClientSession clientSession) {
        this.m_clientSession = clientSession;
        clientSession.addSessionListener(new SessionListener() {

            @Override
            public void sessionEvent(Session session, Event event) {
                LOGGER.debug("Received an event : " + event + " on session : " + session);
            }

            @Override
            public void sessionCreated(Session session) {
                LOGGER.debug("Session created : " + session);
            }

            @Override
            public void sessionClosed(Session session) {
                LOGGER.debug("Session closed : " + session);
                m_sshClient.close(true);
                SshNetconfClientSession.this.sessionClosed();
            }

            @Override
            public void sessionException(Session session, Throwable t) {
                LOGGER.debug("sessionException, session will be closed : " + session, t);
                SshNetconfClientSession.this.sessionClosed();
            }
        });
    }

    public ClientSession getClientSession() {
        return m_clientSession;
    }

    public void useChunkedFraming() {
        m_codec.useChunkedFraming();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return m_clientSession.getIoSession().getRemoteAddress();
    }

    @Override
    public void close() throws IOException {
        if (m_clientSession.isOpen()) {
            m_clientSession.close(true).await();
            LOGGER.debug("closed client session of {}", toString());
        }
        if (m_sshClient.isOpen()) {
            m_sshClient.close(true).await();
            LOGGER.debug("closed ssh client session of {}", toString());
        }
        LOGGER.debug("session {} has been closed", toString());
    }

    public void setSshClient(SshClient sshClient) {
        this.m_sshClient = sshClient;
    }

    public SshClient getSshClient() {
        return m_sshClient;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SshNetconfClientSession{");
        sb.append("localsocket=").append(LOGGER.sensitiveData(m_clientSession.getIoSession().getLocalAddress()));
        sb.append(", remotesocket=").append(LOGGER.sensitiveData(m_clientSession.getIoSession().getRemoteAddress()));
        sb.append(", creationtime=").append(LOGGER.sensitiveData(DATE_TIME_FORMATTER.print(m_creationTime)));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean isOpen() {
        if (m_clientChannel == null) {
            return false;
        }
        return m_clientChannel.isOpen();
    }

    @Override
    public long getCreationTime() {
        return m_creationTime;
    }

    @Override
    public void closeAsync(ClosureReason closureReason) {
        super.closeAsync(closureReason);
        //Close in a separate thread to make it asynchronous
        m_executorService.submit(new Callable<Void>() {
            @Override
            public Void call() {
                if (m_clientSession.isOpen()) {
                    m_clientSession.close(true);
                    LOGGER.debug("closed client session of {}", toString());
                }
                if (m_sshClient.isOpen()) {
                    m_sshClient.close(true);
                    LOGGER.debug("closed ssh client session of {}", toString());
                }
                LOGGER.info("session {} has been closed async", toString());
                return null;
            }
        });
    }

    @Override
    public void closeGracefully() throws IOException {
        if (m_clientSession.isOpen()) {
            m_clientSession.close(false).await();
            LOGGER.debug("closed gracefully client session of {}", toString());
        }
        if (m_sshClient.isOpen()) {
            m_sshClient.close(false).await();
            LOGGER.debug("closed gracefully client session of {}", toString());
        }
        LOGGER.info("session {} has been closed gracefully", toString());
    }

    @Override
    public void setTcpKeepAlive(boolean keepAlive) {
        try {
            ((Nio2Session) m_clientSession.getIoSession()).getSocket().setOption(StandardSocketOptions.SO_KEEPALIVE, keepAlive);
        } catch (IOException e) {
            LOGGER.error("Error while setting TCP keep-alive", e);
        }
    }
}
