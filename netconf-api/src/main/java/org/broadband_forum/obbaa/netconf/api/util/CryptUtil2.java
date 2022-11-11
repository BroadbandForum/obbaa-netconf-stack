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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;

import com.google.common.annotations.VisibleForTesting;

public class CryptUtil2 {
    private static final String ENCODING = "UTF-8";
    public static final Pattern ENCR_STR_PATTERN = Pattern.compile("(\\$\\-([0-9])+\\$(.+))");
    private static Map<Integer, CipherSpec> c_cipherSpecs = new LinkedHashMap<>();

    public static final String NC_SECRET_KEY_PATH = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("NC_SECRET_KEY_PATH",null);
    private String m_keyFilePathForTest = null;
    private static boolean c_disabled = false;

    public void initFile() {
        String ncSecretKeyPath = getNcSecretKeyPath();
        if (ncSecretKeyPath != null) {
            File keyFile = new File(ncSecretKeyPath);
            if (keyFile.isFile()) {
                try {
                    loadKeys(keyFile);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to load keys from " + NC_SECRET_KEY_PATH);
                }
            } else {
                throw new RuntimeException("Not a valid file " + NC_SECRET_KEY_PATH);
            }
        } else {
            c_disabled = true;
        }
    }

    protected void loadKeys(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String keyIndexStr;
            while((keyIndexStr=br.readLine())!=null && keyIndexStr.length()!=0){
                int keyIndex = Integer.parseInt(keyIndexStr);
                ByteBuffer decoded = getKeyBytesSafely(br);
                byte[] array = decoded.array();
                //clear
                //clearByteBuffer(decoded);

                SecretKeySpec keySpec = new SecretKeySpec(array,"AES");
                //clear
               // Arrays.fill(array, (byte)0);

                int ivlength = Integer.parseInt(br.readLine());
                String transformation = br.readLine();
                c_cipherSpecs.put(keyIndex, new CipherSpec(keySpec, ivlength, transformation));
            }

        }
    }

    private static ByteBuffer getKeyBytesSafely(BufferedReader br) throws IOException {
        char[] temp = new char[1000];
        int offset = 0;
        br.read(temp, offset, 1);
        while(temp[offset] != '\n') {
            offset++;
            br.read(temp, offset, 1);
        }

        char [] keyChars = Arrays.copyOf(temp, offset);
        //clear
        Arrays.fill(temp, '0');

        CharBuffer charBuffer = CharBuffer.wrap(keyChars);

        ByteBuffer byteBuffer = Charset.forName(ENCODING).encode(charBuffer);

        //clear
        Arrays.fill(keyChars, '0');
        clearCharBuffer(keyChars, charBuffer);

        ByteBuffer decoded = Base64.getDecoder().decode(byteBuffer);
        clearByteBuffer(byteBuffer);
        return decoded;
    }

    private static void clearCharBuffer(char[] keyChars, CharBuffer charBuffer) {
        charBuffer.clear();
        charBuffer.put(keyChars);
        charBuffer.clear();
    }

    private static void clearByteBuffer(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.put(new byte[byteBuffer.limit()]);
        byteBuffer.clear();
    }

    public static String encrypt(String data) {
        if(c_disabled){
            return data;
        }
        Set<Integer> keyIndices = c_cipherSpecs.keySet();
        if(keyIndices.isEmpty()){
            throw new RuntimeException("Encryption key not found");
        }
        int keyIndex = keyIndices.iterator().next(); // Pick the first key. When key rotation is supported, this would be made configurable
        return encrypt(keyIndex, data);
    }

    protected static String encrypt(int keyIndex, String data) {
        if(c_disabled){
            return data;
        }
        byte[] dataBytes = new byte[0];
        try {
            dataBytes = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return encrypt(keyIndex, dataBytes);
    }

    private static String encrypt(int keyIndex, byte[] dataBytes) {
        Cipher cipher = c_cipherSpecs.get(keyIndex).getEncrCipher();
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("$-"+keyIndex+"$");
            byte[] iv = cipher.getIV();
            byte[] encrBytes = cipher.doFinal(dataBytes);
            byte[] merged = new byte[iv.length + encrBytes.length];

            System.arraycopy(iv,0,merged,0         ,iv.length);
            System.arraycopy(encrBytes,0,merged,iv.length,encrBytes.length);
            sb.append(Base64.getEncoder().encodeToString(merged));
            return sb.toString();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String decrypt(String data) {
        if(c_disabled){
            return data;
        }
        Matcher matcher = ENCR_STR_PATTERN.matcher(data);
        if(matcher.matches()){
            int keyIndex  = Integer.valueOf(matcher.group(2));
            byte[] ivEncr = Base64.getDecoder().decode(matcher.group(3).getBytes());
            byte [] decry = c_cipherSpecs.get(keyIndex).decrypt(ivEncr);
            return new String(decry);
        }
        throw new IllegalArgumentException("Cannot decrypt the string "+data);
    }

    public static CipherSpec getEncrCipherSpec() {
        return c_cipherSpecs.get(0);
    }

    @VisibleForTesting
    public static Map<Integer, CipherSpec> getEncrCipherSpecs() {
        return c_cipherSpecs;
    }

    @VisibleForTesting
    public static void unloadKeys() {
        c_cipherSpecs.clear();
    }

    private String getNcSecretKeyPath() {
        return m_keyFilePathForTest != null ? m_keyFilePathForTest : NC_SECRET_KEY_PATH;
    }

    @VisibleForTesting
    public void setKeyFilePathForTest(String keyFilePathForTest) {
        m_keyFilePathForTest = keyFilePathForTest;
    }
}
