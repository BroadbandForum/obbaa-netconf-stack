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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_COUNT_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_COUNT_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_COUNT_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_COUNT_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_QNAME;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.StateRootNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class NetconfGetWithDepthTest {

    private static final String CLIENT_USERNAME = "test";
    private static final String COMPONENT_ID = "example-jukebox?revision=2014-07-04";
    private static final String MESSAGE_ID = "1";
    private static final SchemaPath JUKEBOX_STATE_SCHEMA_PATH = SchemaPath.create(true,
            QName.create("http://example.com/ns/example-jukebox","2014-07-03","jukebox-state"));

    private SubSystemRegistry m_subSystemRegistry;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private NetConfServerImpl m_server;
    private ModelNodeDataStoreManager m_modelNodeDSM;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private NbiNotificationHelper m_nbiNotificationHelper;
    private NetconfClientInfo m_clientInfo;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {

        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext(COMPONENT_ID, getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());

        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_modelNodeDSM = new InMemoryDSM(m_schemaRegistry);

        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_modelNodeDSM, m_subSystemRegistry);

        m_server = new NetConfServerImpl(m_schemaRegistry);
        ModelNode jukeboxModelNode = YangUtils.createInMemoryModelNode(getYangFilePaths(),
                new TestSubsystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        m_rootModelNodeAggregator.addModelServiceRoot(COMPONENT_ID, jukeboxModelNode);

        StateRootNode jukeboxStateModelNode = new StateRootNode(JUKEBOX_STATE_SCHEMA_PATH,
                null, m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        m_rootModelNodeAggregator.addModelServiceRoot(COMPONENT_ID, jukeboxStateModelNode);

        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        String xmlFilePath = TestUtil.class.getResource("/getwithdepthtest/default-data.xml").getPath();;
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
        m_clientInfo = new NetconfClientInfo(CLIENT_USERNAME, 1);
    }

    @Test
    public void testGetWithDepthWithoutFilter() throws Exception {

        GetRequest request = new GetRequest();
        request.setMessageId(MESSAGE_ID);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(MESSAGE_ID);
        request.setDepth(1);
        m_server.onGet(m_clientInfo, request, response);
        Element actual = response.getData();
        Element expected = getExpectedGetResultForLevel1();
        assertXMLEquals(expected, actual);


        request.setDepth(2);
        m_server.onGet(m_clientInfo, request, response);
        actual = response.getData();
        expected = getExpectedGetResultForLevel2();
        assertXMLEquals(expected, actual);

        request.setDepth(3);
        m_server.onGet(m_clientInfo, request, response);
        actual = response.getData();
        expected = getExpectedResultForLevel3_WithoutFilter();
        assertXMLEquals(expected, actual);

        request.setDepth(5);
        m_server.onGet(m_clientInfo, request, response);
        actual = response.getData();
        expected = getExpectedGetResultForLevel5();
        assertXMLEquals(expected, actual);

        request.setDepth(6);
        m_server.onGet(m_clientInfo, request, response);
        actual = response.getData();
        expected = getExpectedGetResultForLevel6();
        assertXMLEquals(expected, actual);
    }

    @Test
    public void testGetWithDepthAndFieldsWithoutFilter() throws Exception {

        GetRequest request = new GetRequest();
        request.setMessageId(MESSAGE_ID);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(MESSAGE_ID);

        request.setDepth(2);
        Map<String, List<QName>> fieldValues = new HashMap<>();
        fieldValues.put(LIBRARY_LOCAL_NAME, Collections.singletonList(ARTIST_QNAME));
        fieldValues.put(ARTIST_LOCAL_NAME, Collections.singletonList(NAME_QNAME));
        request.setFieldValues(fieldValues);

        m_server.onGet(m_clientInfo, request, response);
        Element actual = response.getData();
        Element expected = getExpectedResultWithDepthAndFields();
        assertXMLEquals(expected, actual);

        fieldValues.put(ARTIST_LOCAL_NAME, Collections.singletonList(NAME_QNAME));

        request.setFieldValues(fieldValues);
        m_server.onGet(m_clientInfo, request, response);
        request.setDepth(3);
        m_server.onGet(m_clientInfo, request, response);
        actual = response.getData();
        expected = getExpectedResultWithDepth3AndFields();// Depth 1 or 2 doesn't matter for this case
        assertXMLEquals(expected, actual);

        fieldValues.put(ARTIST_LOCAL_NAME, Collections.singletonList(NAME_QNAME));
        fieldValues.put(LIBRARY_LOCAL_NAME, Collections.singletonList(SONG_COUNT_QNAME));

        request.setFieldValues(fieldValues);
        m_server.onGet(m_clientInfo, request, response);
        request.setDepth(2);
        m_server.onGet(m_clientInfo, request, response);
        actual = response.getData();
        expected = getExpectedResultWithDepth2AndFields();
        assertXMLEquals(expected, actual);
    }

    @Test
    public void testGetConfigWithDepthWithoutFilter() throws Exception {
        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId(MESSAGE_ID);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(MESSAGE_ID);

        request.setDepth(15); // Full tree
        m_server.onGetConfig(m_clientInfo, request, response);
        assertXMLEquals("/getconfig-unfiltered1.xml", response);


        request.setDepth(6);
        m_server.onGetConfig(m_clientInfo, request, response);
        Element expected = getExpectedConfigResultForLevel6();
        assertXMLEquals(expected, response.getData());

        request.setDepth(5);
        m_server.onGetConfig(m_clientInfo, request, response);
        Element actual = response.getData();
        expected = getExpectedConfigResultForLevel5();
        assertXMLEquals(expected, actual);
    }

    @Test
    public void testGetWithDepthWithFilter() throws Exception {
        GetRequest request = new GetRequest();
        request.setMessageId(MESSAGE_ID);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(MESSAGE_ID);
        String filterInput = "/filter-two-matching.xml";
        NetconfFilter filter = new NetconfFilter();
        filter.setType("subtree");
        filter.addXmlFilter(loadAsXml(filterInput));
        request.setFilter(filter);

        request.setDepth(1);
        m_server.onGet(m_clientInfo, request, response);
        Element actual = response.getData();
        String expectedString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "</data>";
        assertXMLEquals(toXml(expectedString), actual);

        request.setDepth(2);
        m_server.onGet(m_clientInfo, request, response);
        actual = response.getData();
        expectedString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\"/>"
                + "</data>";
        assertXMLEquals(toXml(expectedString), actual);

        request.setDepth(3);
        m_server.onGet(m_clientInfo, request, response);
        actual = response.getData();
        Element expected = getExpectedResultForLevel3_WithFilter();
        assertXMLEquals(expected, actual);
    }

    private Element toXml(String xml) throws Exception {
        return DocumentUtils.stringToDocument(xml).getDocumentElement();
    }

    @Test
    public void testGetWithDepthAndFieldsAndFilter() throws Exception {
        GetRequest request = new GetRequest();
        request.setMessageId(MESSAGE_ID);
        String filterInput = "/filter-two-matching.xml";
        NetconfFilter filter = new NetconfFilter();
        filter.setType("subtree");
        filter.addXmlFilter(loadAsXml(filterInput));
        request.setFilter(filter);

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");

        List<QName> artistQNamesList = new ArrayList<>();
        artistQNamesList.add(ARTIST_QNAME);

        List<QName> albumQNamesList = new ArrayList<>();
        albumQNamesList.add(SONG_QNAME);

        List<QName> songQNamesList = new ArrayList<>();
        songQNamesList.add(SONG_QNAME);

        List<QName> songNameList = new ArrayList<QName>();
        songNameList.add(NAME_QNAME);

        Map<String, List<QName>> fieldValues = new HashMap<>();
        fieldValues.put("library", artistQNamesList);
        fieldValues.put("artist", songQNamesList);
        fieldValues.put("album", albumQNamesList);
        fieldValues.put("song", songNameList);
        request.setDepth(4);
        request.setFieldValues(fieldValues);
        m_server.onGet(m_clientInfo, request, response);
        Element actual = response.getData();
        Element expected = getExpectedResultForLevel4WithFields();
        assertXMLEquals(expected, actual);

    }

    @Test
    public void testGetConfigWithDepthWithFilter() throws Exception {
        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId(MESSAGE_ID);
        request.setSource("running");
        String filterInput = "/filter-two-matching.xml";
        NetconfFilter filter = new NetconfFilter();
        filter.setType(NetconfResources.SUBTREE_FILTER);
        filter.addXmlFilter(loadAsXml(filterInput));
        request.setFilter(filter);

        NetConfResponse response = new NetConfResponse();
        response.setMessageId(MESSAGE_ID);

        request.setDepth(4);
        m_server.onGetConfig(m_clientInfo, request, response);
        Element actual = response.getData();
        Element expected = getExpectedResultForLevel4();
        assertXMLEquals(expected, actual);
    }

    private Element getExpectedGetResultForLevel1() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedGetResultForLevel5() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:album-count>2</jbox:album-count>"
                + "<jbox:artist>"
                + "<jbox:album/>"
                + "<jbox:album/>"
                + "<name xmlns=\"http://example.com/ns/example-jukebox\">Lenny</name>"
                + "</jbox:artist>"
                + "<jbox:artist-count>1</jbox:artist-count>"
                + "<jbox:poprock>rocknroll</jbox:poprock>"
                + "<jbox:song-count>5</jbox:song-count>"
                + "<jbox:symphony>"
                + "<jbox:beethovenlist/>"
                + "</jbox:symphony>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "<jbox:jukebox-state xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:capabilities>"
                + "<jbox:capability>caps1</jbox:capability>"
                + "<jbox:capability>caps2</jbox:capability>"
                + "<jbox:capability>caps3</jbox:capability>"
                + "</jbox:capabilities>"
                + "</jbox:jukebox-state>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedGetResultForLevel6() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:album-count>2</jbox:album-count>"
                + "<jbox:artist>"
                + "<jbox:album>"
                + "<name xmlns=\"http://example.com/ns/example-jukebox\">Greatest hits</name>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<year xmlns=\"http://example.com/ns/example-jukebox\">1933</year>"
                + "</jbox:album>"
                + "<jbox:album>"
                + "<name xmlns=\"http://example.com/ns/example-jukebox\">Circus</name>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<year xmlns=\"http://example.com/ns/example-jukebox\">1932</year>"
                + "</jbox:album>"
                + "<name xmlns=\"http://example.com/ns/example-jukebox\">Lenny</name>"
                + "</jbox:artist>"
                + "<jbox:artist-count>1</jbox:artist-count>"
                + "<jbox:poprock>rocknroll</jbox:poprock>"
                + "<jbox:song-count>5</jbox:song-count>"
                + "<jbox:symphony>"
                + "<jbox:beethovenlist>"
                + "<jbox:name>moonlight</jbox:name>"
                + "</jbox:beethovenlist>"
                + "</jbox:symphony>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "<jbox:jukebox-state xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:capabilities>"
                + "<jbox:capability>caps1</jbox:capability>"
                + "<jbox:capability>caps2</jbox:capability>"
                + "<jbox:capability>caps3</jbox:capability>"
                + "</jbox:capabilities>"
                + "</jbox:jukebox-state>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedResultWithDepthAndFields() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "<jbox:name>Lenny</jbox:name>"
                + "</jbox:artist>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "<jukebox-state xmlns=\"http://example.com/ns/example-jukebox\"/>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedResultWithDepth3AndFields() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "<jbox:name>Lenny</jbox:name>"
                + "</jbox:artist>"
                + "<jbox:symphony/>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "<jukebox-state xmlns=\"http://example.com/ns/example-jukebox\">"
                + "<capabilities/>"
                + "</jukebox-state>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedResultWithDepth2AndFields() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:song-count>5</jbox:song-count>"
                + "<jbox:artist>"
                + "<jbox:name>Lenny</jbox:name>"
                + "</jbox:artist>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "<jukebox-state xmlns=\"http://example.com/ns/example-jukebox\"/>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedGetResultForLevel2() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "</jbox:jukebox>"
                + "<jbox:jukebox-state xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "</jbox:jukebox-state>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedResultForLevel3_WithFilter() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedResultForLevel3_WithoutFilter() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">\n" +
                "<jbox:library/>\n" +
                "</jbox:jukebox>\n" +
                "<jukebox-state xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "<capabilities/>\n" +
                "</jukebox-state>\n" +
                "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedResultForLevel4() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "</jbox:artist>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedResultForLevel4WithFields() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "<jbox:album>"
                + "<jbox:song>"
                + "<jbox:name>Are you gonne go my way</jbox:name>"
                + "</jbox:song>"
                + "<jbox:song>"
                + "<jbox:name>Fly Away</jbox:name>"
                + "</jbox:song>"
                + "</jbox:album>"
                + "</jbox:artist>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedConfigResultForLevel5() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "<jbox:name>Lenny</jbox:name>"
                + "<jbox:album>"
                + "</jbox:album>"
                + "<jbox:album>"
                + "</jbox:album>"
                + "</jbox:artist>"
                + "<jbox:symphony>"
                + "<jbox:beethovenlist/>"
                + "</jbox:symphony>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getExpectedConfigResultForLevel6() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "<jbox:name>Lenny</jbox:name>"
                + "<jbox:album>"
                + "<jbox:name>Greatest hits</jbox:name>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<jbox:year>1933</jbox:year>"
                + "</jbox:album>"
                + "<jbox:album>"
                + "<jbox:name>Circus</jbox:name>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<jbox:year>1932</jbox:year>"
                + "</jbox:album>"
                + "</jbox:artist>"
                + "<jbox:symphony>"
                + "<jbox:beethovenlist/>"
                + "</jbox:symphony>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private static List<YangTextSchemaSource> getJukeBoxYangs() {
        List<String> yangFiles = getYangFilePaths();
        return TestUtil.getByteSources(yangFiles);
    }

    private static List<String> getYangFilePaths() {
        List<String> yangFiles = new ArrayList<>();
        yangFiles.add("/referenceyangs/jukebox/example-jukebox-with-symphony@2014-07-03.yang");
        yangFiles.add("/referenceyangs/ietf-restconf.yang");
        yangFiles.add("/referenceyangs/ietf-yang-types.yang");
        yangFiles.add("/referenceyangs/ietf-inet-types.yang");
        yangFiles.add("/referenceyangs/jukebox/genre2.yang");
        yangFiles.add("/yangs/example-jukebox-list-multikeys.yang");
        return yangFiles;
    }

    private class TestSubsystem extends AbstractSubSystem {

        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes)
                throws GetAttributeException {
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            Document doc = DocumentUtils.createDocument();
            for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                ModelNodeId modelNodeId = entry.getKey();
                List<QName> stateQNames = entry.getValue().getFirst();
                List<FilterNode> stateFN = entry.getValue().getSecond();
                Map<QName, Object> stateAttributes = new HashMap<>();
                if (isStateUnderLibrary(modelNodeId)) {
                    for (QName attr : stateQNames) {
                        if (attr.getLocalName().equals(ARTIST_COUNT_LOCAL_NAME)) {
                            stateAttributes.put(attr, 1);
                        }
                        if (attr.getLocalName().equals(ALBUM_COUNT_LOCAL_NAME)) {
                            stateAttributes.put(attr, 2);
                        }
                        if (attr.getLocalName().equals(SONG_COUNT_LOCAL_NAME)) {
                            stateAttributes.put(attr, 5);
                        }
                        if (attr.getLocalName().equals("poprock")) {
                            stateAttributes.put(attr, "rocknroll");
                        }
                    }
                    List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
                    stateInfo.put(entry.getKey(), stateElements);
                }else if (isStateUnderBeethovenlist(modelNodeId)) {
                    for (QName attr : stateQNames) {
                        if (attr.getLocalName().equals(NAME)) {
                            stateAttributes.put(attr, "moonlight");
                        }
                    }
                    List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
                    stateInfo.put(entry.getKey(), stateElements);
                }else if(isStateUnderJukeboxState(modelNodeId)){
                    for (FilterNode fn : stateFN) {
                        if (fn.getNodeName().equals("capabilities")) {
                            Element capsElement = doc.createElementNS(JB_NS, "capabilities");
                            Element caps1 = doc.createElementNS(JB_NS, "capability");
                            caps1.setTextContent("caps1");
                            Element caps2 = doc.createElementNS(JB_NS, "capability");
                            caps2.setTextContent("caps2");
                            Element caps3 = doc.createElementNS(JB_NS, "capability");
                            caps3.setTextContent("caps3");
                            capsElement.appendChild(caps1);
                            capsElement.appendChild(caps2);
                            capsElement.appendChild(caps3);
                            List<Element> stateElement = Collections.singletonList(capsElement);
                            stateInfo.put(entry.getKey(), stateElement);
                        }
                    }
                }

            }
            return stateInfo;
        }

        private boolean isStateUnderJukeboxState(ModelNodeId modelNodeId) {
            return modelNodeId.matchesTemplate(
                    new ModelNodeId("/container=jukebox-state", JB_NS));
        }

        private boolean isStateUnderLibrary(ModelNodeId modelNodeId) {
            return modelNodeId.matchesTemplate(
                    new ModelNodeId("/container=jukebox/container=library", JB_NS));
        }

        private boolean isStateUnderBeethovenlist(ModelNodeId modelNodeId) {
            return modelNodeId.matchesTemplate(
                    new ModelNodeId("/container=jukebox/container=library/container=symphony/container=beethovenlist", JB_NS));
        }
    }
}
