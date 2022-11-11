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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.opendaylight.yangtools.yang.common.QName;

public class EditMatchNode implements EditLeafNode {

    private final ConfigLeafAttribute m_nodeValue;
    private String m_nodeName;
    private String m_namespace;
	private QName m_qname;
	private boolean m_identityRefNode = false;

    public EditMatchNode(QName qname, ConfigLeafAttribute nodeValue) {
    	this.m_qname = qname;
        this.m_nodeName = m_qname.getLocalName();
        this.m_namespace = m_qname.getNamespace().toString();
        this.m_nodeValue = nodeValue;
    }

	public EditMatchNode(EditMatchNode thatMatchNode) {
		this.m_qname = thatMatchNode.getQName();
		this.m_nodeName = m_qname.getLocalName();
		this.m_namespace = m_qname.getNamespace().toString();
		this.m_nodeValue = thatMatchNode.getConfigLeafAttribute();
		this.m_identityRefNode = thatMatchNode.isIdentityRefNode();
	}

	@Override
    public String toString() {
        return "Match ["+ m_nodeName +","+m_nodeValue.getStringValue()+","+m_namespace+"]";
    }

    @Override
	public String getValue() {
		return m_nodeValue.getStringValue();
	}

	@Override
	public ConfigLeafAttribute getConfigLeafAttribute(){
		return m_nodeValue;
	}

    @Override
	public String getName() {
		return m_nodeName;
	}
	
    @Override
	public String getNamespace() {
		return m_namespace;
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_namespace == null) ? 0 : m_namespace.hashCode());
		result = prime * result
				+ ((m_nodeName == null) ? 0 : m_nodeName.hashCode());
		result = prime * result
				+ ((m_nodeValue == null) ? 0 : m_nodeValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
		    return true;
		}
		if (obj == null){
		    return false;
		}
		if (getClass() != obj.getClass()){
		    return false;
		}
		EditMatchNode other = (EditMatchNode) obj;
		if (m_namespace == null) {
			if (other.m_namespace != null){
			    return false;
			}
		} else if (!m_namespace.equals(other.m_namespace)){
		    return false;
		}
		if (m_nodeName == null) {
			if (other.m_nodeName != null){
			    return false;
			}
		} else if (!m_nodeName.equals(other.m_nodeName)){
		    return false;
		}
		if (m_nodeValue == null) {
			if (other.m_nodeValue != null){
			    return false;
			}
		} else if (!m_nodeValue.equals(other.m_nodeValue)){
		    return false;
		}
		return true;
	}

    public QName getQName() {
        return m_qname;
    }

    @Override
    public void setQName(QName qname) {
        m_qname = qname;
        m_nodeName = m_qname.getLocalName();
        m_namespace = m_qname.getNamespace().toString();
    }
	
	public boolean isIdentityRefNode(){
        return m_identityRefNode;
    }
    
    public void setIdentityRefNode(boolean value){
        m_identityRefNode = value;
    }
}
