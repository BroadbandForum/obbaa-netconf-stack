/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @author keshava
 */
public class AbstractModelNodeFactory implements ModelNodeFactory {
    public static final String NAME = "AbstractModelNodeFactory";

    @Override
    public ModelNode getModelNode(Class<? extends ModelNode> nodeType, ModelNode parent, ModelNodeId parentNodeId,
                                  ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subSystemRegistry,
                                  SchemaRegistry schemaRegistry, Map<QName, ConfigLeafAttribute> keyAttributes,
                                  Object... constructorArgs) throws ModelNodeCreateException {
        HelperDrivenModelNode newNode;
        try {
            List<Object> arguments = new ArrayList<Object>();
            arguments.add(parent);
            arguments.add(parentNodeId);
            for (Object argument : constructorArgs) {
                arguments.add(argument);
            }
            arguments.add(modelNodeHelperRegistry);
            arguments.add(subSystemRegistry);
            arguments.add(schemaRegistry);
            newNode = (HelperDrivenModelNode) nodeType.getConstructors()[0].newInstance(arguments.toArray());
            newNode.setKeyAttributes(keyAttributes);
            return newNode;
        } catch (InstantiationException | IllegalAccessException | SetAttributeException | SecurityException |
                IllegalArgumentException | InvocationTargetException e) {
            throw new ModelNodeCreateException("Cannot instantiate :" + nodeType, e);
        }

    }

}
