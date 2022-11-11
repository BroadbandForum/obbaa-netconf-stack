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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service;

import java.util.Optional;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaContextUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * Fetches the augment/uses node and validates for any condition that may be present on the augmentation/uses 
 *
 */
public class DSAugmentOrUsesValidation extends CachedConstraintValidator implements DSValidation {

	private final DSExpressionValidator m_expressionValidator;
	private final SchemaRegistry m_schemaRegistry;

	public DSAugmentOrUsesValidation(DSExpressionValidator expValidator, SchemaRegistry schemaRegistry) {
		super("DSAugmentOrUsesValidation#validationNeeded");
		m_expressionValidator = expValidator;
		m_schemaRegistry = schemaRegistry;
	}

	@Override
	public ValidationResult evaluateInternal(ModelNode parentNode, DataSchemaNode child, DSValidationContext validationContext) {
		boolean validationWasNeeded = false;
		boolean validationResult = true;
		SchemaRegistry schemaRegistry = m_schemaRegistry;
		if ( parentNode != null){
			schemaRegistry = parentNode.getSchemaRegistry();
		}
		if(child.isAddedByUses()){
			DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(child.getPath().getParent());
			Pair<UsesNode,SchemaPath> usesNodeAndItsResidingNode = SchemaContextUtil.getUsesSchema(schemaRegistry, parentSchemaNode, child);
			UsesNode usesNode = usesNodeAndItsResidingNode == null? null : usesNodeAndItsResidingNode.getFirst();
			if (usesNode != null) {
				Optional<RevisionAwareXPath> xpath = usesNode.getWhenCondition();
				if (xpath.isPresent()) {
					validationWasNeeded = true;
					ModelNode usesParentContext = getActualAugmentUsesParentContext(parentNode, usesNodeAndItsResidingNode.getSecond());
					try {
						// put augment child node in DSValidationContext
						putChildNodeInCache(child, parentNode, validationContext);
						// we need to validate the condition on the parent node.No requirement for childNode to be in our scene.
						validationResult = m_expressionValidator.validateWhen(JXPathUtils.getExpression(xpath.get().getOriginalString()),
								null, usesParentContext, validationContext.getImpactValidation(), validationContext);
					} finally {
						// remove cached child node in DSValidationContext
						validationContext.setAugmentOrUsesChildNode(null);
					}
				}
			} 
		}
		if (child.isAugmenting()) {
			DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(child.getPath().getParent());
			Pair<AugmentationSchemaNode,SchemaPath> augmentNodeAndItsResidingNode = DataStoreValidationUtil.getAugmentationSchema(schemaRegistry, parentSchemaNode, child);
			AugmentationSchemaNode augSchema = augmentNodeAndItsResidingNode == null? null : augmentNodeAndItsResidingNode.getFirst();
			if (augSchema != null) {
				Optional<RevisionAwareXPath> xpath = augSchema.getWhenCondition();
				if (xpath.isPresent()) {
					validationWasNeeded = true;
					ModelNode augmentParentContext = getActualAugmentUsesParentContext(parentNode, augmentNodeAndItsResidingNode.getSecond());
					try {
						// put augment child node in DSValidationContext
						putChildNodeInCache(child, parentNode, validationContext);
						validationContext.setAugmentationSchemaNodeForWhen(augSchema);
						// we need to validate the condition on the parent node.No requirement for childNode to be in our scene.
						validationResult = m_expressionValidator.validateWhen(JXPathUtils.getExpression(xpath.get().getOriginalString()),
								null, augmentParentContext, validationContext.getImpactValidation(), validationContext);
					} finally {
						// remove cached child node in DSValidationContext
						validationContext.setAugmentOrUsesChildNode(null);
						validationContext.setAugmentationSchemaNodeForWhen(null);
					}
				}
			}
		}
		return new ValidationResult(validationWasNeeded, validationResult);
	}

	private ModelNode getActualAugmentUsesParentContext(ModelNode node, SchemaPath augmentResidingPath){
		if(node!= null && node.getModelNodeSchemaPath().equals(augmentResidingPath)){
			return node;
		} else {
			ModelNode parentNode = node.getParent();
			return getActualAugmentUsesParentContext(parentNode, augmentResidingPath);
		}
	}

	private void putChildNodeInCache(DataSchemaNode augmentChild, ModelNode parentModelNode, DSValidationContext validationContext){
		Pair<DataSchemaNode, ModelNode> pair = new Pair<DataSchemaNode, ModelNode>(augmentChild, parentModelNode);
		validationContext.setAugmentOrUsesChildNode(pair);
	}

}
