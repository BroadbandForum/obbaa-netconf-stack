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

import java.io.Serializable;
import java.security.PublicKey;

import org.broadband_forum.obbaa.netconf.api.server.auth.AuthenticationResult;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerSessionListener;

public abstract class AbstractKeyAuthHandler implements NetconfServerAuthenticationHandler {

    @Override
    public AuthenticationResult authenticate(ClientAuthenticationInfo clientAuthInfo) {
        return AuthenticationResult.failedAuthResult();
    }

    @Override
    public abstract AuthenticationResult authenticate(PublicKey pubKey);

    @Override
    public void logout(Serializable sessionId) {
    	
    }

    @Override
    public void registerServerSessionListener(Serializable sessionId, NetconfServerSessionListener sessionListener) {
        
    }

    @Override
    public void unregisterServerSessionListener(Serializable sessionId) {
        
    }

    @Override
    public NetconfServerSessionListener getServerSessionListener(Serializable sessionId) {
        return null;
    }
}
