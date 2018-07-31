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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreIntegrityService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.client.InternalNetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.server.RequestScope;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;

import com.google.common.annotations.VisibleForTesting;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class DataStoreIntegrityServiceImpl implements DataStoreIntegrityService {

    private static final String ELEMENT_LIST = "elementList";
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final SchemaRegistry m_schemaRegistry;
    private NetconfServer m_server;
    private final AdvancedLogger LOGGER = LoggerFactory.getLogger(DataStoreIntegrityServiceImpl.class,
            "netconf-stack", "DEBUG", "GLOBAL");

    @VisibleForTesting
    public DataStoreIntegrityServiceImpl(ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry
            schemaRegistry, NetconfServer server) {
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_schemaRegistry = schemaRegistry;
        m_server = server;
    }

    @Override
    public List<Notification> createOrDeleteNodes(EditConfigRequest sourceRequest, NetconfClientInfo clientInfo)
            throws GetAttributeException {
        Document document = null;
        Map<ModelNode, Collection<QName>> mergeNodes = DataStoreValidationUtil.getValidationContext().getMergeList();
        Map<ModelNode, Collection<QName>> deleteNodes = DataStoreValidationUtil.getValidationContext().getDeleteList();
        Map<ModelNode, Collection<QName>> createNodes = DataStoreValidationUtil.getValidationContext().getCreateList();
        try {
            if (deleteNodes != null && !deleteNodes.isEmpty()) {
                /**
                 * Build a request document with operation delete
                 * for all identified nodes
                 */
                document = buildDocumentForNodes(document, deleteNodes, EditConfigOperations.DELETE);
            }

            if (createNodes != null && !createNodes.isEmpty()) {
                document = buildDocumentForNodes(document, createNodes, EditConfigOperations.CREATE);
            }

            if (mergeNodes != null && !mergeNodes.isEmpty()) {
                document = buildDocumentForNodes(document, mergeNodes, EditConfigOperations.MERGE);
            }

            if (document != null) {
                EditConfigRequest request = new EditConfigRequest();
                request.setMessageId("internal_edit:" + sourceRequest.getMessageId());
                request.setTarget(sourceRequest.getTarget());
                request.setClientInfo(new InternalNetconfClientInfo(NetconfResources.IMPLICATION_CHANGE, 0));
                request.setErrorOption(sourceRequest.getErrorOption());
                request.setReplyTimeout(sourceRequest.getReplyTimeout());
                request.setDefaultOperation(sourceRequest.getDefaultOperation());
                request.setTestOption(sourceRequest.getTestOption());
                request.setWithDelay(sourceRequest.getWithDelay());
                EditConfigElement configElement = new EditConfigElement();
                configElement.addConfigElementContent(document.getDocumentElement());
                request.setConfigElement(configElement);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Internal request generated after validation {}", request.requestToString());
                }
                NetConfResponse response = new NetConfResponse();
                if (m_server != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Sending internal request {}", request.requestToString());
                    }
                    List<Notification> notifications = m_server.onEditConfig(request.getClientInfo(), request,
                            response);
                    if (response.isOk()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Internal modification is success, will notify the subscribers with n{}",
                                    notifications);
                        }
                        return notifications;
                    } else {
                        /**
                         * Indicates some deletion has caused either a
                         * must or leafRef
                         */
                        throw new EditConfigException(response.getErrors().get(0));
                    }
                } else {
                    LOGGER.info("NC server not initialized yet");
                }
            }
        } finally {
            resetCache();
        }
        return null;
    }

    private void resetCache() {
        RequestScope.getCurrentScope().putInCache(ELEMENT_LIST, null);
    }

    @SuppressWarnings("unchecked")
    private Map<ModelNodeId, Element> getElementList() {
        Map<ModelNodeId, Element> returnValue = null;
        RequestScope scope = RequestScope.getCurrentScope();
        returnValue = (Map<ModelNodeId, Element>) scope.getFromCache(ELEMENT_LIST);
        if (returnValue == null) {
            returnValue = new HashMap<ModelNodeId, Element>();
            scope.putInCache(ELEMENT_LIST, returnValue);
        }
        return returnValue;
    }

    private Document buildDocumentForNodes(Document document, Map<ModelNode, Collection<QName>> nodes, String
            operation) throws GetAttributeException {
        Map<ModelNodeId, Element> elementList = getElementList();
        if (document == null) {
            document = DataStoreValidationUtil.getValidationDocument();
        }
        for (Map.Entry<ModelNode, Collection<QName>> node : nodes.entrySet()) {
            // for each entry, find the right parent element and build the node
            for (QName child : node.getValue()) {
                buildElement(document, node.getKey(), child, operation, elementList);
            }
        }
        return document;

    }

    private Element buildElement(DataSchemaNode schemaNode, Document document, ModelNode modelNode) throws
            GetAttributeException {

        Element returnValue = null;
        returnValue = document.createElementNS(modelNode.getQName().getNamespace().toString(), modelNode.getQName()
                .getLocalName());
        /*
         * if there are any keys for a list, append the same. 
         */
        Map<QName, ConfigAttributeHelper> keys = m_modelNodeHelperRegistry.getNaturalKeyHelpers(modelNode
                .getModelNodeSchemaPath());
        for (Map.Entry<QName, ConfigAttributeHelper> entry : keys.entrySet()) {
            String ns = entry.getKey().getNamespace().toString();
            String localName = entry.getKey().getLocalName();
            String value = entry.getValue().getValue(modelNode).getStringValue();
            Element childElement = document.createElementNS(ns, localName);
            ConfigLeafAttribute attribute = entry.getValue().getValue(modelNode);
            if (attribute instanceof IdentityRefConfigAttribute) {
                IdentityRefConfigAttribute idRef = ((IdentityRefConfigAttribute) attribute);
                String idRefNs = idRef.getNamespace();
                String idRefPrefix = m_schemaRegistry.getPrefix(idRefNs);
                childElement.setAttributeNS(PojoToDocumentTransformer.XMLNS_NAMESPACE, PojoToDocumentTransformer
                        .XMLNS + idRefPrefix, idRefNs);
            }
            childElement.setTextContent(value);
            returnValue.appendChild(childElement);
        }
        return returnValue;
    }

    private Element getElementForModelNode(ModelNode modelNode, Document document, Map<ModelNodeId, Element>
            elementList) throws GetAttributeException {
        Element returnValue = null;
        returnValue = elementList.get(modelNode.getModelNodeId());
        if (returnValue == null) {
            DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(modelNode.getModelNodeSchemaPath());
            Element parentElement = null;
            if (modelNode.getParent() != null) {
                parentElement = getElementForModelNode(modelNode.getParent(), document, elementList);
            } else {
                /*
                 * indicates this is the root Element. Build it and append it to the document
                 */
                parentElement = elementList.get(modelNode.getModelNodeId());
                if (parentElement == null) {
                    parentElement = buildElement(schemaNode, document, modelNode);
                    parentElement.setAttribute(NetconfResources.XMLNS, NetconfResources.NETCONF_BASE_CAP_1_0);
                    elementList.put(modelNode.getModelNodeId(), parentElement);
                    document.appendChild(parentElement);
                }
                return parentElement;
            }
            returnValue = buildElement(schemaNode, document, modelNode);
            parentElement.appendChild(returnValue);
            elementList.put(modelNode.getModelNodeId(), returnValue);
        }

        return returnValue;
    }

    private void buildElement(Document doc, ModelNode modelNode, QName qname, String operation, Map<ModelNodeId,
            Element> elementList) throws GetAttributeException {

        Element parentElement = null;
        parentElement = getElementForModelNode(modelNode, doc, elementList);
        if (modelNode instanceof ModelNodeWithAttributes) {
            ModelNodeWithAttributes modelNodeAttributes = (ModelNodeWithAttributes) modelNode;
            if (operation.equals(EditConfigOperations.CREATE) || operation.equals(EditConfigOperations.MERGE)) {
                if (m_modelNodeHelperRegistry.getNaturalKeyHelper(modelNode.getModelNodeSchemaPath(), qname) == null) {
                    // If this is a key of a list, it is already added.
                    SchemaPath childPath = m_schemaRegistry.getDescendantSchemaPath(modelNode.getModelNodeSchemaPath
                            (), qname);
                    Map<SchemaPath, Object> defaultValues = DataStoreValidationUtil.getValidationContext()
                            .getDefaultValues();
                    if (defaultValues != null && !defaultValues.isEmpty()) {
                        Object defaultValue = defaultValues.get(childPath);
                        Element leafElement = createElement(doc, qname, operation, (String) defaultValue);
                        parentElement.appendChild(leafElement);
                    }
                }
            } else if (operation.equals(EditConfigOperations.DELETE)) {
                ConfigLeafAttribute leafAttribute = modelNodeAttributes.getAttribute(qname);
                if (leafAttribute != null) {
                    Element leafElement = createElement(doc, qname, operation, leafAttribute.getStringValue());
                    parentElement.appendChild(leafElement);
                } else if (modelNodeAttributes.getLeafList(qname) != null) {
                    Set<ConfigLeafAttribute> leafLists = modelNodeAttributes.getLeafList(qname);
                    for (ConfigLeafAttribute leafList : leafLists) {
                        Element leafElement = createElement(doc, qname, operation, leafList.getStringValue());
                        parentElement.appendChild(leafElement);
                    }
                } else if (modelNode.getQName().equals(qname)) {
                    parentElement.setAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION,
                            operation);
                } else {
                    if (m_modelNodeHelperRegistry.getChildContainerHelper(modelNode.getModelNodeSchemaPath(), qname)
                            != null) {
                        Element containerElement = createElement(doc, qname, operation, null);
                        parentElement.appendChild(containerElement);
                    } else if (m_modelNodeHelperRegistry.getChildListHelper(modelNode.getModelNodeSchemaPath(),
                            qname) != null) {
                        Element listElement = createElement(doc, qname, operation, null);
                        parentElement.appendChild(listElement);
                    } else {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Qname {} for modelNode {} is not marked for deletion", qname, modelNode
                                    .getModelNodeId());
                        }
                    }
                }
            }
        }
    }

    private Element createElement(Document doc, QName qname, String operation, String leafAttribute) {
        Element leafElement = doc.createElementNS(qname.getNamespace().toString(), qname.getLocalName());
        leafElement.setAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION, operation);
        if (leafAttribute != null) {
            leafElement.setTextContent(leafAttribute);
        }
        return leafElement;
    }

}
