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

package org.broadband_forum.obbaa.netconf.api.client;

import java.io.Serializable;

public class NetconfClientInfo {
    public static final String NON_INET_HOST = "non-inet-host";
    public static final String NON_INET_PORT = "non-inet-port";
    final String username;
    final int sessionId;
    private String m_remoteHost;
    private String m_remotePort;
    private Serializable m_clientSessionId;

    public NetconfClientInfo(String username, int sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }

    public NetconfClientInfo(String username, int sessionId, Serializable clientSessionId) {
        this.username = username;
        this.sessionId = sessionId;
        this.m_clientSessionId = clientSessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public NetconfClientInfo setRemoteHost(String remoteIp) {
        m_remoteHost = remoteIp;
        return this;
    }

    public String getRemoteHost() {
        return m_remoteHost;
    }

    public NetconfClientInfo setRemotePort(String remotePort) {
        m_remotePort = remotePort;
        return this;
    }

    public String getRemotePort() {
        return m_remotePort;
    }

    public boolean isInternalUser() {
        return false;
    }

    @Override
    public String toString() {
        return "NetconfClientInfo{" + "username='" + username + '\'' + ", sessionId=" + sessionId + ", " +
                "m_remoteHost='" + m_remoteHost
                + '\'' + ", m_remotePort='" + m_remotePort + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NetconfClientInfo that = (NetconfClientInfo) o;

        if (sessionId != that.sessionId) {
            return false;
        }
        return username.equals(that.username);

    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + sessionId;
        return result;
    }

    public Serializable getClientSessionId() {
        return m_clientSessionId;
    }
}
