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
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.w3c.dom.Element;

public class IntegerTypeConstraintParser implements TypeValidator {

    private IntegerTypeDefinition m_baseType;
    private List<RangeConstraint> m_rangesList;

    public IntegerTypeConstraintParser(TypeDefinition<?> type) {
        if (type instanceof IntegerTypeDefinition) {
            m_baseType = (IntegerTypeDefinition) type;
            m_rangesList = ((IntegerTypeDefinition) type).getRangeConstraints();
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

    private void validateBaseType(String value) throws ValidationException {
        if (DerivedTypes.isInt8(m_baseType)) {
            TypeValidatorUtil.getInt8Value(value, m_baseType.getRangeConstraints());
        } else if (DerivedTypes.isInt16(m_baseType)) {
            TypeValidatorUtil.getInt16Value(value, m_baseType.getRangeConstraints());
        } else if (DerivedTypes.isInt32(m_baseType)) {
            TypeValidatorUtil.getInt32Value(value, m_baseType.getRangeConstraints());
        } else if (DerivedTypes.isInt64(m_baseType)) {
            TypeValidatorUtil.getInt64Value(value, m_baseType.getRangeConstraints());
        }
    }

    private void validateExtendedType(String stringValue) throws ValidationException {
        if (!m_rangesList.isEmpty()) {
            boolean rangeValidation = false;
            if (DerivedTypes.isInt8(m_baseType)) {
                byte int8Value = TypeValidatorUtil.getInt8Value(stringValue, m_rangesList);
                for (RangeConstraint range : m_rangesList) {
                    if (int8Value >= range.getMin().byteValue() && int8Value <= range.getMax().byteValue()) {
                        rangeValidation = true;
                        break;
                    }
                }
            } else if (DerivedTypes.isInt16(m_baseType)) {
                short int16Value = TypeValidatorUtil.getInt16Value(stringValue, m_rangesList);
                for (RangeConstraint range : m_rangesList) {
                    if (int16Value >= range.getMin().shortValue() && int16Value <= range.getMax().shortValue()) {
                        rangeValidation = true;
                        break;
                    }
                }
            } else if (DerivedTypes.isInt32(m_baseType)) {
                int int32Value = TypeValidatorUtil.getInt32Value(stringValue, m_rangesList);
                for (RangeConstraint range : m_rangesList) {
                    if (int32Value >= range.getMin().intValue() && int32Value <= range.getMax().intValue()) {
                        rangeValidation = true;
                        break;
                    }
                }
            } else if (DerivedTypes.isInt64(m_baseType)) {
                long int64Value = TypeValidatorUtil.getInt64Value(stringValue, m_rangesList);
                for (RangeConstraint range : m_rangesList) {
                    if (int64Value >= range.getMin().longValue() && int64Value <= range.getMax().longValue()) {
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

}
