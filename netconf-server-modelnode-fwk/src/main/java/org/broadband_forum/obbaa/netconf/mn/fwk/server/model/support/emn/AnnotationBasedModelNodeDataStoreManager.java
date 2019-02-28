package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil.getDataParentSchemaPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ListEntryInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ListEntryQName;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.PredicateCondition;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A ModelNodeDataStoreManager for nodeTypes which has corresponding entity classes
 */
public class AnnotationBasedModelNodeDataStoreManager implements ModelNodeDataStoreManager {
    public static final String SCHEMA_PATH = "schemaPath";
    private final EntityRegistry m_entityRegistry;
    protected final EntityToModelNodeMapper m_entityToModelNodeMapper;
    private final PersistenceManagerUtil m_persistenceManagerUtil;
    private final SchemaRegistry m_schemaRegistry;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AnnotationBasedModelNodeDataStoreManager.class, LogAppNames.NETCONF_STACK);

    public AnnotationBasedModelNodeDataStoreManager(PersistenceManagerUtil persistenceManagerUtil, EntityRegistry entityRegistry,
                                                    SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                                    SubSystemRegistry subsystemRegistry, ModelNodeDSMRegistry modelNodeDSMRegistry){
        m_persistenceManagerUtil = persistenceManagerUtil;
        m_entityRegistry = entityRegistry;
        m_schemaRegistry = schemaRegistry;
        m_entityToModelNodeMapper = new EntityToModelNodeMapperImpl(m_entityRegistry, modelNodeHelperRegistry, subsystemRegistry, schemaRegistry);
        m_modelNodeDSMRegistry = modelNodeDSMRegistry;
        
        if (m_persistenceManagerUtil.getEntityDataStoreManager()!=null){
        	m_persistenceManagerUtil.getEntityDataStoreManager().dumpModifiedSessionVariables();
        }
    }

    @Override
    public void beginModify() {

    }

    @Override
    public void endModify() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ModelNode> listNodes(SchemaPath nodeType) {
        LOGGER.debug(String.format("listNodes with nodeType: {}", nodeType));
        List<ModelNode> modelNodes = new ArrayList<>();
        Class entityClass = m_entityRegistry.getEntityClass(nodeType);
        Map<String, Object> matchValues = new HashMap<>();
        SchemaPath nodeTypeWithoutRevision = m_schemaRegistry.stripRevisions(nodeType);
        matchValues.put(SCHEMA_PATH, SchemaPathUtil.toString(nodeTypeWithoutRevision));

        List entities = getEntityDataStoreManager(entityClass).findByMatchValue(entityClass, matchValues, m_entityRegistry.getOrderByFieldName(entityClass));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("listNodes with nodeType: %s, got: %s entities",nodeType, entities.size()));
        }

        for(Object entity : entities){
            ModelNode modelNode = null;
            try {
                modelNode = m_entityToModelNodeMapper.getModelNode(entity, this);
            } catch (ModelNodeMapperException e) {
                throw new DataStoreException(e);
            }
            modelNodes.add(modelNode);
        }
        return modelNodes;
    }

    @Override
    public List<ModelNode> listChildNodes(SchemaPath childType, ModelNodeId parentId) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("listChildNodes result for childType: %s, parentId: %s", childType, parentId.getModelNodeIdAsString()));
        }
        List<ModelNode> children = new ArrayList<>();
        Collection<Object> entities = getChildEntities(childType,parentId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("listChildNodes result for childType: %s, parentId: %s, got: %s entities",childType, parentId.getModelNodeIdAsString(), entities.size()));
        }
        for(Object child : entities){
            ModelNode childMN = null;
            try {
                childMN = m_entityToModelNodeMapper.getModelNode(child, this);
            } catch (ModelNodeMapperException e) {
                throw new DataStoreException(e);
            }
            children.add(childMN);
        }
        return children;
    }

    public ModelNodeWithAttributes findNode(SchemaPath nodeType, ModelNodeKey key, ModelNodeId parentId) {
        if (LOGGER.isDebugEnabled()) {    LOGGER.debug( String.format("findNode with nodeType: %s, key: %s, parentId: %s ",nodeType, key, parentId.getModelNodeIdAsString()));}
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        if(klass != null){
            Object pk = buildPrimaryKey(nodeType, klass, key, parentId);
            Object entity = getEntityDataStoreManager(klass).findById(klass, pk,LockModeType.PESSIMISTIC_READ);
            if(entity == null){
                return null;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("foundNode with nodeType: %s, key: %s, parentId: %s", nodeType, key, parentId.getModelNodeIdAsString()));
            }
            try {
                return m_entityToModelNodeMapper.getModelNode(entity, this);
            } catch (ModelNodeMapperException e) {
                throw new DataStoreException(e);
            }
        }
        return null;

    }

    @Override
    public List<ModelNode> findNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId parentId) throws
            DataStoreException {
        LOGGER.debug("findNodes with nodeType: {} matchCriteria: {} parentId: {}", nodeType, matchCriteria, parentId);
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        List<Object> entities = getEntities(klass, matchCriteria, parentId);
        List<ModelNode> nodes = getModelNodes(entities);
        return nodes;
    }

    private List<ModelNode> getModelNodes(List<Object> entities) {
        List<ModelNode> nodes = new ArrayList<>();
        for(Object entity : entities){
            try {
                nodes.add(m_entityToModelNodeMapper.getModelNode(entity, this));
            } catch (ModelNodeMapperException e) {
                throw new DataStoreException(e);
            }
        }
        return nodes;
    }

    protected List<Object> getEntities(Class storedParentClass, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId parentId) throws
            DataStoreException {
        Map<String, Object> matchValues = new HashMap<>();
        String yangParentId = m_entityRegistry.getYangParentIdFieldName(storedParentClass);
        if (yangParentId == null || yangParentId.isEmpty()){
            matchValues.put(YangParentId.PARENT_ID_FIELD_NAME, parentId.getModelNodeIdAsString());
        }else{
            matchValues.put(yangParentId, parentId.getModelNodeIdAsString());
        }
        for(Map.Entry<QName, ConfigLeafAttribute> entry : matchCriteria.entrySet()){
            String fieldName = m_entityRegistry.getFieldName(storedParentClass, entry.getKey());
            if(fieldName == null){
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("DSM could not find the field : {}",entry.getKey().toString());
                }
                continue;
            }
            matchValues.put(fieldName, entry.getValue().getStringValue());
        }
        return getEntityDataStoreManager(storedParentClass).findByMatchValue(storedParentClass, matchValues, m_entityRegistry.getOrderByFieldName(storedParentClass));
    }
    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) {
        return createNode(modelNode, parentId, -1);
    }

    @Override
	public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId, int insertIndex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createNode : {} parentId: {}", modelNode.getModelNodeSchemaPath(),parentId);
        }
        if (modelNode!=null) {
            SchemaPath nodeType = modelNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(nodeType);

            if (insertIndex != -1) {
                String columnName = m_entityRegistry.getOrderByFieldName(klass);
                Map<String, Object> matchValues = new HashMap<>();
                SchemaPath nodeTypeWithoutRevision = m_schemaRegistry.stripRevisions(nodeType);
                matchValues.put(SCHEMA_PATH, SchemaPathUtil.toString(nodeTypeWithoutRevision));
                if (columnName != null) {
                    matchValues.put(columnName, new Double(insertIndex));
                }
                List entities = getEntityDataStoreManager(klass).findByMatchValue(klass, matchValues, m_entityRegistry.getOrderByFieldName(klass));
                if(entities != null && !entities.isEmpty()) {
                    incrementOrderByColumnForExistingEntities(klass, columnName, insertIndex);
                }
            }

            Object newEntity = m_entityToModelNodeMapper.getEntity(nodeType, modelNode, klass, parentId, insertIndex);
            LOGGER.debug("creating entity {} for parentId {}",newEntity, parentId);
            if (newEntity !=null) {
                createEntityAndUpdateParent(newEntity, nodeType, klass, parentId);
            }
            //flush to make sure newly created objects are flushed
            getEntityDataStoreManager(klass).getEntityManager().flush();
        }
        return modelNode;
	}

    @SuppressWarnings("unchecked")
	protected void incrementOrderByColumnForExistingEntities(Class klass, String columnName, int newInsertIndex) {
    	List<Object> existingEntities = getEntityDataStoreManager(klass).findRange(klass, columnName, PredicateCondition.GREATER_THAN_EQUAL, new Double(newInsertIndex));
    	if (existingEntities != null && !existingEntities.isEmpty()) {
            for (Object entity : existingEntities){
                Method childOrderBySetter = m_entityRegistry.getOrderByUserSetter(entity.getClass());
                Method childOrderByGetter = m_entityRegistry.getOrderByUserGetter(entity.getClass());

                if (childOrderByGetter != null && childOrderBySetter != null){
                	try {
						Integer currentIndex = (Integer) childOrderByGetter.invoke(entity);
						currentIndex++;
						childOrderBySetter.invoke(entity, currentIndex);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new DataStoreException(e);
					}
                }
            }
        }
    }

    @Override
    @Transactional(value=TxType.REQUIRED,rollbackOn={DataStoreException.class,RuntimeException.class,Exception.class})
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName,ConfigLeafAttribute> configAttributes,
                           Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("updateNode : {} parentId: {}",modelNode.getModelNodeSchemaPath(),parentId);
        }
        updateNode(modelNode, parentId, configAttributes, leafListAttributes, -1, removeNode);
    }

	@Override
    @Transactional(value=TxType.REQUIRED,rollbackOn={DataStoreException.class,RuntimeException.class})
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName,ConfigLeafAttribute> configAttributes,
                           Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, int insertIndex, boolean removeNode) {
	    if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("updateNode : {}, parentId: {}",modelNode.getModelNodeSchemaPath() ,parentId);
	    }
        if (modelNode!=null) {
        	ModelNodeWithAttributes entityModelNode = null;
            Object entity = findEntity(modelNode, parentId);
            if(entity!=null){
                try {
                    entityModelNode = m_entityToModelNodeMapper.getModelNode(entity, this);
                } catch (ModelNodeMapperException e) {
                    throw new DataStoreException(e);
                }
                if (configAttributes != null) {
                    entityModelNode.updateConfigAttributes(configAttributes);
                }
                if (leafListAttributes != null) {
                    //issue delete on all of leafList entries which are being updated.
                    m_entityToModelNodeMapper.clearLeafLists(entity, leafListAttributes.keySet());
                    getEntityDataStoreManager(entity.getClass()).flush();
                    if (removeNode){
                        entityModelNode.removeLeafListAttributes(leafListAttributes);
                    } else {
                        entityModelNode.updateLeafListAttributes(leafListAttributes);
                    }
                }
                m_entityToModelNodeMapper.updateEntity(entity, modelNode.getModelNodeSchemaPath(), entityModelNode,
                        m_entityRegistry.getEntityClass(modelNode.getModelNodeSchemaPath()), parentId, insertIndex);
            } else {
            	entityModelNode = (ModelNodeWithAttributes) modelNode;
            	createNode(entityModelNode, parentId, insertIndex);
            }
        }
    }

    @Override
    public void removeNode(ModelNode modelNode, ModelNodeId parentId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeNode : {} parentId: {}",modelNode.getModelNodeSchemaPath(),parentId);
        }
        Object entity = findEntity(modelNode, parentId);
        Class <?> klass = null;
        if (entity != null) {
            klass = entity.getClass();
            deleteEntityAndUpdateParent(entity, modelNode.getModelNodeSchemaPath(), parentId);
        }
        //flush to make sure deleted objects are flushed
        getEntityDataStoreManager(klass).getEntityManager().flush();
    }

    /**
     * Removes all children with the childQName from the modelNode with parentId
     * @param parentNode
     * @param nodeType
     * @param grandParentId
     * @throws DataStoreException
     */
    @Override
    public void removeAllNodes(ModelNode parentNode, SchemaPath nodeType, ModelNodeId grandParentId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeAllNodes : {} parentId: {}",parentNode.getModelNodeSchemaPath(), grandParentId);
        }
        Object entity = findEntity(parentNode, grandParentId);
        if (entity!=null) {
            deleteAllChildEntities(entity, nodeType.getLastComponent());
        }
    }

    protected Collection<Object> getChildEntities(SchemaPath childType, ModelNodeId parentId) {
        Class childEntityClass = m_entityRegistry.getEntityClass(childType);
        Map<String, Object> matchBySchemaPathAndParentId = new HashMap<String, Object>();
        matchBySchemaPathAndParentId.put(m_entityRegistry.getYangParentIdFieldName(childEntityClass), parentId.getModelNodeIdAsString());
        return getEntityDataStoreManager(childEntityClass).findByMatchValue(childEntityClass, matchBySchemaPathAndParentId);
    }

    private Object findEntity(ModelNode modelNode, ModelNodeId parentId) {
        if (modelNode!=null) {
            SchemaPath nodeType = modelNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(nodeType);
            if (klass != null) {
                Object pk = buildPrimaryKey(nodeType, klass, MNKeyUtil.getModelNodeKey(modelNode, m_schemaRegistry), parentId);
                Object entity = getEntityDataStoreManager(klass).findById(klass, pk,LockModeType.PESSIMISTIC_WRITE);
                return entity;
            }
        }
        return null;
    }

    private Object buildPrimaryKey(SchemaPath path, Class klass, ModelNodeKey keys, ModelNodeId parentId) {
        return EMNKeyUtil.buildPrimaryKey(klass,parentId, keys, m_entityRegistry, getEntityDataStoreManager(klass));
    }

    private EntityDataStoreManager getEntityDataStoreManager(Class klass) {
        if (klass != null) {
            EntityDataStoreManager entityDSM = m_modelNodeDSMRegistry.getEntityDSM(klass);
            if (entityDSM != null) {
                return entityDSM;
            }
        }
        return m_persistenceManagerUtil.getEntityDataStoreManager();
    }

    private void createEntityAndUpdateParent(Object newEntity, SchemaPath nodeSchemaPath, Class klass, ModelNodeId modelNodeId)
            {
        DataSchemaNode parentNode = m_schemaRegistry.getNonChoiceParent(nodeSchemaPath);
        SchemaPath parentSchemaPath = null;
        if (parentNode != null) {
            parentSchemaPath = parentNode.getPath();
        }
        if (parentSchemaPath != null) {
            if (hasDirectEntityRelationWithParent(newEntity)) {
                Class parentKlass = m_entityRegistry.getEntityClass(parentSchemaPath);
                if (parentKlass != null) {
                    Object parentPK = buildPrimaryKey(parentSchemaPath, parentKlass,
                            MNKeyUtil.getModelNodeKey(m_schemaRegistry, parentSchemaPath, modelNodeId),
                            EMNKeyUtil.getParentId(m_schemaRegistry, parentSchemaPath, modelNodeId));
                    Object parentEntity = getEntityDataStoreManager(parentKlass).findById(parentKlass, parentPK, LockModeType.PESSIMISTIC_READ);
                    if (parentEntity != null) {
                        getChildSetAndAddChild(parentEntity, newEntity, parentKlass, klass, m_schemaRegistry.getDataSchemaNode(nodeSchemaPath));
                    }
                }
            }
        }
        persistEntity(newEntity);
    }

    private void persistEntity(Object entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("persisting entity object with hashcode {} and entity is  {}",entity.hashCode(), entity);
        }
        getEntityDataStoreManager(entity.getClass()).persist(entity);
    }

    private void deleteEntityAndUpdateParent(Object entity, SchemaPath nodeSchemaPath, ModelNodeId modelNodeId) {
        if(hasDirectEntityRelationWithParent(entity)){
            SchemaPath parentSchemaPath = getDataParentSchemaPath(m_schemaRegistry, nodeSchemaPath);
            if (parentSchemaPath!=null) {
                Class parentKlass = m_entityRegistry.getEntityClass(parentSchemaPath);
                if (parentKlass != null) {
                    Object parentPK = buildPrimaryKey(parentSchemaPath, parentKlass, MNKeyUtil.getModelNodeKey(m_schemaRegistry, parentSchemaPath, modelNodeId),
                            EMNKeyUtil.getParentId(m_schemaRegistry, parentSchemaPath, modelNodeId));
                    Object parentEntity = getEntityDataStoreManager(parentKlass).findById(parentKlass, parentPK, LockModeType.PESSIMISTIC_WRITE);
                    if (parentEntity != null) {
                        DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(nodeSchemaPath);
                        if(schemaNode instanceof ListSchemaNode) {
                            Collection<Object> childSet = m_entityToModelNodeMapper.getChildEntities(parentEntity, nodeSchemaPath.getLastComponent());
                            childSet.remove(entity);

                        }else if (schemaNode instanceof ContainerSchemaNode){
                            Map<QName, Method> yangChildSetters = m_entityRegistry.getYangChildSetters(parentKlass);
                            if (yangChildSetters != null && !yangChildSetters.isEmpty()) {
                                Method setter = yangChildSetters.get(nodeSchemaPath.getLastComponent());
                                try {
                                    setter.invoke(parentEntity, new Object[]{ null });
                                } catch (IllegalAccessException |InvocationTargetException e) {
                                    throw new DataStoreException("Error while deleting child container ", e);
                                }
                            }
                        }
                    }
                }
            }else{
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("parentSchemaPath is null for the nodeType  :"+ nodeSchemaPath.toString() +
                            ". Deleting entity "+ entity.toString());
                }
            }
        }

        deleteEntity(entity);
    }

    private boolean hasDirectEntityRelationWithParent(Object entity) {
        return !m_entityRegistry.classHasYangParentSchemaPathAnnotation(entity.getClass());
    }

    private void deleteAllChildEntities(Object entity, QName qName) {
        Collection<Object> childSet = m_entityToModelNodeMapper.getChildEntities(entity, qName);
        childSet.clear();
    }

    @SuppressWarnings("unchecked")
	private Object getChildSetAndAddChild(Object parentEntity, Object childEntity, Class parentKlass, Class klass, DataSchemaNode schemaNode) {
        if (schemaNode instanceof ListSchemaNode) {
            Map<QName, Method> childGetters = m_entityRegistry.getYangChildGetters(parentKlass);
            QName childQName = m_entityRegistry.getQName(klass);
            Collection<Object> childSet;
            if (childGetters!=null && !childGetters.isEmpty() && childQName!=null) {
                try {
                    Method childGetter = childGetters.get(childQName);
                    if (childGetter!=null) {
                        childSet = (Collection<Object>) childGetter.invoke(parentEntity);
                        childSet.add(childEntity);
                        return parentEntity;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if(schemaNode instanceof ContainerSchemaNode){
            Map<QName, Method> childSetters = m_entityRegistry.getYangChildSetters(parentKlass);
            QName childQName = m_entityRegistry.getQName(klass);
            if (childSetters!=null && !childSetters.isEmpty() && childQName!=null) {
                try{
                    Method childSetter = childSetters.get(childQName);
                    if(childSetter!=null){
                        childSetter.invoke(parentEntity,childEntity);
                        return parentEntity;
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if(schemaNode instanceof LeafListSchemaNode) {
        	Map<QName, Method> childLeafListGetters = m_entityRegistry.getYangLeafListGetters(parentKlass);
        	Map<QName, Method> childLeafListSetters = m_entityRegistry.getYangLeafListSetters(parentKlass);
            QName childQName = m_entityRegistry.getQName(klass);
            Collection<Object> childSet;
            if (childLeafListGetters!=null && !childLeafListGetters.isEmpty() && childLeafListSetters!=null && !childLeafListSetters.isEmpty() && childQName!=null) {
                try {
                    Method childLeafListGetter = childLeafListGetters.get(childQName);
                    if (childLeafListGetter!=null) {
                        childSet = (Collection<Object>) childLeafListGetter.invoke(parentEntity);
                        if (childSet != null) {
                        	childSet.add(childEntity);
                        } else {
                        	Method childLeafListSetter = childLeafListSetters.get(childQName);
                        	Set<Object> child = new LinkedHashSet<Object>();
                        	child.add(childEntity);
                        	childLeafListSetter.invoke(parentEntity, child);
                        }
                        return parentEntity;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private void deleteEntity(Object entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("deleting entity object with hashcode %s and entity is  %s",entity.hashCode(), entity));
        }
        getEntityDataStoreManager(entity.getClass()).delete(entity);
    }
    
    @Override
    public boolean isChildTypeBigList(SchemaPath nodeType) {
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        
        return m_entityRegistry.getBigListType(klass);
    }

    @Override
    public List<ListEntryInfo> findNodesLike(SchemaPath nodeType, ModelNodeId parentId, Map<QName, String> keysLike, int maxResults) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("finding nodes of type {} with parentId {}, keySearch {}, maxResults {}", nodeType, parentId, keysLike, maxResults);
        }
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        if(klass == null){
            throw new IllegalArgumentException(String.format("Nodes of type {} cannot be searched", nodeType));
        }
        Map<String, String> columnKeyMap = EMNKeyUtil.getColumnKeyMap(klass, m_entityRegistry, keysLike, parentId);
        Map<String, Object> parentIdMatchMap = EMNKeyUtil.getParentIdMatchMap(klass, m_entityRegistry, parentId);
        List entries = getEntityDataStoreManager(klass).findByLikeWithFieldsContain(klass, columnKeyMap, parentIdMatchMap, maxResults);
        List<ModelNodeWithAttributes> modelNodes = getNodes(entries);
        return prepareEntryInfos(modelNodes);
    }

    private List<ListEntryInfo> prepareEntryInfos(List<ModelNodeWithAttributes> modelNodes) {
        List<ListEntryInfo> matchingEntries = new ArrayList<>();
        for(ModelNodeWithAttributes node : modelNodes){
            Map<QName, ConfigLeafAttribute> keys = node.getKeyAttributes();
            ListEntryInfo info = new ListEntryInfo();
            for(Map.Entry<QName, ConfigLeafAttribute> key : keys.entrySet()){
                String moduleName = m_schemaRegistry.getModuleByNamespace(key.getKey().getNamespace().toString()).getName();
                info.getKeys().put(new ListEntryQName(moduleName, key.getKey().getLocalName()), key.getValue().getStringValue());
            }
            matchingEntries.add(info);
        }
        return matchingEntries;
    }

    private List<ModelNodeWithAttributes> getNodes(List entries) {
        List<ModelNodeWithAttributes> modelNodes = new ArrayList<>();
        for(Object entry : entries) {
            ModelNodeWithAttributes modelNode = m_entityToModelNodeMapper.getModelNode(entry, this);
            modelNodes.add(modelNode);
        }
        return modelNodes;
    }

    
    public List findByMatchValues(SchemaPath nodeType, Map<String, Object> matchValues){
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        if(klass == null){
            throw new IllegalArgumentException(String.format("Nodes of type {} cannot be searched", nodeType));
        }
        List entries = getEntityDataStoreManager(klass).findByMatchValue(klass, matchValues);
        return entries;
    }
    
    @Override
    public EntityRegistry getEntityRegistry(SchemaPath nodeType){
        return m_entityRegistry;
    }
}
