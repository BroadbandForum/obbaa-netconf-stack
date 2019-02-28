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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CHUNK_SIZE;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DATE_TIME_FORMATTER;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.MAXIMUM_SIZE_OF_CHUNKED_MESSAGES;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.util.concurrent.CompletableFuture;
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
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.FrameAwareNetconfMessageCodec;
import org.broadband_forum.obbaa.netconf.api.FrameAwareNetconfMessageCodecImpl;
import org.broadband_forum.obbaa.netconf.api.NetconfMessageCodec;
import org.broadband_forum.obbaa.netconf.api.NetconfSessionClosedException;
import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class SshNetconfClientSession extends AbstractNetconfClientSession {
    private ChannelSubsystem m_clientChannel;
    private ClientSession m_clientSession;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshNetconfClientSession.class, LogAppNames.NETCONF_LIB);
    private final ExecutorService m_executorService;// NOSONAR
    private SshClient m_sshClient;
    private final long m_creationTime;
    private FrameAwareNetconfMessageCodec m_codec = new FrameAwareNetconfMessageCodecImpl();

    public SshNetconfClientSession(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
        m_creationTime = System.currentTimeMillis();
    }

    @Override
    public synchronized CompletableFuture<NetConfResponse> sendRpcMessage(final String currentMessageId, Document requestDocument, final long messageTimeOut) {
        LOGGER.debug("Sending RPC request, message-id: " + currentMessageId);
        if (m_clientSession.isClosed() || m_clientSession.isClosing()) {
            throw new NetconfSessionClosedException("Session is closed/closing, cannot send messages now");
        }
        try {
            LOGGER.debug("Session is opening, sending...");
            DocumentToPojoTransformer.addNetconfNamespace(requestDocument, NetconfResources.NETCONF_RPC_NS_1_0);
            final byte[] temp = m_codec.encode(requestDocument);
            LOGGER.debug("Channel is writing, requestDocument: {}", LOGGER.sensitiveData(requestDocument));
            TimeoutFutureResponse futureResponse = new TimeoutFutureResponse(messageTimeOut, TimeUnit.MILLISECONDS);
            m_responseFutures.put(currentMessageId, futureResponse);
            IoWriteFuture writeFuture = m_clientChannel.getAsyncIn().write(new ByteArrayBuffer(temp));
            writeFuture.addListener(future -> {
                if(!future.isWritten()){
                    futureResponse.complete(null);
                    LOGGER.error("The RPC with message-id {} could not be completed successfully, closing the session", currentMessageId);
                    try {
                        close();
                    } catch (IOException e) {
                        LOGGER.error("Error while closing the session", e);
                    }
                }
            });
            boolean isWritten = writeFuture.await(messageTimeOut, TimeUnit.MILLISECONDS);
            if (isWritten) {
                LOGGER.debug("Channel wrote successfully");
            } else {
                LOGGER.warn("The RPC with message-id {} could not be written to be channel", currentMessageId);
            }

            return futureResponse;
        } catch (Exception e) {
            LOGGER.error("Error while sending a RPC message", e);
        }
        return null;
    }

    public NetconfMessageCodec getCodec() {
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
        int chunkSize = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(CHUNK_SIZE, "65536"));
        int maxSizeOfChunkMag = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(MAXIMUM_SIZE_OF_CHUNKED_MESSAGES, "67108864"));
        m_codec.useChunkedFraming(maxSizeOfChunkMag, chunkSize);
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return m_clientSession.getIoSession().getRemoteAddress();
    }

    @Override
    public void close() throws IOException {
        if (!m_clientSession.isClosed()) {
            m_clientSession.close(true).await();
        }
        if (!m_sshClient.isClosed()) {
            m_sshClient.close(true).await();
        }
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
    public void closeAsync() {
        if (!m_clientSession.isClosed()) {
            m_clientSession.close(true);
        }
        if (!m_sshClient.isClosed()) {
            m_sshClient.close(true);
        }
    }

    @Override
    public void closeGracefully() throws IOException {
        if (!m_clientSession.isClosed()) {
            m_clientSession.close(false).await();
        }
        if (!m_sshClient.isClosed()) {
            m_sshClient.close(false).await();
        }
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
