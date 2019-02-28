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

package org.broadband_forum.obbaa.netconf.api.util;

import org.opendaylight.yangtools.yang.common.QName;

// only contains a QName for now, but separate class created for extensibility
public class DataPathLevel {
    
    private final QName m_qname;
    private final int m_hash;

    public DataPathLevel(QName qname) {
        m_qname = qname;
        m_hash = computeHashCode();
    }
    
    public QName getQName() {
        return m_qname;
    }

    @Override
    public int hashCode() {
        return m_hash;
    }

    private int computeHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_qname == null) ? 0 : m_qname.hashCode());
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
        DataPathLevel other = (DataPathLevel) obj;
        if (m_qname == null) {
            if (other.m_qname != null)
                return false;
        } else if (!m_qname.equals(other.m_qname))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DataPathLevel [m_qname=" + m_qname + "]";
    }
}
