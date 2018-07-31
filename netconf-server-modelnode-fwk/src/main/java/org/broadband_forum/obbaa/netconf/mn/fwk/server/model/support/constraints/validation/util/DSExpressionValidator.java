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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .COLON;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .CURRENT_PARENT_SINGLE_KEY;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .CURRENT_PATTERN;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .CURRENT_SINGLE_KEY;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .NC_DS_VALIDATION;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .NUMERIC;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .excludeFirstStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.JXPathContext;
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
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ProxyValidationModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaClass;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.YangStep;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ChoiceEffectiveStatementImpl;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InstanceIdentifierConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;

import com.google.common.annotations.VisibleForTesting;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class DSExpressionValidator {

    protected SchemaRegistry m_schemaRegistry;
    protected ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    private final static AdvancedLogger LOGGER = LoggerFactory.getLogger(DSExpressionValidator.class, NC_DS_VALIDATION,
            "DEBUG", "GLOBAL");

    private Map<Class<?>, DSExpressionValidator> m_validators = new HashMap<Class<?>, DSExpressionValidator>();

    protected void logDebug(String message, Object... objects) {
        Boolean isDebugEnabled = (Boolean) RequestScope.getCurrentScope().getFromCache(DSExpressionValidator.class
                .getName());
        if (isDebugEnabled == null) {
            isDebugEnabled = LOGGER.isDebugEnabled();
            RequestScope.getCurrentScope().putInCache(DSExpressionValidator.class.getName(), isDebugEnabled);
        }
        if (isDebugEnabled) {
            LOGGER.debug(message, objects);
        }
    }

    protected boolean hasParentSteps(Expression expression) {
        if (expression instanceof CoreOperation) {
            return hasParentSteps((CoreOperation) expression);
        } else if (expression instanceof LocationPath) {
            return hasParentSteps((LocationPath) expression);
        } else {
            logDebug("{} expression is not of type LocationPath or CoreOperation. " + "Evaluating false for " +
                            "hasParentSteps()",
                    expression);
        }
        return false;
    }

    protected boolean hasParentSteps(CoreOperation operation) {
        // check only the first location path in the expression to see if it is a parent step
        for (Expression exp : operation.getArguments()) {
            if (exp instanceof LocationPath) {
                return hasParentSteps((LocationPath) exp);
            } else if (exp instanceof CoreOperation) {
                return hasParentSteps((CoreOperation) exp);
            } else {
                logDebug("{} is not a location Path or a core operation to check for parent steps", operation);
            }
        }
        return false;
    }

    protected boolean isAbsolutePath(Expression expression) {
        if (expression instanceof LocationPath && ((LocationPath) expression).isAbsolute()) {
            return true;
        } else if (expression instanceof CoreOperation) {
            CoreOperation operation = (CoreOperation) expression;
            if (operation.getArguments().length > 2) {
                return false;
            }

            if (isAbsolutePath(operation.getArguments()[0]) && operation.getArguments()[1] instanceof Constant) {
                return true;
            }

        }
        return false;
    }

    protected boolean hasParentSteps(LocationPath locationPath) {
        Step[] steps = locationPath.getSteps();
        if (steps.length > 0) {
            Step firstStep = steps[0];
            if (firstStep.getAxis() == Compiler.AXIS_PARENT) {
                return true;
            }
        }
        return false;
    }

    protected ModelNode getContextNode(ModelNode modelNode) {

        while (modelNode.getParent() != null) {
            modelNode = modelNode.getParent();
        }
        return modelNode;
    }

    protected boolean isSingleKeyCurrent(Step expression) {
        if (expression.toString().contains(CURRENT_PARENT_SINGLE_KEY) || expression.toString().contains
                (CURRENT_SINGLE_KEY)) {
            return true;
        }
        return false;
    }


    @VisibleForTesting
    Expression removePrefixes(Expression xPath) {
        Expression returnValue = xPath;
        List<String> prefixes = new ArrayList<String>();
        if (xPath instanceof LocationPath) {
            Step[] steps = ((LocationPath) xPath).getSteps();
            for (int i = 0; i < steps.length; i++) {
                Step step = steps[i];
                if (step.getNodeTest() instanceof NodeNameTest) {
                    NodeNameTest node = (NodeNameTest) step.getNodeTest();
                    String prefix = node.getNodeName().getPrefix();
                    if (prefix != null && !prefix.isEmpty()) {
                        prefixes.add(prefix);
                    }
                }
            }
            String xPathString = xPath.toString();
            for (String prefix : prefixes) {
                xPathString = xPathString.replace(prefix + DataStoreValidationUtil.COLON, "");
            }
            returnValue = JXPathUtils.getExpression(xPathString);
        }
        return returnValue;
    }

    protected Expression evaluateCurrent(ExtensionFunction expression, DynaBean contextBean, Object contextModelNode,
                                         String prefix, String leafName) {
        Expression[] childArguments = expression.getArguments();
        if (childArguments == null || childArguments.length == 0) {
            if (contextModelNode instanceof ModelNodeDynaBean && DataStoreValidationUtil.isReadable((DynaBean)
                    contextModelNode, leafName)) {
                Object value = ((DynaBean) contextModelNode).get(leafName);
                return new Constant(value.toString());
            } else if (contextModelNode instanceof DynaBean) {
                Object value = ModelNodeDynaClass.current((DynaBean) contextModelNode);
                return new Constant(value.toString());
            } else if (contextModelNode instanceof ModelNodeWithAttributes) {
                DynaBean bean = (DynaBean) ((ModelNode) contextModelNode).getValue();
                if (DataStoreValidationUtil.isReadable(bean, leafName)) {
                    Object value = bean.get(leafName).toString();
                    return new Constant(value.toString());
                }
            }
        } else {
            Expression[] newExpressions = new Expression[childArguments.length];
            boolean changed = false;
            for (int i = 0; i < newExpressions.length; i++) {
                newExpressions[i] = checkForCurrentFunction(contextBean, contextModelNode, null, childArguments[i],
                        prefix);
                if (!newExpressions[i].equals(childArguments[i])) {
                    changed = true;
                }
            }

            if (changed) {
                expression = new ExtensionFunction(expression.getFunctionName(), newExpressions);
            }
        }
        return expression;
    }

    protected Expression evaluateCurrent(ExpressionPath expression, Object contextModelNode, String prefix, QName
            leafQName) {

        Expression childExpression = expression.getExpression();
        if (childExpression instanceof ExtensionFunction) {
            Step[] steps = expression.getSteps();
            DynaBean contextBean = null;
            if (contextModelNode instanceof ModelNodeWithAttributes) {
                contextBean = (DynaBean) ((ModelNode) contextModelNode).getValue();
                // First step of the path has to be removed. 
                // because the ModelNode indicates we are already at current()/..

                Step[] newSteps = new Step[steps.length - 1];
                System.arraycopy(steps, 1, newSteps, 0, steps.length - 1);
                steps = newSteps;
            } else if (contextModelNode instanceof ModelNodeDynaBean) {
                contextBean = (DynaBean) contextModelNode;
                Step[] newSteps = new Step[steps.length - 1];
                System.arraycopy(steps, 1, newSteps, 0, steps.length - 1);
                steps = newSteps;
            } else if (contextModelNode instanceof DynaBean) {
                contextBean = (DynaBean) contextModelNode;
            }
            LocationPath path = new LocationPath(false, steps);
            Object value = checkForFunctionsAndEvaluate(path, contextBean, contextModelNode, null, leafQName);
            return new Constant(value.toString());
        }
        return expression;
    }

    /**
     * This is to take care of xpath which has the list[child] specified in the path. This
     * actually implies it is list[child = current()]
     *
     * @param expression
     * @return
     */
    protected Expression addCurrentToSingleKeyList(Expression expression) {
        if (DataStoreValidationUtil.isLocationPath(expression)) {
            LocationPath locationPath = (LocationPath) expression;
            Step[] steps = locationPath.getSteps();
            Step[] newSteps = new Step[steps.length];
            boolean changed = false;
            for (int index = 0; index < steps.length; index++) {
                if (steps[index].getPredicates().length == 1 && DataStoreValidationUtil.isLocationPath(steps[index]
                        .getPredicates()[0])) {
                    LocationPath childPath = (LocationPath) steps[index].getPredicates()[0];
                    if (childPath.getSteps().length == 1) {
                        Expression[] newExpressions = new Expression[1];
                        newExpressions[0] = JXPathUtils.getExpression(childPath.toString() + "=" + CURRENT_PATTERN);
                        Step oldStep = steps[index];
                        if (oldStep.getNodeTest() instanceof NodeNameTest) {
                            NodeNameTest node = (NodeNameTest) oldStep.getNodeTest();
                            YangStep newStep = new YangStep(node.getNodeName(), node.getNamespaceURI(), newExpressions);
                            newSteps[index] = newStep;
                            changed = true;
                        } else {
                            newSteps[index] = steps[index];
                        }
                    } else {
                        newSteps[index] = steps[index];
                    }
                } else {
                    newSteps[index] = steps[index];
                }
            }

            if (changed) {
                return new LocationPath(locationPath.isAbsolute(), newSteps);
            }
        } else if (DataStoreValidationUtil.isCoreOperation(expression)) {
            CoreOperation operation = (CoreOperation) expression;
            Expression[] expressions = operation.getArguments();
            Expression[] newExpressions = new Expression[expressions.length];
            boolean changed = false;
            for (int index = 0; index < expressions.length; index++) {
                newExpressions[index] = addCurrentToSingleKeyList(expressions[index]);
                if (!newExpressions[index].equals(expressions[index])) {
                    changed = true;
                }
            }
            if (changed) {
                return JXPathUtils.getCoreOperation(operation, newExpressions);
            }
        } else if (DataStoreValidationUtil.isFunction(expression)) {
            CoreFunction function = (CoreFunction) expression;
            Expression[] expressions = function.getArguments();
            Expression[] newExpressions = new Expression[expressions.length];
            boolean changed = false;
            for (int index = 0; index < expressions.length; index++) {
                newExpressions[index] = addCurrentToSingleKeyList(expressions[index]);
                if (!newExpressions[index].equals(expressions[index])) {
                    changed = true;
                }
            }
            if (changed) {
                return JXPathUtils.getCoreFunction(function.getFunctionCode(), newExpressions);
            }
        }

        return expression;
    }

    /**
     * This method is setting the extension function on DynaBean class.
     */
    protected Expression setFunctionOnContext(DynaBean contextBean, Object contextModelNode, Expression xPath, String
            prefix, QName leafQName) {

        if (xPath.toString().contains(CURRENT_PATTERN)) {
            Expression xpression = xPath;
            if (xpression instanceof LocationPath) {
                Step[] originalSteps = ((LocationPath) xpression).getSteps();
                Step[] newSteps = new Step[originalSteps.length];
                for (int i = 0; i < originalSteps.length; i++) {
                    boolean stepChanged = false;
                    Expression[] predicates = originalSteps[i].getPredicates();
                    Expression[] newPredicates = new Expression[predicates.length];
                    for (int j = 0; j < predicates.length; j++) {
                        if (predicates[j].toString().contains(CURRENT_PATTERN)) {
                            if (predicates[j] instanceof CoreOperation) {
                                Expression childExpression[] = ((Operation) predicates[j]).getArguments();
                                Expression newChildExpression[] = new Expression[childExpression.length];
                                for (int k = 0; k < childExpression.length; k++) {
                                    if (childExpression[k] instanceof ExtensionFunction) {
                                        String leafName = childExpression[k - 1].toString();
                                        newChildExpression[k] = evaluateCurrent((ExtensionFunction)
                                                        childExpression[k], contextBean,
                                                contextModelNode, prefix, leafName);
                                    } else if (childExpression[k] instanceof ExpressionPath) {
                                        newChildExpression[k] = evaluateCurrent((ExpressionPath) childExpression[k],
                                                contextModelNode,
                                                prefix, leafQName);
                                    } else {
                                        newChildExpression[k] = childExpression[k];
                                    }
                                }
                                newPredicates[j] = JXPathUtils.getCoreOperation((CoreOperation) predicates[j],
                                        newChildExpression);
                                stepChanged = true;
                            }
                        } else {
                            newPredicates[j] = predicates[j];
                        }
                    }

                    if (stepChanged) {
                        NodeTest nodeTest = originalSteps[i].getNodeTest();
                        if (nodeTest instanceof NodeNameTest) {
                            NodeNameTest nameNode = (NodeNameTest) nodeTest;
                            newSteps[i] = new YangStep(nameNode.getNodeName(), nameNode.getNamespaceURI(),
                                    newPredicates);
                        } else {
                            newSteps[i] = originalSteps[i];
                        }
                    } else {
                        newSteps[i] = originalSteps[i];
                    }
                }
                return new LocationPath(((LocationPath) xpression).isAbsolute(), newSteps);
            } else if (xpression instanceof ExpressionPath) {
                return evaluateCurrent((ExpressionPath) xpression, contextModelNode, prefix, leafQName);
            } else if (xpression instanceof ExtensionFunction) {
                return evaluateCurrent((ExtensionFunction) xpression, contextBean, contextModelNode, prefix,
                        leafQName.getLocalName());
            } else if (xpression instanceof CoreOperation) {
                Expression[] newExpressions = new Expression[((CoreOperation) xpression).getArguments().length];
                Expression[] expressions = ((CoreOperation) xpression).getArguments();
                int index = 0;
                for (Expression expression : expressions) {
                    newExpressions[index++] = setFunctionOnContext(contextBean, contextModelNode, expression, prefix,
                            leafQName);
                }
                return JXPathUtils.getCoreOperation((CoreOperation) xpression, newExpressions);
            } else if (xpression instanceof CoreFunction) {
                Expression[] expressions = ((CoreFunction) xpression).getArguments();
                Expression[] newExpressions = new Expression[expressions.length];
                int index = 0;
                for (Expression expression : expressions) {
                    newExpressions[index++] = setFunctionOnContext(contextBean, contextModelNode, expression, prefix,
                            leafQName);
                }
                return JXPathUtils.getCoreFunction(((CoreFunction) xpression).getFunctionCode(), newExpressions);
            }
        }
        return xPath;
    }

    protected boolean isStepSameAsModelNode(ModelNode targetNode, Step step) {
        /**
         * Given a JXPath Path Step and a ModelNode, this method determines if the Step represents the same ModelNode.
         *
         * Verify if the step Name and the ModelNode QName.getLocalName() are same, then
         *
         * 1) If the Namespace is available, we need to compare the same --> return true if same 
         * 2) If only NS prefix is available we need to ensure they match --> return true if same 
         * 3) If no NS or its prefix, we return the targetNode
         *
         */
        boolean returnValue = false;
        NodeTest nodeTest = step.getNodeTest();
        if (nodeTest instanceof NodeNameTest) {
            NodeNameTest node = (NodeNameTest) nodeTest;
            String ns = node.getNamespaceURI();
            String name = node.getNodeName().getName();
            if (targetNode.getQName().getLocalName().equals(name)) {
                // Both the local Names match.

                if (ns != null && ns.equals(targetNode.getQName().getNamespace().toString())) {
                    returnValue = true;
                } else {
                    String prefix = node.getNodeName().getPrefix();
                    if (prefix != null) {
                        String modelNodeNS = targetNode.getQName().getNamespace().toString();
                        String modelNodePrefix = m_schemaRegistry.getPrefix(modelNodeNS);
                        if (prefix.equals(modelNodePrefix)) {
                            returnValue = true;
                        }
                    } else {
                        // well we have only localName to match.
                        // no NS or prefix in xPath.
                        returnValue = true;
                    }

                }
            }
        }
        return returnValue;
    }

    protected ModelNode getRootNodeFor(Expression expression) {
        LocationPath locationPath = null;
        if (expression instanceof LocationPath) {
            /** the first step of an locationPath should match with 
             *   any of the Root Model node.
             *   Because all absolute xPath must have one of the root nodes
             *   as the first step
             */
            locationPath = (LocationPath) expression;
            Collection<ModelNode> rootNodes = DataStoreValidationUtil.getValidationContext().getRootNodes();
            if (rootNodes != null) {
                Step[] steps = locationPath.getSteps();
                for (ModelNode targetNode : rootNodes) {
                    if (isStepSameAsModelNode(targetNode, steps[0])) {
                        return targetNode;
                    }
                }
            }
        }
        return null;
    }


    protected Expression checkForSingleKeyCurrent(Expression expression, DynaBean dynaBean, Object currentNode) {
        String xPath = expression.toString();
        if (!xPath.contains(CURRENT_PATTERN)) {
            return expression;
        }
        if (expression instanceof LocationPath) {
            return checkForSingleKeyCurrent((LocationPath) expression, dynaBean);
        } else if (expression instanceof CoreFunction) {
            Expression[] childExpressions = new Expression[((CoreFunction) expression).getArgumentCount()];
            Expression[] expressions = ((CoreFunction) expression).getArguments();
            int index = 0;
            for (Expression childExpression : expressions) {
                childExpressions[index++] = checkForSingleKeyCurrent(childExpression, dynaBean, currentNode);
            }
            return JXPathUtils.getCoreFunction(((CoreFunction) expression).getFunctionCode(), childExpressions);
        } else if (expression instanceof CoreOperation) {
            Expression[] childExpressions = new Expression[((CoreOperation) expression).getArguments().length];
            Expression[] expressions = ((CoreOperation) expression).getArguments();
            int index = 0;
            for (Expression childExpression : expressions) {
                childExpressions[index++] = (checkForSingleKeyCurrent(childExpression, dynaBean, currentNode));
            }
            return JXPathUtils.getCoreOperation((CoreOperation) expression, childExpressions);
        } else if (expression instanceof ExtensionFunction) {
            return evaluateCurrent((ExtensionFunction) expression, dynaBean, currentNode, null, null);
        } else if (expression instanceof ExpressionPath) {
            return evaluateCurrent((ExpressionPath) expression, dynaBean, null, null);
        } else if (expression instanceof Constant) {
            Expression exp = extractConstant(expression);
            return checkForSingleKeyCurrent(exp, dynaBean, currentNode);
        }
        return expression;
    }

    /**
     * For a yang ListSchemaNode which has single key, it can be referenced in xPath as
     * /listSchemaNode[key = 'value'] or  /listSchemaNode[current()]
     * <p>
     * When [current()] is used, xPath basically indicates the single key value has to be matched with the node on
     * which current() is
     * applied. But JXPath, scans DynaBean in the current implementation and looks for name/value pairs.
     * <p>
     * It has no unique way to distinguish between list key fields and ordinary list attributes.
     * <p>
     * This method will covert the /listSchemaNode[current()] to /listSchemaNode[key = current()]
     */
    @SuppressWarnings("rawtypes")
    protected LocationPath checkForSingleKeyCurrent(LocationPath locationPath, DynaBean dynaBean) {
        LocationPath newPath = locationPath;
        Step[] oldSteps = locationPath.getSteps();
        DynaBean contextBean = dynaBean;
        Step[] newSteps = new Step[oldSteps.length];
        SchemaPath currentPath = null;
        int index = 0;
        if (locationPath.toString().contains(CURRENT_SINGLE_KEY) || locationPath.toString().contains
                (CURRENT_PARENT_SINGLE_KEY)) {
            if (locationPath.isAbsolute()) {
                ModelNode rootNode = getRootNodeFor(locationPath);
                contextBean = (DynaBean) rootNode.getValue();
                newSteps[0] = oldSteps[0];
                index = 1;
            }
            for (int i = index; i < oldSteps.length; i++) {
                Object value = null;
                if (contextBean != null && DataStoreValidationUtil.isReadable(contextBean, DataStoreValidationUtil
                        .getLocalName(oldSteps[i]))) {
                    value = contextBean.get(DataStoreValidationUtil.getLocalName(oldSteps[i]));
                } else if (contextBean != null && oldSteps[i].getAxis() == Compiler.AXIS_PARENT) {
                    value = contextBean.get(ModelNodeWithAttributes.PARENT);
                }

                if (value instanceof DynaBean) {
                    contextBean = (DynaBean) value;
                } else if (value instanceof Collection) {
                    Object object = ((Collection) value).iterator().next();
                    if (object instanceof DynaBean) {
                        contextBean = (DynaBean) object;
                    } else {
                        // last leaf/leaf list. no point going further
                        object = null;
                    }
                } else if (value != null && oldSteps.length - 1 == i) {
                    // we are at the last step
                    contextBean = null;
                    if (newSteps[0] != null) {
                        // most likely the last leaf/leaf-list in the xPath
                        newSteps[i] = oldSteps[i];
                    }
                    break;
                } else if (value != null && currentPath == null) {
                    // well we chose a wrong list node, which does not have further children down.
                    // but we can still traverse the schema.
                    SchemaPath previousPath = ((ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE)).getModelNodeSchemaPath();
                    String name = ModelNodeDynaBeanFactory.getModelNodeAttributeName(DataStoreValidationUtil
                            .getLocalName(oldSteps[i]));
                    currentPath = DataStoreValidationUtil.getChildPath(m_schemaRegistry, previousPath, name);
                    contextBean = null;
                } else if (currentPath != null) {
                    String name = ModelNodeDynaBeanFactory.getModelNodeAttributeName(DataStoreValidationUtil
                            .getLocalName(oldSteps[i]));
                    currentPath = DataStoreValidationUtil.getChildPath(m_schemaRegistry, currentPath, name);
                } else if (value == null && currentPath == null && contextBean != null && DataStoreValidationUtil
                        .isReadable(contextBean, ModelNodeWithAttributes.MODEL_NODE)) {
                    ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                    String stepName = ModelNodeDynaBeanFactory.getModelNodeAttributeName(DataStoreValidationUtil
                            .getLocalName(oldSteps[i]));
                    currentPath = DataStoreValidationUtil.getChildPath(m_schemaRegistry, modelNode
                            .getModelNodeSchemaPath(), stepName);
                    contextBean = null;
                    if (currentPath == null) {
                        newSteps = null;
                        break;
                    }
                } else {
                    logDebug(null, " we had a wrong contextNode to start with for path - {} and bean {}",
                            locationPath, dynaBean);
                    newSteps = null;
                    break;
                }

                if (isSingleKeyCurrent(oldSteps[i])) {
                    // Since [current()] is applicable only on List, the DynaBean will have a modelNode and it is
                    // expected to have only one key
                    DataSchemaNode schemaNode = null;
                    if (contextBean != null) {
                        ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
                        schemaNode = (ListSchemaNode) m_schemaRegistry.getDataSchemaNode(schemaPath);
                    } else if (currentPath != null) {
                        schemaNode = m_schemaRegistry.getDataSchemaNode(currentPath);
                    } else {
                        LOGGER.warn(" neither contextBean/CurrentPath can be null for {} and bean -", locationPath,
                                dynaBean);
                        newSteps = null;
                        break;
                    }

                    if (schemaNode == null) {
                        logDebug("we traversed a wrong path for {} and bean ", locationPath, dynaBean);
                        newSteps = null;
                        break;
                    }

                    QName keyName = null;
                    if (schemaNode instanceof ListSchemaNode) {
                        keyName = ((ListSchemaNode) schemaNode).getKeyDefinition().get(0);
                    }

                    // build key = current() as the new predicate
                    Expression[] newExpression = new Expression[1];
                    if (keyName != null) {
                        String dynaBeanAlignedName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(keyName
                                .getLocalName());
                        StringBuilder newPredicate = new StringBuilder().append(dynaBeanAlignedName).append("=")
                                .append(oldSteps[i].getPredicates()[0].toString());
                        newExpression[0] = JXPathUtils.getExpression(newPredicate.toString());
                    }

                    // build the new Step
                    if (oldSteps[i].getNodeTest() instanceof NodeNameTest) {
                        NodeNameTest node = (NodeNameTest) oldSteps[i].getNodeTest();
                        Step newStep = new YangStep(node.getNodeName(), node.getNamespaceURI(), newExpression);
                        newSteps[i] = newStep;
                    }
                } else {
                    newSteps[i] = oldSteps[i];
                }
            }
        }

        if (newSteps != null && newSteps[0] != null) {
            newPath = new LocationPath(locationPath.isAbsolute(), newSteps);
        }

        return newPath;
    }

    protected Set<Object> evaluateXpath(JXPathContext context, String xPath) {
        Set<Object> returnValue = null;
        Iterator<?> leafRefs = context.iterate(xPath);
        while (leafRefs.hasNext()) {
            if (returnValue == null) {
                returnValue = new HashSet<Object>();
            }
            returnValue.add(leafRefs.next());
        }
        return returnValue;
    }

    protected boolean isAnyObjectNull(Object[] objects) {
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAnyLocationPathFalse(CoreOperation operation) {
        for (Expression expression : operation.getArguments()) {
            if (expression.toString().equalsIgnoreCase("'false'")) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAnyLocationPathTrue(CoreOperation operation) {
        for (Expression expression : operation.getArguments()) {
            if (expression.toString().equalsIgnoreCase("'true'")) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAllArgumentsConstants(CoreOperation operation) {
        for (Expression expression : operation.getArguments()) {
            if (!(expression instanceof Constant)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isBoolean(String value) {
        if (value.equals(Boolean.TRUE.toString()) || value.equals(Boolean.FALSE.toString())) {
            return true;
        }
        return false;
    }

    protected Object getDefaults(DataSchemaNode node) {
        if (node instanceof LeafSchemaNode) {
            return ((LeafSchemaNode) node).getDefault();
        }
        return null;
    }

    protected boolean isAnyExpressionIdentity(List<Expression> expressions, QName leafQName) {
        if (leafQName != null) {
            /**
             * An identity is always returned in the form of prefix:value in the current stack. 
             * the below validates if any of the values in the evaluated expressions is of the form prefix:value. 
             *
             * Here the prefix is matched only with the leafQName ns prefix  
             * eg: Assume below is in namespace prefix=validation
             *
             *      container identity-validation {
             *
             *          leaf leaf1 {
             *              type identityref {
             *                  base some-identity;
             *              }
             *          }
             *
             *          leaf leaf2 {
             *              when "../leaf1 = 'identity1'";
             *              type int8;
             *          }
             *      }
             *
             *  Here ../leaf1 will return a value validation:some-identity
             *
             *  Now if this is the case we will return true. 
             *
             *  Case2: What if the identity is in some other module? In that case, the target element must also be
             *  prefixed with that ns.
             *  So when "../leaf1 = 'prefix:identity1'" is the right condition. In that case, the prefix is already
             *  here.
             */
            String nsPrefix = m_schemaRegistry.getPrefix(leafQName.getNamespace().toString());
            for (Expression exp : expressions) {
                String constant = exp.compute(null).toString();
                if (constant.contains(COLON) && constant.split(COLON)[0].equals(nsPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected Object computeExpression(CoreOperation operation, List<Expression> expressions) {
        Object value;
        CoreOperation newOperation = JXPathUtils.getCoreOperation(operation, expressions.toArray(new Expression[0]));
        if (newOperation instanceof CoreOperationAnd && isAllArgumentsConstants(newOperation) &&
                isAnyLocationPathFalse(newOperation)) {
            value = Boolean.FALSE;
        } else if (newOperation instanceof CoreOperationOr && isAllArgumentsConstants(newOperation)
                && isAnyLocationPathTrue(newOperation)) {
            value = Boolean.TRUE;
        } else if (newOperation instanceof CoreOperationAnd && isAllArgumentsConstants(newOperation)
                && !isAnyLocationPathFalse(newOperation)) {
            value = Boolean.TRUE;
        } else if (newOperation instanceof CoreOperationOr && isAllArgumentsConstants(newOperation)
                && !isAnyLocationPathTrue(newOperation)) {
            value = Boolean.FALSE;
        } else {
            value = newOperation.computeValue(null);
        }

        return value;
    }


    protected Object validatePathAtContextBean(Expression xPathCondition, DynaBean contextBean,
                                               Object currentContextNode, String leafRefValue, QName leafQName) {
        Object value = null;
        // if we have the first step of path and context bean at same level, then we need to exclude the parent path
        // and evalute
        //
        // eg> xpath = a/b/c/d and context bean is at 'a'
        //    excluded parent path is b/c/d

        Expression excludeParentPath = null;
        Expression newExpression = xPathCondition;
        if (newExpression instanceof LocationPath) {
            excludeParentPath = excludeFirstStep(((LocationPath) newExpression));
        } else if (newExpression instanceof CoreOperation) {
            List<Expression> expressions = Arrays.asList(((CoreOperation) newExpression).getArguments());
            LocationPath newLocationPath = excludeFirstStep((LocationPath) expressions.get(0));
            Expression[] newExpressions = new Expression[expressions.size()];
            int index = 0;
            newExpressions[index++] = newLocationPath;
            for (int i = 1; i < expressions.size(); i++) {
                newExpressions[index++] = expressions.get(i);
            }
            excludeParentPath = JXPathUtils.getCoreOperation((CoreOperation) newExpression, newExpressions);

        } else {
            /**
             * If we are here, indicates a clause not supported or a serious programming error. 
             * God forbid, if we see this exception
             */
            throw new RuntimeException("Not a valid Expression type for validation a locationPath:" + newExpression);
        }

        if (!excludeParentPath.toString().isEmpty()) {
            /**
             * If after removing the first path, there is no longer a path to travel.... 
             * No point in proceeding further. 
             */
            value = evaluate(excludeParentPath, contextBean, currentContextNode, leafRefValue, leafQName);
        } else {
            logDebug(null, "{} is already at parent of context {}", xPathCondition, ModelNodeDynaClass
                    .getContextBeanName(contextBean));
        }
        return value;
    }

    protected Object validateAbsolutePath(Expression xPathCondition, DynaBean contextBean,
                                          Object currentContextNode, String leafRefValue, QName leafQName) {
        Object returnValue = null;
        // When we come here, we know we have a xpath and a constant or just an xpath.
        Expression ex = xPathCondition;
        LocationPath xPath = null;
        Constant constant = null;
        if (ex instanceof LocationPath) {
            xPath = (LocationPath) ex;
        } else {
            xPath = (LocationPath) ((CoreOperation) ex).getArguments()[0];
            constant = (Constant) ((CoreOperation) ex).getArguments()[1];
        }

        DynaBean rootBean = contextBean;

        while (DataStoreValidationUtil.isReadable(rootBean, ModelNodeWithAttributes.PARENT)) {
            rootBean = (DynaBean) rootBean.get(ModelNodeWithAttributes.PARENT);
        }
        xPath = excludeFirstStep(xPath);
        if (xPath.getSteps().length > 0) {
            Expression xPathForValidation = removePrefixes(xPath);
            JXPathContext context = JXPathContext.newContext(rootBean);
            context.setLenient(true);
            if (currentContextNode == null && xPath.toString().contains(CURRENT_PATTERN)) {
                // indicates the node is not created yet.
                returnValue = true;
            }

            Set<Object> lhs = evaluateXpath(context, xPathForValidation.toString());
            if (lhs != null) {
                if (constant == null) {
                    returnValue = lhs;
                } else {
                    List<Expression> expressions = new ArrayList<Expression>(2);
                    for (Object value : lhs) {
                        if (value instanceof ConfigLeafAttribute) {
                            value = ((ConfigLeafAttribute) value).getStringValue();
                        }
                        expressions.add(JXPathUtils.getConstantExpression(value));
                        expressions.add(constant);
                        returnValue = computeExpression((CoreOperation) ex, expressions);
                        if (returnValue != null && returnValue instanceof Boolean && ((Boolean) returnValue)) {
                            break;
                        }
                        expressions.clear();
                    }
                }

            }
        }
        return returnValue;
    }

    protected boolean isValidationDone(CoreOperation operation, DynaBean contextBean) {
        if (operation.getArguments()[0] instanceof LocationPath
                && ((LocationPath) (operation.getArguments()[0])).getSteps()[0].getAxis() != Compiler.AXIS_PARENT
                && DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
            // indicates the contextBean has a parent and the current xpath is absolute.
            // We still have some distance to cover
            return false;
        } else if (operation.getArguments()[0] instanceof LocationPath
                && DataStoreValidationUtil.isReadable(contextBean, operation.getArguments()[0].toString())) {
            /**
             * Indicates we do not want to validate any further. 
             * Why? Because the property is already in the contextBean and the evaluation has returned a false
             */
            return true;
        } else {
            logDebug("{} does not start with ../, first step is not in current context {}", operation,
                    ModelNodeDynaClass.getContextBeanName(contextBean));
        }
        return false;
    }


    protected boolean isContextBeanAtFirstStep(Expression ex, DynaBean contextBean, String leafRefValue) {
        /**
         * <device-manager>   <!-- Assume contextBean is here -->
         *  <device-holder>
         *      <device>
         *          <device-id>name</device-id>
         *      </device>
         *  </device-holder>
         * </device-manager>
         *
         * xpath: /device-manager/device-holder/device/name
         *
         *
         * We simply want to find out, if above is indeed the case - whether contextBean and start of Xpath are at
         * same level
         */
        boolean returnValue = false;
        if (ex instanceof LocationPath && ((LocationPath) ex).isAbsolute() && !DataStoreValidationUtil.isReadable
                (contextBean, ModelNodeWithAttributes.PARENT)) {
            Step[] steps = ((LocationPath) ex).getSteps();
            Step firstStep = steps[0];
            if (firstStep.getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = ((NodeNameTest) firstStep.getNodeTest());
                String name = node.getNodeName().getName();
                String prefix = node.getNodeName().getPrefix();
                String beanNs = (String) contextBean.get(ModelNodeWithAttributes.NAMESPACE);
                if (prefix != null) {
                    String ns = m_schemaRegistry.getNamespaceURI(prefix);
                    if (ns.equals(beanNs) && name.equals(contextBean.getDynaClass().getName())) {
                        return true;
                    }
                } else if (name.equals(contextBean.getDynaClass().getName())) {
                    returnValue = true;
                }
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} is neither a locationPath or the contextBean has a parent when checking if context " +
                                "bean {} is at first step",
                        ex, ModelNodeDynaClass.getContextBeanName(contextBean));
            }
        }
        return returnValue;
    }


    /**
     * Given a xpath of a leaf inside a list/leaf-list, the below method traverses through all the
     * available such path.
     * Eg:
     * list a{
     * key b;
     * leaf b{
     * type string;
     * }
     * }
     * xpath /a/b --> This will fetch the list of all leaf b inside list of type a.
     * <p>
     * Such fetched leaf values are matched against the target value to find the right path
     */
    protected boolean validateLeafRef(JXPathContext context, String xPath, String leafRefValue) {

        Iterator<?> leafRefs = context.iterate(xPath);

        while (leafRefs.hasNext()) {
            Object value = leafRefs.next();
            if (value instanceof ConfigLeafAttribute) {
                value = ((ConfigLeafAttribute) value).getStringValue();
            }

            if (value.toString().equals(leafRefValue)) {
                return true;
            }

            if (value.toString().matches(NUMERIC) && leafRefValue.matches(NUMERIC)) {
                if (Double.parseDouble(leafRefValue) == Double.parseDouble(value.toString())) {
                    /**
                     * JXPath always retrieves all numeric values as a DOUBLE.toString(), like 10.0, 20.0. So when we
                     * have a yang validation
                     * expression like ../someLeaf = 10, this will be false. So converting the lhs/rhs as double, the
                     * retrieved value and
                     * the target value in DOUBLE for comparison
                     */
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A location path refers to the actual xpath that comes in the condition. For eg: In the following expression,
     * "../adh:hw-type = 'G.FAST'" ../adh:hw-type is the location path
     * <p>
     * A step is referred to each node in the location path. For eg: In the following expression, "../../adh:hw-type
     * = 'G.FAST'" The
     * location path is ../../adh:hw-type and its steps are "..", ".." and "adh:hw-type"
     * <p>
     * This method is responsible for analyzing the location path and choosing the right ModelNode/ModelNodeBean as
     * JXPath context by
     * traversing through the steps
     *
     * @param schemaRegistry
     * @param contextBean
     * @param locationPath
     * @param currentContextNode
     * @return
     */
    protected Object validateLocationPath(DynaBean contextBean, LocationPath inLocationPath,
                                          Object currentContextNode, String leafRefValue, QName leafQName) {
        LocationPath locationPath = DataStoreValidationUtil.getDynaBeanAlignedPath(inLocationPath);
        Object value = null;
        if (!locationPath.isAbsolute()) {

            if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)
                    && locationPath.getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {

                /**
                 * If leafQName is null, the condition could well indicate an xpression on a list or container
                 * referring to its parent
                 *
                 * container test {
                 *       leaf value1 {
                 *           type int8;
                 *       }
                 *
                 *       container c1 {
                 *            must "../value1 = 10";
                 *       }
                 *  }
                 *
                 *  Here the condition refers to a leaf in its parent bean, while the contextBean could be container C1.
                 */
                DynaBean nextBean = contextBean;
                int index = 0;
                do {
                    nextBean = (DynaBean) nextBean.get(ModelNodeWithAttributes.PARENT);
                    index++;
                }
                while (DataStoreValidationUtil.isReadable(nextBean, ModelNodeWithAttributes.PARENT) && locationPath
                        .getSteps()[index].getAxis() == Compiler.AXIS_PARENT);
                Step[] newPathSteps = new Step[locationPath.getSteps().length - index];
                System.arraycopy(locationPath.getSteps(), index, newPathSteps, 0, newPathSteps.length);
                LocationPath newLocationPath = new LocationPath(false, newPathSteps);
                if (isContextBeanAtFirstStep(newLocationPath, nextBean, leafRefValue) &&
                        !DataStoreValidationUtil.isReadable(nextBean, DataStoreValidationUtil.getLocalName
                                (newPathSteps[0]))) {
                    value = validatePathAtContextBean(newLocationPath, nextBean, currentContextNode, leafRefValue,
                            leafQName);
                } else {
                    value = evaluate(newLocationPath, nextBean, currentContextNode, leafRefValue, leafQName);
                }
            } else {
                Step[] steps = locationPath.getSteps();
                if (steps.length > 1 && steps[1].getAxis() == Compiler.AXIS_CHILD) {
                    DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry,
                            steps[1]);
                    DynaBean rootBean = DataStoreValidationUtil.getRootModelNode(m_schemaRegistry, rootSchemaNode);
                    if (rootBean != null && steps.length > 2) {
                        Step[] newSteps = new Step[steps.length - 2];// ../<nextRoot> is already resolved
                        System.arraycopy(steps, 2, newSteps, 0, newSteps.length);
                        LocationPath newLocationPath = new LocationPath(false, newSteps);
                        value = evaluate(newLocationPath, rootBean, currentContextNode, leafRefValue, leafQName);
                    } else if (rootBean != null) {
                        Step[] newSteps = new Step[steps.length - 1];// ../ is resolved
                        System.arraycopy(steps, 1, newSteps, 0, newSteps.length);
                        LocationPath newLocationPath = new LocationPath(false, newSteps);
                        value = evaluate(newLocationPath, rootBean, currentContextNode, leafRefValue, leafQName);
                    }
                } else {
                    logDebug("we are already at parent. no further traversel possible for {} and path {}",
                            contextBean, inLocationPath);
                }
            }
        } else if (!isfirstStepSameAsRootNode(locationPath, contextBean)) {
            value = processCrossTreeReference(locationPath, currentContextNode, leafRefValue, leafQName);
        } else if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
            value = evaluate(locationPath, (DynaBean) contextBean.get(ModelNodeWithAttributes.PARENT),
                    currentContextNode, leafRefValue, leafQName);
        } else if (isContextBeanAtFirstStep(locationPath, contextBean, leafRefValue)) {
            value = validatePathAtContextBean(locationPath, contextBean, currentContextNode, leafRefValue,
                    leafQName);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} is not an absolute location path, or a relative path and does not have a parent at " +
                                "context {}",
                        locationPath, ModelNodeDynaClass.getContextBeanName(contextBean));
            }
        }

        return value;
    }

    protected Object evaluateIdentity(CoreOperation operation, List<Expression> expressions,
                                      QName leafQName) {
        List<Expression> newExpression = new ArrayList<Expression>(expressions.size());
        String nsPrefix = m_schemaRegistry.getPrefix(leafQName.getNamespace().toString());
        for (Expression exp : expressions) {
            String constant = exp.compute(null).toString();

            if (!constant.contains(COLON)) {
                /**
                 * We are here, only because one of the xpression is an identity type. Now if the expression constant
                 * does not have a
                 * prefix, indicate it is local to the current LeafQName
                 */
                newExpression.add(JXPathUtils.getConstantExpression(nsPrefix + COLON + constant));
            } else {
                newExpression.add(exp);
            }
        }

        return computeExpression(operation, newExpression);
    }


    @SuppressWarnings("rawtypes")
    /**
     * Helps in computing the value of an expression.
     */
    protected Object getExpressionValue(DynaBean contextBean, Object currentContextNode, String leafRefValue, QName
            leafQName, CoreOperation operation) {
        List<Expression> expressions = new ArrayList<Expression>();
        List<HashSet> leafListValue = new ArrayList<HashSet>();
        boolean secondExpressionAdded = false;
        for (Expression argumentExpression : operation.getArguments()) {
            /*
             * for every sub-xpression in a xpression, get the value individually and do the computation finally. 
             * 
             * Eg: ../value + 10 < 10 
             *  here ../value+10 is a sub-expression
             */
            Object argumentValue = null;
            String argumentExpressionValue = argumentExpression.toString();
            if (isBoolean(argumentExpressionValue)) {
                // A simple true or false value
                argumentValue = argumentExpressionValue;
            } else {
                // Could be anything from constant to a complex locationPath
                argumentValue = evaluate(argumentExpression, contextBean, currentContextNode, leafRefValue,
                        leafQName);

                // If this is a SELF-VALUE '.' and if we have a leafQName indicates, this represents a leaf/leaflist.
                // In that case, we need to check if we have got the QName as a child in container/list -> received
                // dynabean
                if (argumentValue instanceof DynaBean && argumentExpression instanceof LocationPath
                        && ((LocationPath) argumentExpression).getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                    DynaBean selfValueContainer = (DynaBean) argumentValue;
                    if (leafQName != null) {
                        String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(leafQName.getLocalName());
                        if (DataStoreValidationUtil.isReadable(selfValueContainer, localName)) {
                            argumentValue = selfValueContainer.get(localName);
                        } else {
                            LOGGER.warn("dynaBean {} did not contain attribute {}", selfValueContainer.getDynaClass()
                                    .getName(), localName);
                        }
                    } // leafQName != null
                }
            }

            if (argumentValue instanceof HashSet) {
                /**
                 * If the returned value is a leafList, we need to iterate through the values to check if the
                 * condition matches. It
                 * indicates it is a leaf-list or a list
                 */
                leafListValue.add((HashSet) argumentValue);
                continue;
            } else if (argumentValue instanceof DynaBean) {
                /**
                 * Indicates this is most likely a current() value of a leaf
                 */
                DynaBean currentLeaf = (DynaBean) argumentValue;
                if (DataStoreValidationUtil.isReadable(currentLeaf, ModelNodeWithAttributes.LEAF_VALUE)) {
                    expressions.add(JXPathUtils.getConstantExpression(currentLeaf.get(ModelNodeWithAttributes
                            .LEAF_VALUE)));
                } else {
                    throw new RuntimeException("Not a valid leaf for current() " + currentLeaf.getDynaClass().getName
                            ());
                }

            } else if (argumentValue == null) {
                argumentValue = false;
                logDebug("Evaluating expression {} {}", operation.toString(), DataStoreValidationUtil.RESULTED_IN_NULL);
            }
            Expression ex = null;
            ex = JXPathUtils.getConstantExpression(argumentValue);
            expressions.add(ex);
            if (!leafListValue.isEmpty()) {
                secondExpressionAdded = true;
            }
        }

        return processOperationValue(leafQName, operation, expressions, leafListValue, secondExpressionAdded);
    }

    @SuppressWarnings("rawtypes")
    protected Object processOperationValue(QName leafQName, CoreOperation operation, List<Expression> expressions,
                                           List<HashSet> leafListValue, boolean secondExpressionAdded) {
        Object value = null;
        if (leafListValue.size() == 1) {
            HashSet leafList = leafListValue.get(0);
            for (Object object : leafList) {
                Expression ex = null;
                if (object instanceof ConfigLeafAttribute) {
                    ex = JXPathUtils.getConstantExpression(((ConfigLeafAttribute) object).getStringValue());
                } else {
                    ex = JXPathUtils.getConstantExpression(object.toString());
                }
                if (secondExpressionAdded) {
                    expressions.add(0, ex);
                } else {
                    expressions.add(ex);
                }
                value = computeExpression(operation, expressions);
                if (isBoolean(value.toString()) && value.toString().equalsIgnoreCase(Boolean.TRUE.toString())) {
                    break;
                }
            }
        } else if (leafListValue.size() == 2) {
            /**
             * Indicates boths sides of an operator has returned leaf-list
             */
            if (leafListValue.get(0).equals(leafListValue.get(1))) {
                value = true;
            } else {
                value = false;
            }

        } else if (isAnyExpressionIdentity(expressions, leafQName)) {
            value = evaluateIdentity(operation, expressions, leafQName);
        } else if (leafListValue.isEmpty()) {
            /**
             * Two straight forward values to compute
             */
            value = computeExpression(operation, expressions);
        } else {
            logDebug("we simply must have come here. We are not able to evaluate {}", operation);
        }

        return value;
    }

    protected boolean isfirstStepSameAsRootNode(Expression expression, DynaBean contextBean) {
        if (!DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
            // only if this is a root node, NO PARENT would be available --> more like a GOD :)
            // go ahead

            if (expression instanceof LocationPath) {
                // mostly it would have gone through all paths before hitting here. So we can expect only a location
                // path
                Step step = ((LocationPath) expression).getSteps()[0];
                if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.MODEL_NODE)) {
                    ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                    if (modelNode != null && isStepSameAsModelNode(modelNode, step)) {
                        return true;
                    } else {
                        String beanName = contextBean.getDynaClass().getName();
                        logDebug("LocationPath does not have the right tree {}, {}", expression, beanName);
                    }
                } else {
                    logDebug("Context Bean {} has no modelNode and no parent for xPath {}", contextBean, expression);
                }
            } else {
                logDebug("Not a location path {} and cannot be validated further", expression);
            }

        }
        return false;
    }

    protected Object checkForFunctionsAndEvaluate(Expression xPathCondition, DynaBean contextBean, Object
            currentContextNode,
                                                  String leafRefValue, QName leafQName) {
        Expression xPath = checkForCurrentFunction(contextBean, currentContextNode, leafQName, xPathCondition, null);
        return evaluate(xPath, contextBean, currentContextNode, leafRefValue, leafQName);

    }

    /**
     * This is the main evaluation method that evaluates the xpath (both leafref and non leafref) in the
     * contextModelNode
     * <p>
     * Responsible for initializing JXPathContext for the current contextModelNode DynaBean.
     * Note that, a call to getValue() method of modelNode would give DynaBean of the ModelNode.
     * <p>
     * If the xpath cannot be evaluated in the contextModelNode, this method compiles the xpath into JXPath Expression
     * This JXPath expression is further validated to find the right contextModelNode for evaluating the xpath
     * <p>
     * This method defined a custom extension function on the dynabean to handle the current()
     * This custom extension function uses the currentContextNode argument
     *
     * @param schemaRegistry
     * @param xPathCondition
     * @param contextBean
     * @param currentContextNode
     * @return
     */
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode,
                              String leafRefValue, QName leafQName) {
        logDebug("Evalute for xpath {} contextBean {} currentContextNode {} leafRefValue {} leafQName {}",
                xPathCondition, contextBean,
                currentContextNode, leafRefValue, leafQName);
        Object value = null;
        Expression ex = xPathCondition;
        Expression xPathForValidation = null;
        Expression xPath = xPathCondition;

        DSExpressionValidator childValidator = m_validators.get(xPathCondition.getClass());
        if (childValidator != null) {
            logDebug("Found a validator {} for xpath {}", childValidator, xPathCondition);
            return childValidator.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
        }

        if (DataStoreValidationUtil.isLocationPath(ex) && !hasParentSteps(ex)) {
            xPathForValidation = removePrefixes(xPath);
            JXPathContext context = JXPathContext.newContext(contextBean);
            context.setLenient(true);

            if (currentContextNode == null && xPath.toString().contains(CURRENT_PATTERN)) {
                // indicates the node is not created yet.
                value = true;
            } else if (leafRefValue != null) {
                value = validateLeafRef(context, xPathForValidation.toString(), leafRefValue);
            } else {
                value = context.getValue(xPathForValidation.toString());
            }
            logDebug("evaluate value is {} for xpath {} contextBean {}", value, xPathForValidation, contextBean);

            if ((value == null || (value instanceof Boolean && !((Boolean) value))) && leafRefValue == null &&
                    isAbsolutePath(ex)) {
                value = validateAbsolutePath(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
            }
        }

        if (value == null || (value instanceof Boolean && !((Boolean) value))) {
            if (DataStoreValidationUtil.isCoreOperation(ex)) {
                // xpath is an expression a/b/c = 'a'
                value = getExpressionValue(contextBean, currentContextNode, leafRefValue, leafQName, (CoreOperation)
                        ex);
            } else if (DataStoreValidationUtil.isLocationPath(ex)) {
                if (xPathForValidation != null && ((LocationPath) ex).getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
                    // xpath is already processed and has a parent step
                    String stepName = DataStoreValidationUtil.getLocalName(((LocationPath) ex).getSteps()[0]);
                    if (xPathForValidation != null && stepName != null) {
                        stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(stepName);
                        String contextName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(contextBean
                                .getDynaClass().getName());
                        if (!(DataStoreValidationUtil.isReadable(contextBean, stepName) && !contextName.equals
                                (stepName))) {
                            // if contextBean already has the first step of the location step and
                            // xPathForValidation is not null, indicating it is built
                            // the dynaBean name and stepName are not same (meaning container/attribute do not share
                            // the same name)
                            // then we are good to validate
                            value = validateLocationPath(contextBean, (LocationPath) ex, currentContextNode,
                                    leafRefValue, leafQName);
                        } else {
                            logDebug("Path already evaluated at bean {} for {}", contextBean, ex);
                        }
                    }
                } else if (isContextBeanAtFirstStep(ex, contextBean, leafRefValue)) {
                    value = validatePathAtContextBean(xPathCondition, contextBean, currentContextNode, leafRefValue,
                            leafQName);
                } else if (((LocationPath) ex).getSteps()[0].getAxis() != Compiler.AXIS_PARENT
                        && !isfirstStepSameAsRootNode(ex, contextBean)) {
                    /**
                     * If this is an absolute path starting with one of the root Nodes and the current ContextBean is
                     * on a different tree
                     * than the first step of this tree, well lets try and validate on a different Root Node that
                     * this xPath wants.
                     */
                    ModelNode modelNode = getRootNodeFor(ex);
                    if (modelNode != null) {
                        value = evaluate(xPathCondition, (DynaBean) modelNode.getValue(), currentContextNode,
                                leafRefValue, leafQName);
                    } else {
                        logDebug("No config tree identified for expression {}", ex);
                    }
                } else {
                    value = validateLocationPath(contextBean, (LocationPath) ex, currentContextNode, leafRefValue,
                            leafQName);
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{} is neither at first step, is not a core operation and does not have a parent for" +
                                    " contextBean or a locationPath {}",
                            ex, ModelNodeDynaClass.getContextBeanName(contextBean));
                }
            }
        }
        return value;
    }

    protected Object processCrossTreeReference(Expression xPathCondition, Object currentContextNode, String
            leafRefValue, QName leafQName) {
        Object value = null;
        /**
         * If this is an absolute path starting with one of the root Nodes and the current ContextBean is on a
         * different tree than the first
         * step of this tree, well lets try and validate on a different Root Node that this xPath wants.
         */
        ModelNode modelNode = getRootNodeFor(xPathCondition);
        if (modelNode != null) {
            value = evaluate(xPathCondition, (DynaBean) modelNode.getValue(), currentContextNode, leafRefValue,
                    leafQName);
        } else {
            logDebug("No config tree identified for expression {}", xPathCondition);
        }
        return value;
    }

    private Expression checkForCurrentFunction(DynaBean contextBean, Object currentContextNode, QName leafQName,
                                               Expression xPath, String prefix) {
        Expression xPathForValidation = addCurrentToSingleKeyList(xPath);
        if (xPathForValidation.toString().contains(CURRENT_PATTERN)) {
            xPathForValidation = checkForSingleKeyCurrent(xPathForValidation, contextBean, currentContextNode);
            xPathForValidation = setFunctionOnContext(contextBean, currentContextNode, xPathForValidation, prefix,
                    leafQName);
            return xPathForValidation;
        }
        return xPathForValidation;
    }

    protected DSExpressionValidator() {

    }

    protected void setValidators(Map<Class<?>, DSExpressionValidator> validators) {
        m_validators = validators;
    }

    public DSExpressionValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_validators.put(ExtensionFunction.class, new DSExtensionFunctionValidator(schemaRegistry,
                modelNodeHelperRegistry, m_validators));
        m_validators.put(Constant.class, new DSConstantValidator(schemaRegistry, modelNodeHelperRegistry,
                m_validators));
        m_validators.put(CoreOperationOr.class, new DSExpressionOrValidator(schemaRegistry, modelNodeHelperRegistry,
                m_validators));
        m_validators.put(CoreOperationAnd.class, new DSExpressionAndValidator(schemaRegistry,
                modelNodeHelperRegistry, m_validators));
        m_validators.put(CoreFunction.class, new DSFunctionValidator(schemaRegistry, modelNodeHelperRegistry,
                m_validators));
    }

    /**
     * This method takes care of validating xpath in modelnode other than leafref. Returns the result of the xpath
     * expression as boolean
     *
     * @param schemaRegistry
     * @param xPath
     * @param contextModelNode
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean validateXPathInModelNode(String path, ModelNode contextModelNode,
                                            DataSchemaNode schemaNode) {

        Expression xPath = m_schemaRegistry.getRelativePath(path, schemaNode);
        if (xPath == null) {
            xPath = JXPathUtils.getExpression(path);
        }
        xPath = DataStoreValidationUtil.getDynaBeanAlignedPath(xPath);
        ModelNodeDynaBean contextBean = (ModelNodeDynaBean) contextModelNode.getValue();
        String childName = schemaNode == null ? null : ModelNodeDynaBeanFactory.getDynaBeanAttributeName(schemaNode
                .getQName().getLocalName());
        String childNs = schemaNode == null ? null : schemaNode.getQName().getNamespace().toString();
        DynaBean childBean = null;
        QName childQName = schemaNode == null ? null : schemaNode.getQName();
        Object value = null;
        if (DataStoreValidationUtil.isReadable(contextBean, childName) && contextBean.get(childName) != null &&
                !ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
            if (schemaNode instanceof ContainerSchemaNode) {
                Object childContainerBean = contextBean.get(childName);
                if (childContainerBean != null) {
                    value = checkForFunctionsAndEvaluate(xPath, (DynaBean) childContainerBean, childContainerBean,
                            null, childQName);
                }
            } else if (schemaNode instanceof ListSchemaNode) {
                List<Object> childListBeans = (List<Object>) contextBean.get(childName);
                if (childListBeans != null) {
                    for (Object childListBean : childListBeans) {
                        value = checkForFunctionsAndEvaluate(xPath, (DynaBean) childListBean, childListBean, null,
                                childQName);
                        if (value != null && !(value instanceof Boolean && !((Boolean) value))) {
                            break;
                        }
                    }
                }
            } else {
                /**
                 * 1) if the ContextBean already has the qname 
                 *    a) if it is a collection, indicates ModelNodeWithAttributes#m_leafList. This
                 *       xpath has to be iterated over all the values in the list to check if it is true 
                 *    b) if it is a object, taken the string value convert it to leaf bean and validate the xpath 
                 * 2) If the contextBean does not have the qname, then currentContextBean and leafQname are null
                 */
                Object beanObject = contextBean.get(childName);

                if (beanObject instanceof Collection) {
                    Collection<Object> leafList = (Collection<Object>) beanObject;
                    for (Object leafObject : leafList) {
                        if (leafObject instanceof GenericConfigAttribute) {
                            childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean,
                                    childName,
                                    ((GenericConfigAttribute) leafObject).getStringValue(), childNs);
                        } else {
                            childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean,
                                    childName,
                                    leafObject.toString(), childNs);
                        }
                        value = checkForFunctionsAndEvaluate(xPath, childBean, childBean, null, childQName);
                        if (value != null && !(value instanceof Boolean && !((Boolean) value))) {
                            break;
                        }
                    }
                } else {
                    childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean, childName,
                            beanObject.toString(),
                            childNs);
                    value = checkForFunctionsAndEvaluate(xPath, childBean, childBean, null, childQName);
                }
            }
        } else if (schemaNode instanceof ChoiceEffectiveStatementImpl) {
            childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean, childName, null,
                    childNs);
            value = checkForFunctionsAndEvaluate(xPath, childBean, childBean, null, childQName);
        } else if (schemaNode instanceof ChoiceCaseNode) {
            // if this is a case, create a bean for parent choice also
            SchemaPath choicePath = ((ChoiceCaseNode) schemaNode).getPath().getParent();
            QName choiceQName = choicePath.getLastComponent();
            DynaBean choiceBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean,
                    choiceQName.getLocalName(),
                    null, choiceQName.getNamespace().toString());
            childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, choiceBean, childName, null,
                    childNs);
            value = checkForFunctionsAndEvaluate(xPath, childBean, childBean, null, childQName);
        } else {
            Collection<SchemaPath> schemaPathToCreate = DataStoreValidationUtil.getValidationContext()
                    .getSchemaPathsToCreate();
            if (schemaNode != null && schemaPathToCreate != null && schemaPathToCreate.contains(schemaNode.getPath())) {
                // indicates a when condition checking for node to create
                // it might have default value also.

                String defaultValue = null;
                if (schemaNode instanceof LeafSchemaNode) {
                    defaultValue = ((LeafSchemaNode) schemaNode).getDefault();
                }
                childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean, childName,
                        defaultValue, childNs);
                value = checkForFunctionsAndEvaluate(xPath, childBean, childBean, null, childQName);
            } else {
                value = checkForFunctionsAndEvaluate(xPath, contextBean, contextModelNode, null, childQName);
            }
        }

        if (value != null && !(value instanceof Collection && ((Collection) value).isEmpty())) {
            if (value instanceof Boolean) {
                return (boolean) value;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public void validateMust(MustDefinition definition, DataSchemaNode schemaNode, ModelNode parentModelNode) {
        logDebug("must validation for xpath {} modelNode {} child {} callFromLeaf {} ", definition, parentModelNode
                        .getModelNodeId(),
                schemaNode.getQName());
        boolean isValidConstraint = validateXPathInModelNode(definition.toString(), parentModelNode, schemaNode);
        if (!isValidConstraint) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("must validation failed for xPath {} modelNode{} child{}", definition,
                        parentModelNode.getModelNodeId(), schemaNode.getQName());
            }
            ValidationException violateMustException = DataStoreValidationErrors.getViolateMustContrainsException
                    (definition);
            ModelNodeId id = getErrorPath(schemaNode, parentModelNode, schemaNode.getQName(), (DynaBean)
                    parentModelNode.getValue());
            violateMustException.getRpcError().setErrorPath(id.xPathString(m_schemaRegistry), id
                    .xPathStringNsByPrefix(m_schemaRegistry));
            throw violateMustException;
        }
    }

    public boolean validateWhenConditionOnModule(ModelNode childModelNode, DataSchemaNode child) {
        boolean isValid = true;
        QNameModule qModule = child.getQName().getModule();
        if (qModule != null) {
            Module module = m_schemaRegistry.findModuleByNamespaceAndRevision(qModule.getNamespace(), qModule
                    .getRevision());
            if (module != null) {
                for (AugmentationSchema augmentationSchema : module.getAugmentations()) {
                    RevisionAwareXPath whenCondition = augmentationSchema.getWhenCondition();
                    if (whenCondition != null && augmentationSchema.getChildNodes().contains(child)) {
                        isValid = validateXPathInModelNode(whenCondition.toString(), childModelNode, child);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("{} is {} on {} for Model Node {}", whenCondition.toString(), isValid,
                                    child.getQName().getLocalName(), childModelNode);
                        }
                        return isValid;
                    }
                }
            }
        }
        return isValid;
    }

    public boolean validateWhen(Expression expression, DataSchemaNode schemaNode, ModelNode parentNode) {
        boolean returnValue = true;
        if (SchemaRegistryUtil.hasDefaults(schemaNode) && ((ModelNodeWithAttributes) parentNode).getAttribute
                (schemaNode.getQName()) == null) {
            // if it is a leaf with default and that is not found in modelNode, then we need to create it if "when"
            // validates to true
            Collection<SchemaPath> schemaPathToCreate = DataStoreValidationUtil.getValidationContext()
                    .getSchemaPathsToCreate();
            schemaPathToCreate.add(schemaNode.getPath());
        }

        returnValue = validateXPathInModelNode(expression.toString(), parentNode, schemaNode);
        processWhenCondition(schemaNode, parentNode, returnValue, expression.toString());
        return returnValue;
    }

    @SuppressWarnings("rawtypes")
    protected void processWhenCondition(DataSchemaNode schemaNode, ModelNode modelNode, boolean isValidConstraint,
                                        String xPath) {
        Collection<SchemaPath> schemaPathsToDelete = DataStoreValidationUtil.getValidationContext()
                .getSchemaPathsToDelete();
        Collection<SchemaPath> schemaPathsToCreate = DataStoreValidationUtil.getValidationContext()
                .getSchemaPathsToCreate();
        QName childQName = schemaNode == null ? null : schemaNode.getQName();
        SchemaPath constraintNodePath = schemaNode == null ? null : schemaNode.getPath();
        if (!isValidConstraint) {
            if (modelNode instanceof ProxyValidationModelNode) {
                // condition fails on a non-existent node. It can be ignored.
                return;
            }
            logDebug("when validation is false for xpath{} modelNode {} qname {}", xPath, modelNode.getModelNodeId(),
                    childQName);
            if (schemaPathsToDelete != null && schemaPathsToDelete.contains(constraintNodePath)) {
                /**
                 * if the schemaPath is present in this cache, we know we have hit a when condition for the
                 * schemaPath whose node has to
                 * be deleted.
                 *
                 * Record this node as to be deleted.
                 */
                if (!(schemaPathsToCreate != null && schemaPathsToCreate.contains(constraintNodePath)) &&
                        !ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
                    DynaBean bean = (DynaBean) modelNode.getValue();
                    String stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
                    if (DataStoreValidationUtil.isReadable(bean, stepName)) {
                        Object childValue = bean.get(stepName);
                        if (childValue != null && !(childValue instanceof Collection && ((Collection) childValue)
                                .isEmpty())) {
                            /**
                             * When the node does exists, and if it evaluates to false we want to record that node
                             * for deletion, only if it is
                             * not identified for creation If it is identified for creation(schemaPathsToCreate) -->
                             * it means the node does not
                             * exists yet
                             */
                            Map<ModelNode, Collection<QName>> nodesToDelete = DataStoreValidationUtil
                                    .getValidationContext().getDeleteList();
                            Collection<QName> childQNames = nodesToDelete.get(modelNode);
                            if (childQNames == null) {
                                childQNames = new LinkedHashSet<QName>();
                                nodesToDelete.put(modelNode, childQNames);
                            }
                            childQNames.add(childQName);
                            logDebug("childQName {} must be deleted from modelNode {}", childQName, modelNode
                                    .getModelNodeId());
                        }
                    }
                } else {
                    logDebug("Tried evaluating a non-existent node and it evaluated to false");
                }
            } else if (!(schemaPathsToCreate != null && schemaPathsToCreate.contains(constraintNodePath))) {
                DynaBean bean = (DynaBean) modelNode.getValue();
                if (childQName != null) {
                    String stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
                    if (DataStoreValidationUtil.isReadable(bean, stepName)) {
                        Object childValue = bean.get(stepName);
                        if (childValue != null && !(childValue instanceof Collection && ((Collection) childValue)
                                .isEmpty())) {
                            // We will get collections when we have a leafList. The list could be empty due
                            // to a delete operation in the current scope
                            throwWhenViolation(schemaNode, modelNode, xPath, childQName, bean);
                        }
                    } else if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
                        throwWhenViolation(schemaNode, modelNode, xPath, childQName, bean);
                    }
                } else {
                    DataSchemaNode augmentChildNode = DataStoreValidationUtil.getValidationContext()
                            .getAugmentChildNode();
                    SchemaPath augmentChildPath = augmentChildNode == null ? null : augmentChildNode.getPath();
                    if (!schemaPathsToCreate.contains(augmentChildPath)) {
                        throwWhenViolation(schemaNode, modelNode, xPath, childQName, bean);
                    }
                }
            } else {
                // if a constraint is false and if its exists, we will record it for deletion
                // if its not available yet, we will not create it
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("A node is recorded for creation and also exists already. ParentNode:{},childQName:{}",
                            modelNode.getModelNodeId(), childQName);
                }
            }
        } else {
            if (constraintNodePath == null) {
                DataSchemaNode augmentChildNode = DataStoreValidationUtil.getValidationContext().getAugmentChildNode();
                constraintNodePath = augmentChildNode == null ? null : augmentChildNode.getPath();
                schemaNode = augmentChildNode;
                childQName = augmentChildNode.getQName();

            }
            if (schemaPathsToCreate.contains(constraintNodePath)) {
                Map<SchemaPath, Object> defaultValues = DataStoreValidationUtil.getValidationContext()
                        .getDefaultValues();
                Map<ModelNode, Collection<QName>> nodesToCreate = DataStoreValidationUtil.getValidationContext()
                        .getCreateList();
                Collection<QName> childQNames = nodesToCreate.get(modelNode);
                if (childQNames == null) {
                    childQNames = new LinkedHashSet<QName>();
                    nodesToCreate.put(modelNode, childQNames);
                }
                childQNames.add(childQName);
                defaultValues.put(constraintNodePath, getDefaults(schemaNode));
            }
        }
    }

    protected void throwWhenViolation(DataSchemaNode schemaNode, ModelNode modelNode, String xPath, QName childQName,
                                      DynaBean bean) {
        ValidationException violateWhenException = DataStoreValidationErrors
                .getViolateWhenConditionExceptionThrownUnknownElement(xPath);
        ModelNodeId id = getErrorPath(schemaNode, modelNode, childQName, bean);
        violateWhenException.getRpcError().setErrorPath(id.xPathString(m_schemaRegistry), id.xPathStringNsByPrefix
                (m_schemaRegistry));
        throw violateWhenException;
    }

    @SuppressWarnings("unchecked")
    protected ModelNodeId getErrorPath(DataSchemaNode inSchemaNode, ModelNode modelNode, QName inChildQName, DynaBean
            bean) {
        DataSchemaNode schemaNode = inSchemaNode;
        QName childQName = inChildQName;
        if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
            schemaNode = DataStoreValidationUtil.getValidationContext().getChildOfChoiceCase();
            schemaNode = schemaNode == null ? inSchemaNode : schemaNode;
            childQName = schemaNode.getQName();
        }
        // inSchemaNode and inChildQName value are null if it is AugmentValidator. In this case, cached Augment child
        // node will be fetched from DSValidationContext.
        if (inSchemaNode == null && inChildQName == null) {
            schemaNode = DataStoreValidationUtil.getValidationContext().getAugmentChildNode();
            if (schemaNode != null) {
                childQName = schemaNode.getQName();
            }
        }
        ModelNodeId id = new ModelNodeId(modelNode.getModelNodeId());
        boolean leafValue = schemaNode instanceof LeafSchemaNode || schemaNode instanceof LeafListSchemaNode;
        boolean containerValue = schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ChoiceCaseNode;
        if (containerValue && schemaNode.getPath().getParent().getLastComponent() != null) {
            id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName
                    .getLocalName()));
        } else if (schemaNode instanceof ListSchemaNode) {
            Collection<DynaBean> beans = (Collection<DynaBean>) bean.get(childQName.getLocalName());
            ModelNode childModelNode = (ModelNode) beans.iterator().next().get(ModelNodeWithAttributes.MODEL_NODE);
            id = childModelNode.getModelNodeId();
        } else if (leafValue) {
            id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName
                    .getLocalName()));
        }
        return id;
    }

    /**
     * Instance identifier is a special case in handling xpath XPaths come in the text content of the XML Element
     * This method is responsible
     * for building the JXPath context and validating the instance-identifier on the right context node
     * <p>
     * CAUTION: org.apache.commons.jxpath.ri.model.dynabeans.DynaBeanPropertyPointer#getPropertyNames (Line 78)
     * imposes a restriction that,
     * yang container/list/leaf/leafref cannot contain the name "class". The above mentioned line tries to find the
     * property with name
     * 'class' and is skipping it. This is a limitation of DynaBean
     *
     * @param modelNode
     * @param instanceIdAttr
     * @param isRequired
     * @param elementName
     * @throws ValidationException
     */
    public void validateInstanceIdentifierElement(ModelNode modelNode, ConfigLeafAttribute instanceIdAttr, boolean
            isRequired,
                                                  String elementName, String namespace) throws ValidationException {
        ModelNode contextNode = modelNode;
        if (isRequired) {
            ModelNodeId id = buildModelNodeId(modelNode, elementName, namespace);

            if (instanceIdAttr == null) {
                throw DataStoreValidationErrors.getMissingDataException(String.format("Missing required element %s",
                        elementName),
                        id.xPathString(m_schemaRegistry), id.xPathStringNsByPrefix(m_schemaRegistry));
            } else {
                LocationPath xPath = (LocationPath) JXPathUtils.getExpression(instanceIdAttr.getStringValue());

                String ns = null;
                if (instanceIdAttr instanceof InstanceIdentifierConfigAttribute) {
                    ns = ((InstanceIdentifierConfigAttribute) instanceIdAttr).getAttributeNamespace();
                }
                QName leafQName = m_schemaRegistry.lookupQName(ns, instanceIdAttr.getDOMValue().getLocalName());
                contextNode = getContextNode(contextNode);
                xPath = DataStoreValidationUtil.getDynaBeanAlignedPath(xPath);

                Object value = checkForFunctionsAndEvaluate(xPath, (DynaBean) contextNode.getValue(), modelNode
                                .getValue(), null,
                        leafQName);

                boolean result = false;
                if (value != null && value instanceof Boolean) {
                    result = (boolean) value;
                } else if (value instanceof Object) {
                    result = true;
                }

                if (!result) {
                    throw DataStoreValidationErrors.getMissingDataException(
                            String.format("Missing required element %s", instanceIdAttr.getStringValue()),
                            id.xPathString(m_schemaRegistry), id.xPathStringNsByPrefix(m_schemaRegistry));
                }
            }
        }
    }

    private ModelNodeId buildModelNodeId(ModelNode modeNode, String childName, String namespace) {
        ModelNodeId id = new ModelNodeId(modeNode.getModelNodeId());
        id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, namespace, childName));
        return id;
    }

    /**
     * This method is responsible for validation of xpath involving a leafref node. It returns a boolean based on the
     * xpath's value
     * comparison with leafRefValue
     *
     * @param schemaRegistry
     * @param xPath
     * @param contextModelNode
     * @param leafRefValue
     * @param currentContextNode
     * @param schemaNode
     * @return
     */
    public boolean validateXPathInModelNode(String inputXPath, ModelNode contextModelNode,
                                            String leafRefValue, Object currentContextNode, DataSchemaNode schemaNode) {

        Object value = null;
        QName leafQName = schemaNode.getQName();
        Expression xPath = m_schemaRegistry.getRelativePath(inputXPath, schemaNode);
        if (xPath == null) {
            xPath = JXPathUtils.getExpression(inputXPath);
        }

        logDebug("Relative xpath is {} for {} ", xPath, inputXPath);

        Expression ex = DataStoreValidationUtil.getDynaBeanAlignedPath(xPath);
        if (ex instanceof LocationPath) {
            logDebug("{} is identified to be a locationPath", xPath);
            Step[] steps = ((LocationPath) ex).getSteps();
            Map<QName, ConfigLeafAttribute> attributes = ((ModelNodeWithAttributes) contextModelNode).getAttributes();
            for (QName attribute : attributes.keySet()) {
                if (schemaNode != null && attribute.equals(schemaNode.getQName())) {
                    leafQName = schemaNode.getQName();
                    DynaBean contextBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode,
                            (DynaBean) contextModelNode.getValue(), attribute.getLocalName(), attributes.get
                                    (attribute).getStringValue(),
                            attribute.getNamespace().toString());
                    List<Step> newSteps = new ArrayList<Step>();

                    for (int i = 0; i < steps.length; i++) {
                        if (steps[i].getAxis() != Compiler.AXIS_PARENT) {
                            newSteps.add(steps[i]);
                        } else {
                            if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
                                contextBean = (DynaBean) contextBean.get(ModelNodeWithAttributes.PARENT);
                            } else if (i + 1 < steps.length) {
                                // indicates we might have to look for a different root node/tree than where we have
                                // the constraint
                                DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNode
                                        (m_schemaRegistry, steps[++i]);
                                contextBean = DataStoreValidationUtil.getRootModelNode(m_schemaRegistry,
                                        rootSchemaNode);
                            }
                        }
                    }

                    LocationPath newPath = new LocationPath(((LocationPath) ex).isAbsolute(), newSteps.toArray(new
                            Step[0]));
                    logDebug("new path {} identified for xpath {} with contextBean {} currentNode {} leafRefValue {} " +
                                    "attribute {}", newPath,
                            xPath, contextBean, currentContextNode, leafRefValue, attribute);
                    newPath = DataStoreValidationUtil.getDynaBeanAlignedPath(newPath);
                    logDebug(
                            "dyna bean aligned path {} identified for xpath {} with contextBean {} currentNode {} " +
                                    "leafRefValue {} attribute {}",
                            newPath, xPath, contextBean, currentContextNode, leafRefValue, attribute);
                    value = checkForFunctionsAndEvaluate(newPath, contextBean, currentContextNode, leafRefValue,
                            attribute);
                    break;
                }
            }
        }
        if (value == null) {
            value = checkForFunctionsAndEvaluate(ex, (DynaBean) contextModelNode.getValue(), currentContextNode,
                    leafRefValue, leafQName);
        }
        if (value != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("validateXPathInModelNode returns {} for xPath {} modelNode {} child {}", value,
                        inputXPath,
                        contextModelNode.getModelNodeId(), schemaNode.getQName());
            }
            if (value instanceof Boolean) {
                return (boolean) value;
            } else if (value.equals(leafRefValue)) {
                return true;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("validateXPathInModelNode returns false for xPath {} modelNode {} child {}", inputXPath,
                    contextModelNode.getModelNodeId(), schemaNode.getQName());
        }
        return false;
    }

    public void registerValidators(Class<?> expression, DSExpressionValidator validator) {
        m_validators.put(expression, validator);
    }

    protected Expression extractConstant(Expression exp) {
        /**
         * constant value are always prefixed and suffixed with a single quote('). So contant.toString(A) will be
         * 'A', they have to
         * be removed to evaluate properly
         */
        String constantValue = exp.toString();
        if (constantValue.startsWith("'")) {
            constantValue = constantValue.substring(1);
            constantValue = constantValue.substring(0, constantValue.length() - 1);
            if (constantValue.contains("''")) {
                // we will be here when there are a mix of multiple functions. 
                // E.g. not(contains(../a/b/c, 'test')) will result in contains(../a/b/c, ''test'')
                // we need to resolve these so that we are able to fix the context bean by resolving the "../" and also 
                // changing the final expression as not(contains(b/c,'test')) to be evaluated in contextBean of 'a'
                constantValue = constantValue.replace("''", "'");
            }
            Expression newExp = JXPathUtils.getExpression(constantValue);
            return newExp;
        }
        return exp;
    }


}
