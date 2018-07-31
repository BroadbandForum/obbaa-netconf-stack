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

package org.broadband_forum.obbaa.netconf.api.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestXML {

    private static final Logger LOGGER = Logger.getLogger(TestXML.class);

    public static boolean assertXMLEquals(Element expectedElement, Element actualElement) throws SAXException,
            IOException {
        return assertXMLEquals(expectedElement, actualElement, Collections.EMPTY_LIST);
    }

    public static boolean assertXMLEquals(Element expectedElement, Element actualElement, List<String> ignoreElements)
            throws SAXException, IOException {
        boolean result = compareXMLEquals(expectedElement, actualElement, ignoreElements);
        assertTrue(result);
        return result;
    }

    public static Element loadAsXml(String name) {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(name);
            File file = new File(url.getPath());
            Document doc = DocumentUtils.getDocFromFile(file);
            Element xml = null;
            NodeList childNodes = doc.getChildNodes();
            for(int i=0; i< childNodes.getLength(); i++) {
                if(childNodes.item(i) instanceof Element){
                    xml = (Element) childNodes.item(i);
                    break;
                }
            }
            return xml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean compareXMLEquals(Element expectedElement, Element actualElement, List<String> ignoreElements)
            throws IOException, SAXException {
        try {
            // sort expected and actual elements
            sortElement(expectedElement);
            sortElement(actualElement);

            String expectedXml = DocumentUtils.documentToPrettyString(expectedElement);
            String actualXml = DocumentUtils.documentToPrettyString(actualElement);
            LOGGER.info("expected: \n" + expectedXml);
            LOGGER.info("actual: \n" + actualXml);
            Diff diff = createXMLDiff(expectedXml, actualXml, ignoreElements);
            boolean similar = diff.similar();
            if (!similar) {
                LOGGER.error(diff.toString());
            }
            return similar;
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    private static Diff createXMLDiff(String expectedXml, String actualXml, final List<String> ignoreElements)
            throws SAXException, IOException {
        Diff diff = new Diff(expectedXml, actualXml);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        diff.overrideDifferenceListener(new DifferenceListener() {

            @Override
            public void skippedComparison(Node arg0, Node arg1) {

            }

            @Override
            public int differenceFound(Difference difference) {
                if (DifferenceConstants.CHILD_NODELIST_SEQUENCE.equals(difference)) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                if (DifferenceConstants.NAMESPACE_PREFIX.equals(difference)) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                if (difference.getTestNodeDetail().getNode().getNodeType() == Node.TEXT_NODE
                        && difference.getTestNodeDetail().getValue().startsWith("$")
                        && difference.getTestNodeDetail().getNode().getParentNode().getNodeName().equalsIgnoreCase
                        ("pma:password")) {
                    LOGGER.info("Skipping Password Comparison due to Dynamic Salt generation");
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (difference.getTestNodeDetail().getNode().getParentNode().getNodeName()
                        .equalsIgnoreCase("pma:reachable-last-change")) {
                    LOGGER.info("Skipping reachable-last-change Comparison due to current time changing");
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (ignoreElements.contains(difference.getTestNodeDetail().getNode().getParentNode()
                        .getNodeName())) {
                    LOGGER.info("Skipping comparison for " + difference.getTestNodeDetail().getNode().getParentNode()
                            .getNodeName()
                            + " as requested");
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                return RETURN_ACCEPT_DIFFERENCE;
            }
        });
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        return diff;
    }

    public static void sortElement(Element parentElement) {
        NodeList nodeList = parentElement.getChildNodes();
        List<Element> childElementList = new ArrayList<Element>();
        List<Node> childEmptyTextList = new ArrayList<Node>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                childElementList.add((Element) node);
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String textvalue = node.getTextContent().trim();
                if (textvalue.isEmpty()) {
                    childEmptyTextList.add(node);
                }
            }
        }
        // remove child element now, later will append in sorted order
        for (Element element : childElementList) {
            parentElement.removeChild(element);
        }
        // remote empty text nodes
        for (Node textNode : childEmptyTextList) {
            parentElement.removeChild(textNode);
        }
        // sort immediate children
        if (childElementList.size() > 1) {
            Collections.sort(childElementList, new Comparator<Element>() {
                @Override
                public int compare(Element element1, Element element2) {
                    String name1 = element1.getNodeName();
                    String name2 = element2.getNodeName();
                    int diff = name1.compareTo(name2);
                    if (diff != 0) {
                        return diff;
                    }
                    String namespace1 = element1.getNamespaceURI();
                    String namespace2 = element2.getNamespaceURI();
                    diff = namespace1.compareTo(namespace2);
                    if (diff != 0) {
                        return diff;
                    }
                    return 0;
                }
            });
        }

        // append sorted children, and sort descendant node recursively
        for (Element element : childElementList) {
            parentElement.appendChild(element);
            sortElement(element);
        }
    }

}
