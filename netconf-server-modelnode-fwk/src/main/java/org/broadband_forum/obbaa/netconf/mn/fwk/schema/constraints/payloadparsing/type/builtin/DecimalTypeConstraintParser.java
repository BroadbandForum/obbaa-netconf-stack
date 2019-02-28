package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class DecimalTypeConstraintParser implements TypeValidator {
	
	private static final Map<Integer, Pair<BigDecimal, BigDecimal>> FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING = new HashMap<>();
	private int m_fractionDigit;
		
	static {
		initFractionDigits();
	}

	private static void initFractionDigits() {
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(1, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-922337203685477580.8"), new BigDecimal("922337203685477580.7")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(2, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-92233720368547758.08"), new BigDecimal("92233720368547758.07")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(3, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-9223372036854775.808"), new BigDecimal("9223372036854775.807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(4, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-922337203685477.5808"), new BigDecimal("922337203685477.5807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(5, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-92233720368547.75808"), new BigDecimal("92233720368547.75807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(6, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-9223372036854.775808"), new BigDecimal("9223372036854.775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(7, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-922337203685.4775808"), new BigDecimal("922337203685.4775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(8, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-92233720368.54775808"), new BigDecimal("92233720368.54775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(9, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-9223372036.854775808"), new BigDecimal("9223372036.854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(10, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-922337203.6854775808"), new BigDecimal("922337203.6854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(11, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-92233720.36854775808"), new BigDecimal("92233720.36854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(12, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-9223372.036854775808"), new BigDecimal("9223372.036854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(13, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-922337.2036854775808"), new BigDecimal("922337.2036854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(14, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-92233.72036854775808"), new BigDecimal("92233.72036854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(15, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-9223.372036854775808"), new BigDecimal("9223.372036854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(16, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-922.3372036854775808"), new BigDecimal("922.3372036854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(17, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-92.23372036854775808"), new BigDecimal("92.23372036854775807")));
			FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.put(18, new Pair<BigDecimal, BigDecimal>(new BigDecimal("-9.223372036854775808"), new BigDecimal("9.223372036854775807")));
	}
		
	protected Map<Integer, Pair<BigDecimal, BigDecimal>> getFractionDigitsMapping() {
		return FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING;
	}
		
	protected Pair<BigDecimal, BigDecimal> getFractionDigits(int index) {
		return FRACTION_INDEX_TO_FRACTION_VALUES_MAPPING.get(index);
	}
		
	private Optional<RangeConstraint<BigDecimal>> m_optRanges;
	
	public DecimalTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof DecimalTypeDefinition) {
			m_optRanges = ((DecimalTypeDefinition) type).getRangeConstraint();
			m_fractionDigit = ((DecimalTypeDefinition) type).getFractionDigits();
		}
	}
	
	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
		if (!m_optRanges.isPresent()) {
			validateBaseType(stringValue);
		} else {
			validateExtendedType(stringValue);
		}
	}
	
	private void validateExtendedType(String stringValue) throws ValidationException {
        RangeConstraint rangeConstraint = m_optRanges.get();
        RangeSet<BigDecimal> allowedRanges = rangeConstraint.getAllowedRanges();
		if(!allowedRanges.isEmpty()) {
			boolean rangeValidation = false;
			
			BigDecimal decimalValue = TypeValidatorUtil.getBigDecimalValue(stringValue, rangeConstraint);
			Pair<BigDecimal, BigDecimal> fractionDigitValue = getFractionDigits(m_fractionDigit);
			if (decimalValue.compareTo(fractionDigitValue.getFirst()) >= 0 && decimalValue.compareTo(fractionDigitValue.getSecond()) <= 0) {
				for (Range<BigDecimal> range : allowedRanges.asRanges()) {
					BigDecimal minRange = TypeValidatorUtil.getBigDecimalValue(range.lowerEndpoint().toString(), rangeConstraint);
					BigDecimal maxRange = TypeValidatorUtil.getBigDecimalValue(range.upperEndpoint().toString(), rangeConstraint);
					if (decimalValue.compareTo(minRange) >= 0 && decimalValue.compareTo(maxRange) <= 0) {
						rangeValidation = true;
						break;
					}
				}
			} else {
				throw TypeValidatorUtil.getOutOfRangeException(stringValue, rangeConstraint);
			}
			
			if (!rangeValidation) {
				throw TypeValidatorUtil.getOutOfRangeException(stringValue, rangeConstraint);
			}
		}
	}
	
	private void validateBaseType(String stringValue) throws ValidationException {
		Pair<BigDecimal, BigDecimal> fractionDigitValue = getFractionDigits(m_fractionDigit);
		BigDecimal min = fractionDigitValue.getFirst();
		BigDecimal max = fractionDigitValue.getSecond();
		
		BigDecimal decimalValue = TypeValidatorUtil.getBigDecimalValue(stringValue, min, max);
		if (decimalValue.compareTo(min) < 0 || decimalValue.compareTo(max) > 0) {
			throw TypeValidatorUtil.getOutOfRangeException(stringValue, m_optRanges.orElse(null));
		}
	}
	
}
