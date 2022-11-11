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

import static org.broadband_forum.obbaa.netconf.api.util.CryptUtil2.ENCR_STR_PATTERN;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger.endPhase;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger.startPhase;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMUtils.getXmlSubtree;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMUtils.setVisibility;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EMNKeyUtil.constructParentIdFromEntity;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeToXmlMapper.nodesMatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InvalidIdentityRefException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.hibernate.proxy.HibernateProxy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlModelNodeToXmlMapperImpl implements XmlModelNodeToXmlMapper {

    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final XmlDSMCache m_dsmCache;
    private final XmlSubtreeDSM m_dsm;
    private SchemaRegistry m_schemaRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private EntityRegistry m_entityRegistry;

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(XmlModelNodeToXmlMapperImpl.class, LogAppNames.NETCONF_STACK);

    public XmlModelNodeToXmlMapperImpl(XmlDSMCache dsmCache, SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry, EntityRegistry entityRegistry, XmlSubtreeDSM xmlSubtreeDSM) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_subSystemRegistry = subsystemRegistry;
        m_entityRegistry = entityRegistry;
        m_dsmCache = dsmCache;
        m_dsm = xmlSubtreeDSM;
    }

    @Override
    public XmlModelNodeImpl getModelNodeFromParentSchemaPath(Element nodeXml, SchemaPath parentSchemaPath,
                                                             ModelNodeId parentId, XmlModelNodeImpl parentModelNode,
                                                             ModelNodeDataStoreManager modelNodeDsm, Object storedParentEntity, SchemaPath storedParentSchemaPath) {

        // Get the childSchemaPath using element's rootNode and parentSchemaPath
        SchemaPath childSchemaPath = null;
        SchemaRegistry schemaRegistry = parentModelNode == null ? m_schemaRegistry : parentModelNode.getSchemaRegistry();
        Collection<DataSchemaNode> childSchemaNodes = schemaRegistry.getNonChoiceChildren(parentSchemaPath);
        for (DataSchemaNode dataSchemaNode : childSchemaNodes) {
            ModelNodeHelperRegistry helperRegistry = parentModelNode == null ? m_modelNodeHelperRegistry
                    : parentModelNode.getModelNodeHelperRegistry();
            SubSystemRegistry subSystemRegistry = parentModelNode == null ? m_subSystemRegistry : parentModelNode.getSubSystemRegistry();
            QName qName = dataSchemaNode.getQName();
            if (nodesMatch(nodeXml, qName)) {
                childSchemaPath = dataSchemaNode.getPath();
                return buildModelNodeForXml(nodeXml, parentId, parentModelNode, modelNodeDsm, childSchemaPath, schemaRegistry,
                        helperRegistry, subSystemRegistry, storedParentEntity, storedParentSchemaPath);
            }
        }

        if (AnvExtensions.MOUNT_POINT.isExtensionIn(schemaRegistry.getDataSchemaNode(parentSchemaPath))) {
            SchemaMountRegistryProvider provider = schemaRegistry.getMountRegistry().getProvider(parentSchemaPath);
            if (provider != null) {
                if(parentModelNode != null) {
                    ModelNodeId parentNodeId = parentModelNode.getModelNodeId();
                    schemaRegistry = provider.getSchemaRegistry(parentNodeId);
                    ModelNodeHelperRegistry helperRegistry = provider.getModelNodeHelperRegistry(parentNodeId);
                    SubSystemRegistry subSystemRegistry = provider.getSubSystemRegistry(parentNodeId);
                    childSchemaNodes = schemaRegistry.getNonChoiceChildren(parentSchemaPath);
                    for (DataSchemaNode dataSchemaNode : childSchemaNodes) {
                        QName qName = dataSchemaNode.getQName();
                        if (nodesMatch(nodeXml, qName)) {
                            childSchemaPath = dataSchemaNode.getPath();
                            return buildModelNodeForXml(nodeXml, parentId, parentModelNode, modelNodeDsm, childSchemaPath, schemaRegistry,
                                    helperRegistry, subSystemRegistry, storedParentEntity, storedParentSchemaPath);
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Invalid root element found in nodeXml: " + nodeXml.getLocalName());
    }

    protected XmlModelNodeImpl buildModelNodeForXml(Element nodeXml, ModelNodeId parentId, XmlModelNodeImpl parentModelNode,
                                                    ModelNodeDataStoreManager modelNodeDsm, SchemaPath childSchemaPath, SchemaRegistry schemaRegistry,
                                                    ModelNodeHelperRegistry helperRegistry, SubSystemRegistry subSystemRegistry, Object storedParentEntity, SchemaPath storedParentSchemaPath) {
        startPhase("XmlModelNodeToXmlMapperImpl.buildModelNodeForXml");
        Map<QName, ConfigLeafAttribute> nodeAttrs = new LinkedHashMap<>();
        List<Element> childrenXml = new ArrayList<>();
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs = new HashMap<>();

        fillNodeAttributes(nodeXml, childSchemaPath, nodeAttrs, childrenXml, leafListAttrs, Collections.emptyMap(), parentModelNode);

        startPhase("XmlModelNodeToXmlMapperImpl.buildModelNodeForXml.XmlModelNodeImpl");
        XmlModelNodeImpl node = new XmlModelNodeImpl(nodeXml.getOwnerDocument(), childSchemaPath, nodeAttrs, childrenXml, parentModelNode, parentId, this,
                helperRegistry, schemaRegistry, subSystemRegistry, modelNodeDsm, storedParentEntity, true, storedParentSchemaPath);
        endPhase("XmlModelNodeToXmlMapperImpl.buildModelNodeForXml.XmlModelNodeImpl", false);
        node.setLeafLists(leafListAttrs);
        endPhase("XmlModelNodeToXmlMapperImpl.buildModelNodeForXml", false);
        return node;
    }

    @Override
    public List<XmlModelNodeImpl> getModelNodeFromNodeSchemaPath(boolean xmlLoaded, Element nodeXml, Map<QName, ConfigLeafAttribute> configAttrsFromEntity,
                                                                 Map<QName, LinkedHashSet<ConfigLeafAttribute>> configLeafListsFromEntity,
                                                                 SchemaPath nodeSchemaPath, ModelNodeId parentId,
                                                                 XmlModelNodeImpl parentModelNode, ModelNodeDataStoreManager modelNodeDsm, Object storedParentEntity, SchemaPath storedParentSchemaPath) {
        List<XmlModelNodeImpl> nodes = new ArrayList<>();
        if (nodeXml != null) {
            List<Element> xmlNodes = new ArrayList<>();
            if (SchemaPathUtil.isRootSchemaPath(nodeSchemaPath) && !nodesMatch(nodeXml, nodeSchemaPath.getLastComponent())) {
                List<Element> rootNodes = DocumentUtils.getChildElements(nodeXml);
                for (Element rootNode : rootNodes) {
                    if (nodesMatch(rootNode, nodeSchemaPath.getLastComponent())) {
                        xmlNodes.add(rootNode);
                    }
                }
            } else {
                xmlNodes.add(nodeXml);
            }

            for (Element xmlNode : xmlNodes) {
                if (nodesMatch(xmlNode, nodeSchemaPath.getLastComponent())) {
                    HelperDrivenModelNode.checkForGetTimeOut();
                    prepareNode(xmlLoaded, configAttrsFromEntity, configLeafListsFromEntity, nodeSchemaPath, parentId, parentModelNode, modelNodeDsm,
                            nodes, xmlNode, storedParentEntity, storedParentSchemaPath);
                }
            }
        } else {
            //nodeXml is null, so create a xml modelnode with no attributes from the xml
            boolean allKeysFound = true;
            SchemaRegistry schemaRegistry = parentModelNode == null ? m_schemaRegistry : parentModelNode.getSchemaRegistry();
            DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
            if (dataSchemaNode instanceof ListSchemaNode) {
                for (QName keyQname : ((ListSchemaNode) dataSchemaNode).getKeyDefinition()) {
                    if (!configAttrsFromEntity.containsKey(keyQname)) {
                        allKeysFound = false;
                        break;
                    }
                }
            }
            if (allKeysFound) {
                prepareNode(xmlLoaded, configAttrsFromEntity, configLeafListsFromEntity, nodeSchemaPath, parentId, parentModelNode, modelNodeDsm,
                        nodes, null, storedParentEntity, storedParentSchemaPath);
            }
        }

        return nodes;
    }

    private void prepareNode(boolean xmlLoaded, Map<QName, ConfigLeafAttribute> configAttrsFromEntity, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs, SchemaPath nodeSchemaPath, ModelNodeId parentId, XmlModelNodeImpl parentModelNode, ModelNodeDataStoreManager modelNodeDsm, List<XmlModelNodeImpl> nodes, Element xmlNode, Object storedParentEntity, SchemaPath storedParentSchemaPath) {
        startPhase("XmlModelNodeToXmlMapperImpl.prepareNode");
        Map<QName, ConfigLeafAttribute> nodeAttrs = new LinkedHashMap<>();
        List<Element> childrenXml = new ArrayList<>();

        fillNodeAttributes(xmlNode, nodeSchemaPath, nodeAttrs, childrenXml, leafListAttrs, configAttrsFromEntity, parentModelNode);

        Document document = ConfigAttributeFactory.getDocument();
        if(xmlNode != null){
            document = xmlNode.getOwnerDocument();
        }
        startPhase("XmlModelNodeToXmlMapperImpl.prepareNode.getXmlModelNode");
        XmlModelNodeImpl node = getXmlModelNode(xmlLoaded, document, nodeSchemaPath, parentId, parentModelNode, modelNodeDsm, nodeAttrs, childrenXml, leafListAttrs, storedParentEntity, storedParentSchemaPath);
        endPhase("XmlModelNodeToXmlMapperImpl.prepareNode.getXmlModelNode", false);

        node.setLeafLists(leafListAttrs);
        nodes.add(node);
        endPhase("XmlModelNodeToXmlMapperImpl.prepareNode", false);
    }

    private void fillNodeAttributes(Element nodeXml, SchemaPath nodeSchemaPath, Map<QName, ConfigLeafAttribute> nodeAttrs, List<Element> childrenXml, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs, Map<? extends QName, ? extends ConfigLeafAttribute> configAttrsFromEntity, XmlModelNodeImpl parentModelNode) {
        startPhase("XmlModelNodeToXmlMapperImpl.fillNodeAttributes");
        List<Element> childElements = getChildElements(nodeXml);
        SchemaRegistry schemaRegistry = parentModelNode == null ? m_schemaRegistry : parentModelNode.getSchemaRegistry();
        Collection<DataSchemaNode> dataSchemaNodes = schemaRegistry.getNonChoiceChildren(nodeSchemaPath);

        fillChildren(nodeSchemaPath, nodeAttrs, childrenXml, leafListAttrs, childElements, dataSchemaNodes, schemaRegistry);
        DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
        if (schemaNode != null && AnvExtensions.MOUNT_POINT.isExtensionIn(schemaNode)) {
            SchemaMountRegistryProvider provider = schemaRegistry.getMountRegistry().getProvider(nodeSchemaPath);
            if (provider != null) {
                if(parentModelNode != null) {
                    schemaRegistry = schemaRegistry.getMountRegistry().getProvider(nodeSchemaPath).getSchemaRegistry(parentModelNode.getModelNodeId());
                    if (schemaRegistry != null) {
                        dataSchemaNodes = schemaRegistry.getNonChoiceChildren(nodeSchemaPath);
                        fillChildren(nodeSchemaPath, nodeAttrs, childrenXml, leafListAttrs, childElements, dataSchemaNodes, schemaRegistry);
                    }
                }
            }
        }
        endPhase("XmlModelNodeToXmlMapperImpl.fillNodeAttributes", false);
        nodeAttrs.putAll(configAttrsFromEntity);
    }

    protected void fillChildren(SchemaPath nodeSchemaPath, Map<QName, ConfigLeafAttribute> nodeAttrs, List<Element> childrenXml,
                                Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs, List<Element> childElements,
                                Collection<DataSchemaNode> dataSchemaNodes, SchemaRegistry schemaRegistry) {
        startPhase("XmlModelNodeToXmlMapperImpl.fillNodeAttributes.fillChildren");
        for (Element childElement : childElements) {
            for (DataSchemaNode dataSchemaNode : dataSchemaNodes) {
                QName qName = dataSchemaNode.getQName();
                if (nodesMatch(childElement, qName)) {
                    try {
                        if (dataSchemaNode instanceof ContainerSchemaNode || dataSchemaNode instanceof ListSchemaNode) {
                            childrenXml.add(childElement);
                            break;
                        } else if (dataSchemaNode instanceof LeafSchemaNode) {
                            nodeAttrs.put(dataSchemaNode.getQName(), ConfigAttributeFactory.getConfigAttribute(schemaRegistry,
                                    nodeSchemaPath, qName, childElement));
                            break;
                        } else if (dataSchemaNode instanceof LeafListSchemaNode) {
                            LinkedHashSet<ConfigLeafAttribute> values = leafListAttrs.get(dataSchemaNode.getQName());
                            if (values == null) {
                                values = new LinkedHashSet<>();
                                leafListAttrs.put(dataSchemaNode.getQName(), values);
                            }
                            values.add(ConfigAttributeFactory.getConfigAttribute(schemaRegistry,
                                    nodeSchemaPath, qName, childElement));
                            break;
                        }
                    } catch (InvalidIdentityRefException e) {
                        LOGGER.error("Invalid identity ref value ", e.getRpcError()); //Who should handle this exception?
                    }
                }
            }
        }
        endPhase("XmlModelNodeToXmlMapperImpl.fillNodeAttributes.fillChildren", false);
    }

    @Override
    public XmlModelNodeImpl getModelNode(Object entity, ModelNodeDataStoreManager modelNodeDSM) {
        ModelNodeId parentId = new ModelNodeId();
        SchemaPath schemaPath;
        Map<QName, ConfigLeafAttribute> attributes;
        List<Element> childrenXml = new ArrayList<>();
        Document document = ConfigAttributeFactory.getDocument();
        try {
            //Get parentId and Schemapath
            Class<?> klass = getEntityClass(entity);
            Method parentIdMethod = m_entityRegistry.getParentIdGetter(klass);
            String parentIdStr = (String) parentIdMethod.invoke(entity);
            Method schemaPathMethod = m_entityRegistry.getSchemaPathGetter(klass);
            schemaPath = SchemaPathUtil.fromString((String) schemaPathMethod.invoke(entity));
            schemaPath = m_schemaRegistry.addRevisions(schemaPath);
            if (parentIdStr != null) {
                parentId = constructParentIdFromEntity(schemaPath, klass, parentIdStr, m_entityRegistry, m_schemaRegistry);
            }
            attributes = getConfigAttributesFromEntity(m_schemaRegistry, schemaPath, m_entityRegistry, klass, entity);
            ModelNodeKey key = MNKeyUtil.getKeyFromCriteria(schemaPath, attributes, m_schemaRegistry);
            ModelNodeId nodeId = EMNKeyUtil.getModelNodeId(key, parentId, schemaPath);
            XmlModelNodeImpl node = m_dsmCache.getFromCache(schemaPath, nodeId);
            if (node != null) {
                node.updateConfigAttributes(attributes);
                return node;
            }
            Map<QName, Collection<Object>> leafListAttrs = new HashMap<>();
            //Get leafList attributes
            Map<QName, Method> leafListGetters = m_entityRegistry.getYangLeafListGetters(klass);
            for (Map.Entry<QName, Method> getter : leafListGetters.entrySet()) {
                Collection<Object> values = (Collection<Object>) getter.getValue().invoke(entity);
                leafListAttrs.put(getter.getKey(), values);
            }

            // Get XmlSubtree
            Method yangXmlSubtreeGetter = m_entityRegistry.getYangXmlSubtreeGetter(klass);
            String xmlSubtree = (String) yangXmlSubtreeGetter.invoke(entity);

            if (xmlSubtree != null && !xmlSubtree.isEmpty()) {
                document = stringToDocument(xmlSubtree);
                Element docElement = document.getDocumentElement();
                Collection<Element> rootElements = null;
                if ( docElement.getLocalName().equals(NetconfResources.RPC_REPLY_DATA)){
                    rootElements = getChildElements(docElement);
                } else {
                    rootElements = Arrays.asList(docElement);
                }
                List<Element> childElements = new ArrayList<>();
                for (Element element : rootElements) {
                    Collection<Element> children = getChildElements(element);
                    childElements.addAll(children);
                }
                Collection<DataSchemaNode> dataSchemaNodes = m_schemaRegistry.getChildren(schemaPath);

                for (Element childElement : childElements) {
                    for (DataSchemaNode dataSchemaNode : dataSchemaNodes) {
                        QName qName = dataSchemaNode.getQName();
                        if (nodesMatch(childElement, qName)) {

                            if (dataSchemaNode instanceof ContainerSchemaNode || dataSchemaNode instanceof ListSchemaNode) {
                                childrenXml.add(childElement);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (InvocationTargetException | IllegalAccessException | NetconfMessageBuilderException e) {
            LOGGER.error("Error while getting model node", e);
            throw new RuntimeException("Error while getting model node", e);
        }

        XmlModelNodeImpl xmlModelNode = new XmlModelNodeImpl(document, schemaPath, attributes, childrenXml, null,
                parentId, this, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, modelNodeDSM, entity, true, schemaPath);
        setVisibility(m_entityRegistry, entity, xmlModelNode);
        m_dsmCache.putInCache(schemaPath, xmlModelNode.getModelNodeId(), xmlModelNode);
        return xmlModelNode;
    }

    @Override
    public Element getXmlValue(XmlModelNodeImpl xmlModelNode) {
        SchemaPath schemaPath = xmlModelNode.getModelNodeSchemaPath();
        QName qName = schemaPath.getLastComponent();
        Element element = xmlModelNode.getDocument().createElementNS(qName.getNamespace().toString(), qName.getLocalName());

        //copy attributes
        Class klass = m_entityRegistry.getEntityClass(schemaPath);

        copyAttributes(xmlModelNode, element, klass);

        copyLeafLists(xmlModelNode, element, klass);
        copyChildren(xmlModelNode, element);

        return element;
    }

    private void copyChildren(XmlModelNodeImpl xmlModelNode, Element element) {
        //copy children
        if(xmlModelNode.childrenMaterialised()) {
            //if the children are materialised, most likely they are modified, so do the copy recursively
            for (Map.Entry<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> childrenOfType : xmlModelNode.getChildren().entrySet()) {
                for (XmlModelNodeImpl child : childrenOfType.getValue().list()) {
                    Element childXml = getXmlValue(child);
                    try {
                        startPhase("XmlSubtreeDSM.endModify.getXmlValue.appendingMaterialisedChildXml");
                        element.appendChild(element.getOwnerDocument().importNode(childXml, true));
                    } finally {
                        endPhase("XmlSubtreeDSM.endModify.getXmlValue.appendingMaterialisedChildXml", false);
                    }
                }
            }
        } else {
            //children xml is not materialised, so append as is
            for (Element childXml : xmlModelNode.getChildrenXml()) {
                try {
                    startPhase("XmlSubtreeDSM.endModify.getXmlValue.appendingChildXml");
                    element.appendChild(element.getOwnerDocument().importNode(childXml, true));
                } finally {
                    endPhase("XmlSubtreeDSM.endModify.getXmlValue.appendingChildXml", false);
                }
            }
        }
    }

    private void copyLeafLists(XmlModelNodeImpl xmlModelNode, Element element, Class klass) {
        try {
            startPhase("XmlSubtreeDSM.endModify.getXmlValue.copyLeafLists");
            //copy leaf Lists
            Set<QName> leafListsTobeStoredInXml = new LinkedHashSet<>(xmlModelNode.getLeafLists().keySet());
            if (klass != null) {
                // leaf lists without separate column in entity class
                Set<QName> leafListGettersKeyset = new LinkedHashSet<>(m_entityRegistry.getYangLeafListGetters(klass).keySet());
                leafListsTobeStoredInXml.removeAll(leafListGettersKeyset);
            }

            for (QName leafListQName : leafListsTobeStoredInXml) {
                for (ConfigLeafAttribute value : xmlModelNode.getLeafList(leafListQName)) {
                    appendConfigAttributeToParentElement(value, element);
                }
            }

        } finally {
            endPhase("XmlSubtreeDSM.endModify.getXmlValue.copyLeafLists", false);
        }
    }

    private void copyAttributes(XmlModelNodeImpl xmlModelNode, Element element, Class klass) {
        try {
            startPhase("XmlSubtreeDSM.endModify.getXmlValue.copyAttributes");
            //copy attributes
            Set<QName> xmlModelNodeAttributes = new LinkedHashSet<>(xmlModelNode.getAttributes().keySet());
            Set<QName> attributesTobeStoredInXml = new LinkedHashSet<>(xmlModelNodeAttributes);

            if (klass != null) {
                // attributes without separate column in entity class
                Set<QName> attributeGettersKeyset = new LinkedHashSet<>(m_entityRegistry.getAttributeGetters(klass).keySet());
                attributesTobeStoredInXml.removeAll(attributeGettersKeyset);
            }

            for (QName attributeQName : attributesTobeStoredInXml) {
                appendConfigAttributeToParentElement(xmlModelNode.getAttribute(attributeQName), element);
            }

        } finally {
            endPhase("XmlSubtreeDSM.endModify.getXmlValue.copyAttributes", false);
        }
    }

    @Override
    public XmlModelNodeImpl getRootXmlModelNode(ModelNodeWithAttributes modelNode, ModelNodeDataStoreManager dsm) {
        XmlModelNodeImpl xmlModelNode = new XmlModelNodeImpl(ConfigAttributeFactory.getDocument(), modelNode.getModelNodeSchemaPath(), modelNode.getAttributes(), new
                ArrayList<>(), null, new ModelNodeId(),
                this, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, dsm, null, true, null);
        m_dsmCache.putInCache(modelNode.getModelNodeSchemaPath(), modelNode.getModelNodeId(), xmlModelNode);
        return xmlModelNode;
    }

    @Override
    public List<Element> loadXmlValue(Object storedParentEntity, SchemaPath schemaPath, SchemaRegistry schemaRegistry, SchemaPath storedParentSchemaPath) {
        startPhase("XmlModelNodeToXmlMapperImpl.loadXmlValue");
        List<Element> childElements = new LinkedList<>();
        if (storedParentEntity != null) {
            Class storedParentClass = storedParentEntity.getClass();
            startPhase("XmlModelNodeToXmlMapperImpl.loadXmlValue.getXmlSubtree");
            String xmlString = getXmlSubtree(storedParentEntity, storedParentClass, m_entityRegistry);
            endPhase("XmlModelNodeToXmlMapperImpl.loadXmlValue.getXmlSubtree", false);
            startPhase("XmlModelNodeToXmlMapperImpl.loadXmlValue.string2Dom");
            Element xmlElement = m_dsm.string2Dom(storedParentSchemaPath, storedParentEntity, xmlString);
            endPhase("XmlModelNodeToXmlMapperImpl.loadXmlValue.string2Dom", false);
            if (xmlElement != null) {
                List<Element> xmlNodes = new ArrayList<>();
                if (SchemaPathUtil.isRootSchemaPath(schemaPath) && !nodesMatch(xmlElement, schemaPath.getLastComponent())) {
                    List<Element> rootNodes = DocumentUtils.getChildElements(xmlElement);
                    for (Element rootNode : rootNodes) {
                        if (nodesMatch(rootNode, schemaPath.getLastComponent())) {
                            xmlNodes.add(rootNode);
                        }
                    }
                } else {
                    xmlNodes.add(xmlElement);
                }

                for (Element xmlNode : xmlNodes) {
                    if (nodesMatch(xmlNode, schemaPath.getLastComponent())) {
                        startPhase("XmlModelNodeToXmlMapperImpl.loadXmlValue.getChildContainmentElements");
                        childElements.addAll(getChildContainmentElements(xmlNode, schemaPath, schemaRegistry));
                        endPhase("XmlModelNodeToXmlMapperImpl.loadXmlValue.getChildContainmentElements", false);
                    }
                }
            }
        }
        endPhase("XmlModelNodeToXmlMapperImpl.loadXmlValue", false);
        return childElements;
    }

    private List<Element> getChildContainmentElements(Element parent, SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        List<Element> childContainmentElements = new ArrayList<>();
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                Collection<DataSchemaNode> childSchemaNodes = schemaRegistry.getNonChoiceChildren(schemaPath);
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        for(DataSchemaNode childSchemaNode : childSchemaNodes) {
                            if(childSchemaNode.getQName().getLocalName().equals(child.getLocalName()) &&
                                    childSchemaNode.getQName().getNamespace().toString().equals(child.getNamespaceURI())) {
                                if(childSchemaNode instanceof ListSchemaNode || childSchemaNode instanceof ContainerSchemaNode) {
                                    childContainmentElements.add((Element) child);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return childContainmentElements;
    }

    private void appendConfigAttributeToParentElement(ConfigLeafAttribute attribute, Element parentElement) {
        Element domElement = attribute.getDOMValue(parentElement.getNamespaceURI(), parentElement.getPrefix());
        //cannot avoid import without major refactoring, but this phase is only few milliseconds so far and this is not going to be deep.
        if(attribute.isPassword()){
            Element encryptedLeafDom = (Element) domElement.cloneNode(true);
            if(!ENCR_STR_PATTERN.matcher(domElement.getTextContent()).matches()){
                encryptedLeafDom.setTextContent(CryptUtil2.encrypt(encryptedLeafDom.getTextContent()));
            }
            encryptedLeafDom.setAttribute(NetconfExtensions.IS_PASSWORD.getModuleName(), "true");
            parentElement.appendChild(parentElement.getOwnerDocument().importNode(encryptedLeafDom,true));
        }else{
            parentElement.appendChild(parentElement.getOwnerDocument().importNode(domElement, true));
        }
    }

    static Map<QName, ConfigLeafAttribute> getConfigAttributesFromEntity(SchemaRegistry schemaRegistry, SchemaPath schemaPath,
                                                                         EntityRegistry entityRegistry, Class<?> klass, Object entity) throws IllegalAccessException, InvocationTargetException {
        Map<QName, ConfigLeafAttribute> attributes = new LinkedHashMap<>();
        //Get attributes
        Map<QName, Method> attributeGetters = entityRegistry.getAttributeGetters(klass);
        Map<QName, Method> attributeNSGetters = entityRegistry.getYangAttributeNSGetters(klass);

        for (Map.Entry<QName, Method> attributeGetter : attributeGetters.entrySet()) {
            String value = (String) attributeGetter.getValue().invoke(entity);
            if (value != null) {
                Method identityRefNSGetter = attributeNSGetters.get(attributeGetter.getKey());
                ConfigLeafAttribute configLeafAttribute;
                if (identityRefNSGetter != null) {
                    String namespace = (String) identityRefNSGetter.invoke(entity);
                    configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(schemaRegistry, schemaPath, namespace,
                            attributeGetter.getKey(), value);
                } else {
                    configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(schemaRegistry, schemaPath, null,
                            attributeGetter.getKey(), value);
                }
                attributes.put(attributeGetter.getKey(), configLeafAttribute);
            }
        }
        return attributes;
    }

    private XmlModelNodeImpl getXmlModelNode(boolean xmlLoaded, Document document, SchemaPath nodeSchemaPath,
                                             ModelNodeId parentId, XmlModelNodeImpl parentModelNode,
                                             ModelNodeDataStoreManager modelNodeDsm, Map<QName, ConfigLeafAttribute> nodeAttrs,
                                             List<Element> childrenXml, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs,
                                             Object storedParentEntity, SchemaPath storedParentSchemaPath) {
    	SchemaRegistry schemaRegistry = m_schemaRegistry;
    	if ( parentModelNode !=  null){
    		schemaRegistry = parentModelNode.getSchemaRegistry();
    	}
        ModelNodeKey key = MNKeyUtil.getKeyFromCriteria(nodeSchemaPath, nodeAttrs, schemaRegistry);
        ModelNodeId nodeId = EMNKeyUtil.getModelNodeId(key, parentId, nodeSchemaPath);
        XmlModelNodeImpl node = m_dsmCache.getFromCache(nodeSchemaPath, nodeId);
        if (node == null) {
            schemaRegistry = m_schemaRegistry;
            ModelNodeHelperRegistry helperRegistry = m_modelNodeHelperRegistry;
            SubSystemRegistry subSystemRegistry = m_subSystemRegistry;
            if (parentModelNode != null) {
                if (parentModelNode.hasSchemaMount()) {
                    schemaRegistry = parentModelNode.getMountRegistry();
                    helperRegistry = parentModelNode.getMountModelNodeHelperRegistry();
                    subSystemRegistry = parentModelNode.getMountSubSystemRegistry();
                } else {
                    schemaRegistry = parentModelNode.getSchemaRegistry();
                    helperRegistry = parentModelNode.getModelNodeHelperRegistry();
                    subSystemRegistry = parentModelNode.getMountSubSystemRegistry();
                }
            }
            LOGGER.debug("Adding node of type {} with id {} into DSM cache", nodeSchemaPath, nodeId);
            node = new XmlModelNodeImpl(document, nodeSchemaPath, nodeAttrs, childrenXml, parentModelNode, parentId, this, helperRegistry,
                    schemaRegistry, subSystemRegistry, modelNodeDsm, storedParentEntity, xmlLoaded, storedParentSchemaPath);
            node.setLeafLists(leafListAttrs);
            m_dsmCache.putInCache(nodeSchemaPath, nodeId, node);
        } else {
            node.updateConfigAttributes(nodeAttrs);
            LOGGER.debug("Found node of type {} with id {} in DSM cache, re-using the instance from cache", nodeSchemaPath, nodeId);
        }
        return node;
    }

    private Class<?> getEntityClass(Object entityObject) {
        if (entityObject instanceof HibernateProxy) {
            return ((HibernateProxy) entityObject).getHibernateLazyInitializer()
                    .getPersistentClass();
        }
        return entityObject.getClass();
    }

    public static List<Element> getChildElements(Node parent) {
        List<Element> elementList = new LinkedList<>();
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        elementList.add((Element) child);
                    }
                }
            }
        }
        return elementList;
    }
}
