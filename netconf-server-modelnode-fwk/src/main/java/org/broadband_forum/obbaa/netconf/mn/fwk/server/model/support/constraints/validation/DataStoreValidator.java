package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.util.Collection;
import java.util.List;

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
	 * @param request
	 * @param clientInfo
	 * @return A List EditConfig Requests that need to be sent to correct the DataStore based on Post edit DS validations.
	 * @throws ValidationException
	 */
	List<EditConfigRequest> validate(RootModelNodeAggregator rootModelNodes, EditContainmentNode editTree,
									 EditConfigRequest request, NetconfClientInfo clientInfo) throws ValidationException;

    void validateMissingChildren(ModelNodeWithAttributes modelNode, Collection<SchemaPath> changeNodeSet) throws ModelNodeGetException;

    boolean validateChild(ModelNode modelNode, DataSchemaNode schemaNode);
    
    DSExpressionValidator getValidator();
    
}
