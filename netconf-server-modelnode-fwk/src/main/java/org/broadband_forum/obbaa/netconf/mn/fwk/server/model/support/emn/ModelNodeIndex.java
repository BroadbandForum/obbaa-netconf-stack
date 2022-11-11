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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.opendaylight.yangtools.yang.common.QName;

public class ModelNodeIndex {
    private final Map<String, XmlModelNodeImpl> m_nodeIndex;
    private final Map<String, ConfigLeafAttribute> m_attributeIndex;
    private final Map<String, Map<String, List<ConfigLeafAttribute>>> m_attributeTypeIndex;
    private Set<String> m_newNodes = new HashSet<>();

    public ModelNodeIndex() {
        m_nodeIndex = new LinkedHashMap<>();
        m_attributeIndex = new LinkedHashMap<>();
        m_attributeTypeIndex = new LinkedHashMap<>();
    }

    public NodeIndex addToIndex(XmlModelNodeImpl xmlModelNode, boolean newNode) {
        NodeIndex nodeIndex = addToIndex(xmlModelNode, new NodeIndex());
        if(newNode){
            m_newNodes.add(xmlModelNode.getModelNodeId().xPathString(xmlModelNode.getSchemaRegistry(), true, true));
        }
        return nodeIndex;
    }

    public NodeIndex addToIndex(XmlModelNodeImpl xmlModelNode, NodeIndex indices) {
        ModelNodeId nodeId = xmlModelNode.getIndexNodeId();
        String xPathWithoutPrefixes = nodeId.xPathString(xmlModelNode.getSchemaRegistry(), true, true);
        m_nodeIndex.put(xPathWithoutPrefixes, xmlModelNode);
        NodeIndex nodeIndex = buildAttrIndices(xPathWithoutPrefixes, xmlModelNode, indices);
        nodeIndex.addNodeIndex(xPathWithoutPrefixes);
        return nodeIndex;
    }

    public NodeIndex buildAttrIndices(String nodeXPath, XmlModelNodeImpl xmlModelNode, NodeIndex indices) {
        indices.clearAttrIndex();
        ModelNodeId nodeId = xmlModelNode.getIndexNodeId();
        String typeXPath = nodeId.typeXPath(xmlModelNode.getSchemaRegistry());
        Map<QName, ConfigLeafAttribute> attributes = xmlModelNode.getAttributes();
        for (Map.Entry<QName, ConfigLeafAttribute> attrEntry : attributes.entrySet()) {
            String attrXPath = buildAttrIndex(xmlModelNode.getSchemaRegistry(), nodeXPath, attrEntry);
            indices.addAttrIndex(attrXPath, attrEntry.getValue());
            String attrTypeXPath = buildAttrTypeIndex(xmlModelNode.getSchemaRegistry(), typeXPath, attrEntry);
            indices.addAttrTypeIndex(attrXPath, attrTypeXPath);
        }
        return indices;
    }

    private String buildAttrIndex(SchemaRegistry schemaRegistry, String nodeXPath, Map.Entry<QName, ConfigLeafAttribute> attrEntry) {
        String xPathForAttr = attrEntry.getValue().xPathString(schemaRegistry, nodeXPath);
        m_attributeIndex.put(xPathForAttr, attrEntry.getValue());
        return xPathForAttr;
    }

    private String buildAttrTypeIndex(SchemaRegistry schemaRegistry, String parentNodeXPath, Map.Entry<QName, ConfigLeafAttribute> attrEntry) {
        String attrTypeXPath = attrEntry.getValue().xPathString(schemaRegistry, parentNodeXPath);
        Map<String, List<ConfigLeafAttribute>> attrsOfType = getAttrsOfType(attrTypeXPath);
        List<ConfigLeafAttribute> attrsList = attrsOfType.get(attrEntry.getValue().getStringValue());
        if(attrsList == null){
            attrsList = new ArrayList<>();
            attrsOfType.put(attrEntry.getValue().getStringValue(), attrsList);
        }
        attrsList.add(attrEntry.getValue());
        return attrTypeXPath;
    }

