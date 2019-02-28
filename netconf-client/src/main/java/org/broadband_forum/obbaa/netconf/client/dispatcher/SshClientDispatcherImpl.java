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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.channel.RequestHandler;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.session.ConnectionService;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.global.KeepAliveHandler;
import org.apache.sshd.server.global.NoMoreSessionsHandler;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.authentication.FailureInfo;
import org.broadband_forum.obbaa.netconf.api.authentication.PointOfFailure;
import org.broadband_forum.obbaa.netconf.api.authentication.SuccessInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.transport.SshNetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.client.ssh.SshClientServiceFactoryFactory;
import org.broadband_forum.obbaa.netconf.client.ssh.SshHelloMessageListener;
import org.broadband_forum.obbaa.netconf.client.ssh.SshNetconfClientSession;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.KeyLoginProvider;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.PasswordLoginProvider;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.SshHostKeyUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

/**
 * A dispatcher to dispatch SSH based Netconf Clients.
 */
public class SshClientDispatcherImpl extends AbstractNetconfClientDispatcher {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshClientDispatcherImpl.class, LogAppNames.NETCONF_LIB);

    static {
        SecurityUtils.setRegisterBouncyCastle(false);
    }

    @Deprecated
    public SshClientDispatcherImpl() {
        this(ExecutorServiceProvider.getInstance().getExecutorService());
    }

    public SshClientDispatcherImpl(ExecutorService executorService) {// NOSONAR
        super(executorService);
    }

    protected NetconfClientSession createFutureSession(final NetconfClientConfiguration config) throws NetconfClientDispatcherException {
        SshClient sshClient = null;
        try {
            long start = System.currentTimeMillis();
            LOGGER.debug("Creating default SSH client");
            sshClient = SshClient.setUpDefaultClient();
            sshClient.setMacFactories(NamedFactory.setUpBuiltinFactories(true, NetconfResources.SSH_MAC_PREFERENCE));
            sshClient.setCipherFactories(NamedFactory.setUpBuiltinFactories(true, NetconfResources.SSH_CIPHERS_PREFERENCE));
            PropertyResolverUtils.updateProperty(sshClient, FactoryManager.IDLE_TIMEOUT, 0);
            PropertyResolverUtils.updateProperty(sshClient, FactoryManager.NIO2_READ_TIMEOUT, Long.MAX_VALUE);
            LOGGER.debug("Created default SSH client after " + (System.currentTimeMillis() - start) + " ms");
            sshClient.setGlobalRequestHandlers(Arrays.<RequestHandler<ConnectionService>>asList(new KeepAliveHandler(), new NoMoreSessionsHandler()));
            AsynchronousChannelGroup asyncChannelGroup = config.getAsynchronousChannelGroup();
            if (asyncChannelGroup != null) {
                sshClient.setIoServiceFactoryFactory(new SshClientServiceFactoryFactory(asyncChannelGroup));
            }
            final SshNetconfTransport transport = (SshNetconfTransport) config.getTransport();
            sshClient.start();
            LOGGER.info("SSH client has started");
            Long configuredTimeout = config.getConnectTimeoutMillis();
            if (configuredTimeout == null || configuredTimeout == 0) {
                configuredTimeout = NetconfClientSession.DEFAULT_CONNECT_TIMEOUT_SECS * 1000;
            }
            ConnectFuture connectFuture = null;
            ClientSession clientSession = null;
            if (config.getNetconfLoginProvider() instanceof PasswordLoginProvider) {
                connectFuture = getConnectFuture(sshClient, config.getNetconfLoginProvider().getLogin().getUsername(), transport.getSocketAddress());
                clientSession = getClientSession(transport, configuredTimeout, connectFuture);
                clientSession.addPasswordIdentity(config.getNetconfLoginProvider().getLogin().getPassword());
                LOGGER.debug("SSH Client login using Password Authenticator");
            } else if (config.getNetconfLoginProvider() instanceof KeyLoginProvider) {
                connectFuture = getConnectFuture(sshClient, config.getNetconfLoginProvider().getKeyLogin().getUserName(), transport.getSocketAddress());
                clientSession = getClientSession(transport, configuredTimeout, connectFuture);
                KeyPair keypair = SshHostKeyUtil.doReadKeyPair(config.getNetconfLoginProvider().getKeyLogin());
                if (keypair != null) {
                    clientSession.addPublicKeyIdentity(keypair);
                }
                LOGGER.debug("SSH Client login using Key Authenticator");
            }

            LOGGER.debug("Waiting for authentication...");
            clientSession.auth().addListener(future -> {
                AuthenticationListener authenticationListener = config.getAuthenticationListener();
                if (authenticationListener != null) {
                    if (future.isSuccess()) {
                        authenticationListener.authenticationSucceeded(new SuccessInfo().setIp(
                                transport.getSocketAddress().getAddress().getHostAddress()).setPort(
                                transport.getSocketAddress().getPort()));
                        LOGGER.debug("Authentication success");
                    } else {
                        authenticationListener.authenticationFailed(new FailureInfo()
                                .setIp(transport.getSocketAddress().getAddress().getHostAddress())
                                .setPort(transport.getSocketAddress().getPort()).setPointOfFailure(PointOfFailure.client));
                        LOGGER.debug("Authentication fail");
                    }
                }

            }).verify(configuredTimeout, TimeUnit.MILLISECONDS);

            LOGGER.debug("Initiating clientChannel...");
            ChannelSubsystem clientChannel = clientSession.createSubsystemChannel(NetconfResources.NETCONF_SUBSYSTEM_NAME);
            clientChannel.setStreaming(ClientChannel.Streaming.Async);

            LOGGER.debug("Initiating netconfSession...");
            final SshNetconfClientSession netconfSession = new SshNetconfClientSession(getExecutorService());
            LOGGER.debug("Set client session");
            netconfSession.setClientSession(clientSession);
            LOGGER.debug("Add notification listener");
            netconfSession.addNotificationListener(config.getNotificationListener());
            LOGGER.debug("Add session listener");
            netconfSession.addSessionListener(config.getClientSessionListener());
            LOGGER.debug("Inform the listeners about session closed");
            clientChannel.open().verify();
            LOGGER.debug("Set client capabilities");
            netconfSession.setClientCapabilities(config.getCaps());
            try {
                LOGGER.debug("Building document for client capabilities");
                PojoToDocumentTransformer builder = new PojoToDocumentTransformer().newClientHelloMessage(config.getCaps());
                Document doc = builder.build();

                if (LOGGER.isDebugEnabled()) {
                    try {
                        LOGGER.debug(DocumentUtils.documentToString(doc));
                    } catch (Exception e) {
                        LOGGER.error("Error while logging hello message", e);
                    }
                }

                final byte[] helloBytes = DocumentToPojoTransformer.addRpcDelimiter(DocumentToPojoTransformer.getBytesFromDocument(doc));
                LOGGER.debug("Client sending the netconf Hello message : {}" , LOGGER.sensitiveData(new String(helloBytes)));
                IoWriteFuture writeFuture = clientChannel.getAsyncIn().write(new ByteArrayBuffer(helloBytes));
                SshHelloMessageListener helloMsgListener = new SshHelloMessageListener(clientChannel, netconfSession);
                //read the pipe for hello from the other side
                IoReadFuture ioReadFuture = clientChannel.getAsyncOut().read(new ByteArrayBuffer(ByteArrayBuffer.MAX_LEN)).addListener(helloMsgListener);

                //wait for the write to complete before returning the client session
                writeFuture.await(configuredTimeout, TimeUnit.MILLISECONDS);
                writeFuture.verify(configuredTimeout, TimeUnit.MILLISECONDS);
                LOGGER.debug("Client sent hello message..");

                ioReadFuture.await(configuredTimeout, TimeUnit.MILLISECONDS);
                // verify hello exchange was successful before sending a client session
                ioReadFuture.verify(configuredTimeout, TimeUnit.MILLISECONDS);
                // wait for hello message received
                helloMsgListener.await();
                LOGGER.debug("hello message received from the server..");
            } catch (NetconfMessageBuilderException e) {
                throw new NetconfClientDispatcherException(e);
            }

            netconfSession.setClientChannel(clientChannel);
            netconfSession.setSshClient(sshClient);
            LOGGER.info("Netconf client session has been created");
            return netconfSession;
        } catch (Exception e) {
            if (sshClient != null) {
                sshClient.close(true);
            }
            throw new NetconfClientDispatcherException("Could not get a client session ", e);
        }
    }

    protected ConnectFuture getConnectFuture(SshClient sshClient, String username, InetSocketAddress socketAddress)
            throws IOException {
        return sshClient.connect(username, socketAddress);
    }

    private ClientSession getClientSession(SshNetconfTransport transport, Long configuredTimeout, ConnectFuture
            connectFuture) throws IOException {
        ClientSession clientSession;
        connectFuture.await(configuredTimeout, TimeUnit.MILLISECONDS);
        clientSession = connectFuture.getSession();
        if (clientSession == null) {
            throw new RuntimeException("Could not connect to server / connect timed out");
        }
        return clientSession;
    }

}
