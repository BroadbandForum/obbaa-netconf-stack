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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getNewDocument;

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.joda.time.DateTime;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A utility class to transform a {@link NetconfMessage} to netconf {@link Document}.
 * 
 * 
 * 
 */
public class PojoToDocumentTransformer {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(PojoToDocumentTransformer.class, LogAppNames.NETCONF_LIB);

    private static final String RPC_ELEMENT_IS_NULL_CREATE_THE_RPC_ELEMENT_FIRST = "<rpc> Element is null, create the rpc element first";

    protected static final String ERROR_WHILE_BUILDING_DOCUMENT = "Error while building document ";

    private static final String NEW_LINE = System.getProperty("line.separator");

    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";

    public static final String XMLNS = "xmlns:";

    protected Document m_doc;

    public PojoToDocumentTransformer() {

    }

    public PojoToDocumentTransformer newNetconfRpcDocument(String messageId) throws NetconfMessageBuilderException {
        try {
            m_doc = getNewDocument();

            // create the root element rpc
            Element element = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC);
            if (messageId != null) {
                element.setAttribute(NetconfResources.MESSAGE_ID, messageId);
            }
            m_doc.appendChild(element);
            return this;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }

    public Document build() throws NetconfMessageBuilderException {
    	if(LOGGER.isTraceEnabled()) {
    		try {
    			DocumentUtils.prettyPrint(m_doc);
    		} catch (Exception e) {
    			LOGGER.error(ERROR_WHILE_BUILDING_DOCUMENT, e);
    			throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
    		}
    	}
        return m_doc;
    }

    private void addWithDelayElement(Element element, int withDelay) {
        if (withDelay > 0) {
            Element withDelayElement = m_doc.createElementNS(NetconfResources.WITH_DELAY_NS, NetconfResources.WITH_DELAY);
            withDelayElement.setTextContent(String.valueOf(withDelay));
            element.appendChild(withDelayElement);
        }
    }

