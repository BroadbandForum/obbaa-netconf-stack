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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.YangLibraryChangeNotification;
import org.opendaylight.yangtools.yang.model.api.Module;

import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class YangLibraryChangeNotificationTask implements Runnable {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(YangLibraryChangeNotificationTask.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    private NotificationService m_notificationService;
    private SchemaRegistry m_schemaRegistry;
    private String m_moduleSetId;

    public YangLibraryChangeNotificationTask(NotificationService notificationService, SchemaRegistry schemaRegistry,
                                             String moduleSetId) {
        m_notificationService = notificationService;
        m_schemaRegistry = schemaRegistry;
        m_moduleSetId = moduleSetId;
    }

    @Override
    public void run() {
        try {
            Module module = m_schemaRegistry.getModuleByNamespace(YangLibraryChangeNotification.IETF_YANG_LIBRARY_NS);
            if (module != null) {
                YangLibraryChangeNotification yangLibraryNotification = new YangLibraryChangeNotification(module
                        .getPrefix(), m_moduleSetId);
                //raise notification
                m_notificationService.sendNotification("SYSTEM", yangLibraryNotification);
            }
        } catch (Exception e) {
            LOGGER.error("Error occured while sending yangLibrary notification ", e);
            throw e;
        }
    }

}
