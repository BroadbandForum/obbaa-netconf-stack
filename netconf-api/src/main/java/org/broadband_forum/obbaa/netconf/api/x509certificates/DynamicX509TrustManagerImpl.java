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

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;
import java.io.File;
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

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

/**
 * <pre>
 * DynamicX509TrustManagerImpl is a X509TrustManager that has capability of reloading the trust material.
 * One can call DynamicX509TrustManagerImpl#initTrustManager() with the new List of trust certificates to re-align the trust manager.
 * </pre>
 *
 * Created by keshava on 4/28/15.
 */
public class DynamicX509TrustManagerImpl extends X509ExtendedTrustManager implements DynamicX509TrustManager {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
            Security.addProvider(new BouncyCastleProvider());
        }
        if (Security.getProvider(BouncyCastleJsseProvider.PROVIDER_NAME) == null){
            Security.addProvider(new BouncyCastleJsseProvider(BouncyCastleProvider.PROVIDER_NAME));
        }
    }

    X509ExtendedTrustManager m_innerTrustManager = null;

    private static final Logger LOGGER = Logger.getLogger(DynamicX509TrustManagerImpl.class);
    public static final AdvancedLogger CUSTOMER_LOGGER = LoggerFactory.getLogger(DynamicX509TrustManagerImpl.class,  "netconf-lib", "CUSTOMER", "GLOBAL");

    public DynamicX509TrustManagerImpl(List<String> trustedCaCertificates) throws TrustManagerInitException {
        initTrustManager(trustedCaCertificates);
    }

    public DynamicX509TrustManagerImpl(String caCertificateFilePath) throws TrustManagerInitException {
        initTrustManager(caCertificateFilePath);
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
     * @param trustedCaCertificates - new list of trusted Certificate Authority certificates.This list might as well contain a pieces of a
     *            certificate chain.
     * @throws TrustManagerInitException - If the certificates are invalid.
     */
    @Override
    public synchronized void initTrustManager(List<String> trustedCaCertificates) throws TrustManagerInitException {
        try {
            List<X509Certificate> trustCerts =  CertificateUtil.getX509Certificates(CertificateUtil.getByteArrayCertificates(CertificateUtil.stripDelimiters
                    (trustedCaCertificates)));
            CollectionCertStoreParameters ccsp = new CollectionCertStoreParameters(trustCerts);
            CertStore store = CertStore.getInstance("Collection", ccsp, BouncyCastleProvider.PROVIDER_NAME);
            Set trust = new HashSet();
            X509CertSelector targetConstraints = new X509CertSelector();
            for(X509Certificate trustCert : trustCerts){
                trust.add(new TrustAnchor(trustCert, null));
            }

            PKIXBuilderParameters params = new PKIXBuilderParameters(trust, targetConstraints);
            params.addCertStore(store);
            params.setRevocationEnabled(false);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", BouncyCastleJsseProvider.PROVIDER_NAME);
            trustManagerFactory.init(new CertPathTrustManagerParameters(params));
            m_innerTrustManager = (X509ExtendedTrustManager) trustManagerFactory.getTrustManagers()[0];

        } catch (Exception e) {
            if(e instanceof InvalidAlgorithmParameterException){
                CUSTOMER_LOGGER.error("Could not load CA certificates ", e);
            }else{
                throw new TrustManagerInitException("Exception while TrustManagerInit ", e);
            }
        }

    }

    @Override
    public void initTrustManager(String caCertificateFilePath) throws TrustManagerInitException {
        try {
            File caCertificateFile = new File(caCertificateFilePath);
            if (caCertificateFile.isFile()) {
                List<String> certificates = CertificateUtil.certificateStringsFromFile(caCertificateFile);
                initTrustManager(certificates);
            } else {
                LOGGER.error("CaCertificate file not found: " + caCertificateFilePath);
            }
        } catch (CertificateException e) {
            throw new TrustManagerInitException("Could not load CA certificates ", e);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        try {
            m_innerTrustManager.checkClientTrusted(x509Certificates, authType);
        }catch (CertificateException e){
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        try {
            m_innerTrustManager.checkServerTrusted(x509Certificates, authType);
        }catch (CertificateException e){
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
        }catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType, Socket socket) throws CertificateException {
        try{
            m_innerTrustManager.checkServerTrusted(x509Certificates, authType, socket);
        }catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType, SSLEngine sslEngine) throws CertificateException {
        try{
            m_innerTrustManager.checkClientTrusted(x509Certificates, authType, sslEngine);
        }catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType, SSLEngine sslEngine) throws CertificateException {
        try{
            m_innerTrustManager.checkServerTrusted(x509Certificates, authType, sslEngine);
        }catch (CertificateException e) {
            //When SSL handshake failed, call home ssl handler needs to extract peer certificate
            //so, throw PeerCertificateException that wraps certificate
            throw new PeerCertificateException(e, x509Certificates);
        }
    }

    /**
     * For UT only.
     * @param innerTrustManager
     */
    void setInnerTrustManager(X509ExtendedTrustManager innerTrustManager) {
        m_innerTrustManager = innerTrustManager;
    }
}
