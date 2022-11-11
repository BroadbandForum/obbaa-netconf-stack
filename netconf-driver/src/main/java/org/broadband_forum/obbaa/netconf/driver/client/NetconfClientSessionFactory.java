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

package org.broadband_forum.obbaa.netconf.driver.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.client.dispatcher.SshClientDispatcherImpl;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.KeyLoginProvider;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.PasswordLoginProvider;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class NetconfClientSessionFactory {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NetconfClientSessionFactory.class, LogAppNames.NETCONF_LIB);
    private static NetconfClientDispatcher c_dispatcher = new SshClientDispatcherImpl(Executors.newSingleThreadExecutor());

    public static NetconfClientSession createSSHClientSession(String ip, int port, int heartbeatInterval, String username, String password,
            Set<String> clientCaps) throws UnknownHostException, NetconfConfigurationBuilderException, NetconfClientDispatcherException,
            InterruptedException, ExecutionException {
        NetconfClientConfiguration clientConfig = null;
        NetconfTransportOrder clientTransportOder;
        clientTransportOder = getSshTrasnsportOrder(ip, port, heartbeatInterval);

        try {
            clientConfig = new NetconfClientConfigurationBuilder().setNetconfLoginProvider(new PasswordLoginProvider(username, password))
                    .setTransport(NetconfTransportFactory.makeNetconfTransport(clientTransportOder)).setCapabilities(clientCaps)
                    .setConnectionTimeout(Long.MAX_VALUE)
                    .setAsynchronousChannelGroup(AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool())).build();
        } catch (IOException e) {
            LOGGER.error("Error while building NetconfClientConfiguration", e);
            throw new RuntimeException(e);
        }
        Future<NetconfClientSession> futureSession = c_dispatcher.createClient(clientConfig);
        NetconfClientSession session = futureSession.get();
        return session;
    }

    public static NetconfClientSession createSSHClientSession(String ip, int port, int heartbeatInterval, String username,
            String publickey, String privateKey, Set<String> clientCaps) throws UnknownHostException, NetconfConfigurationBuilderException,
            NetconfClientDispatcherException, InterruptedException, ExecutionException {
        NetconfClientConfiguration clientConfig = null;
        NetconfTransportOrder clientTransportOder = getSshTrasnsportOrder(ip, port, heartbeatInterval);
        clientConfig = new NetconfClientConfigurationBuilder()
                .setNetconfLoginProvider(new KeyLoginProvider(username, publickey, privateKey))
                .setTransport(NetconfTransportFactory.makeNetconfTransport(clientTransportOder)).setCapabilities(clientCaps)
                .setConnectionTimeout(Long.MAX_VALUE).build();
        Future<NetconfClientSession> futureSession = c_dispatcher.createClient(clientConfig);
        NetconfClientSession session = futureSession.get();
        return session;
    }

    private static NetconfTransportOrder getSshTrasnsportOrder(String ip, int port, int heartbeatInterval) throws UnknownHostException {
        NetconfTransportOrder clientTransportOder = new NetconfTransportOrder();
        clientTransportOder.setServerSocketAddress(new InetSocketAddress(InetAddress.getByName(ip), port));
        clientTransportOder.setTransportType(NetconfTransportProtocol.SSH.name());
        clientTransportOder.setHeartbeatInterval(heartbeatInterval);
        return clientTransportOder;
    }
}
