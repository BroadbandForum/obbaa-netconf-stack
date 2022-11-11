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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.documentToPrettyString;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger.endPhase;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger.startPhase;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMUtils.getXmlSubtree;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMUtils.setVisibility;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeToXmlMapper.nodesMatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InvalidIdentityRefException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationMountContext;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A ModelNodeDataStoreManager, which understands Subtree XML annotations.
 */
public class XmlSubtreeDSM extends AnnotationBasedModelNodeDataStoreManager {

    protected EntityRegistry m_entityRegistry;
    private final PersistenceManagerUtil m_persistenceManagerUtil;
    private SchemaRegistry m_schemaRegistry;
    protected final XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(XmlSubtreeDSM.class, LogAppNames.NETCONF_STACK);
    private final RequestScopeXmlDSMCache m_dsmCache;
    private final ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final BundleContext m_bundleContext;

    public XmlSubtreeDSM(PersistenceManagerUtil persistenceManagerUtil, EntityRegistry entityRegistry, SchemaRegistry schemaRegistry,
                         ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry, ModelNodeDSMRegistry modelNodeDSMRegistry) {
        super(persistenceManagerUtil, entityRegistry, schemaRegistry, modelNodeHelperRegistry, subsystemRegistry, modelNodeDSMRegistry);
        if (modelNodeHelperRegistry != null) {
            m_modelNodeHelperRegistry = modelNodeHelperRegistry.unwrap();
        } else {
            m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        }
        m_persistenceManagerUtil = persistenceManagerUtil;
        if (entityRegistry != null) {
            m_entityRegistry = entityRegistry.unwrap();
        } else {
            m_entityRegistry = entityRegistry;
        }
        if (schemaRegistry != null) {
            m_schemaRegistry = schemaRegistry.unwrap();
        } else {
            m_schemaRegistry = schemaRegistry;
        }
        m_dsmCache = new RequestScopeXmlDSMCache();
        m_modelNodeDSMRegistry = modelNodeDSMRegistry;
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry, modelNodeHelperRegistry, subsystemRegistry, m_entityRegistry, this);
        m_bundleContext = getBundleContext();
        try {
            if(m_bundleContext != null) {
                m_bundleContext.addServiceListener(m_schemaRegistrySL, "(" + Constants.OBJECTCLASS + "=" + SchemaRegistry.class.getName() + ")");
                m_bundleContext.addServiceListener(m_entityRegistrySL, "(" + Constants.OBJECTCLASS + "=" + EntityRegistry.class.getName() + ")");
                m_bundleContext.addServiceListener(m_modelNodeHelperRegistrySL, "(" + Constants.OBJECTCLASS + "=" + ModelNodeHelperRegistry.class.getName() + ")");
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private BundleContext getBundleContext() {
        Bundle bundle = FrameworkUtil.getBundle(XmlSubtreeDSM.class);
        if(bundle != null) {
            return bundle.getBundleContext();
        }
        return null;
    }

    private ServiceListener m_schemaRegistrySL = event -> {
        try {
            ServiceReference<?> ref = event.getServiceReference();
            Bundle bundle = ref.getBundle();
            Object service = bundle.getBundleContext().getService(ref);

            switch (event.getType()) {
                case ServiceEvent.UNREGISTERING:
                    if (service instanceof SchemaRegistry) {
                        m_schemaRegistry = null;
                    }
                    break;
                case ServiceEvent.REGISTERED: {
                    if (service instanceof SchemaRegistry) {
                        m_schemaRegistry = ((SchemaRegistry) service).unwrap();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(null, "Error when registering/unregistering schema registry service", e);
            throw new RuntimeException(e);
        }
    };

    private ServiceListener m_entityRegistrySL = event -> {
        try {
            ServiceReference<?> ref = event.getServiceReference();
            Bundle bundle = ref.getBundle();
            Object service = bundle.getBundleContext().getService(ref);

            switch (event.getType()) {
                case ServiceEvent.UNREGISTERING:
                    if (service instanceof EntityRegistry) {
                        m_entityRegistry = null;
                    }
                    break;
                case ServiceEvent.REGISTERED: {
                    if (service instanceof EntityRegistry) {
                        m_entityRegistry = ((EntityRegistry) service).unwrap();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(null, "Error when registering/unregistering entity registry service", e);
            throw new RuntimeException(e);
        }
    };

    private ServiceListener m_modelNodeHelperRegistrySL = event -> {
        try {
            ServiceReference<?> ref = event.getServiceReference();
            Bundle bundle = ref.getBundle();
            Object service = bundle.getBundleContext().getService(ref);

            switch (event.getType()) {
                case ServiceEvent.UNREGISTERING:
                    if (service instanceof ModelNodeHelperRegistry) {
                        m_modelNodeHelperRegistry = null;
                    }
                    break;
                case ServiceEvent.REGISTERED: {
                    if (service instanceof ModelNodeHelperRegistry) {
                        m_modelNodeHelperRegistry = ((ModelNodeHelperRegistry) service).unwrap();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(null, "Error when registering/unregistering modelnode helper registry service", e);
            throw new RuntimeException(e);
        }
    };

    @Override
    public List<ModelNode> listNodes(SchemaPath nodeType, SchemaRegistry mountRegistry) throws DataStoreException {
        Class entityClass = m_entityRegistry.getEntityClass(nodeType);
        if(entityClass!=null && m_entityRegistry.getYangXmlSubtreeGetter(entityClass) == null){
            return super.listNodes(nodeType, mountRegistry);
        }else{
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("listNodes with nodeType: {}",nodeType);
            }
            List<ModelNode> modelNodes = new ArrayList<>();
            SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(nodeType, mountRegistry);
            if (storedParentSchemaPath!=null){
                ModelNodeId storedGrandParentId = EMNKeyUtil.getParentIdFromSchemaPath(storedParentSchemaPath); //FIXME: FNMS-10112 This wont work
                // when stored parent is a list!
                DataSchemaNode storeSchemaNode = m_schemaRegistry.getDataSchemaNode(storedParentSchemaPath);
                if(storeSchemaNode instanceof ListSchemaNode){
                    throw new IllegalArgumentException("Stored schema node is a list node, hence listNodes API will not work here, use findNodes API instead");
                }
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode(storedParentSchemaPath,storedGrandParentId,
                        storedGrandParentId, nodeType); //FIXME:  FNMS-10112  This is clearly wrong
                if (storedParentModelNode != null) {
                    if (nodeType.equals(storedParentSchemaPath)) {
                        modelNodes.add(storedParentModelNode);
                    }else {
                        modelNodes = retrieveModelNodes(nodeType, storedParentModelNode, null);
                    }
                }
            }
            return modelNodes;
        }
    }

    @Override
    public List<ModelNode> listChildNodes(SchemaPath childType, ModelNodeId parentId, SchemaRegistry mountRegistry) throws DataStoreException {
        Class entityClass = m_entityRegistry.getEntityClass(childType);
        if(entityClass!=null ) {
            if (m_entityRegistry.getYangXmlSubtreeGetter(entityClass) == null) {
                return super.listChildNodes(childType, parentId, mountRegistry);
            }else{
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("listChildNodes with childType: {} parentId: {}",childType,parentId);
                }
                // This is a list of stored ModelNodes
                List<ModelNode> modelNodes = new ArrayList<>();
                Collection<Object> childEntities = super.getChildEntities(childType, parentId);
                for(Object entity: childEntities){
                    XmlModelNodeImpl xmlModelNode = m_xmlModelNodeToXmlMapper.getModelNode(entity, this);
                    modelNodes.add(xmlModelNode);
                }
                return modelNodes;
            }
        }else {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("listChildNodes with childType: {} parentId: {}",childType,parentId);
            }
            List<ModelNode> modelNodes = new ArrayList<>();
            SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(childType, mountRegistry);
            if(storedParentSchemaPath != null){
	            ModelNodeId storedGrandParentId = EMNKeyUtil.scopeModelNodeId(m_schemaRegistry, storedParentSchemaPath.getParent(), parentId);
	            XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode(storedParentSchemaPath, parentId, storedGrandParentId, childType);
	            if (storedParentModelNode != null) {
	                modelNodes = retrieveModelNodes(childType, storedParentModelNode, parentId);
	            }
            }
            return modelNodes;
        }
    }

    @Override
    public ModelNodeWithAttributes findNode(SchemaPath nodeType, ModelNodeKey key, ModelNodeId parentId, SchemaRegistry mountRegistry) throws DataStoreException {
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.findNode(nodeType, key, parentId, mountRegistry);
        }

        LOGGER.debug( "findNode with nodeType: {} key: {} parentId: {}",nodeType,key,parentId);
        startPhase("XmlSubtreeDSM.findNode.getModelNodeId");
        ModelNodeId modelNodeId = EMNKeyUtil.getModelNodeId(key, parentId, nodeType);
        endPhase("XmlSubtreeDSM.findNode.getModelNodeId", false);
        startPhase("XmlSubtreeDSM.findNode.getStoredParentSchemaPath");
        //SchemaPath of the parent which is stored as an Entity in DS
        SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(nodeType, mountRegistry);
        if (storedParentSchemaPath == null) {
            SchemaPath mountPath = (SchemaPath) RequestScope.getCurrentScope().getFromCache(SchemaRegistryUtil.MOUNT_PATH);
            if (mountPath != null) {
                storedParentSchemaPath = getStoredParentSchemaPath(mountPath, mountRegistry);
            }
        }
        if(storedParentSchemaPath == null){
            storedParentSchemaPath = nodeType;
        }
        endPhase("XmlSubtreeDSM.findNode.getStoredParentSchemaPath", false);
		if (storedParentSchemaPath != null) {
			// ParentId of the storedParent
            startPhase("XmlSubtreeDSM.findNode.scopeModelNodeId");
			ModelNodeId storedGrandParentId = EMNKeyUtil.scopeModelNodeId(m_schemaRegistry,
					storedParentSchemaPath.getParent(), modelNodeId);
            endPhase("XmlSubtreeDSM.findNode.scopeModelNodeId", false);
            startPhase("XmlSubtreeDSM.findNode.getStoredParentModelNode");
			XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode(storedParentSchemaPath, modelNodeId,
					storedGrandParentId, nodeType);
            endPhase("XmlSubtreeDSM.findNode.getStoredParentModelNode", false);
			if (nodeType.equals(storedParentSchemaPath)) {
				return storedParentModelNode;
			} else if (storedParentModelNode != null) {
			    try {
                    startPhase("XmlSubtreeDSM.findNode.retrieveModelNode");
                    return retrieveModelNode(nodeType, modelNodeId, storedParentModelNode);
                } finally {
                    endPhase("XmlSubtreeDSM.findNode.retrieveModelNode", false);
                }

			}
		}
		return null;
    }

    @Override
    public List<ModelNode> findNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId parentId, SchemaRegistry mountRegistry) throws DataStoreException {
        Class klass = m_entityRegistry.getEntityClass(nodeType);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.findNodes(nodeType, matchCriteria, parentId, mountRegistry);
        }
        List<ModelNode> nodes = new ArrayList<>();
        LOGGER.debug("findNodes with nodeType: {} matchCriteria: {} parentId: {}", nodeType, matchCriteria, parentId);

        if(MNKeyUtil.containsAllKeys(nodeType, matchCriteria, mountRegistry)){
            LOGGER.debug("all keys found in matchCriteria, for findNodes with nodeType: {} matchCriteria: {} parentId: {}", nodeType,
                    matchCriteria, parentId);
            ModelNodeKey key = MNKeyUtil.getKeyFromCriteria(nodeType, matchCriteria, mountRegistry);
            ModelNodeWithAttributes node = findNode(nodeType, key, parentId, mountRegistry);
            if(node != null) {
                nodes.add(node);
            }
        }else {
            LOGGER.debug("all keys not found in matchCriteria, for findNodes with nodeType: {} matchCriteria: {} parentId: {}",
                    nodeType, matchCriteria, parentId);
            //SchemaPath of the parent which is stored as an Entity in DS
            SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(nodeType, mountRegistry);
            if (storedParentSchemaPath == null) {
                SchemaPath mountPath = (SchemaPath) RequestScope.getCurrentScope().getFromCache(SchemaRegistryUtil.MOUNT_PATH);
                if (mountPath != null) {
                    storedParentSchemaPath = getStoredParentSchemaPath(mountPath, mountRegistry);
                }
            }
            //ParentId of the storedParent
            ModelNodeId storedGrandParentId = EMNKeyUtil.scopeModelNodeId(m_schemaRegistry, storedParentSchemaPath.getParent(), parentId);
            Class storedParentClass = m_entityRegistry.getEntityClass(storedParentSchemaPath);
            List<?> storedParentEntities;
            if(nodeType.equals(storedParentSchemaPath)){
                //we cannot scope to a single parent here because the matchCriteria does not contain all keys
                storedParentEntities = getParentEntities(storedParentClass, matchCriteria, parentId);
                if (storedParentEntities != null) {
                    for(Object storedParentEntity : storedParentEntities){
                    	HelperDrivenModelNode.checkForGetTimeOut();
                        String yangXmlSubtree = "";
                        boolean xmlLoaded = false;
                        if(shouldEagerlyLoadXmlData(nodeType, storedParentClass)) {
                            startPhase("XmlSubtreeDSM.findNodes.getXmlSubtree");
                            yangXmlSubtree = getXmlSubtree(storedParentEntity, storedParentClass, m_entityRegistry);
                            endPhase("XmlSubtreeDSM.findNodes.getXmlSubtree", false);
                            xmlLoaded = true;
                        }
                        startPhase("XmlSubtreeDSM.findNodes.string2Dom");
                        Element element = string2Dom(storedParentSchemaPath, storedParentEntity, yangXmlSubtree);
                        endPhase("XmlSubtreeDSM.findNodes.string2Dom", false);
                        Map<QName, ConfigLeafAttribute> configAttrsFromEntity = null;
                        Map<QName, LinkedHashSet<ConfigLeafAttribute>> configLeafListsFromEntity = null;
                        try {
                            configAttrsFromEntity = XmlModelNodeToXmlMapperImpl.getConfigAttributesFromEntity(m_schemaRegistry, storedParentSchemaPath,
                                    m_entityRegistry, storedParentClass, storedParentEntity);
                            configLeafListsFromEntity = DSMUtils.getConfigLeafListsFromEntity(m_schemaRegistry, storedParentSchemaPath,
                                    m_entityRegistry, storedParentClass, storedParentEntity);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        List<XmlModelNodeImpl> storedParentModelNodes = m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(xmlLoaded, element,
                                configAttrsFromEntity, configLeafListsFromEntity, storedParentSchemaPath, storedGrandParentId, null, this, storedParentEntity, storedParentSchemaPath);
                        for(XmlModelNodeImpl storedParentModelNode: storedParentModelNodes){
                            fillNodes(nodeType, matchCriteria, parentId, nodes, storedParentSchemaPath, storedParentModelNode);
                            setVisibility(m_entityRegistry, storedParentEntity, storedParentModelNode);
                        }
                    }
                }
            }else{
                //we can scope to a single stored parent here since we have parentId
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode(storedParentSchemaPath, parentId, storedGrandParentId, nodeType);
                fillNodes(nodeType, matchCriteria, parentId, nodes, storedParentSchemaPath, storedParentModelNode);
            }
        }
        //this is needed in case there are config attributes for the node as columns and as xml subtree content
        doFinalFilter(nodes, matchCriteria);
        return nodes;
    }

    private void fillNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId parentId, List<ModelNode>
            nodes, SchemaPath storedParentSchemaPath, XmlModelNodeImpl storedParentModelNode) {
        if(storedParentModelNode != null){
            if(nodeType.equals(storedParentSchemaPath)) {
                nodes.add(storedParentModelNode);
            } else{
                nodes.addAll(retrieveModelNodes(nodeType, matchCriteria, storedParentModelNode, parentId));
            }
        }
    }

    private void doFinalFilter(List<ModelNode> nodes, Map<QName, ConfigLeafAttribute> matchCriteria) {
        Iterator<ModelNode> nodeIterator = nodes.iterator();
        while(nodeIterator.hasNext()){
            ModelNode node = nodeIterator.next();
            if(!MNKeyUtil.isMatch(matchCriteria, (ModelNodeWithAttributes) node, m_schemaRegistry)){
                LOGGER.debug("node: {}, did not match the match criteria matchCriteria: {} ",node, matchCriteria);
                nodeIterator.remove();
            }
        }
    }

    private List<ModelNodeWithAttributes> retrieveModelNodes(SchemaPath nodeType, Map<QName, ConfigLeafAttribute> matchCriteria, XmlModelNodeImpl
            storedParentModelNode, ModelNodeId parentId) {
        List<ModelNodeWithAttributes> nodes = new ArrayList<>(200);
        XmlModelNodeImpl parentModelNode = retrieveModelNode(nodeType.getParent(), parentId, storedParentModelNode);
        if (parentModelNode != null) {
            IndexedList<ModelNodeId, XmlModelNodeImpl> children = parentModelNode.getChildren().get(nodeType.getLastComponent());
            if (children != null) {
                for (XmlModelNodeImpl child:children.list()) {
                    if (MNKeyUtil.isMatch(matchCriteria, child, parentModelNode.getSchemaRegistry())) {
                        nodes.add(child);
                    }
                }
            }
        }
        return nodes;
    }

    protected List<? extends Object> getParentEntities(Class storedParentClass, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId parentId) throws
            DataStoreException {
        return getEntities(storedParentClass, matchCriteria, parentId);
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException{
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createNode : {} parentId: {}",modelNode.getModelNodeSchemaPath(),parentId);
        }
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        Class klass = m_entityRegistry.getEntityClass(schemaPath);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.createNode(modelNode, parentId);
        } else {
            if(modelNode.isRoot()){
                XmlModelNodeImpl xmlmodelNode = m_xmlModelNodeToXmlMapper.getRootXmlModelNode((ModelNodeWithAttributes)modelNode, this);
                super.createNode(xmlmodelNode, parentId);
                markNodesToBeUpdated(modelNode.getModelNodeSchemaPath(), xmlmodelNode);
                return xmlmodelNode;
            }

            XmlModelNodeImpl parentModelNode = null;
            if (modelNode instanceof XmlModelNodeImpl) {
                parentModelNode = ((XmlModelNodeImpl) modelNode).getParentModelNode();
            }

            if (parentModelNode == null) {
                super.createNode(modelNode, parentId); // EntityModelNode DSM can handle this.
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath, modelNode.getSchemaRegistry());
                if (storedParentSchemaPath == null) {
                    storedParentSchemaPath = getStoredParentSchemaPath(parentModelNode.getModelNodeSchemaPath(), modelNode.getSchemaRegistry());
                }
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) modelNode);
                if (storedParentModelNode != null) {
                    parentModelNode.addChild(modelNode.getQName(), (XmlModelNodeImpl) modelNode);
                    markNodesToBeUpdated(
                            storedParentSchemaPath == null ? storedParentModelNode.getModelNodeSchemaPath() : storedParentSchemaPath,
                            storedParentModelNode);
                }
            }
        }
        return modelNode;
    }

    @Override
    public void endModify() {
        LOGGER.debug("Updating the modified stored parent XML subtree nodes from cache into hibernate context");
        for(XmlModelNodeImpl nodeToBeUpdated: m_dsmCache.getNodesToBeUpdated()){
            startPhase("XmlSubtreeDSM.endModify.init");
            SchemaPath nodeType = nodeToBeUpdated.getModelNodeSchemaPath();
            Class storedParentClass = m_entityRegistry.getEntityClass(nodeType);
            ModelNodeId parentId = nodeToBeUpdated.getParentNodeId();
            endPhase("XmlSubtreeDSM.endModify.init", false);
            startPhase("XmlSubtreeDSM.endModify.getModelNodeKey");
            ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(m_schemaRegistry, nodeType, nodeToBeUpdated.getModelNodeId());
            endPhase("XmlSubtreeDSM.endModify.getModelNodeKey", false);
            Object storedParentEntity;
            startPhase("XmlSubtreeDSM.endModify.getParentEntity");
            storedParentEntity = getParentEntity(storedParentClass, modelNodeKey, parentId, LockModeType.PESSIMISTIC_WRITE);
            endPhase("XmlSubtreeDSM.endModify.getParentEntity", false);
            Element xmlValue = null;
            try {
                startPhase("XmlSubtreeDSM.endModify.getXmlValue");
                xmlValue = m_xmlModelNodeToXmlMapper.getXmlValue(nodeToBeUpdated);
            } finally {
                endPhase("XmlSubtreeDSM.endModify.getXmlValue", false);
            }
            startPhase("XmlSubtreeDSM.endModify.getXmlSubtree");
            String currentXmlStr = getXmlSubtree(storedParentEntity, storedParentClass, m_entityRegistry);
            endPhase("XmlSubtreeDSM.endModify.getXmlSubtree", false);
            startPhase("XmlSubtreeDSM.endModify.buildElementToBeSaved");
            Element fulXmlElement = buildElementToBeSaved(currentXmlStr, nodeToBeUpdated, xmlValue);
            endPhase("XmlSubtreeDSM.endModify.buildElementToBeSaved", false);
            try {
                startPhase("XmlSubtreeDSM.endModify.dom2String");
                String xmlSubtreeString = dom2String(nodeType, EMNKeyUtil.getModelNodeId(modelNodeKey, parentId, nodeType), fulXmlElement, storedParentEntity);
                if (xmlSubtreeString != null) {
                    setXmlSubtree(storedParentEntity, storedParentClass, xmlSubtreeString);
                }
            } catch (NetconfMessageBuilderException e) {
                throw new RuntimeException(e);
            } finally {
                endPhase("XmlSubtreeDSM.endModify.dom2String", false);
            }

        }
        LOGGER.debug("Updating the modified stored parent XML subtree nodes from cache into hibernate context done");
    }

    private Element buildElementToBeSaved(String currentXmlStr, XmlModelNodeImpl node, Element xmlValue) {
        Element fulXmlElement = xmlValue;
        if(node.isRoot()){
            if(currentXmlStr != null && !currentXmlStr.isEmpty()){
                try {
                    Element dataElement = DocumentUtils.stringToDocument(currentXmlStr).getDocumentElement();
                    fulXmlElement = dataElement;
                    appendXmlValue(dataElement, m_schemaRegistry.getDataSchemaNode(node.getModelNodeSchemaPath()), xmlValue, node);
                } catch (NetconfMessageBuilderException | InvalidIdentityRefException e) {
                    throw new RuntimeException(e);
                }
            }else {
                //xml string is null or empty string
                Document ownerDocument = xmlValue.getOwnerDocument();
                fulXmlElement = ownerDocument.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_REPLY_DATA);
                fulXmlElement.appendChild(xmlValue);
            }
        }
        return fulXmlElement;
    }

