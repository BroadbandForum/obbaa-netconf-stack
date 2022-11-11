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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLStringEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.getJukeBoxYangFileName;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class CopyConfigTest {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LogManager.getLogger(CopyConfigTest.class);

    private NetConfServerImpl m_server;
    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("unit-test", 1);

    private ModelNode m_runningModel;

    private ModelNode m_candidateModel;

    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private RootModelNodeAggregator m_runningRootModelNodeAggregator;
    private RootModelNodeAggregator m_candidateRootModelNodeAggregator;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeDataStoreManager m_modelNodeDSM;
    private static final String JUKEBOX_DATA_ARTIST_LENNY_WITH_YEAR = CopyConfigTest.class.getResource("/data-with-artist-lenny-with-year.xml").getPath();
    private static final String JUKEBOX_DATA_ARTISTS_PENNY_AND_LENNY = CopyConfigTest.class.getResource("/data-with-artists-penny-and-lenny-without-year.xml").getPath();
    private static final String EMPTY_JUKEBOX_FILE = CopyConfigTest.class.getResource("/empty-example-jukebox.xml").getPath();

    @Before
    public void initServer() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        createServer();
    }

    private void setNonEmptyCandidateDS() throws ModelNodeInitException, SchemaBuildException {
        createServerWithCandidateDS();
        loadXmlDataIntoServer(m_server, JUKEBOX_DATA_ARTIST_LENNY_WITH_YEAR , StandardDataStores.CANDIDATE);
    }

    private void setEmptyRunningDS() throws ModelNodeInitException, SchemaBuildException {
        createServerWithRunningDS();
        loadXmlDataIntoServer(m_server, EMPTY_JUKEBOX_FILE);

    }

    private void createServer() {
        m_server = new NetConfServerImpl(m_schemaRegistry);
    }

    @Test
    public void testCopyFromNonEmptyCandidateToEmptyRunningWorks() throws SAXException, IOException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals("/ok-response.xml", response);
        
        // do a get-config and make sure the jukebox is not-empty
        verifyGetConfig(null, "/getconfig-unfiltered-with-year.xml");
    }
    
    @Test
    public void testCopyFromNonEmptyCandidateToNonEmptyRunningWorks() throws SAXException, IOException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals("/ok-response.xml", response);
        
        // do a get-config and make sure the jukebox is not-empty and has year fields
        verifyGetConfig(null, "/getconfig-unfiltered-with-year.xml");
    }
    
    @Test
    public void testCopyEmptyCandidateToNonEmptyRunningWorks() throws SAXException, IOException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals("/ok-response.xml", response);
        
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(null, "/empty-jukebox.xml");
    }
    
    @Test
    public void testCopyEmptyCandidateToEmptyRunningWorks() throws SAXException, IOException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals("/ok-response.xml", response);
        
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(null, "/empty-jukebox.xml");
    }

    @Test
    public void testCopySourceConfigElementToEmptyRunningWorks() throws SAXException, IOException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals("/ok-response.xml", response);
        // do a get-config and make sure the jukebox is updated
        verifyGetConfig(null, "/getconfig-unfiltered-with-year-updated.xml");
    }

    @Test
    public void testCopySourceConfigElementToNonEmptyRunningWorks() throws SAXException, IOException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals("/ok-response.xml", response);
        // do a get-config and make sure the jukebox is updated
        verifyGetConfig(null, "/getconfig-unfiltered-with-year-updated.xml");
    }

    @Test
    public void testCopySourceConfigElementToNonEmptyRunningWorks2() throws SAXException, IOException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals("/ok-response.xml", response);
        // do a get-config and make sure the jukebox is updated
        verifyGetConfig(null, "/getconfig-unfiltered2.xml");
    }

    private void setEmptyCandidateDS() throws ModelNodeInitException, SchemaBuildException {
        createServerWithCandidateDS();
        loadXmlDataIntoServer(m_server, EMPTY_JUKEBOX_FILE, StandardDataStores.CANDIDATE);
    }

    private void createServerWithCandidateDS() throws ModelNodeInitException, SchemaBuildException {
        m_modelNodeDSM = new InMemoryDSM(m_schemaRegistry);
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

        m_candidateModel = YangUtils.createInMemoryModelNode(getJukeBoxYangFileName(), new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_candidateRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_modelNodeDSM, m_subSystemRegistry);
        ChildContainerHelper childContainerHelper = new RootEntityContainerModelNodeHelper(
                (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(JUKEBOX_SCHEMA_PATH), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        m_candidateRootModelNodeAggregator.addModelServiceRootHelper(JUKEBOX_SCHEMA_PATH, childContainerHelper);
        DataStore dataStore = new DataStore(StandardDataStores.CANDIDATE, m_candidateRootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setDataStore(StandardDataStores.CANDIDATE, dataStore);
    }

    private void setNonEmptyRunningDS() throws ModelNodeInitException, SchemaBuildException {
        createServerWithRunningDS();
        loadXmlDataIntoServer(m_server, JUKEBOX_DATA_ARTISTS_PENNY_AND_LENNY);
    }

    private void setNonEmptyRunningDSWithYear() throws ModelNodeInitException, SchemaBuildException {
        createServerWithRunningDS();
        loadXmlDataIntoServer(m_server, JUKEBOX_DATA_ARTIST_LENNY_WITH_YEAR);
    }

    private void createServerWithRunningDS() throws ModelNodeInitException, SchemaBuildException {
        m_modelNodeDSM = new InMemoryDSM(m_schemaRegistry);
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

        m_runningModel = YangUtils.createInMemoryModelNode(getJukeBoxYangFileName(), new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_modelNodeDSM, m_subSystemRegistry);
        ChildContainerHelper childContainerHelper = new RootEntityContainerModelNodeHelper(
                (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(JUKEBOX_SCHEMA_PATH), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        m_runningRootModelNodeAggregator.addModelServiceRootHelper(JUKEBOX_SCHEMA_PATH, childContainerHelper);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
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
    public void testCopyConfigOnLockedDS() throws SAXException, IOException, ModelNodeInitException, SchemaBuildException {
        setNonEmptyRunningDS();

        LockRequest lockRequest = new LockRequest();
        lockRequest.setMessageId("101");
        lockRequest.setTarget("running");

        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onHello(m_clientInfo, null);
        m_server.onLock(m_clientInfo,lockRequest,response);
        assertXMLEquals("/ok-response.xml", response);

        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setSource(StandardDataStores.RUNNING, false).
                setTarget(StandardDataStores.RUNNING, false);
        copyConfigRequest.setMessageId("1");

        NetconfClientInfo m_clientInfo2 = new NetconfClientInfo("unit-test2",2);
        response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onHello(m_clientInfo2,null);
        m_server.onCopyConfig(m_clientInfo2, copyConfigRequest, response);
        String errorResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <rpc-error>\n" +
                "      <error-type>application</error-type>\n" +
                "      <error-tag>in-use</error-tag>\n" +
                "      <error-severity>error</error-severity>\n" +
                "      <error-message>The request requires a resource that is already in use</error-message>\n" +
                "   </rpc-error>\n" +
                "</rpc-reply>\n";
        assertXMLStringEquals(errorResponse, responseToString(response));
    }
    
    @Test
    public void testCopyConfigIsValidated() throws ModelNodeInitException, SchemaBuildException {

        setNonEmptyRunningDSWithYear();
        // do a get-config and make sure the jukebox is not empty
        verifyGetConfig(null, "/getconfig-unfiltered-with-year.xml");

        Element sourceConfigElement = loadAsXml("/InvalidCopyConfig.xml");
        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setTarget(StandardDataStores.RUNNING, false).setSourceConfigElement(sourceConfigElement );
        copyConfigRequest.setMessageId("1");

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        try{
        m_server.onCopyConfig(m_clientInfo, copyConfigRequest, response);
        } catch(ValidationException e){
            assertEquals(NetconfRpcErrorTag.UNKNOWN_NAMESPACE, e.getRpcError().getErrorTag());
            assertEquals("An unexpected namespace 'http://example.com/ns/example-jukebox2' is present", e.getRpcError().getErrorMessage());
        }
        
        // do a get-config and make sure the jukebox data is unchanged
        verifyGetConfig(null, "/getconfig-unfiltered-with-year.xml");
      
    }
}
