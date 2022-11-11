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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import static org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams.UNBOUNDED;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil.getMatchingNode;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil.isMatchConditionsPass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PrunedSubtreeFilterUtil {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(PrunedSubtreeFilterUtil.class, LogAppNames.NETCONF_STACK);

    public static Element filter(SchemaRegistry schemaRegistry, Element unfilteredXml, Element filter, int depth) {
        if(LOGGER.isDebugEnabled()){
            try {
                LOGGER.debug("Subtree filtered data: \n {}", LOGGER.sensitiveData(DocumentUtils.documentToPrettyString(unfilteredXml)));
                LOGGER.debug("Pruned-subtree filter: \n {}", DocumentUtils.documentToPrettyString(filter));
            } catch (NetconfMessageBuilderException e) {
                throw new RuntimeException(e);
            }
        }
        StopWatch prunedSubtreeSW = new StopWatch();
        prunedSubtreeSW.start();
        Document doc = DocumentUtils.createDocument();
        Element rootElement = doc.createElementNS(unfilteredXml.getNamespaceURI(), unfilteredXml.getLocalName());
        FilterNode filterNode = new FilterNode();
        FilterUtil.processFilter(filterNode, DocumentUtils.getChildElements(filter), schemaRegistry);
        RootSchemaNode rootSchemaNode = new RootSchemaNode();
        if (!(filterNode.getSelectNodes().isEmpty() && filterNode.getChildNodes().isEmpty() && filterNode.getMatchNodes().isEmpty())) {
            if (doFilter(doc, filterNode, rootSchemaNode, unfilteredXml, rootElement, schemaRegistry)) {
                if (UNBOUNDED != depth) {
                    StopWatch depthFilteringSW = new StopWatch();
                    depthFilteringSW.start();
                    removeAllDataBelowDepth(rootElement, depth - 1);
                    depthFilteringSW.stop();
                    LOGGER.debug("Total time taken to perform depth filtering on pruned data: " + depthFilteringSW.getNanoTime() + " ns");
                }
                prunedSubtreeSW.stop();
                LOGGER.debug("Total time taken to perform pruned-subtree filtering: " + prunedSubtreeSW.getTime() + " ms");
                return rootElement;
            }
        }
        prunedSubtreeSW.stop();
        LOGGER.debug("Total time taken to perform pruned-subtree filtering: " + prunedSubtreeSW.getTime() + " ms");
        return doc.createElementNS(unfilteredXml.getNamespaceURI(), unfilteredXml.getLocalName());
    }

    public static void removeAllDataBelowDepth(Node node, int depth) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                if (depth != 0) {
                    removeAllDataBelowDepth(currentNode, depth - 1);
                } else {
                    node.removeChild(currentNode);
                    i--;
                }
            }
        }
    }

    private static boolean doFilter(Document doc, FilterNode filterNode, DataSchemaNode schemaNode, Element inputNode,
                                    Element outputNode, SchemaRegistry schemaRegistry) {
        if (isMatchConditionsPass(filterNode, inputNode)) {
            if (filterNode.getSelectNodes().isEmpty() && filterNode.getChildNodes().isEmpty()) {
                copyEverything(doc, inputNode, outputNode);
            } else {
                copyMandatoryAttributes(doc, filterNode, schemaNode, inputNode, outputNode);
                boolean matchCriteria = processChildFilters(doc, filterNode, schemaNode, inputNode, outputNode, schemaRegistry);
                if (matchCriteria) {
                    copyNonKeySelectNodes(doc, filterNode, schemaNode, inputNode, outputNode);
                }
                return matchCriteria;
            }
            return true;
        }
        return false;
    }

    private static void copyEverything(Document doc, Element inputNode, Element outputNode) {
        for (Element inputChild : DocumentUtils.getChildElements(inputNode)) {
            outputNode.appendChild(doc.importNode(inputChild, true));
        }
    }

    private static void copyMandatoryAttributes(Document doc, FilterNode filterNode, DataSchemaNode schemaNode, Element inputNode,
                                                Element outputNode) {
        List<FilterMatchNode> keyMatchNodes = getKeyMatchNodes(filterNode, schemaNode);
        List<QName> keyQnames = getKeyQNames(schemaNode);
        for (QName keyQName : keyQnames) {
            List<Element> inputChildren = DocumentUtils.getChildElements(inputNode);
            for (Element inputChild : inputChildren) {
                if (keyQName.getNamespace().toString().equals(inputChild.getNamespaceURI()) && keyQName.getLocalName().equals(inputChild.getLocalName())) {
                    outputNode.appendChild(doc.importNode(inputChild, true));
                    break;
                }
            }
        }
        List<FilterMatchNode> matchNodesToBeCopied = new ArrayList<>(filterNode.getMatchNodes());
        matchNodesToBeCopied.removeAll(keyMatchNodes);
        List<Element> inputChildren = DocumentUtils.getChildElements(inputNode);
        for (FilterMatchNode matchNode : matchNodesToBeCopied) {
            Element matchingInputChild = getMatchingNode(matchNode, inputChildren);
            outputNode.appendChild(doc.importNode(matchingInputChild, true));
        }
    }

    private static List<FilterMatchNode> getKeyMatchNodes(FilterNode filterNode, DataSchemaNode schemaNode) {
        List<FilterMatchNode> keyMatchNodes = new ArrayList<>();
        List<QName> keyQnames = getKeyQNames(schemaNode);
        for (QName keyQName : keyQnames) {
            for (FilterMatchNode matchNode : filterNode.getMatchNodes()) {
                if (matchNode.isSameQName(keyQName)) {
                    keyMatchNodes.add(matchNode);
                }
            }
        }
        return keyMatchNodes;
    }

    private static boolean processChildFilters(Document doc, FilterNode filterNode, DataSchemaNode schemaNode, Element inputNode,
                                               Element outputNode, SchemaRegistry schemaRegistry) {
        if (filterNode.getChildNodes().isEmpty()) {
            return true;
        }
        boolean matchCriteria = true;
        List<Element> cNsToBeAppended = new ArrayList<>();
        for (FilterNode childFilter : filterNode.getChildNodes()) {
            List<Element> matchingInputCNs = getMatchingChildren(childFilter, inputNode);
            for (Element matchingInputCN : matchingInputCNs) {
                Element matchingOutputCN = doc.createElementNS(matchingInputCN.getNamespaceURI(), matchingInputCN.getLocalName());
                if (matchingInputCN.getPrefix() != null && !matchingInputCN.getPrefix().isEmpty()) {
                    matchingOutputCN.setPrefix(matchingInputCN.getPrefix());
                }
                DataSchemaNode childSchemaNode = getChildSchemaNode(matchingInputCN, schemaNode.getPath(), schemaRegistry);
                if (childSchemaNode == null) {
                    //retrieving mounted schema registry
                    schemaRegistry = schemaRegistry.getMountRegistry().getProvider(schemaNode.getPath()).getSchemaRegistry(inputNode);
                    if (schemaRegistry != null) {
                        childSchemaNode = getChildSchemaNode(matchingInputCN, schemaNode.getPath(), schemaRegistry);
                    }
                }
                boolean match = doFilter(doc, childFilter, childSchemaNode, matchingInputCN, matchingOutputCN, schemaRegistry);
                if (match) {
                    cNsToBeAppended.add(matchingOutputCN);
                }
            }
        }
        for (FilterNode childFilter : filterNode.getChildNodes()) {
            if (childFilter.hasMatchCondition()) {
                boolean childFound = false;
                for (Element childElement : cNsToBeAppended) {
                    if (childElement.getLocalName().equals(childFilter.getNodeName())
                            && childElement.getNamespaceURI().equals(childFilter.getNamespace())) {
                        childFound = true;
                        break;
                    }
                }
                if (!childFound) {
                    matchCriteria = false;
                    break;
                }
            }
        }
        if (matchCriteria) {
            for (Element cNToBeAppended : cNsToBeAppended) {
                outputNode.appendChild(cNToBeAppended);
            }
        }
        return matchCriteria;
    }

    private static DataSchemaNode getChildSchemaNode(Element child, SchemaPath parentSchemaPath, SchemaRegistry schemaRegistry) {
        String childLocalName = child.getLocalName();
        String childNs = child.getNamespaceURI();
        if (SchemaPath.ROOT.equals(parentSchemaPath)) {
            Collection<DataSchemaNode> children = schemaRegistry.getRootDataSchemaNodes();
            for (DataSchemaNode childSchemaNode : children) {
                QName childQname = childSchemaNode.getQName();
                if (childLocalName.equals(childQname.getLocalName()) && childNs.equals(childQname.getNamespace().toString())) {
                    return childSchemaNode;
                }
            }
        }
        QName childQName = schemaRegistry.lookupQName(childNs, childLocalName);
        if (childQName != null) {
            SchemaPath childSchemaPath = schemaRegistry.getDescendantSchemaPath(parentSchemaPath, childQName);
            return schemaRegistry.getDataSchemaNode(childSchemaPath);
        }
        return null;
    }

    private static List<Element> getMatchingChildren(FilterNode childFilter, Element parentNode) {
        return getMatchingChildren(childFilter.getNamespace(), childFilter.getNodeName(), parentNode);
    }

    private static List<Element> getMatchingChildren(String childNamespace, String childLocalName, Element parentNode) {
        List<Element> matchingChildren = new ArrayList<>();
        for (Element childElement : DocumentUtils.getChildElements(parentNode)) {
            if (childNamespace.equals(childElement.getNamespaceURI()) && childLocalName.equals(childElement.getLocalName())) {
                matchingChildren.add(childElement);
            }
        }
        return matchingChildren;
    }

    private static void copyNonKeySelectNodes(Document doc, FilterNode filterNode, DataSchemaNode schemaNode, Element input, Element output) {
        List<QName> keyQNames = getKeyQNames(schemaNode);
        for (FilterNode selectNode : filterNode.getSelectNodes()) {
            for (Element childElement : DocumentUtils.getChildElements(input)) {
                if (!keySelectNode(keyQNames, selectNode)) {
                    if (selectNode.getNamespace().equals(childElement.getNamespaceURI()) && selectNode.getNodeName().equals(childElement.getLocalName())) {
                        output.appendChild(doc.importNode(childElement, true));
                    }
                }
            }
        }
    }

    private static boolean keySelectNode(List<QName> keyQNames, FilterNode selectNode) {
        for (QName keyQName : keyQNames) {
            if (keyQName.getNamespace().toString().equals(selectNode.getNamespace()) && keyQName.getLocalName().equals(selectNode.getNodeName())) {
                return true;
            }
        }
        return false;
    }

    private static List<QName> getKeyQNames(DataSchemaNode schemaNode) {
        List<QName> keyQNames = new ArrayList<>();
        if (schemaNode instanceof ListSchemaNode) {
            keyQNames.addAll(((ListSchemaNode) schemaNode).getKeyDefinition());
        }
        return keyQNames;
    }

    /**
     * Calculates the depth till deepest filter match node in any subtree of passed filter node
     * @param filterNode
     * @return pseudo depth
     */
    public static int getPseudoDepth(FilterNode filterNode) {
        return getPseudoDepth(filterNode, 1);
    }

    private static int getPseudoDepth(FilterNode filterNode, int depth) {
        int lastValidDepth = -1;
        depth++;
        if(filterNode.getMatchNodes().size() > 0) {
            lastValidDepth = depth;
        }
        for(FilterNode childFilterNode : filterNode.getChildNodes()) {
            int temp = getPseudoDepth(childFilterNode, depth);
            if(temp > lastValidDepth) {
                lastValidDepth = temp;
            }
        }
        return lastValidDepth;
    }

    //A better way than null checks all over, this node represents a root schema node
    private static class RootSchemaNode implements DataSchemaNode {
        @Override
        public boolean isAugmenting() {
            return false;
        }

        @Override
        public boolean isAddedByUses() {
            return false;
        }

        @Override
        public boolean isConfiguration() {
            return false;
        }

        @Override
        public QName getQName() {
            return SchemaPath.ROOT.getLastComponent();
        }

        @Override
        public SchemaPath getPath() {
            return SchemaPath.ROOT;
        }

        @Override
        public Status getStatus() {
            return null;
        }

        @Override
        public Optional<String> getDescription() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Optional<String> getReference() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Optional<RevisionAwareXPath> getWhenCondition() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
