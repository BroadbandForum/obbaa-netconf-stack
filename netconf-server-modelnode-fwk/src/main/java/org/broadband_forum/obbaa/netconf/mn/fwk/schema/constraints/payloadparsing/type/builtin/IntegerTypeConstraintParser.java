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

import java.util.Optional;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class IntegerTypeConstraintParser implements TypeValidator {

	private RangeRestrictedTypeDefinition m_baseType;
	private Optional<RangeConstraint> m_optRanges;

	public IntegerTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof Int8TypeDefinition
		        || type instanceof Int16TypeDefinition
		        || type instanceof Int32TypeDefinition
		        || type instanceof Int64TypeDefinition) {
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

	private void validateBaseType(String value) throws ValidationException {
	    Optional<RangeConstraint> optRangeConstraint = m_baseType.getRangeConstraint();
	    RangeConstraint rangeConstraint = optRangeConstraint.orElse(null);
		if (m_baseType instanceof Int8TypeDefinition) {
			TypeValidatorUtil.getInt8Value(value, rangeConstraint);
		} else if (m_baseType instanceof Int16TypeDefinition) {
			TypeValidatorUtil.getInt16Value(value, rangeConstraint);
		} else if (m_baseType instanceof Int32TypeDefinition) {
			TypeValidatorUtil.getInt32Value(value, rangeConstraint);
		} else if (m_baseType instanceof Int64TypeDefinition) {
			TypeValidatorUtil.getInt64Value(value, rangeConstraint);
		}
	}

	private void validateExtendedType(String stringValue) throws ValidationException {
	    RangeConstraint rangeConstraint = m_optRanges.get();
	    RangeSet allowedRanges = rangeConstraint.getAllowedRanges();
		if (!allowedRanges.isEmpty()) {
			boolean rangeValidation = false;
			if (m_baseType instanceof Int8TypeDefinition) {
				byte int8Value = TypeValidatorUtil.getInt8Value(stringValue, rangeConstraint);
				for (Range<Byte> range : (Set<Range<Byte>>)allowedRanges.asRanges()) {
					if (int8Value >= range.lowerEndpoint().byteValue() && int8Value <= range.upperEndpoint().byteValue()) {
						rangeValidation = true;
						break;
					}
				}
			} else if (m_baseType instanceof Int16TypeDefinition) {
				short int16Value = TypeValidatorUtil.getInt16Value(stringValue, rangeConstraint);
				for (Range<Short> range : (Set<Range<Short>>)allowedRanges.asRanges()) {
					if (int16Value >= range.lowerEndpoint().shortValue() && int16Value <= range.upperEndpoint().shortValue()) {
						rangeValidation = true;
						break;
					}
				}
			} else if (m_baseType instanceof Int32TypeDefinition) {
				int int32Value = TypeValidatorUtil.getInt32Value(stringValue, rangeConstraint);
				for (Range<Integer> range : (Set<Range<Integer>>)allowedRanges.asRanges()) {
					if (int32Value >= range.lowerEndpoint().intValue() && int32Value <= range.upperEndpoint().intValue()) {
						rangeValidation = true;
						break;
					}
				}
			} else if (m_baseType instanceof Int64TypeDefinition) {
				long int64Value = TypeValidatorUtil.getInt64Value(stringValue, rangeConstraint);
				for (Range<Long> range : (Set<Range<Long>>)allowedRanges.asRanges()) {
					if (int64Value >= range.lowerEndpoint().longValue() && int64Value <= range.upperEndpoint().longValue()) {
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

}
