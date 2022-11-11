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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.TcpConnectionListener;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.logger.DefaultNetconfLogger;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

import io.netty.channel.EventLoopGroup;

/**
 * A builder class to build a netconf server configuration. The following example shows how to build a SSH based server configuration.
 * 
 * <pre>
 * {
 *     &#064;code
 *     HashSet&lt;String&gt; serverCaps = new HashSet&lt;String&gt;();
 *     serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
 *     NetconfTransportOrder transportOder = new NetconfTransportOrder()
 *             .setServerSocketAddress(new InetSocketAddress(InetAddress.getLocalHost(), 1212)).setServerSshHostKeyPath(&quot;hostkey.ser&quot;)
 *             .setTransportType(NetconfTransportProtocol.SSH.name());
 *     NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder()
 *             .setAuthenticationHandler(new UTAuthHandler()).setNetconfServerMessageListener(new UTServerMessageListener())
 *             .setCapabilities(serverCaps).setTransport(NetconfTransportFactory.makeNetconfTransport(transportOder));
 *     NetconfServerConfiguration serverConfig = builder.build();
 * 
 * }
 * </pre>
 * 
 * 
 * 
 */
public class NetconfServerConfigurationBuilder {

    private NetconfTransport m_netconfTransport;
    private Set<String> m_caps;
    private NetconfServerMessageListener m_netconfServerMessageListener;
    private ServerMessageHandler m_serverMessageHandler;
    private NetconfServerAuthenticationHandler m_authenticationHandler;
    private long m_connectionIdleTimeOutMillis = NetconfResources.DEFAULT_CONNECTION_TIMEOUT;
    private EventLoopGroup m_eventLoopGroup;
    private ExecutorService m_executorService;// NOSONAR
    private AuthenticationListener m_authenticationListener;
    private NetconfSessionIdProvider m_sessionIdProvider = new DefaultSessionIdProvider();
    private TcpConnectionListener m_tcpConnectionListener;
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private ServerCapabilityProvider m_capabilityProvider;
    private NetconfLogger m_logger = new DefaultNetconfLogger();

    /**
     * Builds the specified configuration.
     * 
     * @return an instance of {@link NetconfServerConfiguration} with the options specified.
     * @throws NetconfConfigurationBuilderException
     */
    public NetconfServerConfiguration build() throws NetconfConfigurationBuilderException {
        validate();
        NetconfServerConfiguration config = new NetconfServerConfiguration(m_netconfTransport, m_caps, m_capabilityProvider,
                m_netconfServerMessageListener, m_serverMessageHandler, m_authenticationHandler, m_connectionIdleTimeOutMillis,
                m_eventLoopGroup, m_executorService, m_authenticationListener, m_sessionIdProvider,
                m_logger, m_tcpConnectionListener);
        return config;
    }

    public static NetconfServerConfigurationBuilder createDefaultNcServerBuilder()
            throws UnknownHostException, NetconfConfigurationBuilderException {
        return createDefaultNcServerBuilder(NetconfResources.DEFAULT_SSH_CONNECTION_PORT);
    }

