package org.broadband_forum.obbaa.netconf.mn.fwk.tests.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

/**
 * Helps to build XML on the fly, given a string of a predefined format.<br>
 * 
 * <b>E.G:</b> The below string<br>
 * #rpc[@xmlns='urn:ietf:params:xml:ns:netconf:base:1.0', @message-id='1',
 * parent='null'] <br>
 * #validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation',
 * parent='null'] <br>
 * #validation:data-status[parent='validation:testRpc', value='success'] <br>
 * #validation:leaf-type[parent='validation:testRpc', value='good'] <br>
 * <br>
 * Will produce the below xml <br>
 * 
 * <pre>
 * {@code
 *	<rpc message-id="1" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> 
 *		<validation:testRpc xmlns:validation="urn:org:bbf:pma:validation">
 *			<validation:data-status>success</validation:data-status> 
 *			<validation:leaf-type>good</validation:leaf-type> 
 *		</validation:testRpc> 
 *	</rpc>
 * }
 * </pre>
 * 
 * <br>
 * <br>
 * # -> indicates a new element. <br>
 * [] -> indicates parameters for the new element, each parameter seperated by
 * comma(,) <br>
 * @ -> any attributes for the node, must be specified within [] <br>
 * parent -> name of the parent node, must be specified within [] <br>
 * value -> text content for the current node, must be specified within []
 * 
 */
public class XmlGenerator {

	static final String HASH = "#";
	static final String SLASH = "/";
	static final String CLOSE_SQUARE = "]";
	static final String OPEN_SQUARE = "[";
	static final String OPEN_SQUARE_PATTERN = "\\[";
	static final String AT = "@";
	static final String PARENT = "parent";
	static final String VALUE = "value";
	static final String NULL = "null";
	static final String SINGLE_QUOTE = "'";
	static final String EQUALS = "=";
	static final String COLON = ":";
	static final String XMLNS = "xmlns";
	static final String COMMA = ",";
	
	public static final String EMPTY_STRING = "";

	StringBuilder m_mainString = new StringBuilder();

	public XmlGenerator addElement(String parent, String name, String textContent, String... attributes) {
		boolean attributesUpdated = false;
		StringBuilder builder = new StringBuilder();
		builder.append(HASH).append(name).append(OPEN_SQUARE);
		if (attributes != null)
			for (int i = 0; i < attributes.length; i++) {
				if (attributes[i] != null && !attributes[i].isEmpty()) {
					attributesUpdated = true;
					builder.append(AT).append(attributes[i]);
					if (i < attributes.length - 1) {
						builder.append(COMMA);
					}
				}
			}

		if (parent != null) {
			if (attributesUpdated) {
				builder.append(COMMA);
			}
			builder.append(PARENT).append(EQUALS).append(SINGLE_QUOTE).append(parent).append(SINGLE_QUOTE);
		}

		if (textContent != null) {
			if (attributes.length > 0 || parent != null) {
				builder.append(COMMA);
			}
			builder.append(VALUE).append(EQUALS).append(SINGLE_QUOTE).append(textContent).append(SINGLE_QUOTE);
		}

		builder.append(CLOSE_SQUARE);

		m_mainString.append(builder.toString());
		return this;
	}

	public Element buildXml() throws NetconfMessageBuilderException {
		return buildXml(m_mainString.toString());
	}

	public Element buildXml(Document document) {
		return buildXml(m_mainString.toString(), document);
	}

	public static Element buildXml(String xmlPath) throws NetconfMessageBuilderException {
		Document document = DocumentUtils.createDocument();
		Element returnValue = buildXml(xmlPath, document);
		return returnValue;// buildXml(xmlPath, document);
	}

