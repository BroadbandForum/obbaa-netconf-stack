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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.CreateStrategy;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeSetException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class AnnotationChildContainerHelper extends AnnotationConstraintHelper implements ChildContainerHelper {

    private Method m_getterMethod;
    private Method m_setterMethod;
    private String m_name;
    @SuppressWarnings("unused")
    private CreateStrategy m_createStrategy;
    private String m_factoryName;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SchemaPath m_childSchemaPath;

    public AnnotationChildContainerHelper(String name, CreateStrategy createStrategy, String factoryName, Method
            getterMethod, Method setterMethod, SchemaPath childSchemaPath, ModelNodeHelperRegistry
            modelNodeHelperRegistry) {
        m_name = name;
        m_createStrategy = createStrategy;
        m_factoryName = factoryName;
        m_getterMethod = getterMethod;
        m_setterMethod = setterMethod;
        m_childSchemaPath = childSchemaPath;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
    }


    /**
     * For UT Only
     */
    protected Method getGetterMethod() {
        return m_getterMethod;
    }

    /**
     * For UT Only
     */
    protected Method getSetterMethod() {
        return m_setterMethod;
    }

    public AnnotationChildContainerHelper(Method m) {
        m_getterMethod = m;
    }

    public ModelNode getValue(ModelNode node) throws ModelNodeGetException {
        try {
            return (HelperDrivenModelNode) m_getterMethod.invoke(node);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ModelNodeGetException("Could not get child node from ModelNode: " + node, e);
        }
    }

    public void deleteChild(ModelNode parentNode) throws ModelNodeDeleteException {
        Object arg = null;
        try {
            m_setterMethod.invoke(parentNode, arg);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ModelNodeDeleteException("could not delete childNode from modelNode" + parentNode, e);
        }
    }

    @SuppressWarnings("unchecked")
    public ModelNode createChild(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs) throws
            ModelNodeCreateException {
        ModelNodeFactory factory = m_modelNodeHelperRegistry.getCreateFactory(m_factoryName);
        ModelNode newNode = factory.getModelNode((Class<? extends ModelNode>) m_getterMethod.getReturnType(),
                parentNode,
                new ModelNodeId(parentNode.getModelNodeId()), ((HelperDrivenModelNode) parentNode)
                        .getModelNodeHelperRegistry(),
                ((HelperDrivenModelNode) parentNode).getSubSystemRegistry(),
                ((HelperDrivenModelNode) parentNode).getSchemaRegistry(), keyAttrs);
        try {
            m_setterMethod.invoke(parentNode, newNode);
            return newNode;
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ModelNodeCreateException("could not create child node from ModelNode:" + parentNode, e);
        }
    }

    @Override
    public SchemaPath getChildModelNodeSchemaPath() {
        return m_childSchemaPath;
    }

    @Override
    public ModelNode setValue(ModelNode parentNode, ModelNode childNode) throws ModelNodeSetException {
        try {
            m_setterMethod.invoke(parentNode, childNode);
            return childNode;
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ModelNodeSetException("could not create child node from ModelNode:" + parentNode, e);
        }
    }


    @Override
    public DataSchemaNode getSchemaNode() {
        return null;
    }

}
