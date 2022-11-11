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

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.IdentityRefUtil;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;

public class IdentityRefTypeConstraintParser implements TypeValidator {

	private Set<IdentitySchemaNode> m_derivedIdentities = new HashSet<>();
	public final static String DEFAULT_NC_NS = "urn:ietf:params:xml:ns:netconf:base:1.0";

	public IdentityRefTypeConstraintParser(TypeDefinition<?> type) {
		IdentityrefTypeDefinition baseType = null;
		if (type instanceof IdentityrefTypeDefinition) {
			baseType = (IdentityrefTypeDefinition)type;
		} else {
			throw new IllegalArgumentException("type must be identity-ref");
		}
		Set<IdentitySchemaNode> identitySchemaNodes = baseType.getIdentities();
		for(IdentitySchemaNode identitySchemaNode : identitySchemaNodes){
			m_derivedIdentities.addAll(identitySchemaNode.getDerivedIdentities());
		}
	}

	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		String stringValue = validateInsertAttribute ? insertValue : value.getTextContent();
		if (stringValue == null || stringValue.isEmpty()) {
			throw new ValidationException(IdentityRefUtil.getInvalidIdentityRefRpcError(""));
		}

		String namespace;
		String identityValue;
		namespace = IdentityRefUtil.getNamespace(value, stringValue);
		identityValue = IdentityRefUtil.getIdentityValue(stringValue);

		boolean isValid = false;
		for (IdentitySchemaNode identitySchemaNode : m_derivedIdentities) {
			isValid = IdentityRefUtil.identityMatches(identitySchemaNode, namespace, identityValue);
			if(!isValid){
				isValid = IdentityRefUtil.checkDerivedIdentities(identitySchemaNode, namespace, identityValue);
			}
			if(isValid){
				return;
			}
		}

		if (!isValid) {
			throw new ValidationException(IdentityRefUtil.getInvalidIdentityRefRpcError(stringValue));
		}
	}

}
