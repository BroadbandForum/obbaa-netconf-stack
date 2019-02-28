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

package org.broadband_forum.obbaa.netconf.api.utils.tree;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class encapsulate the node implementation. Only the Tree and Node interface are exposed. This would help to change the
 * implementation of tree without affecting the clients using it.
 */
public class NodeHelper {

    public static <T> TreeNode<T> getNodeInstance(T nodeData) {
        return new TreeNodeImpl<T>(nodeData);
    }

    private static class TreeNodeImpl<T> implements TreeNode<T> {

        private TreeNode<T> m_parent;
        private T m_node;
        private Set<TreeNode<T>> m_childs = new LinkedHashSet<TreeNode<T>>();

        public TreeNodeImpl(T data) {
            m_node = data;
        }

        @Override
        public Collection<TreeNode<T>> getChildren() {
            return m_childs;
        }

        @Override
        public TreeNode<T> getParent() {
            return m_parent;
        }

        @Override
        public T getNodeData() {
            return m_node;
        }

        @Override
        public void addChild(TreeNode<T> child) {
            m_childs.add(child);
            child.setParent(this);
        }

        @Override
        public TreeNode<T> getChild(T childData) {
            for (TreeNode<T> child : m_childs) {
                if (child.getNodeData().equals(childData)) {
                    return child;
                }
            }
            return null;
        }

        @Override
        public void addChildren(Collection<TreeNode<T>> children) {
            for (TreeNode<T> child : children) {
                addChild(child);
            }
        }

        @Override
        public boolean isRootNode() {
            return (getParent() == null);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TreeNode)) {
                return false;
            }
            TreeNode<?> node = (TreeNode<?>) obj;
            Object data = node.getNodeData();
            return m_node.equals(data);
        }

        @Override
        public int hashCode() {
            return m_node.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{").append(getNodeData().toString()).append(",[");
            int i = 0;
            for (TreeNode<T> e : getChildren()) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(e.getNodeData().toString());
                i++;
            }
            sb.append("],Parent=");
            sb.append(getParent() == null ? getParent() : getParent().getNodeData().toString()).append("}");
            return sb.toString();
        }

        @Override
        public void setParent(TreeNode<T> parent) {
            m_parent = parent;
        }

        @Override
        public boolean removeChild(TreeNode<T> child) {
            return m_childs.remove(child);
        }

    }

}
