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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.createDocument;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfigWithPassword;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetWithDepth;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtilsUserTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
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
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class GetAndGetConfigWithFilteringTest extends SystemPropertyUtilsUserTest {
	@SuppressWarnings("unused")
	private final static Logger LOGGER = LogManager.getLogger(GetAndGetConfigWithFilteringTest.class);
	public static final String MESSAGE_ID = "1";
	public static final String SUBTREE = "subtree";
	public static final String PRUNED_SUBTREE = "pruned-subtree";
	public static final String EXAMPLE_JUKEBOX_WITHOUT_ADMIN_YANG = "/yangs/example-jukebox-without-admin.yang";
	public static final String EXAMPLE_JUKEBOX_WITH_ADMIN_NS_1_YANG = "/yangs/example-jukebox-with-admin-ns1.yang";
	public static final String EXAMPLE_JUKEBOX_WITH_ADMIN_NS_2_YANG = "/yangs/example-jukebox-with-admin-ns2.yang";

	private NetConfServerImpl m_server;

	private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
	private String m_componentId = "test";
	private ModelNodeId m_modelNodeId;
	private LocalSubSystem m_subSystem;
	private NbiNotificationHelper m_nbiNotificationHelper;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

	@Before
	public void initServer() throws SchemaBuildException, ModelNodeInitException, GetAttributeException {
		initServerWithDefaultXml("/example-jukebox.xml", "/yangs/example-jukebox.yang" );
	}

	private void initServerWithDefaultXml(String defaultXml, String defaultYang) throws SchemaBuildException, GetAttributeException, ModelNodeInitException {
		m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		m_server = new NetConfServerImpl(m_schemaRegistry);
		RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
		m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
		String yangFilePath = TestUtil.class.getResource(defaultYang).getPath();
		String xmlFilePath = TestUtil.class.getResource(defaultXml).getPath();
		m_schemaRegistry.loadSchemaContext("jukebox", TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
		m_subSystem = Mockito.mock(LocalSubSystem.class);
		mockStateAttributes();
		ModelNodeWithAttributes jukeBoxWithYang = YangUtils.createInMemoryModelNode(yangFilePath, m_subSystem, m_modelNodeHelperRegistry,
				m_subSystemRegistry, m_schemaRegistry, new InMemoryDSM(m_schemaRegistry));
		rootModelNodeAggregator.addModelServiceRoot(m_componentId, jukeBoxWithYang);
		m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
		DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
		dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
		m_server.setRunningDataStore(dataStore);
		YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
	}

	@Test
	public void testGetWithoutFiltering() throws SAXException, IOException {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetRequest request = new GetRequest();
		request.setMessageId(MESSAGE_ID);

		NetConfResponse response = new NetConfResponse();
		response.setMessageId(MESSAGE_ID);

		m_server.onGet(client, request, response);
		

		assertXMLEquals("/getandgetconfigwithfilteringtest/get-unfiltered.xml", response);
	}
	
	@Test
	public void testGetWithEmptyFilter() throws SAXException, IOException {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetRequest request = new GetRequest();
		request.setMessageId(MESSAGE_ID);

		NetconfFilter filter	=	new NetconfFilter();
		filter.setType(SUBTREE);
		
		request.setFilter(filter);
		
		NetConfResponse response = new NetConfResponse();
		response.setMessageId(MESSAGE_ID);
		
		NetConfResponse expectedResponse	= new NetConfResponse();
		expectedResponse.setMessageId(MESSAGE_ID);
		expectedResponse.setDataContent(java.util.Collections.emptyList());
		
		m_server.onGet(client, request, response);
		assertXMLEquals(expectedResponse, response);
		
	}

	@Test
	public void testGetConfigWithoutFiltering() throws SAXException, IOException {
		verifyGetConfig(null, "/getandgetconfigwithfilteringtest/getconfig-unfiltered.xml");
	}

	@Test
	public void testGetConfigFilterNonMatchingNode() throws SAXException, IOException {
		verifyGetConfig("/filter-non-matching.xml", "/empty-response.xml");
	}

	@Test
	public void testGetConfigFilterTwoMatchNodes() throws SAXException, IOException {
		verifyGetConfig("/getandgetconfigwithfilteringtest/filter-two-matching.xml", "/getandgetconfigwithfilteringtest/getconfig-response-two-match.xml");
	}

	@Test
	public void testGetConfigFilterSelectNode() throws SAXException, IOException {
		verifyGetConfig("/filter-select.xml", "/getconfig-response-select-for-jukebox.xml");
	}

	@Test
	public void testGetConfigMultipleNodes() throws SAXException, IOException {
		verifyGetConfig("/filter-multiple-nodes.xml", "/getandgetconfigwithfilteringtest/getconfig-response-multiple-nodes_1.xml");
	}
	
	@Test
	public void testGetConfigMultipleEmptyNodes() throws SAXException, IOException {
		verifyGetConfig("/filter-multiple-empty-nodes.xml", "/empty-response.xml");
	}

	@Test
	public void testGetConfigMultipleNodesAndSelect() throws SAXException, IOException {
		verifyGetConfig("/filter-multiple-nodes-with-select.xml", "/getandgetconfigwithfilteringtest/getconfig-response-multiple-nodes-with-select.xml");
	}

	@Test
	public void testGetWithMatchFilterInTwoSiblingContainers() throws Exception {
		verifyGet(m_server, "/filter-matchnodes-in-two-sibling-containers.xml", "/filter-matchnodes-in-two-sibling-containers-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
		verifyGet(m_server, "/filter-matchnodes-in-two-sibling-containers.xml", "/filter-matchnodes-in-two-sibling-containers-subtree-response.xml", MESSAGE_ID, SUBTREE);
	}

	@Test
	public void testGetWithMatchFilterInTwoSiblingContainersOfSameSchemaNode() throws Exception {
		verifyGet(m_server, "/filter-matchnodes-in-two-sibling-containers-of-same-schemanode.xml", "/filter-matchnodes-in-two-sibling-containers-of-same-schemanode-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
		verifyGet(m_server, "/filter-matchnodes-in-two-sibling-containers-of-same-schemanode.xml", "/filter-matchnodes-in-two-sibling-containers-of-same-schemanode-subtree-response.xml", MESSAGE_ID, SUBTREE);
	}

	@Test
	public void testGetWithMatchNodeInOneSiblingAndSelectNodeInAnotherSiblingContainer() throws Exception {
		initServerWithDefaultXml("/example-jukebox-album-with-just-songs.xml", "/yangs/example-jukebox.yang");
		verifyGet(m_server, "/filter-matchnode-in-one-sibling-and-selectnode-in-another-sibling-container.xml", "/filter-matchnode-in-one-sibling-and-selectnode-in-another-sibling-container-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
		verifyGet(m_server, "/filter-matchnode-in-one-sibling-and-selectnode-in-another-sibling-container.xml", "/filter-matchnode-in-one-sibling-and-selectnode-in-another-sibling-container-subtree-response.xml", MESSAGE_ID, SUBTREE);
	}

	@Test
	public void testGetWithFilterHavingSpecificDepth() throws Exception {
		initServerWithDefaultXml("/example-jukebox-album-with-just-songs.xml", "/yangs/example-jukebox.yang");
		verifyGetWithDepth(m_server, "/filter-matchnode-in-one-sibling-and-selectnode-in-another-sibling-container.xml",
				"/filter-with-depth-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE, 5);
		verifyGetWithDepth(m_server, "/filter-matchnode-in-one-sibling-and-selectnode-in-another-sibling-container.xml",
				"/filter-with-depth-subtree-response.xml", MESSAGE_ID, SUBTREE, 5);
	}

	@Test
	public void testGetWithFilterHavingPseudoDepthGreaterThanActualDepth() throws Exception {
		initServerWithDefaultXml("/example-jukebox-album-with-just-songs.xml", "/yangs/example-jukebox.yang");
		verifyGetWithDepth(m_server, "/filter-with-pseudo-depth-greater-than-actual-depth.xml",
				"/filter-with-depth-pruned-subtree-response.xml", MESSAGE_ID, PRUNED_SUBTREE, 5);
	}

	@Test
	public void testGetWithFilterHavingPseudoDepthLesserThanActualDepth() throws Exception {
		verifyGetWithDepth(m_server, "/filter-with-pseudo-depth-lesser-than-actual-depth.xml",
				"/getandgetconfigwithfilteringtest/getconfig-unfiltered.xml", MESSAGE_ID, PRUNED_SUBTREE, 7);
	}

	@Test
	public void verifyGetConfigEmptyFilter() throws SAXException, IOException {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetConfigRequest request = new GetConfigRequest();
		request.setMessageId(MESSAGE_ID);
		request.setSource("running");
		NetconfFilter filter = new NetconfFilter();
		filter.setType(SUBTREE);
		request.setFilter(filter);

		NetConfResponse response = new NetConfResponse();
		response.setMessageId(MESSAGE_ID);

		NetConfResponse expectedResponse = new NetConfResponse();
		expectedResponse.setMessageId(MESSAGE_ID);
		expectedResponse.setDataContent(java.util.Collections.emptyList());
		m_server.onGetConfig(client, request, response);

		assertXMLEquals(expectedResponse, response);
	}

	@Test
	public void testGetWithMatchFilterInContainer() throws Exception {
		initServerWithDefaultXml("/example-jukebox-empty-album-admin.xml", "/yangs/example-jukebox.yang");
		verifyGet(m_server, "/filter-matchnode-in-container.xml", "/filter-matchnode-in-container-response.xml", MESSAGE_ID);
	}

	@Test
	public void testGetOnlyMatchingSubtreesFromMultpleSubTrees() throws Exception {
		initServerWithDefaultXml("/example-jukebox-empty-album-admin.xml", "/yangs/example-jukebox.yang");
		verifyGet(m_server, "/filter-multiple-subtrees.xml", "/filter-matched-subtrees-from-multiple-subtrees.xml", MESSAGE_ID);
	}

	@Test
	public void testGetWithPasswordExtensions() throws Exception {
		initServerWithDefaultXml("/example-jukebox-for-password-ext.xml", "/yangs/example-jukebox-with-is-password-ext.yang");
		verifyGetConfigWithPassword(m_server, "/filter-multiple-subtrees-with-password.xml", "/filter-matched-subtrees-from-multiple-subtrees-with-password.xml", MESSAGE_ID);
		verifyGetConfigWithPassword(m_server, "/filter-multiple-subtrees-with-select-password.xml", "/filter-matched-subtrees-from-multiple-subtrees-with-select-password.xml", MESSAGE_ID);
		verifyGetConfigWithPassword(m_server, "/filter-multiple-subtrees-with-matched-choice-not-password.xml", "/filter-matched-multiple-subtrees-with-matched-choice-not-password.xml", MESSAGE_ID);
		verifyGetConfigWithPassword(m_server, "/filter-multiple-subtrees-with-select-leaf-list-password.xml", "/filter-matched-multiple-subtrees-with-select-leaf-list-password.xml", MESSAGE_ID);
		verifyGetConfigWithPassword(m_server, "/filter-multiple-subtrees-with-match-leaf-list-password.xml", "/filter-matched-multiple-subtrees-with-match-leaf-list-password.xml", MESSAGE_ID);
		verifyGetConfigWithPassword(m_server, "/filter-multiple-subtrees-with-match-leaf-list-genre-password.xml", "/filter-matched-multiple-subtrees-with-match-leaf-list-genre-password.xml", MESSAGE_ID);
	}

	@Test
	public void testGetConfigListWithMultiKeysOrder() throws Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-select-list-with-multikeys.xml", "/getconfig-response-list-multikeys-ordered.xml");
	}

	@Test
	public void testGetConfigWithPrunedSubtreeWithNonExistingSong() throws Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-non-existing-song.xml", "/empty-response.xml", PRUNED_SUBTREE);
	}

	@Test
	public void testGetConfigWithPrunedSubtreeWithExistingSong() throws Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-existing-song.xml", "/fly-away-song-only.xml", PRUNED_SUBTREE);
	}

	@Test
	public void testGetConfigWithSubtreeWithExistingSong() throws Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-non-existing-song.xml", "/album-without-songs.xml", SUBTREE);
	}

	@Test
	public void testGetConfigWithoutSpecifyingListKeyAndHavingSelectNodeUnderChild() throws Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-album-studio-with-name-as-select-node.xml", "/empty-artist.xml", SUBTREE);
	}

	@Test
	public void testGetConfigWithoutSpecifyingListKeyAndHavingMatchNodeUnderChild() throws Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-album-studio-with-name-as-match-node.xml", "/empty-artist.xml", SUBTREE);
	}

	@Test
	public void testGetConfigWithoutSpecifyingListKeyAndHavingSelectNodeUnderChildReturningData() throws Exception {
		initServerWithDefaultXml("/example-jukebox-having-album-with-studio.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-album-studio-with-name-as-select-node.xml", "/get-response-having-album-with-studio-name.xml", SUBTREE);
	}

	@Test
	public void testGetConfigWithoutSpecifyingListKeyAndHavingMatchNodeUnderChildReturningData() throws Exception {
		initServerWithDefaultXml("/example-jukebox-having-album-with-studio.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-album-studio-with-name-as-match-node.xml", "/get-response-having-album-with-studio.xml", SUBTREE);
	}

	@Test
	public void testGetConfigWithOnlySelectNodesForWhichThereIsNoData() throws Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-album-studio-with-only-select-nodes.xml", "/empty-response.xml", SUBTREE);
	}

	@Test
	public void testGetConfigWithNonExistingNodeAsSelectNodes() throws Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGet(m_server, "/filter-album-studio-select-node-not-existing.xml", "/empty-response.xml", MESSAGE_ID);
	}

	@Test
	public void testGetConfigWithTwoContainersWithDiffNs() throws Exception {
		initServerWithJukeboxAdminsFromDiffNs();
		verifyGetConfig("/getandgetconfigwithfilteringtest/getconfig-filter-admin-2ns-select-nodes.xml",
				"/getandgetconfigwithfilteringtest/getconfig-filter-admin-2ns-select-nodes-response.xml");
	}

	private void initServerWithJukeboxAdminsFromDiffNs() throws SchemaBuildException, ModelNodeInitException, SAXException, IOException {
		m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		m_server = new NetConfServerImpl(m_schemaRegistry);
		RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
		m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
		m_schemaRegistry.loadSchemaContext(JUKEBOX_LOCAL_NAME, getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
		m_subSystem = Mockito.mock(LocalSubSystem.class);
		InMemoryDSM inMemoryDSM = new InMemoryDSM(m_schemaRegistry);
		try {
			YangUtils.deployInMemoryHelpers(Arrays.asList(EXAMPLE_JUKEBOX_WITHOUT_ADMIN_YANG,
					EXAMPLE_JUKEBOX_WITH_ADMIN_NS_1_YANG,EXAMPLE_JUKEBOX_WITH_ADMIN_NS_2_YANG), m_subSystem, m_modelNodeHelperRegistry,
					m_subSystemRegistry, m_schemaRegistry, inMemoryDSM
					, null, null, null, null);
		} catch (ModelNodeFactoryException e) {
			throw new ModelNodeInitException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		QName jQname = QName.create("http://example.com/ns/example-jukebox-without-admin", JB_REVISION, JUKEBOX_LOCAL_NAME);
		SchemaPath jboxSP = SchemaPath.create(true, jQname);
		ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(jboxSP);
		ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode, m_modelNodeHelperRegistry,
		m_subSystemRegistry, m_schemaRegistry, inMemoryDSM);
		rootModelNodeAggregator.addModelServiceRootHelper(jboxSP, containerHelper);
		m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
		DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
		dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
		m_server.setRunningDataStore(dataStore);
		String xmlFilePath = TestUtil.class.getResource("/getandgetconfigwithfilteringtest/example-jukebox-with-admin-diff-ns.xml").getPath();
		YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
		verifyGetConfig(null, "/getandgetconfigwithfilteringtest/getconfig-unfiltered-admin-2ns.xml");
	}

	public static List<YangTextSchemaSource> getJukeBoxYangs() {
		return TestUtil.getByteSources(getJukeBoxYangFileName());
	}

	public static List<String> getJukeBoxYangFileName(){
		List<String> yangFiles = new ArrayList<>();
		yangFiles.add(EXAMPLE_JUKEBOX_WITHOUT_ADMIN_YANG);
		yangFiles.add(EXAMPLE_JUKEBOX_WITH_ADMIN_NS_1_YANG);
		yangFiles.add(EXAMPLE_JUKEBOX_WITH_ADMIN_NS_2_YANG);
		return yangFiles;
	}

	private void verifyGetConfig(String filterInput, String expectedOutput) throws SAXException, IOException {
		verifyGetConfig(filterInput, expectedOutput, SUBTREE);
	}

	private void verifyGetConfig(String filterInput, String expectedOutput, String filterType) throws SAXException, IOException {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetConfigRequest request = new GetConfigRequest();
		request.setMessageId(MESSAGE_ID);
		request.setSource("running");
		if (filterInput != null) {
			Element xmlFilter = loadAsXml(filterInput);
			NetconfFilter filter = new NetconfFilter();
			// we have two variants fo the select node in here
			filter.setType(filterType);
			filter.addXmlFilter(xmlFilter);
			request.setFilter(filter);
		}

		NetConfResponse response = new NetConfResponse();
		response.setMessageId(MESSAGE_ID);

		m_server.onGetConfig(client, request, response);

		assertXMLEquals(expectedOutput, response);
	}

	private void populateStateValues(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttributes, Map<ModelNodeId, List<Element>> stateAttributesValues) {
		Document document = createDocument();
		Element albumElement = document.createElementNS(JB_NS, "album-count");
		albumElement.setTextContent("2");

		Element artistElement = document.createElementNS(JB_NS, "artist-count");
		artistElement.setTextContent(MESSAGE_ID);

		Element songElement = document.createElementNS(JB_NS, "song-count");
		songElement.setTextContent("5");

		List<Element> elements = new ArrayList<>();
		elements.add(albumElement);
		elements.add(artistElement);
		elements.add(songElement);

		stateAttributesValues.put(m_modelNodeId, elements);

	}

	private void mockStateAttributes() throws GetAttributeException {

		Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttributes = new HashMap<>();
		List<QName> qNames = new ArrayList<>();
		qNames.add(QName.create(JB_NS, JB_REVISION, "album-count"));
		qNames.add(QName.create(JB_NS, JB_REVISION, "artist-count"));
		qNames.add(QName.create(JB_NS, JB_REVISION, "song-count"));
		m_modelNodeId = new ModelNodeId("/container=jukebox/container=library", JB_NS);
		stateAttributes.put(m_modelNodeId, new Pair<List<QName>, List<FilterNode>>(qNames, new ArrayList<FilterNode>()));
		Map<ModelNodeId, List<Element>> stateAttributesValues = new HashMap<>();
		populateStateValues(stateAttributes,stateAttributesValues);
		Mockito.when(m_subSystem.retrieveStateAttributes(Mockito.eq(stateAttributes), Mockito.eq(NetconfQueryParams.NO_PARAMS), Mockito.any(StateAttributeGetContext.class))).thenReturn(stateAttributesValues);
	}
}
