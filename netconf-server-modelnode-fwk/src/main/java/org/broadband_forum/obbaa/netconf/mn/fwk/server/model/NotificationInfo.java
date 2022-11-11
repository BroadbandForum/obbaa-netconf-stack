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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;

public class NotificationInfo {
    private ModelNode m_changedNode;
    private ModelNodeChange m_change;
    private NetconfClientInfo m_clientInfo;
    private boolean m_isImplied;
    private ModelNode m_childNode;

    public NotificationInfo addNotificationInfo(ModelNode changedNode, ModelNodeChange change, NetconfClientInfo clientInfo) {
        return addNotificationInfo(changedNode, change, clientInfo, false);
    }

    public NotificationInfo addNotificationInfo(ModelNode changedNode, ModelNodeChange change, NetconfClientInfo clientInfo, boolean isImplied) {
        return addNotificationInfo(changedNode, change, clientInfo, isImplied, null);
    }
    
    public NotificationInfo addNotificationInfo(ModelNode changedNode, ModelNodeChange change, NetconfClientInfo clientInfo, boolean isImplied, ModelNode childNode) {
        this.m_changedNode = changedNode;
        this.m_change = change;
        this.setClientInfo(clientInfo);
        this.setImplied(isImplied);
        this.m_childNode = childNode;
        return this;
    }

    public void setChangeData(EditContainmentNode changeData) throws EditConfigException {
        if(m_change == null){
            m_change = new ModelNodeChange(null, null);
        }
        this.m_change.setChangeData(changeData);
    }

    public ModelNode getChangedNode() {
        return m_changedNode;
    }

    public void setChangedNode(ModelNode changedNode) {
        m_changedNode = changedNode;
    }

    public ModelNodeChange getChange() {
        return m_change;
    }

    public void setChange(ModelNodeChange change) {
        m_change = change;
    }

    public NetconfClientInfo getClientInfo() {
        return m_clientInfo;
    }

    public void setClientInfo(NetconfClientInfo clientInfo) {
        m_clientInfo = clientInfo;
    }

    public boolean isImplied() {
        return m_isImplied;
    }

    public void setImplied(boolean implied) {
        m_isImplied = implied;
    }
    
    public ModelNode getChildNode() {
		return m_childNode;
	}

	public void setChildNode(ModelNode childModelNode) {
		this.m_childNode = childModelNode;
	}

	@Override
    public String toString() {
        return "NotificationInfo{" +
                "m_changedNode=" + m_changedNode +
                ", m_change=" + m_change +
                '}';
    }
}
