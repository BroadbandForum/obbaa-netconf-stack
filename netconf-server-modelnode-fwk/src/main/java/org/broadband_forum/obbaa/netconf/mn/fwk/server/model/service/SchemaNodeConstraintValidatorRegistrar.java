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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.ContainerValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.LeafListValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.LeafValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.ListValidator;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.WhenConditionAware;

public class SchemaNodeConstraintValidatorRegistrar implements SchemaRegistryVisitor {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SchemaRegistryTraverser.class, LogAppNames.NETCONF_STACK);

	private final SchemaRegistry m_schemaRegistry;
	private final DSExpressionValidator m_expValidator;
	private final DataStoreValidationPathBuilder m_pathBuilder;

	public SchemaNodeConstraintValidatorRegistrar(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegsitry) {
		m_schemaRegistry = schemaRegistry;
		m_expValidator = new DSExpressionValidator(schemaRegistry, modelNodeHelperRegistry, subsystemRegsitry);
		m_pathBuilder = new DataStoreValidationPathBuilder(schemaRegistry, modelNodeHelperRegistry);
	}

	private void registerConstraintNodeValidator(DataSchemaNode dataSchemaNode) {
	    LOGGER.debug("Register Constraint node validator for schema-node {} of schemapath {} in schema-registry name- {}", dataSchemaNode.getQName(), dataSchemaNode.getPath(), m_schemaRegistry.getName());
		SchemaNodeConstraintParser schemaNodeConstraintParser = m_schemaRegistry.getSchemaNodeConstraintParser(dataSchemaNode);
		if (schemaNodeConstraintParser == null) {
			if (dataSchemaNode instanceof LeafSchemaNode) {
				schemaNodeConstraintParser = new LeafValidator(m_schemaRegistry, null, (LeafSchemaNode) dataSchemaNode, m_expValidator);
			} else if (dataSchemaNode instanceof LeafListSchemaNode) {
				schemaNodeConstraintParser = new LeafListValidator(m_schemaRegistry, null, (LeafListSchemaNode) dataSchemaNode, m_expValidator);
			} else if (dataSchemaNode instanceof ContainerSchemaNode) {
				schemaNodeConstraintParser = new ContainerValidator(m_schemaRegistry, null, (ContainerSchemaNode) dataSchemaNode, m_expValidator);
			} else if (dataSchemaNode instanceof ListSchemaNode) {
				schemaNodeConstraintParser = new ListValidator(m_schemaRegistry, null, (ListSchemaNode) dataSchemaNode, m_expValidator);
			}
			if (schemaNodeConstraintParser != null) {
				m_schemaRegistry.putSchemaNodeConstraintParser(dataSchemaNode, schemaNodeConstraintParser);
			}
		}
	}

	private void registerWhenAndMustConstraintSchemaPaths(String componentId, DataSchemaNode dataSchemaNode) {
		if (m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath()) != null) {
			if (dataSchemaNode instanceof WhenConditionAware) {
				Optional<RevisionAwareXPath> optWhenCondition = dataSchemaNode.getWhenCondition();
				if (optWhenCondition != null && optWhenCondition.isPresent()) {
					String constraintXpath = optWhenCondition.get().getOriginalString();
					SchemaPath referringSP = dataSchemaNode.getPath();
					try {
						Map<SchemaPath, ArrayList<String>> constraints = m_pathBuilder.getSchemaPathsFromXPath(dataSchemaNode, constraintXpath, null);
						for (Map.Entry<SchemaPath, ArrayList<String>> constraint : constraints.entrySet()) {
							SchemaPath referredSP = constraint.getKey();
							if (constraint.getValue() != null) {
								for (String accessPath : constraint.getValue()) {
									m_schemaRegistry.registerWhenReferringNodesForAllSchemaNodes(componentId, referringSP, referredSP, accessPath);
								}
							}
						}
					} catch (Exception e) {
						LOGGER.error("Error while registering when referring Nodes in SchemaRegistry", e);
					}
				}
			}
			if (dataSchemaNode instanceof MustConstraintAware) {
				Collection<MustDefinition> mustConstraints = ((MustConstraintAware) dataSchemaNode).getMustConstraints();
				if (mustConstraints != null) {
					for (MustDefinition mustConstraint : mustConstraints) {
						String constraintXpath = mustConstraint.getXpath().getOriginalString();
						SchemaPath referringSP = dataSchemaNode.getPath();
						try {
							Map<SchemaPath, ArrayList<String>> constraints = m_pathBuilder.getSchemaPathsFromXPath(dataSchemaNode, constraintXpath, null);
							for (Map.Entry<SchemaPath, ArrayList<String>> constraint : constraints.entrySet()) {
								SchemaPath referredSP = constraint.getKey();
								if (constraint.getValue() != null) {
									for (String accessPath : constraint.getValue()) {
										m_schemaRegistry.registerMustReferringNodesForAllSchemaNodes(componentId, referringSP, referredSP, accessPath);
									}
								}
							}
						} catch (Exception e) {
							LOGGER.error("Error while registering must referring Nodes in SchemaRegistry", e);
						}
						}
					}
				}
			}
		}


	@Override
	public void visitLeafListNode(String componentId, SchemaPath parentSchemaPath, LeafListSchemaNode leafListNode) {
		registerConstraintNodeValidator(leafListNode);
		registerWhenAndMustConstraintSchemaPaths(componentId, leafListNode);
	}

	@Override
	public void visitLeafNode(String componentId, SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode) {
		registerConstraintNodeValidator(leafSchemaNode);
		registerWhenAndMustConstraintSchemaPaths(componentId, leafSchemaNode);
	}

	@Override
	public void visitListNode(String componentId, SchemaPath parentPath, ListSchemaNode listSchemaNode) {
		registerConstraintNodeValidator(listSchemaNode);
		registerWhenAndMustConstraintSchemaPaths(componentId, listSchemaNode);
	}

	@Override
	public void visitContainerNode(String componentId, SchemaPath parentSchemaPath, ContainerSchemaNode containerSchemaNode) {
		registerConstraintNodeValidator(containerSchemaNode);
		registerWhenAndMustConstraintSchemaPaths(componentId, containerSchemaNode);
	}

	@Override
	public void visitChoiceCaseNode(String componentId, SchemaPath parentPath, CaseSchemaNode choiceCaseNode) {
		for (DataSchemaNode dataSchemaNode: choiceCaseNode.getChildNodes()) {
			registerWhenAndMustConstraintSchemaPaths(componentId, dataSchemaNode);
		}
	}

	@Override
	public void visitChoiceNode(String componentId, SchemaPath parentPath, ChoiceSchemaNode choiceSchemaNode) {
		registerWhenAndMustConstraintSchemaPaths(componentId, choiceSchemaNode);
		for(CaseSchemaNode caseNode : choiceSchemaNode.getCases().values()){
			visitChoiceCaseNode(componentId, parentPath, caseNode);
		}
	}

	@Override
	public void visitAnyXmlNode(String componentId, SchemaPath parentPath, AnyXmlSchemaNode anyXmlSchemaNode) {
		registerWhenAndMustConstraintSchemaPaths(componentId, anyXmlSchemaNode);
	}

	@Override
	public void visitAnyDataNode(String componentId, SchemaPath parentPath, AnyDataSchemaNode anyDataSchemaNode) {
		registerWhenAndMustConstraintSchemaPaths(componentId, anyDataSchemaNode);
	}
	
	@Override
	public void visitIdentityNode(String componentId, IdentitySchemaNode identitySchemaNode) {

	}

	@Override
	public void visitEnter(String componentId, SchemaPath parentPath, SchemaPath schemaPath) {

	}

	@Override
	public void visitLeave(String componentId, SchemaPath parentPath, SchemaPath schemaPath) {

	}

	@Override
	public String getErrors() {
		return null;
	}
}
