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

package org.broadband_forum.obbaa.netconf.nc.stack.examples;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

import org.broadband_forum.obbaa.netconf.client.dispatcher.NetconfClientDispatcherImpl;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.PasswordLoginProvider;

/**
 * Example SSH Netconf Client.
 */
public class SshNetconfClient {

    public static final String USERNAME = "UT";
    public static final String PASSWORD = "UT";
    public static final int PORT = 9292;
    public static String SERVER_ADDRESS;

    public static void main(String[] args) throws Exception {
        SERVER_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        new SshNetconfClient().run();
    }

    public static Set<String> getSampleClientCaps() {
        Set<String> clientCaps = new HashSet<>();
        clientCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        clientCaps.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        return clientCaps;
    }

    private void run() throws Exception {
        NetconfClientDispatcher dispatcher = new NetconfClientDispatcherImpl();
        NetconfLoginProvider authorizationProvider = new PasswordLoginProvider(USERNAME, PASSWORD);
        NetconfTransportOrder clientTransportOrder = new NetconfTransportOrder();
        clientTransportOrder.setServerSocketAddress(new InetSocketAddress(InetAddress.getByName(SERVER_ADDRESS), PORT));
        clientTransportOrder.setTransportType(NetconfTransportProtocol.SSH.name());

        Set<String> clientCaps = getSampleClientCaps();
        LoggingClientSessionListener listener = new LoggingClientSessionListener();
        NetconfClientConfiguration clientConfig = new NetconfClientConfigurationBuilder()
                .setNetconfLoginProvider(authorizationProvider) //provide username and password login
                .setTransport(NetconfTransportFactory.makeNetconfTransport(clientTransportOrder))
                .setCapabilities(clientCaps)
                .setClientSessionListener(listener).build(); //Callback for session close event
        Future<NetconfClientSession> futureSession = dispatcher.createClient(clientConfig);
        NetconfClientSession session = futureSession.get();
        ClientUtil.runNetconfRequests(session);
        System.exit(0);
    }
}
