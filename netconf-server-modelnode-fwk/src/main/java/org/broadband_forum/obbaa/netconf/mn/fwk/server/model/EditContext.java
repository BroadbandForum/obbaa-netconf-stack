package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;

public class EditContext {
    public EditContainmentNode m_editNode;
    public NotificationContext m_notificationContext;
    private String m_errorOption = EditConfigErrorOptions.ROLLBACK_ON_ERROR;
    private NetconfClientInfo m_clientInfo;
    private EditContext m_parent;

    public EditContext(EditContainmentNode editNode, NotificationContext notificationContext, String errorOption, NetconfClientInfo clientInfo) {
        m_editNode = editNode;
        m_notificationContext = notificationContext;
        m_errorOption = errorOption;
        m_clientInfo = clientInfo;
    }

    public EditContext(EditContext that) {
        m_editNode = new EditContainmentNode(that.getEditNode());
        m_notificationContext = that.getNotificationContext();
        m_errorOption = that.getErrorOption();
        m_clientInfo = that.getClientInfo();
    }
    
	public EditContainmentNode getEditNode() {
        return m_editNode;
    }

    public EditContext setEditNode(EditContainmentNode editNode) {
        m_editNode = editNode;
        return this;
    }

    public EditContext appendNotificationInfo(NotificationInfo info){
        m_notificationContext.appendNotificationInfo(info);
        return this;
    }
    public NotificationContext getNotificationContext() {
        return m_notificationContext;
    }
    public String getErrorOption() {
        return m_errorOption ;
    }
    public EditContext setErrorOption(String errorOption) {
        m_errorOption = errorOption;
        return this;
    }

    public NetconfClientInfo getClientInfo() {
        return m_clientInfo;
    }
    public void setClientInfo(NetconfClientInfo clientInfo) {
        this.m_clientInfo = clientInfo;
    }

	@Override
	public String toString() {
		return "EditContext [m_editNode=" + m_editNode + ", m_clientInfo=" + m_clientInfo + "]";
	}
	
	public void setParentContext(EditContext parentContext) {
	    m_parent = parentContext;
	}
	
	public EditContext getParent() {
	    return m_parent;
	}
}