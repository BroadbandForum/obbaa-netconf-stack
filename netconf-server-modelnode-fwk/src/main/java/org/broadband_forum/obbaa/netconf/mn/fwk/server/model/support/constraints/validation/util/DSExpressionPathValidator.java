package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;

public class DSExpressionPathValidator extends DSExpressionValidator {
	
    public DSExpressionPathValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
            Map<Class<?>, DSExpressionValidator> validators) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        setValidators(validators);
    }	
    	
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode, String leafRefValue,
            QName leafQName) {
        if (DataStoreValidationUtil.isExpressionPath(xPathCondition)) {
        	ExpressionPath path = (ExpressionPath)xPathCondition;
        	ExtensionFunction function = (ExtensionFunction) path.getExpression();
        	if (function.getFunctionName().getName().contains(DataStoreValidationUtil.CURRENT_FUNCTION)) {
        		Object value = evaluteCurrentOnExpressionPath(path, contextBean, leafQName);
        		return validateLeafRef(value, leafRefValue);
        	}
        	
        	throw new ValidationException(String.format("Extension function not supported in expression path %s",xPathCondition));
        	
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
        }
    }
 	

}
