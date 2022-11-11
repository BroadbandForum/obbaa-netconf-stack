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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ChangedLeafInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class NbiNotificationHelperImplTest {
    
    private NbiNotificationHelper m_nbiNotificationHelper = new NbiNotificationHelperImpl();

    @Test
    public void testGetNetconfConfigChangeNotificationsForDeleteContainer() {
        NetconfClientInfo clientInfo = new NetconfClientInfo("testClient", 1);
        clientInfo.setRemoteHost("192.168.92.62");
        
        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = new HashMap<>();
        TestSubsystem subsystem = new TestSubsystem();
        
        
        ModelNodeId nodeId = new ModelNodeId("/container=container1", "namespace1");
        ModelNode changedNode = mock(ModelNode.class);
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        EditContainmentNode changeData = new TestEditContainmentNode(QName.create("namespace2", "container2"), "delete", schemaRegistry);
        EditMatchNode matchNode = new EditMatchNode(QName.create("namespace2", "2015-07-14", "name"), new GenericConfigAttribute
                ("name", "namespace2", "nameValue"));
        changeData.addMatchNode(matchNode);
        ModelNodeChange change = new ModelNodeChange(ModelNodeChangeType.delete, changeData);
        ChangeNotification changeNtf = new EditConfigChangeNotification(nodeId, change, StandardDataStores.RUNNING, changedNode);

        IndexedNotifList ntfList = getIndexedNotifList(changeNtf);
        subSystemNotificationMap.put(subsystem, ntfList);
        
        when(schemaRegistry.getPrefix("namespace1")).thenReturn("prefix1");
        when(schemaRegistry.getPrefix("namespace2")).thenReturn("prefix2");
        when(changedNode.getSchemaRegistry()).thenReturn(schemaRegistry);
        
        List<Notification> result = m_nbiNotificationHelper.getNetconfConfigChangeNotifications(
                subSystemNotificationMap, clientInfo);
        
        NetconfConfigChangeNotification netconfConfigChangeNotification = (NetconfConfigChangeNotification) result.get(0);
        String ipAddress = netconfConfigChangeNotification.getChangedByParams().getCommonSessionParams().getSourceHostIpAddress();
        String userName = netconfConfigChangeNotification.getChangedByParams().getCommonSessionParams().getUserName();
        assertEquals("192.168.92.62", ipAddress);
        assertEquals("testClient", userName);

        assertEquals(1, netconfConfigChangeNotification.getEditList().size());
        String operation = netconfConfigChangeNotification.getEditList().get(0).getOperation();
        String target = netconfConfigChangeNotification.getEditList().get(0).getTarget();
        Map<String, String> namaspaceDeclareMap = netconfConfigChangeNotification.getEditList().get(0).getNamespaceDeclareMap();
        assertEquals(namaspaceDeclareMap.get("prefix1"), "namespace1");
        assertEquals(namaspaceDeclareMap.get("prefix2"), "namespace2");
        assertEquals("delete", operation);
        assertEquals("/prefix1:container1/prefix2:container2[prefix2:name='nameValue']", target);
    }

    private IndexedNotifList getIndexedNotifList(ChangeNotification changeNtf) {
        IndexedNotifList list = new IndexedNotifList();
        list.add(changeNtf);
        return list;
    }

    @Test
    public void testGetNetconfConfigChangeNotificationsForCreateContainer() {
        NetconfClientInfo clientInfo = new NetconfClientInfo("testClient", 1);
        clientInfo.setRemoteHost("192.168.92.62");
        
        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = new HashMap<>();
        TestSubsystem subsystem = new TestSubsystem();
        
        
        ModelNodeId nodeId = new ModelNodeId("/container=container1", "namespace1");
        ModelNode changedNode = mock(ModelNode.class);
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        EditContainmentNode changeData = new TestEditContainmentNode(QName.create("namespace2", "container2"), "delete", schemaRegistry);
        EditMatchNode matchNode = new EditMatchNode(QName.create("namespace2", "2015-07-14", "name"), new GenericConfigAttribute
                ("name", "namespace2", "nameValue"));
        changeData.addMatchNode(matchNode);
        ModelNodeChange change = new ModelNodeChange(ModelNodeChangeType.create, changeData);
        ChangeNotification changeNtf = new EditConfigChangeNotification(nodeId, change, StandardDataStores.RUNNING, changedNode);

        IndexedNotifList ntfList = getIndexedNotifList(changeNtf);
        subSystemNotificationMap.put(subsystem, ntfList);
        
        when(schemaRegistry.getPrefix("namespace1")).thenReturn("prefix1");
		
        
		try {
			m_nbiNotificationHelper.getNetconfConfigChangeNotifications(subSystemNotificationMap, clientInfo);
			fail("expecting exception here");
		} catch ( EditConfigException e){
			assertEquals(NetconfRpcErrorTag.UNKNOWN_NAMESPACE, e.getRpcError().getErrorTag());
		}
        
		when(schemaRegistry.getPrefix("namespace2")).thenReturn("mountPrefix");
		m_nbiNotificationHelper.getNetconfConfigChangeNotifications(subSystemNotificationMap, clientInfo);
    }
    
    @Test
    public void testGetNetconfConfigChangeNotificationsMergeLeaf() {
        List<Notification> result = prepareNotifications(false);
        NetconfConfigChangeNotification netconfConfigChangeNotification = (NetconfConfigChangeNotification) result.get(0);
        String ipAddress = netconfConfigChangeNotification.getChangedByParams().getCommonSessionParams().getSourceHostIpAddress();
        String userName = netconfConfigChangeNotification.getChangedByParams().getCommonSessionParams().getUserName();
        assertEquals("192.168.92.62", ipAddress);
        assertEquals("testClient", userName);

        assertEquals(1, netconfConfigChangeNotification.getEditList().size());
        String operation = netconfConfigChangeNotification.getEditList().get(0).getOperation();
        String target = netconfConfigChangeNotification.getEditList().get(0).getTarget();
        Map<String, String> namaspaceDeclareMap = netconfConfigChangeNotification.getEditList().get(0).getNamespaceDeclareMap();
        assertEquals(namaspaceDeclareMap.get("prefix1"), "namespace1");
        assertEquals("create", operation);
        assertEquals("/prefix1:container1/prefix1:container2[prefix1:name='value1']", target);

        List<ChangedLeafInfo> changedLeafInfos = netconfConfigChangeNotification.getEditList().get(0).getChangedLeafInfos();
        assertEquals(1, changedLeafInfos.size());
        assertEquals("leaf1", changedLeafInfos.get(0).getName());
        assertEquals("currentValue", changedLeafInfos.get(0).getChangedValue());
        assertEquals("namespace1", changedLeafInfos.get(0).getNamespace());
        assertEquals("prefix1", changedLeafInfos.get(0).getPrefix());
    }

    private List<Notification> prepareNotifications(boolean isImplied) {
        NetconfClientInfo clientInfo = new NetconfClientInfo("testClient", 1);
        clientInfo.setRemoteHost("192.168.92.62");
        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = new HashMap<>();
        TestSubsystem subsystem = new TestSubsystem();
        ModelNodeId nodeId = new ModelNodeId("/container=container1/container=container2/name='value1'", "namespace1");
        ModelNode changedNode = mock(ModelNode.class);
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        EditContainmentNode changeData = new TestEditContainmentNode(QName.create("namespace1", "container2"), "merge", schemaRegistry);
        EditMatchNode matchNode = new EditMatchNode(QName.create("namespace1", "2015-07-14", "name"), new GenericConfigAttribute("name", "namespace1", "value1"));
        changeData.addMatchNode(matchNode);
        EditChangeNode editChangeNode = new EditChangeNode(QName.create("namespace1", "leaf1"), new GenericConfigAttribute("leaf1", "namespace1", "currentValue"));
        editChangeNode.setOperation("create");
        changeData.addChangeNode(editChangeNode);
        ModelNodeChange change = new ModelNodeChange(ModelNodeChangeType.merge, changeData);
        ChangeNotification changeNtf = new EditConfigChangeNotification(nodeId, change, StandardDataStores.RUNNING, changedNode);
        ((EditConfigChangeNotification)changeNtf).setImplied(isImplied);
        IndexedNotifList ntfList = getIndexedNotifList(changeNtf);
        subSystemNotificationMap.put(subsystem, ntfList);
        when(schemaRegistry.getPrefix("namespace1")).thenReturn("prefix1");
        when(changedNode.getSchemaRegistry()).thenReturn(schemaRegistry);
        return m_nbiNotificationHelper
                .getNetconfConfigChangeNotifications(subSystemNotificationMap, clientInfo);
    }

    @Test
    public void testNotificationHasImpliedTag(){
        List<Notification> notifications = prepareNotifications(true);
        NetconfConfigChangeNotification netconfConfigChangeNotification = (NetconfConfigChangeNotification) notifications.get(0);
        EditInfo firstEditInfo = netconfConfigChangeNotification.getEditList().get(0);
        assertEquals(true, firstEditInfo.isImplied());
    }
    
    private class TestSubsystem extends AbstractSubSystem {

        private List<ChangeNotification> m_lastNotifList;

        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        public List<ChangeNotification> getLastNotifList() {
            return m_lastNotifList;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) throws GetAttributeException {
            return null;
        }
        
        @Override
        public void notifyChanged(List<ChangeNotification> changeNotificationList) {
            m_lastNotifList = changeNotificationList;
        }

    }

}
