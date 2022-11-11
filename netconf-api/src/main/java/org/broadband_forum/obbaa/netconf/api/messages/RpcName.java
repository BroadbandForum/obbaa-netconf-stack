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

package org.broadband_forum.obbaa.netconf.api.messages;

import java.io.Serializable;

public final class RpcName implements Serializable, Comparable<RpcName> {
    private static final long serialVersionUID = 5398411242927766414L;

    public static final String LEFT_PARENTHESIS = "(";
    public static final String RIGHT_PARENTHESIS = ")";

    private String m_namespace;
    private String m_rpcName;

    public RpcName(final String namespace, final String rpcName) {
        this.m_namespace = namespace;
        this.m_rpcName = rpcName;
    }

    public String getNamespace() {
        return m_namespace;
    }

    public String getName() {
        return m_rpcName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getNamespace() != null) {
            sb.append(LEFT_PARENTHESIS + getNamespace());

            sb.append(RIGHT_PARENTHESIS);
        }
        sb.append(m_rpcName);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_rpcName == null) ? 0 : m_rpcName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RpcName other = (RpcName) obj;
        if (m_namespace != null && other.m_namespace != null) {
            if (!m_namespace.equals(other.m_namespace)) {
                return false;
            }
        }
        if (m_rpcName == null) {
            if (other.m_rpcName != null) {
                return false;
            }
        } else if (!m_rpcName.equals(other.m_rpcName)) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(final RpcName other) {
        int result = m_rpcName.compareTo(other.m_rpcName);
        if (result != 0) {
            return result;
        }

        if (getNamespace() != null && other.getNamespace() != null) {
            result = getNamespace().compareTo(other.getNamespace());
            if (result != 0) {
                return result;
            }
        }
        return result;
    }

}
