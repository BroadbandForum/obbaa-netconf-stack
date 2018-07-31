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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.AggregatedDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.AnnotationBasedModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TestTxUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox1.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.broadband_forum.obbaa.netconf.persistence.EMFactory;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityManagerFactory;
import org.broadband_forum.obbaa.netconf.persistence.jpa.ThreadLocalPersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;


public class GetWithStateFilterTest {

    private static final String YANG_FILE = "/getwithstatefiltertest/example-jukebox-with-state-attributes.yang";
    private static final String DEFAULT_XML = "/getwithstatefiltertest/example-jukebox-default.xml";
    private static final String COMPONENT_ID = "Jukebox";
    private static final String MODULE_NAME = "example-jukebox-with-state-attributes";

    private static final QName ALBUM_COUNT_QNAME = QName.create(JukeboxConstants.JB_NS, JB_REVISION, "album-count");
    private static final QName AWARDS_QNAME = QName.create(JukeboxConstants.JB_NS, JB_REVISION, "awards");

    private static final String GET_UNFILTERED_RESPONSE_XML = "/getwithstatefiltertest/get-unfiltered-response.xml";
    private static final String GET_WITH_FILTER_ALBUM_COUNT_REQUEST =
            "/getwithstatefiltertest/get-with-filter-album-count-request.xml";
    private static final String GET_WITH_FILTER_ALBUM_COUNT_RESPONSE_XML =
            "/getwithstatefiltertest/get-with-filter-album-count-response.xml";
    private static final String GET_WITH_FILTER_AWARD_REQUEST =
            "/getwithstatefiltertest/get-with-filter-award-request.xml";
    private static final String GET_WITH_FILTER_AWARD_RESPONSE_XML =
            "/getwithstatefiltertest/get-with-filter-award-response.xml";

