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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.opendaylight.yangtools.yang.common.QName;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.StringUtil;

public class EditContainmentNode {
    private EditChangeSource m_changeSource = EditChangeSource.user;
    private static final String LF = System.getProperty("line.separator");
    /**
     * To guarentee order in which match nodes/keys are added.
     * They are picked from ModelNodeHelperRegistry.getNaturalKeyHelper,
     * which maintains a TreeMap. So all users will have the same order
     */
    private List<EditMatchNode> m_matchNodes = new LinkedList<EditMatchNode>();
    private List<EditChangeNode> m_changeNodes = new ArrayList<EditChangeNode>();
    private List<EditContainmentNode> m_childNodes = new ArrayList<EditContainmentNode>();
    private List<QName> m_disabledDefaultCreationNodes = new ArrayList<>();

    private String m_name;
    private String m_namespace;

    private String m_editOperation = EditConfigDefaultOperations.MERGE;
    private QName m_qname;

    private InsertOperation m_insertOperation; //for list node

    private EditContainmentNode m_parent;

    private ModelNodeId m_modelNodeId;

    public EditContainmentNode(QName qname, String editOperation) {
        this.m_qname = qname;
        this.m_name = m_qname.getLocalName();
        this.m_namespace = m_qname.getNamespace().toString();
        this.m_editOperation = editOperation;
    }

    public EditContainmentNode(EditContainmentNode that) {
        this.m_name = that.getName();
        this.m_namespace = that.getNamespace();
        this.m_qname = that.getQName();
        this.m_editOperation = that.getEditOperation();
        this.m_changeSource = that.getChangeSource();
        this.m_disabledDefaultCreationNodes = that.getDisabledDefaultCreationNodes();

        for (EditMatchNode thatMatchNode : that.getMatchNodes()) {
            m_matchNodes.add(new EditMatchNode(thatMatchNode));
        }

        for (EditChangeNode thatChangeNode : that.getChangeNodes()) {
            m_changeNodes.add(new EditChangeNode(thatChangeNode));
        }
        for (EditContainmentNode thatChildNode : that.getChildren()) {
            EditContainmentNode childNode = new EditContainmentNode(thatChildNode);
            m_childNodes.add(childNode);
            childNode.setParent(this);
        }
        this.m_insertOperation = that.getInsertOperation();
    }

    public EditContainmentNode() {

    }

    public EditContainmentNode addChild(EditContainmentNode childNode) {
        if (childNode != null) {
            this.m_childNodes.add(childNode);
            childNode.setParent(this);
        }
        return this;
    }

    public EditContainmentNode removeChild(EditContainmentNode childNode) {
        if (childNode != null) {
            this.m_childNodes.remove(childNode);
            childNode.setParent(null);
        }
        return this;
    }

    public EditMatchNode getEditMatchNode(String nodeName, String namespace) {
        for (EditMatchNode matchNode : m_matchNodes) {
            if (nodeName.equals(matchNode.getName()) && (namespace == null || namespace.equals(matchNode.getNamespace
                    ()))) {
                return matchNode;
            }
        }
        return null;
    }

    public EditChangeNode getEditChangeNode(String nodeName, String namespace) {
        for (EditChangeNode changeNode : m_changeNodes) {
            if (nodeName.equals(changeNode.getName()) && (namespace == null || namespace.equals(changeNode
                    .getNamespace()))) {
                return changeNode;
            }
        }
        return null;
    }

    public List<EditContainmentNode> getChildren() {
        return m_childNodes;
    }

    public String getName() {
        if (m_name == null) {
            m_name = m_qname.getLocalName();
        }
        return m_name;
    }

    public String getNamespace() {
        if (m_namespace == null) {
            m_namespace = m_qname.getNamespace().toString();
        }
        return m_namespace;
    }

    public QName getQName() {
        return m_qname;
    }

    public void setQName(QName qname) {
        m_qname = qname;
        m_namespace = m_qname.getNamespace().toString();
        m_name = m_qname.getLocalName();
    }

