package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.Collection;

import org.opendaylight.yangtools.yang.common.QName;

public class SchemaMountKey {

	private QName m_nodeQName;
	private Collection<String> m_leafNames;
	
	public SchemaMountKey(QName nodeQName, Collection<String> leafNames) {
		this.m_nodeQName = nodeQName;
		this.m_leafNames = leafNames;
	}
	
	public QName getNodeQName() {
		return m_nodeQName;
	}

	public Collection<String> getLeafNames() {
		return m_leafNames;
	}
}
