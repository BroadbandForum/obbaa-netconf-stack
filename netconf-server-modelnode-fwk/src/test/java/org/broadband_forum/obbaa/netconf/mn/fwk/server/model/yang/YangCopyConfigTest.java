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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendCopyConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class YangCopyConfigTest {

    private static final String EXPECTED_EMPTY_JUKEBOX_XML = "/empty-jukebox.xml";
    private static final String EXPECTED_UNFILTERED_XML = "/getconfig-unfiltered-yang.xml";
    private static final String COMPONENT_ID = "test";
    private static final String OK_RESPONSE_XML = "/ok-response.xml";
    private static final String YANG_MODEL_PATH = YangCopyConfigTest.class.getResource("/yangs/example-jukebox.yang").getPath();
    private static final String EMPTY_JUKEBOX_PATH = YangCopyConfigTest.class.getResource("/empty-example-jukebox.xml").getPath();
    private static final String JUKEBOX_WITH_LEAF_UNDER_JUKEBOX_PATH = YangCopyConfigTest.class.getResource("/example-jukebox-with-leaf-under-jukebox.xml").getPath();
    private static final String GETCONFIG_UNFILTERED_WITH_LEAF_UNDER_JUKEBOX = "/getconfig-unfiltered-with-leaf-under-jukebox.xml";
    private static final String JUKEBOX_WITHOUT_LEAF_UNDER_JUKEBOX = "/example-jukebox-without-leaf-under-jukebox.xml";
    private static final String GETCONFIG_UNFILTERED_WITHOUT_LEAF_UNDER_JUKEBOX = "/getconfig-unfiltered-without-leaf-under-jukebox.xml";

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
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_xmlFilePath = YangCopyConfigTest.class.getResource("/example-jukebox.xml").getPath();
        m_clientInfo = new NetconfClientInfo("unit-test", 1);
    }

    private void setNonEmptyRunningDS(String defaultXmlFilePath) throws ModelNodeInitException, SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_runningModel = YangUtils.createInMemoryModelNode(YANG_MODEL_PATH, new LocalSubSystem(), modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(COMPONENT_ID, m_runningModel);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator, m_subSystemRegistry );
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, defaultXmlFilePath, StandardDataStores.RUNNING);
    }
    
    private void setNonEmptyCandidateDS(String defaultXmlFilePath) throws ModelNodeInitException, SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_candidateModel = YangUtils.createInMemoryModelNode(YANG_MODEL_PATH, new LocalSubSystem(), modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_candidateRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(COMPONENT_ID, m_candidateModel);
        DataStore dataStore = new DataStore(StandardDataStores.CANDIDATE, m_candidateRootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setDataStore(StandardDataStores.CANDIDATE, dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, defaultXmlFilePath, StandardDataStores.CANDIDATE);
    }

    private void setEmptyRunningDS() throws ModelNodeInitException, SchemaBuildException {
        setNonEmptyRunningDS(EMPTY_JUKEBOX_PATH);
    }
    
    private void setEmptyCandidateDS() throws ModelNodeInitException, SchemaBuildException {
        setNonEmptyCandidateDS(EMPTY_JUKEBOX_PATH);
    }

    @Test
    public void testCopyFromNonEmptyCandidateToEmptyRunningWorks() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        setEmptyRunningDS();
        setNonEmptyCandidateDS(m_xmlFilePath);
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
        setNonEmptyRunningDS(m_xmlFilePath);
        setNonEmptyCandidateDS(m_xmlFilePath);
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
        setNonEmptyRunningDS(m_xmlFilePath);
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
        setNonEmptyRunningDS(m_xmlFilePath);
        setNonEmptyCandidateDS(m_xmlFilePath);
        // do a get-config and make sure the jukebox is non-empty, without year
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_UNFILTERED_XML, "1");

        NetConfResponse response =  sendCopyConfig(m_server, m_clientInfo, StandardDataStores.CANDIDATE, StandardDataStores.RUNNING, "1");

        // assert Ok response
        assertXMLEquals(OK_RESPONSE_XML, response);

        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, EXPECTED_UNFILTERED_XML, "1");

    }

    @Test
    public void testCopyConfigWithNonEmptyRunningToRunning_WithoutLeaf() throws Exception {
        setNonEmptyRunningDS(JUKEBOX_WITH_LEAF_UNDER_JUKEBOX_PATH);
        // do a get-config and make sure the jukebox is empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, GETCONFIG_UNFILTERED_WITH_LEAF_UNDER_JUKEBOX, "1");

        NetConfResponse response = sendCopyConfig(m_server, m_clientInfo, StandardDataStores.RUNNING, TestUtil.loadAsXml(JUKEBOX_WITHOUT_LEAF_UNDER_JUKEBOX), "1");

        // assert Ok response
        assertXMLEquals(OK_RESPONSE_XML, response);

        // do a get-config and make sure the jukebox is not-empty
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, GETCONFIG_UNFILTERED_WITHOUT_LEAF_UNDER_JUKEBOX, "1");
    }
}
