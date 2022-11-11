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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.spi;

import java.io.Serializable;
import java.security.PublicKey;

import org.broadband_forum.obbaa.netconf.api.server.auth.AuthenticationResult;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerSessionListener;
import org.broadband_forum.obbaa.netconf.auth.spi.AuthenticationHandler;

/**
 * Created by keshava on 2/12/16.
 */
public class DefaultPasswordAuthenticationHandler implements AuthenticationHandler{
    private String m_username;
    private String m_password;
    @Override
    public AuthenticationResult authenticate(ClientAuthenticationInfo clientAuthInfo) {
        if (this.m_username != null && this.m_password != null) {
            if (this.m_username.equals(clientAuthInfo.getUsername()) && this.m_password.equals(clientAuthInfo.getPassword())) {
                return new AuthenticationResult(true, null);
            }
        }
        return AuthenticationResult.failedAuthResult();
    }

    @Override
    public AuthenticationResult authenticate(PublicKey pubKey) {
        //this does not support PK auth
        return AuthenticationResult.failedAuthResult();
    }

    @Override
    public void logout(Serializable sessionId) {
    	
    }

    public String getUsername() {
        return m_username;
    }

    public void setUsername(String username) {
        this.m_username = username;
    }

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        this.m_password = password;
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

