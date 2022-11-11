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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.addVisibilityContext;

import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class DeleteListCommand implements ChangeCommand {

	private ModelNode m_parentModelNode;
	private ChildListHelper m_childListHelper;
	private ModelNode m_childModelNode;
	private EditChangeSource m_changeSource;

	@Override
	public void execute() throws CommandExecutionException {
		try {
			m_childListHelper.removeChild(m_parentModelNode, m_childModelNode);
		} catch (IllegalArgumentException | ModelNodeDeleteException e) {
			throw new CommandExecutionException(e);
		}
	}

    public DeleteListCommand addRemoveInfo(ChildListHelper childListHelper, ModelNode instance, ModelNode item, EditChangeSource changeSource) {
		this.m_childListHelper = childListHelper;
		this.m_parentModelNode = instance;
		this.m_childModelNode = item;
		this.m_changeSource = changeSource;
		return this;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DeleteListCommand{");
		sb.append("m_parentModelNode=").append(m_parentModelNode);
		sb.append(", m_childListHelper=").append(m_childListHelper);
		sb.append(", m_childModelNode=").append(m_childModelNode);
		sb.append('}');
		return sb.toString();
	}

    @Override
    public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
        SchemaRegistry schemaRegistry = m_parentModelNode.getSchemaRegistry();
        ListSchemaNode listSchemaNode = m_childListHelper.getSchemaNode();
        ModelNodeId listID = m_childModelNode.getModelNodeId();
        WritableChangeTreeNode listChange = new ChangeTreeNodeImpl(schemaRegistry, parent, listID, listSchemaNode,
                parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
        listChange.setChangeType(ChangeTreeNode.ChangeType.delete);
        listChange.setImplied(isImplied);
        listChange.setEditChangeSource(m_changeSource);
        if(parent.isMountPoint()) {
        	listChange.setMountPoint(true);
			CommandUtils.appendDeleteCtnForChildNodes(listChange, m_childModelNode);
		}
		if(!m_childModelNode.isVisible()) {
			addVisibilityContext(listChange);
		}
		CommandUtils.setPreviousLeafValues(listChange,listID, m_childModelNode, m_changeSource);
        parent.appendChildNode(listChange);
    }
}
