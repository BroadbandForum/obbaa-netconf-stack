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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getNewDocument;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.DsmJukeboxSubsystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;

public class GetWithStateContainerTest {

    private static final String EXAMPLE_JUKEBOX_WITH_STATE_CONTAINER_YANG =
            "/getwithstatecontainertest/example-jukebox-with-state-container.yang";
    private static final String EXAMPLE_JUKEBOX_XML = "/getwithstatecontainertest/example-jukebox.xml";
    private static final String FULL_GET_RESPONSE_WITH_STATE_CONTAINER_XML =
            "/getwithstatecontainertest/full_get_response_with_state_container.xml";
    private static final String CIRCUS_ADMIN_XML = "/getwithstatecontainertest/circus-admin.xml";
    private static final String GREATEST_HITS_ADMIN_XML = "/getwithstatecontainertest/greatest-hits-admin.xml";
    private static final String MODULE_NAME = "example-jukebox-with-state-container";
    private static final String MESSAGE_ID = "1";

    private static final ModelNodeId LIBRARY_MODELNODE_ID = new ModelNodeId("/container=jukebox/container=library",
            JukeboxConstants.JB_NS);
    private static final ModelNodeId CIRCUS_MODELNODEID = new ModelNodeId("/container=jukebox/container=library" +
            "/container=artist/name=Lenny/container=album/name=Circus", JukeboxConstants.JB_NS);
    private static final ModelNodeId GREATESTHITS_MODELNODEID = new ModelNodeId("/container=jukebox/container=library" +
            "/container=artist/name=Lenny/container=album/name=Greatest hits", JukeboxConstants.JB_NS);
    private static final SchemaPath ADMINS_SCHEMA_PATH = new SchemaPathBuilder().withParent(JukeboxConstants
            .ALBUM_SCHEMA_PATH).appendLocalName("admins").build();
    private static final SchemaPath ADMIN_SCHEMA_PATH = new SchemaPathBuilder().withParent(ADMINS_SCHEMA_PATH)
            .appendLocalName("admin").build();
    private static final SchemaPath LABEL_SCHEMA_PATH = new SchemaPathBuilder().withParent(ADMIN_SCHEMA_PATH)
            .appendLocalName("label").build();
    private static final SchemaPath CATALOGUE_NUMBER_SCHEMA_PATH = new SchemaPathBuilder().withParent
            (ADMIN_SCHEMA_PATH).appendLocalName("catalogue-number").build();

    private SchemaRegistry m_schemaRegistry;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private NetConfServerImpl m_server;
    private ModelNode m_jukeBoxModelNode;
    private NbiNotificationHelper m_nbiNotificationHelper;

