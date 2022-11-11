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

package org.broadband_forum.obbaa.netconf.api.util;


import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class CryptHashUtil {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CryptHashUtil.class, LogAppNames.NETCONF_LIB);

    private static final String SHA1PRNG_ALGORITHM = "SHA1PRNG";
    private static final String DOLLAR = "$";
    private static final int ID_INDEX = 1;
    private static final int SALT_INDEX = 2;
    private static final int ENCRYPTED_TEXT_INDEX = 3;

    private static String getValueFromIndex(String fullyEncryptedString, int index) {
        String[] value = fullyEncryptedString.split("\\" + DOLLAR);
        if (value.length != 4) {
            LOGGER.error("Encrypted String is invalid : " + fullyEncryptedString);
        } else {
            return value[index];
        }
        return "";
    }

    public static String getSalt(String fullyEncryptedString) {
        return getValueFromIndex(fullyEncryptedString, SALT_INDEX);
    }

    public static String getId(String fullyEncryptedString) {
        return getValueFromIndex(fullyEncryptedString, ID_INDEX);
    }

    public static String getEncryptedString(String fullyEncryptedString) {
        return getValueFromIndex(fullyEncryptedString, ENCRYPTED_TEXT_INDEX);
    }

    public static HashAlgorithm findAlgorithmById(String id) {
        for (HashAlgorithm algorithm : HashAlgorithm.values()) {
            if (algorithm.getId().equals(id)) {
                return algorithm;
            }
        }
        return HashAlgorithm.SHA_256;
    }

    public static String buildFullyEncryptedString(HashAlgorithm algorithm, String salt, String encryptedString) {
        return DOLLAR + algorithm.getId() + DOLLAR + salt + DOLLAR + encryptedString;
    }

    public static boolean match(String plainString, String fullyEncryptedString) {
        String salt = getSalt(fullyEncryptedString);
        String id = getId(fullyEncryptedString);
        HashAlgorithm algorithm = findAlgorithmById(id);

        String encryptedString = getEncryptedString(fullyEncryptedString);
        String encryptedStringFromInput = buildEncryptedString(plainString, salt, algorithm);

        if (encryptedStringFromInput.equals(encryptedString)) {
            return true;
        }
        return false;
    }

    public static String buildFullyEncryptedStringFromPlainText(String plainText, HashAlgorithm algorithm) {
        String salt = CryptHashUtil.getNewSalt();
        String encryptedPassword = CryptHashUtil.buildEncryptedString(plainText, salt, algorithm);
        String fullyEncryptedPassword = CryptHashUtil.buildFullyEncryptedString(algorithm, salt, encryptedPassword);
        return fullyEncryptedPassword;
    }

    public static String buildEncryptedString(String stringToCrypt, String salt, HashAlgorithm algorithm) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm.getName());
            md.update(salt.getBytes());
            byte[] bytes = md.digest(stringToCrypt.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Algorithm Exception ", e);
        }
        return generatedPassword;
    }

    // Get new salt
    public static String getNewSalt() {
        byte[] salt = new byte[16];
        try {
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(salt);
            return salt.toString();
        } catch (Exception e) {
            LOGGER.error("Algorithm Exception ", e);
        }
        new Random().nextBytes(salt);
        return salt.toString();
    }
}