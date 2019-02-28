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

/**
 * A POJO class to store private key info such as the base 64 encoded string and the private key password. 
 * Created by keshava on 5/4/15.
 */
public class PrivateKeyInfo {
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private final String m_privateKeyString;
    private final String m_privateKeyPassword;

    public PrivateKeyInfo(String privateKeyString, String privateKeyPassword) {
        this.m_privateKeyString = privateKeyString;
        this.m_privateKeyPassword = privateKeyPassword;
    }

    public String getPrivateKeyString() {
        return m_privateKeyString;
    }

    public String getPrivateKeyPassword() {
        return m_privateKeyPassword;
    }

    public char[] getPrivateKeyPasswordChars() {
        if (m_privateKeyPassword != null) {
            return m_privateKeyPassword.toCharArray();
        } else {
            return EMPTY_CHAR_ARRAY;
        }
    }
}