    public Map<String, List<ConfigLeafAttribute>> getAttrsOfType(String attrTypeXPath) {
        Map<String, List<ConfigLeafAttribute>> attrsOfType = m_attributeTypeIndex.get(attrTypeXPath);
        if(attrsOfType == null){
            attrsOfType = new HashMap<>();
            m_attributeTypeIndex.put(attrTypeXPath, attrsOfType);
        }
        return attrsOfType;
    }

    public Object getIndexedValue(String xpath) {
        XmlModelNodeImpl node = m_nodeIndex.get(xpath);
        if(node != null){
            return node;
        }
        ConfigLeafAttribute leafAttribute = m_attributeIndex.get(xpath);
        if(leafAttribute != null){
            return leafAttribute;
        }
        return null;
    }

    public void removeNode(XmlModelNodeImpl node) {
        String childIndex = node.getIndexNodeId().xPathString(node.getSchemaRegistry(), true, true);
        XmlModelNodeImpl removedNode = m_nodeIndex.remove(childIndex);
        if(removedNode != null){
            for (IndexedList<ModelNodeId, XmlModelNodeImpl> childList : removedNode.getChildren().values()) {
                for (XmlModelNodeImpl child : childList.list()) {
                    removeNode(child);
                }
            }
            clearAttributeIndices(node.getNodeIndex());
        }

    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nodes {\n");
        for (Map.Entry<String, XmlModelNodeImpl> nodeEntry : m_nodeIndex.entrySet()) {
            sb.append("  ").append(nodeEntry.getKey()).append("\n");
        }
        sb.append("}\n");
        sb.append("Attributes {\n");
        for (Map.Entry<String, ConfigLeafAttribute> attrEntry : m_attributeIndex.entrySet()) {
            sb.append("  ").append(attrEntry.getKey()).append(" --> ").append(attrEntry.getValue().getStringValue()).append("\n");
        }
        sb.append("}\n");

        sb.append("AttributesOfType {\n");
        for (Map.Entry<String, Map<String, List<ConfigLeafAttribute>>> attrEntry : m_attributeTypeIndex.entrySet()) {
            sb.append("  ").append(attrEntry.getKey()).append(" --> {\n");
            for (Map.Entry<String, List<ConfigLeafAttribute>> valueEntry : attrEntry.getValue().entrySet()) {
                List<ConfigLeafAttribute> list = valueEntry.getValue();
                sb.append("    ").append(valueEntry.getKey()).append(" --> [");
                StringJoiner joiner = new StringJoiner(", ");
                for(ConfigLeafAttribute leaf : list){
                    joiner.add(leaf.toString());
                }
                sb.append(joiner.toString()).append("]\n");

            }
            sb.append("  }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public void clearAttributeIndices(NodeIndex nodeIndex) {
        Map<String, ConfigLeafAttribute> attrIndex = nodeIndex.getAttrIndex();
        m_attributeIndex.keySet().removeAll(attrIndex.keySet());
        for (Map.Entry<String, String> attrToAttrTypeEntry : nodeIndex.getAttrToAttrTypeIndex().entrySet()) {
            String attrTypeXPath = attrToAttrTypeEntry.getValue();
            ConfigLeafAttribute leafAttribute = attrIndex.get(attrToAttrTypeEntry.getKey());
            List<ConfigLeafAttribute> attrList = m_attributeTypeIndex.get(attrToAttrTypeEntry.getValue()).get(leafAttribute.getStringValue());
            attrList.remove(0);
            if(attrList.isEmpty()){
                m_attributeTypeIndex.get(attrTypeXPath).remove(leafAttribute.getStringValue());
                if(m_attributeTypeIndex.get(attrTypeXPath).isEmpty()){
                    m_attributeTypeIndex.remove(attrTypeXPath);
                }
            }
        }
        ;

    }

    public boolean isNewNode(String nodeIdXPath) {
        return m_newNodes.contains(nodeIdXPath);
    }
}
