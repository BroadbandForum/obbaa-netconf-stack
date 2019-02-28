package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;

/**
 * Evaluate a xPath which has "and" in it. 
 */

public class DSExpressionAndValidator extends DSExpressionValidator {

    public DSExpressionAndValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry registry,
                                    Map<Class<?>, DSExpressionValidator> validators) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        this.m_subSystemRegistry = registry;
        setValidators(validators);
    }
    
    @Override
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode,
            String leafRefValue, QName leafQName) {
        if (DataStoreValidationUtil.isCoreOperationAnd(xPathCondition)) {
            return getExpressionValue(contextBean, currentContextNode, leafRefValue, leafQName, (CoreOperation) xPathCondition);
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
        }
    }

    @Override
    protected Object getExpressionValue(DynaBean contextBean, Object currentContextNode, String leafRefValue, QName leafQName, CoreOperation operation) {
        if (DataStoreValidationUtil.isCoreOperationAnd(operation)) {
            boolean returnValue = true;
            
            for (Expression childExpression:operation.getArguments()) {
                Object value = evaluate(childExpression, contextBean, currentContextNode, leafRefValue, leafQName);
                if (!convertToBoolean(value)) {
                    // if there are more than 1 condition to evluate and even 
                    // if one returns "FALSE". The and is "FALSE"
                    returnValue = false;
                    break;
                }
                
            }
            return returnValue;
            
        } else {
            return super.getExpressionValue(contextBean, currentContextNode, leafRefValue, leafQName, operation);
        }
        
    }

}
