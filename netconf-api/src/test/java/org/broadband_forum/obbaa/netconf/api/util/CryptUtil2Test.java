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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Stopwatch;

public class CryptUtil2Test {
    private static final long RUNS = 10000;
    static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CryptUtil2Test.class, LogAppNames.NETCONF_LIB);
    private CryptUtil2 m_cryptUtil2;
    @Before
    public void setUp() throws IOException {
        String keyFilePath = getClass().getResource("/cryptutil2test/keyfile.plain").getPath();
        m_cryptUtil2 = new CryptUtil2();
        m_cryptUtil2.setKeyFilePathForTest(keyFilePath);
        m_cryptUtil2.initFile();
    }

    @After
    public void teardown(){
        m_cryptUtil2.unloadKeys();
    }

    @Test
    public void testEncryptAndDecrypt() {
        String encrStr = CryptUtil2.encrypt("password123");
        assertEquals("password123", CryptUtil2.decrypt(encrStr));

        encrStr = CryptUtil2.encrypt("artist@password1234");
        assertEquals("artist@password1234", CryptUtil2.decrypt(encrStr));

        encrStr = CryptUtil2.encrypt("name@gh$1123");
        assertEquals("name@gh$1123", CryptUtil2.decrypt(encrStr));
    }

    @Test
    public void testEncryptDecryptPerf() {
        Stopwatch encrSw = Stopwatch.createUnstarted();
        Stopwatch decrSw = Stopwatch.createUnstarted();
        Random rand = new Random();
        for (int i = 0; i < RUNS; i++) {
            String data = String.valueOf(rand.nextLong());
            encrSw.start();
            String encrStr = CryptUtil2.encrypt(data);
            encrSw.stop();

            decrSw.start();
            String decryptedData = CryptUtil2.decrypt(encrStr);
            decrSw.stop();

            assertTrue(encrStr.startsWith("$-0$"));
            assertEquals(data, decryptedData);
        }
        LOGGER.debug("Avg encryption time with " + RUNS + " runs, with 256 bit length " +
                "key, with " + CryptUtil2.getEncrCipherSpec().getTransformation() +
                " transformation is " + encrSw.elapsed(TimeUnit.MICROSECONDS) / RUNS +
                " microseconds, decryption avg time is " + decrSw.elapsed(TimeUnit.MICROSECONDS) / RUNS + " microseconds");
    }

    @Test
    public void testEncrLength() throws IOException {
        m_cryptUtil2.unloadKeys();
        m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_cbc.plain").getPath()));
        LOGGER.trace("Plain Text,Key length(bytes), IV length (bytes), transformation, Encrypted Text, ET Length");

        m_cryptUtil2.unloadKeys();
        m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_cbc.plain").getPath()));
        print();

        m_cryptUtil2.unloadKeys();
        m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_ctr.plain").getPath()));
        print();

        m_cryptUtil2.unloadKeys();
        m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_cbc_pkcs5.plain").getPath()));
        print();

        m_cryptUtil2.unloadKeys();
        m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_ctr_pkcs5.plain").getPath()));
        print();

        m_cryptUtil2.unloadKeys();
        m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_gcm.plain").getPath()));
        print();

        m_cryptUtil2.unloadKeys();
        m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_ccm.plain").getPath()));
        print();

    }

    @Test
    public void testKeyFileIncorrectFormat(){
        m_cryptUtil2.unloadKeys();
        try {
            m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_incorrect_format_keyIndex.plain").getPath()));
            fail("Expected exception not thrown ");
        } catch (Exception e) {
            assertTrue(e instanceof NumberFormatException);
            assertEquals("For input string: \"xyz\"", e.getMessage());
        }

        m_cryptUtil2.unloadKeys();
        try {
            m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_incorrect_format_key.plain").getPath()));
            fail("Expected exception not thrown ");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
            assertTrue(e.getMessage().contains("Key length not 128/192/256 bits."));
        }

        m_cryptUtil2.unloadKeys();
        try {
            m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_incorrect_format_invalid_ivlength.plain").getPath()));
            fail("Expected exception not thrown ");
        } catch (Exception e) {
            assertTrue(e instanceof NumberFormatException);
            assertEquals("For input string: \"yujbsd\"", e.getMessage());
        }

        m_cryptUtil2.unloadKeys();
        try {
            m_cryptUtil2.loadKeys(new File(CryptUtil2Test.class.getResource("/cryptutil2test/keyfile_incorrect_format_invalid_transformation.plain").getPath()));
            fail("Expected exception not thrown ");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
            assertEquals("java.security.NoSuchAlgorithmException: No such algorithm: some invalid algo", e.getMessage());
        }
    }

    @Test
    public void testFirstKeyIndexIsAlwaysUsed(){
        m_cryptUtil2.unloadKeys();
        String keyFilePath = getClass().getResource("/cryptutil2test/keyfile_keyIndex1.plain").getPath();
        m_cryptUtil2.setKeyFilePathForTest(keyFilePath);
        m_cryptUtil2.initFile();

        String encrStr = CryptUtil2.encrypt("password123");
        assertTrue(encrStr.startsWith("$-1$"));
        assertEquals("password123", CryptUtil2.decrypt(encrStr));

        m_cryptUtil2.unloadKeys();
        keyFilePath = getClass().getResource("/cryptutil2test/keyfile_keyIndex9.plain").getPath();
        m_cryptUtil2.setKeyFilePathForTest(keyFilePath);
        m_cryptUtil2.initFile();

        encrStr = CryptUtil2.encrypt("password123");
        assertTrue(encrStr.startsWith("$-9$"));
        assertEquals("password123", CryptUtil2.decrypt(encrStr));
    }

    @Test
    public void testEncryptBeforeLoadingKeysThrowsException(){
        m_cryptUtil2.unloadKeys();

        try{
            CryptUtil2.encrypt("password123");
            fail("Expected exception not thrown");
        }catch (Exception e){
            assertEquals("Encryption key not found" , e.getMessage());
        }
    }

    private void print() {
        String str = "abc";
        for (Map.Entry<Integer, CipherSpec> specEntry : CryptUtil2.getEncrCipherSpecs().entrySet()) {
            encryptAndPrint(specEntry, str);
        }
        str = "abcdef";
        for (Map.Entry<Integer, CipherSpec> specEntry : CryptUtil2.getEncrCipherSpecs().entrySet()) {
            encryptAndPrint(specEntry, str);
        }
        str = "abcdefghijklmnopqrstu";
        for (Map.Entry<Integer, CipherSpec> specEntry : CryptUtil2.getEncrCipherSpecs().entrySet()) {
            encryptAndPrint(specEntry, str);
        }
        str = "moreREALISTC#@1235ab6";
        for (Map.Entry<Integer, CipherSpec> specEntry : CryptUtil2.getEncrCipherSpecs().entrySet()) {
            encryptAndPrint(specEntry, str);
        }
    }

    private void encryptAndPrint(Map.Entry<Integer, CipherSpec> specEntry, String str) {
        CipherSpec cipherSpec = specEntry.getValue();
        String encryptStr = CryptUtil2.encrypt(specEntry.getKey(), str);
        LOGGER.trace(str + "," + specEntry.getKey() + "," + cipherSpec.getIvlength() + "," + cipherSpec.getTransformation() + "," + encryptStr + "," + encryptStr.length());
    }

}
