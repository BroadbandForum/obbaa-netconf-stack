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

package org.broadband_forum.obbaa.netconf.server.model.notification.utils;

import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.XMLConstants;

public final class XmlElement {

    public static final String DEFAULT_NAMESPACE_PREFIX = "";

    private final Element element;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
            .NETCONF_NOTIFICATION);

    private XmlElement(final Element element) {
        this.element = element;
    }

    public static XmlElement fromDomElement(final Element element) {
        return new XmlElement(element);
    }

    public static XmlElement fromDomDocument(final Document xml) {
        return new XmlElement(xml.getDocumentElement());
    }

    private Map<String, String> extractNamespaces() throws IllegalArgumentException {
        Map<String, String> namespaces = new HashMap<>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String attribKey = attribute.getNodeName();
            if (attribKey.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                String prefix;
                if (attribKey.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                    prefix = DEFAULT_NAMESPACE_PREFIX;
                } else {
                    if (!attribKey.startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":")) {
                        throw new IllegalArgumentException("Attribute doesn't start with :");
                    }
                    prefix = attribKey.substring(XMLConstants.XMLNS_ATTRIBUTE.length() + 1);
                }
                namespaces.put(prefix, attribute.getNodeValue());
            }
        }

        // namespace does not have to be defined on this element but inherited
        if (!namespaces.containsKey(DEFAULT_NAMESPACE_PREFIX)) {
            Optional<String> namespaceOptionally = getNamespaceOptionally();
            if (namespaceOptionally.isPresent()) {
                namespaces.put(DEFAULT_NAMESPACE_PREFIX, namespaceOptionally.get());
            }
        }

        return namespaces;
    }

    public String getName() {
        final String localName = element.getLocalName();
        if (!Strings.isNullOrEmpty(localName)) {
            return localName;
        }
        return element.getTagName();
    }

    public String getAttribute(final String attributeName) {
        return element.getAttribute(attributeName);
    }

    public String getAttribute(final String attributeName, final String namespace) {
        return element.getAttributeNS(namespace, attributeName);
    }

    public NodeList getElementsByTagName(final String name) {
        return element.getElementsByTagName(name);
    }

    public void appendChild(final Element toAppend) {
        this.element.appendChild(toAppend);
    }

    public Element getDomElement() {
        return element;
    }

    public Map<String, Attr> getAttributes() {

        Map<String, Attr> mappedAttributes = Maps.newHashMap();

        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            mappedAttributes.put(attr.getNodeName(), attr);
        }

        return mappedAttributes;
    }

    /**
     * Non recursive.
     */
    private List<XmlElement> getChildElementsInternal(final ElementFilteringStrategy strat) {
        NodeList childNodes = element.getChildNodes();
        final List<XmlElement> result = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (!(item instanceof Element)) {
                continue;
            }
            if (strat.accept((Element) item)) {
                result.add(new XmlElement((Element) item));
            }
        }

        return result;
    }

    public List<XmlElement> getChildElements() {
        return getChildElementsInternal(e -> true);
    }

    /**
     * Returns the child elements for the given tag.
     *
     * @param tagName tag name without prefix
     * @return List of child elements
     */
    public List<XmlElement> getChildElements(final String tagName) {
        return getChildElementsInternal(e -> {
            // localName returns pure localName without prefix
            return e.getLocalName().equals(tagName);
        });
    }

    public XmlElement getOnlyChildElement() throws IllegalArgumentException {
        List<XmlElement> children = getChildElements();
        if (children.size() != 1) {
            throw new IllegalArgumentException(String.format("One element expected in %s but was %s", toString(), children.size()));
        }
        return children.get(0);
    }

    public String getTextContent() throws IllegalArgumentException {
        NodeList childNodes = element.getChildNodes();
        if (childNodes.getLength() == 0) {
            return DEFAULT_NAMESPACE_PREFIX;
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node textChild = childNodes.item(i);
            if (textChild instanceof Text) {
                String content = textChild.getTextContent();
                return content.trim();
            }
        }
        throw new IllegalArgumentException(getName() + " should contain text.");
    }

    public Optional<String> getOnlyTextContentOptionally() {
        // only return text content if this node has exactly one Text child node
        if (element.getChildNodes().getLength() == 1) {
            Node item = element.getChildNodes().item(0);
            if (item instanceof Text) {
                return Optional.of(((Text) item).getWholeText());
            }
        }
        return Optional.absent();
    }

    public Optional<String> getNamespaceOptionally() {
        String namespaceURI = element.getNamespaceURI();
        if (Strings.isNullOrEmpty(namespaceURI)) {
            return Optional.absent();
        } else {
            return Optional.of(namespaceURI);
        }
    }

    public String getNamespace() throws IllegalArgumentException {
        Optional<String> namespaceURI = getNamespaceOptionally();
        if (!namespaceURI.isPresent()) {
            throw new IllegalArgumentException(String.format("No namespace defined for %s", this));
        }
        return namespaceURI.get();
    }

    /**
     * Search for element's attributes defining namespaces. Look for the one
     * namespace that matches prefix of element's text content. E.g.
     *
     * <pre>
     * &lt;type
     * xmlns:th-java="urn:opendaylight:params:xml:ns:yang:controller:threadpool:impl"&gt;
     *     th-java:threadfactory-naming&lt;/type&gt;
     * </pre>
     *
     * <p>
     * returns {"th-java","urn:.."}. If no prefix is matched, then default
     * namespace is returned with empty string as key. If no default namespace
     * is found value will be null.
     */
    public Map.Entry<String/* prefix */, String/* namespace */> findNamespaceOfTextContent()
            throws IllegalArgumentException {
        Map<String, String> namespaces = extractNamespaces();
        String textContent = getTextContent();
        int indexOfColon = textContent.indexOf(':');
        String prefix;
        if (indexOfColon > -1) {
            prefix = textContent.substring(0, indexOfColon);
        } else {
            prefix = DEFAULT_NAMESPACE_PREFIX;
        }
        if (!namespaces.containsKey(prefix)) {
            throw new IllegalArgumentException("Cannot find namespace for " + element.toString()
                    + ". Prefix from content is " + prefix + ". Found namespaces " + namespaces);
        }
        return Maps.immutableEntry(prefix, namespaces.get(prefix));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("XmlElement{");
        sb.append("name='").append(getName()).append('\'');
        if (element.getNamespaceURI() != null) {
            try {
                sb.append(", namespace='").append(getNamespace()).append('\'');
            } catch (final IllegalArgumentException e) {
                LOGGER.debug(null, "Missing namespace for element.");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        XmlElement that = (XmlElement) obj;

        return element.isEqualNode(that.element);

    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    private interface ElementFilteringStrategy {
        boolean accept(Element element);
    }
}
