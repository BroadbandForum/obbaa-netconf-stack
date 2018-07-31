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

package org.broadband_forum.obbaa.netconf.server.tls;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.authentication.FailureInfo;
import org.broadband_forum.obbaa.netconf.api.authentication.SuccessInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfDelimiters;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.server.ServerCapabilityProvider;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

public class SecureNetconfServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext m_sslCtx;
    private final boolean m_tlsKeepalive;
    private ServerCapabilityProvider m_capabilityProvider;
    private Set<String> m_caps;
    private NetconfServerMessageListener m_serverMessageListener;
    private AuthenticationListener m_authenticationListener;
    private boolean m_needClientAuth = false;
    private ServerMessageHandler m_serverMessageHandler;
    private final NetconfSessionIdProvider m_netconfSessionIdProvider;
    private NetconfLogger m_netconfLogger;
    private static final Logger LOGGER = Logger.getLogger(SecureNetconfServerInitializer.class);
    private final long m_handshakeTimeoutMillis;

    public SecureNetconfServerInitializer(SslContext sslCtx, Set<String> caps,
                                          NetconfServerMessageListener serverMessageListener, ServerMessageHandler
                                                  serverMessageHandler,

                                          AuthenticationListener authenticationListener, boolean needClientAuth,
                                          boolean tlsKeepalive,
                                          NetconfSessionIdProvider netconfSessionIdProvider, NetconfLogger
                                                  netconfLogger,
                                          long handshakeTimeoutMillis) {
        m_sslCtx = sslCtx;
        m_caps = caps;
        m_serverMessageListener = serverMessageListener;
        m_serverMessageHandler = serverMessageHandler;
        m_authenticationListener = authenticationListener;
        m_needClientAuth = needClientAuth;
        m_tlsKeepalive = tlsKeepalive;
        m_netconfSessionIdProvider = netconfSessionIdProvider;
        m_netconfLogger = netconfLogger;
        m_handshakeTimeoutMillis = handshakeTimeoutMillis;
    }

    public SecureNetconfServerInitializer(SslContext sslCtx, ServerCapabilityProvider capabilityProvider,
                                          NetconfServerMessageListener netconfServerMessageListener,
                                          ServerMessageHandler serverMessageHandler,
                                          AuthenticationListener authenticationListener, boolean
                                                  clientAuthenticationNeeded,
                                          boolean tlsKeepalive, NetconfSessionIdProvider sessionIdProvider,
                                          NetconfLogger netconfLogger,
                                          long handshakeTimeoutMillis) {
        this(sslCtx, (Set<String>) null, netconfServerMessageListener, serverMessageHandler, authenticationListener,
                clientAuthenticationNeeded, tlsKeepalive, sessionIdProvider, netconfLogger, handshakeTimeoutMillis);
        m_capabilityProvider = capabilityProvider;

    }

    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        ch.config().setKeepAlive(m_tlsKeepalive);
        SSLEngine sslEngine = m_sslCtx.newEngine(ch.alloc());
        if (m_needClientAuth) {
            sslEngine.setNeedClientAuth(true);
        }
        SslHandler sslHandler = new SslHandler(sslEngine);
        sslHandler.setHandshakeTimeout(m_handshakeTimeoutMillis, TimeUnit.MILLISECONDS);
        pipeline.addLast(sslHandler);

        // On top of the SSL handler, add the text line codec.
        pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, NetconfDelimiters.rpcEndOfMessageDelimiter
                ()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        // and then business logic.
        final int newSessionId = m_netconfSessionIdProvider.getNewSessionId();
        SecureNetconfServerHandler secureNetconfServerHandler = SecureNetconfServerHandlerFactory.getInstance()
                .getSecureNetconfServerHandler(m_serverMessageListener, m_serverMessageHandler, newSessionId,
                        m_netconfLogger);

        SecureSessionManager.getInstance().registerServerHandler(newSessionId, secureNetconfServerHandler);
        pipeline.addLast(secureNetconfServerHandler);

        // hello business, after ssl handshake is done
        sslHandler.handshakeFuture().addListener(future -> {
            try {
                if (future.isSuccess()) {
                    if (m_authenticationListener != null) {
                        InetSocketAddress remoteAddress = ((SocketChannel) ch).remoteAddress();
                        m_authenticationListener.authenticationSucceeded(new SuccessInfo()
                                .setIp(remoteAddress.getAddress().getHostAddress()).setPort(remoteAddress.getPort()));
                    }
                    // start netconf business now, send hello
                    Set<String> caps = m_caps;
                    if (m_capabilityProvider != null) {
                        caps = m_capabilityProvider.getCapabilities();
                    }

                    PojoToDocumentTransformer builder = new PojoToDocumentTransformer().newServerHelloMessage(caps,
                            newSessionId);
                    String helloString = DocumentUtils.documentToString(builder.build()) + NetconfResources
                            .RPC_EOM_DELIMITER;

                    LOGGER.info("SERVER: sending Hello : " + helloString);
                    ch.writeAndFlush(helloString);

                } else {
                    if (m_authenticationListener != null) {
                        InetSocketAddress remoteAddress = ((SocketChannel) ch).remoteAddress();
                        m_authenticationListener.authenticationFailed(new FailureInfo().setIp(remoteAddress
                                .getAddress().getHostAddress())
                                .setPort(remoteAddress.getPort()));
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Error while handling authentication, closing the channel %s", ch), e);
                ch.close();
                throw e;
            }

        });
    }

}
