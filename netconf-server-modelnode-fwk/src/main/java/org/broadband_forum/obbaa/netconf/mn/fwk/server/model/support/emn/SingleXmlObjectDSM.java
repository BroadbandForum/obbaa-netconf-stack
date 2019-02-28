package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class SingleXmlObjectDSM<XMLObjectType> extends XmlSubtreeDSM{
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SingleXmlObjectDSM.class, LogAppNames.NETCONF_STACK);
    private  XMLObjectType m_entity;

    public SingleXmlObjectDSM(XMLObjectType entity, PersistenceManagerUtil persistenceManagerUtil,
                              EntityRegistry entityRegistry, SchemaRegistry schemaRegistry,
                              ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry,
                              ModelNodeDSMRegistry modelNodeDSMRegistry) {
        super(persistenceManagerUtil, entityRegistry, schemaRegistry, modelNodeHelperRegistry, subsystemRegistry,
                modelNodeDSMRegistry);
        m_entity = entity;

    }

    @Override
    public ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException{
        LOGGER.debug("createNode : {} parentId: {}",modelNode.getModelNodeSchemaPath(),parentId);
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        Class klass = m_entityRegistry.getEntityClass(schemaPath);
        if (klass != null && m_entityRegistry.getYangXmlSubtreeGetter(klass) == null) {
            return super.createNode(modelNode, parentId);
        } else {
            if(modelNode.isRoot()){
                XmlModelNodeImpl xmlmodelNode = m_xmlModelNodeToXmlMapper.getRootXmlModelNode((ModelNodeWithAttributes)modelNode, this);
                markNodesToBeUpdated(modelNode.getModelNodeSchemaPath(), xmlmodelNode);
                return xmlmodelNode;
            }
            return super.createNode(modelNode, parentId);
        }
    }


    @Override
    protected XMLObjectType getParentEntity(Class klass, ModelNodeKey modelNodeKey, ModelNodeId parentId, LockModeType
            lockModeType) throws DataStoreException {
        return m_entity;
    }

    @Override
    protected List<XMLObjectType> getParentEntities(Class storedParentClass, Map<QName, ConfigLeafAttribute> matchCriteria, ModelNodeId parentId) throws
            DataStoreException {
        return Arrays.asList(m_entity);
    }

    public void setEntity(XMLObjectType entity) {
        m_entity = entity;
    }

}