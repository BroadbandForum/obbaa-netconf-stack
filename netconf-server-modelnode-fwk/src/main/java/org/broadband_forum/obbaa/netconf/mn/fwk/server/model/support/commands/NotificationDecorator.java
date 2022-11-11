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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.IS_FULL_DS_REBUILT;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
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
    private ChangeCommand m_decoratedCommand;
    private NetconfClientInfo m_clientInfo;
    private boolean m_isImplied;
    private ModelNode m_childNode;
    private WritableChangeTreeNode m_changeTreeNode;

    public NotificationDecorator(ChangeCommand innerCommand, ModelNode changedNode, ModelNodeChange change,
                                 NotificationContext notificationContext, NetconfClientInfo clientInfo, ModelNode childModelNode, WritableChangeTreeNode changeTreeNode) {
        this(innerCommand, changedNode, change, notificationContext, clientInfo, false, childModelNode, changeTreeNode);
    }

    public NotificationDecorator(ChangeCommand innerCommand, ModelNode changedNode, ModelNodeChange change,
                                 NotificationContext notificationContext, NetconfClientInfo clientInfo, boolean isImplied, ModelNode childModelNode, WritableChangeTreeNode changeTreeNode) {
        this.m_decoratedCommand = innerCommand;
        this.m_changedNode = changedNode;
        this.m_change = change;
        this.m_notificationContext = notificationContext;
        this.m_clientInfo = clientInfo;
        this.m_isImplied = isImplied;
        this.m_childNode = childModelNode;
        this.m_changeTreeNode = changeTreeNode;
    }

    @Override
    public void execute() throws CommandExecutionException {
        m_decoratedCommand.appendChange(m_changeTreeNode, m_isImplied);
        m_decoratedCommand.execute();
        //below two lines were added to make the obbaa work
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
