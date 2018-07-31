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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service;

import java.util.Set;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidation;

public class DSConstraintValidation implements DSValidation {
    private final DSExpressionValidator m_expValidator;

    public DSConstraintValidation(DSExpressionValidator expValidator) {
        m_expValidator = expValidator;
    }

    @Override
    public Boolean evaluate(ModelNode parentNode, DataSchemaNode child) {
        boolean returnValue = true;
        ConstraintDefinition definition = child.getConstraints();
        if (definition != null) {
            Set<MustDefinition> definitions = definition.getMustConstraints();
            if (definitions != null && !definitions.isEmpty()) {
                for (MustDefinition must : definitions) {
                    m_expValidator.validateMust(must, child, parentNode);
                }
            } else if (definition.getWhenCondition() != null) {
                // we cannot have both must and when. If must fails, we throw an error. We dont tolerate.
                Expression when = JXPathUtils.getExpression(definition.getWhenCondition().toString());
                returnValue = m_expValidator.validateWhen(when, child, parentNode);
            }
        }
        return returnValue;
    }

}
