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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.appendLeafChange;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttributeWithInsertOp;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.processing.ModelNodeConstraintProcessor;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;


public class SetConfigAttributeCommand implements ChangeCommand {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SetConfigAttributeCommand.class, LogAppNames.NETCONF_STACK);
    private ModelNode m_parentModelNode;
    private Map<QName, ConfigAttributeHelper> m_configAttributeHelpers;
    private Map<QName, ChildLeafListHelper> m_configLeafListHelpers;
    private SchemaRegistry m_schemaRegistry;
    private EditContext m_editContext;
    private WritableChangeTreeNode m_changeTreeNode;

    @Override
    public void execute() throws CommandExecutionException {
        try {
            EditContainmentNode editNode = m_editContext.getEditNode();
            /* For modify changeTreenodes, editconfig interception will not happen.
            Therefore having an explicit call to intercept here.*/
            m_parentModelNode.interceptEditConfig(m_editContext, m_changeTreeNode);
            for (EditChangeNode changeNode : editNode.getChangeNodes()) {
                QName qname = changeNode.getQName();

                ConfigAttributeHelper helper = m_configAttributeHelpers.get(qname);
                if (helper != null) {
                    if (!(EditConfigOperations.REMOVE.equals(changeNode.getOperation()) || EditConfigOperations.DELETE.equals(changeNode
                            .getOperation()))) {
                        helper.setValue(m_parentModelNode, changeNode.getConfigLeafAttribute());
                    }
                }
            }

            for (EditChangeNode changeNode : editNode.getChangeNodes()) {
                QName qname = changeNode.getQName();

                ChildLeafListHelper helper = m_configLeafListHelpers.get(qname);
                if (helper != null) {
                    // leaf-list choice case node
                    ModelNodeConstraintProcessor.handleChoiceCaseNode(m_schemaRegistry, m_configLeafListHelpers, m_parentModelNode, qname);
                }
            }

            for (EditChangeNode changeNode : editNode.getChangeNodes()) {
                QName qName = changeNode.getQName();
                ChildLeafListHelper helper = m_configLeafListHelpers.get(qName);
                if (helper != null) {
                    if(editNode.getEditOperation().equals(EditConfigOperations.REPLACE)) {
                        removeLeafListsThatAreNotPresentInEditNode(editNode, qName, helper);
                    }
                    String leafListOperation = changeNode.getOperation();
                    InsertOperation insertOperation = changeNode.getInsertOperation();
                    if (!(leafListOperation.equals(EditConfigOperations.DELETE) || leafListOperation.equals(EditConfigOperations.REMOVE))) { // for operations except delete
                        if (leafListOperation.equals(EditConfigOperations.CREATE)) {
                            if (insertOperation == null) {
                                insertOperation = InsertOperation.LAST_OP;
                            }
                        }

                        // support ordered-by user statement
                        if (isLeafListOrderedByUser(qName)) {
                            if (insertOperation != null) {
                                ModelNodeConstraintProcessor.validateInsertRequest(changeNode,
                                        leafListOperation, insertOperation, m_parentModelNode, m_schemaRegistry, helper);
                            }
                            helper.addChildByUserOrder(m_parentModelNode, changeNode.getConfigLeafAttribute(), leafListOperation, insertOperation);
                        } else { // ordered-by system
                            helper.addChild(m_parentModelNode, changeNode.getConfigLeafAttribute());
                        }
                    } else { // for the operation delete,
                        helper.removeChild(m_parentModelNode, changeNode.getConfigLeafAttribute());
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

    private void removeLeafListsThatAreNotPresentInEditNode(EditContainmentNode editNode, QName qName, ChildLeafListHelper helper) throws GetAttributeException, ModelNodeDeleteException {
        for(ConfigLeafAttribute leafAttribute : helper.getValue(m_parentModelNode)) {
            boolean present = false;
            for(EditChangeNode changeNode : editNode.getChangeNodes(qName)) {
                if(changeNode.getValue().equals(leafAttribute.getStringValue())) {
                    present = true;
                    break;
                }
            }
            if(!present) {
                helper.removeChild(m_parentModelNode, leafAttribute);
            }
        }
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
        Collection<DataSchemaNode> schemaNodes = m_schemaRegistry.getChildren(m_parentModelNode.getModelNodeSchemaPath());
        LeafListSchemaNode leafListSchemaNode = null;
        for (DataSchemaNode dataSchemaNode : schemaNodes) {
            if (dataSchemaNode.getQName().equals(qName)) {
                if (dataSchemaNode instanceof LeafListSchemaNode) {
                    leafListSchemaNode = (LeafListSchemaNode) dataSchemaNode;
                    break;
                }
            }
            if (dataSchemaNode instanceof ChoiceSchemaNode) {
                LeafListSchemaNode getLeafListFromNestedChoice = ChoiceCaseNodeUtil.getLeafListSchemaNodeFromNestedChoice(dataSchemaNode, qName);
                if (getLeafListFromNestedChoice != null) {
                    return getLeafListFromNestedChoice;
                }
            }
        }
        if (leafListSchemaNode == null) {
            throw new SetAttributeException(String.format("Cannot get the schema node for '%s'", qName));
        }
        return leafListSchemaNode;
    }

    public SetConfigAttributeCommand addSetInfo(SchemaRegistry schemaRegistry, Map<QName, ConfigAttributeHelper> configAttributeHelpers, Map<QName, ChildLeafListHelper> configLeafListHelpers,
                                                ModelNode parentModelNode, EditContext editContext, WritableChangeTreeNode changeTreeNode) {
        this.m_schemaRegistry = parentModelNode.getSchemaRegistry();
        m_configAttributeHelpers = configAttributeHelpers;
        m_configLeafListHelpers = configLeafListHelpers;
        m_parentModelNode = parentModelNode;
        m_editContext = editContext;
        m_changeTreeNode = changeTreeNode;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetConfigAttributeCommand{");
        sb.append("m_parentModelNode=").append(m_parentModelNode);
        sb.append(", m_configAttributeHelpers=").append(m_configAttributeHelpers);
        sb.append(", m_configLeafListHelpers=").append(m_configLeafListHelpers);
        sb.append(", m_schemaRegistry=").append(m_schemaRegistry);
        sb.append(", m_editContext=").append(m_editContext);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
        ModelNodeId parentMNID = new ModelNodeId(m_parentModelNode.getModelNodeId().getRdnsReadOnly());

        //for key changes
        for (EditMatchNode matchNode : m_editContext.getEditNode().getMatchNodes()) {
            appendLeafChange(parent, parentMNID, m_parentModelNode,
                    m_configAttributeHelpers.get(matchNode.getQName()), matchNode.getConfigLeafAttribute(),
                    null, isImplied , m_schemaRegistry);
        }

        //for leaf changes
        for (EditChangeNode changeNode : m_editContext.getEditNode().getChangeNodes()) {
            if (!(EditConfigOperations.REMOVE.equals(changeNode.getOperation()) || EditConfigOperations.DELETE.equals(changeNode.getOperation()))) {
                appendLeafChange(parent, parentMNID, m_parentModelNode, m_configAttributeHelpers.get(changeNode.getQName()), changeNode.getConfigLeafAttribute(),
                        changeNode.getChangeSource(), isImplied, m_schemaRegistry);
            }
        }

        // for leaf-list changes
        Map<ChildLeafListHelper, Set<EditChangeNode>> helperLeafListChangeNodesMap = segregateLeafListInfo();

        for (Map.Entry<ChildLeafListHelper, Set<EditChangeNode>> entry : helperLeafListChangeNodesMap.entrySet()) {
            ChildLeafListHelper helper = entry.getKey();
            LeafListSchemaNode leafListSchemaNode = helper.getLeafListSchemaNode();
            QName leafListQName = leafListSchemaNode.getQName();
            ModelNodeId leafListMNID = new ModelNodeId(parentMNID)
                    .addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, leafListQName.getNamespace().toString(), leafListQName.getLocalName()));
            WritableChangeTreeNode leafListChangeTreeNode = (WritableChangeTreeNode) parent.getChildren().get(leafListMNID);
            if (leafListChangeTreeNode == null) {
                leafListChangeTreeNode = new ChangeTreeNodeImpl(m_schemaRegistry, parent, leafListMNID, leafListSchemaNode,
                        parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());

                Set<ConfigLeafAttribute> previousLeafLists = m_parentModelNode.getLeafList(leafListQName);
                if (previousLeafLists != null && !previousLeafLists.isEmpty()) {
                    LinkedHashSet<ConfigLeafAttributeWithInsertOp> previousLeafListAttributes = new LinkedHashSet<>();
                    for (ConfigLeafAttribute leafListAttribute : previousLeafLists) {
                        previousLeafListAttributes.add(new ConfigLeafAttributeWithInsertOp(leafListAttribute));
                    }
                    leafListChangeTreeNode.setPreviousValue(previousLeafListAttributes);
                }

                LinkedHashSet<ConfigLeafAttributeWithInsertOp> currentLeafLists = getCurrentLeafLists(previousLeafLists, entry.getValue(), m_editContext.getEditNode().getEditOperation());
                if (currentLeafLists != null) {
                    leafListChangeTreeNode.setCurrentValue(currentLeafLists);
                }
                leafListChangeTreeNode.setEditChangeSource(entry.getValue().iterator().next().getChangeSource());
                leafListChangeTreeNode.setImplied(isImplied);
                if(parent.isMountPoint()) {
                    leafListChangeTreeNode.setMountPoint(true);
                }
                parent.appendChildNode(leafListChangeTreeNode);
            }
        }
    }

    private Map<ChildLeafListHelper, Set<EditChangeNode>> segregateLeafListInfo() {
        Map<ChildLeafListHelper, Set<EditChangeNode>> helperLeafListChangeNodesMap = new LinkedHashMap<>();
        for (EditChangeNode changeNode : m_editContext.getEditNode().getChangeNodes()) {
            QName qname = changeNode.getQName();
            ChildLeafListHelper helper = m_configLeafListHelpers.get(qname);
            if (helper != null) {
                Set<EditChangeNode> changeNodes = helperLeafListChangeNodesMap.get(helper);
                if (changeNodes == null) {
                    changeNodes = new LinkedHashSet<>();
                }
                changeNodes.add(changeNode);
                helperLeafListChangeNodesMap.put(helper, changeNodes);
            }
        }
        return helperLeafListChangeNodesMap;
    }

    private LinkedHashSet<ConfigLeafAttributeWithInsertOp> getCurrentLeafLists(Set<ConfigLeafAttribute> previousLeafLists, Set<EditChangeNode> changeNodes, String parentOperation) {
        LinkedHashSet<ConfigLeafAttributeWithInsertOp> currentLeafLists = new LinkedHashSet<>();
        for (EditChangeNode changeNode : changeNodes) {
            String operation = changeNode.getOperation();
            ConfigLeafAttribute newLeafList = changeNode.getConfigLeafAttribute();
            //Removing from previous list in all cases since in create/merge/replace we add new one with proper insert operation and for remove/delete we remove from the list
            if (previousLeafLists != null) {
                previousLeafLists.remove(newLeafList);
            }
            if (operation.equals(EditConfigOperations.CREATE) || operation.equals(EditConfigOperations.MERGE) || operation.equals(EditConfigOperations.REPLACE)) {
                currentLeafLists.add(new ConfigLeafAttributeWithInsertOp(newLeafList, changeNode.getInsertOperation()));
            }
        }
        if (previousLeafLists != null && !parentOperation.equals(EditConfigOperations.REPLACE)) {
            for (ConfigLeafAttribute previousLeafList : previousLeafLists) {
                currentLeafLists.add(new ConfigLeafAttributeWithInsertOp(previousLeafList));
            }
        }
        if (currentLeafLists.isEmpty()) {
            return null;
        }
        return currentLeafLists;
    }
}
