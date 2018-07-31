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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Netconf stack interacts with ModelNodeDataStoreManager It abstracts entity or persistence information by providing
 * ModelNode to entity mapping and vice versa.
 * This is achieved by read and write methods which are used by helpers.
 */
public interface ModelNodeDataStoreManager {
    /**
     * Begin callback to DSMs to optimize DS modify operation.
     * DSMs can choose to cache nodes to avoid multiple writes to the backing storage.
     */
    void beginModify();

    /**
     * End modify callback to DSMs.
     * DSMs that choose to optimize modify operation should synchronise the modified changes with the backing store
     * on this callback.
     */
    void endModify();

    List<ModelNode> listNodes(SchemaPath nodeType) throws DataStoreException;

    List<ModelNode> listChildNodes(SchemaPath childType, ModelNodeId parentId) throws DataStoreException;

    ModelNode findNode(SchemaPath nodeType, ModelNodeKey key, ModelNodeId parentId) throws DataStoreException;

    List<ModelNode> findNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId
            parentId) throws DataStoreException;

    ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException;

    ModelNode createNode(ModelNode modelNode, ModelNodeId parentId, int insertIndex) throws DataStoreException;

    void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute> configAttributes,
                    Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode) throws
            DataStoreException;

    void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute> configAttributes,
                    Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, int insertIndex, boolean
                            removeNode) throws DataStoreException;

    void removeNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException;

    void removeAllNodes(ModelNode parentNode, SchemaPath nodeType, ModelNodeId grandParentId) throws DataStoreException;
}
