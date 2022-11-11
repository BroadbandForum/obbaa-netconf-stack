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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.SchemaMountUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;

public class DSValidationContext {

    private Map<SchemaPath, Map<String,ModelNode>> m_modelNodeCache = new HashMap<SchemaPath, Map<String,ModelNode>>();
    private DSValidationNodeIndex m_nodesToDelete = new DSValidationNodeIndex();
    private Set<SchemaPath> m_schemaPathToDelete = new HashSet<SchemaPath>();
    private DSValidationNodeIndex m_nodesToCreate = new DSValidationNodeIndex();
    private Set<SchemaPath> m_schemaPathToCreate = new HashSet<SchemaPath>();
    private Set<ModelNodeId> m_deletedLists = new HashSet<>();
    private Map<SchemaPath, Map<String, Object>> m_whenDefaultValues = new HashMap<>();
    private DSValidationNodeIndex m_nodesToMerge = new DSValidationNodeIndex();
    private Map<String, EditContainmentNode> m_deletedChangeNodes = new HashMap<String, EditContainmentNode>();
    private Map<String, List<QName>> m_deletedChangeAttributes = new HashMap<String, List<QName>>();
    private Map<String, Map<Pair<String,String>, List<ModelNode>>> m_rootNodesWithSchemaRegistry = new HashMap<>();
    private Document m_document = null;
    private Map<SchemaPath, String> m_mountPathModelNodeIdCache = new HashMap<>();
    private ValidatedChildren m_validatedChilds = new ValidatedChildren();
    private ValidatedAugment m_validatedAugment = new ValidatedAugment();
    private ValidatedConstraints m_validatedConstraints = new ValidatedConstraints();
    private ValidatedUniqueConstraints m_validatedUniqueConstraints = new ValidatedUniqueConstraints();
    private RetrievedParentModelNodes m_retrievedParentModelNodes = new RetrievedParentModelNodes();
    private ValidatedDeletedList m_validatedDeletedList = new ValidatedDeletedList();
    private Map<SchemaPath,ModelNodeId> m_schemaPathModelNodeIdCache = new HashMap<>();
    private SchemaNode m_validationNode;
    private DataSchemaNode m_childOfChoiceCase;
    private Pair<DataSchemaNode, ModelNode> m_augmentOrUsesChildNode;
    private boolean m_impactValidation;
    private RootModelNodeAggregator m_rootNodeAggregator;
    private boolean m_isMandatoryNodeCheck;
    private ModelNodeId m_currentModelNodeId;
    private Module m_module;
    private String m_defaultEditConfigOperation = EditConfigDefaultOperations.MERGE;



    private Map<SchemaPath, Map<ModelNodeId, EditContainmentNode>> changeNodeParentModelNodeIdMap = new HashMap<>();
    private AugmentationSchemaNode m_augmentationSchemaNodeForWhen;


    public Integer nodesToCreateCount() {
        return m_nodesToCreate.size();
    }

    public void addToModelNodeCache(SchemaPath schemaPath, ModelNode modelNode) {
        Map<String, ModelNode> valueMap = m_modelNodeCache.get(schemaPath);
        if (valueMap == null) {
            valueMap = new HashMap<String,ModelNode>(200);
            m_modelNodeCache.put(schemaPath, valueMap);
        }
        
        valueMap.put(modelNode.getModelNodeId().xPathString(), modelNode);
    }

    private void recordMapping(DSValidationNodeIndex parentMap, ModelNode modelNode, QName child) {
        Collection<QName> childQNames = parentMap.get(modelNode);
        if (childQNames == null) {
            childQNames = new TreeSet<QName>();
            parentMap.put(modelNode, childQNames);
        }
        childQNames.add(child);
        
    }
    
    public void addToDeletedList(ModelNodeId listId) {
        m_deletedLists.add(listId);
    }
    
