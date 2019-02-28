package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmContainerModelNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sgs on 2/16/16.
 */
public class XmlContainerModelNodeHelper extends DsmContainerModelNodeHelper {

    protected final SchemaRegistry m_schemaRegistry;

    public XmlContainerModelNodeHelper(ContainerSchemaNode schemaNode,ModelNodeDataStoreManager modelNodeDSM,
                                       SchemaRegistry schemaRegistry) {
        super(schemaNode, modelNodeDSM);
        m_schemaRegistry = schemaRegistry;
    }

    @Override
    public ModelNode createChild(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs) throws ModelNodeCreateException {
        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(parentNode, m_schemaRegistry);
        ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(parentNode, registry);
        ModelNode freshParentNode;
        if (parentNode instanceof XmlModelNodeImpl) {
            freshParentNode = m_modelNodeDSM.findNode(parentNode.getModelNodeSchemaPath(), modelNodeKey, ((XmlModelNodeImpl) parentNode).getParentNodeId());
        } else {
            freshParentNode = parentNode;
        }
        
        SchemaRegistry schemaRegistry = freshParentNode.getSchemaRegistry();
        SubSystemRegistry subSystemRegistry = ((HelperDrivenModelNode) freshParentNode).getSubSystemRegistry();
        ModelNodeHelperRegistry modelNodeHelperRegistry = ((HelperDrivenModelNode) freshParentNode).getModelNodeHelperRegistry();

        if (freshParentNode.hasSchemaMount()) {
            schemaRegistry = freshParentNode.getMountRegistry();
            subSystemRegistry = freshParentNode.getMountSubSystemRegistry();
            modelNodeHelperRegistry = freshParentNode.getMountModelNodeHelperRegistry();
        }
        
        XmlModelNodeImpl newNode;
        if (freshParentNode instanceof XmlModelNodeImpl) {
            newNode = new XmlModelNodeImpl(m_schemaNode.getPath(), keyAttrs, Collections.EMPTY_LIST, (XmlModelNodeImpl) freshParentNode, freshParentNode.getModelNodeId(), null, modelNodeHelperRegistry, schemaRegistry, subSystemRegistry, m_modelNodeDSM);
        } else {
            newNode = new XmlModelNodeImpl(m_schemaNode.getPath(), keyAttrs, Collections.EMPTY_LIST, null, freshParentNode.getModelNodeId(), null, modelNodeHelperRegistry, schemaRegistry, subSystemRegistry, m_modelNodeDSM);
        }
        
        m_modelNodeDSM.createNode(newNode, freshParentNode.getModelNodeId());
        return newNode;
    }
    
}
