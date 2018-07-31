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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DynamicDataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;

public class DataStoreTest extends AbstractValidationTestSetup {

    private DataStore m_dataStore;
    private RootModelNodeAggregator m_rootAggregator;
    private SubSystemRegistry m_subsystemRegistry;
    private NetconfClientInfo m_clientInfo;
    private NotificationService m_notificationService;
    private NotificationExecutor m_editNotifcationExecutor;
    private NbiNotificationHelper m_nbiNotificationHelper;
    private EditConfigRequest m_request;
    private DataStoreValidator m_dsValidator;
    private DynamicDataStoreValidator m_dsDynamicValidator;
    Map<SubSystem, List<ChangeNotification>> m_subMap = new HashMap<>();
    private List<EditContainmentNode> m_editTrees;

    @Before
    public void setUp() throws EditConfigException {
        m_rootAggregator = mock(RootModelNodeAggregator.class);
        m_notificationService = mock(NotificationService.class);
        m_editNotifcationExecutor = mock(SubsystemNotificationExecutor.class);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        System.setProperty(DataStoreValidationUtil.NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT, "true");
        m_request = new EditConfigRequest();
        when(m_editNotifcationExecutor.getSubSystemNotificationMap(anyString(), anyList(), eq(m_request))).thenReturn
                (m_subMap);
        m_dsValidator = mock(DataStoreValidator.class);
        m_dsDynamicValidator = mock(DynamicDataStoreValidator.class);
        m_subsystemRegistry = new SubSystemRegistryImpl();
        m_clientInfo = new NetconfClientInfo("test", 1);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootAggregator, m_subsystemRegistry);
        m_dataStore.setNotificationService(m_notificationService);
        m_dataStore.setEditNotificationExecutor(m_editNotifcationExecutor);
        m_dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_dataStore.setValidator(m_dsValidator);
        m_dataStore.setDynamicValidator(m_dsDynamicValidator);
        m_editTrees = new ArrayList<EditContainmentNode>();
        m_editTrees.add(mock(EditContainmentNode.class));
        when(m_rootAggregator.editConfig(eq(m_request), any(NotificationContext.class))).thenReturn(m_editTrees);

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
        m_dataStore.copyFrom(2, new DataStore("dataSource", root, subSystemRegistry));
    }

    @Test(expected = CopyConfigException.class)
    public void testCopyFromGetException() throws Exception {
        m_dataStore.setLockOwner(2);
        DataStore store = mock(DataStore.class);
        NetconfRpcErrorTag errorTag = NetconfRpcErrorTag.getType("ErrorTag");
        NetconfRpcErrorType errorType = NetconfRpcErrorType.getType("ErrorType");
        NetconfRpcErrorSeverity errorSevrty = NetconfRpcErrorSeverity.getType("ErrorServty");
        NetconfRpcError m_rpcError = NetconfRpcErrorUtil.getNetconfRpcError(errorTag, errorType, errorSevrty, "Error " +
                "Message");
        when(store.getConfig(any(Document.class), any(FilterNode.class), any(NetconfQueryParams.class))).thenThrow
                (new GetException(m_rpcError));
        m_dataStore.copyFrom(2, store);
        verify(store).getConfig(any(Document.class), any(FilterNode.class), any(NetconfQueryParams.class));
    }

    @Test(expected = EditConfigException.class)
    public void testEditConfigException() throws Exception {
        EditConfigRequest request = new EditConfigRequest();
        EditContainmentNode editTree = mock(EditContainmentNode.class);
        request.setErrorOption(EditConfigErrorOptions.ROLLBACK_ON_ERROR);
        Command command = mock(Command.class);
        doThrow(CommandExecutionException.class).when(command).execute();
        doThrow(ValidationException.class).when(m_dataStore.getValidator()).validate(m_rootAggregator, editTree,
                request, m_clientInfo);
        m_dataStore.validateDataStores(editTree, request, m_clientInfo);
        verify(command).execute();
        verify(m_dataStore.getValidator()).validate(m_rootAggregator, any(EditContainmentNode.class), request,
                m_clientInfo);

    }


    @Test
    public void testDataStoreContactsCollaboratorsOnEdit() throws Exception {
        m_dataStore.edit(m_request, m_clientInfo);
        verify(m_rootAggregator).editConfig(eq(m_request), any(NotificationContext.class));
        verify(m_editNotifcationExecutor).getSubSystemNotificationMap(eq(StandardDataStores.RUNNING), anyList(), eq
                (m_request));
        verify(m_editNotifcationExecutor, times(1)).sendNotifications(m_subMap);
        verify(m_nbiNotificationHelper, times(1)).getNetconfConfigChangeNotifications(m_subMap, m_clientInfo, null);
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), m_request, m_clientInfo);
        Assert.assertEquals(m_dsValidator, m_dataStore.getValidator());
        NamespaceContext ctx = mock(NamespaceContext.class);
        m_dataStore.setNamespaceContext(ctx);
        assertEquals(ctx, m_dataStore.getNamespaceContext());
        assertEquals(m_nbiNotificationHelper, m_dataStore.getNbiNotificationHelper());

    }

    @Test
    public void testSentNotificationWithUploadToPmaRequest() throws Exception {
        m_request.setUploadToPmaRequest();
        List<Notification> notifications = new ArrayList<>();
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        notifications.add(netconfConfigChangeNotification);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(m_subMap, m_clientInfo, null)).thenReturn
                (notifications);
        m_dataStore.edit(m_request, m_clientInfo);
        verify(m_rootAggregator).editConfig(eq(m_request), any(NotificationContext.class));
        verify(m_editNotifcationExecutor).getSubSystemNotificationMap(eq(StandardDataStores.RUNNING), anyList(), eq
                (m_request));
        verify(m_editNotifcationExecutor, times(1)).sendNotifications(m_subMap);
        verify(m_nbiNotificationHelper, times(1)).getNetconfConfigChangeNotifications(m_subMap, m_clientInfo, null);
        verify((SubsystemNotificationExecutor) m_editNotifcationExecutor, times(1))
                .refineNetconfConfigChangeNotification(netconfConfigChangeNotification);
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), m_request, m_clientInfo);
    }

    @Test
    public void testDataStoreContactValidatorOnEdit() throws Exception {
        System.setProperty(DataStoreValidationUtil.NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT, "true");
        m_dataStore.edit(m_request, m_clientInfo);
        verify(m_rootAggregator).editConfig(eq(m_request), any(NotificationContext.class));
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), m_request, m_clientInfo);
    }

    @Test
    public void testSentNotification() throws Exception {
        List<Notification> notifications = new ArrayList<>();
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        notifications.add(netconfConfigChangeNotification);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(m_subMap, m_clientInfo, null)).thenReturn
                (notifications);
        m_dataStore.edit(m_request, m_clientInfo);
        verify(m_rootAggregator).editConfig(eq(m_request), any(NotificationContext.class));
        verify(m_editNotifcationExecutor).getSubSystemNotificationMap(eq(StandardDataStores.RUNNING), anyList(), eq
                (m_request));
        verify(m_editNotifcationExecutor, times(1)).sendNotifications(m_subMap);
        verify(m_nbiNotificationHelper, times(1)).getNetconfConfigChangeNotifications(m_subMap, m_clientInfo, null);
        verify((SubsystemNotificationExecutor) m_editNotifcationExecutor, times(0))
                .refineNetconfConfigChangeNotification(netconfConfigChangeNotification);
        verify(m_dsValidator).validate(m_rootAggregator, m_editTrees.get(0), m_request, m_clientInfo);
    }

    @Test
    public void testSendImpliedNotif() throws Exception {
        List<Notification> notifications = new ArrayList<>();
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        notifications.add(netconfConfigChangeNotification);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(any(), any(), any())).thenReturn
                (notifications);
        //assume that post edit validation internally makes another change
        when(m_dsValidator.validate(m_rootAggregator, m_editTrees.get(0), m_request, m_clientInfo)).thenReturn(Arrays
                .asList(new NetconfConfigChangeNotification()));
        List<Notification> toBeSentToNBI = m_dataStore.edit(m_request, m_clientInfo);
        //then we will have 2 notifications which will be sent to NBI 1 is implied, 1 is normal
        assertEquals(2, toBeSentToNBI.size());
    }

    @Test
    public void testNotSendImpliedNotif() throws Exception {
        List<Notification> notifications = new ArrayList<>();
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        notifications.add(netconfConfigChangeNotification);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(any(), any(), any())).thenReturn
                (notifications);
        //assume that post edit validation does NOT internally makes another change
        when(m_dsValidator.validate(m_rootAggregator, m_editTrees.get(0), m_request, m_clientInfo)).thenReturn(null);
        List<Notification> toBeSentToNBI = m_dataStore.edit(m_request, m_clientInfo);
        //then we will have only 1 notifications which will be sent to NBI
        assertEquals(1, toBeSentToNBI.size());
    }

    @Override
    public void setup() throws Exception {

    }
}

