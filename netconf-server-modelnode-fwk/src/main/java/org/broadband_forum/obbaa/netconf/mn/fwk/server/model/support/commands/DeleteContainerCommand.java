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

import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public class DeleteContainerCommand implements ChangeCommand {

    private ChildContainerHelper m_childContainerHelper;
    private ModelNode m_parentModelNode;
    private ModelNode m_childModelNode;
    private EditChangeSource m_editChangeSource;

    public DeleteContainerCommand addDeleteInfo(ChildContainerHelper childContainerHelper, ModelNode parent, ModelNode child, EditChangeSource changeSource) {
        this.m_childContainerHelper = childContainerHelper;
        this.m_parentModelNode = parent;
        this.m_childModelNode = child;
        this.m_editChangeSource = changeSource;
        return this;
    }

    @Override
    public void execute() throws CommandExecutionException {
        try {
            m_childContainerHelper.deleteChild(m_parentModelNode);
        } catch (ModelNodeDeleteException e) {
            throw new CommandExecutionException(e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteContainerCommand{");
        sb.append("m_childContainerHelper=").append(m_childContainerHelper);
        sb.append(", m_instance=").append(m_parentModelNode);
        sb.append(", m_child=").append(m_childModelNode);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
        SchemaRegistry schemaRegistry = m_parentModelNode.getSchemaRegistry();
        ContainerSchemaNode containerSchemaNode = m_childContainerHelper.getSchemaNode();
        ModelNodeId containerID = new ModelNodeId(m_parentModelNode.getModelNodeId().getRdnsReadOnly());
        QName containerQName = containerSchemaNode.getQName();
        containerID.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, containerQName.getNamespace().toString(), containerQName.getLocalName()));
        WritableChangeTreeNode containerChange = new ChangeTreeNodeImpl(schemaRegistry, parent, containerID, containerSchemaNode,
                parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());

        containerChange.setChangeType(ChangeTreeNode.ChangeType.delete);
        containerChange.setImplied(isImplied);
        containerChange.setEditChangeSource(m_editChangeSource);
        if(parent.isMountPoint()) {
            containerChange.setMountPoint(true);
            CommandUtils.appendDeleteCtnForChildNodes(containerChange, m_childModelNode);
        }
        CommandUtils.setPreviousLeafValues(containerChange,containerID, m_childModelNode, m_editChangeSource);
        parent.appendChildNode(containerChange);
    }
}
