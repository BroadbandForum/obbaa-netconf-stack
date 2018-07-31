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


import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.AbstractSchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreConstraintValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;

import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Single validation class, which performs validation of a Leaf
 * at different phases
 * <p>
 * validate(Element) -> phase 1 validation
 * validate(ModelNode) -> phase 2 validation
 */

public class LeafValidator extends AbstractSchemaNodeConstraintParser implements SchemaNodeConstraintParser,
        DataStoreConstraintValidator {
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final LeafSchemaNode m_leafSchemaNode;
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(LeafValidator.class, "netconf-stack",
            "DEBUG", "GLOBAL");

    protected boolean needsTypeValidation(String operationAttribute) {
        if ((operationAttribute.equals(EditConfigOperations.DELETE)
                || operationAttribute.equals(EditConfigOperations.REMOVE))) {
            return m_schemaNode.getConstraints().isMandatory();
        }
        return true;
    }

    private void validateLeafValue(ModelNode modelNode) throws ValidationException {

        validateChoiceCase(modelNode, m_leafSchemaNode);

        ConfigLeafAttribute data = getAttributeValueFromParentNode(modelNode);

        if (data != null) {
            validateType(data, modelNode);
        }
    }

    private ConfigLeafAttribute getAttributeValueFromParentNode(ModelNode parentNode) {
        ConfigAttributeHelper configAttributeHelper = m_modelNodeHelperRegistry.getConfigAttributeHelper(parentNode
                .getModelNodeSchemaPath(), m_leafSchemaNode.getQName());
        if (configAttributeHelper != null) {
            try {
                return configAttributeHelper.getValue(parentNode);
            } catch (GetAttributeException e) {
                LOGGER.error("Error when getting value from ModelNode ConfigAttributeHelper.getValue(ModelNode)", e);
            }
        }
        return null;
    }

    private void validateInstanceIdentifierLeafValue(ModelNode modelNode) throws ValidationException {
        ConfigAttributeHelper leafHelper = m_modelNodeHelperRegistry.getConfigAttributeHelper(modelNode
                .getModelNodeSchemaPath(), m_leafSchemaNode.getQName());
        if (leafHelper != null) {
            try {
                ConfigLeafAttribute xpathStr = leafHelper.getValue(modelNode);
                boolean isRequired = ((InstanceIdentifierTypeDefinition) m_leafSchemaNode.getType()).requireInstance();
                m_expValidator.validateInstanceIdentifierElement(modelNode, xpathStr, isRequired, m_leafSchemaNode
                        .getQName()
                        .getLocalName(), m_leafSchemaNode.getQName().getNamespace().toString());
            } catch (GetAttributeException e) {
                LOGGER.error("Error when geting value from ModelNodes", e);
            }
        }

    }


    private void validateLeafRefValue(ModelNode modelNode) throws ValidationException {
        ConfigAttributeHelper leafrefHelper = m_modelNodeHelperRegistry.getConfigAttributeHelper(modelNode
                .getModelNodeSchemaPath(), m_leafSchemaNode.getQName());
        if (leafrefHelper != null) {
            try {
                ConfigLeafAttribute valueLeafref = leafrefHelper.getValue(modelNode);
                if (valueLeafref == null)
                    return;
                LeafrefTypeDefinition type = (LeafrefTypeDefinition) m_leafSchemaNode.getType();
                String xpathStr = type.getPathStatement().toString();

                boolean isValid = m_expValidator.validateXPathInModelNode(xpathStr, modelNode, valueLeafref
                                .getStringValue(),
                        modelNode, m_leafSchemaNode);
                if (!isValid) {
                    ModelNodeId modelNodeId = buildModelNodeId(modelNode, m_leafSchemaNode.getQName().getLocalName(),
                            m_leafSchemaNode.getQName().getNamespace().toString());

                    throw DataStoreValidationErrors.getMissingDataException(String.format("Instance required with " +
                                    "value - %s", valueLeafref.getStringValue()),
                            modelNodeId.xPathString(modelNode.getSchemaRegistry()), modelNodeId.xPathStringNsByPrefix
                                    (modelNode.getSchemaRegistry()));
                }
            } catch (GetAttributeException e) {
                LOGGER.error("Error when geting value from ModelNodes ", e);
            }
        }
    }

    private void validateOperation(Element dataNode) throws ValidationException {
        String operationAttribute = dataNode.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources
                .OPERATION);
        if (EditConfigOperations.DELETE.equals(operationAttribute)) {
            if (m_leafSchemaNode.getConstraints().isMandatory()) {
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                        "Can not delete the mandatory attribute " + dataNode.getNodeName());
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
     * @see DataStoreConstraintValidator#validate(ModelNode)
     */
    @Override
    public void validate(ModelNode modelNode) throws ValidationException {
        validateLeafValue(modelNode);
        if (m_leafSchemaNode.getType() instanceof LeafrefTypeDefinition) {
            validateLeafRefValue(modelNode);
        } else if (m_leafSchemaNode.getType() instanceof InstanceIdentifierTypeDefinition) {
            validateInstanceIdentifierLeafValue(modelNode);
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
    public void validate(Element dataNode, RequestType requestType) throws ValidationException {
        validateOperation(dataNode);
        super.validate(dataNode, requestType);
    }

    @Override
    protected AdvancedLogger getLogger() {
        return LOGGER;
    }

    @Override
    protected boolean isDataAvailable(ModelNode modelNode) {
        ConfigLeafAttribute data = getAttributeValueFromParentNode(modelNode);
        return (data != null && data.getStringValue() != null);
    }


}
