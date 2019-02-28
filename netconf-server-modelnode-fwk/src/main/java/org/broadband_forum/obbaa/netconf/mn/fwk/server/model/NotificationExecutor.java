package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;

public interface NotificationExecutor {

	public Map<SubSystem, List<ChangeNotification>> getSubSystemNotificationMap(String dataStoreName, List<NotificationInfo> notifInfos, EditConfigRequest request);
	
	public void sendPreCommitNotifications(Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap) throws SubSystemValidationException;
	
	public void sendNotifications(Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap);

}
