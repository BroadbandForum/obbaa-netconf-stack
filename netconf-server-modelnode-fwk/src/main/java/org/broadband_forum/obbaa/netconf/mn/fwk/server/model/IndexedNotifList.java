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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.IndexedList;

public class IndexedNotifList extends IndexedList<ModelNodeId, ChangeNotification> {
    private Map<ModelNodeId, Set<ChangeNotification>> m_childNotifs = new HashMap<>();

    public void replaceChildNotifsWithParentNotif(ChangeNotification parentNotif) {
        add(parentNotif);
        Set<ChangeNotification> childNotifs = new HashSet<>(getChildNotifsForId(parentNotif.getKey()));
        for (ChangeNotification childNotif : childNotifs) {
            remove(childNotif.getKey());
        }
    }

    @Override
    public void add(ChangeNotification value) {
        super.add(value);
        addToChildIndexForAllParents(value);
    }

    private void addToChildIndexForAllParents(ChangeNotification value) {
        if (value.getKey() != null) {
            ModelNodeId nodeId = new ModelNodeId(value.getKey().getRdnsReadOnly());
            nodeId.removeLastRdn();
            while (!nodeId.getRdnsReadOnly().isEmpty()) {
                Set<ChangeNotification> childNotifs = getChildNotifsForId(nodeId);
                childNotifs.add(value);
                nodeId = new ModelNodeId(nodeId.getRdnsReadOnly());
                nodeId.removeLastRdn();
            }
        }
    }

    @Override
    public ChangeNotification remove(ModelNodeId key) {
        ChangeNotification removedValue = super.remove(key);
        removeFromChildIndexFromAllParents(removedValue);
        return removedValue;
    }

    private void removeFromChildIndexFromAllParents(ChangeNotification removedValue) {
        if (removedValue != null) {
            ModelNodeId nodeId = new ModelNodeId(removedValue.getKey());
            nodeId.removeLastRdn();
            while (!nodeId.getRdnsReadOnly().isEmpty()) {
                Set<ChangeNotification> childNotifs = getChildNotifsForId(nodeId);
                childNotifs.remove(removedValue);
                nodeId.removeLastRdn();
            }
        }
    }

    private Set<ChangeNotification> getChildNotifsForId(ModelNodeId nodeId) {
        Set<ChangeNotification> childNotifs = m_childNotifs.get(nodeId);
        if (childNotifs == null) {
            childNotifs = new HashSet<>();
            m_childNotifs.put(nodeId, childNotifs);
        }
        return childNotifs;
    }
}
