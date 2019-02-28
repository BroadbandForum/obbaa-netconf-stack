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

import java.net.InetSocketAddress;

public class ClientAuthenticationInfo {
    private final long m_clientSessionId;
    private final String m_username;
    private final String m_password;
    private final InetSocketAddress m_inetAddressSource;
    private final  InetSocketAddress m_inetAddressDestination;

    public ClientAuthenticationInfo(long clientSessionId, String username, String password, InetSocketAddress inetAddressSource,
            InetSocketAddress inetDestinationAddress) {
        this.m_clientSessionId = clientSessionId;
        this.m_username = username;
        this.m_password = password;
        this.m_inetAddressSource = inetAddressSource;
        this.m_inetAddressDestination = inetDestinationAddress;
    }

    public long getClientSessionId() {
        return m_clientSessionId;
    }

    public String getUsername() {
        return m_username;
    }

    public String getPassword() {
        return m_password;
    }

    public InetSocketAddress getInetAddressSource() {
        return m_inetAddressSource;
    }

    public InetSocketAddress getInetAddressDestination() {
        return m_inetAddressDestination;
    }

    public String getSourceAddress() {
        return getInetAddressSource().getAddress().getHostAddress();
    }
    
    public String getDestinationAddress() {
        return getInetAddressDestination().getAddress().getHostAddress();
    }
    
    public int getSourcePort() {
        return getInetAddressSource().getPort();
    }
    
    public int getDestinationPort() {
        return getInetAddressDestination().getPort();
    }

}
