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

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.w3c.dom.Element;
import org.bouncycastle.util.encoders.Base64;
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
			byte[] octetString = Base64.decode(stringValue);			
			int stringLen = octetString.length;
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
