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

package org.broadband_forum.obbaa.netconf.client.dispatcher;

import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.TcpServerSession;
import org.broadband_forum.obbaa.netconf.api.transport.ReverseTlsNetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.SSLContextUtil;
import org.broadband_forum.obbaa.netconf.client.tls.CallhomeTlsNetconfClientInitializer;
import org.broadband_forum.obbaa.netconf.client.tls.NettyTcpServerSession;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A reverse TLS netconf client is the one that does listens to netconf servers that might "call home" on a IANA
 * assigned TCP port (not yet
 * assigned). Once the client recieves a TCP connection, it immediately starts TLS client protocol.
 * <p>
 * On successful TCP connection the reverse TLS netconf server starts TLS server protocol.
 * <p>
 * Once TLS protocol is done, both parties start their respective netconf protocols.
 * <p>
 * This dispatcher provides a way to get netconf session via call-home mechanism.
 *
 * @author Venkat
 */
public class CallhomeTlsClientDispatcherImpl extends AbstractNetconfClientDispatcher {

    private static final Logger LOGGER = Logger.getLogger(CallhomeTlsClientDispatcherImpl.class);
    private final ExecutorService m_callHomeExecutorService;

    @Deprecated
    public CallhomeTlsClientDispatcherImpl() {
        this(ExecutorServiceProvider.getInstance().getExecutorService());
    }

    public CallhomeTlsClientDispatcherImpl(ExecutorService executorService) {// NOSONAR
        this(executorService, executorService);
    }

    public CallhomeTlsClientDispatcherImpl(ExecutorService executorService, ExecutorService callHomeExecutorService)
    {// NOSONAR
        super(executorService);
        m_callHomeExecutorService = callHomeExecutorService;
    }

    @Override
    public Future<TcpServerSession> createReverseClient(final NetconfClientConfiguration config) throws
            NetconfClientDispatcherException {
        return getExecutorService().submit(new Callable<TcpServerSession>() {
            @Override
            public TcpServerSession call() throws Exception {
                return startListening(config);
            }
        });

    }

    protected TcpServerSession startListening(NetconfClientConfiguration config) throws
            NetconfClientDispatcherException {
        // Configure SSL.
        SslContext sslCtx = SSLContextUtil.getClientSSLContext(config);
        try {
            ReverseTlsNetconfTransport transport = (ReverseTlsNetconfTransport) config.getTransport();
            EventLoopGroup bossGroup = config.getEventLoopGroup();
            EventLoopGroup workerGroup = config.getEventLoopGroup();
            if (bossGroup == null || workerGroup == null) {
                EventLoopGroup group = new NioEventLoopGroup();
                bossGroup = group;
                workerGroup = group;
            }
            CallHomeListener listener = transport.getCallHomeListener();
            ServerBootstrap b = new ServerBootstrap();
            long handshakeTimeoutMillis = transport.getTlsHandshakeTimeOutMillis();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(
                            new CallhomeTlsNetconfClientInitializer(sslCtx, listener, config.getCaps(), config
                                    .getAuthenticationListener(),
                                    config.getNotificationListener(), transport.isTlsKeepalive(), transport
                                    .isSelfSigned(),
                                    getExecutorService(), getCallHomeExecutorService(), handshakeTimeoutMillis));

            Channel channel = b.bind(transport.getCallHomeIp(), Integer.valueOf(transport.getCallHomePort())).sync()
                    .channel();
            TcpServerSession tcpSession = new NettyTcpServerSession(bossGroup, workerGroup, channel);
            return tcpSession;
        } catch (Exception e) {
            throw new NetconfClientDispatcherException("Error while running reverse tls client ", e);
        }

    }

    protected ExecutorService getCallHomeExecutorService() {
        return m_callHomeExecutorService;
    }

    @Override
    protected NetconfClientSession createFutureSession(NetconfClientConfiguration config) throws
            NetconfClientDispatcherException {
        throw new NetconfClientDispatcherException("This client dispatacher supports only reverse TLS connnection");
    }

}
