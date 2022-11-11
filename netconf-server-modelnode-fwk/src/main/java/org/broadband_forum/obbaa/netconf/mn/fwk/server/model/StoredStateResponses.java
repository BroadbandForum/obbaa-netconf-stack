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
import java.util.Map;

public class StoredStateResponses {
    Map<SubSystem, StoredLeaves> m_storedLeaves = new HashMap<>();
    Map<SubSystem, StoredFilters> m_storedFilters = new HashMap<>();

    public StoredLeaves getStoredLeaves(SubSystem subsystem) {
        StoredLeaves leavesForSs = m_storedLeaves.get(subsystem);
        if(leavesForSs == null){
            leavesForSs = new StoredLeaves();
            m_storedLeaves.put(subsystem, leavesForSs);
            return leavesForSs;
        }
        return leavesForSs;
    }

    public StoredFilters getStoredFilterNodes(SubSystem subsystem) {
        StoredFilters filtersForSs = m_storedFilters.get(subsystem);
        if(filtersForSs == null){
            filtersForSs = new StoredFilters();
            m_storedFilters.put(subsystem, filtersForSs);
            return filtersForSs;
        }
        return filtersForSs;
    }
}
