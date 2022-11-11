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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class StateAttributeUtil {

	private static final int ROOT_DEPTH = 1;

	@SuppressWarnings("unchecked")
    public static List<Element> convertToStateElements(Map<QName, Object> stateAttributes, String prefix, Document document) {
		List<Element> stateElements = new ArrayList<>();
		for (Entry<QName, Object> stateAttribute : stateAttributes.entrySet()) {
			Object values = stateAttribute.getValue();
			if (values != null) {
				if (values instanceof Collection) {
					for (Object value : (Collection<Object>) values) {
						createElement(document, stateAttribute.getKey(), value.toString(), prefix, stateElements);
					}
				} else {
					createElement(document, stateAttribute.getKey(), values.toString(), prefix, stateElements);
				}
			}
		}
		return stateElements;
	}
	
	// Create state element and add to list
	private static void createElement(Document document, QName qname, String value, String prefix, List<Element> elements) {
		if (value == null || value.trim().isEmpty()) {
			return;
		}
		String namespace = null;
		if (qname.getNamespace() != null && !qname.getNamespace().toString().isEmpty()) {
			namespace = qname.getNamespace().toString();
		}
		String localName = getPrefixedLocalName(prefix, qname.getLocalName());
		Element element = document.createElementNS(namespace, localName);
		element.setTextContent(value);

		elements.add(element);
	}
	
	private static String getPrefixedLocalName(String prefix, String localname) {
        return prefix+ ":" +localname;
    }
	
	public static List<QName> getQNamesFromStateMatchNodes(List<FilterMatchNode> stateFilterMatchNodes, SchemaRegistry schemaRegistry) {
		List<QName> stateQNames = new ArrayList<>();
		for (FilterMatchNode matchNode : stateFilterMatchNodes) {
			QName fmnQname = schemaRegistry.lookupQName(matchNode.getNamespace(), matchNode.getNodeName());
			stateQNames.add(fmnQname);
		}
		return stateQNames;
	}
	
	public static List<Element> getFilteredElements(List<FilterMatchNode> stateFilterMatchNodes, List<Element> stateElements) {
		List<Element> filteredElements = new ArrayList<>();
		for (FilterMatchNode matchNode : stateFilterMatchNodes) {
			for (Element element : stateElements) {
				if (matchNode.getNodeName().equals(element.getLocalName())) {
					if (matchNode.getFilter().equals(element.getTextContent())) {
						filteredElements.add(element);
					}
				}
			}
		}
		return filteredElements;
	}
	
	public static void trimResultBelowDepth(Map<ModelNodeId, List<Element>> stateInfo, NetconfQueryParams queryParams){
	    if ( queryParams != NetconfQueryParams.NO_PARAMS){
            for ( Entry<ModelNodeId, List<Element>> entry :stateInfo.entrySet()){
                int currentDepth = entry.getKey().getDepth();
                for ( Element element : entry.getValue()){
                    int allowedDepth =  ((queryParams.getDepth() -1) - currentDepth);
                    if (allowedDepth == 0 && currentDepth == ROOT_DEPTH) {
                    	entry.setValue(Collections.emptyList());
                    } 
                    if ( allowedDepth >0){
                        trimBelowTheDepth(element, allowedDepth-1, 0);                        
                    }
                }
            }
	    }
	}
	
	private static void trimBelowTheDepth(Element element, int depth, int tempIndex){
	    NodeList childNodes = element.getChildNodes();
        for ( int index=0; index<childNodes.getLength(); index++){
            Node childNode = childNodes.item(index);
            if ( childNode.getNodeType() == Node.ELEMENT_NODE){
                if (depth <= tempIndex){
                    element.removeChild(childNode);
                    index--;                        
                } else {
                    trimBelowTheDepth((Element) childNode, depth, tempIndex+1);
                }
            }
        }
	}

    public static Element applyFilter(final Element stateElement, FilterNode filterNode, Document document) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String s) {
                return s.equals(stateElement.getPrefix()) ? stateElement.getNamespaceURI() : null;
            }

            @Override
            public String getPrefix(String s) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String s) {
                return null;
            }
        });
        List<String> expression = createXpathExpression(filterNode, stateElement);

        if (expression.isEmpty()) {
            return stateElement;
        }
        Element element = document.createElementNS(stateElement.getNamespaceURI(), stateElement.getPrefix() + ":" + stateElement.getLocalName());
        for (String exp : expression) {
            if (!filterNode.getChildNodes().isEmpty()) {
                exp = exp + "/..";
            }
            NodeList nodeList = (NodeList) xPath.compile(exp).evaluate(stateElement, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                element.appendChild(document.importNode(nodeList.item(i), true));
            }
        }
        return element;
    }

    private static List<String> createXpathExpression(FilterNode filterNode, Element stateElement) {
        List<String> xpath = new ArrayList<>();
        for (int i = 0; i < filterNode.getMatchNodes().size(); i++) {
            xpath.add("//" + stateElement.getPrefix() + ":" + filterNode.getMatchNodes().get(i).getNodeName() + "[text()='" +
                    filterNode.getMatchNodes().get(i).getFilter() + "']");
        }
        for (int i = 0; i < filterNode.getSelectNodes().size(); i++) {
            xpath.add("//" + stateElement.getPrefix() + ":" + filterNode.getSelectNodes().get(i).getNodeName());
        }
        for (int i = 0; i < filterNode.getChildNodes().size(); i++) {
            xpath.addAll(createXpathExpression(filterNode.getChildNodes().get(i), stateElement));
        }
        return xpath;
    }
}
