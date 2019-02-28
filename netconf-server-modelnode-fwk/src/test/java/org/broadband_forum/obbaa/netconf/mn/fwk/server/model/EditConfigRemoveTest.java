package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class EditConfigRemoveTest extends AbstractEditConfigTestSetup{

    private final static Logger LOGGER = Logger.getLogger(EditConfigRemoveTest.class);

    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("unit-test", 1);

    @Before
    public void initServer() throws SchemaBuildException{
      super.setup();
    }

    @Test
    public void testRemoveLibraryGivesOkResponse() {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/remove-config-delete-library.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(null, "/empty-jukebox.xml");
        
        //do one more remove and make sure you get a ok response
        response = new NetConfResponse();
        m_server.onEditConfig(m_clientInfo, request, response);
        response.setMessageId("1");
        
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(null, "/empty-jukebox.xml");
    }

    @Test
    public void testRemoveAlbumCircusGivesOkResponse() {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/remove-album-circus.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(null, "/without-circus.xml");
        
        //do one more remove and make sure you get a ok response
        response = new NetConfResponse();
        m_server.onEditConfig(m_clientInfo, request, response);
        response.setMessageId("1");
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(null, "/without-circus.xml");
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
