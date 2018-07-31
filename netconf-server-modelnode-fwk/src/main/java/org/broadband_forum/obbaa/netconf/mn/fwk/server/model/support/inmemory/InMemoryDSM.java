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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.MNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Created by pgorai on 2/25/16.
 */
public class InMemoryDSM implements ModelNodeDataStoreManager {
    public static final String DEFAULT_DS_NAME = "default";
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(InMemoryDSM.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");
    private String m_dsmName;
    ConcurrentHashMap<SchemaPath, Map<InMemoryNodeKey, ModelNodeWithAttributes>> m_nodes = new ConcurrentHashMap<>();
    private final SchemaRegistry m_schemaRegistry;
    //An index to to retrieve children of a given node faster
    private ConcurrentHashMap<ModelNodeId, ConcurrentHashMap<SchemaPath, List<ModelNodeWithAttributes>>> m_childNodes
            = new ConcurrentHashMap<>();

    public InMemoryDSM(SchemaRegistry schemaRegistry) {
        this(schemaRegistry, DEFAULT_DS_NAME);
    }

    public String getDsmName() {
        return m_dsmName;
    }

    public void setDsmName(String dsmName) {
        m_dsmName = dsmName;
    }

    public InMemoryDSM(SchemaRegistry schemaRegistry, String dsmName) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_dsmName = dsmName;
    }

    @Override
    public void beginModify() {

    }

    @Override
    public void endModify() {

    }

    @Override
    public List<ModelNode> listNodes(SchemaPath nodeType) {
        LOGGER.debug("DSM: {} -listNodes with childType: {}", m_dsmName, nodeType);
        List<ModelNode> listNodes = new ArrayList<>();
        Map<InMemoryNodeKey, ModelNodeWithAttributes> nodesOfType = m_nodes.get(nodeType);
        if (nodesOfType != null) {
            for (Map.Entry<InMemoryNodeKey, ModelNodeWithAttributes> key : nodesOfType.entrySet()) {
                listNodes.add(key.getValue());
            }
        }
        return listNodes;
    }

    @Override
    public List<ModelNode> listChildNodes(SchemaPath childType, ModelNodeId parentId) throws DataStoreException {
        LOGGER.debug("DSM: {} -listChildNodes with childType: {} parentId: {}", m_dsmName, childType, parentId);
        ConcurrentHashMap<SchemaPath, List<ModelNodeWithAttributes>> allChildren = getAllChildren(parentId);
        if (allChildren != null) {
            List<ModelNodeWithAttributes> childrenOfType = getChildrenOfType(childType, allChildren);
            List<ModelNode> nodesToReturn = new ArrayList<>();
            nodesToReturn.addAll(childrenOfType);
            return nodesToReturn;
        }
        return null;
    }

    @Override
    public ModelNode findNode(SchemaPath nodeType, ModelNodeKey key, ModelNodeId parentId) throws DataStoreException {
        LOGGER.debug("DSM: {} -findNode with nodeType: {} key: {} parentId: {}", m_dsmName, nodeType, key, parentId);
        InMemoryNodeKey inMemoryNodeKey = new InMemoryNodeKey(key, parentId);
        Map<InMemoryNodeKey, ModelNodeWithAttributes> nodesOfType = getNodesOfType(nodeType);
        if (nodesOfType != null) {
            ModelNode node = nodesOfType.get(inMemoryNodeKey);
            return node;
        }
        return null;
    }

