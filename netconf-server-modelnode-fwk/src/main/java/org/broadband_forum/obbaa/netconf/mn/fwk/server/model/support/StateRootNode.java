package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;

public class StateRootNode extends ModelNodeWithAttributes {
    public StateRootNode(SchemaPath schemaPath, ModelNodeId parentId, ModelNodeHelperRegistry modelNodeHelperRegistry,
                         SubSystemRegistry subsystemRegistry, SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDSM) {
        super(schemaPath, parentId, modelNodeHelperRegistry, subsystemRegistry, schemaRegistry, modelNodeDSM);
    }

    protected Element createGetResponse(Document doc, FilterNode filter, ModelNodeId modelNodeId, boolean includeState,
                                        StateAttributeGetContext stateContext, NetconfQueryParams params) throws GetException, DOMException, GetAttributeException {
        //call super class logic to mark state nodes to be retrieved
        super.createGetResponse(doc, filter, modelNodeId, includeState, stateContext, params);
        //don't add the state node
        return null;
    }
}
