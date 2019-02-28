package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendCopyConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class YangCopyConfigTest {

    private static final String EXPECTED_EMPTY_JUKEBOX_XML = "/empty-jukebox.xml";
    private static final String EXPECTED_UNFILTERED_XML = "/getconfig-unfiltered-yang.xml";
    private static final String COMPONENT_ID = "test";
    private static final String OK_RESPONSE_XML = "/ok-response.xml";
    private static final String YANG_MODEL_PATH = YangCopyConfigTest.class.getResource("/yangs/example-jukebox.yang").getPath();
    private static final String EMPTY_JUKEBOX_PATH = YangCopyConfigTest.class.getResource("/empty-example-jukebox.xml").getPath();

    private NetConfServerImpl m_server;
    private NetconfClientInfo m_clientInfo;
    private ModelNode m_runningModel;
    private ModelNode m_candidateModel;
    private SubSystemRegistry m_subSystemRegistry;

    private RootModelNodeAggregator m_runningRootModelNodeAggregator;
    private RootModelNodeAggregator m_candidateRootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private String m_xmlFilePath;
    NbiNotificationHelper m_nbiNotificationHelper = mock(NbiNotificationHelper.class);

    @Before
    public void initServer() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_xmlFilePath = YangCopyConfigTest.class.getResource("/example-jukebox.xml").getPath();
        m_clientInfo = new NetconfClientInfo("unit-test", 1);
        RequestScope.resetScope();
    }

    private void setNonEmptyRunningDS() throws ModelNodeInitException, SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_runningModel = YangUtils.createInMemoryModelNode(YANG_MODEL_PATH, new LocalSubSystem(), modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(COMPONENT_ID, m_runningModel);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator, m_subSystemRegistry );
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, m_xmlFilePath, StandardDataStores.RUNNING);
    }
    
    private void setNonEmptyCandidateDS() throws ModelNodeInitException, SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_candidateModel = YangUtils.createInMemoryModelNode(YANG_MODEL_PATH, new LocalSubSystem(), modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_candidateRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(COMPONENT_ID, m_candidateModel);
        DataStore dataStore = new DataStore(StandardDataStores.CANDIDATE, m_candidateRootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setDataStore(StandardDataStores.CANDIDATE, dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, m_xmlFilePath, StandardDataStores.CANDIDATE);
    }

    private void setEmptyRunningDS() throws ModelNodeInitException, SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_runningModel = YangUtils.createInMemoryModelNode(YANG_MODEL_PATH, new LocalSubSystem(), modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(COMPONENT_ID, m_runningModel);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, EMPTY_JUKEBOX_PATH, StandardDataStores.RUNNING);
    }
    
    private void setEmptyCandidateDS() throws ModelNodeInitException, SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_candidateModel = YangUtils.createInMemoryModelNode(YANG_MODEL_PATH, new LocalSubSystem(), modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_candidateRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(COMPONENT_ID, m_candidateModel);
        DataStore dataStore = new DataStore(StandardDataStores.CANDIDATE, m_candidateRootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setDataStore(StandardDataStores.CANDIDATE, dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, EMPTY_JUKEBOX_PATH, StandardDataStores.CANDIDATE);
    }

    @Test
    public void testCopyFromNonEmptyCandidateToEmptyRunningWorks() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        setEmptyRunningDS();
        setNonEmptyCandidateDS();
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_EMPTY_JUKEBOX_XML, "1");
        
        NetConfResponse response = sendCopyConfig(m_server, m_clientInfo, StandardDataStores.CANDIDATE, StandardDataStores.RUNNING, "1");
        
        // assert Ok response
        assertXMLEquals(OK_RESPONSE_XML, response);
        
        // do a get-config and make sure the jukebox is not-empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_UNFILTERED_XML, "1");
    }
    
   @Test
    public void testCopyFromNonEmptyCandidateToNonEmptyRunningWorks() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        setNonEmptyRunningDS();
        setNonEmptyCandidateDS();
        // do a get-config and make sure the jukebox is non-empty, without year
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_UNFILTERED_XML, "1");
        
        NetConfResponse response = sendCopyConfig(m_server, m_clientInfo, StandardDataStores.CANDIDATE, StandardDataStores.RUNNING, "1");
        
        // assert Ok response
        assertXMLEquals(OK_RESPONSE_XML, response);
         
        // do a get-config and make sure the jukebox is not-empty and has year fields
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_UNFILTERED_XML, "1");
    }
    
    @Test
    public void testCopyEmptyCandidateToNonEmptyRunningWorks() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        setNonEmptyRunningDS();
        setEmptyCandidateDS();
        // do a get-config and make sure the jukebox is non-empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_UNFILTERED_XML, "1");
        
        NetConfResponse response = sendCopyConfig(m_server, m_clientInfo, StandardDataStores.CANDIDATE, StandardDataStores.RUNNING, "1");
        
        // assert Ok response
        assertXMLEquals(OK_RESPONSE_XML, response);
        
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_EMPTY_JUKEBOX_XML, "1");
    }
    
    @Test
    public void testCopyEmptyCandidateToEmptyRunningWorks() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        setEmptyRunningDS();
        setEmptyCandidateDS();
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_EMPTY_JUKEBOX_XML, "1");
        
        NetConfResponse response = sendCopyConfig(m_server, m_clientInfo, StandardDataStores.CANDIDATE, StandardDataStores.RUNNING, "1");
        
        // assert Ok response
        assertXMLEquals(OK_RESPONSE_XML, response);
        
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_EMPTY_JUKEBOX_XML, "1");
    }

    @Test
    public void testNonEmptyRunningDatastore() throws Exception{
        setNonEmptyRunningDS();
        setNonEmptyCandidateDS();
        // do a get-config and make sure the jukebox is non-empty, without year
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_UNFILTERED_XML, "1");

        NetConfResponse response =  sendCopyConfig(m_server, m_clientInfo, StandardDataStores.CANDIDATE, StandardDataStores.RUNNING, "1");

        // assert Ok response
        assertXMLEquals(OK_RESPONSE_XML, response);

        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_UNFILTERED_XML, "1");

    }


}
