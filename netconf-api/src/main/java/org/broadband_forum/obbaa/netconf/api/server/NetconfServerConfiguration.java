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

package org.broadband_forum.obbaa.netconf.api.server;

import io.netty.channel.EventLoopGroup;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.TcpConnectionListener;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.logger.DefaultNetconfLogger;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;

public class NetconfServerConfiguration {

    private ServerCapabilityProvider m_capabilityProvider;
    private NetconfTransport m_netconfTransport;
    private Set<String> m_caps;
    private NetconfServerMessageListener m_netconfServerMessageListener;
    private ServerMessageHandler m_serverMessageHandler;
    private NetconfServerAuthenticationHandler m_netconfServerAuthenticationHandler;
    private long m_connectionIdleTimeoutMillis = NetconfResources.DEFAULT_CONNECTION_TIMEOUT;
    private EventLoopGroup m_eventLoopGroup;
    private SocketAddress m_localAddress;
    private ExecutorService m_executorService;// NOSONAR
    private AuthenticationListener m_authenticationListener;
    private NetconfSessionIdProvider m_sessionIdProvider = new DefaultSessionIdProvider();
    private NetconfLogger m_netconfLogger;
    private TcpConnectionListener m_tcpConnectionListener;

    public NetconfServerConfiguration() {
    }

    public NetconfServerConfiguration(NetconfTransport netconfTransport, Set<String> caps, ServerCapabilityProvider
            capabilityProvider,
                                      NetconfServerMessageListener netconfServerMessageListener, ServerMessageHandler
                                              serverMessageHandler,
                                      NetconfServerAuthenticationHandler netconfServerAuthenticationHandler, long
                                              connectionTimeOutMillis,
                                      EventLoopGroup eventLoopGroup, ExecutorService executorService,
                                      AuthenticationListener authenticationListener,// NOSONAR
                                      NetconfSessionIdProvider sessionIdProvider, NetconfLogger netconfLogger,
                                      TcpConnectionListener tcpConnectionListener) {
        m_netconfTransport = netconfTransport;
        m_caps = caps;
        m_capabilityProvider = capabilityProvider;
        m_netconfServerMessageListener = netconfServerMessageListener;
        m_serverMessageHandler = serverMessageHandler;
        m_netconfServerAuthenticationHandler = netconfServerAuthenticationHandler;
        m_connectionIdleTimeoutMillis = connectionTimeOutMillis;
        m_eventLoopGroup = eventLoopGroup;
        m_executorService = executorService;
        m_authenticationListener = authenticationListener;
        m_sessionIdProvider = sessionIdProvider;
        m_tcpConnectionListener = tcpConnectionListener;
        if (netconfLogger == null) {
            m_netconfLogger = new DefaultNetconfLogger();
        } else {
            m_netconfLogger = netconfLogger;
        }
    }

    public NetconfTransport getNetconfTransport() {
        return m_netconfTransport;
    }

    public void setNetconfTransport(NetconfTransport netconfTransport) {
        this.m_netconfTransport = netconfTransport;
    }

    public Set<String> getCaps() {
        return m_caps;
    }

    public AuthenticationListener getAuthenticationListener() {
        return m_authenticationListener;
    }

    public void setCaps(Set<String> caps) {
        this.m_caps = caps;
    }

    public NetconfServerMessageListener getNetconfServerMessageListener() {
        return m_netconfServerMessageListener;
    }

    public void setNetconfServerMessageListener(NetconfServerMessageListener axsNetconfServerMessageListener) {
        this.m_netconfServerMessageListener = axsNetconfServerMessageListener;
    }

    public NetconfServerAuthenticationHandler getNetconfServerAuthenticationHandler() {
        return m_netconfServerAuthenticationHandler;
    }

    public void setNetconfServerAuthenticationHandler(NetconfServerAuthenticationHandler NetconfAuthenticationHandler) {
        this.m_netconfServerAuthenticationHandler = NetconfAuthenticationHandler;
    }

    public long getConnectionIdleTimeOutMillis() {
        return this.m_connectionIdleTimeoutMillis;
    }

    public void setConnectionIdleTimeOutMillis(long newTimeout) {
        this.m_connectionIdleTimeoutMillis = newTimeout;
    }

    public EventLoopGroup getEventLoopGroup() {
        return m_eventLoopGroup;
    }

    public SocketAddress getLocalAddress() {
        return m_localAddress;
    }

    public void setLocalAddress(SocketAddress localAddress) {
        m_localAddress = localAddress;
    }

    public ExecutorService getExecutorService() {
        return m_executorService;
    }

    public void setExecutorService(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
    }

    public ServerMessageHandler getServerMessageHandler() {
        return m_serverMessageHandler;
    }

    public void setServerMessageHandler(ServerMessageHandler serverMessageHandler) {
        m_serverMessageHandler = serverMessageHandler;
    }

    public NetconfSessionIdProvider getSessionIdProvider() {
        return m_sessionIdProvider;
    }

    public void setNetconfLogger(NetconfLogger netconfLogger) {
        m_netconfLogger = netconfLogger;
    }

    public NetconfLogger getNetconfLogger() {
        return m_netconfLogger;
    }

    @Override
    public String toString() {
        return "NetconfServerConfiguration{" + "m_capabilityProvider=" + m_capabilityProvider + ", " +
                "m_netconfTransport=" + m_netconfTransport
                + ", m_caps=" + m_caps + ", m_netconfServerMessageListener=" + m_netconfServerMessageListener + ", " +
                "m_serverMessageHandler="
                + m_serverMessageHandler + ", m_netconfServerAuthenticationHandler=" +
                m_netconfServerAuthenticationHandler
                + ", m_connectionIdleTimeoutMillis=" + m_connectionIdleTimeoutMillis + ", m_eventLoopGroup=" +
                m_eventLoopGroup
                + ", m_localAddress=" + m_localAddress + ", m_executorService=" + m_executorService + ", " +
                "m_authenticationListener="
                + m_authenticationListener + ", m_sessionIdProvider=" + m_sessionIdProvider + '}';
    }

    public ServerCapabilityProvider getCapabilityProvider() {
        return m_capabilityProvider;
    }

    public void setCapabilityProvider(ServerCapabilityProvider capabilityProvider) {
        m_capabilityProvider = capabilityProvider;
    }

    public TcpConnectionListener getTcpConnectionListener() {
        return m_tcpConnectionListener;
    }

    public void setTcpConnectionListener(TcpConnectionListener tcpConnectionListener) {
        this.m_tcpConnectionListener = tcpConnectionListener;
    }
}
