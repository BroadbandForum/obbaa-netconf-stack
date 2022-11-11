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

import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.CREATE;
import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.DELETE;
import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.MERGE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.ChangedByParams;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class CTNNbiNotificationUtil {

    public static List<Notification> buildListNotification(Set<ChangeTreeNode> changeTreeNodes, ChangedByParams changedByParams) {
        List<Notification> netconfConfigChangeNotifications = new ArrayList<>();
        List<EditInfo> editInfos = new ArrayList<>();
        buildEditInfos(changeTreeNodes, editInfos);
        if (!editInfos.isEmpty()) {
            netconfConfigChangeNotifications.add(buildNotification(changedByParams, editInfos));
        }
        return netconfConfigChangeNotifications;
    }

    private static void buildEditInfos(Set<ChangeTreeNode> changeTreeNodes, List<EditInfo> editInfos) {
        for (ChangeTreeNode childCTN : changeTreeNodes) {
            if (childCTN.hasChanged()) {
                if (childCTN.getChange().equals(ChangeTreeNode.ChangeType.modify)) {
                    DataSchemaNode schemaNode = (DataSchemaNode) childCTN.getType();
                    if (schemaNode instanceof LeafSchemaNode || schemaNode instanceof LeafListSchemaNode ||
                            isOrderModifiedForList(childCTN, schemaNode)) {
                        populateEditInfoUsingSpecifiedCTNAndOperation(childCTN, editInfos, MERGE);
                    }
                } else if (childCTN.getChange().equals(ChangeTreeNode.ChangeType.create)) {
                    if (childCTN.getParent().getChange().equals(ChangeTreeNode.ChangeType.modify)) {
                        populateEditInfoUsingSpecifiedCTNAndOperation(childCTN, editInfos, CREATE);
                    }
                } else if (childCTN.getChange().equals(ChangeTreeNode.ChangeType.delete)) {
                    if (childCTN.getParent().getChange().equals(ChangeTreeNode.ChangeType.modify)) {
                        populateEditInfoUsingSpecifiedCTNAndOperation(childCTN, editInfos, DELETE);
                    }
                }
            }
        }
    }

    private static boolean isOrderModifiedForList(ChangeTreeNode ctn, DataSchemaNode schemaNode) {
        return schemaNode instanceof ListSchemaNode && ((ListSchemaNode) schemaNode).isUserOrdered() && ctn.getInsertOperation() != null;
    }

    private static void populateEditInfoUsingSpecifiedCTNAndOperation(ChangeTreeNode changeTreeNode, List<EditInfo> editInfos, String operation) {
        StringBuilder targetPath = new StringBuilder();
        SchemaRegistry schemaRegistry = changeTreeNode.getSchemaRegistry();
        targetPath.append(changeTreeNode.getId().xPathString(schemaRegistry));
        EditInfo editInfo = getEditInfo(operation, changeTreeNode.getId(), targetPath, schemaRegistry);
        editInfos.add(editInfo);
    }

    private static EditInfo getEditInfo(String operation, ModelNodeId modelNodeId, StringBuilder targetPath, SchemaRegistry schemaRegistry) {
        Map<String, String> namespaceDeclareMap = new HashMap<>(modelNodeId.xPathStringNsByPrefix(schemaRegistry));
        EditInfo editInfo = new EditInfo();
        editInfo.setNamespaceDeclareMap(namespaceDeclareMap);
        editInfo.setTarget(targetPath.toString());
        editInfo.setOperation(operation);
        return editInfo;
    }

    private static NetconfConfigChangeNotification buildNotification(ChangedByParams changedByParams, List<EditInfo> editInfos) {
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        netconfConfigChangeNotification.setEditList(editInfos);
        netconfConfigChangeNotification.setChangedByParams(changedByParams);
        return netconfConfigChangeNotification;
    }
}
