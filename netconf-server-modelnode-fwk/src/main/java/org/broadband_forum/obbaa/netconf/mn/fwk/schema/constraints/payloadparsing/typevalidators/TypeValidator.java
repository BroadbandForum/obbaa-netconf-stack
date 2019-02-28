package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators;

import org.w3c.dom.Element;

public interface TypeValidator {

	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException;

}
