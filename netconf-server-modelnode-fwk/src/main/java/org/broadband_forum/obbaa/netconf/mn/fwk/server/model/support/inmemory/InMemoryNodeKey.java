package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;

/**
 * Created by pgorai on 2/25/16.
 */
public class InMemoryNodeKey {
    private final ModelNodeKey m_key;
    private final ModelNodeId m_parentId;

    public InMemoryNodeKey(ModelNodeKey key, ModelNodeId parentId) {
        this.m_key = key;
        this.m_parentId = parentId;
    }

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InMemoryNodeKey that = (InMemoryNodeKey) o;

        if (m_key != null ? !m_key.equals(that.m_key) : that.m_key != null) {
            return false;
        }
        return m_parentId != null ? m_parentId.equals(that.m_parentId) : that.m_parentId == null;

    }

    @Override
    public int hashCode() {
        int result = m_key != null ? m_key.hashCode() : 0;
        result = 31 * result + (m_parentId != null ? m_parentId.hashCode() : 0);
        return result;
    }
    
    @Override
	public String toString() {
		return "InMemoryNodeKey [key=" + m_key + ", parentId=" + m_parentId + "]";
	}
}
