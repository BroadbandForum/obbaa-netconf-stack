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

import static org.broadband_forum.obbaa.netconf.api.util.ReflectionUtils.getAllDeclaredFields;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by keshava on 6/12/15.
 */
public class EntityRegistryImpl implements EntityRegistry {
    private final Map<SchemaPath, Class> m_schemaPaths = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, List<SchemaPath>> m_classToSchemaPath = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Method> m_entityPreDeleteCallback = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Boolean> m_eagerFetchInfo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName, Method>> m_configAttributeGetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName, Method>> m_configAttributeSetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName, Method>> m_yangAttributeNSGetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName, Method>> m_yangAttributeNSSetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName, String>> m_fieldNames = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName, Method>> m_yangChildGetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName, Method>> m_yangChildSetters = new ConcurrentHashMap<>();
    private final Map<Class, Method> m_yangXmlSubtreeGetter = new ConcurrentHashMap<>();
    private final Map<Class, Method> m_yangXmlSubtreeSetter = new ConcurrentHashMap<>();
    private final Map<Class, Method> m_yangVisibilityControllerGetter = new ConcurrentHashMap<>();
    private final Map<Class, Method> m_yangVisibilityControllerSetter = new ConcurrentHashMap<>();
    private final Map<Class, Method> m_schemaPathSetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Method> m_schemaPathGetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Method> m_parentIdSetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Method> m_parentIdGetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Class>> m_componentClass = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Map<SchemaPath, Class>>> m_schemaPathsFromComponent = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName,Method>> m_yangLeafListGetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Map<QName,Method>> m_yangLeafListSetters = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class, Map<QName,String>> m_keyColumnInfos = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Method> m_orderByUserGetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Method> m_orderByUserSetters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class, Boolean> m_bigListType = new ConcurrentHashMap<>();

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(EntityRegistryImpl.class, LogAppNames.NETCONF_STACK);
    private ConcurrentHashMap<String, List<Class>> m_classesWithYangParentSchemaPathAnnotation = new ConcurrentHashMap<>();
    private Map<Class, List<EntityOnDeleteInterceptor>> m_entityOnDeleteInterceptor = new ConcurrentHashMap<>();

    public EntityRegistryImpl(){

    }

    @Override
    public QName getQName(Class aClass) {
        List<SchemaPath> schemaPaths = m_classToSchemaPath.get(aClass);
        if(schemaPaths == null || schemaPaths.isEmpty()){
            return null;
        }
        return schemaPaths.get(0).getLastComponent();
    }

    @Override
    public void addSchemaPaths(String componentId, Map<SchemaPath, Class> schemaPaths) {
        m_schemaPaths.putAll(schemaPaths);
        updateClassToSchemaPathMap(schemaPaths);
        addComponentSchemaPaths(componentId, schemaPaths);
    }

    @Override
    public void addComponentClass(String componentId, Class klass){
        Set<Class> classSet = m_componentClass.get(componentId);
        if(classSet == null){
            classSet = new HashSet<>();
            m_componentClass.putIfAbsent(componentId, classSet);
            classSet = m_componentClass.get(componentId);
        }
        classSet.add(klass);
    }

    @Override
    public Class getEntityClass(SchemaPath schemaPath) {
        return m_schemaPaths.get(schemaPath);
    }

    @Override
    public void addConfigAttributeGetters(Class klass, Map<QName, Method> configAttributeGetters, Map<QName, String> fieldNames) {
        Map<QName, Method> existingGetters = m_configAttributeGetters.get(klass);
        if(existingGetters==null){
            existingGetters= new HashMap<>();
            m_configAttributeGetters.putIfAbsent(klass, existingGetters);
            existingGetters = m_configAttributeGetters.get(klass);
        }
        existingGetters.putAll(configAttributeGetters);

        Map<QName, String> existingFieldNamesMap = m_fieldNames.get(klass);
        if(existingFieldNamesMap==null){
            existingFieldNamesMap = new HashMap<>();
            m_fieldNames.putIfAbsent(klass, existingFieldNamesMap);
            existingFieldNamesMap = m_fieldNames.get(klass);
        }
        existingFieldNamesMap.putAll(fieldNames);
    }

