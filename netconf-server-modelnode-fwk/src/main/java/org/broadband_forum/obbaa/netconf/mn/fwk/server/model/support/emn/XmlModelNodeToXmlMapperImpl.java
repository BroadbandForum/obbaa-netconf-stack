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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InvalidIdentityRefException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.hibernate.proxy.HibernateProxy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangTypeToClassConverter;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeToXmlMapper.nodesMatch;

public class XmlModelNodeToXmlMapperImpl implements XmlModelNodeToXmlMapper {

    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final XmlDSMCache m_dsmCache;
    private SchemaRegistry m_schemaRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private EntityRegistry m_entityRegistry;
    private static DocumentBuilder m_builder;
    private Document m_doc;

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(XmlModelNodeToXmlMapperImpl.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    public XmlModelNodeToXmlMapperImpl(XmlDSMCache dsmCache, SchemaRegistry schemaRegistry, ModelNodeHelperRegistry
            modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry, EntityRegistry entityRegistry) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_subSystemRegistry = subsystemRegistry;
        m_entityRegistry = entityRegistry;
        m_dsmCache = dsmCache;
    }

    @Override
    public XmlModelNodeImpl getModelNodeFromParentSchemaPath(Element nodeXml, SchemaPath parentSchemaPath,
                                                             ModelNodeId parentId, XmlModelNodeImpl parentModelNode,
                                                             ModelNodeDataStoreManager modelNodeDsm) {

        // Get the childSchemaPath using element's rootNode and parentSchemaPath
        SchemaPath childSchemaPath = null;
        boolean foundMatchingXmlElement = false;
        Collection<DataSchemaNode> childSchemaNodes = m_schemaRegistry.getNonChoiceChildren(parentSchemaPath);
        for (DataSchemaNode dataSchemaNode : childSchemaNodes) {
            QName qName = dataSchemaNode.getQName();
            if (nodesMatch(nodeXml, qName)) {
                childSchemaPath = dataSchemaNode.getPath();
                foundMatchingXmlElement = true;
                break;
            }
        }
        if (foundMatchingXmlElement) {

            Map<QName, ConfigLeafAttribute> nodeAttrs = new LinkedHashMap<>();
            List<Element> childrenXml = new ArrayList<>();
            Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs = new HashMap<>();

            fillNodeAttributes(nodeXml, childSchemaPath, nodeAttrs, childrenXml, leafListAttrs, Collections.emptyMap());

            XmlModelNodeImpl node = new XmlModelNodeImpl(childSchemaPath, nodeAttrs, childrenXml, parentModelNode,
                    parentId, this,
                    m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, modelNodeDsm);
            node.setLeafLists(leafListAttrs);
            return node;
        } else {
            throw new RuntimeException("Invalid root element found in nodeXml: " + nodeXml.getLocalName());
        }
    }

    @Override
    public List<XmlModelNodeImpl> getModelNodeFromNodeSchemaPath(Element nodeXml, Map<QName, ConfigLeafAttribute>
            configAttrsFromEntity,
                                                                 SchemaPath nodeSchemaPath, ModelNodeId parentId,
                                                                 XmlModelNodeImpl parentModelNode,
                                                                 ModelNodeDataStoreManager modelNodeDsm) {
        List<XmlModelNodeImpl> nodes = new ArrayList<>();
        if (nodeXml != null) {
            List<Element> xmlNodes = new ArrayList<>();
            if (SchemaPathUtil.isRootSchemaPath(nodeSchemaPath) && !nodesMatch(nodeXml, nodeSchemaPath
                    .getLastComponent())) {
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
                    prepareNode(configAttrsFromEntity, nodeSchemaPath, parentId, parentModelNode, modelNodeDsm,
                            nodes, xmlNode);
                }
            }
        } else {
            //nodeXml is null, so create a xml modelnode with no attributes from the xml
            boolean allKeysFound = true;
            DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(nodeSchemaPath);
            if (dataSchemaNode instanceof ListSchemaNode) {
                for (QName keyQname : ((ListSchemaNode) dataSchemaNode).getKeyDefinition()) {
                    if (!configAttrsFromEntity.containsKey(keyQname)) {
                        allKeysFound = false;
                        break;
                    }
                }
            }
            if (allKeysFound) {
                prepareNode(configAttrsFromEntity, nodeSchemaPath, parentId, parentModelNode, modelNodeDsm,
                        nodes, null);
            }
        }

