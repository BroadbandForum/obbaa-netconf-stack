package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import java.util.Set;

public interface TreeNode<T> {

    TreeNode<T> getParentNode();

    T getValue();

    void addChildNode(TreeNode<T> childNode);

    boolean removeChildNode(TreeNode<T> childNode);

    Set<TreeNode<T>> getChildNodes();

}
