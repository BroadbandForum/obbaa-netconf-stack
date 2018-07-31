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

package org.broadband_forum.obbaa.netconf.api.client;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;

import io.netty.channel.EventLoopGroup;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.Set;

/**
 * Netconf client configuration. Use {@link NetconfClientConfigurationBuilder} to build a configuration object.
 *
 * @author keshava
 */
public class NetconfClientConfiguration {

    private final Long m_connectionTimeoutMillis;
    private final NetconfLoginProvider m_loginProvider;
    private final Set<String> m_caps;
    private NotificationListener m_notificationListener;
    private final NetconfTransport m_transport;
    private NetconfClientSessionListener m_clientSessionListener;
    private final EventLoopGroup m_eventLoopGroup;
    private AsynchronousChannelGroup m_asynChannelGroup;
    private AuthenticationListener m_authenticationListener;

    public NetconfClientConfiguration(Long connectionTimeoutMillis, NetconfLoginProvider loginProvider, Set<String>
            caps,
                                      NetconfTransport transport, EventLoopGroup eventLoopGroup,
                                      AsynchronousChannelGroup asynChannelGroup, AuthenticationListener
                                              authenticationListener) {
        this.m_connectionTimeoutMillis = connectionTimeoutMillis;
        this.m_loginProvider = loginProvider;
        this.m_caps = caps;
        this.m_transport = transport;
        this.m_eventLoopGroup = eventLoopGroup;
        this.m_asynChannelGroup = asynChannelGroup;
        this.m_authenticationListener = authenticationListener;
    }

    public Long getConnectTimeoutMillis() {
        return m_connectionTimeoutMillis;
    }

    public AuthenticationListener getAuthenticationListener() {
        return m_authenticationListener;

    }

    /**
     * This provider will be used for login to the remote netconf server.
     *
     * @return
     */
    public NetconfLoginProvider getNetconfLoginProvider() {
        return m_loginProvider;
    }

    /**
     * Client's capabilities. These will be used during netconf {@code<hello>} message exchange.
     *
     * @return
     */
    public Set<String> getCaps() {
        return m_caps;
    }

    public NotificationListener getNotificationListener() {
        return m_notificationListener;
    }

    public void setNotificationListener(NotificationListener notificationListener) {
        this.m_notificationListener = notificationListener;
    }

    /**
     * The transport to be used for the netconf connection. Following is an example to create a transport using the
     * factory
     * {@link NetconfTransportFactory} and {@link NetconfTransportOrder}.
     * <p>
     * <p>
     * <pre>
     * {
     *     &#064;code
     *     NetconfTransportOrder transportOder = new NetconfTransportOrder().setServerSocketAddress(expectedSockAddr)
     *     .setTransportType(
     *             NetconfTransportProtocol.SSH.name());
     * }
     * </pre>
     *
     * @return
     */
    public NetconfTransport getTransport() {
        return m_transport;
    }

    public void setClientSessionListener(NetconfClientSessionListener listener) {
        this.m_clientSessionListener = listener;
    }

    public NetconfClientSessionListener getClientSessionListener() {
        return this.m_clientSessionListener;
    }

    public EventLoopGroup getEventLoopGroup() {
        return m_eventLoopGroup;
    }

    public AsynchronousChannelGroup getAsynchronousChannelGroup() {
        return m_asynChannelGroup;
    }

    public void setAsynchronousChannelGroup(AsynchronousChannelGroup asynChannelGroup) {
        this.m_asynChannelGroup = asynChannelGroup;
    }

    @Override
    public String toString() {
        return "NetconfClientConfiguration{" + "m_connectionTimeoutMillis=" + m_connectionTimeoutMillis +
                ", m_loginProvider=" + m_loginProvider + ", m_caps=" + m_caps + ", m_notificationListener="
                + m_notificationListener + ", m_transport=" + m_transport + ", m_clientSessionListener=" +
                m_clientSessionListener
                + ", m_eventLoopGroup=" + m_eventLoopGroup + ", m_asynChannelGroup=" + m_asynChannelGroup + ", " +
                "m_authenticationListener="
                + m_authenticationListener + '}';
    }

}
