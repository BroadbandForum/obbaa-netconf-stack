package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class EnumerationTypeConstraintParser implements TypeValidator {
	
	private EnumTypeDefinition m_baseType;
	
	public EnumerationTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof EnumTypeDefinition) {
			m_baseType = (EnumTypeDefinition)type;
		}
	}

	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		List<String> enumValues = new ArrayList<String>();
		String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
		for (EnumPair enumPair : m_baseType.getValues()) {
			enumValues.add(enumPair.getName());
			if (enumPair.getName().equals(stringValue)) {
				return;
			}
		}
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
										 "Value \"" + stringValue + "\" is an invalid value. Expected values: " + enumValues.toString());
		throw new ValidationException(rpcError);
	}

}
