package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

public class SubsystemNotificationExecutor implements NotificationExecutor {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SubsystemNotificationExecutor.class, LogAppNames.NETCONF_STACK);

	@Override
	public Map<SubSystem, List<ChangeNotification>> getSubSystemNotificationMap(String dataStoreName, List<NotificationInfo> notifInfos, EditConfigRequest request) {
		Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = new HashMap<SubSystem, List<ChangeNotification>>();
		for(NotificationInfo notifInfo : notifInfos){
			ModelNode changedNode = notifInfo.getChangedNode();
			SubSystem subsystem = changedNode.getSubSystem();

			List<ChangeNotification> notificationList = subSystemNotificationMap.get(subsystem);
			if (notificationList == null) {
				notificationList = new ArrayList<ChangeNotification>();
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
				ModelNodeId childModelNodeId = changedNode.getModelNodeId().clone();
				childModelNodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, notifInfo.getChange().getChangeData().getNamespace(), notifInfo.getChange().getChangeData().getName()));
				SchemaPath schemaPath = new SchemaPathBuilder().withParent(changedNode.getModelNodeSchemaPath()).appendQName(childQName).build();
				SubSystem childSubsystem = changedNode.getSubSystem(schemaPath);
				if (null != childSubsystem && !childSubsystem.equals(subsystem)) {
					List<ChangeNotification> childNotificationList = subSystemNotificationMap.get(childSubsystem);
					if (childNotificationList == null) {
						childNotificationList = new ArrayList<ChangeNotification>();
						subSystemNotificationMap.put(childSubsystem, childNotificationList);
					}
					refineAndAddNotifications(childNotificationList,changeNotif);
				}
			}

		}

		return subSystemNotificationMap;
	}

	@Override
	public void sendPreCommitNotifications(Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap) throws SubSystemValidationException {
		for(Map.Entry<SubSystem, List<ChangeNotification>> entry : subSystemNotificationMap.entrySet()) {
			SubSystem subSystem = entry.getKey();
			if (subSystem != null){
				List<ChangeNotification> changeNotifications = entry.getValue();
				subSystem.notifyPreCommitChange(changeNotifications);
			}
		}
	}

	@Override
	public void sendNotifications(Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap) {
		for(Map.Entry<SubSystem, List<ChangeNotification>> entry : subSystemNotificationMap.entrySet()) {
			try {
				SubSystem subSystem = entry.getKey();
				if (subSystem != null){
					List<ChangeNotification> changeNotifications = entry.getValue();
					subSystem.notifyChanged(changeNotifications);
				}
			} catch (Exception e) {
				LOGGER.error("Error while sending notification to subsystem", e);
			}
		}
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

	private void refineAndAddNotifications(List<ChangeNotification> notifs,ChangeNotification notif) {
		//we add this notif only if the refined list does not contain parent notif
		if(!parentNotifExists(notifs, notif)){
			notifs.add(notif);
			//if there are already some child notifs of this notif, then we need to take the child notifs off and replace with parent notif
			replaceChildNotifsWithParentNotif(notifs, notif);

		}
	}

	private void replaceChildNotifsWithParentNotif(List<ChangeNotification> refinedNotifs, ChangeNotification parentNotif) {
		if(parentNotif instanceof EditConfigChangeNotification){
			EditConfigChangeNotification parentEditNotif = (EditConfigChangeNotification) parentNotif;
			Iterator<ChangeNotification> notifIter = refinedNotifs.iterator();
			boolean removedChildNotifs = false;
			while(notifIter.hasNext()){
				ChangeNotification notif = notifIter.next();
				if(notif instanceof EditConfigChangeNotification){
					EditConfigChangeNotification childEditNotif = (EditConfigChangeNotification)notif;
					if(isChildNotifIdBeginWithParentNotifChangeId(childEditNotif, parentEditNotif)){
						notifIter.remove();
						removedChildNotifs = true;
					}
				}
				//we don't refine copy config notifs
			}
			if(removedChildNotifs){
				refinedNotifs.add(parentNotif);
			}
		}

	}

	private boolean isChildNotifIdBeginWithParentNotifChangeId(EditConfigChangeNotification childEditNotif, EditConfigChangeNotification parentEditNotif) {
		ModelNodeId parentId = getChangeElementId(parentEditNotif);
		if (childEditNotif.getModelNodeId().beginsWith(parentId)) {
			//and the data store on which the notif is sent should be the same
			if (childEditNotif.getDataStore().equals(parentEditNotif.getDataStore())) {
				return true;
			}
		}
		return false;
	}

	// Get ModelNodeId from matchNodes
	// E.g: with below ChangeNotification, we will have ModelNodeId[/container=pma/container=device-holder/name=ALU_PMA1/container=device/device-id=R1.S1.LT1.PON1.ONT2]
	// EditConfigChangeNotification [m_modelNodeId=ModelNodeId[/container=pma/container=device-holder/name=ALU_PMA1], m_change=ModelNodeChange [m_changeType=replace, m_changeData=Containment [replace,device,urn:org:bbf:pma]
	//   Match [device-id,R1.S1.LT1.PON1.ONT2,urn:org:bbf:pma]
	//   Change [hardware-type,G.FAST,urn:org:bbf:pma]
	//   Change [interface-version,1.0,urn:org:bbf:pma]
	private ModelNodeId getChangeElementId(EditConfigChangeNotification editChangeNotification) {
		ModelNodeChangeType changeType = editChangeNotification.getChange().getChangeType();
		if (ModelNodeChangeType.merge.equals(changeType)) {
			return editChangeNotification.getModelNodeId();
		}
		ModelNodeId result = new ModelNodeId(editChangeNotification.getModelNodeId());
		EditContainmentNode changeData = editChangeNotification.getChange().getChangeData();
		String containerName = changeData.getName();
		List<EditMatchNode> matchNodes = changeData.getMatchNodes();

		result.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, changeData.getNamespace(), containerName));
		for (EditMatchNode matchNode : matchNodes) {
			result.addRdn(new ModelNodeRdn(matchNode.getName(), matchNode.getNamespace(), matchNode.getValue()));
		}

		return result;
	}

	private boolean parentNotifExists(List<ChangeNotification> refinedNotifs, ChangeNotification childNotif) {
		if(childNotif instanceof EditConfigChangeNotification){
			EditConfigChangeNotification childEditNotif = (EditConfigChangeNotification) childNotif;
			for(ChangeNotification notif : refinedNotifs){
				if(notif instanceof EditConfigChangeNotification){
					EditConfigChangeNotification editNotif = (EditConfigChangeNotification)notif;
					if(isChildNotifIdBeginWithParentNotifChangeId(childEditNotif, editNotif)){
						return true;
					}
				}
				//we don't refine copy config notifs
			}
		}

		return false;
	}

}