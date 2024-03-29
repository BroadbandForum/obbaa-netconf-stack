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

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class ValidatedUniqueConstraints {
    Set<Pair<String, DataSchemaNode>> m_validatedUniqueConstraints = new HashSet<>();

    public boolean isValidated(ModelNode parentNode, DataSchemaNode childSN) {
        Pair<String, DataSchemaNode> key = getKey(parentNode, childSN);
        return m_validatedUniqueConstraints.contains(key);
    }

    private Pair<String, DataSchemaNode> getKey(ModelNode parentNode, DataSchemaNode childSN) {
        return new Pair<>(parentNode.getModelNodeId().xPathString(), childSN);
    }

    public void markAsValidated(ModelNode parentNode, DataSchemaNode childSN) {
        Pair<String, DataSchemaNode> key = getKey(parentNode, childSN);
        m_validatedUniqueConstraints.add(key);
    }
}
