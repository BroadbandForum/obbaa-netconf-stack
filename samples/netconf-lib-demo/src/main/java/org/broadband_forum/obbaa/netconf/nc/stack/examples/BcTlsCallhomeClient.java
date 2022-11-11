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
import java.io.FileInputStream;
import java.net.UnknownHostException;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.x509certificates.CertificateUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class BcTlsCallhomeClient extends TlsCallhomeClient {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(BcTlsCallhomeClient.class, LogAppNames.NETCONF_LIB);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        if (Security.getProvider(BouncyCastleJsseProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleJsseProvider(BouncyCastleProvider.PROVIDER_NAME));
        }
    }

    public BcTlsCallhomeClient() {
        super("/tlscertificateswithextensions/client/netconfPeerCert.crt", "/tlscertificateswithextensions/client/netconfPeerPK.pem", "/tlscertificateswithextensions/client/rootCA.pem");
    }

    @Override
    protected NetconfTransportOrder getNetconfTransportOrder() {
        NetconfTransportOrder order = super.getNetconfTransportOrder();
        setupTrustManager(getClass(), m_trustChain, order);
        return order;
    }

    public static void main(String[] args) throws NetconfMessageBuilderException {
        BcTlsCallhomeClient client = new BcTlsCallhomeClient();
        try {
            client.runClient();
        } catch (NetconfConfigurationBuilderException | NetconfClientDispatcherException | InterruptedException | ExecutionException
                | UnknownHostException e) {
            LOGGER.error("Error while running client", e);
        }
    }


    public static void setupTrustManager(Class klass, String trustStore, NetconfTransportOrder order) {
        order.setTrustChain(null);
        try {
            List<X509Certificate> trustCerts = CertificateUtil.getX509Certificates(new FileInputStream(new File(
                    (klass.getResource(trustStore).getFile()))));
            CollectionCertStoreParameters ccsp = new CollectionCertStoreParameters(trustCerts);
            CertStore store = CertStore.getInstance("Collection", ccsp, "BC");

            Set trust = new HashSet();
            X509CertSelector targetConstraints = new X509CertSelector();
            for (X509Certificate trustCert : trustCerts) {
                trust.add(new TrustAnchor(trustCert, null));
                //targetConstraints.setIssuer(trustCert.getSubjectX500Principal().getEncoded());
            }

            PKIXBuilderParameters params = new PKIXBuilderParameters(trust, targetConstraints);
            params.addCertStore(store);
            params.setRevocationEnabled(false);

            TrustManagerFactory fact = TrustManagerFactory.getInstance("PKIX", "BCJSSE");
            fact.init(new CertPathTrustManagerParameters(params));
            X509TrustManager trustManager = (X509TrustManager) fact.getTrustManagers()[0];

            order.setTrustManager(trustManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
