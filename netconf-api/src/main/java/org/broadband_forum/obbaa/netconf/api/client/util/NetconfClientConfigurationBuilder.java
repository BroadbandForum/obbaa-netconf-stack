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

package org.broadband_forum.obbaa.netconf.api.client.util;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSessionListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

import io.netty.channel.EventLoopGroup;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A builder to build netconf client configuration.
 *
 * @author keshava
 */
public class NetconfClientConfigurationBuilder {
    private Long m_connectionTimeoutMillis;
    private NetconfLoginProvider m_loginProvider;
    private Set<String> m_caps;
    private NetconfTransport m_transport;
    private NetconfClientSessionListener m_clientSessionListener;
    private EventLoopGroup m_eventLoopGroup;
    private AsynchronousChannelGroup m_asyncChannelGroup;
    private AuthenticationListener m_authenticationListener;

    public NetconfClientConfiguration build() {
        NetconfClientConfiguration config = new NetconfClientConfiguration(m_connectionTimeoutMillis, m_loginProvider,
                m_caps, m_transport, m_eventLoopGroup, m_asyncChannelGroup, m_authenticationListener);
        config.setClientSessionListener(m_clientSessionListener);
        return config;
    }

    public static NetconfClientConfigurationBuilder createDefaultNcClientBuilder() throws UnknownHostException,
            NetconfConfigurationBuilderException {
        return createDefaultNcClientBuilder(NetconfResources.DEFAULT_SSH_CONNECTION_PORT);
    }

    public static NetconfClientConfigurationBuilder createDefaultNcClientBuilder(int portId) throws
            UnknownHostException,
            NetconfConfigurationBuilderException {
        InetSocketAddress defaultSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), portId);
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.SSH.name());
        transportOrder.setServerSocketAddress(defaultSocketAddress);

        NetconfClientConfigurationBuilder builder = new NetconfClientConfigurationBuilder().setTransport
                (NetconfTransportFactory
                .makeNetconfTransport(transportOrder));
        return builder;
    }

    public NetconfClientConfigurationBuilder setNetconfLoginProvider(NetconfLoginProvider loginProvider) {
        this.m_loginProvider = loginProvider;
        return this;
    }

    public NetconfClientConfigurationBuilder setAuthenticationListener(AuthenticationListener authenticationListener) {
        m_authenticationListener = authenticationListener;
        return this;
    }

    public NetconfClientConfigurationBuilder setTransport(NetconfTransport transport) {
        this.m_transport = transport;
        return this;
    }

    public NetconfClientConfigurationBuilder setConnectionTimeout(long timeout) {
        this.m_connectionTimeoutMillis = timeout;
        return this;
    }

    public NetconfClientConfigurationBuilder setCapabilities(Set<String> caps) {
        this.m_caps = caps;
        return this;
    }

    public NetconfClientConfigurationBuilder setClientSessionListener(NetconfClientSessionListener listener) {
        this.m_clientSessionListener = listener;
        return this;
    }

    public NetconfClientConfigurationBuilder setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.m_eventLoopGroup = eventLoopGroup;
        return this;
    }

    public NetconfClientConfigurationBuilder setAsynchronousChannelGroup(AsynchronousChannelGroup asyncChannelGroup) {
        this.m_asyncChannelGroup = asyncChannelGroup;
        return this;
    }

    public NetconfClientConfigurationBuilder addCapability(String capability) {
        ensureCapsNotNull();
        m_caps.add(capability);
        return this;
    }

    private void ensureCapsNotNull() {
        if (m_caps == null) {
            m_caps = new LinkedHashSet<>();
        }
    }
}
