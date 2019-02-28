package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultTreeNode<T> implements TreeNode<T> {

    private final TreeNode<T> m_parentNode;

    private final T m_value;

    private final Set<TreeNode<T>> m_childNodes = new HashSet<>();

    public DefaultTreeNode(TreeNode<T> parent, T value) {
        this.m_parentNode = parent;
        this.m_value = value;
    }

    @Override
    public T getValue() {
        return m_value;
    }

    @Override
    public TreeNode<T> getParentNode() {
        return m_parentNode;
    }

    @Override
    public synchronized void addChildNode(TreeNode<T> childNode) {
        if (childNode == null) {
            throw new IllegalArgumentException("childNode cannot be null");
        }
        m_childNodes.add(childNode);
    }

    @Override
    public synchronized boolean removeChildNode(TreeNode<T> childNode) {
        if (childNode == null) {
            throw new IllegalArgumentException("childNode cannot be null");
        }
        return m_childNodes.remove(childNode);
    }

    @Override
    public Set<TreeNode<T>> getChildNodes() {
        return Collections.unmodifiableSet(m_childNodes);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        @SuppressWarnings("rawtypes")
        DefaultTreeNode other = (DefaultTreeNode) obj;
        if (m_value == null) {
            if (other.m_value != null){
                return false;
            }
        } else if (!m_value.equals(other.m_value)){
            return false;
        }
        return true;
    }

}
