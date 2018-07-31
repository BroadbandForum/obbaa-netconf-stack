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

import java.util.Collection;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

/**
 * Validates only Expression of type ExtensionFunction. For now supports only derived-from and derived-from-or-self
 */
public class DSExtensionFunctionValidator extends DSExpressionValidator {
    static final String DERIVED_FROM = "derived-from";
    static final String DERIVED_FROM_OR_SELF = "derived-from-or-self";

    public DSExtensionFunctionValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                        Map<Class<?>, DSExpressionValidator> validators) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        setValidators(validators);
    }

    @Override
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode, String
            leafRefValue,
                              QName leafQName) {
        if (DataStoreValidationUtil.isExtensionFunction(xPathCondition)) {
            logDebug("Evaluating for extension function {}", xPathCondition);
            return evaluateDerivedFrom(this, contextBean, currentContextNode, xPathCondition.toString());
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
        }
    }

    private Object evaluateDerivedFrom(DSExpressionValidator validator, DynaBean contextBean, Object contextNode,
                                       String xPath) {
        Expression expression = JXPathUtils.getExpression(xPath);
        boolean selfValue = false;
        if (expression instanceof ExtensionFunction && expression.toString().contains(DERIVED_FROM)) {
            if (expression.toString().contains(DERIVED_FROM_OR_SELF)) {
                selfValue = true;
            }

            Expression[] predicates = ((ExtensionFunction) expression).getArguments();
            // 1st parameter will be xPath/node-set second will be the identityRef value
            Expression expressionToEvaluate = predicates[0];
            String expectedBaseIdentity = (String) predicates[1].compute(null);
            Object value = validator.checkForFunctionsAndEvaluate(expressionToEvaluate, contextBean, contextNode,
                    null, null);

            ModelNode modelNode = null;
            DynaBean newContextBean = contextBean;
            while (!DataStoreValidationUtil.isReadable(newContextBean, ModelNodeWithAttributes.MODEL_NODE) &&
                    DataStoreValidationUtil.isReadable(newContextBean,
                    ModelNodeWithAttributes.PARENT)) {
                newContextBean = (DynaBean) newContextBean.get(ModelNodeWithAttributes.PARENT);
            }

            if (DataStoreValidationUtil.isReadable(newContextBean, ModelNodeWithAttributes.MODEL_NODE)) {
                // get the modelNode and schemaRegistry. 
                // build the childpath from the current ModelNode schemaPath
                modelNode = (ModelNode) newContextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                SchemaRegistry schemaRegistry = modelNode.getSchemaRegistry();
                SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
                Expression evalExp = expressionToEvaluate;

                if (evalExp instanceof LocationPath) {
                    SchemaPath childPath = null;
                    if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.LEAF_VALUE) && ((LocationPath) evalExp)
                            .getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
                        childPath = DataStoreValidationUtil.getXPathSchemaPath(schemaRegistry, schemaPath,
                                DataStoreValidationUtil.excludeFirstStep((LocationPath) evalExp));
                    } else {
                        childPath = DataStoreValidationUtil.getXPathSchemaPath(schemaRegistry, schemaPath,
                                (LocationPath) evalExp);
                    }
                    DataSchemaNode childSchemaNode = schemaRegistry.getDataSchemaNode(childPath);

                    // child SchemaNode has to be a leafRef with type IdentityRef. Else derive-from() wont work. 
                    if (childSchemaNode instanceof LeafSchemaNode && ((LeafSchemaNode) childSchemaNode).getType()
                            instanceof IdentityrefTypeDefinition) {
                        IdentityrefTypeDefinition refDefinition = (IdentityrefTypeDefinition) ((LeafSchemaNode)
                                childSchemaNode).getType();
                        IdentityrefTypeDefinition baseDefition = refDefinition;

                        // Expected identity is the second argument in the function derived-from(arg1,arg2)
                        // the second argument can have prefix. In case it does not have, use the prefix of QName of
                        // leaf.
                        if (expectedBaseIdentity.split(DataStoreValidationUtil.COLON).length != 2) {
                            //indicates the prefix is local
                            expectedBaseIdentity = schemaRegistry.getPrefix(refDefinition.getQName().getNamespace()
                                    .toString()) + DataStoreValidationUtil.COLON + expectedBaseIdentity;
                        }

                        // Now verify against each identity and its derivative if that is what we have.
                        for (IdentitySchemaNode identitySchemaNode : baseDefition.getIdentities()) {
                            boolean returnValue = checkIfIdentity(schemaRegistry, identitySchemaNode,
                                    expectedBaseIdentity, selfValue, value);
                            if (returnValue) {
                                return true;
                            }
                        }


                    }
                }
            }


        }

        return null;
    }


    /**
     * found the targetIdentity. Now evaluate with the actual value
     **/
    @SuppressWarnings("rawtypes")
    private boolean validateDerivedOrSelf(SchemaRegistry schemaRegistry, IdentitySchemaNode identitySchemaNode,
                                          boolean canBeBaseIdentity, Object targetValue, String iValue) {

        if (canBeBaseIdentity) {
            // if it is derived-from-or-self()? 
            if (iValue.equals(targetValue)) {
                return true;
            } else if (targetValue instanceof Collection && ((Collection) targetValue).contains(iValue)) {
                return true;
            }
        }

        for (IdentitySchemaNode derivedSchemaNode : identitySchemaNode.getDerivedIdentities()) {
            String derivedValue = buildIdentityStringValue(schemaRegistry, derivedSchemaNode).toString();
            if (derivedValue.equals(targetValue)) {
                return true;
            } else if (targetValue instanceof Collection && ((Collection) targetValue).contains(derivedValue)) {
                // could be a leaf-list
                return true;
            } else {
                boolean returnValue = validateDerivedOrSelf(schemaRegistry, derivedSchemaNode, false, targetValue,
                        derivedValue);
                if (returnValue) {
                    return returnValue;
                }
            }
        }
        return false;
    }

    private boolean isBaseIdentity(String currentIdentity, String expectedBaseIdentity) {
        return currentIdentity.equals(expectedBaseIdentity);
    }

    private boolean checkIfIdentity(SchemaRegistry schemaRegistry, IdentitySchemaNode baseIdentitySchemaNode, String
            expectedBaseIdentity, boolean canBeBaseIdentity, Object targetValue) {
        boolean returnValue = false;
        boolean baseIdentityFound = false;
        String baseIdentityValue = buildIdentityStringValue(schemaRegistry, baseIdentitySchemaNode).toString();
        baseIdentityFound = isBaseIdentity(baseIdentityValue, expectedBaseIdentity);

        if (baseIdentityFound) {
            // expected base identity is found. Check only against this and its derivatives
            returnValue = validateDerivedOrSelf(schemaRegistry, baseIdentitySchemaNode, canBeBaseIdentity,
                    targetValue, baseIdentityValue);
        }

        if (!returnValue && !baseIdentityFound) {
            // if the base is still not found, go ahead. 
            for (IdentitySchemaNode derivedSchemaNode : baseIdentitySchemaNode.getDerivedIdentities()) {
                returnValue = checkIfIdentity(schemaRegistry, derivedSchemaNode, expectedBaseIdentity,
                        canBeBaseIdentity, targetValue);
                if (returnValue) {
                    break;
                }
            }
        }
        return returnValue;
    }

    private StringBuilder buildIdentityStringValue(SchemaRegistry schemaRegistry, IdentitySchemaNode
            identitySchemaNode) {
        StringBuilder iValue = new StringBuilder();
        iValue.append(schemaRegistry.getPrefix(identitySchemaNode.getQName().getNamespace().toString()))
                .append(DataStoreValidationUtil.COLON)
                .append(identitySchemaNode.getQName().getLocalName());
        return iValue;
    }


}
