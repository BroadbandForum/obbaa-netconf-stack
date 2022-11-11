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

package org.broadband_forum.obbaa.netconf.server.netty.impl;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import io.netty.channel.EventLoopGroup;

public abstract class NettyBasedServerSession implements NetconfServerSession {
    static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NettyBasedServerSession.class, LogAppNames.NETCONF_LIB);
    protected EventLoopGroup m_bossGroup;
    protected EventLoopGroup m_workerGroup;

    @Override
    public void killServer(boolean waitForTermination) {
        if (waitForTermination) {
            try {
                // FIXME: FNMS-9948 shutting down event loop group is not the right way
                m_bossGroup.shutdownGracefully().sync();
                m_workerGroup.shutdownGracefully().sync();
                LOGGER.info("Successfully shut down the server synchronously");
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while shutting down the server ", e);
            }
        } else {
            m_bossGroup.shutdownGracefully();
            m_workerGroup.shutdownGracefully();
            LOGGER.info("Successfully shut down the server asynchronously");
        }

    }

}