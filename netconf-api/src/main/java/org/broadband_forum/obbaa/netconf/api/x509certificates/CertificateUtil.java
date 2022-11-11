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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

/**
 * Utility class that helps prepare a X509 Certificates.
 * Created by keshava on 4/28/15.
 */
public class CertificateUtil {
    
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil
            .getGlobalDebugLogger(CertificateUtil.class, LogAppNames.NETCONF_LIB);

    private static final Pattern CERT_BINARY_PATTERN = Pattern.compile("([a-z0-9+/=\\r\\n]+)", Pattern.CASE_INSENSITIVE);

    public static void validateCertBinary(String trustedCaCertificateBase64String) throws CertificateException {
        Matcher matcher = CERT_BINARY_PATTERN.matcher(trustedCaCertificateBase64String.trim());
        if (!matcher.matches()) {
            throw new CertificateException("Invalid certificate found " + trustedCaCertificateBase64String);
        }
    }

    public static X509Certificate getPeerX509Certifcate(SSLSession sSLSession) {
        X509Certificate peerX509Certificate = null;
        try {
            Certificate[] peerCertificates = sSLSession.getPeerCertificates();
            if (peerCertificates != null && peerCertificates.length > 0) {
                if (peerCertificates[0] instanceof X509Certificate) {
                    peerX509Certificate = (X509Certificate) peerCertificates[0];
                }
            } else {
                LOGGER.warn("empty peer certificate chain");
            }
        } catch (SSLPeerUnverifiedException e) {
            LOGGER.warn("could not retrieve peer certificate chain.", e);
        }
        return peerX509Certificate;
    }

    /**
     * Converts a certificate string into a X509 certificate.
     *
     * @param certificateString
     * @return
     * @throws CertificateException
     */

    public static X509Certificate getX509Certificate(String certificateString) throws CertificateException {
        List<X509Certificate> x509Certificates = getX509Certificates(Arrays.asList(certificateString));
        return x509Certificates.isEmpty() ? null : x509Certificates.get(0);
    }

    /**
     * Converts input stream of CA certificate strings into a list of X509 certificates.
     *
     * @param inStream
     * @return
     * @throws CertificateException
     */
    public static List<X509Certificate> getX509Certificates(InputStream inStream) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (List<X509Certificate>) cf.generateCertificates(inStream);
    }

    /**
     * Converts a list of CA certificate strings into a list of X509 certificates.
     *
     * @param certificateStrings
     * @return
     * @throws CertificateException
     */

    public static List<X509Certificate> getX509Certificates(List<String> certificateStrings) throws CertificateException {
        List<X509Certificate> x509Certificates = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        for (String ca : certificateStrings) {
            try {
                x509Certificates.add((X509Certificate) cf.generateCertificate(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(ca))));
            } catch (Exception e) {
                LOGGER.error("Error when parsing certificate: '" + ca + "'", e);
                throw e;
            }
        }
        return x509Certificates;
    }

    /**
     * Converts a delimited certificate string into a X509 certificate.
     *
     * @param delimitedCertificateString
     * @return
     * @throws CertificateException
     */

    public static X509Certificate getX509CertificateFromDelimitedString(String delimitedCertificateString) throws CertificateException {
        List<X509Certificate> x509Certificates = getX509CertificatesFromDelimitedStrings(Arrays.asList(delimitedCertificateString));
        return x509Certificates.isEmpty() ? null : x509Certificates.get(0);
    }

    /**
     * Converts a list of delimited CA certificate strings into a list of X509 certificates.
     *
     * @param delimitedCertificateStrings
     * @return
     * @throws CertificateException
     */

    public static List<X509Certificate> getX509CertificatesFromDelimitedStrings(List<String> delimitedCertificateStrings) throws CertificateException {
        List<X509Certificate> x509Certificates = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        for (String ca : delimitedCertificateStrings) {
            try {
                x509Certificates.add((X509Certificate) cf.generateCertificate(IOUtils.toInputStream(ca)));
            } catch (Exception e) {
                LOGGER.error("Error when parsing certificate: '" + ca + "'", e);
                throw e;
            }
        }
        return x509Certificates;
    }

}
