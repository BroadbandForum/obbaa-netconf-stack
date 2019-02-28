package org.broadband_forum.obbaa.netconf.mn.fwk.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.AbstractSchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin.TypeValidatorFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreConstraintValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.w3c.dom.Element;

/**
 * Single validation class, which performs validation of a LeafList 
 * at different phases
 * 
 * validate(Element) -> phase 1 validation
 * validate(ModelNode) -> phase 2 validation
 * 
 */

public class LeafListValidator extends AbstractSchemaNodeConstraintParser implements DataStoreConstraintValidator, SchemaNodeConstraintParser {
	private final SchemaRegistry m_schemaRegistry;
	private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	private final LeafListSchemaNode m_leafListSchemaNode;
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(LeafListValidator.class, LogAppNames.NETCONF_STACK);

    
    private void validateInsertAttributes(Element dataNode) throws ValidationException {
    	String insert = getInsertAttributes(dataNode, NetconfResources.INSERT);
    	String value = getInsertAttributes(dataNode, NetconfResources.VALUE);

    	if (InsertOperation.AFTER.equals(insert) || InsertOperation.BEFORE.equals(insert)) { //must have a 'value' attribute
    		if (m_leafListSchemaNode.isUserOrdered()) { //if ordered-by user , need to valid to value attribute is valid type
    			if (value == null) {
    				throw new ValidationException(getBadInsertAttributesError(NetconfResources.VALUE,
    						"value attribute can't be null or empty"));
    			}
    			// Validation the value by the type of list 
    			TypeValidator typeTypeValidator = TypeValidatorFactory.getInstance().getValidator(m_typeDefinition, m_schemaRegistry);
    			if(typeTypeValidator != null) {
    				try {
    					typeTypeValidator.validate(dataNode, true, value);
    				} catch (ValidationException e) {
    					NetconfRpcError rpcError = e.getRpcError();
    					DataSchemaNode parentSchemaNode = m_schemaRegistry.getNonChoiceParent(m_schemaNode.getPath());
    					Pair<String, Map<String, String>> errorPathPair = (parentSchemaNode == null) ? new Pair<String, Map<String, String>>("/", Collections.EMPTY_MAP) : SchemaRegistryUtil.getErrorPath(dataNode.getParentNode(), parentSchemaNode,
    							m_schemaRegistry, dataNode);

    					rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.BAD_ATTRIBUTE, e.getMessage());
    					rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
    					rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadAttribute, NetconfResources.VALUE);
    					throw new ValidationException(rpcError);
    				}
    			}
    		} else { // if ordered-by system, the attributes "before" and "after" can't present
    			// Throw an "unknown-attribute" tag in the rpc-error
    			throw new ValidationException(getUnknownAttributesError(String.format("There is an unknown-attribute '%s' in element '%s'",
    					insert, dataNode.getLocalName())));

    		}
    	}
    }
    
    private static String getInsertAttributes(Element element,String localName) {
		String attribute = element.getAttributeNS(NetconfResources.NETCONF_YANG_1, localName);
		if (attribute == null || attribute.isEmpty()) {
			return null;
		}
		return attribute;
    }
	
	private NetconfRpcError getBadInsertAttributesError(String badAttributeName, String errorMessage) {
		NetconfRpcError rpcError = NetconfRpcError.getBadAttributeError(badAttributeName,
				NetconfRpcErrorType.Application, errorMessage);
		rpcError.setErrorAppTag("missing-instance");
		return rpcError;
	}
	
	private NetconfRpcError getUnknownAttributesError(String errorMessage) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.UNKNOWN_ATTRIBUTE, 
				 errorMessage);
		return rpcError;
	}
	
    private void validateSizeRange(ModelNode modelNode) throws ValidationException {
        Optional<ElementCountConstraint> optElementCountConstraint = m_leafListSchemaNode.getElementCountConstraint();
        if (optElementCountConstraint.isPresent()) {
            ElementCountConstraint elementCountConstraint = optElementCountConstraint.get();
            if (elementCountConstraint.getMinElements() != null || elementCountConstraint.getMaxElements() != null) {
                Collection<ConfigLeafAttribute> leaflistValues = getChildLeafListFromParentNode(modelNode);
                validateSizeRange(elementCountConstraint, modelNode, leaflistValues.size(),
                        m_leafListSchemaNode.getQName().getLocalName(), m_leafListSchemaNode.getQName().getNamespace().toString());
            }
        }
	}

	private void validateInstanceIdentifierLeaflistValues(ModelNode modelNode) throws ValidationException {
		if (m_leafListSchemaNode.getType() instanceof InstanceIdentifierTypeDefinition) {
			boolean isRequired = ((InstanceIdentifierTypeDefinition)m_leafListSchemaNode.getType()).requireInstance();
			Collection<ConfigLeafAttribute> leaflistValues = getChildLeafListFromParentNode(modelNode);
			for (ConfigLeafAttribute leaflistValue : leaflistValues) {
				m_expValidator.validateInstanceIdentifierElement(modelNode, leaflistValue, isRequired,
						m_leafListSchemaNode
						.getQName().getLocalName(), m_leafListSchemaNode.getQName().getNamespace().toString());
				
			}
		}
	}
	
	private Collection<ConfigLeafAttribute> getChildLeafListFromParentNode(ModelNode parentNode) {
		Collection<ConfigLeafAttribute> leaflistValues = new ArrayList<>();
		ChildLeafListHelper configLeafListHelper = parentNode.getMountModelNodeHelperRegistry().getConfigLeafListHelper(parentNode.getModelNodeSchemaPath(), m_leafListSchemaNode.getQName());
		if (configLeafListHelper != null) {
			try {
				leaflistValues =  configLeafListHelper.getValue(parentNode);
			} catch (GetAttributeException e) {
				LOGGER.error("Error when geting child values ChildLeafListHelper.getValue(ModelNode)", e);
			}
		}
		
		return leaflistValues;
	}

	
	public LeafListValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, LeafListSchemaNode leafListSchemaNode,
	        DSExpressionValidator expValidator){
		super(leafListSchemaNode.getType(), leafListSchemaNode, schemaRegistry, expValidator);
		m_schemaRegistry = schemaRegistry;
		m_modelNodeHelperRegistry = modelNodeHelperRegistry;
		m_leafListSchemaNode = leafListSchemaNode;
	}
	
	@Override
	public DataSchemaNode getDataSchemaNode() {
        return m_leafListSchemaNode;
	}

	/* (non-Javadoc)
	 * @see AbstractSchemaNodeConstraintParser#validate(org.w3c.dom.Element)
	 */
	@Override
	public void validate(Element dataNode, RequestType requestType) throws ValidationException {
    	validateInsertAttributes(dataNode);
        super.validate(dataNode, requestType);
	}

	/* (non-Javadoc)
	 * @see DataStoreConstraintValidator#validate(org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode)
	 */
	@Override
	public void validate(ModelNode modelNode) throws ValidationException {
	    validateChoiceCase(modelNode, m_leafListSchemaNode);
        Collection<ConfigLeafAttribute> leaflistValues = getChildLeafListFromParentNode(modelNode);
		
		if (!leaflistValues.isEmpty()) {
		    for (ConfigLeafAttribute attribute:leaflistValues) {
		        validateType(attribute, modelNode);
		    }
		}
		
		validateInstanceIdentifierLeaflistValues(modelNode);
		validateSizeRange(modelNode);
	}

    @Override
    protected AdvancedLogger getLogger() {
        return LOGGER;
    }

    @Override
    protected boolean isDataAvailable(ModelNode modelNode) {
        Collection<ConfigLeafAttribute> leaflistValues = getChildLeafListFromParentNode(modelNode);
        return leaflistValues != null && leaflistValues.size() > 0;
    }

}
