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

package org.broadband_forum.obbaa.netconf.samples.serverfwk.nbi;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DynamicCapabilityProviderImpl;
import org.broadband_forum.obbaa.netconf.server.QueuingMessageHandler;


/**
 * OB-BAA NBI SSH Netconf server implementation class
 * Responsible for initializing/starting/stopping SSH Netconf server.
 */
public class SampleSshNetconfServer {
    private static final Logger LOGGER = Logger.getLogger(SampleSshNetconfServer.class);

    private int m_serverPort = 830;

    private long m_connectionTimeout = 1000000L;

    private SampleAuthService m_auth;

    private NetconfServerMessageListener m_listener;

    private NetconfServerDispatcher m_dispatcher;

    private NetconfServerSession m_session;

    private DynamicCapabilityProviderImpl m_capabilityProvider;

    /**
     * Initialize SSH Netconf server.
     */
    public SampleSshNetconfServer(SampleAuthService netconfAuth, NetconfServerMessageListener messageListener,
                                  NetconfServerDispatcher dispatcher, DynamicCapabilityProviderImpl capabilityProvider) {

        m_auth = netconfAuth;
        m_listener = messageListener;
        m_dispatcher = dispatcher;
        m_capabilityProvider = capabilityProvider;

        addBasicCapability();
    }


    public void setPort(int port) {
        m_serverPort = port;
    }

    public void setConnectionTimeout(long timeout) {
        m_connectionTimeout = timeout;
    }


    public void addBasicCapability() {

        HashSet<String> serverCaps = new HashSet<>();
        serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_1);

        m_capabilityProvider.addStaticCapabilities(serverCaps);
    }


    /**
     * Start SSH Netconf server.
     */
    public void start() throws Exception {


        //Builder the SSH Netconf server configuration
        NetconfServerConfigurationBuilder builder = NetconfServerConfigurationBuilder
                .createDefaultNcServerBuilder(m_serverPort)
                .setAuthenticationHandler(m_auth)
                .setNetconfServerMessageListener(m_listener)
                .setServerMessageHandler(new QueuingMessageHandler(m_listener))
                .setConnectionIdleTimeoutMillis(m_connectionTimeout)
                .setCapabilityProvider(m_capabilityProvider);
        NetconfServerConfiguration configuration = builder.build();

        //Run the SSH Netconf server
        LOGGER.info("Starting server with configuration " + configuration);
        m_session = m_dispatcher.createServer(configuration).get();
        LOGGER.info("Start Netconf Server successfully");

    }

    /**
     * Stop SSH Netconf server.
     */
    public void stop() {

        m_session.killServer(true);

    }

}
