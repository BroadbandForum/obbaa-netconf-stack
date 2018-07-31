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

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

public class BooleanTypeConstraintParser implements TypeValidator {

    private static final String TRUE = "true";

    private static final String FALSE = "false";

    public BooleanTypeConstraintParser() {

    }

    @Override
    public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws
            ValidationException {
        String content = validateInsertAttribute ? insertValue : value.getTextContent();
        if (!TRUE.equals(content) && !FALSE.equals(content)) {
            throw getInvalidBooleanException(content);
        }
    }

    private ValidationException getInvalidBooleanException(String invalidValue) {
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                String.format("Invalid value. It should be \"true\" or \"false\" instead of \"%s\"",
                        invalidValue));
        return new ValidationException(rpcError);
    }
}