    @Override
    public Map<QName, Method> getAttributeGetters(Class aClass) {
        return m_configAttributeGetters.get(aClass);
    }

    @Override
    public String getFieldName(Class klass, QName qName) {
        return m_fieldNames.get(klass).get(qName);
    }

    @Override
    public void addConfigAttributeSetters(Class klass, Map<QName, Method> configAttributeSetters, Method parentIdSetter) {
        Map<QName, Method> existingSetters = m_configAttributeSetters.get(klass);
        if(existingSetters==null){
            existingSetters= new HashMap<>();
            m_configAttributeSetters.putIfAbsent(klass, existingSetters);
            existingSetters = m_configAttributeSetters.get(klass);
        }
        existingSetters.putAll(configAttributeSetters);

        m_parentIdSetters.put(klass, parentIdSetter);
    }

    @Override
    public Map<QName, Method> getAttributeSetters(Class aClass) {
        return m_configAttributeSetters.get(aClass);
    }

    @Override
    public Method getParentIdSetter(Class aClass) {
        return m_parentIdSetters.get(aClass);
    }

    @Override
    public void addParentIdGetter(Class subrootClass, Method parentIdGetter) {
        m_parentIdGetters.put(subrootClass, parentIdGetter);
    }

    public Method getParentIdGetter(Class klass) {
        return m_parentIdGetters.get(klass);
    }

    @Override
    public void addYangSchemaPathGetter(Class subrootClass, Method schemaPathGetter) {
        m_schemaPathGetters.put(subrootClass, schemaPathGetter);
    }

    public Method getSchemaPathGetter(Class klass) {
        return m_schemaPathGetters.get(klass);
    }

    @Override
    public void addYangSchemaPathSetter(Class subrootClass, Method schemaPathSetter) {
        m_schemaPathSetters.put(subrootClass, schemaPathSetter);
    }

    @Override
    public Method getSchemaPathSetter(Class klass) {
        return m_schemaPathSetters.get(klass);
    }
    
    @Override
    public void addOrderByUserGetter(Class subrootClass, Method schemaPathGetter) {
    	if (schemaPathGetter != null) {
    		m_orderByUserGetters.put(subrootClass, schemaPathGetter);
    	}
    }

    public Method getOrderByUserGetter(Class klass) {
        return m_orderByUserGetters.get(klass);
    }

    @Override
    public void addOrderByUserSetter(Class subrootClass, Method schemaPathSetter) {
    	if (schemaPathSetter != null) {
    		m_orderByUserSetters.put(subrootClass, schemaPathSetter);
    	}
    }

    @Override
    public Method getOrderByUserSetter(Class klass) {
        return m_orderByUserSetters.get(klass);
    }

    @Override
    public void addYangChildGetters(Class subrootClass, Map<QName, Method> yangChildGetters) {
        Map<QName,Method> existingGetters = m_yangChildGetters.get(subrootClass);
        if(existingGetters==null) {
            existingGetters = new HashMap<>();
            m_yangChildGetters.putIfAbsent(subrootClass, existingGetters);
            existingGetters = m_yangChildGetters.get(subrootClass);
        }
        existingGetters.putAll(yangChildGetters);
    }

    @Override
    public Map<QName, Method> getYangChildGetters(Class<?> aClass) {
        return m_yangChildGetters.get(aClass);
    }

    @Override
    public void addYangChildSetters(Class subrootClass, Map<QName, Method> yangChildSetters) {
        Map<QName,Method> existingSetters = m_yangChildSetters.get(subrootClass);
        if(existingSetters==null) {
            existingSetters = new HashMap<>();
            m_yangChildSetters.putIfAbsent(subrootClass, existingSetters);
            existingSetters = m_yangChildSetters.get(subrootClass);
        }
        existingSetters.putAll(yangChildSetters);
    }

