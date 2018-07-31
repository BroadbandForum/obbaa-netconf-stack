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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;

/**
 * This class is responsible for handling the netconf notifications which is happening in DataStore.java before
 * pushing them to NBI client
 * e.g. classify the notification by the device id if they are related to device configurations changes.
 */

public interface NbiNotificationHelper {

    /**
     * If a specific sub system want to classify the notifications which relate to its domain with a specific logic,
     * it can register by this method
     * See DeviceSubsystem.java for reference
     */

    public void registerClassifier(SubSystem subsytem,
                                   SubsystemNotificationClassifier classifier);

    /**
     * It builds the netconf notifications from the internal notifications which are sent to subsystems,
     * if a subsystem has registered a mechanism to classify the notifications it will called here.
     */

    public List<Notification> getNetconfConfigChangeNotifications(
            Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap,
            NetconfClientInfo clientInfo, NamespaceContext namespaceContext);
}
