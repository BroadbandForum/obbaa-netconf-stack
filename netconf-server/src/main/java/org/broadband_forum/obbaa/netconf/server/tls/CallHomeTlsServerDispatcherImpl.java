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

import org.broadband_forum.obbaa.netconf.api.TcpConnectionListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.transport.ReverseTlsNetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.SSLContextUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A reverse TLS netconf server is the one that does "call home" to a pre-configured netconf client. The netconf client keeps listening on
 * incoming TCP connections on a IANA assigned port (not yet assigned). Once the client recieves a TCP connection, it immediately starts TLS
 * client protocol.
 * 
 * On successful TCP connection the reverse TLS netconf server starts TLS server protocol.
 * 
 * Once TLS protocol is done, both parties start their respective netconf protocols.
 * 
 * This dispatcher provides a way to get netconf session via call-home mechanism.
 * 
 *
 */
public class CallHomeTlsServerDispatcherImpl implements NetconfServerDispatcher {
    private static final Logger LOGGER = Logger.getLogger(CallHomeTlsServerDispatcherImpl.class);
    private final ExecutorService m_executorService;// NOSONAR

    public CallHomeTlsServerDispatcherImpl(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
    }

    @Deprecated
    public CallHomeTlsServerDispatcherImpl() {
        this(ExecutorServiceProvider.getInstance().getExecutorService());
    }

    @Override
    public Future<NetconfServerSession> createServer(final NetconfServerConfiguration config) throws NetconfServerDispatcherException {
        if (config.getNetconfTransport() instanceof ReverseTlsNetconfTransport) {
            return getExecutorService().submit(new Callable<NetconfServerSession>() {
                @Override
                public NetconfServerSession call() throws Exception {
                    return createAndReturnServerSession(config);
                }
            });
        }
        return null;
    }

    protected NetconfServerSession createAndReturnServerSession(NetconfServerConfiguration config) throws NetconfServerDispatcherException {
        SslContext sslCtx = null;

        sslCtx = SSLContextUtil.getServerSSLContext(config);
        Bootstrap b = new Bootstrap();
        EventLoopGroup bossGroup = config.getEventLoopGroup();
        EventLoopGroup workerGroup = config.getEventLoopGroup();
        if (bossGroup == null || workerGroup == null) {
            EventLoopGroup group = new NioEventLoopGroup();
            bossGroup = group;
            workerGroup = group;
        }
        TlsNetconfServerSession serverSession = new TlsNetconfServerSession(bossGroup, workerGroup);
        SecureNetconfServerInitializer handler = null;

        ReverseTlsNetconfTransport transport = (ReverseTlsNetconfTransport) config.getNetconfTransport();
        String netconfClientIP = transport.getCallHomeIp();
        int netconfClientTCPPort = transport.getCallHomePort();
        SocketAddress localAddress = transport.getLocalAddress();

        long handshakeTimeoutMillis = transport.getTlsHandshakeTimeOutMillis();
        if (config.getCapabilityProvider() != null) {
            handler = new SecureNetconfServerInitializer(sslCtx, config.getCapabilityProvider(), config.getNetconfServerMessageListener(),
                    config.getServerMessageHandler(), config.getAuthenticationListener(), transport.isClientAuthenticationNeeded(),
                    transport.isTlsKeepalive(), config.getSessionIdProvider(), config.getNetconfLogger(), handshakeTimeoutMillis);
        } else {
            handler = new SecureNetconfServerInitializer(sslCtx, config.getCaps(), config.getNetconfServerMessageListener(),
                    config.getServerMessageHandler(), config.getAuthenticationListener(), transport.isClientAuthenticationNeeded(),
                    transport.isTlsKeepalive(), config.getSessionIdProvider(), config.getNetconfLogger(), handshakeTimeoutMillis);
        }

        b.group(bossGroup).channel(NioSocketChannel.class).handler(handler);
        if (transport.getLocalAddress() != null) {
            b.localAddress(transport.getLocalAddress());
        }
        // Start the connection attempt.
        Channel ch;
        try {
            if (localAddress != null) {
                ch = b.connect(new InetSocketAddress(netconfClientIP, netconfClientTCPPort), localAddress).sync().channel();
            } else {
                ch = b.connect(netconfClientIP, netconfClientTCPPort).sync().channel();
            }
        } catch (Exception e) {
            LOGGER.error("Exception while starting server :  " + transport, e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            throw new NetconfServerDispatcherException("Exception while starting server : " + transport, e);
        }
        if (ch == null) {
            return null;
        } else {
            TcpConnectionListener tcpConnectionListener = config.getTcpConnectionListener();
            if (tcpConnectionListener != null) {
                tcpConnectionListener.connected(ch.localAddress());
            }
            SslHandler sslHandler = ch.pipeline().get(SslHandler.class);
            // this means the handshake might have already failed
            if (sslHandler == null) {
                return null;
            }
            // handshake succeeded or going on...
            io.netty.util.concurrent.Future<Channel> handshakeFuture = sslHandler.handshakeFuture();
            if (!handshakeFuture.isDone()) {
                try {
                    handshakeFuture.await();// wait for handshake to happen
                } catch (Exception e) {
                    LOGGER.error("Exception while waiting for SSL handshake", e);
                    // bossGroup.shutdownGracefully();
                    // workerGroup.shutdownGracefully();
                    throw new NetconfServerDispatcherException("interrupted while waiting for SSL handshake", e);
                }
            }

            if (handshakeFuture.isSuccess()) {
                serverSession.setChannel(ch);
                return serverSession;
            } else {
                LOGGER.error("Handshaking is failed..", handshakeFuture.cause());
                // bossGroup.shutdownGracefully();
                // workerGroup.shutdownGracefully();
                return null;
            }
        }

    }

    public ExecutorService getExecutorService() {// NOSONAR
        return m_executorService;
    }
}
