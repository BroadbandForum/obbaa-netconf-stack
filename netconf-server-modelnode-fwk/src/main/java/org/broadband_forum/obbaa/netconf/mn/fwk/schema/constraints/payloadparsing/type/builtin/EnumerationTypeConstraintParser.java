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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

public class EnumerationTypeConstraintParser implements TypeValidator {

    private EnumTypeDefinition m_baseType;

    public EnumerationTypeConstraintParser(TypeDefinition<?> type) {
        if (type instanceof EnumTypeDefinition) {
            m_baseType = (EnumTypeDefinition) type;
        }
    }

    @Override
    public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws
            ValidationException {
        List<String> enumValues = new ArrayList<String>();
        String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
        for (EnumPair enumPair : m_baseType.getValues()) {
            enumValues.add(enumPair.getName());
            if (enumPair.getName().equals(stringValue)) {
                return;
            }
        }
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                "Value \"" + stringValue + "\" is an invalid value! Expected values: " + enumValues.toString());
        throw new ValidationException(rpcError);
    }

}
