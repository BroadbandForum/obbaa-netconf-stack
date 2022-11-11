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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;


import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ReferringNodes {
    private final Map<SchemaPath, Set<ReferringNode>> m_referringNodes;
    private final SchemaPath m_refferedNodeSP;

    public ReferringNodes(SchemaPath refferedNodeSP) {
        this(refferedNodeSP, new HashMap<>());
    }

    public ReferringNodes(SchemaPath refferedNodeSP, Map<SchemaPath, Set<ReferringNode>> referringNodes) {
        m_refferedNodeSP = refferedNodeSP;
        m_referringNodes = referringNodes;
    }

    public void put(SchemaPath referencedSchemaPath, ReferringNode expression) {
        Set<ReferringNode> referringNodes = m_referringNodes.get(referencedSchemaPath);
        if(referringNodes == null){
            referringNodes = new HashSet<>();
            m_referringNodes.put(referencedSchemaPath, referringNodes);
        }
        referringNodes.add(expression);
    }

    public void putAll(ReferringNodes impactNodes) {
        impactNodes.getReferringNodes().forEach((schemaPath, referringNodes) ->
                m_referringNodes.merge(schemaPath, referringNodes, (list1, list2) -> {
                   HashSet<ReferringNode> referringNodeList = new HashSet<>();
                   referringNodeList.addAll(list1);
                   referringNodeList.addAll(list2);
                   return referringNodeList;})
                );
    }

    public void putAll(Map<SchemaPath, Set<ReferringNode>> referringNodesMap) {
        referringNodesMap.forEach((schemaPath, referringNodes) ->
                m_referringNodes.merge(schemaPath, referringNodes, (list1, list2) -> {
                    HashSet<ReferringNode> referringNodeList = new HashSet<>();
                    referringNodeList.addAll(list1);
                    referringNodeList.addAll(list2);
                    return referringNodeList;})
        );
    }

    public Map<SchemaPath, Set<ReferringNode>> getReferringNodes() {
        return m_referringNodes;
    }

    public Collection<SchemaPath> keySet() {
        return m_referringNodes.keySet();
    }

    public Iterable<Map.Entry<SchemaPath, Set<ReferringNode>>> entrySet() {
        return m_referringNodes.entrySet();
    }

    public void put(ReferringNode referringNode) {
        Set<ReferringNode> referringNodes = m_referringNodes.get(referringNode.getReferringSP());
        if(referringNodes == null){
            referringNodes = new HashSet<>();
            m_referringNodes.put(referringNode.getReferringSP(), referringNodes);
        }
        referringNodes.add(referringNode);
    }

    public boolean isEmpty() {
        return m_referringNodes.isEmpty();
    }

    public boolean containsKey(SchemaPath referringNodeSP) {
        return m_referringNodes.containsKey(referringNodeSP);
    }

    public Set<ReferringNode> get(SchemaPath referringNodeSP) {
        Set<ReferringNode> referringNodes = m_referringNodes.get(referringNodeSP);
        if(referringNodes == null) {
            return new HashSet<>();
        }
        return referringNodes;
    }

    public int size() {
        return m_referringNodes.size();
    }

    public Collection<Set<ReferringNode>> values() {
        return m_referringNodes.values();
    }

    @Override
    public String toString() {
        return "ReferringNodes{" +
                "m_referringNodes=" + m_referringNodes +
                ", m_refferedNodeSP=" + m_refferedNodeSP +
                '}';
    }

    public SchemaPath getRefferedNodeSP() {
        return m_refferedNodeSP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferringNodes that = (ReferringNodes) o;
        return Objects.equals(m_refferedNodeSP, that.m_refferedNodeSP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_refferedNodeSP);
    }
}
