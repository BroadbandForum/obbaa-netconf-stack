package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;

/**
 * Service helps in maintaining the data store integrity. 
 * 
 * During phase3 validation, those nodes that fail "when" condition are identified to be deleted
 * Similarly those nodes that pass "when" condition with a default value have to be created. 
 * 
 * This service helps to build a <edit-config> request for the given set of model nodes - create/delete as one request
 * and return it back to DataStore#editInternal for further validation. 
 * 
 */
public interface DataStoreIntegrityService {
	
	/**
	 * The function expects 3 hashmaps to be available in RequestScope and a map of defaultValues for create/merge
	 * Map<ModelNode,QName> -> 3 such maps for Create/Delete/Merge list of nodes. 
	 * Map<SchemaPath,String> -> a map of defaultValues. 
	 */
	public List<EditConfigRequest> createInternalEditRequests(EditConfigRequest sourceRequest, NetconfClientInfo clientInfo) throws GetAttributeException;
}
