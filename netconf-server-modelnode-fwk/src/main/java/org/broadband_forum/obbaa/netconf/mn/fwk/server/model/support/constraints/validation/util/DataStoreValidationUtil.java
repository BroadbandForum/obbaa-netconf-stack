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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationAnd;
import org.apache.commons.jxpath.ri.compiler.CoreOperationOr;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ProxyValidationModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.YangStep;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Houses all constants and utility methods used for yang validation
 */
public class DataStoreValidationUtil {

    public static final String NC_DS_VALIDATION = "netconf-server-datastore-validation";
    public static final String CLOSING_SQUARE_BRACE = "]";
    public static final String SINGLE_STEP_COUNT = "SINGLE_STEP_COUNT";
    public static final String MISSING_MANDATORY_NODE = "Missing mandatory node";
    public static final String RESULTED_IN_NULL = " resulted in null";
    public static final String NUMERIC = "^[+-]?[0-9.]*$";
    public static final String SLASH = "/";
    public static final String PARENT_PATTERN = "..";
    public static final String CURRENT_PATTERN = "current()";
    public static final String CURRENT_SINGLE_KEY = "[current()]";
    public static final String CURRENT_PARENT_SINGLE_KEY = "[current()/..";
    public static final String CURRENT_PARENT = CURRENT_PATTERN + SLASH + PARENT_PATTERN;
    public static final String CURRENT_MULTI_PARENT = CURRENT_PATTERN + SLASH + PARENT_PATTERN + SLASH + PARENT_PATTERN;
    public static final String COLON = ":";
    public static final String NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT = "NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT";

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(DataStoreValidationUtil.class,
            NC_DS_VALIDATION,
            "DEBUG", "GLOBAL");

    private static String getComponentIdFromRequestScopeCache() {
        String componentId = (String) RequestScope.getCurrentScope().getFromCache("componentId");
        return componentId;
    }

    /**
     * Introduced this flag for UT purpose. ComponentID validation should be skipped for Deploy Plug UT (By default
     * this flag is enabled).
     */
    private static boolean COMPONENTID_VALIDATION = true;

    public static void skipComponentIDValidation() {
        COMPONENTID_VALIDATION = false;
    }

    public static void enbleComponentIDValidation() {
        COMPONENTID_VALIDATION = true;
    }

    public static boolean getComponentIDValidation() {
        return COMPONENTID_VALIDATION;
    }

