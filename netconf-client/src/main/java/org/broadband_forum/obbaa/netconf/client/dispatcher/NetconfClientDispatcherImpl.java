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

package org.broadband_forum.obbaa.netconf.client.dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.sshd.common.util.SecurityUtils;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.TcpServerSession;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;

/**
 * A dispatcher that returns a client session for all supported transport types.
 *
 * @author keshava
 */
public class NetconfClientDispatcherImpl implements NetconfClientDispatcher {

    private NetconfClientDispatcher m_sshClientDispatcher;
    private NetconfClientDispatcher m_callhomeTlsClientDispatcher;

    static {
        // register bouncy castle as apache sshd security provider        
        SecurityUtils.setRegisterBouncyCastle(true);
        /**
         * SecurityUtils does not expose register() as public method. 
         * need to call isBouncyCastleRegistered() which in-turn register egarly
         */
        SecurityUtils.isBouncyCastleRegistered();
    }

    /**
     * For UTs only
     *
     * @param sshClientDispatcher
     */
    public NetconfClientDispatcherImpl(SshClientDispatcherImpl sshClientDispatcher) {
        this(ExecutorServiceProvider.getInstance().getExecutorService());
        this.m_sshClientDispatcher = sshClientDispatcher;
    }

    @Deprecated
    public NetconfClientDispatcherImpl() {
        this(ExecutorServiceProvider.getInstance().getExecutorService());
    }

    public NetconfClientDispatcherImpl(ExecutorService executorService) {// NOSONAR
        this(executorService, executorService);
    }

    public NetconfClientDispatcherImpl(ExecutorService executorService, ExecutorService callHomeExecutorService) {//
        // NOSONAR
        m_sshClientDispatcher = new SshClientDispatcherImpl(executorService);
        m_callhomeTlsClientDispatcher = new CallhomeTlsClientDispatcherImpl(executorService, callHomeExecutorService);
    }

    /**
     * Use this method to connect to a netconf server. The following example shows how to connect to a SSH based
     * netconf server.
     * <p>
     * <pre>
     * {
     *     &#064;code
     *     NetconfLoginProvider loginProvider = new NetconfLoginProvider() {
     *         public Login getLogin() {
     *             return new Login(&quot;UT&quot;, &quot;UT&quot;);// This is the username and password going to be
     *             used for logging into the netconf server
     *         }
     *     };
     *
     *     // The below transport order specifies the the transport is SSH , we connecting to a server running in the
     *     localhost on port 830
     *     NetconfTransportOrder clientTransportOder = new NetconfTransportOrder().setServerSocketAddress(
     *             new InetSocketAddress(InetAddress.getLocalHost(), 830)).setTransportType(NetconfTransportProtocol
     *             .SSH.name());
     *
     *     Set&lt;String&gt; clientCaps = new HashSet&lt;String&gt;();
     *     clientCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
     *     clientCaps.add(NetconfResources.NETCONF_BASE_CAP_1_1);
     *     NetconfClientConfiguration clientConfig = new NetconfClientConfigurationBuilder().setNetconfLoginProvider
     *     (loginProvider)// Set the
     *
     *                                                                                                               // login
     *
     *                                                                                                               // provider
     *             .setTransport(NetconfTransportFactory.makeNetconfTransport(clientTransportOder))// set transport
     *             .setCapabilities(clientCaps)// set client capabilities
     *             .setNetconfNotificationListener(new NetconfNotificaitonListener())// set the listener
     *             .build();
     *     Future&lt;NetconfClientSession&gt; futureSession = new NetconfClientDispatcherImpl().createClient
     *     (clientConfig);
     *     NetconfClientSession session = futureSession.get();
     *     LockRequest lockReq = new LockRequest().setTarget(&quot;running&quot;);
     *     NetConfResponse res = session.lock(lockReq).get();
     * }
     * </pre>
     *
     * @param config client configuration.
     *               <p>
     *               See {@link NetconfClientConfigurationBuilder} to see how to build a configuration with different
     *               transport options.
     * @return a Future reference to a {@link NetconfClientSession}, which can be used to send netconf messages.
     * @throws NetconfClientDispatcherException
     */
    @Override
    public Future<NetconfClientSession> createClient(NetconfClientConfiguration config) throws
            NetconfClientDispatcherException {
        NetconfTransport transport = config.getTransport();
        if (transport.getTranportProtocol().equals(NetconfTransportProtocol.SSH.name())) {
            return m_sshClientDispatcher.createClient(config);
        } else if (transport.getTranportProtocol().equals(NetconfTransportProtocol.REVERSE_TLS.name())) {
            throw new NetconfClientDispatcherException(
                    "Cannot get Call Home connection via this interface use createReverseTlsClient() instead");
        }
        return null;
    }

    @Override
    public Future<TcpServerSession> createReverseClient(NetconfClientConfiguration config) throws
            NetconfClientDispatcherException {
        NetconfTransport transport = config.getTransport();
        if (transport.getTranportProtocol().equals(NetconfTransportProtocol.REVERSE_TLS.name())) {
            return m_callhomeTlsClientDispatcher.createReverseClient(config);
        } else {
            throw new NetconfClientDispatcherException(
                    "Can only get Call Home connection via this interface use createClient() for other transport " +
                            "types");
        }

    }

}
