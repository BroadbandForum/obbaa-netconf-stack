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

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.NamespaceUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

public class IdentityRefTypeConstraintParser implements TypeValidator {

    private static final String COLON = ":";
    private Set<IdentitySchemaNode> m_derivedIdentities = new HashSet<>();
    public final static String DEFAULT_NC_NS = "urn:ietf:params:xml:ns:netconf:base:1.0";

    public IdentityRefTypeConstraintParser(TypeDefinition<?> type) {
        IdentityrefTypeDefinition baseType = null;
        if (type instanceof IdentityrefTypeDefinition) {
            baseType = (IdentityrefTypeDefinition) type;
        } else {
            throw new IllegalArgumentException("type must be identity-ref");
        }
        Set<IdentitySchemaNode> identitySchemaNodes = baseType.getIdentities();
        for (IdentitySchemaNode identitySchemaNode : identitySchemaNodes) {
            m_derivedIdentities.addAll(identitySchemaNode.getDerivedIdentities());
        }
    }

    @Override
    public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws
            ValidationException {
        String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
        if (stringValue == null || stringValue.isEmpty()) {
            throw getInvalidValueException("");
        }

        int colonIndex = stringValue.indexOf(COLON);
        String namespace = null;
        String identityValue = null;
        if (colonIndex >= 0) {
            String prefix = stringValue.substring(0, colonIndex);
            namespace = NamespaceUtil.getAttributeNameSpace(value, prefix);
            if (namespace == null) {
                throw getInvalidValueException(stringValue);
            }
            identityValue = stringValue.substring(colonIndex + 1).trim();
        } else {
            identityValue = stringValue.trim();
            namespace = NamespaceUtil.getAttributeNameSpace(value, null);
        }

        boolean isValid = false;
        for (IdentitySchemaNode identitySchemaNode : m_derivedIdentities) {
            isValid = identityMatches(identitySchemaNode, namespace, identityValue);
            if (!isValid) {
                isValid = checkDerivedIdentities(identitySchemaNode, namespace, identityValue);
            }
            if (isValid) {
                return;
            }

        }

        if (!isValid) {
            throw getInvalidValueException(stringValue);
        }
    }

    private boolean checkDerivedIdentities(IdentitySchemaNode identitySchemaNode, String namespace, String
            identityValue) {
        for (IdentitySchemaNode derivedIdentity : identitySchemaNode.getDerivedIdentities()) {
            //DFS
            if (identityMatches(derivedIdentity, namespace, identityValue)) {
                return true;
            } else if (checkDerivedIdentities(derivedIdentity, namespace, identityValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean identityMatches(IdentitySchemaNode identitySchemaNode, String namespace, String identityValue) {
        if (namespace == null || namespace.equals(DEFAULT_NC_NS)) {
            /**
             * Since the prefix is missing, the identity ref namespace is used, since this also has to be local
             * to the module. If this is an imported identity, prefix have to be specified. 
             */
            namespace = identitySchemaNode.getQName().getNamespace().toString();
        }
        QName qName = identitySchemaNode.getQName();
        if (namespace.equals(qName.getNamespace().toString()) && identityValue.equals(qName.getLocalName())) {
            return true;
        }
        return false;
    }

    private ValidationException getInvalidValueException(String value) {
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                "Value \"" + value + "\" is not a valid identityref value.");
        return new ValidationException(rpcError);
    }
}
