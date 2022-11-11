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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.MODULE_NAME_LOCAL_NAME_SEPARATOR;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationEqual;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanContext.ModelNodeDynaBeanContextBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.ExpressionTranformationType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.YangStep;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Takes care of evaluating core functions
 *
 */
public class DSFunctionValidator extends DSExpressionValidator {
    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DSFunctionValidator.class, LogAppNames.NETCONF_STACK);

    public DSFunctionValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry registry,
            Map<Class<?>, DSExpressionValidator> validators) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        this.m_subSystemRegistry = registry;
        setValidators(validators);
    }

    @Override
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode, String leafRefValue,
                              QName leafQName, DSValidationContext validationContext) {
        if (DataStoreValidationUtil.isFunction(xPathCondition)) {
            Object returnValue =  getCoreFunction(contextBean, currentContextNode, null, leafQName, (CoreFunction) xPathCondition, validationContext);
            return validateLeafRef(returnValue, leafRefValue);
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName, validationContext);
        }
    }

    protected Object computeCount(DynaBean contextBean, Object currentContextNode, String leafRefValue, QName leafQName,
                                  CoreFunction expression, DSValidationContext validationContext) {

        try {
            Object returnValue = null;
            Expression[] expressions = expression.getArguments();
            Object[] objects = new Object[expressions.length];
            int index = 0;
            for (Expression exp : expressions) {
                if (exp instanceof CoreFunction) {
                    objects[index++] = getCoreFunction(contextBean, currentContextNode, leafRefValue, leafQName, (CoreFunction) exp, validationContext);
                } else if (exp instanceof LocationPath) {
                    LocationPath path = DataStoreValidationUtil.getDynaBeanAlignedPath((LocationPath) exp);
                    if (path.getSteps()[0].getAxis() != Compiler.AXIS_SELF) {
                        // this is specifically for expression like
                        // count(device)
                        // this is expensive, since all DynaBean has to explored
                        // in the entire tree and counted
                        int countIndex = 0;
                        DynaBean countContextBean = contextBean;
                        while (path.getSteps()[countIndex].getAxis() == Compiler.AXIS_PARENT) {
                            /*
                             * Increments the countIndex equal to the parent
                             * paths encountered in the expression Move up the
                             * hierarchy by fetching parent bean. eg.
                             * count(../../container1/child) countIndex = 2 ,
                             * and grand parent bean will be fetched.
                             */
                            countIndex++;
                            if (DataStoreValidationUtil.isReadable(countContextBean, ModelNodeWithAttributes.PARENT)) {
                                countContextBean = (DynaBean) countContextBean.get(ModelNodeWithAttributes.PARENT);
                            } else if (countIndex < path.getSteps().length && path.getSteps()[countIndex].getAxis() == Compiler.AXIS_CHILD) {
                                DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNodeWithModuleNameInPrefix(m_schemaRegistry, path.getSteps()[countIndex]);
                                countContextBean = DataStoreValidationUtil.getRootModelNode(m_schemaRegistry,rootSchemaNode, validationContext);
                                countIndex++;
                            }
                        }
                        if (path.isAbsolute()) {
                            ModelNode rootModelNodeOfExp = getRootModelNode(path, countContextBean, validationContext);
                            countContextBean = ModelNodeDynaBeanFactory.getDynaBean(rootModelNodeOfExp);
                            countIndex--;
                        }
                        Step step = path.getSteps()[path.getSteps().length - 1];
                        String stepName = DataStoreValidationUtil.getLocalName(step);
                        countOccurenceOfStepInTree(countContextBean, path.getSteps(), currentContextNode, leafRefValue, leafQName,
                                countIndex, validationContext);
                        Integer countValue = getCountFromCacheFor(stepName);
                        setCountToCacheFor(stepName, null);
                        RequestScope.getCurrentScope().putInCache(DataStoreValidationUtil.SINGLE_STEP_COUNT, null);
                        return countValue;
                    } else {
                        Object value = evaluate(exp, contextBean, currentContextNode, leafRefValue, leafQName, validationContext);
                        objects[index++] = value;
                    }
                } else {
                    objects[index++] = evaluate(exp, contextBean, currentContextNode, leafRefValue, leafQName, validationContext);
                }
            }

            CoreFunction function = JXPathUtils.getCoreFunction(expression.getFunctionCode(), objects);
            returnValue = computeFunction(contextBean, function, leafQName);
            return returnValue;
        } catch (RuntimeException e) {

            throw new ValidationException(getBadXPathError(contextBean, expression, e));
        }
    }

    @SuppressWarnings("deprecation")
    private NetconfRpcError getBadXPathError(DynaBean contextBean, CoreFunction expression, Exception exception) {
        NetconfRpcError rpcError = NetconfRpcError.getApplicationError("Failed to validate XPath { " + expression + " }");
        rpcError.setErrorAppTag(NetconfRpcErrorTag.INVALID_VALUE.value());
        rpcError.setErrorPath(((ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE)).getModelNodeId().pathString());
        try {
            Document document = DocumentUtils.getNewDocument();
            Element errorInfo = document.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_INFO);
            Element stackTraceElement = document.createElementNS(NetconfResources.NC_STACK_NS, NetconfResources.RPC_STACK_TRACE);
            stackTraceElement.setTextContent(ExceptionUtils.getStackTrace(exception));
            errorInfo.appendChild(stackTraceElement);
            rpcError.setErrorInfo(errorInfo);
        } catch (Exception ex) {
            LOGGER.error("Error while creating error-info element", ex);
        }
        return rpcError;
    }

    protected boolean containsPath(Expression expression) {
        if (DataStoreValidationUtil.isLocationPath(expression) || DataStoreValidationUtil.isExpressionPath(expression)) {
            return true;
        } else if (DataStoreValidationUtil.isOperation(expression)) {
            Expression[] expressions = ((Operation)expression).getArguments();
            for (Expression exp:expressions) {
                if (containsPath(exp)) {
                    return true;
                }
            }
        }
        return false;
    }


    protected Object getCoreFunction(DynaBean contextBean, Object currentContextNode, String leafRefValue, QName leafQName,
            CoreFunction expression, DSValidationContext validationContext) {
        Object returnValue = null;
        if (expression.getFunctionCode() == Compiler.FUNCTION_COUNT) {
            // if it is count() function
            returnValue = computeCount(contextBean, currentContextNode, leafRefValue, leafQName, expression, validationContext);
        } else {
            if (expression.getArgumentCount() == 1 && DataStoreValidationUtil.isLocationPath(expression.getArg1())) {
                // if it is a single argument function SUM()
                returnValue = computeSingleArgumentFunction(contextBean, expression, leafQName, validationContext);
            } else {
                // functions like contai+ns(locationPath, value)
                Object[] objects = new Object[expression.getArgumentCount()];
                int index = 0;
                for (Expression exp:expression.getArguments()) {
                    Expression newExp = exp;
                    Object value = null;
                    value = evaluate(newExp, contextBean, currentContextNode, leafRefValue, leafQName, validationContext);
                    if (value instanceof DynaBean) {
                        objects[index++] = ((DynaBean) value).get(ModelNodeWithAttributes.LEAF_VALUE);
                    } else if (value == null && isConstantTreatedAsLocationPath(exp, index)){
                        objects[index++] = exp.toString();
                    } else {
                        objects[index++] = value;
                    }
                }
                CoreFunction function = JXPathUtils.getCoreFunction(expression.getFunctionCode(), objects);
                returnValue = computeFunction(contextBean, function, leafQName);
            }

        }
        return returnValue;

    }

    protected Object computeSingleArgumentFunction(DynaBean contextBean, CoreFunction expression, QName leafQName,
                                                   DSValidationContext validationContext) {
        Object returnValue = null;
        LocationPath locationPath = ((LocationPath)expression.getArg1());
        DynaBean nextBean = contextBean;
        CoreFunction function = expression;
        if (locationPath.isAbsolute()) {
            // if it is an absolute path, we need to be at the root.
            ModelNode rootNode = getRootModelNode(locationPath, contextBean, validationContext);
            if (rootNode != null) {
                nextBean = (DynaBean) rootNode.getValue();

                LocationPath newLocationPath = DataStoreValidationUtil.excludeFirstStep(locationPath);
                LocationPath modifiedLocationPath = modifyConstantIdrefAttribute(newLocationPath, nextBean, validationContext);
                Object[] objects = new Object[1];
                objects[0] = modifiedLocationPath;
                function = JXPathUtils.getCoreFunction(expression.getFunctionCode(), objects);
            } else {
                LOGGER.warn(" there is no root modelNode for expression {}", expression);
            }
        } else if (locationPath.getSteps().length > 0 && locationPath.getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
            // if the path starts with a parent path, we need to discard them
            // and set the correct path
            Step[] steps = locationPath.getSteps();
            Step[] newSteps = null;
            int i=0;
            for (;i<steps.length && steps[i].getAxis() == Compiler.AXIS_PARENT;i++) {
                // as long we see a parent, we iterate
                if (DataStoreValidationUtil.isReadable(nextBean, ModelNodeWithAttributes.PARENT)) {
                    nextBean = (DynaBean) nextBean.get(ModelNodeWithAttributes.PARENT);
                } else {
                    // invalid path. return null;
                    LOGGER.warn("No path available for expression {} contextBean {}", expression, contextBean);
                    return returnValue;
                }
            }
            newSteps = new Step[steps.length-i];
            System.arraycopy(steps, i, newSteps, 0, steps.length-i);
            LocationPath newLocationPath = new LocationPath(false, newSteps);
            LocationPath modifiedLocationPath = modifyConstantIdrefAttribute(newLocationPath, nextBean, validationContext);
            Object[] objects = new Object[1];
            objects[0] = modifiedLocationPath;
            function = JXPathUtils.getCoreFunction(expression.getFunctionCode(), objects);
        }
        returnValue = computeFunction(nextBean, function, leafQName);
        return returnValue;
    }

    private LocationPath modifyConstantIdrefAttribute(LocationPath srcPath, DynaBean contextBean, DSValidationContext validationContext) {
        Step[] steps = srcPath.getSteps();
        ModelNode contextNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
        SchemaRegistry schemaRegistry = contextNode.getSchemaRegistry();
        SchemaPath schemaPath = contextNode.getModelNodeSchemaPath();
        Step[] newSteps = new Step[steps.length];
        DataSchemaNode schemaNode = null;
        for(int i=0; i<steps.length; i++) {
            Step currentStep = steps[i];
            if(currentStep.getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = (NodeNameTest) currentStep.getNodeTest();
                String localName = node.getNodeName().getName();
                schemaNode = getChildNode(schemaRegistry, schemaPath, ModelNodeDynaBeanFactory.getModelNodeAttributeName(localName));
                schemaPath = schemaNode == null? null: schemaNode.getPath();
            }
            Expression[] predicates = currentStep.getPredicates();
            Expression[] newPredicates = new Expression[predicates.length];
            if(predicates.length >0) {
                for(int predicateIndex =0; predicateIndex<predicates.length; predicateIndex++) {
                    newPredicates[predicateIndex] = handlePredicatesForIdRefConstant(predicates[predicateIndex], contextBean, schemaPath,
                            schemaRegistry, validationContext);
                }
            }
            newSteps[i] = new YangStep(currentStep, newPredicates);
        }
        return new LocationPath(srcPath.isAbsolute(), newSteps);
    }

    private Expression handlePredicatesForIdRefConstant(Expression expression, DynaBean contextBean, SchemaPath parentPath,
                                                        SchemaRegistry schemaRegistry, DSValidationContext validationContext) {
        if(expression instanceof CoreOperation) {
            Expression[] args = ((CoreOperation) expression).getArguments();
            Expression[] newArgs = new Expression[args.length];
            if(proceedToReplaceIdRefConstant(args)) {
                Expression lhs = args[0];
                Expression rhs = args[1];
                Step predicateKeyStep = ((LocationPath) lhs).getSteps()[0];
                if(predicateKeyStep.getNodeTest() instanceof NodeNameTest) {
                    NodeNameTest node = (NodeNameTest) predicateKeyStep.getNodeTest();
                    String predicateKey = node.getNodeName().getName();

                    DataSchemaNode schemaNode = getChildNode(schemaRegistry, parentPath, ModelNodeDynaBeanFactory.getModelNodeAttributeName(predicateKey));
                    if(schemaNode != null && schemaNode instanceof LeafSchemaNode && ((LeafSchemaNode) schemaNode).getType() instanceof IdentityrefTypeDefinition) {
                        String idRefConstantValue = rhs.compute(null).toString();
                        Module module = validationContext.getNodeConstraintAssociatedModule();
                        if(module == null) {
                            if(schemaNode != null) {
                                String ns= schemaNode.getQName().getNamespace().toString();
                                module = schemaRegistry.getModuleByNamespace(ns);
                            }
                        }
                        newArgs[0] = lhs;
                        newArgs[1] = getIdentityConstantWithProperPrefix(schemaRegistry, module, idRefConstantValue);
                        return JXPathUtils.getCoreOperation((CoreOperation) expression, newArgs);
                    }
                }
            } else {
                for(int i=0; i<args.length; i++) {
                    newArgs[i] = handlePredicatesForIdRefConstant(args[i], contextBean, parentPath, schemaRegistry, validationContext);
                }
                return JXPathUtils.getCoreOperation((CoreOperation) expression, newArgs);
            }
        }
        return expression;
    }

    private boolean proceedToReplaceIdRefConstant(Expression[] args) {
        if(args.length == 2) {
            if(DataStoreValidationUtil.isLocationPath(args[0]) && DataStoreValidationUtil.isConstant(args[1])){
                return true;
            }
        }
        return false;
    }

    private Expression transformLocationPathWithQNameInPrefixInsteadOfModuleName(Expression expresssionToTransform) {

        if(expresssionToTransform.toString().contains(":")) {
            Expression transformedExpression = DataStoreValidationUtil
                    .transformExpression(ExpressionTranformationType.FROM_MODULENAME_TO_MODULENAME_LOCALNAME_WITH_SEPARATOR, expresssionToTransform,
                            null, null, null, null);
            return transformedExpression;
        }

        return expresssionToTransform;
    }

    protected boolean hasOnlyEmptySteps(CoreFunction function) {
        Expression expression = function.getArg1();
        if (function.getArgumentCount() == 1 && expression instanceof LocationPath && ((LocationPath)expression).getSteps().length == 0) {
            return true;
        }
        return false;
    }

    protected Object computeFunction(DynaBean contextBean, CoreFunction function, QName leafQName) {

        int fCode = function.getFunctionCode();
        if (fCode == Compiler.FUNCTION_LOCAL_NAME) {
            if (isFunctionWithoutArgs(function)) {
                if (DataStoreValidationUtil.isReadable(contextBean, leafQName.getLocalName())) {
                    return leafQName.getLocalName();
                }
            } else if (hasOnlyEmptySteps(function)) {
                // special case: JXPath returns "root" for local-name of root object, so we have to compute the correct value ourselves
                // we'll delegate local-name for something in the subtree still to JXPath
                ModelNodeWithAttributes node = (ModelNodeWithAttributes) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                return node.getQName().getLocalName();
            }
        } else if (fCode == Compiler.FUNCTION_NAME && hasOnlyEmptySteps(function)) {
            // similar for the name function
            ModelNodeWithAttributes node = (ModelNodeWithAttributes) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
            QName qName = node.getQName();
            SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(node, m_schemaRegistry);
            Optional<Module> module = registry.findModuleByNamespaceAndRevision(qName.getNamespace(),
                    qName.getRevision().orElse(null));
            String prefix = module.get().getPrefix();
            return prefix + ":" + node.getQName().getLocalName();
        } else if (fCode == Compiler.FUNCTION_NAMESPACE_URI) {
            // similar for the namespace function
            if (isFunctionWithoutArgs(function)) {
                if (contextBean.get(ModelNodeWithAttributes.NAMESPACE).equals(leafQName.getNamespace().toString())) {
                    return leafQName.getNamespace().toString();
                }
            } else if (hasOnlyEmptySteps(function)) {
                ModelNodeWithAttributes node = (ModelNodeWithAttributes) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                QName qName = node.getQName();
                return qName.getNamespace().toString();
            }
        } else if (fCode == Compiler.FUNCTION_STRING || fCode == Compiler.FUNCTION_STRING_LENGTH
                || fCode == Compiler.FUNCTION_NORMALIZE_SPACE || fCode == Compiler.FUNCTION_NUMBER) {
            if (isFunctionWithoutArgs(function)) {
                if (DataStoreValidationUtil.isReadable(contextBean, leafQName.getLocalName())) {
                    /*
                     * Converting function() -> function(current())
                     * eg. , if current() = test
                     * then , string() -> string('test')
                     */
                    Expression newExpressions[] = new Expression[function.getArgumentCount()+1];
                    Constant newConstant = new Constant(contextBean.get(leafQName.getLocalName()).toString());
                    newExpressions[0] = newConstant;
                    CoreFunction currentNodeValueFunction = new CoreFunction(fCode, newExpressions);
                    return computeFunction(contextBean, currentNodeValueFunction, leafQName);
                }
            } else if (isFunctionSingleArgsWithDot(function) && fCode == Compiler.FUNCTION_STRING_LENGTH) {
                /**
                 * Here, converting '.' to corresponding leaf value
                 * string-length(.) --> string-length('test')
                 */
                Expression newExpressions[] = new Expression[function.getArgumentCount()];
                Constant newConstant = new Constant(contextBean.get(leafQName.getLocalName()).toString());
                newExpressions[0] = newConstant;
                CoreFunction currentNodeValueFunction = new CoreFunction(fCode, newExpressions);
                return computeFunction(contextBean, currentNodeValueFunction, leafQName);
            }
        }
        return computeFunctionValueFromContext(contextBean, function);
    }

    private Object computeFunctionValueFromContext(DynaBean contextBean, CoreFunction function) {
        try {
            Object returnValue = null;
            JXPathContext context = JXPathContext.newContext(contextBean);
            if (context instanceof JXPathContextReferenceImpl && context.getContextPointer() instanceof NodePointer) {
                ModelNodeDynaBeanContextBuilder contextBuilder = new ModelNodeDynaBeanContextBuilder();
                contextBuilder.setModuleNameAppendedWithLocalName(true);
                CoreFunction finalFunction = (CoreFunction) transformLocationPathWithQNameInPrefixInsteadOfModuleName(function);
                returnValue = ModelNodeDynaBean.withContext(contextBuilder.build() , () -> finalFunction
                        .computeValue(new RootContext((JXPathContextReferenceImpl) context, (NodePointer) context.getContextPointer())));
            }
            if(function.getFunctionCode() == Compiler.FUNCTION_LOCAL_NAME && returnValue instanceof String && ((String) returnValue).contains(ModelNodeWithAttributes.MODULE_NAME_LOCAL_NAME_SEPARATOR)) {
                String returnValueString = (String) returnValue;
                return returnValueString.substring(returnValueString.indexOf(MODULE_NAME_LOCAL_NAME_SEPARATOR) + MODULE_NAME_LOCAL_NAME_SEPARATOR.length());
            }
            return returnValue;
        } catch (JXPathInvalidSyntaxException e) {
            throw new RpcValidationException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Invalid JXPath syntax: " + e.getMessage()));
        }
    }

    private boolean isFunctionWithoutArgs(CoreFunction function) {
        if(function.getArgumentCount()==0){
            return true;
        }
        return false;
    }

    private boolean isFunctionSingleArgsWithDot(CoreFunction function) {
        if(function.getArgumentCount() == 1 && function.getArg1().toString().equals(DOT)){
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    protected void countOccurenceOfStepInTree(DynaBean contextBean, Step[] step, Object currentContextNode,
            String leafRefValue, QName leafQName, int index, DSValidationContext validationContext) {
        /*
         * Here the expression is broken down into steps and processed further.
         * eg. count(/container/list[enabled='true']/childNode
         *
         * steps : container, list[enabled] , childNode
         * For moving down the hierarchy "index" is incremented by 1.
         * For moving towards up thehierarchy(towards parent) , index is decremented by 1 , and parent
         * bean is passed as an argument
         *
         * Movement towards parent(up the hierarchy) is done until
         * "appAugmentedPath" is reached
         *
         * if xpath starts with different root schemanode, then we should go up until mount-point
         */

        if (index < 0) {
            String stepName = DataStoreValidationUtil.getLocalName(step[0]);
            if (contextBean.getDynaClass().getName()
                    .equals(ModelNodeDynaBeanFactory.getModelNodeAttributeName(stepName))) {
                index = 1;
                if (index > step.length - 1) {
                    if (step.length > 1) {
                        Integer countValue = getCountFromCacheFor(stepName);
                        setCountToCacheFor(stepName, countValue + 1);
                    }
                    checkCountOnParent(contextBean, step, currentContextNode, leafRefValue, leafQName, index, validationContext);
                    return;
                }
            } else {
                /*
                 * Check for presence in bean else check in parent bean
                 */
                if (DataStoreValidationUtil.isReadable(contextBean, stepName)) {
                    // context bean is parent bean of stepName, then index should be 0 and proceed further count evaluation
                    index = 0;
                    if (index == step.length - 1) {
                        Integer countValue = getCountFromCacheFor(stepName);
                        if(countValue == null){
                            countValue = 0;
                        }
                        setCountToCacheFor(stepName, countValue + 1);
                        return;
                    }
                } else {
                    checkCountOnParent(contextBean, step, currentContextNode, leafRefValue, leafQName, index, validationContext);
                    return;
                }
            }
        }
        if (index > step.length - 1) {
            return;
        }

        while (step[index].getAxis() == Compiler.AXIS_PARENT) {
            index++;
            if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
                contextBean = (DynaBean) contextBean.get(ModelNodeWithAttributes.PARENT);
            }
        }
        String stepName = DataStoreValidationUtil.getLocalName(step[index]);
        String moduleName = DataStoreValidationUtil.getPrefix(step[index]);
        Step singleStep = step[index];
        Step nextStep = null;
        if(step.length > index + 1) {
            nextStep = step[index + 1];
        }
        if (!isBeanTraversed(contextBean, stepName)) {
            Integer countValue = getCountFromCacheFor(stepName);
            if (index == step.length - 1) {
                if (countValue == null) {
                    countValue = 0;
                    setCountToCacheFor(stepName, countValue);
                }

                if (contextBean.getDynaClass().getName()
                        .equals(ModelNodeDynaBeanFactory.getModelNodeAttributeName(stepName))) {
                    countValue++;
                    setCountToCacheFor(stepName, countValue);
                }
            }

            if (DataStoreValidationUtil.isReadable(contextBean, stepName)) {

                ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                SchemaRegistry schemaRegistry = modelNode.getSchemaRegistry();
                DataSchemaNode parentDataSchemaNode = schemaRegistry.getDataSchemaNode(modelNode.getModelNodeSchemaPath());
                DataSchemaNode childSchemaNode = null;
                if(parentDataSchemaNode != null) {
                    childSchemaNode = DataStoreValidationUtil.getChildDataSchemaNodeModuleNameAware(schemaRegistry, parentDataSchemaNode.getPath(), stepName, moduleName);
                }

                Map<QName, ConfigLeafAttribute> matchCriteria;
                if(childSchemaNode != null && childSchemaNode instanceof ListSchemaNode) {
                    matchCriteria = getMatchCriteria(contextBean, stepName, moduleName, singleStep, nextStep);
                } else {
                    matchCriteria = new HashMap<>();
                }
                ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil.getModelNodeDynaBeanContext(stepName, DataStoreValidationUtil.getPrefix(step[index]), matchCriteria);
                DynaBean finalContextBean = contextBean;
                Object value = ModelNodeDynaBean.withContext(dynaBeanContext, () -> finalContextBean.get(stepName));
                boolean isLeafPredicateEvaluated = false;
                if (value != null && childSchemaNode != null && childSchemaNode instanceof LeafSchemaNode && singleStep.getPredicates().length > 0) {
                    if(singleStep.getPredicates().length ==1) {
                        Expression predicate = singleStep.getPredicates()[0];
                        if (predicate instanceof CoreOperationEqual) {
                            Expression[] arguements = ((CoreOperationEqual) predicate).getArguments();
                            Expression lhs = arguements[0];
                            Expression rhs = arguements[1];
                            if (rhs instanceof Constant && lhs instanceof LocationPath) {
                                LocationPath lhsLocationPath = (LocationPath) lhs;
                                if (!lhsLocationPath.isAbsolute() && lhsLocationPath.getSteps().length == 1) {
                                    if (lhsLocationPath.getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                                        Object rhsObject = rhs.compute(null);
                                        String rhsValueToCompare;
                                        if (rhsObject instanceof Number) {
                                            rhsValueToCompare = InfoSetUtil.stringValue(rhsObject);
                                        } else {
                                            rhsValueToCompare = rhsObject.toString();
                                        }
//                                        JXPathContext context = JXPathContext.newContext(contextBean);
//                                        context.setLenient(true);
//                                        Step[] newSteps = new Step[1];
//                                        newSteps[0] = singleStep;
//                                        LocationPath newPath = new LocationPath(false, newSteps);
//
//                                        ModelNodeDynaBeanContextBuilder contextBuilder = new ModelNodeDynaBeanContextBuilder();
//                                        contextBuilder.setModuleNameAppendedWithLocalName(true);
//                                        Object jxPathValue = ModelNodeDynaBean.withContext(contextBuilder.build() , () -> context.getValue(transformLocationPathWithQNameInPrefixInsteadOfModuleName(newPath).toString()));
                                        if (!value.equals(rhsValueToCompare)) {
                                            value = null;
                                        }
                                        isLeafPredicateEvaluated = true;
                                    }
                                }
                            }
                        }
                    }
                    if(!isLeafPredicateEvaluated) {
                        boolean matching = false;
                        for (Expression predicate : singleStep.getPredicates()) {
                            Object result = evaluate(predicate, contextBean, currentContextNode, leafRefValue, childSchemaNode.getQName()
                                    , validationContext);
                            if (result != null && result instanceof Boolean && (Boolean) result) {
                                matching = true;
                                break;
                            }
                        }
                        if (!matching) {
                            value = null;
                        }
                        isLeafPredicateEvaluated = true;
                    }
                }

                if (value != null) {
                    if (value instanceof Collection) {
                        for (Object object : ((Collection) value)) {
                            if (object instanceof DynaBean) {
                                if (step[index].getPredicates() == null || step[index].getPredicates().length < 1) {
                                    /*
                                     * For simple cases like count(list1) Here
                                     * the step does not requires further
                                     * evaluation due to lack of predicates.
                                     */
                                    countOccurenceOfStepInTree((DynaBean) object, step, currentContextNode,
                                            leafRefValue, leafQName, index + 1, validationContext);
                                    if (index == step.length - 1) {
                                        countValue++;
                                    }
                                    setCountToCacheFor(stepName, countValue);
                                } else {
                                    /*
                                     * For cases like list1[enabled='true'] Here
                                     * the step requires further evaluation as
                                     * it contains predicates.
                                     */
                                    boolean allPredicateYes = true;
                                    if(!isLeafPredicateEvaluated) {
                                        for (Expression predicate : step[index].getPredicates()) {
                                            Object result = evaluate(predicate, (DynaBean) object, currentContextNode,
                                                    leafRefValue, leafQName, validationContext);
                                            if (result == null || (result instanceof Boolean && !((Boolean) result))) {
                                                allPredicateYes = false;
                                                break;
                                            }
                                        }
                                    }
                                    if (allPredicateYes && index == step.length - 1) {
                                        countValue++;
                                        setCountToCacheFor(stepName, countValue);
                                    } else if (allPredicateYes) {
                                        countOccurenceOfStepInTree((DynaBean) object, step, currentContextNode,
                                                leafRefValue, leafQName, index + 1, validationContext);
                                    }
                                }
                            } else if (index == step.length - 1) {
                                countValue++;
                                setCountToCacheFor(stepName, countValue);
                            }
                        }
                    } else if (value instanceof DynaBean) {
                        boolean predicatesValid = true;
                        Expression[] predicates = step[index].getPredicates();
                        if (!isLeafPredicateEvaluated && predicates != null && predicates.length > 0) {
                            // if step has a condition. Like key of a list
                            // interface[name = 'interface']
                            for (Expression predicate:predicates) {
                                Object returnValue = evaluate(predicate, (DynaBean) value, currentContextNode, leafRefValue, leafQName,
                                        validationContext);
                                if (returnValue != null) {
                                    if (returnValue instanceof Boolean) {
                                        predicatesValid = predicatesValid && ((Boolean)returnValue);
                                    }
                                }

                                if (!predicatesValid) {
                                    break;
                                }
                            }
                        }

                        if (predicatesValid) {
                            if (index == step.length - 1) {
                                countValue++;
                                setCountToCacheFor(stepName, countValue);
                            }
                            countOccurenceOfStepInTree((DynaBean) value, step, currentContextNode, leafRefValue, leafQName,
                                    index + 1, validationContext);
                        }
                    } else {
                        if (index == step.length - 1) {
                            countValue++;
                            setCountToCacheFor(stepName, countValue);
                        }
                    }
                }
            }
            checkCountOnParent(contextBean, step, currentContextNode, leafRefValue, leafQName, index, validationContext);
        }
    }

    private Map<QName, ConfigLeafAttribute> getMatchCriteria(DynaBean contextBean, String stepName, String moduleName, Step singleStep, Step nextStep) {
        ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
        SchemaRegistry schemaRegistry = modelNode.getSchemaRegistry();
        DataSchemaNode parentDataSchemaNode = schemaRegistry.getDataSchemaNode(modelNode.getModelNodeSchemaPath());
        Expression[] stepPredicates = singleStep.getPredicates();
        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        /* As of now filling matchCriteria works only when there only one predicate
			/interfaces/interface[name="eth0"]

			MatchCriteria has to be filled for multiple predicates AND also should suport enum and bit-is-set.
			If matchCriteria is filled count function performs much better.
			It just consumes ~5% time when compared to without matchCriteria
         */
        if(stepPredicates.length == 1) {
            Expression predicate = stepPredicates[0];
            if(predicate instanceof CoreOperationEqual) {
                Expression[] arguements = ((CoreOperationEqual) predicate).getArguments();
                Expression lhs = arguements[0];
                Expression rhs = arguements[1];
                if(rhs instanceof Constant  && lhs instanceof LocationPath) {
                    LocationPath lhsLocationPath = (LocationPath) lhs;
                    if(!lhsLocationPath.isAbsolute() && lhsLocationPath.getSteps().length == 1 && lhsLocationPath.getSteps()[0].getAxis() != Compiler.AXIS_SELF) {
                        DataSchemaNode childSchemaNode = DataStoreValidationUtil.getChildDataSchemaNodeModuleNameAware(schemaRegistry, parentDataSchemaNode.getPath(), stepName, moduleName);
                        if(childSchemaNode instanceof ListSchemaNode){
                            Step lhsStep = lhsLocationPath.getSteps()[0];
                            String lhsLocalName =  DataStoreValidationUtil.getLocalName(lhsStep);
                            String lhsModuleName = DataStoreValidationUtil.getPrefix(lhsStep);
                            DataSchemaNode predicateSchemaNode = DataStoreValidationUtil.getChildDataSchemaNodeModuleNameAware(schemaRegistry, childSchemaNode.getPath(), lhsLocalName, lhsModuleName);
                            if (predicateSchemaNode instanceof LeafSchemaNode) {
                                Object rhsObject = rhs.compute(null);
                                String rhsValueToCompare ;
                                if(rhsObject instanceof Number) {
                                    rhsValueToCompare = InfoSetUtil.stringValue(rhsObject);
                                } else {
                                    rhsValueToCompare = rhsObject.toString();
                                }
                                matchCriteria.put(predicateSchemaNode.getQName(),
                                        ConfigAttributeFactory.getConfigLeafAttribute(schemaRegistry, (LeafSchemaNode) predicateSchemaNode, rhsValueToCompare));
                            }
                        }
                    }
                }
            }
        } else if(nextStep!=null && parentDataSchemaNode!=null && nextStep.getPredicates().length == 1 && nextStep.getPredicates()[0] instanceof CoreOperationEqual) {
            String nextStepName = DataStoreValidationUtil.getLocalName(nextStep);
            String nextStepModuleName = DataStoreValidationUtil.getPrefix(nextStep);
            DataSchemaNode childSchemaNode = DataStoreValidationUtil.getChildDataSchemaNodeModuleNameAware(schemaRegistry, parentDataSchemaNode.getPath(), stepName, moduleName);
            DataSchemaNode predicateSchemaNode = DataStoreValidationUtil.getChildDataSchemaNodeModuleNameAware(schemaRegistry, childSchemaNode.getPath(), nextStepName, nextStepModuleName);
            if(childSchemaNode instanceof ListSchemaNode && predicateSchemaNode instanceof LeafSchemaNode) {
                Expression predicate = nextStep.getPredicates()[0];
                Expression[] arguments = ((CoreOperationEqual) predicate).getArguments();
                Expression lhs = arguments[0];
                Expression rhs = arguments[1];
                if(lhs instanceof LocationPath && rhs instanceof Constant) {
                    LocationPath lhsLocationPath = (LocationPath) lhs;
                    if(!lhsLocationPath.isAbsolute() && lhsLocationPath.getSteps().length == 1) {
                        Step lhsStep = lhsLocationPath.getSteps()[0];
                        if (lhsStep.getAxis() == Compiler.AXIS_SELF) {
                            Object rhsObject = rhs.compute(null);
                            String rhsValueToCompare;
                            if (rhsObject instanceof Number) {
                                rhsValueToCompare = InfoSetUtil.stringValue(rhsObject);
                            } else {
                                rhsValueToCompare = rhsObject.toString();
                            }
                            matchCriteria.put(predicateSchemaNode.getQName(),
                                    ConfigAttributeFactory.getConfigLeafAttribute(schemaRegistry, (LeafSchemaNode) predicateSchemaNode, rhsValueToCompare));
                        }
                    }
                }
            }
        }
        return matchCriteria;
    }

    private void checkCountOnParent(DynaBean contextBean, Step[] step, Object currentContextNode, String leafRefValue,
                                    QName leafQName, int index, DSValidationContext validationContext) {
        if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
            DynaBean parentBean = (DynaBean) contextBean.get(ModelNodeWithAttributes.PARENT);
            if (parentBean != null) {
                countOccurenceOfStepInTree(parentBean, step, currentContextNode, leafRefValue, leafQName, index - 1, validationContext);
            }
        }
    }

    protected Integer getCountFromCacheFor(String stepName) {
        Integer countValue = (Integer) RequestScope.getCurrentScope().getFromCache("count_"+stepName);
        if(countValue == null){
            return 0;
        }
        return countValue;
    }

    protected void setCountToCacheFor(String stepName, Integer countValue) {
        RequestScope.getCurrentScope().putInCache("count_"+stepName, countValue);

    }

    @SuppressWarnings("unchecked")
    protected boolean isBeanTraversed(DynaBean contextBean, String stepName) {
        Map<String, ModelNodeId> traversedBean = (HashMap<String, ModelNodeId>) RequestScope.getCurrentScope().getFromCache(DataStoreValidationUtil.SINGLE_STEP_COUNT);
        if (traversedBean == null) {
            traversedBean = new HashMap<String, ModelNodeId>();
            RequestScope.getCurrentScope().putInCache(DataStoreValidationUtil.SINGLE_STEP_COUNT, traversedBean);
            setCountToCacheFor(stepName,null);
        }

        ModelNode modelNode = null;
        if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.MODEL_NODE)) {
            modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
        }

        if (modelNode != null && traversedBean.get(stepName) != null && traversedBean.get(stepName).equals(modelNode.getModelNodeId())) {
            return true;
        } else if (modelNode != null) {
            traversedBean.put(stepName, modelNode.getModelNodeId());
        }

        return false;

    }

    protected boolean isConstantTreatedAsLocationPath(Expression expression, Integer argumentIndex) {
        if (argumentIndex > 0 && DataStoreValidationUtil.isLocationPath(expression)) {
            // In a function, the second argument tends to be a constant and we recompute the constant, it will treated as a
            // LocationPath unless it is a Number
            LocationPath path = (LocationPath) expression;
            if (path.getSteps().length == 1) {
                // if it is a locationPath, we will likely get more than 1 step to evalute a target node
                return true;
            }
        }
        return false;
    }

}
