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

/**
 *
 */
package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendCopyConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

/**
 * @author gnanavek
 */
public class YangLeafListTest extends AbstractYangValidationTestSetup {

    private static final String LEAFLIST_MODEL_YANG = "/yangs/example-jukebox-with-leaf-list.yang";
    private static final String LEAFLIST_FULL_XML = "/example-jukebox-with-leaf-list.xml";
    private static final String LEAFLIST_WITH_YEAR_XML = "/jukebox-year-leaf-list.xml";
    private static final String LEAFLIST_EMPTY_XML = "/empty-jukebox-library.xml";

    private static final String LEAFLIST_GET_FILTER_REQUEST_XML = "/filter-two-matching-yang-leaf-list.xml";
    private static final String LEAFLIST_GET_FILTER_REQUEST_XML_DELETED = "/filter-yang-leaf-list-for-deleted-removed" +
            ".xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_REQUEST_XML = "/create-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML =
            "/create-album-circus-leaf-list-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_DELETE_REQUEST_XML = "/delete-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_REQUEST_XML = "/merge-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML =
            "/merge-album-circus-leaf-list-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_REMOVE_REQUEST_XML = "/remove-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST_XML = "/replace-config-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML =
            "/replace-album-circus-leaf-list-ordered-by-user.xml";

    private static final String LEAFLIST_GET_EXPECTED_XML = "/get-unfiltered-with-leaf-list-yang.xml";
    private static final String LEAFLIST_GET_FILTER_EXPECTED_XML = "/get-response-two-match-leaf-list.xml";
    private static final String LEAFLIST_GETCONFIG_EXPECTED_XML = "/getconfig-unfiltered-with-leaf-list-yang.xml";
    private static final String LEAFLIST_GETCONFIG_FILTER_EXPECTED_XML = "/get-response-two-match-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_EXPECTED_XML = "/create-album-circus-leaf-list-expected.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML =
            "/create-album-circus-leaf-list-expected-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_DELETE_EXPECTED_XML = "/delete-album-circus-leaf-list-expected.xml";
    private static final String LEAFLIST_EDITCONFIG_REMOVE_EXPECTED_XML = "/remove-album-circus-leaf-list-expected.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_EXPECTED_XML = "/merge-album-circus-leaf-list-expected.xml";
    private static final String MERGE_ALBUM_CIRCUS_LEAF_LIST_GET_FILTER_XML =
            "/merge-album-circus-leaf-list-get-filter.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML =
            "/merge-album-circus-leaf-list-expected-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED_XML = "/replace-album-circus-leaf-list-expected" +
            ".xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML =
            "/replace-album-circus-leaf-list-expected-ordered-by-user.xml";

    private NetConfServerImpl m_server;

    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();

    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeDataStoreManager m_runningModelNodeDsm;
    private ModelNode m_jukeboxModelNode;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), new NoLockService());
        m_server = getNcServer();
    }

    private void setDataStoreAndModel(String datastore, String dataXmlPath) throws ModelNodeInitException,
            SchemaBuildException {

        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(LEAFLIST_MODEL_YANG));
        m_schemaRegistry.loadSchemaContext("yang", yangs, null, Collections.emptyMap());

        m_runningModelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        DsmJukeboxSubsystem subSystem = new DsmJukeboxSubsystem(m_runningModelNodeDsm, "http://example" +
                ".com/ns/example-jukebox-with-singer");

        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_runningModelNodeDsm, m_subSystemRegistry);
        m_jukeboxModelNode = YangUtils.createInMemoryModelNode(YangLeafListTest.class.getResource
                        (LEAFLIST_MODEL_YANG).getPath(), subSystem, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_runningModelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRoot(m_componentId, m_jukeboxModelNode);
        DataStore dataStore = new DataStore(datastore, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setDataStore(datastore, dataStore);

        String xmlFilePath = YangLeafListTest.class.getResource(dataXmlPath).getPath();
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
    }

    @Test
    public void testGet() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        //verify get reponse
        TestUtil.verifyGet(m_server, "", LEAFLIST_GET_EXPECTED_XML, "1");

        //verify get reponse with filter
        TestUtil.verifyGet(m_server, LEAFLIST_GET_FILTER_REQUEST_XML, LEAFLIST_GET_FILTER_EXPECTED_XML, "1");
    }

    @Test
    public void testGetConfig() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        //verify get reponse
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_GETCONFIG_EXPECTED_XML, "1");

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, LEAFLIST_GET_FILTER_REQUEST_XML,
                LEAFLIST_GETCONFIG_FILTER_EXPECTED_XML, "1");
    }

    @Test
    public void testCopyConfig() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {

        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        //prepare netconf server with candidate data store
        ModelNodeDataStoreManager candidateModelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        DsmJukeboxSubsystem subSystem = new DsmJukeboxSubsystem(candidateModelNodeDsm, JB_NS);
        ModelNode jukeboxModelNodeCS = YangUtils.createInMemoryModelNode(YangLeafListTest.class.getResource
                        (LEAFLIST_MODEL_YANG).getPath(), subSystem, new ModelNodeHelperRegistryImpl(m_schemaRegistry),
                m_subSystemRegistry, m_schemaRegistry, candidateModelNodeDsm);
        RootModelNodeAggregator rootModelNodeAggregatorCS = new RootModelNodeAggregatorImpl(m_schemaRegistry,
                m_modelNodeHelperRegistry, candidateModelNodeDsm, m_subSystemRegistry).addModelServiceRoot
                (m_componentId, jukeboxModelNodeCS);
        m_server.setDataStore(StandardDataStores.CANDIDATE, new DataStore(StandardDataStores.CANDIDATE,
                rootModelNodeAggregatorCS, m_subSystemRegistry));

        NetConfResponse response = sendCopyConfig(m_server, m_clientInfo, StandardDataStores.RUNNING,
                StandardDataStores.CANDIDATE, "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.CANDIDATE, LEAFLIST_GET_FILTER_REQUEST_XML,
                LEAFLIST_GET_FILTER_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigCreate() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_EMPTY_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml
                (LEAFLIST_EDITCONFIG_CREATE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_CREATE_EXPECTED_XML, "1");
    }


    @Test
    public void testEditConfigCreateOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_EMPTY_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml
                (LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null,
                LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigDelete() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml
                (LEAFLIST_EDITCONFIG_DELETE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, LEAFLIST_GET_FILTER_REQUEST_XML_DELETED,
                LEAFLIST_EDITCONFIG_DELETE_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigMerge() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_WITH_YEAR_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml
                (LEAFLIST_EDITCONFIG_MERGE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, MERGE_ALBUM_CIRCUS_LEAF_LIST_GET_FILTER_XML,
                LEAFLIST_EDITCONFIG_MERGE_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigMergeOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_WITH_YEAR_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml
                (LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, MERGE_ALBUM_CIRCUS_LEAF_LIST_GET_FILTER_XML,
                LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigRemove() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml
                (LEAFLIST_EDITCONFIG_REMOVE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, LEAFLIST_GET_FILTER_REQUEST_XML_DELETED,
                LEAFLIST_EDITCONFIG_REMOVE_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigReplace() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml
                (LEAFLIST_EDITCONFIG_REPLACE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigReplaceOrderedByUser() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_WITH_YEAR_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, MERGE_ALBUM_CIRCUS_LEAF_LIST_GET_FILTER_XML, LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }
}
