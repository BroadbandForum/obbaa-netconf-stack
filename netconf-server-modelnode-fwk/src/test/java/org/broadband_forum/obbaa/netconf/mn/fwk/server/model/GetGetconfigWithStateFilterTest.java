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

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Album;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Library;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.LibrarySystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createJukeBoxModel;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

/**
 * Created by keshava on 2/9/16.
 */
public class GetGetconfigWithStateFilterTest {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = Logger.getLogger(GetGetconfigWithStateFilterTest.class);

    private NetConfServerImpl m_server;

    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangsWithAlbumStateAttributes(), new
                NoLockService());
        m_server = new NetConfServerImpl(m_schemaRegistry);
        LibrarySystem librarySystem = new LibrarySystem();
        String componentId = Library.QNAME.toString();
        m_subSystemRegistry.register(componentId, new SchemaPathBuilder().withParent(Library.LIBRARY_SCHEMA_PATH)
                .appendLocalName("song-count").build(), librarySystem);
        AlbumSubsystem albumSubsystem = new AlbumSubsystem();
        updateSubsystemRegistry(componentId, albumSubsystem);
        ModelNode jukeboxNode = createJukeBoxModel(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        librarySystem.setRootModelNode(jukeboxNode);
        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry,
                m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry)
                .addModelServiceRoot(componentId, jukeboxNode);
        m_server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator,
                m_subSystemRegistry));
        RequestScope.resetScope();
    }

    private void updateSubsystemRegistry(String componentId, AlbumSubsystem albumSubsystem) {
        m_subSystemRegistry.register(componentId, Album.ALBUM_SCHEMA_PATH, albumSubsystem);
        m_subSystemRegistry.register(componentId, new SchemaPathBuilder().withParent(Album.ALBUM_SCHEMA_PATH)
                .appendLocalName("album-song-count").build(), albumSubsystem);
        m_subSystemRegistry.register(componentId, new SchemaPathBuilder().withParent(Album.ALBUM_SCHEMA_PATH)
                .appendLocalName("album-state-container").build(), albumSubsystem);
    }

    @Test
    public void testGetWithStateFilter() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/album-state-filter1.xml", "/get-with-album-state-filter1-response.xml", "1");
        TestUtil.verifyGet(m_server, "/album-state-filter2.xml", "/get-with-album-state-filter2-response.xml", "1");
        TestUtil.verifyGet(m_server, "/album-state-filter4.xml", "/get-with-album-state-filter4-response.xml", "1");
    }

    @Test
    public void testGetWithStateSubtreeFilter() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/album-state-filter6.xml", "/get-with-album-state-filter6-response.xml", "1");
    }

    @Test
    public void testGetConfigWithStateFilter() throws SAXException, IOException {
        TestUtil.verifyGetConfig(m_server, "/album-state-filter1.xml", "/empty-response.xml", "1");
        TestUtil.verifyGetConfig(m_server, "/album-state-filter3.xml", "/empty-response.xml", "1");
    }

    private class AlbumSubsystem extends AbstractSubSystem {

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
                List<FilterNode>>> mapAttributes) throws GetAttributeException {
            Document doc = DocumentUtils.createDocument();
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                Map<QName, Object> stateAttributes = new LinkedHashMap<QName, Object>();
                ModelNodeId modelNodeId = entry.getKey();
                List<QName> attributes = entry.getValue().getFirst();
                boolean isGreatestHits = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue()
                        .equalsIgnoreCase("Greatest Hits");
                for (QName attribute : attributes) {
                    //dummy logic
                    if (isGreatestHits) {
                        stateAttributes.put(attribute, 2);
                    } else {
                        stateAttributes.put(attribute, 3);
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

        private List<Element> retrieveStateSubtree(boolean isGreatestHits, List<FilterNode> stateSubtrees) {
            List<Element> result = new ArrayList<>();
            try {
                for (FilterNode filterNode : stateSubtrees) {
                    List<FilterMatchNode> filterMatchNodes = filterNode.getMatchNodes();
                    for (FilterMatchNode matchNodes : filterMatchNodes) {
                        if (matchNodes.getNodeName().equalsIgnoreCase("album-state-leaf1") && matchNodes.getFilter()
                                .equalsIgnoreCase("value1") && isGreatestHits) {
                            result.add(DocumentUtils.stringToDocument("<jbox:album-state-container  " +
                                    "xmlns:jbox=\"http://example.com/ns/example-jukebox\">\n" +
                                    "\t<jbox:album-state-leaf1>value1</jbox:album-state-leaf1>\n" +
                                    "\t<jbox:album-state-leaf2>value2</jbox:album-state-leaf2>\n" +
                                    "</jbox:album-state-container>").getDocumentElement());
                        } else if (matchNodes.getNodeName().equalsIgnoreCase("album-state-leaf1") && matchNodes
                                .getFilter().equalsIgnoreCase("value3") && !isGreatestHits) {
                            result.add(DocumentUtils.stringToDocument("<jbox:album-state-container  " +
                                    "xmlns:jbox=\"http://example.com/ns/example-jukebox\">\n" +
                                    "\t<jbox:album-state-leaf1>value3</jbox:album-state-leaf1>\n" +
                                    "\t<jbox:album-state-leaf2>value4</jbox:album-state-leaf2>\n" +
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
