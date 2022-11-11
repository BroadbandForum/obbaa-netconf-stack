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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Simple traversable tree structure with root-node capable of holding multiple
 * branches. A value instance can be added only once to an instance of this
 * class. This is a reasonable limitation added to simplify path determination
 * which doesn't hinder this class being used for storing any fully-qualified
 * nodes (e.g., schema nodes, data nodes, identities, etc,.)
 *
 *
 *
 * @param <T>
 */
public class TraversableTree<T extends Comparable<T>> {

    private TreeNode<T> m_rootNode = new DefaultTreeNode<T>(null, null);

    private final TreeMap<T, TreeNode<T>> m_allNodesInTree = new TreeMap<>();

    public synchronized void insertNodeAt(List<T> path, T newNode) {
        if (m_allNodesInTree.containsKey(newNode)) {
            throw new IllegalArgumentException(
                    "newNode already exists in this tree");
        }
        TreeNode<T> targetNode = getNodeByPath(path);
        if (targetNode != null) {
            targetNode.addChildNode(new DefaultTreeNode<T>(targetNode, newNode));
            m_allNodesInTree.put(newNode, targetNode);
        } else {
            throw new IllegalStateException(
                    "Couldn't find the path specified in this tree");
        }
    }

    public synchronized boolean removeNode(T newNode) {
        TreeNode<T> parentNode = m_allNodesInTree.remove(newNode);
        if (parentNode != null) {
            parentNode.removeChildNode(new DefaultTreeNode<T>(parentNode, newNode));
        }
        return false;
    }

    public List<T> findPathTo(T value) {
        if(value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        TreeNode<T> parentNode = m_allNodesInTree.get(value);
        if(parentNode == null) {
            return null;
        }
        List<T> path = new ArrayList<>();
        while (!m_rootNode.equals(parentNode)) {
            path.add(parentNode.getValue());
            parentNode = parentNode.getParentNode();
        }
        Collections.reverse(path);
        return path;
    }

    public SortedSet<T> getAllChildrenOf(List<T> path) {
        return getChildrenOf(getNodeByPath(path));
    }

    private SortedSet<T> getChildrenOf(TreeNode<T> node) {
        SortedSet<T> result = new TreeSet<>();
        if (node != null) {
            for (TreeNode<T> child : node.getChildNodes()) {
                result.add(child.getValue());
                result.addAll(getChildrenOf(child));
            }
        }
        return result;
    }

    private TreeNode<T> getNodeByPath(List<T> path) {
        if (path == null || path.isEmpty()) {
            return m_rootNode;
        }
        TreeNode<T> currentNode = m_rootNode;
        for (Iterator<T> iterator = path.iterator(); iterator.hasNext();) {
            T pathSegment = iterator.next();
            Set<TreeNode<T>> availableBranches = currentNode.getChildNodes();
            for (TreeNode<T> branch : availableBranches) {
                if (pathSegment.equals(branch.getValue())) {
                    if (iterator.hasNext()) {
                        currentNode = branch;
                        break;
                    } else {
                        return branch;
                    }
                }
            }
        }
        return null;
    }

}
