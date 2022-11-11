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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.CTNNbiNotificationUtil.buildListNotification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.ChangedByParams;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DefaultNotificationSegregator implements NotificationSegregator {

    @Override
    public void classify(Map<Object, Set<ChangeNotification>> classifiedChangeNotifications, List<ChangeNotification> changeNotifications) {

    }

    @Override
    public List<Notification> segregateNotifications(ChangeTreeNode aggregatorCTN, Set<SchemaPath> segregatedSPs, ChangedByParams changedByParams) {
        List<Notification> segregatedNotifications = new ArrayList<>();
        Set<ChangeTreeNode> ctns = new LinkedHashSet<>();
        Set<SchemaPath> changedSPs = aggregatorCTN.getChangedNodeTypes();
        Set<SchemaPath> newChangedSPs = new HashSet<>(changedSPs);
        newChangedSPs.removeAll(segregatedSPs);
        for(SchemaPath schemaPath : newChangedSPs) {
            Collection<ChangeTreeNode> changeTreeNodes = aggregatorCTN.getNodesOfType(schemaPath);
            ctns.addAll(changeTreeNodes);
            segregatedSPs.add(schemaPath);
        }
        List<Notification> notifs = buildListNotification(ctns, changedByParams);
        segregatedNotifications.addAll(notifs);
        return segregatedNotifications;
    }
}
