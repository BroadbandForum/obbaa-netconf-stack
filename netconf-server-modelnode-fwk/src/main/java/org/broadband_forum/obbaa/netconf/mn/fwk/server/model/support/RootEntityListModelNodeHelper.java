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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmListModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;

/**
 * Created by keshava on 2/2/16.
 */
public class RootEntityListModelNodeHelper extends DsmListModelNodeHelper {

    public RootEntityListModelNodeHelper(ListSchemaNode schemaNode, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                         ModelNodeDataStoreManager modelNodeDSM, SchemaRegistry schemaRegistry,
                                         SubSystemRegistry subsystemRegistry) {
        super(schemaNode, modelNodeHelperRegistry, modelNodeDSM, schemaRegistry, subsystemRegistry);
    }

    @Override
    public ModelNode addChild(ModelNode parentNode, String childUri, Map<QName, ConfigLeafAttribute> keyAttrs,
                              Map<QName, ConfigLeafAttribute> configAttrs) {
        ModelNodeWithAttributes newNode = new ModelNodeWithAttributes(m_schemaNode.getPath(), new ModelNodeId(),
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        Map<QName, ConfigLeafAttribute> allAttributes = new LinkedHashMap<>();
        allAttributes.putAll(keyAttrs);
        allAttributes.putAll(configAttrs);
        newNode.setAttributes(allAttributes);
        m_modelNodeDSM.createNode(newNode, parentNode.getModelNodeId());
        return newNode;
    }

    @Override
    public void removeAllChild(ModelNode parentNode) throws ModelNodeDeleteException {
        try {
            for (ModelNode child : getValue(parentNode, Collections.<QName, ConfigLeafAttribute>emptyMap())) {
                removeChild(parentNode, child);
            }
        } catch (ModelNodeGetException e) {
            throw new ModelNodeDeleteException(e);
        }
    }
}
