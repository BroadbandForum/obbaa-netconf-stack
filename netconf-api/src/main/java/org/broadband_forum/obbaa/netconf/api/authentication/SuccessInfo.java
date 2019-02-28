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

package org.broadband_forum.obbaa.netconf.api.authentication;

import java.security.cert.X509Certificate;

public class SuccessInfo {

    private String m_ip;

    private int m_port;
    
    private X509Certificate m_peerCertificate;

    public String getIp() {
        return m_ip;
    }

    public SuccessInfo setIp(String ip) {
        this.m_ip = ip;
        return this;
    }

    public int getPort() {
        return m_port;
    }

    public SuccessInfo setPort(int port) {
        this.m_port = port;
        return this;
    }

    public X509Certificate getPeerCertificate() {
        return m_peerCertificate;
    }

    public SuccessInfo setPeerCertificate(X509Certificate peerCertificate) {
        m_peerCertificate = peerCertificate;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_ip == null) ? 0 : m_ip.hashCode());
        result = prime * result + ((m_peerCertificate == null) ? 0 : m_peerCertificate.hashCode());
        result = prime * result + m_port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SuccessInfo other = (SuccessInfo) obj;
        if (m_ip == null) {
            if (other.m_ip != null)
                return false;
        } else if (!m_ip.equals(other.m_ip))
            return false;
        if (m_peerCertificate == null) {
            if (other.m_peerCertificate != null)
                return false;
        } else if (!m_peerCertificate.equals(other.m_peerCertificate))
            return false;
        if (m_port != other.m_port)
            return false;
        return true;
    }
}
