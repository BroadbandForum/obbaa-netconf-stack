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

package org.broadband_forum.obbaa.netconf.mn.fwk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.OperationAttributeUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AggregatedTreeNode {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AggregatedTreeNode.class, LogAppNames.NETCONF_STACK);
    private static final String LIST_KEY_ELEMENT_NOT_EXISTS = "Could not find the key element %s for the node %s";
    private static final String OPERATION_CLASH_ERROR = "merging multiple edit payload failed because of edit operation clash : %s on node : %s";
    private static final String NODE_OPERATION_FORBIDDEN = "specified operation %s is forbidden on the node %s inside the parent node operation %s ";
    private static final String NODE_NOT_FOUND = "Could not find the node %s in the YANG schema.";
    private static final String LEAFLIST_OPERATION_CLASH = "Cannot merge leaf-lists from more than one edit-config, found multiple leaf-lists of type: %s";

    private SchemaRegistry m_schemaRegistry;
    private Element m_domElement;
    private SchemaPath m_schemaPath;
    private Map<SchemaPath, Map<ModelNodeId, AggregatedTreeNode>> m_childNodes = new LinkedHashMap<>();
    private Map<SchemaPath, Element> m_childLeaves = new LinkedHashMap<>();
    private Map<SchemaPath, List<Element>> m_childLeafLists = new LinkedHashMap<>();
    private Map<SchemaPath, List<Element>> m_childLeafListsTemp = new LinkedHashMap<>();
    private ModelNodeId m_modelNodeId;
    private List<Node> m_attributes = new ArrayList<>();
    private AggregatedTreeNode m_parentTreeNode;
    private String m_atnOperation;

    private AggregatedTreeNode(SchemaRegistry schemaRegistry, Element domElement, SchemaPath schemaPath, ModelNodeId modelNodeId, boolean doAddImplicit) {
        super();
        this.m_domElement = domElement;
        this.m_schemaPath = schemaPath;
        m_schemaRegistry = SchemaRegistryUtil.retrievePossibleMountRegistry(domElement, schemaPath, schemaRegistry, true);
        this.m_modelNodeId = modelNodeId;
        if(m_modelNodeId == null) {
            if(schemaPath.equals(SchemaPath.ROOT)) {
                m_modelNodeId = ModelNodeId.EMPTY_NODE_ID;
            } else {
                DataSchemaNode dsn = m_schemaRegistry.getDataSchemaNode(schemaPath);
                m_modelNodeId = buildModelNodeId(dsn, domElement);
            }
        }
        setNodeAttributes(domElement, this);
        if(doAddImplicit) {
            add(domElement);
        }
    }

    public AggregatedTreeNode(SchemaRegistry schemaRegistry, Element domElement, SchemaPath schemaPath) {
        this(schemaRegistry, domElement, schemaPath, null, true);
    }

    private void setNodeAttributes(Element domElement, AggregatedTreeNode atn) {
        NamedNodeMap attributes = domElement.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attributeNode = attributes.item(i);
                atn.m_attributes.add(attributeNode);
            }
        }
        atn.m_atnOperation = OperationAttributeUtil.getOperationAttribute(domElement);
    }

    public void add(Element element) {
        try {
            doIterateChildElements(element);       
            addLeafListFields();
        } finally {
            m_childLeafListsTemp.clear();
        }
    }

    private void addLeafListFields() {
        for(SchemaPath leafListSP : m_childLeafListsTemp.keySet()) {
            List<Element> leafListEntries = m_childLeafLists.get(leafListSP);
            if(leafListEntries == null || leafListEntries.isEmpty()) {
                leafListEntries = m_childLeafListsTemp.get(leafListSP);
                m_childLeafLists.put(leafListSP, leafListEntries);
            } else {
                QName childQName = leafListSP.getLastComponent();
                ModelNodeId modelNodeId = getModelNodeId(childQName);
                throw new AggregatedTreeNodeException(String.format(LEAFLIST_OPERATION_CLASH, leafListSP.getLastComponent()), modelNodeId, m_schemaRegistry); 
            }
        }
    }

    private void doIterateChildElements(Element element) {
        NodeList nodes = element.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node childNode = nodes.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                String localName = childNode.getLocalName();
                QName nodeQName = m_schemaRegistry.lookupQName(childNode.getNamespaceURI(), localName);
                DataSchemaNode childDSN = null;       
                if(nodeQName != null) {
                    childDSN = m_schemaPath.equals(SchemaPath.ROOT)? m_schemaRegistry.getDataSchemaNode(Arrays.asList(nodeQName)) :m_schemaRegistry.getNonChoiceChild(m_schemaPath, nodeQName);
                }
                if(nodeQName == null || childDSN == null) {
                    throw new AggregatedTreeNodeException(String.format(NODE_NOT_FOUND, nodeQName == null? localName:nodeQName), m_modelNodeId, m_schemaRegistry);
                }
                SchemaPath childPath = childDSN.getPath();
                Attr nodeOperationAttr = OperationAttributeUtil.getOperationAttributeOnNode((Element) childNode);
                if(nodeOperationAttr != null) {
                    String nodeOperation = nodeOperationAttr.getValue();
                    if(!nodeOperation.equals(EditConfigOperations.REPLACE)) {
                        if(EditConfigOperations.REPLACE.equals(m_atnOperation)) {
                            ModelNodeId modelNodeId = getModelNodeId(nodeQName);
                            throw new AggregatedTreeNodeException(String.format(NODE_OPERATION_FORBIDDEN, nodeOperation ,nodeQName, m_atnOperation), modelNodeId, m_schemaRegistry); 
                        }
                    }
                }
                removeAllOtherCaseNodes(childPath);

                if(childDSN instanceof LeafSchemaNode) {
                    Element leafEntry = m_childLeaves.get(childPath);
                    if(leafEntry == null) {
                        m_childLeaves.put(childPath, (Element) childNode);
                    } else {
                        String existingLeafNodeOperation = OperationAttributeUtil.getOperationAttribute((Element) leafEntry);
                        String newLeafNodeOperation = OperationAttributeUtil.getOperationAttribute((Element) childNode);
                        boolean isAggregateOperationAllowed = OperationAttributeUtil.isAggregatedEditOperationAllowed(existingLeafNodeOperation, newLeafNodeOperation);
                        if(!isAggregateOperationAllowed) {
                            ModelNodeId modelNodeId = getModelNodeId(nodeQName);
                            String editOperationClash = getOperationClashFormat(existingLeafNodeOperation, newLeafNodeOperation);
                            throw new AggregatedTreeNodeException(String.format(OPERATION_CLASH_ERROR, editOperationClash, nodeQName), modelNodeId, m_schemaRegistry);  
                        }
                        m_childLeaves.put(childPath, (Element) childNode);
                    }
                } else if(childDSN instanceof LeafListSchemaNode) {                  
                    List<Element> tempLeafListEntries = m_childLeafListsTemp.get(childPath);
                    if(tempLeafListEntries == null) {
                        tempLeafListEntries = new LinkedList<>();
                        m_childLeafListsTemp.put(childPath, tempLeafListEntries);
                    } 
                    tempLeafListEntries.add((Element) childNode);
                } else if(childDSN instanceof ContainerSchemaNode || childDSN instanceof ListSchemaNode) {
                    ModelNodeId childModelNodeId = buildModelNodeId(childDSN, (Element)childNode);                    
                    Map<ModelNodeId, AggregatedTreeNode> childTreeNodes = m_childNodes.get(childPath);
                    if(childTreeNodes == null) {
                        childTreeNodes = new LinkedHashMap<>();
                        m_childNodes.put(childPath, childTreeNodes);
                    }
                    AggregatedTreeNode childTreeNode = childTreeNodes.get(childModelNodeId);
                    if(childTreeNode == null) {
                        childTreeNode = new AggregatedTreeNode(m_schemaRegistry, (Element) childNode, childPath, childModelNodeId, false);
                        childTreeNode.setParentTreeNode(this);
                        if(!EditConfigOperations.isOperationDeleteOrRemove(childTreeNode.m_atnOperation)) {
                            childTreeNode.add((Element) childNode);
                        } else {
                            // it should be either delete or remove, so if it is container node, don't bother about child nodes, but if its list node, then just add key elements
                            clearAllChildNodes(childTreeNode);
                            updateKeyElements(childNode, childDSN, childTreeNode);
                        }
                        childTreeNodes.put(childModelNodeId, childTreeNode);
                    } else {
                        String existingAtnNodeOperation = childTreeNode.m_atnOperation;
                        String newAtnNodeOperation = OperationAttributeUtil.getOperationAttribute((Element) childNode);
                        boolean isAggregateOperationAllowed = OperationAttributeUtil.isAggregatedEditOperationAllowed(existingAtnNodeOperation, newAtnNodeOperation);
                        if(isAggregateOperationAllowed) {
                            setNodeAttributes((Element) childNode, childTreeNode);
                            if(newAtnNodeOperation.equals(EditConfigOperations.REPLACE)) {
                                // have to replace all its child contents if exists
                                clearAllChildNodes(childTreeNode);
                            }
                            if(!EditConfigOperations.isOperationDeleteOrRemove(newAtnNodeOperation)) {
                                childTreeNode.add((Element) childNode);
                            } else {
                                // it should be either delete or remove, so if it is container node, don't bother about child nodes, but if its list node, then just add key elements
                                clearAllChildNodes(childTreeNode);
                                updateKeyElements(childNode, childDSN, childTreeNode);
                            }
                        } else {
                            String editOperationClash = getOperationClashFormat(existingAtnNodeOperation, newAtnNodeOperation);
                            throw new AggregatedTreeNodeException(String.format(OPERATION_CLASH_ERROR, editOperationClash, nodeQName), childModelNodeId, m_schemaRegistry); 
                        }
                    }


                }

            }
        }
    }

    private void clearAllChildNodes(AggregatedTreeNode childTreeNode) {
        childTreeNode.m_childLeaves.clear();
        childTreeNode.m_childLeafLists.clear();
        childTreeNode.m_childNodes.clear();
    }

    private void updateKeyElements(Node childNode, DataSchemaNode childDSN, AggregatedTreeNode childTreeNode) {
        if(childDSN instanceof ListSchemaNode) {
            for(QName key : ((ListSchemaNode) childDSN).getKeyDefinition()) {
                DataSchemaNode keyDSN = childTreeNode.m_schemaRegistry.getNonChoiceChild(childDSN.getPath(), key);
                String keyName = key.getLocalName();
                String keyNS = key.getNamespace().toString();
                Element keyElement = DocumentUtils.getDirectChildElement((Element) childNode, keyName, keyNS);
                childTreeNode.m_childLeaves.put(keyDSN.getPath(), (Element) keyElement);
            }
        }
    }

    private String getOperationClashFormat(String operation1, String operation2) {
        return "<" + operation1 + ">X<" + operation2 + ">";
    }

    private void removeAllOtherCaseNodes(SchemaPath childPath) {
        Set<DataSchemaNode> allOtherCaseChildNodes = new HashSet<>();
        ChoiceCaseNodeUtil.fillAllOtherNestedCaseDataChildNodes(m_schemaRegistry, childPath, allOtherCaseChildNodes);
        if(allOtherCaseChildNodes != null) {
            for(DataSchemaNode caseChildNode : allOtherCaseChildNodes) {
                SchemaPath caseChildNodeSP = caseChildNode.getPath();
                if(caseChildNode instanceof LeafSchemaNode) {
                    m_childLeaves.remove(caseChildNodeSP);
                } else if(caseChildNode instanceof LeafListSchemaNode) {
                    m_childLeafLists.remove(caseChildNodeSP);
                } else {
                    m_childNodes.remove(caseChildNodeSP);
                }
            }
        }
    }

    private ModelNodeId buildModelNodeId(DataSchemaNode dsn, Element node) {
        QName nodeQName = dsn.getQName();
        ModelNodeId modelNodeId = getModelNodeId(nodeQName);

        if(dsn instanceof ListSchemaNode) {
            for(QName key : ((ListSchemaNode) dsn).getKeyDefinition()) {
                String keyName = key.getLocalName();
                String keyNS = key.getNamespace().toString();
                Element keyElement = DocumentUtils.getDirectChildElement(node, keyName, keyNS);
                if(keyElement == null) {
                    throw new AggregatedTreeNodeException(String.format(LIST_KEY_ELEMENT_NOT_EXISTS, key, nodeQName), modelNodeId, m_schemaRegistry);   
                }
                ModelNodeRdn keyRdn = new ModelNodeRdn(keyName, keyNS, keyElement.getTextContent());
                modelNodeId.addRdn(keyRdn);
            }
        }
        return modelNodeId;
    }

    private ModelNodeId getModelNodeId(QName nodeQName) {
        ModelNodeId modelNodeId = new ModelNodeId(m_modelNodeId);
        ModelNodeRdn nodeRdn = new ModelNodeRdn(ModelNodeRdn.CONTAINER, nodeQName.getNamespace().toString(), nodeQName.getLocalName());
        modelNodeId.addRdn(nodeRdn);
        return modelNodeId;
    }

    public Element toDom() {
        Document document = m_domElement.getOwnerDocument();    
        Element element = document.createElementNS(m_domElement.getNamespaceURI(), m_domElement.getNodeName());
        m_domElement = rebuildDomTree(document, element, element, this);
        return m_domElement;
    }

    private Element rebuildDomTree(Document document, Element root, Element element, AggregatedTreeNode treeNode) {
        for(Node attribute :treeNode.m_attributes) {
            element.setAttributeNS(attribute.getNamespaceURI(), attribute.getNodeName(), attribute.getNodeValue());
        }
        treeNode.m_childLeaves.forEach((k, v) -> {
            element.appendChild(v);
        });
        treeNode.m_childLeafLists.forEach((k, v) -> {
            v.forEach(e -> {
                element.appendChild(e);
            });
        });

        treeNode.m_childNodes.values()
        .stream()
        .flatMap(v->v.values().stream())
        .forEach(atn-> {
            Element actualNodeDomElement = atn.m_domElement;
            Element childElement = document.createElementNS(actualNodeDomElement.getNamespaceURI(), actualNodeDomElement.getNodeName());
            element.appendChild(childElement);
            rebuildDomTree(document, root, childElement, atn);
        });
        return root;
    }

    public String toString() {
        try {
            return DocumentUtils.documentToPrettyString(toDom());
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("error while converting aggregatedTreeNode dom element to string ");
        }
        return null;
    }

    public void setParentTreeNode(AggregatedTreeNode node) {
        m_parentTreeNode = node;
    }

}
