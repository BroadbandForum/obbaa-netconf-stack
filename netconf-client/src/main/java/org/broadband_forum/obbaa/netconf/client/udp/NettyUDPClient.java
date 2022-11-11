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

package org.broadband_forum.obbaa.netconf.client.udp;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.SocketUtils;

public class NettyUDPClient {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NettyUDPClient.class, LogAppNames.NETCONF_LIB);
    private final Channel m_channel;

    public NettyUDPClient(ChannelHandler channelHandler, EventLoopGroup eventLoopGroup) throws InterruptedException {
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(channelHandler);
            m_channel = b.bind(0).sync().channel();
        } catch (Exception e) {
            LOGGER.error(null, "Error while initializing UDP client channel", e);
            throw e;
        }
    }


    public void write(ByteBuf data, String host, int port) {
        DatagramPacket packet = new DatagramPacket(data, SocketUtils.socketAddress(host, port));
        //If the destination host is unreachable, no exception is thrown here, instead an intermediate
        // router may respond with a ICMP destination unreachable message
        m_channel.writeAndFlush(packet);
    }

    public void destroy() {
        if (m_channel != null && m_channel.isOpen()) {
            m_channel.close();
        }
    }
}
