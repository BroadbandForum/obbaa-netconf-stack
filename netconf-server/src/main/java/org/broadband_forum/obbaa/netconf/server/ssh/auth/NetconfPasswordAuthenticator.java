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

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.authentication.FailureInfo;
import org.broadband_forum.obbaa.netconf.api.authentication.PointOfFailure;
import org.broadband_forum.obbaa.netconf.api.authentication.SuccessInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.AuthenticationResult;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.broadband_forum.obbaa.netconf.server.ssh.NetconfSubsystem;
import org.broadband_forum.obbaa.netconf.server.ssh.SshChannelListener;

/**
 * This class wraps around ssh password authenticators supplied by the netconf server. It also provides a call back to the supplied
 * AuthenticationListener when a authenticate succeeds or fails.
 * 
 *
 * 
 */
public final class NetconfPasswordAuthenticator implements PasswordAuthenticator {
    private final NetconfServerAuthenticationHandler m_axsNetconfAuthenticationHandler;
    private AuthenticationListener m_authenticationListener;
    private static final Logger LOGGER = Logger.getLogger(NetconfPasswordAuthenticator.class);

    /**
     * Creates a password authenticator.
     * 
     * @param axsNetconfAuthenticationHandler - password authenticator.
     * @param authenticationListener - authentication listener, pass nulll if you dont want to listen to authentication events
     */
    public NetconfPasswordAuthenticator(NetconfServerAuthenticationHandler axsNetconfAuthenticationHandler,
            AuthenticationListener authenticationListener) {
        this.m_axsNetconfAuthenticationHandler = axsNetconfAuthenticationHandler;
        this.m_authenticationListener = authenticationListener;
        
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        ClientAuthenticationInfo clientAuthInfo = new ClientAuthenticationInfo(session.getIoSession().getId(), username, password, null,
                null);
        SocketAddress remoteAddress = session.getIoSession().getRemoteAddress();
        SocketAddress localAddress = session.getIoSession().getLocalAddress();
        if (remoteAddress instanceof InetSocketAddress && localAddress instanceof InetSocketAddress) {
            clientAuthInfo = new ClientAuthenticationInfo(session.getIoSession().getId(), username, password, (InetSocketAddress)remoteAddress,
                    (InetSocketAddress) localAddress);
        }
        
        LOGGER.info("Authenticating client info with authentication handler: " + m_axsNetconfAuthenticationHandler);
        AuthenticationResult result = m_axsNetconfAuthenticationHandler.authenticate(clientAuthInfo);
        boolean authenticated = result.isAuthenticated();
        if(result.isAuthenticated()){
            session.setAttribute(NetconfSubsystem.SESSION_ID, result.getSessionId());
            m_axsNetconfAuthenticationHandler.registerServerSessionListener(result.getSessionId(), () -> {
                try {
                    session.close();
                } catch (IOException e) {
                    LOGGER.error("Error occurred when closing session", e);
                }
            });
        }
        session.addSessionListener(new SessionListener() {
            @Override
            public void sessionClosed(Session session) {
                clearSessionData(result.getSessionId());
            }

            @Override
            public void sessionException(Session session, Throwable t) {
                clearSessionData(result.getSessionId());
            }
        });
        session.addChannelListener(new SshChannelListener() {
            @Override
            public void channelClosed(Channel channel, Throwable reason) {
                super.channelClosed(channel, reason);
                clearSessionData(result.getSessionId());
            }
        });

        if (m_authenticationListener != null) {
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

    private void clearSessionData(Serializable sessionId) {
        if (sessionId != null) {
            m_axsNetconfAuthenticationHandler.logout(sessionId);
        }
        m_axsNetconfAuthenticationHandler.unregisterServerSessionListener(sessionId);
    }

}

