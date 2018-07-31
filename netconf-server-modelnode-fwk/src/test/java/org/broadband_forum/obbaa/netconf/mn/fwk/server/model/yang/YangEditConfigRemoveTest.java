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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;

public class YangEditConfigRemoveTest extends AbstractYangValidationTestSetup {

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }

    @Test
    public void testRemoveLibrary() throws SAXException, IOException {
        //send editconfig to remove libarary
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-config-delete-library" +
                ".xml"), "1");

        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");

        //again send editconfig to remove library that already removed
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-config-delete-library.xml"), "1");

        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");
    }

    @Test
    public void testRemoveAlbum() throws SAXException, IOException {
        //send editconfig to remove libarary
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-album-circus.xml"), "1");

        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");

        //again remove library that already removed
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-album-circus.xml"), "1");
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");
    }

}
