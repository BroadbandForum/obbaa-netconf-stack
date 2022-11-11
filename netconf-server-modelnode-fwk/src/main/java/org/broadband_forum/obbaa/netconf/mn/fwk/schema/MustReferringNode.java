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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.DefaultConcurrentHashMap;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

// This class maintain must referring nodes based on component id
public class MustReferringNode {

    private String m_componentId;
    private DefaultConcurrentHashMap<SchemaPath, Map<SchemaPath, Expression>> m_mustReferringNodes = new DefaultConcurrentHashMap<>(new HashMap<SchemaPath, Expression>(), true);

    public MustReferringNode(String componentId) {
        this.m_componentId = componentId;
    }

    public void registerMustReferringNodes(SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        Map<SchemaPath, Expression> referringNodes = m_mustReferringNodes.get(nodeSchemaPath);
        if(referringNodes != null) {
            referringNodes.put(referencedSchemaPath, JXPathUtils.getExpression(accessPath));
        }
    }

    public String getComponentId() {
        return m_componentId;
    }

    public Map<SchemaPath, Expression> getMustReferringNodes(SchemaPath nodeSchemaPath) {
        return m_mustReferringNodes.get(nodeSchemaPath);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_componentId == null) ? 0 : m_componentId.hashCode());
        result = prime * result + ((m_mustReferringNodes == null) ? 0 : m_mustReferringNodes.hashCode());
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
        MustReferringNode other = (MustReferringNode) obj;
        if (m_componentId == null) {
            if (other.m_componentId != null)
                return false;
        } else if (!m_componentId.equals(other.m_componentId))
            return false;
        if (!m_mustReferringNodes.equals(other.m_mustReferringNodes))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MustReferringNode [m_componentId=" + m_componentId + ", m_mustReferringNodes=" + m_mustReferringNodes
                + "]";
    }
}
