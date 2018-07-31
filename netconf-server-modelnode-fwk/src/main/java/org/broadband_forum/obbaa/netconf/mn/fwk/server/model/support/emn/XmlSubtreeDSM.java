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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.documentToPrettyString;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeToXmlMapper.nodesMatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InvalidIdentityRefException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * A ModelNodeDataStoreManager, which understands Subtree XML annotations.
 */
public class XmlSubtreeDSM extends AnnotationBasedModelNodeDataStoreManager {

    protected final EntityRegistry m_entityRegistry;
    private final PersistenceManagerUtil m_persistenceManagerUtil;
    private final SchemaRegistry m_schemaRegistry;
    protected final XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(XmlSubtreeDSM.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");
    private final RequestScopeXmlDSMCache m_dsmCache;
    private final ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    public XmlSubtreeDSM(PersistenceManagerUtil persistenceManagerUtil, EntityRegistry entityRegistry, SchemaRegistry
            schemaRegistry,
                         ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry,
                         ModelNodeDSMRegistry modelNodeDSMRegistry) {
        super(persistenceManagerUtil, entityRegistry, schemaRegistry, modelNodeHelperRegistry, subsystemRegistry,
                modelNodeDSMRegistry);
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_persistenceManagerUtil = persistenceManagerUtil;
        m_entityRegistry = entityRegistry;
        m_schemaRegistry = schemaRegistry;
        m_dsmCache = new RequestScopeXmlDSMCache();
        m_modelNodeDSMRegistry = modelNodeDSMRegistry;
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry,
                modelNodeHelperRegistry, subsystemRegistry, m_entityRegistry);
    }

