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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Utility class that helps prepare a Private Keys.
 * Created by keshava on 4/29/15.
 */
public class KeyUtil {
    private static final Pattern PRIVATE_KEY_PATTERN_WITH_DELIMITER = Pattern.compile("-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s)*"
            + "([a-z0-9+/=\\s]+)" + "(?:\\s)*-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", Pattern.CASE_INSENSITIVE);

    private static final Pattern PRIVATE_KEY_BINARY_PATTERN = Pattern.compile("([a-z0-9+/=\\s]+)", Pattern.CASE_INSENSITIVE);

    /**
     * Convert a private key from base 64 encoded string to A PrivateKey object.
     *
     * @param encryptedPrivateKeyString
     * @param privateKeyPassword
     * @return
     * @throws KeyException
     */
    public static PrivateKey getPrivateKey(String encryptedPrivateKeyString, String privateKeyPassword) throws KeyException {
        ByteArrayPrivateKey byteArrayPrivateKey = getByteArrayPrivateKeyFromBase64String(encryptedPrivateKeyString);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = getPkcs8EncodedKeySpec(byteArrayPrivateKey, privateKeyPassword);
        PrivateKey privateKey = getPrivateKeyFromKeySpec(pkcs8EncodedKeySpec);
        return privateKey;

    }

    /**
     * Convert a private key from Delimited Certificate String to A PrivateKey object. FOR UT purpose
     *
     * @param encryptedPrivateKeyString
     * @param privateKeyPassword
     * @return
     * @throws KeyException
     */
    public static PrivateKey getPrivateKeyWithDelimiter(String encryptedPrivateKeyString, String privateKeyPassword) throws KeyException {
        ByteArrayPrivateKey byteArrayPrivateKey = getByteArrayPrivateKeyFromDelimitedString(encryptedPrivateKeyString);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = getPkcs8EncodedKeySpec(byteArrayPrivateKey, privateKeyPassword);
        PrivateKey privateKey = getPrivateKeyFromKeySpec(pkcs8EncodedKeySpec);
        return privateKey;

    }

    private static PrivateKey getPrivateKeyFromKeySpec(PKCS8EncodedKeySpec pkcs8EncodedKeySpec) throws KeyException {
        PrivateKey privateKey;
        try {
            KeyFactory rsaKF = KeyFactory.getInstance("RSA");
            privateKey = rsaKF.generatePrivate(pkcs8EncodedKeySpec);
        } catch (InvalidKeySpecException ignore) {
            KeyFactory dsaKF = null;
            try {
                dsaKF = KeyFactory.getInstance("DSA");
                privateKey = dsaKF.generatePrivate(pkcs8EncodedKeySpec);
            } catch (NoSuchAlgorithmException e) {
                throw new KeyException("could not get a DSA KeyFactory", e);
            } catch (InvalidKeySpecException e) {
                throw new KeyException("could not convert key spec into either RSA/DSA Private Key", e);
            }

        } catch (NoSuchAlgorithmException e) {
            throw new KeyException("could not get a RSA KeyFactory", e);
        }
        return privateKey;
    }

    private static PKCS8EncodedKeySpec getPkcs8EncodedKeySpec(ByteArrayPrivateKey byteArrayPrivateKey, String privateKeyPassword)
            throws KeyException {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = null;
        if (privateKeyPassword == null || privateKeyPassword.isEmpty()) {
            pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(byteArrayPrivateKey.getBytes());
            return pkcs8EncodedKeySpec;
        }

        try {
            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(byteArrayPrivateKey.getBytes());

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
            char[] privateKeyPasswordChars = privateKeyPassword.toCharArray();
            PBEKeySpec keySpec = new PBEKeySpec(privateKeyPasswordChars);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, encryptedPrivateKeyInfo.getAlgParameters());
            pkcs8EncodedKeySpec = encryptedPrivateKeyInfo.getKeySpec(cipher);
            return pkcs8EncodedKeySpec;

        } catch (IOException | InvalidKeySpecException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
                | NoSuchPaddingException e) {
            throw new KeyException(e);
        }
    }

    /**
     * Convert a private key from base 64 encoded string to A PrivateKey object.
     *
     * @param privateKey
     * @return
     * @throws KeyException
     */
    public static ByteArrayPrivateKey getByteArrayPrivateKeyFromDelimitedString(String privateKey) throws KeyException {
        Matcher matcher = PRIVATE_KEY_PATTERN_WITH_DELIMITER.matcher(privateKey.trim());

        if (!matcher.matches()) {
            throw new KeyException("Invalid private key found " + privateKey.trim());
        }
        return getByteArrayPrivateKeyFromBase64String(matcher.group(1));
    }

    public static ByteArrayPrivateKey getByteArrayPrivateKeyFromBase64String(String privateKeyBase64String) throws KeyException {
        Matcher matcher = PRIVATE_KEY_BINARY_PATTERN.matcher(privateKeyBase64String.trim());
        if (!matcher.matches()) {
            throw new KeyException("Invalid private key found " + privateKeyBase64String);
        }
        byte[] bytes = DatatypeConverter.parseBase64Binary(privateKeyBase64String);
        return new ByteArrayPrivateKey().setBytes(bytes);
    }

    public static boolean isDelimited(String pkString) {
        Matcher matcher = PRIVATE_KEY_PATTERN_WITH_DELIMITER.matcher(pkString.trim());
        if (matcher.matches()) {
            return true;
        }
        return false;
    }
}
