package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.ModelNodeHelperDeployer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public abstract class AbstractModelNodeHelperDeployer implements ModelNodeHelperDeployer {

    private SchemaRegistry m_schemaRegistry;
    
    public AbstractModelNodeHelperDeployer(SchemaRegistry schemaRegistry) {
        m_schemaRegistry = schemaRegistry;
    }
    
    
    protected boolean isKeyLeafSchemaNode(SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode) {
        DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(parentSchemaPath);
        if(parentNode instanceof ListSchemaNode){
            return ((ListSchemaNode)parentNode).getKeyDefinition().contains(leafSchemaNode.getQName());
        }
        return false;
    }
}
