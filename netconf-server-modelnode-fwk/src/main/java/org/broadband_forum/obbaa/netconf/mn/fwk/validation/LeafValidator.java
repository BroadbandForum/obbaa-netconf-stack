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

package org.broadband_forum.obbaa.netconf.mn.fwk.validation;


import static org.broadband_forum.obbaa.netconf.api.util.CryptUtil2.ENCR_STR_PATTERN;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.AbstractSchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.EncryptDecryptUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreConstraintValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.UniqueConstraintValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MandatoryAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.w3c.dom.Element;
/**
 * Single validation class, which performs validation of a Leaf 
 * at different phases
 * 
 * validate(Element) -> phase 1 validation
 * validate(ModelNode) -> phase 2 validation
 * 
 */

public class LeafValidator extends AbstractSchemaNodeConstraintParser implements SchemaNodeConstraintParser, DataStoreConstraintValidator {
	private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	private final LeafSchemaNode m_leafSchemaNode;
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(LeafValidator.class, LogAppNames.NETCONF_STACK);

    protected boolean needsTypeValidation(String operationAttribute) {
        if ((operationAttribute.equals(EditConfigOperations.DELETE)
                || operationAttribute.equals(EditConfigOperations.REMOVE))) {
            if (m_schemaNode instanceof MandatoryAware) {
                return ((MandatoryAware) m_schemaNode).isMandatory();
            }
            else {
                return false;
            }
        }
        return true;
    }

	private void validateLeafValue(ModelNode modelNode, DSValidationContext validationContext) throws ValidationException {
		
		validateChoiceCase(modelNode, m_leafSchemaNode, validationContext);
		
		ConfigLeafAttribute data = getAttributeValueFromParentNode(modelNode);
		
		if (data != null) {
		    validateType(data, modelNode, validationContext);
		}
	}

	protected void validateUnionType(Element dataNodeElement, ModelNode modelNode, TypeDefinition<?> typeDefinition,
									 ConfigLeafAttribute attribute, DSValidationContext validationContext)
			throws ValidationException {
		validateOperation(dataNodeElement);
		super.validateUnionType(dataNodeElement, modelNode, typeDefinition, attribute, validationContext);
	}

	private ConfigLeafAttribute getAttributeValueFromParentNode(ModelNode parentNode) {
		ConfigAttributeHelper configAttributeHelper = parentNode.getMountModelNodeHelperRegistry().getConfigAttributeHelper(parentNode.getModelNodeSchemaPath(), m_leafSchemaNode.getQName());
		if (configAttributeHelper != null) {
			try {
				return configAttributeHelper.getValue(parentNode);
			} catch (GetAttributeException e) {
				LOGGER.error("Error when getting value from ModelNode ConfigAttributeHelper.getValue(ModelNode)", e);
			}
		}
		return null;
	}

	protected boolean validateInstanceIdentifierValue(ModelNode modelNode, TypeDefinition<?> typeDefinition,
													  ConfigLeafAttribute configLeafAttribute, DSValidationContext validationContext) throws ValidationException {
		ConfigAttributeHelper leafHelper = modelNode.getMountModelNodeHelperRegistry().getConfigAttributeHelper(modelNode.getModelNodeSchemaPath(), m_leafSchemaNode.getQName());
		if (leafHelper != null) {
			try { 
				ConfigLeafAttribute xpathStr = leafHelper.getValue(modelNode);
				InstanceIdentifierTypeDefinition type = (InstanceIdentifierTypeDefinition)typeDefinition;
				boolean isRequired = type.requireInstance();
				m_expValidator.validateInstanceIdentifierElement(modelNode, xpathStr, isRequired, m_leafSchemaNode.getQName()
						.getLocalName(), m_leafSchemaNode.getQName().getNamespace().toString(), validationContext);
				return true;
			} catch (GetAttributeException e) {
				LOGGER.error("Error when geting value from ModelNodes", e);
			}
		}
		return false;
	}
	
