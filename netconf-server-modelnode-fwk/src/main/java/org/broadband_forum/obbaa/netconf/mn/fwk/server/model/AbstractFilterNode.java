package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFilterNode {

    protected String m_nodeName;
    protected String m_namespace;
    protected Map<String, String> m_attributes = new HashMap<>();

    public AbstractFilterNode(String nodeName, String namespace) {
        m_nodeName = nodeName;
        m_namespace = namespace;
    }

    public AbstractFilterNode() {
    }

    public String getNodeName() {
        return m_nodeName;
    }

    public void setNodeName(String nodeName) {
        m_nodeName = nodeName;
    }

    public String getNamespace() {
        return m_namespace;
    }

    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }

    public Map<String, String> getAttributes() {
        return m_attributes;
    }

    public void addAttribute(String attributeName, String attributeValue) {
        m_attributes.put(attributeName,attributeValue);
    }
}
