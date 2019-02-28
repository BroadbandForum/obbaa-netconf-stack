package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import org.joda.time.DateTime;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class DateAndTimeTypeConstraintParser extends StringTypeConstraintParser {

	public static final String LOCAL_TYPE_NAME = "date-and-time";

	public DateAndTimeTypeConstraintParser(TypeDefinition<?> type) {
		super(type);
	}

	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		super.validate(value, validateInsertAttribute, insertValue);
		String strValue = validateInsertAttribute ? insertValue : value.getTextContent();
		try {
		    DateTime.parse(strValue);
		} catch (Exception e) {
			NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
										strValue + " is not a valid " + LOCAL_TYPE_NAME);
			throw new ValidationException(rpcError);
		}
	}
	
}
