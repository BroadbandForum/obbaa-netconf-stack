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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumwithleaflists;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.AttributeType;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttributeNS;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity(name = "albumwithleaflists_dummyIdRef")
@Table(name = "albumwithleaflists_dummyIdRef")
@IdClass(DummyIdRefLeafListPK.class)
@YangLeafList(name = "dummy-leaf-list-id-ref", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
public class DummyIdRefLeafList {
	@Id
	@Column
	@YangAttribute(name="dummy-leaf-list-id-ref", attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
	private String dummyLeafListIdRef;

	@YangAttributeNS(belongsToAttribute = "dummy-leaf-list-id-ref",
			attributeNamespace = JukeboxConstants.JB_NS, attributeRevision = JukeboxConstants.JB_REVISION)
	@Column
	private String dummyLeafListIdRefNs;
	
	@Id
	@YangParentId
	String parentId;
	
	@YangSchemaPath
	@Column(length = 1000)
	String schemaPath;

	@Column
	@YangOrderByUser
	private Integer insertOrder;
	
	public String getSchemaPath() {
		return schemaPath;
	}

	public void setSchemaPath(String schemaPath) {
		this.schemaPath = schemaPath;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	public String getDummyLeafListIdRef() {
		return dummyLeafListIdRef;
	}
	
	public void setDummyLeafListIdRef(String dummyLeafListIdRef) {
		this.dummyLeafListIdRef = dummyLeafListIdRef;
	}

	public String getDummyLeafListIdRefNs() {
		return dummyLeafListIdRefNs;
	}

	public void setDummyLeafListIdRefNs(String dummyLeafListIdRefNs) {
		this.dummyLeafListIdRefNs = dummyLeafListIdRefNs;
	}

	public Integer getInsertOrder() {
		return insertOrder;
	}

	public void setInsertOrder(Integer insertOrder) {
		this.insertOrder = insertOrder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((insertOrder == null) ? 0 : insertOrder.hashCode());
		result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
		result = prime * result + ((dummyLeafListIdRef == null) ? 0 : dummyLeafListIdRef.hashCode());
		result = prime * result + ((dummyLeafListIdRefNs == null) ? 0 : dummyLeafListIdRefNs.hashCode());
		result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
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
		DummyIdRefLeafList other = (DummyIdRefLeafList) obj;
		if (insertOrder == null) {
			if (other.insertOrder != null)
				return false;
		} else if (!insertOrder.equals(other.insertOrder))
			return false;
		if (parentId == null) {
			if (other.parentId != null)
				return false;
		} else if (!parentId.equals(other.parentId))
			return false;
		if (dummyLeafListIdRef == null) {
			if (other.dummyLeafListIdRef != null)
				return false;
		} else if (!dummyLeafListIdRef.equals(other.dummyLeafListIdRef))
			return false;
		if (dummyLeafListIdRefNs == null) {
			if (other.dummyLeafListIdRefNs != null)
				return false;
		} else if (!dummyLeafListIdRefNs.equals(other.dummyLeafListIdRefNs))
			return false;
		if (schemaPath == null) {
			if (other.schemaPath != null)
				return false;
		} else if (!schemaPath.equals(other.schemaPath))
			return false;
		return true;
	}
}
