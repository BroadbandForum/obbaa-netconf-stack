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

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FlagForRestPutOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public class ReplaceContainerCommand implements ChangeCommand {

	private final EditContext m_editContext;
	private EditContainmentNode m_editData;
    private ChildContainerHelper m_childContainerHelper;
    private DefaultCapabilityCommandInterceptor m_interceptor;
    private ModelNode m_parentNode;
	private NotificationContext m_notificationContext;
	private String m_errorOption;
    private NetconfClientInfo m_clientInfo;
	private Command m_childCommands;
	private WritableChangeTreeNode m_changeTreeNode;

	public ReplaceContainerCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor, WritableChangeTreeNode changeTreeNode) {
		m_editContext = editContext;
		m_editData = new EditContainmentNode(editContext.getEditNode());
		m_notificationContext = editContext.getNotificationContext();
		m_errorOption = editContext.getErrorOption();
		m_interceptor = interceptor;
        m_clientInfo = editContext.getClientInfo();
        m_changeTreeNode = changeTreeNode;
	}

	public ReplaceContainerCommand addReplaceInfo(ChildContainerHelper childContainerHelper, ModelNode instance) {
        this.m_childContainerHelper = childContainerHelper;
        this.m_parentNode = instance;
        this.m_childCommands = getChildCommands();
        return this;
    }

	private Command getChildCommands() {
		ModelNode child = m_childContainerHelper.getValue(m_parentNode);
		FlagForRestPutOperations.setInstanceReplaceFlag();
		return child.getEditCommand(m_editContext, m_changeTreeNode);
	}

	protected ModelNode replaceChild() throws CommandExecutionException {
		m_childCommands.execute();
		return m_childContainerHelper.getValue(m_parentNode);
	}

	@Override
	public void execute() throws CommandExecutionException {
		try {
			ModelNode childNode = replaceChild();
			EditContainmentNode newEditData = m_editData;
			if (childNode != null) {
				newEditData = m_interceptor.processMissingData(newEditData, childNode);
			}
			if(newEditData.getHasChanged()) {
				delegateToChild(newEditData, childNode);
			}
		} catch (EditConfigException e) {
			throw new CommandExecutionException(e);
		}
	}

	protected void delegateToChild(EditContainmentNode editData, ModelNode childNode) throws EditConfigException {
		if(editData.getChangeNodes().size() > 0 || editData.getChildren().size() > 0) {
			editData.setEditOperation(EditConfigOperations.MERGE);
			EditContext context = new EditContext(editData, m_notificationContext, m_errorOption, m_clientInfo);
			childNode.editConfig(context, m_changeTreeNode);
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ReplaceContainerCommand{");
		sb.append("m_editData=").append(m_editData);
		sb.append(", m_parentNode=").append(m_parentNode);
		sb.append(", m_childContainerHelper=").append(m_childContainerHelper);
		sb.append(", m_interceptor=").append(m_interceptor);
		sb.append(", m_notificationContext=").append(m_notificationContext);
		sb.append(", m_errorOption='").append(m_errorOption).append('\'');
		sb.append('}');
		return sb.toString();
	}

	@Override
	public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {

		if (((CompositeEditCommand) m_childCommands).getCommands().size() > 1) {
			SchemaRegistry schemaRegistry = m_parentNode.getSchemaRegistry();
			ContainerSchemaNode containerSchemaNode = m_childContainerHelper.getSchemaNode();
			ModelNodeId containerID = new ModelNodeId(m_parentNode.getModelNodeId().getRdnsReadOnly());
			QName containerQName = containerSchemaNode.getQName();
			containerID.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, containerQName.getNamespace().toString(), containerQName.getLocalName()));
			WritableChangeTreeNode containerChange = new ChangeTreeNodeImpl(schemaRegistry, parent, containerID, containerSchemaNode,
					parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
			containerChange.setInsertOperation(m_editData.getInsertOperation());
			containerChange.setEditOperation(m_editData.getEditOperation());
			containerChange.setImplied(isImplied);
			containerChange.setEditChangeSource(m_editData.getChangeSource());
			if(parent.isMountPoint()) {
				containerChange.setMountPoint(true);
			}
			parent.appendChildNode(containerChange);
		}
	}
}
