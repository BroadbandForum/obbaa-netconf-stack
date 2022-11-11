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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class LeafListContext {

    private Map<ChildLeafListHelper, StoredLeafLists> m_cachedLeafLists = new HashMap();

    public void populateExistingLeafListValues(ChildLeafListHelper configLeafListHelper, ModelNode modelNode) throws GetAttributeException {
        StoredLeafLists storedLeafLists = getStoredLeafLists(configLeafListHelper);
        storedLeafLists.populateExistingLeafListValues(configLeafListHelper, modelNode);
    }

    public void addLeafListPresentInEditNode(ChildLeafListHelper configLeafListHelper, String newValue) {
        StoredLeafLists storedLeafLists = getStoredLeafLists(configLeafListHelper);
        storedLeafLists.addLeafListPresentInEditNode(newValue);
    }

    public Collection<ConfigLeafAttribute> getExistingLeafLists(ChildLeafListHelper configLeafListHelper) {
        StoredLeafLists storedLeafLists = getStoredLeafLists(configLeafListHelper);
        return storedLeafLists.getExistingLeafLists();
    }

    public boolean checkIfAnyLeafListsToBeDeleted() {
        if (!m_cachedLeafLists.isEmpty()) {
            for (Map.Entry<ChildLeafListHelper, StoredLeafLists> entry : m_cachedLeafLists.entrySet()) {
                StoredLeafLists storedLeafLists = entry.getValue();
                if(checkIfExistingLeafListsToBeDeleted(storedLeafLists)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfExistingLeafListsToBeDeleted(StoredLeafLists storedLeafLists){
        List<ConfigLeafAttribute> existingLeafLists = storedLeafLists.getExistingLeafLists();
        List<String> newValues = storedLeafLists.getCurrentLeafLists();
        for (ConfigLeafAttribute existingLeafList : existingLeafLists) {
            if(!newValues.contains(existingLeafList.getStringValue())){
                return true;
            }
        }
        return  false;
    }

    private StoredLeafLists getStoredLeafLists(ChildLeafListHelper configLeafListHelper) {
        StoredLeafLists storedLeafLists = m_cachedLeafLists.get(configLeafListHelper);
        if(storedLeafLists == null){
            storedLeafLists = new StoredLeafLists();
            m_cachedLeafLists.put(configLeafListHelper, storedLeafLists);
        }
        return storedLeafLists;
    }
}
