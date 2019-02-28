package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model;

import java.io.Serializable;

public class TestSlicePK implements Serializable {

	private static final long serialVersionUID = 2908328559499653771L;
	private String nameKey;
	private String parentId;

	public TestSlicePK() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nameKey == null) ? 0 : nameKey.hashCode());
		result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
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
		TestSlicePK other = (TestSlicePK) obj;
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
		return true;
	}

	@Override
	public String toString() {
		return "TestSlicePK [nameKey=" + nameKey + ", parentId=" + parentId + "]";
	}
}
