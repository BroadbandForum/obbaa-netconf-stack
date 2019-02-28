package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class UnsignedIntegerTypeConstraintParser implements TypeValidator {

	private RangeRestrictedTypeDefinition m_baseType;
	private Optional<RangeConstraint> m_optRanges;

	public UnsignedIntegerTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof Uint8TypeDefinition
		        || type instanceof Uint16TypeDefinition
		        || type instanceof Uint32TypeDefinition
		        || type instanceof Uint64TypeDefinition) {
			m_baseType = (RangeRestrictedTypeDefinition)type;
			m_optRanges = m_baseType.getRangeConstraint();
		}
	}
	
	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
		if (!m_optRanges.isPresent() || m_optRanges.get().getAllowedRanges().isEmpty()) {
			validateBaseType(stringValue);
		} else {
			validateExtendedType(stringValue);
		}
	}

	private void validateExtendedType(String stringValue) throws ValidationException {
		RangeConstraint rangeConstraint = m_optRanges.get();
        RangeSet allowedRanges = rangeConstraint.getAllowedRanges();
        if (!allowedRanges.isEmpty()) {
			boolean rangeValidation = false;
			if (m_baseType instanceof Uint8TypeDefinition) {
				short shortValue = TypeValidatorUtil.getUint8Value(stringValue, rangeConstraint);
				for (Range<Short> range : (Set<Range<Short>>)allowedRanges.asRanges()) {
					if (shortValue >= range.lowerEndpoint().shortValue() && shortValue <= range.upperEndpoint().shortValue()) {
						rangeValidation = true;
						break;
					}
				}
			} else if (m_baseType instanceof Uint16TypeDefinition) {
				int intValue = TypeValidatorUtil.getUint16Value(stringValue, rangeConstraint);
				for (Range<Integer> range : (Set<Range<Integer>>)allowedRanges.asRanges()) {
					if (intValue >= range.lowerEndpoint().intValue() && intValue <= range.upperEndpoint().intValue()) {
						rangeValidation = true;
						break;
					}
				}
			} else if (m_baseType instanceof Uint32TypeDefinition) {
				long longValue = TypeValidatorUtil.getUint32Value(stringValue, rangeConstraint);
				for (Range<Long> range : (Set<Range<Long>>)allowedRanges.asRanges()) {
					if (longValue >= range.lowerEndpoint().longValue() && longValue <= range.upperEndpoint().longValue()) {
						rangeValidation = true;
						break;
					}
				}
			} else if (m_baseType instanceof Uint64TypeDefinition) {
				BigInteger bigValue = TypeValidatorUtil.getUint64Value(stringValue, rangeConstraint);
				for (Range<BigInteger> range : (Set<Range<BigInteger>>)allowedRanges.asRanges()) {
					BigInteger minValue = range.lowerEndpoint();
					BigInteger maxValue = range.upperEndpoint();
					if (bigValue.compareTo(minValue) >= 0 && maxValue.compareTo(bigValue) >= 0) {
						rangeValidation = true;
						break;
					}
				}

			}

			if (!rangeValidation) {
				throw TypeValidatorUtil.getOutOfRangeException(stringValue, rangeConstraint);
			}
		}
	}

	private void validateBaseType(String stringValue) throws ValidationException {
	    Optional<RangeConstraint> optRangeConstraint = m_baseType.getRangeConstraint();
	    RangeConstraint rangeConstraint = optRangeConstraint.orElse(null);
		if (m_baseType instanceof Uint8TypeDefinition) {
            TypeValidatorUtil.getUint8Value(stringValue, rangeConstraint);
		} else if (m_baseType instanceof Uint16TypeDefinition) {
			TypeValidatorUtil.getUint16Value(stringValue, rangeConstraint);
		} else if (m_baseType instanceof Uint32TypeDefinition) {
			TypeValidatorUtil.getUint32Value(stringValue, rangeConstraint);
		} else if (m_baseType instanceof Uint64TypeDefinition) {
			TypeValidatorUtil.getUint64Value(stringValue, rangeConstraint);
		}
	}

}
