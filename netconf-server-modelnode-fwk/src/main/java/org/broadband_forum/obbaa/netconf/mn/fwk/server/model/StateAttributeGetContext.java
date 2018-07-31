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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.Pair;

public class StateAttributeGetContext {

    private Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> m_subSystemToStateAttributesMap =
            new HashMap<>();
    private Map<ModelNodeId, List<Element>> m_stateMatchNodes = new HashMap<>();

    public StateAttributeGetContext() {

    }

    public StateAttributeGetContext(Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>>
                                            subSystemToStateAttributesMap,
                                    Map<ModelNodeId, List<Element>> stateMachNodes) {
        m_subSystemToStateAttributesMap = subSystemToStateAttributesMap;
        m_stateMatchNodes = stateMachNodes;
    }

    public Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> getSubSystems() {
        return m_subSystemToStateAttributesMap;
    }

    public void updateSubSystems(SubSystem subSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> resutlMap) {
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
