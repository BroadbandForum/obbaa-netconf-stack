package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;

public class EditConfigReplaceTest extends AbstractEditConfigTestSetup {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = Logger.getLogger(EditConfigReplaceTest.class);

    @Before
    public void initServer() throws SchemaBuildException {
       super.setup();
    }

    @Test
    public void testReplceWhenThereisNoLibraryWorks() throws SAXException, IOException {
        
        //remove the library first
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
		assertXMLEquals("/ok-response.xml", response);

		// do a get-config to be sure
        verifyGetConfig(null, "/empty-jukebox.xml");
        
        //do replace and make sure you get a ok response
        request = new EditConfigRequest()
                            .setTargetRunning()
                            .setTestOption(EditConfigTestOptions.SET)
                            .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                            .setConfigElement(new EditConfigElement()
                                    .addConfigElementContent(loadAsXml("/replace-config-library.xml")));
        request.setMessageId("1");
        response = new NetConfResponse();
        m_server.onEditConfig(m_clientInfo, request, response);
        response.setMessageId("1");
        
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(null, "/depeche-mode-jukebox.xml");
    }

    @Test
    public void testReplceWhenThereisLibraryWorks() throws SAXException, IOException {
        // do replace and make sure you get a ok response
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/replace-config-library.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
         m_server.onEditConfig(m_clientInfo, request, response);
        response.setMessageId("1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(null, "/depeche-mode-jukebox.xml");
    }
    
    private void verifyGetConfig(String filterInput, String expectedOutput) throws SAXException, IOException {
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

        assertXMLEquals(expectedOutput, response);
    }
    
    @Test
    public void testReplceWhenThereisNoAlbumWorks() throws SAXException, IOException {
        
        //remove the library first
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
        assertXMLEquals("/ok-response.xml", response);

        
        //do replace and make sure you get a ok response
        request = new EditConfigRequest()
                            .setTargetRunning()
                            .setTestOption(EditConfigTestOptions.SET)
                            .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                            .setConfigElement(new EditConfigElement()
                                    .addConfigElementContent(loadAsXml("/replace-config-album-circus.xml")));
        request.setMessageId("1");
        response = new NetConfResponse();
        m_server.onEditConfig(m_clientInfo, request, response);
        response.setMessageId("1");
        
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(null, "/lennny-circus-replaced.xml");
    }

    @Test
    public void testReplceWhenThereisAlbumWorks() throws SAXException, IOException {
        // do replace and make sure you get a ok response
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/replace-config-album-circus.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
        m_server.onEditConfig(m_clientInfo, request, response);
        response.setMessageId("1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(null, "/lennny-circus-replaced.xml");
    }

    @Test
    public void testReplaceOnRootNode() throws IOException, SAXException {
        verifyGetConfig(null, "/getconfig-response.xml");
        // do replace and make sure you get a ok response
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setDefaultOperation(EditConfigDefaultOperations.REPLACE)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml("/replace-config-jukebox.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
        m_server.onEditConfig(m_clientInfo, request, response);
        response.setMessageId("1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(null, "/replace-jukebox-response.xml");
    }
}
