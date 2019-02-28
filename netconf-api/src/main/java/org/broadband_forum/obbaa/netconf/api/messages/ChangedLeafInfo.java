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

public class ChangedLeafInfo {
    
    private String m_name;
    private String m_changedValue;
    private String m_namespace;
    private String m_prefix;
    
    public ChangedLeafInfo() {
        
    }
    
    public ChangedLeafInfo(String name, String changedValue, String namespace, String prefix) {
        super();
        this.m_name = name;
        this.m_changedValue = changedValue;
        this.m_namespace = namespace;
        this.m_prefix = prefix;
    }
    
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        this.m_name = name;
    }
    public String getChangedValue() {
        return m_changedValue;
    }
    public void setChangedValue(String changedValue) {
        this.m_changedValue = changedValue;
    }
    public String getNamespace() {
        return m_namespace;
    }
    public void setNamespace(String namespace) {
        this.m_namespace = namespace;
    }
    public String getPrefix() {
        return m_prefix;
    }
    public void setPrefix(String prefix) {
        this.m_prefix = prefix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_changedValue == null) ? 0 : m_changedValue.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_namespace == null) ? 0 : m_namespace.hashCode());
        result = prime * result + ((m_prefix == null) ? 0 : m_prefix.hashCode());
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
        ChangedLeafInfo other = (ChangedLeafInfo) obj;
        if (m_changedValue == null) {
            if (other.m_changedValue != null)
                return false;
        } else if (!m_changedValue.equals(other.m_changedValue))
            return false;
        if (m_name == null) {
            if (other.m_name != null)
                return false;
        } else if (!m_name.equals(other.m_name))
            return false;
        if (m_namespace == null) {
            if (other.m_namespace != null)
                return false;
        } else if (!m_namespace.equals(other.m_namespace))
            return false;
        if (m_prefix == null) {
            if (other.m_prefix != null)
                return false;
        } else if (!m_prefix.equals(other.m_prefix))
            return false;
        return true;
    }
    
}