    @Override
    public List<ModelNode> findNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId
            parentId) throws DataStoreException {
        LOGGER.debug("DSM: {} -findNodes with nodeType: {} matchCriteria: {} parentId: {}", m_dsmName, nodeType,
                matchCriteria, parentId);
        List<ModelNode> nodes = new ArrayList<>();
        List<ModelNodeWithAttributes> childNodesOfType = getChildrenOfType(nodeType, getAllChildren(parentId));
        if (childNodesOfType != null) {
            for (ModelNodeWithAttributes node : childNodesOfType) {
                if (MNKeyUtil.isMatch(matchCriteria, node, m_schemaRegistry)) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        return createNode(modelNode, parentId, -1);
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId, int insertIndex) throws DataStoreException {
        LOGGER.debug("DSM: {} -createNode with modelNode: {} parentId: {} insertIndex: {]", m_dsmName, modelNode,
                parentId,
                insertIndex);
        checkType(modelNode);
        ModelNodeWithAttributes modelNodeWithAttr = (ModelNodeWithAttributes) modelNode;
        Map<InMemoryNodeKey, ModelNodeWithAttributes> nodesOfType = getNodesOfType(modelNode.getModelNodeSchemaPath());
        nodesOfType.put(getNodeKey(modelNode, parentId), modelNodeWithAttr);
        updateChildIndex(parentId, modelNodeWithAttr, insertIndex);
        return modelNode;
    }

    private void checkType(ModelNode modelNode) throws DataStoreException {
        if (!(modelNode instanceof ModelNodeWithAttributes) || modelNode == null) {
            throw new DataStoreException("Can work with only ModelNodeWithAttributes type");
        }
    }

    private void updateChildIndex(ModelNodeId parentId, ModelNodeWithAttributes childNode, int insertIndex) {
        ConcurrentHashMap<SchemaPath, List<ModelNodeWithAttributes>> allChildren = getAllChildren(parentId);
        //all children can be null when the node being added is root node
        if (allChildren != null) {
            List<ModelNodeWithAttributes> childrenOfType = getChildrenOfType(childNode.getModelNodeSchemaPath(),
                    allChildren);
            if (insertIndex >= 0 && insertIndex < childrenOfType.size()) {
                childrenOfType.add(insertIndex, childNode);
            } else {
                childrenOfType.add(childNode);
            }
        }
    }

    private List<ModelNodeWithAttributes> getChildrenOfType(SchemaPath modelNodeSchemaPath,
                                                            ConcurrentHashMap<SchemaPath,
                                                                    List<ModelNodeWithAttributes>> allChildren) {
        List<ModelNodeWithAttributes> childrenOfType = allChildren.get(modelNodeSchemaPath);
        if (childrenOfType == null) {
            childrenOfType = new ArrayList<>();
            allChildren.putIfAbsent(modelNodeSchemaPath, childrenOfType);
            childrenOfType = allChildren.get(modelNodeSchemaPath);
        }
        return childrenOfType;
    }

    private ConcurrentHashMap<SchemaPath, List<ModelNodeWithAttributes>> getAllChildren(ModelNodeId parentId) {
        if (parentId == null) {
            parentId = ModelNodeId.EMPTY_NODE_ID;
        }
        ConcurrentHashMap<SchemaPath, List<ModelNodeWithAttributes>> children = m_childNodes.get(parentId);
        if (children == null) {
            children = new ConcurrentHashMap<>();
            m_childNodes.putIfAbsent(parentId, children);
            children = m_childNodes.get(parentId);
        }
        return children;
    }

    private InMemoryNodeKey getNodeKey(ModelNode modelNode, ModelNodeId parentId) {
        InMemoryNodeKey key = new InMemoryNodeKey(MNKeyUtil.getModelNodeKey(modelNode, m_schemaRegistry), parentId);
        return key;
    }

    private Map<InMemoryNodeKey, ModelNodeWithAttributes> getNodesOfType(SchemaPath nodeType) {
        Map<InMemoryNodeKey, ModelNodeWithAttributes> nodesOfType = m_nodes.get(nodeType);
        if (nodesOfType == null) {
            nodesOfType = new HashMap<>();
            m_nodes.putIfAbsent(nodeType, nodesOfType);
            nodesOfType = m_nodes.get(nodeType);
        }
        return nodesOfType;
    }

    @Override
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute> configAttributes,
                           Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode)
            throws DataStoreException {
        updateNode(modelNode, parentId, configAttributes, leafListAttributes, -1, removeNode);
    }

    @Override
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute> configAttributes,
                           Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, int insertIndex,
                           boolean removeNode) throws DataStoreException {
        LOGGER.debug("DSM: {} -updateNode called with modelNode: {} parentId: {} configAttributes: {} " +
                        "leafListAttributes: {} insertIndex: {}",
                m_dsmName, modelNode, parentId, configAttributes, leafListAttributes, insertIndex);
        ModelNodeWithAttributes freshNode = (ModelNodeWithAttributes) findNode(modelNode.getModelNodeSchemaPath(),
                MNKeyUtil.getModelNodeKey(modelNode, m_schemaRegistry), parentId);
        if (freshNode != null) {
            if (configAttributes != null) {
                freshNode.updateConfigAttributes(configAttributes);
            }

            if (leafListAttributes != null) {
                if (removeNode) {
                    freshNode.removeLeafListAttributes(leafListAttributes);
                } else {
                    freshNode.updateLeafListAttributes(leafListAttributes);
                }
            }
        }

        Iterator<Map.Entry<QName, ConfigLeafAttribute>> configLeafAttrIterator = freshNode.getAttributes().entrySet()
                .iterator();
        while (configLeafAttrIterator.hasNext()) {
            Map.Entry<QName, ConfigLeafAttribute> configAttributeMap = configLeafAttrIterator.next();
            if (configAttributeMap.getValue() == null) {
                configLeafAttrIterator.remove();
            }
        }
    }

    @Override
    public void removeNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        LOGGER.debug("DSM: {} -removeNode called with modelNode: {} parentId: {} ", m_dsmName, modelNode, parentId);
        //remove the node and its children
        removeNodeInternal(modelNode, parentId);
        //update the parent node's index that the child is removed
        removeNodeFromChildNodeIndex(modelNode, parentId);

    }

    /**
     * remove the modelNode and all its children
     *
     * @param modelNode
     * @param parentId
     * @throws DataStoreException
     */
    private void removeNodeInternal(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        InMemoryNodeKey inMemoryNodeKey = getNodeKey(modelNode, parentId);
        Map<InMemoryNodeKey, ModelNodeWithAttributes> nodesOfType = getNodesOfType(modelNode.getModelNodeSchemaPath());
        if (nodesOfType != null) {
            nodesOfType.remove(inMemoryNodeKey);
        }
        removeAllChildren(modelNode);
    }

    /**
     * remove all children of a given node.
     *
     * @param modelNode
     * @throws DataStoreException
     */
    private void removeAllChildren(ModelNode modelNode) throws DataStoreException {
        if (modelNode == null) {
            return;
        }
        Map<SchemaPath, List<ModelNodeWithAttributes>> nodesToRemove = m_childNodes.get(modelNode.getModelNodeId());
        if (nodesToRemove != null) {
            for (List<ModelNodeWithAttributes> nodesOfType : nodesToRemove.values()) {
                for (ModelNodeWithAttributes node : nodesOfType) {
                    removeNodeInternal(node, node.getParentNodeId());
                }
            }
        }
        m_childNodes.remove(modelNode.getModelNodeId());
    }

    /**
     * remove the node from parent's child index.
     *
     * @param modelNode
     * @param parentId
     */
    private void removeNodeFromChildNodeIndex(ModelNode modelNode, ModelNodeId parentId) {
        Map<SchemaPath, List<ModelNodeWithAttributes>> siblings = m_childNodes.get(parentId);
        if (siblings != null) {
            List<ModelNodeWithAttributes> siblingsOfType = siblings.get(modelNode.getModelNodeSchemaPath());
            if (siblingsOfType != null) {
                siblingsOfType.remove(modelNode);
            }
        }
    }

    @Override
    public void removeAllNodes(ModelNode parentNode, SchemaPath nodeType, ModelNodeId grandParentId) throws
            DataStoreException {
        LOGGER.debug("DSM: {} -removeAllNodes called with modelNode: {} childQname: {} parentId: {} ", m_dsmName,
                parentNode, nodeType,
                grandParentId);
        Map<SchemaPath, List<ModelNodeWithAttributes>> allChildren = m_childNodes.get(parentNode.getModelNodeId());
        if (allChildren != null) {
            List<ModelNodeWithAttributes> childrenToRemove = allChildren.get(nodeType);

            if (childrenToRemove != null) {
                for (ModelNodeWithAttributes childToBeRemoved : childrenToRemove) {
                    removeNodeInternal(childToBeRemoved, parentNode.getModelNodeId());
                }
            }
            m_childNodes.remove(parentNode.getModelNodeId());
        }

    }

}
