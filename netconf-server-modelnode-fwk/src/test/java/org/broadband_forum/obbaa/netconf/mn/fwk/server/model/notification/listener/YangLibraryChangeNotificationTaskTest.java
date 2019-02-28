package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.YangLibraryChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationTask;

public class YangLibraryChangeNotificationTaskTest {

	private YangLibraryChangeNotificationTask m_task;
	private NotificationService m_notificationService;
	private SchemaRegistry m_schemaRegistry;
	private Module m_module;

	@Before
	public void setUp(){
		m_notificationService = mock(NotificationService.class);
		m_schemaRegistry = mock(SchemaRegistry.class);
		m_module = mock(Module.class);
		when(m_schemaRegistry.getModuleByNamespace(YangLibraryChangeNotification.IETF_YANG_LIBRARY_NS)).thenReturn(m_module);
		when(m_module.getPrefix()).thenReturn("yanglib");
		m_task = new YangLibraryChangeNotificationTask(m_notificationService, m_schemaRegistry, "aabbccdd");
	}

	@Test
	public void testSendNotification(){
		m_task.run();
		verify(m_notificationService).sendNotification(eq("SYSTEM"), any(YangLibraryChangeNotification.class));
	}
}
