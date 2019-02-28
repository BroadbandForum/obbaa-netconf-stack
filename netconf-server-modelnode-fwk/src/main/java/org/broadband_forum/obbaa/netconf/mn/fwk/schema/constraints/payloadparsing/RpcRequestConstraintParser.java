package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.NetconfRpcPayLoadType;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.MountProviderInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
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
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RpcRequestConstraintParser extends SchemaElementChildrenConstraintParser implements RpcPayloadConstraintParser {
	private static final String EMPTY_STR = "";
	private final SchemaRegistry m_schemaRegistry;
	private final ModelNodeDataStoreManager m_modelNodeDsm;
	private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
	private final DSExpressionValidator m_expressionValidator;
	
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(RpcRequestConstraintParser.class, LogAppNames.NETCONF_STACK);


	public RpcRequestConstraintParser(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDSM, DSExpressionValidator validator) {
		m_schemaRegistry = schemaRegistry;
		m_modelNodeDsm = modelNodeDSM;
		m_expressionValidator = validator;
	}

	protected SchemaRegistry getSchemaRegistry(SchemaPath schemaPath) {
		if (schemaPath != null) {
			DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(schemaPath);
			if (schemaNode == null) {
				SchemaRegistry mountRegistry = SchemaRegistryUtil.getMountRegistry();
				if (mountRegistry != null) {
					schemaNode = mountRegistry.getDataSchemaNode(schemaPath);
					if (schemaNode != null) {
						return mountRegistry;
					}
				}
			}
		}
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
		if(requestType.isRpc()){
			validateRpc(((NetconfRpcRequest) netconfRequest).getRpcInput(),(NetconfMessage) netconfRequest, requestType, ((NetconfRpcRequest)netconfRequest).getRpcContext());
		} else if(requestType.isAction()){
			Element actionTreeElement = ((ActionRequest)netconfRequest).getActionTreeElement();            
			validateAction(actionTreeElement, requestType);
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
			        		validateActionOutput(outputElement, actionDef.getOutput().getPath());
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
			validateElement(rpcElement, input.getPath(), requestType, rpcElement, m_modelNodeDsm, rpc.getType(), m_modelNodeDSMRegistry, schemaRegistry, rpcContext);
		}
		
		ContainerSchemaNode output = foundRpcDefinition.getOutput();
		if (rpc.getType().isResponse() && null != output && isData(rpcElement, (NetconfRpcResponse)rpc)){
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
    
	private void validateEditConfig(EditConfigRequest request, RequestType requestType) throws ValidationException {
	    EditConfigElement configElement = request.getConfigElement();
	    Set<SchemaPath> rootSchemaPaths = getSchemaRegistry(null).getRootSchemaPaths();
	    for(Element rootElement : configElement.getConfigElementContents()){
			String editOperation;
			editOperation = getEditOperation(rootElement);
	        if(EditConfigOperations.DELETE.equals(editOperation) || EditConfigOperations.REMOVE.equals(editOperation)){
			    throw new ValidationException(NetconfRpcError.getBadAttributeError(rootElement.getLocalName(), NetconfRpcErrorType.Application, "Do not allow to remove/delete root node").setErrorPath("/", Collections.emptyMap()));
			}
			
	        SchemaPath schemaPath = getSchemaPathForElement(rootElement, rootSchemaPaths);
	        if(schemaPath == null){
	            if(m_schemaRegistry.isKnownNamespace(rootElement.getNamespaceURI())) {
	                throw new ValidationException(NetconfRpcError.getUnknownElementError(rootElement.getLocalName(), NetconfRpcErrorType.Application).setErrorPath("/", Collections.emptyMap()));
	            }
				throw new ValidationException(NetconfRpcError.getUnknownNamespaceError(rootElement.getNamespaceURI(),
						rootElement.getLocalName(), NetconfRpcErrorType.Application).setErrorPath("/", Collections.emptyMap()));
	        }
	        validateElement(rootElement, schemaPath, requestType, rootElement, null, null, null, null, null);
	    }
	}

	private void validateAction(Element rootElement, RequestType requestType) throws ValidationException {
		Set<SchemaPath> rootSchemaPaths = getSchemaRegistry(null).getRootSchemaPaths();

		SchemaPath schemaPath = getSchemaPathForElement(rootElement, rootSchemaPaths);
		if(schemaPath == null){
			if(m_schemaRegistry.isKnownNamespace(rootElement.getNamespaceURI())) {
				throw new ValidationException(NetconfRpcError.getUnknownElementError(rootElement.getLocalName(), NetconfRpcErrorType.Application).setErrorPath("/", Collections.emptyMap()));
			}
			throw new ValidationException(NetconfRpcError.getUnknownNamespaceError(rootElement.getNamespaceURI(),
					rootElement.getLocalName(), NetconfRpcErrorType.Application).setErrorPath("/", Collections.emptyMap()));
		}
		validateElement(rootElement, schemaPath, requestType, rootElement, null, NetconfRpcPayLoadType.REQUEST, m_modelNodeDSMRegistry, m_schemaRegistry, null);
	}
	
	private void validateActionOutput(Element outputElement, SchemaPath outputPath) throws ValidationException{
		throwErrorIfOkResponse(outputElement);
		SchemaPath elementSchemaPath = getChildSchemaPath(m_schemaRegistry, outputElement, outputPath);
		validateElement(outputElement, elementSchemaPath, RequestType.ACTION, outputElement, m_modelNodeDsm, NetconfRpcPayLoadType.RESPONSE, m_modelNodeDSMRegistry, m_schemaRegistry, null);
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
			return new InsertOperation(insert, value);
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
	protected void typeValidation(Element element, SchemaPath schemaPath, RequestType requestType) throws ValidationException {
	    if(isContainerSchemaNode(schemaPath) && !DataStoreValidationUtil.needsFurtherValidation(element, requestType)){
	    	return;
	    }
	    super.typeValidation(element, schemaPath, requestType);
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
