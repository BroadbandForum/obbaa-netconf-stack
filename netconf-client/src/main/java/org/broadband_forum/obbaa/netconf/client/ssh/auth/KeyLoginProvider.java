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

package org.broadband_forum.obbaa.netconf.client.ssh.auth;

import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.authentication.Login;
import org.broadband_forum.obbaa.netconf.api.client.authentication.LoginKey;

public class KeyLoginProvider implements NetconfLoginProvider {

    private String m_username;
    private String m_pubKey;
    private String m_privKey;

    public KeyLoginProvider(String userName, String pubKey, String privKey) {
        this.m_username = userName;
        this.m_pubKey = pubKey;
        this.m_privKey = privKey;
    }

    @Override
    public Login getLogin() {
        return null;
    }

    @Override
    public LoginKey getKeyLogin() {
        return new LoginKey(m_username, m_pubKey, m_privKey);
    }

    public String getUserName() {
        return m_username;
    }

    public void setUserName(String userName) {
        this.m_username = userName;
    }

}
