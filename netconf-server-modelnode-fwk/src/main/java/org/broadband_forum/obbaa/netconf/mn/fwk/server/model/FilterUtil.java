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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getNewDocument;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil
        .getQNamesFromStateMatchNodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class FilterUtil {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(FilterUtil.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    public static void processFilter(FilterNode root, List<Element> filterXmlElements) {
        if (filterXmlElements != null) {

            for (Element filterXmlElement : filterXmlElements) {
                NodeList nodeList = filterXmlElement.getChildNodes();

                boolean selectNode = true;
                boolean containmentNode = false;

                FilterNode node = null;
                List<Element> childElements = new ArrayList<>();
                // we do a peak ahead towards the children
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node child = nodeList.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        // we can be sure current node is a containment node
                        if (!containmentNode) {
                            // only need to log this once...
                            LOGGER.trace("{} containment node", filterXmlElement.getLocalName());
                            selectNode = false;
                            containmentNode = true;

                            node = root.addContainmentNode(filterXmlElement.getLocalName(), filterXmlElement
                                    .getNamespaceURI());
                        }
                        childElements.add((Element) child);

                    } else if (child.getNodeType() == Node.TEXT_NODE && !child.getNodeValue().trim().isEmpty()) {
                        // we can be sure current node is a match node
                        LOGGER.trace("{} match node.  Condtion == {}", filterXmlElement.getLocalName(), child
                                .getNodeValue());
                        root.addMatchNode(filterXmlElement.getLocalName(), filterXmlElement.getNamespaceURI(), child
                                .getNodeValue());
                        selectNode = false;
                    } else if (child.getNodeType() == Node.TEXT_NODE && child.getNodeValue().trim().isEmpty()) {
                        // tells us nothing ...
                        LOGGER.trace("{} child node with only whitespace text", filterXmlElement.getLocalName());
                    }
                }

                if (!childElements.isEmpty() && node != null) {
                    processFilter(node, childElements);
                    reduceFilter(node);
                }
                if (selectNode) {
                    // must be a select node, only 3 options ...
                    LOGGER.trace("{} is a select node", filterXmlElement.getLocalName());
                    if (root.getSelectNode(filterXmlElement.getLocalName(), filterXmlElement.getNamespaceURI()) ==
                            null) {
                        root.addSelectNode(filterXmlElement.getLocalName(), filterXmlElement.getNamespaceURI());
                    } else {
                        LOGGER.trace("found duplicate select node {}, ignoring..", filterXmlElement.getLocalName());
                    }
                }
            }

        }
    }

    private static void reduceFilter(FilterNode node) {
        for (FilterNode selectNode : node.getSelectNodes()) {
            //check if there is a child node for the select node
            Iterator<FilterNode> childIterator = node.getChildNodes().iterator();
            while (childIterator.hasNext()) {
                FilterNode child = childIterator.next();
                //remove the child node if there is a select node for the same child (since child filter is a subset
                // of select filter)
                if (child.getNodeName().equals(selectNode.getNodeName()) && child.getNamespace().equals(selectNode
                        .getNamespace())) {
                    LOGGER.trace("child node found while there was a select node {}, removing child node", child
                            .getNodeName());
                    childIterator.remove();
                }
            }

            //check if there is a match node for the select node
            Iterator<FilterMatchNode> matchNodeIterator = node.getMatchNodes().iterator();
            while (matchNodeIterator.hasNext()) {
                FilterMatchNode child = matchNodeIterator.next();
                //remove the child node if there is a match node for the same child (since match filter is a subset
                // of select filter)
                if (child.getNodeName().equals(selectNode.getNodeName()) && child.getNamespace().equals(selectNode
                        .getNamespace())) {
                    LOGGER.trace("match node found while there was a select node {}, removing match node", child
                            .getNodeName());
                    matchNodeIterator.remove();
                }
            }
        }
    }

    public static Element filterToXml(SchemaRegistry schemaRegistry, FilterNode filter) {
        try {

            Document document = getNewDocument();
            String namespace = filter.getNamespace();
            Element filterElement = document.createElementNS(namespace, EditTreeTransformer.resolveLocalName
                    (schemaRegistry, namespace, filter.getNodeName()));
            Element result = filterToXml(schemaRegistry, document, filterElement, filter);
            LOGGER.debug("Filter XML: \n {}", DocumentUtils.documentToPrettyString(result));
            return (Element) result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Element filterToXml(SchemaRegistry schemaRegistry, Document document, Element filterElement,
                                       FilterNode filter) {
        for (FilterNode node : filter.getChildNodes()) {
            String namespace = node.getNamespace();
            Element childElement = document.createElementNS(namespace, EditTreeTransformer.resolveLocalName
                    (schemaRegistry, namespace, node.getNodeName()));
            filterElement.appendChild(childElement);
            filterToXml(schemaRegistry, document, childElement, node);
        }

        for (FilterMatchNode node : filter.getMatchNodes()) {
            String namespace = node.getNamespace();
            Element childElement = document.createElementNS(namespace, EditTreeTransformer.resolveLocalName
                    (schemaRegistry, namespace, node.getNodeName()));
            childElement.setTextContent(node.getFilter());
            filterElement.appendChild(childElement);
        }

        for (FilterNode node : filter.getSelectNodes()) {
            String namespace = node.getNamespace();
            Element childElement = document.createElementNS(namespace, EditTreeTransformer.resolveLocalName
                    (schemaRegistry, namespace, node.getNodeName()));
            filterElement.appendChild(childElement);
        }
        return filterElement;
    }

    /**
     * Check state filter matchnodes and subtree filternodes with match conditions and return state filter elements.
     * Subsystem responsible for responding to the state attributes and state filter nodes are populated and queried.
     * Empty list is returned if any filter does not match.
     *
     * @param stateFilterMatchNodes
     * @param stateSubtreeFilterNodes
     * @param modelNodeId
     * @param schemaPath
     * @param schemaRegistry
     * @param subSystemRegistry
     * @return
     * @throws GetAttributeException
     */
    public static List<Element> checkAndGetStateFilterElements(List<FilterMatchNode> stateFilterMatchNodes,
                                                               List<FilterNode>
            stateSubtreeFilterNodes, ModelNodeId modelNodeId, SchemaPath schemaPath, SchemaRegistry schemaRegistry,
                                                               SubSystemRegistry subSystemRegistry)
            throws GetAttributeException {
        List<Element> stateElements = new ArrayList<>();

        List<QName> stateQNames = getQNamesFromStateMatchNodes(stateFilterMatchNodes, schemaRegistry);
        Map<SubSystem, Pair<List<QName>, List<FilterNode>>> stateAttributesPerSubSystem = new HashMap<>();
        populateSubSystemToStateAttrMap(stateAttributesPerSubSystem, stateQNames, schemaPath, schemaRegistry,
                subSystemRegistry);
        populateSubSystemToFilterNodeMap(stateAttributesPerSubSystem, stateSubtreeFilterNodes, schemaPath,
                schemaRegistry, subSystemRegistry);

        for (Map.Entry<SubSystem, Pair<List<QName>, List<FilterNode>>> entry : stateAttributesPerSubSystem.entrySet()) {
            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttrs = new HashMap<>();
            stateAttrs.put(modelNodeId, new Pair<>(entry.getValue().getFirst(), entry.getValue().getSecond()));
            Map<ModelNodeId, List<Element>> result = entry.getKey().retrieveStateAttributes(stateAttrs,
                    NetconfQueryParams.NO_PARAMS);

            List<Element> stateElementsFromSubsystem = result.get(modelNodeId);

            if (stateElementsFromSubsystem != null && !stateElementsFromSubsystem.isEmpty()) {
                //check state leaf attribute match conditions
                for (FilterMatchNode matchNode : stateFilterMatchNodes) {
                    boolean found = false;
                    for (Element element : stateElementsFromSubsystem) {
                        if (matchNode.getNodeName().equals(element.getLocalName())) {
                            if (matchNode.getFilter().equals(element.getTextContent())) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        return Collections.emptyList();
                    }
                }
                //check state subtree match conditions (if any)
                for (FilterNode filterNode : stateSubtreeFilterNodes) {
                    if (filterNode.childHasMatchCondition()) {
                        boolean found = false;
                        for (Element element : stateElementsFromSubsystem) {
                            if (filterNode.getNodeName().equals(element.getLocalName())) {
                                //atleast one state subtree element was found, so match was true
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            return Collections.emptyList();
                        }
                    }
                }

                //both leaf match conditions and subtree match conditions passed, so copy
                stateElements.addAll(stateElementsFromSubsystem);
            } else {
                LOGGER.error("Couldn't fetch state attributes from SubSystem");
                return Collections.emptyList();
            }
        }
        return stateElements;
    }

    private static void populateSubSystemToFilterNodeMap(Map<SubSystem, Pair<List<QName>, List<FilterNode>>>
                                                                 stateAttributesPerSubSystem, List<FilterNode>
                                                                 stateSubtreeFilterNodes, SchemaPath schemaPath,
                                                         SchemaRegistry schemaRegistry, SubSystemRegistry
                                                                 subSystemRegistry) {
        // Iterate over filterNodes and find the subsystem responsible
        for (FilterNode filterNode : stateSubtreeFilterNodes) {
            SchemaNode schemaNode = SchemaRegistryUtil.getChildSchemaNode(filterNode.getNamespace(), filterNode
                            .getNodeName(),
                    schemaPath, schemaRegistry);
            SubSystem subSystem = getSubSystem(subSystemRegistry, schemaNode.getPath());
            if (subSystem != null) {
                if (stateAttributesPerSubSystem.get(subSystem) == null) {
                    List<FilterNode> filterNodes = new ArrayList<>();
                    filterNodes.add(filterNode);
                    stateAttributesPerSubSystem.put(subSystem, new Pair<>(new ArrayList<>(), filterNodes));
                } else {
                    Pair<List<QName>, List<FilterNode>> existingPair = stateAttributesPerSubSystem.get(subSystem);
                    existingPair.getSecond().add(filterNode);
                }
            }
        }

    }

    private static void populateSubSystemToStateAttrMap(Map<SubSystem, Pair<List<QName>, List<FilterNode>>>
                                                                stateAttributesPerSubSystem, List<QName>
                                                                stateFilterMatchNodeQNames, SchemaPath schemaPath,
                                                        SchemaRegistry
                                                                schemaRegistry,
                                                        SubSystemRegistry subSystemRegistry) {
        for (QName stateFilterMatchNodeQname : stateFilterMatchNodeQNames) {
            SchemaNode schemaNode = SchemaRegistryUtil.getChildSchemaNode(stateFilterMatchNodeQname.getNamespace()
                            .toString(),
                    stateFilterMatchNodeQname.getLocalName(), schemaPath, schemaRegistry);
            SubSystem subSystem = getSubSystem(subSystemRegistry, schemaNode.getPath());
            if (subSystem != null) {
                if (stateAttributesPerSubSystem.get(subSystem) == null) {
                    List<QName> stateAttributes = new ArrayList<>();
                    stateAttributes.add(stateFilterMatchNodeQname);
                    stateAttributesPerSubSystem.put(subSystem, new Pair<>(stateAttributes, new ArrayList<>()));
                } else {
                    Pair<List<QName>, List<FilterNode>> existingPair = stateAttributesPerSubSystem.get(subSystem);
                    existingPair.getFirst().add(stateFilterMatchNodeQname);
                }
            }
        }

    }

    public static SubSystem getSubSystem(SubSystemRegistry subSystemRegistry, SchemaPath schemaPath) {
        return subSystemRegistry.lookupSubsystem(schemaPath);
    }

    public static FilterNode nodeIdToFilter(ModelNodeId nodeId) {
        List<ModelNodeRdn> rdns = nodeId.getRdns();

        if (rdns.size() > 0) {
            ModelNodeRdn firstRdn = rdns.get(0);
            FilterNode filterNode = new FilterNode(firstRdn.getRdnValue(), firstRdn.getNamespace());
            FilterNode rootNode = filterNode;

            for (int i = 1; i < rdns.size(); i++) {
                ModelNodeRdn rdn = rdns.get(i);
                filterNode = getFilterNode(filterNode, rdn);
            }
            return rootNode;
        }
        return null;
    }

    private static FilterNode getFilterNode(FilterNode filterNode, ModelNodeRdn rdn) {
        if (ModelNodeRdn.CONTAINER.equals(rdn.getRdnName())) {
            FilterNode newContainer = new FilterNode(rdn.getRdnValue(), rdn.getNamespace());
            filterNode.addContainmentNode(newContainer);
            filterNode = newContainer;
        } else {
            filterNode.addMatchNode(rdn.getRdnName(), rdn.getNamespace(), rdn.getRdnValue());
        }
        return filterNode;
    }
}
