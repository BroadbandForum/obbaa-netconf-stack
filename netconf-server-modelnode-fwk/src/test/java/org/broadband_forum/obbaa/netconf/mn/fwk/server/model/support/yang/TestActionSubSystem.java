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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ActionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ActionUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;

public class TestActionSubSystem extends AbstractSubSystem {
    private static final String NS = "urn:example:test-action";

    private SchemaRegistry m_registry;

    public TestActionSubSystem(SchemaRegistry schemaRegistry) {
        m_registry = schemaRegistry;
    }

    @Override
    public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
            List<FilterNode>>> mapAttributes) throws GetAttributeException {
        return new HashMap<>();
    }

    @Override
    public List<Element> executeAction(ActionRequest actionRequest) throws ActionException {
        List<Element> result = new ArrayList<>();
        DataSchemaNode node = m_registry.getDataSchemaNode(actionRequest.getActionTargetpath());
        Set<ActionDefinition> actionDefs = ActionUtils.retrieveActionDefinitionForSchemaNode(actionRequest
                .getActionTargetpath(), m_registry);
        for (ActionDefinition actionDef : actionDefs) {

            if (actionDef.getQName().equals(actionRequest.getActionQName())) {
                Document document = DocumentUtils.createDocument();

                Element outputElement = document.createElementNS(NS, "test:reset-finished-at");
                outputElement.setTextContent("2014-07-29T13:42:00Z");
                result.add(outputElement);
            }
        }
        return result;
    }
}
