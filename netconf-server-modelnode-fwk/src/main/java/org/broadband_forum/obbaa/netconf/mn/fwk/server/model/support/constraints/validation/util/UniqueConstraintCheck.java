package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.TreeMap;

import org.opendaylight.yangtools.yang.common.QName;

public class UniqueConstraintCheck {

	public TreeMap<QName, String> m_attributes = new TreeMap<>();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		UniqueConstraintCheck other = (UniqueConstraintCheck) obj;
		if (m_attributes == null) {
			if (other.m_attributes != null)
				return false;
		} else if (!m_attributes.equals(other.m_attributes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UniqueConstraintCheck [m_attributes=" + m_attributes + "]";
	}
}
