package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.hibernate.proxy.HibernateProxy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;

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
        Class<?> klass = getEntityClass(entityObject);
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
                if(parentId.getRdns().size() > 0){
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
        Map<QName,Method> leafListGetters = m_entityRegistry.getYangLeafListGetters(klass);
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        for(Map.Entry<QName, Method> leafListGetter : leafListGetters.entrySet()){
            Set<Object> values;
            try{
                values = (Set<Object>) leafListGetter.getValue().invoke(entityObject);
                if (values!=null && !values.isEmpty()) {
                    QName leafListQName = leafListGetter.getKey();
                    LinkedHashSet<ConfigLeafAttribute> leafList = new LinkedHashSet<>();

                    Iterator iterator = values.iterator();
                    while (iterator.hasNext()){
                        Object leafListEntity = iterator.next();
                        Class<?> leafListKlass = getEntityClass(leafListEntity);
                        if (leafListKlass!=null) {
                            Map<QName, Method> leafListEntityAttrGetters = m_entityRegistry.getAttributeGetters(leafListKlass);
                            Method attributeGetter = leafListEntityAttrGetters.get(leafListQName);
                            String leafListStringValue = (String) attributeGetter.invoke(leafListEntity);

                            Map<QName, Method> leafListEntityAttrNsGetters = m_entityRegistry.getYangAttributeNSGetters(leafListKlass);
                            Method identityRefNSGetter= leafListEntityAttrNsGetters.get(leafListQName);

                            if(identityRefNSGetter!=null){
                                String leafListStringNsValue = (String) identityRefNSGetter.invoke(leafListEntity);
                                ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(m_schemaRegistry,
                                        parentSchemaPath,leafListStringNsValue, leafListQName,leafListStringValue);
                                leafList.add(configLeafAttribute);
                            }else{
                                leafList.add(ConfigAttributeFactory.getConfigAttributeFromEntity(m_schemaRegistry,
                                        parentSchemaPath,leafListQName.getLocalName(), leafListQName, leafListStringValue));
                            }
                        }
                    }
                    leafLists.put(leafListQName, leafList);
                    node.setLeafLists(leafLists);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new ModelNodeMapperException(e);
            }
        }
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
                    configAttribute.getValue().invoke(entity, configLeafAttribute.getStringValue());
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
                Set<ConfigLeafAttribute> values = ((ModelNodeWithAttributes) modelNode).getLeafList(leafListQName);
                if (values!=null && !values.isEmpty()) {
                    Collection<Object> updatedEntities = buildLeafListEntities(leafListQName,modelNode,values);
                    ((Collection)leafListQNameGetterEntry.getValue().invoke(entity)).addAll(updatedEntities);
                }
            }
            
            if (insertIndex != -1) {
                Method orderByUserSetter = m_entityRegistry.getOrderByUserSetter(klass);
                orderByUserSetter.invoke(entity, insertIndex);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private Set<Object> buildLeafListEntities(QName leafListQName, ModelNode parentModelNode,Set<ConfigLeafAttribute> values) {

        LinkedHashSet<Object> leafListValueEntities = new LinkedHashSet<>();
        SchemaPath parentSchemaPath = parentModelNode.getModelNodeSchemaPath();
        SchemaPath leafListSchemaPath = m_schemaRegistry.getDescendantSchemaPath(parentSchemaPath, leafListQName);
        Class<?> leafListKlass = m_entityRegistry.getEntityClass(leafListSchemaPath);

        Iterator<ConfigLeafAttribute> valueIter = values.iterator();
        while (valueIter.hasNext()) {
            ConfigLeafAttribute configLeafAttribute = valueIter.next();
            Object leafListEntity;
            try {
                leafListEntity = leafListKlass.newInstance();
                Method schemaPathSetter = m_entityRegistry.getSchemaPathSetter(leafListKlass);
                SchemaPath pathWithoutRevisions = m_schemaRegistry.stripRevisions(parentSchemaPath);
                schemaPathSetter.invoke(leafListEntity, SchemaPathUtil.toString(pathWithoutRevisions));
                Method parentIdSetter = m_entityRegistry.getParentIdSetter(leafListKlass);
                parentIdSetter.invoke(leafListEntity, parentModelNode.getModelNodeId().getModelNodeIdAsString());

                Map<QName,Method> attributeSetters = m_entityRegistry.getAttributeSetters(leafListKlass);
                Method attributeSetter = attributeSetters.get(leafListQName);
                if(attributeSetter!=null){
                    attributeSetter.invoke(leafListEntity,configLeafAttribute.getStringValue());

                    Method attributeNsSetter = m_entityRegistry.getYangAttributeNSSetters(leafListKlass).get(leafListQName);
                    if(attributeNsSetter!=null){
                        attributeNsSetter.invoke(leafListEntity,configLeafAttribute.getNamespace());
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            leafListValueEntities.add(leafListEntity);
        }
        return leafListValueEntities;
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
            Class<?> entityClass = getEntityClass(parentEntityObj);
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
        Class<?> klass = getEntityClass(entity);
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

    private Class<?> getEntityClass(Object entityObject) {
        if (entityObject instanceof HibernateProxy) {
            return ((HibernateProxy) entityObject).getHibernateLazyInitializer()
                    .getPersistentClass();
        }
        return entityObject.getClass();
    }
}
