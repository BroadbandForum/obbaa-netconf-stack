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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.w3c.dom.Node;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class IdentityRefUtil {

    public static final String COLON = ":";
    private final static String DEFAULT_NC_NS = "urn:ietf:params:xml:ns:netconf:base:1.0";

    public static boolean checkDerivedIdentities(IdentitySchemaNode identitySchemaNode, String namespace, String identityValue) {
        for(IdentitySchemaNode derivedIdentity: identitySchemaNode.getDerivedIdentities()){
            //DFS
            if(identityMatches(derivedIdentity, namespace, identityValue)){
                return true;
            }else if(checkDerivedIdentities(derivedIdentity, namespace, identityValue)){
                return true;
            }
        }
        return false;
    }

    public static boolean identityMatches(IdentitySchemaNode identitySchemaNode, String namespace, String identityValue) {
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

    public static String getNamespace(Node node, String stringValue) {
        int colonIndex = stringValue.indexOf(COLON);
        String namespace;
        if (colonIndex >= 0) {
            String prefix = stringValue.substring(0, colonIndex);
            namespace = NamespaceUtil.getAttributeNameSpace(node, prefix);
            if (namespace == null) {
                throw new ValidationException(getInvalidIdentityRefRpcError(stringValue));
            }

        } else {
            namespace = NamespaceUtil.getAttributeNameSpace(node, null);
        }
       return namespace;
    }

    public static String getIdentityValue(String stringValue) {
        int colonIndex = stringValue.indexOf(COLON);
        String identityValue;
        if (colonIndex >= 0) {
            identityValue = stringValue.substring(colonIndex + 1).trim();
        } else {
            identityValue = stringValue.trim();
        }
        return identityValue;
    }

    public static NetconfRpcError getInvalidIdentityRefRpcError(String value) {
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                "Value \"" + value + "\" is not a valid identityref value.");
        return rpcError;
    }
}