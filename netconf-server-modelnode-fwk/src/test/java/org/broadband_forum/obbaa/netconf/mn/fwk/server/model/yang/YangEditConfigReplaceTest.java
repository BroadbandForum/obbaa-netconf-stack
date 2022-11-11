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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfigV2;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyNotification;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class YangEditConfigReplaceTest extends AbstractYangValidationTestSetup {

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }

    @Test
    public void testReplaceWhenThereisNoLibraryWorks() throws SAXException, IOException, NetconfMessageBuilderException {
        
        //remove the library first
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-config-delete-library.xml"), "1");
        
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");
        
        //do replace and make sure you get a ok response
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/replace-config-library.xml"), "1");

        response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> create\n" +
                "  artist[/jukebox/library/artist[name='Depeche Mode']] -> create\n" +
                "   name -> create { previousVal = 'null', currentVal = 'Depeche Mode' }\n" +
                "   album[/jukebox/library/artist[name='Depeche Mode']/album[name='Violator']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = 'Violator' }\n" +
                "    year -> create { previousVal = 'null', currentVal = '1990' }\n" +
                "    song[/jukebox/library/artist[name='Depeche Mode']/album[name='Violator']/song[name='Enjoy the Silence']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Enjoy the Silence' }\n" +
                "     location -> create { previousVal = 'null', currentVal = 'here' }\n" +
                "    song[/jukebox/library/artist[name='Depeche Mode']/album[name='Violator']/song[name='Halo']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Halo' }\n" +
                "     location -> create { previousVal = 'null', currentVal = 'there' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/create-library.xml");
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/depeche-mode-jukebox-yang.xml", "1");
    }

    @Test
    public void testReplaceWhenThereisLibraryWorks() throws SAXException, IOException, NetconfMessageBuilderException {
        // do replace and make sure you get a ok response
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/replace-config-library.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> delete\n" +
                "   name -> delete { previousVal = 'Lenny', currentVal = 'null' }\n" +
                "  artist[/jukebox/library/artist[name='Depeche Mode']] -> create\n" +
                "   name -> create { previousVal = 'null', currentVal = 'Depeche Mode' }\n" +
                "   album[/jukebox/library/artist[name='Depeche Mode']/album[name='Violator']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = 'Violator' }\n" +
                "    year -> create { previousVal = 'null', currentVal = '1990' }\n" +
                "    song[/jukebox/library/artist[name='Depeche Mode']/album[name='Violator']/song[name='Enjoy the Silence']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Enjoy the Silence' }\n" +
                "     location -> create { previousVal = 'null', currentVal = 'here' }\n" +
                "    song[/jukebox/library/artist[name='Depeche Mode']/album[name='Violator']/song[name='Halo']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Halo' }\n" +
                "     location -> create { previousVal = 'null', currentVal = 'there' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/replace-artist-lenny-with-depeche-mode.xml");
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/depeche-mode-jukebox-yang.xml", "1");
    }
    
    @Test
    public void testReplaceWhenThereisNoAlbumWorks() throws SAXException, IOException, NetconfMessageBuilderException {
        
        //remove the library first
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-album-circus.xml"), "1");
        
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do replace and make sure you get a ok response
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/replace-config-album-circus.xml"), "1");

        response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = 'Circus' }\n" +
                "    year -> create { previousVal = 'null', currentVal = '1995' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Magdalene']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Magdalene' }\n" +
                "     location -> create { previousVal = 'null', currentVal = 'here' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Thin Ice']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Thin Ice' }\n" +
                "     location -> create { previousVal = 'null', currentVal = 'there' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/create-album-circus.xml");
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/lenny-circus-after-remove-replace.xml", "1");
    }

    @Test
    public void testReplaceWhenThereisAlbumWorks() throws SAXException, IOException, NetconfMessageBuilderException {
        // do replace and make sure you get a ok response
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/replace-config-album-circus.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "    genre -> delete { previousVal = 'jbox:rock', currentVal = 'null' }\n" +
                "    admin[/jukebox/library/artist[name='Lenny']/album[name='Circus']/admin] -> delete\n" +
                "     label -> delete { previousVal = 'Sony', currentVal = 'null' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Rock and roll is dead']] -> delete\n" +
                "     name -> delete { previousVal = 'Rock and roll is dead', currentVal = 'null' }\n" +
                "     location -> delete { previousVal = 'desktop/mymusic', currentVal = 'null' }\n" +
                "     format -> delete { previousVal = 'amr', currentVal = 'null' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Circus']] -> delete\n" +
                "     name -> delete { previousVal = 'Circus', currentVal = 'null' }\n" +
                "     location -> delete { previousVal = 'desktop/mymusic', currentVal = 'null' }\n" +
                "     format -> delete { previousVal = 'wma', currentVal = 'null' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Beyond the 7th Sky']] -> delete\n" +
                "     name -> delete { previousVal = 'Beyond the 7th Sky', currentVal = 'null' }\n" +
                "     location -> delete { previousVal = 'desktop/mymusic', currentVal = 'null' }\n" +
                "     format -> delete { previousVal = 'mp3', currentVal = 'null' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Magdalene']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Magdalene' }\n" +
                "     location -> create { previousVal = 'null', currentVal = 'here' }\n" +
                "    song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='Thin Ice']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Thin Ice' }\n" +
                "     location -> create { previousVal = 'null', currentVal = 'there' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/replace-album-circus.xml");
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/lenny-circus-replace.xml", "1");
    }

    @Test
    public void testReplaceWithLeafNode() throws SAXException, IOException, NetconfMessageBuilderException {
        // do replace to set the initial value for leaf node
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/replace-config-jukebox-with-leaf.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> delete\n" +
                " name -> create { previousVal = 'null', currentVal = 'TEST' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/modify-jukebox.xml");
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-leaf-replaced.xml", "1");

        // do replace with the same value for the leaf node
        result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/replace-config-jukebox-with-leaf.xml"), "1");

        response = (NetConfResponse) result.get("response");
        notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // assert Notifications
        if(notifications != null) {
            assertEquals(0, notifications.size());
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-leaf-replaced.xml", "1");
    }

    @Test
    public void testReplaceOnRootNode() throws IOException, SAXException, NetconfMessageBuilderException {

        // do a get-config to see the data in datastore
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/get-unfiltered-yang.xml", "1");

        // do replace and make sure you get a ok response
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/replace-config-jukebox.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> delete\n" +
                "   name -> delete { previousVal = 'Lenny', currentVal = 'null' }\n" +
                "  artist[/jukebox/library/artist[name='FNMS-1742']] -> create\n" +
                "   name -> create { previousVal = 'null', currentVal = 'FNMS-1742' }\n" +
                "   album[/jukebox/library/artist[name='FNMS-1742']/album[name='There should be only this album in Jukebox']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = 'There should be only this album in Jukebox' }\n" +
                "    year -> create { previousVal = 'null', currentVal = '1995' }\n" +
                "    song[/jukebox/library/artist[name='FNMS-1742']/album[name='There should be only this album in Jukebox']/song[name='We dont talk anymore']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'We dont talk anymore' }\n" +
                "    song[/jukebox/library/artist[name='FNMS-1742']/album[name='There should be only this album in Jukebox']/song[name='Close']] -> create\n" +
                "     name -> create { previousVal = 'null', currentVal = 'Close' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/replace-jukebox.xml");
        }
        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/replace-jukebox-response.xml", "1");
    }

    @Test
    public void testChangeAlbumOrder() throws SAXException, IOException {

        //send edit config to replace album by changing order
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/replace-album-by-changing-order.xml"), "1");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        m_testSubSystem.assertModelNodeId("song/name=Are you gonne go my way",
                "ModelNodeId[/container=jukebox/container=library/container=artist/name=Lenny/container=album/name=Greatest hits/container=song/name=Are you gonne go my way]");

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n"));

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-after-changing-album-order.xml", "1");
    }

    @Test
    public void testChangingAlbumOrderDoesNotResultToAnyEffectiveChange() throws SAXException, IOException, NetconfMessageBuilderException {
        m_testSubSystem.clearChanges();

        //send edit config to change album order using replace operation which results in same order (i.e No effective change in order)
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order-through-replace-operation2.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        try {
            m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                    " library[/jukebox/library] -> modify\n" +
                    "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                    "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                    "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> modify\n" +
                    "    name -> none { previousVal = 'Greatest hits', currentVal = 'Greatest hits' }\n"));
            fail("AssertCTNException should have been thrown!!!");
        } catch (LocalSubSystem.AssertCTNException e) {
            assertEquals(JUKEBOX_SCHEMA_PATH, e.getSchemaPath());
            assertEquals("ChangeTreeNodes not found for specified schema path -> " +
                    "AbsoluteSchemaPath{path=[(http://example.com/ns/example-jukebox?revision=2014-07-03)jukebox]}", e.getMessage());
        }

        // assert Notifications
        if(notifications != null) {
            assertEquals(0, notifications.size());
        }

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");
    }

    @Test
    public void testChangingMultipleAlbumsOrdersTobeInsertedAtFirst() throws SAXException, IOException, NetconfMessageBuilderException {
        m_testSubSystem.clearChanges();

        //send edit config to change order of two albums to be moved to first position
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order-through-replace-operation3.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> modify\n" +
                "    name -> none { previousVal = 'Greatest hits', currentVal = 'Greatest hits' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/modify-multiple-album-order-to-first.xml");
        }

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");
    }

    @Test
    public void testChangingMultipleAlbumsOrdersWithRespectToSameAlbum() throws SAXException, IOException, NetconfMessageBuilderException {

        //send edit config change order of multiple albums with respect to same album using replace operation
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order-through-replace-operation4.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> modify\n" +
                "    name -> none { previousVal = 'Greatest hits', currentVal = 'Greatest hits' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='AlbumWithoutGenre']] -> modify\n" +
                "    name -> none { previousVal = 'AlbumWithoutGenre', currentVal = 'AlbumWithoutGenre' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/modify-multiple-album-order.xml");
        }

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-after-changing-multiple-albums-order.xml", "1");
    }

    @Test
    public void testSimultaneousCreateAndModificationOfAlbumOrder() throws SAXException, IOException, NetconfMessageBuilderException {

        //send edit config to create new album and change existing album order using replace operation
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order-through-replace-operation5.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='New album']] -> create\n" +
                "    name -> create { previousVal = 'null', currentVal = 'New album' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='AlbumWithoutGenre']] -> modify\n" +
                "    name -> none { previousVal = 'AlbumWithoutGenre', currentVal = 'AlbumWithoutGenre' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/simultaneously-create-and-modify-album-order.xml");
        }

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-changing-order-using-newly-created-album.xml", "1");
    }

    @Test
    public void testChangeOrderedByListsOrderDuringReplacingParent_WhereAllListsRearrange() throws SAXException, IOException, NetconfMessageBuilderException {

        //send edit config to change existing albums order using replace operation
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-albums-order-during-replacing-artist.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='AlbumWithoutGenre']] -> modify\n" +
                "    name -> none { previousVal = 'AlbumWithoutGenre', currentVal = 'AlbumWithoutGenre' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> modify\n" +
                "    name -> none { previousVal = 'Greatest hits', currentVal = 'Greatest hits' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/order-change-of-albums-without-specifying-insert-operation-when-parent-is-getting-replaced.xml");
        }

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-changing-orders-of-multiple-albums-through-replace.xml", "1");
    }

    @Test
    public void testChangeOrderedByListsOrderDuringReplacingParent_WhereSomeListsRearrange() throws SAXException, IOException, NetconfMessageBuilderException {

        //send edit config to change existing albums order using replace operation
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-albums-order-during-replacing-artist2.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> modify\n" +
                "    name -> none { previousVal = 'Greatest hits', currentVal = 'Greatest hits' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/order-change-of-albums-without-specifying-insert-operation-when-parent-is-getting-replaced2.xml");
        }

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-changing-orders-of-multiple-albums-through-replace2.xml", "1");
    }

    @Test
    public void testChangeOrderedByListsOrderDuringReplacingParent_WithNoDeltaChangeButStillAllRearrange() throws SAXException, IOException, NetconfMessageBuilderException {

        //send edit config to change existing albums order using replace operation
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-albums-order-during-replacing-artist3.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']] -> modify\n" +
                "    name -> none { previousVal = 'Greatest hits', currentVal = 'Greatest hits' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                "    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='AlbumWithoutGenre']] -> modify\n" +
                "    name -> none { previousVal = 'AlbumWithoutGenre', currentVal = 'AlbumWithoutGenre' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/order-change-of-albums-without-specifying-insert-operation-when-parent-is-getting-replaced3.xml");
        }

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");
    }
}
