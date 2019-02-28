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

import org.broadband_forum.obbaa.netconf.api.util.CryptHashUtil;
import org.broadband_forum.obbaa.netconf.api.util.HashAlgorithm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CryptHashUtilTest {

    private static final String ID = "5"; // SHA-256
    private static final String SALT = "[B@7a808c1f";
    private static final String ENCRYPTED_STRING = "e7214595124ba2cb09b983da434a9353d3e6f12bb82b1973c0fb4669090d77d5";

    private static final String SHA256_FULLY_ENCRYPTED_STRING = "$5$[B@7a808c1f$e7214595124ba2cb09b983da434a9353d3e6f12bb82b1973c0fb4669090d77d5";
    private static final String MD5_FULLY_ENCRYPTED_STRING = "$1$[B@7a808c1f$e7214595124ba2cb09b983da434a9353d3e6f12bb82b1973c0fb4669090d77d5";

    @Test
    public void testBuildEncryptedString() {
        String encryptedString = CryptHashUtil.buildEncryptedString("123456", SALT, HashAlgorithm.SHA_256);
        assertEquals(ENCRYPTED_STRING, encryptedString);
    }

    @Test
    public void testBuildFullyEncryptedString() {
        String fullyEncryptedString = CryptHashUtil.buildFullyEncryptedString(HashAlgorithm.SHA_256, SALT, ENCRYPTED_STRING);
        assertEquals(SHA256_FULLY_ENCRYPTED_STRING, fullyEncryptedString);

        fullyEncryptedString = CryptHashUtil.buildFullyEncryptedString(HashAlgorithm.MD5, SALT, ENCRYPTED_STRING);
        assertEquals(MD5_FULLY_ENCRYPTED_STRING, fullyEncryptedString);

    }

    @Test
    public void testGetEncryptedString() {
        String actuaEncryptedString = CryptHashUtil.getEncryptedString(SHA256_FULLY_ENCRYPTED_STRING);
        assertEquals(ENCRYPTED_STRING, actuaEncryptedString);
    }

    @Test
    public void testGetSalt() {
        String actualSalt = CryptHashUtil.getSalt(SHA256_FULLY_ENCRYPTED_STRING);
        assertEquals(SALT, actualSalt);
    }
    
    @Test
    public void testGetNewSalt() {
        String newSalt = CryptHashUtil.getNewSalt();
        assertNotNull(newSalt);
    }

    @Test
    public void testGetID() {
        String id = CryptHashUtil.getId(SHA256_FULLY_ENCRYPTED_STRING);
        assertEquals(ID, id);
    }
}
