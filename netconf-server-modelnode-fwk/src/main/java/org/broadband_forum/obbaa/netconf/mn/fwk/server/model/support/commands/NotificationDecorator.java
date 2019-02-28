package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChange;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationInfo;

public class NotificationDecorator implements Command{
    private ModelNode m_changedNode;
    private ModelNodeChange m_change;
    private NotificationContext m_notificationContext;
    private Command m_decoratedCommand;
    private NetconfClientInfo m_clientInfo;
    private boolean m_isImplied;
    private ModelNode m_childNode;
    
    public NotificationDecorator(Command innerCommand, ModelNode changedNode, ModelNodeChange change,
            NotificationContext notificationContext, NetconfClientInfo clientInfo, ModelNode childModelNode) {
        this(innerCommand, changedNode, change, notificationContext, clientInfo, false, childModelNode);
    }

    public NotificationDecorator(Command innerCommand, ModelNode changedNode, ModelNodeChange change,
            NotificationContext notificationContext, NetconfClientInfo clientInfo, boolean isImplied, ModelNode childModelNode) {
        this.m_decoratedCommand = innerCommand;
        this.m_changedNode = changedNode;
        this.m_change = change;
        this.m_notificationContext = notificationContext;
        this.m_clientInfo = clientInfo;
        this.m_isImplied = isImplied;
        this.m_childNode = childModelNode;
    }

    @Override
    public void execute() throws CommandExecutionException {
        m_decoratedCommand.execute();
        NotificationInfo notifInfo = new NotificationInfo().addNotificationInfo(m_changedNode, m_change, m_clientInfo, m_isImplied, m_childNode);
        m_notificationContext.appendNotificationInfo(notifInfo);
        
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotificationDecorator{");
        sb.append("m_changedNode=").append(m_changedNode);
        sb.append(", m_change=").append(m_change);
        sb.append(", m_notificationContext=").append(m_notificationContext);
        sb.append(", m_decoratedCommand=").append(m_decoratedCommand);
        sb.append(", m_isImplied=").append(m_isImplied);
        sb.append('}');
        return sb.toString();
    }
}
