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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.w3c.dom.Element;


public class StringTypeConstraintParser implements TypeValidator {

    private StringTypeDefinition m_stringType;

    private List<Pattern> m_patternsList = new ArrayList<Pattern>();

    public StringTypeConstraintParser(TypeDefinition<?> type) {
        if (type instanceof StringTypeDefinition) {
            m_stringType = (StringTypeDefinition) type;
            List<PatternConstraint> patternConstraints = m_stringType.getPatternConstraints();
            if (!patternConstraints.isEmpty()) {
                for (PatternConstraint constraint : patternConstraints) {

                    String pattern = constraint.getRegularExpression();

                    m_patternsList.add(Pattern.compile(pattern));
                }
            }
        }
    }

    @Override
    public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws
            ValidationException {
        if (m_stringType != null) {
            String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
            int stringLen = stringValue.length();
            boolean lengthValiation = false;
            boolean patternValidation = false;
            List<LengthConstraint> lengthConstraints = m_stringType.getLengthConstraints();
            List<PatternConstraint> patternConstraints = m_stringType.getPatternConstraints();

            if (!lengthConstraints.isEmpty()) {
                for (LengthConstraint constraint : lengthConstraints) {
                    if (stringLen >= constraint.getMin().intValue() && stringLen <= constraint.getMax().intValue()) {
                        lengthValiation = true;
                        break;
                    }
                }

                if (!lengthValiation) {
                    throw TypeValidatorUtil.getLengthConstraintException(stringValue, lengthConstraints);
                }
            }

            if (!m_patternsList.isEmpty()) {
                for (Pattern pattern : m_patternsList) {
                    Matcher matcher = pattern.matcher(stringValue);
                    if (matcher.matches()) {
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
