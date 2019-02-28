package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.opendaylight.yangtools.yang.common.QName;

public class StateMetaData {
	
	private List<QName> m_stateAttributes;
	private List<FilterNode> m_stateSubtrees;
	
	public StateMetaData(List<QName> stateAttributes, List<FilterNode> stateSubtrees) {
		this.m_stateAttributes = stateAttributes;
		this.m_stateSubtrees = stateSubtrees;
	}
	public List<QName> getStateAttributes() {
		return m_stateAttributes;
	}
	public void setStateAttributes(List<QName> stateAttributes) {
		this.m_stateAttributes = stateAttributes;
	}
	public List<FilterNode> getStateSubtrees() {
		return m_stateSubtrees;
	}
	public void setStateSubtrees(List<FilterNode> stateSubtrees) {
		this.m_stateSubtrees = stateSubtrees;
	}

}
