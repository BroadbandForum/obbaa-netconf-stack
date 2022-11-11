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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationUnion;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Path;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.WhenConditionAware;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

import com.google.common.collect.ImmutableMap;

public class SchemaSupportVerifierImpl implements SchemaSupportVerifier {
    
    private static final Map<Integer, String> UNSUPPORTED_CORE_FUNCTIONS = ImmutableMap.of(
            Compiler.FUNCTION_LAST, "last",
            Compiler.FUNCTION_POSITION, "position",
            Compiler.FUNCTION_ID, "id",
            Compiler.FUNCTION_LANG, "lang",
            Compiler.FUNCTION_NAME, "name");
    
    public static final List<String> SUPPORTED_EXTENSION_FUNCTIONS = Arrays.asList(
            "current",
            "derived-from",
            "derived-from-or-self",
            "enum-value",
            "re-match",
            "bit-is-set");
    
    private static final List<String> UNSUPPORTED_EXTENSION_FUNCTIONS = Arrays.asList(
            "deref");
    
    private static final Map<Integer, String> UNSUPPORTED_AXES = new ImmutableMap.Builder<Integer, String>()
            .put(Compiler.AXIS_ANCESTOR, "ancestor")
            .put(Compiler.AXIS_ANCESTOR_OR_SELF, "ancestor-or-self")
            .put(Compiler.AXIS_ATTRIBUTE, "attribute")
            .put(Compiler.AXIS_DESCENDANT, "descendant")
            .put(Compiler.AXIS_DESCENDANT_OR_SELF, "descendant-or-self")
            .put(Compiler.AXIS_FOLLOWING, "following")
            .put(Compiler.AXIS_FOLLOWING_SIBLING, "following-sibling")
            .put(Compiler.AXIS_NAMESPACE, "namespace")
            .put(Compiler.AXIS_PRECEDING, "preceding")
            .put(Compiler.AXIS_PRECEDING_SIBLING, "preceding-sibling")
            .build();
    
    private static final Map<Class<? extends CoreOperation>, String> UNSUPPORTED_CORE_OPERATIONS = ImmutableMap.of(
            CoreOperationUnion.class, "|");
    
    private static final Map<Integer, String> UNSUPPORTED_NODE_TYPE_TESTS = ImmutableMap.of(
            Compiler.NODE_TYPE_COMMENT, "comment()",
            Compiler.NODE_TYPE_PI, "processing-instruction()",
            Compiler.NODE_TYPE_TEXT, "text()");

    private static final Map<Integer, String> CORE_FUNCTIONS_ARGS_NOT_ALLOWED = ImmutableMap.of(
    		Compiler.FUNCTION_TRUE, "true",
    		Compiler.FUNCTION_FALSE, "false");

    private static final Map<Integer, String> CORE_FUNCTIONS_ARGS_MANDATORY = ImmutableMap.of(
    		Compiler.FUNCTION_NOT, "not");
    
    @Override
    public void verify(SchemaContext context) {
        Set<String> errorList = new HashSet<>();
        for (DataSchemaNode dataSchemaNode : context.getChildNodes()) {
            traverse(dataSchemaNode, errorList);
        }
        for (RpcDefinition rpcDefinition : context.getOperations()) {
            traverse(rpcDefinition.getInput(), errorList);
            traverse(rpcDefinition.getOutput(), errorList);
        }
        for (NotificationDefinition notificationDefinition : context.getNotifications()) {
            traverse(notificationDefinition, errorList);
        }
        if (! errorList.isEmpty()) {
            throw new RuntimeException(StringUtils.join(errorList, "\n"));
        }
    }

    private void traverse(SchemaNode dataSchemaNode, Collection<String> errorList) {
        handleNode(dataSchemaNode, errorList);        
        
        if (dataSchemaNode instanceof DataNodeContainer) {
            traverseDataNodeContainer((DataNodeContainer)dataSchemaNode, errorList);
        }
        else if (dataSchemaNode instanceof ChoiceSchemaNode) {
            traverseChoice((ChoiceSchemaNode)dataSchemaNode, errorList);
        }        
        
        if (dataSchemaNode instanceof ActionNodeContainer) {
            for (ActionDefinition actionDefinition : ((ActionNodeContainer) dataSchemaNode).getActions()) {
                traverse(actionDefinition.getInput(), errorList);                
                traverse(actionDefinition.getOutput(), errorList);
            }
        }
        
        if (dataSchemaNode instanceof NotificationNodeContainer) {            
            for (NotificationDefinition notificationDefinition : ((NotificationNodeContainer) dataSchemaNode).getNotifications()) {
                traverse(notificationDefinition, errorList);
            }
        }
    }

