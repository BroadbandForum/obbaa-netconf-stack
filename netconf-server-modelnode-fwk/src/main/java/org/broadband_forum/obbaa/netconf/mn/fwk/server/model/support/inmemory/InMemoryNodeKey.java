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
