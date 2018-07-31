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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;

/**
 * Fetches the augment node and validates for any condition that may be present on the augmenation
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
            DataSchemaNode parentSchemaNode = m_schemaRegistry.getDataSchemaNode(parentNode.getModelNodeSchemaPath());
            AugmentationSchema augment = DataStoreValidationUtil.getAugmentationSchema(parentSchemaNode, child);
            if (augment != null) {
                RevisionAwareXPath xpath = augment.getWhenCondition();
                if (xpath != null) {
                    try {
                        // put augment child node in DSValidationContext
                        putChildNodeInCache(child);
                        // we need to validate the condition on the parent node.No requirement for childNode to be in
                        // our scene.
                        returnValue = m_expressionValidator.validateWhen(JXPathUtils.getExpression(xpath.toString()),
                                null, parentNode);
                    } finally {
                        // remove cached child node in DSValidationContext
                        putChildNodeInCache(null);
                    }
                }
            }
        }
        return returnValue;
    }

    private void putChildNodeInCache(DataSchemaNode augmentChild) {
        DataStoreValidationUtil.getValidationContext().setAugmentChildNode(augmentChild);
    }

}
