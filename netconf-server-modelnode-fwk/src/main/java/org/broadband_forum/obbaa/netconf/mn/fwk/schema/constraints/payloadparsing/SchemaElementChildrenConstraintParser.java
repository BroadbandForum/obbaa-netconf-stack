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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.CURRENT_PATTERN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.broadband_forum.obbaa.netconf.api.NetconfRpcPayLoadType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.DataPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ModelNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaContextUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.AccessSchemaPathUtil;
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
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
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
    private final static String XPATH_EXPR_CACHE = "XPathExprCache";

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SchemaElementChildrenConstraintParser.class, LogAppNames.NETCONF_STACK);

    public abstract ModelNodeDataStoreManager getDsm();

    public abstract DSExpressionValidator getExpressionValidator(); 
    protected RootModelNodeAggregator m_rootModelNodeAggregator;

    protected SchemaPath getChildSchemaPath(SchemaRegistry schemaRegistry, Element element, SchemaPath parentPath, ActionDefinition actionDefinition){
        Collection<SchemaNode> children = new ArrayList<>();
        children.addAll(schemaRegistry.getChildren(parentPath));
        if (actionDefinition != null && actionDefinition.getPath().getParent().equals(parentPath)){
            children.add(actionDefinition);
        }
        return getSchemaPathForElement(element, getSchemaPathsForSchemaNodes(children));
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
        schemaRegistry = SchemaRegistryUtil.retrievePossibleMountRegistry(element, schemaPath, schemaRegistry);
        throwErrorIfActionExistsWithinRPC(element, schemaPath, requestType, schemaRegistry, element);
        throwErrorIfActionExistsWithinNotification(element, schemaPath, requestType, schemaRegistry, element);

        //go though children
        NodeList nodes = element.getChildNodes();
        List<SchemaPath> childrenElementNodeSchemaPath = new ArrayList<>();
        List<Element> childrenElement = new ArrayList<>();
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(schemaPath);
        for(int i=0 ; i< nodes.getLength(); i++){
            Node childNode = nodes.item(i);
            if (node instanceof ContainerSchemaNode || node instanceof ListSchemaNode) {
                if (childNode.getNodeType() == Node.TEXT_NODE) {
                    if (!childNode.getNodeValue().trim().isEmpty()) {
                        NetconfRpcError error = NetconfRpcError.getInvalidXMLError(element.getLocalName(),NetconfRpcErrorType.Application);
                        throw new ValidationException(error);
                    }
                }
            }

            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                throwErrorIfActionExistsWithinRPC(element, schemaPath, requestType, schemaRegistry, childNode);
                throwErrorIfActionExistsWithinNotification(element, schemaPath, requestType, schemaRegistry, childNode);
                Collection<DataSchemaNode> children = schemaRegistry.getChildren(schemaPath);
                SchemaPath childSchemaPath = getSchemaPathForElement((Element) childNode, getSchemaPathsForNodes(children));

                if(childSchemaPath == null) {
                    if(requestType.isAction()){
                        ActionDefinition actionDef = getMatchedActionDefinition((Element)childNode, schemaPath, schemaRegistry);
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

                validateAttributes(element, (Element) childNode, schemaPath);
                validateOperation(element, (Element) childNode, schemaPath);
                validateInsertAttributes(element, (Element) childNode, childSchemaPath);
                validateElement((Element) childNode, childSchemaPath, requestType, rootElement, modelNodeDsm, rpcType, dsmRegistry, schemaRegistry, rpcContext);
                childrenElementNodeSchemaPath.add(childSchemaPath);
                childrenElement.add((Element) childNode);
            }  else if ((requestType.isRpc() || requestType.isAction())
                    && doesRootElementNeedLeafRefValidtion(rootElement, element, schemaRegistry, schemaPath)) {
                childrenElement.add(element);
                childrenElementNodeSchemaPath.add(schemaPath);
                validateLeafType(childrenElement, childrenElementNodeSchemaPath, schemaRegistry, element, rootElement, modelNodeDsm, dsmRegistry, rpcContext);
            }
        }

        validateConstraint(element, schemaPath, requestType, rootElement, schemaRegistry, rpcType, modelNodeDsm, dsmRegistry, rpcContext);
        validateChoicecase(childrenElementNodeSchemaPath, childrenElement, schemaPath, requestType, element, schemaRegistry, rpcType, modelNodeDsm, dsmRegistry, rpcContext);
        typeValidation(element, schemaPath, requestType, schemaRegistry);
        boolean isRemoteActionOrRpc = isRemoteActionOrRpc(schemaRegistry);
        if ((requestType.isRpc() || requestType.isAction()) && !isRemoteActionOrRpc){
            validateLeafType(childrenElement, childrenElementNodeSchemaPath, schemaRegistry, element, rootElement, modelNodeDsm, dsmRegistry, rpcContext);
        }

    }

    private Collection<SchemaPath> getSchemaPathsForSchemaNodes(Collection<SchemaNode> nodes) {
        List<SchemaPath> schemaPaths = new ArrayList<>();
        for(SchemaNode node : nodes){
            schemaPaths.add(node.getPath());
        }
        return schemaPaths;
    }

    private boolean doesRootElementNeedLeafRefValidtion(Element rootElement, Element element, SchemaRegistry schemaRegistry, SchemaPath schemaPath )
    {
        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if(dataSchemaNode instanceof LeafSchemaNode || dataSchemaNode instanceof LeafListSchemaNode) {
            if(rootElement.getNamespaceURI() != null && element.getNamespaceURI() != null
                    && rootElement.getNodeName() != null && element.getNodeName() != null
                    && rootElement.getNamespaceURI().equals(element.getNamespaceURI())
                    && rootElement.getNodeName().equals(element.getNodeName())) {
                return true;
            }
        }
        return false;
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

    private ActionDefinition getMatchedActionDefinition(Element childNode, SchemaPath schemaPath, SchemaRegistry schemaRegistry){
        Set<ActionDefinition> actionDefs = ActionUtils.retrieveActionDefinitionForSchemaNode(schemaPath, schemaRegistry);
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
            if (schemaNode != null && (schemaNode instanceof LeafSchemaNode || schemaNode instanceof LeafListSchemaNode)) {
                Element currentChild = childElement.get(index);
                String xPathValue = currentChild.getTextContent();
                if (((TypedDataSchemaNode) schemaNode).getType() instanceof InstanceIdentifierTypeDefinition) {
                    boolean isRequired = ((InstanceIdentifierTypeDefinition) ((TypedDataSchemaNode) schemaNode).getType())
                            .requireInstance();
                    if (isRequired) {
                        Object value = validateXPath(currentChild, currentChild, xPathValue, schemaRegistry);
                        if (value == null) {
                            validateOnDataStore(schemaRegistry, xPathValue, currentChild.getTextContent(),
                                    modelNodeDsm, currentElement, schemaNode, dsmRegistry, rpcContext);
                        }
                        if (value == null || (value instanceof Boolean && !((Boolean) value))) {
                            prepareMissingDataException(schemaRegistry, rootElement, currentChild, xPathValue, schemaNode);

                        }
                    }
                } else if (((TypedDataSchemaNode) schemaNode).getType() instanceof LeafrefTypeDefinition) {
                    Object value;
                    LeafrefTypeDefinition type = (LeafrefTypeDefinition) ((TypedDataSchemaNode) schemaNode).getType();
                    if (type.requireInstance()) {
                        String xPath = type.getPathStatement().getOriginalString();
                        // Leafref validation should be done only if target schema node is config data, otherwise validation should be skipped
                        boolean isConfigurationTargetNode = AccessSchemaPathUtil.isTargetNodeConfiguration(schemaRegistry, schemaNode, xPath);
                        if(isConfigurationTargetNode){
                            // leafRef path has current(), then it will be resolved with in input nodes
                            List<QName> contextNodePath = new ArrayList<>();
                            schemaNode.getPath().getPathFromRoot().forEach(contextNodePath::add);	
                            value = validateXPath(currentElement, currentChild, JXPathUtils.getExpression(xPath),contextNodePath, schemaRegistry);
                            if (value == null) {
                                LOGGER.debug("leafref original xpath :{}", xPath);
                                //leafref paths referring to rpc input leafs are resolved here
                                Map<String, Expression> xpathExprCache = (Map<String, Expression>) RequestScope.getCurrentScope().getFromCache(XPATH_EXPR_CACHE);
                                Expression expression = xpathExprCache.get(JXPathUtils.getExpression(xPath).toString());
                                if(expression != null){
                                    xpathExprCache.remove(JXPathUtils.getExpression(xPath).toString());
                                    xPath = expression.toString();
                                    LOGGER.debug("leafref evaluated xpath from input elements :{}", xPath);
                                }
                                value = validateOnDataStore(schemaRegistry, xPath, currentChild.getTextContent(),
                                        modelNodeDsm, currentElement, schemaNode, dsmRegistry, rpcContext);
                                LOGGER.debug(null, "validateLeafType : value is {} for xPath-{} schemaNode-{} currentElement-{}", value, xPath, schemaNode,
                                        currentElement);
                            }
                            if (value == null || (value instanceof Boolean && !((Boolean) value))
                                    || (!(value instanceof Boolean) && !(value.equals(xPathValue)))) {
                                prepareMissingDataException(schemaRegistry, rootElement, currentChild, xPathValue, schemaNode);
                            }
                        } else {
                            LOGGER.debug("Skipped leafref validation for schema node :{} and xpath {}" ,schemaNode, xPath);
                        }
                    }
                }
            }
            index++;
        }
    }

    private void prepareMissingDataException(SchemaRegistry schemaRegistry, Element rootElement, Element currentChild, String xPathValue, DataSchemaNode schemaNode) throws ValidationException {
        StringBuilder builder = new StringBuilder();
        getAbsolutePathToParent(rootElement, currentChild, builder, schemaRegistry, schemaNode);
        String namespace = currentChild.getNamespaceURI();
        String prefix= schemaRegistry.getPrefix(namespace);
        Map<String, String> nsContext = new HashMap<>();
        nsContext.put(prefix, namespace);
        throw DataStoreValidationErrors.getMissingDataException(
                String.format("Missing required element %s", xPathValue), builder.toString(), nsContext);
    }

    private Step getFirstStepFromAbsolutePath(Expression expression) {
        if (isAbsoluteLocationPath(expression)) {
            return ((LocationPath) expression).getSteps()[0];
        } else if (expression instanceof CoreOperation) {
            CoreOperation operation = (CoreOperation) expression;
            if (operation.getArguments().length == 2 && isAbsoluteLocationPath(operation.getArguments()[0]) && operation.getArguments()[1] instanceof Constant) {
                return ((LocationPath) operation.getArguments()[0]).getSteps()[0];
            }
        }
        return null;
    }

    private boolean isAbsoluteLocationPath(Expression expression) {
        if (DataStoreValidationUtil.isLocationPath(expression) && ((LocationPath) expression).isAbsolute()) {
            return true;
        }
        return false;
    }

    private Collection<SchemaPath> getRootSchemaPaths(SchemaRegistry schemaRegistry, String xpath) {
        /**
         * If xpath is AbsolutePath, then return an exact root schemapath of xpath[0]
         *     eg: xpath:/if:interfaces/if:interface[if:name = "Test"] ==> return only schemapath of 'interfaces' since Step[0] = if:interfaces
         * else 
         *     return all root schemapaths
         */
        Expression expression = JXPathUtils.getExpression(xpath);
        Step firstStep = getFirstStepFromAbsolutePath(expression);
        if (firstStep != null) {
            SchemaPath schemapath = DataStoreValidationUtil.getRootSchemaPath(schemaRegistry, firstStep);
            if (schemapath != null) {
                Set<SchemaPath> rootSchemaPaths = new HashSet<SchemaPath>();
                rootSchemaPaths.add(schemapath);
                return rootSchemaPaths;
            }
        }
        return schemaRegistry.getRootSchemaPaths();
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

        Collection<SchemaPath> rootSchemaPaths = getRootSchemaPaths(schemaRegistry, xPath);
        Iterator<SchemaPath> schemaPaths = rootSchemaPaths.iterator();
        while (schemaPaths.hasNext() && !isValid) {
            SchemaPath schemaPath = schemaPaths.next();
            if (schemaPath != null) {
                QName rootQName = schemaPath.getLastComponent(); // There should be only one component as it is root node
                List<ModelNode> rootModelNodes = null;
                if ( m_rootModelNodeAggregator != null){
                    rootModelNodes = m_rootModelNodeAggregator.getModuleRootFromHelpers(rootQName.getNamespace().toString(), rootQName.getLocalName());			        
                }
                if ( rootModelNodes != null && !rootModelNodes.isEmpty()){
                    nodesList = rootModelNodes;
                } else {
                    if (dsmRegistry != null && dsmRegistry.lookupDSM(schemaPath) != null) {
                        nodesList = dsmRegistry.lookupDSM(schemaPath).listChildNodes(schemaPath, parentModelNodeId, schemaRegistry);
                    } else {
                        nodesList = modelNodeDsm.listChildNodes(schemaPath, parentModelNodeId, schemaRegistry);
                    }
                }
                for (ModelNode modelNode : nodesList) {
                    LOGGER.debug(null, "validateOnDataStore : schemapath-{} xPath-{} schemaNode-{} currentElement-{}", schemaPath, xPath, schemaNode, currentElement);
                    isValid = getExpressionValidator().validateXPathInModelNode(xPath, modelNode,
                            leafRefValue, currentElement, schemaNode, getValidationContext());
                    LOGGER.debug(null, "validateOnDataStore : schemapath-{} xPath-{} value-{}", schemaPath, xPath, isValid);
                    if (isValid) {
                        break;
                    }
                }
            }
        }
        return isValid;
    }

    private static DSValidationContext getValidationContext(){
        DSValidationContext context = new DSValidationContext();
        context.setRootNodeAggregator(DataStoreValidationUtil.getRootModelNodeAggregatorInCache());
        return context;
    }

    private void getAbsolutePathToParent(Element rootElement, Node currentElement, StringBuilder path, SchemaRegistry schemaRegistry, DataSchemaNode schemaNode) {
        // if root element is Leaf/Leaf-list schema node, then currentElement will always be rootElements
        if (!rootElement.equals(currentElement)
                || isLeafSchemaNode(schemaNode)) {
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
                getAbsolutePathToParent(rootElement, currentElement.getParentNode(), path, schemaRegistry, null);
            }
        }

    }

    private boolean isLeafSchemaNode(DataSchemaNode schemaNode) {
        return schemaNode != null
                && (schemaNode instanceof LeafSchemaNode || schemaNode instanceof LeafListSchemaNode);
    }

    private void validateConstraint(Element element, SchemaPath schemaPath, RequestType requestType,
            Element rootElement, SchemaRegistry schemaRegistry, NetconfRpcPayLoadType rpcType,
            ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry, Element rpcContext) throws ValidationException {
        // For RPC/Action requests, when validation should be done on the input request element
        if (requestType.isRpc() || requestType.isAction()) {
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
                    elementSchemaPath = getChildSchemaPath(schemaRegistry, element, schemaPath, null);
                } else {
                    elementSchemaPath = schemaPath;
                }
                schemaNode = schemaRegistry.getDataSchemaNode(elementSchemaPath);
            } else{
                // this is rpc input validation
                schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
            }
            boolean isRemoteActionOrRpc = isRemoteActionOrRpc(schemaRegistry);
            if (schemaNode != null && !isRemoteActionOrRpc){

                Optional<RevisionAwareXPath> optWhenCondition = schemaNode.getWhenCondition();
                if (optWhenCondition.isPresent()) {
                    RevisionAwareXPath whenCondition = optWhenCondition.get();
                    String whenConditionXpath = whenCondition.getOriginalString();
                    // If any one of the targetnode is 'config=false', the skip the when validation
                    boolean isTargetNodeConfiguration = AccessSchemaPathUtil.isTargetNodeConfiguration(schemaRegistry, schemaNode, whenConditionXpath);
                    if(isTargetNodeConfiguration){
                        Element contextElement = element;
                        if (schemaNode instanceof ChoiceSchemaNode
                                || schemaNode instanceof CaseSchemaNode) {
                            contextElement = (Element)contextElement.getParentNode();
                        }
                        List<QName> contextNodePath = new ArrayList<>();
                        schemaNode.getPath().getPathFromRoot().forEach(contextNodePath::add);
                        validateWhen(rootElement, contextElement, element,
                                buildChoiceCasePath(element, JXPathUtils.getExpression(whenConditionXpath), schemaNode, schemaRegistry),
                                whenConditionXpath, contextNodePath, schemaNode, schemaRegistry, modelNodeDsm, dsmRegistry, rpcContext);
                    } else {
                        LOGGER.debug("Skipped when validation for schema node :{} and xpath {}" ,schemaNode, whenConditionXpath);
                    }
                }

                if (schemaNode != null && schemaNode.isAddedByUses()) {
                    validateWhenOnUses(element, rootElement, schemaRegistry, modelNodeDsm, dsmRegistry, rpcContext, schemaNode);
                }
                if (schemaNode != null && schemaNode.isAugmenting()) {
                    validateWhenOnAugment(element, rootElement, schemaRegistry, modelNodeDsm, dsmRegistry, rpcContext, schemaNode);
                }

                if (schemaNode instanceof MustConstraintAware) {
                    Collection<MustDefinition> mustConstraints = ((MustConstraintAware) schemaNode).getMustConstraints();
                    if (mustConstraints != null && !mustConstraints.isEmpty()) {
                        validateMust(rootElement, element, mustConstraints, schemaNode, schemaRegistry, modelNodeDsm, dsmRegistry, rpcContext);
                    }
                }
            }
        }
    }

    private void validateWhenOnUses(Element element, Element rootElement, SchemaRegistry schemaRegistry,
            ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry, Element rpcContext, DataSchemaNode schemaNode) {
        DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(schemaNode.getPath());
        if (parentSchemaNode != null) {
            Pair<UsesNode, SchemaPath> usesNodeAndItsResidingNode = SchemaContextUtil .getUsesSchema(schemaRegistry, parentSchemaNode, schemaNode);
            UsesNode usesNode = usesNodeAndItsResidingNode == null ? null : usesNodeAndItsResidingNode.getFirst();
            if (usesNode != null) {
                SchemaPath usesPath = usesNodeAndItsResidingNode.getSecond();
                Optional<RevisionAwareXPath> xpath = usesNode.getWhenCondition();
                if (xpath.isPresent()) {
                    RevisionAwareXPath whenCondition = xpath.get();
                    String whenConditionXpath = whenCondition.getOriginalString();
                    Element contextElement = element;
                    if (schemaNode instanceof ChoiceSchemaNode || schemaNode instanceof CaseSchemaNode) {
                        contextElement = (Element) contextElement.getParentNode();
                        schemaNode = schemaRegistry.getNonChoiceParent(schemaNode.getPath());
                    }
                    Element usesElement = getCorrectContextElement(contextElement, usesPath, schemaNode.getPath(), schemaRegistry);

                    if(usesElement == null){
                        usesElement= contextElement;
                    }
                    List<QName> contextNodePath = new ArrayList<>();
                    schemaNode.getPath().getPathFromRoot().forEach(contextNodePath::add);
                    validateWhen(rootElement, usesElement, usesElement, buildChoiceCasePath(usesElement, JXPathUtils.getExpression(whenConditionXpath), schemaNode, schemaRegistry), whenConditionXpath, contextNodePath, schemaNode, schemaRegistry, modelNodeDsm, dsmRegistry, rpcContext);
                }
            }
        }
    }

    private void validateWhenOnAugment(Element element, Element rootElement, SchemaRegistry schemaRegistry,
            ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry, Element rpcContext, DataSchemaNode schemaNode) {
        DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(schemaNode.getPath());
        if (parentSchemaNode != null) {
            Pair<AugmentationSchemaNode, SchemaPath> augmentNodeAndItsResidingNode = DataStoreValidationUtil.getAugmentationSchema(schemaRegistry, parentSchemaNode, schemaNode);
            AugmentationSchemaNode augmentNode = augmentNodeAndItsResidingNode == null ? null : augmentNodeAndItsResidingNode.getFirst();
            if (augmentNode != null) {
                SchemaPath augmentedPath = augmentNode.getTargetPath();
                Optional<RevisionAwareXPath> xpath = augmentNode.getWhenCondition();
                if (xpath.isPresent()) {
                    RevisionAwareXPath whenCondition = xpath.get();
                    String whenConditionXpath = whenCondition.getOriginalString();
                    Element contextElement = element;
                    if (schemaNode instanceof ChoiceSchemaNode || schemaNode instanceof CaseSchemaNode) {
                        contextElement = (Element) contextElement.getParentNode();
                        schemaNode = schemaRegistry.getNonChoiceParent(schemaNode.getPath());
                    }
                    Element augmentElement = getCorrectContextElement(contextElement, augmentedPath, schemaNode.getPath(), schemaRegistry);

                    if(augmentElement == null){
                        augmentElement = contextElement;
                    }
                    List<QName> contextNodePath = new ArrayList<>();
                    schemaNode.getPath().getPathFromRoot().forEach(contextNodePath::add);
                    validateWhen(rootElement, augmentElement, augmentElement, buildChoiceCasePath(augmentElement, JXPathUtils.getExpression(whenConditionXpath), schemaNode, schemaRegistry), whenConditionXpath, contextNodePath, schemaNode, schemaRegistry, modelNodeDsm, dsmRegistry, rpcContext);
                }
            }
        }
    }    

    private Element getCorrectContextElement(Element contextElement, SchemaPath augmentedPath, SchemaPath contextNode, SchemaRegistry schemaRegistry){
    	DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(contextNode);
    	if(ChoiceCaseNodeUtil.isChoiceOrCaseNode(parentNode)){
    		parentNode = schemaRegistry.getNonChoiceParent(parentNode.getPath());
    	}
    	Element contextElementNode = contextElement;

    	// iterate through the contextElement from the current instance till the augmentedPath
    	while(parentNode != null && !parentNode.getPath().equals(augmentedPath)){
    		parentNode = schemaRegistry.getNonChoiceParent(parentNode.getPath());
    		contextElementNode = (Element) contextElementNode.getParentNode();
    	}
    	if(contextElementNode.getLocalName().equals(augmentedPath.getLastComponent().getLocalName())){
    		return contextElementNode;
    	}
    	return null;
    }

    private boolean isRemoteActionOrRpc(SchemaRegistry schemaRegistry) {
        return !SchemaRegistry.GLOBAL_SCHEMA_REGISTRY.equals(schemaRegistry.getName());
    }

    private void validateWhen(Element rootElement, Element contextElement, Element currentElement, Expression xPath, String revisionAwareXPath, List<QName> contextNodePath,DataSchemaNode schemaNode, SchemaRegistry schemaRegistry,
            ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry, Element rpcContext) throws ValidationException {
        Object value = validateXPath(contextElement, currentElement, xPath, contextNodePath, schemaRegistry);
        if (value == null || (value instanceof Boolean && !(Boolean)value)) {
            String xPathStr = xPath.toString();
            // if xpath has current() , then it should be resolved locally (from input elemens) 
            Map<String, Expression> xpathExprCache = (Map<String, Expression>) RequestScope.getCurrentScope().getFromCache(XPATH_EXPR_CACHE);
            Expression expression = xpathExprCache.get(xPathStr);
            if(expression != null){
                xpathExprCache.remove(xPathStr);
                xPath = expression;
                xPathStr = xPath.toString();
                LOGGER.debug("when xpath from input elements :{}", xPathStr);
            }
            String currentElementValue = null;
            if(xPathStr.contains(DataStoreValidationUtil.CURRENT_PATTERN)){
                currentElementValue = currentElement.getTextContent();
            }
            value = validateOnDataStore(schemaRegistry, xPathStr, currentElementValue,
                    modelNodeDsm, currentElement, schemaNode, dsmRegistry, rpcContext);
            LOGGER.debug(null, "value is {} for xPath-{} schemaNode-{} currentElement-{}", value, xPathStr, schemaNode,
                    currentElement);
        }
        if (value != null && value instanceof Boolean && !(Boolean)value) {
            LOGGER.error("Validation failed for xpath - {} rootElement - {} contextElement - {} currentElement - {}", xPath, rootElement, contextElement, currentElement);
            throw DataStoreValidationErrors.getViolateWhenConditionExceptionThrownUnknownElement(revisionAwareXPath);
        }
    }

    private void validateMust(Element rootElement, Element currentElement, Collection<MustDefinition> mustConstraints, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry,
            ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry, Element rpcContext) throws ValidationException {
        for (MustDefinition mustDefinition: mustConstraints) {
            String xPath = mustDefinition.getXpath().getOriginalString();
            // If any one of the targetnode is 'config=false', the skip the must validation
            boolean isTargetNodeConfiguration = AccessSchemaPathUtil.isTargetNodeConfiguration(schemaRegistry, schemaNode, xPath);
            if(isTargetNodeConfiguration){
                Expression ex = buildChoiceCasePath(currentElement, JXPathUtils.getExpression(xPath), schemaNode, schemaRegistry);
                List<QName> contextNodePath = new ArrayList<>();
                schemaNode.getPath().getPathFromRoot().forEach(contextNodePath::add);
                // validate the must constraint within RPC/Action request elements
                Object value = validateXPath(rootElement, currentElement, ex, contextNodePath, schemaRegistry);
                String xPathStr = JXPathUtils.getExpression(xPath).toString();
                if (value == null || (value instanceof Boolean && !(Boolean)value)) {
                    Map<String, Expression> xpathExprCache = (Map<String, Expression>) RequestScope.getCurrentScope().getFromCache(XPATH_EXPR_CACHE);
                    if(xpathExprCache !=null){
                        Expression expression = xpathExprCache.get(xPathStr);
                        if(expression != null){
                            xpathExprCache.remove(xPathStr);
                            xPathStr = expression.toString();
                            LOGGER.debug("must xpath from input elements :" + xPathStr);
                        }
                    }
                    String currentElementValue = null;
                    if(xPathStr.contains(DataStoreValidationUtil.CURRENT_PATTERN)){
                        currentElementValue = currentElement.getTextContent();
                    }
                    LOGGER.debug(null, "could not evaluvate must constraint for xPath-{} within input elements", xPathStr);
                    value = validateOnDataStore(schemaRegistry, xPathStr, currentElementValue,
                            modelNodeDsm, currentElement, schemaNode, dsmRegistry, rpcContext);
                }
                if (value != null && value instanceof Boolean && !(Boolean)value) {
                    LOGGER.debug(null, "Violate must constraints for xPath-{}", ex);
                    throw DataStoreValidationErrors.getViolateMustContrainsException(mustDefinition);
                }
            }
        }
    }

    private Object validateXPath(Element contextElement, Element currentElement, Expression expression, List<QName> contextNodePath, SchemaRegistry schemaRegistry) {
        Object returnValue = null;
        boolean isAbsolute = true;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String xPathStr = expression.toString();

        if (isDebugEnabled) {
            LOGGER.debug("validateXPath for contextElement-{} currentElement-{} xPath-{}", contextElement, currentElement, expression);
        }

        if (DataStoreValidationUtil.isLocationPath(expression) && ((LocationPath)expression).getSteps()[0] != null){
            LocationPath path = (LocationPath)expression;
            if (path.getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
                LocationPath newLocationPath = buildPathFromRoot(currentElement, expression, contextNodePath, schemaRegistry);
                Expression newExpresssion = replaceCurrent(currentElement, newLocationPath, schemaRegistry);
                returnValue = validateXPath(contextElement, (Element) currentElement.getParentNode(), newExpresssion, contextNodePath, schemaRegistry);
            } else if (path.getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                returnValue = currentElement.getTextContent();
            } else {
                Step[] steps = path.getSteps();
                Step[] newSteps = new Step[steps.length];
                for (int i=0;i<steps.length;i++) {
                    newSteps[i] = buildNewStepFromOldWithNs(contextElement, currentElement, steps[i], schemaRegistry);
                }
                LocationPath newLocationPath = new LocationPath(path.isAbsolute(), newSteps);
                expression = replaceCurrent(currentElement, newLocationPath, schemaRegistry);
                isAbsolute = path.isAbsolute();

            }
        } else if (DataStoreValidationUtil.isOperation(expression)) {
            returnValue = validateOperation(contextElement, currentElement, expression, returnValue, contextNodePath, schemaRegistry);
        }

        if (returnValue == null) {
            expression = replaceCurrent(currentElement, expression, schemaRegistry);
            RequestScope scope = RequestScope.getCurrentScope();
            Map<String, Expression> xpathExprCache = (Map<String, Expression>) scope.getFromCache(XPATH_EXPR_CACHE);
            if(xpathExprCache == null){
                xpathExprCache = new HashMap<>();
                scope.putInCache(XPATH_EXPR_CACHE, xpathExprCache);
            }
            xpathExprCache.put(xPathStr, expression);
            if(returnValue == null  && !DataStoreValidationUtil.isExtensionFunction(expression)){
                returnValue = evaluateContext(contextElement, currentElement, expression, isAbsolute, contextNodePath, schemaRegistry);
            }
        }

        return returnValue;
    }

    protected Object validateOperation(Element contextElement, Element currentElement, Expression expression, Object returnValue, List<QName> contextNodePath, SchemaRegistry schemaRegistry) {

        if (DataStoreValidationUtil.isFunction(expression) && (((CoreFunction)expression).getFunctionCode() == Compiler.FUNCTION_COUNT)) {
            return evaluateCount(contextElement, currentElement, (CoreFunction) expression, schemaRegistry);
        } else if(DataStoreValidationUtil.isFunction(expression) && (((CoreFunction)expression).getFunctionCode() == Compiler.FUNCTION_BOOLEAN)) {
            return evaluateBoolean(contextElement, currentElement, expression, contextNodePath, schemaRegistry);
        }
        Expression[] expressions = ((Operation)expression).getArguments();
        Expression[] newExpressions = new Expression[expressions != null ? expressions.length : 0];
        Expression[] newListExpressions = null;
        Map<Integer, Expression[]> newListExpressionsMap = new HashMap<>();

        boolean or = DataStoreValidationUtil.isCoreOperationOr(expression);
        boolean and = DataStoreValidationUtil.isCoreOperationAnd(expression);

        for (int i=0;i<newExpressions.length;i++) {
            Object value = validateXPath(contextElement, currentElement, expressions[i], contextNodePath, schemaRegistry);

            if(value instanceof List){
                List listValues = (List)value;
                newListExpressions = new Expression[listValues != null ? listValues.size() : 0];
                for(int k=0; k< listValues.size(); k++){
                    Object listValue = listValues.get(k);
                    newListExpressions[k] = listValue instanceof Expression ? ((Expression)listValue) : JXPathUtils.getConstantExpression(listValue);
                }
                newListExpressionsMap.put(i, newListExpressions);
            } else {
                newExpressions[i] = value instanceof Expression ? ((Expression)value) : JXPathUtils.getConstantExpression(value);
                Expression[] expr = new Expression[1];
                expr[0] = newExpressions[i];
                newListExpressionsMap.put(i, expr);

                if (or || and) {
                    if (or && ((value instanceof Boolean && ((Boolean)value)) || Boolean.TRUE.toString().equals(value))) {
                        return value;
                    }

                    if (and && ((value instanceof Boolean && !((Boolean)value)) || Boolean.FALSE.toString().equals(value))) {
                        return value;
                    }
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

        if(newListExpressionsMap.size() ==2){
            Expression[] lhsExprs = newListExpressionsMap.get(0);
            Expression[] rhsExprs = newListExpressionsMap.get(1);
            if(lhsExprs.length>1 && rhsExprs.length==1){
                Expression[] coreArithmeticExprs = new Expression[2];
                for(Expression lhs : lhsExprs){
                    coreArithmeticExprs[0] = lhs instanceof Expression ? ((Expression)lhs) : JXPathUtils.getConstantExpression(lhs);
                    coreArithmeticExprs[1] = rhsExprs[0];
                    returnValue = evaluateOperation(currentElement, expression, returnValue, coreArithmeticExprs, schemaRegistry);
                    if(returnValue instanceof Boolean && ((Boolean)returnValue) || Boolean.TRUE.toString().equals(returnValue)){
                        return returnValue;
                    }
                }
            } else if(lhsExprs.length==1 && rhsExprs.length>1){
                Expression[] coreArithmeticExprs = new Expression[2];
                for(Expression rhs : rhsExprs){
                    coreArithmeticExprs[1] = rhs instanceof Expression ? ((Expression)rhs) : JXPathUtils.getConstantExpression(rhs);
                    coreArithmeticExprs[0] = lhsExprs[0];
                    returnValue = evaluateOperation(currentElement, expression, returnValue, coreArithmeticExprs, schemaRegistry);
                    if(returnValue instanceof Boolean && ((Boolean)returnValue) || Boolean.TRUE.toString().equals(returnValue)){
                        return returnValue;
                    }
                }
            }
        }
        
        returnValue = evaluateOperation(currentElement, expression, returnValue, newExpressions, schemaRegistry);
        return returnValue;
    }

    private Object evaluateBoolean(Element contextElement, Element currentElement, Expression expression, List<QName> contextNodePath, SchemaRegistry schemaRegistry) {
        Expression path = ((CoreFunction) expression).getArg1();
        Expression[] newExpressions = new Expression[1];
        if (path.toString().contains(DataStoreValidationUtil.CURRENT_PATTERN) && DataStoreValidationUtil.isExpressionPath(path)) {
            path = buildEvaluationPath(currentElement, (ExpressionPath) path, schemaRegistry);
            LocationPath locationPath = (LocationPath) path;
            newExpressions[0] = (Expression) locationPath;
        } else if (path instanceof LocationPath) {
            LocationPath locationPath = (LocationPath) path;
            LocationPath newLocationPath = buildPathFromRoot(currentElement, locationPath, contextNodePath, schemaRegistry);
            newExpressions[0] = (Expression) newLocationPath;
        }
        if (newExpressions[0] != null && newExpressions[0] instanceof LocationPath) {
            int fCode = ((CoreFunction) expression).getFunctionCode();
            CoreFunction function = JXPathUtils.getCoreFunction(fCode, newExpressions);
            Element jxpathContextElement = contextElement;
            jxpathContextElement = getRootElement(contextElement);
            JXPathContext context = JXPathContext.newContext(jxpathContextElement);
            setNSContext(context, currentElement);
            setNSContext(context, contextElement);
            context.setLenient(true);
            if (context instanceof JXPathContextReferenceImpl && context.getContextPointer() instanceof NodePointer) {
                return function.computeValue(new RootContext((JXPathContextReferenceImpl) context, (NodePointer) context.getContextPointer()));
            }
        }
        return null;
    }

    protected Object evaluateOperation(Element currentElement, Expression expression, Object returnValue, Expression[] newExpressions, SchemaRegistry schemaRegistry) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("evaluateOperation for currentElement-{} xpath-{} value-{} expression-{}", currentElement,
                    expression, returnValue, newExpressions);
        }
        if (DataStoreValidationUtil.isCoreOperation(expression)) {
            if (newExpressions.length == 2 && newExpressions[0] != null && newExpressions[1] != null) {
                Expression newExpression = JXPathUtils.getCoreOperation(((CoreOperation) expression), newExpressions);
                returnValue = newExpression.compute(null);
            }
        } else if (DataStoreValidationUtil.isExtensionFunction(expression)) {
            if (newExpressions.length == 0) {
                // FOR NOW ONLY CURRENT is supported
                returnValue = replaceCurrent(currentElement, expression, schemaRegistry).compute(null);
            }
        } else if (DataStoreValidationUtil.isFunction(expression)) {
            int fCode = ((CoreFunction) expression).getFunctionCode();
            CoreFunction function = (CoreFunction) expression;
            Expression newExpression = JXPathUtils.getCoreFunction(fCode, newExpressions);
            if (newExpression instanceof CoreFunction && fCode == Compiler.FUNCTION_LOCAL_NAME) {
                if (isFunctionWithoutArgs(function)) {
                    return currentElement.getLocalName();
                } else if (isFunctionWithOneArgs(function)) {
                    LocationPath path = (LocationPath) ((CoreFunction) expression).getArg1();
                    Step[] steps = path.getSteps();

                    currentElement = getLocalNameForCoreFunction(currentElement, path, steps);
                    return currentElement.getLocalName();
                }
            }

            if (newExpression instanceof CoreFunction && fCode == Compiler.FUNCTION_NAMESPACE_URI) {
                if (isFunctionWithoutArgs(function)) {
                    return currentElement.getNamespaceURI();
                } else if (isFunctionWithOneArgs(function)) {
                    LocationPath path = (LocationPath) ((CoreFunction) expression).getArg1();
                    // Get the parent node based on locationpath of
                    // namesapce-uri function
                    Element parent = getParentNode(path, currentElement);
                    return parent.getNamespaceURI();
                }
            }
            returnValue = newExpression.compute(null);
        }
        return returnValue;
    }

    private Element getLocalNameForCoreFunction(Element currentElement, LocationPath path, Step[] steps) {
        for (int j = 0; j < steps.length; j++) {
            if (steps[j].getAxis() == Compiler.AXIS_PARENT) {
                Element parentElement = getParentNode(path, currentElement);
                currentElement = parentElement;
            } else {
                String conditionString = steps[j].toString();
                NodeList childNodes = currentElement.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getLocalName().equals(conditionString)) {
                        currentElement = (Element) childNode;
                        break;
                    }
                }
            }
        }
        return currentElement;
    }

    private boolean isFunctionWithoutArgs(CoreFunction function) {
        if (function.getArgumentCount() == 0) {
            return true;
        }
        return false;
    }

    private Element getParentNode(LocationPath expression, Element currentNode) {
        Step[] steps = expression.getSteps();
        Node nextElement = currentNode;
        int index = 0;
        while (steps.length > index && steps[index].getAxis() == Compiler.AXIS_PARENT) {
            index++;
            nextElement = nextElement.getParentNode();
        }
        return (Element) nextElement;
    }

    protected boolean isFunctionWithOneArgs(CoreFunction function) {
        if (function.getArgumentCount() == 1) {
            return true;
        }
        return false;
    }

    protected Object evaluateContext(Element contextElement, Element currentElement, Expression expression, boolean isAbsolute, List<QName> paths, SchemaRegistry registry) {
        Object returnValue = null;
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
        setNSContextFromExpression(context, expression, registry);
        context.setLenient(true);
        List<Object> listValues = new ArrayList<>();
        Iterator iterator = context.iterate(expression.toString());
        iterator.forEachRemaining(listValues::add);


        if(listValues.isEmpty()){
            returnValue = getDefaultIfExists(paths, registry, returnValue);
        } else if(listValues.size() ==1){
            returnValue = listValues.get(0);
            returnValue = getDefaultIfExists(paths, registry, returnValue);
        } else {
            returnValue = listValues;
        }
        return returnValue;
    }

    private Object getDefaultIfExists(List<QName> paths, SchemaRegistry registry, Object returnValue) {
        if(returnValue == null && paths != null){
            Iterable<QName> iterable = (Iterable<QName>)paths;
            SchemaPath schemaPath = SchemaPath.create(iterable, true);
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

    private Object validateXPath(Element rootElement, Element currentElement, String xPath, SchemaRegistry schemaRegistry) {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        if (isDebugEnabled) {
            LOGGER.debug("validateXPath for rootElement-{} currentElement-{} xPath-{}", rootElement, currentElement, xPath);
        }
        return validateXPath(rootElement, currentElement, JXPathUtils.getExpression(xPath), null, schemaRegistry);
    }

    private void validateChoicecase(List<SchemaPath> childrenElementNodeSchemaPath,
            List<Element> elements, SchemaPath schemaPath,
            RequestType requestType,
            Element rootElement,
            SchemaRegistry schemaRegistry,
            NetconfRpcPayLoadType rpcType,
            ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry, Element rpcContext) throws ValidationException {
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
                    validateConstraint(element, choiceCaseSchemaPath, requestType, rootElement, schemaRegistry, rpcType, modelNodeDsm, dsmRegistry, rpcContext);
                    SchemaPath choiceSchemaPath = choiceCaseSchemaPath.getParent();
                    validateConstraint(element, choiceSchemaPath, requestType, rootElement, schemaRegistry, rpcType, modelNodeDsm, dsmRegistry, rpcContext);					
                    validatedNestedChoice(elements, requestType, rootElement, schemaRegistry, rpcType, modelNodeDsm, dsmRegistry, rpcContext, index, choiceSchemaPath);					

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

    private void validatedNestedChoice(List<Element> elements, RequestType requestType, Element rootElement, SchemaRegistry schemaRegistry, NetconfRpcPayLoadType rpcType, ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry, Element rpcContext, int index, SchemaPath choiceSchemaPath) {
        SchemaPath ParentSchemaPath = choiceSchemaPath.getParent();
        while (schemaRegistry.getDataSchemaNode(ParentSchemaPath) instanceof CaseSchemaNode) {
            Element element = elements.get(index);
            validateConstraint(element, ParentSchemaPath, requestType, rootElement, schemaRegistry, rpcType, modelNodeDsm, dsmRegistry, rpcContext);
            SchemaPath nestedChoiceSchemaPath = ParentSchemaPath.getParent();
            validateConstraint(element, nestedChoiceSchemaPath, requestType, rootElement, schemaRegistry, rpcType, modelNodeDsm, dsmRegistry, rpcContext);
            ParentSchemaPath = nestedChoiceSchemaPath.getParent();
        }
    }

    protected void typeValidation(Element element, SchemaPath schemaPath, RequestType requestType, SchemaRegistry schemaRegistry) throws ValidationException {
        SchemaNodeConstraintParser schemaNodeConstraintParser = ConstraintValidatorFactoryImpl.getInstance()
                .getConstraintNodeValidator(getDataSchemaNode(schemaPath, schemaRegistry), schemaRegistry, getExpressionValidator());
        if (schemaNodeConstraintParser != null) {
            schemaNodeConstraintParser.validate(element, requestType);
        }
    }

    private DataSchemaNode getDataSchemaNode(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if ( dataSchemaNode == null && schemaRegistry.getParentRegistry() != null){
            dataSchemaNode = schemaRegistry.getParentRegistry().getDataSchemaNode(schemaPath);
        }
        return dataSchemaNode;
    }

    protected void validateAttributes(Element parentElement, Element childNode, SchemaPath schemaPath) throws ValidationException {
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

    private Object evaluateCount(Element contextElement, Element currentElement, CoreFunction xPath, SchemaRegistry schemaRegistry) {
        Expression expression = xPath.getArg1();
        if (DataStoreValidationUtil.isLocationPath(expression)) {
            Object[] values = new Object[1];
            if (((LocationPath)expression).getSteps().length == 1) {
                LocationPath path = buildPathFromRoot(contextElement, currentElement, (LocationPath)expression, schemaRegistry);
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
                values[0] = buildPathFromRoot(contextElement, currentElement, (LocationPath) expression, schemaRegistry);
            }
            Expression newExpression = JXPathUtils.getCoreFunction(xPath.getFunctionCode(), values);
            newExpression = replaceCurrent(currentElement, newExpression, schemaRegistry);
            return evaluateContext(contextElement, currentElement, newExpression, true, null, schemaRegistry);
        } else if (DataStoreValidationUtil.isExpressionPath(expression)) {
            Object[] values = new Object[1];
            values[0] = buildEvaluationPath(currentElement, (ExpressionPath)expression, schemaRegistry);
            Expression newExpression = JXPathUtils.getCoreFunction(xPath.getFunctionCode(), values);
            newExpression = replaceCurrent(currentElement, newExpression, schemaRegistry);
            return evaluateContext(contextElement, currentElement, newExpression, true, null, schemaRegistry);
        }
        return null;
    }

    private LocationPath buildPathFromRoot(Element root, Element currentElement, LocationPath expression,SchemaRegistry schemaRegistry) {
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
                String prefix = rootElement.getPrefix() != null ? rootElement.getPrefix() : getPrefixActionElement(rootElement);
                Step newStep = new YangStep(new org.apache.commons.jxpath.ri.QName(prefix, rootElement.getLocalName()), rootElement.getNamespaceURI());
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
                    Step newStep = buildNewStepFromOldWithNs(root, (Element) nextElement, oldStep, schemaRegistry);
                    totalSteps[totalStepIndex] = newStep;
                }
            }

            if (totalSteps.length > 0 && totalSteps[0] != null) {
                return new LocationPath(false, totalSteps);
            }
        } else {
            Step[] newSteps = new Step[steps.length]; 
            for (int i=0;i<newSteps.length;i++) {
                newSteps[i] = buildNewStepFromOldWithNs(root, currentElement, steps[i], schemaRegistry);
            }
            return new LocationPath(expression.isAbsolute(), newSteps);
        }
        return expression;
    }

    private String getPrefixFromElement(Node currentElement) {
        String prefix = null;
        if (currentElement != null) {
            prefix = currentElement.getPrefix();
            if (prefix == null) {
                prefix = getPrefixFromNS(currentElement.getNamespaceURI());
            }
            if(prefix == null){
                prefix = getPrefixActionElement(currentElement);
            }
        }
        return prefix;
    }

    private String getPrefixFromNS(String namespace) {
        String prefix = null;
        if (namespace != null && !namespace.isEmpty()) {
            prefix = getSchemaRegistry(null).getPrefix(namespace);
        }
        return prefix;
    }

    private String getPrefixFromNS(String namespace, SchemaRegistry schemaRegistry) {
        String prefix = null;
        if (schemaRegistry != null && namespace != null && !namespace.isEmpty()) {
            prefix = schemaRegistry.getPrefix(namespace);

            if(prefix == null) {
                prefix = getSchemaRegistry(null).getPrefix(namespace);
            }
        }
        return prefix;
    }

    private Step buildNewStepFromOldWithNs(Element rootElement, Element nextElement, Step oldStep, SchemaRegistry schemaRegistry) {
        NodeNameTest nodeTest = (NodeNameTest) oldStep.getNodeTest();
        String prefix = nodeTest.getNodeName().getPrefix();
        String name = nodeTest.getNodeName().getName();
        String ns = nodeTest.getNamespaceURI();
        if (prefix == null) {
            if (ns != null && !ns.isEmpty()) {
                prefix = getPrefixFromNS(ns, schemaRegistry);
            } else {
                Element child = null;
                if (nextElement != null) {
                    child = DocumentUtils.getDescendant((Element) nextElement, name, rootElement.getNamespaceURI());
                }
                ns = child == null ? rootElement.getNamespaceURI() : child.getNamespaceURI();
                if(ns != null) {
                    prefix = getPrefixFromNS(ns, schemaRegistry);
                }
                else {
                    prefix = child == null ? rootElement.getPrefix() : child.getPrefix();
                }
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
                String currentElementPrefix = getPrefixFromElement(currentElement);
                if (currentElementPrefix != null) {
                    context.registerNamespace(currentElementPrefix, currentElement.getNamespaceURI());
                }
                currentElement = currentElement.getParentNode();
            }
        }
    }

    private void setNSContextFromExpression(JXPathContext context, Expression expression, SchemaRegistry schemaRegistry) {
        if(expression instanceof LocationPath) {
            LocationPath locationPath = (LocationPath)expression;
            for(Step step : locationPath.getSteps()) {
                if(step != null && step.getNodeTest() != null && step.getNodeTest() instanceof NodeNameTest) {
                    NodeNameTest nodeNameTest = (NodeNameTest) step.getNodeTest();
                    String prefix = nodeNameTest.getNodeName().getPrefix();
                    String nameSpace = nodeNameTest.getNamespaceURI();
                    if (nameSpace != null) {
                        prefix = schemaRegistry.getPrefix(nameSpace);
                    }
                    if(prefix != null && nameSpace != null) {
                        context.registerNamespace(prefix, nameSpace);
                    }
                }
            }
        }
    }

    private Expression replaceCurrent(Element currentElement, Expression xPath, SchemaRegistry schemaRegistry) {
        if (xPath.toString().contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
            if (DataStoreValidationUtil.isLocationPath(xPath)) {
                return replaceCurrentOnLocationPath(currentElement, xPath, schemaRegistry);
            } else if (DataStoreValidationUtil.isOperation(xPath)) {
                return replaceCurrentOnOperation(currentElement, xPath, schemaRegistry);
            } else if (DataStoreValidationUtil.isExpressionPath(xPath)) {
                return replaceCurrentOnExpressionPath(currentElement, xPath, schemaRegistry);
            }
        } else if (DataStoreValidationUtil.isLocationPath(xPath)) {
            return buildPathWithNs(currentElement, xPath, schemaRegistry);
        } 

        return xPath;
    }

    protected Expression buildPathWithNs(Element currentElement, Expression xPath, SchemaRegistry schemaRegistry) {
        Step[] steps = ((LocationPath)xPath).getSteps();
        Step[] newSteps = new Step[steps.length];
        boolean changed = false;
        for (int i=0;i<steps.length;i++) {
            if (steps[i].getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = (NodeNameTest) steps[i].getNodeTest();
                if (node.getNodeName().getPrefix() == null) {
                    newSteps[i] = buildNewStepFromOldWithNs(currentElement, null, steps[i], schemaRegistry);
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

    protected Expression replaceCurrentOnExpressionPath(Element currentElement, Expression xPath, SchemaRegistry schemaRegistry) {
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
        LocationPath newPath = buildEvaluationPath(currentElement, (ExpressionPath)xPath, schemaRegistry);
        return JXPathUtils.getConstantExpression(validateXPath(newContextElement, currentElement, newPath, null, schemaRegistry));
    }

    protected Expression replaceCurrentOnOperation(Element currentElement, Expression xPath, SchemaRegistry schemaRegistry) {
        Operation operation = (Operation) xPath;
        Expression[] oldExpressions = operation.getArguments();
        Expression[] newExpressions = new Expression[oldExpressions == null ? 0 : oldExpressions.length];
        boolean changed = false;

        for (int i=0;i<newExpressions.length;i++) {
            newExpressions[i] = replaceCurrent(currentElement, oldExpressions[i], schemaRegistry);
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

    protected Expression replaceCurrentOnLocationPath(Element currentElement, Expression xPath, SchemaRegistry schemaRegistry) {
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
                        newExpressions[j] = replaceCurrent(currentElement, oldExpressions[j], schemaRegistry);
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

    private static String getPrefixActionElement(Node actionElement) {
        if (actionElement.getPrefix() == null && actionElement.getLocalName().equals(NetconfResources.ACTION)
                && actionElement.getNamespaceURI().equals(NetconfResources.NETCONF_YANG_1)) {
            return NetconfResources.ACTION;
        }
        return null;
    }

    private LocationPath buildPathFromRoot(Element contextElement, Expression expression, List<QName> contextNodeExpressionPath, SchemaRegistry schemaRegistry) {
        Step[] step = ((LocationPath)expression).getSteps();
        int index = 0;
        Node prevElement = contextElement;
        if (contextNodeExpressionPath != null){
            SchemaNode contextNode = schemaRegistry.getDataSchemaNode(SchemaPath.create(contextNodeExpressionPath, true));
            int stepIndexForInputOrOutputNode = 0;
            while ( contextNode != null){
                if ( step.length <= stepIndexForInputOrOutputNode){
                    break;
                }
                if ( step[stepIndexForInputOrOutputNode].getAxis() != Compiler.AXIS_PARENT){
                    break; // This case is condition on input leaf node, but target referring with-in input (action input) itself.
                }
                if ( contextNode instanceof InputEffectiveStatement || contextNode instanceof OutputEffectiveStatement){
                    index++; // This case is condition on input leaf node, but target referring to leaf outside the input (action input).
                    break;
                } else {
                    SchemaPath parentPath = contextNode.getPath().getParent();
                    contextNode = SchemaRegistryUtil.getEffectiveParentNode(contextNode, schemaRegistry);
                    if ( contextNode == null){
                        contextNode = schemaRegistry.getActionDefinitionNode(DataPathUtil.convertToDataPath(parentPath));
                    }
                    stepIndexForInputOrOutputNode++;
                }
            }
        }

        while (step.length > index && step[index].getAxis() == Compiler.AXIS_PARENT) {
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
            String ns = prevElement.getNamespaceURI();
            String prefix = null;
            if(ns != null) {
                prefix = getPrefixFromNS(ns, schemaRegistry);
                if(prefix == null) {
                    prefix = getPrefixActionElement(prevElement);
                }
            } else {
                prefix = prevElement.getPrefix() != null ? prevElement.getPrefix()
                        : getPrefixActionElement(prevElement);
            }
            stepsToRoot.add(0, new YangStep(new org.apache.commons.jxpath.ri.QName(prefix, prevElement.getLocalName()),
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
            newSteps[j] = buildNewStepFromOldWithNs(contextElement, (Element) prevElement, step[i], schemaRegistry);
        }

        return new LocationPath(true, newSteps);	    
    }

    private LocationPath buildEvaluationPath(Element currentElement, ExpressionPath xPath, SchemaRegistry schemaRegistry) {
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
        return buildPathFromRoot(newContextElement, new LocationPath(false, newSteps), null, schemaRegistry);

    }

    private Expression buildChoiceCasePath(Element currentElement, Expression xPath, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
            return buildPathForChoiceCase(currentElement, xPath, schemaNode, schemaRegistry);
        }
        return xPath;
    }

    private Expression buildPathForChoiceCase(Element currentElement, Expression xPath, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {

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
                return buildPathFromRoot(currentElement, new LocationPath(false, newSteps), null, schemaRegistry);
            }
        } else if (DataStoreValidationUtil.isOperation(xPath)) {
            return buildChoiceCasePathForOperation(currentElement, xPath, schemaNode, schemaRegistry);
        } else if (DataStoreValidationUtil.isExpressionPath(xPath)) {
            return buildChoiceCasePathForExpressionPath(currentElement, xPath, schemaNode, schemaRegistry);
        }
        return xPath;
    }

    protected Expression buildChoiceCasePathForExpressionPath(Element currentElement, Expression xPath, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        // For now only current is supported. But this will not break if other functions are used. 
        ExpressionPath path = (ExpressionPath) xPath;
        Expression function = path.getExpression();
        Step[] steps = path.getSteps();
        boolean changed = false;
        if (steps != null) {
            Expression locationPath = new LocationPath(false, steps);
            Expression expression = buildChoiceCasePath(currentElement, locationPath, schemaNode, schemaRegistry);
            if (!expression.equals(locationPath)) {
                changed = true;
                steps = ((LocationPath) expression).getSteps();
            }
        }

        Expression[] predicates = path.getPredicates();
        Expression[] newPredicates = new Expression[predicates != null ? predicates.length : 0];
        for (int i = 0; i < newPredicates.length; i++) {
            newPredicates[i] = buildChoiceCasePath(currentElement, predicates[i], schemaNode, schemaRegistry);
            if (!newPredicates[i].equals(predicates[i])) {
                changed = true;
            }
        }

        if (changed) {
            return new ExpressionPath(function, predicates == null ? predicates : newPredicates, steps);
        }
        return xPath;
    }

    protected Expression buildChoiceCasePathForOperation(Element currentElement, Expression xPath, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        Expression[] oldExpressions = ((Operation) xPath).getArguments();
        Expression[] newExpressions = new Expression[oldExpressions == null ? 0 : oldExpressions.length];
        boolean changed = false;
        for (int i = 0; i < newExpressions.length; i++) {
            newExpressions[i] = buildChoiceCasePath(currentElement, oldExpressions[i], schemaNode, schemaRegistry);
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
