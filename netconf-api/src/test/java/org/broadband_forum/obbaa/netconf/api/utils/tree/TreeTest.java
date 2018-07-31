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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TreeTest {

    @Test
    public void testEmptyTree() {
        Tree<String> tree = TreeHelper.getTreeInstance();
        assertNull(tree.getRootNode());
        assertEquals(0, tree.getSize());
    }

    @Test
    public void testTreeNode() {
        TreeNode<String> node1 = NodeHelper.getNodeInstance("Node 1");
        TreeNode<String> node2 = NodeHelper.getNodeInstance("Node 2");
        node1.addChild(node2);
        TreeNode<String> node3 = NodeHelper.getNodeInstance("Node 3");
        node1.addChild(node3);
        TreeNode<String> node4 = NodeHelper.getNodeInstance("Node 4");
        TreeNode<String> node5 = NodeHelper.getNodeInstance("Node 5");
        node2.addChildren(Arrays.asList(node4, node5));
        TreeNode<String> node6 = NodeHelper.getNodeInstance("Node 6");
        node3.addChild(node6);

        // Node 1 assertion
        assertEquals("Node 1", node1.getNodeData());
        assertTrue(node1.isRootNode());
        assertNull(node1.getParent());
        Collection<TreeNode<String>> node1Children = node1.getChildren();
        assertEquals(2, node1Children.size());
        assertTrue(node1Children.contains(node2));
        assertTrue(node1Children.contains(node3));
        assertEquals(node2, node1.getChild("Node 2"));
        assertEquals(node3, node1.getChild("Node 3"));

        // Node 2 assertion
        assertEquals("Node 2", node2.getNodeData());
        assertFalse(node2.isRootNode());
        assertEquals(node1, node2.getParent());
        Collection<TreeNode<String>> node2Children = node2.getChildren();
        assertEquals(2, node2Children.size());
        assertTrue(node2Children.contains(node4));
        assertTrue(node2Children.contains(node5));
        assertEquals(node4, node2.getChild("Node 4"));
        assertEquals(node5, node2.getChild("Node 5"));

        // Node 3 assertion
        assertEquals("Node 3", node3.getNodeData());
        assertFalse(node3.isRootNode());
        assertEquals(node1, node3.getParent());
        Collection<TreeNode<String>> node3Children = node3.getChildren();
        assertEquals(1, node3Children.size());
        assertTrue(node3Children.contains(node6));
        assertEquals(node6, node3.getChild("Node 6"));

        // Node 4 assertion
        assertEquals("Node 4", node4.getNodeData());
        assertFalse(node4.isRootNode());
        assertEquals(node2, node4.getParent());
        assertTrue(node4.getChildren().isEmpty());

        // Node 5 assertion
        assertEquals("Node 5", node5.getNodeData());
        assertFalse(node5.isRootNode());
        assertEquals(node2, node5.getParent());
        assertTrue(node5.getChildren().isEmpty());

        // Node 6 assertion
        assertEquals("Node 6", node6.getNodeData());
        assertFalse(node6.isRootNode());
        assertEquals(node3, node6.getParent());
        assertTrue(node6.getChildren().isEmpty());

        // Test removeChild() method
        assertFalse(node2.removeChild(node6));
        assertEquals(2, node2.getChildren().size());

        assertTrue(node2.removeChild(node4));
        assertEquals(1, node2.getChildren().size());
        assertFalse(node2.getChildren().contains(node4));
        assertTrue(node2.getChildren().contains(node5));
    }

    @Test
    public void testTree() {
        TreeNode<String> node1 = NodeHelper.getNodeInstance("Node 1");
        TreeNode<String> node2 = NodeHelper.getNodeInstance("Node 2");
        node1.addChild(node2);
        TreeNode<String> node3 = NodeHelper.getNodeInstance("Node 3");
        node1.addChild(node3);
        TreeNode<String> node4 = NodeHelper.getNodeInstance("Node 4");
        TreeNode<String> node5 = NodeHelper.getNodeInstance("Node 5");
        node2.addChildren(Arrays.asList(node4, node5));
        TreeNode<String> node6 = NodeHelper.getNodeInstance("Node 6");
        node3.addChild(node6);

        Tree<String> tree = TreeHelper.getTreeInstance(node1);

        verifyTree(node1, node2, node3, node4, node5, node6, tree);

        Tree<String> cloneTree = TreeHelper.cloneTree(tree);
        verifyTree(node1, node2, node3, node4, node5, node6, cloneTree);

        // Add more node to tree, verify that cloneTree is not affected
        TreeNode<String> node7 = NodeHelper.getNodeInstance("Node 7");
        node1.addChild(node7);

        assertEquals(7, tree.getSize());
        assertEquals(6, cloneTree.getSize());

        TreeNode<String> node8 = NodeHelper.getNodeInstance("Node 8");
        node2.addChild(node8);
        assertEquals(8, tree.getSize());
        assertEquals(6, cloneTree.getSize());

        TreeNode<String> node9 = NodeHelper.getNodeInstance("Node 9");
        node3.addChild(node9);
        assertEquals(9, tree.getSize());
        assertEquals(6, cloneTree.getSize());
    }

    private void verifyTree(TreeNode<String> node1, TreeNode<String> node2, TreeNode<String> node3, TreeNode<String>
            node4,
                            TreeNode<String> node5, TreeNode<String> node6, Tree<String> tree) {
        // findNode(T nodeData) method
        assertEquals(node1, tree.getRootNode());
        assertEquals(node1, tree.findNode("Node 1"));
        assertEquals(node2, tree.findNode("Node 2"));
        assertEquals(node3, tree.findNode("Node 3"));
        assertEquals(node4, tree.findNode("Node 4"));
        assertEquals(node5, tree.findNode("Node 5"));
        assertEquals(node6, tree.findNode("Node 6"));
        assertNull(tree.findNode("Node 7"));

        // findNode(List<T> nodeDataPath) method
        assertEquals(node1, tree.findNode(Arrays.asList("Node 1")));
        assertEquals(node2, tree.findNode(Arrays.asList("Node 1", "Node 2")));
        assertEquals(node3, tree.findNode(Arrays.asList("Node 1", "Node 3")));
        assertEquals(node4, tree.findNode(Arrays.asList("Node 1", "Node 2", "Node 4")));
        assertEquals(node5, tree.findNode(Arrays.asList("Node 1", "Node 2", "Node 5")));
        assertEquals(node6, tree.findNode(Arrays.asList("Node 1", "Node 3", "Node 6")));
        assertNull(tree.findNode(Arrays.asList("Node 2")));
        assertNull(tree.findNode(Arrays.asList("Node 2", "Node 4")));

        // getSize() method
        assertEquals(6, tree.getSize());

        // getRootNode() method
        assertEquals(node1, tree.getRootNode());

        // getPreOrderIterator() method
        Iterator<TreeNode<String>> preOrderIterator = tree.getPreOrderIterator();
        List<TreeNode<String>> expectedPreOrder = Arrays.asList(node1, node2, node4, node5, node3, node6);

        int preorderIndex = 0;
        while (preOrderIterator.hasNext()) {
            TreeNode<String> currentNode = preOrderIterator.next();
            TreeNode<String> expectedNode = expectedPreOrder.get(preorderIndex);
            assertEquals(expectedNode, currentNode);
            preorderIndex++;
        }

        // getPreOrderIterator() method
        Iterator<TreeNode<String>> postOrderIterator = tree.getPostOrderIterator();
        List<TreeNode<String>> expectedPostOrder = Arrays.asList(node6, node3, node5, node4, node2, node1);

        int postorderIndex = 0;
        while (postOrderIterator.hasNext()) {
            TreeNode<String> currentNode = postOrderIterator.next();
            TreeNode<String> expectedNode = expectedPostOrder.get(postorderIndex);
            assertEquals(expectedNode, currentNode);
            postorderIndex++;
        }
    }
}
