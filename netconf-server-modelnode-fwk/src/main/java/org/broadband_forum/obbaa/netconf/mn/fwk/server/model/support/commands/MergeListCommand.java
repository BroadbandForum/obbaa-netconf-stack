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

import java.util.Collection;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode.ChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * It updates the index of the user ordered list and prepares child commands.
 */
public class MergeListCommand implements ChangeCommand {

	private ModelNode m_parentNode;
	private ChildListHelper m_childListHelper;
	private SchemaRegistry m_schemaRegistry;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	private EditContext m_editContext;
	private ModelNode m_childNode;
	private Command m_childCommands;
	private EditContainmentNode m_editData;
	private WritableChangeTreeNode m_changeTreeNode;
	private ModelNode m_existingNode;
	private Collection<ModelNode> m_existingChildNodes;
	private int m_insertIndex;

	public MergeListCommand addAddInfo(ChildListHelper childListHelper, ModelNode parentNode, EditContext editContext, ModelNode childNode, WritableChangeTreeNode changeTreeNode) {
        this.m_childListHelper = childListHelper;
        this.m_parentNode = parentNode;
		this.m_schemaRegistry = parentNode.getSchemaRegistry();
        this.m_modelNodeHelperRegistry = ((HelperDrivenModelNode)parentNode).getModelNodeHelperRegistry();
		this.m_editContext = editContext;
		this.m_childNode = childNode;
		this.m_editData = new EditContainmentNode(editContext.getEditNode());
		this.m_changeTreeNode = changeTreeNode;
		this.m_childCommands = getChildCommands();
        return this;
	}

	private Command getChildCommands() {
		return m_childNode.getEditCommand(new EditContext(m_editContext), m_changeTreeNode);
	}

	@Override
	public void execute() throws CommandExecutionException {
		try {
			checkAndUpdatePosition();
			mergeChild();
		} catch (EditConfigException | ModelNodeCreateException | ModelNodeGetException e) {
			if (e instanceof ModelNodeCreateException) {
				if (((ModelNodeCreateException) e).getRpcError() != null) {
					throw new CommandExecutionException(((ModelNodeCreateException) e).getRpcError(), e);
				}
			}
			throw new CommandExecutionException(e);
		}
	}

	private void checkAndUpdatePosition() throws ModelNodeCreateException, ModelNodeGetException, EditConfigException {
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
		}
	}

	protected void mergeChild() throws CommandExecutionException {
		m_childCommands.execute();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("MergeListCommand{");
		sb.append("m_parentNode=").append(m_parentNode);
		sb.append(", m_childListHelper=").append(m_childListHelper);
		sb.append(", m_schemaRegistry=").append(m_schemaRegistry);
		sb.append(", m_modelNodeHelperRegistry=").append(m_modelNodeHelperRegistry);
		sb.append(", m_editContext=").append(m_editContext);
		sb.append(", m_childNode=").append(m_childNode);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
		populateAdditionalInfo();
		if (m_childListHelper.isOrderUpdateNeededForChild(m_existingChildNodes, m_childNode, m_insertIndex)) {
			SchemaRegistry schemaRegistry = m_parentNode.getSchemaRegistry();
			ListSchemaNode listSchemaNode = m_childListHelper.getSchemaNode();
			ModelNodeId parentId = m_parentNode.getModelNodeId();
			ModelNodeId listId = buildModelNodeId(parentId, m_editData, listSchemaNode);
			WritableChangeTreeNode listChange = new ChangeTreeNodeImpl(schemaRegistry, parent, listId, listSchemaNode,
					parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
			listChange.setInsertOperation(m_editData.getInsertOperation());
			listChange.setEditOperation(m_editData.getEditOperation());
			listChange.setChangeType(ChangeType.modify);
			listChange.setImplied(isImplied);
			listChange.setEditChangeSource(m_editData.getChangeSource());
			if (parent.isMountPoint()) {
				listChange.setMountPoint(true);
			}
			//create key CTNs
			appendKeyCTN(listSchemaNode, listId, listChange, m_editData, ChangeType.none);
			parent.appendChildNode(listChange);
		}
	}

	//These information are required to determine whether update of insert order of a list is really needed.
	protected void populateAdditionalInfo() {
		m_existingChildNodes = m_childListHelper.getValue(m_parentNode, Collections.emptyMap());
		m_existingNode = getExistingNodeBasedOnInsertOperation(m_editData, m_childListHelper, m_parentNode, m_modelNodeHelperRegistry, m_schemaRegistry);
		m_insertIndex = m_childListHelper.getNewInsertIndex(m_existingChildNodes, m_editData.getInsertOperation(), m_existingNode, m_childNode);
	}
}
