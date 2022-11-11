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

import java.net.UnknownHostException;
import java.security.Security;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class BcTlsCallhomeServer extends TlsCallhomeServer {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(BcTlsCallhomeServer.class, LogAppNames.NETCONF_LIB);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        if (Security.getProvider(BouncyCastleJsseProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleJsseProvider(BouncyCastleProvider.PROVIDER_NAME));
        }
    }
    public BcTlsCallhomeServer() {
        super("/tlscertificateswithextensions/server/netconfPeerCert.crt", "/tlscertificateswithextensions/server/netconfPeerPK.pem", "/tlscertificateswithextensions/server/rootCA.pem");
    }

    @Override
    protected NetconfTransportOrder getNetconfTransportOrder() {
        NetconfTransportOrder order = super.getNetconfTransportOrder();
        BcTlsCallhomeClient.setupTrustManager(getClass(), m_trustChain, order);
        return order;
    }


    public static void main(String[] args) {
        BcTlsCallhomeServer server = new BcTlsCallhomeServer();
        try {
            server.runTlsServer();
        } catch (NetconfConfigurationBuilderException | NetconfServerDispatcherException | InterruptedException | ExecutionException
                | UnknownHostException e) {
            LOGGER.error("Error while running server", e);
        }
    }
}