	protected boolean validateLeafRefValue(ModelNode modelNode, TypeDefinition<?> typeDefinition, ConfigLeafAttribute configLeafAttribute
			, DSValidationContext validationContext) throws ValidationException {
        ConfigAttributeHelper leafrefHelper = modelNode.getMountModelNodeHelperRegistry().getConfigAttributeHelper(modelNode.getModelNodeSchemaPath(), m_leafSchemaNode.getQName());
        if (leafrefHelper != null) {
            try {
                ConfigLeafAttribute valueLeafref = leafrefHelper.getValue(modelNode);
                if (valueLeafref == null)
                    return false;
                LeafrefTypeDefinition type = (LeafrefTypeDefinition) typeDefinition;
                if (type.requireInstance()) {
                    String xpathStr = type.getPathStatement().getOriginalString();

                    boolean isValid = m_expValidator.validateXPathInModelNode(xpathStr, modelNode, valueLeafref.getStringValue(),
                            modelNode, m_leafSchemaNode, validationContext);
                    if (!isValid) {
                        ModelNodeId modelNodeId = buildModelNodeId(modelNode, m_leafSchemaNode.getQName().getLocalName(), m_leafSchemaNode.getQName().getNamespace().toString());

                        throw DataStoreValidationErrors.getMissingDataException(String.format("Dependency violated, '%s' must exist", valueLeafref.getStringValue()),
                                modelNodeId.xPathString(modelNode.getMountRegistry()), modelNodeId.xPathStringNsByPrefix(modelNode.getMountRegistry()));
                    }
                    return true;
                }
            } catch (GetAttributeException e) {
                LOGGER.error("Error when geting value from ModelNodes ", e);
            }
        }
        return false;
    }

	private void validateOperation(Element dataNode) throws ValidationException {
		String operationAttribute = dataNode.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION);
		if (EditConfigOperations.DELETE.equals(operationAttribute)) {
			if (m_leafSchemaNode.isMandatory()) {
				NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
						 "Cannot delete the mandatory attribute '" + dataNode.getNodeName() + "'");
				throw new ValidationException(rpcError);
			}
		}
	}

	public LeafValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
			LeafSchemaNode leafSchemaNode, DSExpressionValidator validationUtil) {
        super(leafSchemaNode.getType(), leafSchemaNode, schemaRegistry, validationUtil);
		m_modelNodeHelperRegistry = modelNodeHelperRegistry;
		m_leafSchemaNode = leafSchemaNode;
	}

	/* (non-Javadoc)
	 * @see DataStoreConstraintValidator#validate(org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode)
	 */
	@Override
	public void validate(ModelNode modelNode, DSValidationContext validationContext) throws ValidationException {
		validateLeafValue(modelNode, validationContext);
		if (m_leafSchemaNode.getType() instanceof InstanceIdentifierTypeDefinition){
			validateInstanceIdentifierValue(modelNode, (InstanceIdentifierTypeDefinition)m_leafSchemaNode.getType(), null, validationContext);
		}
		UniqueConstraintValidator.validateUniqueConstraintForLeafChange(modelNode, m_leafSchemaNode, validationContext);
	}

	@Override
	public void validateLeafRef(ModelNode modelNode, DSValidationContext validationContext) throws ValidationException {
		if (m_leafSchemaNode.getType() instanceof  LeafrefTypeDefinition) {
			validateLeafRefValue(modelNode, (LeafrefTypeDefinition)m_leafSchemaNode.getType(), null, validationContext);
		}
	}

	@Override
	public DataSchemaNode getDataSchemaNode() {
        return m_leafSchemaNode;
	}

	/* (non-Javadoc)
	 * @see AbstractSchemaNodeConstraintParser#validate(org.w3c.dom.Element)
	 */
	@Override
	public void validate(Element dataNode,RequestType requestType) throws ValidationException {
    	validateOperation(dataNode);
    	if(EncryptDecryptUtil.isPassword(m_leafSchemaNode,m_schemaRegistry)
				&& ENCR_STR_PATTERN.matcher(dataNode.getTextContent()).matches()){
			Element leafNode = (Element) dataNode.cloneNode(true);
			leafNode.setTextContent(CryptUtil2.decrypt(leafNode.getTextContent()));
			super.validate(leafNode, requestType);
		}else{
			super.validate(dataNode, requestType);
		}
	}

	@Override
    protected AdvancedLogger getLogger() {
        return LOGGER;
    }

    @Override
    protected boolean isDataAvailable(ModelNode modelNode) {
        ConfigLeafAttribute data = getAttributeValueFromParentNode(modelNode);
        return (data != null && data.getStringValue()!=null) ;
    }


}
