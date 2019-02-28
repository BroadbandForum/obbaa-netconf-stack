package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener;

import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.YangLibraryChangeNotification;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.Module;

public class YangLibraryChangeNotificationTask implements Runnable{
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(YangLibraryChangeNotificationTask.class, LogAppNames.NETCONF_STACK);

	private NotificationService m_notificationService;
	private SchemaRegistry m_schemaRegistry;
	private String m_moduleSetId;

	public YangLibraryChangeNotificationTask(NotificationService notificationService, SchemaRegistry schemaRegistry, String moduleSetId){
		m_notificationService = notificationService;
		m_schemaRegistry = schemaRegistry;
		m_moduleSetId = moduleSetId;
	}

	@Override
	public void run() {
		try {
			Module module = m_schemaRegistry.getModuleByNamespace(YangLibraryChangeNotification.IETF_YANG_LIBRARY_NS);
			if(module != null){
				YangLibraryChangeNotification yangLibraryNotification = new YangLibraryChangeNotification(module.getPrefix(), m_moduleSetId);
				//raise notification
				m_notificationService.sendNotification("SYSTEM", yangLibraryNotification);
			}
		} catch(Exception e){
			LOGGER.error("Error occured while sending yangLibrary notification ", e);
			throw e;
		}	
	}

}
