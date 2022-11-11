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

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKeyBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pgorai on 2/25/16.
 */
public class MNKeyUtil {
    public static ModelNodeKey getModelNodeKey(SchemaRegistry schemaRegistry, SchemaPath storedParentSchemaPath,
                                               ModelNodeId modelNodeId) {
        ModelNodeKey modelNodeKey = null;
        DataSchemaNode storedParentSchemaNode = schemaRegistry.getDataSchemaNode(storedParentSchemaPath);
        Map<QName,String> keyValues = new LinkedHashMap<>();
        if(storedParentSchemaNode instanceof ListSchemaNode){

        	QName targetContainerQName = storedParentSchemaPath.getLastComponent();
            List<QName> keyDefinition = ((ListSchemaNode) storedParentSchemaNode).getKeyDefinition();
            List<ModelNodeRdn> rdns = modelNodeId.getRdnsReadOnly();
            int qNameIndex = 0;
            boolean targetContainerFound = false;
            
            for (ModelNodeRdn rdn:rdns){
            	/* if the target QName in the modelNodeId is located,
            	 * find all key/leaf node before we hit next container. 
            	 * 
            	 * Those leaves are the keys for the target ModelNode.
            	 * 
            	 * If we hit next container, break. We have reached the end of description for the target container
            	 * 
            	 * PS: Here container refers to the ModelNodeRdn.CONTAINER and not YANG container
            	 * 
            	 */
            	if (targetContainerFound){
            		
            		if (rdn.getRdnName().equals(ModelNodeRdn.CONTAINER)){
            			break;
            		}else{
                    	QName qName = keyDefinition.get(qNameIndex);
                    	if (rdn.getRdnName().equals(qName.getLocalName()) && 
                    			rdn.getNamespace().equals(qName.getNamespace().toString())){
                    		keyValues.put(qName,rdn.getRdnValue());
                    		qNameIndex++;
                    	}
            		}
            	}else{
            		if (rdn.getRdnValue().equals(targetContainerQName.getLocalName()) && 
            				rdn.getNamespace().equals(targetContainerQName.getNamespace().toString()) &&
            				rdn.getRdnName().equals(ModelNodeRdn.CONTAINER)){
            			targetContainerFound = true;
            		}
            	}
            }
            modelNodeKey = new ModelNodeKey(keyValues);
        }else {
            modelNodeKey = new ModelNodeKeyBuilder().build();
        }
        return modelNodeKey;
    }

    public static ModelNodeKey getModelNodeKey(ModelNode modelNode, SchemaRegistry schemaRegistry) {
        Map<QName,String> keys = new LinkedHashMap<>();
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(modelNode.getModelNodeSchemaPath());
        if(schemaNode == null){
        	schemaNode = modelNode.getSchemaRegistry().getDataSchemaNode(modelNode.getModelNodeSchemaPath());
        }
        if(schemaNode instanceof ListSchemaNode){
            ModelNodeWithAttributes modelNodeWithAttributes = (ModelNodeWithAttributes) modelNode;
            Map<QName, ConfigLeafAttribute> attributes = modelNodeWithAttributes.getAttributes();
            for(QName keyDef : ((ListSchemaNode)schemaNode).getKeyDefinition()){
                ConfigLeafAttribute configLeafAttribute = attributes.get(keyDef);
                if(configLeafAttribute!=null){
                    keys.put(keyDef, configLeafAttribute.getStringValue());
                }
            }
        }
        return modelNodeKey;
    }

    public static ModelNodeKey getModelNodeKey(ModelNodeId modelNodeId, SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        ModelNodeKeyBuilder builder = new ModelNodeKeyBuilder();
        DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if(schemaNode instanceof ListSchemaNode){
            List<QName> keyDefinition = ((ListSchemaNode) schemaNode).getKeyDefinition();
            int i = modelNodeId.getRdnsReadOnly().size() - keyDefinition.size();
            for (QName keyQName : keyDefinition) {
                builder.appendKey(keyQName, modelNodeId.getRdnsReadOnly().get(i).getRdnValue());
                i++;
            }
        }
        return builder.build();
    }

