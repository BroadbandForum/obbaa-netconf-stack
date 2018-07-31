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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;

public class AnyXmlDataStoreConstraintValidator implements DataStoreConstraintValidator {

    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final AnyXmlSchemaNode m_anyXmlSchemaNode;


    public AnyXmlDataStoreConstraintValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry
            modelNodeHelperRegistry,
                                              AnyXmlSchemaNode anyXmlSchemaNode) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_anyXmlSchemaNode = anyXmlSchemaNode;
    }

    @Override
    public void validate(ModelNode modelNode) throws ValidationException {

		/*QName anyXmlQName = m_anyXmlSchemaNode.getQName();
		String childName = m_anyXmlSchemaNode.getQName().getLocalName();
		SchemaPath schemaPath = m_anyXmlSchemaNode.getPath().getParent();
		if (schemaPath != null) {
			ChoiceSchemaNode choiceSchemaNode = ChoiceCaseNodeUtil.getChoiceSchemaNodeFromCaseNodeSchemaPath
			(m_schemaRegistry, schemaPath);;
			if (choiceSchemaNode != null) {
				if (choiceSchemaNode.getConstraints().getWhenCondition() != null) {
					AnyXmlModelNode anyXmlModelNode = ((YangModelNode) modelNode).getAnyXmlModelNode(anyXmlQName);
					if (anyXmlModelNode != null) {
						DataStoreValidationUtil.validateWhenConstraint(choiceSchemaNode, modelNode, m_schemaRegistry,
						m_modelNodeHelperRegistry, childName, false, true);
					}
				}
				DataSchemaNode choiceCaseNode = m_schemaRegistry.getDataSchemaNode(schemaPath);
				if (choiceCaseNode != null && choiceCaseNode.getConstraints().getWhenCondition() != null) {
					AnyXmlModelNode anyXmlModelNode = ((YangModelNode) modelNode).getAnyXmlModelNode(anyXmlQName);
					if (anyXmlModelNode != null) {
						DataStoreValidationUtil.validateWhenConstraint(choiceCaseNode, modelNode, m_schemaRegistry,
						m_modelNodeHelperRegistry, childName, false, true);
					}
				}
			}
		}
		
		ConstraintDefinition definition = m_anyXmlSchemaNode.getConstraints();
		if (definition.getWhenCondition() != null || definition.getMustConstraints().size() > 0) {
			AnyXmlModelNode anyXmlModelNode = ((YangModelNode) modelNode).getAnyXmlModelNode(anyXmlQName);
			if (anyXmlModelNode != null) {
				DataStoreValidationUtil.validateWhenConstraint(m_anyXmlSchemaNode, anyXmlModelNode, m_schemaRegistry,
				m_modelNodeHelperRegistry, childName, false, false);
				// validate must-constraint
				DataStoreValidationUtil.validateMustConstraint(m_anyXmlSchemaNode, anyXmlModelNode, m_schemaRegistry, m_modelNodeHelperRegistry, false);
			}
		}*/
    }

}
