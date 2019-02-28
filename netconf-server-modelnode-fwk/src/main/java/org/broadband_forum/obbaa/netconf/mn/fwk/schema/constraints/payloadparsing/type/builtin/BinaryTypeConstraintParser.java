package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.util.Optional;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import com.google.common.collect.Range;

public class BinaryTypeConstraintParser implements TypeValidator {

	private BinaryTypeDefinition m_binaryType;
	
	public BinaryTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof BinaryTypeDefinition) {
			m_binaryType = (BinaryTypeDefinition) type;
		}
	}
	
	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		if (m_binaryType != null) {
			String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
			int stringLen = (stringValue.length())/8;
			boolean lengthValidation = false;
			Optional<LengthConstraint> optLengthConstraint = m_binaryType.getLengthConstraint();
			if (optLengthConstraint.isPresent()) {
			    LengthConstraint lengthConstraint = optLengthConstraint.get();
				for(Range<Integer> constraint : lengthConstraint.getAllowedRanges().asRanges()) {
					if (stringLen >= constraint.lowerEndpoint().intValue() && stringLen <= constraint.upperEndpoint().intValue()) {
						lengthValidation = true;
						break;
					}
				}
				
				if (!lengthValidation) {
					
					throw TypeValidatorUtil.getLengthConstraintException(stringValue, lengthConstraint);

				}
			}
		}
	}
	
}
