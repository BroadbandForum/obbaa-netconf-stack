package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmChildLeafListHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public class XmlChildLeafListHelper extends DsmChildLeafListHelper {
    public XmlChildLeafListHelper(LeafListSchemaNode leafListSchemaNode, QName qname, ModelNodeDataStoreManager modelNodeDSM, SchemaRegistry schemaRegistry) {
        super(leafListSchemaNode, qname, modelNodeDSM, schemaRegistry);
    }

    @Override
    public void addChildByUserOrder(ModelNode parentNode, ConfigLeafAttribute value, String operation, InsertOperation insertOperation) throws SetAttributeException, GetAttributeException {
        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(parentNode, m_schemaRegistry);
    	ModelNode reloadedNode = XmlDsmUtils.reloadParentNode(parentNode, m_modelNodeDSM, registry);
        super.addChildByUserOrder(reloadedNode, value, operation, insertOperation);
    }

    @Override
    public void removeChild(ModelNode modelNode, ConfigLeafAttribute value) throws ModelNodeDeleteException {
        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(modelNode, m_schemaRegistry);
    	ModelNode reloadedNode = XmlDsmUtils.reloadParentNode(modelNode, m_modelNodeDSM, registry);
        super.removeChild(reloadedNode, value);
    }
}