    @Override
    public Map<QName, Method> getYangChildSetters(Class klass) {
        return m_yangChildSetters.get(klass);
    }

    @Override
    public void addYangXmlSubtreeGetter(Class klass, Method yangXmlSubtreeGetter) {
        m_yangXmlSubtreeGetter.put(klass, yangXmlSubtreeGetter);
    }

    @Override
    public Method getYangXmlSubtreeGetter(Class klass) {
        return m_yangXmlSubtreeGetter.get(klass);
    }

    @Override
    public void addYangVisibilityControllerGetter(Class klass, Method yangVisibilityControllerGetter) {
        if (yangVisibilityControllerGetter != null) {
            m_yangVisibilityControllerGetter.put(klass, yangVisibilityControllerGetter);
        }
    }

    @Override
    public Method getYangVisibilityControllerGetter(Class klass) {
        return m_yangVisibilityControllerGetter.get(klass);
    }

    @Override
    public void addYangXmlSubtreeSetter(Class klass, Method yangXmlSubtreeSetter) {
        m_yangXmlSubtreeSetter.put(klass, yangXmlSubtreeSetter);
    }

    @Override
    public Method getYangXmlSubtreeSetter(Class klass) {
        return m_yangXmlSubtreeSetter.get(klass);
    }

    @Override
    public void addYangVisibilityControllerSetter(Class klass, Method yangVisibilityControllerSetter) {
        if (yangVisibilityControllerSetter != null) {
            m_yangVisibilityControllerSetter.put(klass, yangVisibilityControllerSetter);
        }
    }

    @Override
    public Method getYangVisibilityControllerSetter(Class klass) {
        return m_yangVisibilityControllerSetter.get(klass);
    }

    public void addEagerFetchInfo(Class klass, Boolean eagerlyFetchXmlSubtree) {
        m_eagerFetchInfo.put(klass, eagerlyFetchXmlSubtree);
    }

    @Override
    public Boolean getEagerFetchInfo(Class klass) {
        return m_eagerFetchInfo.get(klass);
    }

    @Override
    public void addYangLeafListGetters(Class subrootClass, Map<QName, Method> yangLeafListGetters){
        Map<QName, Method> existingGetters = m_yangLeafListGetters.get(subrootClass);
        if(existingGetters==null) {
            existingGetters = new HashMap<>();
            m_yangLeafListGetters.putIfAbsent(subrootClass, existingGetters);
            existingGetters = m_yangLeafListGetters.get(subrootClass);
        }
        existingGetters.putAll(yangLeafListGetters);
    }

    @Override
    public Map<QName, Method> getYangLeafListGetters(Class klass) {
        return m_yangLeafListGetters.get(klass);
    }

    @Override
    public void addYangLeafListSetters(Class subrootClass, Map<QName, Method> yangLeafListSetters){
        Map<QName, Method> existingSetters = m_yangLeafListSetters.get(subrootClass);
        if(existingSetters==null) {
            existingSetters = new HashMap<>();
            m_yangLeafListSetters.putIfAbsent(subrootClass, existingSetters);
            existingSetters = m_yangLeafListSetters.get(subrootClass);
        }
        existingSetters.putAll(yangLeafListSetters);
    }

    @Override
    public Map<QName, Method> getYangLeafListSetters(Class klass) {
        return m_yangLeafListSetters.get(klass);
    }
    
    @Override
    public void addBigListType(Class klass, boolean bigListType) {
        m_bigListType.put(klass, bigListType);
    }
    
    @Override
    public boolean getBigListType(Class klass) {
        return m_bigListType.get(klass);
    }

