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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_COUNT_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_COUNT_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_COUNT_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Created by keshava on 2/9/16.
 */
@RunWith(RequestScopeJunitRunner.class)
public class GetGetconfigWithStateFilterTest {

    public static final String PRUNED_SUBTREE = "pruned-subtree";
    public static final String MESSAGE_ID = "1";
    public static final String SUBTREE = "subtree";
    private static final String JUKEBOX_WITH_YEAR = GetGetconfigWithStateFilterTest.class.getResource("/data-with-artists-penny-and-lenny-with-year.xml").getPath();
    private static final String JUKEBOX_WITHOUT_YEAR = GetGetconfigWithStateFilterTest.class.getResource("/data-with-artists-penny-and-lenny-without-year.xml").getPath();

    private NetConfServerImpl m_server;

    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeDataStoreManager m_modelNodeDsm;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangsWithAlbumStateAttributes(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        String componentId = "library";
        TestSubsystem subsystem = new TestSubsystem();
        ModelNode jukeboxNode = YangUtils.createInMemoryModelNode(TestUtil.getJukeBoxDepNamesWithStateAttrs(), subsystem ,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry)
                .addModelServiceRoot(componentId, jukeboxNode);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }

    @Test
    public void testGetWithStateFilterHavingPrunedSubtreeOption() throws SAXException, IOException {
        loadXmlDataIntoServer(m_server, JUKEBOX_WITH_YEAR);

        verifyGet(m_server, "/album-state-filter1.xml", "/get-with-album-state-filter1-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter2.xml", "/get-with-album-state-filter2-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter4.xml", "/get-with-album-state-filter4-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter6.xml", "/get-with-album-state-filter6-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter8.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter9.xml", "/get-with-album-state-filter9-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter10.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter11.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter12.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter13.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter14.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter15.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter16.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter17.xml", "/get-with-album-state-filter17-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter18.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter19.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
    }

    @Test
    public void testGetWithStateFilterHavingSubtreeOption() throws SAXException, IOException {
        loadXmlDataIntoServer(m_server, JUKEBOX_WITH_YEAR);

        verifyGet(m_server, "/album-state-filter1.xml", "/get-with-album-state-filter1-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter2.xml", "/get-with-album-state-filter2-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter4.xml", "/get-with-album-state-filter4-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter6.xml", "/get-with-album-state-filter6-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter8.xml", "/get-with-album-state-filter8-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter9.xml", "/get-with-album-state-filter9-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter10.xml", "/get-with-album-state-filter10-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter11.xml", "/get-with-album-state-filter11-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter12.xml", "/get-with-album-state-filter12-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter13.xml", "/get-with-album-state-filter13-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter14.xml", "/get-with-album-state-filter14-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter15.xml", "/get-with-album-state-filter15-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter16.xml", "/empty-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter17.xml", "/get-with-album-state-filter17-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter18.xml", "/get-with-album-state-filter18-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter19.xml", "/get-with-album-state-filter19-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter20.xml", "/get-with-album-state-filter6-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter21.xml", "/get-with-album-state-filter6-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter22.xml", "/get-with-album-state-filter10-subtree-response.xml", MESSAGE_ID, SUBTREE);
    }

    @Test
    public void testGetConfigWithStateFilter() throws SAXException, IOException {
        loadXmlDataIntoServer(m_server, JUKEBOX_WITH_YEAR);

        verifyGetConfig(m_server, "/album-state-filter1.xml", "/response_with_name_ns.xml", MESSAGE_ID);
        verifyGetConfig(m_server, "/album-state-filter7.xml", "/empty-response.xml", MESSAGE_ID);
        verifyGetConfig(m_server, "/album-state-filter3.xml", "/empty-response.xml", MESSAGE_ID);
    }

    @Test
    public void testAllScenariosWithoutYear() throws IOException, SAXException {
        loadXmlDataIntoServer(m_server, JUKEBOX_WITHOUT_YEAR);

        //test get with state filter having pruned subtree option
        verifyGet(m_server, "/album-state-filter1.xml", "/get-with-album-state-filter1-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter2.xml", "/get-with-album-state-filter2-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter4.xml", "/get-with-album-state-filter4-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter6.xml", "/get-with-album-state-filter6-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter8.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter9.xml", "/get-with-album-state-filter9-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter10.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter11.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter12.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter13.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter14.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter15.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter16.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter17.xml", "/get-with-album-state-filter17-pruned-subtree-response-for-data-without-year.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter18.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        verifyGet(m_server, "/album-state-filter19.xml", "/empty-response.xml", MESSAGE_ID, PRUNED_SUBTREE);

        //test get with state filter having subtree option
        verifyGet(m_server, "/album-state-filter1.xml", "/get-with-album-state-filter1-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter2.xml", "/get-with-album-state-filter2-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter4.xml", "/get-with-album-state-filter4-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter6.xml", "/get-with-album-state-filter6-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter8.xml", "/get-with-album-state-filter8-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter9.xml", "/get-with-album-state-filter9-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter10.xml", "/get-with-album-state-filter10-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter11.xml", "/get-with-album-state-filter11-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter12.xml", "/get-with-album-state-filter12-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter13.xml", "/get-with-album-state-filter13-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter14.xml", "/get-with-album-state-filter14-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter15.xml", "/get-with-album-state-filter15-subtree-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter16.xml", "/empty-response.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter17.xml", "/get-with-album-state-filter17-subtree-response-for-data-without-year.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter18.xml", "/get-with-album-state-filter18-subtree-response-for-data-without-year.xml", MESSAGE_ID, SUBTREE);
        verifyGet(m_server, "/album-state-filter19.xml", "/get-with-album-state-filter19-subtree-response.xml", MESSAGE_ID, SUBTREE);

        //test get with state filter
        verifyGetConfig(m_server, "/album-state-filter1.xml", "/response_with_name_ns.xml", MESSAGE_ID);
        verifyGetConfig(m_server, "/album-state-filter7.xml", "/empty-response.xml", MESSAGE_ID);
        verifyGetConfig(m_server, "/album-state-filter3.xml", "/empty-response.xml", MESSAGE_ID);
    }

    private class TestSubsystem extends AbstractSubSystem {

        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
            Document doc = DocumentUtils.createDocument();
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                Map<QName, Object> stateAttributes = new LinkedHashMap<QName, Object>();
                ModelNodeId modelNodeId = entry.getKey();
                List<QName> attributes = entry.getValue().getFirst();
                List<QName> stateQNames = entry.getValue().getFirst();
                if (isStateUnderLibrary(modelNodeId)) {
                    for (QName attr : stateQNames) {
                        if (attr.getLocalName().equals(ARTIST_COUNT_LOCAL_NAME)) {
                            stateAttributes.put(attr, 2);
                        }
                        if (attr.getLocalName().equals(ALBUM_COUNT_LOCAL_NAME)) {
                            stateAttributes.put(attr, 3);
                        }
                        if (attr.getLocalName().equals(SONG_COUNT_LOCAL_NAME)) {
                            stateAttributes.put(attr, 7);
                        }
                    }
                    List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
                    stateInfo.put(entry.getKey(), stateElements);
                }
                    boolean isGreatestHits = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue()
                            .equalsIgnoreCase("Greatest Hits");
                    boolean isCircus = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue()
                            .equalsIgnoreCase("Circus");
                    boolean isChainSmokers = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue()
                            .equalsIgnoreCase("Inspire");
                    for (QName attribute : attributes) {
                        //dummy logic
                        if (isGreatestHits) {
                            stateAttributes.put(attribute, 2);
                        } else if (isCircus) {
                            stateAttributes.put(attribute, 3);
                        } else if (isChainSmokers) {
                            stateAttributes.put(attribute, 2);
                        }
                    }
                    List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
                    stateInfo.put(modelNodeId, stateElements);

                    List<FilterNode> stateSubtrees = entry.getValue().getSecond();
                    if (!stateSubtrees.isEmpty()) {
                        List<Element> stateSubtreesElement = retrieveStateSubtree(isGreatestHits, stateSubtrees);
                        stateElements.addAll(stateSubtreesElement);
                        // copy the subtrees
                        stateInfo.put(modelNodeId, stateElements);
                    }
            }
            return stateInfo;
        }

        private boolean isStateUnderLibrary(ModelNodeId modelNodeId) {
            return modelNodeId.matchesTemplate(
                    new ModelNodeId("/container=jukebox/container=library", JB_NS));
        }

        private List<Element> retrieveStateSubtree(boolean isGreatestHits, List<FilterNode> stateSubtrees) {
            List<Element> result = new ArrayList<>();
            try {
                for (FilterNode filterNode : stateSubtrees) {
                    List<FilterMatchNode> filterMatchNodes = filterNode.getMatchNodes();
                    for (FilterMatchNode matchNodes : filterMatchNodes) {
                        if (matchNodes.getNodeName().equalsIgnoreCase("album-state-leaf1") && matchNodes.getFilter().equalsIgnoreCase("value1") && isGreatestHits) {
                            result.add(DocumentUtils.stringToDocument("<jbox:album-state-container  " +
                                    "xmlns:jbox=\"http://example.com/ns/example-jukebox\">\n" +
                                    "\t<jbox:album-state-leaf1>value1</jbox:album-state-leaf1>\n" +
                                    "\t<jbox:album-state-leaf2>value2</jbox:album-state-leaf2>\n" +
                                    "\t<jbox:album-state-leaf-list>value1</jbox:album-state-leaf-list>\n" +
                                    "\t<jbox:album-state-leaf-list>value2</jbox:album-state-leaf-list>\n" +
                                    "</jbox:album-state-container>").getDocumentElement());
                        } else if (matchNodes.getNodeName().equalsIgnoreCase("album-state-leaf1") && matchNodes.getFilter().equalsIgnoreCase("value3") && !isGreatestHits) {
                            result.add(DocumentUtils.stringToDocument("<jbox:album-state-container  " +
                                    "xmlns:jbox=\"http://example.com/ns/example-jukebox\">\n" +
                                    "\t<jbox:album-state-leaf1>value3</jbox:album-state-leaf1>\n" +
                                    "\t<jbox:album-state-leaf2>value4</jbox:album-state-leaf2>\n" +
                                    "\t<jbox:album-state-leaf-list>value1</jbox:album-state-leaf-list>\n" +
                                    "\t<jbox:album-state-leaf-list>value2</jbox:album-state-leaf-list>\n" +
                                    "</jbox:album-state-container>").getDocumentElement());
                        } else if (matchNodes.getNodeName().equalsIgnoreCase("album-state-leaf-list") &&
                                (matchNodes.getFilter().equalsIgnoreCase("value1") || matchNodes.getFilter().equalsIgnoreCase("value2")) && isGreatestHits) {
                            result.add(DocumentUtils.stringToDocument("<jbox:album-state-container  " +
                                    "xmlns:jbox=\"http://example.com/ns/example-jukebox\">\n" +
                                    "\t<jbox:album-state-leaf1>value1</jbox:album-state-leaf1>\n" +
                                    "\t<jbox:album-state-leaf2>value2</jbox:album-state-leaf2>\n" +
                                    "\t<jbox:album-state-leaf-list>value1</jbox:album-state-leaf-list>\n" +
                                    "\t<jbox:album-state-leaf-list>value2</jbox:album-state-leaf-list>\n" +
                                    "</jbox:album-state-container>").getDocumentElement());
                        }
                    }
                    List<FilterNode> selectNodes = filterNode.getSelectNodes();
                    for (FilterNode selectNode : selectNodes) {
                        if (selectNode.getNodeName().equalsIgnoreCase("album-state-leaf1") && isGreatestHits) {
                            result.add(DocumentUtils.stringToDocument("<jbox:album-state-container  " +
                                    "xmlns:jbox=\"http://example.com/ns/example-jukebox\">\n" +
                                    "\t<jbox:album-state-leaf1>value1</jbox:album-state-leaf1>\n" +
                                    "</jbox:album-state-container>").getDocumentElement());
                        }
                    }
                }
            } catch (NetconfMessageBuilderException e) {

            }
            return result;
        }
    }
}
