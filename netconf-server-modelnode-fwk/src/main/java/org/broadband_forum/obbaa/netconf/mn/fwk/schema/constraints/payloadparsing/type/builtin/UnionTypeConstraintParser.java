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

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * @author gnanavek
 */
public class UnionTypeConstraintParser implements TypeValidator {

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(UnionTypeConstraintParser.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    private List<TypeValidator> m_typeValidators = new ArrayList<TypeValidator>();

    public UnionTypeConstraintParser(UnionTypeDefinition unionType) {
        for (TypeDefinition<?> type : unionType.getTypes()) {
            TypeValidator valiator = TypeValidatorFactory.getInstance().getValidator(type);
            m_typeValidators.add(valiator);
        }
    }

    @Override
    public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws
            ValidationException {
        boolean isValid = false;
        NetconfRpcError netconfRpcError = null;
        StringBuilder errorMessage = new StringBuilder();
        StringBuilder errorAppTag = new StringBuilder();

        for (TypeValidator typeValidator : m_typeValidators) {
            //value should valid by one of validator
            try {
                typeValidator.validate(value, validateInsertAttribute, insertValue);
                isValid = true;
                break;
            } catch (ValidationException e) {
                errorMessage.append(e.getRpcError().getErrorMessage());
                errorMessage.append(" or ");
                if (e.getRpcError().getErrorAppTag() != null) {
                    errorAppTag.append(e.getRpcError().getErrorAppTag());
                    errorAppTag.append(" or ");
                }
                netconfRpcError = e.getRpcError();
                LOGGER.debug("Ignoring validation exception, as value should be valid by one of union validator", e);
            }
        }
        if (!isValid) {
            netconfRpcError.setErrorMessage(errorMessage.substring(0, errorMessage.length() - 4));
            netconfRpcError.setErrorAppTag(errorAppTag.substring(0, errorAppTag.length() - 4));
            throw new ValidationException(netconfRpcError);
        }
    }

}