    @Override
    public List<ModelNode> listNodes(SchemaPath nodeType) throws DataStoreException {
        Class entityClass = m_entityRegistry.getEntityClass(nodeType);
        if (entityClass != null) {
            return super.listNodes(nodeType);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("listNodes with nodeType: {}", nodeType);
            }
            List<ModelNode> modelNodes = new ArrayList<>();
            SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(nodeType);
            if (storedParentSchemaPath != null) {
                ModelNodeId storedGrandParentId = EMNKeyUtil.getParentIdFromSchemaPath(storedParentSchemaPath);
                //FIXME: FNMS-10112 This wont work
                // when stored parent is a list!
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode(storedParentSchemaPath,
                        storedGrandParentId,
                        storedGrandParentId); //FIXME:  FNMS-10112  This is clearly wrong
                if (storedParentModelNode != null) {
                    modelNodes = retrieveModelNodes(nodeType, storedParentModelNode, null);
                }
            }
            return modelNodes;
        }
    }

    @Override
    public List<ModelNode> listChildNodes(SchemaPath childType, ModelNodeId parentId) throws DataStoreException {
        Class entityClass = m_entityRegistry.getEntityClass(childType);
        if (entityClass != null) {
            if (m_entityRegistry.getYangXmlSubtreeGetter(entityClass) == null) {
                return super.listChildNodes(childType, parentId);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("listChildNodes with childType: {} parentId: {}", childType, parentId);
                }
                // This is a list of stored ModelNodes
                List<ModelNode> modelNodes = new ArrayList<>();
                Collection<Object> childEntities = super.getChildEntities(childType, parentId);
                for (Object entity : childEntities) {
                    XmlModelNodeImpl xmlModelNode = m_xmlModelNodeToXmlMapper.getModelNode(entity, this);
                    modelNodes.add(xmlModelNode);
                }
                return modelNodes;
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("listChildNodes with childType: {} parentId: {}", childType, parentId);
            }
            List<ModelNode> modelNodes = new ArrayList<>();
            SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(childType);
            ModelNodeId storedGrandParentId = EMNKeyUtil.scopeModelNodeId(m_schemaRegistry, storedParentSchemaPath
                    .getParent(), parentId);
            XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode(storedParentSchemaPath, parentId,
                    storedGrandParentId);
            if (storedParentModelNode != null) {
                modelNodes = retrieveModelNodes(childType, storedParentModelNode, parentId);
            }

            return modelNodes;
        }
    }

    @Override
    public ModelNodeWithAttributes findNode(SchemaPath nodeType, ModelNodeKey key, ModelNodeId parentId) throws
            DataStoreException {
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.findNode(nodeType, key, parentId);
        }

        LOGGER.debug("findNode with nodeType: {} key: {} parentId: {}", nodeType, key, parentId);
        ModelNodeId modelNodeId = EMNKeyUtil.getModelNodeId(key, parentId, nodeType);
        //SchemaPath of the parent which is stored as an Entity in DS
        SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(nodeType);
        //ParentId of the storedParent
        ModelNodeId storedGrandParentId = EMNKeyUtil.scopeModelNodeId(m_schemaRegistry, storedParentSchemaPath
                .getParent(), modelNodeId);
        XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode(storedParentSchemaPath, modelNodeId,
                storedGrandParentId);

        if (nodeType.equals(storedParentSchemaPath)) {
            return storedParentModelNode;
        } else if (storedParentModelNode != null) {
            return retrieveModelNode(nodeType, modelNodeId, storedParentModelNode);
        }
        return null;
    }

    @Override
    public List<ModelNode> findNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId
            parentId) throws DataStoreException {
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.findNodes(nodeType, matchCriteria, parentId);
        }
        List<ModelNode> nodes = new ArrayList<>();
        LOGGER.debug("findNodes with nodeType: {} matchCriteria: {} parentId: {}", nodeType, matchCriteria, parentId);

        if (MNKeyUtil.containsAllKeys(nodeType, matchCriteria, m_schemaRegistry)) {
            LOGGER.debug("all keys found in matchCriteria, for findNodes with nodeType: {} matchCriteria: {} " +
                            "parentId: {}", nodeType,
                    matchCriteria, parentId);
            ModelNodeKey key = MNKeyUtil.getKeyFromCriteria(nodeType, matchCriteria, m_schemaRegistry);
            ModelNodeWithAttributes node = findNode(nodeType, key, parentId);
            if (node != null) {
                nodes.add(node);
            }
        } else {
            LOGGER.debug("all keys not found in matchCriteria, for findNodes with nodeType: {} matchCriteria: {} " +
                            "parentId: {}",
                    nodeType, matchCriteria, parentId);
            //SchemaPath of the parent which is stored as an Entity in DS
            SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(nodeType);
            //ParentId of the storedParent
            ModelNodeId storedGrandParentId = EMNKeyUtil.scopeModelNodeId(m_schemaRegistry, storedParentSchemaPath
                    .getParent(), parentId);
            Class storedParentClass = m_entityRegistry.getEntityClass(storedParentSchemaPath);
            List<?> storedParentEntities;
            if (nodeType.equals(storedParentSchemaPath)) {
                //we cannot scope to a single parent here because the matchCriteria does not contain all keys
                storedParentEntities = getParentEntities(storedParentClass, matchCriteria, parentId);
                if (storedParentEntities != null) {
                    for (Object storedParentEntity : storedParentEntities) {
                        String yangXmlSubtree = getXmlSubtree(storedParentEntity, storedParentClass);
                        Element element = getXmlSubtreeElement(yangXmlSubtree);
                        Map<QName, ConfigLeafAttribute> configAttrsFromEntity = null;
                        try {
                            configAttrsFromEntity = XmlModelNodeToXmlMapperImpl.getConfigAttributesFromEntity
                                    (m_schemaRegistry, storedParentSchemaPath,
                                    m_entityRegistry, storedParentClass, storedParentEntity);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        List<XmlModelNodeImpl> storedParentModelNodes = m_xmlModelNodeToXmlMapper
                                .getModelNodeFromNodeSchemaPath(element,
                                configAttrsFromEntity, storedParentSchemaPath, storedGrandParentId, null, this);
                        for (XmlModelNodeImpl storedParentModelNode : storedParentModelNodes) {
                            fillNodes(nodeType, matchCriteria, parentId, nodes, storedParentSchemaPath,
                                    storedParentModelNode);
                        }
                    }
                }
            } else {
                //we can scope to a single stored parent here since we have parentId
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode(storedParentSchemaPath, parentId,
                        storedGrandParentId);
                fillNodes(nodeType, matchCriteria, parentId, nodes, storedParentSchemaPath, storedParentModelNode);
            }
        }
        //this is needed in case there are config attributes for the node as columns and as xml subtree content
        doFinalFilter(nodes, matchCriteria);
        return nodes;
    }

    private void fillNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId parentId,
                           List<ModelNode>
            nodes, SchemaPath storedParentSchemaPath, XmlModelNodeImpl storedParentModelNode) {
        if (storedParentModelNode != null) {
            if (nodeType.equals(storedParentSchemaPath)) {
                nodes.add(storedParentModelNode);
            } else {
                nodes.addAll(retrieveModelNodes(nodeType, matchCriteria, storedParentModelNode, parentId));
            }
        }
    }

    private void doFinalFilter(List<ModelNode> nodes, Map<QName, ConfigLeafAttribute> matchCriteria) {
        Iterator<ModelNode> nodeIterator = nodes.iterator();
        while (nodeIterator.hasNext()) {
            ModelNode node = nodeIterator.next();
            if (!MNKeyUtil.isMatch(matchCriteria, (ModelNodeWithAttributes) node, m_schemaRegistry)) {
                LOGGER.debug("node: {}, did not match the match criteria matchCriteria: {} ", node, matchCriteria);
                nodeIterator.remove();
            }
        }
    }

    private List<ModelNodeWithAttributes> retrieveModelNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute>
            matchCriteria, XmlModelNodeImpl
            storedParentModelNode, ModelNodeId parentId) {
        List<ModelNodeWithAttributes> nodes = new ArrayList<>(200);
        XmlModelNodeImpl parentModelNode = retrieveModelNode(nodeType.getParent(), parentId, storedParentModelNode);
        if (parentModelNode != null) {
            List<XmlModelNodeImpl> children = parentModelNode.getChildren().get(nodeType.getLastComponent());
            if (children != null) {
                for (XmlModelNodeImpl child : children) {
                    if (MNKeyUtil.isMatch(matchCriteria, child, m_schemaRegistry)) {
                        nodes.add(child);
                    }
                }
            }
        }
        return nodes;
    }

    protected List<? extends Object> getParentEntities(Class storedParentClass, Map<QName, ConfigLeafAttribute>
            matchCriteria, ModelNodeId parentId) throws
            DataStoreException {
        return getEntities(storedParentClass, matchCriteria, parentId);
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createNode : {} parentId: {}", modelNode.getModelNodeSchemaPath(), parentId);
        }
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        Class klass = m_entityRegistry.getEntityClass(schemaPath);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.createNode(modelNode, parentId);
        } else {
            if (modelNode.isRoot()) {
                XmlModelNodeImpl xmlmodelNode = m_xmlModelNodeToXmlMapper.getRootXmlModelNode(
                        (ModelNodeWithAttributes) modelNode, this);
                super.createNode(xmlmodelNode, parentId);
                markNodesToBeUpdated(modelNode.getModelNodeSchemaPath(), xmlmodelNode);
                return xmlmodelNode;
            }

            XmlModelNodeImpl parentModelNode = null;
            if (modelNode instanceof XmlModelNodeImpl) {
                parentModelNode = ((XmlModelNodeImpl) modelNode).getParentModelNode();
            }

            if (parentModelNode == null) {
                super.createNode(modelNode, parentId); // EntityModelNode DSM can handle this.
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath);
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) modelNode);
                if (storedParentModelNode != null) {
                    parentModelNode.addChild(modelNode.getQName(), (XmlModelNodeImpl) modelNode);
                    markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                }
            }
        }
        return modelNode;
    }

    @Override
    public void endModify() {
        LOGGER.debug("Updating the modified stored parent XML subtree nodes from cache into hibernate context");
        for (XmlModelNodeImpl nodeToBeUpdated : m_dsmCache.getNodesToBeUpdated()) {
            SchemaPath nodeType = nodeToBeUpdated.getModelNodeSchemaPath();
            Class storedParentClass = m_entityRegistry.getEntityClass(nodeType);
            ModelNodeId parentId = nodeToBeUpdated.getParentNodeId();
            ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(m_schemaRegistry, nodeType, nodeToBeUpdated
                    .getModelNodeId());
            Object storedParentEntity;
            storedParentEntity = getParentEntity(storedParentClass, modelNodeKey, parentId, LockModeType
                    .PESSIMISTIC_WRITE);
            Element xmlValue = m_xmlModelNodeToXmlMapper.getXmlValue(nodeToBeUpdated);
            String xmlSubtreeString = getXmlSubtreeString(getXmlSubtree(storedParentEntity, storedParentClass),
                    nodeToBeUpdated, xmlValue);
            if (xmlSubtreeString != null) {
                setXmlSubtree(storedParentEntity, storedParentClass, xmlSubtreeString);
            }
        }
        LOGGER.debug("Updating the modified stored parent XML subtree nodes from cache into hibernate context done");
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId, int insertIndex) throws DataStoreException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createNode :" + modelNode.getModelNodeSchemaPath() + " parentId:" + parentId);
        }
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        Class klass = m_entityRegistry.getEntityClass(schemaPath);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.createNode(modelNode, parentId, insertIndex);
        } else {
            XmlModelNodeImpl parentModelNode = null;
            if (modelNode instanceof XmlModelNodeImpl) {
                parentModelNode = ((XmlModelNodeImpl) modelNode).getParentModelNode();
            }

            if (parentModelNode == null) {
                super.createNode(modelNode, parentId, insertIndex); // EntityModelNode DSM can handle this.
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath);
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) modelNode);
                if (storedParentModelNode != null) {
                    parentModelNode.addChildAtSpecificIndex(modelNode.getQName(), (XmlModelNodeImpl) modelNode,
                            insertIndex);
                    markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                }
            }
        }
        return modelNode;
    }

    @Override
    @Transactional(value = TxType.REQUIRED, rollbackOn = {DataStoreException.class, RuntimeException.class, Exception
            .class})
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute>
            configAttributes, Map<QName,
            LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode) throws DataStoreException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("updateNode :" + modelNode.getModelNodeSchemaPath() + " parentId:" + parentId);
        }
        updateNode(modelNode, parentId, configAttributes, leafListAttributes, -1, removeNode);
    }

    @Override
    @Transactional(value = TxType.REQUIRED, rollbackOn = {DataStoreException.class, RuntimeException.class, Exception
            .class})
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName, ConfigLeafAttribute>
            configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, int insertIndex,
                           boolean removeNode) throws DataStoreException {
        if (modelNode.isRoot()) {
            SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(schemaPath);
            if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) != null) {
                ((ModelNodeWithAttributes) modelNode).updateConfigAttributes(configAttributes);
                if (leafListAttributes != null) {
                    ((ModelNodeWithAttributes) modelNode).setLeafLists(leafListAttributes);
                }
                markNodesToBeUpdated(modelNode);
                return;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("updateNode :" + modelNode.getModelNodeSchemaPath() + " parentId:" + parentId);
        }
        if (modelNode != null) {
            SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(schemaPath);
            if (klass != null) {
                super.updateNode(modelNode, parentId, configAttributes, leafListAttributes, insertIndex, removeNode);
                updateCache(modelNode, configAttributes, leafListAttributes);
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath);
                ModelNode freshModelNode = findNode(modelNode.getModelNodeSchemaPath(), MNKeyUtil.getModelNodeKey
                        (modelNode, m_schemaRegistry), parentId);
                if (freshModelNode != null) {
                    XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl)
                            freshModelNode);
                    if (storedParentModelNode != null) {
                        findAndUpdateModelNode(storedParentModelNode, (XmlModelNodeImpl) freshModelNode,
                                configAttributes, leafListAttributes, removeNode);
                        markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                    }
                }
            }
        }
    }

    private void markNodesToBeUpdated(ModelNode modelNode) {
        markNodesToBeUpdated(modelNode.getModelNodeSchemaPath(), (XmlModelNodeImpl) modelNode);
    }

    private void updateCache(ModelNode modelNode, Map<QName, ConfigLeafAttribute> configAttributes, Map<QName,
            LinkedHashSet<ConfigLeafAttribute>> leafListAttributes) {
        SchemaPath nodeType = modelNode.getModelNodeSchemaPath();
        ModelNodeId nodeId = modelNode.getModelNodeId();
        XmlModelNodeImpl nodeFromCache = m_dsmCache.getFromCache(nodeType, nodeId);
        if (nodeFromCache != null) {
            LOGGER.debug("Updated the node from cache, nodeType {}, nodeId {}", nodeType, nodeId);
            nodeFromCache.updateConfigAttributes(configAttributes);
            nodeFromCache.updateLeafListAttributes(leafListAttributes);
        }
    }

    @Override
    public void removeNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeNode :" + modelNode.getModelNodeSchemaPath() + " parentId:" + parentId);
        }
        if (modelNode != null) {
            SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(schemaPath);
            if (klass != null) {
                ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(m_schemaRegistry, modelNode
                        .getModelNodeSchemaPath(), modelNode.getModelNodeId());
                Object entity = getParentEntity(klass, modelNodeKey, parentId, LockModeType.PESSIMISTIC_WRITE);
                setXmlSubtree(entity, klass, "");
                super.removeNode(modelNode, parentId);
                m_dsmCache.removeFromCache(modelNode.getModelNodeId());
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath);
                ModelNode storedModelNode = findNode(modelNode.getModelNodeSchemaPath(), MNKeyUtil.getModelNodeKey
                        (modelNode, m_schemaRegistry), parentId);
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) storedModelNode);
                if (storedParentModelNode != null) {
                    XmlModelNodeImpl parentModelNode = ((XmlModelNodeImpl) storedModelNode).getParentModelNode();
                    List<XmlModelNodeImpl> xmlModelNodes = parentModelNode.getChildren().get(storedModelNode.getQName
                            ());
                    Iterator<XmlModelNodeImpl> iterator = xmlModelNodes.iterator();
                    while (iterator.hasNext()) {
                        XmlModelNodeImpl child = iterator.next();
                        if (child.getModelNodeId().equals(storedModelNode.getModelNodeId())) {
                            iterator.remove();
                            break;
                        }
                    }
                    markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                }
            }
        }
    }

    @Override
    public void removeAllNodes(ModelNode parentNode, SchemaPath nodeType, ModelNodeId grandParentId) throws
            DataStoreException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeAllNodes :" + parentNode.getModelNodeSchemaPath() + " parentId:" + grandParentId);
        }
        if (parentNode != null) {
            SchemaPath schemaPath = parentNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(schemaPath);
            if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
                super.removeAllNodes(parentNode, nodeType, grandParentId);
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(nodeType);
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) parentNode);
                if (storedParentModelNode != null) {
                    List<XmlModelNodeImpl> xmlModelNodes = ((XmlModelNodeImpl) parentNode).getChildren().get(nodeType
                            .getLastComponent());
                    if (xmlModelNodes != null) {
                        xmlModelNodes.clear();
                        markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                    }
                }
            }
        }
    }

    private void findAndUpdateModelNode(XmlModelNodeImpl parentModelNode, XmlModelNodeImpl xmlModelNode, Map<QName,
            ConfigLeafAttribute>
            configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode) {
        //Map<QName, List<XmlModelNodeImpl>> children = parentModelNode.getChildren();
        ModelNodeWithAttributes targetModelNode = retrieveModelNode(xmlModelNode.getModelNodeSchemaPath(),
                xmlModelNode.getModelNodeId(), parentModelNode);
        if (targetModelNode != null) {
            if (configAttributes != null) {
                targetModelNode.updateConfigAttributes(configAttributes);
            }
            if (leafListAttributes != null) {
                if (removeNode) {
                    targetModelNode.removeLeafListAttributes(leafListAttributes);
                } else {
                    targetModelNode.updateLeafListAttributes(leafListAttributes);
                }
            }
        }
    }

    private XmlModelNodeImpl retrieveModelNode(SchemaPath nodeType, ModelNodeId modelNodeId, XmlModelNodeImpl
            storedParentModelNode) {
        if (modelNodeId.xPathString().equals(storedParentModelNode.getModelNodeId().xPathString())) {
            return storedParentModelNode;
        }
        /**
         * The idea here is simple. The Super Grand Parent modelNode has a Map<QName,ModelNode>.
         * The QName is the immediate child of the Super grand Parent node and this is how the xml tree is
         * maintained.
         *
         * Here we have the target ModelNodeId that we want to retrieve and the super grand parent
         * ModelNodeId.
         *
         * 1) First we fetch the next Rdn link between the two.
         * 2) Get the next ModelNodeId(A1) in the link.
         * 3) Get the right childList in the current parent with RDN(QName)
         * 4) Iterate through the list and get the right ModelNode which is A1.
         * 5) Do the above steps till we hit the final modelNodeId
         *
         * Example:
         *
         *  A(yang list)-> B(yang container) -> C(yang list) --> D (Yang list).
         *  Here A is the super grand parent and D is what we want
         *  1) we get B's RDN
         *  2) we get B's ModelNodeId
         *  3) We get the list of B's modelNode with the QName
         *  4) We compare the list with B and call recursively.
         *
         */

        ModelNodeRdn rdn = modelNodeId.getNextChildRdn(storedParentModelNode.getModelNodeId());
        QName nextQName = null;
        if (ModelNodeRdn.CONTAINER.equals(rdn.getRdnName())) {
            nextQName = m_schemaRegistry.lookupQName(rdn.getNamespace(), rdn.getRdnValue());
            List<XmlModelNodeImpl> childList = storedParentModelNode.getChildren().get(nextQName);
            if (childList != null && !childList.isEmpty()) {
                if (childList.get(0).getModelNodeSchemaPath().equals(nodeType)) {
                    for (XmlModelNodeImpl child : childList) {
                        if (modelNodeId.xPathString().equals(child.getModelNodeId().xPathString())) {
                            return child;
                        }
                    }
                } else if (childList.size() == 1) {
                    return retrieveModelNode(nodeType, modelNodeId, childList.get(0));
                } else {
                    ModelNodeId nextId = modelNodeId.getNextChildId(storedParentModelNode.getModelNodeId());
                    for (XmlModelNodeImpl child : childList) {
                        if (nextId.xPathString().equals(child.getModelNodeId().xPathString())) {
                            return retrieveModelNode(nodeType, modelNodeId, child);
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<ModelNode> retrieveModelNodes(SchemaPath nodeType, XmlModelNodeImpl storedParentModelNode,
                                               ModelNodeId parentId) {
        List<ModelNode> modelNodes = new ArrayList<>();
        if (parentId != null) {
            XmlModelNodeImpl parentModelNode = retrieveModelNode(nodeType.getParent(), parentId, storedParentModelNode);
            if (parentModelNode != null) {
                QName childQName = nodeType.getLastComponent();
                List children = parentModelNode.getChildren().get(childQName);
                modelNodes.addAll(children);
            }
        } else {
            for (Map.Entry<QName, List<XmlModelNodeImpl>> entry : storedParentModelNode.getChildren().entrySet()) {
                for (XmlModelNodeImpl child : entry.getValue()) {
                    if (child.getModelNodeSchemaPath().equals(nodeType)) {
                        if (parentId != null && parentId.equals(child.getParentModelNode().getModelNodeId())) {
                            modelNodes.add(child);
                        } else if (parentId == null) {
                            modelNodes.add(child);
                        }

                    } else {
                        List<ModelNode> childModelNodes = retrieveModelNodes(nodeType, child, parentId);
                        modelNodes.addAll(childModelNodes);
                    }
                }

            }
        }
        return modelNodes;
    }

    private XmlModelNodeImpl getStoredParentModelNode(SchemaPath storedParentSchemaPath, ModelNodeId parentId,
                                                      ModelNodeId storedGrandParentId) {
        XmlModelNodeImpl storedParentModelNode;
        Class storedParentClass = m_entityRegistry.getEntityClass(storedParentSchemaPath);
        ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(m_schemaRegistry, storedParentSchemaPath, parentId);
        storedParentModelNode = m_dsmCache.getFromCache(storedParentSchemaPath, EMNKeyUtil.getModelNodeId
                (modelNodeKey, storedGrandParentId, storedParentSchemaPath));
        if (storedParentModelNode != null) {
            return storedParentModelNode;
        }
        try {
            Object storedParentEntity = getParentEntity(storedParentClass, modelNodeKey, storedGrandParentId,
                    LockModeType.PESSIMISTIC_READ);
            if (storedParentEntity != null) {
                String yangXmlSubtree = getXmlSubtree(storedParentEntity, storedParentClass);
                Element element = getXmlSubtreeElement(yangXmlSubtree);
                Map<QName, ConfigLeafAttribute> configAttrsFromEntity = XmlModelNodeToXmlMapperImpl
                        .getConfigAttributesFromEntity(m_schemaRegistry, storedParentSchemaPath, m_entityRegistry,
                                storedParentClass, storedParentEntity);
                List<XmlModelNodeImpl> storedParentModelNodes = m_xmlModelNodeToXmlMapper
                        .getModelNodeFromNodeSchemaPath(element, configAttrsFromEntity, storedParentSchemaPath,
                                storedGrandParentId, null, this);
                for (XmlModelNodeImpl node : storedParentModelNodes) {
                    if (storedGrandParentId.equals(node.getParentNodeId())) {
                        storedParentModelNode = node;
                        break;
                    }
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return storedParentModelNode;
    }

    private XmlModelNodeImpl getStoredParentModelNode(XmlModelNodeImpl xmlModelNode) {
        if (xmlModelNode.getParentModelNode() != null) {
            XmlModelNodeImpl parentModelNode = xmlModelNode.getParentModelNode();
            while (parentModelNode.getParentModelNode() != null) {
                parentModelNode = parentModelNode.getParentModelNode();
            }
            return parentModelNode;
        } else {
            return xmlModelNode;
        }
    }

    protected void markNodesToBeUpdated(SchemaPath nodeType, XmlModelNodeImpl node) {
        m_dsmCache.markNodeToBeUpdated(nodeType, node.getModelNodeId());
    }

    @SuppressWarnings("unchecked")
    protected Object getParentEntity(Class klass, ModelNodeKey modelNodeKey, ModelNodeId parentId, LockModeType
            lockModeType) throws
            DataStoreException {
        Object pk = EMNKeyUtil.buildPrimaryKey(klass, parentId, modelNodeKey, m_entityRegistry,
                getEntityDataStoreManager(klass));
        return getEntityDataStoreManager(klass).findById(klass, pk, lockModeType);
    }

    protected EntityDataStoreManager getEntityDataStoreManager(Class klass) {
        if (klass != null) {
            EntityDataStoreManager entityDSM = m_modelNodeDSMRegistry.getEntityDSM(klass);
            if (entityDSM != null) {
                return entityDSM;
            }
        }
        return m_persistenceManagerUtil.getEntityDataStoreManager();
    }

    private SchemaPath getStoredParentSchemaPath(SchemaPath schemaPath) {
        while (schemaPath != null && m_entityRegistry.getEntityClass(schemaPath) == null) {
            schemaPath = schemaPath.getParent();
        }
        return schemaPath;
    }

    private Element getXmlSubtreeElement(String yangXmlSubtree) {
        Element parentElement = null;
        try {
            if (yangXmlSubtree != null && !yangXmlSubtree.isEmpty()) {
                parentElement = stringToDocument(yangXmlSubtree).getDocumentElement();
            }
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
        return parentElement;
    }

    private String getXmlSubtreeString(String currentXmlStr, XmlModelNodeImpl node, Element xmlValue) {
        Element fulXmlElement = xmlValue;
        if (node.isRoot()) {
            if (currentXmlStr != null && !currentXmlStr.isEmpty()) {
                try {
                    Element dataElement = DocumentUtils.stringToDocument(currentXmlStr).getDocumentElement();
                    fulXmlElement = dataElement;
                    appendXmlValue(dataElement, m_schemaRegistry.getDataSchemaNode(node.getModelNodeSchemaPath()),
                            xmlValue, node);
                } catch (NetconfMessageBuilderException | InvalidIdentityRefException e) {
                    throw new RuntimeException(e);
                }
            } else {
                //xml string is null or empty string
                Document ownerDocument = xmlValue.getOwnerDocument();
                fulXmlElement = ownerDocument.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources
                        .RPC_REPLY_DATA);
                fulXmlElement.appendChild(xmlValue);
            }
        }

        String xmlSubtreeString;
        try {
            xmlSubtreeString = documentToPrettyString(fulXmlElement);
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
        return xmlSubtreeString;
    }

    private void appendXmlValue(Element dataElement, DataSchemaNode dataSchemaNode, Element xmlValue,
                                XmlModelNodeImpl node) throws InvalidIdentityRefException {

        Document ownerDocument = dataElement.getOwnerDocument();
        boolean appended = false;
        if (dataSchemaNode instanceof ListSchemaNode) {
            Map<QName, ConfigLeafAttribute> keyAttributesFromNode = node.getKeyAttributes();
            ListSchemaNode listSchemaNode = (ListSchemaNode) dataSchemaNode;
            for (Element rootNode : DocumentUtils.getChildElements(dataElement)) {
                Map<QName, ConfigLeafAttribute> keyAttributesFromXml = getKeysFromXml(listSchemaNode, rootNode);
                if (keyAttributesFromNode.equals(keyAttributesFromXml)) {
                    rootNode.getParentNode().removeChild(rootNode);
                    dataElement.appendChild(ownerDocument.importNode(xmlValue, true));
                    appended = true;
                    break;
                }
            }
        } else {
            //container
            for (Element rootNode : DocumentUtils.getChildElements(dataElement)) {
                if (rootNode.getNamespaceURI().equals(xmlValue.getNamespaceURI()) && rootNode.getLocalName().equals
                        (xmlValue.getLocalName())) {
                    rootNode.getParentNode().removeChild(rootNode);
                    dataElement.appendChild(ownerDocument.importNode(xmlValue, true));
                    appended = true;
                    break;
                }
            }
        }
        if (!appended) {
            //newly created node, so append at the end
            dataElement.appendChild(ownerDocument.importNode(xmlValue, true));
        }
    }

    private Map<QName, ConfigLeafAttribute> getKeysFromXml(ListSchemaNode listSchemaNode, Element rootNode) throws
            InvalidIdentityRefException {
        Map<QName, ConfigLeafAttribute> keyAttributesFromXml = new LinkedHashMap<>();
        for (QName keyQname : listSchemaNode.getKeyDefinition()) {
            for (Element field : DocumentUtils.getChildElements(rootNode)) {
                if (nodesMatch(field, keyQname)) {
                    keyAttributesFromXml.put(keyQname, ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry,
                            listSchemaNode.getPath(), keyQname, field));
                }
            }
        }
        return keyAttributesFromXml;
    }

    private String getXmlSubtree(Object parentEntity, Class parentKlass) throws DataStoreException {
        Method yangXmlSubtreeGetter = m_entityRegistry.getYangXmlSubtreeGetter(parentKlass);
        String yangXmlSubtree = "";
        if (yangXmlSubtreeGetter != null) {
            try {
                yangXmlSubtree = (String) yangXmlSubtreeGetter.invoke(parentEntity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return yangXmlSubtree;
    }

    private void setXmlSubtree(Object parentEntity, Class parentKlass, String value) throws DataStoreException {
        Method yangXmlSubtreeSetter = m_entityRegistry.getYangXmlSubtreeSetter(parentKlass);
        if (yangXmlSubtreeSetter != null) {
            try {
                yangXmlSubtreeSetter.invoke(parentEntity, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
