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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.COLON;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;

public class DerivedFromOrSelfResults {
    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DerivedFromOrSelfResults.class, LogAppNames.NETCONF_STACK);

    Map<IdentitySchemaNode, IdentityResult> m_cache = new HashMap<>();

    public Boolean getResult(SchemaRegistry schemaRegistry, IdentitySchemaNode baseIdentitySchemaNode, String expectedBaseIdentity, boolean canBeBaseIdentity, Object targetValue) {
        IdentityResult identityResult = getIdentityResult(baseIdentitySchemaNode);
        Map<IdentityResult.DerFromOrSelf, Boolean> result = identityResult.getResult(expectedBaseIdentity, targetValue);
        IdentityResult.DerFromOrSelf derFromOrSelf = IdentityResult.DerFromOrSelf.from(canBeBaseIdentity);
        Boolean cachedResult = result.get(derFromOrSelf);
        if(cachedResult == null){
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("Cache miss for {}, {}, {}, {}",baseIdentitySchemaNode, expectedBaseIdentity, canBeBaseIdentity, targetValue);
            }
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
                    returnValue = getResult(schemaRegistry, derivedSchemaNode, expectedBaseIdentity, canBeBaseIdentity, targetValue);
                    if (returnValue) {
                        break;
                    }
                }
            }
            result.put(derFromOrSelf, returnValue);
            return returnValue;
        } else {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("Cache hit for {}, {}, {}, {}",baseIdentitySchemaNode, expectedBaseIdentity, canBeBaseIdentity, targetValue);
            }
        }
        return cachedResult;
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

    private StringBuilder buildIdentityStringValue(SchemaRegistry schemaRegistry, IdentitySchemaNode identitySchemaNode) {
        StringBuilder iValue = new StringBuilder();
        iValue.append(schemaRegistry.getPrefix(identitySchemaNode.getQName().getNamespace().toString()))
                .append(COLON)
                .append(identitySchemaNode.getQName().getLocalName());
        return iValue;
    }

    public IdentityResult getIdentityResult(IdentitySchemaNode baseIdentitySchemaNode) {
        IdentityResult identityResult = m_cache.get(baseIdentitySchemaNode);
        if(identityResult == null){
            identityResult = new IdentityResult();
            m_cache.put(baseIdentitySchemaNode, identityResult);
        }
        return identityResult;
    }

    public void clearCache() {
        m_cache.clear();
    }
}
