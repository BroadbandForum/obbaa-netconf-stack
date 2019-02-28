package org.broadband_forum.obbaa.netconf.mn.fwk.validation;

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.MandatoryTypeConstraintParser.checkMandatoryElementExists;

import java.util.Collection;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.AbstractSchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreConstraintValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

/**
 * Single validation class, which performs validation of a container 
 * at different phases
 * 
 * validate(Element) -> phase 1 validation
 * validate(ModelNode) -> phase 2 validation
 * 
 */
public class ContainerValidator extends AbstractSchemaNodeConstraintParser implements DataStoreConstraintValidator {
	private final SchemaRegistry m_schemaRegistry;
	private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	private final ContainerSchemaNode m_containerSchemaNode;
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ContainerValidator.class, LogAppNames.NETCONF_STACK);


	private ModelNode getChildModelNodeFromParentNode(ModelNode parentNode) {
		ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper(parentNode.getModelNodeSchemaPath(), m_containerSchemaNode.getQName());
		if (childContainerHelper != null) {
			try {
				return childContainerHelper.getValue(parentNode);
			} catch ( ModelNodeGetException e) {
				LOGGER.error("Error when getting child ModelNodes ChildContainerHelper.getValue(ModelNode)", e);
			}
		}
		
		return null;
	}
	
	public ContainerValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
			ContainerSchemaNode containerSchemaNode, DSExpressionValidator expValidator){
		super(null, containerSchemaNode, schemaRegistry, expValidator);
		m_schemaRegistry = schemaRegistry;
		m_modelNodeHelperRegistry = modelNodeHelperRegistry;
		m_containerSchemaNode = containerSchemaNode;
		
	}

	/* (non-Javadoc)
	 * @see DataStoreConstraintValidator#validate(org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode)
	 */
	@SuppressWarnings("unchecked")
    @Override
	public void validate(ModelNode modelNode) throws ValidationException {
	    validateChoiceCase(modelNode, m_containerSchemaNode);
	}

	@Override
	public DataSchemaNode getDataSchemaNode() {
        return m_containerSchemaNode;
	}

	/* (non-Javadoc)
	 * @see SchemaNodeConstraintParser#validate(org.w3c.dom.Element)
	 */
	@Override
	public void validate(Element dataNode, RequestType requestType) throws ValidationException {
		validateChoiceMultiCaseElements(dataNode);
		if (DataStoreValidationUtil.needsFurtherValidation(dataNode, requestType)) {
			Collection<DataSchemaNode> childNodes = m_containerSchemaNode.getChildNodes();
			if (dataNode.getChildNodes().getLength() == 1
					&& dataNode.getFirstChild().getNodeType() == Element.TEXT_NODE) {
				// Indicates only one of the element of all the output element
				// is passed.
				// Mandatory constraint parser checks for all the required set
				// of nodes and not just one element alone.
				// So pass the parent of the current node, which is expected to
				// have all the mandatory nodes.

				// E.g. <rpc-reply>
				// 			<device-holder/> <!-- mandatory -->
				// 			<device-name/>   <!-- mandatory -->
				// 		</rpc-reply>

				// In the above example, if only device-holder is passed,
				// device-name will be found missing, which is a
				// mandatory node, so in order to validate if all mandatory
				// nodes are present, the parent node is passed
				if (dataNode.getParentNode() == null){
				    throw DataStoreValidationErrors.getMissingDataException(String.format("Parent node is null for %s",dataNode), "", Collections.emptyMap());
				}
				else if (dataNode.getParentNode().getNodeType() == Element.ELEMENT_NODE) {
					checkMandatoryElementExists(((Element) dataNode.getParentNode()), childNodes, m_containerSchemaNode,
							m_schemaRegistry);
				}
			} else {
				checkMandatoryElementExists(dataNode, childNodes, m_containerSchemaNode, m_schemaRegistry);
			}
		}
	}

    @Override
    protected AdvancedLogger getLogger() {
        return LOGGER;
    }

    @Override
    protected boolean isDataAvailable(ModelNode modelNode) {
        ModelNode childNode = getChildModelNodeFromParentNode(modelNode);
        return childNode != null; 
    }

}
