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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.VALIDATION_NS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.VALIDATION_REVISION;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@YangList(name = "test-vno", namespace = VALIDATION_NS, revision = VALIDATION_REVISION)
@Entity
@IdClass(value = TestVnoPK.class)
public class TestVno implements Serializable {

	private static final long serialVersionUID = 1375760219441572061L;

	@Id
	@YangListKey
	@Column(unique = true)
	private String nameKey;

	@Id
	@YangParentId
	@Column(name = YangParentId.PARENT_ID_FIELD_NAME)
	private String parentId;

	@YangSchemaPath
	@Column(length = 1000)
	private String schemaPath;

	public TestVno() {
	}

	public String getNameKey() {
		return nameKey;
	}

	public void setNameKey(String nameKey) {
		this.nameKey = nameKey;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getSchemaPath() {
		return schemaPath;
	}

	public void setSchemaPath(String schemaPath) {
		this.schemaPath = schemaPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nameKey == null) ? 0 : nameKey.hashCode());
		result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
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
		TestVno other = (TestVno) obj;
		if (nameKey == null) {
			if (other.nameKey != null)
				return false;
		} else if (!nameKey.equals(other.nameKey))
			return false;
		if (parentId == null) {
			if (other.parentId != null)
				return false;
		} else if (!parentId.equals(other.parentId))
			return false;
		if (schemaPath == null) {
			if (other.schemaPath != null)
				return false;
		} else if (!schemaPath.equals(other.schemaPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TestVno [nameKey=" + nameKey + ", parentId=" + parentId + ", schemaPath=" + schemaPath + "]";
	}
}
