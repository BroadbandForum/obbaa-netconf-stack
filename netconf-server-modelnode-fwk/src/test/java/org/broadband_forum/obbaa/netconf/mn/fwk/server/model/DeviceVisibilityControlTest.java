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
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractYangValidationTestSetup.XML_PATH;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.getByteSources;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfigV2;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractYangValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class DeviceVisibilityControlTest extends AbstractYangValidationTestSetup {

    public static final String RESPONSE = "response";

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }

    @Test
    public void testCreateInvisibleAlbum() throws SAXException, IOException {

        //send edit config to create invisible album
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-invisible-album.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that invisible album is not returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");
    }

    @Test
    public void testCreateNormalAlbumBySpecifyingVisibilityAsTrue() throws SAXException, IOException {

        //send edit config to create visible album by explicitly specifying visibility as true
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-visible-album-with-visibility-true.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that visible album is returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/devicevisibilitycontroltest/jukebox-datastore-with-visible-album.xml", "1");
    }

    @Test
    public void testRPCErrorThrownWhenInvalidVisibilityValueIsSpecified() throws SAXException, IOException {

        //send edit config by specifying invalid value for visibility
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-with-invalid-visibility-value.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert RPC Error reply
        assertXMLEquals("/devicevisibilitycontroltest/bad-visibility-attribute-error-reply.xml", response);
    }

    @Test
    public void testDeleteInvisibleAlbumBySpecifyingVisibilityAsFalse() throws SAXException, IOException {

        //send edit config to create invisible album
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-invisible-album.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that invisible album is not returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");

        //send edit config to delete invisible album by specifying visibility as false
        result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-delete-invisible-album-with-visibility-false.xml"), "1");

        response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response to make sure it was deleted
        assertXMLEquals("/ok-response.xml", response);
    }

    @Test
    public void testDeleteInvisibleAlbumBySpecifyingVisibilityAsTrue() throws SAXException, IOException {

        //send edit config to create invisible album
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-invisible-album.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that invisible album is not returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");

        //send edit config to delete invisible album by specifying visibility as true
        result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-delete-invisible-album-with-visibility-true.xml"), "1");

        response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response to make sure it was deleted
        assertXMLEquals("/ok-response.xml", response);
    }

    @Test
    public void testDeleteNormalAlbumBySpecifyingVisibilityAsFalse() throws SAXException, IOException {

        //send edit config to create visible album
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-visible-album-with-visibility-true.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that visible album is returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/devicevisibilitycontroltest/jukebox-datastore-with-visible-album.xml", "1");

        //send edit config to delete visible album by specifying visibility as false
        result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-delete-visible-album-with-visibility-false.xml"), "1");

        response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response to make sure it was deleted
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that visible album is not returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");
    }

    @Test
    public void testDeleteInvisibleAlbumWithoutSpecifyingVisibility() throws SAXException, IOException {

        //send edit config to create invisible album
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-invisible-album.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that invisible album is not returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");

        //send edit config to delete invisible album without specifying visibility
        result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-delete-invisible-album-without-visibility-value.xml"), "1");

        response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response to make sure it was deleted
        assertXMLEquals("/ok-response.xml", response);
    }

    @Test
    public void testCreateVisibleAndInvisibleAlbumsInSingleEditConfig() throws SAXException, IOException {

        //send edit config to create visible album and invisible album in single edit-config
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-invisible-album-and-visible-album.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response to make sure both are created
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that only visible album is returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/devicevisibilitycontroltest/jukebox-datastore-with-visible-album.xml", "1");
    }

    @Test
    public void testDeleteVisibleAndInvisibleAlbumsInSingleEditConfig() throws SAXException, IOException {

        //send edit config to create visible album and invisible album in single edit-config
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-invisible-album-and-visible-album.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that visible album is returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/devicevisibilitycontroltest/jukebox-datastore-with-visible-album.xml", "1");

        //send edit config to delete visible album and invisible album in single edit config
        result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-delete-invisible-album-and-visible-album.xml"), "1");

        response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response to make sure both are deleted
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure that neither invisible album nor visible album is returned
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-datastore.xml", "1");
    }

    @Test
    public void testGetVisibleAndInvisibleAlbumsInSingleGetConfig() throws SAXException, IOException {

        //send edit config to create visible album and invisible album in single edit-config
        Map<String, Object> result = sendEditConfigV2(m_server, m_clientInfo, loadAsXml("/devicevisibilitycontroltest/editconfig-create-invisible-album-and-visible-album.xml"), "1");

        NetConfResponse response = (NetConfResponse) result.get(RESPONSE);

        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //send get-config to get song from both visible and invisible album
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/devicevisibilitycontroltest/get-visible-and-invisible-album-data.xml", "/devicevisibilitycontroltest/song-from-visible-album.xml", "1");
    }
}
