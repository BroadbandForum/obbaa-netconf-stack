package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener;

import java.util.concurrent.Executor;

import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public class YangLibraryChangeNotificationListenerImpl implements YangLibraryChangeNotificationListener{

	private NotificationService m_notificationService;
	private SchemaRegistry m_schemaRegistry;
	private Executor m_yangLibraryChangeExecutor;
	public YangLibraryChangeNotificationListenerImpl(NotificationService notificationService, SchemaRegistry schemaRegistry, Executor yangLibraryChangeExecutor){
		m_notificationService = notificationService;
		m_schemaRegistry = schemaRegistry;
		m_yangLibraryChangeExecutor = yangLibraryChangeExecutor;
	}
	
	public void init() {
		m_schemaRegistry.registerYangLibraryChangeNotificationListener(this);
	}
	
	public void destroy() {
		m_schemaRegistry.unregisterYangLibraryChangeNotificationListener();
	}
	
	@Override
	public void sendYangLibraryChangeNotification(String moduleSetId) {
		YangLibraryChangeNotificationTask task = new YangLibraryChangeNotificationTask(m_notificationService, m_schemaRegistry, moduleSetId);
		m_yangLibraryChangeExecutor.execute(task);
	}

}
