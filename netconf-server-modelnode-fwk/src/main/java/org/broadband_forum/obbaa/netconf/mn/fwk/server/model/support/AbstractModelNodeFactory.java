package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;

import org.opendaylight.yangtools.yang.common.QName;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * This is factory that builds Nodes that extend {@link HelperDrivenModelNode}s.
 * This is not a abstract factory.
 *
 *
 */
public class AbstractModelNodeFactory implements ModelNodeFactory{
    public static final String NAME = "AbstractModelNodeFactory";
    @Override
    public ModelNode getModelNode(Class<? extends ModelNode> nodeType, ModelNode parent, ModelNodeId parentNodeId,
                                  ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subSystemRegistry,
                                  SchemaRegistry schemaRegistry, Map<QName, ConfigLeafAttribute> keyAttributes, Object... constructorArgs) throws ModelNodeCreateException {
        HelperDrivenModelNode newNode;
        try {
            List<Object> arguments = new ArrayList<Object>();
            arguments.add(parent);
            arguments.add(parentNodeId);
            for(Object argument : constructorArgs) {
                arguments.add(argument);
            }
            arguments.add(modelNodeHelperRegistry);
            arguments.add(subSystemRegistry);
            arguments.add(schemaRegistry);
            newNode = (HelperDrivenModelNode)nodeType.getConstructors()[0].newInstance(arguments.toArray());
            newNode.setKeyAttributes(keyAttributes);
            return newNode;
        } catch (InstantiationException | IllegalAccessException | SetAttributeException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            throw new ModelNodeCreateException("Cannot instantiate :"+nodeType, e);
        }
      
    }

}
