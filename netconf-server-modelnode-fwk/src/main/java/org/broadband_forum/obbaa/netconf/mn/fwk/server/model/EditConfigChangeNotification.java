package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;


public class EditConfigChangeNotification implements ChangeNotification, Comparable<EditConfigChangeNotification> {

    private ModelNode m_changedNode;
    private ModelNodeId m_modelNodeId;
    private ModelNodeChange m_change;
    private String m_dataStore;
    private NetconfClientInfo m_clientInfo;
    private boolean m_isDisabledForListener = false;
    private boolean m_isImplied;
    private ModelNode m_childModelNode;
    
    public EditConfigChangeNotification(ModelNodeId nodeid, ModelNodeChange change, String dataStore, ModelNode changedNode, NetconfClientInfo clientInfo) {
		this(nodeid, change, dataStore, changedNode, clientInfo, null);
    }
    
    public EditConfigChangeNotification(ModelNodeId nodeid, ModelNodeChange change, String dataStore, ModelNode changedNode, NetconfClientInfo clientInfo, ModelNode childModelNode) {
        m_modelNodeId = nodeid;
        m_change = change;
        m_dataStore = dataStore;
        m_changedNode = changedNode;
        m_clientInfo = clientInfo;
        m_childModelNode = childModelNode;
    }
    
    public EditConfigChangeNotification(ModelNodeId nodeid, ModelNodeChange change, String dataStore, ModelNode changedNode) {
        m_modelNodeId = nodeid;
        m_change = change;
        m_dataStore = dataStore;
        m_changedNode = changedNode;
    }

    public ModelNodeId getModelNodeId() {
        return m_modelNodeId;
    }

    @Override
    public String toString(boolean printChangeSource) {
    	String modelNodeChangeValue = m_change == null? "" : m_change.toString(printChangeSource);
        return "EditConfigChangeNotification [m_modelNodeId=" + m_modelNodeId + ", m_change=" + modelNodeChangeValue + ", m_dataStore=" +
                m_dataStore +"]";
    }

    public boolean isImplied() {
        return m_isImplied;
    }

    public void setImplied(boolean isImplied){
        m_isImplied = isImplied;
    }

    public void setModelNodeId(ModelNodeId modelNodeId) {
        m_modelNodeId = modelNodeId;
    }

    public ModelNodeChange getChange() {
        return m_change;
    }

    public void setChange(ModelNodeChange change) {
        m_change = change;
    }

    @Override
    public ChangeType getType() {
        return ChangeType.editConfig;
    }

    public String getDataStore() {
        return m_dataStore;
    }

    public void setDataStore(String dataStore) {
        m_dataStore = dataStore;
    }
    
    public ModelNode getChangedNode() {
        return m_changedNode;
    }

    public void setChangedNode(ModelNode changedNode) {
        this.m_changedNode = changedNode;
    }
    
    public String getUser() {
        if (m_clientInfo == null) {
            return null;
        }
        return m_clientInfo.getUsername();
    }
    
    public void setClientInfo(NetconfClientInfo clientInfo) {
        m_clientInfo = clientInfo;
    }
    
    public NetconfClientInfo getClientInfo() {
        return m_clientInfo;
    }
    
    public boolean isDisabledForListener() {
        return m_isDisabledForListener;
    }
    
    public void setDisabledForListener(boolean isDisabledForListener) {
        m_isDisabledForListener = isDisabledForListener;
    }

    public ModelNode getChildModelNode() {
		return m_childModelNode;
	}

	public void setChildModelNode(ModelNode childModelNode) {
		this.m_childModelNode = childModelNode;
	}

	@Override
    public String toString() {
        return toString(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EditConfigChangeNotification that = (EditConfigChangeNotification) o;

        if (m_modelNodeId != null ? !m_modelNodeId.equals(that.m_modelNodeId) : that.m_modelNodeId != null){
            return false;
        }
        if (m_change != null ? !m_change.equals(that.m_change) : that.m_change != null) {
            return false;
        }
        return !(m_dataStore != null ? !m_dataStore.equals(that.m_dataStore) : that.m_dataStore != null);

    }

    @Override
    public int hashCode() {
        int result = m_modelNodeId != null ? m_modelNodeId.hashCode() : 0;
        result = 31 * result + (m_change != null ? m_change.hashCode() : 0);
        result = 31 * result + (m_dataStore != null ? m_dataStore.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(EditConfigChangeNotification another) {
        if (m_modelNodeId == null) {
            if (another.getModelNodeId() == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (another.getModelNodeId() == null) {
                return 1;
            } else {
                return m_modelNodeId.compareTo(another.getModelNodeId());
            }
        }
    }
}
