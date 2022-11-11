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

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.StringUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Node;

public class EditContainmentNode implements IndexedList.IndexableListEntry<ModelNodeId> {
    public static final String DUPLICATE_ELEMENTS_FOUND = "Duplicate elements in node ";
    public static final String DATA_NOT_UNIQUE = "data-not-unique";
    private EditChangeSource m_changeSource = EditChangeSource.user;
    private static final String LF = System.getProperty("line.separator");
    /**
     * To guarentee order in which match nodes/keys are added.
     * They are picked from ModelNodeHelperRegistry.getNaturalKeyHelper,
     * which maintains a TreeMap. So all users will have the same order
     */
    private List<EditMatchNode> m_matchNodes = new LinkedList<EditMatchNode>();
    private List<EditChangeNode> m_changeNodes = new ArrayList<EditChangeNode>();
    private IndexedList<ModelNodeId, EditContainmentNode> m_childNodes = new IndexedList<>();
    private List<QName> m_disabledDefaultCreationNodes = new ArrayList<>();

    private String m_name;
    private String m_namespace;

    private String m_editOperation = EditConfigDefaultOperations.MERGE;
    private QName m_qname;
    private SchemaPath m_schemaPath;

    private InsertOperation m_insertOperation; //for list node

    private EditContainmentNode m_parent;

    private ModelNodeId m_modelNodeId;
    
    private SchemaRegistry m_schemaRegistry;
    private boolean m_hasChanged;
    private Node m_domValue;
    private Boolean m_visibility = true;

    public Boolean isVisible() {
        return m_visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.m_visibility = visibility;
    }

    public EditContainmentNode(SchemaPath schemaPath, String editOperation, SchemaRegistry schemaRegistry, Node domValue) {
        this.m_schemaPath = schemaPath;
        this.m_qname = schemaPath.getLastComponent();
        this.m_domValue = domValue;
        this.m_name = m_qname.getLocalName();
        this.m_namespace = m_qname.getNamespace().toString();
        this.m_editOperation = editOperation;
        m_schemaRegistry = schemaRegistry;
    }

    public EditContainmentNode(EditContainmentNode that) {
        this.m_name = that.getName();
        this.m_namespace = that.getNamespace();
        this.m_schemaPath = that.getSchemaPath();
        this.m_qname = that.getQName();
        this.m_editOperation = that.getEditOperation();
        this.m_changeSource = that.getChangeSource();
        this.m_visibility = that.isVisible();
        this.m_disabledDefaultCreationNodes = that.getDisabledDefaultCreationNodes();
        m_schemaRegistry = that.getSchemaRegistry();
        this.m_parent = that.getParent();
        this.m_modelNodeId = that.getModelNodeId();

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
        this.m_domValue = that.getDomValue();
    }

    public EditContainmentNode() {

    }

    public EditContainmentNode addChild(EditContainmentNode childNode) {
        if (childNode != null) {
            validateChildNode(childNode);
            this.m_childNodes.add(childNode);
            childNode.setParent(this);
        }
        return this;
    }

    private void validateChildNode(EditContainmentNode childNode) {
        if (m_childNodes.map().containsKey(childNode.getKey()) && m_childNodes.map().get(childNode.getKey()).getNamespace().equals(childNode.getNamespace())) {
            throwDuplicateNodesEditConfigException(childNode.getModelNodeId().xPathString(), childNode.getQName());
        }
    }

