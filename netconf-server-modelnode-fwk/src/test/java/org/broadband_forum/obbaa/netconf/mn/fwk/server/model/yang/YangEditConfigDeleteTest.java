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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;

import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class YangEditConfigDeleteTest extends AbstractYangValidationTestSetup {

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }


    @Test
    public void testDeleteLibrary() throws SAXException, IOException {
        //send edit config to delete library
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, loadAsXml
                ("/delete-config-delete-library.xml"), "1");
        //assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");
    }

    @Test
    public void testDeleteLibraryGivesRpcError() throws SAXException, IOException {
        //send edit config to delete library
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, loadAsXml
                ("/delete-config-delete-library.xml"), "1");
        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");

        //again send edit config to delete library
        NetConfResponse failedResponse = TestUtil.sendEditConfig(m_server, m_clientInfo, loadAsXml
                ("/delete-config-delete-library.xml"), "1");
        //assert not Ok response
        assertFalse(failedResponse.isOk());
        assertEquals(1, failedResponse.getErrors().size());
        NetconfRpcError rpcError = failedResponse.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());

    }

    @Test
    public void testDeleteAlbumCircus() throws SAXException, IOException {
        //send edit config to delete album Circus
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, loadAsXml("/delete-album-circus" +
                ".xml"), "1");
        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");
    }

    @Test
    public void testDeleteAlbumCircusGivesRpcError() throws SAXException, IOException {
        //send edit config to delete album Circus
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, loadAsXml("/delete-album-circus" +
                ".xml"), "1");
        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");

        //again send edit config to delete album Circus
        NetConfResponse failedResponse = TestUtil.sendEditConfig(m_server, m_clientInfo, loadAsXml
                ("/delete-album-circus.xml"), "1");
        //assert not Ok response
        assertFalse(failedResponse.isOk());
        assertEquals(1, failedResponse.getErrors().size());
        NetconfRpcError rpcError = failedResponse.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
    }
}
