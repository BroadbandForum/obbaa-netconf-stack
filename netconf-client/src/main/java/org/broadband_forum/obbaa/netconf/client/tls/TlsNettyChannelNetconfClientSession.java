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

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.FrameAwareNetconfMessageCodec;
import org.broadband_forum.obbaa.netconf.api.FrameAwareNetconfMessageCodecImpl;
import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;

public class TlsNettyChannelNetconfClientSession extends AbstractNetconfClientSession {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(TlsNettyChannelNetconfClientSession.class, LogAppNames.NETCONF_LIB);
    private final long m_creationTime;
    private SocketChannel m_serverChannel;
    private FrameAwareNetconfMessageCodec m_codec = new FrameAwareNetconfMessageCodecImpl();

    public TlsNettyChannelNetconfClientSession(SocketChannel ch) {// NOSONAR
        m_serverChannel = ch;
        m_creationTime = System.currentTimeMillis();
    }

    public synchronized ChannelFuture sendHelloMessage(Set<String> caps) {

        String helloString = null;
        try {
            PojoToDocumentTransformer builder = new PojoToDocumentTransformer().newClientHelloMessage(caps);
            Document doc = builder.build();
            helloString = new String(m_codec.encode(doc));
            LOGGER.debug("CLIENT sending Hello message : {}", LOGGER.sensitiveData(helloString));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return m_serverChannel.writeAndFlush(helloString);
    }

    @Override
    public synchronized CompletableFuture<NetConfResponse> sendRpcMessage(final String currentMessageId, Document requestDocument, final long messageTimeOut) {
        String xmlString = "";
        try {
            xmlString = new String(m_codec.encode(requestDocument));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        m_serverChannel.writeAndFlush(xmlString);

        TimeoutFutureResponse future = new TimeoutFutureResponse(messageTimeOut, TimeUnit.MILLISECONDS);
        m_responseFutures.put(currentMessageId, future);
        return future;
    }

    public Channel getServerChannel() {
        return m_serverChannel;
    }

    public void setServerChannel(SocketChannel serverChannel) {
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
    public void setTcpKeepAlive(boolean keepAlive) {
        try {
            m_serverChannel.config().setKeepAlive(keepAlive);
        } catch (Exception e) {
            LOGGER.error("Error occurred while setting TCP keep-alive", e);
        }
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
        sb.append("localsocket=").append(LOGGER.sensitiveData(m_serverChannel.localAddress()));
        sb.append(", remotesocket=").append(LOGGER.sensitiveData(m_serverChannel.remoteAddress()));
        sb.append(", creationtime=").append(LOGGER.sensitiveData(NetconfResources.DATE_TIME_FORMATTER.print(m_creationTime)));
        sb.append('}');
        return sb.toString();
    }

    public void setCodec(FrameAwareNetconfMessageCodec codec) {
        m_codec = codec;
    }
}
