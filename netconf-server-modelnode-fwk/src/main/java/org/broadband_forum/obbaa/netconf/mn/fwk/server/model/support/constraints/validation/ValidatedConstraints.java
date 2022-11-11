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

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class ValidatedConstraints {
    HashMap<Pair<String, DataSchemaNode>, Boolean> m_validatedConstraints = new HashMap<>();

    public boolean isValidated(ModelNode parentNode, DataSchemaNode childSN) {
        Pair<String, DataSchemaNode> key = getKey(parentNode, childSN);
        return m_validatedConstraints.containsKey(key);
    }

    private Pair<String, DataSchemaNode> getKey(ModelNode parentNode, DataSchemaNode childSN) {
        return new Pair<String, DataSchemaNode>(parentNode.getModelNodeId().xPathString(), childSN);
    }

    public void storeResult(ModelNode parentNode, DataSchemaNode childSN, boolean result) {
        Pair<String, DataSchemaNode> key = getKey(parentNode, childSN);
        m_validatedConstraints.put(key, result);
    }

    public boolean getValidationResult(ModelNode parentNode, DataSchemaNode childSN) {
        Pair<String, DataSchemaNode> key = getKey(parentNode, childSN);
        return m_validatedConstraints.get(key);
    }
}
