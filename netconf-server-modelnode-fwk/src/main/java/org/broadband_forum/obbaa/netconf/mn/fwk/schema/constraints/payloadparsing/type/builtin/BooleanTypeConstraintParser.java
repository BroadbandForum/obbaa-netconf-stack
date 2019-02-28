package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class BooleanTypeConstraintParser implements TypeValidator {

	private static final String TRUE = "true";

	private static final String FALSE = "false";

	public BooleanTypeConstraintParser() {

	}

	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		String content = validateInsertAttribute ? insertValue : value.getTextContent();
		if (!TRUE.equals(content) && !FALSE.equals(content)) {
			throw getInvalidBooleanException(content);
		}
	}

	private ValidationException getInvalidBooleanException(String invalidValue) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, 
				String.format("Invalid value. It should be \"true\" or \"false\" instead of \"%s\"",
						invalidValue));
		return new ValidationException(rpcError);
	}
}
