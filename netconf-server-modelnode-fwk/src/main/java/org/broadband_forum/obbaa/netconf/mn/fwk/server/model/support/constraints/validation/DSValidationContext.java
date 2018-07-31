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
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class DSValidationContext {

    private Map<SchemaPath, Map<String, ModelNode>> m_modelNodeCache = new HashMap<SchemaPath, Map<String,
            ModelNode>>();
    private Map<ModelNode, Collection<QName>> m_nodesToDelete = new HashMap<ModelNode, Collection<QName>>();
    private Set<SchemaPath> m_schemaPathToDelete = new HashSet<SchemaPath>();
    private Map<ModelNode, Collection<QName>> m_nodesToCreate = new HashMap<ModelNode, Collection<QName>>();
    private Set<SchemaPath> m_schemaPathToCreate = new HashSet<SchemaPath>();
    private Map<SchemaPath, Object> m_whenDefaultValues = new HashMap<SchemaPath, Object>();
    private Map<ModelNode, Collection<QName>> m_nodesToMerge = new HashMap<ModelNode, Collection<QName>>();
    private Map<String, EditContainmentNode> m_deletedChangeNodes = new HashMap<String, EditContainmentNode>();
    private Map<String, QName> m_deletedChangeAttributes = new HashMap<String, QName>();
    private Collection<ModelNode> m_rootNodes;
    private Document m_document = DocumentUtils.createDocument();

    private SchemaNode m_validationNode;
    private DataSchemaNode m_childOfChoiceCase;
    private DataSchemaNode m_augmentChildNode;

    public Integer nodesToCreateCount() {
        return m_nodesToCreate.size();
    }


    public void addToModelNodeCache(SchemaPath schemaPath, ModelNode modelNode) {
        Map<String, ModelNode> valueMap = m_modelNodeCache.get(schemaPath);
        if (valueMap == null) {
            valueMap = new HashMap<String, ModelNode>(200);
            m_modelNodeCache.put(schemaPath, valueMap);
        }

        valueMap.put(modelNode.getModelNodeId().xPathString(), modelNode);
    }

    private void recordMapping(Map<ModelNode, Collection<QName>> parentMap, ModelNode modelNode, QName child) {
        Collection<QName> childQNames = parentMap.get(modelNode);
        if (childQNames == null) {
            childQNames = new TreeSet<QName>();
            parentMap.put(modelNode, childQNames);
        }
        childQNames.add(child);

    }

    public void removeFromDelete(ModelNode modelNode) {
        m_nodesToDelete.remove(modelNode);
    }

    public void removeFromMerge(ModelNode modelNode) {
        m_nodesToMerge.remove(modelNode);
    }

    public void removeFromCreate(ModelNode modelNode) {
        m_nodesToCreate.remove(modelNode);
    }

    public void recordForDelete(ModelNode modelNode, QName child) {
        recordMapping(m_nodesToDelete, modelNode, child);
    }

    public void recordForCreate(ModelNode modelNode, QName child) {
        recordMapping(m_nodesToCreate, modelNode, child);
    }

    public void recordForMerge(ModelNode modelNode, QName child) {
        recordMapping(m_nodesToMerge, modelNode, child);
    }

    public void recordForDelete(SchemaPath schemaPath) {
        m_schemaPathToDelete.add(schemaPath);
    }

    public void recrodForCreate(SchemaPath schemaPath) {
        m_schemaPathToCreate.add(schemaPath);
    }

    public void recordDefaultValue(SchemaPath schemaPath, Object object) {
        m_whenDefaultValues.put(schemaPath, object);
    }

    public void recordDeletedChangeNode(String string, EditContainmentNode editContainmentNode) {
        m_deletedChangeNodes.put(string, editContainmentNode);
    }

    public void recrodDeletedChangeAttribute(String string, QName qname) {
        m_deletedChangeAttributes.put(string, qname);
    }

    public Map<String, ModelNode> getModelNodes(SchemaPath schemaPath) {
        return m_modelNodeCache.get(schemaPath);
    }

    public Map<SchemaPath, Object> getDefaultValues() {
        return m_whenDefaultValues;
    }

    public Collection<SchemaPath> getSchemaPathsToCreate() {
        return m_schemaPathToCreate;
    }

    public Document getDocument() {
        return m_document;
    }

    public Collection<SchemaPath> getSchemaPathsToDelete() {
        return m_schemaPathToDelete;
    }

    public boolean isNodeForCreate(ModelNode modelNode) {
        return m_nodesToCreate.containsKey(modelNode);
    }

    public boolean isNodeForCreate(SchemaPath schemaPath) {
        return m_schemaPathToCreate.contains(schemaPath);
    }

    public boolean isNodeForDelete(SchemaPath schemaPath) {
        return m_schemaPathToDelete.contains(schemaPath);
    }

    public boolean isNodeForCreateOrDelete(SchemaPath schemaPath) {
        return isNodeForCreate(schemaPath) || isNodeForDelete(schemaPath);
    }

    public EditContainmentNode getDeletedNode(String modelNodeIdPathString) {
        return m_deletedChangeNodes.get(modelNodeIdPathString);
    }

    public QName getDeletedAttribute(String modelNodePathString) {
        return m_deletedChangeAttributes.get(modelNodePathString);
    }

    public SchemaNode getValidationNode() {
        return m_validationNode;
    }

    public void setValidationNode(SchemaNode validationNode) {
        this.m_validationNode = validationNode;
    }

    public void cacheRootNodes(Collection<ModelNode> rootNodes) {
        m_rootNodes = rootNodes;
    }

    public Collection<ModelNode> getRootNodes() {
        return m_rootNodes;
    }

    public Map<ModelNode, Collection<QName>> getDeleteList() {
        return m_nodesToDelete;
    }

    public Map<ModelNode, Collection<QName>> getCreateList() {
        return m_nodesToCreate;
    }

    public Map<ModelNode, Collection<QName>> getMergeList() {
        return m_nodesToMerge;
    }


    public DataSchemaNode getChildOfChoiceCase() {
        return m_childOfChoiceCase;
    }


    public void setChildOfChoiceCase(DataSchemaNode childOfChoiceCase) {
        this.m_childOfChoiceCase = childOfChoiceCase;
    }

    public DataSchemaNode getAugmentChildNode() {
        return m_augmentChildNode;
    }

    public void setAugmentChildNode(DataSchemaNode augmentChildNode) {
        this.m_augmentChildNode = augmentChildNode;
    }

}
