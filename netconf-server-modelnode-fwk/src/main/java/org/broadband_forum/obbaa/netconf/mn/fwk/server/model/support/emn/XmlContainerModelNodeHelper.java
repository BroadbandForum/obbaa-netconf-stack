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

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmContainerModelNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sgs on 2/16/16.
 */
public class XmlContainerModelNodeHelper extends DsmContainerModelNodeHelper {

    protected final SchemaRegistry m_schemaRegistry;

    public XmlContainerModelNodeHelper(ContainerSchemaNode schemaNode,ModelNodeDataStoreManager modelNodeDSM,
                                       SchemaRegistry schemaRegistry) {
        super(schemaNode, modelNodeDSM,  schemaRegistry);
        m_schemaRegistry = schemaRegistry;
    }

    @Override
    public ModelNode createChild(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs) throws ModelNodeCreateException {
        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(parentNode, m_schemaRegistry);
        ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(parentNode, registry);
        ModelNode freshParentNode;
        if (parentNode instanceof XmlModelNodeImpl) {
            freshParentNode = m_modelNodeDSM.findNode(parentNode.getModelNodeSchemaPath(), modelNodeKey, ((XmlModelNodeImpl) parentNode).getParentNodeId(), parentNode.getSchemaRegistry());
        } else {
            freshParentNode = parentNode;
        }
        
        SchemaRegistry schemaRegistry = freshParentNode.getSchemaRegistry();
        SubSystemRegistry subSystemRegistry = ((HelperDrivenModelNode) freshParentNode).getSubSystemRegistry();
        ModelNodeHelperRegistry modelNodeHelperRegistry = ((HelperDrivenModelNode) freshParentNode).getModelNodeHelperRegistry();

        if (freshParentNode.hasSchemaMount()) {
            schemaRegistry = freshParentNode.getMountRegistry();
            subSystemRegistry = freshParentNode.getMountSubSystemRegistry();
            modelNodeHelperRegistry = freshParentNode.getMountModelNodeHelperRegistry();
        }
        
        XmlModelNodeImpl newNode;
        if (freshParentNode instanceof XmlModelNodeImpl) {
            newNode = new XmlModelNodeImpl(((XmlModelNodeImpl)parentNode).getDocument(), m_schemaNode.getPath(), keyAttrs, Collections.EMPTY_LIST, (XmlModelNodeImpl) freshParentNode,
                    freshParentNode.getModelNodeId(), null, modelNodeHelperRegistry, schemaRegistry, subSystemRegistry, m_modelNodeDSM, true, null, true, null);
        } else {
            newNode = new XmlModelNodeImpl(ConfigAttributeFactory.getDocument(), m_schemaNode.getPath(), keyAttrs, Collections.EMPTY_LIST, null, freshParentNode.getModelNodeId(),
                    null, modelNodeHelperRegistry, schemaRegistry, subSystemRegistry, m_modelNodeDSM, true, null, true, null);
        }
        
        m_modelNodeDSM.createNode(newNode, freshParentNode.getModelNodeId());
        return newNode;
    }
    
}
