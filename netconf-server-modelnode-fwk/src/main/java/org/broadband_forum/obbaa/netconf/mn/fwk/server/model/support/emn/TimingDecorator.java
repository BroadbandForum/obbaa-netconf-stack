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


import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger.endPhase;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger.startPhase;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ListEntryInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class TimingDecorator extends XmlSubtreeDSM{

    private static ThreadLocal<DSMTimingLogger> m_timingLogger = ThreadLocal.withInitial(() -> new DSMTimingLogger());

    public TimingDecorator(PersistenceManagerUtil persistenceManagerUtil, EntityRegistry entityRegistry, SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry, ModelNodeDSMRegistry modelNodeDSMRegistry) {
        super(persistenceManagerUtil, entityRegistry, schemaRegistry, modelNodeHelperRegistry, subsystemRegistry, modelNodeDSMRegistry);
    }

    @Override
    public void beginModify() {
        try {
            startPhase("XmlSubtreeDSM.beginModify");
            super.beginModify();
        } finally {
            endPhase("XmlSubtreeDSM.beginModify");
        }
    }


    @Override
    public void endModify() {
        try {
            startPhase("XmlSubtreeDSM.endModify");
            super.endModify();
        } finally {
            endPhase("XmlSubtreeDSM.endModify");
        }
    }

    @Override
    public List<ModelNode> listNodes(SchemaPath nodeType, SchemaRegistry mountRegistry) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.listNodes");
            return super.listNodes(nodeType, mountRegistry);
        } finally {
            endPhase("XmlSubtreeDSM.listNodes");
        }
    }

    @Override
    public List<ModelNode> listChildNodes(SchemaPath childType, ModelNodeId parentId, SchemaRegistry mountRegistry) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.listChildNodes");
            return super.listChildNodes(childType, parentId, mountRegistry);
        } finally {
            endPhase("XmlSubtreeDSM.listChildNodes");
        }
    }

    @Override
    public ModelNodeWithAttributes findNode(SchemaPath nodeType, ModelNodeKey key, ModelNodeId parentId, SchemaRegistry mountRegistry) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.findNode");
            return super.findNode(nodeType, key, parentId, mountRegistry);
        } finally {
            endPhase("XmlSubtreeDSM.findNode");
        }
    }

    @Override
    public List<ModelNode> findNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId parentId, SchemaRegistry mountRegistry) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.findNodes");
            return super.findNodes(nodeType, matchCriteria, parentId, mountRegistry);
        } finally {
            endPhase("XmlSubtreeDSM.findNodes");
        }
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.createNode0");
            return super.createNode(modelNode,parentId);
        } finally {
            endPhase("XmlSubtreeDSM.createNode0");
        }
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId, int insertIndex) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.createNode1");
            return super.createNode(modelNode,parentId, insertIndex);
        } finally {
            endPhase("XmlSubtreeDSM.createNode1");
        }
    }

    @Override
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute> configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.updateNode0");
            super.updateNode(modelNode,parentId, configAttributes, leafListAttributes, removeNode);
        } finally {
            endPhase("XmlSubtreeDSM.updateNode0");
        }
    }

    @Override
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute> configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, int insertIndex, boolean removeNode) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.updateNode1");
            super.updateNode(modelNode,parentId, configAttributes, leafListAttributes, insertIndex, removeNode);
        } finally {
            endPhase("XmlSubtreeDSM.updateNode1");
        }
    }

    @Override
    public void removeNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.removeNode");
            super.removeNode(modelNode,parentId);
        } finally {
            endPhase("XmlSubtreeDSM.removeNode");
        }
    }

    @Override
    public void removeAllNodes(ModelNode parentNode, SchemaPath nodeType, ModelNodeId grandParentId) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.removeAllNodes");
            super.removeAllNodes(parentNode, nodeType, grandParentId);
        } finally {
            endPhase("XmlSubtreeDSM.removeAllNodes");
        }
    }

    @Override
    public boolean isChildTypeBigList(SchemaPath childType, SchemaRegistry mountRegistry) {
        try {
            startPhase("XmlSubtreeDSM.isChildTypeBigList");
            return super.isChildTypeBigList(childType, mountRegistry);
        } finally {
            endPhase("XmlSubtreeDSM.isChildTypeBigList");
        }
    }

    @Override
    public List<ListEntryInfo> findVisibleNodesLike(SchemaPath nodeType, ModelNodeId parentId, Map<QName, String> keysLike, int maxResults, SchemaRegistry mountRegistry) {
        try {
            startPhase("XmlSubtreeDSM.findNodesLike");
            return super.findVisibleNodesLike(nodeType, parentId, keysLike, maxResults, mountRegistry);
        } finally {
            endPhase("XmlSubtreeDSM.findNodesLike");
        }
    }

    @Override
    public List findByMatchValues(SchemaPath nodeType, Map<String, Object> matchValues, SchemaRegistry mountRegistry) {
        try {
            startPhase("XmlSubtreeDSM.findByMatchValues");
            return super.findByMatchValues(nodeType, matchValues, mountRegistry);
        } finally {
            endPhase("XmlSubtreeDSM.findByMatchValues");
        }
    }

    @Override
    public EntityRegistry getEntityRegistry(SchemaPath nodeType, SchemaRegistry mountRegistry) {
        try {
            startPhase("XmlSubtreeDSM.getEntityRegistry");
            return super.getEntityRegistry(nodeType, mountRegistry);
        } finally {
            endPhase("XmlSubtreeDSM.getEntityRegistry");
        }
    }

    @Override
    public void updateIndex(ModelNode modelNode, ModelNodeId parentId, int newIndex) {
        try {
            startPhase("XmlSubtreeDSM.updateIndex");
            super.updateIndex(modelNode, parentId, newIndex);
        } finally {
            endPhase("XmlSubtreeDSM.updateIndex");
        }
    }

}
