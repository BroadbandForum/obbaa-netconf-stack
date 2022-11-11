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

import java.util.LinkedHashSet;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttributeWithInsertOp;

public interface WritableChangeTreeNode extends ChangeTreeNode {

    void setMountPoint(boolean mountPoint);

    /**
     * Appends the given childNode to the correct place in the tree based on childNode's ModelNodeId.
     * @param childNode: childNode to be appended.
     */
    void appendChildNode(WritableChangeTreeNode childNode);

    void setChangeType(ChangeType delete);

    void setPreviousValue(ConfigLeafAttribute leaf);

    void setCurrentValue(ConfigLeafAttribute leaf);

    void setPreviousValue(LinkedHashSet<ConfigLeafAttributeWithInsertOp> leafListValues);

    void setCurrentValue(LinkedHashSet<ConfigLeafAttributeWithInsertOp> leafListValues);

    void setInsertOperation(InsertOperation insertOperation);

    /**
     * This is the NBI edit-operation being performed on this node. This is needed to differenciate whether a list/container is being created/replaced/merged.
     * This value is not applicable for Leaf and Leaf-list ChangeTreeNodes
     * @param editOperation
     */
    void setEditOperation(String editOperation);

    void setImplied(boolean isImplied);

    void setEditChangeSource(EditChangeSource changeSource);
}
