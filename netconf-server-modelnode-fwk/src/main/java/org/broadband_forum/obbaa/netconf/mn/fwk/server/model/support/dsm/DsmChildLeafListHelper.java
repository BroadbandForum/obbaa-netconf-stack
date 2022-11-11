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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EMNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.MNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangConstraintHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.XmlUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;


public class DsmChildLeafListHelper extends YangConstraintHelper implements ChildLeafListHelper {
    protected final ModelNodeDataStoreManager m_modelNodeDSM;
    protected final SchemaRegistry m_schemaRegistry;
    protected final QName m_qName;
    protected final LeafListSchemaNode m_leafListSchemaNode;

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DsmChildLeafListHelper.class, LogAppNames.NETCONF_STACK);

    public DsmChildLeafListHelper(LeafListSchemaNode leafListSchemaNode, QName qname, ModelNodeDataStoreManager modelNodeDSM, SchemaRegistry schemaRegistry) {
        super(leafListSchemaNode);
        m_qName = qname;
        m_leafListSchemaNode = leafListSchemaNode;
        m_modelNodeDSM = modelNodeDSM;
        m_schemaRegistry = schemaRegistry;
    }
    
    @Override
    public Collection<ConfigLeafAttribute> getValue(ModelNode node) throws GetAttributeException {
    	Collection<ConfigLeafAttribute> leafList = XmlUtil.createCollection(m_leafListSchemaNode);
    	Set<ConfigLeafAttribute> existingValues = ((ModelNodeWithAttributes) node).getLeafList(m_qName);
    	if (existingValues != null) {
    		leafList.addAll(existingValues);
    	}
        return leafList;
    }

    @Override
    public boolean isConfiguration() {
        return m_leafListSchemaNode.isConfiguration();
    }

    @Override
    public SchemaPath getChildModelNodeSchemaPath() {
        return m_leafListSchemaNode.getPath();
    }

    @Override
    public void addChild(ModelNode modelNode, ConfigLeafAttribute value) throws SetAttributeException{
        // Get existing leafList attributes of modelNode and add the value to the list and update ModelNode
        LinkedHashSet<ConfigLeafAttribute> values = new LinkedHashSet<>();
        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(modelNode, m_schemaRegistry);
        try{
            values.addAll(getValue(m_modelNodeDSM.findNode(modelNode.getModelNodeSchemaPath(), MNKeyUtil.getModelNodeKey(modelNode, registry),
                    EMNKeyUtil.getParentId(registry, modelNode.getModelNodeSchemaPath(), modelNode.getModelNodeId()), modelNode.getSchemaRegistry())));
        } catch (GetAttributeException e) {
            LOGGER.error("Failed while retrieving child leaf lists {}" , modelNode.getModelNodeId().getModelNodeIdAsString(), e);
            throw new SetAttributeException(e);
        }

        values.add(value);
        ModelNodeId parentId = EMNKeyUtil.getParentId(registry,modelNode.getModelNodeSchemaPath(),modelNode.getModelNodeId());
        m_modelNodeDSM.updateNode(modelNode, parentId, null,Collections.singletonMap(m_qName, values), false);
    }
    
    @Override
	public void addChildByUserOrder(ModelNode parentNode, ConfigLeafAttribute value, String operation, InsertOperation insertOperation) throws SetAttributeException, GetAttributeException {

        ModelNode reloadedParentNode = reloadModelNode(parentNode, m_modelNodeDSM, m_schemaRegistry);
        ArrayList<ConfigLeafAttribute> childList = new ArrayList<>();
        childList.addAll(getValue(reloadedParentNode));

        if (childList.contains(value)) {
			if (insertOperation != null) {
				childList.remove(value);
			} else if (operation.equals(EditConfigOperations.MERGE) || operation.equals(EditConfigOperations.REPLACE)) {
				return;
			}
		}
    	// default insert operation
		if (insertOperation == null) {
			insertOperation = InsertOperation.LAST_OP;
		}
        ModelNodeId parentId = EMNKeyUtil.getParentId(parentNode.getSchemaRegistry(), parentNode.getModelNodeSchemaPath(), parentNode.getModelNodeId());
        insertChildAtSpecificIndex(childList, value, insertOperation);
        LinkedHashSet<ConfigLeafAttribute> children = new LinkedHashSet<>();
        children.addAll(childList);
        m_modelNodeDSM.updateNode(parentNode, parentId, null, Collections.singletonMap(m_qName, children), -1, false);
	}

    @Override
    public LeafListSchemaNode getLeafListSchemaNode() {
        return m_leafListSchemaNode;
    }

    private void insertChildAtSpecificIndex(List<ConfigLeafAttribute> childList, ConfigLeafAttribute value, InsertOperation insertOperation) {
		if (childList.isEmpty() || insertOperation.getName().equals(InsertOperation.LAST)) {
			childList.add(value);
            setInsertIndex(childList);
            return;
		}

		ArrayList<ConfigLeafAttribute> children = (ArrayList<ConfigLeafAttribute>) childList;
        int indexValueToInsert = 0;
        for(ConfigLeafAttribute leafAttribute : children){
            if(leafAttribute.getStringValue().equals(insertOperation.getValue())){
                indexValueToInsert = children.indexOf(leafAttribute);
            }
        }
		if (insertOperation.getName().equals(InsertOperation.FIRST)) {
			children.add(0, value);
            setInsertIndex(childList);
            return;
		} else if (insertOperation.getName().equals(InsertOperation.AFTER)) {// For After insert, increase
            indexValueToInsert++;
        }
		
		if (indexValueToInsert > children.size() - 1) {
			children.add(value);
            setInsertIndex(childList);
		} else {
			children.add(indexValueToInsert, value);
            setInsertIndex(childList);
		}
	}

    @Override
    public void removeChild(ModelNode modelNode, ConfigLeafAttribute value) throws ModelNodeDeleteException {
        LOGGER.debug("remove child:{} for modelNode:{}", value, modelNode);
        LinkedHashSet<ConfigLeafAttribute> values = null;
        if (value != null ){
            values = new LinkedHashSet<>();
            values.add(value);
        } 
        
        ModelNodeId parentId = EMNKeyUtil.getParentId(modelNode.getSchemaRegistry(),modelNode.getModelNodeSchemaPath(),modelNode.getModelNodeId());
        m_modelNodeDSM.updateNode(modelNode, parentId, null, Collections.singletonMap(m_qName, values), true);
    }

    @Override
    public void removeAllChild(ModelNode modelNode) throws ModelNodeDeleteException {
        LOGGER.debug("remove All child for modelNode:{}", modelNode);
        removeChild(modelNode, null);
    }

    @Override
    public boolean isChildSet(ModelNode node) {
        return true;
    }

    private static ModelNode reloadModelNode(ModelNode modelNode, ModelNodeDataStoreManager modelNodeDSM, SchemaRegistry schemaRegistry)
            throws DataStoreException {
        ModelNode freshModelNode = modelNode;
        ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(modelNode, schemaRegistry);
        SchemaPath modelNodeSchemaPath = modelNode.getModelNodeSchemaPath();
        if (modelNode instanceof ModelNodeWithAttributes) {
            freshModelNode = modelNodeDSM.findNode(modelNodeSchemaPath, modelNodeKey,
                        EMNKeyUtil.getParentId(schemaRegistry, modelNodeSchemaPath, modelNode.getModelNodeId()), modelNode.getSchemaRegistry());
        }
        return freshModelNode;
    }

    private void setInsertIndex(List<ConfigLeafAttribute> childList) {
        for (ConfigLeafAttribute child : childList) {
            child.setInsertIndex(childList.indexOf(child));
        }
    }
}
