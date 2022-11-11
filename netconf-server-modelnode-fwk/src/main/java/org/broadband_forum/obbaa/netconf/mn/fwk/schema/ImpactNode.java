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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ImpactNode {
    private final SchemaPath m_schemaPath;
    private final ReferringNodes m_impactNodeMap;

    public ImpactNode(SchemaPath schemaPath) {
        m_schemaPath = schemaPath;
        m_impactNodeMap = new ReferringNodes(schemaPath);
    }

    public synchronized void addImpactNodes(ReferringNode referringNode) {
        m_impactNodeMap.put(referringNode);
    }

    public synchronized ReferringNodes getImpactNodes() {
        return m_impactNodeMap;
    }

    public SchemaPath getNodeSchemaPath() {
        return m_schemaPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_schemaPath == null) ? 0 : m_schemaPath.hashCode());
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
        ImpactNode other = (ImpactNode) obj;
        if (m_schemaPath == null) {
            if (other.m_schemaPath != null)
                return false;
        } else if (!m_schemaPath.equals(other.m_schemaPath))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ImpactNode [m_schemaPath=" + m_schemaPath + ", m_impactNodeMap=" + m_impactNodeMap + "]";
    }

    
}
