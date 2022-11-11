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

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

public interface NotificationExecutor {

	Map<SubSystem, IndexedNotifList> getSubSystemNotificationMap(String dataStoreName, List<NotificationInfo> notifInfos, EditConfigRequest request);

	void sendPreCommitNotifications(Map<SubSystem, IndexedNotifList> subSystemNotificationMap) throws SubSystemValidationException;

	void sendNotifications(Map<SubSystem, IndexedNotifList> subSystemNotificationMap);

	void sendPreCommitNotificationsV2(Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap) throws SubSystemValidationException;

	void sendNotificationsV2(Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap);

	Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> getSubSystemChangeTreeNodeMap(WritableChangeTreeNode changeTreeNode, boolean isUploadToPmaRequest, NetconfClientInfo clientInfo);
	
	List<Element> sendNcExtensionNotifications(Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap, QName extensionQName);

}
