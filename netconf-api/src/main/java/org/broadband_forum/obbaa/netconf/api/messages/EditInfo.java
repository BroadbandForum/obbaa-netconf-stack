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

package org.broadband_forum.obbaa.netconf.api.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditInfo {

    protected String m_target;
    private String m_operation;
    protected boolean m_implied;
    protected Map<String, String> m_namespaceDeclareMap;
    protected List<ChangedLeafInfo> m_changedLeafInfos = new ArrayList<>();

    public String getTarget() {
        return m_target;
    }

    public EditInfo setTarget(String target) {
        this.m_target = target;
        return this;
    }

    public String getOperation() {
        return m_operation;
    }

    public EditInfo setOperation(String operation) {
        this.m_operation = operation;
        return this;
    }

    public Map<String, String> getNamespaceDeclareMap() {
        return m_namespaceDeclareMap;
    }

    public EditInfo setNamespaceDeclareMap(Map<String, String> map) {
        this.m_namespaceDeclareMap = map;
        return this;
    }

    public List<ChangedLeafInfo> getChangedLeafInfos() {
        return m_changedLeafInfos;
    }

    public void setChangedLeafInfos(List<ChangedLeafInfo> changedLeafInfos) {
        this.m_changedLeafInfos = changedLeafInfos;
    }

    public void setChangedLeafInfos(ChangedLeafInfo changedLeafInfo) {
        this.m_changedLeafInfos.add(changedLeafInfo);
    }

    public boolean isImplied() {
        return m_implied;
    }

    public void setImplied(boolean impled) {
        this.m_implied = impled;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_changedLeafInfos == null) ? 0 : m_changedLeafInfos.hashCode());
        result = prime * result + ((m_operation == null) ? 0 : m_operation.hashCode());
        result = prime * result + ((m_target == null) ? 0 : m_target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EditInfo other = (EditInfo) obj;
        if (m_changedLeafInfos == null) {
            if (other.m_changedLeafInfos != null)
                return false;
        } else if (!m_changedLeafInfos.equals(other.m_changedLeafInfos))
            return false;
        if (m_operation == null) {
            if (other.m_operation != null)
                return false;
        } else if (!m_operation.equals(other.m_operation))
            return false;
        if (m_target == null) {
            if (other.m_target != null)
                return false;
        } else if (!m_target.equals(other.m_target))
            return false;
        return true;
    }
}
