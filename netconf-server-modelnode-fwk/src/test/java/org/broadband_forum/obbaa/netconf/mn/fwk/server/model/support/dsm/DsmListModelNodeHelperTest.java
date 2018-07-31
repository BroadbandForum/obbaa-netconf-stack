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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR_QNAME;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfigWithOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class DsmListModelNodeHelperTest {

    private static final String MESSAGE_ID = "1";
    private static final String COMPONENT_ID = "Jukebox";
    private static final String EXAMPLE_JUKEBOX_YANGFILE = "/dsmlistmodelnodehelpertest/example-jukebox.yang";
    private static final String EXAMPLE_JUKEBOX_DEFAULT_XML = "/dsmlistmodelnodehelpertest/example-jukebox-default.xml";
    private static final String ALBUM_FILTER_WITH_ALLKEYS_REQUEST =
            "/dsmlistmodelnodehelpertest/album-filter-with-allkeys-req.xml";
    private static final String ALBUM_FILTER_WITH_ALLKEYS_RESPONSE =
            "/dsmlistmodelnodehelpertest/album-filter-with-allkeys-res.xml";
    private static final String ALBUM_FILTER_WITH_ALLKEYS_ONE_ATT_REQUEST =
            "/dsmlistmodelnodehelpertest/album-filter-with-allkeys-oneAtt-req.xml";
    private static final String ALBUM_FILTER_WITH_ALLKEYS_ONE_ATTR_RESPONSE =
            "/dsmlistmodelnodehelpertest/album-filter-with-allkeys-oneAttr-res.xml";
    private static final String ALBUM_FILTER_WITH_WRONGKEYVALUE_REQUEST =
            "/dsmlistmodelnodehelpertest/album-filter-with-wrongkeyvalue-req.xml";
    private static final String EMPTY_RESPONSE_XML = "/empty-response.xml";
    private static final String ALBUM_FILTER_WITH_WRONGATTRVALUE_REQUEST =
            "/dsmlistmodelnodehelpertest/album-filter-with-wrongattrvalue-req.xml";
    private static final String ALBUM_FILTER_WITH_ONE_ATTR_ONELEAFLIST_REQUEST =
            "/dsmlistmodelnodehelpertest/album-filter-with-oneAttr-oneleaflist-req.xml";
    private static final String ALBUM_FILTER_WITH_ONE_ATTR_ONELEAFLIST_RESPONSE =
            "/dsmlistmodelnodehelpertest/album-filter-with-oneAttr-oneleaflist-res.xml";
    private static final String ALBUM_FILTER_WITH_WRONGATTR_ONELEAFLIST_REQUEST =
            "/dsmlistmodelnodehelpertest/album-filter-with-wrongattr-oneleaflist-req.xml";

    private static final String LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML =
            "/dsmlistmodelnodehelpertest/create-album-circus-leaf-list-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML =
            "/dsmlistmodelnodehelpertest/create-album-circus-leaf-list-expected-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_FOR_TWO_LEAFLIST_TYPES_ORDER_BY_USER_REQUEST_XML =
            "/dsmlistmodelnodehelpertest/two-ordered-by-user-leaflist-req.xml";
    private static final String LEAFLIST_EDITCONFIG_FOR_TWO_LEAFLIST_TYPES_ORDER_BY_USER_EXPECTED_XML =
            "/dsmlistmodelnodehelpertest/two-ordered-by-user-leaflist-res.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML =
            "/dsmlistmodelnodehelpertest/merge-album-circus-leaf-list-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML =
            "/dsmlistmodelnodehelpertest/merge-album-circus-leaf-list-expected-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML =
            "/dsmlistmodelnodehelpertest/replace-album-circus-leaf-list-ordered-by-user.xml";
    private static final String LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML =
            "/dsmlistmodelnodehelpertest/replace-album-circus-leaf-list-expected-ordered-by-user.xml";

    private static final String FILTER_ALBUM_ORDERED_BY_SYSTEM =
            "/dsmlistmodelnodehelpertest/filter-album-ordered-by-system.xml";
    private static final String ALBUM_ORDERED_BY_SYSTEM = "/dsmlistmodelnodehelpertest/album-ordered-by-system.xml";
    private static final String FILTER_ALBUM_ORDERED_BY_USER =
            "/dsmlistmodelnodehelpertest/filter-album-ordered-by-user.xml";
    private static final String FILTER_ALBUM_SONG_ORDERED_BY_USER =
            "/dsmlistmodelnodehelpertest/filter-album-song-ordered-by-user.xml";
    private static final String FILTER_ALBUM_SONG_ORDERED_BY_USER_FOR_TWO_LEAFLIST =
            "/dsmlistmodelnodehelpertest/filter-album-song-ordered-by-user-for-two-leaflist.xml";
    private static final String LIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML =
            "/dsmlistmodelnodehelpertest/create-album-circus-list-ordered-by-user.xml";
    private static final String LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML =
            "/dsmlistmodelnodehelpertest/replace-album-circus-list-ordered-by-user.xml";
    private static final String LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_WITHOUT_INSERT_ATTRIBUTE_REQUEST_XML =
            "/dsmlistmodelnodehelpertest/replace-album-circus-list-ordered-by-user-without-insert-attribute.xml";
    private static final String LIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML =
            "/dsmlistmodelnodehelpertest/merge-album-circus-list-ordered-by-user.xml";

    private static final String LIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML =
            "/dsmlistmodelnodehelpertest/create-album-circus-list-ordered-by-user-expected.xml";
    private static final String LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML =
            "/dsmlistmodelnodehelpertest/replace-album-circus-list-ordered-by-user-expected.xml";
    private static final String LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_WITHOUT_INSERT_ATTRIBUTE_EXPECTED_XML =
            "/dsmlistmodelnodehelpertest/replace-album-circus-list-ordered-by-user-without-insert-attribute-expected" +
                    ".xml";
    private static final String LIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML =
            "/dsmlistmodelnodehelpertest/merge-album-circus-list-ordered-by-user-expected.xml";

    private NetConfServerImpl m_netconfServer;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private InMemoryDSM m_modelNodeDsm;
    private InMemoryDSM m_modelNodeDsmSpy;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private String m_yangFilePath;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    private static final ModelNodeId m_artistNodeId = new ModelNodeId
            ("/container=jukebox/container=library/container=artist/name=Lenny",
            JB_NS);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(EXAMPLE_JUKEBOX_YANGFILE)); // The only difference in this yang is that
        // Album has two keys - name
        // and year
        m_schemaRegistry = new SchemaRegistryImpl(yangs, new NoLockService());
        m_netconfServer = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_yangFilePath = TestUtil.class.getResource(EXAMPLE_JUKEBOX_YANGFILE).getPath();
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelNodeDsmSpy = spy(m_modelNodeDsm);
        ModelNode yangModel = YangUtils.createInMemoryModelNode(m_yangFilePath, new LocalSubSystem(),
                m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsmSpy);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_modelNodeDsmSpy, m_subSystemRegistry);
        m_rootModelNodeAggregator.addModelServiceRoot(COMPONENT_ID, yangModel);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_netconfServer.setRunningDataStore(dataStore);
        String xmlFilePath = TestUtil.class.getResource(EXAMPLE_JUKEBOX_DEFAULT_XML).getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

    }

    @Test
    public void testDSMMethodInvocation() throws IOException, SAXException, DataStoreException {

        String albumName = "Greatest hits";

        // Case 1 : When all keys are specified in filter, findNode is to be called
        verifyGet(m_netconfServer, ALBUM_FILTER_WITH_ALLKEYS_REQUEST, ALBUM_FILTER_WITH_ALLKEYS_RESPONSE, MESSAGE_ID);
        Map<QName, ConfigLeafAttribute> keys = new HashMap<>();
        keys.put(YEAR_QNAME, new GenericConfigAttribute("2000"));
        keys.put(NAME_QNAME, new GenericConfigAttribute(albumName));
        // 3 including edits initially
        verify(m_modelNodeDsmSpy, times(3)).findNodes(ALBUM_SCHEMA_PATH, keys, m_artistNodeId);

    }

    @Test
    public void testListCreateOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        String xmlFilePath = TestUtil.class.getResource(LIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML).getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

        // verify get response with filter
        verifyGetConfig(m_netconfServer, StandardDataStores.RUNNING, FILTER_ALBUM_ORDERED_BY_USER,
                LIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testListMergeOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        String xmlFilePath = TestUtil.class.getResource(LIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML).getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

        // verify get response with filter
        verifyGetConfig(m_netconfServer, StandardDataStores.RUNNING, FILTER_ALBUM_ORDERED_BY_USER,
                LIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testListReplaceOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        String xmlFilePath = TestUtil.class.getResource(LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML).getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

        // verify get response with filter
        verifyGetConfig(m_netconfServer, StandardDataStores.RUNNING, FILTER_ALBUM_ORDERED_BY_USER,
                LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testListReplaceOrderedByUserWithoutInsertAttribute()
            throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
        String xmlFilePath = TestUtil.class.getResource
                (LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_WITHOUT_INSERT_ATTRIBUTE_REQUEST_XML)
                .getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

        // verify get response with filter
        verifyGetConfig(m_netconfServer, StandardDataStores.RUNNING, FILTER_ALBUM_ORDERED_BY_USER,
                LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_WITHOUT_INSERT_ATTRIBUTE_EXPECTED_XML, "1");
    }

    @Test
    public void testListOrderedBySystem() throws SAXException, IOException {
        verifyGet(m_netconfServer, FILTER_ALBUM_ORDERED_BY_SYSTEM, ALBUM_ORDERED_BY_SYSTEM, MESSAGE_ID);
    }

    @Test
    public void testLeafListCreateOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        String xmlFilePath = TestUtil.class.getResource(LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML)
                .getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

        // verify get response with filter
        verifyGetConfigWithOrder(m_netconfServer, StandardDataStores.RUNNING, FILTER_ALBUM_SONG_ORDERED_BY_USER,
                LEAFLIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testLeafListMergeOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        String xmlFilePath = TestUtil.class.getResource(LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML)
                .getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

        // verify get response with filter
        verifyGetConfigWithOrder(m_netconfServer, StandardDataStores.RUNNING, FILTER_ALBUM_SONG_ORDERED_BY_USER,
                LEAFLIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testEditConfigReplaceOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        String xmlFilePath = TestUtil.class.getResource(LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML)
                .getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

        // verify get response with filter
        verifyGetConfigWithOrder(m_netconfServer, StandardDataStores.RUNNING, FILTER_ALBUM_SONG_ORDERED_BY_USER,
                LEAFLIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testGetWithMatchCriteria() throws IOException, SAXException {

        // Case 1: When filter has two keys and one attribute - All values match
        verifyGet(m_netconfServer, ALBUM_FILTER_WITH_ALLKEYS_ONE_ATT_REQUEST,
                ALBUM_FILTER_WITH_ALLKEYS_ONE_ATTR_RESPONSE, MESSAGE_ID);

        // Case 2: When filter has two keys and one attribute - one key does not match
        verifyGet(m_netconfServer, ALBUM_FILTER_WITH_WRONGKEYVALUE_REQUEST, EMPTY_RESPONSE_XML, MESSAGE_ID);

        // Case 3: When filter has two keys and one attribute - one attribute does not match
        verifyGet(m_netconfServer, ALBUM_FILTER_WITH_WRONGATTRVALUE_REQUEST, EMPTY_RESPONSE_XML, MESSAGE_ID);

        // Case 4: When filter has one leafList and one key - All values match
        verifyGet(m_netconfServer, ALBUM_FILTER_WITH_ONE_ATTR_ONELEAFLIST_REQUEST,
                ALBUM_FILTER_WITH_ONE_ATTR_ONELEAFLIST_RESPONSE,
                MESSAGE_ID);

        // Case 5: When filter has one leafList and one attribute - attribute does not match
        verifyGet(m_netconfServer, ALBUM_FILTER_WITH_WRONGATTR_ONELEAFLIST_REQUEST, EMPTY_RESPONSE_XML, MESSAGE_ID);
    }

    @Test
    public void testTwoOrderByUserForLeafLists() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException {
        String xmlFilePath = TestUtil.class.getResource
                (LEAFLIST_EDITCONFIG_FOR_TWO_LEAFLIST_TYPES_ORDER_BY_USER_REQUEST_XML).getPath();
        YangUtils.loadXmlDataIntoServer(m_netconfServer, xmlFilePath);

        // verify get response with filter
        verifyGetConfigWithOrder(m_netconfServer, StandardDataStores.RUNNING,
                FILTER_ALBUM_SONG_ORDERED_BY_USER_FOR_TWO_LEAFLIST,
                LEAFLIST_EDITCONFIG_FOR_TWO_LEAFLIST_TYPES_ORDER_BY_USER_EXPECTED_XML, "1");
    }

    @Test
    public void testGetChildInsertIndex() throws SchemaPathBuilderException {
        ModelNodeId modelNodeId = new ModelNodeId("/container=jukebox/container=library/container=artist/name=test",
                JB_NS);

        ModelNodeWithAttributes albumModelNode1 = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, modelNodeId,
                m_modelNodeHelperRegistry, null, m_schemaRegistry, null);
        albumModelNode1.setModelNodeId(new ModelNodeId
                ("/container=jukebox/container=library/container=artist/name=Lenny/" +
                "container=album/name=just-test", JB_NS));

        ModelNodeWithAttributes albumModelNode2 = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, modelNodeId,
                m_modelNodeHelperRegistry, null, m_schemaRegistry, null);
        albumModelNode2.setModelNodeId(new ModelNodeId
                ("/container=jukebox/container=library/container=artist/name=Lenny/" +
                "container=album/name=new-album", JB_NS));

        ModelNodeWithAttributes albumModelNode1NewInstance = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, modelNodeId,
                m_modelNodeHelperRegistry, null, m_schemaRegistry, null);
        albumModelNode1NewInstance.setModelNodeId(new ModelNodeId
                ("/container=jukebox/container=library/container=artist/" +
                "name=Lenny/container=album/name=just-test", JB_NS));

        List<ModelNode> modelNodeList = new ArrayList<>();
        modelNodeList.add(albumModelNode2);
        modelNodeList.add(albumModelNode1NewInstance);

        DsmListModelNodeHelper dsmListModelNodeHelper = new DsmListModelNodeHelper(mock(ListSchemaNode.class),
                m_modelNodeHelperRegistry, m_modelNodeDsm, m_schemaRegistry, m_subSystemRegistry);

        InsertOperation insertOperation = new InsertOperation("after", "[name='just-test']");
        Assert.assertEquals(2, dsmListModelNodeHelper.getChildInsertIndex(modelNodeList, insertOperation,
                albumModelNode1));
        insertOperation = new InsertOperation("before", "[name='just-test']");
        Assert.assertEquals(1, dsmListModelNodeHelper.getChildInsertIndex(modelNodeList, insertOperation,
                albumModelNode1));
        insertOperation = new InsertOperation("first", null);
        Assert.assertEquals(0, dsmListModelNodeHelper.getChildInsertIndex(modelNodeList, insertOperation, null));
        insertOperation = new InsertOperation("last", null);
        Assert.assertEquals(2, dsmListModelNodeHelper.getChildInsertIndex(modelNodeList, insertOperation, null));
    }
}
