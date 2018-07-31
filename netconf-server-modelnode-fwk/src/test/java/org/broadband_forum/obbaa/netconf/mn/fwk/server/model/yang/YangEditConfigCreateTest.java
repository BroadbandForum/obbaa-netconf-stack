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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;

public class YangEditConfigCreateTest extends AbstractYangValidationTestSetup {
    private NetConfServerImpl m_server;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private static final String m_empty_jukebox_xmlFile = YangEditConfigCreateTest.class.getResource
            ("/empty-example-jukebox.xml").getPath();
    private static final String m_empty_library_xmlFile = YangEditConfigCreateTest.class.getResource
            ("/empty-library-example-jukebox.xml").getPath();
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeWithAttributes m_modelWithAttributes;
    private InMemoryDSM m_modelNodeDsm;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("jukebox", TestUtil.getJukeBoxYangs(), null, Collections.emptyMap());
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelWithAttributes = YangUtils.createInMemoryModelNode(YANG_PATH, new LocalSubSystem(),
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_modelNodeDsm, m_subSystemRegistry)
                .addModelServiceRoot(m_componentId, m_modelWithAttributes);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }

    @Test
    public void testCreateLibraryWhenThereIsNoLibraryWorks() throws ModelNodeInitException, SAXException, IOException {
        //load server without library
        YangUtils.loadXmlDataIntoServer(m_server, m_empty_jukebox_xmlFile);

        //send edit config to create library
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, TestUtil.loadAsXml("/create-library.xml"),
                "1");

        // assert Ok response
        TestUtil.assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        TestUtil.verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-library.xml", "1");

        TestUtil.verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-library.xml", "/empty-library.xml", "1");
    }

    @Test
    public void testCreateLibraryWhenThereIsLibraryThrowsProperError() throws SAXException, IOException,
            ModelNodeInitException {

        YangUtils.loadXmlDataIntoServer(m_server, XML_PATH);

        //send edit config to create library that already exist
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, TestUtil.loadAsXml("/create-library.xml"),
                "1");

        // assert not-Ok response
        TestUtil.assertXMLEquals("/data-exists-error.xml", response);
    }

    @Test
    public void testCreateAlbumCircus() throws SAXException, IOException, ModelNodeInitException {
        //load server without library
        YangUtils.loadXmlDataIntoServer(m_server, m_empty_library_xmlFile);

        //send edit config to create album Circus
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, TestUtil.loadAsXml("/create-album-circus" +
                ".xml"), "1");

        // assert Ok response
        TestUtil.assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        TestUtil.verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/with-circus.xml", "1");
    }

    @Test
    public void testCreateAlbumCircusThrowsError() throws SAXException, IOException, ModelNodeInitException {
        //load server with empty library
        YangUtils.loadXmlDataIntoServer(m_server, m_empty_library_xmlFile);
        //send edit config to create album Circus
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, TestUtil.loadAsXml("/create-album-circus" +
                ".xml"), "1");

        // assert Ok response
        TestUtil.assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        TestUtil.verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/with-circus.xml", "1");

        //again send edit config to create album Cirus that already exists
        NetConfResponse failedResponse = TestUtil.sendEditConfig(m_server, m_clientInfo, TestUtil.loadAsXml
                ("/create-album-circus.xml"), "1");

        // assert not-Ok response
        TestUtil.assertXMLEquals("/data-exists-error-album.xml", failedResponse);
    }
}
