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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .NC_DS_VALIDATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ChoiceEffectiveStatementImpl;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Utility class helps in validating mandatory nodes of a choice/case
 */
public class ChoiceMandatoryChildrenValidator {

    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final DataStoreValidator m_dataStoreValidator;

    private final static AdvancedLogger LOGGER = LoggerFactory.getLogger(ChoiceMandatoryChildrenValidator.class,
            NC_DS_VALIDATION, "DEBUG", "GLOBAL");

    public ChoiceMandatoryChildrenValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry
            modelNodeHelperRegistry,
                                            DataStoreValidator dataStoreValidator) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        this.m_dataStoreValidator = dataStoreValidator;
    }

    private void throwValidationException(ModelNode parentNode, List<QName> qnames) {
        ValidationException exception = null;

        for (QName qname : qnames) {
            ModelNodeId modelNodeId = new ModelNodeId(parentNode.getModelNodeId());
            modelNodeId.addRdn(ModelNodeRdn.CONTAINER, qname.getNamespace().toString(), qname.getLocalName());
            String errorPath = modelNodeId.xPathString(m_schemaRegistry);
            if (exception == null) {
                exception = DataStoreValidationErrors.getMissingDataException(DataStoreValidationUtil
                        .MISSING_MANDATORY_NODE, errorPath, modelNodeId.xPathStringNsByPrefix(m_schemaRegistry));
            } else {
                NetconfRpcError error = DataStoreValidationErrors.getDataMissingRpcError(DataStoreValidationUtil
                                .MISSING_MANDATORY_NODE,
                        errorPath, modelNodeId.xPathStringNsByPrefix(m_schemaRegistry));
                exception.addNetconfRpcError(error);
            }

        }
        throw exception;
    }

    private Map<ChoiceCaseNode, Collection<DataSchemaNode>> getMandatoryChoiceChildren(ModelNode parentNode,
                                                                                       ChoiceEffectiveStatementImpl
                                                                                               choiceNode) {
        Map<ChoiceCaseNode, Collection<DataSchemaNode>> caseNodes = new HashMap<ChoiceCaseNode,
                Collection<DataSchemaNode>>();

        Collection<ChoiceCaseNode> cases = choiceNode.getCases();
        for (DataSchemaNode caseNode : cases) {
            ConstraintDefinition caseConstraint = caseNode.getConstraints();
            boolean fetchCases = true;
            if (caseConstraint != null) {
                try {
                    // check for any constraint violation of the case.
                    m_dataStoreValidator.validateChild(parentNode, caseNode);
                } catch (ValidationException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Case constraint failed. No need to validate for its existance for ModelNode {} " +
                                        "child {}",
                                parentNode.getModelNodeId(), caseNode, e);
                    }
                    fetchCases = false;
                }
            }
            if (fetchCases) {
                // if all good, fetch list of case vs collection(caseChild)
                caseNodes.put((ChoiceCaseNode) caseNode, getMandatoryCaseChildren((ChoiceCaseNode) caseNode));
            }
        }
        return caseNodes;
    }

    private Collection<DataSchemaNode> getNonMandatoryCaseChildren(ChoiceCaseNode caseNode) {
        Set<DataSchemaNode> childNodes = new LinkedHashSet<DataSchemaNode>();
        Collection<DataSchemaNode> children = caseNode.getChildNodes();
        for (DataSchemaNode child : children) {
            if (!child.isConfiguration()) {
                // not validating for state presence
                continue;
            }
            ConstraintDefinition constraint = child.getConstraints();
            boolean nonMandatory = true;
            if (constraint != null) {
                if (constraint.isMandatory()) {
                    nonMandatory = false;
                } else if (constraint.getMinElements() != null && constraint.getMinElements() > 0) {
                    nonMandatory = false;
                }
            }
            boolean addChild = (child instanceof LeafSchemaNode) || (child instanceof LeafListSchemaNode)
                    || (child instanceof ListSchemaNode);
            if (addChild && nonMandatory) {
                childNodes.add(child);
            } else if (child instanceof ContainerSchemaNode) {
                // we dont add containerSchemaNode as part of non-mandatory case validation.
                // this is because if a container is present, it indicates that case child node is present
                // whether by NBI request or due to presence of mandatory nodes inside the container
                // this will be validated as part of mandatory case children validation
            } else if (child instanceof ChoiceEffectiveStatementImpl && !constraint.isMandatory()) {
                // if it is a choiceCase, lets add it. It could well be the only child to this case and we will
                // lazy load and evaluate it in next cycle
                childNodes.add(child);
            }
        }
        return childNodes;
    }

    private Collection<DataSchemaNode> getMandatoryCaseChildren(ChoiceCaseNode caseNode) {
        Set<DataSchemaNode> childNodes = new LinkedHashSet<DataSchemaNode>();
        Collection<DataSchemaNode> children = caseNode.getChildNodes();
        for (DataSchemaNode child : children) {
            if (!child.isConfiguration()) {
                // not validating for state presence
                continue;
            }
            ConstraintDefinition constraint = child.getConstraints();
            boolean mandatory = false;
            if (constraint != null) {
                if (constraint.isMandatory()) {
                    mandatory = true;
                } else if (constraint.getMinElements() != null && constraint.getMinElements() > 0) {
                    mandatory = true;
                }
            }
            boolean addChild = (child instanceof LeafSchemaNode) || (child instanceof LeafListSchemaNode)
                    || (child instanceof ListSchemaNode);
            if (addChild && mandatory) {
                childNodes.add(child);
            } else if (child instanceof ContainerSchemaNode) {
                childNodes.add(child);
            } else if (child instanceof ChoiceEffectiveStatementImpl && constraint.isMandatory()) {
                // if it is a choiceCase, lets add it. It could well be the only child to this case and we will
                // lazy load and evaluate it in next cycle
                childNodes.add(child);
            }
        }
        return childNodes;
    }

    @SuppressWarnings("rawtypes")
    private boolean validateMandatoryCaseNodes(ModelNodeWithAttributes modelNode, ChoiceCaseNode caseNode,
                                               Collection<DataSchemaNode> caseChildNodes) throws ModelNodeGetException {
        boolean thisCaseIsPresent = false;
        int nodesFound = 0; // How many nodes were found in this ModelNode of this set
        int totalValidNodes = 0; // total number of valid nodes in this case -> leaf/list/leafList/Container/Choice
        // with mandatory constraint
        List<ValidationException> mandatoryChoiceMissing = new ArrayList<ValidationException>();
        List<QName> notAvailable = new LinkedList<QName>();
        for (DataSchemaNode childNode : caseChildNodes) {

            QName childQName = childNode.getQName();
            ConstraintDefinition constraint = childNode.getConstraints();
            int minElements = (constraint.getMinElements() == null) ? 0 : constraint.getMinElements();
            if (childNode instanceof LeafSchemaNode) {
                totalValidNodes++;
                if (modelNode.getAttribute(childQName) != null) {
                    nodesFound++;
                } else {
                    notAvailable.add(childQName);
                }
            } else if (childNode instanceof LeafListSchemaNode) {
                totalValidNodes++;
                Set leafLists = modelNode.getLeafList(childQName);
                if (leafLists != null && leafLists.size() >= minElements) {
                    nodesFound++;
                } else {
                    notAvailable.add(childQName);
                }
            } else if (childNode instanceof ListSchemaNode) {
                totalValidNodes++;

                Collection modelNodes = DataStoreValidationUtil.getChildListModelNodes(modelNode, childNode,
                        m_modelNodeHelperRegistry);
                if (modelNodes != null && modelNodes.size() >= minElements) {
                    nodesFound++;

                } else {
                    notAvailable.add(childQName);
                }


            } else if (childNode instanceof ContainerSchemaNode) {
                totalValidNodes++;
                ModelNode childContainer = DataStoreValidationUtil.getChildContainerModelNode(modelNode, childNode,
                        m_modelNodeHelperRegistry);
                if (childContainer != null) {
                    // A container node that is already here, means it was validated when it was created
                    // as part of internal or NBI request for mandatory nodes. No need to validate it further.
                    nodesFound++;
                }
            } else if (childNode instanceof ChoiceEffectiveStatementImpl) {
                totalValidNodes++;
                try {
                    boolean choicePresent = validateMandatoryChoiceChildren(modelNode, (ChoiceEffectiveStatementImpl)
                            childNode);
                    if (choicePresent) {
                        nodesFound++;
                    }
                } catch (ValidationException e) {
                    // This seems to be a mandatory Choice and it is missing. Record the exception for now. Throw it,
                    // if this case has other
                    // nodes present.
                    mandatoryChoiceMissing.add(e);
                }
            }
        }

        if (nodesFound != 0 && nodesFound != totalValidNodes) {
            // if the totalNodesFound and nodesFound is not equal and nodesFound is more than one,
            // indicates one of the mandatory node of this set is missed
            throwValidationException(modelNode, notAvailable);
        } else if (nodesFound != 0 && nodesFound == totalValidNodes) {
            // indicates all mandatory nodes found and this case of the parent choice is present in the modelNode
            thisCaseIsPresent = true;
        } else if (!thisCaseIsPresent) {
            // check if there are non-mandatory nodes present
            Collection<DataSchemaNode> caseChildren = getNonMandatoryCaseChildren(caseNode);
            if (!caseChildren.isEmpty()) {
                thisCaseIsPresent = thisCaseIsPresent || checkForCasePresence(modelNode, caseChildren);
            }

            if (thisCaseIsPresent && totalValidNodes > 0) {
                // indicates mandatory nodes are totally missing in this choice
                throwValidationException(modelNode, notAvailable);
            } else if (thisCaseIsPresent) {
                // some nodes are present. Check for any defaults with when constraint
                checkForCreateOrDeleteDefault(modelNode, caseChildren);
            }
        }
        return thisCaseIsPresent;
    }

    private boolean checkForCasePresence(ModelNodeWithAttributes modelNode, Collection<DataSchemaNode> caseChildNodes)
            throws ModelNodeGetException {
        /**
         * We come here, only if we want to check if there are any non-mandatory children of this case available and
         * all mandatory child of
         * this case is missing
         */
        boolean thisCaseIsPresent = false;
        for (DataSchemaNode child : caseChildNodes) {
            QName qname = child.getQName();
            if (child instanceof LeafSchemaNode && modelNode.getAttribute(qname) != null) {
                thisCaseIsPresent = true;
                break;
            } else if (child instanceof LeafListSchemaNode) {
                if (modelNode.getLeafList(qname) != null && modelNode.getLeafList(qname).size() > 0) {
                    thisCaseIsPresent = true;
                    break;
                }
            } else if (child instanceof ListSchemaNode) {
                Collection<ModelNode> modelNodes = DataStoreValidationUtil.getChildListModelNodes(modelNode, child,
                        m_modelNodeHelperRegistry);
                if (modelNodes != null && modelNodes.size() > 1) {
                    thisCaseIsPresent = true;
                    break;
                }
            } else if (child instanceof ContainerSchemaNode) {
                ModelNode childContainer = DataStoreValidationUtil.getChildContainerModelNode(modelNode, child,
                        m_modelNodeHelperRegistry);
                if (childContainer != null) {
                    thisCaseIsPresent = true;
                    break;
                }
            } else if (child instanceof ChoiceEffectiveStatementImpl) {
                thisCaseIsPresent = validateMandatoryChoiceChildren(modelNode, (ChoiceEffectiveStatementImpl) child);
            }
        }
        return thisCaseIsPresent;

    }

    @SuppressWarnings("unchecked")
    private void checkForCreateOrDeleteDefault(ModelNode node, Collection<DataSchemaNode> childNodes) {
        for (DataSchemaNode childNode : childNodes) {
            Collection<SchemaPath> schemaPathsToDelete = DataStoreValidationUtil.getValidationContext()
                    .getSchemaPathsToDelete();
            schemaPathsToDelete.add(childNode.getPath());
            if (childNode instanceof LeafSchemaNode && ((LeafSchemaNode) childNode).getDefault() != null) {
                m_dataStoreValidator.validateChild(node, childNode);
            }
        }
    }

    /**
     * Given a modelNode and a choice in the modelNode, it validates if all necessary mandatory conditions are satisfied
     *
     * @param modelNode
     * @param choice
     * @throws ModelNodeGetException
     */
    public boolean validateMandatoryChoiceChildren(ModelNodeWithAttributes modelNode, ChoiceEffectiveStatementImpl
            choice)
            throws ModelNodeGetException {
        ConstraintDefinition choiceConstraints = choice.getConstraints();
        boolean isValid = true;
        if (!choice.isConfiguration()) {
            return true;
        }
        try {
            if (choiceConstraints != null) {
                // if the choice has a constraint, validate it, if it is all good then look for mandatory nodes
                isValid = m_dataStoreValidator.validateChild(modelNode, choice);
            }
        } catch (ValidationException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("constraint validation resulted in error. So this choice {} in modelNode {} cannot " +
                                "exists", choice,
                        modelNode.getModelNodeId(), e);
            }
            isValid = false;
        }

        boolean anyChoicePresent = false;
        if (isValid) {
            Map<ChoiceCaseNode, Collection<DataSchemaNode>> childNodes = getMandatoryChoiceChildren(modelNode, choice);
            for (Map.Entry<ChoiceCaseNode, Collection<DataSchemaNode>> entry : childNodes.entrySet()) {
                anyChoicePresent = anyChoicePresent || validateMandatoryCaseNodes(modelNode, entry.getKey(), entry
                        .getValue());
            }
            if (isValid && choiceConstraints.isMandatory() && !anyChoicePresent) {
                DataStoreValidationErrors.throwDataMissingException(m_schemaRegistry, modelNode, choice.getQName());
            }
        }
        return anyChoicePresent;
    }
}
