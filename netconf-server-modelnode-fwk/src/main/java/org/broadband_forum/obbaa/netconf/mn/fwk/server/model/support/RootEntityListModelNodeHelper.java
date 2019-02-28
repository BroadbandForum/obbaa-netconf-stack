package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmListModelNodeHelper;

/**
 * Created by keshava on 2/2/16.
 */
public class RootEntityListModelNodeHelper extends DsmListModelNodeHelper {

    public RootEntityListModelNodeHelper(ListSchemaNode schemaNode, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                         ModelNodeDataStoreManager modelNodeDSM, SchemaRegistry schemaRegistry, SubSystemRegistry subsystemRegistry) {
        super(schemaNode, modelNodeHelperRegistry,modelNodeDSM, schemaRegistry, subsystemRegistry);
    }

    @Override
    public ModelNode addChild(ModelNode parentNode, String childUri, Map<QName, ConfigLeafAttribute> keyAttrs,
                              Map<QName, ConfigLeafAttribute> configAttrs) {
        ModelNodeWithAttributes newNode = getNewModelNode(null);
        Map<QName,ConfigLeafAttribute> allAttributes = new LinkedHashMap<>();
        allAttributes.putAll(keyAttrs);
        allAttributes.putAll(configAttrs);
        newNode.setAttributes(allAttributes);
        m_modelNodeDSM.createNode(newNode, parentNode.getModelNodeId());
        return newNode;
    }

    @Override
    public void removeAllChild(ModelNode parentNode) throws ModelNodeDeleteException {
        try {
            for(ModelNode child: getValue(parentNode, Collections.<QName, ConfigLeafAttribute>emptyMap())){
                removeChild(parentNode, child);
            }
        } catch (ModelNodeGetException e) {
            throw new ModelNodeDeleteException(e);
        }
    }
}
