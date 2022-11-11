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


public class ModelNodeChange {
    ModelNodeChangeType m_changeType;
    EditContainmentNode m_changeData;
   
    public ModelNodeChange(ModelNodeChangeType changeType, EditContainmentNode changeNode) {
        m_changeType = changeType;
        m_changeData = changeNode;
    }
    public ModelNodeChangeType getChangeType() {
        return this.m_changeType;
    }
    public ModelNodeChange setChangeType(ModelNodeChangeType changeType) {
        this.m_changeType = changeType;
        return this;
    }
    public EditContainmentNode getChangeData() {
        return this.m_changeData;
    }
    public ModelNodeChange setChangeData(EditContainmentNode changeData) {
        this.m_changeData = changeData;
        return this;
    }
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_changeData == null) ? 0 : m_changeData.hashCode());
		result = prime * result
				+ ((m_changeType == null) ? 0 : m_changeType.hashCode());
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
		ModelNodeChange other = (ModelNodeChange) obj;
		if (m_changeData == null) {
			if (other.m_changeData != null){
			    return false;
			}
		} else if (!m_changeData.equals(other.m_changeData)){
		    return false;
		}
		if (m_changeType != other.m_changeType){
		    return false;
		}
		return true;
	}
	@Override
    public String toString() {
        return toString(false);
    }

	public String toString(boolean printChangeSource) {
		return "ModelNodeChange [m_changeType=" + this.m_changeType + ", m_changeData=" + this.m_changeData.toString(0, printChangeSource) + "]";
	}
}
