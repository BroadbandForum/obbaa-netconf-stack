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

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;

public class EditChangeNode implements EditLeafNode {
    private EditChangeSource m_changeSource = EditChangeSource.user;
    private String m_name;
    private final ConfigLeafAttribute m_value;
    private String m_namespace;
    private QName m_qname;
    private InsertOperation m_insertOperation;
    private String m_operation = EditConfigOperations.MERGE;
    private boolean m_identityRefNode = false;


    public EditChangeNode(QName qname, ConfigLeafAttribute nodeValue) {
        this.m_qname = qname;
        this.m_name = m_qname.getLocalName();
        this.m_namespace = m_qname.getNamespace().toString();
        this.m_value = nodeValue;
    }

    public EditChangeNode(EditChangeNode thatChangeNode) {
        this.m_qname = thatChangeNode.getQName();
        this.m_name = m_qname.getLocalName();
        this.m_namespace = m_qname.getNamespace().toString();
        this.m_value = thatChangeNode.getConfigLeafAttribute();
        this.m_changeSource = thatChangeNode.getChangeSource();
        this.m_operation = thatChangeNode.getOperation();
        this.m_insertOperation = thatChangeNode.getInsertOperation();
        this.m_identityRefNode = thatChangeNode.isIdentityRefNode();
    }

    /* (non-Javadoc)
     * @see EditLeafNode#getName()
     */
    @Override
    public String getName() {
        return m_name;
    }

    /* (non-Javadoc)
     * @see EditLeafNode#getValue()
     */
    @Override
    public String getValue() {
        return m_value.getStringValue();
    }

    @Override
    public ConfigLeafAttribute getConfigLeafAttribute() {
        return m_value;
    }

    /* (non-Javadoc)
     * @see EditLeafNode#getNamespace()
     */
    @Override
    public String getNamespace() {
        return m_namespace;
    }

    /* (non-Javadoc)
     * @see EditLeafNode#getQName()
     */
    @Override
    public QName getQName() {
        return m_qname;
    }

    @Override
    public void setQName(QName qname) {
        m_qname = qname;
        m_name = m_qname.getLocalName();
        m_namespace = m_qname.getNamespace().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (m_identityRefNode ? 1231 : 1237);
        result = prime * result + ((m_insertOperation == null) ? 0 : m_insertOperation.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_namespace == null) ? 0 : m_namespace.hashCode());
        result = prime * result + ((m_operation == null) ? 0 : m_operation.hashCode());
        result = prime * result + ((m_qname == null) ? 0 : m_qname.hashCode());
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
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
        EditChangeNode other = (EditChangeNode) obj;
        if (m_identityRefNode != other.m_identityRefNode)
            return false;
        if (m_insertOperation != other.m_insertOperation)
            return false;
        if (m_name == null) {
            if (other.m_name != null)
                return false;
        } else if (!m_name.equals(other.m_name))
            return false;
        if (m_namespace == null) {
            if (other.m_namespace != null)
                return false;
        } else if (!m_namespace.equals(other.m_namespace))
            return false;
        if (m_operation == null) {
            if (other.m_operation != null)
                return false;
        } else if (!m_operation.equals(other.m_operation))
            return false;
        if (m_qname == null) {
            if (other.m_qname != null)
                return false;
        } else if (!m_qname.equals(other.m_qname))
            return false;
        if (m_value == null) {
            if (other.m_value != null)
                return false;
        } else if (!m_value.equals(other.m_value))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean printChangeSource) {
        String value = (m_value == null ? null : (m_value.getStringValue() == null ? null : m_value.getStringValue()));
        if (printChangeSource) {
            return "Change [" + m_operation + "," + m_name + "," + value + "," + m_namespace + "," + m_changeSource +
                    "]";
        }
        return "Change [" + m_operation + "," + m_name + "," + value + "," + m_namespace + "]";
    }

    public InsertOperation getInsertOperation() {
        return m_insertOperation;
    }

    public void setInsertOperation(InsertOperation insertOperation) {
        this.m_insertOperation = insertOperation;
    }

    public String getOperation() {
        return m_operation;
    }

    public void setOperation(String operation) {
        if (operation == null) {
            operation = EditConfigOperations.MERGE;
        }
        this.m_operation = operation;
    }

    public boolean isIdentityRefNode() {
        return m_identityRefNode;
    }

    public void setIdentityRefNode(boolean value) {
        m_identityRefNode = value;
    }

    public EditChangeSource getChangeSource() {
        return m_changeSource;
    }

    public void setChangeSource(EditChangeSource changeSource) {
        m_changeSource = changeSource;
    }
}
