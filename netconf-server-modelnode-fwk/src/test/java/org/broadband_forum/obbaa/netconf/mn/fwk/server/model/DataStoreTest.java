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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.ENABLE_NEW_NOTIF_STRUCTURE;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.resetSystemProperty;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.setSystemPropertyAndReturnResetValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.auth.spi.AuthorizationHandler;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.spi.DefaultAuthorizationHandler;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DynamicDataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;

@RunWith(RequestScopeJunitRunner.class)
public class DataStoreTest extends AbstractValidationTestSetup {

    private DataStore m_dataStore;
    private RootModelNodeAggregator m_rootAggregator;
    private SubSystemRegistry m_subsystemRegistry;
    private NetconfClientInfo m_clientInfo;
    private NotificationService m_notificationService;
    private NotificationExecutor m_editNotifcationExecutor;
    private NbiNotificationHelper m_nbiNotificationHelper;
    private EditConfigRequest m_request;
    private NetConfResponse m_response;
    private DataStoreValidator m_dsValidator;
    private DynamicDataStoreValidator m_dsDynamicValidator;
    Map<SubSystem, IndexedNotifList> m_subMap = new HashMap<>();
    private List<EditContainmentNode> m_editTrees;

    @Before
    public void setUp() throws EditConfigException {
        m_rootAggregator = mock(RootModelNodeAggregator.class);
        m_notificationService = mock(NotificationService.class);
        m_editNotifcationExecutor = mock(SubsystemNotificationExecutor.class);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        System.setProperty(DataStoreValidationUtil.NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT, "true");
        m_clientInfo = new NetconfClientInfo("test", 1);
        m_request = createEditRequest();
        m_response = new NetConfResponse().setMessageId(m_request.getMessageId());
        when(m_editNotifcationExecutor.getSubSystemNotificationMap(anyString(), anyList(), eq(m_request))).thenReturn(m_subMap);
        m_dsValidator = mock(DataStoreValidator.class);
        m_dsDynamicValidator = mock(DynamicDataStoreValidator.class);
        m_subsystemRegistry = new SubSystemRegistryImpl();
        m_subsystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        when(m_rootAggregator.getSubsystemRegistry()).thenReturn(m_subsystemRegistry);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootAggregator, m_subsystemRegistry,m_dsValidator);
        m_dataStore.setNotificationService(m_notificationService);
        m_dataStore.setEditNotificationExecutor(m_editNotifcationExecutor);
        m_dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_dataStore.setValidator(m_dsValidator);
        m_dataStore.setDynamicValidator(m_dsDynamicValidator);
        m_editTrees = new ArrayList<EditContainmentNode>();
        m_editTrees.add(mock(EditContainmentNode.class));
        when(m_rootAggregator.editConfig(eq(m_request), any(NotificationContext.class))).thenReturn(m_editTrees);

    }
    
    private EditConfigRequest createEditRequest(){
    	EditConfigRequest request = new EditConfigRequest();
    	request.setTarget(StandardDataStores.RUNNING);
    	request.setDefaultOperation(EditConfigDefaultOperations.MERGE);
    	request.setTestOption("set");
    	request.setErrorOption(EditConfigErrorOptions.CONTINUE_ON_ERROR);
    	request.setConfigElement(new EditConfigElement());
    	request.setMessageId("101");
    	request.setClientInfo(m_clientInfo);
    	return request;
    }

    @Test
    @Ignore
    public void testEditRequestThrowsException() throws Exception {
        m_request = new EditConfigRequest();
        m_request.setRpcElement(DocumentUtils.stringToDocumentElement("<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"6\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <anv:device-manager xmlns:anv=\"http://www.test-company.com/solutions/anv\">\n" +
                "        <adh:device xmlns:adh=\"http://www.test-company.com/solutions/anv-device-holders\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"replace\">\n" +
                "          <adh:device-id>R1.S1.LT1.PON1.ONT1</adh:device-id>\n" +
                "          <adh:hardware-type>DPU-CFAS-H</adh:hardware-type>\n" +
                "          <adh:interface-version>20A.03</adh:interface-version>\n" +
                "          <adh:duid>MAC-FC12-32AC-23DE</adh:duid>\n" +
                "        </adh:device>\n" +
                "        </anv:device-manager>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>"));
        AuthorizationHandler authorizationHandler = new DefaultAuthorizationHandler();
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> changeTreeNodeMap = new HashMap<>();
        changeTreeNodeMap.put(new DummySubSystem(authorizationHandler), new HashMap<>());
        Map<SubSystem, IndexedNotifList> notificationMap = new HashMap<>();
        notificationMap.put(new DummySubSystem(authorizationHandler), mock(IndexedNotifList.class));
        when(m_editNotifcationExecutor.getSubSystemChangeTreeNodeMap(null, false, m_clientInfo)).thenReturn(changeTreeNodeMap);
        when(m_editNotifcationExecutor.getSubSystemNotificationMap("running", new ArrayList<>(), m_request)).thenReturn(notificationMap);
        try {
            m_dataStore.edit(m_request, m_response, m_clientInfo);
            fail("EditConfigException expected");
        } catch (EditConfigException e) {
            assertEquals("Operation 'edit-config' not authorized for user 'test'", e. getMessage());
        }
    }

    @Test(expected = LockDeniedUncommitedChangesException.class)
    public void testUnCommitedExceptions() throws Exception {
        m_dataStore.setHasUnCommitedChanges(true);
        m_dataStore.lock(1);
    }

    @Test(expected = LockDeniedConfirmedCommitException.class)
    public void testLockDeniedConfirmedCommitException() throws Exception {
        m_dataStore.setLockOwner(1);
        m_dataStore.setconfirmedCommitPending(true);
        m_dataStore.lock(2);
    }

    @Test(expected = LockedByOtherSessionException.class)
    public void testCopyFromLockedByOtherSessionException() throws Exception {
        m_dataStore.setLockOwner(1);
        m_dataStore.setconfirmedCommitPending(true);
        RootModelNodeAggregator root = mock(RootModelNodeAggregator.class);
        SubSystemRegistry subSystemRegistry = mock(SubSystemRegistry.class);
        when(root.getSubsystemRegistry()).thenReturn(subSystemRegistry);
        when(subSystemRegistry.getCompositeSubSystem()).thenReturn(mock(CompositeSubSystem.class));
        m_dataStore.copyFrom(new NetconfClientInfo("admin", 2), new DataStore("dataSource", root, subSystemRegistry));
    }

    @Rule
    public ExpectedException m_thrown = ExpectedException.none();

    @Test
    public void testCopyFromGetException() throws Exception {
        m_thrown.expect(CopyConfigException.class);
        m_thrown.expectMessage("Error Message");

        m_dataStore.setLockOwner(2);
        DataStore store = mock(DataStore.class);
        NetconfRpcErrorTag errorTag = NetconfRpcErrorTag.getType("ErrorTag");
        NetconfRpcErrorType errorType = NetconfRpcErrorType.getType("ErrorType");
        NetconfRpcErrorSeverity errorSevrty = NetconfRpcErrorSeverity.getType("ErrorServty");
        NetconfRpcError m_rpcError = NetconfRpcErrorUtil.getNetconfRpcError(errorTag, errorType, errorSevrty, "Error Message");
        when(store.getConfig(any(NetconfClientInfo.class), any(Document.class), any(FilterNode.class), any(NetconfQueryParams.class))).thenThrow(new GetException(m_rpcError));
        m_dataStore.copyFrom(new NetconfClientInfo("admin", 2), store);
        verify(store).getConfig(any(NetconfClientInfo.class), any(Document.class), any(FilterNode.class), any(NetconfQueryParams.class));
    }

    @Test
    public void testEditConfigException() throws Exception {
        m_thrown.expect(EditConfigException.class);
        m_thrown.expectMessage("error");

        EditConfigRequest request = new EditConfigRequest();
        EditContainmentNode editTree = mock(EditContainmentNode.class);
        request.setErrorOption(EditConfigErrorOptions.ROLLBACK_ON_ERROR);
        Command command = mock(Command.class);
        doThrow(CommandExecutionException.class).when(command).execute();
        doThrow(new ValidationException(new NetconfRpcError(null, null, null, "error"))).when(m_dataStore.getValidator()).validate(m_rootAggregator, editTree, null, request, m_clientInfo);
        m_dataStore.validateDataStores(editTree, request, m_clientInfo);
        verify(command).execute();
        verify(m_dataStore.getValidator()).validate(m_rootAggregator, any(EditContainmentNode.class), null, request, m_clientInfo);

    }


    @Test
    public void testDataStoreContactsCollaboratorsOnEdit() throws Exception {
        m_dataStore.edit(m_request, m_response, m_clientInfo);
        verify(m_rootAggregator).editConfig(eq(m_request), any(NotificationContext.class));
        verify(m_editNotifcationExecutor, times(2)).getSubSystemNotificationMap(eq(StandardDataStores.RUNNING), anyList(), eq(m_request));
        verify(m_editNotifcationExecutor, times(1)).sendNotifications(m_subMap);
        verify(m_nbiNotificationHelper, times(1)).getNetconfConfigChangeNotifications(m_subMap, m_request.getClientInfo());
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), null, m_request, m_clientInfo);
        assertEquals(m_dsValidator, m_dataStore.getValidator());
        NamespaceContext ctx = mock(NamespaceContext.class);
        m_dataStore.setNamespaceContext(ctx);
        assertEquals(ctx, m_dataStore.getNamespaceContext());
        assertEquals(m_nbiNotificationHelper, m_dataStore.getNbiNotificationHelper());

    }

    @Test
    public void testSentNotificationWithUploadToPmaRequest() throws Exception {
        m_request.setUploadToPmaRequest();
        m_request.setClientInfo(m_clientInfo);
        List<Notification> notifications = new ArrayList<>();
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        notifications.add(netconfConfigChangeNotification);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(m_subMap, m_clientInfo)).thenReturn(notifications);
        m_dataStore.edit(m_request, m_response, m_clientInfo);
        verify(m_rootAggregator).editConfig(eq(m_request), any(NotificationContext.class));
        verify(m_editNotifcationExecutor, times(2)).getSubSystemNotificationMap(eq(StandardDataStores.RUNNING), anyList(), eq(m_request));
        verify(m_editNotifcationExecutor, times(1)).sendNotifications(m_subMap);
        verify(m_nbiNotificationHelper, times(1)).getNetconfConfigChangeNotifications(m_subMap, m_clientInfo);
        verify((SubsystemNotificationExecutor) m_editNotifcationExecutor, times(1)).refineNetconfConfigChangeNotification(netconfConfigChangeNotification);
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), null, m_request, m_clientInfo);
    }

    @Test
    public void testDataStoreContactValidatorOnEdit() throws Exception {
        System.setProperty(DataStoreValidationUtil.NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT, "true");
        m_dataStore.edit(m_request, m_response, m_clientInfo);
        verify(m_rootAggregator).editConfig(eq(m_request), any(NotificationContext.class));
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), null, m_request, m_clientInfo);
    }

    @Test
    public void testNotifStructureSwitchUsingChangeTreeNode() throws Exception {
        String previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
        boolean toBeReset = setSystemPropertyAndReturnResetValue(previousValue, "true");
        m_dataStore.edit(m_request, m_response, m_clientInfo);
        verify(m_editNotifcationExecutor, times(2)).getSubSystemChangeTreeNodeMap(any(WritableChangeTreeNode.class), eq(false), eq(m_clientInfo));
        verify(m_editNotifcationExecutor).sendPreCommitNotificationsV2(anyMap());
        verify(m_editNotifcationExecutor).sendNotificationsV2(anyMap());
        verify(m_editNotifcationExecutor, never()).getSubSystemNotificationMap(anyString(), anyList(), any(EditConfigRequest.class));
        verify(m_editNotifcationExecutor, never()).sendPreCommitNotifications(anyMap());
        verify(m_editNotifcationExecutor, never()).sendNotifications(anyMap());
        resetSystemProperty(toBeReset, previousValue);
    }

    @Test
    public void testNotifStructureSwitchUsingNotificationInfo() throws Exception {
        String previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
        boolean toBeReset = setSystemPropertyAndReturnResetValue(previousValue, "false");
        m_dataStore.edit(m_request, m_response, m_clientInfo);
        verify(m_editNotifcationExecutor, times(2)).getSubSystemNotificationMap(anyString(), anyList(), any(EditConfigRequest.class));
        verify(m_editNotifcationExecutor).sendPreCommitNotifications(anyMap());
        verify(m_editNotifcationExecutor).sendNotifications(anyMap());
        verify(m_editNotifcationExecutor, never()).getSubSystemChangeTreeNodeMap(any(WritableChangeTreeNode.class), any(Boolean.class), any(NetconfClientInfo.class));
        verify(m_editNotifcationExecutor, never()).sendPreCommitNotificationsV2(anyMap());
        verify(m_editNotifcationExecutor, never()).sendNotificationsV2(anyMap());
        resetSystemProperty(toBeReset, previousValue);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testSendNotification() throws Exception {
        String previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
        boolean toBeReset = setSystemPropertyAndReturnResetValue(previousValue, "false");
        List<Notification> notifications = new ArrayList<>();
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        notifications.add(netconfConfigChangeNotification);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(m_subMap, m_clientInfo)).thenReturn(notifications);
        m_dataStore.edit(m_request, m_response, m_clientInfo);
        verify(m_rootAggregator).editConfig(eq(m_request), any(NotificationContext.class));
        verify(m_editNotifcationExecutor, times(2)).getSubSystemNotificationMap(eq(StandardDataStores.RUNNING), anyList(), eq(m_request));
        verify(m_editNotifcationExecutor, times(1)).sendNotifications(m_subMap);
        verify(m_nbiNotificationHelper, times(1)).getNetconfConfigChangeNotifications(m_subMap, m_request.getClientInfo());
        verify((SubsystemNotificationExecutor) m_editNotifcationExecutor, times(0)).refineNetconfConfigChangeNotification(netconfConfigChangeNotification);
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), null, m_request, m_clientInfo);
        verify(m_editNotifcationExecutor, times(0)).sendNcExtensionNotifications(any(Map.class), eq(NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME));
        resetSystemProperty(toBeReset, previousValue);
    }

    public void testDoNotSendNotification_WhenDSIsFullyRebuilt() throws Exception {
        System.setProperty(NetconfResources.IS_FULL_DS_REBUILT, "true");
        String previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
        boolean toBeReset = setSystemPropertyAndReturnResetValue(previousValue, "false");
        m_dataStore.edit(m_request, m_response, m_clientInfo);
        verify(m_editNotifcationExecutor).getSubSystemNotificationMap(eq(StandardDataStores.RUNNING), anyList(), eq(m_request));
        verify(m_editNotifcationExecutor, never()).sendNotifications(m_subMap);
        verify(m_nbiNotificationHelper, never()).getNetconfConfigChangeNotifications(m_subMap, m_request.getClientInfo());
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), null, m_request, m_clientInfo);
        resetSystemProperty(toBeReset, previousValue);
        System.setProperty(NetconfResources.IS_FULL_DS_REBUILT, "false");
    }

    @Test
    public void testDoNotSendNotification_WhenDSIsFullyRebuilt_NewNotifStr() throws Exception {
        System.setProperty(NetconfResources.IS_FULL_DS_REBUILT, "true");
        String previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
        boolean toBeReset = setSystemPropertyAndReturnResetValue(previousValue, "true");
        m_dataStore.edit(m_request, m_response, m_clientInfo);
        verify(m_editNotifcationExecutor, never()).sendNotificationsV2(anyMap());
        verify(m_editNotifcationExecutor, never()).getSubSystemNotificationMap(anyString(), anyList(), any(EditConfigRequest.class));
        verify(m_editNotifcationExecutor, never()).sendPreCommitNotifications(anyMap());
        verify(m_editNotifcationExecutor, never()).sendNotifications(anyMap());
        resetSystemProperty(toBeReset, previousValue);
        System.setProperty(NetconfResources.IS_FULL_DS_REBUILT, "false");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testSendNcExtensionNotification_Synchronize_True() throws Exception {
        EditConfigRequest request = createEditRequest();
        request.setTriggerSyncUponSuccess(true);
        NetConfResponse response = new NetConfResponse().setMessageId(request.getMessageId());
        m_dataStore.edit(request, response, m_clientInfo);
        verify(m_editNotifcationExecutor, times(1)).sendNcExtensionNotifications(any(Map.class), eq(NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testSendNcExtensionNotification_Synchronize_False() throws Exception {
        EditConfigRequest request = createEditRequest();
        request.setTriggerSyncUponSuccess(false);
        NetConfResponse response = new NetConfResponse().setMessageId(request.getMessageId());
        m_dataStore.edit(request, response, m_clientInfo);
        verify(m_editNotifcationExecutor, times(0)).sendNcExtensionNotifications(any(Map.class), eq(NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME));
    }

    @Test
    public void testSendImpliedNotif() throws Exception {
        m_request.setClientInfo(m_clientInfo);
        List<Notification> notifications = new ArrayList<>();
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        notifications.add(netconfConfigChangeNotification);
        // Create a internal request
        EditConfigRequest internalEditRequest = createEditRequest();
        internalEditRequest.setDefaultOperation(EditConfigDefaultOperations.REPLACE);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(anyMap(), any())).thenReturn(notifications);
        //assume that post edit validation internally makes another change
        when(m_dsValidator.validate(m_rootAggregator, m_editTrees.get(0), null, m_request, m_clientInfo)).thenReturn(Arrays.asList(internalEditRequest));
        List<Notification> toBeSentToNBI = m_dataStore.edit(m_request, m_response, m_clientInfo);
        //then we will have 2 notifications which will be sent to NBI 1 is implied, 1 is normal
        assertEquals(2, toBeSentToNBI.size());
    }

    @Test
    public void testNotSendImpliedNotif() throws Exception {
        System.setProperty(ENABLE_NEW_NOTIF_STRUCTURE, "false");
        List<Notification> notifications = new ArrayList<>();
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        notifications.add(netconfConfigChangeNotification);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(anyMap(), any())).thenReturn(notifications);
        //assume that post edit validation does NOT internally makes another change
        when(m_dsValidator.validate(m_rootAggregator, m_editTrees.get(0), null, m_request, m_clientInfo)).thenReturn(null);
        List<Notification> toBeSentToNBI = m_dataStore.edit(m_request, m_response, m_clientInfo);
        //then we will have only 1 notifications which will be sent to NBI
        assertEquals(1, toBeSentToNBI.size());
    }

    @Test(expected = LockedByOtherSessionException.class)
    public void testEditConfigFromLockedByOtherSessionException() throws Exception {
        m_dataStore.setLockOwner(2);
        m_dataStore.edit(m_request, m_response, m_clientInfo);
    }

    @Override
    public void setup() throws Exception {

    }

    private class DummySubSystem extends AbstractSubSystem {
        public DummySubSystem(AuthorizationHandler authorizationHandler) {
            super(authorizationHandler);
        }
    }
}

