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

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public class CreateContainerCommand extends AbstractChildCreationCommand implements ChangeCommand {

    private ModelNode m_parentModelNode;
    private ChildContainerHelper m_childContainerHelper;

    public CreateContainerCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor, WritableChangeTreeNode changeTreeNode) {
        super(new EditContainmentNode(editContext.getEditNode()), editContext.getNotificationContext(), editContext.getErrorOption(),
                interceptor, editContext.getClientInfo(), editContext, changeTreeNode);
	}

    public CreateContainerCommand addCreateInfo(ChildContainerHelper childContainerHelper, ModelNode parentModelNode) {
        this.m_childContainerHelper = childContainerHelper;
        this.m_parentModelNode = parentModelNode;
        return this;
    }

	@Override
	protected ModelNode createChild() throws ModelNodeCreateException {
		Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
        for (EditMatchNode node : m_editData.getMatchNodes()) {
            keyAttrs.put(node.getQName(), node.getConfigLeafAttribute());
        }

        ModelNode newNode = m_childContainerHelper.createChild(m_parentModelNode, keyAttrs);

        return newNode;
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateContainerCommand{");
        sb.append("m_parentNode=").append(m_parentModelNode);
        sb.append(", m_childContainerHelper=").append(m_childContainerHelper);
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

        containerChange.setChangeType(ChangeTreeNode.ChangeType.create);
        containerChange.setEditOperation(m_editData.getEditOperation());
        containerChange.setImplied(isImplied);
        containerChange.setEditChangeSource(m_editData.getChangeSource());
        if(parent.isMountPoint()) {
            containerChange.setMountPoint(true);
        }
        parent.appendChildNode(containerChange);
    }
}
