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

import java.util.concurrent.Executor;

import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public class YangLibraryChangeNotificationListenerImpl implements YangLibraryChangeNotificationListener {

    private NotificationService m_notificationService;
    private SchemaRegistry m_schemaRegistry;
    private Executor m_yangLibraryChangeExecutor;

    public YangLibraryChangeNotificationListenerImpl(NotificationService notificationService, SchemaRegistry
            schemaRegistry, Executor yangLibraryChangeExecutor) {
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
        YangLibraryChangeNotificationTask task = new YangLibraryChangeNotificationTask(m_notificationService,
                m_schemaRegistry, moduleSetId);
        m_yangLibraryChangeExecutor.execute(task);
    }

}
