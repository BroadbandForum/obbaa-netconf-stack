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

import static org.apache.sshd.server.ServerBuilder.DH2KEX;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DEFAULT_NC_SSH_CIPHERS;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.DEFAULT_NC_SSH_MACS;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.REGEX_FOR_COMMA;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.ServiceFactory;
import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.io.nio2.Nio2ServiceFactoryFactory;
import org.apache.sshd.common.kex.BuiltinDHFactories;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.mac.BuiltinMacs;
import org.apache.sshd.common.util.threads.NoCloseExecutor;
import org.apache.sshd.server.ServerFactoryManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.session.ServerUserAuthServiceFactory;
import org.apache.sshd.server.subsystem.SubsystemFactory;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.server.NetconfSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.transport.SshNetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.StringUtil;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.NetconfPasswordAuthenticator;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.NetconfPublicKeyAuthenticator;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class SshServerDispatcherImpl implements NetconfServerDispatcher {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshServerDispatcherImpl.class, LogAppNames.NETCONF_LIB);
    private static final String NC_SSH_SERVER_MACS = "NC_SSH_SERVER_MACS";
    private static final String NC_SSH_SERVER_CIPHERS = "NC_SSH_SERVER_CIPHERS";
    private static final String NC_SSH_SERVER_DH_KEXS = "NC_SSH_SERVER_DH_KEXS";

    private final ExecutorService m_executorService;// NOSONAR

    public SshServerDispatcherImpl(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
    }

    @Deprecated
    public SshServerDispatcherImpl() {
        this(ExecutorServiceProvider.getInstance().getExecutorService());
    }

    @Override
    public Future<NetconfServerSession> createServer(final NetconfServerConfiguration config) throws NetconfServerDispatcherException {
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
            setUpMacs(sshd);
            setUpCiphers(sshd);
            setUpDHKex(sshd);
            sshd.setPort(transport.getSocketAddress().getPort());
            sshd.setHost(transport.getSocketAddress().getAddress().getHostAddress());

            String hostKeyPath = transport.getHostKeyPath();
            File file = new File(hostKeyPath);
            if (!file.exists()) {
                LOGGER.warn("hostkey '{}' does not exist, write default hostkey to it.", hostKeyPath);
                file.getParentFile().mkdirs();
                InputStream defaultHostkeyStream = this.getClass().getClassLoader().getResource("hostkey").openStream();
                String defaultHostkey = FileUtil.loadStreamAsString(new StringBuffer(), defaultHostkeyStream);
                Files.write(Paths.get(hostKeyPath), defaultHostkey.getBytes());
            }

            KeyPairProvider keyPairProvider = new FileKeyPairProvider(Paths.get(hostKeyPath));
            String hostKeyPassword = transport.getHostKeyPassword();
            if (hostKeyPassword != null && !hostKeyPassword.isEmpty()) {
                FilePasswordProvider passwordProvider = FilePasswordProvider.of(hostKeyPassword);
                ((FileKeyPairProvider) keyPairProvider).setPasswordFinder(passwordProvider);
            }

            sshd.setKeyPairProvider(keyPairProvider);

            sshd.setPasswordAuthenticator(new NetconfPasswordAuthenticator(config.getNetconfServerAuthenticationHandler(), config
                    .getAuthenticationListener()));
            sshd.setPublickeyAuthenticator(new NetconfPublicKeyAuthenticator(config.getNetconfServerAuthenticationHandler(), config
                    .getAuthenticationListener()));

            List<SubsystemFactory> factories = new ArrayList<>();
            NetconfSessionIdProvider sessionIdProvider = config.getSessionIdProvider();
            SubsystemFactory netconfSubsystemFactory = null;
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
            Map<String, String> properties = new HashMap<>();
            properties.put(ServerFactoryManager.IDLE_TIMEOUT, String.valueOf(config.getConnectionIdleTimeOutMillis()));
            properties.put(ServerFactoryManager.NIO2_READ_TIMEOUT, String.valueOf(Long.MAX_VALUE));
            sshd.getProperties().putAll(properties);
            ExecutorService executor = config.getExecutorService();
            if (executor != null) {
                sshd.setIoServiceFactoryFactory(new Nio2ServiceFactoryFactory(() -> new NoCloseExecutor(executor)));
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

    private void setUpMacs(SshServer sshServer) throws NoSuchAlgorithmException {
        String sshPreferences = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(NC_SSH_SERVER_MACS, DEFAULT_NC_SSH_MACS);
        if (!StringUtil.isEmpty(sshPreferences)) {
            String[] macStrings = sshPreferences.split(REGEX_FOR_COMMA);
            List<BuiltinMacs> macs = new ArrayList<>();
            for (String macStr : macStrings) {
                BuiltinMacs mac = BuiltinMacs.fromFactoryName(macStr);
                if (mac == null) {
                    try {
                        mac = BuiltinMacs.valueOf(macStr);
                    } catch (IllegalArgumentException e) {
                        throw new NoSuchAlgorithmException("MAC not supported / Invalid MAC : " + macStr, e);
                    }
                }
                macs.add(mac);
            }
            sshServer.setMacFactories((List) NamedFactory.setUpBuiltinFactories(true, Collections.unmodifiableList(macs)));
        }
        LOGGER.debug("The MAC/s supported by the SSH server are {}", sshServer.getMacFactories());
    }

    private void setUpCiphers(SshServer sshServer) throws NoSuchAlgorithmException {
        String sshPreferences = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(NC_SSH_SERVER_CIPHERS, DEFAULT_NC_SSH_CIPHERS);
        if (!StringUtil.isEmpty(sshPreferences)) {
            String[] cipherStrings = sshPreferences.split(REGEX_FOR_COMMA);
            List<BuiltinCiphers> ciphers = new ArrayList<>();
            for (String cipherStr : cipherStrings) {
                BuiltinCiphers cipher = BuiltinCiphers.fromFactoryName(cipherStr);
                if (cipher == null) {
                    try {
                        cipher = BuiltinCiphers.valueOf(cipherStr);
                    } catch (IllegalArgumentException e) {
                        throw new NoSuchAlgorithmException("Cipher not supported / Invalid Cipher : " + cipherStr, e);
                    }
                }
                ciphers.add(cipher);
            }
            sshServer.setCipherFactories((List) NamedFactory.setUpBuiltinFactories(true, Collections.unmodifiableList(ciphers)));
        }
        LOGGER.debug("The Cipher/s supported by the SSH server are {}", sshServer.getCipherFactories());
    }

    private void setUpDHKex(SshServer sshServer) throws NoSuchAlgorithmException {
        String sshPreferences = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(NC_SSH_SERVER_DH_KEXS, "");
        if (!StringUtil.isEmpty(sshPreferences)) {
            String[] dhKexStrs = sshPreferences.split(REGEX_FOR_COMMA);
            List<BuiltinDHFactories> dhKexs = new ArrayList<>();
            for (String dhKexStr : dhKexStrs) {
                BuiltinDHFactories dhKex = BuiltinDHFactories.fromFactoryName(dhKexStr);
                if (dhKex == null) {
                    try {
                        dhKex = BuiltinDHFactories.valueOf(dhKexStr);
                    } catch (IllegalArgumentException e) {
                        throw new NoSuchAlgorithmException("DH Kex not supported / Invalid DH Kex: " + dhKexStr, e);
                    }
                }
                dhKexs.add(dhKex);
            }
            sshServer.setKeyExchangeFactories(NamedFactory.setUpTransformedFactories(true, dhKexs, DH2KEX));
        }
        LOGGER.debug("The Diffie Hellman Kex/s supported by the SSH server are {}", sshServer.getKeyExchangeFactories());
    }

}