    private void handleNode(SchemaNode dataSchemaNode, Collection<String> errorList) {
        if (dataSchemaNode instanceof WhenConditionAware) {
            checkWhenCondition(((WhenConditionAware)dataSchemaNode).getWhenCondition(), dataSchemaNode, errorList);
        }
        if (dataSchemaNode instanceof MustConstraintAware) {
            checkMustConditions(((MustConstraintAware) dataSchemaNode).getMustConstraints(), dataSchemaNode, errorList);
        }
        if (dataSchemaNode instanceof LeafListSchemaNode) {
            checkDefault((LeafListSchemaNode)dataSchemaNode, errorList);
        }
        if (dataSchemaNode instanceof TypedDataSchemaNode) {
            TypeDefinition<?> type = ((TypedDataSchemaNode) dataSchemaNode).getType();
            if (type instanceof LeafrefTypeDefinition) {
                verifyXPath(((LeafrefTypeDefinition) type).getPathStatement().getOriginalString(), dataSchemaNode, errorList);
            }
        }
    }

    private void traverseDataNodeContainer(DataNodeContainer dataNodeContainer, Collection<String> errorList) {
        for (DataSchemaNode dataSchemaNode : dataNodeContainer.getChildNodes()) {
            traverse(dataSchemaNode, errorList);
        }
    }
    
    private void traverseChoice(ChoiceSchemaNode choice, Collection<String> errorList) {
        for (CaseSchemaNode caseSchemaNode : choice.getCases().values()) {
            traverse(caseSchemaNode, errorList);
        }
    }

    private void checkWhenCondition(Optional<RevisionAwareXPath> whenCondition, SchemaNode node, Collection<String> errorList) {
        if (whenCondition.isPresent()) {
            RevisionAwareXPath xpath = whenCondition.get();
            verifyXPath(xpath.getOriginalString(), node, errorList);
        }
    }

    private void checkMustConditions(Collection<MustDefinition> mustConstraints, SchemaNode node, Collection<String> errorList) {
        for (MustDefinition mustConstraint: mustConstraints) {
            RevisionAwareXPath xpath = mustConstraint.getXpath();
            verifyXPath(xpath.getOriginalString(), node, errorList);
        }
    }
    
    private void verifyXPath(String xpathString, SchemaNode node, Collection<String> errorList) {
        Expression expression = null;
        try {
            expression = JXPathUtils.getExpression(xpathString);
        } catch (Exception e) {
            String errorMessage = "Problem in XPath in " + node.getQName() + ": " + e.getMessage();
            errorList.add(errorMessage);            
        }
        traverseXPath(expression, node, errorList);
    }
    
    private void traverseXPath(Expression expression, SchemaNode node, Collection<String> errorList) {
        if (expression instanceof Operation) {
            checkOperation(expression, node, errorList);
            Expression[] arguments = ((Operation) expression).getArguments();
            if (arguments != null) {
                for (Expression argument : arguments) {
                    traverseXPath(argument, node, errorList);
                }
            }
        }
        else if (expression instanceof Path) {
            if (expression instanceof ExpressionPath) {
                Expression pathExpression = ((ExpressionPath) expression).getExpression();
                traverseXPath(pathExpression, node, errorList);
                
                Expression[] predicates = ((ExpressionPath) expression).getPredicates();
                if (predicates != null) {
                    for (Expression predicate : predicates) {
                        checkPredicate(predicate, node, errorList);
                        traverseXPath(predicate, node, errorList);
                    }                    
                }
            }
            Step[] steps = ((Path) expression).getSteps();
            if (steps != null) {
                for (Step step : steps) {
                    checkStep(step, node, errorList);
                    Expression[] predicates = step.getPredicates();
                    if (predicates != null) {
                        for (Expression predicate : predicates) {
                            checkPredicate(predicate, node, errorList);
                            traverseXPath(predicate, node, errorList);
                        }
                    }
                }
            }
        }
    }

    private void checkPredicate(Expression predicate, SchemaNode node, Collection<String> errorList) {
        String errorMessage = null;
        if (predicate instanceof Constant) {
            errorMessage = "Indexing in an XPath step is not supported";
        }
        if (errorMessage != null) {
            errorMessage = "Problem in XPath in " + node.getQName() + ": " + errorMessage;
            errorList.add(errorMessage);
        }
    }

