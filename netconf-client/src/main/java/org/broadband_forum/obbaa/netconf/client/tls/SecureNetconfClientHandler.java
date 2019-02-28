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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CHUNKED_HANDLER;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CHUNK_SIZE;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.EOM_HANDLER;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.MAXIMUM_SIZE_OF_CHUNKED_MESSAGES;

import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.FrameAwareNetconfMessageCodec;
import org.broadband_forum.obbaa.netconf.api.FrameAwareNetconfMessageCodecImpl;
import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfDelimiters;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfHelloMessage;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;


public class SecureNetconfClientHandler extends SimpleChannelInboundHandler<String> {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SecureNetconfClientHandler.class, LogAppNames.NETCONF_LIB);
    private final Set<String> m_caps;
    private final ExecutorService m_callHomeExecutorService;
    private final CallHomeListener m_callHomeListener;
    private final X509Certificate m_peerCertificate;
    private final boolean m_selfSigned;
    private TlsNettyChannelNetconfClientSession m_clientSession;
    private AtomicBoolean m_helloRecieved = new AtomicBoolean(false);
    private boolean m_closeSession = false;
    private FrameAwareNetconfMessageCodec m_currentCodec = new FrameAwareNetconfMessageCodecImpl();

    public SecureNetconfClientHandler(TlsNettyChannelNetconfClientSession clientSession, Set<String> capabilities, 
            ExecutorService callHomeExecutorService, CallHomeListener callHomeListener, X509Certificate peerCertificate, boolean selfSigned) {
        m_clientSession = clientSession;
        m_caps = capabilities;
        m_callHomeExecutorService = callHomeExecutorService;
        m_callHomeListener = callHomeListener;
        m_peerCertificate = peerCertificate;
        m_selfSigned = selfSigned;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws NetconfMessageBuilderException {
        Document doc = null;
        try {
            doc = m_currentCodec.decode(msg);
            if (!m_helloRecieved.get()) {
                handleHelloMsg(ctx, doc);
            }
            m_clientSession.responseRecieved(doc);
        } catch (MessageToolargeException e) {
            LOGGER.warn("Closing netconf session, incoming message too long to decode", e);
            m_closeSession = true;
        } finally {
            if (m_closeSession) {
                try {
                    m_clientSession.close();
                } catch (InterruptedException e) {
                    LOGGER.error("Closing client session failed. ", e);
                }
            }
        }
    }

    private void handleHelloMsg(ChannelHandlerContext ctx, Document doc) throws NetconfMessageBuilderException {
        if (!NetconfResources.HELLO.equals(doc.getFirstChild().getNodeName())) {
            ctx.channel().close();
            ctx.flush();
        } else {
            NetconfHelloMessage hello = DocumentToPojoTransformer.getHelloMessage(doc);
            LOGGER.info("Client Received Hello message :" + hello.toString());

            if (hello.getCapabilities().contains(NetconfResources.NETCONF_BASE_CAP_1_1)
                    && this.m_caps.contains(NetconfResources.NETCONF_BASE_CAP_1_1)) {
                ctx.pipeline().replace(EOM_HANDLER, CHUNKED_HANDLER, new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, false , NetconfDelimiters.rpcChunkMessageDelimiter()));
                int chunkSize = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(CHUNK_SIZE,"65536"));
                int maxSizeOfChunkMag = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(MAXIMUM_SIZE_OF_CHUNKED_MESSAGES,"67108864"));
                m_currentCodec.useChunkedFraming(maxSizeOfChunkMag , chunkSize);
            }
            m_clientSession.setCodec(m_currentCodec);
            m_helloRecieved.set(true);
            notifyCallHomeListener(m_clientSession, m_peerCertificate);
        }
    }

    private void notifyCallHomeListener(final TlsNettyChannelNetconfClientSession session, final X509Certificate peerX509Certificate) {
        try {
            // inform call home listeners
            LOGGER.debug("Calling call-home listener to inform new call home connection with remote-address: {}",
                    LOGGER.sensitiveData(session.getRemoteAddress()));

            m_callHomeExecutorService.execute(() -> {
                try {
                    // Let the client know that some server called home and is ready to talk netconf
                    m_callHomeListener.connectionEstablished(session, null, peerX509Certificate, m_selfSigned);
                } catch (Exception e) {
                    logAndCloseChannel((SocketChannel)session.getServerChannel(), e);
                    throw e;
                }
            });
        } catch (RejectedExecutionException ree) {
            LOGGER.warn("executor service could not notify call-home listener"
                    + " about connection established for the session {}, closing the connection", LOGGER.sensitiveData(session.getRemoteAddress()));
            logAndCloseChannel((SocketChannel)session.getServerChannel(), ree);
        }
    }

    private void logAndCloseChannel(SocketChannel socketChannel, Exception e) {
        LOGGER.error("Error while handling authentication, closing the channel {}", LOGGER.sensitiveData(socketChannel), e);
        try {
            socketChannel.close().sync();
        } catch (InterruptedException e1) {
            throw new RuntimeException("Interrupted while closing channel", e1);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (m_clientSession != null) {
            m_clientSession.sessionClosed();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.debug("Exception caught : ", cause);
        ctx.close();
        if (m_clientSession != null) {
            m_clientSession.sessionClosed();
        }
    }

    public void setNetconfSession(TlsNettyChannelNetconfClientSession session) {
        this.m_clientSession = session;

    }

    public void setServerSocketChannel(SocketChannel ch) {
        m_clientSession.setServerChannel(ch);
    }
}
