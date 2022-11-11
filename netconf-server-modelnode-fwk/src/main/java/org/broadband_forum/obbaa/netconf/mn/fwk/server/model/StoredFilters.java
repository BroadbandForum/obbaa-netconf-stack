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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

public class StoredFilters {

    private Map<ModelNodeId, Map<FilterNode, List<Element>>> m_filteredNodes = new HashMap<>();
    private Map<ModelNodeId, List<FilterNode>> m_filterNodeWithNoResponse = new HashMap<>();

    public Map<FilterNode, List<Element>> get(ModelNodeId key) {
        Map<FilterNode, List<Element>> leavesAtMN = m_filteredNodes.get(key);
        if (leavesAtMN == null) {
            leavesAtMN = new HashMap<>();
            m_filteredNodes.put(key, leavesAtMN);
        }
        return leavesAtMN;
    }

    private void populateFiltersNotInCache(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateResponseNotInCache, ModelNodeId mnId, FilterNode filterNode) {
        Pair<List<QName>, List<FilterNode>> filtersAtMN = stateResponseNotInCache.get(mnId);
        if(filtersAtMN == null){
            filtersAtMN = new Pair<>(new ArrayList<>(), new ArrayList<>());
            stateResponseNotInCache.put(mnId, filtersAtMN);
        }
        if (!isFilterNodeWithEmptyResponse(mnId, filterNode)) {
            filtersAtMN.getSecond().add(filterNode);
        }

    }

    protected boolean isFilterNodeWithEmptyResponse(ModelNodeId mnId, FilterNode filterNode) {
        return m_filterNodeWithNoResponse.get(mnId)!=null ? m_filterNodeWithNoResponse.get(mnId).contains(filterNode) : false;
    }

    public void put(ModelNodeId mnId, FilterNode filterNode, Element result) {
        Map<FilterNode, List<Element>> leavesAtMN = m_filteredNodes.get(mnId);
        if (leavesAtMN == null) {
            leavesAtMN = new HashMap<>();
            m_filteredNodes.put(mnId, leavesAtMN);
        }
        List<Element> values = leavesAtMN.get(filterNode);
        if(values == null){
            values = new ArrayList<>();
            leavesAtMN.put(filterNode, values);
        }
        values.add(result);
    }

    public void fillDataWithCacheHitAndMiss(Map<ModelNodeId, List<Element>> returnValues,
                                            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateResponseNotInCache,
                                            ModelNodeId mnId, List<FilterNode> filterNodes) {
        Map<FilterNode, List<Element>> storedFiltersAtMN = get(mnId);
        for(FilterNode filterNode : filterNodes){
            List<Element> storedFilter = storedFiltersAtMN.get(filterNode);
            if(storedFilter != null && !storedFilter.isEmpty()){
                populateReturnValues(returnValues, mnId, storedFilter);
            } else {
                populateFiltersNotInCache(stateResponseNotInCache, mnId, filterNode);
            }
        }
    }

    private void populateReturnValues(Map<ModelNodeId, List<Element>> returnValues, ModelNodeId mnId, List<Element> storedFilters) {
        List<Element> valuesToReturn = returnValues.get(mnId);
        if(valuesToReturn == null){
            valuesToReturn = new ArrayList<>();
            returnValues.put(mnId, valuesToReturn);
        }
        valuesToReturn.addAll(storedFilters);
    }

    public void addFilterNodesWithNoResponse(ModelNodeId mnId, List<FilterNode> filterNodesWithNoResponse) {
        if (!filterNodesWithNoResponse.isEmpty()) {
            List<FilterNode> filterNodes = m_filterNodeWithNoResponse.get(mnId);
            if(filterNodes == null){
                filterNodes = new ArrayList<>();
                m_filterNodeWithNoResponse.put(mnId, filterNodes);
            }
            filterNodes.addAll(filterNodesWithNoResponse);
        }
    }
}