    public static NetconfServerConfigurationBuilder createDefaultNcServerBuilder(int portId)
            throws UnknownHostException, NetconfConfigurationBuilderException {
        InetSocketAddress defaultServerSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), portId);
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.SSH.name());
        transportOrder.setServerSocketAddress(defaultServerSocketAddress);
        transportOrder.setServerSshHostKeyPath(USER_HOME + FILE_SEPARATOR + "hostkey.pem");

        NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder()
                .setConnectionIdleTimeoutMillis(NetconfResources.DEFAULT_CONNECTION_TIMEOUT)
                .setTransport(NetconfTransportFactory.makeNetconfTransport(transportOrder));
        return builder;
    }

    private void validate() throws NetconfConfigurationBuilderException {
        if (m_netconfTransport == null) {
            throw new NetconfConfigurationBuilderException("netconfTransport is null");
        }
        if (m_caps == null && m_capabilityProvider == null) {
            throw new NetconfConfigurationBuilderException("caps and capability provider is null");
        }
        boolean isCHTlsTransport = m_netconfTransport.getTranportProtocol().equals(NetconfTransportProtocol.REVERSE_TLS.name());
        boolean noAuthHandler = m_authenticationHandler == null;
        if (noAuthHandler && !isCHTlsTransport) {
            throw new NetconfConfigurationBuilderException("NetconfAuthenticationHandler is null");
        }
    }

    /**
     * The type of transport to be used. See {@link NetconfTransportProtocol} to find out the currently supported transport types.
     * 
     * @param netconfTransport
     * @return modified {@link NetconfServerConfigurationBuilder} instance.
     */
    public NetconfServerConfigurationBuilder setTransport(NetconfTransport netconfTransport) {
        this.m_netconfTransport = netconfTransport;
        return this;
    }

    /**
     * Server capabilities. These capabilities will be used for {@code netconf <hello>} message exchange.
     * 
     * @param caps
     * @return
     */
    public NetconfServerConfigurationBuilder setCapabilities(Set<String> caps) {
        this.m_caps = caps;
        return this;
    }

    /**
     * Netconf server message listener. This listener is called when a new netconf message is received.
     * 
     * @param netconfServerMessageListener
     * @return modified {@link NetconfServerConfigurationBuilder} instance.
     */
    public NetconfServerConfigurationBuilder setNetconfServerMessageListener(NetconfServerMessageListener netconfServerMessageListener) {
        this.m_netconfServerMessageListener = netconfServerMessageListener;
        return this;
    }

    public ServerMessageHandler getServerMessageHandler() {
        return m_serverMessageHandler;
    }

    public ExecutorService getExecutorService() {
        return m_executorService;
    }

    /**
     * Client logins with username and password are handled as follows
     * {@link NetconfServerAuthenticationHandler#authenticate(ClientAuthenticationInfo authInfo)}
     * 
     * @param authenticationHandler
     * @return modified {@link NetconfServerConfigurationBuilder} instance.
     */
    public NetconfServerConfigurationBuilder setAuthenticationHandler(NetconfServerAuthenticationHandler authenticationHandler) {
        this.m_authenticationHandler = authenticationHandler;
        return this;
    }

    public AuthenticationListener getAuthenticationListener() {
        return m_authenticationListener;
    }

    public NetconfServerConfigurationBuilder setAuthenticationListener(AuthenticationListener authenticationListener) {
        m_authenticationListener = authenticationListener;
        return this;
    }

    /**
     * Idle timeout for a client connection.
     * 
     * @param timeoutMillis
     * @return
     */
    public NetconfServerConfigurationBuilder setConnectionIdleTimeoutMillis(long timeoutMillis) {
        this.m_connectionIdleTimeOutMillis = timeoutMillis;
        return this;
    }

    /**
     * Sets EventLoopGroup with configurable number of NIO threads for TLS connection
     * 
     * @param eventLoopGroup
     * @return
     */
    public NetconfServerConfigurationBuilder setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.m_eventLoopGroup = eventLoopGroup;
        return this;
    }

    /**
     * This is a way for clients to supply a AsynchronousChannelGroup, which is used to server requests on the SSH channel.
     * 
     * @param executorService
     * @return
     */
    public NetconfServerConfigurationBuilder setExecutorService(ExecutorService executorService) {// NOSONAR
        this.m_executorService = executorService;
        return this;
    }

    public NetconfServerConfigurationBuilder setServerMessageHandler(ServerMessageHandler serverMessageHandler) {
        this.m_serverMessageHandler = serverMessageHandler;
        return this;
    }

    public NetconfServerConfigurationBuilder setCapabilityProvider(ServerCapabilityProvider capabilityProvider) {
        m_capabilityProvider = capabilityProvider;
        return this;
    }

    public void setLogger(NetconfLogger logger) {
        m_logger = logger;
    }

    /**
     * Set a callback interface to be used when the call-home TCP connection is established.
     * 
     * @param tcpConnectionListener
     * @return
     */
    public NetconfServerConfigurationBuilder setTcpConnectionListener(TcpConnectionListener tcpConnectionListener) {
        m_tcpConnectionListener = tcpConnectionListener;
        return this;
    }

    public NetconfServerConfigurationBuilder addCapability(String capability) {
        ensureCapsNotNull();
        m_caps.add(capability);
        return this;
    }

    private void ensureCapsNotNull() {
        if(m_caps == null){
            m_caps = new LinkedHashSet<>();
        }
    }
}
