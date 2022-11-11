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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.util.HashMap;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

public class ValidatedAugment {
    HashMap<String, Boolean> m_validatedChildren = new HashMap<>();

    public boolean isValidated(ModelNode parentNode, RevisionAwareXPath revisionAwareXPath) {
        String key = getKey(parentNode, revisionAwareXPath);
        return m_validatedChildren.containsKey(key);
    }

    private String getKey(ModelNode parentNode, RevisionAwareXPath revisionAwareXPath) {
        return parentNode.getModelNodeId().xPathString()+"/"+revisionAwareXPath.getOriginalString();
    }

    public void storeResult(ModelNode parentNode, RevisionAwareXPath revisionAwareXPath, boolean result) {
        String key = getKey(parentNode, revisionAwareXPath);
        m_validatedChildren.put(key, result);
    }

    public boolean getValidationResult(ModelNode parentNode, RevisionAwareXPath revisionAwareXPath) {
        String key = getKey(parentNode, revisionAwareXPath);
        return m_validatedChildren.get(key);
    }
}
