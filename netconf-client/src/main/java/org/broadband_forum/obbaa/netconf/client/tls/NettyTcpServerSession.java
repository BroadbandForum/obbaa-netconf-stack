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

import org.broadband_forum.obbaa.netconf.api.client.TcpServerSession;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import org.apache.log4j.Logger;

public class NettyTcpServerSession implements TcpServerSession {
    private static final Logger LOGGER = Logger.getLogger(NettyTcpServerSession.class);
    protected EventLoopGroup m_bossGroup;
    protected EventLoopGroup m_workerGroup;
    private Channel m_tcpChannel;

    public NettyTcpServerSession(EventLoopGroup bossGroup, EventLoopGroup workerGroup, Channel tcpChannel) {
        this.m_bossGroup = bossGroup;
        this.m_workerGroup = workerGroup;
        m_tcpChannel = tcpChannel;
    }

    public void stopListening(boolean waitForTermination) {

        if (waitForTermination) {
            try {
                m_bossGroup.shutdownGracefully().sync();
                m_workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while shutting down the TCP server ", e);
            }
        } else {
            m_bossGroup.shutdownGracefully();
            m_workerGroup.shutdownGracefully();
        }
        LOGGER.info("TCP session shutting down");
    }

    @Override
    public void closeTcpChannel() {
        try {
            m_tcpChannel.close().await();
            LOGGER.info("Closing TCP Channel");
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while shutting down the TCP server ", e);
        }
    }

}
