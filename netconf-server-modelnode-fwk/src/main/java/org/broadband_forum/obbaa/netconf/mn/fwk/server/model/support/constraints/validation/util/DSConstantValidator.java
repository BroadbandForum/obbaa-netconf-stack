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
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;

public class DSConstantValidator extends DSExpressionValidator {
    public DSConstantValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry registry,
            Map<Class<?>, DSExpressionValidator> validators) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        this.m_subSystemRegistry = registry;
        setValidators(validators);
    }

    @Override
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode, String leafRefValue,
                              QName leafQName, DSValidationContext validationContext) {
        if (DataStoreValidationUtil.isConstant(xPathCondition)) {
            logDebug("Evaluating for Constant {}", xPathCondition);
            Object value = ((Constant) xPathCondition).compute(null);
            return validateLeafRef(value, leafRefValue);
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName, validationContext);
        }

    }

}
