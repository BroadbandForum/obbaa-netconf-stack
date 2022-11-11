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

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.WhenConditionAware;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;

public class DSConstraintValidation extends CachedConstraintValidator implements DSValidation {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DSConstraintValidation.class, LogAppNames.NETCONF_STACK);

    private final DSExpressionValidator m_expValidator;
    
    public DSConstraintValidation(DSExpressionValidator expValidator) {
        super("DSConstraintValidation#validationNeeded");
        m_expValidator = expValidator;
    }

    @Override
    protected ValidationResult evaluateInternal(ModelNode parentNode, DataSchemaNode child, DSValidationContext validationContext) {
    	SchemaRegistry schemaRegistry = parentNode.getSchemaRegistry();
        boolean validationResult = true;
        boolean requiresValidation = false;
        Collection<MustDefinition> definitions = null;
        RevisionAwareXPath whenCondition = null;
        if (child instanceof MustConstraintAware) {
            definitions = ((MustConstraintAware) child).getMustConstraints();
        }
        if (child instanceof WhenConditionAware) {
            Optional<RevisionAwareXPath> optWhenCondition = child.getWhenCondition();
            if (optWhenCondition.isPresent()) {
                whenCondition = optWhenCondition.get();
            }
        }
        if (definitions != null && !definitions.isEmpty()) {
            requiresValidation = true;
            for (MustDefinition must: definitions) {
                if(!schemaRegistry.isSkipValidationBySchemaPathWithConstraintXpath(child.getPath(), must.getXpath().getOriginalString())) {
                    validationResult = m_expValidator.validateMust(must, child, parentNode, validationContext);
                    if (! validationResult){
                        break;
                    }
                }
            }
        }

        if (whenCondition != null && validationResult) {
            requiresValidation = true;
            if(!schemaRegistry.isSkipValidationBySchemaPathWithConstraintXpath(child.getPath(), whenCondition.getOriginalString())) {
                //If must fails, we throw an error. We dont tolerate.But if must passes ,
                //then we also need to check for when condition before returning final boolean validationResult.
                Expression when = JXPathUtils.getExpression(whenCondition.getOriginalString());
                validationResult = m_expValidator.validateWhen(when, child, parentNode,
                        validationContext.getImpactValidation(), validationContext);
            }
        }
        return new ValidationResult(requiresValidation, validationResult);
    }
}
