package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;

public abstract class AbstractChildCreationCommand implements Command {

	protected EditContainmentNode m_editData;
	protected NotificationContext m_notificationContext;
	protected String m_errorOption;
	protected DefaultCapabilityCommandInterceptor m_interceptor;
    protected NetconfClientInfo m_clientInfo;
    protected EditContext m_parentEditContext;

	public AbstractChildCreationCommand(EditContainmentNode editData, NotificationContext notificationContext, String errorOption,
            DefaultCapabilityCommandInterceptor interceptor, NetconfClientInfo clientInfo, EditContext parentEditContext) {
		m_editData = editData;
		m_notificationContext = notificationContext;
		m_errorOption = errorOption;
		m_interceptor = interceptor;
        m_clientInfo = clientInfo;
        m_parentEditContext = parentEditContext;
	}

	@Override
	public void execute() throws CommandExecutionException {
		try {
			ModelNode childNode = createChild();
			EditContainmentNode newEditData = m_editData;
			if (childNode != null) {
				newEditData = m_interceptor.processMissingData(newEditData, childNode);
			}
			delegateToChild(newEditData, childNode);
		} catch (EditConfigException | ModelNodeCreateException e1) {
			if (e1 instanceof ModelNodeCreateException) {
				if (((ModelNodeCreateException) e1).getRpcError() != null) {
					throw new CommandExecutionException(((ModelNodeCreateException) e1).getRpcError(), e1);
				}
			}
			throw new CommandExecutionException(e1);
		}
	}

	protected abstract ModelNode createChild() throws ModelNodeCreateException;

	protected void delegateToChild(EditContainmentNode editData, ModelNode childNode) throws EditConfigException {
		if(editData.getChangeNodes().size() > 0 || editData.getChildren().size() > 0) {
			editData.setEditOperation(EditConfigOperations.MERGE);
            EditContext context = new EditContext(editData, m_notificationContext, m_errorOption, m_clientInfo);
            context.setParentContext(m_parentEditContext);
            childNode.editConfig(context);
        }
	}

	protected void setEditData(EditContainmentNode editData){
	    m_editData=new EditContainmentNode(editData);
	}

}
