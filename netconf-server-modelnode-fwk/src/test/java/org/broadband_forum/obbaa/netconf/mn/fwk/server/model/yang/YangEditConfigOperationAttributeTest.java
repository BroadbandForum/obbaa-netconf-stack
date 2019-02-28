package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.createInMemoryModelNode;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class YangEditConfigOperationAttributeTest extends AbstractYangValidationTestSetup{

    private NetConfServerImpl m_server;

    private ModelNodeWithAttributes m_modelNodeWithAttributes;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    public static final String EDITCONFIGOPERATIONATTRIBUTETEST_YANG_FILE_YANG = "/editconfigoperationattributetest/yang-file.yang";
    public static final String AUGMENT_FILE = "/editconfigoperationattributetest/augment-default-example.yang";
	private static final String m_default_xmlFile = YangEditConfigCreateTest.class.getResource("/editconfigoperationattributetest/default-file.xml").getPath();
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeDataStoreManager m_modelNodeDsm;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        createServer(new LocalSubSystem());
    }
    
    private void createServer(SubSystem subSystem) throws ModelNodeInitException, SchemaBuildException {
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_schemaRegistry.loadSchemaContext("yang", Arrays.asList(TestUtil.getByteSource(EDITCONFIGOPERATIONATTRIBUTETEST_YANG_FILE_YANG), TestUtil.getByteSource(AUGMENT_FILE)), Collections.emptySet(), Collections.emptyMap());
        m_modelNodeWithAttributes = (ModelNodeWithAttributes) createInMemoryModelNode(Arrays.asList(EDITCONFIGOPERATIONATTRIBUTETEST_YANG_FILE_YANG, AUGMENT_FILE), subSystem, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry,m_modelNodeDsm);
        
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(m_componentId, m_modelNodeWithAttributes);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        loadXmlDataIntoServer(m_server,m_default_xmlFile);
    }
    
    @Test
    public void testCreate() throws SAXException, IOException {
    	NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-create-a-create-b-request.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result1.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-create-a-create-b-request.xml"), "1");
        assertXMLEquals("/editconfigoperationattributetest/edit-config-error-response-1.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result1.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-create-withoutnamespace-a-create-b-request.xml"), "1");
        assertXMLEquals("/editconfigoperationattributetest/edit-config-error-response-2.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result1.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-create-withoutnamespace-a-create-withoutnamespace-b-request.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result1.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-replace-withoutnamespace-a-replace-b-request.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result2.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-replace-a-replace-b-request.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result1.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-merge-a-delete-withoutnamespace-b-request.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result1.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-merge-a-delete-b-request.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result3.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-create-c-request.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/editconfigoperationattributetest/get-config-result4.xml", "1");
        
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfigoperationattributetest/edit-config-create-c-request.xml"), "1");
        assertXMLEquals("/editconfigoperationattributetest/edit-config-error-response-3.xml", response);
    }
    
}
