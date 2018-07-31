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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.xml.bind.DatatypeConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that helps prepare a X509 Certificates.
 * Created by keshava on 4/28/15.
 */
public class CertificateUtil {

    private static final Logger LOGGER = Logger.getLogger(CertificateUtil.class);

    private static final Pattern X509_CERT_WITH_DELIMITER_PATTERN = Pattern.compile("-+BEGIN\\s+.*CERTIFICATE[^-]*-+" +
            "(?:\\s|\\r|\\n)+"
            + "([a-z0-9+/=\\r\\n]+)" + "-+END\\s+.*CERTIFICATE[^-]*-+", Pattern.CASE_INSENSITIVE);

    private static final Pattern CERT_BINARY_PATTERN = Pattern.compile("([a-z0-9+/=\\r\\n]+)", Pattern
            .CASE_INSENSITIVE);

    /**
     * Convert certificate Strings into ByteArrayCertificates.
     *
     * @param trustedCaCertificates - base64 encoded Strings containing certificate bytes
     * @return
     * @throws CertificateException
     */
    public static List<ByteArrayCertificate> getByteArrayCertificates(List<String> trustedCaCertificates) throws
            CertificateException {
        List<ByteArrayCertificate> certs = new ArrayList<ByteArrayCertificate>();

        for (String trustedCaCertificate : trustedCaCertificates) {
            ByteArrayCertificate cert = getByteArrayCertificateFromBase64String(trustedCaCertificate);
            certs.add(cert);
        }
        return certs;
    }

    /**
     * Convert Certificate string to base64 encoded certificate String
     *
     * @param trustedCaCertificate
     * @return
     * @throws CertificateException
     */
    public static ByteArrayCertificate getByteArrayCertificateFromDelimitedString(String trustedCaCertificate) throws
            CertificateException {
        Matcher matcher = X509_CERT_WITH_DELIMITER_PATTERN.matcher(trustedCaCertificate.trim());
        if (!matcher.matches()) {
            throw new CertificateException("Invalid certificate found " + trustedCaCertificate);
        }
        return getByteArrayCertificateFromBase64String(matcher.group(1));
    }

    /**
     * Convert base64 encoded certificate String into ByteArrayCertificate.
     *
     * @param trustedCaCertificateBase64String
     * @return
     * @throws CertificateException
     */
    public static ByteArrayCertificate getByteArrayCertificateFromBase64String(String trustedCaCertificateBase64String)
            throws CertificateException {
        validateCertBinary(trustedCaCertificateBase64String);
        byte[] bytes = DatatypeConverter.parseBase64Binary(trustedCaCertificateBase64String);
        return new ByteArrayCertificate().setBytes(bytes);
    }

    public static void validateCertBinary(String trustedCaCertificateBase64String) throws CertificateException {
        Matcher matcher = CERT_BINARY_PATTERN.matcher(trustedCaCertificateBase64String.trim());
        if (!matcher.matches()) {
            throw new CertificateException("Invalid certificate found " + trustedCaCertificateBase64String);
        }
    }

    /**
     * Convert ByteArrayCertificates into X509Certificates.
     *
     * @param byteCertificates
     * @return
     * @throws CertificateException
     */
    public static List<X509Certificate> getX509Certificates(List<ByteArrayCertificate> byteCertificates) throws
            CertificateException {
        List<X509Certificate> x509Certificates = new ArrayList<>();
        for (ByteArrayCertificate byteCertificate : byteCertificates) {
            x509Certificates.add(getX509Certificate(byteCertificate));
        }
        return x509Certificates;
    }

    /**
     * Convert ByteArrayCertificate into X509Certificate.
     *
     * @param byteCertificate
     * @return
     * @throws CertificateException
     */
    public static X509Certificate getX509Certificate(ByteArrayCertificate byteCertificate) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(byteCertificate
                .getBytes()));
    }

    /**
     * Convert base64 encoded certificate String to X509Certificate. This method is a shorthand for
     * <code>CertificateUtil.getX509Certificate(CertificateUtil.getByteArrayCertificate(certificateString)); <code>
     *
     * @param certificateStringInBase64
     * @return
     * @throws CertificateException
     */
    public static X509Certificate getX509Certificate(String certificateStringInBase64) throws CertificateException {
        return getX509Certificate(getByteArrayCertificateFromBase64String(certificateStringInBase64));
    }

    /**
     * Convert base64 encoded certificate Strings to X509Certificates. This method is a shorthand for
     * <code>CertificateUtil.getX509Certificates(CertificateUtil.getByteArrayCertificates(certificateString)); <code>
     *
     * @param certificateStringsInBase64
     * @return
     * @throws CertificateException
     */
    public static List<X509Certificate> getX509CertificatesFromCertificateStrings(List<String>
                                                                                          certificateStringsInBase64)
            throws CertificateException {
        return getX509Certificates(getByteArrayCertificates(certificateStringsInBase64));
    }

    /**
     * Read the -----BEGIN CERTIFICATE----- and -----END CERTIFICATE----- delimited strings from the file.
     *
     * @param certificateChainFile
     * @return
     * @throws CertificateException
     */
    public static List<String> certificateStringsFromFile(File certificateChainFile) throws CertificateException {
        try {
            String fileContents = FileUtils.readFileToString(certificateChainFile);
            Matcher matcher = X509_CERT_WITH_DELIMITER_PATTERN.matcher(fileContents);
            List<String> certificateStrings = new ArrayList<>();

            while (matcher.find()) {
                String matchedString = matcher.group(1);
                certificateStrings.add(matchedString);
            }
            return certificateStrings;
        } catch (IOException e) {
            throw new CertificateException("File read error ", e);
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

    public static List<String> stripDelimiters(List<String> certificateChain) {
        List<String> strippedBinaries = new ArrayList<>();
        for (String cert : certificateChain) {
            String strippedCert = cert;
            Matcher matcher = X509_CERT_WITH_DELIMITER_PATTERN.matcher(cert);
            if (matcher.find()) {
                strippedCert = matcher.group(1).trim();
            }
            strippedBinaries.add(strippedCert);
        }
        return strippedBinaries;

    }
}
