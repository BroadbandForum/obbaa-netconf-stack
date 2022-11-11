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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;

public interface DataStoreValidator {
	/**
	 * Validate the DataStore and create the list of internal validation edit requests based on validation constraints such as when condition and return the list.
	 * 
	 * @param rootModelNodes
	 * @param editTree
	 * @param changeTree
     * @param request
     * @param clientInfo
     * @return A List EditConfig Requests that need to be sent to correct the DataStore based on Post edit DS validations.
	 * @throws ValidationException
	 */
	List<EditConfigRequest> validate(RootModelNodeAggregator rootModelNodes, EditContainmentNode editTree,
                                     ChangeTreeNode changeTree, EditConfigRequest request, NetconfClientInfo clientInfo) throws ValidationException;

    void validateMissingChildren(ModelNodeWithAttributes modelNode, Collection<SchemaPath> changeNodeSet,  Map<SchemaPath,
			Collection<EditContainmentNode>> changeNodeMap, DSValidationContext validationContext) throws ModelNodeGetException;

    boolean validateChild(ModelNode modelNode, DataSchemaNode schemaNode, DSValidationContext validationContext);
	void clearValidationCache();
    DSExpressionValidator getValidator();
    
}
