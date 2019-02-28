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

package org.broadband_forum.obbaa.netconf.api.utils;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * This class provides the method to read the public and private keys written in PEM format.
 * 
 *
 * 
 */
public class PemReader {

    private static final Logger LOGGER = Logger.getLogger(PemReader.class);

    private static final Pattern PUB_KEY_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*PUBLIC\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*PUBLIC\\s+KEY[^-]*-+", 2);

    private static final Pattern PRIV_KEY_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", 2);
    public static final Charset US_ASCII_CHARSET = Charset.forName("US-ASCII");

    public static List<PublicKey> readPublicKey(InputStream inStream) throws CertificateException, NoSuchAlgorithmException,
            InvalidKeySpecException, KeyException {

        String content;
        try {
            content = readContent(inStream);
        } catch (IOException e) {
            throw new CertificateException("failed to read from the input file ", e);
        }

        Matcher m = PUB_KEY_PATTERN.matcher(content);
        int start = 0;

        List<PublicKey> pubKeyList = new ArrayList<PublicKey>();
        KeyFactory rsaKF = KeyFactory.getInstance("RSA");
        KeyFactory dsaKF = KeyFactory.getInstance("DSA");

        while (m.find(start)) {
            String encodedKeyStr = m.group(1);
            start = m.end();
            X509EncodedKeySpec encodedKeySpec = null;
            encodedKeySpec = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(encodedKeyStr));
            PublicKey key;
            try {
                key = rsaKF.generatePublic(encodedKeySpec);
            } catch (InvalidKeySpecException e) {
                LOGGER.info("Could not get keyFactory using RSA", e);
                key = dsaKF.generatePublic(encodedKeySpec);
            }
            pubKeyList.add(key);
        }

        if (pubKeyList.isEmpty()) {
            throw new CertificateException("found no Public Keys in the file");
        }

        return pubKeyList;

    }

    public static PrivateKey readPrivateKey(InputStream inStream) throws KeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        String content;
        try {
            content = readContent(inStream);
        } catch (IOException e) {
            throw new KeyException("failed to read a file: ", e);
        }

        Matcher m = PRIV_KEY_PATTERN.matcher(content);
        if (!m.find()) {
            throw new KeyException("found no private key: ");
        }

        KeyFactory rsaKF = KeyFactory.getInstance("RSA");
        KeyFactory dsaKF = KeyFactory.getInstance("DSA");

        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(m.group(1)));
        PrivateKey privkey;
        try {
            privkey = rsaKF.generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException e) {
            LOGGER.info("Could not get keyFactory using RSA", e);
            privkey = dsaKF.generatePrivate(encodedKeySpec);
        }
        return privkey;

    }

    private static String readContent(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[8192];
            int ret;
            while (true) {
                ret = in.read(buf);
                if (ret < 0) {
                    break;
                }
                out.write(buf, 0, ret);
            }
            return out.toString(US_ASCII_CHARSET.name());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close a stream.", e);
            }
            try {
                out.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close a stream.", e);
            }
        }
    }

    public static String stripPKDelimiters(String pkStringWitDelimiters) {
        Matcher matcher = PRIV_KEY_PATTERN.matcher(pkStringWitDelimiters);
        if(matcher.find()){
            return matcher.group(1).trim();
        }
        return pkStringWitDelimiters.trim();
    }
}
