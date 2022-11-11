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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaSupportVerifierImpl.SUPPORTED_EXTENSION_FUNCTIONS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExtensionFunctionValidator.DERIVED_FROM;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExtensionFunctionValidator.DERIVED_FROM_OR_SELF;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.CURRENT_FUNCTION;

import java.util.Optional;

import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationAnd;
import org.apache.commons.jxpath.ri.compiler.CoreOperationEqual;
import org.apache.commons.jxpath.ri.compiler.CoreOperationNotEqual;
import org.apache.commons.jxpath.ri.compiler.CoreOperationOr;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class ImpactHintsAnalyser {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ImpactHintsAnalyser.class, LogAppNames.NETCONF_STACK);

    public Optional<ValidationHint> getValidationHint(Expression expression) {
        if(canSkipImpactOnCreate(expression)){
            return of(ValidationHint.autoHint(ValidationHint.SKIP_IMPACT_ON_CREATE));
        }
        return empty();
    }

    private boolean canSkipImpactOnCreate(Expression expression) {
        if (expression instanceof CoreOperationEqual
                || expression instanceof CoreOperationOr || expression instanceof CoreOperationAnd ||
                expression instanceof CoreOperationNotEqual) {
            for (Expression argument : ((CoreOperation) expression).getArguments()) {
                if (!canSkipImpactOnCreate(argument)) {
                    return false;
                }
            }
            return true;
        } else if (expression instanceof ExtensionFunction) {
            ExtensionFunction extnFunc = (ExtensionFunction) expression;
            if (SUPPORTED_EXTENSION_FUNCTIONS.contains(extnFunc.getFunctionName().getName())){
                if(extnFunc.getArguments() != null) {
                    for (Expression argument : extnFunc.getArguments()) {
                        if (!canSkipImpactOnCreate(argument)) {
                            return false;
                        }
                    }
                }
            } else {
                //unknown function
                return false;
            }
            return true;
        } else if(expression instanceof LocationPath){
            for (Step step : ((LocationPath) expression).getSteps()) {
                for (Expression predicate : step.getPredicates()) {
                    if (!canSkipImpactOnCreate(predicate)) {
                        return false;
                    }
                }
            }
            return true;
        }else if(expression instanceof ExpressionPath){
            if (!canSkipImpactOnCreate(((ExpressionPath)expression).getExpression())) {
                return false;
            }
            for (Step step : ((ExpressionPath) expression).getSteps()) {
                for (Expression predicate : step.getPredicates()) {
                    if (!canSkipImpactOnCreate(predicate)) {
                        return false;
                    }
                }
            }
            return true;
        } else if(expression instanceof Constant){
            return true;
        }
        LOGGER.debug("Found an expression that cannot be analysed {}", expression);
        return false;
    }
}
