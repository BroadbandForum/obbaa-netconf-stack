package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ActionUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ActionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

public class TestActionSubSystem extends AbstractSubSystem {
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
				if (actionDef.getOutput() != null && !actionDef.getOutput().getChildNodes().isEmpty()) {
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
}
