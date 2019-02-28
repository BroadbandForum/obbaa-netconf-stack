package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Map;

/**
 * Helper used to retrieve/create/delete/update a container node.
 */
public interface ChildContainerHelper extends ConstraintHelper {

	ModelNode getValue(ModelNode node) throws ModelNodeGetException;
	
	SchemaPath getChildModelNodeSchemaPath();

    void deleteChild(ModelNode parentNode) throws ModelNodeDeleteException;
    
    ModelNode createChild(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs) throws ModelNodeCreateException;
    
    ModelNode setValue(ModelNode parentNode, ModelNode childNode) throws ModelNodeSetException;
    
    public DataSchemaNode getSchemaNode();
    
}