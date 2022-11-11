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

public class StoredLeaves {
    private Map<ModelNodeId, Map<QName, Element>> m_leaves = new HashMap<>();
    private Map<ModelNodeId, List<QName>> m_leavesWithNoResponse = new HashMap<>();

    public Map<QName, Element> get(ModelNodeId key) {
        Map<QName, Element> leavesAtMN = m_leaves.get(key);
        if (leavesAtMN == null) {
            leavesAtMN = new HashMap<>();
            m_leaves.put(key, leavesAtMN);
        }
        return leavesAtMN;
    }

    public void put(ModelNodeId key, QName qName, Element value) {
        Map<QName, Element> leavesAtMN = m_leaves.get(key);
        if (leavesAtMN == null) {
            leavesAtMN = new HashMap<>();
            m_leaves.put(key, leavesAtMN);
        }
        leavesAtMN.put(qName,value);
    }

    private void populateLeavesNotInCache(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributesNotInCache, ModelNodeId mnId, QName leaf) {
        Pair<List<QName>, List<FilterNode>> attrAtMN = attributesNotInCache.get(mnId);
        if(attrAtMN == null){
            attrAtMN = new Pair<>(new ArrayList<>(), new ArrayList<>());
            attributesNotInCache.put(mnId, attrAtMN);
        }
        if (!isFilterLeafWithEmptyResponse(mnId, leaf)) {
            attrAtMN.getFirst().add(leaf);
        }
    }

    protected boolean isFilterLeafWithEmptyResponse(ModelNodeId mnId, QName leaf) {
        return m_leavesWithNoResponse.get(mnId)!=null ? m_leavesWithNoResponse.get(mnId).contains(leaf) : false;
    }

    public void fillDataWithCacheHitAndMiss(Map<ModelNodeId, List<Element>> returnValues,
                                            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateResponseNotInCache,
                                            ModelNodeId mnId, List<QName> leaves) {
        Map<QName, Element> storeLeavesAtMn = get(mnId);
        for(QName leaf : leaves){
            Element storedLeaf = storeLeavesAtMn.get(leaf);
            if(storedLeaf != null){
                populateReturnValues(returnValues, mnId, storedLeaf);
            } else {
                populateLeavesNotInCache(stateResponseNotInCache, mnId, leaf);
            }
        }
    }

    private void populateReturnValues(Map<ModelNodeId, List<Element>> returnValues, ModelNodeId mnId, Element storedLeaf) {
        List<Element> valuesToReturn = returnValues.get(mnId);
        if(valuesToReturn == null){
            valuesToReturn = new ArrayList<>();
            returnValues.put(mnId, valuesToReturn);
        }
        valuesToReturn.add(storedLeaf);
    }

    public void addFilterLeavesWithNoResponse(ModelNodeId mnId, List<QName> leavesWithNoResponse) {
        if (!leavesWithNoResponse.isEmpty()) {
            List<QName> qNames = m_leavesWithNoResponse.get(mnId);
            if(qNames == null){
                qNames = new ArrayList<>();
                m_leavesWithNoResponse.put(mnId, qNames);
            }
            qNames.addAll(leavesWithNoResponse);
        }
    }
}
