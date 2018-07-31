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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.Notification;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;

/**
 * Service helps in maintaining the data store integrity.
 * <p>
 * During phase3 validation, those nodes that fail "when" condition are identified to be deleted
 * Similarly those nodes that pass "when" condition with a default value have to be created.
 * <p>
 * This service helps to build a <edit-config> request for the given set of model nodes - create/delete as one request
 * and sends it to the NC server.
 */
public interface DataStoreIntegrityService {

    /**
     * The function expects 3 hashmaps to be available in RequestScope and a map of defaultValues for create/merge
     * Map<ModelNode,QName> -> 3 such maps for Create/Delete/Merge list of nodes.
     * Map<SchemaPath,String> -> a map of defaultValues.
     */
    public List<Notification> createOrDeleteNodes(EditConfigRequest request, NetconfClientInfo clientInfo) throws
            GetAttributeException;
}