    @Override
    public void undeploy(String componentId) {
        Set<Class> classSet = m_componentClass.get(componentId);
        if(classSet != null){
            m_configAttributeGetters.keySet().removeAll(classSet);
            m_configAttributeSetters.keySet().removeAll(classSet);
            m_yangChildGetters.keySet().removeAll(classSet);
            m_yangChildSetters.keySet().removeAll(classSet);
            m_yangXmlSubtreeGetter.keySet().removeAll(classSet);
            m_yangXmlSubtreeSetter.keySet().removeAll(classSet);
            m_schemaPathSetters.keySet().removeAll(classSet);
            m_schemaPathGetters.keySet().removeAll(classSet);
            m_parentIdGetters.keySet().removeAll(classSet);
            m_parentIdSetters.keySet().removeAll(classSet);
            m_orderByUserGetters.keySet().removeAll(classSet);
            m_orderByUserSetters.keySet().removeAll(classSet);
            m_yangLeafListGetters.keySet().removeAll(classSet);
            m_yangLeafListSetters.keySet().removeAll(classSet);
            m_fieldNames.keySet().removeAll(classSet);
            m_bigListType.keySet().removeAll(classSet);
            m_yangVisibilityControllerGetter.keySet().removeAll(classSet);
            m_yangVisibilityControllerSetter.keySet().removeAll(classSet);
        }
        List<Map<SchemaPath, Class>> schemaPathsFromComponent = m_schemaPathsFromComponent.get(componentId);
        if(schemaPathsFromComponent != null){
            for(Map<SchemaPath,Class> schemaPathClassMap : schemaPathsFromComponent) {
                m_schemaPaths.keySet().removeAll(schemaPathClassMap.keySet());
                m_classToSchemaPath.keySet().removeAll(schemaPathClassMap.values());
            }
        }

        m_componentClass.remove(componentId);
        m_schemaPathsFromComponent.remove(componentId);
        m_classesWithYangParentSchemaPathAnnotation.remove(componentId);
        LOGGER.debug("Un-deployed {}",componentId);
    }

    @Override
    public void updateRegistry(String componentId, List<Class> classes, SchemaRegistry schemaRegistry, EntityDataStoreManager entityDSM, ModelNodeDSMRegistry modelNodeDSMRegistry) throws AnnotationAnalysisException {
        EntityRegistryBuilder.updateEntityRegistry(componentId, classes, this, schemaRegistry, entityDSM, modelNodeDSMRegistry);
    }

    private void updateClassToSchemaPathMap(Map<SchemaPath, Class> schemaPaths) {
        for(Map.Entry<SchemaPath, Class> entry : schemaPaths.entrySet()) {
            List<SchemaPath> existingSchemaPathsForClass = m_classToSchemaPath.get(entry.getValue());
            if(existingSchemaPathsForClass == null){
                existingSchemaPathsForClass = new ArrayList<>();
                m_classToSchemaPath.putIfAbsent(entry.getValue(), existingSchemaPathsForClass);
                existingSchemaPathsForClass = m_classToSchemaPath.get(entry.getValue());
            }
            existingSchemaPathsForClass.add(entry.getKey());
        }
    }

    private void addComponentSchemaPaths(String componentId, Map<SchemaPath, Class> schemaPaths) {
        List<Map<SchemaPath, Class>> schemaPathsFromComponent = m_schemaPathsFromComponent.get(componentId);
        if(schemaPathsFromComponent == null){
            schemaPathsFromComponent = new ArrayList<>();
            m_schemaPathsFromComponent.putIfAbsent(componentId, schemaPathsFromComponent);
            schemaPathsFromComponent = m_schemaPathsFromComponent.get(componentId);
        }
        schemaPathsFromComponent.add(schemaPaths);
    }
    
	@Override
	public String getOrderByFieldName(Class entityClass) {
		for (Field field : getAllDeclaredFields(entityClass)) {
			if (field.getAnnotation(YangOrderByUser.class) != null) {
				return field.getName();
			}
		}
		return null;
	}

