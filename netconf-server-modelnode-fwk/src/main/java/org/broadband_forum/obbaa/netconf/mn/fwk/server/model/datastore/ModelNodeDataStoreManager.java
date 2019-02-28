package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ListEntryInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;

/**
 * Netconf stack interacts with ModelNodeDataStoreManager It abstracts entity or persistence information by providing ModelNode to entity mapping and vice versa.
 * This is achieved by read and write methods which are used by helpers.
 */
public interface ModelNodeDataStoreManager {
    /**
     * Begin callback to DSMs to optimize DS modify operation.
     * DSMs can choose to cache nodes to avoid multiple writes to the backing storage.
     */
    void beginModify();
    /**
     * End modify callback to DSMs.
     * DSMs that choose to optimize modify operation should synchronise the modified changes with the backing store on this callback.
     */
    void endModify();

    @Deprecated()
    /**
     * This API is not supported for nodes whose parent id a list type.
     * Use {@link  ModelNodeDataStoreManager#findNodes(SchemaPath, Map, ModelNodeId)} API instead.
     * This API will be removed soon.
     */
    List<ModelNode> listNodes(SchemaPath nodeType) throws DataStoreException;
    List<ModelNode> listChildNodes(SchemaPath childType, ModelNodeId parentId) throws DataStoreException;
    ModelNode findNode(SchemaPath nodeType, ModelNodeKey key, ModelNodeId parentId) throws DataStoreException;
    List<ModelNode> findNodes(SchemaPath nodeType, Map<QName,ConfigLeafAttribute> matchCriteria, ModelNodeId parentId) throws DataStoreException;
    ModelNode createNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException;
    ModelNode createNode(ModelNode modelNode, ModelNodeId parentId, int insertIndex) throws DataStoreException;
    void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName,ConfigLeafAttribute> configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, boolean removeNode) throws DataStoreException;
    void updateNode(ModelNode modelNode, ModelNodeId parentId, Map<QName,ConfigLeafAttribute> configAttributes, Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes, int insertIndex, boolean removeNode) throws DataStoreException;
    void removeNode(ModelNode modelNode, ModelNodeId parentId) throws DataStoreException;
    void removeAllNodes(ModelNode parentNode, SchemaPath nodeType, ModelNodeId grandParentId) throws DataStoreException;
    boolean isChildTypeBigList(SchemaPath childType);
    List<ListEntryInfo> findNodesLike(SchemaPath nodeType, ModelNodeId parentId, Map<QName, String> keysLike, int maxResults);
    public List findByMatchValues(SchemaPath nodeType, Map<String, Object> matchValues);
    public EntityRegistry getEntityRegistry(SchemaPath nodeType);

}