    private void addWithSliceOwnerElement(Element element, String sliceOwner) {
        if (sliceOwner!=null) {
            Element sliceOwnerElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, DocumentToPojoTransformer.SLICE_OWNER);
            sliceOwnerElement.setTextContent(sliceOwner);
            element.appendChild(sliceOwnerElement);
        }
    }

    private void addDepthElement(Element element, int depth) {
        if (depth != NetconfQueryParams.UNBOUNDED) {
            Element depthElement = m_doc.createElementNS(NetconfResources.EXTENSION_NS, NetconfResources.DEPTH);
            depthElement.setTextContent(String.valueOf(depth));
            element.appendChild(depthElement);
        }
    }
    
    private void addFieldElements(Element element, Map<String, List<QName>> fieldValues) {
        for ( Entry<String, List<QName>> entry : fieldValues.entrySet()){
            Element fieldsElement = m_doc.createElementNS(NetconfResources.EXTENSION_NS, NetconfResources.FIELDS);
            element.appendChild(fieldsElement);
            Element dataNodeElement = m_doc.createElementNS(NetconfResources.EXTENSION_NS, NetconfResources.DATA_NODE);
            dataNodeElement.setTextContent(entry.getKey());
            fieldsElement.appendChild(dataNodeElement);
            for ( QName attribute : entry.getValue()){
                Element attributeElement = m_doc.createElementNS(NetconfResources.EXTENSION_NS, NetconfResources.ATTRIBUTE);
                attributeElement.setTextContent(attribute.getLocalName());
                fieldsElement.appendChild(attributeElement);
            }
        }
    }

    public PojoToDocumentTransformer addUserContextAttributes(String userContext, String sessionId){
        Node rpcNode = m_doc.getFirstChild();
        if (StringUtils.isNotEmpty(userContext)) {
            ((Element)rpcNode).setAttributeNS(XMLNS_NAMESPACE, NetconfResources.XMLNS_CTX, NetconfResources.EXTENSION_NS);
            ((Element)rpcNode).setAttribute(NetconfResources.CTX_USER_CONTEXT, userContext);
            if (StringUtils.isNotEmpty(sessionId)) {
                ((Element)rpcNode).setAttribute(NetconfResources.CTX_SESSION_ID, sessionId);
            }
        }
        return this;
    }

    public PojoToDocumentTransformer addGetConfigElement(String source, NetconfFilter netconfFilter, WithDefaults withDefaults, int withDelay, int depth, Map<String, List<QName>> fieldValues)
            throws NetconfMessageBuilderException {
        // Add it right after "<rpc>"
        Node rpcNode = m_doc.getFirstChild();
        if (rpcNode == null) {
            throw new NetconfMessageBuilderException(RPC_ELEMENT_IS_NULL_CREATE_THE_RPC_ELEMENT_FIRST);
        }
        Element getConfigElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.GET_CONFIG);
        rpcNode.appendChild(getConfigElement);
        addSource(source, getConfigElement);
        if (netconfFilter != null) {
            addFilter(netconfFilter, getConfigElement);
        }
        if (withDefaults != null) {
            Element withDefaultElement = m_doc.createElementNS(NetconfResources.WITH_DEFAULTS_NS, NetconfResources.WITH_DEFAULTS);
            withDefaultElement.setTextContent(withDefaults.getValue());
            getConfigElement.appendChild(withDefaultElement);
        }

        addWithDelayElement(getConfigElement, withDelay);
        
        addDepthElement(getConfigElement, depth);
        addFieldElements(getConfigElement, fieldValues);

        return this;
    }

    public PojoToDocumentTransformer addCreateSubscriptionElement(String stream, NetconfFilter netconfFilter, DateTime startTime,
            DateTime stopTime) throws NetconfMessageBuilderException {
        // Add it right after "<rpc>"
        Node rpcNode = m_doc.getFirstChild();
        if (rpcNode == null) {
            throw new NetconfMessageBuilderException(RPC_ELEMENT_IS_NULL_CREATE_THE_RPC_ELEMENT_FIRST);
        }
        Element createSubscriptionElement = m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS,
                NetconfResources.CREATE_SUBSCRIPTION);
        rpcNode.appendChild(createSubscriptionElement);
        addStream(stream, createSubscriptionElement);
        addFilter(netconfFilter, createSubscriptionElement);
        addStartTime(startTime, createSubscriptionElement);
        addStopTime(stopTime, createSubscriptionElement);
        return this;
    }

    private void addStream(String stream, Element createSubscriptionElement) throws NetconfMessageBuilderException {
        if (stream != null) {
            Element streamElementWrapper = m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS, NetconfResources.STREAM);
            if (stream != null && !stream.isEmpty()) {
                streamElementWrapper.setTextContent(stream);
            } else {
                throw new NetconfMessageBuilderException("stream name is empty/null");
            }

            /*
             * arg1 should be Stream to enable this code if (stream.getDescription() != null && !stream.getDescription().isEmpty()) {
             * Element description = m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS, NetconfResources.DESCRIPTION);
             * description.setTextContent(stream.getDescription()); streamElementWrapper.appendChild(description); }
             * 
             * if (stream.getReplaySupport()) { Element replaySupport = m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS,
             * NetconfResources.REPLAY_SUPPORT); replaySupport.setTextContent(Boolean.toString(stream.getReplaySupport()));
             * streamElementWrapper.appendChild(replaySupport); }
             * 
             * if (stream.getReplayLogCreationTime() != null) { Element replayLogCreationTime =
             * m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS, NetconfResources.REPLAY_LOG_CREATION_TIME);
             * replayLogCreationTime.setTextContent(stream.getReplayLogCreationTime().toString());
             * streamElementWrapper.appendChild(replayLogCreationTime); }
             */

            createSubscriptionElement.appendChild(streamElementWrapper);
        }
    }

    private void addStartTime(DateTime startTime, Element createSubscriptionElement) {
        if (startTime != null) {
            Element startTimeElement = m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS, NetconfResources.START_TIME);
            startTimeElement.setTextContent(NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(startTime));
            createSubscriptionElement.appendChild(startTimeElement);
        }
    }

    private void addStopTime(DateTime stopTime, Element createSubscriptionElement) {
        if (stopTime != null) {
            Element stopTimeElement = m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS, NetconfResources.STOP_TIME);
            stopTimeElement.setTextContent(NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(stopTime));
            createSubscriptionElement.appendChild(stopTimeElement);
        }
    }

    private void addFilter(NetconfFilter netconfFilter, Element getConfigElement) {
        if (netconfFilter != null) {
            Element filterElementWrapper = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.FILTER);
            if (netconfFilter.getType() != null && !netconfFilter.getType().isEmpty()) {
                filterElementWrapper.setAttribute(NetconfResources.TYPE, netconfFilter.getType());
            }
            if (NetconfFilter.XPATH_TYPE.equals(netconfFilter.getType())) {
                for(Entry<String, String> nsPrefixEntry : netconfFilter.getNsPrefixMap().entrySet()){
                    filterElementWrapper.setAttributeNS(PojoToDocumentTransformer.XMLNS_NAMESPACE,
                            PojoToDocumentTransformer.XMLNS+nsPrefixEntry.getValue(), nsPrefixEntry.getKey());
                }
                filterElementWrapper.setAttribute(NetconfResources.SELECT, netconfFilter.getSelectAttribute());
            } else {
                List<Element> xmlFilterElements = netconfFilter.getXmlFilterElements();
                if (xmlFilterElements != null) {
                    for (Element xmlFilterElement : xmlFilterElements) {
                        filterElementWrapper.appendChild(m_doc.importNode(xmlFilterElement, true));
                    }
                }
            }
            getConfigElement.appendChild(filterElementWrapper);
        }
    }

    private void addSource(String source, Element getConfigElement) {
        if (source != null) {
            Element sourceElementWrapper = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DATA_SOURCE);
            getConfigElement.appendChild(sourceElementWrapper);

            Element sourceElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, source);
            sourceElementWrapper.appendChild(sourceElement);
        }
    }

    public PojoToDocumentTransformer addEditConfigElement(Boolean withTransactionId, String source, String defaultOperation, String testOption, String errorOption,
            int withDelay, EditConfigElement configElement, String userContext, String sessionId) throws NetconfMessageBuilderException {

        Node rpcNode = m_doc.getFirstChild();
        Element editConfigElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.EDIT_CONFIG);
        rpcNode.appendChild(editConfigElement);
        if (StringUtils.isNotEmpty(userContext)) {
            ((Element)rpcNode).setAttributeNS(XMLNS_NAMESPACE, NetconfResources.XMLNS_CTX, NetconfResources.EXTENSION_NS);
            ((Element)rpcNode).setAttribute(NetconfResources.CTX_USER_CONTEXT, userContext);
            if (StringUtils.isNotEmpty(sessionId)) {
                ((Element)rpcNode).setAttribute(NetconfResources.CTX_SESSION_ID, sessionId);
            }
        }
        
        if (source != null) {
            Element sourceElementWrapper = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DATA_TARGET);
            editConfigElement.appendChild(sourceElementWrapper);

            Element sourceElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, source);
            sourceElementWrapper.appendChild(sourceElement);
        }
        if (defaultOperation != null) {
            Element defaultOperationElement = m_doc
                    .createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DEFAULT_OPERATION);
            defaultOperationElement.setTextContent(defaultOperation);
            editConfigElement.appendChild(defaultOperationElement);
        }

        if (testOption != null) {
            Element testOptionElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.TEST_OPTION);
            testOptionElement.setTextContent(testOption);
            editConfigElement.appendChild(testOptionElement);
        }

        if (errorOption != null) {
            Element errorOptionElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.ERROR_OPTION);
            errorOptionElement.setTextContent(errorOption);
            editConfigElement.appendChild(errorOptionElement);
        }

        if (withTransactionId) {
            Element withTransactionIdElement = m_doc
                    .createElementNS(NetconfResources.TRANSACTION_ID_NS, NetconfResources.WITH_TRANSACTION_ID);
            editConfigElement.appendChild(withTransactionIdElement);
        }

        addWithDelayElement(editConfigElement, withDelay);

        if (configElement == null) {
            throw new NetconfMessageBuilderException("empty/null <config> for <edit-config> :" + configElement);
        } else {
            validateEditConfigElement(configElement);
            editConfigElement.appendChild(m_doc.importNode(configElement.getXmlElement(), true));
        }
        return this;
    }

    private boolean validateEditConfigElement(EditConfigElement configElement) throws NetconfMessageBuilderException {
        if (configElement != null) {
            if (configElement.getConfigElementContents() == null) {
                throw new NetconfMessageBuilderException("One or more <config> elements in <edit-config> request are null ");
            }
        } else {
            throw new NetconfMessageBuilderException("One or more <config> elements in <edit-config> request are null ");
        }
        return false;
    }

    public static boolean validateErrorOption(String errorOption) {
        if (EditConfigErrorOptions.CONTINUE_ON_ERROR.equals(errorOption)) {
            return true;
        }
        if (EditConfigErrorOptions.ROLLBACK_ON_ERROR.equals(errorOption)) {
            return true;
        }
        if (EditConfigErrorOptions.STOP_ON_ERROR.equals(errorOption)) {
            return true;
        }
        return false;
    }

    public static boolean validateTestOption(String testOption) {
        if (EditConfigTestOptions.SET.equals(testOption)) {
            return true;
        }
        if (EditConfigTestOptions.TEST_ONLY.equals(testOption)) {
            return true;
        }
        if (EditConfigTestOptions.TEST_THEN_SET.equals(testOption)) {
            return true;
        }
        return false;
    }

    public static boolean validateDefaultEditOpertation(String defaultOperation) {
        if (EditConfigDefaultOperations.MERGE.equals(defaultOperation)) {
            return true;
        }
        if (EditConfigDefaultOperations.NONE.equals(defaultOperation)) {
            return true;
        }
        if (EditConfigDefaultOperations.REPLACE.equals(defaultOperation)) {
            return true;
        }
        return false;
    }

    public Element newEditconfigElement(Collection<Element> configElementContents) throws NetconfMessageBuilderException {
        Document newDocument;
        try {
            newDocument = getNewDocument();
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
        Element configElement = newDocument.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.EDIT_CONFIG_CONFIG);
        newDocument.appendChild(configElement);

        for (Element configElementContent : configElementContents) {
            configElement.appendChild(newDocument.importNode(configElementContent, true));
        }
        return configElement;
    }

    public PojoToDocumentTransformer addCopyConfigElement(boolean withTransactionId, String source, boolean srcIsUrl, String target, boolean targetIsUrl,
                                                          Element config) throws NetconfMessageBuilderException {
        if (source == null || source.isEmpty()) {
            if (config == null) {
                throw new NetconfMessageBuilderException("<source> element not set for <copy-config>");
            }
        }
        if (target == null || target.isEmpty()) {
            throw new NetconfMessageBuilderException("<target> element not set for <copy-config>");
        }

        Node rpcNode = m_doc.getFirstChild();
        Element copyConfigElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.COPY_CONFIG);
        rpcNode.appendChild(copyConfigElement);

        Element targetElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DATA_TARGET);
        Element targetContentElement = null;
        if (targetIsUrl) {
            targetContentElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.URL);
            targetContentElement.setTextContent(target);
        } else {
            targetContentElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, target);
            targetElement.appendChild(targetContentElement);
        }
        copyConfigElement.appendChild(targetElement);
        
        Element sourceElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DATA_SOURCE);
        Element sourceContentElement = null;
        if (srcIsUrl) {
            sourceContentElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.URL);
            sourceContentElement.setTextContent(source);
        } else {
            if (source != null && !source.isEmpty()) {
                sourceContentElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, source);
            } else {
                sourceContentElement = (Element) m_doc.importNode(config, true);
            }
        }
        sourceElement.appendChild(sourceContentElement);        
        copyConfigElement.appendChild(sourceElement);

        if(withTransactionId){
            Element withTransactionIdElement = m_doc
                    .createElementNS(NetconfResources.TRANSACTION_ID_NS, NetconfResources.WITH_TRANSACTION_ID);
            copyConfigElement.appendChild(withTransactionIdElement);
        }

        return this;
    }

    /**
     * @param target
     * @return
     * @throws NetconfMessageBuilderException
     */
    public PojoToDocumentTransformer addDeleteConfigElement(String target) throws NetconfMessageBuilderException {
        if (target == null || target.isEmpty()) {
            throw new NetconfMessageBuilderException("<target> element not set for <delete-config>");
        }
        Node rpcNode = m_doc.getFirstChild();
        Element deleteConfigElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DELETE_CONFIG);
        Element targetElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DATA_TARGET);
        Element targetContent = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, target);
        targetElement.appendChild(targetContent);
        deleteConfigElement.appendChild(targetElement);
        rpcNode.appendChild(deleteConfigElement);

        return this;
    }

    public PojoToDocumentTransformer addActionElement(Element actionTreeElement) throws NetconfMessageBuilderException{
        if (actionTreeElement == null) {
            throw new NetconfMessageBuilderException("actionTargetElement element can not be null");
        }
        Node rpcNode = m_doc.getFirstChild();
        Element actionElement = m_doc.createElementNS(NetconfResources.NETCONF_YANG_1, NetconfResources.ACTION);
        rpcNode.appendChild(actionElement);
        actionElement.appendChild(m_doc.importNode(actionTreeElement, true));

        return this;
    }
    /**
     * @param target
     * @return
     * @throws NetconfMessageBuilderException
     */
    public PojoToDocumentTransformer addLockElement(String target) throws NetconfMessageBuilderException {
        if (target == null || target.isEmpty()) {
            throw new NetconfMessageBuilderException("<target> element not set for <lock>");
        }
        Node rpcNode = m_doc.getFirstChild();
        Element lockElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.LOCK);
        Element targetElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DATA_TARGET);
        Element targetContent = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, target);
        targetElement.appendChild(targetContent);
        lockElement.appendChild(targetElement);
        rpcNode.appendChild(lockElement);
        return this;
    }

    /**
     * @param target
     * @return
     * @throws NetconfMessageBuilderException
     */
    public PojoToDocumentTransformer addUnLockElement(String target) throws NetconfMessageBuilderException {
        if (target == null || target.isEmpty()) {
            throw new NetconfMessageBuilderException("<target> element not set for <unlock>");
        }
        Node rpcNode = m_doc.getFirstChild();
        Element unlockElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.UNLOCK);
        Element targetElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.DATA_TARGET);
        Element targetContent = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, target);
        targetElement.appendChild(targetContent);
        unlockElement.appendChild(targetElement);
        rpcNode.appendChild(unlockElement);
        return this;
    }

    /**
     * @param filter
     * @return
     * @throws NetconfMessageBuilderException
     */
    public PojoToDocumentTransformer addGetElement(NetconfFilter filter, String sliceOwner, WithDefaults withDefaults, int withDelay,
                                                   int depth,
                                                   Map<String, List<QName>> fieldValues)
            throws NetconfMessageBuilderException {
        // Add it right after "<rpc>"
        Node rpcNode = m_doc.getFirstChild();
        if (rpcNode == null) {
            throw new NetconfMessageBuilderException(RPC_ELEMENT_IS_NULL_CREATE_THE_RPC_ELEMENT_FIRST);
        }
        Element getElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.GET);
        rpcNode.appendChild(getElement);
        if (filter != null) {
            Element filterElementWrapper = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.FILTER);
            if (filter.getType() != null && !filter.getType().isEmpty()) {
                filterElementWrapper.setAttribute(NetconfResources.TYPE, filter.getType());
            }
            List<Element> xmlFilterElements = filter.getXmlFilterElements();
            if (xmlFilterElements != null) {
                for (Element xmlFilterElement : xmlFilterElements) {
                    filterElementWrapper.appendChild(m_doc.importNode(xmlFilterElement, true));
                }
            }
            getElement.appendChild(filterElementWrapper);
        }
        if (withDefaults != null) {
            Element withDefaultElement = m_doc.createElementNS(NetconfResources.WITH_DEFAULTS_NS, NetconfResources.WITH_DEFAULTS);
            withDefaultElement.setTextContent(withDefaults.getValue());
            getElement.appendChild(withDefaultElement);
        }

        addWithDelayElement(getElement, withDelay);
        
        addDepthElement(getElement, depth);
        if(sliceOwner!=null) {
            addWithSliceOwnerElement(getElement, sliceOwner);
        }
        addFieldElements(getElement, fieldValues);
        return this;
    }

    public PojoToDocumentTransformer addRpcElement(Element rpcElement) throws NetconfMessageBuilderException {
        Node rpcNode = m_doc.getFirstChild();
        if (rpcNode == null) {
            throw new NetconfMessageBuilderException(RPC_ELEMENT_IS_NULL_CREATE_THE_RPC_ELEMENT_FIRST);
        }
        if (rpcElement != null) {
            Node importedNode = m_doc.importNode(rpcElement, true);
            rpcNode.appendChild(importedNode);
        } else {
            throw new NetconfMessageBuilderException("rpc element is empty/null");
        }
        return this;
    }

    /**
     * 
     * @return
     * @throws NetconfMessageBuilderException
     */
    public PojoToDocumentTransformer addCloseSessionElement() throws NetconfMessageBuilderException {
        // Add it right after "<rpc>"
        Node rpcNode = m_doc.getFirstChild();
        if (rpcNode == null) {
            throw new NetconfMessageBuilderException(RPC_ELEMENT_IS_NULL_CREATE_THE_RPC_ELEMENT_FIRST);
        }
        Element closeSessionElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.CLOSE_SESSION);
        rpcNode.appendChild(closeSessionElement);
        return this;
    }

    public PojoToDocumentTransformer addKillSessionElement(Integer sessionId) throws NetconfMessageBuilderException {
        Node rpcNode = m_doc.getFirstChild();
        if (rpcNode == null) {
            throw new NetconfMessageBuilderException(RPC_ELEMENT_IS_NULL_CREATE_THE_RPC_ELEMENT_FIRST);
        }
        Element killSessionElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.KILL_SESSION);
        addSessionId(sessionId, killSessionElement);
        rpcNode.appendChild(killSessionElement);
        return this;
    }

    public PojoToDocumentTransformer newServerHelloMessage(Set<String> caps, Integer i) throws NetconfMessageBuilderException {
        createDocument();
        Element helloElement = createHelloElement();
        addCapabilities(helloElement, caps);
        addSessionId(i, helloElement);
        m_doc.appendChild(helloElement);
        return this;
    }
    
    public PojoToDocumentTransformer newClientHelloMessage(Set<String> caps) throws NetconfMessageBuilderException {
        createDocument();
        Element helloElement = createHelloElement();
        addCapabilities(helloElement, caps);
        m_doc.appendChild(helloElement);
        return this;
    }

    private Element createHelloElement() {
        return m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.HELLO);
    }

    private void createDocument() throws NetconfMessageBuilderException {
        try {
            m_doc = getNewDocument();
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException("Errow while building hello message", e);
        }
    }


    private void addSessionId(Integer i, Element helloElement) {
        Element sessionId = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.SESSION_ID);
        sessionId.setTextContent(String.valueOf(i));
        helloElement.appendChild(sessionId);
    }

    private void addCapabilities(Element helloElement, Set<String> caps) {
        Element capabilitesWrapper = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.CAPABILITIES);
        helloElement.appendChild(capabilitesWrapper);

        for (String capability : caps) {
            Element capabilityElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.CAPABILITY);
            capabilityElement.setTextContent(capability);
            capabilitesWrapper.appendChild(capabilityElement);
        }
    }

    public PojoToDocumentTransformer newNetconfRpcReplyDocument(String messageId, Map<String, String> additionalAttributes) {
        try {
            m_doc = getNewDocument();
            // create the root element rpc-reply
            Element rpcReply = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_REPLY);
            for (String key : additionalAttributes.keySet()) {
                rpcReply.setAttribute(key, additionalAttributes.get(key));
            }
            if (messageId != null) {
                rpcReply.setAttribute(NetconfResources.MESSAGE_ID, messageId);
            } else {
                try {
                    throw new Exception("message-id is not set ");
                }catch (Exception e) {
                    LOGGER.warn("", e);
                }
            }
            m_doc.appendChild(rpcReply);
            return this;
        } catch (Exception e) {
            LOGGER.error(ERROR_WHILE_BUILDING_DOCUMENT, e);
            throw new RuntimeException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }

    public PojoToDocumentTransformer addOk() throws NetconfMessageBuilderException {
        Node rpcReplyNode = m_doc.getFirstChild();
        if (rpcReplyNode == null) {
            throw new NetconfMessageBuilderException("<rpc-reply> Element is null, create the rpc element first");
        }
        Element okElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OK);
        rpcReplyNode.appendChild(okElement);
        return this;
    }

    public PojoToDocumentTransformer addTxId(String txId) throws NetconfMessageBuilderException {
        Node rpcReplyNode = m_doc.getFirstChild();
        if (rpcReplyNode == null) {
            throw new NetconfMessageBuilderException("<rpc-reply> Element is null, create the rpc element first");
        }
        Element txIdElement = m_doc.createElementNS(NetconfResources.TRANSACTION_ID_NS, NetconfResources.TRANSACTION_ID);
        txIdElement.setTextContent(txId);
        rpcReplyNode.appendChild(m_doc.importNode(txIdElement, true));
        return this;
    }

    public PojoToDocumentTransformer addData(Element data) throws NetconfMessageBuilderException {
        Node rpcReplyNode = m_doc.getFirstChild();
        if (rpcReplyNode == null) {
            throw new NetconfMessageBuilderException("<rpc-reply> Element is null, create the rpc element first");
        }
        if (data != null) {
            rpcReplyNode.appendChild(m_doc.importNode(data, true));
        }
        return this;
    }
    
	public PojoToDocumentTransformer addNcExtensionsResponses(Map<QName, List<Element>> ncExtensionResponses)
			throws NetconfMessageBuilderException {
		Node rpcReplyNode = m_doc.getFirstChild();
        if (rpcReplyNode == null) {
            throw new NetconfMessageBuilderException("<rpc-reply> Element is null, create the rpc element first");
        }
        if (ncExtensionResponses != null && !ncExtensionResponses.isEmpty()) {
        	for (Entry<QName, List<Element>> entry : ncExtensionResponses.entrySet()) {
				Element ncExtensionElement = m_doc.createElementNS(NetconfResources.EXTENSION_NS,
						String.format(NetconfResources.RPC_REPLY_EXTENSION_RESULT, entry.getKey().getLocalName()));
        		if (entry.getValue() != null) {
					for (Element element : entry.getValue()) {
						ncExtensionElement.appendChild(m_doc.importNode(element, true));
					}
					rpcReplyNode.appendChild(m_doc.importNode(ncExtensionElement, true));
				}
			}
        }
        return this;
	}

    public PojoToDocumentTransformer addRpcErrors(List<NetconfRpcError> rpcErrors) throws NetconfMessageBuilderException {
        for (NetconfRpcError rpcError : rpcErrors) {
            addRpcError(rpcError);
        }
        return this;
    }

    public PojoToDocumentTransformer addRpcError(NetconfRpcError rpcError) throws NetconfMessageBuilderException {
        Node rpcReplyNode = m_doc.getFirstChild();
        if (rpcReplyNode == null) {
            throw new NetconfMessageBuilderException("<rpc-reply> Element is null, create the rpc element first");
        }
        if (rpcError != null) {
            Element rpcErrorElement = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR);
            if (rpcError.getErrorType() != null) {
                Element errorType = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_TYPE);
                errorType.setTextContent(rpcError.getErrorType().value());
                rpcErrorElement.appendChild(errorType);
            }
            if (rpcError.getErrorTag() != null) {
                Element errorTag = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_TAG);
                errorTag.setTextContent(rpcError.getErrorTag().value());
                rpcErrorElement.appendChild(errorTag);
            }
            if (rpcError.getErrorSeverity() != null) {
                Element errorSeverity = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_SEVERITY);
                errorSeverity.setTextContent(rpcError.getErrorSeverity().value());
                rpcErrorElement.appendChild(errorSeverity);
            }
            if (rpcError.getErrorAppTag() != null) {
                Element errorAppTag = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_APP_TAG);
                errorAppTag.setTextContent(rpcError.getErrorAppTag());
                rpcErrorElement.appendChild(errorAppTag);
            }
            if (rpcError.getErrorPathElement() != null) {
                Node errorPathNode = m_doc.importNode(rpcError.getErrorPathElement(), true);
                rpcErrorElement.appendChild(errorPathNode);
            }
            else if (rpcError.getErrorPath() != null) {
                Element errorPath = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_PATH);
                errorPath.setTextContent(rpcError.getErrorPath());
                Map<String, String> errorPathNsByPrefix = rpcError.getErrorPathNsByPrefix();
                if (errorPathNsByPrefix != null) {
                    for (String prefix : errorPathNsByPrefix.keySet()) {
                        errorPath.setAttributeNS(XMLNS_NAMESPACE, XMLNS + prefix, errorPathNsByPrefix.get(prefix));
                    }
                }
                rpcErrorElement.appendChild(errorPath);
            }
            if (rpcError.getErrorMessage() != null) {
                Element errorMessage = m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_MESSAGE);
                errorMessage.setTextContent(rpcError.getErrorMessage());
                rpcErrorElement.appendChild(errorMessage);
            }
            if (rpcError.getErrorInfo() != null) {
                Node errorInfoNode = m_doc.importNode(rpcError.getErrorInfo(), true);
                rpcErrorElement.appendChild(errorInfoNode);
            }
            rpcReplyNode.appendChild(rpcErrorElement);
        }
        return this;
    }

    public static String responseToString(NetConfResponse response) {
        try {

            Document responseDoc = response.getResponseDocument();
            return prettyPrint(responseDoc);
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String requestToString(Document document) {
        return prettyPrint(document);
    }

    public static String notificationToString(Notification notification) {
        try {
            Document document = notification.getNotificationDocument();
            return DocumentUtils.documentToString(document);
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String notificationToPrettyString(Notification notification) {
        try {
            Document requestDoc = notification.getNotificationDocument();
            return prettyPrint(requestDoc);
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrint(Collection<Element> elements) {
        StringBuffer buffer = new StringBuffer();
        for (Element element : elements) {
            buffer.append(prettyPrint(element));
            buffer.append(NEW_LINE);
        }
        return buffer.toString();
    }

    public static String prettyPrint(Node element) {
        Source xmlInput = new DOMSource(element);
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StreamResult xmlOutput = new StreamResult(new StringWriter());

            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException(e);
        }

    }

    public PojoToDocumentTransformer newNetconfNotificationDocument(String eventTime, Element notificationContent)
            throws NetconfMessageBuilderException {
        try {
            m_doc = getNewDocument();

            // create the root element notification
            Element notificationElement = m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS, NetconfResources.NOTIFICATION);
            Element eventTimeElement = m_doc.createElementNS(NetconfResources.NETCONF_NOTIFICATION_NS, NetconfResources.EVENT_TIME);
            eventTimeElement.setTextContent(eventTime);
            notificationElement.appendChild(eventTimeElement);
            if (notificationContent != null) {
                notificationElement.appendChild(m_doc.importNode(notificationContent, true));
            }
            m_doc.appendChild(notificationElement);
            return this;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }

    public Element getConfigChangeNotificationElement(String datastore, List<EditInfo> editList, ChangedByParams changedByParams)
            throws NetconfMessageBuilderException {
        try {
            m_doc = getNewDocument();

            Element netconfConfigChangeElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS,
                    NetconfResources.CONFIG_CHANGE_NOTIFICATION);

            Element dataStoreElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.DATA_STORE);
            dataStoreElement.setTextContent(datastore);
            netconfConfigChangeElement.appendChild(dataStoreElement);

            if (changedByParams != null) {
                SessionInfo sessionInfo = changedByParams.getCommonSessionParams();
                Element changeBy = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.CHANGED_BY);
                Element userNameElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.USER_NAME);
                userNameElement.setTextContent(sessionInfo.getUserName());
                changeBy.appendChild(userNameElement);

                Element sessionIdElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.SESSION_ID);
                sessionIdElement.setTextContent(String.valueOf(sessionInfo.getSessionId()));
                changeBy.appendChild(sessionIdElement);

                Element sourcehostElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.SOURCE_HOST);
                sourcehostElement.setTextContent(sessionInfo.getSourceHostIpAddress());
                changeBy.appendChild(sourcehostElement);

                netconfConfigChangeElement.appendChild(changeBy);
            }

            for (EditInfo editInfo : editList) {
                Element editInfoElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.EDIT);
                if(editInfo.isImplied()){
                    Element impliedElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.IMPLIED);
                    editInfoElement.appendChild(impliedElement);
                }
                Element targetElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.TARGET);
                Map<String, String> namespaceDeclareMap = editInfo.getNamespaceDeclareMap();
                if (namespaceDeclareMap != null) {
                    for (String prefix : namespaceDeclareMap.keySet()) {
                        targetElement.setAttributeNS(XMLNS_NAMESPACE, XMLNS + prefix, namespaceDeclareMap.get(prefix));
                    }
                }
                targetElement.setTextContent(editInfo.getTarget());
                editInfoElement.appendChild(targetElement);

                Element operationElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.OPERATION);
                operationElement.setTextContent(editInfo.getOperation());
                editInfoElement.appendChild(operationElement);
                
                List<ChangedLeafInfo> changedLeafInfos = editInfo.getChangedLeafInfos();
                for (ChangedLeafInfo changedLeafInfo : changedLeafInfos) {
                    Element changedLeafElement = m_doc
                            .createElementNS(NetconfResources.NC_STACK_NS, NetconfResources.CHANGED_LEAF);
                    Element leafElement = m_doc.createElementNS(changedLeafInfo.getNamespace(), changedLeafInfo.getName());
                    leafElement.setPrefix(changedLeafInfo.getPrefix());
                    leafElement.setTextContent(changedLeafInfo.getChangedValue());
                    changedLeafElement.appendChild(leafElement);
                    editInfoElement.appendChild(changedLeafElement);
                }

                netconfConfigChangeElement.appendChild(editInfoElement);
            }

            return netconfConfigChangeElement;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }

    public Element getStateChangeNotificationElement(String target, String value) throws NetconfMessageBuilderException {
        try {
            m_doc = getNewDocument();
            Element stateChangeNotificationElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS,
                    NetconfResources.STATE_CHANGE_NOTIFICATION);

            Element targetElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.TARGET);
            targetElement.setTextContent(target);
            stateChangeNotificationElement.appendChild(targetElement);

            Element valueElement = m_doc.createElementNS(NetconfResources.IETF_NOTIFICATION_NS, NetconfResources.STATE_CHANGE_VALUE);
            valueElement.setTextContent(value);
            stateChangeNotificationElement.appendChild(valueElement);

            return stateChangeNotificationElement;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }

    public Element newReplayCompleteElement() throws NetconfMessageBuilderException {
        try {
            m_doc = getNewDocument();
            Element replyCompleteElement = m_doc.createElementNS(NetconfResources.NC_NOTIFICATION_NS, NetconfResources.REPLAY_COMPLETE);

            return replyCompleteElement;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }

    public Element newNotificationCompleteElement() throws NetconfMessageBuilderException {
        try {
            m_doc = getNewDocument();
            Element replyCompleteElement = m_doc.createElementNS(NetconfResources.NC_NOTIFICATION_NS,
                    NetconfResources.NOTIFICATION_COMPLETE);

            return replyCompleteElement;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }
    
    public Element getStateChangeNotificationElement(List<StateChangeInfo> changesList) throws NetconfMessageBuilderException {

        try {
            m_doc = getNewDocument();

            Element stateChangeElement = m_doc.createElementNS(NetconfResources.NC_STACK_NS,
                    NetconfResources.NC_STATE_CHANGE_NOTIFICATION);
            
            for (StateChangeInfo stateChangeInfo : changesList) {
                Element changesInfoElement = m_doc.createElementNS(NetconfResources.NC_STACK_NS, NetconfResources.CHANGES);

                Element targetElement = m_doc.createElementNS(NetconfResources.NC_STACK_NS, NetconfResources.TARGET);
                Map<String, String> namespaceDeclareMap = stateChangeInfo.getNamespaceDeclareMap();
                if (namespaceDeclareMap != null) {
                    for (String prefix : namespaceDeclareMap.keySet()) {
                        targetElement.setAttributeNS(XMLNS_NAMESPACE, XMLNS + prefix, namespaceDeclareMap.get(prefix));
                    }
                }
                targetElement.setTextContent(stateChangeInfo.getTarget());
                changesInfoElement.appendChild(targetElement);

                List<ChangedLeafInfo> changedLeafInfos = stateChangeInfo.getChangedLeafInfos();
                int changedLeafItem = 1;
                for (ChangedLeafInfo changedLeafInfo : changedLeafInfos) {

                    // output
                    // <changed-leaf>
                    //   <item>1</item>
                    //   <value>
                    //      <swmgmt:software-targets-aligned xmlns:swmgmt="http://www.test-company.com/solutions/anv-software">false</swmgmt:software-targets-aligned>
                    //   </value>
                    // </changed-leaf>

                    // create item
                    Element changedLeafItemElement = m_doc.createElementNS(NetconfResources.NC_STACK_NS, NetconfResources.ITEM);
                    changedLeafItemElement.setTextContent(String.valueOf(changedLeafItem));
                    changedLeafItem++;

                    //create anyxml content
                    Element changeValuesAnyXml = m_doc.createElementNS(changedLeafInfo.getNamespace(), changedLeafInfo.getName());
                    changeValuesAnyXml.setPrefix(changedLeafInfo.getPrefix());
                    changeValuesAnyXml.setTextContent(changedLeafInfo.getChangedValue());

                    // create value and append anyxml element into value element
                    Element changedLeafValueElement = m_doc.createElementNS(NetconfResources.NC_STACK_NS, NetconfResources.VALUE);
                    changedLeafValueElement.appendChild(changeValuesAnyXml);

                    //append item and value into changed-leaf element
                    Element changedLeafElement = m_doc.createElementNS(NetconfResources.NC_STACK_NS, NetconfResources.CHANGED_LEAF);
                    changedLeafElement.appendChild(changedLeafItemElement);
                    changedLeafElement.appendChild(changedLeafValueElement);
                    changesInfoElement.appendChild(changedLeafElement);
                }

                stateChangeElement.appendChild(changesInfoElement);
            }

            return stateChangeElement;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }

    }
}