    /**
     * Prepares a ModelNodeKey out of supplied superset of key-value pairs.
     * If the supplied superset does not contain all the keys, it returns an empty key.
     * @param nodeType
     * @param matchCriteria
     * @param schemaRegistry
     * @return
     */
    public static ModelNodeKey getKeyFromCriteria(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, SchemaRegistry schemaRegistry) {
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(nodeType);
        return getKeyFromCriteria(node, matchCriteria);
    }

    public static ModelNodeKey getKeyFromCriteria(DataSchemaNode node, Map<QName, ConfigLeafAttribute> matchCriteria) {
        ModelNodeKeyBuilder builder = new ModelNodeKeyBuilder();
        if(node instanceof ListSchemaNode) {
            ListSchemaNode listNode = (ListSchemaNode) node;
            for (QName keyQname : listNode.getKeyDefinition()) {
                ConfigLeafAttribute value = matchCriteria.get(keyQname);
                if (value != null && value.getStringValue() != null) {
                    builder.appendKey(keyQname, value.getStringValue());
                } else {
                    return ModelNodeKey.EMPTY_KEY;
                }
            }
        }
        return builder.build();
    }

    /**
     * Returns true of matchCriteria contains values for all keys of the given node.
     * If the node is a container node, it returns true always.
     * @param nodeType
     * @param matchCriteria
     * @param schemaRegistry
     * @return
     */
    public static boolean containsAllKeys(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, SchemaRegistry schemaRegistry) {
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(nodeType);
        if(node instanceof ListSchemaNode){
            ListSchemaNode listNode = (ListSchemaNode) node;
            for(QName keyQname : listNode.getKeyDefinition()){
                // I don't think we have to check for empty string, because by definition, yang list keys cannot be empty
                if(matchCriteria.get(keyQname) == null){
                    return false;
                }
            }
        }else if(node instanceof ContainerSchemaNode){
            return true;
        }else{
            //any other schema node, the call probably is invalid, so return false
            return false;
        }
        return true;
    }

    /**
     * Returns true of the ModelNode matches the given criteria, false otherwise.
     * @param matchCriteria
     * @param modelNode
     * @param schemaRegistry
     * @return
     */
    public static boolean isMatch(Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeWithAttributes modelNode, SchemaRegistry schemaRegistry) {
        for(Map.Entry<QName,ConfigLeafAttribute> entry: matchCriteria.entrySet()){
            QName attributeQName = entry.getKey();
            String expectedValue = null;
            if(entry.getValue()!=null){
                expectedValue = entry.getValue().getStringValue();
            }
            SchemaPath nodeSchemaPath = new SchemaPathBuilder().withParent(modelNode.getModelNodeSchemaPath()).appendQName(attributeQName).build();
            DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
            if (schemaRegistry.getDataSchemaNode(nodeSchemaPath) == null) {
                schemaNode = modelNode.getSchemaRegistry().getDataSchemaNode(nodeSchemaPath);
                if(schemaNode == null) {
                    // there are chances that this child might be a choice child
                    schemaNode = modelNode.getSchemaRegistry().getNonChoiceChild(modelNode.getModelNodeSchemaPath(), attributeQName);
                }
            }
        	
            if(schemaNode instanceof LeafSchemaNode){
                Map<QName, ConfigLeafAttribute> configAttributes = modelNode.getAttributes();
                if(configAttributes.get(attributeQName) != null){
                    if(!configAttributes.get(attributeQName).getStringValue().equals(expectedValue)) {
                        return false;
                    }
                }else if (expectedValue != null){
                    //expected to be not null, but it is null, so not match
                    return false;
                }
            }else if(schemaNode instanceof LeafListSchemaNode){
                Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes = modelNode.getLeafLists();
                if(leafListAttributes.get(attributeQName)!=null){
                    Set<ConfigLeafAttribute> leafListValuesOfType = leafListAttributes.get(attributeQName);
                    if(!leafListValuesOfType.contains(entry.getValue())){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
