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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.authentication.FailureInfo;
import org.broadband_forum.obbaa.netconf.api.authentication.SuccessInfo;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.TcpServerSession;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.client.dispatcher.CallhomeTlsClientDispatcherImpl;

import io.netty.handler.ssl.SslProvider;

/**
 * Example TLS Callhome Client.
 */

public class TlsCallhomeClient {

    private static final Logger LOGGER = Logger.getLogger(TlsCallhomeClient.class);
    protected final String m_certChain;
    protected final String m_privateKey;
    protected final String m_trustChain;
    private NetconfClientSession m_clientSession;

    public TlsCallhomeClient() {
        this("tlscertificates/client/netconfPeerCert.crt", "tlscertificates/client/netconfPeerPK.pem","tlscertificates/rootCA.pem");
    }

    public TlsCallhomeClient(String certChain, String privateKey, String trustChain) {
        m_certChain = certChain;
        m_privateKey = privateKey;
        m_trustChain = trustChain;
    }

    protected TcpServerSession runClient() throws NetconfConfigurationBuilderException,
            InterruptedException, ExecutionException, UnknownHostException, NetconfClientDispatcherException, NetconfMessageBuilderException {
        CallhomeTlsClientDispatcherImpl clientDispatcher = new CallhomeTlsClientDispatcherImpl(ExecutorServiceProvider.getInstance().getExecutorService());
        HashSet<String> caps = new HashSet<>();
        caps.add("urn:ietf:params:netconf:base:1.0");
        NetconfTransportOrder transportOrder = getNetconfTransportOrder();

        Set<String> clientCaps = new HashSet<>();
        NetconfClientConfiguration clientConfig = new NetconfClientConfigurationBuilder()
                .setTransport(NetconfTransportFactory.makeNetconfTransport(transportOrder))
                .setConnectionTimeout(10000)
                .setCapabilities(clientCaps)
                .setAuthenticationListener(new AuthHandlerImpl()).build(); // Authentication events will be given on this interface
        Future<TcpServerSession> futureSession = clientDispatcher.createReverseClient(clientConfig);
        TcpServerSession session = futureSession.get();
        return session;
    }

    protected NetconfTransportOrder getNetconfTransportOrder() {
        //trust store for the client
        URL clientTrustStore = getClass().getResource(m_trustChain);
        File clientTrustStoreFile = new File(clientTrustStore.getPath());

        //private key of the netconf client
        URL privKey = getClass().getResource(m_privateKey);
        File privKeyFile = new File(privKey.getPath());

        //signed certificate of the netconf client
        URL certificate = getClass().getResource(m_certChain);
        File certificateFile = new File(certificate.getPath());

        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        try {
            transportOrder.setCallHomeIp(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        transportOrder.setCallHomePort(Integer.valueOf(8282));
        transportOrder.setTlsKeepalive(true);
        transportOrder.setAllowSelfSigned(false);
        transportOrder.setSslProvider(SslProvider.OPENSSL);
        transportOrder.setCertificateChain(certificateFile);
        transportOrder.setTrustChain(clientTrustStoreFile);
        transportOrder.setPrivateKey(privKeyFile);
        transportOrder.setCallHomeListener(new SampleCallHomeListener()); // a listener interface to get callhome netconf client sessions
        return transportOrder;
    }

    public static void main(String[] args) throws NetconfMessageBuilderException {
        TlsCallhomeClient client = new TlsCallhomeClient();
        try {
            client.runClient();
        }catch (NetconfConfigurationBuilderException | NetconfClientDispatcherException | InterruptedException | ExecutionException
                | UnknownHostException e) {
            LOGGER.error("Error while running client", e);
        }
    }

    private final class AuthHandlerImpl implements AuthenticationListener {
        volatile List<SuccessInfo> m_successInfos = new ArrayList<>();
        volatile List<FailureInfo> m_failureInfos = new ArrayList<>();

        @Override
        public void authenticationSucceeded(SuccessInfo info) {
            m_successInfos.add(info);
        }

        @Override
        public void authenticationFailed(FailureInfo info) {
            m_failureInfos.add(info);
        }
    }

    private class SampleCallHomeListener implements CallHomeListener {
        @Override
        public void connectionEstablished(NetconfClientSession clientSession, NetconfLoginProvider netconfLoginProvider, X509Certificate
                peerX509Certificate, boolean isSelfSigned) {
            m_clientSession = clientSession;
            //Run netconf requests on a new threadpool
            Executors.newSingleThreadExecutor().execute(() -> ClientUtil.runNetconfRequests(TlsCallhomeClient.this.m_clientSession));
        }
    }

}
