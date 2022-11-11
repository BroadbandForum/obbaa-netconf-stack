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
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil.getQNamesFromStateMatchNodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.FilterNodeUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FilterUtil {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(FilterUtil.class, LogAppNames.NETCONF_STACK);

    public static void processFilter(FilterNode root, List<Element> filterXmlElements, SchemaRegistry schemaRegistry) {
    	processFilter(root, filterXmlElements, schemaRegistry.retrieveAllMountPointsPath());
    }

    public static void processFilter(FilterNode root, List<Element> filterXmlElements) {
    	processFilter(root, filterXmlElements, new HashSet<>());
    }
    
    public static void processFilter(FilterNode root, List<Element> filterXmlElements, Set<QName> mountPointQNames) {
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

                            node = root.addContainmentNode(filterXmlElement.getLocalName(), filterXmlElement.getNamespaceURI());
                            Node parentNode = filterXmlElement.getParentNode();
                            if(parentNode != null && parentNode.getNamespaceURI()!= null && parentNode.getLocalName() != null){
                            	QName parentQName = QName.create(parentNode.getNamespaceURI(), parentNode.getLocalName());
                            	if(mountPointQNames.contains(parentQName)){
                            		node.setMountPointImmediateChild(true);
                            	}
                            }
                        }
                        childElements.add((Element) child);

                    } else if (child.getNodeType() == Node.TEXT_NODE && !child.getNodeValue().trim().isEmpty()) {
                        // we can be sure current node is a match node
                        LOGGER.trace("{} match node.  Condtion == {}", filterXmlElement.getLocalName(), child.getNodeValue());
                        FilterMatchNode filterMatchNode = root.addMatchNode(filterXmlElement.getLocalName(), filterXmlElement.getNamespaceURI(), child.getNodeValue());
                        FilterNodeUtil.xmlElementToFilterNode(filterMatchNode, filterXmlElement);
                        selectNode = false;
                    } else if (child.getNodeType() == Node.TEXT_NODE && child.getNodeValue().trim().isEmpty()) {
                        // tells us nothing ...
                        LOGGER.trace("{} child node with only whitespace text", filterXmlElement.getLocalName());
                    }
                }

                if (!childElements.isEmpty() && node != null) {
                    processFilter(node, childElements, mountPointQNames);
                    reduceFilter(node);
                }
                if (selectNode) {
                    // must be a select node, only 3 options ...
                    LOGGER.trace("{} is a select node", filterXmlElement.getLocalName());
                    if (root.getSelectNode(filterXmlElement.getLocalName(), filterXmlElement.getNamespaceURI()) == null) {
                        root.addSelectNode(filterXmlElement.getLocalName(), filterXmlElement.getNamespaceURI());
                        FilterNode selectFilterNode = root.getSelectNode(filterXmlElement.getLocalName(), filterXmlElement.getNamespaceURI());
                        
                        Node parentNode = filterXmlElement.getParentNode();
                        if(parentNode != null && parentNode.getNamespaceURI()!= null && parentNode.getLocalName() != null){
                        	QName parentQName = QName.create(parentNode.getNamespaceURI(), parentNode.getLocalName());
                        	if(mountPointQNames.contains(parentQName)){
                        		selectFilterNode.setMountPointImmediateChild(true);
                        	}
                        }
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
                //remove the child node if there is a select node for the same child (since child filter is a subset of select filter)
                if (child.getNodeName().equals(selectNode.getNodeName()) && child.getNamespace().equals(selectNode.getNamespace())) {
                    LOGGER.trace("child node found while there was a select node {}, removing child node", child.getNodeName());
                    childIterator.remove();
                }
            }

            //check if there is a match node for the select node
            Iterator<FilterMatchNode> matchNodeIterator = node.getMatchNodes().iterator();
            while (matchNodeIterator.hasNext()) {
                FilterMatchNode child = matchNodeIterator.next();
                //remove the child node if there is a match node for the same child (since match filter is a subset of select filter)
                if (child.getNodeName().equals(selectNode.getNodeName()) && child.getNamespace().equals(selectNode.getNamespace())) {
                    LOGGER.trace("match node found while there was a select node {}, removing match node", child.getNodeName());
                    matchNodeIterator.remove();
                }
            }
        }
    }

    public static Element filterToXml(SchemaRegistry schemaRegistry, FilterNode filter) {
        try {

            Document document = getNewDocument();
            String namespace = filter.getNamespace();
            Element filterElement = document.createElementNS(namespace, EditTreeTransformer.resolveLocalName(schemaRegistry, namespace, filter.getNodeName()));
            Element result = filterToXml(schemaRegistry, document, filterElement, filter);
            LOGGER.debug("Filter XML: \n {}", DocumentUtils.documentToPrettyString(result));
            return (Element) result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Element filterToXml(SchemaRegistry schemaRegistry, Document document, Element filterElement, FilterNode filter) {
        for (FilterNode node : filter.getChildNodes()) {
            String namespace = node.getNamespace();
            Element childElement = document.createElementNS(namespace, EditTreeTransformer.resolveLocalName(schemaRegistry, namespace, node.getNodeName()));
            filterElement.appendChild(childElement);
            filterToXml(schemaRegistry, document, childElement, node);
        }

        for (FilterMatchNode node : filter.getMatchNodes()) {
            String namespace = node.getNamespace();
            Element childElement = document.createElementNS(namespace, EditTreeTransformer.resolveLocalName(schemaRegistry, namespace, node.getNodeName()));
            childElement.setTextContent(node.getFilter());
            FilterNodeUtil.filterNodeToXmlElement(node,childElement);
            filterElement.appendChild(childElement);
        }

        for (FilterNode node : filter.getSelectNodes()) {
            String namespace = node.getNamespace();
            Element childElement = document.createElementNS(namespace, EditTreeTransformer.resolveLocalName(schemaRegistry, namespace, node.getNodeName()));
            filterElement.appendChild(childElement);
        }
        return filterElement;
    }

    /**
     * Check state filter matchnodes and subtree filternodes with match conditions and return state filter elements.
     * Subsystem responsible for responding to the state attributes and state filter nodes are populated and queried.
     * Empty list is returned if any filter does not match.
     *
     *
     * @param stateContext
     * @param stateFilterMatchNodes
     * @param stateSubtreeFilterNodes
     * @param modelNodeId
     * @param schemaPath
     * @param schemaRegistry
     * @param subSystemRegistry
     * @return
     * @throws GetAttributeException
     */
    public static List<Element> checkAndGetStateFilterElements(StateAttributeGetContext stateContext, List<FilterMatchNode> stateFilterMatchNodes, List<FilterNode>
            stateSubtreeFilterNodes, ModelNodeId modelNodeId, SchemaPath schemaPath, SchemaRegistry schemaRegistry,
                                                               SubSystemRegistry subSystemRegistry)
            throws GetAttributeException {
        List<Element> stateElements = new ArrayList<>();

        List<QName> stateQNames = getQNamesFromStateMatchNodes(stateFilterMatchNodes, schemaRegistry);
        Map<SubSystem, Pair<List<QName>, List<FilterNode>>> stateAttributesPerSubSystem = new HashMap<>();
        populateSubSystemToStateAttrMap(stateAttributesPerSubSystem, stateQNames, schemaPath, schemaRegistry, subSystemRegistry);
        populateSubSystemToFilterNodeMap(stateAttributesPerSubSystem, stateSubtreeFilterNodes, schemaPath, schemaRegistry, subSystemRegistry);

        for (Map.Entry<SubSystem, Pair<List<QName>, List<FilterNode>>> entry : stateAttributesPerSubSystem.entrySet()) {
            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttrs = new HashMap<>();
            stateAttrs.put(modelNodeId, new Pair<>(entry.getValue().getFirst(), entry.getValue().getSecond()));
            Map<ModelNodeId, List<Element>> result = entry.getKey().retrieveStateAttributes(stateAttrs, NetconfQueryParams.NO_PARAMS,stateContext);

            List<Element> stateElementsFromSubsystem = result.get(modelNodeId);

            if (stateElementsFromSubsystem != null && !stateElementsFromSubsystem.isEmpty()) {
                //check state leaf attribute match conditions
                for (FilterMatchNode matchNode : stateFilterMatchNodes) {
                    boolean found = false;
                    Element stateElementMatchingFilterMatchNode = null;
                    for (Element element : stateElementsFromSubsystem) {
                        if (matchNode.getNodeName().equals(element.getLocalName())) {
                            if (matchNode.getFilter().equals(element.getTextContent())) {
                                stateElementMatchingFilterMatchNode = element;
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        return Collections.emptyList();
                    }else if(stateElementMatchingFilterMatchNode!=null){
                        stateElements.add(stateElementMatchingFilterMatchNode);
                    }
                }

                for (FilterNode filterNode : stateSubtreeFilterNodes) {
                    for (Element element : stateElementsFromSubsystem) {
                        if (filterNode.getNodeName().equals(element.getLocalName())) {
                            stateElements.add(element);
                            break;
                        }
                    }
                }

            } else {
                LOGGER.debug("Couldn't fetch state attributes for {} from SubSystem {}", entry.getValue().getSecond(), entry.getKey());
                return Collections.emptyList();
            }
        }
        return stateElements;
    }

    private static void populateSubSystemToFilterNodeMap(Map<SubSystem, Pair<List<QName>, List<FilterNode>>>
                                                                 stateAttributesPerSubSystem, List<FilterNode>
                                                                 stateSubtreeFilterNodes, SchemaPath schemaPath,
                                                         SchemaRegistry schemaRegistry, SubSystemRegistry subSystemRegistry) {
        // Iterate over filterNodes and find the subsystem responsible
        for (FilterNode filterNode : stateSubtreeFilterNodes) {
            SchemaNode schemaNode = SchemaRegistryUtil.getChildSchemaNode(filterNode.getNamespace(), filterNode.getNodeName(),
                    schemaPath, schemaRegistry);
            if ( schemaNode == null){
            	schemaNode = SchemaRegistryUtil.getChildSchemaNode(filterNode.getNamespace(), filterNode.getNodeName(),
            			SchemaPath.ROOT, schemaRegistry);
            	schemaPath = schemaNode.getPath();
            }
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
                                                                stateFilterMatchNodeQNames, SchemaPath schemaPath, SchemaRegistry
                                                                schemaRegistry,
                                                        SubSystemRegistry subSystemRegistry) {
        for (QName stateFilterMatchNodeQname : stateFilterMatchNodeQNames) {
            SchemaNode schemaNode = SchemaRegistryUtil.getChildSchemaNode(stateFilterMatchNodeQname.getNamespace().toString(),
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
        List<ModelNodeRdn> rdns = nodeId.getRdnsReadOnly();

        if(rdns.size() >0){
            ModelNodeRdn firstRdn = rdns.get(0);
            FilterNode filterNode = new FilterNode(firstRdn.getRdnValue(), firstRdn.getNamespace());
            FilterNode rootNode = filterNode;

            for(int i = 1; i< rdns.size(); i++){
                ModelNodeRdn rdn = rdns.get(i);
                filterNode = getFilterNode(filterNode, rdn);
            }
            return rootNode;
        }
        return null;
    }

    private static FilterNode getFilterNode(FilterNode filterNode, ModelNodeRdn rdn) {
        if(CONTAINER.equals(rdn.getRdnName())) {
            FilterNode newContainer = new FilterNode(rdn.getRdnValue(), rdn.getNamespace());
            filterNode.addContainmentNode(newContainer);
            filterNode = newContainer;
        } else {
            filterNode.addMatchNode(rdn.getRdnName(), rdn.getNamespace(), rdn.getRdnValue());
        }
        return filterNode;
    }

    public static void buildMergedFilter(FilterNode rootFilter, List<Element> xmlFilterElements, SchemaRegistry schemaRegistry){
        processFilter(rootFilter, xmlFilterElements,schemaRegistry);
        mergeChildNodes(rootFilter, null);
    }

    private static void mergeChildNodes(FilterNode node, FilterNode parentNode) {
        if (parentNode != null) {
            //check if it can be merged with its siblings, if yes, merge them
            checkDuplicateSiblingAndMerge(node, parentNode);
        }

        //iterate through the children and merge them
        for(int i=0; i< node.getChildNodes().size();i++){
            mergeChildNodes(node.getChildNodes().get(i), node);
        }
        reduceFilter(node);
    }

    private static void checkDuplicateSiblingAndMerge(FilterNode node, FilterNode parentNode) {
        List<FilterNode> siblings = new ArrayList<>(parentNode.getChildNodes());
        siblings.remove(node);
        for(FilterNode sibling : siblings){
            if(node.canBeMerged(sibling)){
                mergeRightToLeft(parentNode, node, sibling);
            }
        }
    }

    private static void mergeRightToLeft(FilterNode parentNode, FilterNode leftNode, FilterNode rightNode) {
        leftNode.merge(rightNode);
        parentNode.getChildNodes().remove(rightNode);
    }

    public static boolean isMatchConditionsPass(FilterNode filterNode, Element inputNode) {
        return isMatchConditionsPass(filterNode.getMatchNodes(), inputNode);
    }

    public static boolean isMatchConditionsPass(List<FilterMatchNode> filterMatchNodes, Element inputNode) {
        boolean match = true;
        List<Element> childElements = DocumentUtils.getChildElements(inputNode);
        for(FilterMatchNode matchNode : filterMatchNodes){
            Element matchingNode = getMatchingNode(matchNode, childElements);
            if(matchingNode == null){
                match = false;
                break;
            }
        }
        return match;
    }

    public static Element getMatchingNode(FilterMatchNode matchNode, List<Element> childElements) {
        for(Element childElement : childElements){
            if(matchNode.getNamespace().equals(childElement.getNamespaceURI())
                    && matchNode.getNodeName().equals(childElement.getLocalName())
                    && matchNode.getFilter().equals(childElement.getTextContent())){
                return childElement;
            }
        }
        return null;
    }

}