        return nodes;
    }

    private void prepareNode(Map<QName, ConfigLeafAttribute> configAttrsFromEntity, SchemaPath nodeSchemaPath,
                             ModelNodeId parentId, XmlModelNodeImpl parentModelNode, ModelNodeDataStoreManager
                                     modelNodeDsm, List<XmlModelNodeImpl> nodes, Element xmlNode) {
        Map<QName, ConfigLeafAttribute> nodeAttrs = new LinkedHashMap<>();
        List<Element> childrenXml = new ArrayList<>();
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs = new HashMap<>();

        fillNodeAttributes(xmlNode, nodeSchemaPath, nodeAttrs, childrenXml, leafListAttrs, configAttrsFromEntity);
        XmlModelNodeImpl node = getXmlModelNode(nodeSchemaPath, parentId, parentModelNode, modelNodeDsm, nodeAttrs,
                childrenXml, leafListAttrs);

        node.setLeafLists(leafListAttrs);
        nodes.add(node);
    }

    private void fillNodeAttributes(Element nodeXml, SchemaPath nodeSchemaPath, Map<QName, ConfigLeafAttribute>
            nodeAttrs, List<Element> childrenXml, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs, Map<?
            extends QName, ? extends ConfigLeafAttribute> configAttrsFromEntity) {
        List<Element> childElements = getChildElements(nodeXml);
        Collection<DataSchemaNode> dataSchemaNodes = m_schemaRegistry.getNonChoiceChildren(nodeSchemaPath);

        for (Element childElement : childElements) {
            for (DataSchemaNode dataSchemaNode : dataSchemaNodes) {
                QName qName = dataSchemaNode.getQName();
                if (nodesMatch(childElement, qName)) {
                    try {
                        if (dataSchemaNode instanceof ContainerSchemaNode || dataSchemaNode instanceof ListSchemaNode) {
                            childrenXml.add(childElement);
                            break;
                        } else if (dataSchemaNode instanceof LeafSchemaNode) {
                            nodeAttrs.put(dataSchemaNode.getQName(), ConfigAttributeFactory.getConfigAttribute
                                    (m_schemaRegistry,
                                    nodeSchemaPath, qName, childElement));
                            break;
                        } else if (dataSchemaNode instanceof LeafListSchemaNode) {
                            LinkedHashSet<ConfigLeafAttribute> values = leafListAttrs.get(dataSchemaNode.getQName());
                            if (values == null) {
                                values = new LinkedHashSet<>();
                                leafListAttrs.put(dataSchemaNode.getQName(), values);
                            }
                            values.add(ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry,
                                    nodeSchemaPath, qName, childElement));
                            break;
                        }
                    } catch (InvalidIdentityRefException e) {
                        LOGGER.error("Invalid identity ref value ", e.getRpcError()); //Who should handle this
                        // exception?
                    }
                }
            }
        }
        nodeAttrs.putAll(configAttrsFromEntity);
    }

    @Override
    public XmlModelNodeImpl getModelNode(Object entity, ModelNodeDataStoreManager modelNodeDSM) {
        ModelNodeId parentId = new ModelNodeId();
        SchemaPath schemaPath;
        Map<QName, ConfigLeafAttribute> attributes;
        List<Element> childrenXml = new ArrayList<>();
        try {
            //Get parentId and Schemapath
            Class<?> klass = getEntityClass(entity);
            Method parentIdMethod = m_entityRegistry.getParentIdGetter(klass);
            String parentIdStr = (String) parentIdMethod.invoke(entity);
            Method schemaPathMethod = m_entityRegistry.getSchemaPathGetter(klass);
            schemaPath = SchemaPathUtil.fromString((String) schemaPathMethod.invoke(entity));
            if (parentIdStr != null) {
                parentId = new ModelNodeId(parentIdStr, m_entityRegistry.getQName(klass)
                        .getNamespace().toString());
            }
            attributes = getConfigAttributesFromEntity(m_schemaRegistry, schemaPath, m_entityRegistry, klass, entity);
            ModelNodeKey key = MNKeyUtil.getKeyFromCriteria(schemaPath, attributes, m_schemaRegistry);
            ModelNodeId nodeId = EMNKeyUtil.getModelNodeId(key, parentId, schemaPath);
            XmlModelNodeImpl node = m_dsmCache.getFromCache(schemaPath, nodeId);
            if (node != null) {
                node.updateConfigAttributes(attributes);
                return node;
            }
            Map<QName, List<Object>> leafListAttrs = new HashMap<>();
            //Get leafList attributes
            Map<QName, Method> leafListGetters = m_entityRegistry.getYangLeafListGetters(klass);
            for (Map.Entry<QName, Method> getter : leafListGetters.entrySet()) {
                List<Object> values = (List<Object>) getter.getValue().invoke(entity);
                leafListAttrs.put(getter.getKey(), values);
            }

            // Get XmlSubtree
            Method yangXmlSubtreeGetter = m_entityRegistry.getYangXmlSubtreeGetter(klass);
            String xmlSubtree = (String) yangXmlSubtreeGetter.invoke(entity);

            if (xmlSubtree != null && !xmlSubtree.isEmpty()) {
                Collection<Element> rootElements = getChildElements(stringToDocument(xmlSubtree));
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

                            if (dataSchemaNode instanceof ContainerSchemaNode || dataSchemaNode instanceof
                                    ListSchemaNode) {
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

        XmlModelNodeImpl xmlModelNode = new XmlModelNodeImpl(schemaPath, attributes, childrenXml, null,
                parentId, this, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, modelNodeDSM);
        m_dsmCache.putInCache(schemaPath, xmlModelNode.getModelNodeId(), xmlModelNode);
        return xmlModelNode;
    }

    @Override
    public Element getXmlValue(XmlModelNodeImpl xmlModelNode) {
        SchemaPath schemaPath = xmlModelNode.getModelNodeSchemaPath();
        QName qName = schemaPath.getLastComponent();
        Element element = getDocument().createElementNS(qName.getNamespace().toString(), qName.getLocalName());

        //copy attributes
        Class klass = m_entityRegistry.getEntityClass(schemaPath);
        Set<QName> xmlModelNodeAttributes = new LinkedHashSet<>(xmlModelNode.getAttributes().keySet());
        Set<QName> attributesTobeStoredInXml = new LinkedHashSet<>(xmlModelNodeAttributes);

        if (klass != null) {
            // attributes without separate column in entity class
            Set<QName> attributeGettersKeyset = new LinkedHashSet<>(m_entityRegistry.getAttributeGetters(klass)
                    .keySet());
            attributesTobeStoredInXml.removeAll(attributeGettersKeyset);
        }

        for(QName attributeQName : attributesTobeStoredInXml){
            appendConfigAttributeToParentElement(xmlModelNode.getAttribute(attributeQName), element);
        }

        //copy leaf Lists
        Set<QName> leafListsTobeStoredInXml = new LinkedHashSet<>(xmlModelNode.getLeafLists().keySet());
        if (klass != null) {
            // leaf lists without separate column in entity class
            Set<QName> leafListGettersKeyset = new LinkedHashSet<>(m_entityRegistry.getYangLeafListGetters(klass)
                    .keySet());
            leafListsTobeStoredInXml.removeAll(leafListGettersKeyset);
        }

        for(QName leafListQName : leafListsTobeStoredInXml) {
            for(ConfigLeafAttribute value : xmlModelNode.getLeafList(leafListQName)){
                appendConfigAttributeToParentElement(value, element);
            }
        }

        //copy children
        for (Map.Entry<QName, List<XmlModelNodeImpl>> childrenOfType : xmlModelNode.getChildren().entrySet()) {
            for (XmlModelNodeImpl child : childrenOfType.getValue()) {
                element.appendChild(getDocument().importNode(getXmlValue(child), true));
            }
        }

        Element returnElement = element;
        /*if(xmlModelNode.isRoot()){
            returnElement = getDocument().createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources
            .RPC_REPLY_DATA);
            returnElement.appendChild(getDocument().importNode(element, true));
        }*/
        return returnElement;
    }

    @Override
    public XmlModelNodeImpl getRootXmlModelNode(ModelNodeWithAttributes modelNode, ModelNodeDataStoreManager dsm) {
        XmlModelNodeImpl xmlModelNode = new XmlModelNodeImpl(modelNode.getModelNodeSchemaPath(), modelNode
                .getAttributes(), new
                ArrayList<>(), null,
                new ModelNodeId(), this, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, dsm);
        m_dsmCache.putInCache(modelNode.getModelNodeSchemaPath(), modelNode.getModelNodeId(), xmlModelNode);
        return xmlModelNode;
    }

    private void appendConfigAttributeToParentElement(ConfigLeafAttribute attribute, Element parentElement) {
        parentElement.appendChild(getDocument().importNode(attribute.getDOMValue(),true));
    }

    static Map<QName, ConfigLeafAttribute> getConfigAttributesFromEntity(SchemaRegistry schemaRegistry, SchemaPath
            schemaPath,
                                                                         EntityRegistry entityRegistry, Class<?>
                                                                                 klass, Object entity) throws
            IllegalAccessException, InvocationTargetException {
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
                    configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(schemaRegistry,
                            schemaPath, namespace,
                            attributeGetter.getKey(), value);
                } else {
                    configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(schemaRegistry,
                            schemaPath, null,
                            attributeGetter.getKey(), value);
                }
                attributes.put(attributeGetter.getKey(), configLeafAttribute);
            }
        }
        return attributes;
    }

    private XmlModelNodeImpl getXmlModelNode(SchemaPath nodeSchemaPath, ModelNodeId parentId, XmlModelNodeImpl
            parentModelNode, ModelNodeDataStoreManager modelNodeDsm, Map<QName, ConfigLeafAttribute> nodeAttrs,
                                             List<Element> childrenXml, Map<QName,
            LinkedHashSet<ConfigLeafAttribute>> leafListAttrs) {
        ModelNodeKey key = MNKeyUtil.getKeyFromCriteria(nodeSchemaPath, nodeAttrs, m_schemaRegistry);
        ModelNodeId nodeId = EMNKeyUtil.getModelNodeId(key, parentId, nodeSchemaPath);
        XmlModelNodeImpl node = m_dsmCache.getFromCache(nodeSchemaPath, nodeId);
        if (node == null) {
            LOGGER.debug("Adding node of type {} with id {} into DSM cache", nodeSchemaPath, nodeId);
            node = new XmlModelNodeImpl(nodeSchemaPath, nodeAttrs, childrenXml, parentModelNode, parentId, this,
                    m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, modelNodeDsm);
            node.setLeafLists(leafListAttrs);
            m_dsmCache.putInCache(nodeSchemaPath, nodeId, node);
        } else {
            node.updateConfigAttributes(nodeAttrs);
            LOGGER.debug("Found node of type {} with id {} in DSM cache, re-using the instance from cache",
                    nodeSchemaPath, nodeId);
        }
        return node;
    }

    public static DocumentBuilder getDocBuilder() throws ParserConfigurationException {
        if (m_builder == null) {
            synchronized (XmlModelNodeToXmlMapperImpl.class) {
                if (m_builder == null) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    m_builder = factory.newDocumentBuilder();
                }
            }
        }
        return m_builder;
    }

    private Document getDocument() {
        if (m_doc == null) {
            try {
                m_doc = getDocBuilder().newDocument();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        return m_doc;
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
