package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service;

import java.util.Optional;

import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationMountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;

/**
 * Fetches the augment node and validates for any condition that may be present on the augmenation 
 *
 */
public class DSAugmentValidation implements DSValidation {
    
    private final DSExpressionValidator m_expressionValidator;
    private final SchemaRegistry m_schemaRegistry;
    
    public DSAugmentValidation(DSExpressionValidator expValidator, SchemaRegistry schemaRegistry) {
        m_expressionValidator = expValidator;
        m_schemaRegistry = schemaRegistry;
    }
    
    @Override
    public Boolean evaluate(ModelNode parentNode, DataSchemaNode child) {
        boolean returnValue = true;
        if (child.isAugmenting()) {
        	DSValidationMountContext mountContext = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, parentNode.getModelNodeSchemaPath());
            DataSchemaNode parentSchemaNode = mountContext.getDataSchemaNode();
            AugmentationSchemaNode augment = DataStoreValidationUtil.getAugmentationSchema(parentSchemaNode, child);
			if (augment != null) {
				Optional<RevisionAwareXPath> xpath = augment.getWhenCondition();
				if (xpath.isPresent()) {
					try {
						// put augment child node in DSValidationContext
						putChildNodeInCache(child);
						// we need to validate the condition on the parent node.No requirement for childNode to be in our scene.
						returnValue = m_expressionValidator.validateWhen(JXPathUtils.getExpression(xpath.get().toString()), null, parentNode, DataStoreValidationUtil.getValidationContext().getImpactValidation());
					} finally {
						// remove cached child node in DSValidationContext
						putChildNodeInCache(null);
					}
				}
			}
        }
        return returnValue;
    }
    
    private void putChildNodeInCache(DataSchemaNode augmentChild){
    	DataStoreValidationUtil.getValidationContext().setAugmentChildNode(augmentChild);
    }

}
