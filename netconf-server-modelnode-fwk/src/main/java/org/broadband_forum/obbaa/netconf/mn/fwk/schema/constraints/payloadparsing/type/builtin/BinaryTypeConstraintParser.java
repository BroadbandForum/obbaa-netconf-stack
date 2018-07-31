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

import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.w3c.dom.Element;

public class BinaryTypeConstraintParser implements TypeValidator {

    private BinaryTypeDefinition m_binaryType;

    public BinaryTypeConstraintParser(TypeDefinition<?> type) {
        if (type instanceof BinaryTypeDefinition) {
            m_binaryType = (BinaryTypeDefinition) type;
        }
    }

    @Override
    public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws
            ValidationException {
        if (m_binaryType != null) {
            String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
            int stringLen = (stringValue.length()) / 8;
            boolean lengthValidation = false;
            List<LengthConstraint> lengthConstraints = m_binaryType.getLengthConstraints();
            if (!lengthConstraints.isEmpty()) {
                for (LengthConstraint constraint : lengthConstraints) {
                    if (stringLen >= constraint.getMin().intValue() && stringLen <= constraint.getMax().intValue()) {
                        lengthValidation = true;
                        break;
                    }
                }

                if (!lengthValidation) {

                    throw TypeValidatorUtil.getLengthConstraintException(stringValue, lengthConstraints);

                }
            }
        }
    }

}
