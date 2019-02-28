package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConstraintHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MandatoryAware;

public abstract class YangConstraintHelper implements ConstraintHelper{
	
	private DataSchemaNode m_dataSchemaNode;

	public YangConstraintHelper(DataSchemaNode dataSchemaNode) {
		m_dataSchemaNode = dataSchemaNode;
	}
	
	@Override
	public boolean isMandatory() {
	    if (m_dataSchemaNode instanceof MandatoryAware) {
	        return ((MandatoryAware) m_dataSchemaNode).isMandatory();
	    }
		return false;
	}

    protected ModelNodeWithAttributes getNewModelNode(ModelNode parentNode, ModelNodeDataStoreManager dsm) {
        ModelNodeWithAttributes newNode;
        if (parentNode.hasSchemaMount()) {
            SchemaMountRegistryProvider provider = (SchemaMountRegistryProvider) RequestScope.getCurrentScope()
                    .getFromCache(SchemaRegistryUtil.MOUNT_CONTEXT_PROVIDER);
            newNode = new ModelNodeWithAttributes(m_dataSchemaNode.getPath(), parentNode.getModelNodeId(),
                    provider.getModelNodeHelperRegistry(parentNode), provider.getSubSystemRegistry(parentNode),
                    provider.getSchemaRegistry(parentNode), dsm);
            newNode.setSchemaMountChild(true);
            newNode.setParentMountPath(parentNode.getModelNodeSchemaPath());
        } else {
            newNode = new ModelNodeWithAttributes(m_dataSchemaNode.getPath(), parentNode.getModelNodeId(),
                    ((HelperDrivenModelNode)parentNode).getModelNodeHelperRegistry(),((HelperDrivenModelNode)parentNode).getSubSystemRegistry(),
                    ((HelperDrivenModelNode)parentNode).getSchemaRegistry(), dsm);
        }
        return newNode;
    }


}