    private SchemaRegistry m_schemaRegistry;
    private EntityRegistry m_entityRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private AggregatedDSM m_aggregatedDSM;
    private PersistenceManagerUtil m_persistenceManagerUtil;
    private EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;
    private ModelNodeDataStoreManager m_annotationBasedDSM;
    private NbiNotificationHelper m_nbiNotificationHelper;

    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private NetConfServerImpl m_server;
    private DataStore m_dataStore;
    private ArtistSubSystem m_subsystem;

    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
        List<YangTextSchemaSource> yangFiles = Arrays.asList(TestUtil.getByteSource(YANG_FILE));
        EMFactory managerFactory = new JPAEntityManagerFactory("hsql", Collections.EMPTY_MAP);

        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, new NoLockService());
        m_entityRegistry = new EntityRegistryImpl();
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subsystem = new ArtistSubSystem();
        m_subSystemRegistry.register(COMPONENT_ID, ARTIST_SCHEMA_PATH, m_subsystem);
        m_subSystemRegistry.register(COMPONENT_ID, new SchemaPathBuilder().withParent(ARTIST_SCHEMA_PATH)
                .appendLocalName("album-count").build(), m_subsystem);
        m_subSystemRegistry.register(COMPONENT_ID, new SchemaPathBuilder().withParent(ARTIST_SCHEMA_PATH)
                .appendLocalName("awards").build(), m_subsystem);
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();
        m_aggregatedDSM = new AggregatedDSM(m_modelNodeDSMRegistry);
        m_persistenceManagerUtil = new ThreadLocalPersistenceManagerUtil(managerFactory);
        m_annotationBasedDSM = TestTxUtils.getTxDecoratedDSM(m_persistenceManagerUtil, new
                AnnotationBasedModelNodeDataStoreManager(m_persistenceManagerUtil,
                m_entityRegistry, m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_modelNodeDSMRegistry));
        m_entityModelNodeHelperDeployer = new EntityModelNodeHelperDeployer(m_modelNodeHelperRegistry,
                m_schemaRegistry, m_aggregatedDSM,
                m_entityRegistry, m_subSystemRegistry);
        m_server = new NetConfServerImpl(m_schemaRegistry);

        updateEntityRegistry();
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser(COMPONENT_ID,
                Collections.singletonList((SchemaRegistryVisitor) m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule(MODULE_NAME, JB_REVISION));
        traverser.traverse();

        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_aggregatedDSM, m_subSystemRegistry);
        addRootNodeHelper();
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        when(m_nbiNotificationHelper.getNetconfConfigChangeNotifications(Matchers.any(), Matchers.any(), Matchers.any
                ())).thenReturn(Collections.emptyList());
        m_dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        updateDSMRegistry();
        m_server.setRunningDataStore(m_dataStore);

        YangUtils.loadXmlDataIntoServer(m_server, getClass().getResource(DEFAULT_XML).getPath());
    }

    @Test
    public void testGetWithStateFilter() throws NetconfMessageBuilderException, IOException, SAXException {
        TestUtil.verifyGet(m_server, (NetconfFilter) null, GET_UNFILTERED_RESPONSE_XML, "1");

        // Get with filter on a state attribute
        TestUtil.verifyGet(m_server, GET_WITH_FILTER_ALBUM_COUNT_REQUEST, GET_WITH_FILTER_ALBUM_COUNT_RESPONSE_XML,
                "1");

        // Get with filter on a state leaf-list attribute
        TestUtil.verifyGet(m_server, GET_WITH_FILTER_AWARD_REQUEST, GET_WITH_FILTER_AWARD_RESPONSE_XML, "1");
    }

    private void updateDSMRegistry() {
        m_modelNodeDSMRegistry.register(COMPONENT_ID, JUKEBOX_SCHEMA_PATH, m_annotationBasedDSM);
        m_modelNodeDSMRegistry.register(COMPONENT_ID, LIBRARY_SCHEMA_PATH, m_annotationBasedDSM);
        m_modelNodeDSMRegistry.register(COMPONENT_ID, ARTIST_SCHEMA_PATH, m_annotationBasedDSM);
    }

    private void addRootNodeHelper() {
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(JukeboxConstants
                .JUKEBOX_SCHEMA_PATH);
        ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_schemaRegistry, m_aggregatedDSM);
        m_rootModelNodeAggregator.addModelServiceRootHelper(JukeboxConstants.JUKEBOX_SCHEMA_PATH, containerHelper);
    }

    private void updateEntityRegistry() throws AnnotationAnalysisException {
        List<Class> classes = new ArrayList<>();
        classes.add(Jukebox.class);
        EntityRegistryBuilder.updateEntityRegistry(COMPONENT_ID, classes, m_entityRegistry, m_schemaRegistry, null,
                m_modelNodeDSMRegistry);
        m_modelNodeDSMRegistry.register(COMPONENT_ID, JukeboxConstants.JUKEBOX_SCHEMA_PATH, m_annotationBasedDSM);
    }

    private class ArtistSubSystem extends AbstractSubSystem {
        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
                List<FilterNode>>> mapAttributes) throws GetAttributeException {
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();

            Document doc = DocumentUtils.createDocument();
            for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                ModelNodeId modelNodeId = entry.getKey();
                Map<QName, Object> stateAttributes = new LinkedHashMap<>();
                boolean isArtistNameLenny = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue()
                        .equalsIgnoreCase("Lenny");
                List<QName> qNames = entry.getValue().getFirst();
                if (isArtistNameLenny) {
                    if (qNames.contains(ALBUM_COUNT_QNAME)) {
                        stateAttributes.put(ALBUM_COUNT_QNAME, "2");
                    }
                    if (qNames.contains(AWARDS_QNAME)) {
                        List<String> awardNames = new ArrayList<>();
                        awardNames.add("Lenny's 1st award");
                        awardNames.add("Lenny's 2nd award");
                        awardNames.add("Lenny's 3rd award");
                        stateAttributes.put(AWARDS_QNAME, awardNames);
                    }
                } else {
                    if (qNames.contains(ALBUM_COUNT_QNAME)) {
                        stateAttributes.put(ALBUM_COUNT_QNAME, "3");
                    }
                    if (qNames.contains(AWARDS_QNAME)) {
                        stateAttributes.put(AWARDS_QNAME, "Penny's 1st award");
                    }
                }
                List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
                stateInfo.put(modelNodeId, stateElements);
            }
            return stateInfo;
        }
    }
}
