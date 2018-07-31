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

package org.broadband_forum.obbaa.netconf.client.tls;

import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfDelimiters;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TlsNettyChannelNetconfClientSession extends AbstractNetconfClientSession {

    private static final Logger LOGGER = Logger.getLogger(TlsNettyChannelNetconfClientSession.class);
    private final long m_creationTime;
    private Channel m_serverChannel;
    private final ExecutorService m_executorService;// NOSONAR

    public TlsNettyChannelNetconfClientSession(Channel ch, ExecutorService executorService) {// NOSONAR
        m_serverChannel = ch;
        m_executorService = executorService;
        m_creationTime = System.currentTimeMillis();
    }

    public synchronized ChannelFuture sendHelloMessage(Set<String> caps) {

        String helloString = null;
        try {
            PojoToDocumentTransformer builder = new PojoToDocumentTransformer().newClientHelloMessage(caps);
            helloString = DocumentUtils.documentToString(builder.build()) + NetconfResources.RPC_EOM_DELIMITER;
            LOGGER.debug("CLIENT sending Hello message : " + helloString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return m_serverChannel.writeAndFlush(helloString);
    }

    @Override
    public synchronized Future<NetConfResponse> sendRpcMessage(final String currentMessageId, Document
            requestDocument, final long messageTimeOut) {
        String xmlString = "";
        try {
            xmlString = DocumentUtils.documentToString(requestDocument) + NetconfDelimiters
                    .rpcEndOfMessageDelimiterString();
            logRequest(requestDocument, currentMessageId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        m_serverChannel.writeAndFlush(xmlString);

        Future<NetConfResponse> futureResponse = m_executorService.submit(new Callable<NetConfResponse>() {
            @Override
            public NetConfResponse call() throws Exception {
                // wait for a certain time to get the message back
                NetConfResponse response = m_rpcResponses.get(currentMessageId, messageTimeOut, TimeUnit.MILLISECONDS);
                logResponse(response, currentMessageId);
                return response;
            }
        });

        return futureResponse;
    }

    public Channel getServerChannel() {
        return m_serverChannel;
    }

    public void setServerChannel(Channel serverChannel) {
        m_serverChannel = serverChannel;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return m_serverChannel.remoteAddress();
    }

    @Override
    public void close() throws InterruptedException {
        if (m_serverChannel.isOpen()) {
            m_serverChannel.close().sync();
        }
    }

    @Override
    public synchronized void sendHeartBeat(long timeout) {
        LOGGER.warn("TLS client session does not support heartbeats yet");
    }

    @Override
    public boolean isOpen() {
        if (m_serverChannel == null) {
            return false;
        }
        return m_serverChannel.isOpen();
    }

    @Override
    public long getCreationTime() {
        return m_creationTime;
    }

    @Override
    public void closeAsync() {
        if (m_serverChannel.isOpen()) {
            m_serverChannel.close();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TlsNettyChannelNetconfClientSession{");
        sb.append("localsocket=").append(m_serverChannel.localAddress());
        sb.append(", remotesocket=").append(m_serverChannel.remoteAddress());
        sb.append(", creationtime=").append(NetconfResources.DATE_TIME_FORMATTER.print(m_creationTime));
        sb.append('}');
        return sb.toString();
    }
}
