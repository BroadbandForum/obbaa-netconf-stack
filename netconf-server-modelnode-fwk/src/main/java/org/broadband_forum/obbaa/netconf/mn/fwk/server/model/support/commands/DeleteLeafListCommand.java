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

import java.util.LinkedHashSet;

import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttributeWithInsertOp;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public class DeleteLeafListCommand implements ChangeCommand {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DeleteLeafListCommand.class, LogAppNames.NETCONF_STACK);

	private ChildLeafListHelper m_childLeafListHelper;

	private ModelNode m_leafListModelNode;
	private EditChangeSource m_changeSource;

	public DeleteLeafListCommand addDeleteInfo(ChildLeafListHelper childLeafListHelper, ModelNode instance, EditChangeSource changeSource) {
		this.m_childLeafListHelper = childLeafListHelper;
		this.m_leafListModelNode = instance;
		this.m_changeSource = changeSource;
		return this;
	}

	@Override
	public void execute() throws CommandExecutionException {
		try {
			m_childLeafListHelper.removeAllChild(m_leafListModelNode);
		} catch (ModelNodeDeleteException e) {
			LOGGER.error("Error while deleting leaf-lists for ModelNode {}" , m_leafListModelNode, e);
			throw new CommandExecutionException(e);
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DeleteLeafListCommand{");
		sb.append("m_childLeafListHelper=").append(m_childLeafListHelper);
		sb.append(", m_leafListModelNode=").append(m_leafListModelNode);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
		SchemaRegistry schemaRegistry = m_leafListModelNode.getSchemaRegistry();
		LeafListSchemaNode leafListSchemaNode = m_childLeafListHelper.getLeafListSchemaNode();
		ModelNodeId leafListId = new ModelNodeId(m_leafListModelNode.getModelNodeId().getRdnsReadOnly());
		QName leafListQname = leafListSchemaNode.getQName();
		leafListId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, leafListQname.getNamespace().toString(), leafListQname.getLocalName()));
		WritableChangeTreeNode leafListChange = new ChangeTreeNodeImpl(schemaRegistry, parent, leafListId, leafListSchemaNode,
				parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());

		if (m_leafListModelNode.getLeafList(leafListQname) != null) {
			LinkedHashSet<ConfigLeafAttributeWithInsertOp> leafListAttributes = new LinkedHashSet<>();
			for (ConfigLeafAttribute leafListAttribute : m_leafListModelNode.getLeafList(leafListQname)) {
				leafListAttributes.add(new ConfigLeafAttributeWithInsertOp(leafListAttribute));
			}
			leafListChange.setPreviousValue(leafListAttributes);
		}
		leafListChange.setImplied(isImplied);
		leafListChange.setEditChangeSource(m_changeSource);
		if(parent.isMountPoint()) {
			leafListChange.setMountPoint(true);
		}
		parent.appendChildNode(leafListChange);
	}
}