    public static void throwDuplicateNodesEditConfigException(String errorPath, QName qName) {
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                DUPLICATE_ELEMENTS_FOUND + qName);
        rpcError.setErrorAppTag(DATA_NOT_UNIQUE);
        rpcError.setErrorPath(errorPath, null);
        throw new EditConfigException(rpcError);
    }
    public EditContainmentNode removeChild(EditContainmentNode childNode) {
        if (childNode != null) {
            this.m_childNodes.remove(childNode.getModelNodeId());
            childNode.setParent(null);
        }
        return this;
    }

    public EditMatchNode getEditMatchNode(String nodeName, String namespace) {
        for (EditMatchNode matchNode : m_matchNodes) {
            if (nodeName.equals(matchNode.getName()) && (namespace == null || namespace.equals(matchNode.getNamespace()))) {
                return matchNode;
            }
        }
        return null;
    }

    public EditChangeNode getEditChangeNode(String nodeName, String namespace) {
        for (EditChangeNode changeNode : m_changeNodes) {
            if (nodeName.equals(changeNode.getName()) && (namespace == null || namespace.equals(changeNode.getNamespace()))) {
                return changeNode;
            }
        }
        return null;
    }

    public List<EditContainmentNode> getChildren() {
        return m_childNodes.list();
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

    public SchemaPath getSchemaPath() {
        return m_schemaPath;
    }

    public void setSchemaPath(SchemaPath schemaPath) {
        this.m_schemaPath = schemaPath;
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
     * public List<EditConfigNode> getLeavesUnderNode() { List<EditConfigNode> leaves = new ArrayList<EditConfigNode>(); for(EditConfigNode
     * child : m_childNodes){ if(child.isLeaf()){ leaves.add(child); } } return leaves; }
     */

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

    public List<EditChangeNode> getChangeNodes(QName qName) {
        List<EditChangeNode> changeNodes = new LinkedList<>();
        for (EditChangeNode changeNode : m_changeNodes) {
            if (changeNode.getQName().equals(qName)) {
                changeNodes.add(changeNode);
            }
        }
        return changeNodes;
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

    public EditContainmentNode addLeafChangeNode(QName qname, ConfigLeafAttribute nodeValue) {
        return addLeafChangeNode(qname, nodeValue, EditChangeSource.user);
    }

    public EditContainmentNode addLeafChangeNode(QName qname, ConfigLeafAttribute nodeValue, EditChangeSource changeSource) {
        EditChangeNode node = new EditChangeNode(qname, nodeValue);
        node.setChangeSource(changeSource);
        validateLeafChangeNode(this, node);
        this.m_changeNodes.add(node);
        return this;
    }

    private void validateLeafChangeNode(EditContainmentNode parentNode, EditChangeNode childNode) {

        for (EditChangeNode changeNode : m_changeNodes) {
            if (changeNode.getQName().equals(childNode.getQName())) {
                throwDuplicateNodesEditConfigException(parentNode.getModelNodeId().xPathString() + "/" + childNode.getName(), childNode.getQName());
            }
        }
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EditContainmentNode that = (EditContainmentNode) o;

        if (m_editOperation != null ? !m_editOperation.equals(that.m_editOperation) : that.m_editOperation != null)
            return false;
        return m_modelNodeId != null ? m_modelNodeId.equals(that.m_modelNodeId) : that.m_modelNodeId == null;
    }

    @Override
    public int hashCode() {
        int result = m_editOperation != null ? m_editOperation.hashCode() : 0;
        result = 31 * result + (m_modelNodeId != null ? m_modelNodeId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent, boolean printChangeSource) {
        StringBuilder sb = new StringBuilder();
        String filler = StringUtil.blanks(indent);

        if (printChangeSource) {
            sb.append(filler).append("Containment [" + m_editOperation + "," + getName() + "," + getNamespace() + "," + getChangeSource() +
                    "]" + LF);
        } else {
            sb.append(filler).append("Containment [" + m_editOperation + "," + getName() + "," + getNamespace() + "]" + LF);
        }

        for (EditMatchNode matchNode : m_matchNodes) {
            sb.append(filler).append(" ").append(matchNode.toString()).append(LF);
        }
        for (EditChangeNode changeNode : m_changeNodes) {
            sb.append(filler).append(" ").append(changeNode.toString(printChangeSource)).append(LF);
        }
        for (EditContainmentNode configNode : m_childNodes.list()) {
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
        for (EditContainmentNode child : m_childNodes.list()) {
            if (child.getQName().equals(qName)) {
                return child;
            }
        }
        return null;
    }

    public List<EditContainmentNode> getChildNodes(QName qName) {
        List<EditContainmentNode> childNodes = new LinkedList<>();
        for (EditContainmentNode child : m_childNodes.list()) {
            if (child.getQName().equals(qName)) {
                childNodes.add(child);
            }
        }
        return childNodes;
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


    /**
     * This API should not be used if the sole purpose is to identify the type of the node. For that getSchemaPath() should be used.
     */
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
    
    public void setSchemaRegistry(SchemaRegistry schemaRegistry){
    	m_schemaRegistry = schemaRegistry;
    }

    public SchemaRegistry getSchemaRegistry(){
    	return m_schemaRegistry;
    }
    
    public static void setParentForEditContainmentNode(EditContainmentNode node, EditContainmentNode parent) {
        node.setParent(parent);
        for (EditContainmentNode child : node.getChildren()) {
            setParentForEditContainmentNode(child, node);
        }
    }

    @Override
    public ModelNodeId getKey() {
        return getModelNodeId();
    }

    public void setHasChanged(boolean hasChanged) {
        m_hasChanged = hasChanged;
    }

    public boolean getHasChanged() {
        return m_hasChanged;
    }

    public Node getDomValue() {
        return m_domValue;
    }
}