    public EditContainmentNode setEditOperation(String editOperation) {
        this.m_editOperation = editOperation;
        return this;
    }

    public String getEditOperation() {
        return m_editOperation;
    }

	/*
     * public boolean isLeaf(){ return (m_childNodes.size() == 0); }
	 * 
	 * public List<EditConfigNode> getLeavesUnderNode() { List<EditConfigNode> leaves = new ArrayList<EditConfigNode>
	 *     (); for(EditConfigNode
	 * child : m_childNodes){ if(child.isLeaf()){ leaves.add(child); } } return leaves; }
	 */

    public EditContainmentNode addContainmentNode(QName qname, String editOperation) {
        EditContainmentNode node = new EditContainmentNode(qname, editOperation);
        m_childNodes.add(node);
        node.setParent(this);
        return node;
    }

    public List<EditChangeNode> getChangeNodes() {
        return m_changeNodes;
    }

    public EditChangeNode getChangeNode(QName qName) {
        for (EditChangeNode changeNode : m_changeNodes) {
            if (changeNode.getQName().equals(qName)) {
                return changeNode;
            }
        }
        return null;
    }

    public List<EditMatchNode> getMatchNodes() {
        return m_matchNodes;
    }

    public EditContainmentNode addMatchNode(QName qname, ConfigLeafAttribute nodeValue) {
        EditMatchNode node = new EditMatchNode(qname, nodeValue);
        this.m_matchNodes.add(node);
        addRdn(new ModelNodeRdn(qname, nodeValue.getStringValue()));
        return this;
    }

    public EditContainmentNode addChangeNode(QName qname, ConfigLeafAttribute nodeValue) {
        return addChangeNode(qname, nodeValue, EditChangeSource.user);
    }

    public EditContainmentNode addChangeNode(QName qname, ConfigLeafAttribute nodeValue, EditChangeSource
            changeSource) {
        EditChangeNode node = new EditChangeNode(qname, nodeValue);
        node.setChangeSource(changeSource);
        this.m_changeNodes.add(node);
        return this;
    }

    public EditContainmentNode addChangeNode(EditChangeNode node) {
        this.m_changeNodes.add(node);
        return this;

    }

    public EditContainmentNode removeChangeNode(EditChangeNode node) {
        this.m_changeNodes.remove(node);
        return this;

    }

