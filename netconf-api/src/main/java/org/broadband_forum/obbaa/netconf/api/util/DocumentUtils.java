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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Utility class to work with {@link Document}
 *
 * 
 *
 */
public class DocumentUtils {

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    public static final String NAME_NODE = "name";
    public static final String COLON = ":";
    public static final String SEPARATED = "/";
    public static final String XMLNS = "xmlns";
    private static DocumentUtils c_instance = new DocumentUtils();
    public static boolean c_getNewDocBuildFactoryEveryTime;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DocumentUtils.class, LogAppNames.NETCONF_LIB);

    private static final String PARSE_ERROR = "Error while converting string to xml document";

    private DocumentUtils() {

    }

    static {
        c_getNewDocBuildFactoryEveryTime = Boolean.valueOf(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("GET_NEW_DOC_BUILD_FACTORY", "true"));
    }

    public static DocumentUtils getInstance() {
        return c_instance;
    }

    public static Document createDocument() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactoryWithoutDTD.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            return doc;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    public static Element stringToDocumentElement(String xmlStr) throws NetconfMessageBuilderException {
        return stringToDocument(xmlStr).getDocumentElement();
    }

    public static Element getFirstChildElement(Element parent) {
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                       return (Element) child;
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasDirectChildElement(Element parent) {
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getUserContextFromRpcDocument(Node rpcNode) {
    	try {
            return getAttributeValueOrNullNS(rpcNode,NetconfResources.EXTENSION_NS, NetconfResources.USER_CONTEXT);
    	} catch (Exception e) {
            LOGGER.error("Error while parsing user context from Rpc Document", e);
            return null;
    	}
    }

    public String getSessionIdFromRpcDocument(Node rpcNode) {
    	try {
            return getAttributeValueOrNullNS(rpcNode,NetconfResources.EXTENSION_NS, NetconfResources.SESSION_ID_CONTEXT);
    	} catch (Exception e) {
            LOGGER.error("Error while parsing session Id from Rpc Document", e);
            return null;
    	}
    }
    
	public String getApplicationFromRpcDocument(Node rpcNode) {
		try {
			return getAttributeValueOrNullNS(rpcNode, NetconfResources.EXTENSION_NS, NetconfResources.APPLICATION);
		} catch (Exception e) {
			LOGGER.error("Error while parsing application from Rpc Document", e);
			return null;
		}
	}

    public String getMessageIdFromRpcDocument(Document request) {
        try {
            return getChildNodeByName(request, NetconfResources.RPC, NetconfResources.NETCONF_RPC_NS_1_0)
                    .getAttributes().getNamedItem(NetconfResources.MESSAGE_ID).getNodeValue();
        } catch (Exception e) {
//            Changed logger to debug.
//            If messageId is not set here, it will be set in InternalNetconfClientImpl::assignIdAndSend.
//            There is no functional impact.
            LOGGER.debug("Error while parsing messageId from Rpc Document");
            return null;
        }
    }

    public String getMessageIdFromRpcReplyDocument(Document responseDoc) {
        try {
            return responseDoc.getElementsByTagName(NetconfResources.RPC_REPLY).item(0).getAttributes()
                    .getNamedItem(NetconfResources.MESSAGE_ID).getNodeValue();
        } catch (Exception e) {
            LOGGER.error("Error while parsing messageId from Rpc Reply Document");
            return null;
        }
    }

    public Map<String, String> getRpcOtherAttributes(Document request) {
        Map<String, String> additionalAttrs = new HashMap<>();
        NamedNodeMap rpcAttrs = request.getElementsByTagName(NetconfResources.RPC).item(0).getAttributes();

        for (int i = 0; i < rpcAttrs.getLength(); i++) {
            Node node = rpcAttrs.item(i);
            if (node.getNodeName() != NetconfResources.MESSAGE_ID) {
                additionalAttrs.put(node.getNodeName(), node.getNodeValue());
            }
        }
        rpcAttrs.getNamedItem(NetconfResources.MESSAGE_ID).getNodeValue();
        return additionalAttrs;
    }

    public Integer getSessionIdFromHelloMessage(Document helloDoc) {
        NodeList sessionIdList = helloDoc.getElementsByTagName(NetconfResources.SESSION_ID);
        if (sessionIdList != null && sessionIdList.getLength() > 0) {
            return Integer.valueOf(sessionIdList.item(0).getTextContent());
        }
        return null;
    }

    public Set<String> getCapsFromHelloMessage(Document responseDoc) {
        Set<String> capsSet = new HashSet<>();
        Node caps = getChildNodeByName(responseDoc.getDocumentElement(), NetconfResources.CAPABILITIES, NetconfResources.NETCONF_RPC_NS_1_0);
        for (int i = 0; i < caps.getChildNodes().getLength(); i++) {
            Node cap = caps.getChildNodes().item(i);
            if (cap.getNodeType() == Node.ELEMENT_NODE) {
                String capability = cap.getTextContent().trim();
                //reuse capability string object using String.intern
                capsSet.add(capability.intern());
            }
        }
        return capsSet;
    }

    public List<Element> getDataElementsFromRpcReply(Document responseDoc){
        List<Element> returnValue = new ArrayList<>();
        Element rpcReplyNode = responseDoc.getDocumentElement();
        if (rpcReplyNode != null && NetconfResources.RPC_REPLY.equals(rpcReplyNode.getLocalName())) {
        	NodeList nodes = rpcReplyNode.getChildNodes();
        	for (int i=0;i<nodes.getLength();i++){
        		if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE){
        			Element dataNode = (Element) nodes.item(i);
    	    		if (NetconfResources.RPC_ERROR.equals(dataNode.getLocalName()) || NetconfResources.OK.equals(dataNode.getLocalName()) || isTransactionIdResponseElement(dataNode)) {
    	    			return null;
    	    		} else {
    	    			returnValue.add(dataNode);
    	    		}

        		}
        	}
    	}

        return returnValue;

    }

    public static boolean isResponseWithRpcError(Document responseDoc){
        Element rpcReplyNode = responseDoc == null? null: responseDoc.getDocumentElement();
        if (rpcReplyNode != null && NetconfResources.RPC_REPLY.equals(rpcReplyNode.getLocalName())) {
            NodeList nodes = rpcReplyNode.getChildNodes();
            for (int i=0;i<nodes.getLength();i++){
                if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE){
                    Element dataNode = (Element) nodes.item(i);
                    if (NetconfResources.RPC_ERROR.equals(dataNode.getLocalName())) {
                        return true;
                    }

                }
            }
        }
        return false;
    }
    
    private boolean isTransactionIdResponseElement(Element dataNode) {
        return NetconfResources.TRANSACTION_ID.equals(dataNode.getLocalName()) && NetconfResources.TRANSACTION_ID_NS.equals(dataNode.getNamespaceURI());
    }

    public static Element getTxIdElementFromRpcReply(Document responseDoc) {
        Element rpcReplyNode = responseDoc.getDocumentElement();
        if (rpcReplyNode != null && NetconfResources.RPC_REPLY.equals(rpcReplyNode.getLocalName())) {
            return getDirectChildElement(rpcReplyNode, NetconfResources.TRANSACTION_ID, NetconfResources.TRANSACTION_ID_NS);
        }
        return null;
    }

    public String getEventTimeFromNotification(Document notificationDoc) {
        Element notificationElement = notificationDoc.getDocumentElement();
        Element eventTimeElement = getChildElement(notificationElement, NetconfResources.EVENT_TIME);
        if (eventTimeElement != null) {
            return eventTimeElement.getTextContent();
        }
        return null;
    }

    public String getDataStoreFromNotification(Document notificationDoc) {
        Element notificationElement = notificationDoc.getDocumentElement();
        Element dataStoreElement = getChildElement(notificationElement, NetconfResources.DATA_STORE);
        if (dataStoreElement != null) {
            return dataStoreElement.getTextContent();
        }
        return null;
    }

    public String getValueFromNotification(Element element) {
        Element valueElement = getChildElement(element, NetconfResources.STATE_CHANGE_VALUE);
        if (valueElement != null) {
            return valueElement.getTextContent();
        }
        return null;
    }

    public CreateSubscriptionRequest getSubscriptionRequest(NetconfRpcRequest request) throws DOMException, ParseException {

        Element rpcInput = request.getRpcInput();
        Node streamNode = getDescendant(rpcInput, CreateSubscriptionRequest.STREAM, NetconfResources.NETCONF_NOTIFICATION_NS);
        Node filterNode = getDescendant(rpcInput, CreateSubscriptionRequest.FILTER, NetconfResources.NETCONF_NOTIFICATION_NS);
        Node startTimeNode = getDescendant(rpcInput, CreateSubscriptionRequest.START_TIME, NetconfResources.NETCONF_NOTIFICATION_NS);
        Node stopTimeNode = getDescendant(rpcInput, CreateSubscriptionRequest.STOP_TIME, NetconfResources.NETCONF_NOTIFICATION_NS);

        // create subscription request
        CreateSubscriptionRequest subscriptionRequest = new CreateSubscriptionRequest();
        subscriptionRequest.setRpcInput(rpcInput);
        subscriptionRequest.setMessageId(request.getMessageId());
        if (streamNode != null) {
            String stream = streamNode.getTextContent().trim();
            if (!stream.isEmpty()) {
                subscriptionRequest.setStream(stream);
            }
        }

        if (filterNode != null) {
            NetconfFilter filter = new NetconfFilter();

            Node type = filterNode.getAttributes().getNamedItem(NetconfResources.TYPE);
            filter.setType(type.getTextContent());

            Element xmlFilter = DocumentUtils.getInstance().getFirstElementChildNode(filterNode);
            filter.addXmlFilter(xmlFilter);

            subscriptionRequest.setFilter(filter);
        }
        if (startTimeNode != null) {
            subscriptionRequest.setStartTime(startTimeNode.getTextContent());
        }

        if (stopTimeNode != null) {
            subscriptionRequest.setStopTime(stopTimeNode.getTextContent());
        }

        return subscriptionRequest;
    }

    public String getNodeValueOrNull(Node node) {
        if (node != null) {
            if (node.getNodeValue() == null) {
                return node.getTextContent();
            }
            return node.getNodeValue();
        }
        return null;
    }

    // this is a very bad method, because it will traverse the entire subtree if 
    // the child is not there!
    @Deprecated
    public static Node getChildNodeByName(Node parent, String expectedChildName, String namespace) {
        NodeList childNodes = parent.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String namespaceURI = child.getNamespaceURI();
                    if (child.getLocalName().equals(expectedChildName) && namespaceURI != null && namespaceURI.equals(namespace)) {
                        return child;
                    } else {
                        Node candidate = getChildNodeByName(child, expectedChildName, namespace);
                        if (candidate != null) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    // this is a very bad method, because it will traverse the entire subtree if 
    // the child is not there!
    @Deprecated
    public static Element getChildElement(Element parent, String childName) {
        Element element = null;
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (child.getNodeName().equals(childName)) {
                            return (Element) child;
                        } else {
                            element = getChildElement((Element) child, childName);
                            if (element != null) {
                                return element;
                            }
                        }
                    }
                }
            }
        }
        return element;
    }

    // this is a very bad method, because it will traverse the entire subtree if 
    // the child is not there!
    @Deprecated
    public static Element getDescendant(Element parent, String childName, String namespace) {
        Element element = null;
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (getLocalName(child).equals(childName) && child.getNamespaceURI().equals(namespace)) {
                            return (Element) child;
                        } else {
                            element = getDescendant((Element) child, childName, namespace);
                            if (element != null) {
                                return element;
                            }
                        }
                    }
                }
            }
        }
        return element;
    }

    public static Element getDirectChildElement(Element parent, String childName) {
        Element element = null;
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (child.getNodeName().equals(childName)) {
                            return (Element) child;
                        }
                    }
                }
            }
        }
        return element;
    }

    public static Element getDirectChildElement(Element parent, String childName, String namespace) {
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (child.getLocalName().equals(childName) && child.getNamespaceURI() != null
                        		&& child.getNamespaceURI().equals(namespace)) {
                            return (Element) child;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String getLocalName(Node node) {
        if (node.getLocalName() == null) {
            return node.getNodeName();
        }
        return node.getLocalName();
    }

    public static Element getChildElement(Element parent) {
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        return (Element) child;
                    }
                }
            }
        }
        return null;
    }

    // this is a very bad method, because it will traverse the entire subtree if 
    // the child is not there!
    @Deprecated
    public static List<Element> getChildElements(Node parent, String childName) {
        List<Element> elementList = new ArrayList<>();
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (child.getNodeName().equals(childName)) {
                            elementList.add((Element) child);
                        } else {
                            elementList.addAll(getChildElements(child, childName));
                        }
                    }
                }
            }
        }
        return elementList;
    }

    public static List<Element> getChildElements(Node parent, String childName, String namespace) {
        List<Element> elementList = new ArrayList<>();
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    String namespaceURI = child.getNamespaceURI();
                    if (getLocalName(child).equals(childName) && namespaceURI != null && namespaceURI.equals(namespace)) {
                        if (child instanceof Element) {
                            elementList.add((Element) child);
                        }
                    }
                }
            }
        }
        return elementList;
    }

    public static List<Element> getChildElements(Node parent) {
        List<Element> elementList = new ArrayList<>();
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

    public static List<Element> getDirectChildElements(Node parent, String childName) {
        List<Element> elementList = new ArrayList<>();
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (child.getLocalName().equals(childName)) {
                            elementList.add((Element) child);
                        }
                    }
                }
            }
        }
        return elementList;
    }

    public static List<Element> getDirectChildElements(Node parent, String childName, String namespace) {
        List<Element> elementList = new ArrayList<>();
        if (parent != null) {
            NodeList childNodes = parent.getChildNodes();
            if (childNodes != null) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child instanceof Element) {
                        if (child.getLocalName().equals(childName) && child.getNamespaceURI().equals(namespace)) {
                            elementList.add((Element) child);
                        }
                    }
                }
            }
        }
        return elementList;
    }

    public static boolean documentContainsElementWithName(Document responseDoc, String name) {
        if (responseDoc.getElementsByTagName(name) == null || responseDoc.getElementsByTagName(name).getLength() == 0) {
            return false;
        }
        return true;
    }

    public String getTypeOfNetconfRequest(Node rpc) {
        Element element = getFirstElementChildNode(rpc);
        if (element != null) {
            return element.getNodeName();
        } else {
            return null;
        }

    }

    public Node getRpcNode(Document request) {
        return request.getElementsByTagName(NetconfResources.RPC).item(0);
    }

    public String getTargetFromRpcDocument(Document request) {
        Node target = request.getElementsByTagName(NetconfResources.DATA_TARGET).item(0);
        return getFirstElementChildNode(target).getNodeName();
    }

    public String getSourceFromRpcDocument(Document request) throws NetconfMessageBuilderException {
        Node source = getChildNodeByName(request, NetconfResources.DATA_SOURCE, NetconfResources.NETCONF_RPC_NS_1_0);
        if(source == null){
            throw new NetconfMessageBuilderException("<source> cannot be null/empty");
        }
        return getFirstElementChildNode(source).getLocalName();
    }

    public Collection<Element> getInputsFromRpcDocument(Document request) {
        if (!request.hasChildNodes()) {
            return null;
        }
        return getChildElements(request);
    }

    public String getAttributeValueOrNull(Node node, String attrName) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node namedItem = attributes.getNamedItem(attrName);
            if (namedItem != null) {
                return namedItem.getNodeValue();
            }
        }

        return null;
    }

    public static Map<String, String> getAllNamespacesPrefixesOfCurrentNode(Node node) {
        Map<String,String> nsPrefixMap = new HashMap<>();
        NamedNodeMap atts = node.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node nodeItem = atts.item(i);
            String name = nodeItem.getNodeName();
            if ((name != null && (XMLNS.equals(name) || name.startsWith(PojoToDocumentTransformer.XMLNS)))) {
                nsPrefixMap.put(nodeItem.getNodeValue(), nodeItem.getLocalName());
            }
        }
        return nsPrefixMap;
    }

    public static Map<String, String> getAllPrefixesToNamespacesOfCurrentNode(Node node) {
        Map<String,String> nsPrefixMap = new HashMap<>();
        NamedNodeMap atts = node.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node nodeItem = atts.item(i);
            String name = nodeItem.getNodeName();
            if ((name != null && (XMLNS.equals(name) || name.startsWith(PojoToDocumentTransformer.XMLNS)))) {
                nsPrefixMap.put(nodeItem.getLocalName(), nodeItem.getNodeValue());
            }
        }
        return nsPrefixMap;
    }

    public String getAttributeValueOrNullNS(Node node, String attrNs, String attrName) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node namedItem = attributes.getNamedItemNS(attrNs, attrName);
            if (namedItem != null) {
                return namedItem.getNodeValue();
            }
        }

        return null;
    }

    public Element getElementByName(Document doc, String elementName) {
        NodeList elements = doc.getElementsByTagName(elementName);
        if (elements != null) {
            for (int i = 0; i < elements.getLength(); i++) {
                Node element = elements.item(i);
                if (element.getNodeType() == Node.ELEMENT_NODE) {
                    return (Element) element;
                }
            }
        }
        return null;
    }

    public static Document stringToDocument(String msg) throws NetconfMessageBuilderException {
        return stringToDocument(msg, true);
    }

    public static Document stringToDocument(String msg, boolean logging) throws NetconfMessageBuilderException {
        try {
            DocumentBuilderFactory factory;
            if (c_getNewDocBuildFactoryEveryTime) {
                factory = DocumentBuilderFactoryWithoutDTD.newInstance();
            } else {
                factory = DocumentBuilderFactoryWithoutDTD.getInstance();
            }
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {

                @Override
                public void warning(SAXParseException e) throws SAXException {
                    if (logging) {
                        LOGGER.warn(PARSE_ERROR, e);
                    }
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    if (logging) {
                        LOGGER.error(PARSE_ERROR, e);
                    }
                }

                @Override
                public void error(SAXParseException e) throws SAXException {
                    if (logging) {
                        LOGGER.error(PARSE_ERROR, e);
                    }
                }
            });
            return builder.parse(new InputSource(new StringReader(msg.trim())));

        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(PARSE_ERROR, e);
        }
    }

    public static String format(String unformattedXml) {
        try {
            final Document document = parseXmlFile(unformattedXml);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactoryWithoutDTD.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String prettyPrint(Document xml) throws NetconfMessageBuilderException {
        Transformer tf;
        try {
            tf = TRANSFORMER_FACTORY.newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            Writer out = new StringWriter();
            tf.transform(new DOMSource(xml), new StreamResult(out));
            LOGGER.trace("Pretty printed doc : " + out.toString());
            return out.toString();
        } catch (TransformerException e) {
            throw new NetconfMessageBuilderException("Error while pretty printing ", e);
        }

    }

    public static final String documentToString(Node xml) throws NetconfMessageBuilderException {
        Transformer tf;
        try {
            tf = TRANSFORMER_FACTORY.newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            Writer out = new StringWriter();
            tf.transform(new DOMSource(xml), new StreamResult(out));
            return out.toString();
        } catch (TransformerException e) {
            throw new NetconfMessageBuilderException("Error while converting document to String ", e);
        }

    }

    public static final String documentToPrettyString(Node xml) throws NetconfMessageBuilderException {
        Transformer tf;
        try {
            tf = TRANSFORMER_FACTORY.newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Writer out = new StringWriter();
            tf.transform(new DOMSource(xml), new StreamResult(out));
            return out.toString();
        } catch (TransformerException e) {
            throw new NetconfMessageBuilderException("Error while converting document to String ", e);
        }

    }

    public static Document getNewDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactoryWithoutDTD.newInstance();
        return dbFactory.newDocumentBuilder().newDocument();
    }

    public static Document loadXmlDocument(InputStream inputStream) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactoryWithoutDTD.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Document getDocFromFile(File xmlFile) {
        DocumentBuilder dBuilder;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactoryWithoutDTD.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;

    }

    public Element getFirstElementChildNode(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) childNode;
            }
        }
        return null;
    }

    public static String getErrorInfoContents(Element errorInfo) {
        if (errorInfo != null) {
            StringBuilder errorInfoContent = new StringBuilder();
            try {
                NodeList nodeList = errorInfo.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        errorInfoContent.append(documentToPrettyString(nodeList.item(i)));
                    }
                }
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error(e.getMessage());
            }
            return errorInfoContent.toString().trim();
        }
        return null;
    }

    public static Element getElement(Document document, String parentName, List<Pair<String, Object>> childs, String namespace,
            String prefix) {
        Element element = document.createElementNS(namespace, getPrefixedLocalName(prefix, parentName));
        for (Pair<String, Object> child : childs) {
            Object value = child.getSecond();
            if (value != null) {
                Element childElement = document.createElementNS(namespace, getPrefixedLocalName(prefix, child.getFirst()));
                if (value instanceof AppendDirectly){
                    childElement = ((AppendDirectly)value).m_element;
                } else if (value instanceof Node) {
                    Node importNode = document.importNode((Node) value, true);
                    childElement.appendChild(importNode);
                } else {
                    childElement.setTextContent(String.valueOf(value));
                }
                element.appendChild(childElement);
            }
        }

        return element;
    }

    public static Element getElement(Document document, String localName, String namespace, String prefix, Object value) {
        Element element = null;
        if (value != null) {
            element = document.createElementNS(namespace, getPrefixedLocalName(prefix, localName));
            if (value instanceof Node) {
                Node importNode = document.importNode((Node) value, true);
                element.appendChild(importNode);
            } else {
                element.setTextContent(String.valueOf(value));
            }
        }
        return element;
    }

    public static String getPrefixedLocalName(String prefix, String localname) {
        return prefix + ":" + localname;
    }

    public static Element getDocumentElement(String string) throws IOException, SAXException, ParserConfigurationException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(string.getBytes())) {
            DocumentBuilderFactory dbf = DocumentBuilderFactoryWithoutDTD.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(input).getDocumentElement();
        }
    }

    public static String getChildNodeValue(Element parentNode, String parentTagName, String childTagName, String namespace) {
        Element parentElement = DocumentUtils.getDescendant(parentNode, parentTagName, namespace);
        Element childNameElement = DocumentUtils.getDescendant(parentElement, childTagName, namespace);
        return childNameElement.getFirstChild().getNodeValue();
    }

    public static boolean isChildNodeExists(Node parent, Node child) {
        for (Element childElement : DocumentUtils.getChildElements(parent)) {
            if (childElement.isEqualNode(child)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchingXmlNotification(String xpathExpression, Document notification) {
        boolean matches = false;
        NodeList entries = notification.getElementsByTagName("*");
        String xpath = "";
        for (int i = 0; i < entries.getLength(); i++) {
            Element element = (Element) entries.item(i);
            String nodeName = element.getNodeName();
            if (!nodeName.contains(NAME_NODE)) {
                String nodeNameAfterRemovingPrefix = nodeName.contains(COLON) ? nodeName.split(COLON)[1] : nodeName;
                xpath += SEPARATED + nodeNameAfterRemovingPrefix;
            }
        }
        matches = xpath.contains(xpathExpression);
        return matches;
    }

    public static class AppendDirectly {
        private Element m_element;

        public AppendDirectly(Element element) {
            m_element = element;
        }
    }
}
