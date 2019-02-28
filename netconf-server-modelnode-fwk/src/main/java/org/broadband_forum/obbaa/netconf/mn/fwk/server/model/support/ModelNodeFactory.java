package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;

import org.opendaylight.yangtools.yang.common.QName;

import java.util.Map;

public interface ModelNodeFactory {
    public ModelNode getModelNode(Class<? extends ModelNode> nodeType, ModelNode parent, ModelNodeId parentNodeId,
                                  ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subSystemRegistry,
                                  SchemaRegistry schemaRegistry, Map<QName, ConfigLeafAttribute> keyattributes, Object... constructorArgs) throws ModelNodeCreateException;
}
