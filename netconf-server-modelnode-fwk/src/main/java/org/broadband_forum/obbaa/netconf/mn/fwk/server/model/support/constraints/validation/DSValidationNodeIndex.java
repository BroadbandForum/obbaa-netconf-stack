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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DSValidationNodeIndex {
    private Map<ModelNode, Collection<QName>> m_index = new HashMap<>();
    private Map<SchemaPath, Pair<Integer, Set<QName>>> m_typeIndex = new HashMap<>();

    public DSValidationNodeIndex(DSValidationNodeIndex copy) {
        m_index = new HashMap<>(copy.getIndex());
        m_typeIndex = new HashMap<>(copy.getTypeIndex());
    }

    private Map<SchemaPath, Pair<Integer, Set<QName>>> getTypeIndex() {
        return m_typeIndex;
    }

    public DSValidationNodeIndex() {
    }

    private Map<ModelNode, Collection<QName>> getIndex() {
        return m_index;
    }

    public void remove(ModelNode modelNode) {
        m_index.remove(modelNode);
        SchemaPath nodeType = modelNode.getModelNodeSchemaPath();
        Pair<Integer, Set<QName>> entryForType = m_typeIndex.get(nodeType);
        if(entryForType != null){
            int count = entryForType.getFirst();
            count --;
            if(count == 0){
                m_typeIndex.remove(nodeType);
            } else {
                entryForType.setFirst(count);
            }
        }
    }

    public Collection<QName> get(ModelNode modelNode) {
        return m_index.get(modelNode);
    }

    public void put(ModelNode modelNode, Collection<QName> childQNames) {
        m_index.put(modelNode, childQNames);
        SchemaPath nodeType = modelNode.getModelNodeSchemaPath();
        Pair<Integer, Set<QName>> entryForType = m_typeIndex.get(nodeType);
        if(entryForType == null){
            entryForType = new Pair<>(0, new HashSet<>());
            m_typeIndex.put(nodeType, entryForType);
        }
        int count = entryForType.getFirst();
        count ++;
        entryForType.setFirst(count);
        entryForType.getSecond().addAll(childQNames);
    }

    public boolean containsKey(ModelNode modelNode) {
        return m_index.containsKey(modelNode);
    }

    public Integer size() {
        return m_index.size();
    }

    public Set<Map.Entry<ModelNode, Collection<QName>>> entrySet() {
        return m_index.entrySet();
    }

    public boolean isEmpty() {
        return m_index.isEmpty();
    }

    public boolean presentInTypeIndex(SchemaPath modelNodeSchemaPath, QName childQname) {
        Pair<Integer, Set<QName>> children = m_typeIndex.get(modelNodeSchemaPath);
        if(children == null){
            return false;
        }
        return children.getSecond().contains(childQname);
    }

    @Override
    public String toString() {
        return "DSValidationNodeIndex [m_index=" + m_index + ", m_typeIndex=" + m_typeIndex + "]";
    }
}
