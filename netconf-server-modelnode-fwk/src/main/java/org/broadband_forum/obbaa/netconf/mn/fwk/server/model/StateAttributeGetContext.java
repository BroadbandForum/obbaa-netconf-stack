package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.Pair;

public class StateAttributeGetContext {

	private Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> m_subSystemToStateAttributesMap = new HashMap<>();
    private Map<ModelNodeId, List<Element>> m_stateMatchNodes = new HashMap<>();
	
    public StateAttributeGetContext() {
		
	}
    
    public StateAttributeGetContext(Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> subSystemToStateAttributesMap,
			Map<ModelNodeId, List<Element>> stateMachNodes) {
		m_subSystemToStateAttributesMap = subSystemToStateAttributesMap;
		m_stateMatchNodes = stateMachNodes;
	}
    
    public Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> getSubSystems() {
		return m_subSystemToStateAttributesMap;
	}
    
    public void updateSubSystems(SubSystem subSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> resutlMap){
        m_subSystemToStateAttributesMap.put(subSystem, resutlMap);
    }

	public void setSubSystems(Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> subSystems) {
		this.m_subSystemToStateAttributesMap = subSystems;
	}

	public Map<ModelNodeId, List<Element>> getStateMatchNodes() {
		return m_stateMatchNodes;
	}

	public void setStateMatchNodes(Map<ModelNodeId, List<Element>> stateMatchNodes) {
		this.m_stateMatchNodes = stateMatchNodes;
	}
}
