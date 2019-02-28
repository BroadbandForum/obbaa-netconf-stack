package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

/**
 * Yang phase 2 validation.
 */
public interface DataStoreConstraintValidator {
	
	
	/**
	 * Phase 2 validation with modelnode
	 *
	 * @param modelNode the model node that needs to be validated
	 * @throws ValidationException the validation exception
	 */
	void validate(ModelNode modelNode) throws ValidationException;
}
