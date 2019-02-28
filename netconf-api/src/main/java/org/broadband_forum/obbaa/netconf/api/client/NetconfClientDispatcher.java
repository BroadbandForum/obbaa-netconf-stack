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

import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface NetconfClientDispatcher {

    /**
     * Use this method to connect to a netconf server. The following example shows how to connect to a SSH based netconf server.
     * 
     * <pre>
     * {
     *     &#064;code
     *     NetconfLoginProvider loginProvider = new NetconfLoginProvider() {
     *         public Login getLogin() {
     *             return new Login(&quot;UT&quot;, &quot;UT&quot;);// This is the username and password going to be used for logging into the netconf server
     *         }
     *     };
     * 
     *     // The below transport order specifies the the transport is SSH , we connecting to a server running in the localhost on port 830
     *     NetconfTransportOrder clientTransportOder = new NetconfTransportOrder().setServerSocketAddress(
     *             new InetSocketAddress(InetAddress.getLocalHost(), 830)).setTransportType(NetconfTransportProtocol.SSH.name());
     * 
     *     Set&lt;String&gt; clientCaps = new HashSet&lt;String&gt;();
     *     clientCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
     *     clientCaps.add(NetconfResources.NETCONF_BASE_CAP_1_1);
     *     NetconfClientConfiguration clientConfig = new NetconfClientConfigurationBuilder().setNetconfLoginProvider(loginProvider)// Set the
     *                                                                                                                                   // login
     *                                                                                                                                   // provider
     *             .setTransport(NetconfTransportFactory.makeNetconfTransport(clientTransportOder))// set transport
     *             .setCapabilities(clientCaps)// set client capabilities
     *             .setNetconfNotificationListener(new NetconfNotificaitonListener())// set the listener
     *             .build();
     *     Future&lt;NetconfClientSession&gt; futureSession = new NetconfClientDispatcherImpl().createClient(clientConfig);
     *     NetconfClientSession session = futureSession.get();
     *     LockRequest lockReq = new LockRequest().setTarget(&quot;running&quot;);
     *     NetConfResponse res = session.lock(lockReq).get();
     * }
     * </pre>
     * 
     * See SshClientServerTest for more concrete example.
     * 
     * @param config client configuration.
     *            <p>
     *            See {@link NetconfClientConfigurationBuilder} to see how to build a configuration with different transport options.
     * 
     * @return a Future reference to a {@link NetconfClientSession}, which can be used to send netconf messages.
     */
    public Future<NetconfClientSession> createClient(NetconfClientConfiguration config) throws NetconfClientDispatcherException;

    /**
     * Use this method to create a client that knows "call-home" protocol.
     * 
     * <pre>
     * {
     *     &#064;code
     *     NetconfClientDispatcher dispatcher = new NetconfClientDispatcherImpl();
     * 
     *     NetconfClientConfigurationBuilder clientConfigBuilder = new NetconfClientConfigurationBuilder();
     *     UTCallHomeListener listener = new UTCallHomeListener();
     *     NetconfTransportOrder transportOder = new NetconfTransportOrder()
     *             .setTransportType(NetconfTransportProtocol.REVERSE_TLS.name()).setCallHomeIp(InetAddress.getLocalHost().getHostAddress())
     *             .setCallHomePort(NetconfResources.CALL_HOME_IANA_PORT).setCallHomeListener(listener);
     * 
     *     NetconfTransport transport = NetconfTransportFactory.makeNetconfTransport(transportOder);
     *     clientConfigBuilder.setCapabilities(m_caps).setConnectionTimeout(10000L).setTransport(transport);
     *     NetconfClientConfiguration config = clientConfigBuilder.build();
     * 
     *     TcpServerSession session = dispatcher.createReverseTlsClient(config).get();
     * }
     * 
     * And when you want to stop TCP listening do the following
     * 
     * <pre>
     * {@code session.stopListening(true);}
     * </pre>
     * 
     * </pre>
     * 
     * @param config
     * @return
     * @throws NetconfClientDispatcherException
     */
    public Future<TcpServerSession> createReverseClient(NetconfClientConfiguration config) throws NetconfClientDispatcherException,ExecutionException;

}
