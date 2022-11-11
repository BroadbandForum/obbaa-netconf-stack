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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLStringEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.getDeviationsFromFile;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.getFeaturesFromFile;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountRegistries;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.support.SchemaMountRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelperImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.RpcRequestHandlerRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaNodeConstraintValidatorRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreIntegrityService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DataStoreIntegrityServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.SingleXmlObjectDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.SchemaMountUtil;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class RootModelNodeAggregatorSchemaMountTest {
    public static final SchemaPath MP_SP = SchemaPathBuilder.fromString("(ns:ns?revision=2019-03-13)root,mp");
    private static final Logger LOGGER = LogManager.getLogger(RootModelNodeAggregatorSchemaMountTest.class);
    private static SchemaRegistryImpl c_mountSR;
    private static SchemaMountRegistry c_mountRegistry;
    private SchemaMountRegistryProvider m_provider;
    @Mock
    private PersistenceManagerUtil m_persistenceMgrUtil;
    @Mock
    private SubSystem m_jbSubsystem;
    private NetConfServerImpl m_server;
    private EntityRegistryImpl m_entityRegistry;
    private RootModelNodeAggregatorSchemaMountTest.XMLstore m_xmlObject;
    private SubSystemRegistry m_subSystemRegistry;
    private ModelNodeDSMRegistry m_modelNodeDsmRegistry;
    private RootModelNodeAggregatorImpl m_rootModelNodeAggregator;
    private RootModelNodeAggregatorImpl m_mountRootNodeAggregator;
    private EntityModelNodeHelperDeployer m_modelNodeHelperDeployer;
    private DataStore m_dataStore;
    private NetconfClientInfo m_clientInfo;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SingleXmlObjectDSM m_modelNodeDsm;
    private static SchemaRegistryImpl c_globalSR;
    private MountRegistries m_mountRegistries;
    private DataStoreValidatorImpl m_datastoreValidator;
    protected DataStoreIntegrityService m_integrityService;
    private DSExpressionValidator m_expValidator;
    private AddDefaultDataInterceptor m_interceptor;
    private ModelNodeHelperRegistry m_mountMNHelperRegistry;
    private AddDefaultDataInterceptor m_mountDefaultDatainterceptor;

    @BeforeClass
    public static void beforeClass() throws Exception {
        c_globalSR = new SchemaRegistryImpl( getGlobalYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        c_mountSR = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        Set<QName> features = getFeaturesFromFile(RootModelNodeAggregatorSchemaMountTest.class.getResourceAsStream("/rootmodelnodeaggregatorschemamounttest/extras/supported-features.txt"));
        Map<QName, Set<QName>> deviations = getDeviationsFromFile(RootModelNodeAggregatorSchemaMountTest.class.getResourceAsStream("/rootmodelnodeaggregatorschemamounttest/extras/supported-deviations.txt"));
        List<YangTextSchemaSource> yangs = getYangs();
        assertEquals("the mount YANG files are not correct", 4, yangs.size());
        RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
            @Override
            protected Void execute()
                    throws org.broadband_forum.obbaa.netconf.server.RequestScope.RsTemplate.RequestScopeExecutionException {
                try {
                    c_mountSR.loadSchemaContext("ut", yangs, features, deviations, false);
                    c_mountSR.setName("Mount-SR");
                    c_mountSR.setMountPath(MP_SP);
                    c_mountSR.setParentRegistry(c_globalSR);
                    c_mountRegistry = new SchemaMountRegistryImpl();
                    c_globalSR.setSchemaMountRegistry(c_mountRegistry);
                } catch (SchemaBuildException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
            
        });

    }

    @AfterClass
    public static void afterClass() {
    }
    private static List<YangTextSchemaSource> getGlobalYangs() {
        List<YangTextSchemaSource> yangs = new ArrayList<>();

        File yangDir = new File(RootModelNodeAggregatorSchemaMountTest.class.getResource("/rootmodelnodeaggregatorschemamounttest/globalYangs").getFile());
        for(File file : yangDir.listFiles()){
            if(file.isFile()) {
                yangs.add(YangParserUtil.getYangSource(file.getPath()));
            }
        }
        return yangs;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_server = new NetConfServerImpl(c_globalSR);
        m_entityRegistry = new EntityRegistryImpl();
        m_modelNodeDsmRegistry = new ModelNodeDSMRegistryImpl();

        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_clientInfo = new NetconfClientInfo("ut", 1);
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(c_globalSR);
        m_xmlObject = new XMLstore();
        m_modelNodeDsm = new SingleXmlObjectDSM<>(m_xmlObject, m_persistenceMgrUtil, m_entityRegistry,
                c_globalSR, m_modelNodeHelperRegistry, m_subSystemRegistry, m_modelNodeDsmRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(c_globalSR, m_modelNodeHelperRegistry, m_modelNodeDsm,
                m_subSystemRegistry);
        m_modelNodeHelperDeployer = new EntityModelNodeHelperDeployer(m_modelNodeHelperRegistry, c_globalSR, m_modelNodeDsm,
                m_entityRegistry, m_subSystemRegistry);
        ModelServiceDeployerImpl modelServiceDeployer = new ModelServiceDeployerImpl(m_modelNodeDsmRegistry, m_modelNodeHelperRegistry,
                m_subSystemRegistry, new RpcRequestHandlerRegistryImpl(), m_modelNodeHelperDeployer, c_globalSR, new NoLockService());
        modelServiceDeployer.setRootModelNodeAggregator(m_rootModelNodeAggregator);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        m_dataStore.setNbiNotificationHelper(new NbiNotificationHelperImpl());
        m_integrityService = new DataStoreIntegrityServiceImpl(m_server);

        m_expValidator = new DSExpressionValidator(c_globalSR, m_modelNodeHelperRegistry, m_subSystemRegistry);
        m_datastoreValidator = new DataStoreValidatorImpl(c_globalSR, m_modelNodeHelperRegistry, m_modelNodeDsm, m_integrityService, m_expValidator);


        m_interceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, c_globalSR, m_expValidator);
        m_interceptor.init();


        m_dataStore.setValidator(m_datastoreValidator);
        m_server.setRunningDataStore(m_dataStore);

        EntityRegistryBuilder.updateEntityRegistry("test", Arrays.asList(XMLstore.class), m_entityRegistry, c_globalSR, null,
                m_modelNodeDsmRegistry);
        SchemaMountUtil.addRootNodeHelpers(c_globalSR, m_subSystemRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, c_globalSR.getRootSchemaPaths(), m_rootModelNodeAggregator);
        // register the module revision
        deployHelpers(c_globalSR, "global", m_modelNodeHelperRegistry, null);

        m_mountRegistries = new MountRegistries(new NoLockService());
        m_mountRegistries.getSchemaRegistry().setMountPath(MP_SP);
        m_mountRegistries.getSchemaRegistry().setParentRegistry(c_globalSR);
        m_mountRootNodeAggregator = new MountedRootModelNodeAggregator(c_mountSR, m_mountMNHelperRegistry, m_modelNodeDsm,
                m_subSystemRegistry);
        m_mountRegistries.setRootModelNodeAggregator(m_mountRootNodeAggregator);
        m_mountMNHelperRegistry = new ModelNodeHelperRegistryImpl(c_mountSR);
        m_provider = new TestMPProvider(m_mountRegistries, m_mountMNHelperRegistry, m_subSystemRegistry);
        RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, MP_SP);
        c_mountRegistry.register(MP_SP, m_provider);
        m_mountDefaultDatainterceptor = new AddDefaultDataInterceptor(m_mountMNHelperRegistry, c_mountSR, m_expValidator);
        m_mountDefaultDatainterceptor.init();

        SchemaMountUtil.addRootNodeHelpers(c_mountSR, m_subSystemRegistry, m_mountMNHelperRegistry, m_modelNodeDsm, c_mountSR.getRootSchemaPaths(),
                m_mountRootNodeAggregator);
        deployHelpers(c_mountSR, "mounted", m_mountMNHelperRegistry, MP_SP);

        RequestScope.resetScope();
        createRootNodes();
        m_subSystemRegistry.register("ut", JukeboxConstants.ARTIST_SCHEMA_PATH, m_jbSubsystem);
        m_subSystemRegistry.register("ut", SchemaPathBuilder.fromString("(http://example.com/ns/example-jukebox?revision=2014-07-03)jukebox",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)library",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)system-security",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)server-identity",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)host-key",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)host-key-type",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)public-key",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)public-key",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)local-or-keystore",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)local",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)local-definition",
                "(http://example.com/ns/example-jukebox?revision=2014-07-03)name"), m_jbSubsystem);
        m_subSystemRegistry.register("ut", new SchemaPathBuilder().withParent(ARTIST_SCHEMA_PATH).appendLocalName("state-count").build(), m_jbSubsystem);


        doAnswer(invocationOnMock -> {
            Map<ModelNodeId, List<Element>> artistCountState = new HashMap<>();
            Element albumcount2 = DocumentUtils.stringToDocument("<jbox:state-count xmlns:jbox=\"http://example" +
                    ".com/ns/example-jukebox\">27</jbox:state-count>").getDocumentElement();
            Iterator iterator = ((HashMap) invocationOnMock.getArguments()[0]).keySet().iterator();
            ModelNodeId artistId = (ModelNodeId) iterator.next();
            while(!artistId.getModelNodeIdAsString().equals("/container=root/container=mp/container=jukebox/container=library/container=artist/name=Lenny")){
                artistId = (ModelNodeId) iterator.next();
            }
            artistCountState.put(artistId, Arrays.asList(albumcount2));
            return artistCountState;
        }).when(m_jbSubsystem).retrieveStateAttributes(any(), any(), any());

    }

    public void deployHelpers(SchemaRegistry schemaRegistry, String ut, ModelNodeHelperRegistry mnHelper, SchemaPath mountPath) {
        List<SchemaRegistryVisitor> visitors = new ArrayList<>();
        visitors.add(new EntityModelNodeHelperDeployer(mnHelper, schemaRegistry, m_modelNodeDsm, m_entityRegistry, m_subSystemRegistry));
        visitors.add(new SchemaPathRegistrar(schemaRegistry, mnHelper));
        visitors.add(new SchemaNodeConstraintValidatorRegistrar(schemaRegistry, mnHelper, m_subSystemRegistry));
        for (Module module : schemaRegistry.getSchemaContext().getModules()) {
            SchemaRegistryTraverser traverser = new SchemaRegistryTraverser(ut, visitors, schemaRegistry,
                    module);
            traverser.setMountPath(mountPath);
            traverser.traverse();
        }
    }

    @After
    public void tearDown() {
        c_mountRegistry.deregister(MP_SP);
        m_interceptor.init();
    }

    private void createRootNodes() throws Exception {
        EditConfigRequest request = new EditConfigRequest().setTargetRunning();
        request.setMessageId("1");

        Document doc = DocumentUtils.getNewDocument();
        Element data = doc.createElementNS("urn:ietf:params:xml:ns:netconf:base:1.0", "data");
        for (DataSchemaNode rootSN : c_globalSR.getRootDataSchemaNodes()) {
            if (rootSN.isConfiguration()) {
                if (rootSN instanceof ContainerSchemaNode) {
                    data.appendChild(doc.createElementNS(rootSN.getQName().getNamespace().toString(), rootSN.getQName().getLocalName()));
                } else {
                    rootSN.toString();
                }
            }
        }
        m_xmlObject.setXml(DocumentUtils.documentToPrettyString(data));
    }

    private static List<YangTextSchemaSource> getYangs() {
        List<YangTextSchemaSource> yangs = new ArrayList<>();

        File yangDir = new File(RootModelNodeAggregatorSchemaMountTest.class.getResource("/rootmodelnodeaggregatorschemamounttest/yangs").getFile());
        for(File file : yangDir.listFiles()){
            if(file.isFile()) {
                yangs.add(YangParserUtil.getYangSource(file.getPath()));
            }
        }
        return yangs;
    }


    @Test
    public void testStateValuesAreRetrieved() throws Exception {

        createCommonConfig();
        GetRequest getReq = new GetRequest();
        getReq.setMessageId("1");
        NetConfResponse response;
        String requestStr;
        EditConfigRequest request;

        response = new NetConfResponse();
        response.setMessageId("1");
        requestStr = loadAsString("/rootmodelnodeaggregatorschemamounttest/reqs/edit-jb.xml");
        request = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(requestStr));
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());

        response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onGet(m_clientInfo, getReq, response);
        assertTrue(response.responseToString().contains("<jbox:state-count>27</jbox:state-count>"));
    }

    @Test
    public void testStateValuesAreRetrievedChoiceCase() throws Exception {
        doAnswer(invocationOnMock -> {
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            List<Element> elements = new ArrayList<>();
            String serverIdentityXml = "<algorithm xmlns=\"http://example.com/ns/example-jukebox\">dsa1024</algorithm>";
            Element serverIdentityElement = null;
            serverIdentityElement = DocumentUtils.stringToDocumentElement(serverIdentityXml);
            elements.add(serverIdentityElement);

            Iterator iterator = ((HashMap) invocationOnMock.getArguments()[0]).keySet().iterator();
            ModelNodeId entry = (ModelNodeId) iterator.next();
            stateInfo.put(entry, elements);
            return stateInfo;
        }).when(m_jbSubsystem).retrieveStateAttributes(any(), any(), any());

        createCommonConfig();

        String SystemSecurityLocalDefinitionFilter = "/rootmodelnodetest/system_security_local_definition_filter.xml";

        GetRequest localDefinitionGetReq = new GetRequest();
        localDefinitionGetReq.setMessageId("1");

        NetconfFilter localDefinitionFilter = new NetconfFilter();
        localDefinitionFilter.addXmlFilter(TestUtil.loadAsXml(SystemSecurityLocalDefinitionFilter));
        localDefinitionGetReq.setFilter(localDefinitionFilter);

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        String requestStr = loadAsString("/rootmodelnodeaggregatorschemamounttest/reqs/edit-system-security.xml");
        EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(requestStr));
        m_server.onEditConfig(m_clientInfo, request, response);

        assertTrue(response.isOk());

        response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onGet(m_clientInfo, localDefinitionGetReq, response);
        assertXMLEquals("/rootmodelnodeaggregatorschemamounttest/response/get-response.xml", response);

        String SystemSecurityPublicKeyFilter = "/rootmodelnodetest/system_security_public_key_filter.xml";
        GetRequest publicKeygetReq = new GetRequest();
        publicKeygetReq.setMessageId("1");

        NetconfFilter publicKeyFilter = new NetconfFilter();
        publicKeyFilter.addXmlFilter(TestUtil.loadAsXml(SystemSecurityPublicKeyFilter));
        publicKeygetReq.setFilter(publicKeyFilter);

        response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onGet(m_clientInfo, publicKeygetReq, response);

        assertXMLEquals("/rootmodelnodeaggregatorschemamounttest/response/get-response.xml", response);
    }

    private void createCommonConfig() throws NetconfMessageBuilderException, SAXException, IOException {
        String requestStr = loadAsString("/rootmodelnodeaggregatorschemamounttest/reqs/common-config.xml");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");

        GetConfigRequest request1 = new GetConfigRequest().setSourceRunning();
        request1.setMessageId("1");
        m_server.onGetConfig(m_clientInfo, request1, response);
        assertXMLStringEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <data>\n" +
                "    <mm:root xmlns:mm=\"ns:ns\"/>\n" +
                "  </data>\n" +
                "</rpc-reply>\n", response.responseToString());

        response = new NetConfResponse();
        response.setMessageId("1");
        EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(requestStr));
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue("response is: "+response.responseToString(), response.isOk());

        assertXMLStringEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <ok/>\n" +
                "</rpc-reply>\n", response.responseToString());

        response = new NetConfResponse();
        response.setMessageId("1");
        RequestScope.resetScope();
        m_server.onGetConfig(m_clientInfo, request1, response);
        assertEquals("<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <mm:root xmlns:mm=\"ns:ns\">\n" +
                "         <mp xmlns=\"ns:ns\">\n" +
                "            <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "               <library>\n" +
                "                  <system-security/>\n" +
                "               </library>\n" +
                "               <player/>\n" +
                "            </jukebox>\n" +
                "         </mp>\n" +
                "      </mm:root>\n" +
                "   </data>\n" +
                "</rpc-reply>\n", response.responseToString());
    }

    @YangContainer(name = "root", namespace = "ns:ns", revision="2019-03-13")
    public static class XMLstore {
        @YangXmlSubtree
        String xml;
        @YangSchemaPath
        String schemaPath;
        @YangParentId
        String parentId;

        public String getXml() {
            return xml;
        }

        public void setXml(String xml) {
            this.xml = xml;
        }

        public String getSchemaPath() {
            return schemaPath;
        }

        public void setSchemaPath(String schemaPath) {
            this.schemaPath = schemaPath;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }
    }

    public static class TestMPProvider implements SchemaMountRegistryProvider {

        private MountRegistries m_mountRegistries;
        private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
        private SubSystemRegistry m_subSystemRegistry;

        public TestMPProvider(MountRegistries mountRegistries, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry) {
            m_mountRegistries = mountRegistries;
            m_modelNodeHelperRegistry = modelNodeHelperRegistry;
            m_subSystemRegistry = subsystemRegistry;
        }

        @Override
        public SchemaRegistry getSchemaRegistry(ModelNodeId modelNodeId) {
            return c_mountSR;
        }

        @Override
        public SchemaRegistry getSchemaRegistry(Element element) {
            return c_mountSR;
        }

        @Override
        public SchemaRegistry getSchemaRegistry(EditContainmentNode editContainmentNode) {
            return c_mountSR;
        }

        @Override
        public SchemaRegistry getSchemaRegistry(Map<String, String> keyValues) {
            return c_mountSR;
        }

        @Override
        public SchemaRegistry getSchemaRegistry(String mountKey) {
            return c_mountSR;
        }

        @Override
        public ModelNodeHelperRegistry getModelNodeHelperRegistry(ModelNodeId modelNodeId) {
            return m_modelNodeHelperRegistry;
        }

        @Override
        public ModelNodeHelperRegistry getModelNodeHelperRegistry(Element element) {
            return m_modelNodeHelperRegistry;
        }

        @Override
        public ModelNodeHelperRegistry getModelNodeHelperRegistry(EditContainmentNode editContainmentNode) {
            return m_modelNodeHelperRegistry;
        }

        @Override
        public SubSystemRegistry getSubSystemRegistry(ModelNodeId modelNodeId) {
            return m_subSystemRegistry;
        }

        @Override
        public SchemaMountKey getSchemaMountKey() {
            return null;
        }

        @Override
        public MountRegistries getMountRegistries(String mountkey) {
            return m_mountRegistries;
        }

        @Override
        public void setCorrectPlugMountContextInCache(EditContainmentNode node) {

        }

        @Override
        public List<MountContext> getMountContexts() {
            return null;
        }

        @Override
        public boolean isValidMountPoint(ModelNodeId nodeID) {
            return false;
        }
    }

}
