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

package org.broadband_forum.obbaa.netconf.mn.fwk;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttributeWithInsertOp;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a single changed node in a Data Store.
 */
public interface ChangeTreeNode {
    /**
     * The type of change, that is create, delete, modify.
     * @return
     */
    ChangeType getChange();

    /**
     * The Schema Node that this data node represents.
     * @return
     */
    SchemaNode getType();

    /**
     * The ModelNodeId of the Changed Node.
     * If the changed node s=is not a list or a container,
     * it returns ModelNodeId of the parent.
     * @return
     */
    ModelNodeId getId();

    /**
     * The child nodes that are part of the current change.
     * Empty list if there are no children.
     * @return
     */
    Map<ModelNodeId, ChangeTreeNode> getChildren();

    /**
     * Parent Change node.
     * null if the node is a root node.
     * @return
     */
    ChangeTreeNode getParent();

    /**
     * Returns a set of ChangeTreeNodes of given type that are present in the tree.
     * Use this API when you are interested in changeTreeNodes belonging to any schema mount point.
     * @return
     */
    Collection<ChangeTreeNode> getNodesOfType(SchemaPath type);

    /**
     * Returns a set of ChangeTreeNodes of given type that are present in the tree within the schema mount point.
     * Use this API when you are interested in changeTreeNodes belonging to a specific schema mount point.
     * @param type
     * @return
     */
    Collection<ChangeTreeNode> getNodesOfTypeWithinSchemaMount(SchemaPath type);

    /**
     * Returns a set of ChangeTreeNode's of given type and that match the given attribute index that are present in the tree.
     * @return
     */
    Collection<ChangeTreeNode> getNodesOfType(AttributeIndex index);

    /**
     * Builds a XML DOM tree of the changed state of the subtree.
     * Could be null if the node is deleted.
     * @return
     */
    Element currentSubtreeXml();

    /**
     * Builds a XML DOM tree of the changed state of the subtree.
     * Could be null if the node is deleted.
     * @return
     */
    Element currentSubtreeXml(Document doc);

    /**
     * Returns the string value of the node after the change.
     * If the node is a leaf/leaf-list, then it returns the string value of the leaf/leaf-list.
     *
     * If the node is a list/container, it returns the string representation of the DOM tree.
     *
     * Could be null if the node is deleted.
     * @return
     */
    String currentValue();
    /**
     * Returns the string value of the node before the change.
     * If the node is a leaf/leaf-list, then it returns the string value of the leaf/leaf-list.
     *
     * If the node is a list/container, it returns the string representation of the DOM tree.
     *
     * Could be null if the node is newly created.
     * @return
     */
    String previousValue();

    /**
     * Builds a XML DOM tree of the original state of the subtree.
     * Could be null if the node is newly created.
     * @return
     */
    Element previousSubtreeXml();

    /**
     * Builds a XML DOM tree of the original state of the subtree.
     * Could be null if the node is newly created.
     * @return
     */
    Element previousSubtreeXml(Document doc);

    Map<String, Object> getContextMap();

    void addContextValue(String key, Object value);

    Object getContextValue(String key);

    String print();

    void print(Integer indent, StringBuilder sb);

    boolean hasChanged();

    boolean isLeafNode();

    Set<SchemaPath> getChangedNodeTypes();

    String getEditOperation();

    boolean isImplied();

    SchemaRegistry getSchemaRegistry();

    InsertOperation getInsertOperation();

    EditChangeSource getChangeSource();

    ConfigLeafAttribute getCurrentLeafValue();

    ConfigLeafAttribute getPreviousLeafValue();

    Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> getNodesIndex();

    Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> getNodesOfTypeIndexWithinSchemaMount();

    Map<AttributeIndex, Set<ChangeTreeNode>> getAttributeIndex();

    LinkedHashSet<ConfigLeafAttributeWithInsertOp> getCurrentLeafListValue();

    LinkedHashSet<ConfigLeafAttributeWithInsertOp> getPreviousLeafListValue();

    boolean isMountPoint();

    enum ChangeType {
        create, delete, modify, none;

        public static boolean isDelete(ChangeType change) {
            return delete.equals(change);
        }
    }
}
