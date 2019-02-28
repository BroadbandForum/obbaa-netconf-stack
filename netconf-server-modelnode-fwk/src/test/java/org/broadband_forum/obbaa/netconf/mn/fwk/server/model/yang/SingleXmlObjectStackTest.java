package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelperImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.RpcRequestHandlerRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.SingleXmlObjectDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.ModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

@Ignore("This fails only in jenkins, comemnting out for sometime")
public class SingleXmlObjectStackTest {
    private static final Logger LOGGER = Logger.getLogger(SingleXmlObjectStackTest.class);
    private NetConfServerImpl m_server;
    private NetconfClientInfo m_clientInfo;

    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private DataStore m_dataStore;
    private SubSystemRegistry m_subsystemRegistry;
    private TestXml m_xmlObject;
    @Mock
    private PersistenceManagerUtil m_persistenceUtil;
    private EntityRegistry m_entityRegistry;
    private ModelNodeDSMRegistry m_modelNodeDsmRegistry;
    private ModelNodeHelperDeployer m_modelNodeHelperDeployer;

    @Before
    public void initServer() throws SchemaBuildException, AnnotationAnalysisException, ModelServiceDeployerException {
        MockitoAnnotations.initMocks(this);
        m_modelNodeDsmRegistry = new ModelNodeDSMRegistryImpl();

        m_subsystemRegistry = new SubSystemRegistryImpl();
        m_clientInfo = new NetconfClientInfo("ut", 1);
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getByteSources(Arrays.asList("/singlexmlobjectstacktest/test.yang")), new NoLockService());
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_entityRegistry = new EntityRegistryImpl();
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_xmlObject = new TestXml();
        SingleXmlObjectDSM modelNodeDsm = new SingleXmlObjectDSM<>(m_xmlObject, m_persistenceUtil, m_entityRegistry,
                m_schemaRegistry, modelNodeHelperRegistry, m_subsystemRegistry, m_modelNodeDsmRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm,
                m_subsystemRegistry);
        m_modelNodeHelperDeployer = new EntityModelNodeHelperDeployer(modelNodeHelperRegistry, m_schemaRegistry, modelNodeDsm,
                m_entityRegistry, m_subsystemRegistry);
        ModelServiceDeployerImpl modelServiceDeployer = new ModelServiceDeployerImpl(m_modelNodeDsmRegistry, modelNodeHelperRegistry,
                m_subsystemRegistry, new RpcRequestHandlerRegistryImpl(), m_modelNodeHelperDeployer, m_schemaRegistry, new NoLockService());
        modelServiceDeployer.setRootModelNodeAggregator(m_rootModelNodeAggregator);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subsystemRegistry);
        m_dataStore.setNbiNotificationHelper(new NbiNotificationHelperImpl());
        m_server.setRunningDataStore(m_dataStore);

        EntityRegistryBuilder.updateEntityRegistry("test", Arrays.asList(TestXml.class), m_entityRegistry, m_schemaRegistry, null,
                m_modelNodeDsmRegistry);


        ModelService modelService1 = new ModelService();
        modelService1.setModuleName("test");
        modelService1.setModuleRevision("2016-02-10");
        modelService1.setModelNodeDSM(modelNodeDsm);
        modelServiceDeployer.deploy(Arrays.asList(modelService1));
        updateEntityRegistry();
        RequestScope.resetScope();
    }


    private void updateEntityRegistry() {
        Map<SchemaPath, Class> rootNodeMap = getRootNodeMap();
        m_entityRegistry.addSchemaPaths("ut", rootNodeMap);
    }

    private Map<SchemaPath, Class> getRootNodeMap() {
        Map<SchemaPath, Class> rootNodeMap = new HashMap<>();
        for (DataSchemaNode rootNode : m_schemaRegistry.getRootDataSchemaNodes()) {
            rootNodeMap.put(rootNode.getPath(), TestXml.class);
        }
        return rootNodeMap;
    }

    @After
    public void tearDown() {
        RequestScope.resetScope();
    }

    @Test
    public void testXmlGetsUpdatedForRootContainerNode() throws NetconfMessageBuilderException, IOException, SAXException {
        String requestStr = "<rpc " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running />\n" +
                "    </target>\n" +
                "    <test-option>set</test-option>\n" +
                "    <config>\n" +
                "      <rootContainer1 xmlns=\"urn:test\">\n" +
                "        <leaf1>leaf1Value</leaf1>\n" +
                "      </rootContainer1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n";

        testEdit("", expectedStoreXml, requestStr);
    }

    @Test
    public void testGetContainerNode() throws NetconfMessageBuilderException, IOException, SAXException {
        String requestStr = "<rpc " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"1\">\n" +
                "  <get/>\n" +
                "</rpc>";
        String dataStore = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value</leaf1>\n" +
                "        <level1Container1>\n" +
                "           <leaf4>leaf4Value1</leaf4>\n" +
                "        </level1Container1>\n" +
                "</rootContainer1>\n" +
                "</data>\n";

        testGet(requestStr, dataStore);
    }

    private void testGet(String requestStr, String dataStore) throws NetconfMessageBuilderException, SAXException, IOException {
        GetRequest request = DocumentToPojoTransformer.getGet(DocumentUtils.stringToDocument(requestStr));
        m_xmlObject.setXml(dataStore);
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onGet(m_clientInfo, request, response);
        LOGGER.error("Expected: \n"+dataStore+"\nActual : \n"+DocumentUtils.documentToPrettyString(response.getData()));
        assertXMLEquals(DocumentUtils.stringToDocument(dataStore).getDocumentElement(), response.getData());
    }

    @Test
    public void testGetListNode() throws NetconfMessageBuilderException, IOException, SAXException {
        String requestStr = "<rpc " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"1\">\n" +
                "  <get/>\n" +
                "</rpc>";
        String dataStore = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rootContainer1 xmlns=\"urn:test\">\n" +
                "    <leaf1>leaf1Value</leaf1>\n" +
                "    <level1Container1>\n" +
                "      <leaf4>leaf4Value1</leaf4>\n" +
                "    </level1Container1>\n" +
                "  </rootContainer1>\n" +
                "  <list1 xmlns=\"urn:test\">\n" +
                "    <listKey>listKey1</listKey>\n" +
                "    <leaf3>leaf3Value</leaf3>\n" +
                "  </list1>\n" +
                "  <list1 xmlns=\"urn:test\">\n" +
                "    <listKey>listKey2</listKey>\n" +
                "    <leaf3>leaf3Value2</leaf3>\n" +
                "  </list1>\n" +
                "</data>\n";

        testGet(requestStr, dataStore);
    }

    @Test
    public void testXmlGetsUpdatedForListUnderRootContainerNode() throws NetconfMessageBuilderException, IOException, SAXException {
        String requestStr = "<rpc " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running />\n" +
                "    </target>\n" +
                "    <test-option>set</test-option>\n" +
                "    <config>\n" +
                "      <rootContainer1 xmlns=\"urn:test\">\n" +
                "        <level1Container1>\n" +
                "           <leaf4>leaf4Value1</leaf4>\n" +
                "        </level1Container1>\n" +
                "      </rootContainer1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value</leaf1>\n" +
                "        <level1Container1>\n" +
                "           <leaf4>leaf4Value1</leaf4>\n" +
                "        </level1Container1>\n" +
                "</rootContainer1>\n" +
                "</data>\n";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>", expectedStoreXml, requestStr);
    }

    @Test
    public void testXmlGetsUpdatedForListUnderRootListNode() throws NetconfMessageBuilderException, IOException, SAXException {
        String requestStr = "<rpc " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running />\n" +
                "    </target>\n" +
                "    <test-option>set</test-option>\n" +
                "    <config>\n" +
                "      <list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey1</listKey>\n" +
                "           <list3>\n" +
                "               <list3Key>list3KeyValue1</list3Key>\n"+
                "               <leaf6>leaf6Value1</leaf6>\n"+
                "           </list3>\n"+
                "      </list1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rootContainer1 xmlns=\"urn:test\">\n" +
                "    <leaf1>leaf1Value</leaf1>\n" +
                "  </rootContainer1>\n" +
                "  <list1 xmlns=\"urn:test\">\n" +
                "    <listKey>listKey1</listKey>\n" +
                "    <leaf3>leaf3Value1</leaf3>\n" +
                "    <list3>\n" +
                "       <list3Key>list3KeyValue1</list3Key>\n"+
                "       <leaf6>leaf6Value1</leaf6>\n"+
                "    </list3>\n"+
                "  </list1>\n" +
                "</data>";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rootContainer1 xmlns=\"urn:test\">\n" +
                "    <leaf1>leaf1Value</leaf1>\n" +
                "  </rootContainer1>\n" +
                "  <list1 xmlns=\"urn:test\">\n" +
                "    <listKey>listKey1</listKey>\n" +
                "    <leaf3>leaf3Value1</leaf3>\n" +
                "  </list1>\n" +
                "</data>", expectedStoreXml, requestStr);
    }

    @Test
    public void testXmlGetsUpdatedForListUnderRootListNodeGetsDeleted() throws NetconfMessageBuilderException, IOException, SAXException {
        String requestStr = "<rpc " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running />\n" +
                "    </target>\n" +
                "    <test-option>set</test-option>\n" +
                "    <config>\n" +
                "      <list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey1</listKey>\n" +
                "           <list3 xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"remove\" >\n" +
                "               <list3Key>list3KeyValue1</list3Key>\n"+
                "           </list3>\n"+
                "      </list1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rootContainer1 xmlns=\"urn:test\">\n" +
                "    <leaf1>leaf1Value</leaf1>\n" +
                "  </rootContainer1>\n" +
                "  <list1 xmlns=\"urn:test\">\n" +
                "    <listKey>listKey1</listKey>\n" +
                "    <leaf3>leaf3Value1</leaf3>\n" +
                "  </list1>\n" +
                "</data>";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rootContainer1 xmlns=\"urn:test\">\n" +
                "    <leaf1>leaf1Value</leaf1>\n" +
                "  </rootContainer1>\n" +
                "  <list1 xmlns=\"urn:test\">\n" +
                "    <listKey>listKey1</listKey>\n" +
                "    <leaf3>leaf3Value1</leaf3>\n" +
                "    <list3>\n" +
                "       <list3Key>list3KeyValue1</list3Key>\n"+
                "       <leaf6>leaf6Value1</leaf6>\n"+
                "    </list3>\n"+
                "  </list1>\n" +
                "</data>", expectedStoreXml, requestStr);
    }


    @Test
    public void testXmlGetsUpdatedForRootListNode() throws Exception {
        String requestStr = "<rpc message-id=\"1\" " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <default-operation>merge</default-operation>\n" +
                "    <test-option>set</test-option>\n" +
                "    <error-option>stop-on-error</error-option>\n" +
                "    <config>\n" +
                "      <list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey1</listKey>\n" +
                "        <leaf3>leaf3Value</leaf3>\n" +
                "      </list1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey1</listKey>\n" +
                "        <leaf3>leaf3Value</leaf3>\n" +
                "</list1>\n" +
                "</data>\n";

        testEdit("", expectedStoreXml, requestStr);
    }

    @Test
    public void testXmlGetsUpdatedForRootListNodes() throws Exception {
        String requestStr = "<rpc message-id=\"1\" " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <default-operation>merge</default-operation>\n" +
                "    <test-option>set</test-option>\n" +
                "    <error-option>stop-on-error</error-option>\n" +
                "    <config>\n" +
                "      <list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "      </list1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey1</listKey>\n" +
                "        <leaf3>leaf3Value</leaf3>\n" +
                "</list1>\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "</list1>\n" +
                "</data>\n";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey1</listKey>\n" +
                "        <leaf3>leaf3Value</leaf3>\n" +
                "</list1>\n" +
                "</data>", expectedStoreXml, requestStr);
    }

    @Test
    public void testXmlGetsUpdatedForRootListAndContainerNodes1() throws Exception {
        String requestStr = "<rpc message-id=\"1\" " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <default-operation>merge</default-operation>\n" +
                "    <test-option>set</test-option>\n" +
                "    <error-option>stop-on-error</error-option>\n" +
                "    <config>\n" +
                "      <list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "      </list1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "</list1>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n", expectedStoreXml, requestStr);
    }

    @Test
    public void testXmlGetsUpdatedForRootListAndContainerNodes2() throws Exception {
        String requestStr = "<rpc message-id=\"1\" " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <default-operation>merge</default-operation>\n" +
                "    <test-option>set</test-option>\n" +
                "    <error-option>stop-on-error</error-option>\n" +
                "    <config>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value</leaf1>\n" +
                "</rootContainer1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "</list1>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "</list1>\n" +
                "</data>\n", expectedStoreXml, requestStr);
    }

    @Test
    public void testXmlGetsUpdatedForRootListAndContainerNodesForUpdate() throws Exception {
        String requestStr = "<rpc message-id=\"1\" " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <default-operation>merge</default-operation>\n" +
                "    <test-option>set</test-option>\n" +
                "    <error-option>stop-on-error</error-option>\n" +
                "    <config>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value2</leaf1>\n" +
                "</rootContainer1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "</list1>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value2</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "</list1>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value1</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n", expectedStoreXml, requestStr);
    }

    @Test
    public void testXmlGetsUpdatedForRootListAndContainerNodesForUpdate2() throws Exception {
        String requestStr = "<rpc message-id=\"1\" " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <default-operation>merge</default-operation>\n" +
                "    <test-option>set</test-option>\n" +
                "    <error-option>stop-on-error</error-option>\n" +
                "    <config>\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value3</leaf3>\n" +
                "</list1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value3</leaf3>\n" +
                "</list1>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value1</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "</list1>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value1</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n", expectedStoreXml, requestStr);
    }

    @Ignore("Stack does not allow deletion of root nodes !")
    @Test
    public void testDeleteContainerNodeDoesNotDeleteLists() throws Exception {
        String requestStr = "<rpc message-id=\"1\" " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <default-operation>merge</default-operation>\n" +
                "    <test-option>set</test-option>\n" +
                "    <error-option>stop-on-error</error-option>\n" +
                "    <config>\n" +
                "<list1 xmlns=\"urn:test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"remove\" >\n" +
                "        <listKey>listKey2</listKey>\n" +
                "</list1>\n" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>";
        String expectedStoreXml = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value1</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n";

        testEdit("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<list1 xmlns=\"urn:test\">\n" +
                "        <listKey>listKey2</listKey>\n" +
                "        <leaf3>leaf3Value2</leaf3>\n" +
                "</list1>\n" +
                "<rootContainer1 xmlns=\"urn:test\">\n" +
                "<leaf1>leaf1Value1</leaf1>\n" +
                "</rootContainer1>\n" +
                "</data>\n", expectedStoreXml, requestStr);
    }

    private void testEdit(String initialStoreXml, String expectedStoreXml, String requestStr) throws NetconfMessageBuilderException,
            IOException, SAXException {
        EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(requestStr));
        m_xmlObject.setXml(initialStoreXml);
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());
        LOGGER.error("Expected: \n"+expectedStoreXml+"\nActual : \n"+m_xmlObject.getXml());
        assertXMLEquals(DocumentUtils.stringToDocument(expectedStoreXml).getDocumentElement(), DocumentUtils.stringToDocument(m_xmlObject
                .getXml()).getDocumentElement());
    }

    @YangContainer(name = "store", namespace = "ns:ns")
    public class TestXml {

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


}
