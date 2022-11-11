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

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public abstract class CachedConstraintValidator implements DSValidation {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CachedConstraintValidator.class, LogAppNames.NETCONF_STACK);

    private final String m_cacheKey;

    public CachedConstraintValidator(String cacheKey) {
        m_cacheKey = cacheKey;
    }

    protected abstract ValidationResult evaluateInternal(ModelNode parentNode, DataSchemaNode child, DSValidationContext validationContext);

    @Override
    public Boolean evaluate(ModelNode parentNode, DataSchemaNode child, DSValidationContext validationContext) {
        if(requiresValidation(child)){
            ValidationResult result = evaluateInternal(parentNode, child, validationContext);
            getRequiresValidationCache().put(child, result.validationWasNeeded);
            return result.validationResult;
        } else {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} does not require validation");
            }
            return true;
        }
    }

    private boolean requiresValidation(DataSchemaNode child) {
        Map<DataSchemaNode, Boolean> cache = getRequiresValidationCache();
        Boolean requiresValidation = cache.get(child);
        if(requiresValidation != null){
            return requiresValidation;
        }
        return true;
    }

    private Map<DataSchemaNode, Boolean> getRequiresValidationCache() {
        Map<DataSchemaNode, Boolean> cache = (Map<DataSchemaNode, Boolean>) RequestScope.getCurrentScope().getFromCache(m_cacheKey);
        if(cache == null){
            cache = new HashMap<>();
            RequestScope.getCurrentScope().putInCache(m_cacheKey, cache);
        }
        return cache;
    }

    public class ValidationResult {
        private final boolean validationWasNeeded;
        private final boolean validationResult;

        public ValidationResult(boolean validationWasNeeded, boolean validationResult) {
            this.validationWasNeeded = validationWasNeeded;
            this.validationResult = validationResult;
        }
    }
}
