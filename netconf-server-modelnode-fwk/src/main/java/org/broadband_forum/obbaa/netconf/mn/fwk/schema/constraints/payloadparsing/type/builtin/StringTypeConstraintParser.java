/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin.StringUnicodeNonSupportedCharacter;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import com.google.common.collect.Range;


public class StringTypeConstraintParser implements TypeValidator {

	private StringTypeDefinition m_stringType;

	public StringTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof StringTypeDefinition) {
			m_stringType = (StringTypeDefinition) type;
		}
	}

	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		if (m_stringType != null) {
			/**
			 * As per RFC section 9.4,String Built-in type supporting unicode character but excluding the non characters
			 */
			boolean isInvalidCharacter = StringUnicodeNonSupportedCharacter.containForbiddenCharacter(value);
			if (isInvalidCharacter) {
				NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
						"String value contains unsupported noncharacter unicode: " + value.getTextContent());
				throw new ValidationException(rpcError);
			}
			String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
			int stringLen = stringValue.length();
			boolean lengthValiation = false;
			Optional<LengthConstraint> optLengthConstraint = m_stringType.getLengthConstraint();
			// validation for length constraints
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

			/**
			 * As per RFC section 9.4.5, if the type has multiple "pattern", the expressions are ANDed together.
			 * If pattern has "invert-match" modifier, the type is restricted to values that do not match the pattern
			 */
			List<PatternConstraint> patternConstraints = m_stringType.getPatternConstraints();
			List<PatternConstraint> failurePatternList = new ArrayList<PatternConstraint>();
			if (!patternConstraints.isEmpty()) {
				for (PatternConstraint constraint : patternConstraints) {
					String patternStr = constraint.getJavaPatternString();
					Pattern pattern = Pattern.compile(patternStr);
					Matcher matcher = pattern.matcher(stringValue);
					if (constraint.getModifier().isPresent()
							&& ModifierKind.INVERT_MATCH.equals(constraint.getModifier().get())) {
						//For 'invert-match' modifier, add to failure list if inputStr should match with given pattern
						if (matcher.matches()) {
							failurePatternList.add(constraint);
							break;
						}
					} else {
						//For regular pattern, add to failure list if inputStr should not match with given pattern
						if (!matcher.matches()) {
							failurePatternList.add(constraint);
							break;
						}
					}
				}
				if (!failurePatternList.isEmpty()) {
					throw TypeValidatorUtil.getPatternConstraintException(stringValue, failurePatternList);
				}
			}
		}
	}
}
