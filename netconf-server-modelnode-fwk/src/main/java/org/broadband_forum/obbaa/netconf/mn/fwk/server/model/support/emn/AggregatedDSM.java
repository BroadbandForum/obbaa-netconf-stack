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


import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Looks up the DSM corresponding to nodeType registered in ModelNodeDSMRegistry and delegates to the corresponding DSM.
 */
public class AggregatedDSM implements ModelNodeDataStoreManager {

    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    public static final String DSM_NOT_REGISTERED_FOR = "DSM not registered for ";


    public AggregatedDSM(ModelNodeDSMRegistry modelNodeDSMRegistry) {
        m_modelNodeDSMRegistry = modelNodeDSMRegistry;
    }

    @Override
    public void beginModify() {
        for (ModelNodeDataStoreManager dsm : m_modelNodeDSMRegistry.getAllDSMs()) {
            dsm.beginModify();
        }
    }

    @Override
    public void endModify() {
        for (ModelNodeDataStoreManager dsm : m_modelNodeDSMRegistry.getAllDSMs()) {
            dsm.endModify();
        }
    }

    @Override
    public List<ModelNode> listNodes(SchemaPath nodeType) throws DataStoreException {
        return getModelNodeDSM(nodeType).listNodes(nodeType);
    }

    @Override
    public List<ModelNode> listChildNodes(SchemaPath childType, ModelNodeId parentId) throws DataStoreException {
        return getModelNodeDSM(childType).listChildNodes(childType, parentId);
    }

    @Override
    public ModelNode findNode(SchemaPath nodeType, ModelNodeKey key, ModelNodeId parentId) throws DataStoreException {
        return getModelNodeDSM(nodeType).findNode(nodeType, key, parentId);
    }

    @Override
    public List<ModelNode> findNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId
            parentId) throws DataStoreException {
        return getModelNodeDSM(nodeType).findNodes(nodeType, matchCriteria, parentId);
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        if (modelNode != null) {
            return getModelNodeDSM(modelNode.getModelNodeSchemaPath()).createNode(modelNode, parentId);
        }
        return null;
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId, int insertIndex) throws DataStoreException {
        if (modelNode != null) {
            return getModelNodeDSM(modelNode.getModelNodeSchemaPath()).createNode(modelNode, parentId, insertIndex);
        }
        return null;
    }

    @Override
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute>
            configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>>
            leafListAttributes, boolean removeNode) throws DataStoreException {
        if (modelNode != null) {
            getModelNodeDSM(modelNode.getModelNodeSchemaPath()).updateNode(modelNode, parentId, configAttributes,
                    leafListAttributes, removeNode);
        }
    }

    @Override
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute>
            configAttributes, Map<QName,
            LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, int insertIndex, boolean removeNode) throws
            DataStoreException {
        if (modelNode != null) {
            getModelNodeDSM(modelNode.getModelNodeSchemaPath()).updateNode(modelNode, parentId, configAttributes,
                    leafListAttributes, insertIndex, removeNode);
        }
    }

    @Override
    public void removeNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        if (modelNode != null) {
            getModelNodeDSM(modelNode.getModelNodeSchemaPath()).removeNode(modelNode, parentId);
        }
    }

    @Override
    public void removeAllNodes(ModelNode parentNode, SchemaPath nodeType, ModelNodeId grandParentId) throws
            DataStoreException {
        if (nodeType != null && parentNode != null) {
            getModelNodeDSM(nodeType).removeAllNodes(parentNode, nodeType, grandParentId);
        }
    }

    private ModelNodeDataStoreManager getModelNodeDSM(SchemaPath nodeType) throws DataStoreException {
        ModelNodeDataStoreManager dsm = m_modelNodeDSMRegistry.lookupDSM(nodeType);
        if (dsm == null) {
            throw new DsmNotRegisteredException(DSM_NOT_REGISTERED_FOR + nodeType);
        }
        return dsm;
    }

}
