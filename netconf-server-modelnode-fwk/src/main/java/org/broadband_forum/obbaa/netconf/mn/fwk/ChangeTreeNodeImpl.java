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

package org.broadband_forum.obbaa.netconf.mn.fwk;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttributeWithInsertOp;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ChangeTreeNodeImpl implements WritableChangeTreeNode {
    private SchemaRegistry m_schemaRegistry;
    private final ModelNodeId m_nodeId;
    private final DataSchemaNode m_nodeType;
    private ChangeType m_changeType = ChangeType.none;
    private Map<ModelNodeId, ChangeTreeNode> m_childNodes = new LinkedHashMap<>();
    private final Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfType;
    private final Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfTypeWithinSchemaMount;
    private ChangeTreeNode m_parent;
    private ConfigLeafAttribute m_previousVal;
    private ConfigLeafAttribute m_currentVal;
    private InsertOperation m_insertOperation;
    private final Map<AttributeIndex, Set<ChangeTreeNode>> m_attributeIndex;
    private LinkedHashSet<ConfigLeafAttributeWithInsertOp> m_previousLeafLists;
    private LinkedHashSet<ConfigLeafAttributeWithInsertOp> m_currentLeafLists;
    private Set<SchemaPath> m_changedSchemaPaths;
    private final Map<String, Object> m_context;
    private String m_editOperation;
    private boolean m_isImplied;
    private boolean m_isMountPoint = false;
    private EditChangeSource m_changeSource;
    private boolean m_isPassword = false;

    public ChangeTreeNodeImpl(SchemaRegistry schemaRegistry, ChangeTreeNode parent, ModelNodeId nodeId, DataSchemaNode nodeType, Map<SchemaPath,
            Map<ModelNodeId, ChangeTreeNode>> nodesOfType, Map<AttributeIndex, Set<ChangeTreeNode>> attributeIndex, Set<SchemaPath> changedNodeSPs, Map<String, Object> contextMap, Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> nodesOfTypeWithinSchemaMount) {
        m_schemaRegistry = schemaRegistry;
        m_nodeId = nodeId;
        m_nodeType = nodeType;
        //every node in the tree share the index
        if(isMountPoint(nodeType)){
            m_nodesOfTypeWithinSchemaMount = new HashMap<>();
            m_isMountPoint = true;
        }else{
            m_nodesOfTypeWithinSchemaMount = nodesOfTypeWithinSchemaMount;
        }
        m_nodesOfType = nodesOfType;
        m_parent = parent;
        m_attributeIndex = attributeIndex;
        m_changedSchemaPaths = changedNodeSPs;
        m_context = contextMap;
    }

    @Override
    public boolean isMountPoint() {
        return m_isMountPoint;
    }

    @Override
    public void setMountPoint(boolean mountPoint) {
        m_isMountPoint = mountPoint;
    }

    @Override
    public ChangeType getChange() {
        return m_changeType;
    }

    @Override
    public SchemaNode getType() {
        return m_nodeType;
    }

    @Override
    public ModelNodeId getId() {
        return m_nodeId;
    }

    @Override
    public Map<ModelNodeId, ChangeTreeNode> getChildren() {
        return m_childNodes;
    }

    @Override
    public ChangeTreeNode getParent() {
        return m_parent;
    }

    @Override
    public Collection<ChangeTreeNode> getNodesOfType(SchemaPath type) {
        return getNodesOfTypeInternal(type).values();
    }

    @Override
    public Collection<ChangeTreeNode> getNodesOfTypeWithinSchemaMount(SchemaPath type) {
        return getNodesOfTypeInternalWithinSchemaMount(type).values();
    }

    @Override
    public Collection<ChangeTreeNode> getNodesOfType(AttributeIndex index) {
        Collection<ChangeTreeNode> indexedAttributes = getAttributeIndexInternal(index);
        return indexedAttributes;
    }

    @Override
    public Element currentSubtreeXml() {
        if (ChangeType.delete.equals(m_changeType)) {
            return null;
        }
        try {
            Document doc = DocumentUtils.getNewDocument();
            return currentSubtreeXml(doc);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Element currentSubtreeXml(Document doc) {
        if (ChangeType.delete.equals(m_changeType)) {
            return null;
        }
        QName qname = getType().getQName();

        Element element = null;
        if (getType() instanceof DataNodeContainer) {
            element = doc.createElementNS(qname.getNamespace().toString(), qname.getLocalName());
            for (ChangeTreeNode child : m_childNodes.values()) {
                Element childNode = child.currentSubtreeXml(doc);
                if (childNode != null) {
                    element.appendChild(childNode);
                }
            }
        } else if (m_currentVal != null) {
            element = (Element) doc.importNode(m_currentVal.getDOMValue(), true);
        }
        return element;
    }

    @Override
    public String currentValue() {
        return returnStringValue(m_currentVal);
    }

    @Override
    public String previousValue() {
        return returnStringValue(m_previousVal);
    }

    public String returnStringValue(ConfigLeafAttribute previousVal) {
        if (previousVal == null) {
            return null;
        }
        return previousVal.getStringValue();
    }

    @Override
    public Element previousSubtreeXml() {
        try {
            Document doc = DocumentUtils.getNewDocument();
            return previousSubtreeXml(doc);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Element previousSubtreeXml(Document doc) {
        if (ChangeType.create.equals(m_changeType)) {
            return null;
        }
        QName qname = getType().getQName();
        Element element = null;

        if (getType() instanceof DataNodeContainer) {
            element = doc.createElementNS(qname.getNamespace().toString(), qname.getLocalName());
            for (ChangeTreeNode child : m_childNodes.values()) {
                Element childNode = child.previousSubtreeXml(doc);
                if (childNode != null) {
                    element.appendChild(childNode);
                }
            }
        } else if (m_previousVal != null) {
            element = (Element) doc.importNode(m_previousVal.getDOMValue(), true);
        }
        return element;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        print(0, sb);
        return sb.toString();
    }

    @Override
    public void print(Integer indent, StringBuilder sb) {
        String indentStr = "";
        for (int i = 0; i < indent; i++) {
            indentStr += " ";
        }
        sb.append(indentStr);
        if (getType() instanceof DataNodeContainer) {
            sb.append(getType().getQName().getLocalName()).append("[").append(getId().xPathString()).append("] -> ").append(getChange().toString()).append("\n");
            indent++;
        } else {
            if (getType() instanceof LeafListSchemaNode) {
                printLeafLists(indentStr, sb);
            } else {
                printLeaf(sb);
            }
        }

        for (ChangeTreeNode child : m_childNodes.values()) {
            child.print(indent, sb);
        }

        if (getType() instanceof DataNodeContainer) {
            indent--;
        }

    }

    public void printLeaf(StringBuilder sb) {
        sb.append(getType().getQName().getLocalName()).append(" -> ").append(getChange().toString()).append(" { ");
        sb.append("previousVal = '").append(previousValue());
        sb.append("', currentVal = '").append(currentValue()).append("' }").append("\n");
    }

    public void printLeafLists(String indentStr, StringBuilder sb) {
        Set<String> previousLeafListValues = new LinkedHashSet<>();
        if (m_previousLeafLists != null) {
            for (ConfigLeafAttributeWithInsertOp leafListAttribute : m_previousLeafLists) {
                previousLeafListValues.add(leafListAttribute.getLeafListAttribute().getStringValue());
            }
        }
        Set<String> currentLeafListValues = new LinkedHashSet<>();
        if (m_currentLeafLists != null) {
            for (ConfigLeafAttributeWithInsertOp leafListAttribute : m_currentLeafLists) {
                currentLeafListValues.add(leafListAttribute.getLeafListAttribute().getStringValue());
            }
        }
        Set<String> unionLeafListValues = new LinkedHashSet<>();
        unionLeafListValues.addAll(previousLeafListValues);
        unionLeafListValues.addAll(currentLeafListValues);
        int index = 0;
        for (String leafListValue : unionLeafListValues) {
            if (index > 0) {
                sb.append(indentStr);
            }
            sb.append(getType().getQName().getLocalName()).append(" -> ").append(getChange().toString()).append(" { ");
            String previousVal = previousLeafListValues.contains(leafListValue) ? leafListValue : null;
            String currentVal = currentLeafListValues.contains(leafListValue) ? leafListValue : null;
            sb.append("previousVal = '").append(previousVal);
            sb.append("', currentVal = '").append(currentVal).append("' }").append("\n");
            index++;
        }
    }

    @Override
    public boolean hasChanged() {
        return !ChangeType.none.equals(getChange());
    }

    @Override
    public void appendChildNode(WritableChangeTreeNode childNode) {
        m_childNodes.put(childNode.getId(), childNode);
        updateChange(childNode);
        updateIndex(childNode);
    }

    private void updateChange(WritableChangeTreeNode childNode) {
        if (!hasChanged() && childNode.hasChanged()) {
            setChangeType(ChangeType.modify);
        }
    }

    private boolean isMountPoint(DataSchemaNode nodeType) {
        return (nodeType != null && AnvExtensions.MOUNT_POINT.isExtensionIn(nodeType));
    }

    public void updateIndex(WritableChangeTreeNode childNode) {
        SchemaPath nodeType = childNode.getType().getPath();
        Map<ModelNodeId, ChangeTreeNode> nodesOfType = getNodesOfTypeInternal(nodeType);
        nodesOfType.put(childNode.getId(), childNode);
        Map<ModelNodeId, ChangeTreeNode> nodesOfTypeWithInSchemaMountInternal = getNodesOfTypeInternalWithinSchemaMount(nodeType);
        nodesOfTypeWithInSchemaMountInternal.put(childNode.getId(),childNode);
        if(childNode.hasChanged()){
            m_changedSchemaPaths.add(childNode.getType().getPath());
        }
        if (childNode.isLeafNode() && childNode.getParent() != null) {
            if (childNode.getType() instanceof LeafListSchemaNode) {
                if (childNode.getCurrentLeafListValue() != null) {
                    for (ConfigLeafAttributeWithInsertOp leafListEntry : childNode.getCurrentLeafListValue()) {
                        Set<ChangeTreeNode> index = getAttributeIndexInternal(new AttributeIndex(childNode.getType().getPath(), leafListEntry.getLeafListAttribute()));
                        index.add(childNode.getParent());
                    }
                }
                if (childNode.hasChanged() && childNode.getPreviousLeafListValue() != null) {
                    for (ConfigLeafAttributeWithInsertOp leafListEntry : childNode.getPreviousLeafListValue()) {
                        Set<ChangeTreeNode> index = getAttributeIndexInternal(new AttributeIndex(childNode.getType().getPath(), leafListEntry.getLeafListAttribute()));
                        index.add(childNode.getParent());
                    }
                }
            } else {
                if (childNode.getCurrentLeafValue() != null) {
                    Set<ChangeTreeNode> index = getAttributeIndexInternal(new AttributeIndex(childNode.getType().getPath(), childNode.getCurrentLeafValue()));
                    index.add(childNode.getParent());
                }
                if (childNode.hasChanged() && childNode.getPreviousLeafValue() != null) {
                    Set<ChangeTreeNode> index = getAttributeIndexInternal(new AttributeIndex(childNode.getType().getPath(), childNode.getPreviousLeafValue()));
                    index.add(childNode.getParent());
                }
            }

        }
    }

    private Map<ModelNodeId, ChangeTreeNode> getNodesOfTypeInternalWithinSchemaMount(SchemaPath nodeType) {
        Map<ModelNodeId, ChangeTreeNode> nodesOfType = m_nodesOfTypeWithinSchemaMount.get(nodeType);
        if (nodesOfType == null) {
            nodesOfType = new LinkedHashMap<>();
            m_nodesOfTypeWithinSchemaMount.put(nodeType, nodesOfType);
        }
        return nodesOfType;
    }

    public Map<ModelNodeId, ChangeTreeNode> getNodesOfTypeInternal(SchemaPath nodeType) {
        Map<ModelNodeId, ChangeTreeNode> nodesOfType = m_nodesOfType.get(nodeType);
        if (nodesOfType == null) {
            nodesOfType = new LinkedHashMap<>();
            m_nodesOfType.put(nodeType, nodesOfType);
        }
        return nodesOfType;
    }

    private Set<ChangeTreeNode> getAttributeIndexInternal(AttributeIndex index) {
        Set<ChangeTreeNode> indexedNodes = m_attributeIndex.get(index);
        if (indexedNodes == null) {
            indexedNodes = new HashSet<>();
            m_attributeIndex.put(index, indexedNodes);
        }
        return indexedNodes;
    }

    @Override
    public void setChangeType(ChangeType changeType) {
        m_changeType = changeType;
    }

    @Override
    public void setPreviousValue(ConfigLeafAttribute leaf) {
        m_previousVal = leaf;
        assessChange();
    }

    @Override
    public void setCurrentValue(ConfigLeafAttribute leaf) {
        m_currentVal = leaf;
        assessChange();
    }

    private void assessChange() {
        if (isLeafNode()) {
            if (getType() instanceof LeafListSchemaNode) {
                if (m_currentLeafLists != null && m_previousLeafLists != null) {
                    if (!m_currentLeafLists.equals(m_previousLeafLists)) {
                        m_changeType = ChangeType.modify;
                    } else {
                        m_changeType = ChangeType.none;
                    }
                } else if (m_currentLeafLists == null && m_previousLeafLists == null) {
                    m_changeType = ChangeType.none;
                } else if (m_previousLeafLists == null) {
                    m_changeType = ChangeType.create;
                } else {
                    m_changeType = ChangeType.delete;
                }
            } else {
                if (m_currentVal != null && m_previousVal != null) {
                    if (!m_currentVal.equals(m_previousVal)) {
                        m_changeType = ChangeType.modify;
                    } else {
                        m_changeType = ChangeType.none;
                    }
                } else if (m_currentVal == null && m_previousVal == null) {
                    m_changeType = ChangeType.none;
                } else if (m_previousVal == null) {
                    m_changeType = ChangeType.create;
                } else {
                    m_changeType = ChangeType.delete;
                }
            }
        }
    }

    @Override
    public boolean isLeafNode() {
        return !(getType() instanceof DataNodeContainer);
    }

    @Override
    public Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> getNodesIndex() {
        return m_nodesOfType;
    }

    @Override
    public Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> getNodesOfTypeIndexWithinSchemaMount() {
        return m_nodesOfTypeWithinSchemaMount;
    }

    @Override
    public Map<AttributeIndex, Set<ChangeTreeNode>> getAttributeIndex() {
        return m_attributeIndex;
    }

    @Override
    public void setPreviousValue(LinkedHashSet<ConfigLeafAttributeWithInsertOp> leafListValues) {
        m_previousLeafLists = leafListValues;
        assessChange();
    }

    @Override
    public void setCurrentValue(LinkedHashSet<ConfigLeafAttributeWithInsertOp> leafListValues) {
        m_currentLeafLists = leafListValues;
        assessChange();
    }

    @Override
    public LinkedHashSet<ConfigLeafAttributeWithInsertOp> getCurrentLeafListValue() {
        return m_currentLeafLists;
    }

    @Override
    public LinkedHashSet<ConfigLeafAttributeWithInsertOp> getPreviousLeafListValue() {
        return m_previousLeafLists;
    }

    @Override
    public void setInsertOperation(InsertOperation insertOperation) {
        m_insertOperation = insertOperation;
    }

    @Override
    public InsertOperation getInsertOperation() {
        return m_insertOperation;
    }

    @Override
    public ConfigLeafAttribute getCurrentLeafValue() {
        return m_currentVal;
    }

    @Override
    public ConfigLeafAttribute getPreviousLeafValue() {
        return m_previousVal;
    }

    @Override
    public Object getContextValue(String key) {
    	return m_context.get(key);
    }

    @Override
    public Map<String, Object> getContextMap() {
    	return m_context;
    }

    @Override
    public void addContextValue(String key, Object value) {
    	m_context.put(key, value);
    }

    @Override
    public String getEditOperation() {
        return m_editOperation;
    }

    @Override
    public void setEditOperation(String editOperation) {
        m_editOperation = editOperation;
    }

    @Override
    public void setImplied(boolean isImplied) {
        m_isImplied = isImplied;
    }

    @Override
    public void setEditChangeSource(EditChangeSource changeSource) {
        m_changeSource = changeSource;
    }

    public EditChangeSource getChangeSource() {
        return m_changeSource;
    }

    @Override
    public boolean isImplied() {
        return m_isImplied;
    }

    @Override
    public SchemaRegistry getSchemaRegistry() {
        return m_schemaRegistry;
    }

    @Override
    public String toString() {
        return "ChangeTreeNodeImpl{" +
                "m_nodeId=" + m_nodeId +
                ", m_changeType=" + m_changeType +
                '}';
    }

    @Override
    public Set<SchemaPath> getChangedNodeTypes() {
        return m_changedSchemaPaths;
    }
}
