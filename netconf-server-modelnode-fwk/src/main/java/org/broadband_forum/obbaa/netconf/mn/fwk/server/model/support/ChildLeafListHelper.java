package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Collection;

/**
 * Helper used to retrieve/update leaf-list attributes.
 */
public interface ChildLeafListHelper extends ConstraintHelper {
    
    public Collection<ConfigLeafAttribute> getValue(ModelNode node) throws GetAttributeException;
	
	public boolean isConfiguration();

	SchemaPath getChildModelNodeSchemaPath();

    public void addChild(ModelNode instance, ConfigLeafAttribute value) throws SetAttributeException;

	public void removeChild(ModelNode instance, ConfigLeafAttribute value) throws ModelNodeDeleteException;
	
	public void removeAllChild(ModelNode instance) throws ModelNodeDeleteException;
	
	public void addChildByUserOrder(ModelNode instance, ConfigLeafAttribute value, String leafOperation, InsertOperation insertOperation) throws SetAttributeException, GetAttributeException;

	public LeafListSchemaNode getLeafListSchemaNode();
}