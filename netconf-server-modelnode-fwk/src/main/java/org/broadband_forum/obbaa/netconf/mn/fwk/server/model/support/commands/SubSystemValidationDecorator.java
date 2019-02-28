package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;

public class SubSystemValidationDecorator implements Command{
	
    private Command m_decoratedCommand;
    private SubSystem m_subSystem;
    private EditContext m_editContext;
    private ModelNodeChangeType m_changeType;
    private ModelNode m_changedNode;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    
	public SubSystemValidationDecorator(Command innerCommand, SubSystem subSystem, EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode, ModelNodeHelperRegistry modelNodeHelperRegistry) {
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
}
