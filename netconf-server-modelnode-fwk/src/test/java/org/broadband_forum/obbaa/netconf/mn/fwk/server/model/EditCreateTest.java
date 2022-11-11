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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class EditCreateTest extends AbstractEditConfigTestSetup {
    private final static Logger LOGGER = LogManager.getLogger(EditCreateTest.class);

    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("unit-test", 1);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }

    @Test
    public void testAddNewAlbum() throws SAXException, IOException {
        EditConfigRequest request = new EditConfigRequest()
                                                .setTargetRunning()
                                                .setTestOption(EditConfigTestOptions.SET)
                                                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                                                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml("/create-album.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(null, "/lenny-new-album.xml");
    }
    

    @Test
    public void testCreateNewSong() throws SAXException, IOException{
        EditConfigRequest request = new EditConfigRequest()
                                            .setTargetRunning()
                                            .setTestOption(EditConfigTestOptions.SET)
                                            .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                                            .setConfigElement(new EditConfigElement()
                                                                        .addConfigElementContent(loadAsXml("/create-song.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        //assert Ok response
        assertXMLEquals("/ok-response.xml", response);
        
        //do a get-config to be sure
        verifyGetConfig(null, "/get-all-lenny-with-new-song.xml");
    }

    private void verifyGetConfig(String filterInput, String expectedOutput) {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);

        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId("1");
        request.setSource("running");
        if (filterInput != null) {
            NetconfFilter filter = new NetconfFilter();
            // we have two variants fo the select node in here
            filter.addXmlFilter(loadAsXml(filterInput));
            request.setFilter(filter);
        }

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");

        m_server.onGetConfig(client, request, response);

        try {
            TestUtil.assertXMLEquals(expectedOutput, response);
        } catch (SAXException | IOException e) {
            LOGGER.error("test comparison failed" , e);
            fail("test comparison failed" + e.getMessage());
        }
    }

}
