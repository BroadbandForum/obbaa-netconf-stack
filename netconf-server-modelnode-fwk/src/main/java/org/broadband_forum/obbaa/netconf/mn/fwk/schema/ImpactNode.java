package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ImpactNode
{
    
    private SchemaPath m_schemaPath;    
    private Map<SchemaPath, Expression> m_impactNodeMap = new HashMap<>();

    public ImpactNode(SchemaPath schemaPath){
        m_schemaPath = schemaPath;
    }
    
    public synchronized void addImpactNodes(SchemaPath referencedSchemaPath, Expression expression){
        m_impactNodeMap.put(referencedSchemaPath, expression);
    }
    
    public synchronized Map<SchemaPath, Expression> getImpactNodes(){
        return m_impactNodeMap; 
    }     
    
    public SchemaPath getNodeSchemaPath(){
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
    
}
