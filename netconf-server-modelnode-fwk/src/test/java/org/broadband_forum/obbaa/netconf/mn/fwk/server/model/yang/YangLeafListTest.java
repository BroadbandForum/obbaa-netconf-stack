/**
 *
 */
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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendCopyConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfigV2;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfigWithExactMatch;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyNotification;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelperImpl;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

/**
 *
 *
 */
@RunWith(RequestScopeJunitRunner.class)
public class YangLeafListTest extends AbstractYangValidationTestSetup {

    private static final String LEAFLIST_MODEL_YANG = "/yangs/example-jukebox-with-leaf-list.yang";
    private static final String LEAFLIST_FULL_XML = "/example-jukebox-with-leaf-list.xml";
    private static final String LEAFLIST_FULL_XML_WITH_ORDERED_BY_USER_LEAFLISTS = "/example-jukebox-with-ordered-by-user-leaf-lists.xml";
    private static final String LEAFLIST_WITH_YEAR_XML = "/jukebox-year-leaf-list.xml";
    private static final String LEAFLIST_EMPTY_XML = "/empty-jukebox-library.xml";
    private static final String TWO_LEAFLIST_XML = "/example-jukebox-with-two-leaf-list.xml";
    private static final String TWO_LEAFLIST_DIFF_TYPE_XML = "/example-jukebox-with-two-leaf-list-diff-type.xml";

    private static final String LEAFLIST_GET_FILTER_REQUEST_XML = "/filter-two-matching-yang-leaf-list.xml";
    private static final String LEAFLIST_GET_FILTER_REQUEST_XML_DELETED = "/filter-yang-leaf-list-for-deleted-removed.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_REQUEST_XML = "/create-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_REQUEST2_XML = "/create-leaf-list-singer.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML = "/create-album-circus-leaf-list-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_DELETE_REQUEST_XML = "/delete-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_REQUEST_XML = "/merge-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_REQUEST_WITH_CREATE_LEAF_XML = "/create-album-circus-leaf-list-createSameValue.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML = "/merge-album-circus-leaf-list-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_REMOVE_REQUEST_XML = "/remove-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST_XML = "/replace-config-album-circus-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST2_XML = "/replace-config-album-circus-leaf-list2.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST3_XML = "/replace-config-album-circus-leaf-list3.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST4_XML = "/replace-config-album-circus-leaf-list4.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST6_XML = "/replace-config-album-circus-leaf-list6.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST7_XML = "/replace-config-album-circus-leaf-list7.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST8_XML = "/replace-config-album-circus-leaf-list8.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST9_XML = "/replace-config-album-circus-leaf-list9.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_REQUEST10_XML = "/replace-config-album-circus-leaf-list10.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML = "/replace-album-circus-leaf-list-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST2_XML = "/replace-album-circus-leaf-list-ordered-by-user2.xml";

    private static final String LEAFLIST_GET_EXPECTED_XML = "/get-unfiltered-with-leaf-list-yang.xml";
    private static final String LEAFLIST_GET_FILTER_EXPECTED_XML = "/get-response-two-match-leaf-list.xml";
    private static final String LEAFLIST_GETCONFIG_EXPECTED_XML = "/getconfig-unfiltered-with-leaf-list-yang.xml";
    private static final String LEAFLIST_GETCONFIG_FILTER_EXPECTED_XML = "/get-response-two-match-leaf-list.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_EXPECTED_XML = "/create-album-circus-leaf-list-expected.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_SAMEVALUE_ERROR_RESPONSE_XML = "/create-album-circus-leaf-list-createSameValue-errorResponse.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_SAMEVALUE_ERROR_EXPECTED_XML = "/create-album-circus-leaf-list-createSameValue-expected.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML = "/create-album-circus-leaf-list-expected-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_DELETE_EXPECTED_XML = "/delete-album-circus-leaf-list-expected.xml";
    private static final String LEAFLIST_EDITCONFIG_REMOVE_EXPECTED_XML = "/remove-album-circus-leaf-list-expected.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_EXPECTED_XML = "/merge-album-circus-leaf-list-expected.xml";
    private static final String MERGE_ALBUM_CIRCUS_LEAF_LIST_GET_FILTER_XML = "/merge-album-circus-leaf-list-get-filter.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML = "/merge-album-circus-leaf-list-expected-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED_XML = "/replace-album-circus-leaf-list-expected.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED2_XML = "/replace-album-circus-leaf-list-expected2.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED3_XML = "/replace-album-circus-leaf-list-expected3.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED4_XML = "/replace-album-circus-leaf-list-expected4.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED6_XML = "/replace-album-circus-leaf-list-expected6.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED7_XML = "/replace-album-circus-leaf-list-expected7.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED8_XML = "/replace-album-circus-leaf-list-expected8.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED9_XML = "/replace-album-circus-leaf-list-expected9.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_EXPECTED10_XML = "/replace-album-circus-leaf-list-expected10.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML = "/replace-album-circus-leaf-list-expected-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED2_XML = "/replace-album-circus-leaf-list-expected-ordered-by-user2.xml";

