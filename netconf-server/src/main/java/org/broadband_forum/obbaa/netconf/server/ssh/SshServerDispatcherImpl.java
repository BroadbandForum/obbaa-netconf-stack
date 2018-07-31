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

package org.broadband_forum.obbaa.netconf.server.ssh;

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.SSH_CIPHERS_PREFERENCE;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.SSH_MAC_PREFERENCE;

import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.server.NetconfSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.transport.SshNetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.NetconfPasswordAuthenticator;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.NetconfPublicKeyAuthenticator;
import org.apache.log4j.Logger;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.ServiceFactory;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.io.nio2.Nio2ServiceFactoryFactory;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ServerFactoryManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerUserAuthServiceFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SshServerDispatcherImpl implements NetconfServerDispatcher {
    private static final Logger LOGGER = Logger.getLogger(SshServerDispatcherImpl.class);

    static {
        SecurityUtils.setRegisterBouncyCastle(false);
    }

    private final ExecutorService m_executorService;// NOSONAR

    public SshServerDispatcherImpl(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
    }

    @Deprecated
    public SshServerDispatcherImpl() {
        this(ExecutorServiceProvider.getInstance().getExecutorService());
    }

    @Override
    public Future<NetconfServerSession> createServer(final NetconfServerConfiguration config) throws
            NetconfServerDispatcherException {
        if (config.getNetconfTransport() instanceof SshNetconfTransport) {
            return m_executorService.submit(new Callable<NetconfServerSession>() {
                @Override
                public NetconfServerSession call() throws Exception {
                    return createAndReturnServerSession(config);
                }
            });
        }
        return null;
    }

    protected NetconfServerSession createAndReturnServerSession(final NetconfServerConfiguration config)
            throws NetconfServerDispatcherException {
        try {

            SshNetconfTransport transport = (SshNetconfTransport) config.getNetconfTransport();
            SshServer sshd = SshServer.setUpDefaultServer();
            sshd.setMacFactories(NamedFactory.setUpBuiltinFactories(true, SSH_MAC_PREFERENCE));
            sshd.setCipherFactories(NamedFactory.setUpBuiltinFactories(true, SSH_CIPHERS_PREFERENCE));
            sshd.setPort(transport.getSocketAddress().getPort());
            sshd.setHost(transport.getSocketAddress().getAddress().getHostAddress());

            File file = new File(transport.getHostKeyPath());
            SimpleGeneratorHostKeyProvider keyPairProvider = new SimpleGeneratorHostKeyProvider(file);
            keyPairProvider.setAlgorithm(KeyUtils.RSA_ALGORITHM);
            sshd.setKeyPairProvider(keyPairProvider);

            sshd.setPasswordAuthenticator(new NetconfPasswordAuthenticator(config
                    .getNetconfServerAuthenticationHandler(), config
                    .getAuthenticationListener()));
            sshd.setPublickeyAuthenticator(new NetconfPublicKeyAuthenticator(config
                    .getNetconfServerAuthenticationHandler(), config
                    .getAuthenticationListener()));

            List<NamedFactory<Command>> factories = new ArrayList<>();
            NetconfSessionIdProvider sessionIdProvider = config.getSessionIdProvider();
            NamedFactory<Command> netconfSubsystemFactory = null;
            if (config.getCapabilityProvider() != null) {
                netconfSubsystemFactory = new NetconfSubsystemFactory(config.getNetconfServerMessageListener(),
                        config.getServerMessageHandler(), config.getCapabilityProvider(), sessionIdProvider
                );
            } else {
                netconfSubsystemFactory = new NetconfSubsystemFactory(config.getNetconfServerMessageListener(),
                        config.getServerMessageHandler(), config.getCaps(), sessionIdProvider
                );
            }
            factories.add(netconfSubsystemFactory);
            sshd.setSubsystemFactories(factories);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(ServerFactoryManager.IDLE_TIMEOUT, String.valueOf(config.getConnectionIdleTimeOutMillis()));
            sshd.getProperties().putAll(properties);
            ExecutorService executor = config.getExecutorService();
            if (executor != null) {
                sshd.setIoServiceFactoryFactory(new Nio2ServiceFactoryFactory(executor, false));
            }
            String heartbeatInt;
            if (transport.getHeartBeatInterval() != 0) {
                heartbeatInt = Integer.toString(transport.getHeartBeatInterval() * 1000);
            } else {
                heartbeatInt = "0";
            }
            sshd.getProperties().put(NetconfResources.HEARTBEAT_INTERVAL, heartbeatInt);
            sshd.setServiceFactories(Arrays.asList(new ServiceFactory[]{new ServerUserAuthServiceFactory(),
                    new NetconfSshServerConnectionService.Factory()}));
            sshd.start();
            NetconfServerSession serverSession = new SshNetconfServerSession(sshd);
            return serverSession;
        } catch (Exception e) {
            LOGGER.error("Error while trying to create netconf server ", e);
            throw new NetconfServerDispatcherException("Error while trying to create netconf server ", e);
        }
    }

}
