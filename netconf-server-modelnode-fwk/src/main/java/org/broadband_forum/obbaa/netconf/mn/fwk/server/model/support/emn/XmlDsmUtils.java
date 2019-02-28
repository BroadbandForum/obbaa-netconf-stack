package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;

public class XmlDsmUtils {
    public static ModelNode reloadParentNode(ModelNode parentNode, ModelNodeDataStoreManager modelNodeDSM, SchemaRegistry schemaRegistry)
            throws DataStoreException {
        ModelNode freshParentNode = parentNode;
        ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(parentNode, schemaRegistry);

        if (parentNode instanceof XmlModelNodeImpl) {
            freshParentNode = modelNodeDSM.findNode(parentNode.getModelNodeSchemaPath(), modelNodeKey,
                    ((XmlModelNodeImpl) parentNode).getParentNodeId());
        }
        return freshParentNode;
    }
}
