package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;

import org.broadband_forum.obbaa.netconf.api.client.InternalNetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreIntegrityService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;

public class DataStoreIntegrityServiceImpl implements DataStoreIntegrityService {

    private static final String ELEMENT_LIST = "elementList";
    private NetconfServer m_server;
    private final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DataStoreIntegrityServiceImpl.class, LogAppNames.NETCONF_STACK);
    
    @VisibleForTesting
    public DataStoreIntegrityServiceImpl(NetconfServer server){
        m_server = server;
    }
    
    @Override
    public List<EditConfigRequest> createInternalEditRequests(EditConfigRequest sourceRequest, NetconfClientInfo clientInfo) throws GetAttributeException {
    	
    	Document document = null;
        Map<ModelNode, Collection<QName>> mergeNodes = DataStoreValidationUtil.getValidationContext().getMergeList();
        Map<ModelNode, Collection<QName>> deleteNodes = DataStoreValidationUtil.getValidationContext().getDeleteList();
        Map<ModelNode, Collection<QName>> createNodes = DataStoreValidationUtil.getValidationContext().getCreateList();
		resetCache();
        if (deleteNodes!=null && !deleteNodes.isEmpty()) {
            /**
             * Build a request document with operation delete
             * for all identified nodes
             */
            document = buildDocumentForNodes(document, deleteNodes, EditConfigOperations.REMOVE);
        }
		
        if (createNodes!=null && !createNodes.isEmpty()) {
        	document = buildDocumentForNodes(document, createNodes, EditConfigOperations.CREATE);
        }
        
        if (mergeNodes!=null && !mergeNodes.isEmpty()) {
        	document = buildDocumentForNodes(document, mergeNodes, EditConfigOperations.MERGE);
        }
        
        if (document != null){
            EditConfigRequest request = new EditConfigRequest();
            request.setMessageId("internal_edit:"+sourceRequest.getMessageId());
            request.setTarget(sourceRequest.getTarget());
            request.setClientInfo(new InternalNetconfClientInfo(NetconfResources.IMPLICATION_CHANGE, 0));
            request.setErrorOption(sourceRequest.getErrorOption());
            request.setReplyTimeout(sourceRequest.getReplyTimeout());
            request.setDefaultOperation(sourceRequest.getDefaultOperation());
            request.setTestOption(sourceRequest.getTestOption());
            request.setWithDelay(sourceRequest.getWithDelay());
            request.setUploadToPmaRequest(sourceRequest.isUploadToPmaRequest());
            EditConfigElement configElement = new EditConfigElement();
            configElement.addConfigElementContent(document.getDocumentElement());
            request.setConfigElement(configElement);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Internal request generated after validation {}", request.requestToString());
            }
            List<EditConfigRequest> configRequests = new ArrayList<EditConfigRequest>();
            configRequests.add(request);
            return configRequests;
        }
        return null;
    }

	private void resetCache(){
		RequestScope.getCurrentScope().putInCache(ELEMENT_LIST, null);
	}
	
	@SuppressWarnings("unchecked")
	private Map<ModelNodeId, Element> getElementList() {
		Map<ModelNodeId,Element> returnValue = null;
		RequestScope scope = RequestScope.getCurrentScope();
		returnValue = (Map<ModelNodeId, Element>) scope.getFromCache(ELEMENT_LIST);
		if (returnValue == null){
			returnValue = new HashMap<ModelNodeId,Element>();
			scope.putInCache(ELEMENT_LIST, returnValue);
		}
		return returnValue;
	}
	
    private Document buildDocumentForNodes(Document document, Map<ModelNode, Collection<QName>> nodes, String operation) throws GetAttributeException {
        Map<ModelNodeId, Element> elementList = getElementList();
        if (document == null){
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

    private Element buildElement(DataSchemaNode schemaNode, Document document, ModelNode modelNode) throws GetAttributeException {

        Element returnValue = null;
        returnValue = document.createElementNS(modelNode.getQName().getNamespace().toString(), modelNode.getQName().getLocalName());
        /*
         * if there are any keys for a list, append the same. 
         */
        Map<QName, ConfigAttributeHelper> keys = modelNode.getMountModelNodeHelperRegistry().getNaturalKeyHelpers(modelNode.getModelNodeSchemaPath());
        for (Map.Entry<QName, ConfigAttributeHelper> entry : keys.entrySet()) {
            String ns = entry.getKey().getNamespace().toString();
            String localName = entry.getKey().getLocalName();
            String value = entry.getValue().getValue(modelNode).getStringValue();
            Element childElement = document.createElementNS(ns, localName);
            ConfigLeafAttribute attribute = entry.getValue().getValue(modelNode);
            if (attribute instanceof IdentityRefConfigAttribute) {
                IdentityRefConfigAttribute idRef = ((IdentityRefConfigAttribute)attribute);
                String idRefNs = idRef.getNamespace();
                String idRefPrefix = modelNode.getSchemaRegistry().getPrefix(idRefNs);
                childElement.setAttributeNS(PojoToDocumentTransformer.XMLNS_NAMESPACE, PojoToDocumentTransformer.XMLNS+idRefPrefix, idRefNs);
            }
            childElement.setTextContent(value);
            returnValue.appendChild(childElement);
        }
        return returnValue;
    }

    private Element getElementForModelNode(ModelNode modelNode, Document document, Map<ModelNodeId, Element> elementList) throws GetAttributeException {
        Element returnValue = null;
        returnValue = elementList.get(modelNode.getModelNodeId());
        if (returnValue == null) {
            DataSchemaNode schemaNode = modelNode.getSchemaRegistry().getDataSchemaNode(modelNode.getModelNodeSchemaPath());
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

    private void buildElement(Document doc, ModelNode modelNode, QName qname, String operation, Map<ModelNodeId, Element> elementList) throws GetAttributeException {

        Element parentElement = null;
        parentElement = getElementForModelNode(modelNode, doc, elementList);
        if (modelNode instanceof ModelNodeWithAttributes) {
            ModelNodeWithAttributes modelNodeAttributes = (ModelNodeWithAttributes) modelNode;
            if (operation.equals(EditConfigOperations.CREATE) || operation.equals(EditConfigOperations.MERGE)) {
                if (modelNode.getMountModelNodeHelperRegistry().getNaturalKeyHelper(modelNode.getModelNodeSchemaPath(), qname) == null) {
                    // If this is a key of a list, it is already added.
                    SchemaPath childPath = modelNode.getSchemaRegistry().getDescendantSchemaPath(modelNode.getModelNodeSchemaPath(), qname);
                    Map<SchemaPath, Map<String, Object>> defaultValues = DataStoreValidationUtil.getValidationContext().getDefaultValues();
                    if (defaultValues != null && !defaultValues.isEmpty()) {
                        Map<String, Object> defaultValueMap = defaultValues.get(childPath);
                        if (defaultValueMap != null && !defaultValueMap.isEmpty()) {
                            Object defaultValue = defaultValueMap.get(modelNode.getModelNodeId().xPathString());
                            Element leafElement = getChildElementIfExists(parentElement, qname, operation);
                            if ( leafElement == null){
                            	leafElement = createElement(doc, qname, operation, (String) defaultValue);	
                            	parentElement.appendChild(leafElement);
                            }
                            for(Entry<QName, ConfigLeafAttribute> identity: modelNodeAttributes.getAttributes().entrySet()){
                                if(identity.getKey().equals(qname)){
                                    ConfigLeafAttribute configAttr = identity.getValue();
                                    if(configAttr!=null && configAttr instanceof IdentityRefConfigAttribute){
                                        if(configAttr.getNamespace()!=null && ((IdentityRefConfigAttribute)configAttr).getNamespacePrefix()!=null){
                                            leafElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, NetconfResources.XMLNS+ ":"+ ((IdentityRefConfigAttribute)configAttr).getNamespacePrefix(), configAttr.getNamespace());
                                        }
                                    }
                                }
                            }
                            //If 'IdentityRef' type is added by 'uses' keyword in edit tree and originally it is in different name-space, then original namespace should be appended in leaf-element for 'IdentityRef' type validation
							DataSchemaNode defaultNode = modelNode.getSchemaRegistry().getDataSchemaNode(childPath);
							if (defaultNode != null) {
								DataStoreValidationUtil.setOrginalNSIfAddedByUses(defaultNode, leafElement, modelNode);
							}
                        }
                    }
                }
            } else if (operation.equals(EditConfigOperations.DELETE) || operation.equals(EditConfigOperations.REMOVE)) {
                ConfigLeafAttribute leafAttribute = modelNodeAttributes.getAttribute(qname);
                if (leafAttribute != null) {
                    Element leafElement = createElement(doc, qname, operation, leafAttribute.getStringValue());
                    parentElement.appendChild(leafElement);
                } else if (modelNodeAttributes.getLeafList(qname) != null){
                    Set<ConfigLeafAttribute> leafLists = modelNodeAttributes.getLeafList(qname);
                    for (ConfigLeafAttribute leafList : leafLists) {
                        Element leafElement = createElement(doc, qname, operation, leafList.getStringValue());
                        parentElement.appendChild(leafElement);
                    }
                } else if (modelNode.getQName().equals(qname)){
                    parentElement.setAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION, operation);
                } else {
                	if (modelNode.getMountModelNodeHelperRegistry().getChildContainerHelper(modelNode.getModelNodeSchemaPath(), qname) != null) {
                		Element containerElement = createElement(doc, qname, operation, null);
                		parentElement.appendChild(containerElement);
                	} else if (modelNode.getMountModelNodeHelperRegistry().getChildListHelper(modelNode.getModelNodeSchemaPath(), qname) != null) {
                		Element listElement = createElement(doc, qname, operation, null);
                		parentElement.appendChild(listElement);
                	} else {
                		if (LOGGER.isInfoEnabled()) {
                    		LOGGER.info("Qname {} for modelNode {} is not marked for deletion", qname, modelNode.getModelNodeId());
                		}
                	}
                }
            }
        }
    }
    
    private Element getChildElementIfExists(Element parentElement, QName qname, String operation) {
    	Element child = null;
    	if ( parentElement !=null){
			child = DocumentUtils.getDescendant(parentElement, qname.getLocalName(), qname.getNamespace().toString());
			if ( child != null){
				child.setAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION, operation);
			}
		}
		return child;
	}

	private Element createElement(Document doc, QName qname, String operation, String leafAttribute) {
        Element leafElement = doc.createElementNS(qname.getNamespace().toString(), qname.getLocalName());
        leafElement.setAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION, operation);
        if (leafAttribute != null){
            leafElement.setTextContent(leafAttribute);
        }
        return leafElement;
    }

}
