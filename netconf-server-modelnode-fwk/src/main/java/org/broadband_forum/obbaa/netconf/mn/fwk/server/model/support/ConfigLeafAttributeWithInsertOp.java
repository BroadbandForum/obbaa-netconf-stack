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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;

public class ConfigLeafAttributeWithInsertOp {

    private InsertOperation m_insertOperation;
    private ConfigLeafAttribute m_leafListAttribute;

    public ConfigLeafAttributeWithInsertOp(ConfigLeafAttribute configLeafAttribute) {
        m_leafListAttribute = configLeafAttribute;
    }

    public ConfigLeafAttributeWithInsertOp(ConfigLeafAttribute configLeafAttribute, InsertOperation insertOperation) {
        m_leafListAttribute = configLeafAttribute;
        m_insertOperation = insertOperation;
    }

    public ConfigLeafAttribute getLeafListAttribute() {
        return m_leafListAttribute;
    }

    public void setLeafListAttribute(ConfigLeafAttribute leafListAttribute) {
        m_leafListAttribute = leafListAttribute;
    }

    public InsertOperation getInsertOperation() {
        return m_insertOperation;
    }

    public void setInsertOperation(InsertOperation insertOperation) {
        m_insertOperation = insertOperation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ConfigLeafAttributeWithInsertOp that = (ConfigLeafAttributeWithInsertOp) o;

        if (!m_leafListAttribute.equals(that.m_leafListAttribute))
            return false;

        return m_insertOperation != null ? m_insertOperation.equals(that.m_insertOperation) : that.m_insertOperation == null;
    }

    @Override
    public int hashCode() {
        int result = m_leafListAttribute.hashCode();
        result = 31 * result + m_leafListAttribute.hashCode();
        result = 31 * result + (m_insertOperation != null ? m_insertOperation.hashCode() : 0);
        return result;
    }
}
