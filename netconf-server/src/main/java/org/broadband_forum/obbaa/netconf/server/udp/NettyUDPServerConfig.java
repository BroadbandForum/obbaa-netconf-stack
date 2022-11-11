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

import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;

public class NettyUDPServerConfig {

    private final ChannelHandler m_channelHandler;
    private final int m_port;
    private final EventLoopGroup m_eventLoopGroup;

    public NettyUDPServerConfig(ChannelHandler channelHandler, int port,
                          EventLoopGroup eventLoopGroup) {
        m_channelHandler = channelHandler;
        m_port = port;
        m_eventLoopGroup = eventLoopGroup;
    }

    public ChannelHandler getChannelHandler() {
        return m_channelHandler;
    }

    public int getPort() {
        return m_port;
    }

    public EventLoopGroup getEventLoopGroup() {
        return m_eventLoopGroup;
    }
}
