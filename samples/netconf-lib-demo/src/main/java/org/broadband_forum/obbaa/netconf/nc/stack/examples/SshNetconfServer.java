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

import java.io.Serializable;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.server.auth.AuthenticationResult;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerSessionListener;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.server.QueuingMessageHandler;
import org.broadband_forum.obbaa.netconf.server.dispatcher.NetconfServerDispatcherImpl;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

/**
 * Example SSH Netconf Server.
 */
public class SshNetconfServer {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshNetconfServer.class, LogAppNames.NETCONF_LIB);
    public static int c_serverPort = 9292;
    public static long c_connectionTimeout = 1000000L;
    private AuthenticationListener m_authListener;
    private ExecutorService m_executorService = ExecutorServiceProvider.getInstance().getExecutorService();// NOSONAR

    public NetconfServerSession runServer() throws UnknownHostException, NetconfServerDispatcherException,
            NetconfConfigurationBuilderException, InterruptedException, ExecutionException {
        HashSet<String> serverCaps = new HashSet<>();
        serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        LoggingServerMessageListener loggingListener = new LoggingServerMessageListener();
        NetconfServerConfigurationBuilder builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder(c_serverPort)
                .setAuthenticationHandler(new AuthHandlerImpl()) // You can plugin your authentication layer using Auth handler
                .setAuthenticationListener(m_authListener) // Authentication events will be given on this interface
                .setNetconfServerMessageListener(loggingListener) //Callback interface for handling netconf RPCs
                .setServerMessageHandler(new QueuingMessageHandler(loggingListener)) // Message handler to handle rpc queuing/scheduling
                .setConnectionIdleTimeoutMillis(c_connectionTimeout)
                .setCapabilities(serverCaps);
        NetconfServerDispatcher dispatcher = new NetconfServerDispatcherImpl(m_executorService);
        NetconfServerConfiguration configuration = builder.build();
        LOGGER.info("Starting server with configuration "+ configuration);
        return dispatcher.createServer(configuration).get();
    }

    public static void main(String[] args) throws UnknownHostException, NetconfServerDispatcherException,
            NetconfConfigurationBuilderException, InterruptedException, ExecutionException {
        SshNetconfServer server = new SshNetconfServer();
        server.runServer();
        LOGGER.info("Server started");
        for(;;){
            Thread.sleep(Long.MAX_VALUE);
        }

    }

    public final class AuthHandlerImpl implements NetconfServerAuthenticationHandler {
        public AuthenticationResult authenticate(ClientAuthenticationInfo clientAuthInfo) {
            // You can plugin anything here for authentication
            if ("UT".equals(clientAuthInfo.getUsername()) && "UT".equals(clientAuthInfo.getPassword())) {
                LOGGER.info("Authentication is successful");
                return new AuthenticationResult(true, "");
            }
            LOGGER.info("Authentication is failed");
            return AuthenticationResult.failedAuthResult();
        }

        public AuthenticationResult authenticate(PublicKey pubKey) {
            //You can plugin anything here for authentication
            return new AuthenticationResult(true, "");
        }

        public void logout(Serializable sessionId) {
        }

        @Override
        public void registerServerSessionListener(Serializable sessionId, NetconfServerSessionListener sessionListener) {
            
        }

        @Override
        public void unregisterServerSessionListener(Serializable sessionId) {
            
        }

        @Override
        public NetconfServerSessionListener getServerSessionListener(Serializable sessionId) {
            return null;
        }

    }
}

