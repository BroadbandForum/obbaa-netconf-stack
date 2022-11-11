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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;

public class BitsTypeConstraintParser implements TypeValidator {
	private final String m_space = " ";
	private final String m_doubleSpace = "  ";
	
	private BitsTypeDefinition m_bitsType;
	private List<String> m_bitNames = new ArrayList<String>();
	
	public BitsTypeConstraintParser(TypeDefinition<?> type) {
		if (type instanceof BitsTypeDefinition) {
			m_bitsType = (BitsTypeDefinition) type;
			m_bitNames = getBitNames(m_bitsType.getBits());//Get a list names of bits
		}
	}
	
	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		if (m_bitsType != null) {
			String textValue = validateInsertAttribute ? insertValue : value.getTextContent();
			if (!textValue.isEmpty()) {
				//Check invalid format bit values
				if (checkInvalidFormatValue(textValue)) {
					throw TypeValidatorUtil.getInvalidFormatBitsException(textValue);
				}
				List<String> bitValues = getBitValues(textValue);
				if (!m_bitNames.isEmpty()) {
					//Check invalid bits
					List<String> invalidBitValues = checkInvalidBits(m_bitNames, bitValues);
					if (!invalidBitValues.isEmpty()) {
						throw TypeValidatorUtil.getInvalidBitsException(invalidBitValues, m_bitNames);
					}
					//Check duplicated bits
					boolean validCheckDuplicatedBits = checkDuplicatedBits(bitValues);
					if (!validCheckDuplicatedBits) {
						throw TypeValidatorUtil.getDuplicatedBitsException(textValue);
					}
				}
			}
		}
	}
	
	/**
	* Check value string before splitting to array bit values*/
	private boolean checkInvalidFormatValue(String value) {
		return value.startsWith(m_space) || value.endsWith(m_space) || value.contains(m_doubleSpace);
	}
	
	/**
	 * Get a list of invalid bit values*/
	private List<String> checkInvalidBits(List<String> bitConstaints, List<String> bitValues) {
		Set<String> invalidBitValues = new LinkedHashSet<String>();
		for (String bitValue : bitValues) {
			if (!bitConstaints.contains(bitValue)) {
				invalidBitValues.add(bitValue);
			}
		}
		List<String> result = new ArrayList<String>();
		result.addAll(invalidBitValues);
		return result;
	}
	
	/**
	 * check duplicated bit values*/
	private boolean checkDuplicatedBits(List<String> bitValues) {
		Set<String> setBitValues = new HashSet<String>(bitValues);
		return setBitValues.size() == bitValues.size();
	}
	
	private List<String> getBitNames(List<Bit> bitConstaints) {
		List<String> bitNames = new ArrayList<String>();
		for (Bit bit : bitConstaints) {
			bitNames.add(bit.getName());
		}
		return bitNames;
	}
	
	private List<String> getBitValues(String strValue) {
		List<String> values = new ArrayList<String>();
		if (!strValue.isEmpty()) {
			values = Arrays.asList(strValue.split(m_space));
		}
		return values;
	}
}