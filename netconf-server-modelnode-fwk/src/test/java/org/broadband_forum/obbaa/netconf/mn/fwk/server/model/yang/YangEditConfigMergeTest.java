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
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;

import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class YangEditConfigMergeTest extends AbstractYangValidationTestSetup {

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }

    @Test
    public void testChangeAlbumYear() throws SAXException, IOException {
        //send edit config to merge album
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfig-simple" +
                ".xml"), "1");
        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-year-editconfig-expected.xml", "1");
    }

    @Test
    public void testMergeNewSong() throws SAXException, IOException {
        //send edit config to merge album
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfig-merge-songs" +
                ".xml"), "1");
        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        //do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-year-editconfig-merge-songs-expected.xml", "1");
    }

}
