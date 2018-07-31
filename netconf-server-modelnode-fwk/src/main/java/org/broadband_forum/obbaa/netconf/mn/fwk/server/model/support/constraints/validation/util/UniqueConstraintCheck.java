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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.TreeMap;

import org.opendaylight.yangtools.yang.common.QName;

public class UniqueConstraintCheck {

    public TreeMap<QName, String> m_attributes = new TreeMap<>();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_attributes == null) ? 0 : m_attributes.hashCode());
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
        UniqueConstraintCheck other = (UniqueConstraintCheck) obj;
        if (m_attributes == null) {
            if (other.m_attributes != null)
                return false;
        } else if (!m_attributes.equals(other.m_attributes))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "UniqueConstraintCheck [m_attributes=" + m_attributes + "]";
    }
}
