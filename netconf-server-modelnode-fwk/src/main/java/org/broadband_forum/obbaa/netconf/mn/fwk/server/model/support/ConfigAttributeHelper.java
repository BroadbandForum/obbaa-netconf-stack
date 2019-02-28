package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 *  Helper used to retrieve/update configuration attributes of a node.
 */
public interface ConfigAttributeHelper extends ConstraintHelper {

	public Class<?> getAttributeType();

	SchemaPath getChildModelNodeSchemaPath();

	public String getDefault();

	public ConfigLeafAttribute getValue(ModelNode modelNode) throws GetAttributeException;

	public void setValue(ModelNode abstractModelNode,ConfigLeafAttribute attr) throws SetAttributeException;

	public void removeAttribute(ModelNode abstractModelNode);
	
}