    protected String dom2String(SchemaPath nodeType, ModelNodeId storedNodeId, Element fulXmlElement, Object storedEntity) throws NetconfMessageBuilderException {
        String xmlSubtreeString;
        try {
            startPhase("XmlSubtreeDSM.dom2String.documentToPrettyString");
            xmlSubtreeString = documentToPrettyString(fulXmlElement);
        } finally {
            endPhase("XmlSubtreeDSM.dom2String.documentToPrettyString", false);
        }
        return xmlSubtreeString;
    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId, int insertIndex) throws DataStoreException{
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createNode :"+modelNode.getModelNodeSchemaPath() +" parentId:"+parentId);
        }
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        Class klass = m_entityRegistry.getEntityClass(schemaPath);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.createNode(modelNode, parentId, insertIndex);
        } else {
            XmlModelNodeImpl parentModelNode = null;
            if (modelNode instanceof XmlModelNodeImpl){
                parentModelNode = ((XmlModelNodeImpl) modelNode).getParentModelNode();
            }

            if (parentModelNode == null) {
                super.createNode(modelNode, parentId, insertIndex); // EntityModelNode DSM can handle this.
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath, modelNode.getSchemaRegistry());
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) modelNode);
                if (storedParentModelNode != null) {
                    parentModelNode.addChildAtSpecificIndex(modelNode.getQName(), (XmlModelNodeImpl) modelNode, insertIndex);
                    markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                }
            }
        }
        return modelNode;
    }

    @Override
    @Transactional(value=TxType.REQUIRED,rollbackOn={DataStoreException.class,RuntimeException.class,Exception.class})
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName,ConfigLeafAttribute> configAttributes, Map<QName,
            LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode) throws DataStoreException {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("updateNode :"+modelNode.getModelNodeSchemaPath() +" parentId:"+parentId);
        }
        updateNode(modelNode, parentId, configAttributes, leafListAttributes, -1, removeNode);
    }

    @Override
    @Transactional(value=TxType.REQUIRED,rollbackOn={DataStoreException.class,RuntimeException.class,Exception.class})
    public void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName,ConfigLeafAttribute> configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, int insertIndex, boolean removeNode) throws DataStoreException {
        if(modelNode.isRoot()){
            SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(schemaPath);
            if(klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) != null){
                ((ModelNodeWithAttributes)modelNode).updateConfigAttributes(configAttributes);
                if(leafListAttributes != null){
                    ((ModelNodeWithAttributes)modelNode).setLeafLists(leafListAttributes);
                }
                markNodesToBeUpdated(modelNode);
                return;
            }
        }
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("updateNode :"+modelNode.getModelNodeSchemaPath() +" parentId:"+parentId);
        }
        if(modelNode!=null) {
            SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(schemaPath);
            if (klass != null) {
                super.updateNode(modelNode, parentId, configAttributes, leafListAttributes, insertIndex, removeNode);
                updateCache(modelNode, configAttributes, leafListAttributes);
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath, modelNode.getSchemaRegistry());
                if (storedParentSchemaPath == null && modelNode.isSchemaMountImmediateChild()) {
                    storedParentSchemaPath = getStoredParentSchemaPath(modelNode.getParentMountPath(), modelNode.getSchemaRegistry());
                }
                ModelNode freshModelNode = findNode(modelNode.getModelNodeSchemaPath(), MNKeyUtil.getModelNodeKey(modelNode, modelNode.getSchemaRegistry()), parentId, modelNode.getSchemaRegistry());
                if (freshModelNode != null) {
                    XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) freshModelNode);
                    if (storedParentModelNode != null) {
                        findAndUpdateModelNode(storedParentModelNode, (XmlModelNodeImpl) freshModelNode, configAttributes, leafListAttributes, removeNode);
                        markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                    }
                }
            }
        }
    }

    @Override
    public void updateIndex(ModelNode modelNode, ModelNodeId parentId, int newIndex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("updateIndex Of Node:" + modelNode.getModelNodeSchemaPath() + " parentId:" + parentId);
        }
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        Class klass = m_entityRegistry.getEntityClass(schemaPath);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            super.updateIndex(modelNode, parentId, newIndex);
        } else {
            XmlModelNodeImpl parentModelNode = null;
            if (modelNode instanceof XmlModelNodeImpl) {
                parentModelNode = ((XmlModelNodeImpl) modelNode).getParentModelNode();
            }

            if (parentModelNode == null) {
                super.updateIndex(modelNode, parentId, newIndex);
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath, modelNode.getSchemaRegistry());
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) modelNode);
                if (storedParentModelNode != null) {
                    parentModelNode.updateChildIndex(modelNode.getQName(), (XmlModelNodeImpl) modelNode, newIndex);
                    markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                }
            }
        }
    }

    private void markNodesToBeUpdated(ModelNode modelNode) {
        markNodesToBeUpdated(modelNode.getModelNodeSchemaPath(), (XmlModelNodeImpl)modelNode);
    }

    private void updateCache(ModelNode modelNode, Map<QName, ConfigLeafAttribute> configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes) {
        SchemaPath nodeType = modelNode.getModelNodeSchemaPath();
        ModelNodeId nodeId = modelNode.getModelNodeId();
        XmlModelNodeImpl nodeFromCache = m_dsmCache.getFromCache(nodeType, nodeId);
        if(nodeFromCache != null){
            LOGGER.debug("Updated the node from cache, nodeType {}, nodeId {}", nodeType, nodeId);
            nodeFromCache.updateConfigAttributes(configAttributes);
            nodeFromCache.updateLeafListAttributes(leafListAttributes);
        }
    }

    @Override
    public void removeNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeNode :"+modelNode.getModelNodeSchemaPath() +" parentId:"+parentId);
        }
        if(modelNode!=null) {
            SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(modelNode, m_schemaRegistry);
            SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(schemaPath);
            if (klass != null) {
                ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(registry, modelNode.getModelNodeSchemaPath(), modelNode.getModelNodeId());
                Object entity = getParentEntity(klass, modelNodeKey, parentId, LockModeType.PESSIMISTIC_WRITE);
                setXmlSubtree(entity, klass, "");
                super.removeNode(modelNode, parentId);
                m_dsmCache.removeFromCache(modelNode.getModelNodeId());
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(schemaPath, modelNode.getSchemaRegistry());
                ModelNode storedModelNode = findNode(modelNode.getModelNodeSchemaPath(), MNKeyUtil.getModelNodeKey(modelNode, registry),parentId, modelNode.getSchemaRegistry());
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) storedModelNode);
                if (storedParentModelNode != null) {
                    XmlModelNodeImpl parentModelNode = ((XmlModelNodeImpl) storedModelNode).getParentModelNode();
                    parentModelNode.removeChild((XmlModelNodeImpl)storedModelNode);
                    IndexedList<ModelNodeId, XmlModelNodeImpl> xmlModelNodes = parentModelNode.getChildren().get(storedModelNode.getQName());
                    xmlModelNodes.remove(storedModelNode.getModelNodeId());
                    markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                }
            }
        }
    }

    @Override
    public void removeAllNodes(ModelNode parentNode, SchemaPath nodeType, ModelNodeId grandParentId) throws DataStoreException {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeAllNodes :" + parentNode.getModelNodeSchemaPath() + " parentId:" + grandParentId);
        }
        if(parentNode !=null) {
            SchemaPath schemaPath = parentNode.getModelNodeSchemaPath();
            Class klass = m_entityRegistry.getEntityClass(schemaPath);
            if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
                super.removeAllNodes(parentNode,nodeType,grandParentId);
            } else {
                SchemaPath storedParentSchemaPath = getStoredParentSchemaPath(nodeType, parentNode.getSchemaRegistry());
                XmlModelNodeImpl storedParentModelNode = getStoredParentModelNode((XmlModelNodeImpl) parentNode);
                if (storedParentModelNode != null) {
                    IndexedList<ModelNodeId, XmlModelNodeImpl> xmlModelNodes = ((XmlModelNodeImpl) parentNode).getChildren().get(nodeType.getLastComponent());
                    if (xmlModelNodes != null) {
                        xmlModelNodes.clear();
                        markNodesToBeUpdated(storedParentSchemaPath, storedParentModelNode);
                    }
                }
            }
        }
    }

    private void findAndUpdateModelNode(XmlModelNodeImpl parentModelNode, XmlModelNodeImpl xmlModelNode, Map<QName, ConfigLeafAttribute>
            configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode) {
        //Map<QName, List<XmlModelNodeImpl>> children = parentModelNode.getChildren();
        ModelNodeWithAttributes targetModelNode = retrieveModelNode(xmlModelNode.getModelNodeSchemaPath(), xmlModelNode.getModelNodeId(), parentModelNode);
        if (targetModelNode != null) {
            if (configAttributes!=null) {
                targetModelNode.updateConfigAttributes(configAttributes);
            }
            if(leafListAttributes!=null){
                if (removeNode){
                    targetModelNode.removeLeafListAttributes(leafListAttributes);
                } else {
                    targetModelNode.updateLeafListAttributes(leafListAttributes);
                }
            }
        }
    }

    private XmlModelNodeImpl retrieveModelNode(SchemaPath nodeType, ModelNodeId modelNodeId, XmlModelNodeImpl storedParentModelNode, SchemaRegistry schemaRegistry) {
        if (modelNodeId.equals(storedParentModelNode.getModelNodeId())) {
            return storedParentModelNode;
        }

        if (schemaRegistry == null) {
            schemaRegistry = m_schemaRegistry;
        }

        /**
         * The idea here is simple. The Super Grand Parent modelNode has a Map<QName,ModelNode>.
         * The QName is the immediate child of the Super grand Parent node and this is how the xml tree is
         * maintained.
         *
         * Here we have the target ModelNodeId that we want to retrieve and the super grand parent
         * ModelNodeId.
         *
         * 1) First we fetch the next Rdn link between the two.
         * 2) Get the next ModelNodeId(A1) in the link.
         * 3) Get the right childList in the current parent with RDN(QName)
         * 4) Iterate through the list and get the right ModelNode which is A1.
         * 5) Do the above steps till we hit the final modelNodeId
         *
         * Example:
         *
         *  A(yang list)-> B(yang container) -> C(yang list) --> D (Yang list).
         *  Here A is the super grand parent and D is what we want
         *  1) we get B's RDN
         *  2) we get B's ModelNodeId
         *  3) We get the list of B's modelNode with the QName
         *  4) We compare the list with B and call recursively.
         *
         */

        ModelNodeRdn rdn = modelNodeId.getNextChildRdn(storedParentModelNode.getModelNodeId());
        QName nextQName = null;
        if (ModelNodeRdn.CONTAINER.equals(rdn.getRdnName())) {
            nextQName = getRdnQNameFromRegistry(rdn, storedParentModelNode);
            startPhase("XmlSubtreeDSM.retrieveModelNode.storedParentModelNode.getChildren");
            IndexedList<ModelNodeId, XmlModelNodeImpl> childList = storedParentModelNode.getChildren().get(nextQName);
            endPhase("XmlSubtreeDSM.retrieveModelNode.storedParentModelNode.getChildren", false);
            if (( childList == null || childList.isEmpty()) && AnvExtensions.MOUNT_POINT.isExtensionIn(storedParentModelNode.getSchemaRegistry().getDataSchemaNode(storedParentModelNode.getModelNodeSchemaPath()))) {
                if (storedParentModelNode.getSchemaRegistry().getMountRegistry() != null) {
                    SchemaMountRegistryProvider provider = storedParentModelNode.getSchemaRegistry().getMountRegistry().getProvider(storedParentModelNode.getModelNodeSchemaPath());
                    if (provider != null) {
                        SchemaRegistry mountRegistry = provider.getSchemaRegistry(storedParentModelNode.getModelNodeId());
                        if (mountRegistry != null && !mountRegistry.getMountPath().equals(schemaRegistry.getMountPath())) {
                            return retrieveModelNode(nodeType, modelNodeId, storedParentModelNode, mountRegistry);
                        }
                    }
                }
            }

            if (childList != null && !childList.isEmpty()) {
                if (childList.get(0).getModelNodeSchemaPath().equals(nodeType)) {
                    return childList.map().get(modelNodeId);
                } else if (childList.size() == 1){
                    return retrieveModelNode(nodeType, modelNodeId, childList.get(0), schemaRegistry);
                } else {
                    ModelNodeId nextId = modelNodeId.getNextChildId(storedParentModelNode.getModelNodeId());
                    XmlModelNodeImpl child = childList.map().get(nextId);
                    if(child != null){
                        return retrieveModelNode(nodeType, modelNodeId, child, schemaRegistry);
                    }
                }
            }
        }
        return null;

    }

    private QName getRdnQNameFromRegistry(ModelNodeRdn rdn, XmlModelNodeImpl storedParentModelNode) {
    	SchemaRegistry registry = m_schemaRegistry;
    	if(storedParentModelNode.hasSchemaMount() || storedParentModelNode.isSchemaMountImmediateChild()){
    		registry = storedParentModelNode.getSchemaRegistry();
    	} else {
    		XmlModelNodeImpl grandParent = storedParentModelNode.getParentModelNode();
    		while(grandParent != null){
    			if(grandParent.hasSchemaMount() || grandParent.isSchemaMountImmediateChild()){
    				registry = grandParent.getSchemaRegistry();
    				break;
    			}else {
    				grandParent = grandParent.getParentModelNode();
    			}
    		}
    	}
    	QName nextQName = registry.lookupQName(rdn.getNamespace(), rdn.getRdnValue());
    	return nextQName;
    }

    private XmlModelNodeImpl retrieveModelNode(SchemaPath nodeType, ModelNodeId modelNodeId, XmlModelNodeImpl storedParentModelNode) {
        return retrieveModelNode(nodeType, modelNodeId, storedParentModelNode, null);
    }

    private List<ModelNode> retrieveModelNodes(SchemaPath nodeType, XmlModelNodeImpl storedParentModelNode, ModelNodeId parentId) {
        List<ModelNode> modelNodes = new ArrayList<>();
        if (parentId != null) {
            XmlModelNodeImpl parentModelNode = retrieveModelNode(nodeType.getParent(), parentId, storedParentModelNode);
            if (parentModelNode != null) {
                QName childQName = nodeType.getLastComponent();
                IndexedList<ModelNodeId, XmlModelNodeImpl> children = parentModelNode.getChildren().get(childQName);
                if (children!=null) {
                    modelNodes.addAll(children.list());
                }
            }
        } else {
            for (Map.Entry<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> entry : storedParentModelNode.getChildren().entrySet()) {
                for (XmlModelNodeImpl child : entry.getValue().list()) {
                    if (child.getModelNodeSchemaPath().equals(nodeType)) {
                        if (parentId != null && parentId.equals(child.getParentModelNode().getModelNodeId())) {
                            modelNodes.add(child);
                        } else if (parentId == null) {
                            modelNodes.add(child);
                        }

                    } else {
                        List<ModelNode> childModelNodes = retrieveModelNodes(nodeType, child, parentId);
                        modelNodes.addAll(childModelNodes);
                    }
                }

            }
        }
        return modelNodes;
    }

    private XmlModelNodeImpl getStoredParentModelNode(SchemaPath storedParentSchemaPath, ModelNodeId parentId, ModelNodeId storedGrandParentId, SchemaPath nodeType){
        XmlModelNodeImpl storedParentModelNode;
        Class storedParentClass = m_entityRegistry.getEntityClass(storedParentSchemaPath);
        ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(m_schemaRegistry, storedParentSchemaPath, parentId);
        storedParentModelNode = m_dsmCache.getFromCache(storedParentSchemaPath, EMNKeyUtil.getModelNodeId(modelNodeKey, storedGrandParentId, storedParentSchemaPath));
        if(storedParentModelNode != null){
            LOGGER.debug("Model node {} is loaded from cache", storedParentModelNode);
            return storedParentModelNode;
        }
        try {
            Object storedParentEntity = getParentEntity(storedParentClass, modelNodeKey, storedGrandParentId, LockModeType.PESSIMISTIC_READ);
            if (storedParentEntity != null) {
                String yangXmlSubtree = "";
                boolean xmlLoaded = false;
                if(shouldEagerlyLoadXmlData(nodeType, storedParentClass)) {
                    LOGGER.debug("Xml data is being loaded eagerly");
                    startPhase("XmlSubtreeDSM.getStoredParentModelNode.getXmlSubtree");
                    yangXmlSubtree = getXmlSubtree(storedParentEntity, storedParentClass, m_entityRegistry);
                    endPhase("XmlSubtreeDSM.getStoredParentModelNode.getXmlSubtree", false);
                    xmlLoaded = true;
                }
                startPhase("XmlSubtreeDSM.getStoredParentModelNode.string2Dom");
                Element element = string2Dom(storedParentSchemaPath, storedParentEntity, yangXmlSubtree);
                endPhase("XmlSubtreeDSM.getStoredParentModelNode.string2Dom", false);
                Map<QName, ConfigLeafAttribute> configAttrsFromEntity = XmlModelNodeToXmlMapperImpl.getConfigAttributesFromEntity(m_schemaRegistry, storedParentSchemaPath, m_entityRegistry, storedParentClass, storedParentEntity);
                Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListsFromEntity = DSMUtils.getConfigLeafListsFromEntity(m_schemaRegistry, storedParentSchemaPath, m_entityRegistry, storedParentClass, storedParentEntity);
                startPhase("XmlSubtreeDSM.getStoredParentModelNode.getModelNodeFromNodeSchemaPath");
                List<XmlModelNodeImpl> storedParentModelNodes = m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(xmlLoaded, element, configAttrsFromEntity, leafListsFromEntity, storedParentSchemaPath, storedGrandParentId, null, this, storedParentEntity, storedParentSchemaPath);
                endPhase("XmlSubtreeDSM.getStoredParentModelNode.getModelNodeFromNodeSchemaPath", false);
                for(XmlModelNodeImpl node : storedParentModelNodes){
                    if(storedGrandParentId.equals(node.getParentNodeId())){
                        storedParentModelNode = node;
                        break;
                    }
                }
            }
            if(storedParentEntity != null) {
                setVisibility(m_entityRegistry, storedParentEntity, storedParentModelNode);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return storedParentModelNode;
    }

    private XmlModelNodeImpl getStoredParentModelNode(XmlModelNodeImpl xmlModelNode){
        if(xmlModelNode !=null && xmlModelNode.getParentModelNode()!=null){
            XmlModelNodeImpl parentModelNode = xmlModelNode.getParentModelNode();
            while(parentModelNode.getParentModelNode()!=null){
                parentModelNode = parentModelNode.getParentModelNode();
            }
            return parentModelNode;
        }else{
            return xmlModelNode;
        }
    }

    protected void markNodesToBeUpdated(SchemaPath nodeType, XmlModelNodeImpl node) {
        m_dsmCache.markNodeToBeUpdated(nodeType, node.getModelNodeId());
    }

    @SuppressWarnings("unchecked")
    protected Object getParentEntity(Class klass, ModelNodeKey modelNodeKey, ModelNodeId parentId, LockModeType lockModeType) throws
            DataStoreException {
        Object pk = EMNKeyUtil.buildPrimaryKey(klass, parentId, modelNodeKey, m_entityRegistry, getEntityDataStoreManager(klass));
        return getEntityDataStoreManager(klass).findById(klass, pk,lockModeType);
    }

    protected EntityDataStoreManager getEntityDataStoreManager(Class klass) {
        if (klass != null) {
            EntityDataStoreManager entityDSM = m_modelNodeDSMRegistry.getEntityDSM(klass);
            if (entityDSM != null) {
                return entityDSM;
            }
        }
        return m_persistenceManagerUtil.getEntityDataStoreManager();
    }

    private SchemaPath getStoredParentSchemaPath(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        SchemaPath inPath = schemaPath;
        while (schemaPath != null && m_entityRegistry.getEntityClass(schemaPath) == null) {
            schemaPath = schemaPath.getParent();
        }
        if (schemaPath == null) {
        	DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, inPath); 
            DataSchemaNode inSchemaNode = context.getDataSchemaNode();
            if (inSchemaNode != null) {
                DataSchemaNode parentNode = SchemaRegistryUtil.getEffectiveParentNode(inSchemaNode, schemaRegistry);
                schemaPath = parentNode != null ? parentNode.getPath() : null;
                if (schemaPath != null) {
                    return getStoredParentSchemaPath(schemaPath, schemaRegistry);
                }
            }
        }
        return schemaPath;
    }

    protected Element string2Dom(SchemaPath nodeType, Object storedEntity, String yangXmlSubtree) {
        try {
            startPhase("XmlSubtreeDSM.string2Dom");
            Element parentElement = null;
            try {
                if(yangXmlSubtree !=null && !yangXmlSubtree.isEmpty()) {
                    parentElement = stringToDocument(yangXmlSubtree).getDocumentElement();
                }
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error("XmlSubtree: \n" + yangXmlSubtree);
                throw new RuntimeException(e);
            }
            return parentElement;
        } finally {
            endPhase("XmlSubtreeDSM.string2Dom", false);
        }
    }

    private void appendXmlValue(Element dataElement, DataSchemaNode dataSchemaNode, Element xmlValue, XmlModelNodeImpl node) throws InvalidIdentityRefException {

        Document ownerDocument = dataElement.getOwnerDocument();
        boolean appended = false;
        if(dataSchemaNode instanceof ListSchemaNode){
            Map<QName, ConfigLeafAttribute> keyAttributesFromNode = node.getKeyAttributes();
            ListSchemaNode listSchemaNode = (ListSchemaNode) dataSchemaNode;
            for(Element rootNode: DocumentUtils.getChildElements(dataElement)){
                Map<QName, ConfigLeafAttribute> keyAttributesFromXml = getKeysFromXml(listSchemaNode, rootNode);
                if(keyAttributesFromNode.equals(keyAttributesFromXml)){
                    rootNode.getParentNode().removeChild(rootNode);
                    dataElement.appendChild(ownerDocument.importNode(xmlValue, true));
                    appended = true;
                    break;
                }
            }
        } else {
            //container
            for(Element rootNode: DocumentUtils.getChildElements(dataElement)){
                if(rootNode.getNamespaceURI().equals(xmlValue.getNamespaceURI()) && rootNode.getLocalName().equals(xmlValue.getLocalName())){
                    rootNode.getParentNode().removeChild(rootNode);
                    dataElement.appendChild(ownerDocument.importNode(xmlValue, true));
                    appended = true;
                    break;
                }
            }
        }
        if(!appended){
            //newly created node, so append at the end
            dataElement.appendChild(ownerDocument.importNode(xmlValue, true));
        }
    }

    private Map<QName, ConfigLeafAttribute> getKeysFromXml(ListSchemaNode listSchemaNode, Element rootNode) throws InvalidIdentityRefException {
        Map<QName, ConfigLeafAttribute> keyAttributesFromXml = new LinkedHashMap<>();
        for(QName keyQname:listSchemaNode.getKeyDefinition()){
            for(Element field : DocumentUtils.getChildElements(rootNode)){
                if(nodesMatch(field, keyQname)){
                    keyAttributesFromXml.put(keyQname, ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, listSchemaNode.getPath(), keyQname, field));
                }
            }
        }
        return keyAttributesFromXml;
    }

    private Boolean shouldEagerlyLoadXmlData(SchemaPath schemaPath, Class storedParentClass) {
        boolean eagerlyLoadXml = m_entityRegistry.getEagerFetchInfo(storedParentClass);
        if(!eagerlyLoadXml) {
            Collection<SchemaNode> leafAndLeafListSNs = m_schemaRegistry.getNonChoiceChildren(schemaPath)
                    .stream()
                    .filter(sn -> (sn instanceof LeafSchemaNode || sn instanceof LeafListSchemaNode) && sn.isConfiguration())
                    .collect(Collectors.toSet());

            Set<QName> leafLeafListInEntityCols = new HashSet<>();
            leafLeafListInEntityCols.addAll(m_entityRegistry.getAttributeGetters(storedParentClass).keySet());
            leafLeafListInEntityCols.addAll(m_entityRegistry.getYangLeafListGetters(storedParentClass).keySet());

            Collection<SchemaNode> difference = leafAndLeafListSNs
                    .stream()
                    .filter(sn -> !leafLeafListInEntityCols.contains(sn.getQName())).collect(Collectors.toSet());
            return !difference.isEmpty();
        }
        return eagerlyLoadXml;
    }

    private void setXmlSubtree(Object parentEntity, Class parentKlass, String value) throws DataStoreException {
        Method yangXmlSubtreeSetter = m_entityRegistry.getYangXmlSubtreeSetter(parentKlass);
        if (yangXmlSubtreeSetter != null) {
            try {
               startPhase("XmlSubtreeDSM.setXmlSubtree");
               yangXmlSubtreeSetter.invoke(parentEntity, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                endPhase("XmlSubtreeDSM.setXmlSubtree", false);
            }
        }
    }
    
    @Override
    public boolean isChildTypeBigList(SchemaPath nodeType, SchemaRegistry mountRegistry) {
        Class entityClass = m_entityRegistry.getEntityClass(nodeType);
        if (entityClass != null) {
            return super.isChildTypeBigList(nodeType, mountRegistry);
        } else {
            return m_schemaRegistry.isChildBigList(nodeType);
        }
    }
}