    @Before
    public void setUp() throws SchemaBuildException, ParserConfigurationException, GetAttributeException,
            ModelNodeInitException {

        String yangFilePath = GetWithStateContainerTest.class.getResource(EXAMPLE_JUKEBOX_WITH_STATE_CONTAINER_YANG)
                .getPath(); //The only difference is that admin is a dummy state container in library
        String xmlFilePath = GetWithStateContainerTest.class.getResource(EXAMPLE_JUKEBOX_XML).getPath();
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), new NoLockService());
        m_schemaRegistry.loadSchemaContext(MODULE_NAME, Arrays.asList(TestUtil.getByteSource
                (EXAMPLE_JUKEBOX_WITH_STATE_CONTAINER_YANG)), Collections.emptySet(), Collections.emptyMap());

        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
        m_jukeBoxModelNode = YangUtils.createInMemoryModelNode(yangFilePath, mock(DsmJukeboxSubsystem.class),
                m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, new InMemoryDSM(m_schemaRegistry));
        registerSubSystems();
        m_rootModelNodeAggregator.addModelServiceRoot(MODULE_NAME, m_jukeBoxModelNode);

        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setDataStore(StandardDataStores.RUNNING, dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
    }

    @Test
    public void testGetStateAttributes() throws Exception {
        verifyGet(m_server, (NetconfFilter) null, FULL_GET_RESPONSE_WITH_STATE_CONTAINER_XML, MESSAGE_ID);
    }

    @Test
    public void testGetStateContainerSubtree() throws IOException, SAXException, ParserConfigurationException,
            GetAttributeException {

        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admins-selectnode.xml",
                "/getwithstatecontainertest/get-with-filter-admins-response.xml", MESSAGE_ID);
        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-selectnode.xml",
                "/getwithstatecontainertest/get-with-filter-admins-response.xml", MESSAGE_ID);
        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-matchnode.xml",
                "/getwithstatecontainertest/get-with-filter-admin-matchnode-response.xml", MESSAGE_ID);
        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-2-matchnodes.xml",
                "/getwithstatecontainertest/get-with-filter-admin-2-matchnodes-response.xml", MESSAGE_ID);

    }

    private class AdminSubSystem extends AbstractSubSystem {
        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
                List<FilterNode>>> mapAttributes) throws GetAttributeException {
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                ModelNodeId modelNodeId = entry.getKey();
                String albumName = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue();
                if (albumName.equalsIgnoreCase("Circus")) {
                    List<FilterNode> filterNode = entry.getValue().getSecond();
                    if (!filterNode.isEmpty() && !filterNode.get(0).getChildNodes().isEmpty() &&
                            filterNode.get(0).getChildNodes().get(0).getMatchNodes().get(0).getFilter()
                                    .equalsIgnoreCase("admin1")) {
                        stateInfo.put(CIRCUS_MODELNODEID, Collections.singletonList(TestUtil.loadAsXml
                                ("/getwithstatecontainertest/circus-admin1-only.xml")));
                    } else {
                        Element adminElement = TestUtil.loadAsXml(CIRCUS_ADMIN_XML);
                        stateInfo.put(CIRCUS_MODELNODEID, Collections.singletonList(adminElement));
                    }
                } else {
                    List<FilterNode> filterNode = entry.getValue().getSecond();
                    if (!filterNode.isEmpty() && !filterNode.get(0).getChildNodes().isEmpty() &&
                            filterNode.get(0).getChildNodes().get(0).getMatchNodes().get(0).getFilter()
                                    .equalsIgnoreCase("admin3")) {
                        stateInfo.put(GREATESTHITS_MODELNODEID, Collections.singletonList(TestUtil.loadAsXml
                                ("/getwithstatecontainertest/circus-admin3-only.xml")));
                    } else {
                        Element adminElement = TestUtil.loadAsXml(GREATEST_HITS_ADMIN_XML);
                        stateInfo.put(GREATESTHITS_MODELNODEID, Collections.singletonList(adminElement));
                    }
                }
            }

            return stateInfo;
        }
    }


    private class LibrarySubSystem extends AbstractSubSystem {
        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
                List<FilterNode>>> mapAttributes) throws GetAttributeException {
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            try {
                Document newDocument = getNewDocument();
                Element albumCount = newDocument.createElementNS(JB_NS, "album-count");
                albumCount.setTextContent(MESSAGE_ID);

                Element artistCount = newDocument.createElementNS(JB_NS, "artist-count");
                artistCount.setTextContent("2");

                Element songCount = newDocument.createElementNS(JB_NS, "song-count");
                songCount.setTextContent("5");

                List<Element> elements = new ArrayList<>();
                elements.add(albumCount);
                elements.add(artistCount);
                elements.add(songCount);

                stateInfo.put(LIBRARY_MODELNODE_ID, elements);
            } catch (ParserConfigurationException e) {

            }
            return stateInfo;
        }
    }


    private void registerSubSystems() {
        LibrarySubSystem librarySystem = new LibrarySubSystem();
        m_subSystemRegistry.register(MODULE_NAME, LIBRARY_SCHEMA_PATH, librarySystem);

        AdminSubSystem adminSubSystem = new AdminSubSystem();
        m_subSystemRegistry.register(MODULE_NAME, ADMINS_SCHEMA_PATH, adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME, ADMIN_SCHEMA_PATH, adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME, LABEL_SCHEMA_PATH, adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME, CATALOGUE_NUMBER_SCHEMA_PATH, adminSubSystem);
    }
}
