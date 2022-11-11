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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getDirectChildElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class StateAttributeGetContext {

	private Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> m_subSystemToStateAttributesMap = new HashMap<>();

	private StoredStateResponses m_storedStateResponses = new StoredStateResponses();

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

	public StoredLeaves getStoredLeavesForSS(SubSystem subsystem) {
		return m_storedStateResponses.getStoredLeaves(subsystem);
	}

	public StoredFilters getStoredFilterNodesForSS(SubSystem subsystem) {
		return m_storedStateResponses.getStoredFilterNodes(subsystem);
	}

    public void findAndPopulateStoredLeavesForSS(Map<ModelNodeId, List<Element>> returnValues,
                                                 Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateResponseNotInCache,
                                                 Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                                 SubSystem subSystem) {
        StoredLeaves storedLeavesForSS = getStoredLeavesForSS(subSystem);
        for(Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : attributes.entrySet()){
            storedLeavesForSS.fillDataWithCacheHitAndMiss(returnValues, stateResponseNotInCache, entry.getKey(), entry.getValue().getFirst());
        }
    }

    public void findAndPopulateStoredFiltersForSS(Map<ModelNodeId, List<Element>> returnValues,
                                                  Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateResponseNotInCache,
                                                  Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                                  SubSystem subSystem) {
        StoredFilters storedFiltersForSS = getStoredFilterNodesForSS(subSystem);
        for(Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : attributes.entrySet()){
            storedFiltersForSS.fillDataWithCacheHitAndMiss(returnValues, stateResponseNotInCache, entry.getKey(), entry.getValue().getSecond());
        }
    }

    public void cacheStateResponse(Map<ModelNodeId, List<Element>> stateValues, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateResponseNotInCache,
                                   SubSystem subSystem) {
        StoredLeaves storedLeavesForSS = getStoredLeavesForSS(subSystem);
        StoredFilters storedFilterNodesForSS = getStoredFilterNodesForSS(subSystem);

        for(Map.Entry<ModelNodeId, List<Element>> entry : stateValues.entrySet()){
            ModelNodeId mnId = entry.getKey();
            List<QName> leaves = stateResponseNotInCache.get(mnId).getFirst();
            List<FilterNode> filterNodes = stateResponseNotInCache.get(mnId).getSecond();

            List<FilterNode> filterNodesWithNoResponse = new ArrayList<>();
            filterNodesWithNoResponse.addAll(filterNodes);

            List<QName> filterLeavesWithNoResponse = new ArrayList<>();
            filterLeavesWithNoResponse.addAll(leaves);

            mapSubsystemResponseWithFilters(storedLeavesForSS, storedFilterNodesForSS, entry.getValue(), mnId, filterNodes, leaves, filterNodesWithNoResponse, filterLeavesWithNoResponse);

            storedLeavesForSS.addFilterLeavesWithNoResponse(mnId,filterLeavesWithNoResponse);
            storedFilterNodesForSS.addFilterNodesWithNoResponse(mnId,filterNodesWithNoResponse);

        }
    }

    private void mapSubsystemResponseWithFilters(StoredLeaves storedLeavesForSS, StoredFilters storedFilterNodesForSS, List<Element> stateElements, ModelNodeId mnId, List<FilterNode> filterNodes, List<QName> leaves,
                                                 List<FilterNode> filterNodesWithNoResponse, List<QName> filterLeavesWithNoResponse) {
        for(Element result : stateElements){
            for(QName leaf : leaves){
                if(leaf.getNamespace().toString().equals(result.getNamespaceURI()) && leaf.getLocalName().equals(result.getLocalName())){
                    storedLeavesForSS.put(mnId,leaf,result);
                    filterLeavesWithNoResponse.remove(leaf);
                    break;
                }
            }
            for(FilterNode filterNode : filterNodes){
                if(filterNode.getNamespace().equals(result.getNamespaceURI()) && filterNode.getNodeName().equals(result.getLocalName())){
                    if(!filterNode.getMatchNodes().isEmpty()){
                        if (evaluateFilterMatchCondition(filterNode, result)) {
                            storedFilterNodesForSS.put(mnId,filterNode,result);
                            filterNodesWithNoResponse.remove(filterNode);
                            break;
                        }
                    }else{
                        storedFilterNodesForSS.put(mnId,filterNode,result);
                        filterNodesWithNoResponse.remove(filterNode);
                        break;
                    }

                }
            }
        }
    }

    /**
     * Returns true if result matches all the matchNodes of filterNode
     * @param filterNode
     * @param result
     * @return
     */
    private boolean evaluateFilterMatchCondition(FilterNode filterNode, Element result) {
        for (FilterMatchNode matchNode : filterNode.getMatchNodes()) {
            List<Element> matchingChildNodes = getDirectChildElements(result, matchNode.getNodeName(), matchNode.getNamespace());
            boolean matchingNodeFound = false;
            for (Node matchedFilterResponseElement : matchingChildNodes) {
                if (matchedFilterResponseElement != null && matchNode.getFilter().equals(matchedFilterResponseElement.getTextContent())) {
                    matchingNodeFound = true;
                    break;
                }
            }
            if (!matchingNodeFound) {
                return false;
            }
        }
        return true;
    }
}
