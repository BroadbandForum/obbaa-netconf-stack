package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChange;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;

import java.util.ArrayList;
import java.util.List;

public class SubsystemNotificationCommand implements Command{

    private ModelNode m_changedNode;
    private List<ModelNodeRdn> m_ids;
    private ModelNodeChange m_change;
    private String m_dataStore;
    private List<SubsystemNotificationCommand> m_subsystemNotificationCommand;
    
    @Override
    public void execute() throws CommandExecutionException {
    	
    	List<ChangeNotification> changeNotification = new ArrayList<ChangeNotification>();
    	
    	SubSystem subsystem = null;
    	for (SubsystemNotificationCommand notifCommand : m_subsystemNotificationCommand) {
    		changeNotification.add((ChangeNotification)new EditConfigChangeNotification(new ModelNodeId(notifCommand.m_ids), notifCommand.m_change, notifCommand.m_dataStore, notifCommand.m_changedNode));
    		subsystem = notifCommand.m_changedNode.getSubSystem();
    	}
    	
    	if (subsystem != null){
    		subsystem.notifyChanged(changeNotification);
    	}
    }

    public SubsystemNotificationCommand addNotificationInfo(ModelNode changedNode, List<ModelNodeRdn> ids, ModelNodeChange change) {
        this.m_changedNode = changedNode;
        this.m_ids = ids;
        this.m_change = change;
        return this;
    }

    public void setChangeData(EditContainmentNode changeData) throws EditConfigException {
        if(m_change == null){
            m_change = new ModelNodeChange(null, null);
        }
        this.m_change.setChangeData(changeData);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SubsystemNotificationCommand{");
        sb.append("m_changedNode=").append(m_changedNode);
        sb.append(", m_ids=").append(m_ids);
        sb.append(", m_change=").append(m_change);
        sb.append(", m_dataStore='").append(m_dataStore).append('\'');
        sb.append(", m_subsystemNotificationCommand=").append(m_subsystemNotificationCommand);
        sb.append('}');
        return sb.toString();
    }

    public void setDataStore(String dataStore) {
        m_dataStore=dataStore;
        
    }

	public List<SubsystemNotificationCommand> getSubsystemNotificationCommand() {
		return m_subsystemNotificationCommand;
	}

	public void setSubsystemNotificationCommand(
			List<SubsystemNotificationCommand> subsystemNotificationCommand) {
		this.m_subsystemNotificationCommand = subsystemNotificationCommand;
	}


}
