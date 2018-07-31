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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfDelimiters;

public class CallhomeTlsNetconfClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext m_sslContext;
    static final Logger LOGGER = Logger.getLogger(CallhomeTlsNetconfClientInitializer.class);
    private final boolean m_tlsKeepalive;
    private final ExecutorService m_executorService;
    private Set<String> m_capabilities;
    private CallHomeListener m_callHomeListener;
    protected AuthenticationListener m_authenticationListener;
    private boolean m_selfSigned;
    private final ExecutorService m_callHomeExecutorService;// NOSONAR
    private final NotificationListener m_notificationListener;
    private final long m_handshakeTimeoutMillis;

    public CallhomeTlsNetconfClientInitializer(SslContext sslCtx, CallHomeListener callHomeListener, Set<String>
            clientCaps,
                                               AuthenticationListener authenticationListener, NotificationListener
                                                       notificationListener, boolean tlsKeepalive,

                                               boolean selfSigned, ExecutorService executorService, ExecutorService
                                                       callHomeExecutorService, long handshakeTimeoutMillis) {// NOSONAR
        m_sslContext = sslCtx;
        m_callHomeListener = callHomeListener;
        m_capabilities = clientCaps;
        m_authenticationListener = authenticationListener;
        m_notificationListener = notificationListener;
        m_tlsKeepalive = tlsKeepalive;
        m_selfSigned = selfSigned;
        m_executorService = executorService;
        m_callHomeExecutorService = callHomeExecutorService;
        m_handshakeTimeoutMillis = handshakeTimeoutMillis;
    }

    @Override
    protected void initChannel(final SocketChannel channel) throws Exception {
        LOGGER.debug("A netconf server is calling home on " + channel);
        channel.config().setKeepAlive(m_tlsKeepalive);
        ChannelPipeline pipeline = channel.pipeline();
        SSLEngine sslEngine = m_sslContext.newEngine(channel.alloc());
        // Add ssl handlers
        SslHandler sslHandler = new SslHandler(sslEngine);
        sslHandler.setHandshakeTimeout(m_handshakeTimeoutMillis, TimeUnit.MILLISECONDS);
        pipeline.addLast(sslHandler);
        // On top of the SSL handler, add the text line codec.
        pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, NetconfDelimiters.rpcEndOfMessageDelimiter
                ()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        sslHandler.handshakeFuture().addListener(
                new SslFutureChannelListener(sslEngine, channel, m_authenticationListener, m_selfSigned,
                        m_capabilities, m_notificationListener,
                        m_executorService, m_callHomeExecutorService, m_callHomeListener));

    }

}
