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

package org.broadband_forum.obbaa.netconf.server.ssh.auth;

import java.security.PublicKey;

import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;

public final class KeyAuthHandler extends AbstractKeyAuthHandler {

    private String m_authKeyFile;
    private SshFileKeyValidataion m_sshFileKeyValidator = new SshFileKeyValidataion();

    public KeyAuthHandler(String authKeyFile) {
        this.m_authKeyFile = authKeyFile;
    }

    @Override
    public boolean authenticate(ClientAuthenticationInfo clientAuthInfo) {
        return false;
    }

    @Override
    public boolean authenticate(PublicKey pubKey) {

        String filePathAuthKey = Thread.currentThread().getContextClassLoader().getResource(m_authKeyFile).getPath();
        return m_sshFileKeyValidator.isValidPublicKey(filePathAuthKey, pubKey);
    }
}

