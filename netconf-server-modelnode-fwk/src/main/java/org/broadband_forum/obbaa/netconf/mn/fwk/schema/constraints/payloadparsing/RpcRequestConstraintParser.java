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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors.getViolateMaxElementException;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors.getViolateMinElementException;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ActionUtils.getActionInputChildNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.NetconfRpcPayLoadType;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.DataPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.MountProviderInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RpcRequestConstraintParser extends SchemaElementChildrenConstraintParser implements RpcPayloadConstraintParser {
	private static final String EMPTY_STR = "";
	public static final String IGNORE_PASSWORD_ATTRIBUTE = "ignore-password-attribute";
	private final SchemaRegistry m_schemaRegistry;
	private final ModelNodeDataStoreManager m_modelNodeDsm;
	private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
	private final DSExpressionValidator m_expressionValidator;
	
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(RpcRequestConstraintParser.class, LogAppNames.NETCONF_STACK);


	public RpcRequestConstraintParser(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDSM, DSExpressionValidator validator, RootModelNodeAggregator aggregator) {
		m_schemaRegistry = schemaRegistry;
		m_modelNodeDsm = modelNodeDSM;
		m_expressionValidator = validator;
		m_rootModelNodeAggregator = aggregator;
	}

	protected SchemaRegistry getSchemaRegistry(SchemaPath schemaPath) {
		return m_schemaRegistry;
	}

	public ModelNodeDSMRegistry getModelNodeDSMRegistry() {
        return m_modelNodeDSMRegistry;
    }

    public void setModelNodeDSMRegistry(ModelNodeDSMRegistry modelNodeDSMRegistry) {
        m_modelNodeDSMRegistry = modelNodeDSMRegistry;
    }

    @Override
	public void validate(AbstractNetconfRequest netconfRequest, RequestType requestType) throws ValidationException {             	
		if (requestType.isRpc()) {
			validateRpc(((NetconfRpcRequest) netconfRequest).getRpcInput(), (NetconfMessage) netconfRequest,requestType, ((NetconfRpcRequest) netconfRequest).getRpcContext());
		} else if (requestType.isAction()) {
			Element actionTreeElement = ((ActionRequest) netconfRequest).getActionTreeElement();
			ActionDefinition actionDefinition = ((ActionRequest) netconfRequest).getActionDefinition();
			validateAction(actionTreeElement, requestType, actionDefinition);
		} else if(requestType.equals(RequestType.COPY_CONFIG)){
            validateCopyConfig((CopyConfigRequest) netconfRequest, requestType);
		} else {
		    validateEditConfig((EditConfigRequest) netconfRequest, requestType);
		}
	}
	
	@Override
	public void validate(NetConfResponse response, RequestType type)  {
		try {
			List<Element> outputElements = new ArrayList<>();
			if(type.isAction()){
				outputElements = ((ActionResponse)response).getActionOutputElements();
			} else {
			    outputElements = DocumentUtils.getChildElements(((NetconfRpcResponse)response).getResponseDocument());
			}
		    if (outputElements.size() > 0){
		    	for (Element outputElement:outputElements){
			        try {
			        	if(type.isAction()){
			        		ActionDefinition actionDef = ((ActionResponse)response).getActionDefinition();
			        		Element actionContext = ((ActionResponse)response).getActionContext();
			        		SchemaRegistry schemaRegistry = null;
			        		MountProviderInfo mountProviderInfo = SchemaRegistryUtil.getMountProviderInfo(actionContext, m_schemaRegistry);
			        		if ( mountProviderInfo == null){
			        			schemaRegistry = m_schemaRegistry;
			        		} else {
			        			schemaRegistry = mountProviderInfo.getMountedRegistry();
			        		}
			        		
			        		validateActionOutput(outputElement, actionDef.getOutput().getPath(), schemaRegistry);
			        	} else {
			        		validateRpc( outputElement, (NetconfMessage)response, RequestType.RPC, null);
			        	}
					} catch (ValidationException e) {
						response.setOk(false);
						response.setDataContent(null);
						response.addError(e.getRpcError());
					}
		    	}
		    }
		    
		    if (!response.getErrors().isEmpty()){
		    	response.setData(null);
		    	response.setOk(false);
		    }
		} catch (Exception e) {
		    NetconfRpcError rpcError = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application, 
		            NetconfRpcErrorSeverity.Error, "Internal Server Error");
		    ValidationException exception = new ValidationException(rpcError);
		    exception.addSuppressed(e);
			throw exception;
		}
	}
	private void validateRpc(Element inputElement, NetconfMessage rpc, RequestType requestType, Element rpcContext) throws ValidationException {
		Element rpcElement = inputElement;
		String rpcName = rpc.getRpcName().getName();
		String namespace = rpc.getRpcName().getNamespace();
		MountProviderInfo providerInfo = SchemaRegistryUtil.getMountProviderInfo(rpc.getRpcContext() , m_schemaRegistry);
		SchemaRegistry schemaRegistry = m_schemaRegistry;
		if ( providerInfo != null && providerInfo.getMountedRegistry() != null ){
		    schemaRegistry = providerInfo.getMountedRegistry();;    
		}
		Collection<RpcDefinition> rpcDefinitions = schemaRegistry.getRpcDefinitions();
		RpcDefinition foundRpcDefinition = getRpcDefinition(rpcName, namespace, rpcDefinitions);
		if(null == foundRpcDefinition){
		    throw new ValidationException(NetconfRpcError.getUnknownElementError(rpcName,NetconfRpcErrorType.Application));
		}
		//if rpc request does not need input, we have nothing to validate the input
		ContainerSchemaNode input = foundRpcDefinition.getInput();
		if(rpc.getType().isRequest() && null != input){
			//Collection<DataSchemaNode> children = input.getChildNodes();
			validateRpcMaxMinElements(rpcElement, input.getPath(), schemaRegistry);
			validateDuplicateSiblings(rpcElement, input.getPath(), schemaRegistry, null);
			validateElement(rpcElement, input.getPath(), requestType, rpcElement, m_modelNodeDsm, rpc.getType(), m_modelNodeDSMRegistry, schemaRegistry, rpcContext);
		}
		
		ContainerSchemaNode output = foundRpcDefinition.getOutput();
		if (rpc.getType().isResponse() && null != output && isData(rpcElement, (NetconfRpcResponse)rpc)){
			validateDuplicateSiblings(rpcElement, output.getPath(), schemaRegistry, null);
		    validateElement(rpcElement, output.getPath(), requestType, rpcElement, m_modelNodeDsm, rpc.getType(), m_modelNodeDSMRegistry, schemaRegistry, rpcContext);
		}
	}

    private RpcDefinition getRpcDefinition(String rpcName, String namespace, Collection<RpcDefinition> rpcDefinitions) {
        for (RpcDefinition rpcDefinition : rpcDefinitions) {
			QName qName = rpcDefinition.getQName();
			if (qName.getLocalName().equals(rpcName) && qName.getNamespace().toString().equals(namespace)) {
					RequestScope.getCurrentScope().putInCache(DataStoreValidationUtil.STATE_VALIDATION_MODULE_NS,
                        rpcDefinition.getQName().getNamespace().toString());
					return rpcDefinition;
			}
		}
        return null;
    }

    protected boolean isData(Element element, NetconfRpcResponse response){
    	if (response.isOk() && (response.getData() != null || !response.getRpcOutputElements().isEmpty())){
			throw new RuntimeException("<ok/> cannot be part of response if there are errors or output data");
    	} else if (response.isOk() && response.getData() == null && response.getRpcOutputElements().isEmpty()){
    		return false;
    	} else if (!response.getErrors().isEmpty()){
    		return false;
    	}
    	return true;
    }
    
	private void throwErrorIfOkResponse(Element outputElement) {
		if (outputElement.getNodeType() == Node.ELEMENT_NODE && NetconfResources.OK.equals(outputElement.getNodeName())) {
			throw new ValidationException(
					NetconfRpcError.getApplicationError("<ok/> cannot be part of response if there is output data"));
		}
	}

	private void throwErrorForDuplicateElements(Element element, SchemaPath schemaPath, SchemaRegistry schemaRegistry, DataSchemaNode parentSchemaNode) {
		NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
				String.format("Duplicate elements in node %s", schemaPath.getLastComponent()));
		Pair<String, Map<String, String>> errorPathPair = (parentSchemaNode == null) ? new Pair<String, Map<String, String>>("/", Collections.emptyMap()) : SchemaRegistryUtil.getErrorPath(element.getParentNode(), parentSchemaNode,
				schemaRegistry, element);

		if(errorPathPair != null) {
			error.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
		}
		error.setErrorAppTag(EditContainmentNode.DATA_NOT_UNIQUE);
		throw new ValidationException(error);
	}

	private void validateEditConfig(EditConfigRequest request, RequestType requestType) throws ValidationException {
		EditConfigElement configElement = request.getConfigElement();
		SchemaRegistry schemaRegistry = getSchemaRegistry(null);
		Set<SchemaPath> rootSchemaPaths = schemaRegistry.getRootSchemaPaths();
		List<SchemaPath> schemaPathsForDuplicateCheck = new ArrayList<>();

		configElement.getConfigElementContents().forEach(rootElement -> {
			checkDuplicateElements(schemaRegistry, rootSchemaPaths, schemaPathsForDuplicateCheck, rootElement);
		});

		for (Element rootElement : configElement.getConfigElementContents()) {
			String editOperation;
			editOperation = getEditOperation(rootElement);
			if (EditConfigOperations.DELETE.equals(editOperation) || EditConfigOperations.REMOVE.equals(editOperation)) {
				throw new ValidationException(NetconfRpcError.getBadAttributeError(rootElement.getLocalName(), NetconfRpcErrorType.Application, "Do not allow to remove/delete root node").setErrorPath("/", Collections.emptyMap()));
			}

			validateRootElement(requestType, schemaRegistry, rootSchemaPaths, rootElement);
		}
	}

    private void validateRootElement(RequestType requestType, SchemaRegistry schemaRegistry, Set<SchemaPath> rootSchemaPaths,
            Element rootElement) {
        SchemaPath schemaPath = getSchemaPathForElement(rootElement, rootSchemaPaths);
        if (schemaPath == null) {
            if (m_schemaRegistry.isKnownNamespace(rootElement.getNamespaceURI())) {
                throw new ValidationException(
                        NetconfRpcError.getUnknownElementError(rootElement.getLocalName(), NetconfRpcErrorType.Application)
                                .setErrorPath("/", Collections.emptyMap()));
            }
            throw new ValidationException(NetconfRpcError.getUnknownNamespaceError(rootElement.getNamespaceURI(),
                    rootElement.getLocalName(), NetconfRpcErrorType.Application).setErrorPath("/", Collections.emptyMap()));
        }

        validateDuplicateSiblings(rootElement, schemaPath, schemaRegistry, null);
        validateElement(rootElement, schemaPath, requestType, rootElement, null, null, null, null, null);
    }

    private void checkDuplicateElements(SchemaRegistry schemaRegistry, Set<SchemaPath> rootSchemaPaths,
            List<SchemaPath> schemaPathsForDuplicateCheck, Element rootElement) {
        SchemaPath schemaPathOfRootElement = getSchemaPathForElement(rootElement, rootSchemaPaths);
        if (schemaPathOfRootElement != null
                && schemaRegistry.getDataSchemaNode(schemaPathOfRootElement) instanceof ContainerSchemaNode) {
            if (schemaPathsForDuplicateCheck.contains(schemaPathOfRootElement)) {
                throwErrorForDuplicateElements(rootElement, schemaPathOfRootElement, schemaRegistry,
                        schemaRegistry.getDataSchemaNode(schemaPathOfRootElement));
            } else {
                schemaPathsForDuplicateCheck.add(schemaPathOfRootElement);
            }
        }
    }

    private void validateCopyConfig(CopyConfigRequest request, RequestType requestType) throws ValidationException {

        Element configElement = request.getSourceConfigElement();
        if (configElement != null) {
            NodeList childElements = configElement.getChildNodes();
            SchemaRegistry schemaRegistry = getSchemaRegistry(null);
            Set<SchemaPath> rootSchemaPaths = schemaRegistry.getRootSchemaPaths();
            List<SchemaPath> schemaPathsForDuplicateCheck = new ArrayList<>();
            for (int i = 0; i < childElements.getLength(); i++) {
                Node childNode = childElements.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element rootElement = (Element) childNode;
                    checkDuplicateElements(schemaRegistry, rootSchemaPaths, schemaPathsForDuplicateCheck, rootElement);
                    validateRootElement(requestType, schemaRegistry, rootSchemaPaths, rootElement);

                }
            }
        }
    }

	private void validateMaxMinElements(Element parentElement, SchemaPath parentSchemaPath, SchemaRegistry schemaRegistry, ActionDefinition actionDefinition) {
		List<Element> siblingElements = new ArrayList<>();
		NodeList siblingNodes = parentElement.getChildNodes();
		if (schemaRegistry == null) {
			schemaRegistry = getSchemaRegistry(parentSchemaPath);
		}
		schemaRegistry = SchemaRegistryUtil.retrievePossibleMountRegistry(parentElement, parentSchemaPath, schemaRegistry);
		ActionDefinition schemaNode = schemaRegistry.getActionDefinitionNode(DataPathUtil.convertToDataPath(parentSchemaPath));
		if (schemaNode != null)/*schemaNode instanceof ActionDefinition */ {
			Collection<DataSchemaNode> childrenNode = schemaNode.getInput().getChildNodes();
			validateMinMaxConstrainsInChildren(parentElement, childrenNode, parentSchemaPath, schemaRegistry);

		} else {
			for (int i = 0; i < siblingNodes.getLength(); i++) {
				Node siblingNode = siblingNodes.item(i);
				if (siblingNode.getNodeType() == Node.ELEMENT_NODE) {
					siblingElements.add((Element) siblingNode);
				}
			}
			SchemaRegistry finalSchemaRegistry = schemaRegistry;
			siblingElements.forEach(siblingElement -> {
				SchemaPath schemaPathOfSiblingElement = getChildSchemaPath(finalSchemaRegistry, siblingElement, parentSchemaPath, actionDefinition);
				if (schemaPathOfSiblingElement != null) {
					validateMaxMinElements(siblingElement, schemaPathOfSiblingElement, finalSchemaRegistry, actionDefinition);
				}
			});
		}
	}

	private void validateMinMaxConstrainsInChildren(Element parentElement, Collection<DataSchemaNode> childrenNode, SchemaPath parentSchemaPath, SchemaRegistry schemaRegistry) {
		for (DataSchemaNode child : childrenNode) {
			ElementCountConstraint elementCountConstraint = null;
			if (child instanceof ElementCountConstraintAware) {
				Optional<ElementCountConstraint> optElementCountConstraint = ((ElementCountConstraintAware) child).getElementCountConstraint();
				if (optElementCountConstraint.isPresent()) {
					elementCountConstraint = optElementCountConstraint.get();
				}
				if (elementCountConstraint != null) {
					int elementCount = DocumentUtils.getDirectChildElements(parentElement, child.getQName().getLocalName()).size();
					Integer minElements = elementCountConstraint.getMinElements();
					Integer maxElements = elementCountConstraint.getMaxElements();
					if (minElements != null && elementCount < minElements) {
						ValidationException violateMinException = getViolateMinElementException(child.getQName().getLocalName(), minElements);
						throw violateMinException;
					}
					if (maxElements != null && elementCount > maxElements) {
						ValidationException violateMaxException = getViolateMaxElementException(child.getQName().getLocalName(), maxElements);
						throw violateMaxException;
					}
				}
			}
			if (child instanceof DataNodeContainer) {
				Collection<DataSchemaNode> grandChildren = ((DataNodeContainer) child).getChildNodes();
				List<Element> childElements = DocumentUtils.getDirectChildElements(parentElement, child.getQName().getLocalName(), child.getQName().getNamespace().toString());
				for (Element childElement : childElements) {
					validateMinMaxConstrainsInChildren(childElement, grandChildren, child.getPath(), schemaRegistry);
				}
			}
		}
	}

	private void validateRpcMaxMinElements(Element parentElement, SchemaPath parentSchemaPath, SchemaRegistry schemaRegistry) {
		RpcDefinition schemaNode = schemaRegistry.getRpcDefinition(parentSchemaPath.getParent());
		if (schemaNode != null)/*schemaNode instanceof RpcDefinition */ {
			Collection<DataSchemaNode> childrenNode = schemaNode.getInput().getChildNodes();
			validateMinMaxConstrainsInChildren(parentElement, childrenNode, parentSchemaPath, schemaRegistry);
		}
	}

	private void validateDuplicateSiblings(Element parentElement, SchemaPath parentSchemaPath, SchemaRegistry schemaRegistry, ActionDefinition actionDefinition) {
		List<Element> siblingElements = new ArrayList<>();
		NodeList siblingNodes = parentElement.getChildNodes();
		List<SchemaPath> schemaPaths = new ArrayList<>();

		for (int i = 0; i < siblingNodes.getLength(); i++) {
			Node siblingNode = siblingNodes.item(i);
			if (siblingNode.getNodeType() == Node.ELEMENT_NODE) {
				siblingElements.add((Element) siblingNode);
			}
		}

		siblingElements.forEach(siblingElement -> {
			SchemaPath schemaPathOfSiblingElement = getChildSchemaPath(schemaRegistry, siblingElement, parentSchemaPath, actionDefinition);
			//For Actions, since it is not a regular DataSchemaNode the getChildren(actionSchemaPath) will return null. Hence it is handled as below

			if( schemaPathOfSiblingElement == null && actionDefinition != null) {
				DataSchemaNode actionChildNode = getActionInputChildNode(schemaRegistry.getActionDefinitionNode(DataPathUtil.convertToDataPath(parentSchemaPath)),
						QName.create(parentSchemaPath.getLastComponent(), siblingElement.getLocalName()));
				if (actionChildNode != null) {
					schemaPathOfSiblingElement = actionChildNode.getPath();
				}
			}
			if (schemaPathOfSiblingElement != null
					&& (schemaRegistry.getDataSchemaNode(schemaPathOfSiblingElement) instanceof ContainerSchemaNode
					|| schemaRegistry.getDataSchemaNode(schemaPathOfSiblingElement) instanceof LeafSchemaNode)) {
				if (schemaPaths.contains(schemaPathOfSiblingElement)) {
					throwErrorForDuplicateElements(siblingElement, schemaPathOfSiblingElement, schemaRegistry, schemaRegistry.getDataSchemaNode(parentSchemaPath));
				} else {
					schemaPaths.add(schemaPathOfSiblingElement);
				}
			}

		});

		siblingElements.forEach(siblingElement -> {
			SchemaPath schemaPathOfSiblingElement = getChildSchemaPath(schemaRegistry, siblingElement, parentSchemaPath, actionDefinition);
			if (schemaPathOfSiblingElement != null) {
				validateDuplicateSiblings(siblingElement, schemaPathOfSiblingElement, schemaRegistry, actionDefinition);
			}
		});
	}

	private void validateAction(Element rootElement, RequestType requestType, ActionDefinition actionDefinition) throws ValidationException {
		Set<SchemaPath> rootSchemaPaths = getSchemaRegistry(null).getRootSchemaPaths();

		SchemaPath schemaPath = getSchemaPathForElement(rootElement, rootSchemaPaths);
		if(schemaPath == null){
			if(m_schemaRegistry.isKnownNamespace(rootElement.getNamespaceURI())) {
				throw new ValidationException(NetconfRpcError.getUnknownElementError(rootElement.getLocalName(), NetconfRpcErrorType.Application).setErrorPath("/", Collections.emptyMap()));
			}
			throw new ValidationException(NetconfRpcError.getUnknownNamespaceError(rootElement.getNamespaceURI(),
					rootElement.getLocalName(), NetconfRpcErrorType.Application).setErrorPath("/", Collections.emptyMap()));
		}
		validateMaxMinElements(rootElement, schemaPath, m_schemaRegistry, actionDefinition);
		validateDuplicateSiblings(rootElement, schemaPath, m_schemaRegistry, actionDefinition);
		validateElement(rootElement, schemaPath, requestType, rootElement, m_modelNodeDsm, NetconfRpcPayLoadType.REQUEST, m_modelNodeDSMRegistry, m_schemaRegistry, rootElement);
	}
	
	private void validateActionOutput(Element outputElement, SchemaPath outputPath, SchemaRegistry schemaRegistry) throws ValidationException{
		throwErrorIfOkResponse(outputElement);
		SchemaPath elementSchemaPath = getChildSchemaPath(schemaRegistry, outputElement, outputPath, null);
		validateDuplicateSiblings(outputElement, elementSchemaPath, schemaRegistry, null);
		validateElement(outputElement, elementSchemaPath, RequestType.ACTION, outputElement, m_modelNodeDsm, NetconfRpcPayLoadType.RESPONSE, m_modelNodeDSMRegistry, schemaRegistry, outputElement);
	}
	
	@Override
	protected void validateElement(Element element, SchemaPath schemaPath, RequestType requestType, Element rootElement,
			ModelNodeDataStoreManager modelNodeDsm, NetconfRpcPayLoadType rpcType, ModelNodeDSMRegistry dsmRegistry, SchemaRegistry schemaRegistry, Element rpcContext) throws ValidationException {
		// reject state attribute from edit-config
	    if (schemaRegistry == null) {
	        schemaRegistry = m_schemaRegistry;
	    }
		DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
		// all 'config' statements for nodes in the input/output tree are ignored
		boolean skipConfigFalseNodes = false;
		if (requestType.isAction() || requestType.isRpc()) {
			skipConfigFalseNodes = true;
		}
	    if (dataSchemaNode!=null && !dataSchemaNode.isConfiguration() && !skipConfigFalseNodes) {
	    	NetconfRpcError rpcError = NetconfRpcError.getUnknownElementError(dataSchemaNode.getQName().getLocalName(), NetconfRpcErrorType.Application);
	    	DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(schemaPath);
	    	Pair<String, Map<String, String>> errorPathPair = (parentSchemaNode == null) ? new Pair<String, Map<String, String>>("/", Collections.emptyMap()) : SchemaRegistryUtil.getErrorPath(element.getParentNode(), parentSchemaNode,
	                schemaRegistry, element);
	    	rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
	    	if (!dataSchemaNode.isConfiguration()) {
		    	rpcError.setErrorMessage(element.getLocalName() + " is config false");
	    	}
	    	throw new ValidationException(rpcError);
	    }
	    
	    super.validateElement(element, schemaPath, requestType, rootElement, modelNodeDsm, rpcType, dsmRegistry, schemaRegistry, rpcContext);
	}
	
	public static String getInsertAttributes(Element element,String localName) {
		String attribute = element.getAttributeNS(NetconfResources.NETCONF_YANG_1, localName);
		if (attribute == null || attribute.isEmpty()) {
			return null;
		} else {
			return attribute;
		}
	}
	
	public static String getEditOperation(Element editConfigXml) throws ValidationException {
	    String operation = editConfigXml.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.EDIT_CONFIG_OPERATION);
	    boolean isOperationPresent = editConfigXml.hasAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION);
        if(isOperationPresent && EMPTY_STR.equals(operation)) {
                throw new ValidationException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Invalid <edit-config> operation"));
        }
	    if (operation != null && !EMPTY_STR.equals(operation)) {
	        if (EditConfigOperations.MERGE.equals(operation)) {
	            return EditConfigOperations.MERGE;
	        }
	        if (EditConfigOperations.DELETE.equals(operation)) {
	            return EditConfigOperations.DELETE;
	        }
	        if (EditConfigOperations.CREATE.equals(operation)) {
	            return EditConfigOperations.CREATE;
	        }
	        if (EditConfigOperations.REMOVE.equals(operation)) {
	            return EditConfigOperations.REMOVE;
	        }
	        if (EditConfigOperations.REPLACE.equals(operation)) {
	            return EditConfigOperations.REPLACE;
	        }
	        LOGGER.error("Invalid <edit-config> operation :{}" , operation);
	        throw new ValidationException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
	                "Invalid <edit-config> operation : " + operation));
	    }
	    return null;
	
	}
	
   @Override
   protected void validateAttributes(Element parentElement, Element childNode, SchemaPath schemaPath) throws ValidationException {
       NamedNodeMap attributes = childNode.getAttributes();
       if (attributes != null) {
           for (int i = 0; i < attributes.getLength(); i++) {
               Node attributeNode = attributes.item(i);
               String namespace = attributeNode.getNamespaceURI();
               if (namespace == null) {
                   namespace = "";
               }
               String attrName = attributeNode.getNodeName();
               String[] attrNameSplit = attrName.split(":");
               String localAttrName = attrNameSplit[attrNameSplit.length-1];
               if(namespace.isEmpty() && NetconfResources.TYPE.equals(localAttrName)) {
               		namespace = childNode.getNamespaceURI();
			   }
               if (!(namespace.equals(NetconfResources.XMLNS_NS)
				   || NetconfResources.KNOWN_ATTRIBUTES.contains(QName.create(namespace, localAttrName))
			       || isPasswordAttribute(localAttrName))) {
                   LOGGER.error("Invalid attribute: {}, {}", namespace, localAttrName);
                   SchemaRegistry schemaRegistry = getSchemaRegistry(schemaPath);
                   DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
                   Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(parentElement, parentSchemaNode, schemaRegistry, childNode);
                   NetconfRpcError rpcError = NetconfRpcError.getBadAttributeError(localAttrName, NetconfRpcErrorType.Application,
                           String.format("Bad attribute: namespace '%s', attribute name '%s'", namespace, localAttrName));
                   rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
                   throw new ValidationException(rpcError);
               }
           }
       }
   }

	private boolean isPasswordAttribute(String localAttrName) {
		boolean canIgnore = false;
		if(NetconfExtensions.IS_PASSWORD.getModuleName().equals(localAttrName)){
			Object fromCache = RequestScope.getCurrentScope().getFromCache(IGNORE_PASSWORD_ATTRIBUTE);
			canIgnore = fromCache != null ? (boolean) fromCache : false;
		}
		return canIgnore;
	}

	@Override
	protected void validateOperation(Element parentElement, Element childNode, SchemaPath schemaPath) throws ValidationException {
		SchemaRegistry schemaRegistry = getSchemaRegistry(schemaPath);
	    String childOperation = getEditOperation(childNode);
	    String parentOperation = getEditOperation(parentElement);
	    // Child cannot override operation if parent is set to "replace".
	    if (null != childOperation && null != parentOperation && EditConfigDefaultOperations.REPLACE.equals(parentOperation) && !EditConfigDefaultOperations.REPLACE.equals(childOperation)) {
	        DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
	        Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(parentElement, parentSchemaNode, schemaRegistry, childNode);
	        throw new ValidationException(NetconfRpcError.getBadAttributeError(childNode.getLocalName(), NetconfRpcErrorType.Application, "Child cannot override operation if parent is set to replace").setErrorAppTag("replace-violation").setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond()));
	    }
	}

	public static InsertOperation getInsertOperation(String insert, String value) throws ValidationException {
		if(insert == null){
			return null;
		} else if (!EMPTY_STR.equals(insert) && (insert.equals(InsertOperation.FIRST) || insert.equals(InsertOperation.LAST) || insert.equals(InsertOperation.BEFORE) || insert.equals(InsertOperation.AFTER))) {
			return InsertOperation.get(insert, value);
		} else {
			LOGGER.error("Invalid <edit-config> insert operation :{}" , insert);
			NetconfRpcError rpcError = NetconfRpcError.getBadAttributeError(NetconfResources.INSERT, NetconfRpcErrorType.Application,
					String.format("Bad insert attribute : %s", insert));
			rpcError.setErrorAppTag("missing-instance");
			throw new ValidationException(rpcError);
		}
	}
    
	@Override
	protected void validateInsertAttributes(Element parentElement, Element childNode, SchemaPath schemaPath) throws ValidationException {
		String insert = getInsertAttributes(childNode, NetconfResources.INSERT);
		InsertOperation insertOperation = getInsertOperation(insert, null); // not care the value attribute
		if (insertOperation != null) { //if insert present
			// if it's the attribute "after" or "before" , the element must be a list or leaf-list
			if (InsertOperation.AFTER.equals(insertOperation.getName()) || InsertOperation.BEFORE.equals(insertOperation.getName())) {
				DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(schemaPath);
				if (!(dataSchemaNode instanceof ListSchemaNode) && !(dataSchemaNode instanceof LeafListSchemaNode)) {
					getUnknownAttributeError(NetconfResources.INSERT, String.format("The attribute '%s' is not for element %s ", insert, childNode.getLocalName()));
				}
				
			}
		}
	}
	
	private NetconfRpcError getUnknownAttributeError(String attributeName, String errorMessage) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.UNKNOWN_ATTRIBUTE,
				errorMessage);
		return rpcError;
	}
	
	@Override
	protected void typeValidation(Element element, SchemaPath schemaPath, RequestType requestType, SchemaRegistry schemaRegistry) throws ValidationException {
	    if(isContainerSchemaNode(schemaPath) && !DataStoreValidationUtil.needsFurtherValidation(element, requestType)){
	    	return;
	    }
	    super.typeValidation(element, schemaPath, requestType, schemaRegistry);
	}
	
	private boolean isContainerSchemaNode(SchemaPath schemaPath) {
		return m_schemaRegistry.getDataSchemaNode(schemaPath) instanceof ContainerSchemaNode;
	}

	@Override
	public ModelNodeDataStoreManager getDsm() {
		return m_modelNodeDsm;
	}

    @Override
    public DSExpressionValidator getExpressionValidator() {
        return m_expressionValidator;
    }
	
}
