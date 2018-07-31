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

import java.math.BigInteger;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.w3c.dom.Element;

public class UnsignedIntegerTypeConstraintParser implements TypeValidator {

    private UnsignedIntegerTypeDefinition m_baseType;
    private List<RangeConstraint> m_rangesList;

    public UnsignedIntegerTypeConstraintParser(TypeDefinition<?> type) {
        if (type instanceof UnsignedIntegerTypeDefinition) {
            m_baseType = (UnsignedIntegerTypeDefinition) type;
            m_rangesList = ((UnsignedIntegerTypeDefinition) type).getRangeConstraints();
        }
    }

    @Override
    public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws
            ValidationException {
        String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
        if (m_rangesList == null || m_rangesList.isEmpty()) {
            validateBaseType(stringValue);
        } else {
            validateExtendedType(stringValue);
        }
    }

    private void validateExtendedType(String stringValue) throws ValidationException {
        if (!m_rangesList.isEmpty()) {
            boolean rangeValidation = false;
            if (DerivedTypes.isUint8(m_baseType)) {
                short shortValue = TypeValidatorUtil.getUint8Value(stringValue, m_rangesList);
                for (RangeConstraint range : m_rangesList) {
                    if (shortValue >= range.getMin().shortValue() && shortValue <= range.getMax().shortValue()) {
                        rangeValidation = true;
                        break;
                    }
                }
            } else if (DerivedTypes.isUint16(m_baseType)) {
                int intValue = TypeValidatorUtil.getUint16Value(stringValue, m_rangesList);
                for (RangeConstraint range : m_rangesList) {
                    if (intValue >= range.getMin().intValue() && intValue <= range.getMax().intValue()) {
                        rangeValidation = true;
                        break;
                    }
                }
            } else if (DerivedTypes.isUint32(m_baseType)) {
                long longValue = TypeValidatorUtil.getUint32Value(stringValue, m_rangesList);
                for (RangeConstraint range : m_rangesList) {
                    if (longValue >= range.getMin().longValue() && longValue <= range.getMax().longValue()) {
                        rangeValidation = true;
                        break;
                    }
                }
            } else if (DerivedTypes.isUint64(m_baseType)) {
                BigInteger bigValue = TypeValidatorUtil.getUint64Value(stringValue, m_rangesList);
                BigInteger minValue, maxValue;
                for (RangeConstraint range : m_rangesList) {
                    minValue = new BigInteger(range.getMin().toString());
                    maxValue = new BigInteger(range.getMax().toString());
                    if (bigValue.compareTo(minValue) >= 0 && maxValue.compareTo(bigValue) >= 0) {
                        rangeValidation = true;
                        break;
                    }
                }

            }

            if (!rangeValidation) {
                throw TypeValidatorUtil.getOutOfRangeException(stringValue, m_rangesList);
            }
        }
    }

    private void validateBaseType(String stringValue) throws ValidationException {
        if (DerivedTypes.isUint8(m_baseType)) {
            TypeValidatorUtil.getUint8Value(stringValue, m_baseType.getRangeConstraints());
        } else if (DerivedTypes.isUint16(m_baseType)) {
            TypeValidatorUtil.getUint16Value(stringValue, m_baseType.getRangeConstraints());
        } else if (DerivedTypes.isUint32(m_baseType)) {
            TypeValidatorUtil.getUint32Value(stringValue, m_baseType.getRangeConstraints());
        } else if (DerivedTypes.isUint64(m_baseType)) {
            TypeValidatorUtil.getUint64Value(stringValue, m_baseType.getRangeConstraints());
        }
    }

}
