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

import java.util.Iterator;
import java.util.List;

public interface Tree<T> {

    /**
     * @return returns rootNode of the Tree
     */
    TreeNode<T> getRootNode();

    /**
     * @param rootNode rootNode to be set for this tree.
     */
    void addRootNode(TreeNode<T> rootNode);

    /**
     * Method used to get the TreeNode<T> with the given data.
     *
     * @param nodeData data to be searched for the TreeNode
     * @return returns TreeNode is found with the given data, or null
     */
    TreeNode<T> findNode(T nodeData);

    /**
     * Method used to get the TreeNode<T> with the given data path.
     *
     * @param nodeDataPath to be searched for the TreeNode. Ex: (pma/device-holders/device-holder/device)
     * @return returns TreeNode is found with the given data, or null
     */
    TreeNode<T> findNode(List<T> nodeDataPath);

    /**
     * @return returns the size of the tree, Returns 0 is empty otherwise number of nodes in tree
     */
    int getSize();

    /**
     * @return the iterator, which will iterate the tree in pre order.
     */
    Iterator<TreeNode<T>> getPreOrderIterator();

    /**
     * @return the iterator, which will iterate the tree in post order.
     */
    public Iterator<TreeNode<T>> getPostOrderIterator();

}