    private void checkStep(Step step, SchemaNode node, Collection<String> errorList) {
        String errorMessage = null;
        int axis = step.getAxis();
        NodeTest nodeTest = step.getNodeTest();
        if (UNSUPPORTED_AXES.containsKey(axis)) {
            String axisName = UNSUPPORTED_AXES.get(axis);
            errorMessage = "XPath axis '" + axisName + "' is not supported";
        }
        else if (nodeTest instanceof NodeNameTest && ((NodeNameTest) nodeTest).isWildcard()) {
            errorMessage = "Wildcard * is not supported";
        }
        else if (nodeTest instanceof NodeTypeTest) {
            int nodeType = ((NodeTypeTest) nodeTest).getNodeType();
            if (UNSUPPORTED_NODE_TYPE_TESTS.containsKey(nodeType)) {
                String nodeTypeName = UNSUPPORTED_NODE_TYPE_TESTS.get(nodeType);
                errorMessage = "XPath node type test '" + nodeTypeName + "' is not supported";
            }
        }
        else if (nodeTest instanceof ProcessingInstructionTest) {
            errorMessage = "XPath processing instruction test is not supported";
        }
        if (errorMessage != null) {
            errorMessage = "Problem in XPath in " + node.getQName() + ": " + errorMessage;
            errorList.add(errorMessage);
        }
    }

    private void checkOperation(Expression expression, SchemaNode node, Collection<String> errorList) {
        String errorMessage = null;
        if (expression instanceof CoreOperation) {
            if (UNSUPPORTED_CORE_OPERATIONS.containsKey(expression.getClass())) {
                String operator = UNSUPPORTED_CORE_OPERATIONS.get(expression.getClass());
                errorMessage = "XPath core operation '" + operator + "' is not supported";
            }
        }
        else if (expression instanceof CoreFunction) {
            CoreFunction coreFunction = (CoreFunction) expression;
            int functionCode = coreFunction.getFunctionCode();
            if (UNSUPPORTED_CORE_FUNCTIONS.containsKey(functionCode)) {
                String functionName = UNSUPPORTED_CORE_FUNCTIONS.get(functionCode);
                errorMessage = "XPath core function '" + functionName + "' is not supported";
            } else if(CORE_FUNCTIONS_ARGS_NOT_ALLOWED.containsKey(functionCode) && coreFunction.getArguments() != null) {
            	String functionName = CORE_FUNCTIONS_ARGS_NOT_ALLOWED.get(functionCode);
            	errorMessage = "Invalid JXPath syntax: Arguments not allowed for the function '" + functionName + "'";
            } else if(CORE_FUNCTIONS_ARGS_MANDATORY.containsKey(functionCode) && coreFunction.getArguments() == null) {
            	String functionName = CORE_FUNCTIONS_ARGS_MANDATORY.get(functionCode);
            	errorMessage = "Invalid JXPath syntax: Incorrect number of arguments for the function '" + functionName + "'";
            } else {
                errorMessage = checkFunctionUsage(coreFunction);
            }
        }
        else if (expression instanceof ExtensionFunction) {
            String functionName = ((ExtensionFunction) expression).getFunctionName().getName();
            if (UNSUPPORTED_EXTENSION_FUNCTIONS.contains(functionName)) {
                errorMessage = "XPath extension function '" + functionName + "' is not supported yet.";
            }
            else if (! SUPPORTED_EXTENSION_FUNCTIONS.contains(functionName)) {
                errorMessage = "Unknown XPath function '" + functionName + "'";
            }
        }
        if (errorMessage != null) {
            errorMessage = "Problem in XPath in " + node.getQName() + ": " + errorMessage;
            errorList.add(errorMessage);
        }
    }

    private String checkFunctionUsage(CoreFunction coreFunction) {
        if (Compiler.FUNCTION_SUBSTRING == coreFunction.getFunctionCode()) {
            Expression[] arguments = coreFunction.getArguments();
            if (arguments.length > 1) {
                Expression indexArg = arguments[1];
                if (indexArg instanceof Constant && indexArg.toString().equals("0")) {
                    return "String indexing in XPath function substring starts from 1, not 0";
                }
            }
        }
        return null;
    }

    private void checkDefault(LeafListSchemaNode dataSchemaNode, Collection<String> errorList) {
        Collection<? extends Object> defaults = dataSchemaNode.getDefaults();
        if (! defaults.isEmpty()) {
            String message = "Leaf List " + dataSchemaNode.getQName() + " has one or more default values. ";
            message += "Default values for leaf-list are not supported yet. ";
            message += "Contact Test Company to plan support for this construct ";
            errorList.add(message);
        }
    }
    
}
