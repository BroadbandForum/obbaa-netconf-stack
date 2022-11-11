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

import static org.broadband_forum.obbaa.netconf.api.util.CryptUtil2.ENCR_STR_PATTERN;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMUtils.setVisibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A Mapper that maps JPA + NCY Stack annotated objects into ModelNodes and vice-versa.
 */
public class EntityToModelNodeMapperImpl implements EntityToModelNodeMapper {

    private final EntityRegistry m_entityRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final SubSystemRegistry m_subsystemRegistry;
    private final SchemaRegistry m_schemaRegistry;

    public EntityToModelNodeMapperImpl(EntityRegistry entityRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                       SubSystemRegistry subsystemRegistry, SchemaRegistry schemaRegistry) {
        m_entityRegistry = entityRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_subsystemRegistry = subsystemRegistry;
        m_schemaRegistry = schemaRegistry;
    }

    @Override
    public ModelNodeWithAttributes getModelNode(Object entityObject, ModelNodeDataStoreManager modelNodeDSM){
        ModelNodeId parentId = new ModelNodeId();
        SchemaPath schemaPath;
        Class<?> klass = DSMUtils.getEntityClass(entityObject);
        SchemaPath parentSchemaPath = null;
        try {
            Method parentIdMethod = m_entityRegistry.getParentIdGetter(klass);
            String parentIdStr = (String) parentIdMethod.invoke(entityObject);
            Method schemaPathMethod = m_entityRegistry.getSchemaPathGetter(klass);
            schemaPath = SchemaPathUtil.fromString((String) schemaPathMethod.invoke(entityObject));
            schemaPath = m_schemaRegistry.addRevisions(schemaPath);
            if(parentIdStr!=null ){
                parentId = new ModelNodeId(parentIdStr, m_entityRegistry.getQName(klass)
                        .getNamespace().toString());
                if(parentId.getRdnsReadOnly().size() > 0){
                    DataSchemaNode nonChoiceParent = m_schemaRegistry.getNonChoiceParent(schemaPath);
                    if(nonChoiceParent != null){
                        parentSchemaPath = nonChoiceParent.getPath();
                    }else {
                        parentSchemaPath = SchemaPath.ROOT;
                    }
                    parentId = EMNKeyUtil.populateNamespaces(parentId, parentSchemaPath, m_schemaRegistry);
                }
            }
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new ModelNodeMapperException("Error while getting model node", e);
        }

        ModelNodeWithAttributes node = new ModelNodeWithAttributes(schemaPath,
                parentId, m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, modelNodeDSM);
        // Set the config attributes
        Map<QName, Method> attributeGetters = m_entityRegistry.getAttributeGetters(klass);
        Map<QName, Method> attributeNSGetters = m_entityRegistry.getYangAttributeNSGetters(klass);
        Map<QName, ConfigLeafAttribute> configValues = new HashMap<>();
        for(Map.Entry<QName, Method> attributeGetter: attributeGetters.entrySet()){
            try {
                String value = (String)attributeGetter.getValue().invoke(entityObject);
                if (value!=null) {
                    Method identityRefNSGetter= attributeNSGetters.get(attributeGetter.getKey());
                    ConfigLeafAttribute configLeafAttribute;
                    if(identityRefNSGetter!=null){
                        String namespace = (String)identityRefNSGetter.invoke(entityObject);
                        configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(m_schemaRegistry,schemaPath,namespace,
                                attributeGetter.getKey(),value);
                    }else{
                        configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(m_schemaRegistry,schemaPath,null,
                                attributeGetter.getKey(),value);
                    }
                    configValues.put(attributeGetter.getKey(), configLeafAttribute);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        node.setAttributes(configValues);
        // Set the config leafLists
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = DSMUtils.getConfigLeafListsFromEntity(m_schemaRegistry, schemaPath,
                m_entityRegistry, klass, entityObject);
        node.setLeafLists(leafLists);
        setVisibility(m_entityRegistry, entityObject, node);
        return node;
    }

    @Override
    public void updateEntity(Object entity, SchemaPath nodeSchemaPath, ModelNode modelNode, Class klass, ModelNodeId parentId, int insertIndex) {
        try {
            ModelNodeWithAttributes node = (ModelNodeWithAttributes) modelNode;
            Map<QName, ConfigLeafAttribute> configValues = node.getAttributes();
            // Set config attributes
            Map<QName, Method> attributeSetters = m_entityRegistry.getAttributeSetters(klass);
            for(Map.Entry<QName, Method> configAttribute : attributeSetters.entrySet()){
                ConfigLeafAttribute configLeafAttribute = configValues.get(configAttribute.getKey());
                if(configLeafAttribute!=null) {
                    String leafValue = getConfigValue(configLeafAttribute);
                    configAttribute.getValue().invoke(entity, leafValue);
                }else{
                    configAttribute.getValue().invoke(entity, (String)null);
                }
            }

            Map<QName, Method> attributeNSSetters = m_entityRegistry.getYangAttributeNSSetters(klass);
            for(Map.Entry<QName,Method> identityRefNSSetter : attributeNSSetters.entrySet()){
                ConfigLeafAttribute configLeafAttribute = configValues.get(identityRefNSSetter.getKey());
                if(configLeafAttribute!=null){
                    identityRefNSSetter.getValue().invoke(entity, configLeafAttribute.getNamespace());

                }
            }
            // Set Parent id and schema Path
            Method schemaPathSetter = m_entityRegistry.getSchemaPathSetter(klass);
            SchemaPath pathWithoutRevisions = m_schemaRegistry.stripRevisions(nodeSchemaPath);
            schemaPathSetter.invoke(entity, SchemaPathUtil.toString(pathWithoutRevisions));
            Method parentIdSetter = m_entityRegistry.getParentIdSetter(klass);
            parentIdSetter.invoke(entity, parentId.getModelNodeIdAsString());

            // Set Leaflist attributes
            Map<QName, Method> leafListGetters = m_entityRegistry.getYangLeafListGetters(klass);
            for(Map.Entry<QName, Method> leafListQNameGetterEntry: leafListGetters.entrySet()){
                QName leafListQName = leafListQNameGetterEntry.getKey();
                Set<ConfigLeafAttribute> values = modelNode.getLeafList(leafListQName);
                if (values!=null && !values.isEmpty()) {
                    Collection<Object> updatedEntities = buildLeafListEntities(leafListQName,modelNode,values);
                    ((Collection)leafListQNameGetterEntry.getValue().invoke(entity)).addAll(updatedEntities);
                }
            }

            // Set Visibility attribute only if the node is invisible
            if(!modelNode.isVisible()) {
                Method yangVisibilityControllerSetter = m_entityRegistry.getYangVisibilityControllerSetter(klass);
                if (yangVisibilityControllerSetter != null) {
                    yangVisibilityControllerSetter.invoke(entity, false);
                }
            }

            if (insertIndex != -1) {
                Method orderByUserSetter = m_entityRegistry.getOrderByUserSetter(klass);
                if (orderByUserSetter != null) {
                    orderByUserSetter.invoke(entity, insertIndex);
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private String getConfigValue(ConfigLeafAttribute configLeafAttribute) {
        String value = configLeafAttribute.getStringValue();
        if (configLeafAttribute.isPassword() && !ENCR_STR_PATTERN.matcher(value).matches()) {
            value = CryptUtil2.encrypt(value);
        }
        return value;
    }

    private Set<Object> buildLeafListEntities(QName leafListQName, ModelNode parentModelNode,Set<ConfigLeafAttribute> values) {

        LinkedHashSet<Object> leafListValueEntities = new LinkedHashSet<>();
        SchemaPath parentSchemaPath = parentModelNode.getModelNodeSchemaPath();
        SchemaPath leafListSchemaPath = m_schemaRegistry.getDescendantSchemaPath(parentSchemaPath, leafListQName);
        Class<?> leafListKlass = m_entityRegistry.getEntityClass(leafListSchemaPath);
        boolean isLeafListOrderedByUser = SchemaRegistryUtil.isLeafListOrderedByUser(leafListQName,
                parentModelNode.getModelNodeSchemaPath(), m_schemaRegistry);

        Iterator<ConfigLeafAttribute> valueIter = values.iterator();
        while (valueIter.hasNext()) {
            ConfigLeafAttribute configLeafAttribute = valueIter.next();
            Object leafListEntity;
            try {
                leafListEntity = leafListKlass.newInstance();
                Method schemaPathSetter = m_entityRegistry.getSchemaPathSetter(leafListKlass);
                SchemaPath pathWithoutRevisions = m_schemaRegistry.stripRevisions(leafListSchemaPath);
                schemaPathSetter.invoke(leafListEntity, SchemaPathUtil.toString(pathWithoutRevisions));
                Method parentIdSetter = m_entityRegistry.getParentIdSetter(leafListKlass);
                parentIdSetter.invoke(leafListEntity, parentModelNode.getModelNodeId().getModelNodeIdAsString());

                Map<QName,Method> attributeSetters = m_entityRegistry.getAttributeSetters(leafListKlass);
                Method attributeSetter = attributeSetters.get(leafListQName);
                if(attributeSetter!=null){
                    String leafListValue = getConfigValue(configLeafAttribute);
                    attributeSetter.invoke(leafListEntity, leafListValue);

                    Method attributeNsSetter = m_entityRegistry.getYangAttributeNSSetters(leafListKlass).get(leafListQName);
                    if(attributeNsSetter!=null){
                        attributeNsSetter.invoke(leafListEntity,configLeafAttribute.getNamespace());
                    }
                }
                updateLeafListIndex(configLeafAttribute, leafListKlass, leafListEntity, isLeafListOrderedByUser);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            leafListValueEntities.add(leafListEntity);
        }
        return leafListValueEntities;
    }

    private void updateLeafListIndex(ConfigLeafAttribute configLeafAttribute, Class<?> leafListKlass,
                                     Object leafListEntity, boolean isLeafListOrderedByUser)
            throws InvocationTargetException, IllegalAccessException {
        if(isLeafListOrderedByUser){
            Method orderByUserSetter = m_entityRegistry.getOrderByUserSetter(leafListKlass);
            if(orderByUserSetter != null){
                orderByUserSetter.invoke(leafListEntity, configLeafAttribute.getInsertIndex());
            }
        }
    }

    @Override
    public Object getEntity(SchemaPath nodeSchemaPath, ModelNode modelNode, Class klass, ModelNodeId parentId, int insertIndex) {
        Object entity;
        try {
            entity = klass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    	updateEntity(entity, nodeSchemaPath, modelNode, klass, parentId, insertIndex);
    	return entity;
    }

    @Override
    public Collection<Object> getChildEntities(Object parentEntityObj, QName childQname) {
        try {
            Class<?> entityClass = DSMUtils.getEntityClass(parentEntityObj);
            Map<QName, Method> childGetters = m_entityRegistry.getYangChildGetters(entityClass);
            if(childGetters !=null && !childGetters.isEmpty()) {
                Method childGetter = childGetters.get(childQname);
                if(childGetter!=null) {
                    return (Collection<Object>) childGetter.invoke(parentEntityObj);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return Collections.emptyList();
    }

    @Override
    public void clearLeafLists(Object entity, Set<QName> qNames) {
        Class<?> klass = DSMUtils.getEntityClass(entity);
        Map<QName, Method> leafListGetters = m_entityRegistry.getYangLeafListGetters(klass);
        for(QName qName : qNames){
            try {
                Collection leafListValues = (Collection) leafListGetters.get(qName).invoke(entity);
                leafListValues.clear();
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
