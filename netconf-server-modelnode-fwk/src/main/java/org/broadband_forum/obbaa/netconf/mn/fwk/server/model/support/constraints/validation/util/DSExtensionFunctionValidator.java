package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.COLON;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.isReadable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

/**
 * Validates only Expression of type ExtensionFunction.
 *
 */
public class DSExtensionFunctionValidator extends DSExpressionValidator {
    static final String DERIVED_FROM = "derived-from";
    static final String DERIVED_FROM_OR_SELF = "derived-from-or-self";
    static final String ENUM_VALUE = "enum-value";
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
            QName leafQName) {
        if (DataStoreValidationUtil.isExtensionFunction(xPathCondition)) {
            logDebug("Evaluating for extension function {}", xPathCondition);
            Object returnValue = null;
            if (((ExtensionFunction) xPathCondition).getFunctionName().getName().equals(DERIVED_FROM)
                    || ((ExtensionFunction) xPathCondition).getFunctionName().getName().equals(DERIVED_FROM_OR_SELF)) {
                returnValue = evaluateDerivedFrom(this, contextBean, currentContextNode, xPathCondition);
            } else if (((ExtensionFunction) xPathCondition).getFunctionName().getName().equals(ENUM_VALUE)) {
                returnValue = evaluateEnumValue(this, contextBean, currentContextNode, xPathCondition, leafRefValue,
                        leafQName);
            } else {
                LOGGER.warn("Function : {} is not supported yet",
                        ((ExtensionFunction) xPathCondition).getFunctionName().getName());
            }
            return validateLeafRef(returnValue, leafRefValue);
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
        }
    }

    private Object evaluateDerivedFrom(DSExpressionValidator validator, DynaBean contextBean, Object contextNode, Expression xPath) {
        Expression expression = xPath;
        boolean selfValue = false;
        if (DataStoreValidationUtil.isExtensionFunction(expression) && ((ExtensionFunction)expression).getFunctionName().getName().contains(DERIVED_FROM)) {
            if (((ExtensionFunction)expression).getFunctionName().getName().equals(DERIVED_FROM_OR_SELF)) {
                selfValue = true;
            }
            
            Expression[] predicates = ((ExtensionFunction) expression).getArguments();
            // 1st parameter will be xPath/node-set second will be the identityRef value
            Expression expressionToEvaluate = predicates[0];
            String expectedBaseIdentity = (String) predicates[1].compute(null);
            Object value = validator.checkForFunctionsAndEvaluate(expressionToEvaluate, contextBean, contextNode, null, null);
            
            ModelNode modelNode = null;
            DynaBean newContextBean = contextBean;
            while (!isReadable(newContextBean, ModelNodeWithAttributes.MODEL_NODE) && isReadable(newContextBean, ModelNodeWithAttributes.PARENT)) {
                newContextBean = (DynaBean) newContextBean.get(ModelNodeWithAttributes.PARENT);
            }

            if (isReadable(newContextBean, ModelNodeWithAttributes.MODEL_NODE)) {
                // get the modelNode and schemaRegistry. 
                // build the childpath from the current ModelNode schemaPath
                modelNode = (ModelNode) newContextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                SchemaRegistry schemaRegistry = modelNode.getSchemaRegistry();
                SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
                Expression evalExp = expressionToEvaluate;
                
                if (evalExp instanceof LocationPath) {
                    SchemaPath childPath = null;
                    if (isReadable(contextBean, ModelNodeWithAttributes.LEAF_VALUE) && ((LocationPath)evalExp).getSteps()[0].getAxis() == Compiler.AXIS_PARENT){
                        childPath = DataStoreValidationUtil.getXPathSchemaPath(schemaRegistry, schemaPath, DataStoreValidationUtil.excludeFirstStep((LocationPath) evalExp));
                    } else {
                        childPath = DataStoreValidationUtil.getXPathSchemaPath(schemaRegistry, schemaPath, (LocationPath) evalExp);
                    }
                    DataSchemaNode childSchemaNode = schemaRegistry.getDataSchemaNode(childPath);
                    
                    // child SchemaNode has to be a leafRef with type IdentityRef. Else derive-from() wont work. 
                    if (childSchemaNode instanceof LeafSchemaNode && ((LeafSchemaNode)childSchemaNode).getType() instanceof  IdentityrefTypeDefinition) {
                       IdentityrefTypeDefinition refDefinition = (IdentityrefTypeDefinition) ((LeafSchemaNode)childSchemaNode).getType();
                       IdentityrefTypeDefinition baseDefition = refDefinition; 
                       
                       // Expected identity is the second argument in the function derived-from(arg1,arg2)
                       // the second argument can have prefix. In case it does not have, use the prefix of QName of leaf. 
                       if (expectedBaseIdentity.split(COLON).length != 2) {
                           //indicates the prefix is local
                           expectedBaseIdentity = schemaRegistry.getPrefix(refDefinition.getQName().getNamespace().toString()) + COLON + expectedBaseIdentity;
                       }
                       
                       // Now verify against each identity and its derivative if that is what we have.
                       for (IdentitySchemaNode identitySchemaNode:baseDefition.getIdentities()) {
                           boolean returnValue = checkIfIdentity(schemaRegistry, identitySchemaNode, expectedBaseIdentity, selfValue, value);
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

    
    private Object evaluateEnumValue(DSExpressionValidator validator, DynaBean contextBean, Object contextNode,
            Expression xPath, String leafRefValue, QName leafQName) {
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
                        leafRefValue, leafQName);
            } else {
                value = validator.checkForFunctionsAndEvaluate(expressionToEvaluate, contextBean, contextNode,
                        leafRefValue, leafQName);
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
        if (targetValue instanceof Collection) {
            // This indicates a node-set
            for (Object object : ((Collection) targetValue)) {
                Object enumValue = fetchEnumValue(enumTypeDefinition, object);
                if (enumValue != null) {
                    enumValues.add(enumValue);
                }
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

    /** found the targetIdentity. Now evaluate with the actual value **/
    @SuppressWarnings("rawtypes")
    private boolean validateDerivedOrSelf(SchemaRegistry schemaRegistry, IdentitySchemaNode identitySchemaNode, boolean canBeBaseIdentity, Object targetValue, String iValue) {

        if (canBeBaseIdentity) {
            // if it is derived-from-or-self()? 
            if (iValue.equals(targetValue)) {
                return true;
            } else if (targetValue instanceof Collection && ((Collection)targetValue).contains(iValue)) {
                return true;
            }
        }

        for (IdentitySchemaNode derivedSchemaNode:identitySchemaNode.getDerivedIdentities()) {
            String derivedValue = buildIdentityStringValue(schemaRegistry, derivedSchemaNode).toString();
            if (derivedValue.equals(targetValue)) {
                return true;
            } else if (targetValue instanceof Collection && ((Collection)targetValue).contains(derivedValue)) {
                // could be a leaf-list
                return true;
            } else {
                boolean returnValue = validateDerivedOrSelf(schemaRegistry, derivedSchemaNode, false, targetValue, derivedValue);
                if (returnValue){
                    return returnValue;
                }
            }
        }
        return false;
    }
    
    private boolean isBaseIdentity(String currentIdentity, String expectedBaseIdentity) {
        return currentIdentity.equals(expectedBaseIdentity);
    }
    
    private boolean checkIfIdentity(SchemaRegistry schemaRegistry, IdentitySchemaNode baseIdentitySchemaNode, String expectedBaseIdentity, boolean canBeBaseIdentity, Object targetValue){
        boolean returnValue = false;
        boolean baseIdentityFound = false;
        String baseIdentityValue = buildIdentityStringValue(schemaRegistry, baseIdentitySchemaNode).toString();
        baseIdentityFound = isBaseIdentity(baseIdentityValue,expectedBaseIdentity); 
        
        if (baseIdentityFound){
            // expected base identity is found. Check only against this and its derivatives
            returnValue =  validateDerivedOrSelf(schemaRegistry, baseIdentitySchemaNode, canBeBaseIdentity, targetValue, baseIdentityValue);
        }
        
        if (!returnValue && !baseIdentityFound) {
            // if the base is still not found, go ahead. 
            for (IdentitySchemaNode derivedSchemaNode:baseIdentitySchemaNode.getDerivedIdentities()) {
                returnValue = checkIfIdentity(schemaRegistry, derivedSchemaNode, expectedBaseIdentity, canBeBaseIdentity, targetValue);
                if (returnValue) {
                    break;
                }
            }
        }
        return returnValue;
    }

    private StringBuilder buildIdentityStringValue(SchemaRegistry schemaRegistry, IdentitySchemaNode identitySchemaNode) {
        StringBuilder iValue = new StringBuilder();
        iValue.append(schemaRegistry.getPrefix(identitySchemaNode.getQName().getNamespace().toString()))
        .append(COLON)
        .append(identitySchemaNode.getQName().getLocalName());
        return iValue;
    }


}
