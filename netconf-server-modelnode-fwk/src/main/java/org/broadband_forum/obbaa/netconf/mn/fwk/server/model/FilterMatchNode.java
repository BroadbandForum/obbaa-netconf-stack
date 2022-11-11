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

import org.opendaylight.yangtools.yang.common.QName;

public class FilterMatchNode extends AbstractFilterNode {

	private String m_filter;

	public FilterMatchNode(String nodeName, String namespace, String filter) {
	    super(nodeName,namespace);
		m_filter = filter;
	}

	public String getFilter() {
		return m_filter;
	}

    public void setFilter(String filter) {
        this.m_filter = filter;
    }

    @Override
	public String toString() {
	    if(m_attributes.isEmpty()){
            return "FilterMatchNode [" + m_nodeName + "=" + m_filter + ", namespace=" + m_namespace + "]";
        }else {
            return "FilterMatchNode [" + m_nodeName + "=" + m_filter + ", namespace=" + m_namespace + ", attributes=" + m_attributes + "]";
        }
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_filter == null) ? 0 : m_filter.hashCode());
        result = prime * result + ((m_namespace == null) ? 0 : m_namespace.hashCode());
        result = prime * result + ((m_nodeName == null) ? 0 : m_nodeName.hashCode());
        result = prime * result + ((m_attributes == null) ? 0 : m_attributes.hashCode());
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
        FilterMatchNode other = (FilterMatchNode) obj;
        if (m_filter == null) {
            if (other.m_filter != null)
                return false;
        } else if (!m_filter.equals(other.m_filter))
            return false;
        if (m_namespace == null) {
            if (other.m_namespace != null)
                return false;
        } else if (!m_namespace.equals(other.m_namespace))
            return false;
        if (m_nodeName == null) {
            if (other.m_nodeName != null)
                return false;
        } else if (!m_nodeName.equals(other.m_nodeName))
            return false;
        if (m_attributes == null) {
            if (other.m_attributes != null)
                return false;
        } else if (!m_attributes.equals(other.m_attributes))
            return false;
        return true;
    }


    public boolean isSameQName(QName qName) {
	    if(qName.getNamespace().toString().equals(m_namespace) && qName.getLocalName().equals(m_nodeName)){
	        return true;
        }
        return false;
    }
}
