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
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.SessionInfo;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;

public class NbiNotificationHelperImpl implements NbiNotificationHelper {
    
    private Map<SubSystem, SubsystemNotificationClassifier> m_classifiedSubsystemNotifications = new HashMap<>();
    
    public void registerClassifier(SubSystem subsytem, SubsystemNotificationClassifier classifier){
        m_classifiedSubsystemNotifications.put(subsytem, classifier);
    }

    public List<Notification> getNetconfConfigChangeNotifications(Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap,
            NetconfClientInfo clientInfo, NamespaceContext namespaceContext) {
        List<Notification> netconfConfigChangeNotifications = new ArrayList<>();
        ChangedByParams changedByParams = buildChangedByInfo(clientInfo);
        Map<Object, Set<ChangeNotification>> classifiedChangeNotifications = classifyNotifications(subSystemNotificationMap);
        for (Map.Entry<Object, Set<ChangeNotification>> entry : classifiedChangeNotifications.entrySet()) {
            List<EditInfo> editInfos = new ArrayList<>();
            List<EditInfo> impliedEditInfos = new ArrayList<>();
            buildEditInfos(entry.getValue(), namespaceContext, editInfos, impliedEditInfos);
            if (!editInfos.isEmpty()) {
                netconfConfigChangeNotifications.add(buildNotification(changedByParams, editInfos));
            }
            if (!impliedEditInfos.isEmpty()) {
                netconfConfigChangeNotifications.add(buildNotification(changedByParams, impliedEditInfos));
            }
        }
        return netconfConfigChangeNotifications;
    }
    
    private void editNotification(EditContainmentNode changeDataNode, StringBuilder targetPath, List<EditInfo> editInfos,
                                  NamespaceContext namespaceContext, Map<String, String> namespaceDeclareMap, boolean isImplied, ModelNode modelNode) {
        List<EditChangeNode> editChangeNodes = changeDataNode.getChangeNodes();
        Map<String, EditInfo> editInfoMap = new HashMap<>();
        for (EditChangeNode editChangeNode : editChangeNodes) {
            String changeNodeNamespace = editChangeNode.getNamespace();
            String changeNodePrefix = getPrefix(namespaceContext, changeNodeNamespace, modelNode);
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
            updateTargetForMatchNode(childNode, namespaceDeclareMapForChild, namespaceContext, childTarget, modelNode);
            if (childNode.getEditOperation().equals(ModelNodeChangeType.merge.name())) {
                childTarget.append("/");
                editNotification(childNode, childTarget, editInfos, namespaceContext, namespaceDeclareMapForChild, isImplied, modelNode);
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
    
    private void updateTargetForMatchNode(EditContainmentNode editContainmentNode, Map<String, String> namespaceDeclareMap,
            NamespaceContext namespaceContext, StringBuilder targetPath, ModelNode modelNode) {
        String nodeNamespace = editContainmentNode.getNamespace();
        String nodePrefix = getPrefix(namespaceContext, nodeNamespace, modelNode);
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
            String matchedNodePrefix = getPrefix(namespaceContext, matchedNodeNamespace, modelNode);

            if (matchedNodePrefix != null) {
                namespaceDeclareMap.put(matchedNodePrefix, matchedNodeNamespace);

                targetPath.append("[").append(matchedNodePrefix).append(":").append(matchedNode.getName()).append("=").append("'")
                        .append(matchedNode.getValue()).append("'").append("]");

            } else {
                targetPath.append("[").append(matchedNode.getName()).append("=").append("'").append(matchedNode.getValue()).append("'").append("]");
            }
        }
    }
    
    private String getPrefix(NamespaceContext namespaceContext, String namespace, ModelNode modelNode) {
        String returnValue = "";
        if (namespaceContext != null) {
            returnValue = namespaceContext.getPrefix(namespace);
        }
        
        if (returnValue == null) {
            NamespaceContext mountContext = SchemaRegistryUtil.getMountRegistry();
            if (mountContext != null) {
            	returnValue =  mountContext.getPrefix(namespace);
            	if ( returnValue != null){
            		return returnValue;
            	}
            	if (modelNode != null) {
            		mountContext = modelNode.getMountRegistry();
            		return mountContext.getPrefix(namespace);
            	}
            }
        } else {
            return returnValue;
        }
        return "";
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

    private Map<Object, Set<ChangeNotification>> classifyNotifications(Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap) {
        Map<Object, Set<ChangeNotification>> classifiedChangeNotifications = new HashMap<>();
        for (Map.Entry<SubSystem, List<ChangeNotification>> entry : subSystemNotificationMap.entrySet()) {
            Set<ChangeNotification> unClassifiedChangeNotifications = classifiedChangeNotifications
                    .get(NetconfResources.UNCLASSIFIED_NOTIFICATIONS);
            if (unClassifiedChangeNotifications == null) {
                unClassifiedChangeNotifications = new HashSet<>(entry.getValue());
                classifiedChangeNotifications.put(NetconfResources.UNCLASSIFIED_NOTIFICATIONS, unClassifiedChangeNotifications);
            } else {
                unClassifiedChangeNotifications.addAll(entry.getValue());
            }
            SubSystem subSystem = entry.getKey();
            if (null != m_classifiedSubsystemNotifications.get(subSystem)) {
                SubsystemNotificationClassifier classifier = m_classifiedSubsystemNotifications.get(subSystem);
                List<ChangeNotification> changeNotifications = entry.getValue();
                classifier.classify(classifiedChangeNotifications, changeNotifications);
            }
        }
        return classifiedChangeNotifications;
    }

    private void buildEditInfos(Collection<ChangeNotification> changeNotifications, NamespaceContext namespaceContext, List<EditInfo> editInfos, List<EditInfo> impliedEditInfos){
        for (ChangeNotification change : changeNotifications) {
            EditConfigChangeNotification changeNotification = (EditConfigChangeNotification) change;
            Map<String, String> namespaceDeclareMap = new HashMap<>();
            StringBuilder targetPath = new StringBuilder();
            targetPath.append(changeNotification.getModelNodeId().xPathString(namespaceContext));
            ModelNodeChange nodeChange = changeNotification.getChange();
            ModelNodeChangeType changeOperation = nodeChange.getChangeType();
            EditContainmentNode changeDataNode = nodeChange.getChangeData();
            if (namespaceContext != null) {
                namespaceDeclareMap.putAll(changeNotification.getModelNodeId().xPathStringNsByPrefix(namespaceContext));
            }
            if (changeOperation.equals(ModelNodeChangeType.merge)) {
                editNotification(changeDataNode, targetPath, editInfos, namespaceContext, namespaceDeclareMap, changeNotification.isImplied(), changeNotification.getChangedNode());
            } else {
                updateTargetForMatchNode(changeDataNode, namespaceDeclareMap, namespaceContext, targetPath, changeNotification.getChangedNode());
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
