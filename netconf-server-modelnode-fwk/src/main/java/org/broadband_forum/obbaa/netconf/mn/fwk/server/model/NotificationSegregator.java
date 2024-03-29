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
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.ChangedByParams;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface NotificationSegregator {
    void classify(Map<Object, Set<ChangeNotification>> classifiedChangeNotifications, List<ChangeNotification> changeNotifications);

    List<Notification> segregateNotifications(ChangeTreeNode aggregatorCTN, Set<SchemaPath> segregatedSPs, ChangedByParams changedByParams);
}