    private static boolean verifyComponentId(DataSchemaNode childNode) {
        if (!COMPONENTID_VALIDATION) {
            return true;
        }
        String namespace = childNode.getQName().getNamespace().toString();
        String componentId = getComponentIdFromRequestScopeCache();
        if (componentId == null) {
            LOGGER.debug("Component-Id not found in Request Scope Cache ");
            return true;
        } else {
            if (namespace.contains(componentId)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns true if
     * 1) given ContainerSchemaNode is a non-presence container
     * 2a) has at least one leaf with default values or
     * 2b) any of its child non-presence container node has at least one leaf with default values
     */
    public static boolean containerHasDefaultLeafs(SchemaRegistry schemaRegistry, ContainerSchemaNode node) {
        if (node.isPresenceContainer()) {
            // this is a presence container. has to be created by a edit-config
            return false;
        }
        Collection<DataSchemaNode> children = schemaRegistry.getChildren(node.getPath());
        for (DataSchemaNode child : children) {
            if (SchemaRegistryUtil.hasDefaults(child)) {
                return true;
            } else if (child instanceof ContainerSchemaNode) {
                boolean mustWhen = DataStoreValidationUtil.containsMustWhen(schemaRegistry, child);
                if (!mustWhen && containerHasDefaultLeafs(schemaRegistry, (ContainerSchemaNode) child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean needsFurtherValidation(Element element, RequestType requestType) {
        String operationAttribute = element.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources
                .OPERATION);
        if (!requestType.isRpc() && (operationAttribute == null || operationAttribute.isEmpty()
                || (!operationAttribute.equals(EditConfigOperations.CREATE) && !operationAttribute.equals
                (EditConfigOperations.REPLACE)))) {
            return false;

        }

        return true;
    }

    /**
     * Given a DataSchemaNode, collects each data step name in the path towards root in a list. Returns the
     * schemaPath of the
     * input schemaNode
     */
    public static SchemaPath buildAbsAccessPath(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, List<Step>
            inputPath) {
        SchemaPath inPath = schemaNode.getPath();
        SchemaPath returnValue = inPath;
        do {
            DataSchemaNode node = schemaRegistry.getDataSchemaNode(inPath);
            if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(node)) {
                node = SchemaRegistryUtil.getEffectiveParentNode(node, schemaRegistry);
                inPath = node.getPath();
            }
            inputPath.add(getYangStep(inPath.getLastComponent()));
            inPath = inPath.getParent();
            returnValue = inPath.getParent() != null ? inPath : returnValue;
        } while (inPath.getLastComponent() != null);
        Collections.reverse(inputPath);
        return returnValue;
    }

    /**
     * given a LocationPath->Step, retrieves the localName
     *
     * @param step
     * @return
     */
    public static String getLocalName(Step step) {
        if (step.getNodeTest() instanceof NodeNameTest) {
            return ((NodeNameTest) step.getNodeTest()).getNodeName().getName();
        }
        return null;
    }

    /**
     * Given a parent schemaPath and a child local name, returns the first instance of child matching the local name
     */
    public static SchemaPath getChildPath(SchemaRegistry schemaRegistry, SchemaPath parentPath, String localName) {
        SchemaPath returnValue = null;
        Collection<DataSchemaNode> childNodes = schemaRegistry.getChildren(parentPath);
        for (DataSchemaNode childNode : childNodes) {
            if (childNode.getQName().getLocalName().equals(localName) && verifyComponentId(childNode)) {
                returnValue = childNode.getPath();
                break;
            }
        }
        return returnValue;
    }

    public static boolean isPostEditValidationSupported() {
        boolean returnValue = true;
        /* The property is for UT purpose only. Not to be documented */
        if (System.getProperties().containsKey(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT)) {
            returnValue = Boolean.parseBoolean(System.getProperty(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT));
        } else if (System.getenv().containsKey(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT)) {
            returnValue = Boolean.parseBoolean(System.getenv(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT));
        }
        // Returning true by default. Will be kept in observation
        // for a few days before removing.
        // PHASE 3 Yang validation is enabled
        LOGGER.debug("Post edit-config validation is {}", returnValue);
        return returnValue;
    }

    /**
     * Evaluates to true, if for a dynaBean has the given property name
     */
    @SuppressWarnings("unchecked")
    public static boolean isReadable(DynaBean dynaBean, String localName) {
        if (localName != null) {
            if (dynaBean instanceof ModelNodeDynaBean) {
                return ((ModelNodeDynaBean) dynaBean).isReadable(localName);
            } else if (dynaBean != null) {
                Set<String> properties = (Set<String>) dynaBean.get(ModelNodeDynaBeanFactory.ATTRIBUTE_LIST);
                return properties.contains(localName);
            }
        }

        return false;
    }


    /**
     * Modifies the Expression to work on a JxPath/DynaBean evaluation by replacing certain non-compliance step names
     * with
     * modified values in the given context
     */
    public static Expression getDynaBeanAlignedPath(Expression expression) {
        if (expression instanceof LocationPath) {
            return getDynaBeanAlignedPath(((LocationPath) expression));
        } else if (expression instanceof Operation) {
            Expression[] innerExpressions = ((Operation) expression).getArguments();
            Expression[] expressions = new Expression[innerExpressions != null ? innerExpressions.length : 0];
            int index = 0;
            if (((Operation) expression).getArguments() != null) {
                for (Expression exp : ((Operation) expression).getArguments()) {
                    expressions[index++] = getDynaBeanAlignedPath(exp);
                }
            }

            if (expression instanceof CoreOperation) {
                return JXPathUtils.getCoreOperation((CoreOperation) expression, expressions);
            } else if (expression instanceof CoreFunction) {
                return JXPathUtils.getCoreFunction(((CoreFunction) expression).getFunctionCode(), expressions);
            } else if (expression instanceof ExtensionFunction) {
                ExtensionFunction oldFunction = (ExtensionFunction) expression;
                return new ExtensionFunction(oldFunction.getFunctionName(), expressions);
            } else {
                LOGGER.warn("A new type of operation is identified - {} for {}", expression.getClass(), expression
                        .toString());
            }
        }

        return expression;
    }

    public static YangStep getYangStep(QName yangQName) {
        org.apache.commons.jxpath.ri.QName qname = new org.apache.commons.jxpath.ri.QName(null, yangQName
                .getLocalName());
        return new YangStep(qname, yangQName.getNamespace().toString());
    }

    public static LocationPath getDynaBeanAlignedPath(LocationPath locationPath) {

        LocationPath newPath = locationPath;
        boolean stepModified = false;
        if (newPath.toString().contains(ModelNodeDynaBeanFactory.ATTRIBUTE_CLASS)) {
            Step[] oldSteps = newPath.getSteps();
            Step[] newSteps = new Step[oldSteps.length];
            for (int i = 0; i < newSteps.length; i++) {
                Step step = oldSteps[i];
                if (step.toString().contains(ModelNodeDynaBeanFactory.ATTRIBUTE_CLASS) && step.getNodeTest()
                        instanceof NodeNameTest) {
                    boolean somethingChanged = false;
                    NodeNameTest node = (NodeNameTest) step.getNodeTest();
                    String prefix = node.getNodeName().getPrefix();
                    String localName = node.getNodeName().getName();
                    List<Expression> newExpression = new LinkedList<Expression>();
                    String newName = localName;
                    List<Expression> oldExpression = new LinkedList<Expression>(Arrays.asList(step.getPredicates()));
                    if (localName.contains(ModelNodeDynaBeanFactory.ATTRIBUTE_CLASS)) {
                        newName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(localName);
                        if (!localName.equals(newName)) {
                            somethingChanged = true;
                        }
                    }

                    for (Expression expression : oldExpression) {
                        if (expression.toString().contains(ModelNodeDynaBeanFactory.ATTRIBUTE_CLASS)) {
                            newExpression.add(getDynaBeanAlignedPath(expression));
                        } else {
                            newExpression.add(expression);
                        }
                    }

                    if (!oldExpression.containsAll(newExpression)) {
                        oldExpression = newExpression;
                        somethingChanged = true;
                    }
                    if (somethingChanged) {
                        org.apache.commons.jxpath.ri.QName qname = new org.apache.commons.jxpath.ri.QName(prefix,
                                newName);
                        Step newStep = new YangStep(qname, node.getNamespaceURI(),
                                oldExpression.toArray(new Expression[oldExpression.size()]));
                        newSteps[i] = newStep;
                        stepModified = true;
                    } else {
                        newSteps[i] = step;
                    }
                } else {
                    newSteps[i] = step;
                }
            }
            if (stepModified) {
                newPath = new LocationPath(locationPath.isAbsolute(), newSteps);
            }
        }

        return newPath;
    }

    public static LocationPath excludeFirstStep(LocationPath locationPath) {
        /**
         * Given a xpath the first step is removed and the resulted xpath is returned.
         * eg: input: ../a/b/c   output: a/b/c
         *     input: device-holder/device output: device
         */
        Step[] newSteps = new Step[locationPath.getSteps().length - 1];
        System.arraycopy(locationPath.getSteps(), 1, newSteps, 0, newSteps.length);
        return new LocationPath(true, newSteps);
    }

    public static SchemaPath getXPathSchemaPath(SchemaRegistry schemaRegistry, SchemaPath currentPath, LocationPath
            xPath) {
        Step[] steps = xPath.getSteps();
        SchemaPath nextPath = currentPath;
        if (steps[0].getAxis() == Compiler.AXIS_PARENT && nextPath.getParent() != null && nextPath.getParent()
                .getLastComponent() != null) {
            return getXPathSchemaPath(schemaRegistry, SchemaRegistryUtil.getDataParentSchemaPath(schemaRegistry,
                    nextPath), excludeFirstStep(xPath));
        } else {
            for (Step step : steps) {
                String localName = getLocalName(step);
                nextPath = getChildPath(schemaRegistry, nextPath, ModelNodeDynaBeanFactory.getModelNodeAttributeName
                        (localName));
                if (nextPath == null) {
                    break;
                }
            }
            if (nextPath == null) {
                QName rootName = currentPath.getPathFromRoot().iterator().next();
                if (rootName.getLocalName().equals(getLocalName(steps[0]))) {
                    nextPath = currentPath;
                    while (nextPath.getParent().getLastComponent() != null) {
                        nextPath = nextPath.getParent();
                    }
                    return getXPathSchemaPath(schemaRegistry, nextPath, excludeFirstStep(xPath));
                }
            } else {
                return nextPath;
            }
        }
        return null;
    }

    public static Collection<ModelNode> getChildListModelNodes(ModelNode parentNode, DataSchemaNode childSchemaNode,
                                                               ModelNodeHelperRegistry modelNodeHelperRegistry)
            throws ModelNodeGetException {
        return getChildListModelNodes(parentNode, childSchemaNode, modelNodeHelperRegistry, Collections.emptyMap());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ModelNode> getChildListModelNodes(ModelNode parentNode, DataSchemaNode childSchemaNode,
                                                               ModelNodeHelperRegistry modelNodeHelperRegistry,
                                                               Map<QName, ConfigLeafAttribute> matchCrieteria) throws
            ModelNodeGetException {
        Collection<ModelNode> listNodes = null;
        QName childQName = childSchemaNode.getQName();
        ModelNodeId parentModelNodeId = parentNode.getModelNodeId();
        String dynaAttributeName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());

        boolean proxyModelNode = parentNode instanceof ProxyValidationModelNode;

        if (ModelNodeDynaBeanFactory.containsBeanForModelNode(parentModelNodeId)) {
            // If the modelNodeId is in cache, dynaBean is already created
            Collection<DynaBean> beans = null;
            DynaBean dynaBean = (DynaBean) parentNode.getValue();
            beans = (Collection) (dynaBean == null ? null : dynaBean.get(dynaAttributeName));
            if (beans != null) {
                for (DynaBean bean : beans) {
                    if (bean instanceof ModelNodeDynaBean) {
                        ModelNodeWithAttributes listNode = (ModelNodeWithAttributes) bean.get(ModelNodeWithAttributes
                                .MODEL_NODE);
                        boolean matchFound = true;
                        for (Map.Entry<QName, ConfigLeafAttribute> key : matchCrieteria.entrySet()) {
                            if (!key.getValue().equals(listNode.getAttribute(key.getKey()))) {
                                matchFound = false;
                                break;
                            }
                        }
                        if (matchFound) {
                            if (listNodes == null) {
                                listNodes = new LinkedList<ModelNode>();
                            }
                            listNodes.add(listNode);
                            break;
                        }
                    }
                }
            } else {
                listNodes = Collections.emptyList();
            }
        } else if (!proxyModelNode) {
            // if dynaBean is not created and this is a not a proxy Node
            ChildListHelper helper = modelNodeHelperRegistry.getChildListHelper(parentNode.getModelNodeSchemaPath(),
                    childQName);
            if (helper != null) {
                listNodes = helper.getValue(parentNode, matchCrieteria);
            }
        } else {
            listNodes = Collections.emptyList();
        }

        if (listNodes == null) {
            listNodes = Collections.emptyList();
        }
        return listNodes;
    }

    public static ModelNode getChildContainerModelNode(ModelNode parentNode, DataSchemaNode childSchemaNode,
                                                       ModelNodeHelperRegistry modelNodeHelperRegistry) throws
            ModelNodeGetException {
        ModelNode childContainer = null;
        QName childQName = childSchemaNode.getQName();
        String dynaAttributeName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
        boolean proxyModelNode = parentNode instanceof ProxyValidationModelNode;
        ModelNodeId parentModelNodeId = parentNode.getModelNodeId();

        if (ModelNodeDynaBeanFactory.containsBeanForModelNode(parentModelNodeId)) {
            ModelNodeDynaBean dynaBean = (ModelNodeDynaBean) parentNode.getValue();
            if (DataStoreValidationUtil.isReadable(dynaBean, dynaAttributeName)) {
                Object object = dynaBean.get(dynaAttributeName);
                if (object instanceof DynaBean) {
                    // for a root node which can have an attribute as the same name as the node, we will have a non
                    // bean object.
                    DynaBean childBean = (DynaBean) dynaBean.get(dynaAttributeName);
                    childContainer = (ModelNode) (childBean == null ? null : childBean.get(ModelNodeWithAttributes
                            .MODEL_NODE));
                }
            }
        } else if (!proxyModelNode) {
            ChildContainerHelper helper = modelNodeHelperRegistry.getChildContainerHelper(parentNode
                    .getModelNodeSchemaPath(), childQName);
            if (helper != null) {
                childContainer = (ModelNodeWithAttributes) helper.getValue(parentNode);
            }
        }

        return childContainer;
    }

    public static Document getValidationDocument() {
        return getValidationContext().getDocument();
    }

    public static boolean isConstant(Expression expression) {
        return expression instanceof Constant;
    }

    public static boolean isLocationPath(Expression expression) {
        return expression instanceof LocationPath;
    }

    public static boolean isCoreOperationOr(Expression expression) {
        return expression instanceof CoreOperationOr;
    }

    public static boolean isCoreOperationAnd(Expression expression) {
        return expression instanceof CoreOperationAnd;
    }

    public static boolean isCoreOperation(Expression expression) {
        return expression instanceof CoreOperation;
    }

    public static boolean isFunction(Expression expression) {
        return expression instanceof CoreFunction;
    }

    public static boolean isExpressionPath(Expression expression) {
        return expression instanceof ExpressionPath;
    }

    public static boolean isExtensionFunction(Expression expression) {
        return expression instanceof ExtensionFunction;
    }

    public static boolean isOperation(Expression expression) {
        return expression instanceof Operation;
    }

    public static AugmentationSchema getAugmentationSchema(DataSchemaNode parentSchemaNode, DataSchemaNode child) {
        if (parentSchemaNode instanceof AugmentationTarget) {
            Set<AugmentationSchema> augs = ((AugmentationTarget) parentSchemaNode).getAvailableAugmentations();
            for (AugmentationSchema aug : augs) {
                if (aug.getChildNodes().contains(child)) {
                    return aug;
                }
            }
        }
        return null;
    }

    public static boolean containsMustWhen(SchemaRegistry schemaRegistry, DataSchemaNode node) {
        ConstraintDefinition constraint = node.getConstraints();
        boolean mustWhen = constraint.getWhenCondition() != null
                || (constraint.getMustConstraints() != null && !constraint.getMustConstraints().isEmpty());
        if (!mustWhen && node.isAugmenting()) {
            DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(node.getPath().getParent());
            AugmentationSchema augSchema = DataStoreValidationUtil.getAugmentationSchema(parentNode, node);
            RevisionAwareXPath xpath = augSchema == null ? null : augSchema.getWhenCondition();
            if (xpath != null) {
                mustWhen = true;
            }
        }

        return mustWhen;
    }

    public static DSValidationContext getValidationContext() {
        DSValidationContext context = (DSValidationContext) RequestScope.getCurrentScope().getFromCache
                (DSValidationContext.class.getName());
        if (context == null) {
            context = new DSValidationContext();
            RequestScope.getCurrentScope().putInCache(DSValidationContext.class.getName(), context);
        }
        return context;
    }

    public static void resetValidationContext() {
        ModelNodeDynaBeanFactory.resetCache();
        RequestScope.getCurrentScope().putInCache(DSValidationContext.class.getName(), null);
        SchemaRegistryUtil.resetCache();
    }

    public static DataSchemaNode getRootSchemaNode(SchemaRegistry schemaRegistry, Step step) {
        if (step != null && step.getNodeTest() instanceof NodeNameTest) {
            NodeNameTest nodeTest = (NodeNameTest) step.getNodeTest();
            String nodeName = nodeTest.getNodeName().getName();
            String prefix = nodeTest.getNodeName().getPrefix();
            Collection<DataSchemaNode> rootNodes = schemaRegistry.getRootDataSchemaNodes();
            for (DataSchemaNode rootNode : rootNodes) {
                QName qname = rootNode.getQName();
                String rootPrefix = schemaRegistry.getPrefix(qname.getNamespace().toString());
                if (qname.getLocalName().equals(nodeName)) {
                    if (prefix != null && prefix.equals(rootPrefix)) {
                        return rootNode;
                    } else if (prefix == null) {
                        return rootNode;
                    }
                }
            }
        }
        return null;
    }

    public static DynaBean getRootModelNode(SchemaRegistry schemaRegistry, DataSchemaNode rootSchemaNode) {
        DynaBean contextBean = null;
        Collection<ModelNode> rootModelNodes = DataStoreValidationUtil.getValidationContext().getRootNodes();
        for (ModelNode rootModelNode : rootModelNodes) {
            DataSchemaNode rootModelNodeSchemaNode = schemaRegistry.getDataSchemaNode(rootModelNode
                    .getModelNodeSchemaPath());
            if (rootModelNodeSchemaNode.equals(rootSchemaNode)) {
                contextBean = (DynaBean) rootModelNode.getValue();
                break;
            }
        }
        return contextBean;
    }


}
