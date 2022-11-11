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

package org.broadband_forum.obbaa.netconf.server.udp;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;

// Generic UDP server which registers a Handler to process UDP packet
public class NettyUDPServer {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NettyUDPServer.class, LogAppNames.NETCONF_LIB);
    private Channel m_channel;
    private final ExecutorService m_executorService;
    private final NettyUDPServerConfig m_config;

    public NettyUDPServer(ExecutorService executorService, NettyUDPServerConfig config) {
        m_executorService = executorService;
        m_config = config;
    }

    //For Bean Initialization, non-static init-methods throw exception 'does not have init-method', so use this method instead
    public void startServerNoChannelReturned() {
        startServer();
    }

    public Future<Channel> startServer() {
        return m_executorService.submit(() -> {
            try {
                return startListening();
            } catch (Exception e) {
                LOGGER.error(null, "Error while starting Netty UDP server", e);
                throw e;
            }
        });
    }

    private Channel startListening() throws Exception {
        final Bootstrap b = new Bootstrap();
        b.group(m_config.getEventLoopGroup()).channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(final NioDatagramChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(m_config.getChannelHandler());
                    }
                });
        //Bind and start to accept incoming connections.
        InetAddress address = InetAddress.getByName("0.0.0.0");
        LOGGER.debug(null, "waiting for message on {}, {}", LOGGER.sensitiveData(m_config.getPort()), LOGGER.sensitiveData(address.toString()));
        m_channel = b.bind(address, m_config.getPort()).sync().channel();
        return m_channel;
    }

    public void stopServer() {
        try {
            if (m_channel != null) {
                m_channel.close().await();
            }
            m_config.getEventLoopGroup().shutdownGracefully();
            LOGGER.debug(null, "Closing netty udp server server");
        } catch (InterruptedException e) {
            LOGGER.error(null, "Interrupted while shutting down the UDP server ", e);
        }
    }
}
