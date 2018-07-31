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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationAnd;
import org.apache.commons.jxpath.ri.compiler.CoreOperationCompare;
import org.apache.commons.jxpath.ri.compiler.CoreOperationOr;
import org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ActionUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ElementWrapper;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.OperationAttributeUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.NetconfRpcPayLoadType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;

import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Checks if there are invalid nodes in the children of a given node.
 * <p>
 * Created by keshava on 11/24/15.
 */
public abstract class SchemaElementChildrenConstraintParser {
    static final String OPEN_SQUARE = "[";
    static final String CLOSE_SQUARE = "]";

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(SchemaElementChildrenConstraintParser.class,
            "netconf-stack", "DEBUG", "GLOBAL");

    public abstract ModelNodeDataStoreManager getDsm();

    public abstract DSExpressionValidator getExpressionValidator();

    protected SchemaPath getChildSchemaPath(SchemaRegistry schemaRegistry, Element element, SchemaPath parentPath) {
        Collection<DataSchemaNode> children = schemaRegistry.getChildren(parentPath);
        SchemaPath childSchemaPath = getSchemaPathForElement(element, getSchemaPathsForNodes(children));
        return childSchemaPath;
    }

    /**
     * Iterates though child nodes to search for invalid nodes.
     * Override this method to perform more specific checks on the parent node.
     *
     * @param element      - parent element
     * @param schemaPath   - schema path of the parent
     * @param modelNodeDsm - DataStoreManager
     * @throws ValidationException
     */

    protected void validateElement(Element element, SchemaPath schemaPath, RequestType requestType, Element rootElement,
                                   ModelNodeDataStoreManager modelNodeDsm, NetconfRpcPayLoadType rpcType,
                                   ModelNodeDSMRegistry dsmRegistry) throws ValidationException {

        SchemaRegistry schemaRegistry = getSchemaRegistry();
        throwErrorIfActionExistsWithinRPC(element, schemaPath, requestType, schemaRegistry, element);
        throwErrorIfActionExistsWithinNotification(element, schemaPath, requestType, schemaRegistry, element);

        //go though children
        NodeList nodes = element.getChildNodes();
        List<SchemaPath> childrenElementNodeSchemaPath = new ArrayList<>();
        List<Element> childrenElement = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childNode = nodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                throwErrorIfActionExistsWithinRPC(element, schemaPath, requestType, schemaRegistry, childNode);
                throwErrorIfActionExistsWithinNotification(element, schemaPath, requestType, schemaRegistry, childNode);
                Collection<DataSchemaNode> children = schemaRegistry.getChildren(schemaPath);
                SchemaPath childSchemaPath = getSchemaPathForElement((Element) childNode, getSchemaPathsForNodes
                        (children));

                if (childSchemaPath == null) {
                    if (requestType.isAction()) {
                        ActionDefinition actionDef = getMatchedActionDefinition((Element) childNode, schemaPath);
                        if (actionDef != null) {
                            if (actionDef.getInput() != null) {
                                childSchemaPath = actionDef.getInput().getPath();
                            } else {
                                break;
                            }
                        }
                    }
                }
                /**
                 * During RPC output validation, the 'schemaPath' received and the 'childNode' for which we are
                 * getting a null value,
                 * are child-grand-parent relation. So we need to get the schemaPath of 'element' and look for the
                 * childNode.
                 */
                if (childSchemaPath == null) {
                    SchemaPath parentPath = getSchemaPathForElement(element, getSchemaPathsForNodes(children));
                    if (parentPath != null) {
                        Collection<DataSchemaNode> allChildren = schemaRegistry.getChildren(parentPath);
                        childSchemaPath = getSchemaPathForElement((Element) childNode, getSchemaPathsForNodes
                                (allChildren));
                    }

                }

                if (childSchemaPath == null) {
                    childSchemaPath = getSchemaPathForElement((Element) childNode, getSchemaPathFromCases(children));
                    if (childSchemaPath == null) {
                        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);

                        if (!(dataSchemaNode instanceof AnyXmlSchemaNode)) {
                            Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath
                                    (element, dataSchemaNode, getSchemaRegistry(), (Element) childNode);
                            NetconfRpcError rpcError;
                            if (schemaRegistry.isKnownNamespace(childNode.getNamespaceURI())) {
                                rpcError = NetconfRpcError.getUnknownElementError(childNode.getLocalName(),
                                        NetconfRpcErrorType.Application);
                            } else {
                                rpcError = NetconfRpcError.getUnknownNamespaceError(childNode.getNamespaceURI(),
                                        childNode.getLocalName(), NetconfRpcErrorType.Application);
                            }

                            rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
                            throw new ValidationException(rpcError);
                        } else {
                            break; // To exclude validation of xml content of anyXml node
                        }
                    }
                }

