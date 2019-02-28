package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmContainerModelNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

import java.util.Map;

/**
 * Created by keshava on 2/2/16.
 */
public class RootEntityContainerModelNodeHelper extends DsmContainerModelNodeHelper {
    private final SubSystemRegistry m_subsystemRegistry;
    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    /**
     * @param schemaNode
     * @param modelNodeHelperRegistry
     * @param subsystemRegistry
     * @param schemaRegistry
     * @param modelNodeDSM
     */
    public RootEntityContainerModelNodeHelper(ContainerSchemaNode schemaNode, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                              SubSystemRegistry subsystemRegistry,
                                              SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDSM) {
        super(schemaNode, modelNodeDSM);
        m_subsystemRegistry = subsystemRegistry;
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
    }

    @Override
    public ModelNode createChild(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs){
        ModelNodeId parentId = new ModelNodeId();
        ModelNodeWithAttributes newNode = new ModelNodeWithAttributes(m_schemaNode.getPath(), parentId, m_modelNodeHelperRegistry,
                m_subsystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        newNode.setAttributes(keyAttrs);
        return m_modelNodeDSM.createNode(newNode, parentId);
    }

    @Override
    public void deleteChild(ModelNode parentNode){
        m_modelNodeDSM.removeNode(parentNode, new ModelNodeId());
    }
}
