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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil.isMatchConditionsPass;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil.getMatchingNode;


public class SubtreeFilterUtil {
    private final SchemaRegistry m_schemaRegistry;

    public SubtreeFilterUtil(SchemaRegistry schemaRegistry) {
        m_schemaRegistry = schemaRegistry;
    }

    public Element filter(Element unfilteredXml, Element filter) {
        Document doc = DocumentUtils.createDocument();
        Element rootElement = doc.createElementNS(unfilteredXml.getNamespaceURI(), unfilteredXml.getLocalName());
        FilterNode filterNode = new FilterNode();
        FilterUtil.processFilter(filterNode, DocumentUtils.getChildElements(filter), m_schemaRegistry);
        RootSchemaNode rootSchemaNode = new RootSchemaNode();
        //non-emtpy filter
        if(!(filterNode.getSelectNodes().isEmpty() && filterNode.getChildNodes().isEmpty() && filterNode.getMatchNodes().isEmpty())){
            if(doFilter(doc, filterNode, rootSchemaNode, unfilteredXml, rootElement)) {
                return rootElement;
            }
        }
        return doc.createElementNS(unfilteredXml.getNamespaceURI(), unfilteredXml.getLocalName());
    }

    public boolean doFilter(Document doc, FilterNode filterNode, DataSchemaNode schemaNode, Element inputNode, Element outputNode) {
        if(isMatchConditionsPass(filterNode, inputNode)){
            if(filterNode.getSelectNodes().isEmpty() && filterNode.getChildNodes().isEmpty()){
                copyEverything(doc, inputNode, outputNode);
            }else{
                copyMandatoryAttributes(doc, filterNode, schemaNode, inputNode, outputNode);
                boolean atLeastOneMatch = processChildFilters(doc, filterNode, schemaNode, inputNode, outputNode);                
                copyNonKeySelectNodes(doc, filterNode, schemaNode, inputNode, outputNode);                
                return atLeastOneMatch || (!atLeastOneMatch && !filterNode.getSelectNodes().isEmpty());
            }
            return true;
        }
        return false;
    }

    private void copyEverything(Document doc, Element inputNode, Element outputNode) {
        for(Element inputChild:DocumentUtils.getChildElements(inputNode)){
            outputNode.appendChild(doc.importNode(inputChild,true));
        }
    }


    private void copyMandatoryAttributes(Document doc, FilterNode filterNode, DataSchemaNode schemaNode, Element inputNode, Element outputNode) {
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
        for(FilterMatchNode matchNode : matchNodesToBeCopied){
            Element matchingInputChild = getMatchingNode(matchNode, inputChildren);
            outputNode.appendChild(doc.importNode(matchingInputChild, true));
        }

    }

    private List<FilterMatchNode> getKeyMatchNodes(FilterNode filterNode, DataSchemaNode schemaNode) {
        List<FilterMatchNode> keyMatchNodes = new ArrayList<>();
        List<QName> keyQnames = getKeyQNames(schemaNode);
        for (QName keyQName : keyQnames) {
            for(FilterMatchNode matchNode : filterNode.getMatchNodes()){
                if(matchNode.isSameQName(keyQName)){
                    keyMatchNodes.add(matchNode);
                }
            }
        }
        return keyMatchNodes;
    }

    private boolean processChildFilters(Document doc, FilterNode filterNode, DataSchemaNode schemaNode, Element inputNode, Element outputNode) {
        if(filterNode.getChildNodes().isEmpty()){
            //nothing to filter, so return true
            return true;
        }

        boolean atLeastOneMatch = false;
        List<Element> cNsToBeAppended = new ArrayList<>();
        for (FilterNode childFilter : filterNode.getChildNodes()) {
            List<Element> matchingInputCNs = getMatchingChildren(childFilter, inputNode);
            for (Element matchingInputCN : matchingInputCNs) {
                Element matchingOutputCN = doc.createElementNS(matchingInputCN.getNamespaceURI(), matchingInputCN.getLocalName());
                if (matchingInputCN.getPrefix() != null && !matchingInputCN.getPrefix().isEmpty()) {
                    matchingOutputCN.setPrefix(matchingInputCN.getPrefix());
                }
                DataSchemaNode childSchemaNode = SchemaRegistryUtil.getChildSchemaNode(matchingOutputCN, schemaNode.getPath(), m_schemaRegistry);
                boolean match = doFilter(doc, childFilter, childSchemaNode, matchingInputCN, matchingOutputCN);
                if(match) {
                    cNsToBeAppended.add(matchingOutputCN);
                    atLeastOneMatch = true;
                }
            }
        }
        if (!cNsToBeAppended.isEmpty()) {
            for (Element cNToBeAppended : cNsToBeAppended) {
                outputNode.appendChild(cNToBeAppended);
            }
        }
        return atLeastOneMatch;
    }

    private List<Element> getMatchingChildren(FilterNode childFilter, Element parentNode) {
        return getMatchingChildren(childFilter.getNamespace(), childFilter.getNodeName(), parentNode);
    }

    private List<Element> getMatchingChildren(String childNamespace, String childLocalName, Element parentNode) {
        List<Element> matchingChildren = new ArrayList<>();
        for (Element childElement : DocumentUtils.getChildElements(parentNode)) {
            if (childNamespace.equals(childElement.getNamespaceURI()) && childLocalName.equals(childElement.getLocalName())) {
                matchingChildren.add(childElement);
            }
        }
        return matchingChildren;
    }

    private void copyNonKeySelectNodes(Document doc, FilterNode filterNode, DataSchemaNode schemaNode, Element input, Element output) {
        List<QName> keyQNames = getKeyQNames(schemaNode);
        for (FilterNode selectNode : filterNode.getSelectNodes()) {
            for (Element childElement : DocumentUtils.getChildElements(input)) {
                if(!keySelectNode(keyQNames, selectNode)){
                    if (selectNode.getNamespace().equals(childElement.getNamespaceURI()) && selectNode.getNodeName().equals(childElement.getLocalName())) {
                        output.appendChild(doc.importNode(childElement, true));
                    }
                }

            }
        }
    }

    private boolean keySelectNode(List<QName> keyQNames, FilterNode selectNode) {
        for(QName keyQName : keyQNames){
            if(keyQName.getNamespace().toString().equals(selectNode.getNamespace()) && keyQName.getLocalName().equals(selectNode.getNodeName())){
                return true;
            }
        }
        return false;
    }

    private List<QName> getKeyQNames(DataSchemaNode schemaNode) {
        List<QName> keyQNames = new ArrayList<>();
        if(schemaNode instanceof ListSchemaNode){
            keyQNames.addAll(((ListSchemaNode)schemaNode).getKeyDefinition());
        }
        return keyQNames;
    }

    //A better way than null checks all over, this node represents a root schema node
    private class RootSchemaNode implements DataSchemaNode {
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
