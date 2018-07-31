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

import org.broadband_forum.obbaa.netconf.api.NetconfSessionClosedException;
import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.apache.log4j.Logger;
import org.apache.sshd.client.ClientFactoryManager;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DATE_TIME_FORMATTER;

public class SshNetconfClientSession extends AbstractNetconfClientSession {
    public static final String KEEP_ALIVE_REQ_NAME = "keep-alive";
    private ChannelSubsystem m_clientChannel;
    private boolean m_useChunkedFraming = false;
    private ClientSession m_clientSession;
    private static final Logger LOGGER = Logger.getLogger(SshNetconfClientSession.class);
    private final ExecutorService m_executorService;// NOSONAR
    private SshClient m_sshClient;
    private final long m_creationTime;

    public SshNetconfClientSession(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
        m_creationTime = System.currentTimeMillis();
    }

    @Override
    public synchronized Future<NetConfResponse> sendRpcMessage(final String currentMessageId, Document
            requestDocument, final long messageTimeOut) {
        LOGGER.debug("Sending RPC request, message-id: " + currentMessageId);
        if (m_clientSession.isClosed() || m_clientSession.isClosing()) {
            throw new NetconfSessionClosedException("Session is closed/closing, cannot send messages now");
        }
        try {
            byte[] bytesToSend;
            LOGGER.debug("Session is opening, sending...");
            DocumentToPojoTransformer.addNetconfNamespace(requestDocument, NetconfResources.NETCONF_RPC_NS_1_0);
            if (!m_useChunkedFraming) {
                bytesToSend = DocumentToPojoTransformer.addRpcDelimiter(DocumentToPojoTransformer
                        .getBytesFromDocument(requestDocument));
            } else {
                bytesToSend = DocumentToPojoTransformer.chunkMessage(ByteArrayBuffer.MAX_LEN, DocumentUtils
                        .documentToString(requestDocument))
                        .getBytes();
            }
            LOGGER.debug("Byte to be sent: " + bytesToSend + " ,isUseChunkedFraming: " + m_useChunkedFraming);
            final byte[] temp = bytesToSend;
            logRequest(requestDocument, currentMessageId);
            LOGGER.debug("Channel is writing, requestDocument: " + requestDocument);
            m_clientChannel.getAsyncIn().write(new ByteArrayBuffer(temp)).await();
            LOGGER.debug("Channel wrote successfully");
            Future<NetConfResponse> futureResponse = m_executorService.submit(new Callable<NetConfResponse>() {
                @Override
                public NetConfResponse call() throws Exception {
                    // wait for a certain time to get the message back
                    LOGGER.debug("Waiting for response...");
                    NetConfResponse response = m_rpcResponses.get(currentMessageId, messageTimeOut, TimeUnit
                            .MILLISECONDS);
                    logResponse(response, currentMessageId);
                    LOGGER.debug("Received response: " + response);
                    return response;
                }
            });
            return futureResponse;
        } catch (Exception e) {
            LOGGER.error("Error while sending a RPC message", e);
        }
        return null;
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
        this.m_useChunkedFraming = true;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return m_clientSession.getIoSession().getRemoteAddress();
    }

    @Override
    public void close() throws InterruptedException, IOException {
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
    public synchronized void sendHeartBeat(long timeout) throws InterruptedException, IOException {
        Buffer buf = m_clientSession.createBuffer(SshConstants.SSH_MSG_GLOBAL_REQUEST);
        String request = (String) m_clientSession.getFactoryManager().getProperties().get(ClientFactoryManager
                .HEARTBEAT_REQUEST);
        if (request == null) {
            request = "keepalive@bbf.org";
        }
        buf.putString(request);
        //wantReply = true
        buf.putBoolean(true);
        LOGGER.trace(String.format("sending keep-alive on channel: %s ", m_clientChannel));
        m_clientSession.request(KEEP_ALIVE_REQ_NAME, buf, timeout, TimeUnit.MILLISECONDS);
        m_clientSession.resetIdleTimeout();
        resetIdleTimeStart();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SshNetconfClientSession{");
        sb.append("localsocket=").append(m_clientSession.getIoSession().getLocalAddress());
        sb.append(", remotesocket=").append(m_clientSession.getIoSession().getRemoteAddress());
        sb.append(", creationtime=").append(DATE_TIME_FORMATTER.print(m_creationTime));
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
}
