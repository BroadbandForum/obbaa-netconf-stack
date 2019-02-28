package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import com.google.common.collect.Range;


public class StringTypeConstraintParser implements TypeValidator {
	
	private StringTypeDefinition m_stringType;
	
	private List<Pattern> m_patternsList = new ArrayList<Pattern>();
	
	public StringTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof StringTypeDefinition) {
			m_stringType = (StringTypeDefinition) type;
			List<PatternConstraint> patternConstraints = m_stringType.getPatternConstraints();
			if (!patternConstraints.isEmpty()) {
				for(PatternConstraint constraint : patternConstraints) {

					String pattern = constraint.getJavaPatternString();

					m_patternsList.add(Pattern.compile(pattern));
				}
			}
		}
	}

	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		if (m_stringType != null) {
			String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
			int stringLen = stringValue.length();
			boolean lengthValiation = false;
			boolean patternValidation = false;
			Optional<LengthConstraint> optLengthConstraint = m_stringType.getLengthConstraint();
			List<PatternConstraint> patternConstraints = m_stringType.getPatternConstraints();
					
			if (optLengthConstraint.isPresent()) {
			    LengthConstraint lengthConstraint = optLengthConstraint.get();
				for(Range<Integer> constraint : lengthConstraint.getAllowedRanges().asRanges()) {
					if (stringLen >= constraint.lowerEndpoint().intValue() && stringLen <= constraint.upperEndpoint().intValue()) {
						lengthValiation = true;
						break;
					}
				}
				
				if (!lengthValiation) {
					throw TypeValidatorUtil.getLengthConstraintException(stringValue, lengthConstraint);
				}
			}
			
			if (!m_patternsList.isEmpty()) {
				for (Pattern pattern : m_patternsList) {
					Matcher matcher = pattern.matcher(stringValue);
					if(matcher.matches()) {
						patternValidation = true;
						break;
					}
				}
				
				if (!patternValidation) {
					throw TypeValidatorUtil.getPatternConstraintException(stringValue, patternConstraints);
				}
			}
		}
	}
}