    public Set<ModelNodeId> getDeletedListIds(){
        return m_deletedLists;
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
    
    public void recordForCreate(SchemaPath schemaPath) {
        m_schemaPathToCreate.add(schemaPath);
    }
    
    public void recordDefaultValue(SchemaPath schemaPath, ModelNode modelNode, Object object) {
        Map<String, Object> valueMap = m_whenDefaultValues.get(schemaPath);
        if (valueMap == null) {
            valueMap = new HashMap<String,Object>();
            m_whenDefaultValues.put(schemaPath, valueMap);
        }

        valueMap.put(modelNode.getModelNodeId().xPathString(), object);
    }
    
    public void recordDeletedChangeNode(String string, EditContainmentNode editContainmentNode){
        m_deletedChangeNodes.put(string, editContainmentNode);
    }
    
    public void recordDeletedChangeAttribute(String string, QName qname) {
    	List<QName> qnames = m_deletedChangeAttributes.get(string);
    	if(qnames == null){
    		qnames = new ArrayList<QName>();
    		m_deletedChangeAttributes.put(string, qnames);
    	}
    	qnames.add(qname);
    }
    
    public Map<String, ModelNode> getModelNodes(SchemaPath schemaPath) {
        return m_modelNodeCache.get(schemaPath);
    }
    
    public Map<SchemaPath,Map<String, Object>> getDefaultValues() {
        return m_whenDefaultValues;
    }
    
    public Collection<SchemaPath> getSchemaPathsToCreate(){
        return m_schemaPathToCreate;
    }
    
    public void clearSchemaPathsToCreate(){
        m_schemaPathToCreate.clear();
    }
    
    public Document getDocument() {
        if(m_document == null){
            m_document = DocumentUtils.createDocument();
        }
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
    
    public List<QName> getDeletedAttribute(String modelNodePathString) {
        return m_deletedChangeAttributes.get(modelNodePathString);
    }

    public ValidatedChildren getValidatedChilds() {
        return m_validatedChilds;
    }

    public ValidatedAugment getValidatedAugment() {
        return m_validatedAugment;
    }

    public void setValidatedAugment(ValidatedAugment validatedAugment) {
        m_validatedAugment = validatedAugment;
    }

    public RetrievedParentModelNodes getRetrievedParentModelNodes() {
        return m_retrievedParentModelNodes;
    }

    public ValidatedDeletedList getValidatedIsDeletedList() {
        return m_validatedDeletedList;
    }

    public SchemaNode getValidationNode() {
        return m_validationNode;
    }

    public void setValidationNode(SchemaNode validationNode) {
        this.m_validationNode = validationNode;
    }

	public void cacheRootNodesWithSchemaRegistry(String namespace, String localName, Collection<ModelNode> rootNodes,
			String registryName) {
		Pair<String, String> qnameKey = new Pair<>(namespace, localName);
		Map<Pair<String, String>, List<ModelNode>> rootNodeMap = new HashMap<>();
		if (m_rootNodesWithSchemaRegistry.containsKey(registryName)) {
			rootNodeMap = m_rootNodesWithSchemaRegistry.get(registryName);
		}
		rootNodeMap.put(qnameKey, new ArrayList<ModelNode>(rootNodes));
		m_rootNodesWithSchemaRegistry.put(registryName, rootNodeMap);
	}

    public DSValidationNodeIndex getDeleteList() {
        return m_nodesToDelete;
    }

    public DSValidationNodeIndex getCreateList() {
        return m_nodesToCreate;
    }

    public DSValidationNodeIndex getMergeList() {
        return m_nodesToMerge;
    }


    public DataSchemaNode getChildOfChoiceCase() {
        return m_childOfChoiceCase;
    }


    public void setChildOfChoiceCase(DataSchemaNode childOfChoiceCase) {
        this.m_childOfChoiceCase = childOfChoiceCase;
    }
    
    public Pair<DataSchemaNode, ModelNode> getAugmentOrUsesChildNode(){
    	return m_augmentOrUsesChildNode;
    }
    
    public void setAugmentOrUsesChildNode(Pair<DataSchemaNode, ModelNode> augmentUsesNodeMap){
    	this.m_augmentOrUsesChildNode = augmentUsesNodeMap;
    }
    
    public boolean getImpactValidation() {
		return m_impactValidation;
	}

	public void setImpactValidation(boolean impactValidation) {
		this.m_impactValidation = impactValidation;
	}

    public String getDefaultEditConfigOperation() {
        return m_defaultEditConfigOperation;
    }

    public void setDefaultEditConfigOperation(String defaultEditConfigOperation) {
        this.m_defaultEditConfigOperation = defaultEditConfigOperation;
    }

    public void setRootNodeAggregator(RootModelNodeAggregator rootNodeAggregator) {
        this.m_rootNodeAggregator = rootNodeAggregator;
    }
    
    public void setCurrentModelNodeId(ModelNodeId id){
    	m_currentModelNodeId = id;
    }
    
    // Returns the model node which is at present getting validated. 
    private ModelNode getCurrentModelNode(Collection<ModelNode> mountNodes){
    	if ( mountNodes.size() == 1){
    		return mountNodes.iterator().next();
    	} else {
    		if ( m_currentModelNodeId != null){
    			for ( ModelNode mountNode : mountNodes){
        			ModelNodeId mountNodeId = mountNode.getModelNodeId();
        			if ( m_currentModelNodeId.beginsWith(mountNodeId)){
        				return mountNode;
        			}
        		}    			
    		}
    	}
    	return null;
    }

    public List<ModelNode> getRootNodesOfType(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        QName qName = schemaPath.getLastComponent();
        String namespace = qName.getNamespace().toString();
        String localName = qName.getLocalName();
        Pair<String, String> qnameKey = new Pair<>(namespace, ModelNodeDynaBeanFactory.getModelNodeAttributeName(localName));
        Map<Pair<String,String>, List<ModelNode>> rootNodes = m_rootNodesWithSchemaRegistry.get(schemaRegistry.getName());
        if (rootNodes != null && rootNodes.containsKey(qnameKey)) {
            return rootNodes.get(qnameKey);
        }
        List<ModelNode> nodes = m_rootNodeAggregator.getModuleRootFromHelpers(namespace, localName);
        if ( nodes.isEmpty() && schemaRegistry.getMountPath() != null){
            SchemaPath mountPath = schemaRegistry.getMountPath();
            Map<String, ModelNode> parentModelNodes = m_modelNodeCache.get(mountPath);
            if ( parentModelNodes != null && !parentModelNodes.isEmpty()){
                RootModelNodeAggregator rootModelNodeAggregator = SchemaMountUtil.getMountedRootModelNodeAggregator(schemaRegistry);
                ModelNode parentModelNodeId = getCurrentModelNode(parentModelNodes.values());
                nodes = rootModelNodeAggregator.getModuleRootFromHelpers(namespace, localName, parentModelNodeId);                
            }
        }
        cacheRootNodesWithSchemaRegistry(namespace, localName, nodes, schemaRegistry.getName());
        return nodes;
    }

    public Set<SchemaPath> getRootNodeSchemaPaths() {
        return m_rootNodeAggregator.getRootNodeSchemaPaths();
    }

    public <T> T withRootNodeAggregator(RootNodeAggregatorTemplate<T> template, RootModelNodeAggregator
            rootModelNodeAggregator) {
        RootModelNodeAggregator old = m_rootNodeAggregator;
        try {
            DataStoreValidationUtil.setRootModelNodeAggregatorInCache(rootModelNodeAggregator);
            setRootNodeAggregator(rootModelNodeAggregator);
            return template.execute();
        } finally {
            setRootNodeAggregator(old);
        }
    }

    public ValidatedConstraints getValidatedConstraints() {
        return m_validatedConstraints;
    }

    public ValidatedUniqueConstraints getValidatedUniqueConstraints() {
        return m_validatedUniqueConstraints;
    }

    public void setAugmentationSchemaNodeForWhen(AugmentationSchemaNode augmentationSchemaNodeForWhen) {
        m_augmentationSchemaNodeForWhen = augmentationSchemaNodeForWhen;
    }

    public AugmentationSchemaNode getAugmentationSchemaNodeForWhen() {
        return m_augmentationSchemaNodeForWhen;
    }

    public interface RootNodeAggregatorTemplate<T> {
        @SuppressWarnings("hiding")
        <T> T execute();
    }
    
    //Only for UT
    public void setModeNodeCache(SchemaPath schemaPath, Map<String, ModelNode> valueMap){
    	m_modelNodeCache.put(schemaPath, valueMap);
    }
    
	public Map<SchemaPath, String> getMountPathModelNodeIdCache() {
		return m_mountPathModelNodeIdCache;
	}

	public void addToMountPathModelNodeIdCache(SchemaPath schemaPath, ModelNodeId modelNodeId) {
		m_mountPathModelNodeIdCache.put(schemaPath, modelNodeId.xPathString());
	}
	
    public boolean isThisMandatoryNodesCheck() {
		return m_isMandatoryNodeCheck;
	}

	public void setAsMandatoryNodesCheck(boolean isMandatoryNodeCheck) {
		this.m_isMandatoryNodeCheck = isMandatoryNodeCheck;
	}
	
	public void setNodeConstraintAssociatedModule(Module module) {
	    m_module = module;
	}
	
	public void clearNodeConstraintAssociatedModule() {
	    m_module = null;
	}
	
	public Module getNodeConstraintAssociatedModule() {
	    return m_module;
	}

    public void addFailedListModelNodeIdCache(SchemaPath path, ModelNodeId modelNodeId){
        m_schemaPathModelNodeIdCache.put(path,modelNodeId);
    }

    public ModelNodeId getFailedListModelNodeIdCache(SchemaPath schemaPath){
        return m_schemaPathModelNodeIdCache.get(schemaPath);
    }

    public void clearFailedListModelNodeIdCache(SchemaNode schemanode){
        if(schemanode != null) {
            m_schemaPathModelNodeIdCache.remove(schemanode.getPath());
        }
    }

    public Map<ModelNodeId, EditContainmentNode> getParentModelNodeIdByChangeNodePath(SchemaPath changeNodePath) {
        return changeNodeParentModelNodeIdMap.get(changeNodePath);
    }

    public void setChangeNodeParentModelNodeIdMap(SchemaPath changeNodePath, EditContainmentNode editTreeNode) {
        Map<ModelNodeId, EditContainmentNode> parentModelNodeIds = changeNodeParentModelNodeIdMap.get(changeNodePath);
        if (parentModelNodeIds == null) {
            parentModelNodeIds = new HashMap<ModelNodeId, EditContainmentNode>();
            changeNodeParentModelNodeIdMap.put(changeNodePath, parentModelNodeIds);
        }
        parentModelNodeIds.put(editTreeNode.getParent().getModelNodeId(), editTreeNode);
    }

}
