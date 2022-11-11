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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import static junit.framework.TestCase.fail;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNodes;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigTestFailedException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.LockedByOtherSessionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.RpcRequestHandlerRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.ModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.LockServiceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.WriteLockTemplate;
import org.broadband_forum.obbaa.netconf.persistence.DataStoreMetaProvider;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class ModelServiceDeployerTest {
    public static final String COMPONENT_ID = "my-service-component-id";
    public static final String MODULE_NAME = JukeboxConstants.JUKEBOX_MODULE_NAME;
    private static final String MODULE_REVISION = JukeboxConstants.JB_REVISION;
    private ModelServiceDeployerImpl m_modelServiceDeployer;
    @Mock
    ModelService m_modelService;
    @Mock
    ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    @Mock
    ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    @Mock
    SubSystemRegistry m_subSystemRegistry;
    @Mock
    RpcRequestHandlerRegistry m_rpcRequestHandlerRegistry;
    @Mock
    ModelNodeHelperDeployer m_modelNodeHelperDeployer;
    SchemaRegistry m_schemaRegistry;
    static final Logger LOGGER = LogManager.getLogger(ModelServiceDeployerImpl.class);
    @Mock
    EntityRegistry m_entityRegistry;
    @Mock
    RootModelNodeAggregator m_rootModelNodeAggregator;
    @Mock
    DataStoreMetaProvider m_dataStoreMetaProvider;
    @Mock
    NetconfServer m_netconfServer;
    @Mock
    Module m_mockModule;
    @Mock
    SubSystem m_ss1;
    @Mock
    SubSystem m_ss2;
    @Mock
    SubSystem m_defaultSs;
    @Mock
    DeployListener m_listener1;
    @Mock
    DeployListener m_listener2;
    @Mock
    DeployListener m_listener3;

    @Before
    public void setUp() throws SchemaBuildException, ModelServiceDeployerException {
        MockitoAnnotations.initMocks(this);
        m_modelNodeHelperDeployer = mock(ModelNodeHelperDeployer.class);
        m_schemaRegistry = spy(new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService()));
        Map<String, SchemaPath> abspath = new HashMap<String, SchemaPath>();
        abspath.put("/absPath", mock(SchemaPath.class));

        Map<SchemaPath, SubSystem> ms1Subsystems = new HashMap<>();
        ms1Subsystems.put(mock(SchemaPath.class), m_ss1);
        ms1Subsystems.put(mock(SchemaPath.class), m_ss2);
        when(m_modelService.getSubSystems()).thenReturn(ms1Subsystems);
        when(m_modelService.getDefaultSubsystem()).thenReturn(m_defaultSs);

        when(m_modelService.getName()).thenReturn(COMPONENT_ID);
        when(m_modelService.getModuleName()).thenReturn(MODULE_NAME);
        when(m_modelService.getModuleRevision()).thenReturn(MODULE_REVISION);
        when(m_modelService.getAppAugmentedPaths()).thenReturn(abspath);
        m_modelServiceDeployer = Mockito.spy(new ModelServiceDeployerImpl(m_modelNodeDSMRegistry, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_rpcRequestHandlerRegistry, m_modelNodeHelperDeployer, m_schemaRegistry, spy(new ReadWriteLockServiceImpl())));
        ((ModelServiceDeployerImpl) m_modelServiceDeployer).setEntityRegistry(m_entityRegistry);
        ((ModelServiceDeployerImpl) m_modelServiceDeployer).setRootModelNodeAggregator(m_rootModelNodeAggregator);
        ((ModelServiceDeployerImpl)m_modelServiceDeployer).setDataStoreMetadataProvider(m_dataStoreMetaProvider);
        ((ModelServiceDeployerImpl)m_modelServiceDeployer).setNetconfServer(m_netconfServer);
    }

    @Test
    public void testRegisteredRelativePath() throws Exception {
        m_modelServiceDeployer.deploy(Arrays.asList(m_modelService));
        assertEquals("/absPath", m_schemaRegistry.getMatchingPath("/absPath/something"));
        assertEquals(null, m_schemaRegistry.getMatchingPath("/something"));
        verify(m_modelServiceDeployer.getReadWriteLockService()).executeWithWriteLock(Mockito.any(WriteLockTemplate.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUndeploy() throws ModelServiceDeployerException, SchemaBuildException {

        m_modelServiceDeployer.undeploy(Arrays.asList(m_modelService));

        verify(m_modelNodeDSMRegistry, never()).register((String) anyObject(), (SchemaPath) anyObject(),
                (ModelNodeDataStoreManager) anyObject());
        verify(m_modelNodeDSMRegistry, times(1)).undeploy(COMPONENT_ID);
        verify(m_modelNodeHelperRegistry,times(1)).undeploy(COMPONENT_ID);
        verify(m_subSystemRegistry,times(1)).undeploy(COMPONENT_ID);
        verify(m_rpcRequestHandlerRegistry,times(1)).undeploy(COMPONENT_ID);
        verify(m_entityRegistry, times(1)).undeploy(COMPONENT_ID);
        verify(m_schemaRegistry,times(1)).unloadSchemaContext(COMPONENT_ID, Collections.emptySet(), Collections.emptyMap());
        verify(m_rootModelNodeAggregator, times(1)).removeModelServiceRootHelpers(JukeboxConstants.JUKEBOX_SCHEMA_PATH);
        verify(m_schemaRegistry).unregisterMountPointSchemaPath(COMPONENT_ID);

        //verify undeploy notifications
        verify(m_ss1).appUndeployed();
        verify(m_ss2).appUndeployed();
        verify(m_defaultSs).appUndeployed();
    }

    @Test
    public void testAddRootContainerNodeHelper() throws ModelServiceDeployerException, SchemaBuildException {
        ContainerSchemaNode mockStateSchemaNode = mock(ContainerSchemaNode.class);
        when(mockStateSchemaNode.isConfiguration()).thenReturn(false);

        when(m_schemaRegistry.getDataSchemaNode(JukeboxConstants.JUKEBOX_SCHEMA_PATH)).thenReturn(mockStateSchemaNode);
        m_modelServiceDeployer.addRootNodeHelpers(m_modelService);
        verify(m_rootModelNodeAggregator).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH), (ChildContainerHelper) anyObject());
        verify(m_rootModelNodeAggregator, never()).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH), (ChildListHelper) anyObject());

        when(mockStateSchemaNode.isConfiguration()).thenReturn(true);
        when(m_schemaRegistry.getDataSchemaNode(JukeboxConstants.JUKEBOX_SCHEMA_PATH)).thenReturn(mockStateSchemaNode);
        m_modelServiceDeployer.addRootNodeHelpers(m_modelService);
        verify(m_rootModelNodeAggregator, times(2)).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH), (ChildContainerHelper) anyObject());
        verify(m_rootModelNodeAggregator, never()).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH), (ChildListHelper) anyObject());
    }

    @Test
    public void testOnlyRootNodeHelpersAreAdded(){
        ContainerSchemaNode mockStateSchemaNode = mock(ContainerSchemaNode.class);
        when(mockStateSchemaNode.isConfiguration()).thenReturn(true);

        when(m_schemaRegistry.getDataSchemaNode(JukeboxConstants.LIBRARY_SCHEMA_PATH)).thenReturn(mockStateSchemaNode);
        m_modelServiceDeployer.addRootNodeHelpers(m_modelService);
        verify(m_rootModelNodeAggregator, never()).addModelServiceRootHelper(eq(JukeboxConstants.LIBRARY_SCHEMA_PATH),
                (ChildContainerHelper) anyObject());
        verify(m_rootModelNodeAggregator, never()).addModelServiceRootHelper(eq(JukeboxConstants.LIBRARY_SCHEMA_PATH),
                (ChildListHelper) anyObject());
        verify(m_rootModelNodeAggregator).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH),
                (ChildContainerHelper) anyObject());
        verify(m_rootModelNodeAggregator, never()).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH),
                (ChildListHelper) anyObject());
    }

    @Test
    public void testAddRootListNodeHelper() throws ModelServiceDeployerException, SchemaBuildException {
        ListSchemaNode mockStateSchemaNode = mock(ListSchemaNode.class);
        when(mockStateSchemaNode.isConfiguration()).thenReturn(false);

        // non-config list should be ignored
        when(m_schemaRegistry.getDataSchemaNode(JukeboxConstants.JUKEBOX_SCHEMA_PATH)).thenReturn(mockStateSchemaNode);
        m_modelServiceDeployer.addRootNodeHelpers(m_modelService);
        verify(m_rootModelNodeAggregator, never()).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH), (ChildContainerHelper)
                anyObject());
        verify(m_rootModelNodeAggregator, never()).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH), (ChildListHelper) anyObject());

        when(mockStateSchemaNode.isConfiguration()).thenReturn(true);
        when(m_schemaRegistry.getDataSchemaNode(JukeboxConstants.JUKEBOX_SCHEMA_PATH)).thenReturn(mockStateSchemaNode);
        m_modelServiceDeployer.addRootNodeHelpers(m_modelService);
        verify(m_rootModelNodeAggregator, never()).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH), (ChildContainerHelper)
                anyObject());
        verify(m_rootModelNodeAggregator).addModelServiceRootHelper(eq(JukeboxConstants.JUKEBOX_SCHEMA_PATH), (ChildListHelper) anyObject());
    }

    @Test
    public void testDeployingMinimalModelService() throws ModelServiceDeployerException, LockServiceException {
        ModelService service = new ModelService();
        service.setModuleName("noname");
        service.setModuleRevision("2016-07-01");
        m_modelServiceDeployer.deploy(Arrays.asList(service));
        verify(m_modelServiceDeployer.getReadWriteLockService()).executeWithWriteLock(Mockito.any(WriteLockTemplate.class));
    }

    @Test
    public void testDeployLoadsDefaulXml() throws Exception {
        ModelService service = spy(new ModelService());
        service.setModuleName("noname");
        service.setModuleRevision("2016-07-01");
        List<Element> rootElements = Collections.singletonList(TestUtil.transformToElement("<some-root-node xmlns=\"some-namespace\">"
                + "</some-root-node>"));
        doReturn(rootElements).when(service).getDefaultSubtreeRootNodes();
        when(service.getName()).thenReturn("ms-name");
        when(m_dataStoreMetaProvider.getDataStoreVersion("ms-name")).thenReturn(0L);
        DataStore mockDs = mock(DataStore.class);
        when(m_netconfServer.getDataStore(StandardDataStores.RUNNING)).thenReturn(mockDs);

        m_modelServiceDeployer.deploy(Arrays.asList(service));

        EditConfigRequest expectedEdit = new EditConfigRequest().setConfigElement(new EditConfigElement()
                .addConfigElementContent(TestUtil.parseXml("<some-root-node xmlns=\"some-namespace\"></some-root-node>")));
        expectedEdit.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId(expectedEdit.getMessageId());
        ArgumentCaptor<EditConfigRequest> requestCaptor = ArgumentCaptor.forClass(EditConfigRequest.class);
        ArgumentCaptor<NetConfResponse> responseCaptor = ArgumentCaptor.forClass(NetConfResponse.class);
        verify(mockDs).edit(((EditConfigRequest)requestCaptor.capture()), ((NetConfResponse)responseCaptor.capture()), any(NetconfClientInfo.class));
        assertXMLEquals(expectedEdit.getConfigElement().getXmlElement(), requestCaptor.getValue().getConfigElement().getXmlElement());
        assertEquals(response.getMessageId(), responseCaptor.getValue().getMessageId());
        
        //make sure when meta is greater than 0, datastore edit is not called
        mockDs = mock(DataStore.class);
        when(m_netconfServer.getDataStore(StandardDataStores.RUNNING)).thenReturn(mockDs);
        when(m_dataStoreMetaProvider.getDataStoreVersion("ms-name")).thenReturn(1L);
        m_modelServiceDeployer.deploy(Arrays.asList(service));

        verify(mockDs, never()).edit(any(EditConfigRequest.class), any(NetConfResponse.class), isNotNull(NetconfClientInfo.class));

        //make sure when there si no default xml DS edit is not called
        doReturn(null).when(service).getDefaultSubtreeRootNodes();
        m_modelServiceDeployer.deploy(Arrays.asList(service));
        verify(mockDs, never()).edit(any(EditConfigRequest.class), any(NetConfResponse.class), isNotNull(NetconfClientInfo.class));
        verify(m_modelServiceDeployer.getReadWriteLockService(),times(3)).executeWithWriteLock(Mockito.any(WriteLockTemplate.class));
    }


    @Test
    public void testDeployWithMultipleRootsDefaultXml() throws Exception {
        ModelService service = spy(new ModelService());
        service.setModuleName("noname");
        service.setModuleRevision("2016-07-01");

        List<Element> rootElements = new ArrayList<>();
        Element rootElement1 = TestUtil.transformToElement("<rootNode1 xmlns=\"rootNode1-namespace\"></rootNode1>");
        Element rootElement2 = TestUtil.transformToElement("<rootNode2 xmlns=\"rootNode2-namespace\"></rootNode2>");
        Element rootElement3 = TestUtil.transformToElement("<rootNode3 xmlns=\"rootNode3-namespace\"></rootNode3>");

        rootElements.add(rootElement1);
        rootElements.add(rootElement2);
        rootElements.add(rootElement3);

        doReturn(rootElements).when(service).getDefaultSubtreeRootNodes();

        when(m_dataStoreMetaProvider.getDataStoreVersion("serviceName")).thenReturn(0L);
        DataStore mockDs = mock(DataStore.class);
        when(m_netconfServer.getDataStore(StandardDataStores.RUNNING)).thenReturn(mockDs);

        m_modelServiceDeployer.deploy(Arrays.asList(service));

        EditConfigRequest expectedEdit = new EditConfigRequest().setConfigElement(
                new EditConfigElement().setConfigElementContents(rootElements));
        expectedEdit.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId(expectedEdit.getMessageId());
        ArgumentCaptor<EditConfigRequest> requestCaptor = ArgumentCaptor.forClass(EditConfigRequest.class);
        ArgumentCaptor<NetConfResponse> responseCaptor = ArgumentCaptor.forClass(NetConfResponse.class);
        verify(mockDs).edit(((EditConfigRequest)requestCaptor.capture()), ((NetConfResponse)responseCaptor.capture()), any(NetconfClientInfo.class));
        assertXMLEquals(expectedEdit.getConfigElement().getXmlElement(), requestCaptor.getValue().getConfigElement().getXmlElement());
        assertEquals(response.getMessageId(), responseCaptor.getValue().getMessageId());
        verify(m_modelServiceDeployer.getReadWriteLockService()).executeWithWriteLock(Mockito.any(WriteLockTemplate.class));
    }

    @Test
    public void testSubsystemDeploy_WithSpecificSubsystems() throws ModelServiceDeployerException, LockServiceException {
        Map<SchemaPath, SubSystem> specificSubsystems = new HashedMap();
        SubSystem libSubsytem = mock(SubSystem.class);
        SubSystem artistSubsystem = mock(SubSystem.class);
        specificSubsystems.put(JukeboxConstants.LIBRARY_SCHEMA_PATH, libSubsytem);
        specificSubsystems.put(JukeboxConstants.ARTIST_SCHEMA_PATH, artistSubsystem);
        when(m_modelService.getSubSystems()).thenReturn(specificSubsystems);

        m_modelServiceDeployer.deploy(Arrays.asList(m_modelService));
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.LIBRARY_SCHEMA_PATH, libSubsytem);
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.ARTIST_SCHEMA_PATH, artistSubsystem);
        //song takes parent's subsystem
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.SONG_SCHEMA_PATH, artistSubsystem);
        verify(m_subSystemRegistry, never()).register(COMPONENT_ID, JukeboxConstants.JUKEBOX_SCHEMA_PATH, artistSubsystem);
        verify(m_modelServiceDeployer.getReadWriteLockService()).executeWithWriteLock(Mockito.any(WriteLockTemplate.class));

    }

    @Test
    public void testSubsystemDeploy_WithDefaultSubsystem() throws ModelServiceDeployerException, LockServiceException {
        SubSystem defaultSubsystem = mock(SubSystem.class);

        when(m_modelService.getDefaultSubsystem()).thenReturn(defaultSubsystem);

        m_modelServiceDeployer.deploy(Arrays.asList(m_modelService));
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.LIBRARY_SCHEMA_PATH, defaultSubsystem);
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.ARTIST_SCHEMA_PATH, defaultSubsystem);
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.JUKEBOX_SCHEMA_PATH, defaultSubsystem);
        verify(m_modelServiceDeployer.getReadWriteLockService()).executeWithWriteLock(Mockito.any(WriteLockTemplate.class));
    }

    @Test
    public void testSubsystemDeploy_WithDefaultAndSpecificSubsystems() throws ModelServiceDeployerException, LockServiceException {
        Map<SchemaPath, SubSystem> specificSubsystems = new HashedMap();
        SubSystem artistSubsystem = mock(SubSystem.class);
        specificSubsystems.put(JukeboxConstants.ARTIST_SCHEMA_PATH, artistSubsystem);
        when(m_modelService.getSubSystems()).thenReturn(specificSubsystems);
        SubSystem defaultSubsystem = mock(SubSystem.class);
        when(m_modelService.getDefaultSubsystem()).thenReturn(defaultSubsystem);

        m_modelServiceDeployer.deploy(Arrays.asList(m_modelService));
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.LIBRARY_SCHEMA_PATH, defaultSubsystem);
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.ARTIST_SCHEMA_PATH, artistSubsystem);
        //song takes parent's subsystem
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.SONG_SCHEMA_PATH, artistSubsystem);
        verify(m_subSystemRegistry).register(COMPONENT_ID, JukeboxConstants.JUKEBOX_SCHEMA_PATH, defaultSubsystem);

        //verify deploy notification
        verify(artistSubsystem).appDeployed();
        verify(defaultSubsystem).appDeployed();
        verify(m_modelServiceDeployer.getReadWriteLockService()).executeWithWriteLock(Mockito.any(WriteLockTemplate.class));
    }

    @Test
    public void testPostStartupWithException() throws Exception {
        ModelService service = spy(new ModelService());
        service.setModuleName("noname");
        service.setModuleRevision("2016-07-01");
        List<Element> rootElements = Collections
                .singletonList(TestUtil.transformToElement("<some-root-node xmlns=\"some-namespace\">" + "</some-root-node>"));
        doReturn(rootElements).when(service).getDefaultSubtreeRootNodes();
        when(service.getName()).thenReturn("ms-name");
        when(m_dataStoreMetaProvider.getDataStoreVersion("ms-name")).thenReturn(0L);

        DataStore mockDs = mock(DataStore.class);
        when(m_netconfServer.getDataStore(StandardDataStores.RUNNING)).thenReturn(mockDs);

        when(mockDs.edit(any(EditConfigRequest.class), any(NetConfResponse.class), any(NetconfClientInfo.class)))
        .thenThrow(new EditConfigException(NetconfRpcError.getBadElementError("test", NetconfRpcErrorType.Application)));
        try {
            m_modelServiceDeployer.postStartup(service);
            fail("ModelServiceDeployerException expected");
        } catch (ModelServiceDeployerException e) {
            assertEquals(((EditConfigException) e.getCause()).getRpcError(),
                    (NetconfRpcError.getBadElementError("test", NetconfRpcErrorType.Application)));
        }

        mockDs = mock(DataStore.class);
        when(m_netconfServer.getDataStore(StandardDataStores.RUNNING)).thenReturn(mockDs);
        when(mockDs.edit(any(EditConfigRequest.class), any(NetConfResponse.class), any(NetconfClientInfo.class)))
        .thenThrow(new EditConfigTestFailedException(NetconfRpcError.getBadElementError("test", NetconfRpcErrorType.Application)));
        try {
            m_modelServiceDeployer.postStartup(service);
            fail("ModelServiceDeployerException expected");
        } catch (ModelServiceDeployerException e) {
            assertEquals(((EditConfigTestFailedException) e.getCause()).getRpcError(),
                    (NetconfRpcError.getBadElementError("test", NetconfRpcErrorType.Application)));
        }

        mockDs = mock(DataStore.class);
        when(m_netconfServer.getDataStore(StandardDataStores.RUNNING)).thenReturn(mockDs);
        when(mockDs.edit(any(EditConfigRequest.class), any(NetConfResponse.class), any(NetconfClientInfo.class)))
        .thenThrow(new PersistenceException(NetconfRpcError.getBadElementError("test", NetconfRpcErrorType.Application)));
        try {
            m_modelServiceDeployer.postStartup(service);
            fail("ModelServiceDeployerException expected");
        } catch (ModelServiceDeployerException e) {
            assertEquals(((PersistenceException) e.getCause()).getRpcError(),
                    (NetconfRpcError.getBadElementError("test", NetconfRpcErrorType.Application)));
        }

        mockDs = mock(DataStore.class);
        when(m_netconfServer.getDataStore(StandardDataStores.RUNNING)).thenReturn(mockDs);
        when(mockDs.edit(any(EditConfigRequest.class), any(NetConfResponse.class), any(NetconfClientInfo.class))).thenThrow(new LockedByOtherSessionException(1));
        try {
            m_modelServiceDeployer.postStartup(service);
            fail("ModelServiceDeployerException expected");
        } catch (ModelServiceDeployerException e) {
            assertEquals(((LockedByOtherSessionException) e.getCause()).getLockOwner(), 1);
        }
    }

    @Test
    public void testDeployServicesWithModelServiceDeployerException() throws SchemaBuildException {
        doThrow(new SchemaBuildException("deploy exception")).when(m_schemaRegistry).loadSchemaContext(Mockito.anyString(), Mockito.anyList(), Mockito.anySet(),Mockito.anyMap());

        ModelService service = mock(ModelService.class);
        when(service.updateSchema()).thenReturn(true);
        try{
            m_modelServiceDeployer.deploy(Arrays.asList(service));
            fail("ModelServiceDeployerException expected");
        }catch (Exception e){
            assertTrue(e instanceof ModelServiceDeployerException);
            assertEquals("org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException: deploy exception",e.getMessage());
        }
    }

    @Test
    public void testDeployServicesWithRequestScopeExecutionException() throws SchemaBuildException {
        doThrow(new RequestScope.RsTemplate.RequestScopeExecutionException(new Exception("Request Scope Execution Exception"))).when(m_schemaRegistry).loadSchemaContext(Mockito.anyString(), Mockito.anyList(), Mockito.anySet(),Mockito.anyMap());

        ModelService service = mock(ModelService.class);
        when(service.updateSchema()).thenReturn(true);
        try{
            m_modelServiceDeployer.deploy(Arrays.asList(service));
            fail("RequestScopeExecutionException expected");
        }catch (Exception e){
            assertTrue(e instanceof LockServiceException);
            assertTrue(e.getCause() instanceof RequestScope.RsTemplate.RequestScopeExecutionException);
            assertEquals("java.lang.Exception: Request Scope Execution Exception",e.getCause().getMessage());
        }
    }

    @Test
    public void testDeployServicesWithoutModelServiceDeployerException() throws SchemaBuildException {
        doThrow(new SchemaBuildException("deploy exception")).when(m_schemaRegistry).loadSchemaContext(Mockito.anyString(), Mockito.anyList(), Mockito.anySet(),Mockito.anyMap());
        ModelService service = spy(new ModelService());
        when(service.updateSchema()).thenReturn(false);
        try{
            m_modelServiceDeployer.deploy(Arrays.asList(service));
        }catch (Exception e){
            assertFalse(e instanceof ModelServiceDeployerException);
            assertNotEquals("org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException: deploy exception",e.getMessage());
        }
    }

    @Test
    public void testUndeployIsCalledWhenDeployFails() {
        ModelService modelServiceSpy = spy(new ModelService());
        modelServiceSpy.setModuleName("someName");
        modelServiceSpy.setModuleRevision("2016-07-01");
        List<Element> rootElements = Collections.singletonList(TestUtil.transformToElement("<some-root-node xmlns=\"some-namespace\">"
                + "</some-root-node>"));
        doReturn(rootElements).when(modelServiceSpy).getDefaultSubtreeRootNodes();
        when(modelServiceSpy.getName()).thenReturn("someName");
        doThrow(new JDBCConnectionException("Error during postStartUp", new SQLException())).when(m_dataStoreMetaProvider).getDataStoreVersion("someName");
        DataStore mockDs = mock(DataStore.class);
        when(m_netconfServer.getDataStore(StandardDataStores.RUNNING)).thenReturn(mockDs);

        List<ModelService> services = Arrays.asList(modelServiceSpy);
        try {
            m_modelServiceDeployer.deploy(services);
            fail("Expected exception not thrown");
        } catch (Exception e) {
            assertTrue(e instanceof LockServiceException);
            assertEquals("org.hibernate.exception.JDBCConnectionException: Error during postStartUp", e.getMessage());
        }

        verify(m_modelServiceDeployer).undeploy(services);
    }

    @Test
    public void testUndeploySucceedsWithAllListenersExecuted() throws ModelServiceDeployerException {

        m_modelServiceDeployer.addDeployListener(m_listener1);
        m_modelServiceDeployer.addDeployListener(m_listener2);
        m_modelServiceDeployer.addDeployListener(m_listener3);

        doThrow(Exception.class).when(m_listener1).preUndeploy();
        doThrow(Exception.class).when(m_modelServiceDeployer).unloadSchemaContext(any(ModelService.class), anyString());
        doThrow(Exception.class).when(m_listener1).postUndeploy();
        doThrow(Exception.class).when(m_ss1).appUndeployed();

        m_modelServiceDeployer.undeploy(Arrays.asList(m_modelService));

        verify(m_listener1).preUndeploy();
        verify(m_listener2).preUndeploy();
        verify(m_listener3).preUndeploy();

        verify(m_modelServiceDeployer).unloadSchemaContext(m_modelService, COMPONENT_ID);

        verify(m_listener1).postUndeploy();
        verify(m_listener2).postUndeploy();
        verify(m_listener3).postUndeploy();

        verify(m_ss1).appUndeployed();
        verify(m_ss2).appUndeployed();
        verify(m_defaultSs).appUndeployed();
    }

    @Test
    public void testDeployAndUnDeploy() throws Exception{
    	ModelService ms1 = new ModelService("ibn", "2017-11-28", null, Collections.emptyMap() , Collections.emptySet(), Collections.emptySet(), Arrays.asList("/modelServiceDeployerYangs/ibn.yang", "/modelServiceDeployerYangs/ietf-yang-types.yang"));
    	ModelService ms2 = new ModelService("mpls-access-ring-hsi-infra", "2017-03-01", null, Collections.emptyMap() , Collections.emptySet(), Collections.emptySet(), Arrays.asList("/modelServiceDeployerYangs/yang-content1.yang"));
    	ModelService ms3 = new ModelService("mpls-access-ring-msi-infra", "2017-03-01", null, Collections.emptyMap() , Collections.emptySet(), Collections.emptySet(), Arrays.asList("/modelServiceDeployerYangs/yang-content2.yang"));
    	m_modelServiceDeployer.deploy(ms1);
    	m_modelServiceDeployer.deploy(ms2);
    	SchemaPath intentSP = SchemaPath.create(true, QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "ibn"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "intent"));
    	SchemaPath test1SP = SchemaPath.create(true, QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "ibn"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "intent"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "configuration"), QName.create("http://www.test-company.com/solutions/mpls-access-ring-hsi-infra", "2017-03-01", "mpls-access-ring-hsi-infra"), QName.create("http://www.test-company.com/solutions/mpls-access-ring-hsi-infra", "2017-03-01", "test1"));
    	ReferringNodes deps = m_schemaRegistry.getReferringNodesForSchemaPath(intentSP);
    	assertTrue(deps.getReferringNodes().isEmpty());
    	m_schemaRegistry.addImpactNodeForChild(intentSP, deps, Collections.EMPTY_SET);
    	assertFalse(deps.getReferringNodes().isEmpty());
    	ReferringNodes updatedDeps = m_schemaRegistry.getReferringNodesForSchemaPath(intentSP);
    	assertFalse(updatedDeps.getReferringNodes().isEmpty());
    	String namespaceOfModule = m_schemaRegistry.getNamespaceOfModule("mpls-access-ring-hsi-infra");
    	QNameModule module = QName.create(namespaceOfModule, "2017-03-01", "lol").getModule();
    	for (Map.Entry<SchemaPath, Set<ReferringNode>> referringNode : deps.getReferringNodes().entrySet()) {
    		for (ReferringNode node : referringNode.getValue()) {
    			//update only newly loaded nodes which are part of the new module
    			if(node.getReferringSP().getLastComponent().getModule().equals(module)){
    				if(node.getValidationHint() == null || node.getValidationHint().isAutoHint()) {
    					//works because schema registry would pass the same reference
    					node.setValidationHint(ValidationHint.SKIP_IMPACT_VALIDATION);
    				}
    			}
    		}
    	}
    	ReferringNodes updatedDepsWithChild = m_schemaRegistry.getReferringNodesForSchemaPath(intentSP);
    	for (Map.Entry<SchemaPath, Set<ReferringNode>> referringNode : updatedDepsWithChild.getReferringNodes().entrySet()) {
    		for (ReferringNode node : referringNode.getValue()) {
    			if(node.getReferringSP().equals(test1SP)){
    				assertEquals(ValidationHint.SKIP_IMPACT_VALIDATION, node.getValidationHint());
    			}
    		}
    	}

    	m_modelServiceDeployer.deploy(ms3);
    	assertEquals(6, m_schemaRegistry.getReferringNodesForSchemaPath(SchemaPath.create(true, QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "ibn"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "intent"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "intent-type"))).size());
    	m_modelServiceDeployer.undeploy(Arrays.asList(ms2));
    	assertEquals(3, m_schemaRegistry.getReferringNodesForSchemaPath(SchemaPath.create(true, QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "ibn"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "intent"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "intent-type"))).size());
    	m_modelServiceDeployer.undeploy(Arrays.asList(ms3));
    	assertEquals(0, m_schemaRegistry.getReferringNodesForSchemaPath(SchemaPath.create(true, QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "ibn"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "intent"), QName.create("http://www.test-company.com/solutions/ibn", "2017-11-28", "intent-type"))).size());
    }
}
