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

import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CipherSpec {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final SecretKeySpec m_keySpec;
    private final int m_ivlength;
    private final String m_transformation;

    public CipherSpec(SecretKeySpec keySpec, int ivlength, String transformation) {
        m_keySpec = keySpec;
        m_ivlength = ivlength;
        m_transformation = transformation;

        //validating input params
        getEncrCipher();
        getDecrCipher(new byte[m_ivlength]);
    }

    public Cipher getDecrCipher(byte[] ivEncr) throws IllegalStateException {
        try {
            Cipher cipher = Cipher.getInstance(m_transformation, BouncyCastleProvider.PROVIDER_NAME);
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(ivEncr, 0, m_ivlength));
            cipher.init(Cipher.DECRYPT_MODE, m_keySpec, ivSpec, new SecureRandom());
            return cipher;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private IvParameterSpec getIVSpec() {
        byte iv[] = new byte[m_ivlength];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv); // self-seeded randomizer to generate IV
        return new IvParameterSpec(iv);
    }

    public Cipher getEncrCipher() throws IllegalStateException {
        try {
            Cipher cipher = Cipher.getInstance(m_transformation, BouncyCastleProvider.PROVIDER_NAME);
            IvParameterSpec ivSpec = getIVSpec();
            cipher.init(Cipher.ENCRYPT_MODE, m_keySpec, ivSpec, new SecureRandom());
            return cipher;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] decrypt(byte[] ivEncr) {
        try {
            Cipher cipher = Cipher.getInstance(m_transformation, BouncyCastleProvider.PROVIDER_NAME);
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(ivEncr, 0, m_ivlength));
            cipher.init(Cipher.DECRYPT_MODE, m_keySpec, ivSpec, new SecureRandom());
            return cipher.doFinal(Arrays.copyOfRange(ivEncr, m_ivlength, ivEncr.length));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public int getIvlength() {
        return m_ivlength;
    }

    public String getTransformation() {
        return m_transformation;
    }
}
