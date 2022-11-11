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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfigV2;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyNotification;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelperImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class YangEditConfigCreateTest extends AbstractYangValidationTestSetup {
    private NetConfServerImpl m_server;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private static final String m_empty_jukebox_xmlFile = YangEditConfigCreateTest.class.getResource("/empty-example-jukebox.xml").getPath();
	private static final String m_empty_library_xmlFile = YangEditConfigCreateTest.class.getResource("/empty-library-example-jukebox.xml").getPath();
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeWithAttributes m_modelWithAttributes;
    private InMemoryDSM m_modelNodeDsm;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("jukebox", TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelWithAttributes = YangUtils.createInMemoryModelNode(YANG_PATH, m_testSubSystem,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry)
                .addModelServiceRoot(m_componentId, m_modelWithAttributes);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }

    @Test
    public void testCreateLibraryWhenThereIsNoLibraryWorks() throws ModelNodeInitException, SAXException, IOException, NetconfMessageBuilderException {
    	//load server without library
        loadXmlDataIntoServer(m_server,m_empty_jukebox_xmlFile);

        //send edit config to create library
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/create-library.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/create-library.xml");
        }

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> create\n"));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-library.xml", "1");
        
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-library.xml", "/empty-library.xml", "1");
    }
    
     @Test
    public void testCreateLibraryWhenThereIsLibraryThrowsProperError() throws SAXException, IOException, ModelNodeInitException {
         //empty library
         loadXmlDataIntoServer(m_server, m_empty_library_xmlFile);

         //send edit config to create library that already exist
         NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-library.xml"), "1");

         // assert not-Ok response
         assertXMLEquals("/data-exists-error.xml", response);

         //some data
         loadXmlDataIntoServer(m_server, XML_PATH);

         //send edit config to create library that already exist
         response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-library.xml"), "1");

         // assert not-Ok response
         assertXMLEquals("/data-exists-error.xml", response);
    }

    @Test
    public void testCreateArtistLennyFreshly() throws SAXException, IOException, ModelNodeInitException, NetconfMessageBuilderException {
    	//load server without library
        loadXmlDataIntoServer(m_server, m_empty_library_xmlFile);


        //send edit config to create artist lenny having album circus
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/create-artist-lenny-having-album-circus.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/create-artist-lenny.xml");
        }

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> create\n" +
                "   name -> create { previousVal = 'null', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = 'Circus' }\n" +
                "    year -> create { previousVal = 'null', currentVal = '1995' }\n"));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/with-circus.xml", "1");
    }

    @Test
    public void testEditWhenDSIsFullyRebuilt() throws SAXException, IOException, ModelNodeInitException, NetconfMessageBuilderException, SchemaBuildException {
        SystemPropertyUtils systemPropertyUtils = mock(SystemPropertyUtils.class);
        SystemPropertyUtils originalUtil = SystemPropertyUtils.getInstance();
        SystemPropertyUtils.setInstance(systemPropertyUtils);
        when(systemPropertyUtils.getFromEnvOrSysProperty("IS_FULL_DS_REBUILT")).thenReturn("true");

        initServer();
        //load server without library
        loadXmlDataIntoServer(m_server, m_empty_library_xmlFile);

        //send edit config to create artist lenny having album circus
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/create-artist-lenny-having-album-circus.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
        }

        //assert changeTreeNode
        try {
            m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                    " library[/jukebox/library] -> modify\n" +
                    "  artist[/jukebox/library/artist[name='Lenny']] -> create\n" +
                    "   name -> create { previousVal = 'null', currentVal = 'Lenny' }\n" +
                    "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> create\n" +
                    "    name -> create { previousVal = 'null', currentVal = 'Circus' }\n" +
                    "    year -> create { previousVal = 'null', currentVal = '1995' }\n"));
            fail("Exception should have been thrown");
        } catch(Exception e) {
            assertEquals("ChangeTreeNodes not found for specified schema path -> AbsoluteSchemaPath{path=[(http://example.com/ns/example-jukebox?revision=2014-07-03)jukebox]}", e.getMessage());
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/with-circus.xml", "1");
        SystemPropertyUtils.setInstance(originalUtil);
    }

    @Test
    public void testCreateArtistLennyHavingAlbumCircusThrowsError() throws SAXException, IOException, ModelNodeInitException {
    	//load server with empty library
        loadXmlDataIntoServer(m_server, m_empty_library_xmlFile);
        //send edit config to create album Circus
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-artist-lenny-having-album-circus.xml"), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/with-circus.xml", "1");
        
        //again send edit config to create album Cirus that already exists
        NetConfResponse failedResponse = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-artist-lenny-having-album-circus.xml"), "1");
        
        // assert not-Ok response
        assertXMLEquals("/data-exists-error-album.xml", failedResponse);
    }

    @Test
    public void testNaturalKeyWithSpaceWorks() throws IOException, SAXException, NetconfMessageBuilderException {
        //load server without library
        loadXmlDataIntoServer(m_server,m_empty_jukebox_xmlFile);

        //send edit config to create empty library
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-library.xml"), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //send edit config to create album Circus with space
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/create-album-circus-with-space.xml"), "1");

        response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/create-album-circus-with-space.xml");
        }

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name=' Lenny']] -> create\n" +
                "   name -> create { previousVal = 'null', currentVal = ' Lenny' }\n" +
                "   album[/jukebox/library/artist[name=' Lenny']/album[name=' Circus ']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = ' Circus ' }\n" +
                "    year -> create { previousVal = 'null', currentVal = '1995' }\n"));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/naturalkey-with-space.xml", "1");

        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/get-artist-with-filter.xml", "/naturalkey-with-space.xml", "1");
    }
}
