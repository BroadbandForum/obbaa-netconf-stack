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

package org.broadband_forum.obbaa.netconf.api.server.auth;

import java.io.Serializable;
import java.security.PublicKey;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfigurationBuilder;

/**
 * An interface to authenticate netconf client logins. A Netconf server should provide an implementation of this interface via
 * {@link NetconfServerConfigurationBuilder#setAuthenticationHandler(NetconfServerAuthenticationHandler)} to authenticate client logins.
 * 
 *
 * 
 */
public interface NetconfServerAuthenticationHandler {
    Logger LOGGER = Logger.getLogger(NetconfServerAuthenticationHandler.class);

    AuthenticationResult authenticate(ClientAuthenticationInfo clientAuthInfo);

    AuthenticationResult authenticate(PublicKey pubKey);

    void logout(Serializable sessionId);

    void registerServerSessionListener(Serializable sessionId, NetconfServerSessionListener sessionListener);
    
    void unregisterServerSessionListener(Serializable sessionId);
    
    NetconfServerSessionListener getServerSessionListener(Serializable sessionId);
}

