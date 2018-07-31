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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmListModelNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;

/**
 * Created by sgs on 2/16/16.
 */
public class XmlListModelNodeHelper extends DsmListModelNodeHelper {

    /**
     * For UT
     */
    private int m_insertIndex;

    public XmlListModelNodeHelper(ListSchemaNode schemaNode, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                  ModelNodeDataStoreManager modelNodeDSM, SchemaRegistry schemaRegistry,
                                  SubSystemRegistry
                                          subSystemRegistry) {
        super(schemaNode, modelNodeHelperRegistry, modelNodeDSM, schemaRegistry, subSystemRegistry);
    }

    @Override
    public ModelNode addChild(ModelNode parentNode, String childUri, Map<QName, ConfigLeafAttribute> keyAttrs,
                              Map<QName, ConfigLeafAttribute> configAttrs)
            throws ModelNodeCreateException {

        // needs to be LinkedHashMap to preserve the insert order
        // https://tools.ietf.org/html/rfc6020#section-7.8.5 : keys need to be put first
        Map<QName, ConfigLeafAttribute> allAttributes = new LinkedHashMap<>();
        allAttributes.putAll(keyAttrs);
        allAttributes.putAll(configAttrs);

        ModelNode reloadedParentNode = null;
        reloadedParentNode = XmlDsmUtils.reloadParentNode(parentNode, m_modelNodeDSM, m_schemaRegistry);
        SchemaRegistry schemaRegistry = parentNode.getSchemaRegistry();
        SubSystemRegistry subSystemRegistry = ((HelperDrivenModelNode) parentNode).getSubSystemRegistry();
        ModelNodeHelperRegistry modelNodeHelperRegistry = ((HelperDrivenModelNode) parentNode)
                .getModelNodeHelperRegistry();
        XmlModelNodeImpl newNode;
        if (reloadedParentNode instanceof XmlModelNodeImpl) {
            newNode = new XmlModelNodeImpl(m_schemaNode.getPath(), allAttributes, Collections.EMPTY_LIST,
                    (XmlModelNodeImpl) reloadedParentNode,
                    reloadedParentNode.getModelNodeId(), null, modelNodeHelperRegistry, schemaRegistry,
                    subSystemRegistry, m_modelNodeDSM);
        } else {
            newNode = new XmlModelNodeImpl(m_schemaNode.getPath(), allAttributes, Collections.EMPTY_LIST, null,
                    parentNode.getModelNodeId(),
                    null, modelNodeHelperRegistry, schemaRegistry, subSystemRegistry, m_modelNodeDSM);
        }

        m_modelNodeDSM.createNode(newNode, parentNode.getModelNodeId());
        return newNode;
    }

    @Override
    public ModelNode addChildByUserOrder(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs, Map<QName,
            ConfigLeafAttribute> configAttrs,
                                         InsertOperation
                                                 insertOperation, ModelNode indexNode) throws ModelNodeCreateException {

        Map<QName, ConfigLeafAttribute> allAttributes = new LinkedHashMap<>();
        allAttributes.putAll(keyAttrs);
        allAttributes.putAll(configAttrs);
        SchemaRegistry schemaRegistry = ((HelperDrivenModelNode) parentNode).getSchemaRegistry();
        SubSystemRegistry subSystemRegistry = ((HelperDrivenModelNode) parentNode).getSubSystemRegistry();
        ModelNodeHelperRegistry modelNodeHelperRegistry = ((HelperDrivenModelNode) parentNode)
                .getModelNodeHelperRegistry();

        parentNode = XmlDsmUtils.reloadParentNode(parentNode, m_modelNodeDSM, m_schemaRegistry);

        XmlModelNodeImpl childNode;
        if (parentNode instanceof XmlModelNodeImpl) {
            childNode = new XmlModelNodeImpl(m_schemaNode.getPath(), allAttributes, Collections.<Element>emptyList(),
                    (XmlModelNodeImpl) parentNode, parentNode.getModelNodeId(), null, modelNodeHelperRegistry,
                    schemaRegistry, subSystemRegistry, m_modelNodeDSM);
        } else {
            childNode = new XmlModelNodeImpl(m_schemaNode.getPath(), allAttributes, Collections.<Element>emptyList(),
                    null, parentNode.getModelNodeId(), null, modelNodeHelperRegistry, schemaRegistry,
                    subSystemRegistry, m_modelNodeDSM);
        }
        return addChildByUserOrder(parentNode, childNode, indexNode, insertOperation);
    }

    @Override
    public ModelNode addChildByUserOrder(ModelNode parentNode, ModelNode childNode, ModelNode indexNode,
                                         InsertOperation insertOperation) throws ModelNodeCreateException {
        try {
            Collection<ModelNode> childList = getValue(parentNode, Collections.<QName, ConfigLeafAttribute>emptyMap());
            m_insertIndex = getChildInsertIndex(childList, insertOperation, indexNode);
            m_modelNodeDSM.createNode(childNode, parentNode.getModelNodeId(), m_insertIndex);
        } catch (ModelNodeGetException e) {
            throw new ModelNodeCreateException("could not add child ModelNode to parent", e);
        }
        return childNode;
    }

    /**
     * For UT
     */
    int getInsertIndex() {
        return m_insertIndex;
    }
}