    public static final String JB_WITH_SINGER_NS = "http://example.com/ns/example-jukebox-with-singer";
    public static final SchemaPath JUKEBOX_SCHEMA_PATH = SchemaPath.create(true, QName.create(JB_WITH_SINGER_NS, JB_REVISION, JUKEBOX_LOCAL_NAME));

    private NetConfServerImpl m_server;

    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();

    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private ModelNodeDataStoreManager m_runningModelNodeDsm;
    private ModelNode m_jukeboxModelNode;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    private DsmJukeboxSubsystem m_dsmJukeboxSubsystem;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_server = getNcServer();
    }

    private void setDataStoreAndModel(String datastore, String dataXmlPath) throws ModelNodeInitException, SchemaBuildException {

        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(LEAFLIST_MODEL_YANG));
        m_schemaRegistry.loadSchemaContext("yang", yangs, Collections.emptySet(), Collections.emptyMap());

        m_runningModelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_dsmJukeboxSubsystem = new DsmJukeboxSubsystem(m_runningModelNodeDsm, "http://example.com/ns/example-jukebox-with-singer", m_schemaRegistry);

        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_runningModelNodeDsm, m_subSystemRegistry);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_jukeboxModelNode = YangUtils.createInMemoryModelNode(YangLeafListTest.class.getResource(LEAFLIST_MODEL_YANG).getPath(), m_dsmJukeboxSubsystem, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_runningModelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRoot(m_componentId, m_jukeboxModelNode);
        DataStore dataStore = new DataStore(datastore, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setDataStore(datastore, dataStore);

        String xmlFilePath = YangLeafListTest.class.getResource(dataXmlPath).getPath();
        loadXmlDataIntoServer(m_server, xmlFilePath);
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
        verifyGetConfig(m_server, StandardDataStores.RUNNING, LEAFLIST_GET_FILTER_REQUEST_XML, LEAFLIST_GETCONFIG_FILTER_EXPECTED_XML, "1");
    }

    @Test
    public void testCopyConfig() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {

        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        //prepare netconf server with candidate data store
        ModelNodeDataStoreManager candidateModelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        DsmJukeboxSubsystem subSystem = new DsmJukeboxSubsystem(candidateModelNodeDsm, JB_NS, m_schemaRegistry);
        ModelNode jukeboxModelNodeCS = YangUtils.createInMemoryModelNode(YangLeafListTest.class.getResource(LEAFLIST_MODEL_YANG).getPath(), subSystem, new ModelNodeHelperRegistryImpl(m_schemaRegistry),
                m_subSystemRegistry, m_schemaRegistry, candidateModelNodeDsm);
        RootModelNodeAggregator rootModelNodeAggregatorCS = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, candidateModelNodeDsm, m_subSystemRegistry).addModelServiceRoot(m_componentId, jukeboxModelNodeCS);
        m_server.setDataStore(StandardDataStores.CANDIDATE, new DataStore(StandardDataStores.CANDIDATE, rootModelNodeAggregatorCS, m_subSystemRegistry));

        NetConfResponse response = sendCopyConfig(m_server, m_clientInfo, StandardDataStores.RUNNING, StandardDataStores.CANDIDATE, "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.CANDIDATE, LEAFLIST_GET_FILTER_REQUEST_XML, LEAFLIST_GET_FILTER_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigCreate() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_EMPTY_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_CREATE_REQUEST_XML), "1");

        sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_CREATE_REQUEST_XML), "1");
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_CREATE_EXPECTED_XML, "1");
    }


    @Test
    public void testEditConfigCreateLeafWithSameValue() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_EMPTY_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_MERGE_REQUEST_WITH_CREATE_LEAF_XML), "1");

        // assert Not-Ok response
        assertXMLEquals(LEAFLIST_EDITCONFIG_CREATE_SAMEVALUE_ERROR_RESPONSE_XML, response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_CREATE_SAMEVALUE_ERROR_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigCreateOrderedByUser() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_EMPTY_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigDelete() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_DELETE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, LEAFLIST_GET_FILTER_REQUEST_XML_DELETED, LEAFLIST_EDITCONFIG_DELETE_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigMerge() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_WITH_YEAR_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_MERGE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, MERGE_ALBUM_CIRCUS_LEAF_LIST_GET_FILTER_XML, LEAFLIST_EDITCONFIG_MERGE_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigMergeOrderedByUser() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_WITH_YEAR_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML), "1");

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

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REMOVE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, LEAFLIST_GET_FILTER_REQUEST_XML_DELETED, LEAFLIST_EDITCONFIG_REMOVE_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigReplace() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigReplace2() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException, NetconfMessageBuilderException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST2_XML), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Circus']] -> modify\n" +
                "     name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "     location -> modify { previousVal = 'desktop/mymusic', currentVal = 'downloads/songs' }\n" +
                "     format -> delete { previousVal = 'wma', currentVal = 'null' }\n" +
                "     length -> delete { previousVal = '180', currentVal = 'null' }\n" +
                "     singer -> delete { previousVal = 'Johnson', currentVal = 'null' }\n" +
                "     singer -> delete { previousVal = 'Angelina', currentVal = 'null' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/replace-song-circus-with-leaflist-deletion.xml");
        }

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED2_XML, "1");
    }

    @Test
    public void testEditConfigReplace3() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException, NetconfMessageBuilderException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST3_XML), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Circus']] -> modify\n" +
                "     name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "     location -> modify { previousVal = 'desktop/mymusic', currentVal = 'downloads/songs' }\n" +
                "     singer -> modify { previousVal = 'Johnson', currentVal = 'null' }\n" +
                "     singer -> modify { previousVal = 'Angelina', currentVal = 'null' }\n" +
                "     singer -> modify { previousVal = 'null', currentVal = 'Michel' }\n" +
                "     singer -> modify { previousVal = 'null', currentVal = 'George' }\n" +
                "     format -> delete { previousVal = 'wma', currentVal = 'null' }\n" +
                "     length -> delete { previousVal = '180', currentVal = 'null' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/replace-song-circus-with-leaflist-modification.xml");
        }

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED3_XML, "1");
    }

    @Test
    public void testEditConfigReplace4() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException, NetconfMessageBuilderException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST4_XML), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Circus']] -> modify\n" +
                "     name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "     location -> modify { previousVal = 'desktop/mymusic', currentVal = 'downloads/songs' }\n" +
                "     singer -> modify { previousVal = 'Johnson', currentVal = 'Johnson' }\n" +
                "     singer -> modify { previousVal = 'Angelina', currentVal = 'null' }\n" +
                "     singer -> modify { previousVal = 'null', currentVal = 'George' }\n" +
                "     format -> delete { previousVal = 'wma', currentVal = 'null' }\n" +
                "     length -> delete { previousVal = '180', currentVal = 'null' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/replace-song-circus-with-leaflist-modification.xml");
        }

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED4_XML, "1");
    }

    @Test
    public void testEditConfigReplace5() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException, NetconfMessageBuilderException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST2_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_CREATE_REQUEST2_XML), "1");

        response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Circus']] -> modify\n" +
                "     name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "     singer -> create { previousVal = 'null', currentVal = 'Michel' }\n" +
                "     singer -> create { previousVal = 'null', currentVal = 'George' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/replace-song-circus-with-leaflist-creation.xml");
        }

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED3_XML, "1");
    }

    @Test
    public void testEditConfigReplace6() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST6_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Circus']] -> modify\n" +
                "     name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "     location -> modify { previousVal = 'desktop/mymusic', currentVal = 'downloads/songs' }\n" +
                "     singer -> none { previousVal = 'Johnson', currentVal = 'Johnson' }\n" +
                "     singer -> none { previousVal = 'Angelina', currentVal = 'Angelina' }\n" +
                "     format -> delete { previousVal = 'wma', currentVal = 'null' }\n" +
                "     length -> delete { previousVal = '180', currentVal = 'null' }\n"));

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED6_XML, "1");
    }

    @Test
    public void testEditConfigReplace_ReordersLeafListsAsSpecifiedInRequest() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML_WITH_ORDERED_BY_USER_LEAFLISTS);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST9_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Circus']] -> modify\n" +
                "     name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "     singer-ordered-by-user -> modify { previousVal = 'Angelina', currentVal = 'Angelina' }\n" +
                "     singer-ordered-by-user -> modify { previousVal = 'Johnson', currentVal = 'Johnson' }\n" +
                "     singer-ordered-by-user -> modify { previousVal = 'Micheal', currentVal = 'Micheal' }\n" +
                "     location -> delete { previousVal = 'desktop/mymusic', currentVal = 'null' }\n" +
                "     format -> delete { previousVal = 'wma', currentVal = 'null' }\n" +
                "     length -> delete { previousVal = '180', currentVal = 'null' }\n"));

        //verify get reponse with filter

        verifyGetConfigWithExactMatch(m_server, (String) null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED9_XML, "1");
    }

    @Test
    public void testEditConfigReplace_ReordersLeafListsAsSpecifiedInRequest_WithNoDeltaChangeButStillRearrange() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_FULL_XML_WITH_ORDERED_BY_USER_LEAFLISTS);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST10_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Circus']] -> modify\n" +
                "     name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "     singer-ordered-by-user -> modify { previousVal = 'Angelina', currentVal = 'Angelina' }\n" +
                "     singer-ordered-by-user -> modify { previousVal = 'Johnson', currentVal = 'Johnson' }\n" +
                "     singer-ordered-by-user -> modify { previousVal = 'Micheal', currentVal = 'Micheal' }\n" +
                "     location -> delete { previousVal = 'desktop/mymusic', currentVal = 'null' }\n" +
                "     format -> delete { previousVal = 'wma', currentVal = 'null' }\n" +
                "     length -> delete { previousVal = '180', currentVal = 'null' }\n"));

        //verify get reponse with filter

        verifyGetConfigWithExactMatch(m_server, (String) null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED10_XML, "1");
    }

    @Test
    public void testEditConfigReplace_DeletesNonExistantLeafLists() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, TWO_LEAFLIST_XML);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> create\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> create\n" +
                "   name -> create { previousVal = 'null', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = 'Greatest hits' }\n" +
                "    genre -> create { previousVal = 'null', currentVal = 'jbox:jazz' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']/song[name='Are you gonne go my way']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Are you gonne go my way' }\n" +
                "     singer -> create { previousVal = 'null', currentVal = 'Michael' }\n" +
                "     singer -> create { previousVal = 'null', currentVal = 'Jackson' }\n"));

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST7_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> modify\n" +
                "    name -> none { previousVal = 'Greatest hits', currentVal = 'Greatest hits' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']/song[name='Are you gonne go my way']] -> modify\n" +
                "     name -> none { previousVal = 'Are you gonne go my way', currentVal = 'Are you gonne go my way' }\n" +
                "     singer -> modify { previousVal = 'Michael', currentVal = 'Michael' }\n" +
                "     singer -> modify { previousVal = 'Jackson', currentVal = 'null' }\n"));

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED7_XML, "1");
    }

    @Test
    public void testEditConfigReplace_DeletesNonExistantLeafListsDiffType() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, TWO_LEAFLIST_DIFF_TYPE_XML);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> create\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> create\n" +
                "   name -> create { previousVal = 'null', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = 'Greatest hits' }\n" +
                "    genre -> create { previousVal = 'null', currentVal = 'jbox:jazz' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']/song[name='Are you gonne go my way']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Are you gonne go my way' }\n" +
                "     singer -> create { previousVal = 'null', currentVal = 'Michael' }\n" +
                "     singer -> create { previousVal = 'null', currentVal = 'Jackson' }\n" +
                "     singer-ordered-by-user -> create { previousVal = 'null', currentVal = 'singerA' }\n" +
                "     singer-ordered-by-user -> create { previousVal = 'null', currentVal = 'singerB' }\n"));

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_REQUEST8_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_dsmJukeboxSubsystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> modify\n" +
                "    name -> none { previousVal = 'Greatest hits', currentVal = 'Greatest hits' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']/song[name='Are you gonne go my way']] -> modify\n" +
                "     name -> none { previousVal = 'Are you gonne go my way', currentVal = 'Are you gonne go my way' }\n" +
                "     singer -> modify { previousVal = 'Michael', currentVal = 'Michael' }\n" +
                "     singer -> modify { previousVal = 'Jackson', currentVal = 'null' }\n" +
                "     singer-ordered-by-user -> modify { previousVal = 'singerA', currentVal = 'singerA' }\n" +
                "     singer-ordered-by-user -> modify { previousVal = 'singerB', currentVal = 'null' }\n"));

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LEAFLIST_EDITCONFIG_REPLACE_EXPECTED8_XML, "1");
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

    @Test
    public void testEditConfigReplaceOrderedByUser2() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        //prepare netconf server with running data store
        setDataStoreAndModel(StandardDataStores.RUNNING, LEAFLIST_WITH_YEAR_XML);

        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST2_XML), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //verify get reponse with filter
        verifyGetConfig(m_server, StandardDataStores.RUNNING, MERGE_ALBUM_CIRCUS_LEAF_LIST_GET_FILTER_XML, LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED2_XML, "1");
    }
}
