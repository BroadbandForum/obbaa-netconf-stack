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

package org.broadband_forum.obbaa.netconf.server.ssh;

import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.server.ServerCapabilityProvider;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;

import java.util.Set;

public final class NetconfSubsystemFactory implements NamedFactory<Command> {
    private ServerCapabilityProvider m_capabilityProvider;
    private NetconfServerMessageListener m_netconfServerMessageListener;
    private ServerMessageHandler m_serverMessageHandler;
    private Set<String> m_caps;
    private final NetconfSessionIdProvider m_sessionIdProvider;

    public NetconfSubsystemFactory(NetconfServerMessageListener netconfServerMessageListener,
                                   ServerMessageHandler serverMessageHandler, Set<String> caps,
                                   NetconfSessionIdProvider sessionIdProvider) {
        m_netconfServerMessageListener = netconfServerMessageListener;
        m_serverMessageHandler = serverMessageHandler;
        m_caps = caps;
        m_sessionIdProvider = sessionIdProvider;
    }

    public NetconfSubsystemFactory(NetconfServerMessageListener serverMessageListener,
                                   ServerMessageHandler serverMessageHandler, ServerCapabilityProvider
                                           capabilityProvider,
                                   NetconfSessionIdProvider sessionIdProvider) {
        this(serverMessageListener, serverMessageHandler, (Set<String>) null, sessionIdProvider);
        m_capabilityProvider = capabilityProvider;
    }

    @Override
    public Command create() {
        if (m_capabilityProvider != null) {
            return new NetconfSubsystem(m_netconfServerMessageListener, m_serverMessageHandler, m_capabilityProvider
                    .getCapabilities(),
                    m_sessionIdProvider);
        } else {
            return new NetconfSubsystem(m_netconfServerMessageListener, m_serverMessageHandler, m_caps,
                    m_sessionIdProvider);
        }
    }

    @Override
    public String getName() {
        return NetconfResources.NETCONF_SUBSYSTEM_NAME;
    }
}