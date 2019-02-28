package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Collection;
import java.util.Set;

/**
 * Created by keshava on 5/12/15.
 */
public interface EntityToModelNodeMapper {
    ModelNodeWithAttributes getModelNode(Object entityObject, ModelNodeDataStoreManager modelNodeDSM) throws ModelNodeMapperException;
    void updateEntity(Object entity, SchemaPath nodeSchemaPath, ModelNode modelNode, Class klass, ModelNodeId parentId, int insertIndex);
    Object getEntity(SchemaPath nodeSchemaPath, ModelNode modelNode, Class klass, ModelNodeId parentId, int insertIndex);
    Collection<Object> getChildEntities(Object parentEntityObj, QName childQname);
    void clearLeafLists(Object entity, Set<QName> qNames);
}
