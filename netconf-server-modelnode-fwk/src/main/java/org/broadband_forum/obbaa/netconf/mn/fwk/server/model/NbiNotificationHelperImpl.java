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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ChangedByParams;
import org.broadband_forum.obbaa.netconf.api.messages.ChangedLeafInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.SessionInfo;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class NbiNotificationHelperImpl implements NbiNotificationHelper {

    private Map<SubSystem, NotificationSegregator> m_subSystemNotificationSegregators = new HashMap<>();
    private DefaultNotificationSegregator m_defaultNotificationSegregator = new DefaultNotificationSegregator();
    
    public void registerClassifier(SubSystem subsytem, NotificationSegregator segregator){
        m_subSystemNotificationSegregators.put(subsytem, segregator);
    }

    public List<Notification> getNetconfConfigChangeNotifications(Map<SubSystem, IndexedNotifList> subSystemNotificationMap,
            NetconfClientInfo clientInfo) {
        List<Notification> netconfConfigChangeNotifications = new ArrayList<>();
        ChangedByParams changedByParams = buildChangedByInfo(clientInfo);
        Map<Object, Set<ChangeNotification>> classifiedChangeNotifications = classifyNotifications(subSystemNotificationMap);
        for (Map.Entry<Object, Set<ChangeNotification>> entry : classifiedChangeNotifications.entrySet()) {
            netconfConfigChangeNotifications.addAll(buildListNotification(entry.getValue(), changedByParams));
        }
        return netconfConfigChangeNotifications;
    }

    @Override
    public List<Notification> getNetconfConfigChangeNotifications(List<ChangeNotification> changeNotifications, NetconfClientInfo clientInfo) {
        return buildListNotification(changeNotifications, buildChangedByInfo(clientInfo));
    }

    public List<Notification> getNetconfConfigChangeNotificationsUsingAggregatorCTN(ChangeTreeNode aggregatorCTN, NetconfClientInfo clientInfo) {
        List<Notification> netconfConfigChangeNotifications = new ArrayList<>();
        ChangedByParams changedByParams = buildChangedByInfo(clientInfo);
        Set<SchemaPath> segregatedSPs = new HashSet<>();
        for(NotificationSegregator segregator: m_subSystemNotificationSegregators.values()) {
            List<Notification> segregatedNotifs;
            segregatedNotifs = segregator.segregateNotifications(aggregatorCTN, segregatedSPs, changedByParams);
            netconfConfigChangeNotifications.addAll(segregatedNotifs);
        }
        //This should execute at last as it handles all the remaining SPs
        netconfConfigChangeNotifications.addAll(m_defaultNotificationSegregator.segregateNotifications(aggregatorCTN, segregatedSPs, changedByParams));
        return netconfConfigChangeNotifications;
    }

    private List<Notification> buildListNotification(Collection<ChangeNotification> changeNotifications, ChangedByParams changedByParams) {
        List<Notification> netconfConfigChangeNotifications = new ArrayList<>();
        List<EditInfo> editInfos = new ArrayList<>();
        List<EditInfo> impliedEditInfos = new ArrayList<>();
        buildEditInfos(changeNotifications, editInfos, impliedEditInfos);
        if (!editInfos.isEmpty()) {
            netconfConfigChangeNotifications.add(buildNotification(changedByParams, editInfos));
        }
        if (!impliedEditInfos.isEmpty()) {
            netconfConfigChangeNotifications.add(buildNotification(changedByParams, impliedEditInfos));
        }
        return netconfConfigChangeNotifications;
    }

    private void editNotification(EditContainmentNode changeDataNode, StringBuilder targetPath, List<EditInfo> editInfos,
                                  NamespaceContext namespaceContext, Map<String, String> namespaceDeclareMap, boolean isImplied) {
        List<EditChangeNode> editChangeNodes = changeDataNode.getChangeNodes();
        Map<String, EditInfo> editInfoMap = new HashMap<>();
        for (EditChangeNode editChangeNode : editChangeNodes) {
            String changeNodeNamespace = editChangeNode.getNamespace();
            String changeNodePrefix = getPrefix(changeDataNode.getSchemaRegistry(), changeNodeNamespace);
            String operation = editChangeNode.getOperation();
            EditInfo editInfo = new EditInfo();
            editInfo.setNamespaceDeclareMap(namespaceDeclareMap);
            editInfo.setTarget(targetPath.toString());
            editInfo.setOperation(operation);
            editInfo.setImplied(isImplied);
            if (operation.equals(ModelNodeChangeType.delete.name())) {
                String newTargetPath = targetPath + editChangeNode.getName();
                if (changeNodePrefix != null) {
                    namespaceDeclareMap.put(changeNodePrefix, changeNodeNamespace);
                    newTargetPath = targetPath + "/" + changeNodePrefix + ":" + editChangeNode.getName();
                }
                editInfo.setTarget(newTargetPath);
                editInfos.add(editInfo);
            } else {
                ChangedLeafInfo changedLeafInfo = new ChangedLeafInfo(editChangeNode.getName(), editChangeNode.getValue(), changeNodeNamespace, changeNodePrefix);
                if (editInfoMap.get(operation) == null) {
                    editInfo.setChangedLeafInfos(changedLeafInfo);
                    editInfos.add(editInfo);
                    editInfoMap.put(operation, editInfo);
                } else {
                    editInfoMap.get(operation).setChangedLeafInfos(changedLeafInfo);
                }
            }
        }
        List<EditContainmentNode> children = changeDataNode.getChildren();
        for (EditContainmentNode childNode : children) {
            Map<String, String> namespaceDeclareMapForChild = new HashMap<>(namespaceDeclareMap);
            StringBuilder childTarget = new StringBuilder(targetPath);
            updateTargetForMatchNode(childNode, namespaceDeclareMapForChild, childTarget);
            if (childNode.getEditOperation().equals(ModelNodeChangeType.merge.name())) {
                editNotification(childNode, childTarget, editInfos, namespaceContext, namespaceDeclareMapForChild, isImplied);
            } else {
                String operation = childNode.getEditOperation();
                EditInfo editInfo = new EditInfo();
                editInfo.setNamespaceDeclareMap(namespaceDeclareMapForChild);
                editInfo.setTarget(childTarget.toString());
                editInfo.setOperation(operation);
                editInfos.add(editInfo);
            }
        }
    }
    
    private void updateTargetForMatchNode(EditContainmentNode editContainmentNode, Map<String, String> namespaceDeclareMap, StringBuilder targetPath) {
        String nodeNamespace = editContainmentNode.getNamespace();
        String nodePrefix = getPrefix(editContainmentNode.getSchemaRegistry(), nodeNamespace);
        targetPath.append("/");
        if(!editContainmentNode.getChangeNodes().isEmpty() && ModelNodeChangeType.delete.name().equals(editContainmentNode.getChangeNodes().get(0).getOperation())){
            targetPath.append(nodePrefix).append(":").append(editContainmentNode.getChangeNodes().get(0).getName());
        } else {
            if (!nodePrefix.isEmpty()) {
                namespaceDeclareMap.put(nodePrefix, nodeNamespace);
                targetPath.append(nodePrefix).append(":").append(editContainmentNode.getName());
            } else {
                targetPath.append(editContainmentNode.getName());
            }
        }

        for (EditMatchNode matchedNode : editContainmentNode.getMatchNodes()) {
            String matchedNodeNamespace = matchedNode.getNamespace();
            String matchedNodePrefix = getPrefix(editContainmentNode.getSchemaRegistry(), matchedNodeNamespace);

            if (matchedNodePrefix != null) {
                namespaceDeclareMap.put(matchedNodePrefix, matchedNodeNamespace);

                targetPath.append("[").append(matchedNodePrefix).append(":").append(matchedNode.getName()).append("=").append("'")
                        .append(matchedNode.getValue()).append("'").append("]");

            } else {
                targetPath.append("[").append(matchedNode.getName()).append("=").append("'").append(matchedNode.getValue()).append("'").append("]");
            }
        }
    }
    
    private String getPrefix(SchemaRegistry schemaRegistry, String namespace) {
        String prefix = schemaRegistry.getPrefix(namespace);
        if (prefix == null) {
        	NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.UNKNOWN_NAMESPACE,
    				"An unexpected namespace is present for schema registry: "+ schemaRegistry.getName());
            throw new EditConfigException(rpcError);
        }
        return prefix;
    }

    private ChangedByParams buildChangedByInfo(NetconfClientInfo clientInfo){
        if (clientInfo != null) {
            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.setSessionId(clientInfo.getSessionId());
            sessionInfo.setSourceHostIpAddress(clientInfo.getRemoteHost());
            if (clientInfo.getUsername() != null) {
                sessionInfo.setUserName(clientInfo.getUsername());
            }
            return new ChangedByParams(sessionInfo);
        }
        return null;
    }

    private Map<Object, Set<ChangeNotification>> classifyNotifications(Map<SubSystem, IndexedNotifList> subSystemNotificationMap) {
        Map<Object, Set<ChangeNotification>> classifiedChangeNotifications = new HashMap<>();
        for (Map.Entry<SubSystem, IndexedNotifList> entry : subSystemNotificationMap.entrySet()) {
            Set<ChangeNotification> unClassifiedChangeNotifications = classifiedChangeNotifications
                    .get(NetconfResources.UNCLASSIFIED_NOTIFICATIONS);
            if (unClassifiedChangeNotifications == null) {
                unClassifiedChangeNotifications = new HashSet<>(entry.getValue().list());
                classifiedChangeNotifications.put(NetconfResources.UNCLASSIFIED_NOTIFICATIONS, unClassifiedChangeNotifications);
            } else {
                unClassifiedChangeNotifications.addAll(entry.getValue().list());
            }
            SubSystem subSystem = entry.getKey();
            if (null != m_subSystemNotificationSegregators.get(subSystem)) {
                NotificationSegregator classifier = m_subSystemNotificationSegregators.get(subSystem);
                List<ChangeNotification> changeNotifications = entry.getValue().list();
                classifier.classify(classifiedChangeNotifications, changeNotifications);
            }
        }
        return classifiedChangeNotifications;
    }

    private void buildEditInfos(Collection<ChangeNotification> changeNotifications, List<EditInfo> editInfos, List<EditInfo> impliedEditInfos){
        for (ChangeNotification change : changeNotifications) {
            EditConfigChangeNotification changeNotification = (EditConfigChangeNotification) change;
            Map<String, String> namespaceDeclareMap = new HashMap<>();
            StringBuilder targetPath = new StringBuilder();
            ModelNodeChange nodeChange = changeNotification.getChange();
            ModelNodeChangeType changeOperation = nodeChange.getChangeType();
            EditContainmentNode changeDataNode = nodeChange.getChangeData();
            NamespaceContext schemaRegistry = changeDataNode.getSchemaRegistry();
            targetPath.append(changeNotification.getModelNodeId().xPathString(schemaRegistry));
            namespaceDeclareMap.putAll(changeNotification.getModelNodeId().xPathStringNsByPrefix(schemaRegistry));
            if (changeOperation.equals(ModelNodeChangeType.merge)) {
                editNotification(changeDataNode, targetPath, editInfos, schemaRegistry, namespaceDeclareMap, changeNotification.isImplied());
            } else {
                updateTargetForMatchNode(changeDataNode, namespaceDeclareMap, targetPath);
                EditInfo editInfo = new EditInfo();
                editInfo.setNamespaceDeclareMap(namespaceDeclareMap);
                editInfo.setTarget(targetPath.toString());
                editInfo.setOperation(changeOperation.name());
                if(changeNotification.isImplied()){
                    editInfo.setImplied(true);
                    impliedEditInfos.add(editInfo);
                } else {
                    editInfos.add(editInfo);
                }
            }
        }
    }

    private NetconfConfigChangeNotification buildNotification(ChangedByParams changedByParams, List<EditInfo> editInfos){
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        netconfConfigChangeNotification.setEditList(editInfos);
        netconfConfigChangeNotification.setChangedByParams(changedByParams);
        return netconfConfigChangeNotification;
    }
}
