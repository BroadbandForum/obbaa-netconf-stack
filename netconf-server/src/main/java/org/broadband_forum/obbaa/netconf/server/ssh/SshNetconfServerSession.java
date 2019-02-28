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

package org.broadband_forum.obbaa.netconf.server.ssh;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.NetconfPasswordAuthenticator;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.NetconfPublicKeyAuthenticator;

import org.apache.log4j.Logger;
import org.apache.sshd.server.SshServer;

import java.io.IOException;

public class SshNetconfServerSession implements NetconfServerSession {

    private SshServer m_sshd;
    private static final Logger LOGGER = Logger.getLogger(SshNetconfServerSession.class);

    public SshNetconfServerSession(SshServer sshd) {
        this.m_sshd = sshd;
    }

    @Override
    public void killServer(boolean waitForTermination) {
        try {
            if (waitForTermination) {
                m_sshd.close(true).await();
            } else {
                m_sshd.stop();
            }
        } catch (IOException e) {
            LOGGER.error("Interrupted while shutting down the server ", e);
        }
    }

    public void setAuthenticationHandler(NetconfServerAuthenticationHandler authenticationHandler,
            AuthenticationListener authenticationListener) {
        m_sshd.setPasswordAuthenticator(new NetconfPasswordAuthenticator(authenticationHandler, authenticationListener));
        m_sshd.setPublickeyAuthenticator(new NetconfPublicKeyAuthenticator(authenticationHandler, authenticationListener));
    }
}