    public EditContainmentNode removeMatchNode(EditMatchNode node) {
        this.m_matchNodes.remove(node);
        reBuildModelNodeId();
        return this;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_editOperation == null) ? 0 : m_editOperation.hashCode());
        result = prime * result
                + ((m_changeNodes == null) ? 0 : m_changeNodes.hashCode());
        result = prime * result
                + ((m_childNodes == null) ? 0 : m_childNodes.hashCode());
        result = prime * result
                + ((m_matchNodes == null) ? 0 : m_matchNodes.hashCode());
        result = prime * result + ((m_qname == null) ? 0 : m_qname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EditContainmentNode other = (EditContainmentNode) obj;
        if (m_editOperation == null) {
            if (other.m_editOperation != null) {
                return false;
            }
        } else if (!m_editOperation.equals(other.m_editOperation)) {
            return false;
        }
        if (m_changeNodes == null) {
            if (other.m_changeNodes != null) {
                return false;
            }
        } else if (!m_changeNodes.equals(other.m_changeNodes)) {
            return false;
        }
        if (m_childNodes == null) {
            if (other.m_childNodes != null) {
                return false;
            }
        } else if (!m_childNodes.equals(other.m_childNodes)) {
            return false;
        }
        if (m_matchNodes == null) {
            if (other.m_matchNodes != null) {
                return false;
            }
        } else if (!m_matchNodes.equals(other.m_matchNodes)) {
            return false;
        }
        if (m_qname == null) {
            if (other.m_qname != null) {
                return false;
            }
        } else if (!m_qname.equals(other.m_qname)) {
            return false;
        }
        if (m_insertOperation == null) {
            if (other.m_insertOperation != null) {
                return false;
            }
        } else if (!m_insertOperation.equals(other.m_insertOperation)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent, boolean printChangeSource) {
        StringBuilder sb = new StringBuilder();
        String filler = StringUtil.blanks(indent);

        if (printChangeSource) {
            sb.append(filler).append("Containment [" + m_editOperation + "," + getName() + "," + getNamespace() + ","
                    + getChangeSource() +
                    "]" + LF);
        } else {
            sb.append(filler).append("Containment [" + m_editOperation + "," + getName() + "," + getNamespace() + "]"
                    + LF);
        }

        for (EditMatchNode matchNode : m_matchNodes) {
            sb.append(filler).append(" ").append(matchNode.toString()).append(LF);
        }
        for (EditChangeNode changeNode : m_changeNodes) {
            sb.append(filler).append(" ").append(changeNode.toString(printChangeSource)).append(LF);
        }
        for (EditContainmentNode configNode : m_childNodes) {
            sb.append(configNode.toString(indent + 1, printChangeSource));
        }
        return sb.toString();
    }

    private String toString(int indent) {
        return toString(indent, false);
    }

    public EditContainmentNode addMatchNode(EditMatchNode artistMatchNode) {
        this.m_matchNodes.add(artistMatchNode);
        addRdn(new ModelNodeRdn(artistMatchNode.getQName(), artistMatchNode.getValue()));
        return this;
    }

    public EditContainmentNode getChildNode(QName qName) {
        for (EditContainmentNode child : m_childNodes) {
            if (child.getQName().equals(qName)) {
                return child;
            }
        }
        return null;
    }

    public InsertOperation getInsertOperation() {
        return m_insertOperation;
    }

    public void setInsertOperation(InsertOperation insertOperation) {
        this.m_insertOperation = insertOperation;
    }

    public EditContainmentNode getParent() {
        return m_parent;
    }

    public void setParent(EditContainmentNode parent) {
        this.m_parent = parent;
        reBuildModelNodeId();
    }

    public EditChangeSource getChangeSource() {
        return m_changeSource;
    }

    public void setChangeSource(EditChangeSource changeSource) {
        m_changeSource = changeSource;
    }

    public void addDisabledDefaultCreationNode(QName qName) {
        this.m_disabledDefaultCreationNodes.add(qName);
    }

    public List<QName> getDisabledDefaultCreationNodes() {
        return m_disabledDefaultCreationNodes;
    }

    public void setDisabledDefaultCreationNodes(List<QName> disabledDefaultCreationNodes) {
        this.m_disabledDefaultCreationNodes = disabledDefaultCreationNodes;
    }

    public ModelNodeId getModelNodeId() {
        buildModelNodeId();
        return m_modelNodeId;
    }

    private void reBuildModelNodeId() {
        m_modelNodeId = null;
        buildModelNodeId();
        for (EditMatchNode editMatchNode : getMatchNodes()) {
            addRdn(new ModelNodeRdn(editMatchNode.getQName(), editMatchNode.getValue()));
        }
    }

    private void buildModelNodeId() {
        if (m_modelNodeId == null) {
            ModelNodeId parentId = null;
            if (getParent() != null) {
                parentId = getParent().getModelNodeId();
            }
            m_modelNodeId = new ModelNodeId(parentId);
            ModelNodeRdn thisRdn = new ModelNodeRdn(ModelNodeRdn.CONTAINER, getNamespace(), getName());
            m_modelNodeId.addRdn(thisRdn);
        }
    }

    private void addRdn(ModelNodeRdn rdn) {
        buildModelNodeId();
        m_modelNodeId.addRdn(rdn);
    }

    public static void setParentForEditContainmentNode(EditContainmentNode node, EditContainmentNode parent) {
        node.setParent(parent);
        for (EditContainmentNode child : node.getChildren()) {
            setParentForEditContainmentNode(child, node);
        }
    }
}
