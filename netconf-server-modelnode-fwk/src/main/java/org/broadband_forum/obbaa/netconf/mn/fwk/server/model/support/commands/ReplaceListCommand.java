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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.appendKeyCTN;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.buildModelNodeId;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.getExistingNodeBasedOnInsertOperation;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.setInsertOperation;

import java.util.Collection;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode.ChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FlagForRestPutOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ReplaceMNUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class ReplaceListCommand implements ChangeCommand {

    private final EditContext m_editContext;
    private EditContainmentNode m_editData;
    private ChildListHelper m_childListHelper;
    private DefaultCapabilityCommandInterceptor m_interceptor;
    private ModelNode m_parentNode;
    private ModelNode m_childNode;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private NotificationContext m_notificationContext;
    private String m_errorOption;
    private NetconfClientInfo m_clientInfo;
    private Command m_childCommands;
    private WritableChangeTreeNode m_changeTreeNode;
    private ModelNode m_existingNode;
    private Collection<ModelNode> m_existingChildNodes;
    private int m_insertIndex;

    public ReplaceListCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor, WritableChangeTreeNode changeTreeNode) {
        m_editData = new EditContainmentNode(editContext.getEditNode());
        m_interceptor = interceptor;
        m_editContext = editContext;
        m_notificationContext = editContext.getNotificationContext();
        m_errorOption = editContext.getErrorOption();
        m_clientInfo = editContext.getClientInfo();
        m_changeTreeNode = changeTreeNode;
    }

    public ReplaceListCommand addReplaceInfo(ChildListHelper childListHelper, ModelNode instance, ModelNode child) {
        this.m_childListHelper = childListHelper;
        this.m_parentNode = instance;
        this.m_childNode = child;
        this.m_schemaRegistry = instance.getSchemaRegistry();
        this.m_modelNodeHelperRegistry = ((HelperDrivenModelNode)instance).getModelNodeHelperRegistry();
        this.m_childCommands = getChildCommands();
        return this;
    }

    private Command getChildCommands() {
        FlagForRestPutOperations.setInstanceReplaceFlag();
        return m_childNode.getEditCommand(m_editContext, m_changeTreeNode);
    }

    @Override
    public void execute() throws CommandExecutionException {
        try {
            checkIfInsertPosIsValid();
            ModelNode childNode = replaceChild();
            EditContainmentNode newEditData = m_editData;
            if (childNode != null) {
                newEditData = m_interceptor.processMissingData(newEditData, childNode);
            }
            if(newEditData.getHasChanged()) {
                delegateToChild(newEditData, childNode);
            }
        } catch (EditConfigException | ModelNodeCreateException e) {
            if (e instanceof ModelNodeCreateException) {
                if (((ModelNodeCreateException) e).getRpcError() != null) {
                    throw new CommandExecutionException(((ModelNodeCreateException) e).getRpcError(), e);
                }
            }
            throw new CommandExecutionException(e);
        }
    }

    protected void delegateToChild(EditContainmentNode editData, ModelNode childNode) throws EditConfigException {
        if(editData.getChangeNodes().size() > 0 || editData.getChildren().size() > 0) {
            editData.setEditOperation(EditConfigOperations.MERGE);
            EditContext context = new EditContext(editData, m_notificationContext, m_errorOption, m_clientInfo);
            context.setParentContext(m_editContext);
            childNode.editConfig(context, m_changeTreeNode);
        }
    }

    protected ModelNode replaceChild() throws CommandExecutionException {
        m_childCommands.execute();
        Collection<ModelNode> modelNodes = m_childListHelper.getValue(m_parentNode, ReplaceMNUtil.getKeyFromEditNode(m_editData));
        if(modelNodes.size() > 1 || modelNodes.isEmpty()){
            throw new RuntimeException("Single instance of list entry expected for " + m_childListHelper.getSchemaNode());
        }
        return modelNodes.iterator().next();
    }

    private void checkIfInsertPosIsValid() throws ModelNodeCreateException {
        if (isPositionChanged()) { //ordered-by user
            InsertOperation insertOperation = m_editData.getInsertOperation();
            if (InsertOperation.AFTER.equals(insertOperation.getName()) || InsertOperation.BEFORE.equals(insertOperation.getName())) {
                if (m_existingNode == null) {
                    throw new ModelNodeCreateException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.BAD_ATTRIBUTE,
                            "The instance - " + m_editData.getQName().getLocalName() + " getting by key '"
                                    + insertOperation.getValue() + "' does not exist. Request Failed."));
                }
                //Check same list node
                if (m_childNode.equals(m_existingNode)) {
                    String errorMessage = "The instance - " + m_editData.getQName().getLocalName() + " getting by key '"
                            + insertOperation.getValue() + "' can't be same as the edit node. Request Failed.";
                    throw new ModelNodeCreateException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.DATA_MISSING, errorMessage));
                }
            }
            if (m_childListHelper.isOrderUpdateNeededForChild(m_existingChildNodes, m_childNode, m_insertIndex)) {
                m_childListHelper.updateChildByUserOrder(m_parentNode, m_childNode, m_insertIndex);
                FlagForRestPutOperations.setInstanceReplaceFlag();
            }
        }
    }

    private boolean isPositionChanged() {
        return SchemaRegistryUtil.isListOrderedByUser(m_editData.getQName(), m_parentNode.getModelNodeSchemaPath(), m_schemaRegistry)
                && m_editData.getInsertOperation() != null;
    }

	@Override
	public String toString() {
        final StringBuilder sb = new StringBuilder("ReplaceListCommand{");
		sb.append("m_childListHelper=").append(m_childListHelper);
		sb.append(", m_parentNode=").append(m_parentNode);
		sb.append(", m_childNode=").append(m_childNode);
		sb.append(", m_schemaRegistry=").append(m_schemaRegistry);
		sb.append(", m_modelNodeHelperRegistry=").append(m_modelNodeHelperRegistry);
		sb.append('}');
		return sb.toString();
	}

    @Override
    public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
        if(m_editData.getInsertOperation() != null) {
            populateAdditionalInfo();
            if (m_childListHelper.isOrderUpdateNeededForChild(m_existingChildNodes, m_childNode, m_insertIndex)) {
                SchemaRegistry schemaRegistry = m_parentNode.getSchemaRegistry();
                ListSchemaNode listSchemaNode = m_childListHelper.getSchemaNode();
                ModelNodeId parentId = m_parentNode.getModelNodeId();
                ModelNodeId listID = buildModelNodeId(parentId, m_editData, listSchemaNode);
                WritableChangeTreeNode listChange = new ChangeTreeNodeImpl(schemaRegistry, parent, listID, listSchemaNode,
                        parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
                if (listSchemaNode.isUserOrdered()) {
                    setInsertOperation(m_editData, listChange);
                }
                listChange.setEditOperation(m_editData.getEditOperation());
                listChange.setChangeType(ChangeType.modify);
                listChange.setImplied(isImplied);
                listChange.setEditChangeSource(m_editData.getChangeSource());
                if (parent.isMountPoint()) {
                    listChange.setMountPoint(true);
                }

                //create key CTNs
                appendKeyCTN(listSchemaNode, listID, listChange, m_editData, ChangeType.none);
                parent.appendChildNode(listChange);
            }
        }
    }

    //These information are required to determine whether update of insert order of a list is really needed.
    protected void populateAdditionalInfo() {
        m_existingChildNodes = m_childListHelper.getValue(m_parentNode, Collections.emptyMap());
        m_existingNode = getExistingNodeBasedOnInsertOperation(m_editData, m_childListHelper, m_parentNode, m_modelNodeHelperRegistry, m_schemaRegistry);
        m_insertIndex = m_childListHelper.getNewInsertIndex(m_existingChildNodes, m_editData.getInsertOperation(), m_existingNode, m_childNode);
    }
}
