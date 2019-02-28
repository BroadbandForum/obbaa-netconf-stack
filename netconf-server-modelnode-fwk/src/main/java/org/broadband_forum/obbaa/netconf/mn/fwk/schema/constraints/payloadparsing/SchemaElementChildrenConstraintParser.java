package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.CURRENT_PATTERN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.NetconfRpcPayLoadType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ModelNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.YangStep;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ActionUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.OperationAttributeUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks if there are invalid nodes in the children of a given node.
 *
 * Created by keshava on 11/24/15.
 */
public abstract class SchemaElementChildrenConstraintParser {
	static final String OPEN_SQUARE="[";
	static final String CLOSE_SQUARE="]";
	
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SchemaElementChildrenConstraintParser.class, LogAppNames.NETCONF_STACK);
	
	public abstract ModelNodeDataStoreManager getDsm();
	
	public abstract DSExpressionValidator getExpressionValidator(); 
	
	protected SchemaPath getChildSchemaPath(SchemaRegistry schemaRegistry, Element element, SchemaPath parentPath){
		schemaRegistry = getMountRegistryIfApplicable(schemaRegistry, parentPath);
        Collection<DataSchemaNode> children = schemaRegistry.getChildren(parentPath);
        SchemaPath childSchemaPath = getSchemaPathForElement(element, getSchemaPathsForNodes(children));
	    return childSchemaPath;
	}
    
    private SchemaRegistry getMountRegistryIfApplicable(SchemaRegistry schemaRegistry, SchemaPath parentPath) {
    	DataSchemaNode dsn = schemaRegistry.getDataSchemaNode(parentPath);
    	SchemaRegistry mountRegistry = (SchemaRegistry) RequestScope.getCurrentScope().getFromCache(SchemaRegistryUtil.MOUNT_CONTEXT_SCHEMA_REGISTRY);
    	if (dsn == null && mountRegistry != null){
    		return mountRegistry;
    	}
    	return schemaRegistry;
	}

	/**
     * Iterates though child nodes to search for invalid nodes.
     * Override this method to perform more specific checks on the parent node.
     * @param element - parent element
     * @param schemaPath - schema path of the parent
     * @param modelNodeDsm - DataStoreManager
     * @throws ValidationException
     */

    protected void validateElement(Element element, SchemaPath schemaPath, RequestType requestType, Element rootElement,
            ModelNodeDataStoreManager modelNodeDsm, NetconfRpcPayLoadType rpcType, ModelNodeDSMRegistry dsmRegistry, SchemaRegistry schemaRegistry, Element rpcContext) throws ValidationException {
        if (schemaRegistry == null) {
            schemaRegistry = getSchemaRegistry(schemaPath);
        }
        if (AnvExtensions.MOUNT_POINT.isExtensionIn(schemaRegistry.getDataSchemaNode(schemaPath))) {
        	SchemaRegistryUtil.resetSchemaRegistryCache();
			SchemaMountRegistryProvider provider = schemaRegistry.getMountRegistry().getProvider(schemaPath);
            if (provider != null) {
                SchemaRegistry mountRegistry = null;
				try {
					mountRegistry = provider.getSchemaRegistry(null, element);
				} catch (GetException e) {
					throw new ValidationException(e.getRpcError());
				}
                schemaRegistry = mountRegistry != null ? mountRegistry : schemaRegistry;
            }
        }
        throwErrorIfActionExistsWithinRPC(element, schemaPath, requestType, schemaRegistry, element);
        throwErrorIfActionExistsWithinNotification(element, schemaPath, requestType, schemaRegistry, element);
        
        //go though children
        NodeList nodes = element.getChildNodes();
        List<SchemaPath> childrenElementNodeSchemaPath = new ArrayList<>();
        List<Element> childrenElement = new ArrayList<>();
        for(int i=0 ; i< nodes.getLength(); i++){
            Node childNode = nodes.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                throwErrorIfActionExistsWithinRPC(element, schemaPath, requestType, schemaRegistry, childNode);
                throwErrorIfActionExistsWithinNotification(element, schemaPath, requestType, schemaRegistry, childNode);
                Collection<DataSchemaNode> children = schemaRegistry.getChildren(schemaPath);
                SchemaPath childSchemaPath = getSchemaPathForElement((Element) childNode, getSchemaPathsForNodes(children));
                
                if(childSchemaPath == null) {
                    if(requestType.isAction()){
                        ActionDefinition actionDef = getMatchedActionDefinition((Element)childNode, schemaPath);
                        if(actionDef != null){
                            if(actionDef.getInput() != null) {
                                childSchemaPath = actionDef.getInput().getPath();
                            } else {
                                break;
                            }
                        }
                    }
                }
                /**
                 * During RPC output validation, the 'schemaPath' received and the 'childNode' for which we are getting a null value,
                 * are child-grand-parent relation. So we need to get the schemaPath of 'element' and look for the childNode. 
                 */
                if (childSchemaPath == null){
                    SchemaPath parentPath = getSchemaPathForElement(element, getSchemaPathsForNodes(children));
                    if (parentPath!=null){
                        Collection<DataSchemaNode> allChildren = schemaRegistry.getChildren(parentPath);
                        childSchemaPath = getSchemaPathForElement((Element) childNode, getSchemaPathsForNodes(allChildren));
                    }
                    
                }
                
                if(childSchemaPath == null){
                    childSchemaPath = getSchemaPathForElement((Element)childNode, getSchemaPathFromCases(children));
                    if(childSchemaPath == null) {
                        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);

                        if (!(dataSchemaNode instanceof AnyXmlSchemaNode)) {
                            Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(element, dataSchemaNode, schemaRegistry, (Element)childNode);
                            NetconfRpcError rpcError;
                            if(schemaRegistry.isKnownNamespace(childNode.getNamespaceURI())) {
                                rpcError = NetconfRpcError.getUnknownElementError(childNode.getLocalName(), NetconfRpcErrorType.Application);
                            } else {
                                rpcError = NetconfRpcError.getUnknownNamespaceError(childNode.getNamespaceURI(), childNode.getLocalName(), NetconfRpcErrorType.Application);
                            }

                            rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
                            throw new ValidationException(rpcError);
                        }else{
                            break; // To exclude validation of xml content of anyXml node
                        }
                    }
                }

                validateOperation(element, (Element) childNode, schemaPath);
                validateInsertAttributes(element, (Element) childNode, childSchemaPath);
                validateElement((Element) childNode, childSchemaPath, requestType, rootElement, modelNodeDsm, rpcType, dsmRegistry, schemaRegistry, rpcContext);
                childrenElementNodeSchemaPath.add(childSchemaPath);
                childrenElement.add((Element) childNode);
            }
        }

        validateConstraint(element, schemaPath, requestType, rootElement, schemaRegistry, rpcType);
        validateChoicecase(childrenElementNodeSchemaPath, childrenElement, schemaPath, requestType, element, schemaRegistry, rpcType);
        typeValidation(element, schemaPath, requestType);
        if (requestType.isRpc() || requestType.isAction()){
            validateLeafType(childrenElement, childrenElementNodeSchemaPath, schemaRegistry, element, rootElement, modelNodeDsm, dsmRegistry, rpcContext);
        }
        
    }

	private void throwErrorIfActionExistsWithinNotification(Element element, SchemaPath schemaPath, RequestType requestType,
			SchemaRegistry schemaRegistry, Node childNode) {
		if(requestType.isAction() && checkNotificationExistsOnActionTree((Element)childNode, schemaPath)) {
			NetconfRpcError rpcError = getRpcError(element, schemaPath, schemaRegistry, childNode);
			rpcError.setErrorMessage("Notification Element " +childNode.getLocalName() + " should not exist within Action Tree");
			throw new ValidationException(rpcError);
		}
	}

	private void throwErrorIfActionExistsWithinRPC(Element element, SchemaPath schemaPath, RequestType requestType,
			SchemaRegistry schemaRegistry, Node childNode) {
		if(requestType.isAction() && checkRPCExistsOnActionTree((Element)childNode, schemaPath)) {
			NetconfRpcError rpcError = getRpcError(element, schemaPath, schemaRegistry, childNode);
			rpcError.setErrorMessage("RPC Element " +childNode.getLocalName() + " should not exist within Action Tree");
			throw new ValidationException(rpcError);
		}
	}

	private ActionDefinition getMatchedActionDefinition(Element childNode, SchemaPath schemaPath){
		Set<ActionDefinition> actionDefs = ActionUtils.retrieveActionDefinitionForSchemaNode(schemaPath, getSchemaRegistry(schemaPath));
		for(ActionDefinition action : actionDefs){
			QName actionQName = action.getQName();
			if(actionQName.getNamespace().toString(). equals(childNode.getNamespaceURI()) && actionQName.getLocalName().equals(childNode.getLocalName())){
				return action;
			}
		}
		return null;
	}

	private NetconfRpcError getRpcError(Element element, SchemaPath schemaPath, SchemaRegistry schemaRegistry,
			Node childNode) {
		DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
		Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(element, dataSchemaNode, schemaRegistry, (Element)childNode);
		NetconfRpcError rpcError = NetconfRpcError.getBadElementError(childNode.getLocalName(), NetconfRpcErrorType.Protocol);

		rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
		return rpcError;
	}

	private boolean checkRPCExistsOnActionTree(Element element, SchemaPath schemaPath){
		SchemaRegistry schemaRegistry = getSchemaRegistry(schemaPath);
		Collection<RpcDefinition> rpcDefinitions = schemaRegistry.getRpcDefinitions();
		for (RpcDefinition rpcDefinition : rpcDefinitions) {
			QName qName = rpcDefinition.getQName();
			if (qName.getLocalName().equals(element.getLocalName()) && qName.getNamespace().toString().equals(element.getNamespaceURI())) {
				return true;
			}
		}
		return false;
	}

	private boolean checkNotificationExistsOnActionTree(Element element, SchemaPath schemaPath){
		SchemaRegistry schemaRegistry = getSchemaRegistry(schemaPath);
		@SuppressWarnings("deprecation")
		Collection<NotificationDefinition> notificationDefinitions = schemaRegistry.getSchemaContext().getNotifications();
		for (NotificationDefinition notificationDefinition : notificationDefinitions) {
			QName qName = notificationDefinition.getQName();
			if (qName.getLocalName().equals(element.getLocalName()) && qName.getNamespace().toString().equals(element.getNamespaceURI())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * check if the leafs are of instance identifier or leaf-ref and do a
	 * validation
	 */
	private void validateLeafType(List<Element> childElement, List<SchemaPath> childSchemaPath,
			SchemaRegistry schemaRegistry, Element currentElement, Element rootElement,
			ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry, Element rpcContext) throws ValidationException, DOMException {
		int index = 0;
		for (SchemaPath schemaPath : childSchemaPath) {
			DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
			if (schemaNode != null && schemaNode instanceof LeafSchemaNode) {
				Element currentChild = childElement.get(index);
				String xPathValue = currentChild.getTextContent();
				if (((LeafSchemaNode) schemaNode).getType() instanceof InstanceIdentifierTypeDefinition) {
					boolean isRequired = ((InstanceIdentifierTypeDefinition) ((LeafSchemaNode) schemaNode).getType())
							.requireInstance();
					if (isRequired) {
						Object value = validateXPath(currentChild, currentChild, xPathValue);
						if (value == null) {
							validateOnDataStore(schemaRegistry, xPathValue, currentChild.getTextContent(),
									modelNodeDsm, currentElement, schemaNode, dsmRegistry, rpcContext);
						}
						if (value == null || (value instanceof Boolean && !((Boolean) value))) {
							prepareMissingDataException(schemaRegistry, rootElement, currentChild, xPathValue);

						}
					}
				} else if (((LeafSchemaNode) schemaNode).getType() instanceof LeafrefTypeDefinition) {
				    if (schemaNode.isAddedByUses()){
				        // FIXME: FNMS-10117 Currently grouping nodes are not validated.
				        return;
				    }
				    Object value;
					LeafrefTypeDefinition type = (LeafrefTypeDefinition) ((LeafSchemaNode) schemaNode).getType();
					if (type.requireInstance()) {
					    String xPath = type.getPathStatement().toString();

					    // leafRef paths referring to a state/config leaf are resolved here
					    value = validateXPath(currentElement, currentChild, xPath);
					    if (value == null) {
					        value = validateOnDataStore(schemaRegistry, xPath, currentChild.getTextContent(),
					                modelNodeDsm, currentElement, schemaNode, dsmRegistry, rpcContext);
					        LOGGER.debug(null, "value is {} for xPath-{} schemaNode-{} currentElement-{}", value, xPath, schemaNode,
					                currentElement);
					    }
					    if (value == null || (value instanceof Boolean && !((Boolean) value))
					            || (!(value instanceof Boolean) && !(value.equals(xPathValue)))) {
					        prepareMissingDataException(schemaRegistry, rootElement, currentChild, xPathValue);
					    }
					}
				}
			}
			index++;
		}
	}

	private void prepareMissingDataException(SchemaRegistry schemaRegistry, Element rootElement, Element currentChild, String xPathValue) throws ValidationException {
		StringBuilder builder = new StringBuilder();
		getAbsolutePathToParent(rootElement, currentChild, builder, schemaRegistry);
		String namespace = currentChild.getNamespaceURI();
		String prefix= schemaRegistry.getPrefix(namespace);
		Map<String, String> nsContext = new HashMap<>();
		nsContext.put(prefix, namespace);
		throw DataStoreValidationErrors.getMissingDataException(
                String.format("Missing required element %s", xPathValue), builder.toString(), nsContext);
	}

	private boolean validateOnDataStore(SchemaRegistry schemaRegistry, String xPath, String leafRefValue, 
			ModelNodeDataStoreManager modelNodeDsm, Element currentElement, DataSchemaNode schemaNode, ModelNodeDSMRegistry dsmRegistry, Element rpcContext) throws ValidationException {
		boolean isValid = false;
		List<ModelNode> nodesList;
		SchemaRegistry globalRegistry = schemaRegistry;
		if(schemaRegistry.getParentRegistry() != null){
			globalRegistry = schemaRegistry.getParentRegistry();
		}
		// Get the parent model node from rpcContext.
		ModelNode parentModelNode = ModelNodeUtil.getParentModelNode(rpcContext, globalRegistry, modelNodeDsm);
		// If parentmodelnode is null, then it is a root node.
		ModelNodeId parentModelNodeId = parentModelNode != null ? parentModelNode.getModelNodeId() : new ModelNodeId();
		Iterator<SchemaPath> schemaPaths = schemaRegistry.getRootSchemaPaths().iterator();
		while (schemaPaths.hasNext() && !isValid) {
			SchemaPath schemaPath = schemaPaths.next();
			ModelNodeDSMRegistry dsmRegistryCache = SchemaMountRegistryProvider.getCurrentDsmRegistry();
			if(dsmRegistryCache != null){
				dsmRegistry = dsmRegistryCache;
			}
			if (schemaPath != null) {
				if (dsmRegistry != null && dsmRegistry.lookupDSM(schemaPath) != null) {
					nodesList = dsmRegistry.lookupDSM(schemaPath).listChildNodes(schemaPath, parentModelNodeId);
				} else {
					nodesList = modelNodeDsm.listChildNodes(schemaPath, parentModelNodeId);
				}
				for (ModelNode modelNode : nodesList) {
					isValid = getExpressionValidator().validateXPathInModelNode(xPath, modelNode,
							leafRefValue, currentElement, schemaNode);
					if (isValid) {
						break;
					}
				}
			}
		}
		return isValid;
	}

	private void getAbsolutePathToParent(Element rootElement, Node currentElement, StringBuilder path, SchemaRegistry schemaRegistry) {
		if (!rootElement.equals(currentElement)) {
			if (currentElement instanceof Element) {
				String value = currentElement.getPrefix();
				String colon = DataStoreValidationUtil.COLON;
				if (value == null) {
					String namespace = currentElement.getNamespaceURI();
					String prefix = schemaRegistry.getPrefix(namespace);
					path.insert(0, DataStoreValidationUtil.SLASH + prefix + colon + currentElement.getLocalName());
				} else {
					path.insert(0, DataStoreValidationUtil.SLASH + value + colon + currentElement.getLocalName());
				}

			}

			if (currentElement.getParentNode()!=null){
				getAbsolutePathToParent(rootElement, currentElement.getParentNode(), path, schemaRegistry );
			}
		}
		
	}
	
	private void validateConstraint(Element element, SchemaPath schemaPath, RequestType requestType,
			Element rootElement, SchemaRegistry schemaRegistry, NetconfRpcPayLoadType rpcType) throws ValidationException {
		// For RPC requests, when validation should be done on the input request element
        if (requestType.isRpc()) {
        	DataSchemaNode schemaNode = null;
        	
        	if (rpcType.isResponse()){
        		/**
        		 *  In the output validation, below is the format of the xml
        		 *  <rpc-reply> 
        		 *  	<device-holder/>  
        		 *  	<device-name/>
        		 *  <rpc-reply>
        		 *  
        		 *  Here 
        		 *  schemaPath is of Rpc Output element 
        		 *  element could be device-holder
        		 *  In this case, the right schemaPath of the element must be found and the
        		 *  right constraints must be validated
        		 */
				QName outputQName = schemaPath.getLastComponent();
				SchemaPath elementSchemaPath = null;
		
				if (!outputQName.getNamespace().toString().equals(element.getNamespaceURI())
						|| !outputQName.getLocalName().equals(element.getLocalName())) {
					elementSchemaPath = getChildSchemaPath(schemaRegistry, element, schemaPath);
				} else {
					elementSchemaPath = schemaPath;
				}
        		schemaNode = schemaRegistry.getDataSchemaNode(elementSchemaPath);
        	} else{
        		// this is rpc input validation
                schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        	}
        	
        	if (schemaNode != null){
        	    Optional<RevisionAwareXPath> optWhenCondition = schemaNode.getWhenCondition();
                if (optWhenCondition.isPresent()) {
                    RevisionAwareXPath whenCondition = optWhenCondition.get();
                    Element contextElement = element;
                    if (schemaNode instanceof ChoiceSchemaNode
                            || schemaNode instanceof CaseSchemaNode) {
                        contextElement = (Element)contextElement.getParentNode();
                    }
                    List<QName> contextNodePath = new ArrayList<>();
                    schemaNode.getPath().getPathFromRoot().forEach(contextNodePath::add);
                    validateWhen(rootElement, contextElement, element,
                            buildChoiceCasePath(element, JXPathUtils.getExpression(whenCondition.toString()), schemaNode),
                            whenCondition.toString(), contextNodePath);
                }
                
                if (schemaNode instanceof MustConstraintAware) {
                    Collection<MustDefinition> mustConstraints = ((MustConstraintAware) schemaNode).getMustConstraints();
                    if (mustConstraints != null && !mustConstraints.isEmpty()) {
                        validateMust(rootElement, element, mustConstraints, schemaNode);
                    }
                }
        	}
        }
	}

	private void validateWhen(Element rootElement, Element contextElement, Element currentElement, Expression xPath, String revisionAwareXPath, List<QName> contextNodePath) throws ValidationException {
		Object value = validateXPath(contextElement, currentElement, xPath, contextNodePath);
		if (value != null && value instanceof Boolean && !(Boolean)value) {
		    LOGGER.error("Validation failed for xpath - {} rootElement - {} contextElement - {} currentElement - {}", xPath, rootElement, contextElement, currentElement);
			throw DataStoreValidationErrors.getViolateWhenConditionExceptionThrownUnknownElement(revisionAwareXPath);
		}
	}

	private void validateMust(Element rootElement, Element currentElement, Collection<MustDefinition> mustConstraints, DataSchemaNode schemaNode) throws ValidationException {
		for (MustDefinition mustDefinition: mustConstraints) {
			String xPath = mustDefinition.getXpath().toString();
            Expression ex = buildChoiceCasePath(currentElement, JXPathUtils.getExpression(xPath), schemaNode);
            List<QName> contextNodePath = new ArrayList<>();
            schemaNode.getPath().getPathFromRoot().forEach(contextNodePath::add);
            Object value = validateXPath(rootElement, currentElement, ex, contextNodePath);
            if (value != null && value instanceof Boolean && !(Boolean)value) {
                throw DataStoreValidationErrors.getViolateMustContrainsException(mustDefinition);
            }
		}
	}
	
	private Object validateXPath(Element contextElement, Element currentElement, Expression expression, List<QName> contextNodePath) {
	    Object returnValue = null;
	    boolean isAbsolute = true;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        
        if (isDebugEnabled) {
            LOGGER.debug("validateXPath for contextElement-{} currentElement-{} xPath-{}", contextElement, currentElement, expression);
        }

        if (DataStoreValidationUtil.isLocationPath(expression) && ((LocationPath)expression).getSteps()[0] != null){
            LocationPath path = (LocationPath)expression;
            if (path.getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
                LocationPath newLocationPath = buildPathFromRoot(currentElement, expression, contextNodePath);
                Expression newExpresssion = replaceCurrent(currentElement, newLocationPath);
                returnValue = validateXPath(contextElement, (Element) currentElement.getParentNode(), newExpresssion, contextNodePath);
            } else if (path.getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                returnValue = currentElement.getTextContent();
            } else {
                Step[] steps = path.getSteps();
                Step[] newSteps = new Step[steps.length];
                for (int i=0;i<steps.length;i++) {
                    newSteps[i] = buildNewStepFromOldWithNs(contextElement, currentElement, steps[i]);
                }
                expression = new LocationPath(path.isAbsolute(), newSteps);
                isAbsolute = path.isAbsolute();
            }
        } else if (DataStoreValidationUtil.isOperation(expression)) {
            returnValue = validateOperation(contextElement, currentElement, expression, returnValue, contextNodePath);
        }

        if (returnValue == null) {
          returnValue = evaluateContext(contextElement, currentElement, expression, isAbsolute, contextNodePath);
        }

        return returnValue;
	}

    protected Object validateOperation(Element contextElement, Element currentElement, Expression expression, Object returnValue, List<QName> contextNodePath) {
        
        if (DataStoreValidationUtil.isFunction(expression) && (((CoreFunction)expression).getFunctionCode() == Compiler.FUNCTION_COUNT)) {
            return evaluateCount(contextElement, currentElement, (CoreFunction) expression);
        }
        
        
        Expression[] expressions = ((Operation)expression).getArguments();
        Expression[] newExpressions = new Expression[expressions != null ? expressions.length : 0];
        
        boolean or = DataStoreValidationUtil.isCoreOperationOr(expression);
        boolean and = DataStoreValidationUtil.isCoreOperationAnd(expression);
        
        for (int i=0;i<newExpressions.length;i++) {
            Object value = validateXPath(contextElement, currentElement, expressions[i], contextNodePath);
            newExpressions[i] = value instanceof Expression ? ((Expression)value) : JXPathUtils.getConstantExpression(value);
            
            if (or || and) {
                if (or && ((value instanceof Boolean && ((Boolean)value)) || Boolean.TRUE.toString().equals(value))) {
                    return value;
                }
                
                if (and && ((value instanceof Boolean && !((Boolean)value)) || Boolean.FALSE.toString().equals(value))) {
                    return value;
                }
            }
        }
        
        if (or) {
            // none of the values is true. That is why we are here.
            return false;
        }
        
        if (and) {
            // none of the value is false. That is why we are here. 
            return true;
        }
        
        returnValue = evaluateOperation(currentElement, expression, returnValue, newExpressions);
        
        return returnValue;
    }

    protected Object evaluateOperation(Element currentElement, Expression expression, Object returnValue, Expression[] newExpressions) {
        
        if (DataStoreValidationUtil.isCoreOperation(expression)) {
            Expression newExpression = JXPathUtils.getCoreOperation(((CoreOperation)expression), newExpressions);
            returnValue = newExpression.compute(null);
        } else if (DataStoreValidationUtil.isExtensionFunction(expression)) {
            if (newExpressions.length == 0 || newExpressions[0] == null) {
                // FOR NOW ONLY CURRENT is supported
               returnValue = replaceCurrent(currentElement, expression).compute(null);
            } else {
                returnValue = new ExtensionFunction(((ExtensionFunction)expression).getFunctionName(),newExpressions).compute(null);
            }
        } else if (DataStoreValidationUtil.isFunction(expression)) {
            Expression newExpression = JXPathUtils.getCoreFunction(((CoreFunction)expression).getFunctionCode(), newExpressions);
            returnValue = newExpression.compute(null);
        }
        return returnValue;
    }

    protected Object evaluateContext(Element contextElement, Element currentElement, Expression expression, boolean isAbsolute, List<QName> paths) {
        Object returnValue;
        Element jxpathContextElement = contextElement;
        if (isAbsolute) {
            jxpathContextElement = getRootElement(contextElement);
        }
        JXPathContext context = JXPathContext.newContext(jxpathContextElement);
        /**
         *  Register namespace of all parent element to JXPathContext, since xpath expression might be have the different prefix. In below case, we should register both 'ibn' and' moduinf' namespaces in JXPathContext
         *  ex: /ibn:intents/ibn:intent/ibn:configuration/fmoduinf:fm-odu-infra/fmoduinf:workmode
         */
        setNSContext(context, currentElement);
        setNSContext(context, contextElement);
          context.setLenient(true);
          returnValue = context.getValue(expression.toString());
          if(returnValue == null && paths != null){
              Iterable<QName> iterable = (Iterable<QName>)paths;
              SchemaPath schemaPath = SchemaPath.create(iterable, true);
              SchemaRegistry registry = getSchemaRegistry(schemaPath);
              SchemaPath schemaPathWithRevision = registry.addRevisions(schemaPath);
              SchemaNode node = registry.getDataSchemaNode(schemaPathWithRevision);
              if(node != null && node instanceof LeafSchemaNode){
                  LeafSchemaNode leafNode = (LeafSchemaNode)node;
                  Optional<? extends Object> defaultValue = leafNode.getType().getDefaultValue();
                  if(defaultValue.isPresent()){
                      returnValue = defaultValue.get();
                  }
              }
          }
        return returnValue;
    }
	
	private Object validateXPath(Element rootElement, Element currentElement, String xPath) {
	    boolean isDebugEnabled = LOGGER.isDebugEnabled();
	    
	    if (isDebugEnabled) {
	        LOGGER.debug("validateXPath for rootElement-{} currentElement-{} xPath-{}", rootElement, currentElement, xPath);
	    }
	    return validateXPath(rootElement, currentElement, JXPathUtils.getExpression(xPath), null);
	}
	
	private void validateChoicecase(List<SchemaPath> childrenElementNodeSchemaPath,
			List<Element> elements, SchemaPath schemaPath,
			RequestType requestType,
			Element rootElement,
			SchemaRegistry schemaRegistry,
			NetconfRpcPayLoadType rpcType) throws ValidationException {
		Map<SchemaPath, Pair<SchemaPath, String>> choiceCaseExistMap = new HashMap<>();
		int index = 0;
		SchemaPath choiceCaseRootElementPath = ChoiceCaseNodeUtil.getChoiceCaseNodeSchemaPath(schemaRegistry, schemaPath);
		DataSchemaNode rootNode = schemaRegistry.getDataSchemaNode(schemaPath);
		/**
		 * If the current node is a container and also a choice case, then don't validate it. This will be validate
		 * during the parent node choice case validation. The individual leaf/list validation of this container would 
		 * have already been done at validateElement()
		 */
		if (rootNode !=null && rootNode instanceof ContainerSchemaNode && choiceCaseRootElementPath==null){
			for (SchemaPath childSchemaPath : childrenElementNodeSchemaPath) {
				SchemaPath choiceCaseSchemaPath = ChoiceCaseNodeUtil.getChoiceCaseNodeSchemaPath(schemaRegistry, childSchemaPath);
				if (choiceCaseSchemaPath != null ) {
					Element element = elements.get(index);
					validateConstraint(element, choiceCaseSchemaPath, requestType, rootElement, schemaRegistry, rpcType);
				    SchemaPath choiceSchemaPath = choiceCaseSchemaPath.getParent();
					validateConstraint(element, choiceSchemaPath, requestType, rootElement, schemaRegistry, rpcType);

					//two case of same choice can't exists, so no other case should exists in choiceCaseExistMap
				    Pair<SchemaPath,String> existingPair = choiceCaseExistMap.get(choiceSchemaPath);
	                if (existingPair != null) {
						String existingOperation = choiceCaseExistMap.get(choiceSchemaPath).getSecond();
						String currentOperation = OperationAttributeUtil.getOperationAttribute(element);
						if(!OperationAttributeUtil.isAllowedOperation(existingOperation, currentOperation) &&
								!existingPair.getFirst().equals(choiceCaseSchemaPath)) {
							NetconfRpcError error = NetconfRpcError.getBadElementError(childSchemaPath.getLastComponent().getLocalName(), NetconfRpcErrorType.Application);

							throw new ValidationException(error);
						}
				    }
				    choiceCaseExistMap.put(choiceSchemaPath, new Pair<SchemaPath, String>(choiceCaseSchemaPath, OperationAttributeUtil
									.getOperationAttribute(element)));
				}
				index++;
			}
		}
				
	}
	
	protected void typeValidation(Element element, SchemaPath schemaPath, RequestType requestType) throws ValidationException {
		SchemaRegistry schemaRegistry = getSchemaRegistry(schemaPath);
        SchemaNodeConstraintParser schemaNodeConstraintParser = ConstraintValidatorFactoryImpl.getInstance()
                .getConstraintNodeValidator(schemaRegistry.getDataSchemaNode(schemaPath), schemaRegistry, getExpressionValidator());
        if (schemaNodeConstraintParser != null) {
            schemaNodeConstraintParser.validate(element, requestType);
        }
    }

	protected void validateInsertAttributes(Element parentElement, Element childNode, SchemaPath schemaPath) throws ValidationException {
	}
	
    protected void validateOperation(Element parentElement, Element childNode, SchemaPath schemaPath) throws ValidationException {
    }

    private List<SchemaPath> getSchemaPathFromCases(Collection<DataSchemaNode> nodes) {
        List<SchemaPath> casePaths = new ArrayList<>();
        for(DataSchemaNode node : nodes){
            if(node instanceof ChoiceSchemaNode){
                for(CaseSchemaNode caseNode : ((ChoiceSchemaNode)node).getCases().values()){
                    for(DataSchemaNode caseChild : caseNode.getChildNodes()){
                        if(caseChild instanceof ChoiceSchemaNode){
                            casePaths.addAll(getSchemaPathFromCases(Collections.singleton(caseChild)));
                        }else{
                            casePaths.add(caseChild.getPath());
                        }
                    }
                }
            }
        }
        return casePaths;
    }

    protected SchemaPath getSchemaPathForElement(Element element, Collection<SchemaPath> availableSchemaPaths) {
        for(SchemaPath schemaPath : availableSchemaPaths){
            QName lastComponent = schemaPath.getLastComponent();
            if(lastComponent.getNamespace().toString(). equals(element.getNamespaceURI()) && lastComponent.getLocalName().equals(element.getLocalName())){
                return schemaPath;
            }
        }
        return null;
    }

    private Collection<SchemaPath> getSchemaPathsForNodes(Collection<DataSchemaNode> nodes) {
        List<SchemaPath> schemaPaths = new ArrayList<>();
        for(DataSchemaNode node : nodes){
            schemaPaths.add(node.getPath());
        }
        return schemaPaths;
    }


    protected abstract SchemaRegistry getSchemaRegistry(SchemaPath schemaPath);

    protected Collection<DataSchemaNode> getChildren(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        return schemaRegistry.getChildren(schemaPath);
    }
    
	private Object evaluateCount(Element contextElement, Element currentElement, CoreFunction xPath) {
	    Expression expression = xPath.getArg1();
	    if (DataStoreValidationUtil.isLocationPath(expression)) {
	        Object[] values = new Object[1];
	        if (((LocationPath)expression).getSteps().length == 1) {
                LocationPath path = buildPathFromRoot(contextElement, currentElement, (LocationPath)expression);
                /**
                 * If this is a count(Single-node-path), we need to append * as the first step in the path
                 * to indicate the count(single-node-path) must count all such occurence.   
                 */
	            Step[] steps = path.getSteps();
	            Step[] newSteps = new Step[steps.length+1];
	            newSteps[0] = new YangStep(new org.apache.commons.jxpath.ri.QName("*"));
	            newSteps[1] = steps[0];
	            values[0] = new LocationPath(false, newSteps);
	        } else {
	            values[0] = buildPathFromRoot(contextElement, currentElement, (LocationPath) expression);
	        }
	        Expression newExpression = JXPathUtils.getCoreFunction(xPath.getFunctionCode(), values);
            newExpression = replaceCurrent(currentElement, newExpression);
            return evaluateContext(contextElement, currentElement, newExpression, true, null);
	    } else if (DataStoreValidationUtil.isExpressionPath(expression)) {
            Object[] values = new Object[1];
            values[0] = buildEvaluationPath(currentElement, (ExpressionPath)expression);
            Expression newExpression = JXPathUtils.getCoreFunction(xPath.getFunctionCode(), values);
            newExpression = replaceCurrent(currentElement, newExpression);
            return evaluateContext(contextElement, currentElement, newExpression, true, null);
	    }
	    return null;
	}
	
	private LocationPath buildPathFromRoot(Element root, Element currentElement, LocationPath expression) {
	    if (expression.isAbsolute()) {
	        return expression;
	    }
	    
	    int index = 0;
	    Step[] steps = expression.getSteps();
	    Node nextElement = currentElement;
	    while (steps[index].getAxis() == Compiler.AXIS_PARENT) {
	        //count the number of parent path and goto the parent element each time
	        index++;
	        nextElement = nextElement.getParentNode();
	    }
	    
	    if (index > 0) {
	        // indicates there are parent paths. 
	        Node rootElement = nextElement;
	        List<Step> stepsToRoot = new LinkedList<Step>();
	        while (isParentNotDocument(rootElement)){
	            // build step in the reverse fashion till root. 
	            Step newStep = new YangStep(new org.apache.commons.jxpath.ri.QName(rootElement.getPrefix(), rootElement.getLocalName()), rootElement.getNamespaceURI());
	            stepsToRoot.add(0, newStep);
	            rootElement = rootElement.getParentNode();
	        }
	        // So total steps for new path will be sum of
	        // 1) steps to root 
	        // 2) original steps length - discarded parent steps
	        int totalStepCount = stepsToRoot.size()+steps.length-index;
	        Step[] totalSteps = new Step[totalStepCount];
	        int totalStepIndex = 0;
	        for (;totalStepIndex <stepsToRoot.size();totalStepIndex++) {
	            totalSteps[totalStepIndex] = stepsToRoot.get(totalStepIndex);
	        }
	        for (int j=index;j>0 && totalStepIndex<totalStepCount;j--,totalStepIndex++) {
	            Step oldStep = steps[j];
	            if (oldStep.getNodeTest() instanceof NodeNameTest) {
	                Step newStep = buildNewStepFromOldWithNs(root, (Element) nextElement, oldStep);
	                totalSteps[totalStepIndex] = newStep;
	            }
	        }
	        
	        if (totalSteps.length > 0 && totalSteps[0] != null) {
	            return new LocationPath(false, totalSteps);
	        }
	    } else {
	        Step[] newSteps = new Step[steps.length]; 
	        for (int i=0;i<newSteps.length;i++) {
	            newSteps[i] = buildNewStepFromOldWithNs(root, currentElement, steps[i]);
	        }
	        return new LocationPath(expression.isAbsolute(), newSteps);
	    }
	    return expression;
	}
	
	private Step buildNewStepFromOldWithNs(Element rootElement, Element nextElement, Step oldStep) {
        NodeNameTest nodeTest = (NodeNameTest) oldStep.getNodeTest();
        String prefix = nodeTest.getNodeName().getPrefix();
        String name = nodeTest.getNodeName().getName();
        String ns = nodeTest.getNamespaceURI();
        if (prefix == null) {
            if (ns != null && !ns.isEmpty()) {
                prefix = getSchemaRegistry(null).getPrefix(ns);
                if (prefix == null) {
                	SchemaRegistry mountRegistry = SchemaRegistryUtil.getMountRegistry();
                	if (mountRegistry != null) {
                		prefix = mountRegistry.getPrefix(ns);
                	}
                }
            } else {
                Element child = null;
                if (nextElement != null) {
                    child = DocumentUtils.getDescendant((Element) nextElement, name, rootElement.getNamespaceURI());
                }
                prefix = child == null ? rootElement.getPrefix() : child.getPrefix();
                ns = child == null ? rootElement.getNamespaceURI() : child.getNamespaceURI();
            }
        }
        Step newStep = new YangStep(new org.apache.commons.jxpath.ri.QName(prefix, name), ns, oldStep.getPredicates());
        return newStep;
	}
	
	private boolean isParentNotDocument(Node node) {
	    if (node.getParentNode() != null && node.getParentNode().getNodeType() != Node.DOCUMENT_NODE) {
	        return true;
	    }
	    return false;
	}
	private Element getRootElement(Node currentElement) {
	    while (isParentNotDocument(currentElement)) {
	        currentElement = currentElement.getParentNode();
	    }
	    return currentElement.getNodeType() == Node.ELEMENT_NODE ?  ((Element)currentElement) : null;
	}
	
	private void setNSContext(JXPathContext context, Node currentElement) {
		if (currentElement != null) {
			while (isParentNotDocument(currentElement)) {
				if (currentElement.getPrefix() != null) {
					context.registerNamespace(currentElement.getPrefix(), currentElement.getNamespaceURI());
				}
				currentElement = currentElement.getParentNode();
			}
		}
	}
	
	private Expression replaceCurrent(Element currentElement, Expression xPath) {
	    if (xPath.toString().contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
	        if (DataStoreValidationUtil.isLocationPath(xPath)) {
	            return replaceCurrentOnLocationPath(currentElement, xPath);
	        } else if (DataStoreValidationUtil.isOperation(xPath)) {
	            return replaceCurrentOnOperation(currentElement, xPath);
	        } else if (DataStoreValidationUtil.isExpressionPath(xPath)) {
                return replaceCurrentOnExpressionPath(currentElement, xPath);
	        }
	    } else if (DataStoreValidationUtil.isLocationPath(xPath)) {
	        return buildPathWithNs(currentElement, xPath);
	    } 
	    
	    return xPath;
	}

    protected Expression buildPathWithNs(Element currentElement, Expression xPath) {
        Step[] steps = ((LocationPath)xPath).getSteps();
        Step[] newSteps = new Step[steps.length];
        boolean changed = false;
        for (int i=0;i<steps.length;i++) {
            if (steps[i].getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = (NodeNameTest) steps[i].getNodeTest();
                if (node.getNodeName().getPrefix() == null) {
                    newSteps[i] = buildNewStepFromOldWithNs(currentElement, null, steps[i]);
                    changed = true;
                } else {
                    newSteps[i] = steps[i];
                }
            }
        }
        
        if (changed) {
            return new LocationPath(((LocationPath)xPath).isAbsolute(), newSteps);
        }
        return xPath;
    }

    protected Expression replaceCurrentOnExpressionPath(Element currentElement, Expression xPath) {
        // currently only current() is supported in RPCs. 
        ExpressionPath path = ((ExpressionPath)xPath);
        Step[] oldSteps = path.getSteps();
        Element newContextElement =  null;
        if (oldSteps[0].getAxis() == Compiler.AXIS_PARENT) {
            // this is current()/..
            newContextElement = (Element) currentElement.getParentNode();
        } else {
            newContextElement = currentElement;
        }
        LocationPath newPath = buildEvaluationPath(currentElement, (ExpressionPath)xPath);
        return JXPathUtils.getConstantExpression(validateXPath(newContextElement, currentElement, newPath, null));
    }

    protected Expression replaceCurrentOnOperation(Element currentElement, Expression xPath) {
        Operation operation = (Operation) xPath;
        Expression[] oldExpressions = operation.getArguments();
        Expression[] newExpressions = new Expression[oldExpressions == null ? 0 : oldExpressions.length];
        boolean changed = false;
        
        for (int i=0;i<newExpressions.length;i++) {
            newExpressions[i] = replaceCurrent(currentElement, oldExpressions[i]);
            if (!newExpressions[i].equals(oldExpressions[i])) {
                changed = true;
            }
        }
        
        if (changed) {
            if (DataStoreValidationUtil.isCoreOperation(xPath)) {
                return JXPathUtils.getCoreOperation((CoreOperation)xPath, newExpressions);
            } else if (DataStoreValidationUtil.isFunction(xPath)) {
                return JXPathUtils.getCoreFunction(((CoreFunction)xPath).getFunctionCode(), newExpressions);
            } else if (DataStoreValidationUtil.isExtensionFunction(xPath)) {
                return new ExtensionFunction(((ExtensionFunction)operation).getFunctionName(), newExpressions);
            } 
        } else {
            if (newExpressions.length == 0 || newExpressions[0] == null) {
                if (DataStoreValidationUtil.isExtensionFunction(xPath) && xPath.toString().equals(CURRENT_PATTERN)) {
                    return JXPathUtils.getConstantExpression(currentElement.getTextContent());
                }
            }
            return xPath;
        }
        return xPath;
    }

    protected Expression replaceCurrentOnLocationPath(Element currentElement, Expression xPath) {
        Step[] oldSteps = ((LocationPath)xPath).getSteps();
        Step[] newSteps = new Step[oldSteps.length];
        boolean changed = false;
        for (int i=0;i<oldSteps.length;i++) {
            if (oldSteps[i].toString().contains(CURRENT_PATTERN)) {
                if (oldSteps[i].getPredicates() == null || oldSteps[i].getPredicates().length == 0) {
                    newSteps[i] = new YangStep(new org.apache.commons.jxpath.ri.QName(null, currentElement.getTextContent()), null);
                } else {
                    Expression[] oldExpressions = oldSteps[i].getPredicates();
                    Expression[] newExpressions = new Expression[oldExpressions.length];
                    for (int j=0;j<oldExpressions.length;j++) {
                        newExpressions[j] = replaceCurrent(currentElement, oldExpressions[j]);
                    }
                    newSteps[i] = new YangStep(oldSteps[i], newExpressions);
                }
                changed = true;
            } else {
                newSteps[i] = oldSteps[i];
            }
        }// for oldSteps.length
        if (changed) {
            return new LocationPath(((LocationPath)xPath).isAbsolute(), newSteps);
        } 
        return xPath;
    }
	
	private LocationPath buildPathFromRoot(Element contextElement, Expression expression, List<QName> contextNodeExpressionPath) {
        Step[] step = ((LocationPath)expression).getSteps();
        int index = 0;
        Node prevElement = contextElement;
        while (step[index].getAxis() == Compiler.AXIS_PARENT) {
            index++;
            prevElement = prevElement.getParentNode();
            if(contextNodeExpressionPath != null) {
                contextNodeExpressionPath.remove(contextNodeExpressionPath.size()-1);
            }
        }
        for(int i=index;i<step.length && contextNodeExpressionPath != null; i++){
            Step xpathStep = step[i];
            if(xpathStep.getAxis() != Compiler.AXIS_PARENT){
                NodeNameTest nodeTest = (NodeNameTest) xpathStep.getNodeTest();
                String name = nodeTest.getNodeName().getName();
                String ns = nodeTest.getNamespaceURI()!= null? nodeTest.getNamespaceURI():contextElement.getNamespaceURI();
                QName qname = QName.create(ns, name);
                contextNodeExpressionPath.add(qname);
            }
        }
        
        List<Step> stepsToRoot = new LinkedList<Step>();
        while (isParentNotDocument(prevElement)) {
            stepsToRoot.add(0, new YangStep(new org.apache.commons.jxpath.ri.QName(prevElement.getPrefix(), prevElement.getLocalName()),
                    prevElement.getNamespaceURI()));
            prevElement = prevElement.getParentNode();
        }

        Step[] newSteps = new Step[step.length - index + stepsToRoot.size()];
        Iterator<Step> iterator = stepsToRoot.iterator();
        //append steps from root
        for (int i=0;i<stepsToRoot.size();i++) {
            newSteps[i] = iterator.next();
        }
        
        //append steps in xPath to build the abs path
        for (int i=index,j=stepsToRoot.size();j<newSteps.length;j++,i++) {
            newSteps[j] = buildNewStepFromOldWithNs(contextElement, (Element) prevElement, step[i]);
        }
        
        return new LocationPath(true, newSteps);	    
	}
	
	private LocationPath buildEvaluationPath(Element currentElement, ExpressionPath xPath) {
        // only current() is supported in RPCs. 
        ExpressionPath path = ((ExpressionPath)xPath);
        ExtensionFunction function = (ExtensionFunction) path.getExpression();
        if (!function.toString().equals(CURRENT_PATTERN)) {
            throw new ValidationException(String.format("Function %s not supported in xpath %s", function, xPath));
        }
        
        Step[] oldSteps = path.getSteps();
        Step[] newSteps = null;
        Element newContextElement =  null;
        if (oldSteps[0].getAxis() == Compiler.AXIS_PARENT) {
            // this is current()/..
            newContextElement = (Element) currentElement.getParentNode();
            newSteps = new Step[oldSteps.length-1];
            System.arraycopy(oldSteps, 1, newSteps, 0, newSteps.length);
        } else {
            // this is current()/abc/bcd
            newContextElement = currentElement;
            newSteps = oldSteps;
        }
        return buildPathFromRoot(newContextElement, new LocationPath(false, newSteps), null);
	    
	}

    private Expression buildChoiceCasePath(Element currentElement, Expression xPath, DataSchemaNode schemaNode) {
        if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
            return buildPathForChoiceCase(currentElement, xPath, schemaNode);
        }
        return xPath;
    }
    
    private Expression buildPathForChoiceCase(Element currentElement, Expression xPath, DataSchemaNode schemaNode) {

        if (DataStoreValidationUtil.isLocationPath(xPath) && !((LocationPath) xPath).isAbsolute()) {
            // only in case of relative paths, we need to build the actual path. 
            // Consider
            /* container A {
             *    leaf a {type string;}
             *    choice a1 {
             *      case a2 {
             *         when "(../../a = 'a')";
             *         leaf b {type string;}
             *      }
             *  }
             *  
             *  Here in a rpc input/output the when will actually be on node <A> for a data schema.  
             *  <A>
             *    <a>a</a>
             *    <b>b</b>
             *  </A>
             *  
             *  The below set of instructions aim to turn the ../../a into /A/a --> an absolute path. 
             */
             
            Step[] oldSteps = ((LocationPath) xPath).getSteps();
            if (oldSteps[0].getAxis() == Compiler.AXIS_PARENT) {
                // reduce index is 1 in case the schemaNode is Choice and 2 for Case node. Indicates the number of parent path to remove
                int reduceIndex = schemaNode instanceof CaseSchemaNode ? 2 : schemaNode instanceof ChoiceSchemaNode ? 1 : 0;
                Step[] newSteps = new Step[oldSteps.length - reduceIndex];
                System.arraycopy(oldSteps, reduceIndex, newSteps, 0, newSteps.length);
                currentElement = reduceIndex > 0 ? (Element) currentElement.getParentNode() : currentElement;
                return buildPathFromRoot(currentElement, new LocationPath(false, newSteps), null);
            }
        } else if (DataStoreValidationUtil.isOperation(xPath)) {
            return buildChoiceCasePathForOperation(currentElement, xPath, schemaNode);
        } else if (DataStoreValidationUtil.isExpressionPath(xPath)) {
            return buildChoiceCasePathForExpressionPath(currentElement, xPath, schemaNode);
        }
        return xPath;
    }

    protected Expression buildChoiceCasePathForExpressionPath(Element currentElement, Expression xPath, DataSchemaNode schemaNode) {
        // For now only current is supported. But this will not break if other functions are used. 
        ExpressionPath path = (ExpressionPath) xPath;
        Expression function = path.getExpression();
        Step[] steps = path.getSteps();
        boolean changed = false;
        if (steps != null) {
            Expression locationPath = new LocationPath(false, steps);
            Expression expression = buildChoiceCasePath(currentElement, locationPath, schemaNode);
            if (!expression.equals(locationPath)) {
                changed = true;
                steps = ((LocationPath) expression).getSteps();
            }
        }

        Expression[] predicates = path.getPredicates();
        Expression[] newPredicates = new Expression[predicates != null ? predicates.length : 0];
        for (int i = 0; i < newPredicates.length; i++) {
            newPredicates[i] = buildChoiceCasePath(currentElement, predicates[i], schemaNode);
            if (!newPredicates[i].equals(predicates[i])) {
                changed = true;
            }
        }

        if (changed) {
            return new ExpressionPath(function, predicates == null ? predicates : newPredicates, steps);
        }
        return xPath;
    }

    protected Expression buildChoiceCasePathForOperation(Element currentElement, Expression xPath, DataSchemaNode schemaNode) {
        Expression[] oldExpressions = ((Operation) xPath).getArguments();
        Expression[] newExpressions = new Expression[oldExpressions == null ? 0 : oldExpressions.length];
        boolean changed = false;
        for (int i = 0; i < newExpressions.length; i++) {
            newExpressions[i] = buildChoiceCasePath(currentElement, oldExpressions[i], schemaNode);
            if (!newExpressions[i].equals(oldExpressions[i])) {
                changed = true;
            }
        }

        if (changed) {
            if (DataStoreValidationUtil.isCoreOperation(xPath)) {
                return JXPathUtils.getCoreOperation(((CoreOperation) xPath), newExpressions);
            } else if (DataStoreValidationUtil.isFunction(xPath)) {
                return JXPathUtils.getCoreFunction(((CoreFunction) xPath).getFunctionCode(), newExpressions);
            } else if (DataStoreValidationUtil.isExtensionFunction(xPath)) {
                return new ExtensionFunction(((ExtensionFunction) xPath).getFunctionName(), newExpressions);
            }
        }
        return xPath;
    }
}
