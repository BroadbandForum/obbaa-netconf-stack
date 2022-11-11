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

package org.broadband_forum.obbaa.netconf.api.x509certificates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

/**
 * <pre>
 * DynamicX509TrustManagerImpl is a X509TrustManager that has capability of reloading the trust material.
 * One can call DynamicX509TrustManagerImpl#initTrustManager() with the new List of trust certificates to re-align the trust manager.
 * </pre>
 * <p>
 * Created by keshava on 4/28/15.
 */
public class DynamicX509TrustManagerImpl extends X509ExtendedTrustManager implements DynamicX509TrustManager {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        if (Security.getProvider(BouncyCastleJsseProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleJsseProvider(BouncyCastleProvider.PROVIDER_NAME));
        }
    }

    X509ExtendedTrustManager m_innerTrustManager = null;

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil
            .getGlobalDebugLogger(DynamicX509TrustManagerImpl.class, LogAppNames.NETCONF_LIB);
    public static final AdvancedLogger CUSTOMER_LOGGER = AdvancedLoggerUtil.getGlobalCustomerLogger(DynamicX509TrustManagerImpl.class, LogAppNames.NETCONF_LIB);

    public DynamicX509TrustManagerImpl(List<String> trustedCaCertificates) throws TrustManagerInitException {
        initTrustManager(trustedCaCertificates);
    }

    public DynamicX509TrustManagerImpl(String caCertificateFilePath) throws TrustManagerInitException {
        initTrustManager(caCertificateFilePath);
    }

    @Override
    public void initTrustManager(List<String> trustedCaCertificates) throws TrustManagerInitException {
        try {
            List<X509Certificate> certificates = CertificateUtil.getX509Certificates(trustedCaCertificates);
            initializeTrustManager(certificates);
        } catch (Exception e) {
            throw new TrustManagerInitException("Exception while TrustManagerInit ", e);
        }
    }

    /**
     * A method to reload the trust material. The changes are affected only on the upcoming TLS connections, existing connections are not
     * affected from this re-init. Initialization can be done as follows
     *
     * <pre>
     * If you trust root CAs rootCA1 and rootCA2, then you need to initialize the trust manager as follows.
     * {@code List<String> caCerts = new ArrayList<>();
     *   caCerts.add(rootCA1_Cert);
     *   caCerts.add(rootCA2_Cert);
     *   m_dynamicX509TrustManager.initTrustManager(caCerts);}
     * </pre>
     *
     * @param trustedCaCertificates - List of trusted Certificate Authority certificates.
     * @throws TrustManagerInitException - If the certificates are invalid.
     */

    private synchronized void initializeTrustManager(List<X509Certificate> trustedCaCertificates) throws TrustManagerInitException {
        try {
            CollectionCertStoreParameters ccsp = new CollectionCertStoreParameters(trustedCaCertificates);
            CertStore store = CertStore.getInstance("Collection", ccsp, BouncyCastleProvider.PROVIDER_NAME);
            Set trust = new HashSet();
            X509CertSelector targetConstraints = new X509CertSelector();
            for (X509Certificate trustCert : trustedCaCertificates) {
                trust.add(new TrustAnchor(trustCert, null));
            }

            PKIXBuilderParameters params = new PKIXBuilderParameters(trust, targetConstraints);
            params.addCertStore(store);
            params.setRevocationEnabled(false);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", BouncyCastleJsseProvider.PROVIDER_NAME);
            trustManagerFactory.init(new CertPathTrustManagerParameters(params));
            m_innerTrustManager = (X509ExtendedTrustManager) trustManagerFactory.getTrustManagers()[0];

        } catch (Exception e) {
            if (e instanceof InvalidAlgorithmParameterException) {
                CUSTOMER_LOGGER.error("Could not load CA certificates ", e);
            } else {
                throw new TrustManagerInitException("Exception while TrustManagerInit ", e);
            }
        }

    }

    @Override
    public void initTrustManager(String caCertificateFilePath) throws TrustManagerInitException {
        try {
            File caCertificateFile = new File(caCertificateFilePath);
            if (caCertificateFile.isFile()) {
                List<X509Certificate> certificates = CertificateUtil.getX509Certificates(new FileInputStream(caCertificateFile));
                initializeTrustManager(certificates);
            } else {
                LOGGER.error("CaCertificate file not found: " + caCertificateFilePath);
            }
        } catch (CertificateException | IOException e) {
            throw new TrustManagerInitException("Could not load CA certificates ", e);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        try {
            m_innerTrustManager.checkClientTrusted(x509Certificates, authType);
        } catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        try {
            m_innerTrustManager.checkServerTrusted(x509Certificates, authType);
        } catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return m_innerTrustManager.getAcceptedIssuers();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType, Socket socket) throws CertificateException {
        try {
            m_innerTrustManager.checkClientTrusted(x509Certificates, authType, socket);
        } catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType, Socket socket) throws CertificateException {
        try {
            m_innerTrustManager.checkServerTrusted(x509Certificates, authType, socket);
        } catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType, SSLEngine sslEngine) throws CertificateException {
        try {
            m_innerTrustManager.checkClientTrusted(x509Certificates, authType, sslEngine);
        } catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType, SSLEngine sslEngine) throws CertificateException {
        try {
            m_innerTrustManager.checkServerTrusted(x509Certificates, authType, sslEngine);
        } catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    /**
     * For UT only.
     *
     * @param innerTrustManager
     */
    void setInnerTrustManager(X509ExtendedTrustManager innerTrustManager) {
        m_innerTrustManager = innerTrustManager;
    }
}
