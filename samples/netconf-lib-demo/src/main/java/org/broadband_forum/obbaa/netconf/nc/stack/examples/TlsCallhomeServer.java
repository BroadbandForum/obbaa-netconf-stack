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

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.server.QueuingMessageHandler;
import org.broadband_forum.obbaa.netconf.server.dispatcher.NetconfServerDispatcherImpl;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import io.netty.handler.ssl.SslProvider;

/**
 * Example TLS callhome server.
 */
public class TlsCallhomeServer {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(TlsCallhomeServer.class, LogAppNames.NETCONF_LIB);
    protected final String m_certChain;
    protected final String m_privateKey;
    protected final String m_trustChain;
    private NetconfServerMessageListener m_listener = new LoggingServerMessageListener();
    public TlsCallhomeServer() {
        this("/tlscertificates/client/netconfPeerCert.crt", "/tlscertificates/client/netconfPeerPK.pem","/tlscertificates/rootCA.pem");
    }

    public TlsCallhomeServer(String certChain, String privateKey, String trustChain) {
        m_certChain = certChain;
        m_privateKey = privateKey;
        m_trustChain = trustChain;
    }
    protected NetconfServerSession runTlsServer() throws NetconfConfigurationBuilderException, NetconfServerDispatcherException,
            InterruptedException, ExecutionException, UnknownHostException {

        NetconfServerSession serverSession = null;
        NetconfServerDispatcher serverDispatcher = new NetconfServerDispatcherImpl(ExecutorServiceProvider.getInstance().getExecutorService());
        HashSet<String> caps = new HashSet<>();
        caps.add("urn:ietf:params:netconf:base:1.0");
        NetconfTransportOrder transportOrder = getNetconfTransportOrder();

        NetconfServerConfigurationBuilder builder = new NetconfServerConfigurationBuilder();
        NetconfTransport transport;
        try {
            transport = NetconfTransportFactory.makeNetconfTransport(transportOrder);

            builder.setCapabilities(caps).setTransport(transport)
                    .setNetconfServerMessageListener(m_listener) //Callback interface for handling netconf RPCs
                    .setServerMessageHandler(new QueuingMessageHandler(m_listener)); // Message handler to handle rpc queuing/scheduling
            NetconfServerConfiguration config = builder.build();

            try {
                serverSession = serverDispatcher.createServer(config).get();
            } catch (Exception e) {
                LOGGER.debug(String.format("Error while connecting to callhome client"),e);
            }
        } catch (NetconfConfigurationBuilderException e1) {
            LOGGER.error(String.format("Could not create reverse tls transport", e1));
        }
        return serverSession;
    }

    protected NetconfTransportOrder getNetconfTransportOrder() {
        //trust store for the server
        URL serverTrustStore = getClass().getResource(m_trustChain);
        File serverTrustStoreFile = new File(serverTrustStore.getPath());

        //private key of the netconf server
        URL privKey = getClass().getResource(m_privateKey);
        File privKeyFile = new File(privKey.getPath());

        //signed certificate of the netconf server
        URL certificate = getClass().getResource(m_certChain);
        File certificateFile = new File(certificate.getPath());

        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        try {
            transportOrder.setCallHomeIp(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        transportOrder.setTlsKeepalive(true);
        transportOrder.setCallHomePort(Integer.valueOf(8282));
        transportOrder.setSslProvider(SslProvider.OPENSSL);
        transportOrder.setAllowSelfSigned(false);
        transportOrder.setCertificateChain(certificateFile);
        transportOrder.setTrustChain(serverTrustStoreFile);
        transportOrder.setPrivateKey(privKeyFile);
        transportOrder.setClientAuthenticationNeeded();
        return transportOrder;
    }

    public static void main(String[] args) {
        TlsCallhomeServer server = new TlsCallhomeServer();
        try {
            server.runTlsServer();
        } catch (NetconfConfigurationBuilderException | NetconfServerDispatcherException | InterruptedException | ExecutionException
                | UnknownHostException e) {
            LOGGER.error("Error while running server", e);
        }
    }
}
