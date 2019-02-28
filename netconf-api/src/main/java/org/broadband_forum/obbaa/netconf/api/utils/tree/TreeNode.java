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

public interface TreeNode<T> {
    /**
     * 
     * @return returns the parent of the TreeNode
     */

    TreeNode<T> getParent();

    /**
     * 
     * @return return all the children of the TreeNode. The TreeNode can contain 0..n children
     */

    Collection<TreeNode<T>> getChildren();

    /**
     * 
     * @param parent TreeNode<T> which need to be set as parent of this TreeNode.
     */

    void setParent(TreeNode<T> parent);

    /**
     * 
     * @param child child TreeNode of this TreeNode
     */

    void addChild(TreeNode<T> child);

    /**
     * 
     * @param children List of child for this TreeNode
     */

    void addChildren(Collection<TreeNode<T>> children);

    /**
     * 
     * @return returns the data of the TreeNode
     */
    T getNodeData();

    /**
     * 
     * @return true if this TreeNode is rootNode
     */

    boolean isRootNode();

    /**
     * 
     * @param childData
     * @return returns the reference to TreeNode which has this data. If not found returns null.
     */
    TreeNode<T> getChild(T childData);

    /**
     * 
     * @param child child TreeNode of this TreeNode
     * @return <tt>true</tt> if child is one of children of this TreeNode
     */
    boolean removeChild(TreeNode<T> child);

}
