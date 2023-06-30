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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.validation.xsd.XmlValidator;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A utility class to transform a netconf {@link Document} to a netconf message.
 *
 * 
 */
public class DocumentToPojoTransformer {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DocumentToPojoTransformer.class, LogAppNames.NETCONF_LIB);
    static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    static final String LF = "\n";
    static final String HASH = "#";
    protected static final String SLICE_OWNER = "slice-owner";
    static final int MAX_CHUNK_SIZE = Integer.MAX_VALUE;// FIXME: FNMS-6877 this is no good, RFC 6242 says max chunk size can be as long as
    // 4294967295,
    // but this would mean we cannot use String, need to revisit this.
    static final Pattern CHUNK_SIZE_PATTERN = Pattern.compile("\n#[0-9]+\n");
    static final String EOM_CHUNK = "\n##\n";
    static final Pattern CHUNK_SIZE_HASH = Pattern.compile("#");
    private static final String EMPTY_STR = "";

    public static CloseSessionRequest getCloseSession(Document request) {
        CloseSessionRequest axsCloseSessionRequest = new CloseSessionRequest();
        axsCloseSessionRequest.setMessageId(DocumentUtils.getInstance().getMessageIdFromRpcDocument(request));
        return axsCloseSessionRequest;
    }

    private static int getWithDelayFromNode(Node delayNode) throws NetconfMessageBuilderException {
        String delayOption = delayNode.getTextContent();
        try {
            int delay = Integer.valueOf(delayOption);
            if (delay < 0 || delay > 255) {
                throw new NetconfMessageBuilderException("The value of with-delay \"" + delayOption
                        + "\" is not valid. Expected length is [0...255].");
            }
            return delay;
        } catch (NumberFormatException e) {
            throw new NetconfMessageBuilderException("The value of with-delay \"" + delayOption
                    + "\" is not valid. Expected length is [0...255].");
        }
    }
    
    private static int getDepthFromNode(Node depthNode) throws NetconfMessageBuilderException {
        String depthOption = depthNode.getTextContent();
        try {
            int delay = Integer.valueOf(depthOption);
            if (delay < 1 || delay > 65535) {
                throw new NetconfMessageBuilderException("The value of depth \"" + depthOption
                        + "\" is not valid. Expected length is [1...65535].");
            }
            return delay;
        } catch (NumberFormatException e) {
            throw new NetconfMessageBuilderException("The value of depth \"" + depthOption
                    + "\" is not valid. Expected length is [1...65535].");
        }
    }

    public static EditConfigRequest getEditConfig(Document request) throws NetconfMessageBuilderException {
        try {
            XmlValidator.validateXmlMessage(new DOMSource(request));
        } catch (Exception e) {
            throw new NetconfMessageBuilderException(e.getMessage());
        }
        EditConfigRequest editConfigRequest = new EditConfigRequest();
        DocumentUtils docUtils = DocumentUtils.getInstance();
        editConfigRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(request));
        Element rpcNode = request.getDocumentElement();
        editConfigRequest.setRpcElement(rpcNode);
        Element editConfig = docUtils.getDirectChildElement(rpcNode, NetconfResources.EDIT_CONFIG, NetconfResources.NETCONF_RPC_NS_1_0);
        Node defaultOp = docUtils.getDirectChildElement(editConfig, NetconfResources.DEFAULT_OPERATION, NetconfResources.NETCONF_RPC_NS_1_0);
        if (defaultOp != null) {
            editConfigRequest.setDefaultOperation(defaultOp.getTextContent());
        }
        Node errorOption = docUtils.getDirectChildElement(editConfig, NetconfResources.ERROR_OPTION, NetconfResources.NETCONF_RPC_NS_1_0);
        if (errorOption != null) {
            editConfigRequest.setErrorOption(errorOption.getTextContent());
        }
        Node testOption = docUtils.getDirectChildElement(editConfig, NetconfResources.TEST_OPTION, NetconfResources.NETCONF_RPC_NS_1_0);
        if (testOption != null) {
            editConfigRequest.setTestOption(testOption.getTextContent());
        }

        Node withDelayOption = docUtils.getDirectChildElement(editConfig, NetconfResources.WITH_DELAY, NetconfResources.WITH_DELAY_NS);
        if (withDelayOption != null) {
            editConfigRequest.setWithDelay(getWithDelayFromNode(withDelayOption));
        }

        Node target = docUtils.getDirectChildElement(editConfig, NetconfResources.DATA_TARGET, NetconfResources.NETCONF_RPC_NS_1_0);
        String tar = docUtils.getFirstElementChildNode(target).getLocalName();
        editConfigRequest.setTarget(tar);

        Node config = docUtils.getDirectChildElement(editConfig, NetconfResources.EDIT_CONFIG_CONFIG, NetconfResources.NETCONF_RPC_NS_1_0);
        List<Element> configElements = new ArrayList<>();
        NodeList configChildren = config.getChildNodes();
        for (int i = 0; i < configChildren.getLength(); i++) {
            Node configChild = configChildren.item(i);
            if (configChild.getNodeType() == Node.ELEMENT_NODE) {
                configElements.add((Element) configChild);
            }
        }
        if (configElements.isEmpty()) {
            throw new NetconfMessageBuilderException("edit-config request is empty not valid.");
        }
        EditConfigElement editConfigElement = new EditConfigElement();
        editConfigRequest.setConfigElement(editConfigElement);
        editConfigElement.setConfigElementContents(configElements);
        Node triggerSyncUponSuccess = DocumentUtils.getDirectChildElement(editConfig, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS, NetconfResources.EXTENSION_NS);
        if (triggerSyncUponSuccess != null) {
            editConfigRequest.setTriggerSyncUponSuccess(Boolean.parseBoolean(triggerSyncUponSuccess.getTextContent()));
        }
        String userContextFromRpcDocument = docUtils.getUserContextFromRpcDocument(rpcNode);
        editConfigRequest.setUserContext(userContextFromRpcDocument);
        String sessionIdFromRpcDocument = docUtils.getSessionIdFromRpcDocument(rpcNode);
        editConfigRequest.setContextSessionId(sessionIdFromRpcDocument);
        String application = docUtils.getApplicationFromRpcDocument(rpcNode);
        editConfigRequest.setApplication(application);
        Node forceInstanceCreation = DocumentUtils.getDirectChildElement(editConfig, NetconfResources.FORCE_INSTANCE_CREATION, NetconfResources.EXTENSION_NS);
        if (forceInstanceCreation != null) {
            editConfigRequest.setForceInstanceCreation(Boolean.parseBoolean(forceInstanceCreation.getTextContent()));
        }
        return editConfigRequest;

    }

    public static CopyConfigRequest getCopyConfig(Document document) throws NetconfMessageBuilderException {
        CopyConfigRequest axsCopyConfigRequest = new CopyConfigRequest();
        DocumentUtils docUtils = DocumentUtils.getInstance();

        axsCopyConfigRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(document));
        Node rpcNode = docUtils.getChildNodeByName(document, NetconfResources.RPC, NetconfResources.NETCONF_RPC_NS_1_0);
        Node source = docUtils.getChildNodeByName(rpcNode, NetconfResources.DATA_SOURCE, NetconfResources.NETCONF_RPC_NS_1_0);
        if (source == null) {
            throw new NetconfMessageBuilderException("<source> cannot be null/empty");
        }
        boolean sourceIsUrl = true;
        String src = EMPTY_STR;
        Node srcUrlNode = docUtils.getChildNodeByName(source, NetconfResources.URL, NetconfResources.NETCONF_RPC_NS_1_0);
        if (srcUrlNode == null) {
            sourceIsUrl = false;
            Node sourceStore = docUtils.getFirstElementChildNode(source);
            if (sourceStore == null) {
                throw new NetconfMessageBuilderException("<source> cannot be null/empty");
            }
            if (NetconfResources.COPY_CONFIG_SRC_CONFIG.equals(sourceStore.getNodeName())) {
                axsCopyConfigRequest.setSourceConfigElement(docUtils.getElementByName(document, NetconfResources.COPY_CONFIG_SRC_CONFIG));
            } else {
                src = sourceStore.getLocalName();
            }
        } else {
            src = srcUrlNode.getTextContent();
            if (EMPTY_STR.equals(src)) {
                throw new NetconfMessageBuilderException("source <url> cannot be null/empty");
            }
        }
        axsCopyConfigRequest.setSource(src, sourceIsUrl);

        Node target = docUtils.getChildNodeByName(rpcNode, NetconfResources.DATA_TARGET, NetconfResources.NETCONF_RPC_NS_1_0);
        if (target == null) {
            throw new NetconfMessageBuilderException("<target> cannot be null/empty");
        }
        boolean targetIsUrl = true;
        String tar = EMPTY_STR;
        Node tarUrlNode = docUtils.getChildNodeByName(target, NetconfResources.URL, NetconfResources.NETCONF_RPC_NS_1_0);
        if (tarUrlNode == null) {
            targetIsUrl = false;
            Node targetStore = docUtils.getFirstElementChildNode(target);
            if (targetStore == null) {
                throw new NetconfMessageBuilderException("<target> cannot be null/empty");
            }
            tar = targetStore.getLocalName();
        } else {
            tar = tarUrlNode.getTextContent();
            if (EMPTY_STR.equals(tar)) {
                throw new NetconfMessageBuilderException("target <url> cannot be null/empty");
            }
        }
        axsCopyConfigRequest.setTarget(tar, targetIsUrl);

        return axsCopyConfigRequest;
    }

    public static GetRequest getGet(Document document) throws NetconfMessageBuilderException {
        GetRequest getRequest = new GetRequest();
        DocumentUtils docUtils = DocumentUtils.getInstance();

        getRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(document));
        getRequest.setRpcElement(document.getDocumentElement());
        getRequest.setFilter(getFilterFromRpcDocument(document));

        Node withDelayOption = DocumentUtils.getChildNodeByName(document.getDocumentElement(), NetconfResources.WITH_DELAY,
                NetconfResources.WITH_DELAY_NS);
        Node sliceOwner = DocumentUtils.getChildNodeByName(document.getDocumentElement(), SLICE_OWNER ,
                NetconfResources.NETCONF_RPC_NS_1_0);
        if(sliceOwner!=null) {
            getRequest.setSliceOwner(sliceOwner.getTextContent());
        }
        if (withDelayOption != null) {
            getRequest.setWithDelay(getWithDelayFromNode(withDelayOption));
        }
        
        Node depthElement = DocumentUtils.getChildNodeByName(document.getDocumentElement(), NetconfResources.DEPTH,
                NetconfResources.EXTENSION_NS);
        if (depthElement != null) {
            getRequest.setDepth(getDepthFromNode(depthElement));
        
        }

        Node withDefaultsNode = DocumentUtils.getChildNodeByName(document.getDocumentElement(), NetconfResources.WITH_DEFAULTS,
            NetconfResources.WITH_DEFAULTS_NS);
        if (withDefaultsNode != null) {
            for(WithDefaults withDefaults : WithDefaults.values()) {
                if(withDefaults.getValue().equals(withDefaultsNode.getTextContent())) {
                    getRequest.setWithDefaults(withDefaults);
                }
            }
        }
        
        return getRequest;
    }

    public static ActionRequest getAction(Document request) throws NetconfMessageBuilderException {
        try {
            XmlValidator.validateXmlMessage(new DOMSource(request));
        } catch (Exception e) {
            throw new NetconfMessageBuilderException(e.getMessage());
        }
        ActionRequest actionRequest = new ActionRequest();
        DocumentUtils docUtils = DocumentUtils.getInstance();
        Element rpcNode = request.getDocumentElement();
        Node actionNode = docUtils.getChildNodeByName(request, NetconfResources.ACTION, NetconfResources.NETCONF_YANG_1);
        if (actionNode == null) {
            throw new NetconfMessageBuilderException("<action> cannot be null");
        }  
        Element actionTreeElement = docUtils.getFirstElementChildNode(actionNode);

        if (actionTreeElement == null) {
            throw new NetconfMessageBuilderException("action tree element cannot be null");
        }        
        actionRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(request));
        actionRequest.setActionTreeElement(actionTreeElement);
        
        String userContextFromRpcDocument = docUtils.getUserContextFromRpcDocument(rpcNode);
        actionRequest.setUserContext(userContextFromRpcDocument);
        String sessionIdFromRpcDocument = docUtils.getSessionIdFromRpcDocument(rpcNode);
        actionRequest.setContextSessionId(sessionIdFromRpcDocument);
        String application = docUtils.getApplicationFromRpcDocument(rpcNode);
        actionRequest.setApplication(application);
        List<Element> forceInstanceCreationElement = DocumentUtils.getDirectChildElements(actionNode, NetconfResources.FORCE_INSTANCE_CREATION, NetconfResources.EXTENSION_NS);
        if (forceInstanceCreationElement != null && forceInstanceCreationElement.size() == 1) {
            actionRequest.setForceInstanceCreation(Boolean.parseBoolean(forceInstanceCreationElement.get(0).getTextContent()));
        }
        return actionRequest;
    }
    
    public static DeleteConfigRequest getDeleteConfig(Document document) throws NetconfMessageBuilderException {
        DeleteConfigRequest deleteConfigRequest = new DeleteConfigRequest();
        DocumentUtils docUtils = DocumentUtils.getInstance();

        deleteConfigRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(document));
        Node rpcNode = docUtils.getChildNodeByName(document, NetconfResources.RPC, NetconfResources.NETCONF_RPC_NS_1_0);
        Node target = docUtils.getChildNodeByName(rpcNode, NetconfResources.DATA_TARGET, NetconfResources.NETCONF_RPC_NS_1_0);
        if (target == null) {
            throw new NetconfMessageBuilderException("<target> cannot be null/empty");
        }
        String tar = docUtils.getFirstElementChildNode(target).getLocalName();
        deleteConfigRequest.setTarget(tar);
        return deleteConfigRequest;
    }

    public static GetConfigRequest getGetConfig(Document request) throws NetconfMessageBuilderException {
        DocumentUtils docUtils = DocumentUtils.getInstance();

        GetConfigRequest getConfigRequest = new GetConfigRequest();
        getConfigRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(request));
        getConfigRequest.setRpcElement(request.getDocumentElement());

        getConfigRequest.setSource(docUtils.getSourceFromRpcDocument(request));
        getConfigRequest.setFilter(getFilterFromRpcDocument(request));

        Node withDelayOption = DocumentUtils.getChildNodeByName(request.getDocumentElement(), NetconfResources.WITH_DELAY,
                NetconfResources.WITH_DELAY_NS);
        if (withDelayOption != null) {
            getConfigRequest.setWithDelay(getWithDelayFromNode(withDelayOption));
        }
        
        Node depthElement = DocumentUtils.getChildNodeByName(request.getDocumentElement(), NetconfResources.DEPTH,
                NetconfResources.EXTENSION_NS);
        if (depthElement != null) {
            getConfigRequest.setDepth(getDepthFromNode(depthElement));
        
        }

        Node withDefaultsNode = DocumentUtils.getChildNodeByName(request.getDocumentElement(), NetconfResources.WITH_DEFAULTS,
            NetconfResources.WITH_DEFAULTS_NS);
        if (withDefaultsNode != null) {
            for(WithDefaults withDefaults : WithDefaults.values()) {
                if(withDefaults.getValue().equals(withDefaultsNode.getTextContent())) {
                    getConfigRequest.setWithDefaults(withDefaults);
                }
            }
        }

        return getConfigRequest;
    }

    public static NetconfRpcRequest getRpcRequest(Document request) throws NetconfMessageBuilderException {
        DocumentUtils docUtils = DocumentUtils.getInstance();

        NetconfRpcRequest rpcRequest = new NetconfRpcRequest();
        rpcRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(request));

        Node rpcNode = docUtils.getChildNodeByName(request, NetconfResources.RPC, NetconfResources.NETCONF_RPC_NS_1_0);
        Element rpcInput = docUtils.getFirstElementChildNode(rpcNode);

        if (rpcInput.getNamespaceURI() == null) {
            throw new NetconfMessageBuilderException("RPC namespace can not be null");
        }
        
        rpcRequest.setRpcInput(rpcInput);
        String userContextFromRpcDocument = docUtils.getUserContextFromRpcDocument(rpcNode);
        rpcRequest.setUserContext(userContextFromRpcDocument);
        String sessionIdFromRpcDocument = docUtils.getSessionIdFromRpcDocument(rpcNode);
        rpcRequest.setContextSessionId(sessionIdFromRpcDocument);
        String application = docUtils.getApplicationFromRpcDocument(rpcNode);
        rpcRequest.setApplication(application);
        return rpcRequest;
    }

    public static CreateSubscriptionRequest getCreateSubscriptionRequest(Document request) throws NetconfMessageBuilderException {
        DocumentUtils docUtils = DocumentUtils.getInstance();

        NetconfRpcRequest netconfRpcRequest = new NetconfRpcRequest();
        netconfRpcRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(request));

        Node rpcNode = docUtils.getChildNodeByName(request, NetconfResources.RPC, NetconfResources.NETCONF_RPC_NS_1_0);
        Element subscriptionElement = docUtils.getFirstElementChildNode(rpcNode);
        docUtils.getFirstElementChildNode(subscriptionElement);

        if (subscriptionElement.getNamespaceURI() == null) {
            throw new NetconfMessageBuilderException("subscription namespace can not be null");
        }
        netconfRpcRequest.setRpcInput(subscriptionElement);

        try {
            return docUtils.getSubscriptionRequest(netconfRpcRequest);
        } catch (DOMException | ParseException e) {
            LOGGER.error("Error while parsing date", e);
        }
        return null;
    }

    public static KillSessionRequest getKillSession(Document request) throws NetconfMessageBuilderException {
        KillSessionRequest killSessionReq = new KillSessionRequest();
        DocumentUtils docUtils = DocumentUtils.getInstance();
        killSessionReq.setMessageId(docUtils.getMessageIdFromRpcDocument(request));

        Node sessionId = docUtils.getElementByName(request, NetconfResources.SESSION_ID);
        if (sessionId == null) {
            throw new NetconfMessageBuilderException("Session-id is empty, invalid request");
        }
        killSessionReq.setSessionId(Integer.valueOf(sessionId.getTextContent()));
        return killSessionReq;
    }

    public static NetconfFilter getFilterFromRpcDocument(Document request) {
        NetconfFilter getConfigFilter = null;
        DocumentUtils docUtils = DocumentUtils.getInstance();
        Node filterNode = docUtils.getChildNodeByName(request, NetconfResources.FILTER, NetconfResources.NETCONF_RPC_NS_1_0);
        if (filterNode != null) {
            getConfigFilter = new NetconfFilter();
            String type = docUtils.getAttributeValueOrNullNS(filterNode,
                    NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.TYPE);
            if(type == null){
                type = docUtils.getAttributeValueOrNull(filterNode, NetconfResources.TYPE);
            }
            getConfigFilter.setType(type);
            if (NetconfFilter.XPATH_TYPE.equals(type)) {
                getConfigFilter.setSelectAttribute(docUtils.getAttributeValueOrNull(filterNode, NetconfResources.SELECT),
                        docUtils.getAllNamespacesPrefixesOfCurrentNode(filterNode));
            } else {
                getConfigFilter.setXmlFilters(DocumentUtils.getChildElements(filterNode));
            }
        }
        return getConfigFilter;
    }

    public static NetconfHelloMessage getHelloMessage(Document responseDoc) {
        Integer sessionIdFromHelloMessage = DocumentUtils.getInstance().getSessionIdFromHelloMessage(responseDoc);
        NetconfHelloMessage hello = new NetconfHelloMessage().setCapabilities(DocumentUtils.getInstance().getCapsFromHelloMessage(
                responseDoc));
        if (sessionIdFromHelloMessage != null) {
            hello.setSessionId(sessionIdFromHelloMessage);
        }

        return hello;
    }

    public static NetConfResponse getNetconfResponse(Document responseDoc) {
    	List<Element> outputElements = DocumentUtils.getInstance().getDataElementsFromRpcReply(responseDoc);
    	String txId = null;
        Element txIdElement = DocumentUtils.getInstance().getTxIdElementFromRpcReply(responseDoc);
        if (txIdElement != null) {
            txId = txIdElement.getTextContent();
        }
    	if (outputElements != null && outputElements.size() > 1){
    		NetconfRpcResponse response =  new NetconfRpcResponse();
    		((NetConfResponse)response)
    					.setMessageId(DocumentUtils.getInstance().getMessageIdFromRpcReplyDocument(responseDoc))
    					.addErrors(getRpcErrorsFromRpcReply(responseDoc))
    					.setOk(isResponseOk(responseDoc))
                        .setTxId(txId);
    		for (Element element:outputElements){
    			response.addRpcOutputElement(element);
    		}
    		return response;
    	} else if (outputElements != null && !outputElements.isEmpty()) {
    		return new NetConfResponse().setData(outputElements.get(0))
    				.setMessageId(DocumentUtils.getInstance().getMessageIdFromRpcReplyDocument(responseDoc))
    				.addErrors(getRpcErrorsFromRpcReply(responseDoc))
    				.setOk(isResponseOk(responseDoc))
                    .setTxId(txId);
    	} else {
    		return new NetConfResponse().setData(null)
    				.setMessageId(DocumentUtils.getInstance().getMessageIdFromRpcReplyDocument(responseDoc))
    				.addErrors(getRpcErrorsFromRpcReply(responseDoc))
    				.setOk(isResponseOk(responseDoc))
                    .setTxId(txId);
    	}

    }

    private static boolean isResponseOk(Document responseDoc){
        return DocumentUtils.getInstance().documentContainsElementWithName(responseDoc, NetconfResources.OK)
                || DocumentUtils.getInstance().getTxIdElementFromRpcReply(responseDoc) != null;
    }

    public static List<NetconfRpcError> getRpcErrorsFromRpcReply(Document responseDoc) {
        List<NetconfRpcError> rpcErrors = new ArrayList<NetconfRpcError>();
        List<Node> errorNodeList = getRpcErrorNodes(responseDoc);
        if (errorNodeList != null) {
            for (int i = 0; i < errorNodeList.size(); i++) {
                Node error = errorNodeList.get(i);
                DocumentUtils docUtils = DocumentUtils.getInstance();
                NetconfRpcErrorTag errorTag = NetconfRpcErrorTag.getType(docUtils.getNodeValueOrNull(docUtils.getDirectChildElement((Element) error,
                        NetconfResources.RPC_ERROR_TAG)));
                NetconfRpcErrorType errorType = NetconfRpcErrorType.getType(docUtils.getNodeValueOrNull(docUtils.getDirectChildElement((Element) error,
                        NetconfResources.RPC_ERROR_TYPE)));
                NetconfRpcErrorSeverity errorSeverity = NetconfRpcErrorSeverity.getType(docUtils.getNodeValueOrNull(docUtils
                        .getDirectChildElement((Element) error, NetconfResources.RPC_ERROR_SEVERITY)));
                String errorMessage = docUtils.getNodeValueOrNull(docUtils.getDirectChildElement((Element) error, NetconfResources.RPC_ERROR_MESSAGE));

                NetconfRpcError rpcError = new NetconfRpcError(errorTag, errorType, errorSeverity, errorMessage)
                        .setErrorAppTag(docUtils.getNodeValueOrNull(docUtils.getDirectChildElement((Element) error, NetconfResources.RPC_ERROR_APP_TAG)))
                        .setErrorInfo(docUtils.getDirectChildElement((Element) error, NetconfResources.RPC_ERROR_INFO))
                        .setErrorPathElement(docUtils.getDirectChildElement((Element) error, NetconfResources.RPC_ERROR_PATH));

                rpcErrors.add(rpcError);
            }
        }
        return rpcErrors;
    }

    public static List<Node> getRpcErrorNodes(Document doc) {
        List<Node> listNodeErrors = new ArrayList<Node>();
        Element rpcReplyNode = doc.getDocumentElement();
        if (rpcReplyNode != null && NetconfResources.RPC_REPLY.equals(rpcReplyNode.getLocalName())) {
            NodeList childNodes = rpcReplyNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (NetconfResources.RPC_ERROR.equals(node.getLocalName())) {
                    listNodeErrors.add(node);
                }
            }
        }
        return listNodeErrors;
    }

    public static Notification getNotification(DocumentInfo documentInfo) throws NetconfMessageBuilderException {
        Document notificationDoc = documentInfo.getDocument();
        NetconfNotification notification = new NetconfNotification();
        notification.setEventTime(DocumentUtils.getInstance().getEventTimeFromNotification(notificationDoc));
        if (isConfigChangeNotification(notificationDoc)) {
            notification = handleConfigChangeNotification(notificationDoc, notification);
        } else if (isStateChangeNotification(notificationDoc)) {
            notification = handleStateChangeNotification(notificationDoc, notification);
        } else if (isReplayCompleteNotification(notificationDoc)) {
            notification = new NetconfNotification(notificationDoc, documentInfo.getDocumentString());
        } else if (isNotificationCompleteNotification(notificationDoc)) {
            notification = new NetconfNotification(notificationDoc, documentInfo.getDocumentString());
        } else {
            notification = new NetconfNotification(notificationDoc, documentInfo.getDocumentString());
        }
        return notification;
    }

    public static Notification getNotification(String xmlContent) {
        Notification notification = new NetconfNotification();
        try {
            Document doc = DocumentUtils.stringToDocument(xmlContent);
            notification = getNotification(new DocumentInfo(doc, xmlContent));
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Cannot convert xml content to Notification", e);
        }
        return notification;
    }

    private static boolean isConfigChangeNotification(Document notificationDoc) {
        String datastore = DocumentUtils.getInstance().getDataStoreFromNotification(notificationDoc);
        List<EditInfo> editInfoList = getEditInfoListFromNotification(notificationDoc);
        if ((datastore != null && !datastore.isEmpty()) || !editInfoList.isEmpty()) {
            return true;
        }
        return false;
    }

    private static boolean isStateChangeNotification(Document notificationDoc) {
        Element element = notificationDoc.getDocumentElement();
        String target = getTargetFromNotification(element);
        // This logic should be refactored, should not get any <value> tag
        Element valueElement = DocumentUtils.getInstance().getChildElement(element, NetconfResources.STATE_CHANGE_VALUE);
        String value = valueElement != null ? valueElement.getTextContent() : null;
        if (target != null && !target.isEmpty() && value != null && !value.isEmpty()) {
            return !isContainedItemAndValueNodeUnderChangeLeaf(valueElement);
        }
        return false;
    }

    private static boolean isContainedItemAndValueNodeUnderChangeLeaf(Element valueElement) {
        // If found NetconfResources.STATE_CHANGE_VALUE, we should check and ignore the notification with bellow format:
        // <changed-leaf>
        //   <item>1</item>
        //   <value>
        //      <swmgmt:software-targets-aligned xmlns:swmgmt="http://www.test-company.com/solutions/anv-software">false</swmgmt:software-targets-aligned>
        //   </value>
        // </changed-leaf>
        if (valueElement == null) {
            return false;
        }
        Node parentNode = valueElement.getParentNode();
        NodeList nodeList = parentNode != null ? parentNode.getChildNodes() : null;
        if (nodeList == null) {
            return false;
        }
        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item).anyMatch(n -> NetconfResources.ITEM.equals(n.getNodeName()))
                && IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item).anyMatch(n -> NetconfResources.VALUE.equals(n.getNodeName()));
    }

    private static boolean isReplayCompleteNotification(Document notificationDoc) {
        Element element = notificationDoc.getDocumentElement();
        Element replayCompleteElement = DocumentUtils.getChildElement(element, NetconfResources.REPLAY_COMPLETE);
        if (replayCompleteElement != null) {
            return true;
        }
        return false;
    }

    private static boolean isNotificationCompleteNotification(Document notificationDoc) {
        Element element = notificationDoc.getDocumentElement();
        Element notificationCompleteElement = DocumentUtils.getChildElement(element, NetconfResources.NOTIFICATION_COMPLETE);
        if (notificationCompleteElement != null) {
            return true;
        }
        return false;
    }

    private static NetconfNotification handleStateChangeNotification(Document notificationDoc, NetconfNotification notification) throws
            NetconfMessageBuilderException {
        Element element = notificationDoc.getDocumentElement();
        String target = getTargetFromNotification(element);
        String value = DocumentUtils.getInstance().getValueFromNotification(element);
        StateChangeNotification stateChangeNotification = new StateChangeNotification(target, value);
        stateChangeNotification.setEventTime(notification.getEventTime());
        return stateChangeNotification;
    }


    private static NetconfNotification handleConfigChangeNotification(Document notificationDoc, NetconfNotification notification) throws
            NetconfMessageBuilderException {
        String datastore = DocumentUtils.getInstance().getDataStoreFromNotification(notificationDoc);
        List<EditInfo> editInfoList = getEditInfoListFromNotification(notificationDoc);
        NetconfConfigChangeNotification configChangeNotification = new NetconfConfigChangeNotification();
        ChangedByParams changedByParam = getChangedByParamsFromNotification(notificationDoc);
        configChangeNotification.setEventTime(notification.getEventTime());
        configChangeNotification.setDataStore(datastore);
        configChangeNotification.setEditList(editInfoList);
        configChangeNotification.setChangedByParams(changedByParam);
        configChangeNotification.setNotificationElement(configChangeNotification.getNetconfConfigChangeNotificationElement());
        return configChangeNotification;
    }

    public static LockRequest getLockRequest(Document request) throws NetconfMessageBuilderException {
        LockRequest axsLockRequest = new LockRequest();
        DocumentUtils docUtils = DocumentUtils.getInstance();
        axsLockRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(request));
        Node target = docUtils.getChildNodeByName(request.getDocumentElement(), NetconfResources.DATA_TARGET, NetconfResources
                .NETCONF_RPC_NS_1_0);
        if (target == null) {
            throw new NetconfMessageBuilderException("<target> cannot be null/empty");
        }
        axsLockRequest.setTarget(docUtils.getFirstElementChildNode(target).getLocalName());
        return axsLockRequest;
    }

    public static UnLockRequest getUnLockRequest(Document request) throws NetconfMessageBuilderException {
        UnLockRequest unLockRequest = new UnLockRequest();
        DocumentUtils docUtils = DocumentUtils.getInstance();
        unLockRequest.setMessageId(docUtils.getMessageIdFromRpcDocument(request));
        Node target = docUtils.getChildNodeByName(request.getDocumentElement(), NetconfResources.DATA_TARGET, NetconfResources
                .NETCONF_RPC_NS_1_0);
        if (target == null) {
            throw new NetconfMessageBuilderException("<target> cannot be null/empty");
        }
        unLockRequest.setTarget(docUtils.getFirstElementChildNode(target).getLocalName());
        return unLockRequest;
    }

    public static String processChunkedMessage(final String msg) throws NetconfMessageBuilderException {
        try {
            LOGGER.trace("Processing the chunked message: {}", LOGGER.sensitiveData(msg));
            String tempReply = new String(msg);
            if (!msg.endsWith(EOM_CHUNK)) {
                throw new NetconfMessageBuilderException("the reply does not contain end-of-chunks delimiter" + tempReply);
            }

            Matcher chunkSizeMatcher = CHUNK_SIZE_PATTERN.matcher(tempReply);
            List<String> chunks = new ArrayList<String>();

            while (chunkSizeMatcher.find()) {
                String chunkSizeStr = tempReply.substring(chunkSizeMatcher.start(), chunkSizeMatcher.end());
                int chunkSize = Integer.valueOf(CHUNK_SIZE_HASH.matcher(chunkSizeStr).replaceAll(EMPTY_STR).trim());
                int startIndex = chunkSizeMatcher.end();
                int endIndex = chunkSize + startIndex;
                String chunk = DocumentToPojoTransformer.getChunk(tempReply, startIndex, endIndex);
                if (chunkSize != chunk.length()) {
                    throw new NetconfMessageBuilderException("Invalid chunk/chunkSize for message" + tempReply);
                }
                chunks.add(chunk);
            }
            StringBuilder sb = new StringBuilder();
            for (String chunk : chunks) {
                sb.append(chunk);
            }
            String trimMsg = sb.toString().trim();
            LOGGER.trace("After Processing the chunked messages , output message : {}", LOGGER.sensitiveData(trimMsg));
            return trimMsg;
        } catch (Exception e) {
            throw new NetconfMessageBuilderException("Error while creating message from chunks ", e);
        }
    }

    public static String chunkMessage(int chunkSize, final String message) throws NetconfMessageBuilderException {
        LOGGER.trace("chunking the incoming message : {} with chunk size : {}", LOGGER.sensitiveData(message), chunkSize);
        StringBuilder chunkBuilder = new StringBuilder();

        if (message.length() > chunkSize) {
            int startIndex = 0;
            int endIndex = chunkSize;
            int currentChunkSize = chunkSize;

            while (true) {
                chunkBuilder.append(LF).append(HASH).append(currentChunkSize).append(LF); // chunk = LF HASH chunk-size LF
                chunkBuilder.append(message.substring(startIndex, endIndex));
                if (endIndex == message.length()) {
                    break;
                }
                startIndex = endIndex;
                if (message.length() - endIndex > chunkSize) {
                    endIndex += chunkSize;
                } else {
                    endIndex = message.length();
                }
                currentChunkSize = endIndex - startIndex;
            }
        } else {
            chunkBuilder.append(LF).append(HASH).append(message.length()).append(LF) // chunk = LF HASH chunk-size LF
                    .append(message);
        }
        chunkBuilder.append(LF).append(HASH).append(HASH).append(LF); // end-of-chunks = LF HASH HASH LF
        String chunkedStr = chunkBuilder.toString();
        LOGGER.trace("Chunked message : {} with chunk size : {}", LOGGER.sensitiveData(chunkedStr), chunkSize);
        return chunkedStr;
    }

    public static byte[] addRpcDelimiter(byte[] bytes) {
        byte[] delimiterBytes = (NetconfResources.RPC_EOM_DELIMITER).getBytes();
        byte[] merged = new byte[bytes.length + delimiterBytes.length];
        System.arraycopy(bytes, 0, merged, 0, bytes.length);
        System.arraycopy(delimiterBytes, 0, merged, bytes.length, delimiterBytes.length);
        return merged;
    }

    public static byte[] getBytesFromDocument(Document requestDocument) throws NetconfMessageBuilderException {
        try {
            Transformer transformer;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            transformer = TRANSFORMER_FACTORY.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StreamResult result = new StreamResult(new BufferedWriter(new OutputStreamWriter(bos)));
            DOMSource source = new DOMSource(requestDocument);
            transformer.transform(source, result);

            return bos.toByteArray();
        } catch (TransformerException e) {
            throw new NetconfMessageBuilderException("Error while getting bytes from document ", e);// $COVERAGE-IGNORE$
        }

    }

    static String getChunk(String reply, int startIndex, int endIndex) {
        return reply.substring(startIndex, endIndex);
    }

    public static String getTypeOfNetconfRequest(Document request) {
        Node childNodeByName = DocumentUtils.getInstance().getChildNodeByName(request, NetconfResources.RPC, NetconfResources
                .NETCONF_RPC_NS_1_0);
        String requestType = NetconfResources.NONE;
        if (childNodeByName != null) {
            requestType = DocumentUtils.getLocalName(DocumentUtils.getInstance().getDirectChildElement((Element) childNodeByName,
                    DocumentUtils.getInstance().getTypeOfNetconfRequest(childNodeByName)));
        }
        return requestType;
    }

    public static void addMessageId(Document doc, int messageId) {
        Element docElement = doc.getDocumentElement();
        docElement.setAttribute(NetconfResources.MESSAGE_ID, String.valueOf(messageId));
    }

    public static void addNetconfNamespace(Document doc, String namespace) {
        Element docElement = doc.getDocumentElement();
        docElement.setAttribute(NetconfResources.XMLNS, namespace);
    }

    public static AbstractNetconfRequest getRequest(Document request) throws NetconfMessageBuilderException {
        String typeOfRequest = getTypeOfNetconfRequest(request);
        AbstractNetconfRequest axsNetconfRequest;
        switch (typeOfRequest) {
            case NetconfResources.CLOSE_SESSION:
                axsNetconfRequest = DocumentToPojoTransformer.getCloseSession(request);
                break;
            case NetconfResources.COPY_CONFIG:
                axsNetconfRequest = DocumentToPojoTransformer.getCopyConfig(request);
                break;
            case NetconfResources.DELETE_CONFIG:
                axsNetconfRequest = DocumentToPojoTransformer.getDeleteConfig(request);
                break;
            case NetconfResources.EDIT_CONFIG:
                axsNetconfRequest = DocumentToPojoTransformer.getEditConfig(request);
                break;
            case NetconfResources.GET:
                axsNetconfRequest = DocumentToPojoTransformer.getGet(request);
                break;
            case NetconfResources.GET_CONFIG:
                axsNetconfRequest = DocumentToPojoTransformer.getGetConfig(request);
                break;
            case NetconfResources.KILL_SESSION:
                axsNetconfRequest = DocumentToPojoTransformer.getKillSession(request);
                break;
            case NetconfResources.LOCK:
                axsNetconfRequest = DocumentToPojoTransformer.getLockRequest(request);
                break;
            case NetconfResources.UNLOCK:
                axsNetconfRequest = DocumentToPojoTransformer.getUnLockRequest(request);
                break;
            case NetconfResources.CREATE_SUBSCRIPTION:
                axsNetconfRequest = DocumentToPojoTransformer.getCreateSubscriptionRequest(request);
                break;
            case NetconfResources.ACTION:
                axsNetconfRequest = DocumentToPojoTransformer.getAction(request);
                break;
            default:
                // Could be a special rpc request
                axsNetconfRequest = DocumentToPojoTransformer.getRpcRequest(request);
        }
        return axsNetconfRequest;
    }

    public static List<EditInfo> getEditInfoListFromNotification(Document notificationDoc) {
        Element notificationElement = notificationDoc.getDocumentElement();
        List<EditInfo> editInfoList = new ArrayList<EditInfo>();
        Collection<Element> editList = DocumentUtils.getChildElements(notificationElement, NetconfResources.EDIT);
        for (Element element : editList) {
            EditInfo editInfo = new EditInfo();

            if(isImpliedEdit(element)){
                editInfo.setImplied(true);
            }

            String target = getTargetFromNotification(element);
            if (target != null) {
                editInfo.setTarget(target);
            }
            List<ChangedLeafInfo> changedLeafInfos = getChangedLeafInfoFromNotification(element);
            if (changedLeafInfos != null) {
                editInfo.setChangedLeafInfos(changedLeafInfos);
            }

            String operation = getOperationFromNotification(element);
            if (operation != null) {
                editInfo.setOperation(getOperationFromNotification(element));
            }

            Map<String, String> namespaceDeclareMap = getnamespaceDeclareMapFromNotification(element);
            if (namespaceDeclareMap != null) {
                editInfo.setNamespaceDeclareMap(namespaceDeclareMap);
            }

            if (!(editInfo.getOperation() == null && editInfo.getTarget() == null)) {
                editInfoList.add(editInfo);
            }
        }
        return editInfoList;
    }

    private static boolean isImpliedEdit(Element element) {
        Element targetElement = DocumentUtils.getChildElement(element, NetconfResources.IMPLIED);
        return targetElement != null;
    }

    private static List<ChangedLeafInfo> getChangedLeafInfoFromNotification(Element editElement) {
        Collection<Element> changedLeafElements = DocumentUtils.getChildElements(editElement, NetconfResources.CHANGED_LEAF);
        if (!changedLeafElements.isEmpty()) {
            List<ChangedLeafInfo> changedLeafInfos = new ArrayList<>();
            for (Element e : changedLeafElements) {
                Element leafElements = DocumentUtils.getChildElement(e);
                ChangedLeafInfo changedLeafInfo = new ChangedLeafInfo(leafElements.getLocalName(), leafElements.getTextContent(),
                        leafElements.getNamespaceURI(), leafElements.getPrefix());
                changedLeafInfos.add(changedLeafInfo);
            }
            return changedLeafInfos;
        }
        return null;
    }

    public static ChangedByParams getChangedByParamsFromNotification(Document notificationDoc) {
        Element notificationElement = notificationDoc.getDocumentElement();
        Element changedbyElement = DocumentUtils.getChildElement(notificationElement, NetconfResources.CHANGED_BY);
        SessionInfo sessionInfo = new SessionInfo();
        String userName = getTextContentFromElement(changedbyElement, NetconfResources.USER_NAME);
        if (userName != null) {
            sessionInfo.setUserName(userName);
        }

        String sessionId = getTextContentFromElement(changedbyElement, NetconfResources.SESSION_ID);
        if (sessionId != null) {
            sessionInfo.setSessionId(Integer.parseInt(sessionId));
        }

        String sourceHostIpAddress = getTextContentFromElement(changedbyElement, NetconfResources.SOURCE_HOST);

        if (sourceHostIpAddress != null) {

            sessionInfo.setSourceHostIpAddress(sourceHostIpAddress);
        }

        ChangedByParams changedByParam = new ChangedByParams(sessionInfo);
        return changedByParam;
    }

    public static String getTextContentFromElement(Element element, String tagName) {
        Element targetElement = DocumentUtils.getChildElement(element, tagName);
        if (targetElement != null) {
            return targetElement.getTextContent();
        }
        return null;
    }

    public static String getTargetFromNotification(Element editElement) {
        Element targetElement = DocumentUtils.getChildElement(editElement, NetconfResources.TARGET);
        if (targetElement != null) {
            return targetElement.getTextContent();
        }
        return null;
    }

    private static String getOperationFromNotification(Element editElement) {
        Element operationElement = DocumentUtils.getChildElement(editElement, NetconfResources.OPERATION);
        if (operationElement != null) {
            return operationElement.getTextContent();
        }
        return null;
    }

    private static Map<String, String> getnamespaceDeclareMapFromNotification(Element editElement) {
        Map<String, String> namespaceDeclareMap = new HashMap<String, String>();
        Element targetElement = DocumentUtils.getChildElement(editElement, NetconfResources.TARGET);
        if (targetElement != null) {
            NamedNodeMap attributes = targetElement.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                namespaceDeclareMap.put(attributes.item(i).getLocalName(), attributes.item(i).getTextContent());
            }
            return namespaceDeclareMap;
        }
        return null;
    }
}
