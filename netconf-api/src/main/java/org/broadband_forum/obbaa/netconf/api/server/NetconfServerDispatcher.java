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

import java.util.concurrent.Future;

public interface NetconfServerDispatcher {

    /**
     * Use this method to run a netconf server. The following example shows how to run to a SSH based netconf server with certain set of
     * configurations.
     * 
     * <pre>
     * {@code
     *     HashSet<String> serverCaps = new HashSet<String>();
     *     serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
     *     NetconfTransportOrder transportOder = new NetconfTransportOrder()
     *                                                     .setServerSocketAddress(new InetSocketAddress(InetAddress.getLocalHost(), SERVERPORT))
     *                                                     .setServerSshHostKeyPath("hostkey.ser")
     *                                                     .setTransportType(NetconfTransportProtocol.SSH.name());
     *     NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder()
     *                                                         .setAuthenticationHandler(new UTAuthHandler()) // handler for client auth 
     *                                                         .setNetconfServerMessageListener(new UTServerMessageListener()) // listener for client messages
     *                                                         .setConnectionIdleTimeoutMillis(CONNECTIONTIMEOUT)
     *                                                         .setCapabilities(serverCaps)
     *                                                         .setTransport(NetconfTransportFactory.makeNetconfTransport(transportOder));
     *     
     *     SshServerDispatcherImpl dispatcher = new SshServerDispatcherImpl();
     *     NetconfServerSession serverSession = dispatcher.createServer(builder.build()).get();
     *     //when you are done, shutdown the server
     *     serverSession.killServer(true);
     *     
     *     }
     *  @param config
     *  @return
     * @throws NetconfServerDispatcherException
     */
    public Future<NetconfServerSession> createServer(NetconfServerConfiguration config) throws NetconfServerDispatcherException;

}