	@Override
	public String getYangParentIdFieldName(Class entityClass){
        for (Field field : getAllDeclaredFields(entityClass)) {
            if (field.getAnnotation(YangParentId.class) != null) {
                return field.getName();
            }
        }
        return null;
	}

    @Override
    public List<EntityOnDeleteInterceptor> getEntityOnDeleteInterceptor(Class<?> klass) {
        List<EntityOnDeleteInterceptor> entityOnDeleteInterceptors = m_entityOnDeleteInterceptor.get(klass);
        if(entityOnDeleteInterceptors != null) {
            return entityOnDeleteInterceptors;
        }
        return Collections.emptyList();
    }

    @Override
    public void registerEntityOnDeleteInterceptor(Class<?> klass, EntityOnDeleteInterceptor entityOnDeleteInterceptor) {
        List<EntityOnDeleteInterceptor> entityOnDeleteInterceptors = m_entityOnDeleteInterceptor.get(klass);
        if(entityOnDeleteInterceptors == null) {
            entityOnDeleteInterceptors = new ArrayList<>();
            m_entityOnDeleteInterceptor.put(klass, entityOnDeleteInterceptors);
        }
        entityOnDeleteInterceptors.add(entityOnDeleteInterceptor);
    }

    @Override
    public void addYangAttributeNSGetters(Class klass, Map<QName, Method> yangAttributeNSGetters) {
        Map<QName, Method> existingGetters = m_yangAttributeNSGetters.get(klass);
        if(existingGetters==null) {
            existingGetters = new HashMap<>();
            m_yangAttributeNSGetters.putIfAbsent(klass, existingGetters);
            existingGetters = m_yangAttributeNSGetters.get(klass);
        }
        existingGetters.putAll(yangAttributeNSGetters);
    }

    @Override
    public Map<QName, Method> getYangAttributeNSGetters(Class klass) {
        return m_yangAttributeNSGetters.get(klass);
    }

    @Override
    public void addYangAttributeNSSetters(Class klass, Map<QName, Method> yangAttributeNSSetters) {
        Map<QName, Method> existingSetters = m_yangAttributeNSSetters.get(klass);
        if(existingSetters==null) {
            existingSetters = new HashMap<>();
            m_yangAttributeNSSetters.putIfAbsent(klass, existingSetters);
            existingSetters = m_yangAttributeNSSetters.get(klass);
        }
        existingSetters.putAll(yangAttributeNSSetters);
    }

    @Override
    public Map<QName, Method> getYangAttributeNSSetters(Class klass) {
        return m_yangAttributeNSSetters.get(klass);
    }

    @Override
    public void addJpaAttributesInfo(Class klass, Map<QName, String> attributesInfo) {
        Map<QName, String> existingInfos = m_keyColumnInfos.get(klass);
        if(existingInfos==null) {
            existingInfos = new HashMap<>();
            m_keyColumnInfos.putIfAbsent(klass, existingInfos);
            existingInfos = m_keyColumnInfos.get(klass);
        }
        existingInfos.putAll(attributesInfo);
    }

    @Override
    public Map<QName, String> getJpaAttributesInfo(Class klass) {
        return m_keyColumnInfos.get(klass);
    }

    @Override
    public void addClassWithYangParentSchemaPathAnnotation(String componentId, Class klass) {
        List<Class> existingClasses = m_classesWithYangParentSchemaPathAnnotation.get(componentId);
        if(existingClasses == null){
            existingClasses = new ArrayList<>();
            m_classesWithYangParentSchemaPathAnnotation.put(componentId, existingClasses);
        }
        existingClasses.add(klass);
    }

    @Override
    public boolean classHasYangParentSchemaPathAnnotation(Class<?> klass) {
        return m_classesWithYangParentSchemaPathAnnotation.entrySet().stream().filter(entry -> entry.getValue().contains(klass)).findAny().isPresent();
    }

    @Override
    public EntityRegistry unwrap() {
        return this;
    }
}
