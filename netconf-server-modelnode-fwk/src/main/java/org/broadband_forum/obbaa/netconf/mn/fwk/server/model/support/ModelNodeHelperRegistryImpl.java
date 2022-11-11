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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.DefaultConcurrentHashMap;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ModelNodeHelperRegistryImpl implements ModelNodeHelperRegistry {
	
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ModelNodeHelperRegistryImpl.class, LogAppNames.NETCONF_STACK);

	private DefaultConcurrentHashMap<SchemaPath, ConcurrentSkipListMap<QName, ChildListHelper>> m_childListHelpers = new DefaultConcurrentHashMap<>(new ConcurrentSkipListMap<>(), true);

	private DefaultConcurrentHashMap<SchemaPath, ConcurrentSkipListMap<QName, ChildLeafListHelper>> m_childLeafListHelpers = new DefaultConcurrentHashMap<>(new ConcurrentSkipListMap<>(), true);

	private DefaultConcurrentHashMap<SchemaPath, ConcurrentSkipListMap<QName, ChildContainerHelper>> m_childContainerHelpers = new DefaultConcurrentHashMap<>(new ConcurrentSkipListMap<>(), true);

	private Map<SchemaPath, Map<QName, ConfigAttributeHelper>> m_naturalKeyHelpers = Collections.synchronizedMap(new LinkedHashMap<SchemaPath, Map<QName, ConfigAttributeHelper>>());

	private DefaultConcurrentHashMap<SchemaPath, ConcurrentSkipListMap<QName, ConfigAttributeHelper>> m_configAttributeHelpers = new DefaultConcurrentHashMap<>(new ConcurrentSkipListMap<>(), true);

	private Set<SchemaPath> m_registeredNodes = Collections.synchronizedSet(new HashSet<SchemaPath>());

	private DefaultCapabilityCommandInterceptor m_defaultCapabilityCommandInterceptor;

	private DefaultConcurrentHashMap<String, HashSet<SchemaPath>> m_componentSchemaPaths = new DefaultConcurrentHashMap<>(new HashSet<SchemaPath>(), true);

	private SchemaRegistry m_schemaRegistry;
	public ModelNodeHelperRegistryImpl(SchemaRegistry schemaRegistry){
		m_schemaRegistry = schemaRegistry;
		m_defaultCapabilityCommandInterceptor = new DoNothingInterceptor();
	}
	
	public boolean isRegistered(SchemaPath modelNodeSchemaPath){
		return m_registeredNodes.contains(modelNodeSchemaPath);
	}

	public boolean registrationComplete(SchemaPath modelNodeSchemaPath){
		return m_registeredNodes.add(modelNodeSchemaPath);
	}

    public Map<QName, ChildListHelper> getChildListHelpers(SchemaPath modelNodeSchemaPath) {
		 return m_childListHelpers.get(modelNodeSchemaPath);
	}

    public void registerChildListHelper(String componentId, SchemaPath parentSchemaPath, QName name, ChildListHelper helper){
		Map<QName, ChildListHelper> helpersForNode = getChildListHelpers(parentSchemaPath);
		if(name != null && helper != null){
			if(!helpersForNode.containsKey(name)){
				helpersForNode.put(name, helper);
				LOGGER.debug("Registered a child List helper : {} , {}, {}",parentSchemaPath,name,helper);
			}
			addSchemaPath(componentId, helper.getChildModelNodeSchemaPath());
		}else{
			LOGGER.error("Cannot deployHelpers child List helper : {} , {}, {}",parentSchemaPath,name,helper);
		}

	}

	public ChildListHelper getChildListHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
		Map<QName, ChildListHelper> helpersForClass = m_childListHelpers.get(modelNodeSchemaPath);
		return helpersForClass.get(helperName);
	}

	public void registerChildContainerHelper(String componentId, SchemaPath parentSchemaPath, QName name, ChildContainerHelper helper){
		if(parentSchemaPath !=null && name != null && helper != null){
			Map<QName, ChildContainerHelper> helpersForNode = m_childContainerHelpers.get(parentSchemaPath);
			if(!helpersForNode.containsKey(name)){
				helpersForNode.put(name, helper);
				LOGGER.debug("Registered a child container helper : {}, {}, {}",parentSchemaPath,name,helper);
			}
			addSchemaPath(componentId, helper.getChildModelNodeSchemaPath());
		}else{
			LOGGER.error("Cannot register child container helper :{}, {}, {} ",parentSchemaPath,name ,helper);
		}

	}

	public ChildContainerHelper getChildContainerHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
		Map<QName, ChildContainerHelper> helpersForClass = m_childContainerHelpers.get(modelNodeSchemaPath);
		if(helpersForClass != null){
			return helpersForClass.get(helperName); 
		}
		return null;
	}

	public Map<QName, ChildContainerHelper> getChildContainerHelpers(SchemaPath modelNodeSchemaPath) {
		return m_childContainerHelpers.get(modelNodeSchemaPath);
	}

	public void registerNaturalKeyHelper(String componentId, SchemaPath parentSchemaPath, QName name, ConfigAttributeHelper helper){
		if(parentSchemaPath !=null && name != null && helper != null){
			Map<QName, ConfigAttributeHelper> helpersForNode = m_naturalKeyHelpers.get(parentSchemaPath);
			if(helpersForNode == null){
				//first timers
				helpersForNode = new LinkedHashMap<QName, ConfigAttributeHelper>();
				m_naturalKeyHelpers.put(parentSchemaPath, helpersForNode);
			}
			if(!helpersForNode.containsKey(name)){
				helpersForNode.put(name, helper);
				LOGGER.debug("Registered a Natural Key helper: {} ,{}, {}",parentSchemaPath,name,helper);
			}
			addSchemaPath(componentId, helper.getChildModelNodeSchemaPath());
		}else{
			LOGGER.error("Cannot deployHelpers Natural Key helper : {}, {}, {}",parentSchemaPath,name,helper);
		}

	}

	public ConfigAttributeHelper getNaturalKeyHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
		Map<QName, ConfigAttributeHelper> helpersForClass = m_naturalKeyHelpers.get(modelNodeSchemaPath);
		if(helpersForClass != null){
			return helpersForClass.get(helperName);
		}
		return null;
	}

	public void registerConfigAttributeHelper(String componentId, SchemaPath parentSchemaPath, QName name, ConfigAttributeHelper helper) {
		if(parentSchemaPath !=null && name != null && helper != null){
			Map<QName, ConfigAttributeHelper> helpersForNode = m_configAttributeHelpers.get(parentSchemaPath);
			if(!helpersForNode.containsKey(name)){
				helpersForNode.put(name, helper);
				LOGGER.debug("Registered a config attribute helper: {}, {}, {}",parentSchemaPath,name,helper);
			}
			addSchemaPath(componentId, helper.getChildModelNodeSchemaPath());

		}else{
			LOGGER.error("Cannot deployHelpers config attribute helper : {}, {}, {}",parentSchemaPath,name,helper);
		}

	}

	public ConfigAttributeHelper getConfigAttributeHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
		Map<QName, ConfigAttributeHelper> helpersForClass = m_configAttributeHelpers.get(modelNodeSchemaPath);
		if(helpersForClass != null){
			return helpersForClass.get(helperName);
		}
		return null;
	}

    public Map<QName, ConfigAttributeHelper> getConfigAttributeHelpers(SchemaPath modelNodeSchemaPath) {
    	return m_configAttributeHelpers.get(modelNodeSchemaPath);
	}

    public Map<QName, ConfigAttributeHelper> getNaturalKeyHelpers(SchemaPath modelNodeSchemaPath) {
		if(modelNodeSchemaPath !=null){
			Map<QName, ConfigAttributeHelper> helpersForClass = m_naturalKeyHelpers.get(modelNodeSchemaPath);
			if(helpersForClass == null){
				helpersForClass = new TreeMap<QName, ConfigAttributeHelper>();
				m_naturalKeyHelpers.put(modelNodeSchemaPath, helpersForClass);
			}
			return helpersForClass;
		}
		return null;
	}

    public ChildLeafListHelper getConfigLeafListHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
		Map<QName, ChildLeafListHelper> helpersForURI = m_childLeafListHelpers.get(modelNodeSchemaPath);
		if(helpersForURI != null){
			return helpersForURI.get(helperName);
		}
		return null;
	}

	public Map<QName, ChildLeafListHelper> getConfigLeafListHelpers(SchemaPath modelNodeSchemaPath) {
		return m_childLeafListHelpers.get(modelNodeSchemaPath);
	}

	public void registerConfigLeafListHelper(String componentId, SchemaPath parentSchemaPath, QName name, ChildLeafListHelper childLeafListHelper) {
		if(parentSchemaPath !=null && name != null && childLeafListHelper != null){
			Map<QName, ChildLeafListHelper> helpersForNode = m_childLeafListHelpers.get(parentSchemaPath);
			if(!helpersForNode.containsKey(name)){
				helpersForNode.put(name, childLeafListHelper);
				LOGGER.debug("Registered a child LeafList helper : {}, {}, {}",parentSchemaPath,name,childLeafListHelper);
			}
			addSchemaPath(componentId, childLeafListHelper.getChildModelNodeSchemaPath());

		}else{
			LOGGER.error("Cannot deployHelpers child LeafList helper : {}, {}, {}",parentSchemaPath,name,childLeafListHelper);
		}
	}

	public DefaultCapabilityCommandInterceptor getDefaultCapabilityCommandInterceptor() {
		return m_defaultCapabilityCommandInterceptor;
	}

	public void setDefaultCapabilityCommandInterceptor(DefaultCapabilityCommandInterceptor defaultCapabilityCommandInterceptor) {
		m_defaultCapabilityCommandInterceptor = defaultCapabilityCommandInterceptor;
	}

	@Override
	public void undeploy(String componentId) {
		Set<SchemaPath> schemaPaths = getRemovableSchemaPaths(componentId);
		undeployHelpersFromComponentSubtree(schemaPaths);
		undeployComponentSubtreeFromParentNode(schemaPaths);
		m_componentSchemaPaths.remove(componentId);
		LOGGER.debug("helpers from {} undeployed",componentId);
	}

	@Override
	public void resetDefaultCapabilityCommandInterceptor() {
		m_defaultCapabilityCommandInterceptor = new DoNothingInterceptor();
	}

	@Override
	public void clear() {
		m_componentSchemaPaths.clear();
		m_registeredNodes.clear();
		m_configAttributeHelpers.clear();
		m_naturalKeyHelpers.clear();
		m_childContainerHelpers.clear();
		m_childLeafListHelpers.clear();
		m_childListHelpers.clear();
	}

	private void undeployHelpersFromComponentSubtree(Set<SchemaPath> schemaPaths) {
		if(schemaPaths != null) {
			m_childContainerHelpers.keySet().removeAll(schemaPaths);
			m_childListHelpers.keySet().removeAll(schemaPaths);
			m_naturalKeyHelpers.keySet().removeAll(schemaPaths);
			m_configAttributeHelpers.keySet().removeAll(schemaPaths);
			m_childLeafListHelpers.keySet().removeAll(schemaPaths);
		}
	}

	private void undeployComponentSubtreeFromParentNode(Set<SchemaPath> schemaPaths) {
		for(SchemaPath schemaPath: schemaPaths){
			DataSchemaNode parentSchemaNode = m_schemaRegistry.getNonChoiceParent(schemaPath);
			if(parentSchemaNode!=null){
				SchemaPath parentSchemaPath = parentSchemaNode.getPath();
				QName childQName = schemaPath.getLastComponent();
				removeHelper(parentSchemaPath,childQName,m_childContainerHelpers);
				removeHelper(parentSchemaPath,childQName,m_childListHelpers);
				removeHelper(parentSchemaPath,childQName,m_configAttributeHelpers );
				removeHelper(parentSchemaPath,childQName,m_childLeafListHelpers );
			}
		}
	}

	private <HT> void removeHelper(SchemaPath parentSchemaPath, QName childQName, DefaultConcurrentHashMap<SchemaPath,
			ConcurrentSkipListMap<QName,HT>> helpers) {
		Map<QName, HT> helperMap = helpers.get(parentSchemaPath);
		if(helperMap!=null) {
			if (helperMap.keySet().contains(childQName)) {
				helperMap.remove(childQName);
			}
		}
	}

	private void addSchemaPath(String componentId, SchemaPath modelNodeSchemaPath) {
        Set<SchemaPath> schemaPathSet = m_componentSchemaPaths.get(componentId);
        schemaPathSet.add(modelNodeSchemaPath);
    }
    
    private Set<SchemaPath> getRemovableSchemaPaths(String componentId){
    	Map<SchemaPath, String> augmentedPathToComponent = m_schemaRegistry.retrieveAppAugmentedPathToComponent();
    	Set<SchemaPath> schemaPaths = m_componentSchemaPaths.get(componentId);
    	Set<SchemaPath> removablePaths = new HashSet<>(schemaPaths);
    	for(SchemaPath augmentedPath : augmentedPathToComponent.keySet()){
    		if(augmentedPathToComponent.get(augmentedPath)!= componentId){
    			for(SchemaPath path : schemaPaths){
    				if(augmentedPath.equals(path)){
    					removablePaths.remove(augmentedPath);
    				}
    			}
    		}
    	}
    	return removablePaths;
    }

	@Override
	public ModelNodeHelperRegistry unwrap() {
		return this;
	}

	private static class DoNothingInterceptor implements DefaultCapabilityCommandInterceptor {
		@Override
        public EditContainmentNode processMissingData(EditContainmentNode oldEditData, ModelNode childModelNode) {
            return oldEditData;
        }

		@Override
        public boolean populateChoiceCase(EditContainmentNode newData, CaseSchemaNode caseNode, ModelNode childModelNode, boolean checkExistenceBeforeCreate) {
            return false;
        }
	}
}
