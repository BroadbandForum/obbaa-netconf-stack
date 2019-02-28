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
