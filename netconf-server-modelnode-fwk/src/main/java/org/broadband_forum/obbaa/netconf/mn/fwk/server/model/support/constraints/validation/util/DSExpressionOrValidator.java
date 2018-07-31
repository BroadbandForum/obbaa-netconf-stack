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

import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

/**
 * Evaluates a xpath if it has "or" in it
 */
public class DSExpressionOrValidator extends DSExpressionValidator {

    public DSExpressionOrValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                   Map<Class<?>, DSExpressionValidator> validators) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        setValidators(validators);
    }

    @Override
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode,
                              String leafRefValue, QName leafQName) {
        if (DataStoreValidationUtil.isCoreOperation(xPathCondition)) {
            return getExpressionValue(contextBean, currentContextNode, leafRefValue, leafQName, (CoreOperation)
                    xPathCondition);
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
        }
    }


    @Override
    protected Object getExpressionValue(DynaBean contextBean, Object currentContextNode, String leafRefValue, QName
            leafQName, CoreOperation operation) {
        if (DataStoreValidationUtil.isCoreOperationOr(operation)) {
            boolean returnValue = false;

            for (Expression childExpression : operation.getArguments()) {
                Object value = evaluate(childExpression, contextBean, currentContextNode, leafRefValue, leafQName);
                if (value != null && Boolean.parseBoolean(value.toString())) {
                    // if there are more than one condition to evaluate and
                    // even if one is "true" the result is true
                    returnValue = true;
                    break;
                }

            }
            return returnValue;

        } else {
            return super.getExpressionValue(contextBean, currentContextNode, leafRefValue, leafQName, operation);
        }

    }

}
