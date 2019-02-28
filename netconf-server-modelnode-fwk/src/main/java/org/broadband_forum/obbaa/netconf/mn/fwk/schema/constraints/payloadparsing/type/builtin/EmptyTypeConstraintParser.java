package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;

public class EmptyTypeConstraintParser implements TypeValidator {
	
	private EmptyTypeDefinition m_emptyType;
	
	public EmptyTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof EmptyTypeDefinition) {
			m_emptyType = (EmptyTypeDefinition) type;
		}
	}
	
	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		if (m_emptyType != null) {
			if (!value.getTextContent().isEmpty()) {
				throw TypeValidatorUtil.getEmptyTypeException(value.getTextContent(), value.getTagName());
			}
		}
	}

}
