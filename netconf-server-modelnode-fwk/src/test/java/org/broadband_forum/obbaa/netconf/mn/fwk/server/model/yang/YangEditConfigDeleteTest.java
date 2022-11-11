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
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class YangEditConfigDeleteTest extends AbstractYangValidationTestSetup{

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
       super.setup();
	}


	@Test
	public void testDeleteLibrary() throws SAXException, IOException, NetconfMessageBuilderException {

        //send edit config to delete library
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/delete-config-delete-library.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
		assertXMLEquals("/ok-response.xml", response);

        // assert Notifications
        if(notifications != null) {
            assertEquals(1, notifications.size());
            verifyNotification((NetconfConfigChangeNotification) notifications.get(0), "/notifications/delete-library.xml");
        }

        //assert changeTreeNode
        m_testSubSystem.assertChangeTreeNodeForGivenSchemaPath(JUKEBOX_SCHEMA_PATH, Collections.singletonList("jukebox[/jukebox] -> modify\n" +
                " library[/jukebox/library] -> delete\n"));

		//do a get-config to be sure
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");
	}

    @Test
    public void testDeleteLibraryGivesRpcError() throws SAXException, IOException{
    	//send edit config to delete library
    	NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/delete-config-delete-library.xml"), "1");
        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);
        
        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");
        
        //again send edit config to delete library
        NetConfResponse failedResponse = sendEditConfig(m_server, m_clientInfo, loadAsXml("/delete-config-delete-library.xml"), "1");
        //assert not Ok response
        assertFalse(failedResponse.isOk());
        assertEquals(1, failedResponse.getErrors().size());
        NetconfRpcError rpcError = failedResponse.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        
    }

    @Test
    public void testDeletingAlreadyDeletedLeafGivesRpcError() throws SAXException, IOException, NetconfMessageBuilderException {
        //send edit config to delete Year
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/delete-config-year-leaf.xml"), "1");

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

        //again send edit config to delete Year
        NetConfResponse failedResponse = sendEditConfig(m_server, m_clientInfo, loadAsXml("/delete-config-year-leaf.xml"), "1");

         //assert not Ok response
         assertFalse(failedResponse.isOk());
         assertEquals(1, failedResponse.getErrors().size());
         NetconfRpcError rpcError = failedResponse.getErrors().get(0);
         assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
         assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
         assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
         assertEquals("Data does not exist year", rpcError.getErrorMessage());
    }

    @Test
    public void testDeletingAlreadyDeletedLeafBySpecifyingValueGivesRpcError() throws SAXException, IOException, NetconfMessageBuilderException {
        //send edit config to delete Year
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/delete-config-year-leaf-withValue.xml"), "1");

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

        //again send edit config to delete Year
        NetConfResponse failedResponse = sendEditConfig(m_server, m_clientInfo, loadAsXml("/delete-config-year-leaf-withValue.xml"), "1");

        //assert not Ok response
        assertFalse(failedResponse.isOk());
        assertEquals(1, failedResponse.getErrors().size());
        NetconfRpcError rpcError = failedResponse.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
    }

    @Test
	public void testDeleteAlbumCircus() throws SAXException, IOException, NetconfMessageBuilderException {
		//send edit config to delete album Circus
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/delete-album-circus.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get("response");
        List<Notification> notifications = (List<Notification>) result.get("notifications");

        //assert Ok response
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

		//do a get-config to be sure
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");
	}
	
	@Test
    public void testDeleteAlbumCircusGivesRpcError() throws SAXException, IOException{
		//send edit config to delete album Circus
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/delete-album-circus.xml"), "1");
        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);
        
        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");
        
        //again send edit config to delete album Circus
        NetConfResponse failedResponse = sendEditConfig(m_server, m_clientInfo, loadAsXml("/delete-album-circus.xml"), "1");
        //assert not Ok response
        assertFalse(failedResponse.isOk());
        assertEquals(1, failedResponse.getErrors().size());
        NetconfRpcError rpcError = failedResponse.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
    }
}
