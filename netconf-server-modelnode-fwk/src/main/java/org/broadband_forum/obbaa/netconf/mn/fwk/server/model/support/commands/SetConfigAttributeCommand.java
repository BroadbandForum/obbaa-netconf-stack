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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.processing.ModelNodeConstraintProcessor;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class SetConfigAttributeCommand implements Command {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(SetConfigAttributeCommand.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");
    private ModelNode m_instance;
    private Map<QName, ConfigAttributeHelper> m_configAttributeHelpers;
    private Map<QName, ChildLeafListHelper> m_configLeafListHelpers;
    private SchemaRegistry m_schemaRegistry;
    private EditContext m_editContext;

    @Override
    public void execute() throws CommandExecutionException {
        try {
            for (EditChangeNode changeNode : m_editContext.getEditNode().getChangeNodes()) {
                QName qname = changeNode.getQName();

                ConfigAttributeHelper helper = m_configAttributeHelpers.get(qname);
                if (helper != null) {
                    // leaf choice case node
                    ModelNodeConstraintProcessor.handleChoiceCaseNode(m_schemaRegistry, m_configAttributeHelpers,
                            m_instance, qname);
                    if (!(EditConfigOperations.REMOVE.equals(changeNode.getOperation()) || EditConfigOperations
                            .DELETE.equals(changeNode
                            .getOperation()))) {
                        helper.setValue(m_instance, changeNode.getConfigLeafAttribute());
                    }
                }
            }

            for (EditChangeNode changeNode : m_editContext.getEditNode().getChangeNodes()) {
                QName qname = changeNode.getQName();

                ChildLeafListHelper helper = m_configLeafListHelpers.get(qname);
                if (helper != null) {
                    // leaf-list choice case node
                    ModelNodeConstraintProcessor.handleChoiceCaseNode(m_schemaRegistry, m_configLeafListHelpers,
                            m_instance, qname);
                }
            }

            Set<ConfigLeafAttribute> existedValues = new HashSet<>();
            for (EditChangeNode changeNode : m_editContext.getEditNode().getChangeNodes()) {
                ChildLeafListHelper helper = m_configLeafListHelpers.get(changeNode.getQName());
                if (helper != null) {
                    existedValues.addAll(helper.getValue(m_instance));
                }

                if (helper != null) {
                    String leafListOperation = changeNode.getOperation();
                    InsertOperation insertOperation = changeNode.getInsertOperation();
                    if (!(leafListOperation.equals(EditConfigOperations.DELETE) || leafListOperation.equals
                            (EditConfigOperations.REMOVE))) { // for operations except delete
                        if (leafListOperation.equals(EditConfigOperations.CREATE)) {
                            if (insertOperation == null) {
                                insertOperation = new InsertOperation(InsertOperation.LAST, null);
                            }
                            // Check existed node
                            if (isExistingLeafListNode(changeNode.getConfigLeafAttribute(), existedValues)) {
                                //throw a rfc error data-existed
                                LOGGER.debug("The value already exists in leaf-list elements: {}", changeNode
                                        .getValue());
                                throw new SetAttributeException(NetconfRpcErrorUtil.getApplicationError
                                        (NetconfRpcErrorTag.DATA_EXISTS,
                                        "Create instance attempted while the instance - " + changeNode.getQName()
                                                .getLocalName() + " = "
                                                + changeNode.getValue() + " already exists; Request Failed."));
                            }
                        }

                        // support ordered-by user statement
                        if (isLeafListOrderedByUser(changeNode.getQName())) {
                            if (insertOperation != null) {
                                ModelNodeConstraintProcessor.validateInsertRequest(changeNode, existedValues,
                                        leafListOperation, insertOperation, m_instance);
                            }
                            helper.addChildByUserOrder(m_instance, changeNode.getConfigLeafAttribute(),
                                    leafListOperation, insertOperation);
                            existedValues.add(changeNode.getConfigLeafAttribute());
                        } else { // ordered-by system
                            helper.addChild(m_instance, changeNode.getConfigLeafAttribute());
                        }
                    } else { // for the operation delete, remove
                        //Check existed index node
                        if (!isExistingLeafListNode(changeNode.getConfigLeafAttribute(), existedValues)) {
                            // throw a rfc error data-existed
                            LOGGER.debug("The delete value doesn't exist in leaf-list elements: {} . Removed from " +
                                    "editContext !", changeNode.getValue());
                            throw new SetAttributeException(NetconfRpcErrorUtil.getApplicationError
                                    (NetconfRpcErrorTag.DATA_MISSING,
                                    "The instance - " + changeNode.getQName().getLocalName() + " = "
                                            + changeNode.getValue() + " does not exist; Request Failed."));
                        }
                        helper.removeChild(m_instance, changeNode.getConfigLeafAttribute());
                    }
                }
            }
        } catch (GetAttributeException | SetAttributeException | ModelNodeDeleteException e) {
            if (e instanceof SetAttributeException) {
                if (((SetAttributeException) e).getRpcError() != null) {
                    throw new CommandExecutionException(((SetAttributeException) e).getRpcError(), e);
                }
            }
            throw new CommandExecutionException(e);
        }
    }

    private boolean isExistingLeafListNode(ConfigLeafAttribute value, Set<ConfigLeafAttribute> existedValues) throws
            GetAttributeException {
        return existedValues.contains(value);
    }

    private boolean isLeafListOrderedByUser(QName qName) throws SetAttributeException {
        boolean isOrderedByUser = false;
        LeafListSchemaNode leafListSchemaNode = getLeafListSchemaNode(qName);
        if (leafListSchemaNode != null) {
            isOrderedByUser = leafListSchemaNode.isUserOrdered();
        }
        return isOrderedByUser;
    }

    private LeafListSchemaNode getLeafListSchemaNode(QName qName) throws SetAttributeException {
        Collection<DataSchemaNode> schemaNodes = m_schemaRegistry.getChildren(m_instance.getModelNodeSchemaPath());
        LeafListSchemaNode leafListSchemaNode = null;
        for (DataSchemaNode dataSchemaNode : schemaNodes) {
            if (dataSchemaNode.getQName().equals(qName)) {
                if (dataSchemaNode instanceof LeafListSchemaNode) {
                    leafListSchemaNode = (LeafListSchemaNode) dataSchemaNode;
                    break;
                }
            }
            if (dataSchemaNode instanceof ChoiceSchemaNode) {
                Collection<ChoiceCaseNode> schemaChoiceCases = ((ChoiceSchemaNode) dataSchemaNode).getCases();
                for (ChoiceCaseNode choiceCaseNode : schemaChoiceCases) {
                    if (choiceCaseNode.getDataChildByName(qName) != null && (choiceCaseNode.getDataChildByName(qName)
                    ) instanceof LeafListSchemaNode) {
                        leafListSchemaNode = (LeafListSchemaNode) choiceCaseNode.getDataChildByName(qName);
                        break;
                    }
                }
            }
        }
        if (leafListSchemaNode == null) {
            throw new SetAttributeException(String.format("Cannot get the schema node for '%s'", qName));
        }
        return leafListSchemaNode;
    }

    public SetConfigAttributeCommand addSetInfo(SchemaRegistry schemaRegistry, Map<QName, ConfigAttributeHelper> configAttributeHelpers, Map<QName, ChildLeafListHelper> configLeafListHelpers, ModelNode instance, EditContext editContext) {
        m_schemaRegistry = schemaRegistry;
        m_configAttributeHelpers = configAttributeHelpers;
        m_configLeafListHelpers = configLeafListHelpers;
        m_instance = instance;
        m_editContext = editContext;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetConfigAttributeCommand{");
        sb.append("m_instance=").append(m_instance);
        sb.append(", m_configAttributeHelpers=").append(m_configAttributeHelpers);
        sb.append(", m_configLeafListHelpers=").append(m_configLeafListHelpers);
        sb.append(", m_schemaRegistry=").append(m_schemaRegistry);
        sb.append(", m_editContext=").append(m_editContext);
        sb.append('}');
        return sb.toString();
    }
}
