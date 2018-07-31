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
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.StringUtil;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;

public class FilterNode {
    private List<FilterNode> m_childNodes = new ArrayList<FilterNode>();
    private List<FilterMatchNode> m_matchNodes = new ArrayList<FilterMatchNode>();
    private List<FilterNode> m_selectNodes = new ArrayList<FilterNode>();
    private boolean m_isEmpty = false;

    private String m_namespace;
    private String m_nodeName;
    private NetconfQueryParams m_params;

    public FilterNode(List<FilterNode> childNodes, List<FilterMatchNode> matchNodes, List<FilterNode> selectNodes,
                      boolean isEmpty,
                      String namespace, String nodeName, NetconfQueryParams params) {
        m_childNodes = childNodes;
        m_matchNodes = matchNodes;
        m_selectNodes = selectNodes;
        m_isEmpty = isEmpty;
        m_namespace = namespace;
        m_nodeName = nodeName;
        m_params = params;
    }

    public List<FilterNode> getChildNodes() {
        return m_childNodes;
    }

    public void setChildNodes(List<FilterNode> childNodes) {
        this.m_childNodes = childNodes;
    }

    public List<FilterMatchNode> getMatchNodes() {
        return m_matchNodes;
    }

    public void setMatchNodes(List<FilterMatchNode> matchNodes) {
        this.m_matchNodes = matchNodes;
    }

    public List<FilterNode> getSelectNodes() {
        return m_selectNodes;
    }

    public void setSelectNodes(List<FilterNode> selectNodes) {
        this.m_selectNodes = selectNodes;
    }

    public void setEmpty(boolean empty) {
        this.m_isEmpty = empty;
    }

    public void setNamespace(String namespace) {
        this.m_namespace = namespace;
    }

    public void setNodeName(String nodeName) {
        this.m_nodeName = nodeName;
    }

    public FilterNode(String nodeName, String namespace) {
        m_nodeName = nodeName;
        m_namespace = namespace;
    }

    public FilterNode(QName qName) {
        m_nodeName = qName.getLocalName();
        m_namespace = qName.getNamespace().toString();
    }

    public boolean isSelectNode() {
        return m_childNodes.isEmpty() && m_matchNodes.isEmpty() && m_selectNodes.isEmpty();
    }

    public FilterNode() {
    }

    public String getNodeName() {
        return m_nodeName;
    }

    public FilterNode addContainmentNode(String nodeName, String namespace) {
        FilterNode node = new FilterNode(nodeName, namespace);
        m_childNodes.add(node);
        return node;
    }

    public FilterNode addContainmentNode(FilterNode node) {
        m_childNodes.add(node);
        return this;
    }

    public void addMatchNode(String nodeName, String namespace, String filter) {
        FilterMatchNode node = new FilterMatchNode(nodeName, namespace, filter);
        m_matchNodes.add(node);
    }

    public void addSelectNode(FilterNode selectNode) {
        m_selectNodes.add(selectNode);
    }

    public void addSelectNode(String nodeName, String namespace) {
        m_selectNodes.add(new FilterNode(nodeName, namespace));
    }

    public String toString() {
        return toString(0);
    }

    public void markEmpty() {
        m_isEmpty = true;
    }

    public boolean isEmpty() {
        return m_isEmpty;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String filler = StringUtil.blanks(indent);

        if (isSelectNode()) {
            sb.append(filler).append("Select [" + m_nodeName + ", namespace=" + m_namespace + "]\n");
        } else {
            sb.append(filler).append("Containment [" + m_nodeName + ", namespace=" + m_namespace + "]\n");
        }

        for (FilterMatchNode mn : m_matchNodes) {
            sb.append(filler).append(" ").append(mn.toString()).append("\n");
        }

        for (FilterNode sn : m_selectNodes) {
            sb.append(filler).append(" ").append(sn.toString()).append("\n");
        }
        for (FilterNode sn : m_childNodes) {
            sb.append(sn.toString(indent + 1)).append("\n");
        }

        sb.append("Query Params : " + m_params);

        return sb.toString();
    }

    public boolean hasSelectNode(String name) {
        for (FilterNode sn : m_selectNodes) {
            if (sn.getNodeName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasContainmentNode(String name) {
        for (FilterNode cn : m_childNodes) {
            if (cn.getNodeName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public List<FilterNode> getContainmentNodes(String name) {
        List<FilterNode> result = new ArrayList<FilterNode>();
        for (FilterNode sn : m_childNodes) {
            if (sn.getNodeName().equals(name)) {
                result.add(sn);
            }
        }

        return result;
    }

    public String getNamespace() {
        return m_namespace;
    }

    public FilterNode getSelectNode(String name, String namespace) {
        for (FilterNode sn : m_selectNodes) {
            if (sn.getNodeName().equals(name)) {
                return sn;
            }
        }
        return null;
    }

    public NetconfQueryParams getParams() {
        return m_params;
    }

    public void setParams(NetconfQueryParams params) {
        m_params = params;
    }

    public boolean childHasMatchCondition() {
        for (FilterNode child : m_childNodes) {
            if (!child.getMatchNodes().isEmpty()) {
                return true;
            }
            boolean returnValue = child.childHasMatchCondition();
            if (returnValue) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMatchCondition() {
        if (!this.getMatchNodes().isEmpty() || childHasMatchCondition()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_childNodes == null) ? 0 : m_childNodes.hashCode());
        result = prime * result + (m_isEmpty ? 1231 : 1237);
        result = prime * result + ((m_matchNodes == null) ? 0 : m_matchNodes.hashCode());
        result = prime * result + ((m_namespace == null) ? 0 : m_namespace.hashCode());
        result = prime * result + ((m_nodeName == null) ? 0 : m_nodeName.hashCode());
        result = prime * result + ((m_params == null) ? 0 : m_params.hashCode());
        result = prime * result + ((m_selectNodes == null) ? 0 : m_selectNodes.hashCode());
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
        FilterNode other = (FilterNode) obj;
        if (m_childNodes == null) {
            if (other.m_childNodes != null)
                return false;
        } else if (!m_childNodes.equals(other.m_childNodes))
            return false;
        if (m_isEmpty != other.m_isEmpty)
            return false;
        if (m_matchNodes == null) {
            if (other.m_matchNodes != null)
                return false;
        } else if (!m_matchNodes.equals(other.m_matchNodes))
            return false;
        if (m_namespace == null) {
            if (other.m_namespace != null)
                return false;
        } else if (!m_namespace.equals(other.m_namespace))
            return false;
        if (m_nodeName == null) {
            if (other.m_nodeName != null)
                return false;
        } else if (!m_nodeName.equals(other.m_nodeName))
            return false;
        if (m_params == null) {
            if (other.m_params != null)
                return false;
        } else if (!m_params.equals(other.m_params))
            return false;
        if (m_selectNodes == null) {
            if (other.m_selectNodes != null)
                return false;
        } else if (!m_selectNodes.equals(other.m_selectNodes))
            return false;
        return true;
    }
}
