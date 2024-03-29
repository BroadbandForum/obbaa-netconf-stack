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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ConfigAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StateRootNode extends ModelNodeWithAttributes {
    public StateRootNode(SchemaPath schemaPath, ModelNodeId parentId, ModelNodeHelperRegistry modelNodeHelperRegistry,
                         SubSystemRegistry subsystemRegistry, SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDSM) {
        super(schemaPath, parentId, modelNodeHelperRegistry, subsystemRegistry, schemaRegistry, modelNodeDSM);
    }

    protected Element createGetResponse(NetconfClientInfo clientInfo, Document doc, FilterNode filter, ModelNodeId modelNodeId, boolean includeState,
                                        StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext, NetconfQueryParams params) throws GetException, DOMException, GetAttributeException {
        //call super class logic to mark state nodes to be retrieved
        super.createGetResponse(clientInfo, doc, filter, modelNodeId, includeState, stateContext, configContext, params);
        //don't add the state node
        return null;
    }
}
