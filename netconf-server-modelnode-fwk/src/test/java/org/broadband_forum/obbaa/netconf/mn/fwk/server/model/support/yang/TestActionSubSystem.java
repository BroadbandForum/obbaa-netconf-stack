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

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ActionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ActionUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TestActionSubSystem extends AbstractSubSystem {
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(TestActionSubSystem.class, LogAppNames.NETCONF_STACK);
	private static final String NS = "urn:example:test-action";
	
	private SchemaRegistry m_registry;
	public TestActionSubSystem(SchemaRegistry schemaRegistry){
		m_registry = schemaRegistry;
	}

	@Override
	public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
		return new HashMap<>();
	}
	
	@Override
	public List<Element> executeAction(ActionRequest actionRequest) throws ActionException{
		List<Element> result = new ArrayList<>();
		Set<ActionDefinition> actionDefs = ActionUtils.retrieveActionDefinitionForSchemaNode(actionRequest.getActionTargetpath(), m_registry);
		for(ActionDefinition actionDef : actionDefs) {
			if (actionDef.getQName().equals(actionRequest.getActionQName())) {
				Document document = DocumentUtils.createDocument();
				if (actionDef.getPath() != null
						&& actionDef.getPath().getLastComponent() != null
						&& actionDef.getPath().getLastComponent().getLocalName() != null
						&& actionDef.getPath().getLastComponent().getLocalName().equals("get-device-list")) {
					try {
						Element element = DocumentUtils.getDocumentElement(
								"<test:device-list xmlns:test=\"urn:example:test-action\">" +
								"		<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>" +
								"		<test:hardware-type xmlns:test=\"urn:example:test-action\">123</test:hardware-type>" +
								"</test:device-list>");
						result.add(element);
					} catch (Exception e) {
						LOGGER.error("Exception shouldn't have occured.");
					}
				} else if (actionDef.getPath() != null
						&& actionDef.getPath().getLastComponent() != null
						&& actionDef.getPath().getLastComponent().getLocalName() != null
						&& actionDef.getPath().getLastComponent().getLocalName().equals("get-device")) {
					try {
						Element element = DocumentUtils.getDocumentElement("<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>");
						result.add(element);
					} catch (Exception e) {
						LOGGER.error("Exception shouldn't have occured.");
					}
				} else if (actionDef.getPath() != null
						&& actionDef.getPath().getLastComponent() != null
						&& actionDef.getPath().getLastComponent().getLocalName() != null
						&& actionDef.getPath().getLastComponent().getLocalName().equals("get-device-leaf-list")) {
					try {
//						Element element = DocumentUtils.getDocumentElement(
//								"<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>"
//										+  "<test:device-id xmlns:test=\"urn:example:test-action\">device2</test:device-id>");
//						result.add(element);
						Element element = DocumentUtils.getDocumentElement("<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>");
						result.add(element);
						element = DocumentUtils.getDocumentElement("<test:device-id xmlns:test=\"urn:example:test-action\">device2</test:device-id>");
						result.add(element);
					} catch (Exception e) {
						LOGGER.error("Exception shouldn't have occured.");
					}
				} else if (actionDef.getOutput() != null && !actionDef.getOutput().getChildNodes().isEmpty()) {
					Element outputElement = document.createElementNS(NS, "test:reset-finished-at");
					outputElement.setTextContent("2014-07-29T13:42:00Z");
					result.add(outputElement);
				} else {
					// ok response if output is not defined in action
					Element outputElement = document.createElement("ok");
					result.add(outputElement);
				}
			}
		}
		return result;
	}

	@Override
	protected boolean byPassAuthorization() {
		return true;
	}
}