                validateOperation(element, (Element) childNode, schemaPath);
                validateInsertAttributes(element, (Element) childNode, childSchemaPath);
                validateElement((Element) childNode, childSchemaPath, requestType, rootElement, modelNodeDsm,
                        rpcType, dsmRegistry);
                childrenElementNodeSchemaPath.add(childSchemaPath);
                childrenElement.add((Element) childNode);
            }
        }

        validateConstraint(element, schemaPath, requestType, rootElement, schemaRegistry, rpcType);
        validateChoicecase(childrenElementNodeSchemaPath, childrenElement, schemaPath, requestType, element,
                schemaRegistry, rpcType);
        typeValidation(element, schemaPath, requestType);
        if (requestType.isRpc() || requestType.isAction()) {
            validateLeafType(childrenElement, childrenElementNodeSchemaPath, schemaRegistry, element, rootElement,
                    modelNodeDsm, dsmRegistry);
        }
    }

    private void throwErrorIfActionExistsWithinNotification(Element element, SchemaPath schemaPath, RequestType
            requestType,
                                                            SchemaRegistry schemaRegistry, Node childNode) {
        if (requestType.isAction() && checkNotificationExistsOnActionTree((Element) childNode)) {
            NetconfRpcError rpcError = getRpcError(element, schemaPath, schemaRegistry, childNode);
            rpcError.setErrorMessage("Notification Element " + childNode.getLocalName() + " should not exist within " +
                    "Action Tree");
            throw new ValidationException(rpcError);
        }
    }

    private void throwErrorIfActionExistsWithinRPC(Element element, SchemaPath schemaPath, RequestType requestType,
                                                   SchemaRegistry schemaRegistry, Node childNode) {
        if (requestType.isAction() && checkRPCExistsOnActionTree((Element) childNode)) {
            NetconfRpcError rpcError = getRpcError(element, schemaPath, schemaRegistry, childNode);
            rpcError.setErrorMessage("RPC Element " + childNode.getLocalName() + " should not exist within Action " +
                    "Tree");
            throw new ValidationException(rpcError);
        }
    }

    private ActionDefinition getMatchedActionDefinition(Element childNode, SchemaPath schemaPath) {
        Set<ActionDefinition> actionDefs = ActionUtils.retrieveActionDefinitionForSchemaNode(schemaPath,
                getSchemaRegistry());
        for (ActionDefinition action : actionDefs) {
            QName actionQName = action.getQName();
            if (actionQName.getNamespace().toString().equals(childNode.getNamespaceURI()) && actionQName.getLocalName
                    ().equals(childNode.getLocalName())) {
                return action;
            }
        }
        return null;
    }

    private NetconfRpcError getRpcError(Element element, SchemaPath schemaPath, SchemaRegistry schemaRegistry,
                                        Node childNode) {
        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(element, dataSchemaNode,
                getSchemaRegistry(), (Element) childNode);
        NetconfRpcError rpcError = NetconfRpcError.getBadElementError(childNode.getLocalName(), NetconfRpcErrorType
                .Protocol);

        rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
        return rpcError;
    }

    private boolean checkRPCExistsOnActionTree(Element element) {
        SchemaRegistry schemaRegistry = getSchemaRegistry();
        Collection<RpcDefinition> rpcDefinitions = schemaRegistry.getRpcDefinitions();
        for (RpcDefinition rpcDefinition : rpcDefinitions) {
            QName qName = rpcDefinition.getQName();
            if (qName.getLocalName().equals(element.getLocalName()) && qName.getNamespace().toString().equals(element
                    .getNamespaceURI())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNotificationExistsOnActionTree(Element element) {
        SchemaRegistry schemaRegistry = getSchemaRegistry();
        Collection<NotificationDefinition> notificationDefinitions = schemaRegistry.getSchemaContext()
                .getNotifications();
        for (NotificationDefinition notificationDefinition : notificationDefinitions) {
            QName qName = notificationDefinition.getQName();
            if (qName.getLocalName().equals(element.getLocalName()) && qName.getNamespace().toString().equals(element
                    .getNamespaceURI())) {
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
                                  ModelNodeDataStoreManager modelNodeDsm, ModelNodeDSMRegistry dsmRegistry) throws
            ValidationException, DOMException {
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
                        Object value = validateXPath(rootElement, currentChild, xPathValue);
                        if (value == null) {
                            validateOnDataStore(schemaRegistry, xPathValue, currentChild.getTextContent(),
                                    modelNodeDsm, currentElement, schemaNode, dsmRegistry);
                        }
                        if (value == null || (value instanceof Boolean && !((Boolean) value))) {
                            prepareMissingDataException(schemaRegistry, rootElement, currentChild, xPathValue);

                        }
                    }
                } else if (((LeafSchemaNode) schemaNode).getType() instanceof LeafrefTypeDefinition) {
                    if (schemaNode.isAddedByUses()) {
                        // FIXME: FNMS-10117 Currently grouping nodes are not validated.
                        return;
                    }
                    Object value;
                    LeafrefTypeDefinition type = (LeafrefTypeDefinition) ((LeafSchemaNode) schemaNode).getType();
                    Module moduleName = schemaRegistry.getModuleByNamespace(schemaNode.getQName().getNamespace()
                            .toString());
                    String xPath = type.getPathStatement().toString();
                    SchemaPath leafRefPath = SchemaRegistryUtil.getSchemaPath(schemaRegistry, xPath,
                            schemaNode.getQName().getNamespace().toString(), moduleName.getName());
                    DataSchemaNode leafRefNode = null;
                    if (leafRefPath != null) {
                        leafRefNode = schemaRegistry.getDataSchemaNode(leafRefPath);
                    }

                    // FIXME: FNMS-10117 Workaround to unblock tests failing on config false leafrefs
                    // Assumption: LeafRef paths referring to state leaf can only be absolute in a custom RPC
                    if (leafRefNode != null && !leafRefNode.isConfiguration()) {
                        value = true;
                    } else {
                        // Relative leafRef paths referring to a config leaf are resolved here
                        value = validateXPath(currentElement, currentChild, xPath);
                        if (value == null) {
                            value = validateOnDataStore(schemaRegistry, xPath, currentChild.getTextContent(),
                                    modelNodeDsm, currentElement, schemaNode, dsmRegistry);
                        }
                    }
                    if (value == null || (value instanceof Boolean && !((Boolean) value))
                            || (!(value instanceof Boolean) && !(value.equals(xPathValue)))) {
                        prepareMissingDataException(schemaRegistry, rootElement, currentChild, xPathValue);
                    }
                }
            }
            index++;
        }
    }

    private void prepareMissingDataException(SchemaRegistry schemaRegistry, Element rootElement, Element
            currentChild, String xPathValue) throws ValidationException {
        StringBuilder builder = new StringBuilder();
        getAbsolutePathToParent(rootElement, currentChild, builder, schemaRegistry);
        String namespace = currentChild.getNamespaceURI();
        String prefix = schemaRegistry.getPrefix(namespace);
        Map<String, String> nsContext = new HashMap<>();
        nsContext.put(prefix, namespace);
        throw DataStoreValidationErrors.getMissingDataException(
                String.format("Missing required element %s", xPathValue), builder.toString(), nsContext);
    }

    private boolean validateOnDataStore(SchemaRegistry schemaRegistry, String xPath, String leafRefValue,
                                        ModelNodeDataStoreManager modelNodeDsm, Element currentElement,
                                        DataSchemaNode schemaNode, ModelNodeDSMRegistry dsmRegistry) throws
            ValidationException {
        boolean isValid = false;
        List<ModelNode> nodesList;
        Iterator<SchemaPath> schemaPaths = schemaRegistry.getRootSchemaPaths().iterator();
        while (schemaPaths.hasNext() && !isValid) {
            SchemaPath schemaPath = schemaPaths.next();
            if (schemaPath != null) {
                if (dsmRegistry != null && dsmRegistry.lookupDSM(schemaPath) != null) {
                    nodesList = dsmRegistry.lookupDSM(schemaPath).listNodes(schemaPath);
                } else {
                    nodesList = modelNodeDsm.listNodes(schemaPath);
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

    private void getAbsolutePathToParent(Element rootElement, Node currentElement, StringBuilder path, SchemaRegistry
            schemaRegistry) {
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

            if (currentElement.getParentNode() != null) {
                getAbsolutePathToParent(rootElement, currentElement.getParentNode(), path, schemaRegistry);
            }
        }

    }

    private void validateConstraint(Element element, SchemaPath schemaPath, RequestType requestType,
                                    Element rootElement, SchemaRegistry schemaRegistry, NetconfRpcPayLoadType
                                            rpcType) throws ValidationException {
        // For RPC requests, when validation should be done on the input request element
        if (requestType.isRpc()) {
            DataSchemaNode schemaNode = null;

            if (rpcType.isResponse()) {
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
            } else {
                // this is rpc input validation
                schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
            }

            if (schemaNode != null) {
                RevisionAwareXPath whenCondition = schemaNode.getConstraints().getWhenCondition();
                if (whenCondition != null) {
                    validateWhen(rootElement, element, whenCondition);
                }

                Set<MustDefinition> mustConstraints = schemaNode.getConstraints().getMustConstraints();
                if (mustConstraints != null && !mustConstraints.isEmpty()) {
                    validateMust(rootElement, element, mustConstraints);
                }
            }
        }
    }

    private void validateWhen(Element rootElement, Element currentElement, RevisionAwareXPath revisionAwareXPath)
            throws ValidationException {
        String xPath = revisionAwareXPath.toString();
        Object value = validateXPath(rootElement, currentElement, xPath);
        if (value != null && value instanceof Boolean && !(Boolean) value) {
            LOGGER.error("Validation failed for xpath - {} rootElement - {} currentElement - {}", xPath, rootElement,
                    currentElement);
            throw DataStoreValidationErrors.getViolateWhenConditionExceptionThrownUnknownElement(xPath);
        }
    }

    private void validateMust(Element rootElement, Element currentElement, Set<MustDefinition> mustConstraints)
            throws ValidationException {
        for (MustDefinition mustDefinition : mustConstraints) {
            String xPath = mustDefinition.getXpath().toString();
            Expression ex = JXPathUtils.getExpression(xPath);
            if (ex != null && ex instanceof CoreOperation) {
                Object value = validateOperation(rootElement, currentElement, (CoreOperation) ex);
                if (value != null && value instanceof Boolean && !(Boolean) value) {
                    throw DataStoreValidationErrors.getViolateMustContrainsException(mustDefinition);
                }
            }
        }
    }

    private Object validateXPath(Element rootElement, Element currentElement, String xPath) {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        if (isDebugEnabled) {
            LOGGER.debug("validateXPath for rootElement-{} currentElement-{} xPath-{}", rootElement, currentElement,
                    xPath);
        }

        // remove the parent path, since we always set the context at parent
        String excludeParentPath = xPath.substring(xPath.indexOf(DataStoreValidationUtil.SLASH, 1) + 1);

        // if the path steps dont have prefix, append them
        excludeParentPath = appendPrefix(currentElement, excludeParentPath, true);

        //create context, set current() custom function, register current ns, and evaluate
        JXPathContext context = JXPathContext.newContext(rootElement);
        excludeParentPath = setFunctionOnContext(context, excludeParentPath, currentElement);
        context.registerNamespace(currentElement.getPrefix(), currentElement.getNamespaceURI());
        context.setLenient(true);
        Object returnValue = context.getValue(excludeParentPath);

        if (returnValue == null || (returnValue instanceof Boolean && !((Boolean) returnValue))) {
            if (excludeParentPath.startsWith(DataStoreValidationUtil.PARENT_PATTERN)) {
                Expression ex = JXPathUtils.getExpression(xPath);
                if (ex instanceof LocationPath) {
                    /**
                     * Indicates this is most likely a leaf ref case.
                     *
                     * container a{
                     * 		list b{
                     * 			list c{
                     * 				leaf d {
                     * 					../../../leaf1
                     *                }
                     *            }
                     *        }
                     * 		leaf leaf1{
                     *        }
                     * }
                     *
                     * When we reach here, we are at 'd' with leaf ref.
                     *
                     * validate again with 'c', 'd's parent and remove one step of the xpath.
                     */

                    Step[] step = ((LocationPath) ex).getSteps();
                    List<Step> newSteps = new ArrayList<Step>(Arrays.asList(step));
                    newSteps.remove(0);
                    LocationPath newLocationPath = new LocationPath(true, newSteps.toArray(new Step[]{}));

                    returnValue = validateXPath((Element) rootElement.getParentNode(),
                            (Element) currentElement.getParentNode(), newLocationPath.toString());
                } else {
                    if (xPath.contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
                        rootElement = (Element) currentElement.getParentNode();
                        returnValue = validateXPath((Element) rootElement.getParentNode(),
                                (Element) currentElement.getParentNode(), excludeParentPath);
                    } else {
                        /**
                         * Since it is an operation, we might be in the middle of already parsed operation.
                         * So continue to use excludeParentPath and not the original xpath
                         */
                        ex = JXPathUtils.getExpression(excludeParentPath);
                        returnValue = validateOperation(rootElement, currentElement, ex);
                    }
                }
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug("validateXPath for rootElement-{} currentElement-{} xPath-{} and returnValue {}",
                    rootElement, currentElement, xPath, returnValue);
        }
        return returnValue;
    }

    private static String appendPrefixForCount(String prefix, String path, boolean validateRpcOnly) {
        String returnValue = path;
        Expression expression = JXPathUtils.getExpression(path);
        if (expression instanceof CoreFunction) {
            CoreFunction function = (CoreFunction) expression;
            if (function.getFunctionCode() == Compiler.FUNCTION_COUNT) {
                returnValue = appendPrefix(prefix, function.getArg1().toString(), validateRpcOnly);
            }
        } else if (expression instanceof CoreOperation) {
            CoreOperation operation = (CoreOperation) expression;
            Expression[] expressions = new Expression[operation.getArguments().length];
            int index = 0;
            for (Expression exp : operation.getArguments()) {
                if (exp instanceof CoreFunction) {
                    Expression countArgument = ((CoreFunction) exp).getArg1();
                    String newArgument = appendPrefix(prefix, countArgument.toString(), validateRpcOnly);
                    Expression[] newExpressions = new Expression[1];
                    newExpressions[0] = JXPathUtils.getExpression(newArgument);
                    expressions[index++] = (JXPathUtils.getCoreFunction(((CoreFunction) exp).getFunctionCode(),
                            newExpressions));

                } else {
                    expressions[index++] = (exp);
                }
            }

            if (index > 0) {
                returnValue = JXPathUtils.getCoreOperation(operation, expressions).toString();
            }
        }

        return returnValue;
    }

    public static String appendPrefix(String prefix, String excludeParentPath, boolean validateRpcOnly) {

        if (prefix != null && !prefix.isEmpty()) {

            // append the local prefix. A non prefix condition indicates, it is
            // referring to a local leaf/same namespace
            //FIXME: The below string comparison will be fixed by FNMS-7415
            String[] paths = excludeParentPath.split(DataStoreValidationUtil.SLASH);
            StringBuilder newPath = new StringBuilder();
            for (String path : paths) {
                if (!path.isEmpty() && !path.equals("..") && !path.contains(DataStoreValidationUtil.COLON)
                        && path.startsWith("count(")) {
                    newPath.append(appendPrefixForCount(prefix, path, validateRpcOnly));
                } else if (!path.isEmpty() && !path.equals("..") && !path.contains(DataStoreValidationUtil.COLON)
                        && !path.equals(DataStoreValidationUtil.CURRENT_PATTERN)) {
                    newPath.append(prefix).append(DataStoreValidationUtil.COLON).append(path).append
                            (DataStoreValidationUtil.SLASH);
                } else {
                    newPath.append(path).append(DataStoreValidationUtil.SLASH);
                }
            }

            /**
             * Appending prefix incase of special functions are added. 
             * 1) validateRpcOnly is true only when we are validating against the payload. 
             *    in this case, /abc[name = current()/../abc]/xyz
             *
             *    here name must be appended with prefix. like
             *    /prefix:abc[prefix:name = current()/../prefix:abc]/prefix/yxz
             *
             * 2) validateRpcOnly  is false when we are validating against datastore. 
             *    in this case,we don't need the name to be prefixed. This is becasue dyna bean is built without
             *    prefixes.
             *
             *    //FIXME: FNMS-10116 this has to be removed, once ModelNodeWithDynaBean has prefix with it.
             */
            if (validateRpcOnly && newPath.indexOf(OPEN_SQUARE) != -1) {
                String currentXpath = newPath.substring(newPath.indexOf(OPEN_SQUARE) + 1, newPath.indexOf
                        (CLOSE_SQUARE));
                String newCurrentXpath = appendPrefix(prefix, currentXpath, validateRpcOnly);
                newPath.replace(newPath.indexOf(OPEN_SQUARE) + 1, newPath.indexOf(CLOSE_SQUARE), newCurrentXpath);
            }

            if (newPath.charAt(newPath.length() - 1) == '/') {
                newPath.deleteCharAt(newPath.length() - 1);// delete the last /
            }
            excludeParentPath = newPath.toString();
        }
        return excludeParentPath;

    }

    public static String appendPrefix(Element currentElement, String excludeParentPath, boolean validateRpcOnly) {
        return appendPrefix(currentElement.getPrefix(), excludeParentPath, validateRpcOnly);
    }

    private Boolean validateOperation(Element rootElement, Element currentElement, Expression expression) {
        boolean isLogEnabled = LOGGER.isDebugEnabled();

        if (isLogEnabled) {
            LOGGER.debug("Validate operation for rootElement-{} currentElement-{} expression-{}", rootElement,
                    currentElement,
                    expression);
        }
        Boolean returnValue = null;
        if (expression instanceof CoreOperation) {
            CoreOperation operation = (CoreOperation) expression;
            if (operation instanceof CoreOperationRelationalExpression || operation instanceof CoreOperationCompare) {
                returnValue = (Boolean) validateXPath(rootElement, currentElement, operation.toString());
            } else if (operation instanceof CoreOperationAnd) {
                for (Expression arg : ((Operation) operation).getArguments()) {
                    if (returnValue == null) {
                        returnValue = validateOperation(rootElement, currentElement, arg);
                    } else {
                        returnValue = returnValue && validateOperation(rootElement, currentElement, arg);
                    }

                }
            } else if (operation instanceof CoreOperationOr) {
                for (Expression arg : operation.getArguments()) {
                    if (returnValue == null) {
                        returnValue = validateOperation(rootElement, currentElement, arg);
                    } else {
                        returnValue = returnValue || validateOperation(rootElement, currentElement, arg);
                    }

                }
            } else {
                if (isLogEnabled) {
                    LOGGER.debug("Validate operation for rootElement-{} currentElement-{} expression-{} is not " +
                            "coreOperation", rootElement, currentElement, expression);
                }
            }
        }
        if (isLogEnabled) {
            LOGGER.debug("Validate operation for rootElement-{} currentElement-{} expression-{} returnValue-{}",
                    rootElement,
                    currentElement, expression, returnValue);
        }
        return returnValue;
    }

    private void validateChoicecase(List<SchemaPath> childrenElementNodeSchemaPath,
                                    List<Element> elements, SchemaPath schemaPath,
                                    RequestType requestType,
                                    Element rootElement,
                                    SchemaRegistry schemaRegistry,
                                    NetconfRpcPayLoadType rpcType) throws ValidationException {
        Map<SchemaPath, Pair<SchemaPath, String>> choiceCaseExistMap = new HashMap<>();
        int index = 0;
        SchemaPath choiceCaseRootElementPath = ChoiceCaseNodeUtil.getChoiceCaseNodeSchemaPath(schemaRegistry,
                schemaPath);
        DataSchemaNode rootNode = schemaRegistry.getDataSchemaNode(schemaPath);
        /**
         * If the current node is a container and also a choice case, then don't validate it. This will be validate
         * during the parent node choice case validation. The individual leaf/list validation of this container would
         * have already been done at validateElement()
         */
        if (rootNode != null && rootNode instanceof ContainerSchemaNode && choiceCaseRootElementPath == null) {
            for (SchemaPath childSchemaPath : childrenElementNodeSchemaPath) {
                SchemaPath choiceCaseSchemaPath = ChoiceCaseNodeUtil.getChoiceCaseNodeSchemaPath(getSchemaRegistry(),
                        childSchemaPath);
                if (choiceCaseSchemaPath != null) {
                    Element element = elements.get(index);
                    validateConstraint(element, choiceCaseSchemaPath, requestType, rootElement, schemaRegistry,
                            rpcType);
                    SchemaPath choiceSchemaPath = choiceCaseSchemaPath.getParent();
                    validateConstraint(element, choiceSchemaPath, requestType, rootElement, schemaRegistry, rpcType);

                    //two case of same choice can't exists, so no other case should exists in choiceCaseExistMap
                    Pair<SchemaPath, String> existingPair = choiceCaseExistMap.get(choiceSchemaPath);
                    if (existingPair != null) {
                        String existingOperation = choiceCaseExistMap.get(choiceSchemaPath).getSecond();
                        String currentOperation = OperationAttributeUtil.getOperationAttribute(element);
                        if (!OperationAttributeUtil.isAllowedOperation(existingOperation, currentOperation) &&
                                !existingPair.getFirst().equals(choiceCaseSchemaPath)) {
                            NetconfRpcError error = NetconfRpcError.getBadElementError(childSchemaPath
                                    .getLastComponent().getLocalName(), NetconfRpcErrorType.Application);

                            throw new ValidationException(error);
                        }
                    }
                    choiceCaseExistMap.put(choiceSchemaPath, new Pair<SchemaPath, String>(choiceCaseSchemaPath,
                            OperationAttributeUtil
                            .getOperationAttribute(element)));
                }
                index++;
            }
        }

    }

    protected void typeValidation(Element element, SchemaPath schemaPath, RequestType requestType) throws ValidationException {
        DataSchemaNode schemaNode = getSchemaRegistry().getDataSchemaNode(schemaPath);
        boolean mustWhen = DataStoreValidationUtil.containsMustWhen(getSchemaRegistry(), schemaNode);

        if (!mustWhen) {
            // there is no must/when and will not be taken care in phase 3
            SchemaNodeConstraintParser schemaNodeConstraintParser = ConstraintValidatorFactoryImpl.getInstance()
                    .getConstraintNodeValidator(getSchemaRegistry().getDataSchemaNode(schemaPath), getSchemaRegistry(), getExpressionValidator());
            if (schemaNodeConstraintParser != null) {
                schemaNodeConstraintParser.validate(element, requestType);
            }
        }
    }

    protected void validateInsertAttributes(Element parentElement, Element childNode, SchemaPath schemaPath) throws ValidationException {
    }

    protected void validateOperation(Element parentElement, Element childNode, SchemaPath schemaPath) throws ValidationException {
    }

    private List<SchemaPath> getSchemaPathFromCases(Collection<DataSchemaNode> nodes) {
        List<SchemaPath> casePaths = new ArrayList<>();
        for (DataSchemaNode node : nodes) {
            if (node instanceof ChoiceSchemaNode) {
                for (ChoiceCaseNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
                    for (DataSchemaNode caseChild : caseNode.getChildNodes()) {
                        if (caseChild instanceof ChoiceSchemaNode) {
                            casePaths.addAll(getSchemaPathFromCases(Collections.singleton(caseChild)));
                        } else {
                            casePaths.add(caseChild.getPath());
                        }
                    }
                }
            }
        }
        return casePaths;
    }

    protected SchemaPath getSchemaPathForElement(Element element, Collection<SchemaPath> availableSchemaPaths) {
        for (SchemaPath schemaPath : availableSchemaPaths) {
            QName lastComponent = schemaPath.getLastComponent();
            if (lastComponent.getNamespace().toString().equals(element.getNamespaceURI()) && lastComponent.getLocalName().equals(element.getLocalName())) {
                return schemaPath;
            }
        }
        return null;
    }

    private Collection<SchemaPath> getSchemaPathsForNodes(Collection<DataSchemaNode> nodes) {
        List<SchemaPath> schemaPaths = new ArrayList<>();
        for (DataSchemaNode node : nodes) {
//            if (node instanceof ContainerSchemaNode) {
//                schemaPaths.addAll(getSchemaPathsForNodes(((ContainerSchemaNode)node).getChildNode()));
//            } else if (node instanceof ListSchemaNode) {
//                schemaPaths.addAll(getSchemaPathsForNodes(((ListSchemaNode)node).getChildNode()));
//            }
            schemaPaths.add(node.getPath());
        }
        return schemaPaths;
    }


    protected abstract SchemaRegistry getSchemaRegistry();

    protected Collection<DataSchemaNode> getChildren(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        return schemaRegistry.getChildren(schemaPath);
    }

    private String setFunctionOnContext(JXPathContext context, String xPath, Element currentElement) {
        context.setFunctions(new ClassFunctions(ElementWrapper.class, "leafRef"));
        context.getVariables().declareVariable("element", new ElementWrapper(currentElement));
        if (xPath.contains(DataStoreValidationUtil.CURRENT_PARENT)) {
            return xPath.replace(DataStoreValidationUtil.CURRENT_PARENT, "leafRef:current($element)");
        } else if (xPath.contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
            return xPath.replace(DataStoreValidationUtil.CURRENT_PATTERN, "leafRef:currentElementText($element)");
        }
        return xPath;
    }

}
