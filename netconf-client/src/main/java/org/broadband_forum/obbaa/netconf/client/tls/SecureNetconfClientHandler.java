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

import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.reInitializeByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.releaseByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CHUNKED_HANDLER;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.EOM_HANDLER;

import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.codec.v2.FrameAwareNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.codec.v2.FrameAwareNetconfMessageCodecV2Impl;
import org.broadband_forum.obbaa.netconf.api.codec.v2.NetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfDelimiters;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfHelloMessage;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;

import io.netty.buffer.ByteBuf;
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
    private AtomicBoolean m_helloReceived = new AtomicBoolean(false);
    private boolean m_closeSession = false;
    private FrameAwareNetconfMessageCodecV2 m_currentCodec = new FrameAwareNetconfMessageCodecV2Impl();
    private ByteBuf m_byteBuf;

    public SecureNetconfClientHandler(TlsNettyChannelNetconfClientSession clientSession, Set<String> capabilities,
                                      ExecutorService callHomeExecutorService, CallHomeListener callHomeListener, X509Certificate peerCertificate, boolean selfSigned) {
        m_clientSession = clientSession;
        m_caps = capabilities;
        m_callHomeExecutorService = callHomeExecutorService;
        m_callHomeListener = callHomeListener;
        m_peerCertificate = peerCertificate;
        m_selfSigned = selfSigned;
        m_byteBuf = unpooledHeapByteBuf();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        m_byteBuf.writeBytes(msg.getBytes());
        try {
            boolean readFurther = true;
            while (readFurther) {
                DocumentInfo documentInfo = m_currentCodec.decode(m_byteBuf);
                if (documentInfo != null && documentInfo.getDocument() != null) {
                    if (!m_helloReceived.get()) {
                        handleHelloMsg(ctx, documentInfo);
                    } else {
                        m_clientSession.responseRecieved(documentInfo);
                    }
                } else {
                    readFurther = false;
                }
            }
        } catch(NetconfMessageBuilderException e){
            if (!m_helloReceived.get()) {
                m_closeSession = true;
                LOGGER.error("Closing netconf session as hello received is malformed", e);
            } else {
                LOGGER.error("Error - got malformed XML from {}", m_clientSession, e);
            }
        } catch (Exception e) {
            LOGGER.error("Error while processing message, closing the session ", e);
            m_closeSession = true;
        } finally {
            m_byteBuf = reInitializeByteBuf(m_byteBuf);
            if (m_closeSession) {
                try {
                    m_clientSession.close();
                    releaseByteBuf(m_byteBuf);
                } catch (InterruptedException e) {
                    LOGGER.error("Closing client session failed. ", e);
                }
            }
        }
    }
    private void handleHelloMsg(ChannelHandlerContext ctx, DocumentInfo documentInfo) throws NetconfMessageBuilderException {
        Document doc = documentInfo.getDocument();
        if (!NetconfResources.HELLO.equals(doc.getFirstChild().getNodeName())) {
            ctx.channel().close();
            ctx.flush();
        } else {
            NetconfHelloMessage hello = DocumentToPojoTransformer.getHelloMessage(doc);
            LOGGER.debug("Client Received Hello message :" + hello.toString());

            if (hello.getCapabilities().contains(NetconfResources.NETCONF_BASE_CAP_1_1)
                    && this.m_caps.contains(NetconfResources.NETCONF_BASE_CAP_1_1)) {
                ctx.pipeline().replace(EOM_HANDLER, CHUNKED_HANDLER, new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, false , NetconfDelimiters.rpcChunkMessageDelimiter()));
                m_currentCodec.useChunkedFraming();
            }
            m_clientSession.setCodec(m_currentCodec);
            m_helloReceived.set(true);
            m_clientSession.responseRecieved(documentInfo);
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
            releaseByteBuf(m_byteBuf);
        } catch (InterruptedException e1) {
            throw new RuntimeException("Interrupted while closing channel", e1);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (m_clientSession != null) {
            m_clientSession.sessionClosed();
            releaseByteBuf(m_byteBuf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.debug("Exception caught : ", cause);
        ctx.close();
        if (m_clientSession != null) {
            m_clientSession.sessionClosed();
        }
        releaseByteBuf(m_byteBuf);
    }

    public void setNetconfSession(TlsNettyChannelNetconfClientSession session) {
        this.m_clientSession = session;

    }

    public void setServerSocketChannel(SocketChannel ch) {
        m_clientSession.setServerChannel(ch);
    }

    @VisibleForTesting
    public NetconfMessageCodecV2 getCodec() {
        return m_currentCodec.currentCodec();
    }

    @VisibleForTesting
    public ByteBuf getByteBuf() {
        return m_byteBuf;
    }
}
