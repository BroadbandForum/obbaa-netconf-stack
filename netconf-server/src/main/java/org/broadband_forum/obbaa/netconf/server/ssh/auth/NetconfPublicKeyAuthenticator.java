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

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.authentication.FailureInfo;
import org.broadband_forum.obbaa.netconf.api.authentication.PointOfFailure;
import org.broadband_forum.obbaa.netconf.api.authentication.SuccessInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PublicKey;

/**
 * This class wraps around ssh public key authenticators supplied by the netconf server. It also provides a call back to the supplied
 * AuthenticationListener when a authenticate succeeds or fails.
 * 
 * 
 * 
 */
public class NetconfPublicKeyAuthenticator implements PublickeyAuthenticator {

    private final NetconfServerAuthenticationHandler m_axsNetconfAuthenticationHandler;
    private AuthenticationListener m_authenticationListener;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NetconfPublicKeyAuthenticator.class, LogAppNames.NETCONF_LIB);

    /**
     * Creates a public key authenticator.
     * 
     * @param axsNetconfAuthenticationHandler - public key authenticator.
     * @param authenticationListener - authentication listener, pass nulll if you dont want to listen to authentication events
     */
    public NetconfPublicKeyAuthenticator(NetconfServerAuthenticationHandler axsNetconfAuthenticationHandler,
            AuthenticationListener authenticationListener) {
        this.m_axsNetconfAuthenticationHandler = axsNetconfAuthenticationHandler;
        this.m_authenticationListener = authenticationListener;
    }

    @Override
    public boolean authenticate(String paramString, PublicKey paramPublicKey, ServerSession session) {
        boolean authenticated = m_axsNetconfAuthenticationHandler.authenticate(paramPublicKey).isAuthenticated();

        if (m_authenticationListener != null) {
            SocketAddress remoteAddress = session.getIoSession().getRemoteAddress();
            if (remoteAddress instanceof InetSocketAddress) {
                InetSocketAddress inetSocAddr = (InetSocketAddress) remoteAddress;
                if (authenticated) {
                    m_authenticationListener.authenticationSucceeded(new SuccessInfo().setIp(inetSocAddr.getAddress().getHostAddress())
                            .setPort(inetSocAddr.getPort()));
                } else {
                    m_authenticationListener.authenticationFailed(new FailureInfo().setIp(inetSocAddr.getAddress().getHostAddress())
                            .setPort(inetSocAddr.getPort()).setPointOfFailure(PointOfFailure.client));
                }
            } else {
                LOGGER.error("The remote socket addres is not a InetSocketAddress, cannot determine source IP/port");
            }
        }
        return authenticated;

    }

}
