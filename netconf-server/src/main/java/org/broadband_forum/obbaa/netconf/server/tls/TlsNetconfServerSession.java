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

import org.broadband_forum.obbaa.netconf.server.netty.impl.NettyBasedServerSession;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.apache.log4j.Logger;

public class TlsNetconfServerSession extends NettyBasedServerSession {
    private Channel m_channel;
    private static final Logger LOGGER = Logger.getLogger(TlsNetconfServerSession.class);

    public TlsNetconfServerSession(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.m_bossGroup = bossGroup;
        this.m_workerGroup = workerGroup;
    }

    @Override
    public void killServer(boolean waitForTermination) {
        try {
            // in case of regular tls nc server channel is not set
            if (m_channel != null) {
                m_channel.close().sync();
                LOGGER.info("Channel is not null and closed");
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while closing the TLS channel", e);
        }
        LOGGER.info("Channel is already closed");
        super.killServer(waitForTermination);
    }

    public void setChannel(Channel ch) {
        this.m_channel = ch;

    }

    protected Channel getChannel() {
        return m_channel;
    }
    public boolean isOpen() {
        if(m_channel==null){
            return false;
        }
        return m_channel.isOpen();
    }

    @Override
    public String toString() {
        return "TlsNetconfServerSession [m_channel_id=" + (m_channel == null ? null : m_channel.id()) + "]";
    }
}
