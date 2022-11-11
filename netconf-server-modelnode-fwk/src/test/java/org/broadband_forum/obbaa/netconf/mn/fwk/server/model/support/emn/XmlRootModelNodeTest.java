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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.getByteSources;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetWithDepth;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaVerifierImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.HintDetails;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelperImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelNodeDSMDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaNodeConstraintValidatorRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SubsystemDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TestTxUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.YangModelFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxxmlsubtree.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class XmlRootModelNodeTest {

    private static final String APPLICATION_CONTEXT_XML = "/xmlrootmodelnodetest/application-context.xml";
    private static final String EXAMPLE_JUKEBOX_YANG = "/xmlrootmodelnodetest/example-jukebox.yang";
    private static final String DEFAULT_DATASTORE_FILE_PATH = "/xmlrootmodelnodetest/datastore.xml";
    private static final String COMPONENT_ID_FORMAT = "%s?revision=%s";
    private static final String MESSAGE_ID = "1" ;
    private static final String SUBTREE = "subtree";
    public static final SchemaPath LIBRARY_SPECIFIC_DATA_SP = new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH).appendLocalName("library-specific-data").build();

    private XmlDSMCache m_dsmCache = mock(XmlDSMCache.class);
    private AbstractApplicationContext m_context;
    private EntityRegistry m_entityRegistry;
    private PersistenceManagerUtil m_persistenceManagerUtil;
    private EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;
    private XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistrySpy;
    private SubSystemRegistry m_subSystemRegistry;
    private ModelNodeDataStoreManager m_xmlSubtreeDSM;
    private XmlSubtreeDSM m_xmlSubtreeDSMTarget;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private EntityDataStoreManager m_entityDataStoreManager;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private NetConfServerImpl m_server;
    private LocalSubSystem m_subSystem;

    @Before
    public void setUp() throws AnnotationAnalysisException, ModelNodeInitException, SchemaBuildException {

        m_context = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_XML);
        m_schemaRegistry = spy(new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService()));
        m_schemaRegistry.loadSchemaContext(JUKEBOX_LOCAL_NAME, getJukeboxYang(), Collections.emptySet(), Collections.emptyMap());
        m_entityRegistry = new EntityRegistryImpl();
        m_persistenceManagerUtil = (PersistenceManagerUtil) m_context.getBean("persistenceManagerUtil");
        m_entityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_modelNodeHelperRegistrySpy = spy(m_modelNodeHelperRegistry);
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_xmlSubtreeDSMTarget = new XmlSubtreeDSM(m_persistenceManagerUtil, m_entityRegistry, m_schemaRegistry, m_modelNodeHelperRegistrySpy,
                m_subSystemRegistry, m_modelNodeDSMRegistry);
        m_xmlSubtreeDSM = TestTxUtils.getTxDecoratedDSM(m_persistenceManagerUtil, m_xmlSubtreeDSMTarget);

        initialiseJukeboxEntity();

        m_entityModelNodeHelperDeployer = new EntityModelNodeHelperDeployer(m_modelNodeHelperRegistrySpy, m_schemaRegistry,
                m_xmlSubtreeDSM, m_entityRegistry, m_subSystemRegistry);
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry, m_modelNodeHelperRegistrySpy,
                m_subSystemRegistry, m_entityRegistry, null);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistrySpy, m_xmlSubtreeDSM,
                m_subSystemRegistry).addModelServiceRoot(JUKEBOX_LOCAL_NAME, retrieveRootModelNode());

        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);

        String xmlFilePath = this.getClass().getResource(DEFAULT_DATASTORE_FILE_PATH).getPath();

        String keyFilePath = getClass().getResource("/xmlrootmodelnodetest/keyfile.plain").getPath();
        CryptUtil2 cryptUtil2 = new CryptUtil2();
        cryptUtil2.setKeyFilePathForTest(keyFilePath);
        cryptUtil2.initFile();

        loadXmlDataIntoServer(m_server, xmlFilePath);
    }

    @Test
    public void testGetJukebox() throws IOException, SAXException {
        //reset cache and mock
        RequestScope.resetScope();
        reset(m_modelNodeHelperRegistrySpy);

        //send get config to fetch artist lenny data
        verifyGetConfig(m_server, "/xmlrootmodelnodetest/get-jukebox-request.xml",
                "/xmlrootmodelnodetest/get-artist-lenny-response.xml", MESSAGE_ID);

        //verify that only jukebox helpers are called since it is a root xml model node
        verify(m_modelNodeHelperRegistrySpy).getChildListHelpers(JUKEBOX_SCHEMA_PATH);
        verify(m_modelNodeHelperRegistrySpy).getChildContainerHelpers(JUKEBOX_SCHEMA_PATH);
        verify(m_modelNodeHelperRegistrySpy, never()).getChildListHelpers(LIBRARY_SCHEMA_PATH);
        verify(m_modelNodeHelperRegistrySpy, never()).getChildContainerHelpers(LIBRARY_SCHEMA_PATH);
    }

    @Test
    public void testGetArtistLenny() throws IOException, SAXException {
        //reset cache and mock
        RequestScope.resetScope();
        reset(m_modelNodeHelperRegistrySpy);

        //send get config to fetch artist lenny data
        verifyGetConfig(m_server, "/xmlrootmodelnodetest/get-artist-lenny-request.xml",
                "/xmlrootmodelnodetest/get-artist-lenny-response.xml", MESSAGE_ID);

        //verify that helpers are not called to retrieve child model nodes of artist
        verify(m_modelNodeHelperRegistrySpy, never()).getChildListHelpers(ARTIST_SCHEMA_PATH);
        verify(m_modelNodeHelperRegistrySpy, never()).getChildContainerHelpers(ARTIST_SCHEMA_PATH);
    }

    @Test
    public void testGetSongFlyAway() throws IOException, SAXException {
        //reset cache and mock
        RequestScope.resetScope();
        reset(m_modelNodeHelperRegistrySpy);

        //send get config to fetch song fly away data
        verifyGetConfig(m_server, "/xmlrootmodelnodetest/get-song-fly-away-request.xml",
                "/xmlrootmodelnodetest/get-song-fly-away-response.xml", MESSAGE_ID);

        //verify that helpers are not called to retrieve child model nodes of song
        verify(m_modelNodeHelperRegistrySpy, never()).getChildListHelpers(SONG_SCHEMA_PATH);
        verify(m_modelNodeHelperRegistrySpy, never()).getChildContainerHelpers(SONG_SCHEMA_PATH);
    }

    @Test
    public void testGetJukeboxWithDepthFilter() throws Exception {
        resetDatastoreAndPopulateLibrarySpecificData();

        verifyGetConfig(m_server, "/xmlrootmodelnodetest/get-jukebox-request.xml",
                "/xmlrootmodelnodetest/get-response-library-with-specific-data.xml", MESSAGE_ID);
        mockMountPoint();
        verifyGetWithDepth(m_server, "/xmlrootmodelnodetest/get-library-with-depth.xml",
                "/xmlrootmodelnodetest/get-library-with-depth-response.xml", MESSAGE_ID, SUBTREE, 4);

        verify(m_modelNodeHelperRegistrySpy, never()).getChildContainerHelpers(LIBRARY_SPECIFIC_DATA_SP);
        verify(m_modelNodeHelperRegistrySpy, never()).getChildListHelpers(ARTIST_SCHEMA_PATH);
        verify(m_subSystem).isNodePresent(eq(LIBRARY_SPECIFIC_DATA_SP), eq(new ModelNodeId("/container=jukebox/container=library/container=library-specific-data",JB_NS)));
    }

    @Test
    public void testGetConfigFollowedByEditInSameThreadShouldReturnDataAddedByEditConfig() throws IOException, SAXException {
        //reset cache and mock
        RequestScope.resetScope();
        reset(m_modelNodeHelperRegistrySpy);

        //send edit config to add new album under artist 'lenny'
        sendEditConfig(m_server, new NetconfClientInfo("UT", 1), loadAsXml("/editconfig-createalbum.xml"), "1");

        //send get config to fetch artist 'lenny' data which should contain the newly added album
        verifyGetConfig(m_server, "/xmlrootmodelnodetest/get-artist-lenny-request.xml",
                "/xmlrootmodelnodetest/get-artist-lenny-with-newly-added-album-response.xml", MESSAGE_ID);

        //Since same thread does both edit and get, full xml copy optimization wont be used because xml data would not have the data added by edit.
        //i.e. Data would be populated via iterative approach
        verify(m_modelNodeHelperRegistrySpy).getChildListHelpers(ARTIST_SCHEMA_PATH);
        verify(m_modelNodeHelperRegistrySpy).getChildContainerHelpers(ARTIST_SCHEMA_PATH);
    }

    private void resetDatastoreAndPopulateLibrarySpecificData() {
        RequestScope.resetScope();
        loadXmlDataIntoServer(m_server, this.getClass().getResource("/xmlrootmodelnodetest/library-with-specific-data.xml").getPath());
        RequestScope.resetScope();
        reset(m_modelNodeHelperRegistrySpy);
    }

    private void mockMountPoint() {
        DataSchemaNode librarySpecificDataSN = mock(DataSchemaNode.class);
        doReturn(librarySpecificDataSN).when(m_schemaRegistry).getDataSchemaNode(LIBRARY_SPECIFIC_DATA_SP);
        UnknownSchemaNode unknownSN = mock(UnknownSchemaNode.class);
        ExtensionDefinition extensionDef = mock(ExtensionDefinition.class);
        QName qname = QName.create(AnvExtensions.MOUNT_POINT.getExtensionNamespace(),AnvExtensions.MOUNT_POINT.getName(), AnvExtensions.MOUNT_POINT.getRevision());
        when(extensionDef.getQName()).thenReturn(qname);
        when(unknownSN.getExtensionDefinition()).thenReturn(extensionDef);
        when(librarySpecificDataSN.getUnknownSchemaNodes()).thenReturn(Collections.singletonList(unknownSN));
        SchemaMountRegistry schemaMountRegistry = mock(SchemaMountRegistry.class);
        when(m_schemaRegistry.getMountRegistry()).thenReturn(schemaMountRegistry);
        SchemaMountRegistryProvider provider = mock(SchemaMountRegistryProvider.class);
        when(schemaMountRegistry.getProvider(any(SchemaPath.class))).thenReturn(provider);
        when(provider.getSchemaRegistry(any(ModelNodeId.class))).thenReturn(m_schemaRegistry);
    }

    private List<YangTextSchemaSource> getJukeboxYang() {
        List<String> yangFiles = new ArrayList<>();
        yangFiles.add(EXAMPLE_JUKEBOX_YANG);
        yangFiles.add("/yangs/ietf/dummy-extension.yang");
        return getByteSources(yangFiles);
    }

    private void initialiseJukeboxEntity() throws AnnotationAnalysisException {
        EntityRegistryBuilder.updateEntityRegistry(JUKEBOX_LOCAL_NAME, Collections.singletonList(Jukebox.class), m_entityRegistry, m_schemaRegistry, m_entityDataStoreManager, m_modelNodeDSMRegistry);
    }

    private ModelNode retrieveRootModelNode() throws SchemaBuildException {
        SchemaVerifierImpl verifier = new SchemaVerifierImpl();
        String yangFilePath = this.getClass().getResource(EXAMPLE_JUKEBOX_YANG).getPath();
        Module module = YangModelFactory.getInstance().loadModule(yangFilePath);
        m_subSystem = spy(new LocalSubSystem());
        traverseModule(m_subSystem, m_modelNodeHelperRegistrySpy, m_subSystemRegistry, m_schemaRegistry, m_xmlSubtreeDSM,
                module, new HashMap<>(), new HashMap<>());
        verifier.verify(m_schemaRegistry);
        Collection<DataSchemaNode> containers = module.getChildNodes();
        for (DataSchemaNode node : containers) {
            if (node instanceof ContainerSchemaNode) {
                XmlModelNodeImpl xmlModelNode = new XmlModelNodeImpl(ConfigAttributeFactory.getDocument(), JUKEBOX_SCHEMA_PATH, Collections.EMPTY_MAP, Collections.EMPTY_LIST, null, new ModelNodeId(),
                        m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistrySpy, m_schemaRegistry, m_subSystemRegistry, m_xmlSubtreeDSM, true, null, true, null);
                m_xmlSubtreeDSM.createNode(xmlModelNode, new ModelNodeId());
                return xmlModelNode;
            }
        }
        return null;
    }

    private void traverseModule(SubSystem subSystem, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subSystemRegistry,
                                SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm, Module module,
                                Map<SchemaPath, ValidationHint> deviceHints, Map<SchemaPath, HintDetails> globalHints) {
        String componentId = module.getRevision().isPresent() ? String.format(COMPONENT_ID_FORMAT, module.getName(),module.getRevision().get()):module.getName();
        Map<SchemaPath, SubSystem> subSystemMap = new HashMap<>();
        for(DataSchemaNode child : module.getChildNodes()){
            subSystemMap.put(child.getPath(), subSystem);
        }

        List<SchemaRegistryVisitor> visitors = new ArrayList<>();
        visitors.add(new SubsystemDeployer(subSystemRegistry, subSystemMap));
        visitors.add(new ModelNodeDSMDeployer(new ModelNodeDSMRegistryImpl(), modelNodeDsm));
        visitors.add(new SchemaPathRegistrar(schemaRegistry, modelNodeHelperRegistry, deviceHints, globalHints));
        visitors.add(new SchemaNodeConstraintValidatorRegistrar(schemaRegistry, modelNodeHelperRegistry, subSystemRegistry));
        visitors.add(m_entityModelNodeHelperDeployer);
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser(componentId, visitors, schemaRegistry, module);
        traverser.traverse();
    }
}