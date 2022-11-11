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

package org.broadband_forum.obbaa.netconf.server.dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.server.ssh.SshServerDispatcherImpl;
import org.broadband_forum.obbaa.netconf.server.tls.CallHomeTlsServerDispatcherImpl;

/**
 * A dispatcher that runs a netconf server all supported transport types.
 * 
 * 
 * 
 */
public class NetconfServerDispatcherImpl implements NetconfServerDispatcher {

    private final ExecutorService m_executorService;// NOSONAR
    private final NetconfServerDispatcher m_sshDispatcher;
    private final NetconfServerDispatcher m_reverseTlsDispatcher;

    public NetconfServerDispatcherImpl(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
        m_sshDispatcher = new SshServerDispatcherImpl(m_executorService);
        m_reverseTlsDispatcher = new CallHomeTlsServerDispatcherImpl(m_executorService);
    }

    @Deprecated
    public NetconfServerDispatcherImpl() {
        this(ExecutorServiceProvider.getInstance().getExecutorService());
    }

    /**
     * Use this method to connect to a netconf server. The following example shows how to run a SSH based netconf server.
     * 
     * <pre>
     * {
     *     &#064;code
     *     HashSet&lt;String&gt; serverCaps = new HashSet&lt;String&gt;();
     *     serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
     *     NetconfTransportOrder transportOder = new NetconfTransportOrder()
     *             .setServerSocketAddress(new InetSocketAddress(InetAddress.getLocalHost(), 1212)).setServerSshHostKeyPath(&quot;hostkey.ser&quot;)
     *             .setTransportType(NetconfTransportProtocol.SSH.name());
     *     NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder().setAuthenticationHandler(new UTAuthHandler())
     *             .setNetconfServerMessageListener(new UTServerMessageListener()).setCapabilities(serverCaps)
     *             .setTransport(NetconfTransportFactory.makeNetconfTransport(transportOder));
     * 
     *     SshServerDispatcherImpl dispatcher = new SshServerDispatcherImpl();
     *     NetconfServerSession serverSession = dispatcher.createServer(builder.build()).get();
     * 
     *     // when you are done, shutdown the server
     *     serverSession.killServer(true);
     * }
     * </pre>
     * 
     * See SshNetconfServer for more concrete example.
     * 
     * @param config server configuration.
     *            <p>
     *            See {@link NetconfServerConfigurationBuilder} to see how to build a configuration with different transport options.
     * 
     */
    @Override
    public Future<NetconfServerSession> createServer(NetconfServerConfiguration config) throws NetconfServerDispatcherException {
        NetconfTransport transport = config.getNetconfTransport();
        if (transport.getTranportProtocol().equals(NetconfTransportProtocol.SSH.name())) {
            return m_sshDispatcher.createServer(config);
        }  else if (transport.getTranportProtocol().equals(NetconfTransportProtocol.REVERSE_TLS.name())) {
            return m_reverseTlsDispatcher.createServer(config);
        }
        return null;
    }

}
