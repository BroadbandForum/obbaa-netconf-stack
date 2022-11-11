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

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.w3c.dom.Element;

/**
 *
 *
 */
public class UnionTypeConstraintParser implements TypeValidator {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(UnionTypeConstraintParser.class, LogAppNames.NETCONF_STACK);
	
	private List<TypeValidator> m_typeValidators = new ArrayList<TypeValidator>();
	private boolean m_leafrefORInstanceIdentifierTypeExits = false;
	
	/**
	 * 
	 */
	public UnionTypeConstraintParser(UnionTypeDefinition unionType, SchemaRegistry schemaRegistry) {
		for(TypeDefinition<?> type : unionType.getTypes()) {
			// skip leafref type validation, it will be handled in phase-II validation
			if (type instanceof LeafrefTypeDefinition || type instanceof InstanceIdentifierTypeDefinition) {
				m_leafrefORInstanceIdentifierTypeExits = true;
			}
			TypeValidator validator = TypeValidatorFactory.getInstance().getValidator(type, schemaRegistry);
			if (validator != null) {
			    m_typeValidators.add(validator);
			}
		}
	}

	@Override
	public void validate(Element value, boolean validateInsertAttribute, String insertValue) throws ValidationException {
		boolean isValid = false;
		NetconfRpcError netconfRpcError = null;
		StringBuilder errorMessage = new StringBuilder();
		StringBuilder errorAppTag = new StringBuilder();

		for(TypeValidator typeValidator : m_typeValidators) {
			//value should valid by one of validator 
			try {
				typeValidator.validate(value, validateInsertAttribute, insertValue);
				isValid = true;
				break;
			} catch (ValidationException e) {
				errorMessage.append(e.getRpcError().getErrorMessage());
				errorMessage.append(" or ");
				if(e.getRpcError().getErrorAppTag() != null){
					errorAppTag.append(e.getRpcError().getErrorAppTag());
					errorAppTag.append(" or ");
				}
				netconfRpcError = e.getRpcError();
				LOGGER.debug("Ignoring validation exception, as value should be valid by one of union validator", e);
			}
		}
		if(!m_leafrefORInstanceIdentifierTypeExits && !isValid) {
			netconfRpcError.setErrorMessage(errorMessage.substring(0, errorMessage.length() - 4));
			netconfRpcError.setErrorAppTag(errorAppTag.substring(0, errorAppTag.length() == 0 ? 0 : errorAppTag.length() - 4));
			throw new ValidationException(netconfRpcError);
		}
	}

}
