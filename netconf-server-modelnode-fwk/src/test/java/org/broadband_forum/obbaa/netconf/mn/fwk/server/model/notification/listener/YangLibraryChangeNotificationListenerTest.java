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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Executor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListenerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationTask;

public class YangLibraryChangeNotificationListenerTest {
    private NotificationService m_notificationService;
    private SchemaRegistry m_schemaRegistry;
    private YangLibraryChangeNotificationListenerImpl m_listener;
    private Executor m_executor;
    
 @Before
 public void setUp() throws SchemaBuildException {
    m_notificationService = mock(NotificationService.class);
    m_schemaRegistry = mock(SchemaRegistry.class);
    m_executor = mock(Executor.class);
    m_listener = new YangLibraryChangeNotificationListenerImpl(m_notificationService, m_schemaRegistry, m_executor);
    m_schemaRegistry.registerYangLibraryChangeNotificationListener(m_listener);
 }
 
 @After
 public void destroy() throws SchemaBuildException {
    m_schemaRegistry.unregisterYangLibraryChangeNotificationListener();
 }
 
 @Test
 public void testSendYangLibraryChangeNotification() throws Exception{
	 m_listener.sendYangLibraryChangeNotification("aabbccdd");
	 verify(m_executor).execute(any(YangLibraryChangeNotificationTask.class));
 }
}
