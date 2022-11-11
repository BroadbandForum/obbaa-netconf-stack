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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

public class SubsystemNotificationExecutor implements NotificationExecutor {

	public final static String CONTEXT_MAP_UPLOAD_REQUEST_KEY = "UPLOAD_REQUEST";
	public final static String CONTEXT_MAP_CLIENT_INFO_KEY = "CLIENT_INFO";

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SubsystemNotificationExecutor.class, LogAppNames.NETCONF_STACK);
	private SubSystemRegistry m_subSystemRegistry;

	public SubsystemNotificationExecutor(SubSystemRegistry subSystemRegistry) {
		m_subSystemRegistry = subSystemRegistry;
	}

	@Override
	public Map<SubSystem, IndexedNotifList> getSubSystemNotificationMap(String dataStoreName, List<NotificationInfo> notifInfos, EditConfigRequest request) {
		Map<SubSystem, IndexedNotifList> subSystemNotificationMap = new HashMap<>();
    	for(NotificationInfo notifInfo : notifInfos){
			ModelNode changedNode = notifInfo.getChangedNode();
			SubSystem subsystem = changedNode.getSubSystem();

			IndexedNotifList notificationList = subSystemNotificationMap.get(subsystem);
    		if (notificationList == null) {
    			notificationList = new IndexedNotifList();
    			subSystemNotificationMap.put(subsystem, notificationList);
    		}
    		
    		//set message id and session id. Need this for regrouping the changes later
            EditConfigChangeNotification changeNotif = new EditConfigChangeNotification(new ModelNodeId(changedNode.getModelNodeId()),
                    notifInfo.getChange(), dataStoreName, changedNode, notifInfo.getClientInfo(), notifInfo.getChildNode());
            changeNotif.setDisabledForListener(request.isUploadToPmaRequest());
			changeNotif.setImplied(notifInfo.isImplied());
    		refineAndAddNotifications(notificationList,changeNotif);
    		
    		ModelNodeChangeType changeType = changeNotif.getChange().getChangeType();
			if (changeType.equals(ModelNodeChangeType.delete) || changeType.equals(ModelNodeChangeType.remove) || changeType.equals(ModelNodeChangeType.replace)) {
    			QName childQName = notifInfo.getChange().getChangeData().getQName();
                SchemaPath schemaPath = new SchemaPathBuilder().withParent(changedNode.getModelNodeSchemaPath()).appendQName(childQName).build();
    			SubSystem childSubsystem = changedNode.getSubSystem(schemaPath);
    			if (null != childSubsystem && !childSubsystem.equals(subsystem)) {
        			IndexedNotifList childNotificationList = subSystemNotificationMap.get(childSubsystem);
            		if (childNotificationList == null) {
            			childNotificationList = new IndexedNotifList();
            			subSystemNotificationMap.put(childSubsystem, childNotificationList);
            		}
            		refineAndAddNotifications(childNotificationList,changeNotif);
    			}
    		}
    		
    	}

    	return subSystemNotificationMap;
	}
	
    @Override
    public void sendPreCommitNotifications(Map<SubSystem, IndexedNotifList> subSystemNotificationMap) throws SubSystemValidationException {
        for(Map.Entry<SubSystem, IndexedNotifList> entry : subSystemNotificationMap.entrySet()) {
            SubSystem subSystem = entry.getKey();
            if (subSystem != null){
                List<ChangeNotification> changeNotifications = entry.getValue().list();
                subSystem.notifyPreCommitChange(changeNotifications);
            }
        }
    }

	@Override
	public void sendPreCommitNotificationsV2(Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap) throws SubSystemValidationException {
		for(Map.Entry<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> entry : subSystemChangeTreeNodeMap.entrySet()) {
			SubSystem subSystem = entry.getKey();
			if (subSystem != null){
				subSystem.preCommit(entry.getValue());
			}
		}
	}
	
	@Override
	public void sendNotifications(Map<SubSystem, IndexedNotifList> subSystemNotificationMap) {
		for(Map.Entry<SubSystem,IndexedNotifList> entry : subSystemNotificationMap.entrySet()) {
			try {
				SubSystem subSystem = entry.getKey();
				if (subSystem != null){
					List<ChangeNotification> changeNotifications = entry.getValue().list();
					subSystem.notifyChanged(changeNotifications);
				}
			} catch (Exception e) {
				LOGGER.error("Error while sending notification to subsystem", e);
			}
		}
	}

	@Override
	public Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> getSubSystemChangeTreeNodeMap(WritableChangeTreeNode changeTreeNode,
																							   boolean isUploadToPmaRequest, NetconfClientInfo clientInfo) {
		Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap = new HashMap<>();
		for (SchemaPath schemaPath : changeTreeNode.getChangedNodeTypes()) {
			SubSystem subSystem = m_subSystemRegistry.lookupSubsystem(schemaPath);
			List<ChangeTreeNode> changeTreeNodes = new ArrayList<>(changeTreeNode.getNodesOfType(schemaPath));
			Map<SchemaPath, List<ChangeTreeNode>> changesMap = subSystemChangeTreeNodeMap.get(subSystem);
			if (changesMap == null) {
				changesMap = new HashMap<>();
			}
			changesMap.put(schemaPath, changeTreeNodes);
			subSystemChangeTreeNodeMap.put(subSystem, changesMap);
		}
		changeTreeNode.addContextValue(CONTEXT_MAP_UPLOAD_REQUEST_KEY,isUploadToPmaRequest);
		changeTreeNode.addContextValue(CONTEXT_MAP_CLIENT_INFO_KEY, clientInfo);
		return subSystemChangeTreeNodeMap;
	}

	@Override
	public void sendNotificationsV2(Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap) {
		for(Map.Entry<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> entry : subSystemChangeTreeNodeMap.entrySet()) {
			try {
				SubSystem subSystem = entry.getKey();
				if (subSystem != null){
					subSystem.postCommit(entry.getValue());
				}
			} catch (Exception e) {
				LOGGER.error("Error while sending notification to subsystem", e);
			}
		}
	}
	
	@Override
	public List<Element> sendNcExtensionNotifications(Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap, QName extensionQName) {
		List<Element> ncExtensionsReponsesFromAllSubsytems = new ArrayList<>();
		for(Map.Entry<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> entry : subSystemChangeTreeNodeMap.entrySet()) {
			try {
				SubSystem subSystem = entry.getKey();
				if (subSystem != null){
					Element ncExtensionsResponse = subSystem.handleNcExtensions(entry.getValue(), extensionQName);
					if (ncExtensionsResponse != null) {
						ncExtensionsReponsesFromAllSubsytems.add(ncExtensionsResponse);
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error while sending notification to subsystem", e);
			}
		}
		return ncExtensionsReponsesFromAllSubsytems;
	}
	

	public void refineNetconfConfigChangeNotification(NetconfConfigChangeNotification netconfConfigChangeNotification) {
		if (netconfConfigChangeNotification != null){
		    
		    String actualTarget = netconfConfigChangeNotification.getEditList().get(0).getTarget();
		    String[] nodeNames = actualTarget.split("/");
		    StringBuilder expectedTarget = new StringBuilder();
		    for (String nodeName : nodeNames) {
		        expectedTarget.append(nodeName);
		        if (nodeName.startsWith("adh:device[adh:device-id=")) {
		            break;
		        }
		        expectedTarget.append("/");
		    }
		    List<EditInfo> editList = new ArrayList<>();
		    EditInfo editInfo = new EditInfo();
		    editInfo.setOperation(EditConfigOperations.REPLACE);
		    editInfo.setTarget(expectedTarget.toString());
		    editList.add(editInfo);
		    
		    netconfConfigChangeNotification.setEditList(editList);
		}
	}

	private void refineAndAddNotifications(IndexedNotifList notifs,ChangeNotification notif) {
		//we add this notif only if the refined list does not contain parent notif
		if(!parentNotifExists(notifs, notif)){
			notifs.replaceChildNotifsWithParentNotif(notif);
		}
	}

	private boolean parentNotifExists(IndexedNotifList refinedNotifs, ChangeNotification childNotif) {
		List<ModelNodeRdn> rdns = new ArrayList<>(childNotif.getKey().getRdnsReadOnly());
		while (rdns.size() > 0) {
			if (refinedNotifs.map().containsKey(new ModelNodeId(rdns))) {
				return true;
			}
			if (rdns.size() == 0) {
				return false;
			}
			rdns.remove(rdns.size() - 1);
		}

		return false;
	}

}
