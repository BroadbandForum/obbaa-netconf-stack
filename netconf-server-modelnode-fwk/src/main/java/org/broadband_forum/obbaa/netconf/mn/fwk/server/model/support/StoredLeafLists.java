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

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class StoredLeafLists {

    private List<ConfigLeafAttribute> m_existingLeafLists = new ArrayList<>();
    private List<String> m_currentLeafLists = new ArrayList<>();

    public void populateExistingLeafListValues(ChildLeafListHelper configLeafListHelper, ModelNode modelNode) throws GetAttributeException {
        if(m_existingLeafLists.isEmpty()){ // This ensures we query DB for existing values only once for a given modelNode per leaf-list type
            m_existingLeafLists.addAll(configLeafListHelper.getValue(modelNode));
        }
    }

    public void addLeafListPresentInEditNode(String leafList) {
        m_currentLeafLists.add(leafList);
    }

    public List<ConfigLeafAttribute> getExistingLeafLists() {
        return m_existingLeafLists;
    }

    public List<String> getCurrentLeafLists() {
        return m_currentLeafLists;
    }
}
