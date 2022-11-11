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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfigV2;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyNotification;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class YangEditConfigMergeTest extends AbstractYangValidationTestSetup{
	
	@Before
	public void initServer() throws SchemaBuildException, ModelNodeInitException {	
	    super.setup();
	}

	@Test
	public void testChangeAlbumYear() throws SAXException, IOException, NetconfMessageBuilderException {
		//send edit config to merge album
		Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-simple.xml"), "1");

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
				"    year -> modify { previousVal = '1995', currentVal = '1996' }\n"));

		// assert Notifications
		if(notifications != null) {
			assertEquals(1, notifications.size());
			verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/modify-year-in-album.xml");
		}

		//do a get-config to be sure
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-year-editconfig-expected.xml", "1");
	}

	@Test
	public void testMergeNewSong() throws SAXException, IOException, NetconfMessageBuilderException {
		//send edit config to merge album
		Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-merge-songs.xml"), "1");

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
				"    song[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']/song[name='Sing my way']] -> create\n" +
				"     name -> create { previousVal = 'null', currentVal = 'Sing my way' }\n" +
				"     location -> create { previousVal = 'null', currentVal = 'desktop/mymusic' }\n" +
				"    song[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']/song[name='Musical feast']] -> create\n" +
				"     name -> create { previousVal = 'null', currentVal = 'Musical feast' }\n" +
				"     location -> create { previousVal = 'null', currentVal = 'desktop/mymusic' }\n" +
				"    song[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']/song[name='Fly Away']] -> delete\n" +
				"     name -> delete { previousVal = 'Fly Away', currentVal = 'null' }\n" +
				"     location -> delete { previousVal = 'desktop/mymusic', currentVal = 'null' }\n" +
				"     format -> delete { previousVal = 'flac', currentVal = 'null' }\n"));

		// assert Notifications
		if(notifications != null) {
			assertEquals(1, notifications.size());
			verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/modify-album-greatest-hits.xml");
		}

		//do a get-config to be sure
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-year-editconfig-merge-songs-expected.xml", "1");
	}

	@Test
	public void testChangeAlbumOrder() throws SAXException, IOException, NetconfMessageBuilderException {

		//send edit config to merge album by changing order
		Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order.xml"), "1");

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
				"    name -> none { previousVal = 'Circus', currentVal = 'Circus' }\n"));

		// assert Notifications
		if(notifications != null) {
			assertEquals(1, notifications.size());
			verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/modify-album-order.xml");
		}

		//do a get-config to be sure
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-after-changing-album-order.xml", "1");
	}

	@Test
	public void testAddAlbumWithoutInsertOrder() throws SAXException, IOException, NetconfMessageBuilderException {

		Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-add-album-without-order.xml"), "1");
		NetConfResponse response = (NetConfResponse) result.get("response");
		List<Notification> notifications = (List<Notification>) result.get("notifications");

		assertXMLEquals("/ok-response.xml", response);

		m_testSubSystem.assertChangeTreeNodeWithoutClearingChanges(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
				" library[/jukebox/library] -> modify\n" +
				"  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
				"   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
				"   album[/jukebox/library/artist[name='Lenny']/album[name='albumWithoutInsertOrder1']] -> create\n" +
				"    name -> create { previousVal = 'null', currentVal = 'albumWithoutInsertOrder1' }\n" +
				"   album[/jukebox/library/artist[name='Lenny']/album[name='albumWithoutInsertOrder2']] -> create\n" +
				"    name -> create { previousVal = 'null', currentVal = 'albumWithoutInsertOrder2' }\n"));

		if(notifications != null) {
			assertEquals(1, notifications.size());
			verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/create-albums-without-specifying-order.xml");
		}

		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-after-adding-albums-without-order.xml", "1");

		Map<SchemaPath, List<ChangeTreeNode>> changesMap = m_testSubSystem.getChangesMap();

		List<ChangeTreeNode> albumCTNs = changesMap.get(ALBUM_SCHEMA_PATH);

		ChangeTreeNode albumWithoutInsertOrder1CTN = albumCTNs.get(0);
		assertNull(albumWithoutInsertOrder1CTN.getInsertOperation());
		ModelNodeId album1ModelNodeId = new ModelNodeId("/container=jukebox/container=library/" +
				"container=artist/name=Lenny/container=album/name=albumWithoutInsertOrder1/container=name", JB_NS);
		assertEquals("albumWithoutInsertOrder1", albumWithoutInsertOrder1CTN.getChildren().get(album1ModelNodeId).currentValue());

		ChangeTreeNode albumWithoutInsertOrder2CTN = albumCTNs.get(1);
		assertNull(albumWithoutInsertOrder2CTN.getInsertOperation());
		ModelNodeId album2ModelNodeId = new ModelNodeId("/container=jukebox/container=library/" +
				"container=artist/name=Lenny/container=album/name=albumWithoutInsertOrder2/container=name", JB_NS);
		assertEquals("albumWithoutInsertOrder2", albumWithoutInsertOrder2CTN.getChildren().get(album2ModelNodeId).currentValue());
		m_testSubSystem.clearChanges();
	}


	@Test
	public void testChangingAlbumOrderDoesNotResultToAnyEffectiveChange() throws SAXException, IOException {
		m_testSubSystem.clearChanges();

		//send edit config to change album order using merge operation which results in same order (i.e No effective change in order)
		Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order2.xml"), "1");

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
		Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order3.xml"), "1");

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

		//send edit config to change orders of two albums with respect to same album using merge operation
		Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order4.xml"), "1");

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
		Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/editconfig-change-album-order5.xml"), "1");

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
}
