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

// This class maintain when referring nodes based on component id
public class WhenReferringNode {

    private String m_componentId;
    private DefaultConcurrentHashMap<SchemaPath, Map<SchemaPath, Expression>> m_whenReferringNodes = new DefaultConcurrentHashMap<>(new HashMap<SchemaPath, Expression>(), true);

    public WhenReferringNode(String componentId) {
        this.m_componentId = componentId;
    }

    public void registerWhenReferringNodes(SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        Map<SchemaPath, Expression> referringNodes = m_whenReferringNodes.get(nodeSchemaPath);
        referringNodes.put(referencedSchemaPath, JXPathUtils.getExpression(accessPath));
    }

    public String getComponentId() {
        return m_componentId;
    }

    public Map<SchemaPath, Expression> getWhenReferringNodes(SchemaPath nodeSchemaPath) {
        return m_whenReferringNodes.get(nodeSchemaPath);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_componentId == null) ? 0 : m_componentId.hashCode());
        result = prime * result + ((m_whenReferringNodes == null) ? 0 : m_whenReferringNodes.hashCode());
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
        WhenReferringNode other = (WhenReferringNode) obj;
        if (m_componentId == null) {
            if (other.m_componentId != null)
                return false;
        } else if (!m_componentId.equals(other.m_componentId))
            return false;
        if (m_whenReferringNodes == null) {
            if (other.m_whenReferringNodes != null)
                return false;
        } else if (!m_whenReferringNodes.equals(other.m_whenReferringNodes))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "WhenReferringNode [m_componentId=" + m_componentId + ", m_whenReferringNodes=" + m_whenReferringNodes
                + "]";
    }
}
