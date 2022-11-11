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

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;

public class SubSystemValidationDecorator implements ChangeCommand {

    private ChangeCommand m_decoratedCommand;
    private SubSystem m_subSystem;
    private EditContext m_editContext;
    private ModelNodeChangeType m_changeType;
    private ModelNode m_changedNode;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    public SubSystemValidationDecorator(ChangeCommand innerCommand, SubSystem subSystem, EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode, ModelNodeHelperRegistry modelNodeHelperRegistry) {
		this.m_decoratedCommand = innerCommand;
		this.m_subSystem = subSystem;
		this.m_editContext = editContext;
		this.m_changeType = changeType;
		this.m_changedNode = changedNode;
		this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
	}
    @Override
    public void execute() throws CommandExecutionException {
    	try {
    		boolean isChanged = m_subSystem.handleChanges(m_editContext, m_changeType, m_changedNode);
	    	m_subSystem.testChange(m_editContext, m_changeType, m_changedNode, m_modelNodeHelperRegistry);
	    	if(isChanged && m_decoratedCommand instanceof AbstractChildCreationCommand){
	    		((AbstractChildCreationCommand)m_decoratedCommand).setEditData(m_editContext.getEditNode());
	    	}
	    	m_decoratedCommand.execute();
    	} catch (SubSystemValidationException e) {
			if (e.getRpcError() != null && e.getRpcError().getErrorPath() == null) {
				NetconfRpcError error = e.getRpcError();
				error.setErrorPath(m_changedNode.getModelNodeId().xPathString(m_editContext.getEditNode().getSchemaRegistry()), m_changedNode.getModelNodeId().xPathStringNsByPrefix(m_editContext.getEditNode().getSchemaRegistry()));
			}
    		throw new CommandExecutionException(e.getRpcError(), e);
    	}
    }

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("SubSystemValidationDecorator{");
		sb.append("m_decoratedCommand=").append(m_decoratedCommand);
		sb.append(", m_subSystem=").append(m_subSystem);
		sb.append(", m_editContext=").append(m_editContext);
		sb.append(", m_changeType=").append(m_changeType);
		sb.append(", m_changedNode=").append(m_changedNode);
		sb.append(", m_modelNodeHelperRegistry=").append(m_modelNodeHelperRegistry);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
    	if (!Boolean.parseBoolean(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(NetconfResources.IS_FULL_DS_REBUILT, "false"))) {
			m_decoratedCommand.appendChange(parent, isImplied);
		}
	}
}
