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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.createInMemoryModelNode;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class YangListTest extends AbstractYangValidationTestSetup{
	
	private static final String LIST_MODEL_YANG = "/yangs/example-jukebox-with-list-ordered-by-user.yang";
	private static final String LIST_XML = "/example-jukebox-list-ordered-by-user.xml";
	
	private NetConfServerImpl m_server;
	
	private NetconfClientInfo m_clientInfo;
	
	private ModelNodeWithAttributes m_model;

    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();

    private static final String m_yangFilePath = YangListTest.class.getResource(LIST_MODEL_YANG).getPath();
	private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    
	private static final String LIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML = "/create-album-circus-list-ordered-by-user.xml";
	private static final String LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML = "/replace-album-circus-list-ordered-by-user.xml";
	private static final String LIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML = "/merge-album-circus-list-ordered-by-user.xml";
	
	private static final String LIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML = "/create-album-circus-list-ordered-by-user-expected.xml";
	private static final String LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML = "/replace-album-circus-list-ordered-by-user-expected.xml";
	private static final String LIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML = "/merge-album-circus-list-ordered-by-user-expected.xml";
	private ModelNodeDataStoreManager m_modelNodeDsm;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

	@Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		m_server = new NetConfServerImpl(m_schemaRegistry);
	}
	
	private void setDataStoreAndModel(String datastore, String dataXmlPath) throws ModelNodeInitException, SchemaBuildException {
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(LIST_MODEL_YANG));
		String xmlPath = YangListTest.class.getResource(dataXmlPath).getPath();
        m_schemaRegistry.loadSchemaContext("yang", yangs, Collections.emptySet(), Collections.emptyMap());
		m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
		DsmJukeboxSubsystem subSystem = new DsmJukeboxSubsystem(m_modelNodeDsm, "http://example.com/ns/example-jukebox-with-singer", m_schemaRegistry);
        m_model = createInMemoryModelNode( m_yangFilePath,subSystem, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry,m_modelNodeDsm);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(m_componentId, m_model);
		m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        DataStore dataStore = new DataStore(datastore, m_rootModelNodeAggregator, m_subSystemRegistry );
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setDataStore(datastore, dataStore);
		loadXmlDataIntoServer(m_server,xmlPath);
    }
	
	@Test
	public void testEditConfigCreateOrderedByUser() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
		//prepare netconf server with running data store
		setDataStoreAndModel(StandardDataStores.RUNNING, LIST_XML);

		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LIST_EDITCONFIG_CREATE_ORDERED_BY_USER_REQUEST_XML ), "1");

		// assert Ok response
		assertXMLEquals("/ok-response.xml", response);

		//verify get reponse with filter
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LIST_EDITCONFIG_CREATE_ORDERED_BY_USER_EXPECTED_XML, "1");
	}
	
	@Test
	public void testEditConfigMergeOrderedByUser() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
		//prepare netconf server with running data store
		setDataStoreAndModel(StandardDataStores.RUNNING, LIST_XML);

		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LIST_EDITCONFIG_MERGE_ORDERED_BY_USER_REQUEST_XML), "1");

		// assert Ok response
		assertXMLEquals("/ok-response.xml", response);

		//verify get reponse with filter
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LIST_EDITCONFIG_MERGE_ORDERED_BY_USER_EXPECTED_XML, "1");
	}
	
	@Test
	public void testEditConfigReplaceOrderedByUser() throws ModelNodeInitException, SAXException, IOException, SchemaBuildException {
		//prepare netconf server with running data store
		setDataStoreAndModel(StandardDataStores.RUNNING, LIST_XML);

		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_REQUEST_XML), "1");

		// assert Ok response
		assertXMLEquals("/ok-response.xml", response);

		//verify get reponse with filter
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, LIST_EDITCONFIG_REPLACE_ORDERED_BY_USER_EXPECTED_XML, "1");
	}
}