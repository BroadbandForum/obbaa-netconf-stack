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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class TestSubsystem extends AbstractSubSystem {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(TestSubsystem.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");
    private List<ChangeNotification> m_lastChangeNotifications = new ArrayList<>();

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {
        clearNotifs();
        m_lastChangeNotifications.addAll(changeNotificationList);
    }

    public void clearNotifs() {
        m_lastChangeNotifications.clear();
    }

    public List<ChangeNotification> getLastChangeNotifications() {
        return m_lastChangeNotifications;
    }

    public void assertContainsNotification(String notifStr) {
        boolean notifFound = false;
        for (ChangeNotification notification : m_lastChangeNotifications) {
            if (notification.toString(true).equals(notifStr)) {
                notifFound = true;
                break;
            }
        }
        if (!notifFound) {
            LOGGER.error("Current Notifications {}", m_lastChangeNotifications);
            fail(String.format("Change notification %s not found ", notifStr));
        }
    }

    public void assertContainsNoNotification() {
        assertEquals(0, m_lastChangeNotifications.size());
    }
}
