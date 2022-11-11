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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.LibrarySystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.DsmJukeboxSubsystem;
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

@RunWith(RequestScopeJunitRunner.class)
public class SingleCallbackSubSystemTest {

	private NetConfServerImpl m_server;

	private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();

	private SchemaRegistry m_schemaRegistry;

	private LibrarySystem m_librarySystem;

	private static final String JUKEBOX_DATA_WITHOUT_YEAR = GetGetconfigWithStateFilterTest.class.getResource("/data-with-artists-penny-and-lenny-without-year.xml").getPath();

	private int m_numOfCallToLibrarySystem;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
	private ModelNodeDataStoreManager m_modelNodeDsm;
	private AlbumSubsystem m_albumSubsystem;
	private DsmJukeboxSubsystem m_jukeboxSubsystem;

	@Before
	public void initServer() throws SchemaBuildException, ModelNodeInitException {
		m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangsWithAlbumStateAttributes(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		m_server = new NetConfServerImpl(m_schemaRegistry);
		m_numOfCallToLibrarySystem = 0;
		m_librarySystem = new LibrarySystem() {
			@Override
			public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes)
					throws GetAttributeException {
				m_numOfCallToLibrarySystem++;
				return super.retrieveStateAttributes(attributes);
			}
		};

		m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
		String componentId = "example-jukebox";
		m_albumSubsystem = spy(new AlbumSubsystem());

		m_jukeboxSubsystem = mock(DsmJukeboxSubsystem.class);
		ModelNode jukeboxNode = YangUtils.createInMemoryModelNode(TestUtil.getJukeBoxDepNamesWithStateAttrs(), m_jukeboxSubsystem,
				m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
		m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
		registerSubsystems(componentId);
		RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				m_modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(componentId, jukeboxNode);
		DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
		NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
		dataStore.setNbiNotificationHelper(nbiNotificationHelper);
		m_server.setRunningDataStore(dataStore);
		loadXmlDataIntoServer(m_server, JUKEBOX_DATA_WITHOUT_YEAR);
	}

	@Test
	public void testGetWithoutFiltering() throws SAXException, IOException, GetAttributeException {
		verifyGet(m_server, "", "/get-unfiltered-single-callback-yang.xml", "1");

		// verify single call to LibrarySystem (has 3 state attributes : song-count, album-count,
		// artist-count)
		assertEquals(1, m_numOfCallToLibrarySystem);

		// verify single call to AlbumSystem (has 1 state attributes : album-song-count in 2 nodes,
		// 1 for album Greatest hits, 1 for album Circus)
		assertEquals(1, m_albumSubsystem.getNumOfCalls());

		verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
	}

	// <library>
	// 	<song-count>5</song-count>
	// 		<artist>
	// 			<name/>
	// 			<album>
	// 				<name/>
	// 			</album>
	// 		</artist>
	// </library>
	@Test
	public void testGetWithFilter() throws SAXException, IOException, GetAttributeException {
		verifyGet(m_server, "/single-callback-album-state-filter3.xml", "/single-callback-get-with-album-state-filter3-response.xml", "1");

		// 1 call for 1 filter node: <jbox:song-count>5</jbox:song-count>
		assertEquals(1, m_numOfCallToLibrarySystem);

		verify(m_albumSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
		verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
	}

	// <library>
	// 		<song-count>5</song-count>
	// 		<album-count>2</album-count>
	// </library>
	@Test
	public void testGetWithStateFilter4() throws SAXException, IOException, GetAttributeException {
		verifyGet(m_server, "/single-callback-album-state-filter4.xml", "/single-callback-get-with-album-state-filter4-response.xml", "1");

		// 1 call for 2 filter nodes: <jbox:song-count>5</jbox:song-count> and
		// <jbox:album-count>2</jbox:album-count>
		// 1 call for other state attributes (artist-count)
		assertEquals(2, m_numOfCallToLibrarySystem);
		assertEquals(1, m_albumSubsystem.getNumOfCalls());
		verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));

	}

	// <library>
	// 		<song-count>5</song-count>
	// 		<album-count>2</album-count>
	// 		<artist-count>1</artist-count>
	// </library>
	@Test
	public void testGetWithStateFilter5() throws SAXException, IOException, GetAttributeException {
		verifyGet(m_server, "/single-callback-album-state-filter5.xml", "/single-callback-get-with-album-state-filter5-response.xml", "1");

		// 1 call for 3 filter nodes: <song-count>5</song-count>
		// <album-count>2</album-count>
		// <artist-count>1</artist-count>
		assertEquals(1, m_numOfCallToLibrarySystem);
		verify(m_albumSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
		verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
	}

	@Test
	public void testGetConfigWithoutFiltering() throws SAXException, IOException, GetAttributeException {
		verifyGetConfig(m_server, "", "/getconfig-unfiltered-single-callback-yang.xml", "1");

		// get-config has no call to SubSystem
		assertEquals(0, m_numOfCallToLibrarySystem);
		assertEquals(0, m_albumSubsystem.getNumOfCalls());
	}

	@Test
	public void testGetConfigWithStateFilter() throws SAXException, IOException, GetAttributeException {
		// results does not contain state attributes
		verifyGetConfig(m_server, "/single-callback-album-state-filter1.xml", "/response_with_name_ns.xml", "1");
		verifyGetConfig(m_server, "/single-callback-album-state-filter2.xml", "/empty-response.xml", "1");

		// get-config has no call to SubSystem
		assertEquals(0, m_numOfCallToLibrarySystem);
		assertEquals(0, m_albumSubsystem.getNumOfCalls());
		verify(m_jukeboxSubsystem, never()).retrieveStateAttributes(any(Map.class), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
	}

	private void registerSubsystems(String componentId) {
		m_subSystemRegistry.register(componentId, LIBRARY_SCHEMA_PATH, m_librarySystem);
		m_subSystemRegistry.register(componentId,new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH).appendLocalName("song-count").build(),m_librarySystem);
		m_subSystemRegistry.register(componentId,new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH).appendLocalName("album-count").build(),m_librarySystem);
		m_subSystemRegistry.register(componentId,new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH).appendLocalName("artist-count").build(),m_librarySystem);
		m_subSystemRegistry.register(componentId, ALBUM_SCHEMA_PATH, m_albumSubsystem);
		m_subSystemRegistry.register(componentId, new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).appendLocalName("album-song-count").build(), m_albumSubsystem);
		m_subSystemRegistry.register(componentId, new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).appendLocalName("album-state-container").build(), m_albumSubsystem);
	}

	private class AlbumSubsystem extends AbstractSubSystem {

		private int m_numOfCalls;

		public int getNumOfCalls() {
			return m_numOfCalls;
		}

		@Override
		protected boolean byPassAuthorization() {
			return true;
		}

		@Override
		public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
			m_numOfCalls++;
			Document doc = DocumentUtils.createDocument();
			Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
			for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
				Map<QName, Object> stateAttributes = new LinkedHashMap<QName, Object>();
				ModelNodeId modelNodeId = entry.getKey();
				List<QName> attributes = entry.getValue().getFirst();
				for (QName attribute : attributes) {
					// dummy logic
					if (modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue().equalsIgnoreCase("Circus")) {
						stateAttributes.put(attribute, 3);
					} else {
						stateAttributes.put(attribute, 2);
					}
				}
				List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
				stateInfo.put(modelNodeId, stateElements);
			}
			return stateInfo;
		}
	}

}
