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

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import com.google.common.base.Optional;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;

/**
 * Created by pregunat on 2/5/16.
 */
public class NotificationFilterUtil {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
            .NETCONF_NOTIFICATION);

    /**
     * Matches @param notification elements with the @param filterNode
     * 1) Checks whether the current element of notification is mentioned as a SelectNode in Filter
     * E.g:
     * Notification:
     * <notification>
     * <test-notification>
     * ...
     * </test-notification>
     * </notification>
     * Filter:
     * <test-notification/>
     * 2) Else it would check the filterNode's child nodes for a match
     *
     * @param notification
     * @param filterNode
     * @return
     */

    public static boolean matches(Notification notification, FilterNode filterNode) {
        if (filterNode == null) {
            return true;
        }
        try {
            Element notificationElement = getNotificationElement(notification.getNotificationDocument().getDocumentElement());
            if (notificationElement != null) {
                boolean match = matchSelectNode(notificationElement, filterNode);
                if (match) {
                    return true;
                } else {
                    return matchChildNode(notificationElement, filterNode);
                }
            }
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error(null, "Exception while building notification element", e);
        }
        return false;
    }

    private static Element getNotificationElement(Element documentElement) {
        Element childElement = DocumentUtils.getInstance().getFirstElementChildNode(documentElement);
        if (childElement != null && childElement.getNodeName().equals(NetconfResources.EVENT_TIME)) {
            return (Element) childElement.getNextSibling();
        } else {
            return childElement;
        }
    }

    /**
     * Checks the child nodes of filterNode, if notification element matches with any of the child node
     * 1) If match is found, checks whether the child element of notification is matching to a FilterMatchNode or SelectNode (if any) in Filter
     * a) When child element doesn't match to a MatchNode in filter, we would check the filterNode's child nodes for a match
     * 2) Else it would check the notification element's sibling node matches with child nodes of FilterNode
     * <p>
     * Eg:
     * Notification:
     * <test-notification>
     * <element1>
     * <sub-element1>value</sub-element1>
     * <sub-element2>value</sub-element2>
     * </element1>
     * <element2>
     * <sub-element1>value</sub-element1>
     * <sub-element2>value</sub-element2>
     * </element2>
     * </test-notification>
     * Filter:
     * <test-notification>
     * <element2>
     * <sub-element1>value</sub-element1>  // match node
     * </element2>
     * </test-notification>
     *
     * @param notificationElement
     * @param filterNode
     * @return
     */

    private static boolean matchChildNode(Element notificationElement, FilterNode filterNode) {
        boolean match = false;
        List<FilterNode> childNodes = filterNode.getChildNodes();
        for (FilterNode node : childNodes) {
            if (notificationElement.getLocalName().equals(node.getNodeName())) {
                Element childElement = getNotificationElement(notificationElement);
                match = matchMatchNode(childElement, node);
                if (!match) {
                    return matchChildNode(childElement, node);
                }
            } else {
                Element siblingElement = (Element) notificationElement.getNextSibling();
                if (siblingElement != null) {
                    return matchChildNode(siblingElement, filterNode);
                }
            }
        }
        return match;
    }

    /**
     * Checks the notification element matches a FilterMatchNode of FilterNode
     * Upon a match, it would match the sibling element of notification element matches to a SelectNode or a FilterMatchNode
     * <p>
     * Eg:
     * Notification:
     * <test-notification>
     * <element1>
     * <sub-element1>value</sub-element1>
     * <sub-element2>value</sub-element2>
     * </element1>
     * <element2>
     * <sub-element1>value</sub-element1>
     * <sub-element2>value</sub-element2>
     * </element2>
     * </test-notification>
     * Filter:
     * <test-notification>
     * <element2>
     * <sub-element1>value</sub-element1>  // match node
     * <sub-element2>value</sub-element2>	// select node
     * </element2>
     * </test-notification>
     *
     * @param element
     * @param filterNode
     * @return
     */

    private static boolean matchMatchNode(Element element, FilterNode filterNode) {
        boolean match = false;
        match = matchSelectNode(element, filterNode);
        if (!match) {
            match = matchFilterMatchNode(element, filterNode);
        }

        if (match) {
            Element siblingElement = (Element) element.getNextSibling();
            if (siblingElement != null) {
                return matchMatchNode(siblingElement, filterNode);
            }
        }
        return match;
    }

    /**
     * Checks whether notificationElement matches to a SelectNode of FilterNode
     *
     * @param notificationElement
     * @param filterNode
     * @return
     */

    private static boolean matchSelectNode(Element notificationElement, FilterNode filterNode) {
        List<FilterNode> selectNodes = filterNode.getSelectNodes();
        for (FilterNode node : selectNodes) {
            if (notificationElement.getLocalName().equals(node.getNodeName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether notificationElement matches to a FilterMatchNode of FilterNode
     *
     * @param notificationElement
     * @param filterNode
     * @return
     */

    private static boolean matchFilterMatchNode(Element notificationElement, FilterNode filterNode) {
        List<FilterMatchNode> matchNodes = filterNode.getMatchNodes();
        for (FilterMatchNode node : matchNodes) {
            if (notificationElement.getLocalName().equals(node.getNodeName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filters notification content. If no match is found, null is returned
     *
     * @param notification notification
     * @param filter       filter
     * @return document containing filtered notification content or null
     */
    public static Notification filterNotification(Notification notification, NetconfFilter filter) {
        if (filter == null || filter.getXmlFilterElements() == null) {
            // return original notification if no filter
            return notification;
        }
        try {
            LOGGER.debug(null, "Applying filter {} on notification message {}",
                    DocumentUtils.documentToPrettyString(filter.getXmlFilter()), notification.notificationToPrettyString());
            XmlElement filterElement = XmlElement.fromDomElement(filter.getXmlFilter());
            Document notificationDoc = notification.getNotificationDocument();

            // remove eventType node
            final Node eventTimeNode = notificationDoc.getDocumentElement().getElementsByTagName(NetconfResources.EVENT_TIME).item(0);
            notificationDoc.getDocumentElement().removeChild(eventTimeNode);

            Document result = doFilter(notificationDoc, filterElement);
            if (result != null) {
                NetconfNotification filteredNotification = new NetconfNotification(notification.getNotificationDocument());
                filteredNotification.setNotificationElement(result.getDocumentElement());
                return filteredNotification;
            }
        } catch (Exception ex) {
            LOGGER.error(null, "Error while applying filter notification", ex);
        }
        return null;
    }

    private static Document doFilter(Document notification, XmlElement filter) throws IllegalArgumentException {
        Document result = DocumentUtils.createDocument();
        XmlElement dataSrc = XmlElement.fromDomDocument(notification);
        Element dataDst = (Element) result.importNode(dataSrc.getDomElement(), false);
        for (XmlElement filterChild : filter.getChildElements()) {
            addSubtree(filterChild, dataSrc.getOnlyChildElement(), XmlElement.fromDomElement(dataDst));
        }
        if (dataDst.getFirstChild() != null) {
            result.appendChild(dataDst.getFirstChild());
            return result;
        }
        return null;
    }

    private static MatchingResult addSubtree(XmlElement filter, XmlElement src, XmlElement dstParent) throws IllegalArgumentException {
        Document document = dstParent.getDomElement().getOwnerDocument();
        MatchingResult matches = matchesElement(src, filter);
        if (matches != MatchingResult.NO_MATCH && matches != MatchingResult.CONTENT_MISMATCH) {
            // copy srcChild to dst
            boolean filterHasChildren = !filter.getChildElements().isEmpty();
            // copy to depth if this is leaf of filter tree
            Element copied = (Element) document.importNode(src.getDomElement(), !filterHasChildren);
            boolean shouldAppend = !filterHasChildren;
            if (filterHasChildren) { // this implies TAG_MATCH
                // do the same recursively
                int numberOfTextMatchingChildren = 0;
                for (XmlElement srcChild : src.getChildElements()) {
                    for (XmlElement filterChild : filter.getChildElements()) {
                        MatchingResult childMatch = addSubtree(filterChild, srcChild, XmlElement.fromDomElement(copied));
                        if (childMatch == MatchingResult.CONTENT_MISMATCH) {
                            return MatchingResult.NO_MATCH;
                        }
                        if (childMatch == MatchingResult.CONTENT_MATCH) {
                            numberOfTextMatchingChildren++;
                        }
                        // append if child matches or shouldAppend already true
                        shouldAppend |= childMatch != MatchingResult.NO_MATCH;
                    }
                }
                // if only text matching child filters are specified..
                if (numberOfTextMatchingChildren == filter.getChildElements().size()) {
                    // force all children to be added (to depth). This is done by copying parent node to depth.
                    // implies shouldAppend == true
                    copied = (Element) document.importNode(src.getDomElement(), true);
                }
            }
            if (shouldAppend) {
                dstParent.getDomElement().appendChild(copied);
            }
        }
        return matches;
    }

    /**
     * Shallow compare src node to filter: tag name and namespace must match.
     * If filter node has no children and has text content, it also must match.
     */
    private static MatchingResult matchesElement(XmlElement src, XmlElement filter) throws IllegalArgumentException {
        boolean tagMatch = src.getName().equals(filter.getName())
                && src.getNamespaceOptionally().equals(filter.getNamespaceOptionally());
        MatchingResult result = null;
        if (tagMatch) {
            // match text content
            Optional<String> maybeText = filter.getOnlyTextContentOptionally();
            if (maybeText.isPresent() && !maybeText.get().trim().isEmpty()) {
                if (maybeText.equals(src.getOnlyTextContentOptionally()) || prefixedContentMatches(filter, src)) {
                    result = MatchingResult.CONTENT_MATCH;
                } else {
                    result = MatchingResult.CONTENT_MISMATCH;
                }
            }
            // match attributes, combination of content and tag is not supported
            if (result == null) {
                for (Attr attr : filter.getAttributes().values()) {
                    // ignore namespace declarations
                    if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attr.getNamespaceURI())) {
                        // find attr with matching localName(),  namespaceURI(),  == value() in src
                        String found = src.getAttribute(attr.getLocalName(), attr.getNamespaceURI());
                        if (attr.getValue().equals(found) && result != MatchingResult.NO_MATCH) {
                            result = MatchingResult.TAG_MATCH;
                        } else {
                            result = MatchingResult.NO_MATCH;
                        }
                    }
                }
            }
            if (result == null) {
                result = MatchingResult.TAG_MATCH;
            }
        }
        if (result == null) {
            result = MatchingResult.NO_MATCH;
        }
        LOGGER.debug(null, "Matching {} to {} resulted in {}", src, filter, result);
        return result;
    }

    private static boolean prefixedContentMatches(final XmlElement filter, final XmlElement src) throws IllegalArgumentException {
        final Map.Entry<String, String> prefixToNamespaceOfFilter;
        final Map.Entry<String, String> prefixToNamespaceOfSrc;
        try {
            prefixToNamespaceOfFilter = filter.findNamespaceOfTextContent();
            prefixToNamespaceOfSrc = src.findNamespaceOfTextContent();
        } catch (IllegalArgumentException e) {
            //if we can't find namespace of prefix - it's not a prefix, so it doesn't match
            LOGGER.debug(null, "Cannot find namespace of prefix", e);
            return false;
        }

        final String prefix = prefixToNamespaceOfFilter.getKey();
        // If this is not a prefixed content, we do not need to continue since content do not match
        if (prefix.equals(XmlElement.DEFAULT_NAMESPACE_PREFIX)) {
            return false;
        }
        // Namespace mismatch
        if (!prefixToNamespaceOfFilter.getValue().equals(prefixToNamespaceOfSrc.getValue())) {
            return false;
        }

        final String unprefixedFilterContent = filter.getTextContent().substring(prefixToNamespaceOfFilter.getKey().length() + 1);
        final String unprefixedSrcContnet = src.getTextContent().substring(prefixToNamespaceOfSrc.getKey().length() + 1);
        // Finally compare unprefixed content
        return unprefixedFilterContent.equals(unprefixedSrcContnet);
    }

    enum MatchingResult {
        NO_MATCH, TAG_MATCH, CONTENT_MATCH, CONTENT_MISMATCH
    }
}
