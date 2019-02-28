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

package org.broadband_forum.obbaa.netconf.api.transport;

import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;

import java.net.InetSocketAddress;

public class SshNetconfTransport extends NetconfTransport {
    private InetSocketAddress m_socketAddr;
    private String m_hostKeyPath;
    private int m_heartBeatIntervalSecs = 0;

    /**
     * ssh heart beat interval in seconds.
     * 
     * @return
     */
    public int getHeartBeatInterval() {
        return m_heartBeatIntervalSecs;
    }

    /**
     * ssh heart beat interval in seconds.
     * 
     * @return
     */
    public SshNetconfTransport setHeartBeatInterval(int heartBeatIntervalSecs) {
        m_heartBeatIntervalSecs = heartBeatIntervalSecs;
        return this;
    }

    @Override
    public String getTranportProtocol() {
        return NetconfTransportProtocol.SSH.name();
    }

    public InetSocketAddress getSocketAddress() {
        return this.m_socketAddr;
    }

    public SshNetconfTransport setSocketAddrress(InetSocketAddress socketAddr) {
        this.m_socketAddr = socketAddr;
        return this;
    }

    public String getHostKeyPath() {
        return this.m_hostKeyPath;
    }

    public SshNetconfTransport setHostKeyPath(String hostKeyPath) {
        this.m_hostKeyPath = hostKeyPath;
        return this;
    }
}
