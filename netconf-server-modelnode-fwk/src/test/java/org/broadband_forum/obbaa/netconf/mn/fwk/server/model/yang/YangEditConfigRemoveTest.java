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
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem.AssertCTNException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class YangEditConfigRemoveTest extends AbstractYangValidationTestSetup{

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
       super.setup();
    }

    @Test
    public void testRemoveLibrary() throws SAXException, IOException, NetconfMessageBuilderException {
    	//send editconfig to remove library
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/remove-config-delete-library.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> delete\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/delete-library.xml");
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");

        //again send editconfig to remove library that was already removed
        result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/remove-config-delete-library.xml"), "1");

        response = (NetConfResponse) result.get("response");
        notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        try {
            m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                    " library[/jukebox/library] -> delete\n"));
            fail("AssertCTNException should have been thrown!!!");
        } catch (AssertCTNException e) {
            assertEquals(JUKEBOX_SCHEMA_PATH, e.getSchemaPath());
            assertEquals("ChangeTreeNodes not found for specified schema path -> " +
                    "AbsoluteSchemaPath{path=[(http://example.com/ns/example-jukebox?revision=2014-07-03)jukebox]}", e.getMessage());
        }

        // assert Notifications
        if(notifications != null) {
            assertEquals(0, notifications.size());
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");
    }

    @Test
    public void testRemoveAlbum() throws SAXException, IOException, NetconfMessageBuilderException {
        //send editconfig to remove album
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/remove-album-circus.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> modify\n" +
                "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> delete\n" +
                "    name -> delete { previousVal = 'Circus', currentVal = 'null' }\n" +
                "    genre -> delete { previousVal = 'jbox:rock', currentVal = 'null' }\n" +
                "    year -> delete { previousVal = '1995', currentVal = 'null' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/delete-album-circus.xml");
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");
        
        //again remove album that was already removed
        result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/remove-album-circus.xml"), "1");

        response = (NetConfResponse) result.get("response");
        notifications = (List<Notification>) result.get("notifications");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //assert changeTreeNode
        try {
            m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                    " library[/jukebox/library] -> modify\n" +
                    "  artist[/jukebox/library/artist[name='Lenny']] -> modify\n" +
                    "   name -> none { previousVal = 'Lenny', currentVal = 'Lenny' }\n" +
                    "   album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> delete\n" +
                    "    name -> delete { previousVal = 'Circus', currentVal = 'null' }\n" +
                    "    genre -> delete { previousVal = 'jbox:rock', currentVal = 'null' }\n" +
                    "    year -> delete { previousVal = '1995', currentVal = 'null' }\n"));
            fail("AssertCTNException should have been thrown!!!");
        } catch (AssertCTNException e) {
            assertEquals(JUKEBOX_SCHEMA_PATH, e.getSchemaPath());
            assertEquals("ChangeTreeNodes not found for specified schema path -> " +
                    "AbsoluteSchemaPath{path=[(http://example.com/ns/example-jukebox?revision=2014-07-03)jukebox]}", e.getMessage());
        }

        // assert Notifications
        if(notifications != null) {
            assertEquals(0, notifications.size());
        }

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");
    }

    @Test
    public void testRemovingLeafTwice() throws SAXException, IOException, NetconfMessageBuilderException {
        reset(m_testSubSystem);

        //send edit config to delete year
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/remove-config-year-leaf.xml"), "1");

        verify(m_testSubSystem).postCommit(anyMap());

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
                "    year -> delete { previousVal = '2000', currentVal = 'null' }\n"));

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/delete-year-in-album.xml");
        }

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-without-year.xml", "1");

        reset(m_testSubSystem);

        //again send edit config to delete year
        NetConfResponse failedResponse = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-config-year-leaf.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verify(m_testSubSystem, never()).postCommit(anyMap()); //postCommit should not be called for above edit.

        try {
            m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("dummy"));
            fail("AssertCTNException should have been thrown!!!");
        } catch (AssertCTNException e) {
            assertEquals(JUKEBOX_SCHEMA_PATH, e.getSchemaPath());
            assertEquals("ChangeTreeNodes not found for specified schema path -> " +
                    "AbsoluteSchemaPath{path=[(http://example.com/ns/example-jukebox?revision=2014-07-03)jukebox]}", e.getMessage());
        }
    }

    @Test
    public void testRemoveNonExistentLeaf() throws IOException, SAXException {
        reset(m_testSubSystem);

        //send edit config to remove non-existent year
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/remove-non-existent-year-leaf.xml"), "1");
        NetConfResponse response = (NetConfResponse) result.get("response");

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);
        verify(m_testSubSystem, never()).postCommit(anyMap()); //postCommit should not be called for above edit.

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-lenny-circus.xml", "/get-album-circus.xml", "1");
    }
}
