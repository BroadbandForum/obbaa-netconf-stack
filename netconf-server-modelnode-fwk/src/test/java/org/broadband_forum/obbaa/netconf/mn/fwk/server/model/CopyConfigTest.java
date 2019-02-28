package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createEmptyJukeBox;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createJukeBoxModel;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createJukeBoxModelWithYear;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.Collections;

public class CopyConfigTest {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = Logger.getLogger(EditConfigDeleteTest.class);

    private NetConfServerImpl m_server;
    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("unit-test", 1);

    private ModelNode m_runningModel;

    private Jukebox m_candidateModel;

    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private RootModelNodeAggregator m_runningRootModelNodeAggregator;
    private RootModelNodeAggregator m_candidateRootModelNodeAggregator;
    private String m_componentId = "test";
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        createServer();
    }

    private void setNonEmptyCandidateDS() {
        m_candidateModel = createJukeBoxModelWithYear(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_candidateRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_candidateModel);
        m_server.setDataStore(StandardDataStores.CANDIDATE, new DataStore(StandardDataStores.CANDIDATE, m_candidateRootModelNodeAggregator, m_subSystemRegistry));
    }

    private void setEmptyRunningDS() {
        m_runningModel = createEmptyJukeBox(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_runningModel);
        m_server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator, m_subSystemRegistry));
        
    }

    private void createServer() {
        m_server = new NetConfServerImpl(m_schemaRegistry);
    }

    @Test
    public void testCopyFromNonEmptyCandidateToEmptyRunningWorks() {
        setEmptyRunningDS();
        setNonEmptyCandidateDS();
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(null, "/empty-jukebox.xml");
        
        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setSource(StandardDataStores.CANDIDATE, false).setTarget(StandardDataStores.RUNNING, false);
        copyConfigRequest.setMessageId("1");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onCopyConfig(m_clientInfo, copyConfigRequest, response);
        
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));
        
        // do a get-config and make sure the jukebox is not-empty
        verifyGetConfig(null, "/getconfig-unfiltered-with-year.xml");
    }
    
    @Test
    public void testCopyFromNonEmptyCandidateToNonEmptyRunningWorks() {
        setNonEmptyRunningDS();
        setNonEmptyCandidateDS();
        // do a get-config and make sure the jukebox is non-empty, without year
        verifyGetConfig(null, "/getconfig-unfiltered.xml");
        
        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setSource(StandardDataStores.CANDIDATE, false).setTarget(StandardDataStores.RUNNING, false);
        copyConfigRequest.setMessageId("1");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onCopyConfig(m_clientInfo, copyConfigRequest, response);
        
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));
        
        // do a get-config and make sure the jukebox is not-empty and has year fields
        verifyGetConfig(null, "/getconfig-unfiltered-with-year.xml");
    }
    
    @Test
    public void testCopyEmptyCandidateToNonEmptyRunningWorks() {
        setNonEmptyRunningDS();
        setEmptyCandidateDS();
        // do a get-config and make sure the jukebox is non-empty
        verifyGetConfig(null, "/getconfig-unfiltered.xml");
        
        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setSource(StandardDataStores.CANDIDATE, false).setTarget(StandardDataStores.RUNNING, false);
        copyConfigRequest.setMessageId("1");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onCopyConfig(m_clientInfo, copyConfigRequest, response);
        
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));
        
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(null, "/empty-jukebox.xml");
    }
    
    @Test
    public void testCopyEmptyCandidateToEmptyRunningWorks() {
        setEmptyRunningDS();
        setEmptyCandidateDS();
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(null, "/empty-jukebox.xml");
        
        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setSource(StandardDataStores.CANDIDATE, false).setTarget(StandardDataStores.RUNNING, false);
        copyConfigRequest.setMessageId("1");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onCopyConfig(m_clientInfo, copyConfigRequest, response);
        
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));
        
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(null, "/empty-jukebox.xml");
    }

    @Test
    public void testCopySourceConfigElementToEmptyRunningWorks(){
    	setEmptyRunningDS();
    	// do a get-config and make sure the jukebox is empty
    	verifyGetConfig(null, "/empty-jukebox.xml");
    	
    	Element sourceConfigElement = loadAsXml("/copyConfigSourceConfigElement.xml");
		CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setTarget(StandardDataStores.RUNNING, false).setSourceConfigElement(sourceConfigElement );
        copyConfigRequest.setMessageId("1");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onCopyConfig(m_clientInfo, copyConfigRequest, response);
        
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));
        // do a get-config and make sure the jukebox is updated
        verifyGetConfig(null, "/getconfig-unfiltered-with-year-updated.xml");
    }
    
    @Test
    public void testCopySourceConfigElementToNonEmptyRunningWorks(){
    	setNonEmptyRunningDSWithYear();
    	// do a get-config and make sure the jukebox is not empty
    	verifyGetConfig(null, "/getconfig-unfiltered-with-year.xml");
    	
    	Element sourceConfigElement = loadAsXml("/copyConfigSourceConfigElement.xml");
		CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setTarget(StandardDataStores.RUNNING, false).setSourceConfigElement(sourceConfigElement );
        copyConfigRequest.setMessageId("1");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onCopyConfig(m_clientInfo, copyConfigRequest, response);
        
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));
        // do a get-config and make sure the jukebox is updated
        verifyGetConfig(null, "/getconfig-unfiltered-with-year-updated.xml");
    }

    @Test
    public void testCopySourceConfigElementToNonEmptyRunningWorks2(){
        setNonEmptyRunningDSWithYear();
        // do a get-config and make sure the jukebox is not empty
        verifyGetConfig(null, "/getconfig-unfiltered-with-year.xml");

        Element sourceConfigElement = loadAsXml("/copyConfigSourceConfigElement2.xml");
        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setTarget(StandardDataStores.RUNNING, false).setSourceConfigElement(sourceConfigElement );
        copyConfigRequest.setMessageId("1");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onCopyConfig(m_clientInfo, copyConfigRequest, response);

        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));
        // do a get-config and make sure the jukebox is updated
        verifyGetConfig(null, "/getconfig-unfiltered2.xml");
    }
    
    private void setEmptyCandidateDS() {
        m_candidateModel = createEmptyJukeBox(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_candidateRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_candidateModel);
        m_server.setDataStore(StandardDataStores.CANDIDATE, new DataStore(StandardDataStores.CANDIDATE, m_candidateRootModelNodeAggregator, m_subSystemRegistry));
    }

    private void setNonEmptyRunningDS() {
        m_runningModel = createJukeBoxModel(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_runningModel);
        m_server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator, m_subSystemRegistry));
    }
    
    private void setNonEmptyRunningDSWithYear() {
        m_runningModel = createJukeBoxModelWithYear(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_runningModel);
        m_server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator, m_subSystemRegistry));
    }
    

    private void verifyGetConfig(String filterInput, String expectedOutput) {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);

        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId("1");
        request.setSource("running");
        if (filterInput != null) {
            NetconfFilter filter = new NetconfFilter();
            // we have two variants fo the select node in here
            filter.addXmlFilter(loadAsXml(filterInput));
            request.setFilter(filter);
        }

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");

        m_server.onGetConfig(client, request, response);

        try {
            TestUtil.assertXMLEquals(expectedOutput, response);
        } catch (Exception e) {
            fail("Failed to compare expected and actual XML");
        }
    }

    @Test
    public void testCopyConfigOnLockedDS() {
        setNonEmptyRunningDS();

        LockRequest lockRequest = new LockRequest();
        lockRequest.setMessageId("101");
        lockRequest.setTarget("running");

        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onHello(m_clientInfo, null);
        m_server.onLock(m_clientInfo,lockRequest,response);
        assertEquals(load("/ok-response.xml"),responseToString(response));

        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setSource(StandardDataStores.RUNNING, false).
                setTarget(StandardDataStores.RUNNING, false);
        copyConfigRequest.setMessageId("1");

        NetconfClientInfo m_clientInfo2 = new NetconfClientInfo("unit-test2",2);
        response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onHello(m_clientInfo2,null);
        m_server.onCopyConfig(m_clientInfo2, copyConfigRequest, response);
        String errorResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <rpc-error>\n" +
                "    <error-type>application</error-type>\n" +
                "    <error-tag>in-use</error-tag>\n" +
                "    <error-severity>error</error-severity>\n" +
                "    <error-message>The request requires a resource that is already in use</error-message>\n" +
                "  </rpc-error>\n" +
                "</rpc-reply>\n";
        assertEquals(errorResponse, responseToString(response));
    }
}
