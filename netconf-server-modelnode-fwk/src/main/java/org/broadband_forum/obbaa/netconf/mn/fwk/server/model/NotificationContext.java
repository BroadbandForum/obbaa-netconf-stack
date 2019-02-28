package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationContext {

    List<NotificationInfo> m_notifInfos = new ArrayList<NotificationInfo>();
    
    public NotificationContext(NotificationContext notificationContext) {
        m_notifInfos = new ArrayList<>(notificationContext.getNotificationInfos());
    }

    public NotificationContext() {
    }

    public void appendNotificationInfo(NotificationInfo command) {
    	//add on top of stack
        //m_notifInfos.add(0, command);
    	//FIXME:FNMS-10115 ideally we want the notification to be on top. Since we
    	//are building the command recursively.
    	//but add on top of list is expensive as list grows. 
    	//reversing the list while retriving is cheaper than adding on top
    	m_notifInfos.add(command);
    }

    public List<NotificationInfo> getNotificationInfos() {
    	List<NotificationInfo> returnList = new ArrayList<NotificationInfo>(m_notifInfos);
    	Collections.reverse(returnList);
        return returnList;
    }
    
    @Override
    public String toString() {
        return "NotificationContext [m_notifInfos=" + m_notifInfos + "]";
    }

}
