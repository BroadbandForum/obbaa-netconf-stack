package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.createDocument;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class GetAndGetConfigWithFilteringTest {
	@SuppressWarnings("unused")
	private final static Logger LOGGER = Logger.getLogger(GetAndGetConfigWithFilteringTest.class);

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
		m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		m_server = new NetConfServerImpl(m_schemaRegistry);
		RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
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
		request.setMessageId("1");

		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");

		m_server.onGet(client, request, response);
		

		assertXMLEquals("/getandgetconfigwithfilteringtest/get-unfiltered.xml", response);
	}
	
	@Test
	public void testGetWithEmptyFilter() throws SAXException, IOException {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetRequest request = new GetRequest();
		request.setMessageId("1");

		NetconfFilter filter	=	new NetconfFilter();
		filter.setType("subtree");
		
		request.setFilter(filter);
		
		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");
		
		NetConfResponse expectedResponse	= new NetConfResponse();
		expectedResponse.setMessageId("1");
		expectedResponse.setDataContent(java.util.Collections.<Element>emptyList());
		
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
	public void verifyGetConfigEmptyFilter() throws SAXException, IOException {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetConfigRequest request = new GetConfigRequest();
		request.setMessageId("1");
		request.setSource("running");
		NetconfFilter filter = new NetconfFilter();
		filter.setType("subtree");
		request.setFilter(filter);

		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");

		NetConfResponse expectedResponse = new NetConfResponse();
		expectedResponse.setMessageId("1");
		expectedResponse.setDataContent(java.util.Collections.<Element>emptyList());
		m_server.onGetConfig(client, request, response);

		assertXMLEquals(expectedResponse, response);
	}

	@Test
	public void testGetWithMatchFilterInContainer() throws Exception {
		initServerWithDefaultXml("/example-jukebox-empty-album-admin.xml", "/yangs/example-jukebox.yang");
		verifyGet(m_server, "/filter-matchnode-in-container.xml", "/filter-matchnode-in-container-response.xml","1");
	}

	@Test
	public void testGetConfigListWithMultiKeysOrder() throws SAXException, IOException, Exception {
		initServerWithDefaultXml("/example-jukebox-list-multikeys.xml", "/yangs/example-jukebox-list-multikeys.yang");
		verifyGetConfig("/filter-select-list-with-multikeys.xml", "/getconfig-response-list-multikeys-ordered.xml");
	}

	private void verifyGetConfig(String filterInput, String expectedOutput) throws SAXException, IOException {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetConfigRequest request = new GetConfigRequest();
		request.setMessageId("1");
		request.setSource("running");
		if (filterInput != null) {
			NetconfFilter filter = new NetconfFilter();
			// we have two variants fo the select node in here
			filter.setType("subtree");
			filter.addXmlFilter(loadAsXml(filterInput));
			request.setFilter(filter);
		}

		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");

		m_server.onGetConfig(client, request, response);

		assertXMLEquals(expectedOutput, response);
	}

	private void populateStateValues(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttributes, Map<ModelNodeId, List<Element>> stateAttributesValues) {
		Document document = createDocument();
		Element albumElement = document.createElementNS(JB_NS, "album-count");
		albumElement.setTextContent("2");

		Element artistElement = document.createElementNS(JB_NS, "artist-count");
		artistElement.setTextContent("1");

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
		Mockito.when(m_subSystem.retrieveStateAttributes(stateAttributes, NetconfQueryParams.NO_PARAMS)).thenReturn(stateAttributesValues);
	}
}
