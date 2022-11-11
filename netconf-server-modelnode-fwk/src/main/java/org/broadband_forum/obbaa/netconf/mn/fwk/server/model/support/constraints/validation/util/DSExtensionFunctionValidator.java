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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.COLON;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.isReadable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

/**
 * Validates only Expression of type ExtensionFunction.
 *
 */
public class DSExtensionFunctionValidator extends DSExpressionValidator {
    private static final String DOLLAR = "$";
    private static final String CARET = "^";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String SINGLE_QUOTE = "'";
    public static final String DERIVED_FROM = "derived-from";
    public static final String DERIVED_FROM_OR_SELF = "derived-from-or-self";
    static final String ENUM_VALUE = "enum-value";
    static final String RE_MATCH = "re-match";
    private static final String BIT_IS_SET = "bit-is-set";
    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DSExtensionFunctionValidator.class, LogAppNames.NETCONF_STACK);

    public DSExtensionFunctionValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
            SubSystemRegistry registry, Map<Class<?>, DSExpressionValidator> validators) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        this.m_subSystemRegistry = registry;
        validators.put(ExtensionFunction.class, this); 
    }

    @Override
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode, String leafRefValue,
                              QName leafQName, DSValidationContext validationContext) {
        if (DataStoreValidationUtil.isExtensionFunction(xPathCondition)) {
            logDebug("Evaluating for extension function {}", xPathCondition);
            Object returnValue = null;
            if (((ExtensionFunction) xPathCondition).getFunctionName().getName().equals(DERIVED_FROM)
                    || ((ExtensionFunction) xPathCondition).getFunctionName().getName().equals(DERIVED_FROM_OR_SELF)) {
                returnValue = evaluateDerivedFrom(this, contextBean, currentContextNode, xPathCondition, leafRefValue,
                        leafQName, validationContext);
            } else if (((ExtensionFunction) xPathCondition).getFunctionName().getName().equals(ENUM_VALUE)) {
                returnValue = evaluateEnumValue(this, contextBean, currentContextNode, xPathCondition, leafRefValue,
                        leafQName, validationContext);
            } else if (((ExtensionFunction) xPathCondition).getFunctionName().getName().equals(RE_MATCH)) {
                returnValue = evaluateReMatchValue(this, contextBean, currentContextNode, xPathCondition, leafRefValue,
                        leafQName, validationContext);
            } else if (((ExtensionFunction) xPathCondition).getFunctionName().getName().equals(BIT_IS_SET)) {
                returnValue = evaluateBitInSetValue(this, contextBean, currentContextNode, xPathCondition, leafRefValue,
                        leafQName, validationContext);
            } else {
                LOGGER.warn("Function : {} is not supported yet",
                        ((ExtensionFunction) xPathCondition).getFunctionName().getName());
            }
            return validateLeafRef(returnValue, leafRefValue);
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName, validationContext);
        }
    }

    private Object evaluateBitInSetValue(DSExtensionFunctionValidator validator, DynaBean contextBean, Object contextNode, Expression xPathCondition, String leafRefValue,
            QName leafQName, DSValidationContext validationContext) {
        
        Expression[] predicates = ((ExtensionFunction) xPathCondition).getArguments();
        if (predicates.length == 2) {
            Expression expressionToEvaluate = predicates[0];
            Expression patternExp = predicates[1];
            String bit = patternExp.toString();            
            Object selectedBits = validator.checkForFunctionsAndEvaluate(expressionToEvaluate, contextBean, contextNode, null, leafQName,
                    validationContext);
            Pair<SchemaRegistry, DataSchemaNode> pair = DataStoreValidationUtil.getDataSchemaNodeAndSchemaRegistryPair(contextBean,
                    expressionToEvaluate, leafQName);
            if ( pair != null ){
                DataSchemaNode dataSchemaNode = pair.getSecond();
                TypeDefinition<?> refDefinition = ((TypedDataSchemaNode)dataSchemaNode).getType();
                boolean isBitType = refDefinition instanceof BitsTypeDefinition;
                if ( isBitType && isBitSet(selectedBits, bit, dataSchemaNode)){
                    return true;
                }
            }
        }
        return null;
    }
    
    private boolean isBitSet(Object selectedBits, String bit, DataSchemaNode dataSchemaNode) {
        bit = removeQuote(bit);
        if ( selectedBits != null){
            List<String> selectedBitsArray = getSelectedBits(selectedBits, dataSchemaNode);
            if (selectedBitsArray.contains(bit)){
                return true;
            }
        }
        return false;
    }

    private List<String> getSelectedBits(Object selectedBits, DataSchemaNode dataSchemaNode) {
        if (dataSchemaNode instanceof LeafListSchemaNode){
            List<String> bits = new ArrayList<>();
            if (selectedBits instanceof Collection) {
                for (Object object : ((Collection<?>) selectedBits)) {
                    if (object instanceof GenericConfigAttribute) {
                        // in case for leaf-list
                        GenericConfigAttribute genericConfigAttribute = (GenericConfigAttribute) object;
                        String selectedBitsString = genericConfigAttribute.getStringValue();
                        bits.addAll(Arrays.asList(StringUtils.split(selectedBitsString)));
                    }
                }
            }
            return bits;
        } else {
            String selectedBitsString = selectedBits.toString();
            return Arrays.asList(StringUtils.split(selectedBitsString));            
        }
    }

    private String removeQuote(String bit) {
        if (bit.startsWith(SINGLE_QUOTE) && bit.endsWith(SINGLE_QUOTE)) {
            bit = StringUtils.stripStart(bit, SINGLE_QUOTE);
            bit = StringUtils.stripEnd(bit, SINGLE_QUOTE);
        }
        return bit;
    }

    private Object evaluateDerivedFrom(DSExpressionValidator validator, DynaBean contextBean, Object contextNode, Expression xPath,
                                       String leafRefValue, QName leafQName, DSValidationContext validationContext) {
        Expression expression = xPath;
        boolean selfValue = false;
        if (DataStoreValidationUtil.isExtensionFunction(expression)
                && ((ExtensionFunction) expression).getFunctionName().getName().contains(DERIVED_FROM)) {
            if (((ExtensionFunction) expression).getFunctionName().getName().equals(DERIVED_FROM_OR_SELF)) {
                selfValue = true;
            }

            Expression[] predicates = ((ExtensionFunction) expression).getArguments();
            // 1st parameter will be xPath/node-set second will be the identityRef value
            Expression expressionToEvaluate = predicates[0];
            String expectedBaseIdentity = (String) predicates[1].compute(null);
            Object value = validator.checkForFunctionsAndEvaluate(expressionToEvaluate, contextBean, contextNode, leafRefValue, leafQName,
                    validationContext);

            // If this is a SELF-VALUE '.' and if we have a leafQName indicates, this represents a leaf/leaflist.
            // In that case, we need to check if we have got the QName as a child in container/list -> received dynabean
            if (value instanceof DynaBean && expressionToEvaluate instanceof LocationPath
                    && ((LocationPath) expressionToEvaluate).getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                DynaBean selfValueContainer = (DynaBean) value;
                if (leafQName != null) {
                    String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(leafQName.getLocalName());
                    if (DataStoreValidationUtil.isReadable(selfValueContainer, localName)) {
                        ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil
                                .getModelNodeDynaBeanContext(selfValueContainer, localName, leafQName.getNamespace().toString(), null);
                        value = ModelNodeDynaBean.withContext(dynaBeanContext, () -> selfValueContainer.get(localName));
                    } else {
                        LOGGER.warn("dynaBean {} did not contain attribute {}", selfValueContainer.getDynaClass().getName(), localName);
                    }
                } // leafQName != null
            }
            Pair<SchemaRegistry, DataSchemaNode> pair = DataStoreValidationUtil.getDataSchemaNodeAndSchemaRegistryPair(contextBean,
                    expressionToEvaluate, leafQName);
            
            if ( pair != null && pair.getSecond() != null){
                TypeDefinition<?> refDefinition = ((TypedDataSchemaNode)pair.getSecond()).getType();
                SchemaRegistry schemaRegistry = pair.getFirst();
                if (refDefinition instanceof IdentityrefTypeDefinition) {
                    IdentityrefTypeDefinition baseDefition = (IdentityrefTypeDefinition) refDefinition;

                    // Expected identity is the second argument in the function derived-from(arg1,arg2)
                    // the second argument can have prefix. In case it does not have, use the prefix of QName of leaf.
                    if(expectedBaseIdentity.split(COLON).length == 2) {
                        Module module = validationContext.getNodeConstraintAssociatedModule();
                        String constraintPrefix = expectedBaseIdentity.split(COLON)[0];
                        String constraintIdentity= expectedBaseIdentity.split(COLON)[1];
                        String matchedModuleName = DataStoreValidationUtil.getMatchedModuleNameForPrefix(module, constraintPrefix);
                        if(matchedModuleName == null && validationContext.getAugmentationSchemaNodeForWhen() != null) {
                            matchedModuleName = DataStoreValidationUtil.getModuleNameFromPrefix(schemaRegistry, constraintPrefix,
                                    validationContext.getAugmentationSchemaNodeForWhen());
                        }
                        if(matchedModuleName != null) {
                            Optional<Module> matchedModule = schemaRegistry.getModule(matchedModuleName);
                            if(matchedModule.isPresent()) {
                                String moduleAssociatedPrefix = matchedModule.get().getPrefix();
                                expectedBaseIdentity = moduleAssociatedPrefix + COLON + constraintIdentity;
                            }
                        }
                        
                    } else if(expectedBaseIdentity.split(COLON).length == 1) {
                        // indicates the prefix is local
                        expectedBaseIdentity = schemaRegistry.getPrefix(refDefinition.getQName().getNamespace().toString()) + COLON
                                + expectedBaseIdentity;
                    }
                    Object targetValue = value;
                    if (value instanceof Collection) {
                        targetValue = buildValuesList(value);
                    }

                    // Now verify against each identity and its derivative if that is what we have.
                    for (IdentitySchemaNode identitySchemaNode : baseDefition.getIdentities()) {
                        boolean returnValue = checkIfIdentity(schemaRegistry, identitySchemaNode, expectedBaseIdentity, selfValue,
                                targetValue);
                        if (returnValue) {
                            return true;
                        }
                    }
                }

            }
        }
        
        return null;
    }

    private LinkedHashSet<String> buildValuesList(Object value) {
        LinkedHashSet<String> leafListValues = new LinkedHashSet<String>();
        String attributeValue = null;
        for (Object nodeValue : (Collection<?>) value) {
            if (nodeValue instanceof IdentityRefConfigAttribute) {
                attributeValue = ((IdentityRefConfigAttribute) nodeValue).getStringValue();
                leafListValues.add(attributeValue);
            } else {
                leafListValues.add(nodeValue.toString());
            }
        }
        return leafListValues;
    }

    
    private Object evaluateEnumValue(DSExpressionValidator validator, DynaBean contextBean, Object contextNode,
            Expression xPath, String leafRefValue, QName leafQName, DSValidationContext validationContext) {
        Expression expression = xPath;
        Expression[] predicates = ((ExtensionFunction) expression).getArguments();
        if (predicates.length == 1) {
            Expression expressionToEvaluate = predicates[0];
            // argument will be a xPath/node-set
            Object value = null;
            if (expressionToEvaluate instanceof LocationPath
                    && ((LocationPath) expressionToEvaluate).getSteps().length > 1
                    && ((LocationPath) expressionToEvaluate).getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                /*
                 * For cases such as "./../some-container/some-leaf" ie. xpath
                 * beginning from self. We need to remove first step "." , as we
                 * are already in currentBean , we need to evaluate with
                 * remaining path starting from currentBean
                 */

                Expression modifiedExpression = expressionToEvaluate;
                modifiedExpression = removeFirstStep((LocationPath) expressionToEvaluate);
                value = validator.checkForFunctionsAndEvaluate(modifiedExpression, contextBean, contextNode,
                        leafRefValue, leafQName, validationContext);
            } else {
                value = validator.checkForFunctionsAndEvaluate(expressionToEvaluate, contextBean, contextNode,
                        leafRefValue, leafQName, validationContext);
            }
            if (value != null) {
                Set<Object> result = new HashSet<>();
                // If this is a SELF-VALUE '.' and if we have a leafQName
                // indicates, this represents a leaf/leaflist.
                // In that case, we need to check if we have got the QName as a
                // child in received dynabean
                if (value instanceof DynaBean && expressionToEvaluate instanceof LocationPath
                        && ((LocationPath) expressionToEvaluate).getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                    DynaBean selfValue = (DynaBean) value;
                    if (leafQName != null) {
                        String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(leafQName.getLocalName());
                        if (DataStoreValidationUtil.isReadable(selfValue, localName)) {
                            result.add(selfValue.get(localName));
                            value = result;
                        } else {
                            LOGGER.warn("dynaBean {} did not contain attribute {}", selfValue.getDynaClass().getName(),
                                    localName);
                        }
                    }
                }

                ModelNode modelNode = null;

                LocationPath path = DataStoreValidationUtil.getDynaBeanAlignedPath((LocationPath) expressionToEvaluate);

                DynaBean nextReadableBean = contextBean;
                while (!isReadable(nextReadableBean, ModelNodeWithAttributes.MODEL_NODE)
                        && isReadable(nextReadableBean, ModelNodeWithAttributes.PARENT)) {
                    nextReadableBean = (DynaBean) nextReadableBean.get(ModelNodeWithAttributes.PARENT);
                }
                if (isReadable(nextReadableBean, ModelNodeWithAttributes.MODEL_NODE)) {
                    modelNode = (ModelNode) nextReadableBean.get(ModelNodeWithAttributes.MODEL_NODE);
                    SchemaRegistry schemaRegistry = modelNode.getSchemaRegistry();
                    SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
                    Expression evalExp = path;
                    DataSchemaNode childSchemaNode = fetchDataSchemaNode(contextBean, schemaRegistry, schemaPath,
                            evalExp, leafQName);
                    return validateAndFetchEnumValue(childSchemaNode, schemaRegistry, value, leafQName, xPath);
                }
            }
            return Double.NaN;

        } else {
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Missing argument in enum-value function. You need to provide one argument in the enum-value() function : "
                            + xPath.toString() + " in node : " + leafQName.getLocalName());
            throw new ValidationException(rpcError);
        }
    }

    private DataSchemaNode fetchDataSchemaNode(DynaBean contextBean, SchemaRegistry schemaRegistry,
            SchemaPath schemaPath, Expression evalExp, QName leafQName) {
        DataSchemaNode dataSchemaNode = null;
        if (evalExp instanceof LocationPath) {
            SchemaPath childPath = null;
            if (isReadable(contextBean, ModelNodeWithAttributes.LEAF_VALUE)
                    && ((LocationPath) evalExp).getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
                childPath = DataStoreValidationUtil.getXPathSchemaPath(schemaRegistry, schemaPath,
                        DataStoreValidationUtil.excludeFirstStep((LocationPath) evalExp));
            } else if ((isReadable(contextBean, ModelNodeWithAttributes.LEAF_VALUE)
                    && ((LocationPath) evalExp).getSteps()[0].getAxis() == Compiler.AXIS_SELF
                    && ((LocationPath) evalExp).getSteps().length == 1)) {
                childPath = DataStoreValidationUtil.getChildPath(schemaRegistry, schemaPath, leafQName.getLocalName());
            } else if ((isReadable(contextBean, ModelNodeWithAttributes.LEAF_VALUE)
                    && ((LocationPath) evalExp).getSteps()[0].getAxis() == Compiler.AXIS_SELF
                    && ((LocationPath) evalExp).getSteps().length > 1)) {
                return fetchDataSchemaNode(contextBean, schemaRegistry, schemaPath, removeFirstStep((LocationPath) evalExp), leafQName);
            } else {
                childPath = DataStoreValidationUtil.getXPathSchemaPath(schemaRegistry, schemaPath,
                        (LocationPath) evalExp);
            }
            dataSchemaNode = schemaRegistry.getDataSchemaNode(childPath);
        }
        return dataSchemaNode;
    }

    private Object validateAndFetchEnumValue(DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry,
            Object targetValue, QName leafQName, Expression xPath) {

        HashSet<Object> enumValues = new HashSet<>();
        if (dataSchemaNode instanceof LeafSchemaNode) {

            if (((LeafSchemaNode) dataSchemaNode).getType() instanceof EnumTypeDefinition) {
                EnumTypeDefinition enumTypeDefinition = (EnumTypeDefinition) ((LeafSchemaNode) dataSchemaNode)
                        .getType();
                if (targetValue instanceof Collection) {
                    fetchCorrespondingEnumValues(targetValue, enumValues, enumTypeDefinition);
                    return enumValues;
                } else {
                    for (EnumPair enumPair : enumTypeDefinition.getValues()) {
                        if (enumPair.getName().equals(targetValue.toString())) {
                            return enumPair.getValue();
                        }
                    }
                }
            }
        }

        else if (dataSchemaNode instanceof LeafListSchemaNode) {
            if (((LeafListSchemaNode) dataSchemaNode).getType() instanceof EnumTypeDefinition) {
                EnumTypeDefinition enumTypeDefinition = (EnumTypeDefinition) ((LeafListSchemaNode) dataSchemaNode)
                        .getType();
                fetchCorrespondingEnumValues(targetValue, enumValues, enumTypeDefinition);
                return enumValues;
            }
        }
        return Double.NaN;
    }

    private void fetchCorrespondingEnumValues(Object targetValue, HashSet<Object> enumValues,
            EnumTypeDefinition enumTypeDefinition) {
        if (targetValue instanceof Collection && !((Collection<?>) targetValue).isEmpty()) {
            // This indicates a node-set
            Object enumValue = fetchEnumValue(enumTypeDefinition, ((Collection<?>) targetValue).iterator().next());
            if (enumValue != null) {
                enumValues.add(enumValue);
            }
        }
    }

    private Object fetchEnumValue(EnumTypeDefinition enumTypeDefinition, Object object) {
        for (EnumPair enumPair : enumTypeDefinition.getValues()) {
            if (object instanceof GenericConfigAttribute) {
                // in case for leaf-list
                GenericConfigAttribute genericConfigAttribute = (GenericConfigAttribute) object;
                if (enumPair.getName().equals(genericConfigAttribute.getStringValue())) {
                    return enumPair.getValue();
                }
            } else if (enumPair.getName().equals(object.toString())) {
                return enumPair.getValue();
            }
        }
        return null;
    }
    
    private LocationPath removeFirstStep(LocationPath locationPath) {
        Step[] newSteps = new Step[locationPath.getSteps().length -1 ];
        System.arraycopy(locationPath.getSteps(), 1, newSteps, 0, newSteps.length);
        return new LocationPath(false, newSteps);
    }

    private Object evaluateReMatchValue(DSExpressionValidator validator, DynaBean contextBean, Object contextNode,
                                        Expression xPath, String leafRefValue, QName leafQName, DSValidationContext validationContext) {
        Expression expression = xPath;
        Expression[] predicates = ((ExtensionFunction) expression).getArguments();
        if (predicates.length == 2) {
            // eg. re-match(.,"\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}")
            Expression expressionToEvaluate = predicates[0];
            /*
            If the first argument of the re-match function was current() function ,
            then by the time call reaches here current() had been already evaluated to corresponding constant value
             */
            Expression patternExp = predicates[1];
            String patternString = patternExp.toString();

            //trim the patternString of any " or '
            patternString = patternString.lastIndexOf(DOUBLE_QUOTE) == patternString.length() - 1
                    ? StringUtils.strip(patternString, DOUBLE_QUOTE) : StringUtils.strip(patternString, SINGLE_QUOTE);

            //replace // by / as yang 1.1 require backslash for escape sequences
            patternString = patternString.replace("\\\\","\\");
     
            /*
             * As per RFC-7950
             * Re-match function includes implicit anchoring of the regular expression at the head and tail.
             * Thus this anchoring needs to be handled here.
             */
            StringBuilder patternBuilder = new StringBuilder(CARET);
            patternBuilder.append(patternString);
            patternBuilder.append(DOLLAR);
            Pattern pattern = Pattern.compile(patternBuilder.toString());

            Object value = null;
            if (expressionToEvaluate instanceof LocationPath
                    && ((LocationPath) expressionToEvaluate).getSteps().length > 1
                    && ((LocationPath) expressionToEvaluate).getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                /*
                 * For cases such as "./../some-container/some-leaf" ie. xpath
                 * beginning from self. We need to remove first step "." , as we
                 * are already in currentBean , we need to evaluate with
                 * remaining path starting from currentBean
                 */

                Expression modifiedExpression = expressionToEvaluate;
                modifiedExpression = removeFirstStep((LocationPath) expressionToEvaluate);
                value = validator.checkForFunctionsAndEvaluate(modifiedExpression, contextBean, contextNode,
                        leafRefValue, leafQName, validationContext);
            } else if (expressionToEvaluate instanceof Constant) {
                String stringValue = expressionToEvaluate.toString();
                stringValue = stringValue.lastIndexOf(DOUBLE_QUOTE) == patternString.length() - 1
                        ? StringUtils.strip(stringValue, DOUBLE_QUOTE) : StringUtils.strip(stringValue, SINGLE_QUOTE);
                return getPatternMatchResult(pattern, stringValue);
            } else {
                value = validator.checkForFunctionsAndEvaluate(expressionToEvaluate, contextBean, contextNode,
                        leafRefValue, leafQName, validationContext);
            }
            if (value != null) {
                // If this is a SELF-VALUE '.' and if we have a leafQName
                // indicates, this represents a leaf/leaflist.
                // In that case, we need to check if we have got the QName as a
                // child in received dynabean
                if (value instanceof DynaBean && expressionToEvaluate instanceof LocationPath
                        && ((LocationPath) expressionToEvaluate).getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                    DynaBean selfValue = (DynaBean) value;
                    if (leafQName != null) {
                        String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(leafQName.getLocalName());
                        if (DataStoreValidationUtil.isReadable(selfValue, localName)) {
                            return getPatternMatchResult(pattern, selfValue.get(localName).toString());
                        } else {
                            LOGGER.warn("dynaBean {} did not contain attribute {}", selfValue.getDynaClass().getName(),
                                    localName);
                        }
                    }
                } else if (value instanceof Collection) {
                    // This indicates a node-set
                    // Generally leaflist xpaths will return a node-set
                    /*
                    Each node in the node-set should return true when matched to pattern else return false.
                     */
                    for (Object object : ((Collection<?>) value)) {
                        String stringValue = (String) getObjectValue(object);
                        if (!getPatternMatchResult(pattern, stringValue)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return getPatternMatchResult(pattern, (String) value);
                }
            }
            return false;

        } else {
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Missing arguments in re-match function. You need to provide two arguments in the re-match() function : "
                            + xPath.toString() + " in node : " + leafQName.getLocalName());
            throw new ValidationException(rpcError);
        }
    }

    private boolean getPatternMatchResult(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    private Object getObjectValue(Object object) {
        if (object instanceof GenericConfigAttribute) {
            // in case for leaf-list
            GenericConfigAttribute genericConfigAttribute = (GenericConfigAttribute) object;
                return genericConfigAttribute.getStringValue();

        } else {
            return object.toString();
        }
    }


    private boolean checkIfIdentity(SchemaRegistry schemaRegistry, IdentitySchemaNode baseIdentitySchemaNode, String expectedBaseIdentity, boolean canBeBaseIdentity, Object targetValue){
        DerivedFromOrSelfResults cachedResults = getResultCache();
        Boolean returnValue = cachedResults.getResult(schemaRegistry, baseIdentitySchemaNode, expectedBaseIdentity, canBeBaseIdentity, targetValue);

        return returnValue;
    }

    private DerivedFromOrSelfResults getResultCache() {
        DerivedFromOrSelfResults cachedResults = (DerivedFromOrSelfResults) RequestScope.getCurrentScope().getFromCache("DerivedFromOrSelfEvalResults");
        if(cachedResults == null){
            cachedResults = new DerivedFromOrSelfResults();
            RequestScope.getCurrentScope().putInCache("DerivedFromOrSelfEvalResults", cachedResults);
        }
        return cachedResults;
    }

    public static void clearCachedNodes() {
        RequestScope currentScope = RequestScope.getCurrentScope();
        DerivedFromOrSelfResults cachedResults = (DerivedFromOrSelfResults) RequestScope.getCurrentScope().getFromCache("DerivedFromOrSelfEvalResults");
        if(cachedResults != null){
            cachedResults.clearCache();
        }
        currentScope.removeFromCache("DerivedFromOrSelfEvalResults");
    }
}
