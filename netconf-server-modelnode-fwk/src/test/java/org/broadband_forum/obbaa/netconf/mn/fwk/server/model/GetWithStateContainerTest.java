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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.DsmJukeboxSubsystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class GetWithStateContainerTest {

    private static final String EXAMPLE_JUKEBOX_WITH_STATE_CONTAINER_YANG = "/getwithstatecontainertest/example-jukebox-with-state-container.yang";
    private static final String EXAMPLE_JUKEBOX_XML = "/getwithstatecontainertest/example-jukebox.xml";
    private static final String FULL_GET_RESPONSE_WITH_STATE_CONTAINER_XML = "/getwithstatecontainertest/full_get_response_with_state_container.xml";
    private static final String CIRCUS_ADMIN_XML = "/getwithstatecontainertest/circus-admin.xml";
    private static final String GREATEST_HITS_ADMIN_ONLY_XML = "/getwithstatecontainertest/greatest-hits-admin-only.xml";
    private static final String MODULE_NAME = "example-jukebox-with-state-container";
    private static final String MESSAGE_ID = "1";

    private static final ModelNodeId LIBRARY_MODELNODE_ID = new ModelNodeId("/container=jukebox/container=library",
            JukeboxConstants.JB_NS);
    private static final ModelNodeId CIRCUS_MODELNODEID = new ModelNodeId("/container=jukebox/container=library" +
            "/container=artist/name=Lenny/container=album/name=Circus", JukeboxConstants.JB_NS);
    private static final ModelNodeId GREATESTHITS_MODELNODEID = new ModelNodeId("/container=jukebox/container=library" +
            "/container=artist/name=Lenny/container=album/name=Greatest hits", JukeboxConstants.JB_NS);
    public static final String ADMINS = "admins";
    private static final SchemaPath ADMINS_SCHEMA_PATH = new SchemaPathBuilder().withParent(JukeboxConstants.ALBUM_SCHEMA_PATH).appendLocalName(ADMINS).build();
    public static final String ADMIN = "admin";
    private static final SchemaPath ADMIN_SCHEMA_PATH = new SchemaPathBuilder().withParent(ADMINS_SCHEMA_PATH).appendLocalName(ADMIN).build();
    private static final SchemaPath LABEL_SCHEMA_PATH = new SchemaPathBuilder().withParent(ADMIN_SCHEMA_PATH).appendLocalName("label").build();
    private static final SchemaPath CATALOGUE_NUMBER_SCHEMA_PATH = new SchemaPathBuilder().withParent(ADMIN_SCHEMA_PATH).appendLocalName("catalogue-number").build();

    private static final SchemaPath SPONSORS_SCHEMA_PATH = new SchemaPathBuilder().withParent(JukeboxConstants.ALBUM_SCHEMA_PATH).appendLocalName("sponsors").build();
    private static final SchemaPath SPONSOR_SCHEMA_PATH = new SchemaPathBuilder().withParent(SPONSORS_SCHEMA_PATH).appendLocalName("sponsor").build();
    private static final SchemaPath NAME_SCHEMA_PATH = new SchemaPathBuilder().withParent(SPONSOR_SCHEMA_PATH).appendLocalName("name").build();
    private static final SchemaPath TYPE_SCHEMA_PATH = new SchemaPathBuilder().withParent(SPONSOR_SCHEMA_PATH).appendLocalName("type").build();
    public static final String GREATEST_HITS_SPONSOR_ONLY_XML = "/getwithstatecontainertest/greatest-hits-sponsor-only.xml";
    public static final String PRUNED_SUBTREE = "pruned-subtree";

    private SchemaRegistry m_schemaRegistry;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private NetConfServerImpl m_server;
    private ModelNode m_jukeBoxModelNode;
    private NbiNotificationHelper m_nbiNotificationHelper;
    private AdminSubSystem m_adminSubSystem;
    private LibrarySubSystem m_librarySystem;
    private ChoiceCaseSubSystem m_choiceCaseSubSystem;
    private DsmJukeboxSubsystem m_jukeboxSubsystem;

    @Before
    public void setUp() throws SchemaBuildException, ModelNodeInitException {

        String yangFilePath = GetWithStateContainerTest.class.getResource(EXAMPLE_JUKEBOX_WITH_STATE_CONTAINER_YANG).getPath(); //The only difference is that admin is a dummy state container in library
        String xmlFilePath = GetWithStateContainerTest.class.getResource(EXAMPLE_JUKEBOX_XML).getPath();
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext(MODULE_NAME, Arrays.asList(TestUtil.getByteSource(EXAMPLE_JUKEBOX_WITH_STATE_CONTAINER_YANG)), Collections.emptySet(), Collections.emptyMap());

        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
        m_jukeboxSubsystem = mock(DsmJukeboxSubsystem.class);
        m_jukeBoxModelNode = YangUtils.createInMemoryModelNode(yangFilePath, m_jukeboxSubsystem, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, new InMemoryDSM(m_schemaRegistry));
        m_adminSubSystem = spy(new AdminSubSystem());
        m_librarySystem = spy(new LibrarySubSystem());
        m_choiceCaseSubSystem = spy(new ChoiceCaseSubSystem());
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
        verifyGet(m_server, (NetconfFilter)null, FULL_GET_RESPONSE_WITH_STATE_CONTAINER_XML, MESSAGE_ID);
        assertEquals(1, m_librarySystem.getNumOfCalls());
        assertEquals(1, m_adminSubSystem.getNumOfCalls());
        assertEquals(1, m_choiceCaseSubSystem.getNumOfCalls());
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
    }

    @Test
    public void testGetWithStateFilter_CallsOnlyChoiceCaseSubsystem() throws Exception {
        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-state-choice-selectnode.xml",
                "/getwithstatecontainertest/get-with-filter-state-choice-selectnode-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        assertEquals(0, m_librarySystem.getNumOfCalls());
        assertEquals(0, m_adminSubSystem.getNumOfCalls());
        assertEquals(1, m_choiceCaseSubSystem.getNumOfCalls());
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
    }

    @Test
    public void testGetWithStateFilterHavingMatchAndSelectNodesInSiblingContainmentNodes() throws Exception {
        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admins-matchnode-sponsors-selectnode.xml",
                "/getwithstatecontainertest/get-with-filter-admin-matchnode-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        assertEquals(0, m_librarySystem.getNumOfCalls());
        assertEquals(2, m_adminSubSystem.getNumOfCalls());
        assertEquals(0, m_choiceCaseSubSystem.getNumOfCalls());
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
    }

    @Test
    public void testGetWithStateFilterHavingMultipleInstancesOfSameSchemaNode() throws Exception {
        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admins-matchnode-with-two-instances.xml",
                "/getwithstatecontainertest/get-with-filter-admin-matchnode-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        assertEquals(0, m_librarySystem.getNumOfCalls());
        assertEquals(1, m_adminSubSystem.getNumOfCalls());
        assertEquals(0, m_choiceCaseSubSystem.getNumOfCalls());
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
    }

    @Test
    public void testSubsystemCalledOnceForStateSelect() throws Exception{

        /**
         *  State Filter Node : <album>
         *                         <name>Circus</name>
         *                         <admins/> --> State container select node
         *                      </album>
         */
        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admins-selectnode.xml",
                "/getwithstatecontainertest/get-with-filter-admins-response.xml", MESSAGE_ID);
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = new HashMap<>();
        FilterNode adminsFilterNode = new FilterNode(ADMINS, JB_NS);
        attributes.put(CIRCUS_MODELNODEID, new Pair<>(new ArrayList<>(), Collections.singletonList(adminsFilterNode)));

        verify(m_adminSubSystem, times(1)).retrieveStateAttributes(Mockito.eq(attributes), Mockito.any(NetconfQueryParams.class));
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_librarySystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_choiceCaseSubSystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        /**
         *  State Filter Node : <album>
         *                          <name>Circus</name>
         *                          <admins> --> State container select node
         *                              <admin/>
         *                          </admins>
         *                      </album>
         */
        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-selectnode.xml",
                "/getwithstatecontainertest/get-with-filter-admins-response.xml", MESSAGE_ID);

        FilterNode adminFilterNode = new FilterNode(ADMIN, JB_NS);
        adminsFilterNode.setSelectNodes(Collections.singletonList(adminFilterNode));
        verify(m_adminSubSystem, times(1)).retrieveStateAttributes(Mockito.eq(attributes), Mockito.any(NetconfQueryParams.class));
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_librarySystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_choiceCaseSubSystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
    }

    @Test
    public void testSubsystemCalledOnceForStateMatch() throws Exception{

        /**
         *  State Match Node : <admins>
         *                          <admin>
         *                              <label>admin1</label> -> State match Node
         *                          </admin>
         *                      </admins>
         */

        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> admin1Attributes = new HashMap<>();
        FilterNode adminsFilterNode = getAdminsFilterNode("admin1");
        admin1Attributes.put(CIRCUS_MODELNODEID, new Pair<>(new ArrayList<>(), Collections.singletonList(adminsFilterNode)));

        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-matchnode.xml",
                "/getwithstatecontainertest/get-with-filter-admin-matchnode-response.xml", MESSAGE_ID);
        verify(m_adminSubSystem, times(1)).retrieveStateAttributes(Mockito.eq(admin1Attributes), Mockito.any(NetconfQueryParams.class));
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_librarySystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_choiceCaseSubSystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        /**
         *  Two state match nodes : <album>
         *                 <name>Circus</name>
         *                 <admins>
         *                    <admin>
         *                        <label>admin1</label> -> State match Node
         *                    </admin>
         *                 </admins>
         *             </album>
         *             <album>
         *                 <name>Greatest hits</name>
         *                 <admins>
         *                     <admin>
         *                         <label>admin3</label> -> State match Node
         *                     </admin>
         *                 </admins>
         *             </album>
         */

        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> admin3Attributes = new HashMap<>();
        FilterNode admin3MatchNode = getAdminsFilterNode("admin3");
        List<FilterNode> filterNodes = new ArrayList<>();
        filterNodes.add(admin3MatchNode);
        admin3Attributes.put(GREATESTHITS_MODELNODEID, new Pair<>(new ArrayList<>(), filterNodes));

        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-2-matchnodes.xml",
                "/getwithstatecontainertest/get-with-filter-admin-2-matchnodes-response.xml", MESSAGE_ID);
        verify(m_adminSubSystem, times(2)).retrieveStateAttributes(Mockito.eq(admin1Attributes), Mockito.any(NetconfQueryParams.class));
        verify(m_adminSubSystem, times(1)).retrieveStateAttributes(Mockito.eq(admin3Attributes), Mockito.any(NetconfQueryParams.class));
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_librarySystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_choiceCaseSubSystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));

        /**
         * Two State Match nodes : <admins>
         *                    <admin>
         *                        <label>admin3</label> -> State match Node
         *                    </admin>
         *                 </admins>
         *                 <sponsors>
         *                     <sponsor>
         *                         <name>JD</name> -> State match Node
         *                     </sponsor>
         *                 </sponsors>
         */

        FilterNode sponsorsMatchNode = getSponsorsFilterNode("JD");
        filterNodes.add(sponsorsMatchNode);

        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-matchnode-sponsor-matchnode.xml",
                "/getwithstatecontainertest/get-with-filter-admin-matchnode-sponsor-matchnode-response.xml", MESSAGE_ID);
        verify(m_adminSubSystem, times(1)).retrieveStateAttributes(Mockito.eq(admin3Attributes), Mockito.any(NetconfQueryParams.class));
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_librarySystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_choiceCaseSubSystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
    }

    @Test
    public void testSubsystemCalledOnceForStateMatchNonMatch() throws Exception {

        /**
         * State Match Node : <admins>
         *                    <admin>
         *                        <label>admin3</label>  --> Match Node
         *                    </admin>
         *                 </admins>
         *                 <sponsors>
         *                     <sponsor>
         *                         <name>BDog</name>  -> Non match node
         *                     </sponsor>
         *                 </sponsors>
         */

        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> sponsorsAdmin3Attributes = new HashMap<>();
        List<FilterNode> filterNodes = new ArrayList<>();
        FilterNode sponsorsMatchNode = getSponsorsFilterNode("BDog");
        FilterNode admin3MatchNode = getAdminsFilterNode("admin3");
        filterNodes.add(admin3MatchNode);
        filterNodes.add(sponsorsMatchNode);
        sponsorsAdmin3Attributes.put(GREATESTHITS_MODELNODEID, new Pair<>(new ArrayList<>(), filterNodes));

        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> sponsorsAttributes = new HashMap<>();
        sponsorsAttributes.put(GREATESTHITS_MODELNODEID, new Pair<>(new ArrayList<>(), Collections.singletonList(sponsorsMatchNode)));

        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-matchnode-sponsor-nonmatchnode.xml",
                "/getwithstatecontainertest/get-with-filter-admin-matchnode-sponsor-nonmatchnode-response.xml", MESSAGE_ID);
        verify(m_adminSubSystem, times(1)).retrieveStateAttributes(Mockito.eq(sponsorsAdmin3Attributes), Mockito.any(NetconfQueryParams.class));
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_librarySystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_choiceCaseSubSystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));

        /**
         *  State Match Node : <admins>
         *                    <admin>
         *                        <label>admin8</label>  -> Non match node
         *                    </admin>
         *                 </admins>
         *                 <sponsors>
         *                     <sponsor>
         *                         <name>BDog</name>  -> Non match node
         *                     </sponsor>
         *                 </sponsors>
         */

        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> sponsorsAdmin8Attributes = new HashMap<>();
        filterNodes = new ArrayList<>();
        FilterNode admin8MatchNode = getAdminsFilterNode("admin8");
        filterNodes.add(admin8MatchNode);
        filterNodes.add(sponsorsMatchNode);
        sponsorsAdmin8Attributes.put(GREATESTHITS_MODELNODEID, new Pair<>(new ArrayList<>(), filterNodes));

        verifyGet(m_server, "/getwithstatecontainertest/get-with-filter-admin-nonmatchnode-sponsor-nonmatchnode.xml",
                "/getwithstatecontainertest/get-with-filter-admin-nonmatchnode-sponsor-nonmatchnode-response.xml", MESSAGE_ID);
        verify(m_adminSubSystem, times(1)).retrieveStateAttributes(Mockito.eq(sponsorsAdmin8Attributes), Mockito.any(NetconfQueryParams.class));
        verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_librarySystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        verify(m_choiceCaseSubSystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
    }

    private FilterNode getAdminsFilterNode(String matchValue){
        FilterNode adminsFilterNode = new FilterNode(ADMINS, JB_NS);
        FilterNode adminFilterNode = new FilterNode(ADMIN, JB_NS);
        adminsFilterNode.addContainmentNode(adminFilterNode);
        adminFilterNode.addMatchNode("label", JB_NS, matchValue);
        return adminsFilterNode;
    }

    private FilterNode getSponsorsFilterNode(String matchValue){
        FilterNode sponsorsFilterNode = new FilterNode("sponsors", JB_NS);
        FilterNode sponsorFilterNode = new FilterNode("sponsor", JB_NS);
        sponsorsFilterNode.addContainmentNode(sponsorFilterNode);
        sponsorFilterNode.addMatchNode("name", JB_NS, matchValue);
        return sponsorsFilterNode;
    }

    private class AdminSubSystem extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        private int m_numOfCalls;

        public int getNumOfCalls() {
            return m_numOfCalls;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
            m_numOfCalls++;
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            for(Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()){
                ModelNodeId modelNodeId = entry.getKey();
                List<QName> filterLeaves = entry.getValue().getFirst();
                List<FilterNode> filterNodes = entry.getValue().getSecond();
                if (!(filterLeaves.isEmpty() && filterNodes.isEmpty())) {
                    String albumName = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue();
                    if(albumName.equalsIgnoreCase("Circus")){
                        List<FilterNode> filterNode = entry.getValue().getSecond();
                        if(!filterNode.isEmpty() && !filterNode.get(0).getChildNodes().isEmpty() &&
                                filterNode.get(0).getChildNodes().get(0).getMatchNodes().get(0).getFilter().equalsIgnoreCase("admin1")){
                            stateInfo.put(CIRCUS_MODELNODEID,Collections.singletonList(TestUtil.loadAsXml("/getwithstatecontainertest/circus-admin1-only.xml")));
                        }else{
                            Element adminElement = TestUtil.loadAsXml(CIRCUS_ADMIN_XML);
                            stateInfo.put(CIRCUS_MODELNODEID, Collections.singletonList(adminElement));
                        }
                    }else{
                        if(!filterNodes.isEmpty() && !filterNodes.get(0).getChildNodes().isEmpty()){
                            List<Element> stateElements = new ArrayList<>();
                            for(FilterNode filterNode : filterNodes){
                                if(filterNode.getChildNodes().get(0).getMatchNodes().get(0).getFilter().equalsIgnoreCase("admin3")){
                                    stateElements.add(TestUtil.loadAsXml("/getwithstatecontainertest/greatesthits-admin3-only.xml"));
                                }else if(filterNode.getChildNodes().get(0).getMatchNodes().get(0).getFilter().equalsIgnoreCase("JD")){
                                    stateElements.add(TestUtil.loadAsXml("/getwithstatecontainertest/greatest-hits-JD-only.xml"));
                                }
                            }
                            stateInfo.put(GREATESTHITS_MODELNODEID,stateElements);

                        }else{
                            Element adminElement = TestUtil.loadAsXml(GREATEST_HITS_ADMIN_ONLY_XML);
                            Element sponsorsElement = TestUtil.loadAsXml(GREATEST_HITS_SPONSOR_ONLY_XML);
                            List<Element> stateElements = new ArrayList<>();
                            stateElements.add(adminElement);
                            stateElements.add(sponsorsElement);
                            stateInfo.put(GREATESTHITS_MODELNODEID, stateElements);
                        }
                    }
                }
            }

            return stateInfo;
        }
    }



    private class LibrarySubSystem extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        private int m_numOfCalls;

        public int getNumOfCalls() {
            return m_numOfCalls;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
            m_numOfCalls++;
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
            }catch(ParserConfigurationException e){

            }
            return stateInfo;
        }
    }



    private void registerSubSystems() {
        m_subSystemRegistry.register(MODULE_NAME,LIBRARY_SCHEMA_PATH,m_librarySystem);
        m_subSystemRegistry.register(MODULE_NAME,new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH).appendLocalName("song-count").build(),m_librarySystem);
        m_subSystemRegistry.register(MODULE_NAME,new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH).appendLocalName("album-count").build(),m_librarySystem);
        m_subSystemRegistry.register(MODULE_NAME,new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH).appendLocalName("artist-count").build(),m_librarySystem);

        m_subSystemRegistry.register(MODULE_NAME,SchemaPathBuilder.fromString("("+ JB_NS +"?revision="+ JB_REVISION +")jukebox,library,test-choice,case1,case1-list"),m_choiceCaseSubSystem);
        m_subSystemRegistry.register(MODULE_NAME,SchemaPathBuilder.fromString("("+ JB_NS +"?revision="+ JB_REVISION +")jukebox,library,test-choice,case2,case2-leaf"),m_choiceCaseSubSystem);

        m_subSystemRegistry.register(MODULE_NAME,ADMINS_SCHEMA_PATH, m_adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME,ADMIN_SCHEMA_PATH, m_adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME,LABEL_SCHEMA_PATH, m_adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME,CATALOGUE_NUMBER_SCHEMA_PATH, m_adminSubSystem);

        m_subSystemRegistry.register(MODULE_NAME,SPONSORS_SCHEMA_PATH, m_adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME,SPONSOR_SCHEMA_PATH, m_adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME,NAME_SCHEMA_PATH, m_adminSubSystem);
        m_subSystemRegistry.register(MODULE_NAME,TYPE_SCHEMA_PATH, m_adminSubSystem);
    }

    private class ChoiceCaseSubSystem extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        private int m_numOfCalls;

        public int getNumOfCalls() {
            return m_numOfCalls;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
            m_numOfCalls++;
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            try {
                Document newDocument = getNewDocument();
                Element case2StateLeaf = newDocument.createElementNS(JB_NS, "case2-leaf");
                case2StateLeaf.setTextContent("case2-leaf-value");

                List<Element> elements = new ArrayList<>();
                elements.add(case2StateLeaf);
                stateInfo.put(LIBRARY_MODELNODE_ID, elements);
            }catch(ParserConfigurationException e){
                throw new RuntimeException(e);
            }
            return stateInfo;
        }
    }
}
