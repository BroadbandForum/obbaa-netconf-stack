package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;

public interface SubsystemNotificationClassifier {
    void classify(Map<Object, Set<ChangeNotification>> classifiedChangeNotifications, List<ChangeNotification> changeNotifications);
}
