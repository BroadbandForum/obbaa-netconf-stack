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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class encapsulates the Tree implementation. Only the Tree interface is exposed to the user. This would help to change the tree
 * implementation without effecting the clients.
 * 
 */
public class TreeHelper {

    /**
     * This method returns Tree.
     * 
     * @param <T> Data type to be stored in Node of tree
     * @param rootNode root node for this tree
     * @return This method returns Tree.
     */
    public static <T> Tree<T> getTreeInstance(TreeNode<T> rootNode) {
        return new TreeImpl<T>(rootNode);
    }

    /**
     * 
     * @param <T> the parameter type for the tree
     * @return returns a tree with rootNode as null
     */
    public static <T> Tree<T> getTreeInstance() {
        return new TreeImpl<T>();
    }

    public static <T> Tree<T> cloneTree(Tree<T> tree) {
        TreeNode<T> newRootNode = cloneTreeNode(tree.getRootNode());
        return getTreeInstance(newRootNode);
    }

    private static <T> TreeNode<T> cloneTreeNode(TreeNode<T> treeNode) {
        TreeNode<T> newTreeNode = NodeHelper.getNodeInstance(treeNode.getNodeData());
        for (TreeNode<T> childNode : treeNode.getChildren()) {
            TreeNode<T> newChildNode = cloneTreeNode(childNode);
            newTreeNode.addChild(newChildNode);
        }
        return newTreeNode;
    }

    private static class TreeImpl<T> implements Tree<T> {

        private TreeNode<T> m_rootNode;

        public TreeImpl() {

        }

        public TreeImpl(TreeNode<T> rootNode) {
            m_rootNode = rootNode;
            if (m_rootNode == null) {
                throw new NullPointerException();
            }

        }

        @Override
        public void addRootNode(TreeNode<T> rootNode) {
            m_rootNode = rootNode;
            if (m_rootNode == null)
                throw new NullPointerException();
        }

        @Override
        public TreeNode<T> getRootNode() {
            return m_rootNode;
        }

        @Override
        public TreeNode<T> findNode(T nodeData) {
            if (m_rootNode == null) {
                return null;
            }
            return findNodeInternal(m_rootNode, nodeData);
        }

        @Override
        public TreeNode<T> findNode(List<T> nodeDataPath) {
            if (m_rootNode == null || nodeDataPath == null || nodeDataPath.isEmpty()) {
                return null;
            }

            if (!m_rootNode.getNodeData().equals(nodeDataPath.get(0))) {
                return null;
            }

            if (nodeDataPath.size() == 1) {
                return m_rootNode;
            }

            TreeNode<T> parentNode = m_rootNode;
            TreeNode<T> node = null;
            for (int i = 1; i < nodeDataPath.size(); i++) {
                T nodeData = nodeDataPath.get(i);
                node = findNodeFromDirectChilds(parentNode, nodeData);
                if (node == null) {
                    break;
                }
                parentNode = node;
            }

            return node;
        }

        @Override
        public int getSize() {
            return getListOfNodes().size();
        }

        @Override
        public Iterator<TreeNode<T>> getPreOrderIterator() {
            return new PreOrderIterator();
        }

        @Override
        public Iterator<TreeNode<T>> getPostOrderIterator() {
            return new PostOrderIterator();
        }

        @Override
        public String toString() {
            return getListOfNodes().toString();
        }

        private List<TreeNode<T>> getListOfNodes() {
            List<TreeNode<T>> preorderList = new ArrayList<TreeNode<T>>();
            if (m_rootNode == null) {
                return preorderList;
            }
            return addChildsToList(m_rootNode, new ArrayList<TreeNode<T>>());
        }

        private List<TreeNode<T>> addChildsToList(TreeNode<T> node, List<TreeNode<T>> nodes) {
            nodes.add(node);
            for (TreeNode<T> childNode : node.getChildren()) {
                addChildsToList(childNode, nodes);
            }
            return nodes;
        }

        private TreeNode<T> findNodeFromDirectChilds(TreeNode<T> parentNode, T data) {
            for (TreeNode<T> childNode : parentNode.getChildren()) {
                if (data.equals(childNode.getNodeData())) {
                    return childNode;
                }
            }

            return null;
        }

        private TreeNode<T> findNodeInternal(TreeNode<T> node, T data) {

            if (data.equals(node.getNodeData()))
                return node;
            for (TreeNode<T> childNode : node.getChildren()) {
                TreeNode<T> returnNode = findNodeInternal(childNode, data);
                if (returnNode != null)
                    return returnNode;
            }
            return null;

        }

        private class PreOrderIterator implements Iterator<TreeNode<T>> {
            private int m_position = 0;
            private List<TreeNode<T>> m_nodes = getListOfNodes();

            @Override
            public boolean hasNext() {
                return m_position < m_nodes.size();
            }

            @Override
            public TreeNode<T> next() {
                return m_nodes.get(m_position++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

        private class PostOrderIterator implements Iterator<TreeNode<T>> {

            private List<TreeNode<T>> m_nodes = getListOfNodes();
            private int m_position = m_nodes.size();

            @Override
            public boolean hasNext() {
                return m_position > 0 ? true : false;
            }

            @Override
            public TreeNode<T> next() {
                return m_nodes.get(--m_position);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