	private static Element buildXml(String xmlPath, Document document) {
		String namespace = null;
		Element xmlElement = null;

		// split and get each element detail for xml
		String[] splitHash = xmlPath.split(HASH);
		String parent = null;
		for (String str : splitHash) {
			String elementName = null;
			String textContent = null;
			String parentNode = null;
			List<String> attributes = new ArrayList<String>();
			Map<String, String> attributeValueMap = new HashMap<String, String>();
			if (str.isEmpty()) {
				continue;
			}

			// check for parameters for the current element
			if (str.contains(OPEN_SQUARE) && str.endsWith(CLOSE_SQUARE)) {
				str = str.substring(0, str.length() - 1);
				String[] xmlContents = str.split(OPEN_SQUARE_PATTERN);
				elementName = xmlContents[0];
				if (xmlContents[1].contains(COMMA)) {
					String[] splitComma = xmlContents[1].split(COMMA);
					for (String predicate : splitComma) {
						predicate = predicate.trim();
						if (predicate.startsWith(AT)) {
							attributes.add(predicate);
						} else if (predicate.startsWith(VALUE)) {
							textContent = predicate.split(EQUALS)[1];
							textContent = textContent.trim();
							textContent = textContent.substring(textContent.indexOf(SINGLE_QUOTE) + 1,
									textContent.lastIndexOf(SINGLE_QUOTE));
						} else if (predicate.startsWith(PARENT)) {
							parentNode = predicate.split(EQUALS)[1];
							parentNode = parentNode.trim();
							parentNode = parentNode.substring(parentNode.indexOf(SINGLE_QUOTE) + 1,
									parentNode.lastIndexOf(SINGLE_QUOTE));
							if (!parentNode.equals(NULL)) {
								parent = parentNode;
							}
						}
					}

				} else {
					String predicate = xmlContents[1].trim();
					if (predicate.startsWith(AT)) {
						attributes.add(predicate);
					} else if (predicate.startsWith(VALUE)) {
						textContent = predicate.split(EQUALS)[1];
						textContent = textContent.trim();
						textContent = textContent.substring(textContent.indexOf(SINGLE_QUOTE) + 1,
								textContent.lastIndexOf(SINGLE_QUOTE));
					} else if (predicate.startsWith(PARENT)) {
						parentNode = predicate.split(EQUALS)[1];
						parentNode = parentNode.trim();
						parentNode = parentNode.substring(parentNode.indexOf(SINGLE_QUOTE) + 1,
								parentNode.lastIndexOf(SINGLE_QUOTE));
						if (!parentNode.equals(NULL)) {
							parent = parentNode;
						}
					}
				}
				for (String attribute : attributes) {
					if (attribute.startsWith(AT + XMLNS + COLON)) {
						String[] splitEqual = attribute.split(EQUALS);
						String attr = splitEqual[0].trim();
						String val = splitEqual[1].trim();
						val = val.substring(val.indexOf(SINGLE_QUOTE) + 1, val.lastIndexOf(SINGLE_QUOTE));
						if (elementName.contains(COLON) && attr.split(COLON)[1].equals(elementName.split(COLON)[0])) {
							namespace = val;
						} else {
							attributeValueMap.put(attr, val);
						}
					} else if (attribute.startsWith(AT + XMLNS + EQUALS)) {
						String[] splitEqual = attribute.split(EQUALS);
						namespace = splitEqual[1].trim();
						namespace = namespace.substring(namespace.indexOf(SINGLE_QUOTE) + 1,
								namespace.lastIndexOf(SINGLE_QUOTE));
					} else {
						String[] splitEqual = attribute.split(EQUALS);
						String attr = splitEqual[0].substring(1).trim();
						String val = splitEqual[1].trim();
						val = val.substring(val.indexOf(SINGLE_QUOTE) + 1, val.lastIndexOf(SINGLE_QUOTE));
						attributeValueMap.put(attr, val);
					}
				}
			}
			Element element = document.createElementNS(namespace, elementName);

			for (String attr : attributeValueMap.keySet()) {
				element.setAttribute(attr, attributeValueMap.get(attr));
			}

			if (textContent != null) {
				element.setTextContent(textContent);
			}

			if (xmlElement == null && parent == null) {
				xmlElement = element;
				parent = xmlElement.getNodeName();
				document.appendChild(xmlElement);
			} else {
				if (xmlElement.getNodeName().equals(parent)) {
					xmlElement.appendChild(element);
				} else {
					NodeList nodes = xmlElement.getElementsByTagName(parent);
					for (int i = 0; i < nodes.getLength(); i++) {
						if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE
								&& nodes.item(i).getNodeName().equals(parent)) {
							nodes.item(i).appendChild(element);
						}
					}
				}
			}
		}
		return xmlElement;
	}
}
